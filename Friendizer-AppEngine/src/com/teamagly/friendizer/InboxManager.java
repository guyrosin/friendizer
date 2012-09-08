package com.teamagly.friendizer;

import java.io.*;
import java.util.List;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.ChatMessage;

@SuppressWarnings("serial")
public class InboxManager extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "send")
			send(request, response);
		else if (servlet.intern() == "getConversation")
			getConversation(request, response);
		else if (servlet.intern() == "getUnread")
			getUnread(request, response);
		else
			getInbox(request, response);
	}

	private void send(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		long source = Long.parseLong(request.getParameter("src"));
		long destination = Long.parseLong(request.getParameter("dest"));
		String text = request.getParameter("text");

		PersistenceManager pm = PMF.get().getPersistenceManager();

		ChatMessage message = new ChatMessage(source, destination, text);

		pm.makePersistent(message);
		pm.close();
		out.println(message);

		Builder msg = new Builder();
		msg.addData("type", NotificationType.CHAT.toString());
		msg.addData("userID", String.valueOf(source));
		msg.addData("text", message.getText());
		SendMessage.sendMessage(destination, msg.build());
	}

	@SuppressWarnings("unchecked")
	private void getConversation(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		long user1 = Long.parseLong(request.getParameter("user1"));
		long user2 = Long.parseLong(request.getParameter("user2"));

		long from = Long.parseLong(request.getParameter("from"));
		int to = Integer.parseInt(request.getParameter("to"));

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(ChatMessage.class);
		query.setFilter("(source == user1 || source == user2)" + " && (destination == user2 || destination == user1)");
		query.setOrdering("time desc");
		query.setRange(from, to);
		query.declareParameters("long user1, long user2");

		List<ChatMessage> messages = (List<ChatMessage>) query.execute(user1, user2);
		if (!messages.isEmpty())
			out.println(new Gson().toJson(messages));
		query.closeAll();
	}

	@SuppressWarnings("unchecked")
	private void getUnread(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		long destination = Long.parseLong(request.getParameter("userID"));

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(ChatMessage.class);
		query.setFilter("destination == dest && unread == true");
		query.setOrdering("id time");
		query.declareParameters("long dest");

		List<ChatMessage> messages = (List<ChatMessage>) query.execute(destination);
		query.closeAll();
		if (!messages.isEmpty()) {
			for (ChatMessage m : messages) {
				m.setUnread(false);
				pm.makePersistent(m);
			}
			out.println(new Gson().toJson(messages));
		}
		pm.close();
	}

	@SuppressWarnings("unchecked")
	private void getInbox(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		long userId = Long.parseLong(request.getParameter("userID"));

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(ChatMessage.class);
		query.setFilter("source == src");
		query.setOrdering("id desc");
		query.declareParameters("long src");

		List<ChatMessage> messages = (List<ChatMessage>) query.execute(userId);
		query.closeAll();
		if (!messages.isEmpty()) {
			for (ChatMessage m : messages) {
				out.println(m);
				m.setUnread(false);
				pm.makePersistent(m);
			}
			out.println(new Gson().toJson(messages));
		}
		pm.close();
	}
}
