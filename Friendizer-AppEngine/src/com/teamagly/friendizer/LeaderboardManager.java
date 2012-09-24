package com.teamagly.friendizer;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.gson.Gson;
import com.teamagly.friendizer.model.User;

@SuppressWarnings("serial")
public class LeaderboardManager extends HttpServlet {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(LeaderboardManager.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String type = request.getParameter("type");

		// Note: doesn't work (when trying to put in a list of objects)
		// try { // Cache hit
		// List<User> users = LeaderboardCache.get(type);
		// if (users != null) {
		// log.info("Leaderboard cache hit");
		// response.getWriter().println(new Gson().toJson(users));
		// return;
		// }
		// } catch (NullPointerException e) { // Cache miss
		// }

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		String order = type + " desc";
		query.setOrdering(order);
		query.setRange(0, 10);

		List<User> users = (List<User>) query.execute();
		query.closeAll();
		if (!users.isEmpty())
			out.println(new Gson().toJson(users));

		pm.close();

		// Save the leaderboard in the cache
		// LeaderboardCache.put(type, users);
	}
}