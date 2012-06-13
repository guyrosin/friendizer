package com.teamagly.friendizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import com.google.android.c2dm.server.PMF;
import com.teamagly.friendizer.model.Message;
import com.teamagly.friendizer.model.User;


@SuppressWarnings("serial")
public class LeaderboardManager extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getRequestURI().endsWith("/valueLeaderboard"))
			getLeaderboard(request, response,"value");
		else if (request.getRequestURI().endsWith("/moneyLeaderboard"))
			getLeaderboard(request, response,"money");
	}
	
	
	private void getLeaderboard(HttpServletRequest request,
			HttpServletResponse response, String type)
					throws ServletException, IOException {
		PrintWriter out = response.getWriter();



		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(Message.class);
		String order = type + " desc";
		query.setOrdering(order);
		query.setRange(0, 10);

		try {
			@SuppressWarnings("unchecked")
			List<User> results = (List<User>) query.execute();
			if (!results.isEmpty()) {
				JSONArray users = new JSONArray();
				for (User user : results) {
					users.put(user.toJSONObject());
				}
				out.println(users);
			}
		} finally {
			query.closeAll();
		}
	}

}
