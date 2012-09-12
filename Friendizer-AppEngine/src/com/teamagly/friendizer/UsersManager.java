package com.teamagly.friendizer;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.gson.Gson;
import com.restfb.*;
import com.restfb.exception.FacebookException;
import com.restfb.json.JsonObject;
import com.restfb.util.StringUtils;
import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class UsersManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());
	private static final int DAILY_BONUS_POINTS = 50;
	private static final int WELCOME_EMAIL_DELAY = 10 * 60 * 1000; // 10 minutes

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "userDetails")
			userDetails(request, response);
		else if (servlet.intern() == "ownList")
			ownList(request, response);
		else if (servlet.intern() == "getFriends")
			getFriends(request, response);
		else if (servlet.intern() == "updateStatus")
			updateStatus(request, response);
		else if (servlet.intern() == "matching")
			matching(request, response);
		else if (servlet.intern() == "mutualLikes")
			mutualLikes(request, response);
		else if (servlet.intern() == "dailyBonus")
			dailyBonus(request, response);
		else if (servlet.intern() == "achievements")
			achievements(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "login")
			login(request, response);
	}

	private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		String regID = request.getParameter("regID");
		long userID = Long.parseLong(request.getParameter("userID"));
		String accessToken = request.getParameter("accessToken");
		User user;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			user = pm.getObjectById(User.class, userID);
			log.info("User " + userID + " exists, with an access token");
			if (accessToken != null && accessToken.length() > 0) {
				log.info("Got a new access token");
				// Update the new access token of the user
				user.setToken(accessToken);
			}
			// Update the current date
			user.setSince(new Date());

			if (user.isFbUpdate())
				try {
					updateUserFromFacebook(user, Arrays.asList("name,gender,birthday,picture".split("\\s*,\\s*")));
					user.setFbUpdate(false);
				} catch (Exception e) {
					log.severe(e.getMessage());
				}

			// TODO: temporary, remove this later
			if (user.getPicture() == null || user.getPicture().length() == 0)
				updateUserFromFacebook(user);
		} catch (JDOObjectNotFoundException e) {
			log.info("Registering " + userID);
			// Create a new user with the given ID and access token
			user = new User(userID, accessToken);
			updateUserFromFacebook(user);
			if (accessToken == null || accessToken.length() == 0)
				user.setFbUpdate(true); // Update the user's missing details next time
			pm.makePersistent(user);

			// Send a welcome email
			Queue queue = QueueFactory.getQueue("welcome-email");
			queue.add(TaskOptions.Builder
					.withUrl("/welcome_email")
					.countdownMillis(WELCOME_EMAIL_DELAY)
					.param("userID", String.valueOf(user.getId()))
					.method(Method.GET));
		}

		response.getWriter().println(new Gson().toJson(user));

		// Create/update the device registration
		if (regID != null && regID.length() > 0) {
			UserDevice device = null;
			try {
				device = pm.getObjectById(UserDevice.class, regID);
				device.setUserID(userID); // Update the user ID
			} catch (JDOObjectNotFoundException e) {
				device = new UserDevice(regID, userID);
				pm.makePersistent(device);
			}
		} else
			log.severe("No reg ID in the request");
		pm.close();
	}

	private void updateUserFromFacebook(User user) {
		FacebookClient facebook = new DefaultFacebookClient(user.getToken());
		// Get the user's data from Facebook
		// Note: using JsonObject instead of User object for the profile picture
		JsonObject jsonObject = facebook.fetchObject(String.valueOf(user.getId()), JsonObject.class,
				Parameter.with("fields", "name,gender,birthday,picture"));
		user.updateFacebookData(jsonObject);
	}

	void updateUserFromFacebook(User user, List<String> fields) throws FacebookException {
		FacebookClient facebook = new DefaultFacebookClient(user.getToken());
		// Request those fields from Facebook
		// Note: using JsonObject instead of User object in case we want the profile picture
		log.info("Requesting update for uid " + user.getId() + " for " + StringUtils.join(fields));
		JsonObject jsonObject = facebook.fetchObject(String.valueOf(user.getId()), JsonObject.class,
				Parameter.with("fields", StringUtils.join(fields)));
		user.updateFacebookData(jsonObject);
	}

	private void userDetails(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			User user = pm.getObjectById(User.class, userID);
			response.getWriter().println(new Gson().toJson(user));
		} catch (JDOObjectNotFoundException e) {
			log.severe("User doesn't exist");
		} finally {
			pm.close();
		}
	}

	@SuppressWarnings("unchecked")
	private void ownList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.getObjectById(User.class, userID); // Check if the user exists
			Query query = pm.newQuery(User.class);
			query.setFilter("owner == " + userID);
			List<User> result = (List<User>) query.execute();
			result.size(); // Important: this is an App Engine bug workaround
			query.closeAll();
			response.getWriter().println(new Gson().toJson(result));
		} catch (JDOObjectNotFoundException e) {
			log.severe("User doesn't exist");
		}
		pm.close();
	}

	@SuppressWarnings("unchecked")
	private void getFriends(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("UTF-8");
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.getObjectById(User.class, userID); // Check if the user exists
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		HashSet<String> names = new HashSet<String>();
		Query query = pm.newQuery(Action.class);
		query.setFilter("buyerID == " + userID);
		List<Action> result = (List<Action>) query.execute();
		query.closeAll();
		List<User> friends = new ArrayList<User>();
		for (Action action : result) {
			long friendID = action.getBoughtID() != userID ? action.getBoughtID() : action.getBuyerID();
			try {
				User friend = pm.getObjectById(User.class, friendID);
				if (!names.contains(friend.getName())) {
					friends.add(friend);
					names.add(friend.getName());
				}
			} catch (JDOObjectNotFoundException e) {
				log.severe("User " + friendID + " doesn't exist");
			}
		}
		query = pm.newQuery(Action.class);
		query.setFilter("boughtID == " + userID);
		result = (List<Action>) query.execute();
		query.closeAll();
		for (Action action : result) {
			long friendID = action.getBoughtID() != userID ? action.getBoughtID() : action.getBuyerID();
			try {
				User friend = pm.getObjectById(User.class, friendID);
				if (!names.contains(friend.getName())) {
					friends.add(friend);
					names.add(friend.getName());
				}
			} catch (JDOObjectNotFoundException e) {
				log.severe("User " + friendID + " doesn't exist");
			}
		}

		query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + userID);
		List<UserBlock> blockList = (List<UserBlock>) query.execute();
		query.closeAll();
		for (UserBlock userBlock : blockList) {
			int i = 0;
			for (User friend : friends) {
				if (friend.getId() == userBlock.getBlockedID())
					break;
				i++;
			}
			if (i < friends.size())
				friends.remove(i);
		}

		response.getWriter().println(new Gson().toJson(friends));
		pm.close();
	}

	private void updateStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");

		long userID = Long.parseLong(request.getParameter("userID"));
		String status = request.getParameter("status");

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			User user = pm.getObjectById(User.class, userID);
			user.setStatus(status);
		} catch (JDOObjectNotFoundException e) {
			log.severe("User doesn't exist");
		} finally {
			pm.close();
		}
	}

	private void matching(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long user1ID = Long.parseLong(request.getParameter("userID1"));
		long user2ID = Long.parseLong(request.getParameter("userID2"));

		try { // Cache hit
			long matching = MatchingCache.get(user1ID, user2ID);
			log.info("Matching hit! between " + user1ID + " and " + user2ID);
			response.getWriter().println(matching);
			return;
		} catch (NullPointerException e) { // Cache miss
		}

		PersistenceManager pm = PMF.get().getPersistenceManager();

		/* Get the details of user1 from the database */
		User user1;
		try {
			user1 = pm.getObjectById(User.class, user1ID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User1 doesn't exist");
			return;
		}
		/* Get the details of user2 from the database */
		User user2;
		try {
			user2 = pm.getObjectById(User.class, user2ID);
		} catch (JDOObjectNotFoundException e) {
			log.severe("User2 doesn't exist");
			return;
		} finally {
			pm.close();
		}

		try {
			// Get the access token of user1
			FacebookClient facebookClient1 = new DefaultFacebookClient(user1.getToken());
			// Get the access token of user2
			FacebookClient facebookClient2 = new DefaultFacebookClient(user2.getToken());

			// Get the likes of user1
			Connection<Like> user1Likes = facebookClient1.fetchConnection("me/likes", Like.class, Parameter.with("fields", "id"));
			// Get the likes of user2
			Connection<Like> user2Likes = facebookClient2.fetchConnection("me/likes", Like.class, Parameter.with("fields", "id"));

			if (user1Likes == null || user2Likes == null || user1Likes.getData() == null || user2Likes.getData() == null)
				return;
			List<Like> user1LikesList = user1Likes.getData();
			List<Like> user2LikesList = user2Likes.getData();

			// Get the number of likes of the first user
			int likesNumber1 = user1LikesList.size();
			// Get the number of likes of the second user
			int likesNumber2 = user2LikesList.size();

			/*
			 * If there is no likes for one of the users - the matching is 0
			 */
			if ((likesNumber1 == 0) || (likesNumber2 == 0)) {
				response.getWriter().println(0);
				return;
			}

			double commonLikes = 0;

			/*
			 * The loops goes over the likes of the users and counts common likes
			 */
			for (int index1 = 0; index1 < likesNumber1; index1++) {
				String like1ID = user1LikesList.get(index1).getId();
				for (int index2 = 0; index2 < likesNumber2; index2++)
					// If both users have the same like - increase the counter
					if (like1ID.equals(user2LikesList.get(index2).getId())) {
						commonLikes++;
						break;
					}
			}

			// Determine the factor of the formula
			double factor = 150;

			// Calculate the matching according to the formula
			double formula = Math.sqrt(commonLikes / likesNumber1);
			formula = formula * factor;

			int matching = new Double(formula).intValue();

			// Save the matching in the cache
			MatchingCache.put(user1.getId(), user2.getId(), matching);

			response.getWriter().println(matching);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}

	private void mutualLikes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");

		long user1ID = Long.parseLong(request.getParameter("userID1"));
		long user2ID = Long.parseLong(request.getParameter("userID2"));

		PersistenceManager pm = PMF.get().getPersistenceManager();

		/* Get the details of user1 from the database */
		User user1;
		try {
			user1 = pm.getObjectById(User.class, user1ID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User1 doesn't exist");
			return;
		}
		/* Get the details of user2 from the database */
		User user2;
		try {
			user2 = pm.getObjectById(User.class, user2ID);
		} catch (JDOObjectNotFoundException e) {
			log.severe("User2 doesn't exist");
			return;
		} finally {
			pm.close();
		}
		FacebookClient facebookClient1 = null;
		Connection<Like> user1Likes = null;
		Connection<Like> user2Likes = null;
		try {
			// Get the access token of user1
			facebookClient1 = new DefaultFacebookClient(user1.getToken());
			// Get the access token of user2
			FacebookClient facebookClient2 = new DefaultFacebookClient(user2.getToken());

			// Get the likes of user1
			user1Likes = facebookClient1.fetchConnection("me/likes", Like.class, Parameter.with("fields", "id"));
			// Get the likes of user2
			user2Likes = facebookClient2.fetchConnection("me/likes", Like.class, Parameter.with("fields", "id"));
		} catch (FacebookException e) {
			log.severe(e.getMessage());
		}
		if (user1Likes == null || user2Likes == null || user1Likes.getData() == null || user2Likes.getData() == null)
			return;

		List<Like> user1LikesList = user1Likes.getData();
		List<Like> user2LikesList = user2Likes.getData();
		HashSet<String> mutualLikesIDs = new HashSet<String>();

		/*
		 * The loops goes over the likes of the users and counts common likes
		 */
		for (Like like1 : user1LikesList) {
			String like1ID = like1.getId();
			for (Like like2 : user2LikesList)
				// If both users have the same like - add to the set
				if (like1ID.equals(like2.getId())) {
					mutualLikesIDs.add(like1ID);
					break;
				}
		}
		String mutualLikesStr = StringUtils.join(mutualLikesIDs.toArray(new String[mutualLikesIDs.size()]));
		String query = "SELECT page_id, pic_square, name, type FROM page WHERE page_id IN (" + mutualLikesStr + ")";
		List<Page> likes = facebookClient1.executeQuery(query, Page.class);

		response.getWriter().println(new Gson().toJson(likes));
	}

	@SuppressWarnings("unchecked")
	private void dailyBonus(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		Date yesterdayDate = cal.getTime();
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(User.class);
		query.setFilter("since > yesterdayDate");
		query.declareParameters("java.util.Date yesterdayDate");
		List<User> users = (List<User>) query.execute(yesterdayDate);
		query.closeAll();

		for (User user : users)
			try {
				user.setMoney(user.getMoney() + DAILY_BONUS_POINTS);
			} catch (Exception e) {
				log.info(e.getMessage());
			}

		pm.close();
	}

	@SuppressWarnings("unchecked")
	private void achievements(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.getObjectById(User.class, userID); // Check if the user exists
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		Query query = pm.newQuery(Achievement.class);
		List<Achievement> achvs = (List<Achievement>) query.execute();
		query.closeAll();
		query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + userID);
		List<UserAchievement> userAchvs = (List<UserAchievement>) query.execute();
		query.closeAll();
		List<AchievementInfo> achvInfos = new ArrayList<AchievementInfo>();
		for (Achievement achv : achvs) {
			boolean earned = false;
			for (UserAchievement userAchv : userAchvs)
				if (userAchv.getAchievementID() == achv.getId()) {
					earned = true;
					break;
				}
			achvInfos.add(new AchievementInfo(achv, earned));
		}
		pm.close();
		response.getWriter().println(new Gson().toJson(achvInfos));
	}
}
