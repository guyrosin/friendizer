package com.teamagly.friendizer;

import java.io.IOException;
import java.util.List;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;

import com.google.android.c2dm.server.PMF;

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
		response.getWriter().println(userAchvs);
	}
}
