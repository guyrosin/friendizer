package com.teamagly.friendizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;

import com.google.android.c2dm.server.PMF;

@SuppressWarnings("serial")
public class MarketManager extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "register")
			register(request, response);
		else if (servlet.intern() == "userDetails")
			userDetails(request, response);
		else if (servlet.intern() == "ownList")
			ownList(request, response);
		else
			buy(request, response);
	}

	@SuppressWarnings("unchecked")
	private void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PrintWriter out = response.getWriter();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (!result.isEmpty()) {
			out.println("This user already exists");
			return;
		}
		User user = new User(userID, 0, 100, 1000, -1, -1, new Date());
		pm.makePersistent(user);
		pm.close();
		out.println("The user was added");
	}

	@SuppressWarnings("unchecked")
	private void userDetails(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PrintWriter out = response.getWriter();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty()) {
			out.println("This user doesn't exist");
			return;
		}
		User user = result.get(0);
		pm.close();
		out.println(user);
	}

	@SuppressWarnings("unchecked")
	private void ownList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PrintWriter out = response.getWriter();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty()) {
			out.println("This user doesn't exist");
			return;
		}
		query = pm.newQuery(User.class);
		query.setFilter("owner == " + userID);
		result = (List<User>) query.execute();
		query.closeAll();
		JSONArray users = new JSONArray();
		for (User user : result)
			users.put(user.toJSONObject());
		pm.close();
		out.println(users);
	}

	@SuppressWarnings("unchecked")
	private void buy(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long buyID = Long.parseLong(request.getParameter("buyID"));
		PrintWriter out = response.getWriter();
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
		if (buyer == null) {
			out.println("This user doesn't exist");
			return;
		}
		if (buy == null) {
			out.println("The user you want to buy doesn't exist");
			return;
		}
		if (buy.getOwner() == userID) {
			out.println("You already own the user you want to buy");
			return;
		}
		if (buyer.getMoney() < buy.getValue()) {
			out.println("You don't have enough money to buy this user");
			return;
		}
		buyer.setMoney(buyer.getMoney() - buy.getValue());
		pm.makePersistent(buyer);
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
		pm.close();
		out.println("Purchase Done");
	}
}
