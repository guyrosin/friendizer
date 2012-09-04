package com.teamagly.friendizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.gson.Gson;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.Action;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.UserMatching;

@SuppressWarnings("serial")
public class LocationManager extends HttpServlet {
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
		PersistenceManager pm = PMF.get().getPersistenceManager();
		User user = pm.getObjectById(User.class, userID);
		pm.close();
		if (user == null)
			throw new ServletException("This user doesn't exist");
		user.setLatitude(latitude);
		user.setLongitude(longitude);
		user.setSince(new Date());

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
			if ((nearbyUser.getId() != userID)
					&& (user.getLatitude() - nearbyUser.getLatitude()) * (user.getLatitude() - nearbyUser.getLatitude())
							+ (user.getLongitude() - nearbyUser.getLongitude())
							* (user.getLongitude() - nearbyUser.getLongitude()) <= 1000) {

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
						// Send a notification to the nearby user suggesting to buy the user again
						Message msg = new Message.Builder().addData("type", NotificationType.NEARBY.toString())
								.addData(Util.USER_ID, String.valueOf(userID)).addData("text", Notifications.NEARBY_MSG).build();
						SendMessage.sendMessage(nearbyUser.getId(), msg);
					}
				}
			}
		}

		pm.close();
		response.getWriter().println("The user location was changed");
	}

	@SuppressWarnings("unchecked")
	private void nearbyUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		User user = pm.getObjectById(User.class, userID);
		if (user == null) {
			pm.close();
			throw new ServletException("This user doesn't exist");
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

		ArrayList<UserMatching> nearbyUsers = new ArrayList<UserMatching>();

		/*
		 * The loop goes over the users and adds only the nearby users to a list
		 */
		for (User nearbyUser : result) {
			if ((nearbyUser.getId() != userID)
					&& (user.getLatitude() - nearbyUser.getLatitude()) * (user.getLatitude() - nearbyUser.getLatitude())
							+ (user.getLongitude() - nearbyUser.getLongitude())
							* (user.getLongitude() - nearbyUser.getLongitude()) <= 1000) {

				UserMatching userMatching = new UserMatching(nearbyUser, 0);
				nearbyUsers.add(userMatching);
			}
		}

		pm.close();
		response.getWriter().println(new Gson().toJson(nearbyUsers));
	}
}
