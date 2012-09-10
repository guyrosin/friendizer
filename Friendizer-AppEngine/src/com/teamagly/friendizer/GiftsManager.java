package com.teamagly.friendizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.Gift;
import com.teamagly.friendizer.model.GiftCount;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.UserGift;

@SuppressWarnings("serial")
public class GiftsManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "allGifts")
			allGifts(request, response);
		else if (servlet.intern() == "userGifts")
			userGifts(request, response);
		else if (servlet.intern() == "sendGift")
			sendGift(request, response);
		else
			getGift(request, response);
	}

	@SuppressWarnings("unchecked")
	private void allGifts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Gift.class);
		List<Gift> result = (List<Gift>) query.execute();
		result.size(); // Important: App Engine bug workaround
		query.closeAll();
		pm.close();
		response.getWriter().println(new Gson().toJson(result));
	}

	@SuppressWarnings("unchecked")
	private void userGifts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		// Check if that user exists
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.getObjectById(User.class, userID); // Check if the user exists
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		Query query = pm.newQuery(UserGift.class);
		query.setFilter("receiverID == " + userID);
		List<UserGift> userGifts = (List<UserGift>) query.execute();
		query.closeAll();
		if (userGifts.isEmpty()) {
			pm.close();
			return;
		}
		StringBuilder giftsFilter = new StringBuilder();
		for (UserGift userGift : userGifts)
			giftsFilter.append("id == " + userGift.getGiftID() + " || ");
		giftsFilter.delete(giftsFilter.length() - 4, giftsFilter.length()); // Delete the last "or" sign
		query = pm.newQuery(Gift.class);
		query.setFilter(giftsFilter.toString());
		List<Gift> gifts = (List<Gift>) query.execute();
		gifts.size(); // App Engine bug workaround
		query.closeAll();
		pm.close();
		HashMap<Long, Integer> counters = new HashMap<Long, Integer>();
		for (Gift gift : gifts)
			counters.put(gift.getId(), 0);
		// Update the counters
		for (UserGift userGift : userGifts)
			counters.put(userGift.getGiftID(), counters.get(userGift.getGiftID()) + 1);
		// Create the GiftCount objects
		ArrayList<GiftCount> giftCounts = new ArrayList<GiftCount>();
		for (Long giftID : counters.keySet())
			giftCounts.add(new GiftCount(getGift(gifts, giftID), counters.get(giftID)));

		response.getWriter().println(new Gson().toJson(giftCounts));
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
		for (User user : result1)
			if (user.getId() == senderID)
				sender = user;
			else
				receiver = user;
		if (sender == null) {
			pm.close();
			log.severe("The sender doesn't exist");
			return;
		}
		if (receiver == null) {
			pm.close();
			log.severe("The receiver doesn't exist");
			return;
		}

		Gift gift;
		try {
			gift = pm.getObjectById(Gift.class, giftID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("This gift doesn't exist");
			response.getWriter().println("This gift doesn't exist");
			return;
		}
		if (sender.getMoney() < gift.getValue()) {
			pm.close();
			log.info("The sender doesn't have enough money to send this gift");
			response.getWriter().println("Sorry, you don't have enough money to buy this gift");
			return;
		}
		sender.setMoney(sender.getMoney() - gift.getValue());

		UserGift userGift = new UserGift(receiverID, senderID, giftID);
		pm.makePersistent(userGift);
		pm.close();
		response.getWriter().println("The gift has been sent");

		Builder msg = new Builder();
		msg.addData("type", NotificationType.GFT.toString());
		msg.addData("userID", String.valueOf(senderID));
		msg.addData("giftID", String.valueOf(userGift.getGiftID()));
		msg.addData("giftName", String.valueOf(gift.getName()));
		SendMessage.sendMessage(receiverID, msg.build());
	}

	private Gift getGift(List<Gift> gifts, long giftID) {
		for (Gift gift : gifts)
			if (gift.getId() == giftID)
				return gift;
		return null;
	}

	@SuppressWarnings("unchecked")
	private void getGift(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long giftID = Long.parseLong(request.getParameter("giftID"));
		// Check if that user exists
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.getObjectById(User.class, userID); // Check if the user exists
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		Query query = pm.newQuery(UserGift.class);
		query.setFilter("receiverID == " + userID + " && giftID == " + giftID);
		List<UserGift> userGifts = (List<UserGift>) query.execute();
		query.closeAll();
		if (userGifts.isEmpty()) {
			pm.close();
			return;
		}
		try {
			Gift gift = pm.getObjectById(Gift.class, giftID);
			GiftCount giftCount = new GiftCount(gift, userGifts.size());
			response.getWriter().println(new Gson().toJson(giftCount));
		} catch (JDOObjectNotFoundException e) {
			log.severe("Gift doesn't exist");
			return;
		} finally {
			pm.close();
		}
	}

}
