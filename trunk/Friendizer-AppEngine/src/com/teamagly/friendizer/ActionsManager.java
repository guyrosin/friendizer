package com.teamagly.friendizer;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import com.google.android.c2dm.server.PMF;
import com.teamagly.friendizer.model.Action;
import com.teamagly.friendizer.model.User;

@SuppressWarnings("serial")
public class ActionsManager extends HttpServlet {
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
		query = pm.newQuery(Action.class);
		query.setFilter("buyerID == " + userID);
		query.setOrdering("date desc");
		List<Action> result1 = (List<Action>) query.execute();
		query.closeAll();
		query = pm.newQuery(Action.class);
		query.setFilter("boughtID == " + userID);
		query.setOrdering("date desc");
		List<Action> result2 = (List<Action>) query.execute();
		query.closeAll();
		int i = 0, j = 0;
		JSONArray actionsArray = new JSONArray();
		while (true) {
			if (i < result1.size()) {
				if (j < result2.size()) {
					if (result1.get(i).getDate().after(result2.get(j).getDate())) {
						actionsArray.put(result1.get(i));
						i++;
					} else {
						actionsArray.put(result2.get(j));
						j++;
					}
				} else {
					actionsArray.put(result1.get(i));
					i++;
				}
			} else if (j < result2.size()) {
				actionsArray.put(result2.get(j));
				j++;
			} else
				break;
		}
		pm.close();
		response.getWriter().println(actionsArray);
	}
	
	public static void madeBuy(long buyerID, long boughtID) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(new Action(buyerID, boughtID, new Date()));
		pm.close();
	}
}
