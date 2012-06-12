package com.teamagly.friendizer;

import java.io.IOException;
import java.util.List;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;

import com.google.android.c2dm.server.PMF;
import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.model.AchievementInfo;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.UserAchievement;

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
	public static void userBoughtSomeone(User user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == 28001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			pm.makePersistent(new UserAchievement(user.getId(), 28001));
		pm.close();
	}

	@SuppressWarnings("unchecked")
	public static void someoneBoughtUser(User user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == 30001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			pm.makePersistent(new UserAchievement(user.getId(), 30001));
		pm.close();
	}

	@SuppressWarnings("unchecked")
	public static void userValueIncreased(User user) {
		if (user.getValue() >= 1000) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Query query = pm.newQuery(UserAchievement.class);
			query.setFilter("userID == " + user.getId() + " && achievementID == 29001");
			List<UserAchievement> result = (List<UserAchievement>) query.execute();
			query.closeAll();
			if (result.isEmpty())
				pm.makePersistent(new UserAchievement(user.getId(), 29001));
			pm.close();
		}
	}
}
