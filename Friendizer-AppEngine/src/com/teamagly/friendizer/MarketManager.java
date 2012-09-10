package com.teamagly.friendizer;

import java.io.IOException;
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
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.User;

@SuppressWarnings("serial")
public class MarketManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());

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
		for (User user : result)
			if (user.getId() == userID)
				buyer = user;
			else
				buy = user;
		if (buyer == null) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		if (buy == null) {
			pm.close();
			log.severe("The user you want to buy doesn't exist");
			return;
		}
		if (buy.getOwner() == userID) {
			pm.close();
			log.severe("You already own the user you want to buy");
			response.getWriter().println("You already own the user you want to buy");
			return;
		}
		if (buyer.getMoney() < buy.getPoints()) {
			pm.close();
			log.severe("You don't have enough money to buy this user");
			response.getWriter().println("You don't have enough money to buy this user");
			return;
		}
		buyer.setMoney(buyer.getMoney() - buy.getPoints());
		buyer.setPoints(buyer.getPoints() + 10);
		// Check for level up
		buyer.setLevel(Util.calculateLevel(buyer.getLevel(), buyer.getPoints()));
		ActionsManager.madeBuy(userID, buyID);
		AchievementsManager.userBoughtSomeone(pm.detachCopy(buyer), getServletContext());
		if (buy.getOwner() > 0)
			try {
				User preOwner = pm.getObjectById(User.class, buy.getOwner());
				preOwner.setMoney(preOwner.getMoney() + buy.getPoints());
			} catch (JDOObjectNotFoundException e) {
			}
		buy.setPoints(buy.getPoints() + 20);
		// Check for level up
		buy.setLevel(Util.calculateLevel(buy.getLevel(), buy.getPoints()));
		buy.setOwner(userID);
		AchievementsManager.someoneBoughtUser(pm.detachCopy(buy), getServletContext());
		pm.close();
		response.getWriter().println("Purchase Done");

		Builder msg = new Builder();
		msg.addData("type", NotificationType.BUY.toString());
		msg.addData("userID", String.valueOf(userID));
		SendMessage.sendMessage(buyID, msg.build());
	}
}
