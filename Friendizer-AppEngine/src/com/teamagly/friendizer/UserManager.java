package com.teamagly.friendizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookException;
import com.restfb.json.JsonObject;
import com.restfb.util.StringUtils;
import com.teamagly.friendizer.model.Like;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.UserDevice;
import com.teamagly.friendizer.model.UserMatching;

@SuppressWarnings("serial")
public class UserManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "userDetails")
			userDetails(request, response);
		else if (servlet.intern() == "ownList")
			ownList(request, response);
		else if (servlet.intern() == "updateStatus")
			updateStatus(request, response);
		else if (servlet.intern() == "matching")
			matching(request, response);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String address = req.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "login")
			login(req, resp);
	}

	@SuppressWarnings("unchecked")
	private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("UTF-8");
		String regID = request.getParameter(Util.REG_ID);
		long userID = Long.parseLong(request.getParameter(Util.USER_ID));
		String accessToken = request.getParameter(Util.ACCESS_TOKEN);
		User user;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			user = pm.getObjectById(User.class, userID);
			// Update the new access token of the user
			user.setToken(accessToken);
			// Update the current date
			user.setSince(new Date());
			pm.makePersistent(user);

			if (user.isFbUpdate()) // Check if there's a Facebook data update for this user
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
			// Create a new user with the given ID and access token
			user = new User(userID, accessToken);
			updateUserFromFacebook(user);
			pm.makePersistent(user);
		}

		pm.makePersistent(user);
		response.getWriter().println(new Gson().toJson(user));

		// Create/update the device registration
		if (regID != null && regID.length() > 0) {
			Query query = pm.newQuery(UserDevice.class);
			query.setFilter("regID == regIDParam");
			query.declareParameters("String regIDParam");
			List<UserDevice> devices = (List<UserDevice>) query.execute(regID);
			UserDevice device;
			if (devices.size() == 0)
				device = new UserDevice(regID, userID);
			else { // Update the user ID
				device = devices.get(0);
				device.setUserID(userID);
			}
			pm.makePersistent(device);
		} else
			log.severe("No reg ID");
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

	private void userDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("UTF-8");
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			User user = pm.getObjectById(User.class, userID);
			response.getWriter().println(new Gson().toJson(new UserMatching(user, 0)));
		} catch (JDOObjectNotFoundException e) {
			log.severe("User doesn't exist");
		} finally {
			pm.close();
		}
	}

	@SuppressWarnings("unchecked")
	private void ownList(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

	private void updateStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("UTF-8");

		long userID = Long.parseLong(request.getParameter("userID"));
		String status = request.getParameter("status");

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			User user = pm.getObjectById(User.class, userID);
			user.setStatus(status);
			pm.makePersistent(user);
		} catch (JDOObjectNotFoundException e) {
			log.severe("User doesn't exist");
		} finally {
			pm.close();
		}
	}

	private void matching(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
			Connection<Like> user1Likes = facebookClient1.fetchConnection("me/likes", Like.class);
			// Get the likes of user2
			Connection<Like> user2Likes = facebookClient2.fetchConnection("me/likes", Like.class);

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
}
