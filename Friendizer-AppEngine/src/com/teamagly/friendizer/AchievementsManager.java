package com.teamagly.friendizer;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class AchievementsManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

	@SuppressWarnings("unchecked")
	public static void userBoughtSomeone(User user, ServletContext context) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == 28001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();

		Achievement achv;
		/* Get the achievement from the database */try {
			achv = pm.getObjectById(Achievement.class, 28001);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("This achievement doesn't exist");
			return;
		}

		// Reward the user with money
		user.setMoney(user.getMoney() + achv.getReward());
		// Reward the user with points
		user.setPoints(user.getPoints() + achv.getPoints());
		// Check for another achievement
		AchievementsManager.userValueIncreased(user, context);
		// check for level up
		user.setLevel(Util.calculateLevel(user.getLevel(), user.getPoints()));

		if (result.isEmpty()) {
			pm.makePersistent(new UserAchievement(user.getId(), 28001));
			notificate(user, 28001, context);
		}
		pm.close();
	}

	@SuppressWarnings("unchecked")
	public static void someoneBoughtUser(User user, ServletContext context) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == 30001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();

		Achievement achv;
		/* Get the achievement from the database */
		try {
			achv = pm.getObjectById(Achievement.class, 30001);
		} catch (JDOObjectNotFoundException e) {
			log.severe("This achievement doesn't exist");
			return;
		} finally {
			pm.close();
		}

		// Reward the user with money
		user.setMoney(user.getMoney() + achv.getReward());
		// Reward the user with points
		user.setPoints(user.getPoints() + achv.getPoints());

		// Check for another achievement
		AchievementsManager.userValueIncreased(user, context);
		// check for level up
		user.setLevel(Util.calculateLevel(user.getLevel(), user.getPoints()));

		if (result.isEmpty()) {
			pm = PMF.get().getPersistenceManager();
			pm.makePersistent(new UserAchievement(user.getId(), 30001));
			pm.close();
			notificate(user, 30001, context);
		}
	}

	@SuppressWarnings("unchecked")
	public static void userValueIncreased(User user, ServletContext context) {
		if (user.getPoints() >= 1000) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Query query = pm.newQuery(UserAchievement.class);
			query.setFilter("userID == " + user.getId() + " && achievementID == 29001");
			List<UserAchievement> result = (List<UserAchievement>) query.execute();
			query.closeAll();

			Achievement achv;
			/* Get the achievement from the database */try {
				achv = pm.getObjectById(Achievement.class, 29001);
			} catch (JDOObjectNotFoundException e) {
				log.severe("This achievement doesn't exist");
				return;
			} finally {
				pm.close();
			}

			// Reward the user with money
			user.setMoney(user.getMoney() + achv.getReward());
			// Reward the user with points
			user.setPoints(user.getPoints() + achv.getPoints());
			// check for level up
			user.setLevel(Util.calculateLevel(user.getLevel(), user.getPoints()));

			if (result.isEmpty()) {
				pm = PMF.get().getPersistenceManager();
				pm.makePersistent(new UserAchievement(user.getId(), 29001));
				notificate(user, 29001, context);
				pm.close();
			}
		}
	}

	private static void notificate(User user, int achievementID, ServletContext context) {
		Achievement achievement;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			achievement = pm.getObjectById(Achievement.class, new Long(achievementID));
		} catch (JDOObjectNotFoundException e) {
			log.severe("Achievement was not found");
			return;
		} finally {
			pm.close();
		}

		Builder msg = new Builder();
		msg.addData("type", NotificationType.ACH.toString());
		msg.addData("userID", String.valueOf(user.getId()));
		msg.addData("title", achievement.getTitle());
		msg.addData("iconRes", achievement.getIconRes());
		SendMessage.sendMessage(user.getId(), msg.build());
	}
}
