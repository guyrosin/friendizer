package com.teamagly.friendizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.Gift;
import com.teamagly.friendizer.model.GiftCount;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.UserGift;

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
			giftsArray.put(gift.toJSONObject());
		pm.close();
		response.getWriter().println(giftsArray);
	}

	@SuppressWarnings("unchecked")
	private void userGifts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		// Check if that user exists
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		if (result.isEmpty())
			throw new ServletException("This user doesn't exist");
		query = pm.newQuery(UserGift.class);
		query.setFilter("receiverID == " + userID);
		List<UserGift> userGifts = (List<UserGift>) query.execute();
		query.closeAll();
		if (userGifts.isEmpty()) {
			response.getWriter().println(new JSONArray());
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
		query.closeAll();
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

		JSONArray giftsArray = new JSONArray();
		for (GiftCount gift : giftCounts)
			giftsArray.put(gift.toJSONObject());
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

		UserGift userGift = new UserGift(receiverID, senderID, giftID);
		query = pm.newQuery(UserGift.class);
		query.setFilter("receiverID == " + receiverID + " && giftID == " + giftID);
		List<UserGift> result3 = (List<UserGift>) query.execute();
		query.closeAll();
		if (result3.isEmpty())
			pm.makePersistent(userGift);
		pm.close();
		response.getWriter().println("The gift has been sent");

		Message msg = new Message.Builder().addData("type", NotificationType.GFT.toString())
				.addData(Util.USER_ID, String.valueOf(senderID)).addData("giftID", String.valueOf(userGift.getGiftID()))
				.addData("giftName", String.valueOf(gift.getName())).build();
		SendMessage.sendMessage(receiverID, msg);
	}

	public Gift getGift(List<Gift> gifts, long giftID) {
		for (Gift gift : gifts)
			if (gift.getId() == giftID)
				return gift;
		return null;
	}
}
