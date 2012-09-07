package com.teamagly.friendizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.teamagly.friendizer.model.Action;
import com.teamagly.friendizer.model.User;

@SuppressWarnings("serial")
public class ActionsManager extends HttpServlet {
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
		Query query = pm.newQuery(Action.class);
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
		ArrayList<Action> actions = new ArrayList<Action>();
		while (true) {
			if (i < result1.size()) {
				if (j < result2.size()) {
					if (result1.get(i).getDate().after(result2.get(j).getDate())) {
						actions.add(result1.get(i));
						i++;
					} else {
						actions.add(result2.get(j));
						j++;
					}
				} else {
					actions.add(result1.get(i));
					i++;
				}
			} else if (j < result2.size()) {
				actions.add(result2.get(j));
				j++;
			} else
				break;
		}
		pm.close();
		response.getWriter().println(new Gson().toJson(actions));
	}

	public static void madeBuy(long buyerID, long boughtID) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(new Action(buyerID, boughtID, new Date()));
		pm.close();
	}
}
