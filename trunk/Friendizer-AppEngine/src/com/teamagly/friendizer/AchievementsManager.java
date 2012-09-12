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
	public static void userValueIncreased(User user) {
		if (user.getPoints() < 1000)
			return;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == 29001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		
		if (result.isEmpty()) {
			// Get the achievement from the database
			Achievement achv;
			try {
				achv = pm.getObjectById(Achievement.class, 29001);
			} catch (JDOObjectNotFoundException e) {
				pm.close();
				log.severe("This achievement doesn't exist");
				return;
			}
	
			// Reward the user with money
			user.setMoney(user.getMoney() + achv.getReward());
			// Reward the user with points
			user.setPoints(user.getPoints() + achv.getPoints());
			// check for level up
			user.setLevel(UsersManager.calculateLevel(user.getLevel(), user.getPoints()));
			pm.makePersistent(user); // Necessary since the user is detached
	
			pm.makePersistent(new UserAchievement(user.getId(), achv.getId()));
			notificate(user, achv);
		}
		
		pm.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void purchaseMade(User buyer, User buy) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + buyer.getId() + " && achievementID == 28001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		
		if (result.isEmpty()) {
			// Get the achievement from the database
			Achievement achv;
			try {
				achv = pm.getObjectById(Achievement.class, 28001);
			} catch (JDOObjectNotFoundException e) {
				pm.close();
				log.severe("This achievement doesn't exist");
				return;
			}
			
			// Reward the user with money
			buyer.setMoney(buyer.getMoney() + achv.getReward());
			// Reward the user with points
			buyer.setPoints(buyer.getPoints() + achv.getPoints());
			// check for level up
			buyer.setLevel(UsersManager.calculateLevel(buyer.getLevel(), buyer.getPoints()));
			pm.makePersistent(buyer); // Necessary since the user is detached
			
			// Check for another achievement
			userValueIncreased(buyer);
			
			pm.makePersistent(new UserAchievement(buyer.getId(), achv.getId()));
			notificate(buyer, achv);
		}
		
		query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + buy.getId() + " && achievementID == 30001");
		result = (List<UserAchievement>) query.execute();
		query.closeAll();
		
		if (result.isEmpty()) {
			Achievement achv;
			// Get the achievement from the database
			try {
				achv = pm.getObjectById(Achievement.class, 30001);
			} catch (JDOObjectNotFoundException e) {
				pm.close();
				log.severe("This achievement doesn't exist");
				return;
			}
			
			// Reward the user with money
			buy.setMoney(buy.getMoney() + achv.getReward());
			// Reward the user with points
			buy.setPoints(buy.getPoints() + achv.getPoints());
			// check for level up
			buy.setLevel(UsersManager.calculateLevel(buy.getLevel(), buy.getPoints()));
			pm.makePersistent(buy); // Necessary since the user is detached
			
			// Check for another achievement
			userValueIncreased(buy);
			
			pm.makePersistent(new UserAchievement(buy.getId(), achv.getId()));
			notificate(buy, achv);
		}
		
		pm.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void userSentGift(User user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == 66001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		
		if (result.isEmpty()) {
			// Get the achievement from the database
			Achievement achv;
			try {
				achv = pm.getObjectById(Achievement.class, 66001);
			} catch (JDOObjectNotFoundException e) {
				pm.close();
				log.severe("This achievement doesn't exist");
				return;
			}
	
			// Reward the user with money
			user.setMoney(user.getMoney() + achv.getReward());
			// Reward the user with points
			user.setPoints(user.getPoints() + achv.getPoints());
			// check for level up
			user.setLevel(UsersManager.calculateLevel(user.getLevel(), user.getPoints()));
			pm.makePersistent(user); // Necessary since the user is detached
			
			// Check for another achievement
			userValueIncreased(user);
	
			pm.makePersistent(new UserAchievement(user.getId(), achv.getId()));
			notificate(user, achv);
		}
		
		pm.close();
	}
	
	private static void notificate(User user, Achievement achv) {
		Builder msg = new Builder();
		msg.addData("type", NotificationType.ACH.toString());
		msg.addData("userID", String.valueOf(user.getId()));
		msg.addData("title", achv.getTitle());
		msg.addData("iconRes", achv.getIconRes());
		SendMessage.sendMessage(user.getId(), msg.build());
	}
}
