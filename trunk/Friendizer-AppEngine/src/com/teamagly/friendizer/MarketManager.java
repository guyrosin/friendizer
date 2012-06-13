package com.teamagly.friendizer;

import java.io.IOException;
import java.util.List;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.android.c2dm.server.PMF;

import com.teamagly.friendizer.model.*;

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
		if (buyer.getMoney() < buy.getValue())
			throw new ServletException("You don't have enough money to buy this user");
		buyer.setMoney(buyer.getMoney() - buy.getValue());
		pm.makePersistent(buyer);
		ActionsManager.madeBuy(userID, buyID);
		AchievementsManager.userBoughtSomeone(buyer);
		if (buy.getOwner() > 0) {
			query = pm.newQuery(User.class);
			query.setFilter("id == " + buy.getOwner());
			result = (List<User>) query.execute();
			query.closeAll();
			if (!result.isEmpty()) {
				User preOwner = result.get(0);
				preOwner.setMoney(preOwner.getMoney() + buy.getValue());
				pm.makePersistent(preOwner);
			}
		}
		buy.setValue(buy.getValue() * 11 / 10);
		buy.setOwner(userID);
		pm.makePersistent(buy);
		AchievementsManager.userValueIncreased(buy);
		AchievementsManager.someoneBoughtUser(buy);
		pm.close();
		response.getWriter().println("Purchase Done");
		
		DeviceInfo device = DatastoreHelper.getInstance().getDeviceInfo(buyID);
		
		SendMessage.sendMessage(getServletContext(), device, Notifications.BEEN_BOUGHT_MSG);
	}

}
