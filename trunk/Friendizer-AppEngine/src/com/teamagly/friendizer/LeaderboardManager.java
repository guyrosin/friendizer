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

import com.google.gson.Gson;
import com.teamagly.friendizer.model.User;

@SuppressWarnings("serial")
public class LeaderboardManager extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "getLeaderboard")
			getLeaderboard(request, response);
	}

	@SuppressWarnings("unchecked")
	private void getLeaderboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String type = request.getParameter("type");

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		String order = type + " desc";
		query.setOrdering(order);
		query.setRange(0, 10);

		List<User> users = (List<User>) query.execute();
		query.closeAll();
		if (!users.isEmpty())
			out.println(new Gson().toJson(users));
	}
}