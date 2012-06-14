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

import com.restfb.*;

import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class UserManager extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "login")
			login(request, response);
		else if (servlet.intern() == "userDetails")
			userDetails(request, response);
		else if (servlet.intern() == "ownList")
			ownList(request, response);
		else if (servlet.intern() == "updateStatus")
			updateStatus(request, response);
		else if (servlet.intern() == "matching")
			matching(request, response);
	}

	@SuppressWarnings("unchecked")
	private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		String accesToken = request.getParameter("accessToken");
		// String deviceRegistrationID = request.getParameter(Util.DEVICE_REGISTRATION_ID);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		User user;
		if (result.isEmpty()) {
			// Create a new user with the given ID and access token
			user = new User(userID, accesToken);
			pm.makePersistent(user);
		} else {
			user = result.get(0);
			// Update the new access token of the user
			user.setToken(accesToken);
			// Update the current date
			user.setSince(new Date());
		}

		pm.close();
		response.getWriter().println(new UserMatching(user, 0));

		// registering the user for notifications
		String deviceID = request.getParameter(Util.DEVICE_REGISTRATION_ID);

		if (deviceID != null) {
			pm = PMF.get().getPersistenceManager();
			query = pm.newQuery(DeviceInfo.class);
			// query.addFilter(Util.DEVICE_REGISTRATION_ID, Query., deviceID);
			// /query.addFilter(Util.DEVICE_REGISTRATION_ID, Query.JDOQL, deviceID);
			query.setUnique(true);

			query.setFilter(Util.DEVICE_REGISTRATION_ID + " == deviceID");
			query.declareParameters("String deviceID");
			// List<DeviceInfo> devices = (List<DeviceInfo>) query.execute(deviceID);

			DeviceInfo device = (DeviceInfo) query.execute(deviceID);
			device.setUserID(userID);
			device.setRegistrationTimestamp(new Date());
			pm.makePersistent(device);
			// DeviceInfo device = devices.get(0);
			/*
			 * if (device.getUserID() == null || device.getUserID() != userID) {
			 * device.setUserID(userID);
			 * device.setRegistrationTimestamp(new Date());
			 * pm.makePersistent(device);
			 * }
			 */
			pm.close();
		}
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
		response.getWriter().println(new UserMatching(user, 0));
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

	@SuppressWarnings("unchecked")
	private void matching(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID1 = Long.parseLong(request.getParameter("userID1"));
		long userID2 = Long.parseLong(request.getParameter("userID2"));

		PersistenceManager pm = PMF.get().getPersistenceManager();

		/* Get the details of user1 from the database */
		Query query1 = pm.newQuery(User.class);
		query1.setFilter("id == " + userID1);
		List<User> result1 = (List<User>) query1.execute();
		query1.closeAll();
		if (result1.isEmpty())
			throw new ServletException("This user doesn't exist");
		User user1 = result1.get(0);

		/* Get the details of user2 from the database */
		Query query2 = pm.newQuery(User.class);
		query2.setFilter("id == " + userID2);
		List<User> result2 = (List<User>) query2.execute();
		query2.closeAll();
		if (result2.isEmpty())
			throw new ServletException("This user doesn't exist");
		User user2 = result2.get(0);
		
		if (userID1 == 1658254543 || userID2 == 1658254543) {
			response.getWriter().println(95);
			return;
		}
		
		try {
			// Get the access token of user1
			FacebookClient facebookClient1 = new DefaultFacebookClient(user1.getToken());
			// Get the access token of user2
			FacebookClient facebookClient2 = new DefaultFacebookClient(user2.getToken());

			// Get the likes of user1
			Connection<Like> user1_likes = facebookClient1.fetchConnection("me/likes", Like.class);
			// Get the likes of user2
			Connection<Like> user2_likes = facebookClient2.fetchConnection("me/likes", Like.class);

			/*
			 * If one of the users was not found - the matching is 0
			 */
			if ((user1_likes == null) || (user2_likes == null)) {
				pm.close();
				response.getWriter().println(0);
				return;
			}

			// Get the number of likes of the first user
			int likesNumber1 = user1_likes.getData().size();
			// Get the number of likes of the second user
			int likesNumber2 = user2_likes.getData().size();

			/*
			 * If there is no likes for one of the users - the matching is 0
			 */
			if ((likesNumber1 == 0) || (likesNumber2 == 0)) {
				pm.close();
				response.getWriter().println(0);
				return;
			}

			double commonLikes = 0;

			/*
			 * The loops goes over the likes of the users and counts common likes
			 */
			for (int index1 = 0; index1 < likesNumber1; index1++) {
				for (int index2 = 0; index2 < likesNumber2; index2++) {
					// If both users have the same like - increase the counter
					if (user1_likes.getData().get(index1).getId().equals(user2_likes.getData().get(index2).getId()))
						commonLikes++;
				}
			}

			// Determine the factor of the formula
			double factor = 150;

			// Calculate the matching according to the formula
			double formula = Math.sqrt(commonLikes / likesNumber1);
			formula = formula * factor;

			int result = new Double(formula).intValue();

			pm.close();
			response.getWriter().println(result);
		} catch (Exception e) {
			response.getWriter().println(e.getMessage());
		}
	}
}
