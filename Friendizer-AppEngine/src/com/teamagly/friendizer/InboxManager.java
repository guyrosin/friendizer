package com.teamagly.friendizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.c2dm.server.PMF;

@SuppressWarnings("serial")
public class InboxManager extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getRequestURI().endsWith("/send"))
			send(request, response);
		else if (request.getRequestURI().endsWith("/read"))
			read(request, response);
		else if (request.getRequestURI().endsWith("/unread"))
			unread(request, response);
	}

	private void send(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

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

	}

	private void read(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		long source = Long.parseLong(request.getParameter("src"));
		//String tmp = request.getParameter("dest");
		long destination = Long.parseLong(request.getParameter("dest"));
		long from = Long.parseLong(request.getParameter("from"));
		int to = Integer.parseInt(request.getParameter("to"));
		
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(Message.class);
		query.setFilter("source == src && destination == dest");
		query.setOrdering("id desc");
		query.setRange(from,to);
		query.declareParameters("long src, long dest");

		try {
			@SuppressWarnings("unchecked")
			List<Message> results = (List<Message>) query.execute(source,
					destination);
			if (!results.isEmpty()) {
				for (Message m : results) {
					out.println(m);
				}
			} else {
				out.println("no messages");
			}
		} finally {
			query.closeAll();
		}
	}
	
	private void unread(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		long source = Long.parseLong(request.getParameter("src"));
		//long destination = Long.parseLong(request.getParameter("dest"));
		
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query query = pm.newQuery(Message.class);
		//query.setFilter("source == src && destination == dest && unread == true");
		query.setFilter("source == src && unread == true");
		query.setOrdering("id desc");
		//query.declareParameters("long src, long dest");
		query.declareParameters("long src");

		try {
			@SuppressWarnings("unchecked")
			List<Message> results = (List<Message>) query.execute(source);
			if (!results.isEmpty()) {
				for (Message m : results) {
					out.println(m);
					m.setUnread(false);
					pm.makePersistent(m);
				}
			} else {
				out.println("no messages");
			}
		} finally {
			query.closeAll();
			pm.close();
		}
	}
	


}
