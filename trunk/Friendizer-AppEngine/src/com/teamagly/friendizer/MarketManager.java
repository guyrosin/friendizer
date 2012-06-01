package com.teamagly.friendizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;

import com.google.android.c2dm.server.PMF;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.UserMatching;

@SuppressWarnings("serial")
public class MarketManager extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		System.err.println(servlet.intern());
		if (servlet.intern() == "login")
			login(request, response);
		else if (servlet.intern() == "userDetails")
			userDetails(request, response);
		else if (servlet.intern() == "ownList")
			ownList(request, response);
		else if (servlet.intern() == "buy")
			buy(request, response);
		else if (servlet.intern() == "updateStatus")
			updateStatus(request, response);
	}
	
	@SuppressWarnings("unchecked")
	private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		String accesToken = request.getParameter("accessToken");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		User user;
		if (result.isEmpty()) {
			user = new User(userID);
			pm.makePersistent(user);
		} else
			user = result.get(0);
		pm.close();
		response.getWriter().println(new UserMatching(user, 1));
	}

	@SuppressWarnings("unchecked")
	private void userDetails(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			throw new ServletException("This user doesn't exist");
		User user = result.get(0);
		pm.close();
		response.getWriter().println(new UserMatching(user, 1));
	}

	@SuppressWarnings("unchecked")
	private void ownList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			throw new ServletException("This user doesn't exist");
		query = pm.newQuery(User.class);
		query.setFilter("owner == " + userID);
		result = (List<User>) query.execute();
		query.closeAll();
		JSONArray users = new JSONArray();
		for (User user : result)
			users.put(user.toJSONObject());
		pm.close();
		response.getWriter().println(users);
	}

	@SuppressWarnings("unchecked")
	private void buy(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long buyID = Long.parseLong(request.getParameter("buyID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID + " || id == " + buyID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		User buyer = null, buy = null;
		for (User user : result) {
			if (user.getId() == userID)
				buyer = user;
			else
				buy = user;
		}
		if (buyer == null)
			throw new ServletException("This user doesn't exist");
		if (buy == null)
			throw new ServletException("The user you want to buy doesn't exist");
		if (buy.getOwner() == userID)
			throw new ServletException("You already own the user you want to buy");
		if (buyer.getMoney() < buy.getValue())
			throw new ServletException("You don't have enough money to buy this user");
		buyer.setMoney(buyer.getMoney() - buy.getValue());
		pm.makePersistent(buyer);
		AchievementsManager.userBoughtSomeone(buyer);
		if (buy.getOwner() > 0) {
			query = pm.newQuery(User.class);
			query.setFilter("id == " + buy.getOwner());
			result = (List<User>) query.execute();
			query.closeAll();
			if (!result.isEmpty()) {
				User preOwner = result.get(0);
				preOwner.setMoney(preOwner.getMoney() + buy.getValue());
				pm.makePersistent(preOwner);
			}
		}
		buy.setValue(buy.getValue() * 11 / 10);
		buy.setOwner(userID);
		pm.makePersistent(buy);
		AchievementsManager.userValueIncreased(buy);
		AchievementsManager.someoneBoughtUser(buy);
		pm.close();
		response.getWriter().println("Purchase Done");
	}

	@SuppressWarnings("unchecked")
	private void updateStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		long userID = Long.parseLong(request.getParameter("userID"));
		String status = request.getParameter("status");

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			throw new ServletException("This user doesn't exist");
		User user = result.get(0);
		user.setStatus(status);

		try {
			pm.makePersistent(user);
		} finally {
			pm.close();
		}
		out.println("Updated status: " + status);

	}

}
