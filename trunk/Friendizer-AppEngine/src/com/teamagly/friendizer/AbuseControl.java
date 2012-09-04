package com.teamagly.friendizer;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.UserBlock;

@SuppressWarnings("serial")
public class AbuseControl extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "block")
			block(request, response);
		else
			blockList(request, response);
	}

	@SuppressWarnings("unchecked")
	private void block(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long blockedID = Long.parseLong(request.getParameter("blockedID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID + " || id == " + blockedID);
		List<User> result1 = (List<User>) query.execute();
		query.closeAll();
		User user = null, blocked = null;
		for (User u : result1) {
			if (u.getId() == userID)
				user = u;
			else
				blocked = u;
		}
		if (user == null)
			throw new ServletException("The user doesn't exist");
		if (blocked == null)
			throw new ServletException("The user you want to block doesn't exist");
		query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + userID + " && blockedID == " + blockedID);
		List<UserBlock> result2 = (List<UserBlock>) query.execute();
		query.closeAll();
		if (!result2.isEmpty())
			throw new ServletException("You already blocked this user");
		pm.makePersistent(new UserBlock(userID, blockedID));
		pm.close();
		response.getWriter().println("You blocked the user");
	}

	@SuppressWarnings("unchecked")
	private void blockList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		User user = pm.getObjectById(User.class, userID);
		pm.close();
		if (user == null)
			throw new ServletException("This user doesn't exist");
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
		query.closeAll();
		pm.close();
		response.getWriter().println(new Gson().toJson(blocked));
	}
}
