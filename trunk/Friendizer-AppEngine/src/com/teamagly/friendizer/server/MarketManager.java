package com.teamagly.friendizer.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

@SuppressWarnings("serial")
public class MarketManager extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getRequestURI().endsWith("/register"))
			register(request, response);
		else if (request.getRequestURI().endsWith("/userDetails"))
			userDetails(request, response);
		else if (request.getRequestURI().endsWith("/ownList"))
			ownList(request, response);
		else
			buy(request, response);
	}

	private void register(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		DatastoreService dataStore = DatastoreServiceFactory
				.getDatastoreService();
		Query query = new Query("User");
		query.addFilter("id", FilterOperator.EQUAL, userID);
		PreparedQuery result = dataStore.prepare(query);
		if (result.countEntities(FetchOptions.Builder.withLimit(1)) > 0) {
			response.getWriter().println("User already exists");
			return;
		}
		Entity user = new Entity("User");
		user.setProperty("id", userID);
		user.setProperty("value", 100L);
		user.setProperty("money", 1000L);
		user.setProperty("owner", 0L);
		dataStore.put(user);
		response.getWriter().println("User added");
	}

	private void userDetails(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PrintWriter out = response.getWriter();
		DatastoreService dataStore = DatastoreServiceFactory
				.getDatastoreService();
		Query query = new Query("User");
		query.addFilter("id", FilterOperator.EQUAL, userID);
		PreparedQuery result = dataStore.prepare(query);
		List<Entity> list = result.asList(FetchOptions.Builder.withLimit(1));
		if (list.size() == 0) {
			out.println("User doesn't exist");
			return;
		}
		Entity user = list.get(0);
		out.println(user.getProperty("value"));
		out.println(user.getProperty("money"));
		out.println(user.getProperty("owner"));
	}

	private void ownList(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PrintWriter out = response.getWriter();
		DatastoreService dataStore = DatastoreServiceFactory
				.getDatastoreService();
		Query query = new Query("User");
		query.addFilter("owner", FilterOperator.EQUAL, userID);
		PreparedQuery result = dataStore.prepare(query);
		List<Entity> list = result.asList(FetchOptions.Builder.withLimit(100));
		for (Entity entity : list)
			out.println(entity.getProperty("id"));
	}

	private void buy(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long buyID = Long.parseLong(request.getParameter("buyID"));
		DatastoreService dataStore = DatastoreServiceFactory
				.getDatastoreService();
		Query query = new Query("User");
		query.addFilter("id", FilterOperator.EQUAL, userID);
		PreparedQuery result = dataStore.prepare(query);
		List<Entity> list = result.asList(FetchOptions.Builder.withLimit(1));
		if (list.size() == 0) {
			response.getWriter().println("User doesn't exist");
			return;
		}
		Entity user = list.get(0);
		query = new Query("User");
		query.addFilter("id", FilterOperator.EQUAL, buyID);
		result = dataStore.prepare(query);
		list = result.asList(FetchOptions.Builder.withLimit(1));
		if (list.size() == 0) {
			response.getWriter().println("Buy doesn't exist");
			return;
		}
		Entity buy = list.get(0);
		query = new Query("User");
		query.addFilter("id", FilterOperator.EQUAL,
				(Long) (buy.getProperty("owner")));
		result = dataStore.prepare(query);
		list = result.asList(FetchOptions.Builder.withLimit(1));
		Entity owner = (list.size() == 0) ? null : list.get(0);
		if ((Long)(owner.getProperty("id")) == (Long)(user.getProperty("id"))) {
			response.getWriter().println("Buy already belongs to user");
			return;
		}
		long userMoney = (Long) (user.getProperty("money"));
		long buyValue = (Long) (buy.getProperty("value"));
		if (userMoney < buyValue) {
			response.getWriter().println("User doesn't have enough money");
			return;
		}
		user.setProperty("money", userMoney - buyValue);
		dataStore.put(user);
		buy.setProperty("value", buyValue * 11 / 10);
		buy.setProperty("owner", (Long) (user.getProperty("id")));
		dataStore.put(buy);
		if (owner != null) {
			owner.setProperty("money", (Long) (owner.getProperty("money"))
					+ buyValue);
			dataStore.put(owner);
		}
		response.getWriter().println("Purchase Done");
	}
}
