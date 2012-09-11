package com.teamagly.friendizer;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.teamagly.friendizer.model.Action;
import com.teamagly.friendizer.model.ChatMessage;
import com.teamagly.friendizer.model.User;

public class RelRankingManager extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "ranking")
			ranking(request, response);
	}

	private void ranking(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		long user1ID = Long.parseLong(request.getParameter("user1ID"));
		long user2ID = Long.parseLong(request.getParameter("user2ID"));
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		User user1 = pm.getObjectById(User.class, user1ID);
		User user2 = pm.getObjectById(User.class, user2ID);
		
		if (user1 == null || user2 == null) {
			response.getWriter().println(0);
			return;
		}
		
		float ranking = 0;
		
		if (user1.getOwner() == user2.getId())
			ranking += 5;
		else if (user2.getOwner() == user1.getId())
			ranking += 5;
		else
			ranking += actionRanking(user1ID, user2ID);
		
		ranking += chatRanking(user1ID, user2ID);
		
		ranking = Math.round(ranking * 10) / 10;
		response.getWriter().println(ranking);

	}
	
	private double actionRanking(long user1ID, long user2ID) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		double actionRanking = 0;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -7);
		Date weekAgo = cal.getTime();
		
		Query query = pm.newQuery(Action.class);
		query.setFilter("buyerID == " + user1ID);
		query.setFilter("boughtID == " + user2ID);
		query.setFilter("date > weekAgo");
		query.declareParameters("java.util.Date weekAgo");
		// Get all the the action in last week from user1 to user2
		List<User> actions1 = (List<User>) query.execute(weekAgo);
		query.closeAll();
		
		query = pm.newQuery(Action.class);
		query.setFilter("buyerID == " + user2ID);
		query.setFilter("boughtID == " + user1ID);
		query.setFilter("date > weekAgo");
		query.declareParameters("java.util.Date weekAgo");
		// Get all the the action in last week from user2 to user1
		List<User> actions2 = (List<User>) query.execute(weekAgo);
		query.closeAll();
		
		int buys = actions1.size() + actions2.size();
		
		actionRanking = (float) buys /  2.0;
		if (actionRanking > 5)
			actionRanking = 5;
		return actionRanking;
		
	}
	
	private double chatRanking(long user1ID, long user2ID) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		double chatRanking = 0;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -7);
		Date weekAgo = cal.getTime();
		
		Query query = pm.newQuery(ChatMessage.class);
		query.setFilter("destination == " + user1ID);
		query.setFilter("source == " + user2ID);
		query.setFilter("time > weekAgo");
		query.declareParameters("java.util.Date weekAgo");
		// Get all the the action in last week from user1 to user2
		List<User> actions1 = (List<User>) query.execute(weekAgo);
		query.closeAll();
		
		query = pm.newQuery(ChatMessage.class);
		query.setFilter("destination == " + user1ID);
		query.setFilter("source == " + user2ID);
		query.setFilter("time > weekAgo");
		query.declareParameters("java.util.Date weekAgo");
		// Get all the the action in last week from user1 to user2
		List<User> actions2 = (List<User>) query.execute(weekAgo);
		query.closeAll();
		
		int messages = actions1.size() + actions2.size();
		
		chatRanking = (float) messages /  50.0;
		
		if (chatRanking > 5)
			chatRanking = 5;
		
		return chatRanking;
		
	}

}
