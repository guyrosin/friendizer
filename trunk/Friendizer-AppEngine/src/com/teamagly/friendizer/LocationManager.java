package com.teamagly.friendizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LocationManager extends HttpServlet {
	private HashMap<Long, UserLocation> onlineUsers;

	public LocationManager() {
		onlineUsers = new HashMap<Long, UserLocation>();
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getRequestURI().endsWith("/changeLocation"))
			changeLocation(request, response);
		else if (request.getRequestURI().endsWith("/exit"))
			exit(request, response);
		else
			nearbyUsers(request, response);
	}

	private void changeLocation(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		double xCord = Double.parseDouble(request.getParameter("xCord"));
		double yCord = Double.parseDouble(request.getParameter("yCord"));
		UserLocation location = onlineUsers.get(userID);
		if (location == null) {
			onlineUsers.put(userID, new UserLocation(xCord, yCord));
			response.getWriter().println("User added");
		} else {
			location.setXCord(xCord);
			location.setYCord(yCord);
			response.getWriter().println("User changed");
		}
	}

	private void exit(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		UserLocation location = onlineUsers.get(userID);
		if (location == null)
			response.getWriter().println("User doesn't exist");
		else {
			onlineUsers.remove(userID);
			response.getWriter().println("User deleted");
		}
	}

	private void nearbyUsers(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		double distance = Double.parseDouble(request.getParameter("distance"));
		PrintWriter out = response.getWriter();
		UserLocation location = onlineUsers.get(userID);
		if (location == null) {
			out.println("User doesn't exist");
			return;
		}
		for (Entry<Long, UserLocation> entry : onlineUsers.entrySet()) {
			if ((entry.getKey() != userID)
					&& (Math.pow(
							entry.getValue().getXCord() - location.getXCord(),
							2)
							+ Math.pow(
									entry.getValue().getYCord()
											- location.getYCord(), 2) <= Math
								.pow(distance, 2))) {
				out.println(entry.getKey());
			}
		}
	}
}
