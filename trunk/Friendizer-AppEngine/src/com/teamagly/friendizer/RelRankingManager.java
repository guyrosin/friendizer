package com.teamagly.friendizer;

import java.io.IOException;
import java.util.*;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class RelRankingManager extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "ranking")
			ranking(request, response);
	}

	private void ranking(HttpServletRequest request, HttpServletResponse response) throws IOException {
		long user1ID = Long.parseLong(request.getParameter("user1ID"));
		long user2ID = Long.parseLong(request.getParameter("user2ID"));

		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		User user1 = pm.getObjectById(User.class, user1ID);
		User user2 = pm.getObjectById(User.class, user2ID);
		
		if (user1 == null || user2 == null) {
			response.getWriter().println(0);
			return;
		}
		
		double ranking = 0;
		
		if (user1.getOwner() == user2.getId())
			ranking += 5;
		else if (user2.getOwner() == user1.getId())
			ranking += 5;
		else
			ranking += actionRanking(user1ID, user2ID,response);
		

		double tmp = chatRanking(user1ID, user2ID);

		ranking += tmp;
		
		ranking = Math.round(ranking * 10) / 10.0;
		response.getWriter().println(ranking);

	}
	
	@SuppressWarnings("unchecked")
	private double actionRanking(long user1ID, long user2ID, HttpServletResponse response) throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		double actionRanking = 0;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -7);
		Date weekAgo = cal.getTime();
		
		
		Query query = pm.newQuery(Action.class);
		String f = "buyerID == " + user1ID + " && " + "boughtID =="  + user2ID + " && date > weekAgo";
		query.setFilter(f);
		query.declareParameters("java.util.Date weekAgo");
		// Get all the the action in last week from user2 to user1
		List<Action> actions1 = (List<Action>) query.execute(weekAgo);
		query.closeAll();


		query = pm.newQuery(Action.class);
		f = "buyerID == " + user2ID + " && " + "boughtID =="  + user1ID + " && date > weekAgo";
		query.setFilter(f);
		query.declareParameters("java.util.Date weekAgo");
		// Get all the the action in last week from user2 to user1
		List<Action> actions2 = (List<Action>) query.execute(weekAgo);
		query.closeAll();
		
		
		
		int buys = actions1.size() + actions2.size();
		actionRanking = (double) buys /  2.0;
		if (actionRanking > 5)
			actionRanking = 5;
		return actionRanking;
		
	}
	
	@SuppressWarnings("unchecked")
	private double chatRanking(long user1ID, long user2ID) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		double chatRanking = 0;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -7);
		Date weekAgo = cal.getTime();
		
		Query query = pm.newQuery(ChatMessage.class);
		String f = "destination == " + user1ID + " && " + "source == " + user2ID;
		query.setFilter(f);
		// Get all the the action in last week from user1 to user2
		List<User> actions1 = (List<User>) query.execute(weekAgo);
		query.closeAll();
		
		query = pm.newQuery(ChatMessage.class);
		
		f = "destination == " + user1ID + " && " + "source == " + user2ID;
		query.setFilter(f);
		List<User> actions2 = (List<User>) query.execute(weekAgo);
		query.closeAll();
		
		int messages = actions1.size() + actions2.size();
		
		chatRanking = (double) messages /  10.0;
		
		if (chatRanking > 5)
			chatRanking = 5;
		
		return chatRanking;
		
	}

}
