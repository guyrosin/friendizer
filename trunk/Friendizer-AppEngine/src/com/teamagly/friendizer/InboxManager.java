package com.teamagly.friendizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class InboxManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());
	
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
		
		if (!isSendingLegal(source, destination))
			return;

		PersistenceManager pm = PMF.get().getPersistenceManager();

		ChatMessage message = new ChatMessage(source, destination, text);

		pm.makePersistent(message);
		pm.close();
		out.println(new Gson().toJson(message));

		Builder msg = new Builder();
		msg.addData("type", NotificationType.CHAT.toString());
		msg.addData("userID", String.valueOf(source));
		msg.addData("text", message.getText());
		SendMessage.sendMessage(destination, msg.build());
	}
	
	@SuppressWarnings("unchecked")
	private boolean isSendingLegal(long source, long destination) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + destination + " && blockedID == " + source);
		List<UserBlock> result = (List<UserBlock>) query.execute();
		query.closeAll();
		if (!result.isEmpty()) {
			pm.close();
			log.severe("You are not allowed to send a message to this user");
			return false;
		}
		pm.close();
		return true;
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
		query.closeAll();
		if (!messages.isEmpty()) {
			out.println(new Gson().toJson(messages));
			for (ChatMessage m : messages)
				m.setUnread(false);
		}

		pm.close();
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
		if (!messages.isEmpty()) {
			out.println(new Gson().toJson(messages));
			for (ChatMessage m : messages)
				m.setUnread(false);
		}

		query.closeAll();
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
		if (!messages.isEmpty()) {
			out.println(new Gson().toJson(messages));
			for (ChatMessage m : messages)
				m.setUnread(false);
		}

		query.closeAll();
		pm.close();
	}
}
