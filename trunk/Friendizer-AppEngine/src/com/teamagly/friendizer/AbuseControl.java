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
	private static final Logger log = Logger.getLogger(AbuseControl.class.getName());

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

	/**
	 * Block a user.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void block(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long blockedID = Long.parseLong(request.getParameter("blockedID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + userID + " && blockedID == " + blockedID);
		List<UserBlock> result = (List<UserBlock>) query.execute();
		query.closeAll();
		// Check if already blocked this user
		if (!result.isEmpty()) {
			pm.close();
			log.severe("You've already blocked this user");
			return;
		}
		// Block the user
		pm.makePersistent(new UserBlock(userID, blockedID));
		pm.close();
		response.getWriter().println("You've successfully blocked the user");
	}

	/**
	 * Unblock a user.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void unblock(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long blockedID = Long.parseLong(request.getParameter("blockedID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + userID + " && blockedID == " + blockedID);
		List<UserBlock> result = (List<UserBlock>) query.execute();
		query.closeAll();
		// Check if blocked this user in the past
		if (result.isEmpty()) {
			pm.close();
			log.severe("You didn't block this user");
			return;
		}
		// Unblock the user
		pm.deletePersistent(result.get(0));
		pm.close();
		response.getWriter().println("You've successfully unblocked the user");
	}

	/**
	 * Get the blocked list of a user.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void blockList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Get the blocked users IDs
		Query query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + userID);
		List<UserBlock> blockedID = (List<UserBlock>) query.execute();
		query.closeAll();
		if (blockedID.isEmpty()) {
			pm.close();
			return;
		}
		// Create the query of the blocked users
		StringBuilder blockedFilter = new StringBuilder();
		for (UserBlock userBlock : blockedID)
			blockedFilter.append("id == " + userBlock.getBlockedID() + " || ");
		blockedFilter.delete(blockedFilter.length() - 4, blockedFilter.length());
		// Get the blocked users
		query = pm.newQuery(User.class);
		query.setFilter(blockedFilter.toString());
		List<User> blocked = (List<User>) query.execute();
		blocked.size(); // App Engine bug workaround
		query.closeAll();
		pm.close();
		response.getWriter().println(new Gson().toJson(blocked));
	}
}
