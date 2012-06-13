package com.teamagly.friendizer;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;

import com.google.android.c2dm.server.PMF;

import com.teamagly.friendizer.model.*;

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
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			throw new ServletException("This user doesn't exist");
		User user = result.get(0);
		user.setLatitude(latitude);
		user.setLongitude(longitude);
		user.setSince(new Date());
		pm.close();
		response.getWriter().println("The user location was changed");
	}

	@SuppressWarnings("unchecked")
	private void nearbyUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			throw new ServletException("This user doesn't exist");
		User user = result.get(0);
		// Update the current date
		user.setSince(new Date());
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, -30);
		Date updated = cal.getTime();
		query = pm.newQuery(User.class);
		query.setFilter("since > updatedDate");
		query.declareParameters("java.util.Date updatedDate");
		// Get all the online users from the database
		result = (List<User>) query.execute(updated);
		query.closeAll();
		
		JSONArray nearbyUsers = new JSONArray();
		
		/*
		 * The loop goes over the users and adds only the nearby users to a list
		 */
		for (User nearbyUser : result) {
			if ((nearbyUser.getId() != userID) && (user.getLatitude() - nearbyUser.getLatitude()) * (user.getLatitude() - nearbyUser.getLatitude()) + 
					(user.getLongitude() - nearbyUser.getLongitude()) * (user.getLongitude() - nearbyUser.getLongitude()) <= 1000) {
				
				UserMatching userMatching = new UserMatching(nearbyUser,0);
				nearbyUsers.put(userMatching.toJSONObject());
			}
		}

		pm.close();
		response.getWriter().println(nearbyUsers);
	}
}
