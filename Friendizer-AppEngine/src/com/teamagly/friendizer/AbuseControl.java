package com.teamagly.friendizer;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.gson.Gson;
import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class AbuseControl extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "block")
			block(request, response);
		else if (servlet.intern() == "unblock")
			unblock(request, response);
		else
			blockList(request, response);
	}

	@SuppressWarnings("unchecked")
	private void block(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long blockedID = Long.parseLong(request.getParameter("blockedID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.getObjectById(User.class, userID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		try {
			pm.getObjectById(User.class, blockedID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("The user you want to block doesn't exist");
			return;
		}
		Query query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + userID + " && blockedID == " + blockedID);
		List<UserBlock> result = (List<UserBlock>) query.execute();
		query.closeAll();
		if (!result.isEmpty()) {
			pm.close();
			log.severe("You've already blocked this user");
			return;
		}
		pm.makePersistent(new UserBlock(userID, blockedID));
		pm.close();
		response.getWriter().println("You've successfully blocked the user");
	}
	
	@SuppressWarnings("unchecked")
	private void unblock(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long blockedID = Long.parseLong(request.getParameter("blockedID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.getObjectById(User.class, userID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		try {
			pm.getObjectById(User.class, blockedID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("The user you want to block doesn't exist");
			return;
		}
		Query query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + userID + " && blockedID == " + blockedID);
		List<UserBlock> result = (List<UserBlock>) query.execute();
		query.closeAll();
		if (result.isEmpty()) {
			pm.close();
			log.severe("You didn't block this user");
			return;
		}
		pm.deletePersistent(result.get(0));
		pm.close();
		response.getWriter().println("You've successfully unblocked the user");
	}

	@SuppressWarnings("unchecked")
	private void blockList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Check if the user exists
		try {
			pm.getObjectById(User.class, userID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		Query query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + userID);
		List<UserBlock> blockedID = (List<UserBlock>) query.execute();
		query.closeAll();
		if (blockedID.isEmpty()) {
			pm.close();
			return;
		}
		StringBuilder blockedFilter = new StringBuilder();
		for (UserBlock userBlock : blockedID)
			blockedFilter.append("id == " + userBlock.getBlockedID() + " || ");
		blockedFilter.delete(blockedFilter.length() - 4, blockedFilter.length());
		query = pm.newQuery(User.class);
		query.setFilter(blockedFilter.toString());
		List<User> blocked = (List<User>) query.execute();
		blocked.size(); // App Engine bug workaround
		query.closeAll();
		pm.close();
		response.getWriter().println(new Gson().toJson(blocked));
	}
}
