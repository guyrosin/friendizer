package com.teamagly.friendizer;

import java.io.IOException;
import java.util.List;

import javax.jdo.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.android.c2dm.server.PMF;

import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class AchievementsManager extends HttpServlet {
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			throw new ServletException("This user doesn't exist");
		query = pm.newQuery(Achievement.class);
		List<Achievement> achvs = (List<Achievement>) query.execute();
		query.closeAll();
		query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + userID);
		List<UserAchievement> userAchvs = (List<UserAchievement>) query.execute();
		query.closeAll();
		JSONArray achvsArray = new JSONArray();
		for (Achievement achv : achvs) {
			boolean earned = false;
			for (UserAchievement userAchv : userAchvs) {
				if (userAchv.getAchievementID() == achv.getId()) {
					earned = true;
					break;
				}
			}
			achvsArray.put(new AchievementInfo(achv, earned).toJSONObject());
		}
		pm.close();
		response.getWriter().println(achvsArray);
	}

	@SuppressWarnings("unchecked")
	public static void userBoughtSomeone(User user, ServletContext context) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == 28001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		
		/* Get the achievement from the database */
		query = pm.newQuery(Achievement.class);
		query.setFilter("id == " + 28001);
		List<Achievement> achvResult = (List<Achievement>) query.execute();
		query.closeAll();
		
		Achievement achv = achvResult.get(0);
		
		// Reward the user with money
		user.setMoney(user.getMoney() + achv.getReward());
		// Reward the user with points
		user.setPoints(user.getPoints() + achv.getPoints());
		// Check for another achievement
//		AchievementsManager.userValueIncreased(user,getServletContext()); TODO: getServletContext() not working (undo)
		// check for level up
		Util.calculateLevel(user.getLevel(), user.getPoints());
		
		if (result.isEmpty()) {
			pm.makePersistent(new UserAchievement(user.getId(), 28001));
			notificate(user,28001,context);
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
		
		/* Get the achievement from the database */
		query = pm.newQuery(Achievement.class);
		query.setFilter("id == " + 30001);
		List<Achievement> achvResult = (List<Achievement>) query.execute();
		query.closeAll();
		
		Achievement achv = achvResult.get(0);
		
		// Reward the user with money
		user.setMoney(user.getMoney() + achv.getReward());
		// Reward the user with points
		user.setPoints(user.getPoints() + achv.getPoints());
		
		// Check for another achievement
//		AchievementsManager.userValueIncreased(user,getServletContext()); TODO: getServletContext() not working (undo)
		// check for level up
		Util.calculateLevel(user.getLevel(), user.getPoints());
		
		if (result.isEmpty()) {
			pm.makePersistent(new UserAchievement(user.getId(), 30001));
			notificate(user,30001,context);
		}
		pm.close();
	}

	@SuppressWarnings("unchecked")
	public static void userValueIncreased(User user, ServletContext context) {
		if (user.getPoints() >= 1000) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Query query = pm.newQuery(UserAchievement.class);
			query.setFilter("userID == " + user.getId() + " && achievementID == 29001");
			List<UserAchievement> result = (List<UserAchievement>) query.execute();
			query.closeAll();

			/* Get the achievement from the database */
			query = pm.newQuery(Achievement.class);
			query.setFilter("id == " + 29001);
			List<Achievement> achvResult = (List<Achievement>) query.execute();
			query.closeAll();
			
			Achievement achv = achvResult.get(0);
			
			// Reward the user with money
			user.setMoney(user.getMoney() + achv.getReward());
			// Reward the user with points
			user.setPoints(user.getPoints() + achv.getPoints());
			// check for level up
			Util.calculateLevel(user.getLevel(), user.getPoints());
			
			if (result.isEmpty()) {
				pm.makePersistent(new UserAchievement(user.getId(), 29001));				
				notificate(user,29001,context);
			}
			pm.close();
		}
	}
	
	private static void notificate(User user, int achievementID, ServletContext context) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Achievement achievement =  pm.getObjectById(Achievement.class, new Long(achievementID));
		
		DeviceInfo device = DatastoreHelper.getInstance().getDeviceInfo(user.getId());
		
		AchievementNotification notification = new AchievementNotification(achievement,
				user.getId(), Notifications.ACHIEVMENT_MSG);
		try {
			SendMessage.sendMessage(context, device, notification.toC2DMMessage());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
