package com.teamagly.friendizer;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class LocationManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "changeLocation")
			changeLocation(request, response);
		else
			nearbyUsers(request, response);
	}

	@SuppressWarnings("unchecked")
	private void changeLocation(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		double latitude = Double.parseDouble(request.getParameter("latitude"));
		double longitude = Double.parseDouble(request.getParameter("longitude"));
		User user;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			user = pm.getObjectById(User.class, userID);
		} catch (JDOObjectNotFoundException e) {
			log.severe("User doesn't exist");
			return;
		}
		user.setLatitude(latitude);
		user.setLongitude(longitude);
		user.setSince(new Date());

		pm.close();

		// TODO: Put it in a new thread

		/* Sending notification to a nearby user who bought me in the past (but didn't buy me for a week) */

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, -30);
		Date updated = cal.getTime();
		pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("since > updatedDate");
		query.declareParameters("java.util.Date updatedDate");
		// Get all the online users from the database
		List<User> users = (List<User>) query.execute(updated);
		query.closeAll();

		Query query2;
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(new Date());
		// Set a date of a week ago
		cal2.add(Calendar.DAY_OF_MONTH, -7);
		Date updated2 = cal2.getTime();
		List<Action> result2;

		/*
		 * The loop goes over the users and check only the nearby users
		 */
		for (User nearbyUser : users) {
			if (nearbyUser.getId() == userID)
				continue;
			double latitudeDiff = user.getLatitude() - nearbyUser.getLatitude();
			double longitudeDiff = user.getLongitude() - nearbyUser.getLongitude();
			if (latitudeDiff * latitudeDiff + longitudeDiff * longitudeDiff > 1)
				continue;

			query2 = pm.newQuery(Action.class);
			query2.setFilter("date < updatedDate && (buyerID == nearbyUserID && boughtID == userID)");
			query2.declareParameters("java.util.Date updatedDate, long userID, long nearbyUserID");

			// Check if the nearby user bought the current user before one week (or more)
			result2 = (List<Action>) query2.execute(updated2, userID, nearbyUser.getId());

			query2.closeAll();

			// If he bought him in the past
			if (!result2.isEmpty()) {
				query2 = pm.newQuery(Action.class);
				query2.setFilter("date > updatedDate && (buyerID == nearbyUserID && boughtID == userID)");
				query2.declareParameters("java.util.Date updatedDate, long userID, long nearbyUserID");

				// Check if the nearby user bought the current user in the last week
				result2 = (List<Action>) query2.execute(updated2, userID, nearbyUser.getId());

				query2.closeAll();

				// If he didn't buy him in the last week (didn't buy him for a long time)
				if (result2.isEmpty()) {
					// Send a collapsible notification to the nearby user suggesting to buy the user again
					Builder msg = new Builder();
					msg.addData("type", NotificationType.NEARBY.toString());
					msg.addData("userID", String.valueOf(userID));
					msg.addData("text", Notifications.NEARBY_MSG);
					msg.collapseKey("NEARBY");
					SendMessage.sendMessage(nearbyUser.getId(), msg.build());
				}
			}
		}

		pm.close();
		response.getWriter().println("The user location was changed");
	}

	@SuppressWarnings("unchecked")
	private void nearbyUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		User user;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			user = pm.getObjectById(User.class, userID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		// Update the current date
		user.setSince(new Date());
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, -30);
		Date updated = cal.getTime();
		Query query = pm.newQuery(User.class);
		query.setFilter("since > updatedDate");
		query.setOrdering("since asc");
		query.declareParameters("java.util.Date updatedDate");
		// Get all the online users from the database
		List<User> result = (List<User>) query.execute(updated);
		query.closeAll();

		ArrayList<User> nearbyUsers = new ArrayList<User>();

		/*
		 * The loop goes over the users and adds only the nearby users to a list
		 */
		for (User nearbyUser : result) {
			if (nearbyUser.getId() == userID)
				continue;
			double latitudeDiff = user.getLatitude() - nearbyUser.getLatitude();
			double longitudeDiff = user.getLongitude() - nearbyUser.getLongitude();
			if (latitudeDiff * latitudeDiff + longitudeDiff * longitudeDiff <= 1)
				nearbyUsers.add(nearbyUser);
		}

		pm.close();
		response.getWriter().println(new Gson().toJson(nearbyUsers));
	}
}
