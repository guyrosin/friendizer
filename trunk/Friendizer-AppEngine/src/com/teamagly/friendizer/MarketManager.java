package com.teamagly.friendizer;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.User;

@SuppressWarnings("serial")
public class MarketManager extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		buy(request, response);
	}

	@SuppressWarnings("unchecked")
	private void buy(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long buyID = Long.parseLong(request.getParameter("buyID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		query.setFilter("id == " + userID + " || id == " + buyID);
		List<User> result = (List<User>) query.execute();
		query.closeAll();
		User buyer = null, buy = null;
		for (User user : result) {
			if (user.getId() == userID)
				buyer = user;
			else
				buy = user;
		}
		if (buyer == null)
			throw new ServletException("This user doesn't exist");
		if (buy == null)
			throw new ServletException("The user you want to buy doesn't exist");
		if (buy.getOwner() == userID)
			throw new ServletException("You already own the user you want to buy");
		if (buyer.getMoney() < buy.getPoints())
			throw new ServletException("You don't have enough money to buy this user");
		buyer.setMoney(buyer.getMoney() - buy.getPoints());
		buyer.setPoints(buyer.getPoints() + 10);
		// Check for level up
		buyer.setLevel(Util.calculateLevel(buyer.getLevel(), buyer.getPoints()));
		pm.makePersistent(buyer);
		ActionsManager.madeBuy(userID, buyID);
		AchievementsManager.userBoughtSomeone(buyer, getServletContext());
		if (buy.getOwner() > 0) {
			User preOwner = pm.getObjectById(User.class, buy.getOwner());
			if (preOwner != null) {
				preOwner.setMoney(preOwner.getMoney() + buy.getPoints());
				pm.makePersistent(preOwner);
			}
		}
		buy.setPoints(buy.getPoints() + 20);
		// Check for level up
		buy.setLevel(Util.calculateLevel(buy.getLevel(), buy.getPoints()));
		buy.setOwner(userID);
		pm.makePersistent(buy);
		AchievementsManager.userValueIncreased(buy, getServletContext());
		AchievementsManager.someoneBoughtUser(buy, getServletContext());
		pm.close();
		response.getWriter().println("Purchase Done");

		Message msg = new Message.Builder().addData("type", NotificationType.BUY.toString())
				.addData(Util.USER_ID, String.valueOf(userID)).build();
		SendMessage.sendMessage(buyID, msg);
	}

}
