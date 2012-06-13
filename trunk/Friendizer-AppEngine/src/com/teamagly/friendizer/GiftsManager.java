package com.teamagly.friendizer;

import java.io.IOException;
import java.util.List;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;

import com.google.android.c2dm.server.PMF;

import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class GiftsManager extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "allGifts")
			allGifts(request, response);
		else if (servlet.intern() == "userGifts")
			userGifts(request, response);
		else
			sendGift(request, response);
	}
	
	@SuppressWarnings("unchecked")
	private void allGifts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Gift.class);
		List<Gift> result = (List<Gift>) query.execute();
		query.closeAll();
		JSONArray giftsArray = new JSONArray();
		for (Gift gift : result)
			giftsArray.put(gift);
		pm.close();
		response.getWriter().println(giftsArray);
	}
	
	@SuppressWarnings("unchecked")
	private void userGifts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			throw new ServletException("This user doesn't exist");
		query = pm.newQuery(UserGift.class);
		query.setFilter("userID == " + userID);
		List<UserGift> userGiftsID = (List<UserGift>) query.execute();
		query.closeAll();
		if (userGiftsID.isEmpty()) {
			response.getWriter().println(new JSONArray());
			pm.close();
			return;
		}
		StringBuilder giftsFilter = new StringBuilder();
		for (UserGift userGift : userGiftsID)
			giftsFilter.append("id == " + userGift.getGiftID() + " || ");
		giftsFilter.delete(giftsFilter.length() - 4, giftsFilter.length());
		query = pm.newQuery(Gift.class);
		query.setFilter(giftsFilter.toString());
		List<Gift> userGifts = (List<Gift>) query.execute();
		query.closeAll();
		JSONArray giftsArray = new JSONArray();
		for (Gift gift : userGifts)
			giftsArray.put(gift);
		pm.close();
		response.getWriter().println(giftsArray);
	}

	@SuppressWarnings("unchecked")
	private void sendGift(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long senderID = Long.parseLong(request.getParameter("senderID"));
		long receiverID = Long.parseLong(request.getParameter("receiverID"));
		long giftID = Long.parseLong(request.getParameter("giftID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + senderID + " || id == " + receiverID);
		List<User> result1 = (List<User>) query.execute();
		query.closeAll();
		User sender = null, receiver = null;
		for (User user : result1) {
			if (user.getId() == senderID)
				sender = user;
			else
				receiver = user;
		}
		if (sender == null)
			throw new ServletException("The sender doesn't exist");
		if (receiver == null)
			throw new ServletException("The receiver doesn't exist");
		query = pm.newQuery(Gift.class);
		query.setFilter("id == " + giftID);
		List<Gift> result2 = (List<Gift>) query.execute();
		query.closeAll();
		if (result2.isEmpty())
			throw new ServletException("The gift doesn't exist");
		Gift gift = result2.get(0);
		if (sender.getMoney() < gift.getValue())
			throw new ServletException("The sender doesn't have enough money to send this gift");
		sender.setMoney(sender.getMoney() - gift.getValue());
		pm.makePersistent(sender);
		query = pm.newQuery(UserGift.class);
		query.setFilter("userID == " + receiverID + " && giftID == " + giftID);
		List<UserGift> result3 = (List<UserGift>) query.execute();
		query.closeAll();
		if (result3.isEmpty())
			pm.makePersistent(new UserGift(receiverID, giftID));
		pm.close();
		response.getWriter().println("The gift has been sent");
	}
}
