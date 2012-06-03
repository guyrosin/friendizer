package com.teamagly.friendizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import com.teamagly.friendizer.model.*;
import com.google.android.c2dm.server.PMF;

@SuppressWarnings("serial")
public class InboxManager extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getRequestURI().endsWith("/send"))
			send(request, response);
		else if (request.getRequestURI().endsWith("/getConversation"))
			getConversation(request, response);
		else if (request.getRequestURI().endsWith("/getUnread"))
			getUnread(request, response);
		else if (request.getRequestURI().endsWith("/getInbox"))
			getInbox(request, response);
	}

	private void send(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		long source = Long.parseLong(request.getParameter("src"));
		long destination = Long.parseLong(request.getParameter("dest"));
		String text = request.getParameter("text");

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Message message = new Message(source, destination, text);

		try {
			pm.makePersistent(message);
		} finally {
			pm.close();
		}
		out.println(message);
		
		pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(DeviceInfo.class);
		
		query.setFilter("userID == " + destination);
		query.setUnique(true);
		
		DeviceInfo device = (DeviceInfo) query.execute();
		
		String recipient = device.getDeviceRegistrationID();
		SendMessage.sendMessage(getServletContext(), recipient, message.toString());

	}

	private void getConversation(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		long user1 = Long.parseLong(request.getParameter("user1"));
		long user2 = Long.parseLong(request.getParameter("user2"));

		long from = Long.parseLong(request.getParameter("from"));
		int to = Integer.parseInt(request.getParameter("to"));

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(Message.class);
		query.setFilter("(source == user1 || source == user2)"
				+ " && (destination == user2 || destination == user1)");
		query.setOrdering("time desc");
		query.setRange(from, to);
		query.declareParameters("long user1, long user2");

		try {
			@SuppressWarnings("unchecked")
			List<Message> results = (List<Message>) query.execute(user1, user2);
			if (!results.isEmpty()) {
				JSONArray messages = new JSONArray();
				for (Message m : results) {
					messages.put(m.toJSONObject());
				}
				out.println(messages);
			}
		} finally {
			query.closeAll();
		}
	}

	private void getUnread(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		long destination = Long.parseLong(request.getParameter("userID"));

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(Message.class);
		query.setFilter("destination == dest && unread == true");
		query.setOrdering("id time");
		query.declareParameters("long dest");

		try {
			@SuppressWarnings("unchecked")
			List<Message> results = (List<Message>) query.execute(destination);
			if (!results.isEmpty()) {
				JSONArray messages = new JSONArray();
				for (Message m : results) {
					m.setUnread(false);
					pm.makePersistent(m);
					messages.put(m);
				}
				out.println(messages);
			}
		} finally {
			query.closeAll();
			pm.close();
		}
	}

	private void getInbox(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		long userId = Long.parseLong(request.getParameter("userID"));

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(Message.class);
		query.setFilter("source == src && unread == true");
		query.setOrdering("id desc");
		query.declareParameters("long src");

		try {
			@SuppressWarnings("unchecked")
			List<Message> results = (List<Message>) query.execute(userId);
			if (!results.isEmpty()) {
				for (Message m : results) {
					out.println(m);
					m.setUnread(false);
					pm.makePersistent(m);
				}
			}
		} finally {
			query.closeAll();
			pm.close();
		}
	}

}
