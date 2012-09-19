package com.teamagly.friendizer;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class AchievementsManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(AchievementsManager.class.getName());

	/**
	 * Get the achievements of a user.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Get all the achievements
		Query query = pm.newQuery(Achievement.class);
		List<Achievement> achvs = (List<Achievement>) query.execute();
		query.closeAll();
		// Get the user achievements IDs
		query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + userID);
		List<UserAchievement> userAchvs = (List<UserAchievement>) query.execute();
		query.closeAll();
		// Create a list of AchievementInfo
		List<AchievementInfo> achvInfos = new ArrayList<AchievementInfo>();
		for (Achievement achv : achvs) {
			boolean earned = false;
			// Check if the user has this achievement
			for (UserAchievement userAchv : userAchvs) {
				if (userAchv.getAchievementID() == achv.getId()) {
					earned = true;
					break;
				}
			}
			// Add the AchievementInfo
			achvInfos.add(new AchievementInfo(achv, earned));
		}
		pm.close();
		response.getWriter().println(new Gson().toJson(achvInfos));
	}

	/**
	 * Check if the user should get an achievement for a high value.
	 * 
	 * @param user
	 *            The user to check
	 */
	@SuppressWarnings("unchecked")
	public static void userValueIncreased(User user) {
		// Check if the user value has reached 1000
		if (user.getPoints() < 1000)
			return;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Get the achievement
		Query query = pm.newQuery(Achievement.class);
		query.setFilter("title == 'Value I'");
		List<Achievement> achvList = (List<Achievement>) query.execute();
		query.closeAll();
		if (achvList.isEmpty()) {
			pm.close();
			log.severe("This achievement doesn't exist");
			return;
		}
		Achievement achv = achvList.get(0);
		query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == " + achv.getId());
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		// Check if the user doesn't have this achievement yet
		if (result.isEmpty()) {
			// Reward the user with money
			user.setMoney(user.getMoney() + achv.getReward());
			// Reward the user with points
			user.setPoints(user.getPoints() + achv.getPoints());
			// Check for level up
			user.setLevel(UsersManager.calculateLevel(user.getLevel(), user.getPoints()));
			pm.makePersistent(user); // Necessary since the user is detached
			// Add the achievement to the user
			pm.makePersistent(new UserAchievement(user.getId(), achv.getId()));
			// Send notification to the user device
			notificate(user, achv);
		}
		pm.close();
	}

	/**
	 * Check if the buyer or the bought user should get an achievement for the buy action.
	 * 
	 * @param buyer
	 *            The buyer
	 * @param buy
	 *            The user bought
	 */
	@SuppressWarnings("unchecked")
	public static void purchaseMade(User buyer, User buy) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Get the achievement
		Query query = pm.newQuery(Achievement.class);
		query.setFilter("title == 'Buy I'");
		List<Achievement> achvList = (List<Achievement>) query.execute();
		query.closeAll();
		if (achvList.isEmpty()) {
			pm.close();
			log.severe("This achievement doesn't exist");
			return;
		}
		Achievement achv = achvList.get(0);
		query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + buyer.getId() + " && achievementID == " + achv.getId());
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		// Check if the buyer doesn't have this achievement yet
		if (result.isEmpty()) {
			// Reward the buyer with money
			buyer.setMoney(buyer.getMoney() + achv.getReward());
			// Reward the buyer with points
			buyer.setPoints(buyer.getPoints() + achv.getPoints());
			// Check for level up
			buyer.setLevel(UsersManager.calculateLevel(buyer.getLevel(), buyer.getPoints()));
			pm.makePersistent(buyer); // Necessary since the user is detached
			// Check for another achievement
			userValueIncreased(buyer);
			// Add the achievement to the buyer
			pm.makePersistent(new UserAchievement(buyer.getId(), achv.getId()));
			// Send notification to the buyer device
			notificate(buyer, achv);
		}

		// Get the achievement
		query = pm.newQuery(Achievement.class);
		query.setFilter("title == 'Buy II'");
		achvList = (List<Achievement>) query.execute();
		query.closeAll();
		if (achvList.isEmpty()) {
			pm.close();
			log.severe("This achievement doesn't exist");
			return;
		}
		achv = achvList.get(0);
		query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + buy.getId() + " && achievementID == " + achv.getId());
		result = (List<UserAchievement>) query.execute();
		query.closeAll();
		// Check if the user bought doesn't have this achievement yet
		if (result.isEmpty()) {
			// Reward the user bought with money
			buy.setMoney(buy.getMoney() + achv.getReward());
			// Reward the user bought with points
			buy.setPoints(buy.getPoints() + achv.getPoints());
			// Check for level up
			buy.setLevel(UsersManager.calculateLevel(buy.getLevel(), buy.getPoints()));
			pm.makePersistent(buy); // Necessary since the user is detached
			// Check for another achievement
			userValueIncreased(buy);
			// Add the achievement to the user bought
			pm.makePersistent(new UserAchievement(buy.getId(), achv.getId()));
			// Send notification to the user bought device
			notificate(buy, achv);
		}
		pm.close();
	}

	/**
	 * Check if the user should get an achievement for a sending a gift.
	 * 
	 * @param user
	 *            The user to check
	 */
	@SuppressWarnings("unchecked")
	public static void userSentGift(User user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Get the achievement
		Query query = pm.newQuery(Achievement.class);
		query.setFilter("title == 'Gift I'");
		List<Achievement> achvList = (List<Achievement>) query.execute();
		query.closeAll();
		if (achvList.isEmpty()) {
			pm.close();
			log.severe("This achievement doesn't exist");
			return;
		}
		Achievement achv = achvList.get(0);
		query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == " + achv.getId());
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		// Check if the user doesn't have this achievement yet
		if (result.isEmpty()) {
			// Reward the user with money
			user.setMoney(user.getMoney() + achv.getReward());
			// Reward the user with points
			user.setPoints(user.getPoints() + achv.getPoints());
			// Check for level up
			user.setLevel(UsersManager.calculateLevel(user.getLevel(), user.getPoints()));
			pm.makePersistent(user); // Necessary since the user is detached
			// Check for another achievement
			userValueIncreased(user);
			// Add the achievement to the user
			pm.makePersistent(new UserAchievement(user.getId(), achv.getId()));
			// Send notification to the user device
			notificate(user, achv);
		}
		pm.close();
	}

	/**
	 * Send a notification about the achievement to the user device.
	 * 
	 * @param user
	 *            The user
	 * @param achv
	 *            The achievement
	 */
	private static void notificate(User user, Achievement achv) {
		Builder msg = new Builder();
		msg.addData("type", NotificationType.ACH.toString());
		msg.addData("userID", String.valueOf(user.getId()));
		msg.addData("title", achv.getTitle());
		msg.addData("iconRes", achv.getIconRes());
		SendMessage.sendMessage(user.getId(), msg.build());
	}
}
