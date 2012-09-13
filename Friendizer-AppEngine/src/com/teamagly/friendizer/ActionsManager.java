package com.teamagly.friendizer;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.teamagly.friendizer.Notifications.NotificationType;
import com.teamagly.friendizer.model.*;

@SuppressWarnings("serial")
public class ActionsManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(ActionsManager.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "buy")
			buy(request, response);
		else
			actionHistory(request, response);
	}

	private void buy(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long buyID = Long.parseLong(request.getParameter("buyID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		User buyer;
		try {
			buyer = pm.getObjectById(User.class, userID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		User buy;
		try {
			buy = pm.getObjectById(User.class, buyID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("The user you want to buy doesn't exist");
			return;
		}
		if (!isPurchaseLegal(buyer, buy)) {
			pm.close();
			return;
		}
		buyer.setMoney(buyer.getMoney() - buy.getPoints());
		buyer.setPoints(buyer.getPoints() + 10);
		AchievementsManager.userValueIncreased(pm.detachCopy(buyer));
		// Check for level up
		buyer.setLevel(UsersManager.calculateLevel(buyer.getLevel(), buyer.getPoints()));
		if (buy.getOwner() > 0) {
			try {
				User preOwner = pm.getObjectById(User.class, buy.getOwner());
				preOwner.setMoney(preOwner.getMoney() + buy.getPoints());
			} catch (JDOObjectNotFoundException e) {
			}
		}
		buy.setPoints(buy.getPoints() + 20);
		AchievementsManager.userValueIncreased(pm.detachCopy(buy));
		// Check for level up
		buy.setLevel(UsersManager.calculateLevel(buy.getLevel(), buy.getPoints()));
		buy.setOwner(userID);
		pm.makePersistent(new Action(userID, buyID, new Date()));
		AchievementsManager.purchaseMade(pm.detachCopy(buyer), pm.detachCopy(buy));
		pm.close();
		response.getWriter().println("Purchase Done");

		Builder msg = new Builder();
		msg.addData("type", NotificationType.BUY.toString());
		msg.addData("userID", String.valueOf(userID));
		SendMessage.sendMessage(buyID, msg.build());
	}

	@SuppressWarnings("unchecked")
	private boolean isPurchaseLegal(User buyer, User buy) {
		if (buy.getOwner() == buyer.getId()) {
			log.severe("You already own the user you want to buy");
			return false;
		}
		if (buyer.getMoney() < buy.getPoints()) {
			log.severe("You don't have enough money to buy this user");
			return false;
		}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + buy.getId() + " && blockedID == " + buyer.getId());
		List<UserBlock> result1 = (List<UserBlock>) query.execute();
		query.closeAll();
		if (!result1.isEmpty()) {
			pm.close();
			log.severe("You are not allowed to buy this user");
			return false;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, -1);
		Date lastAllowed = cal.getTime();
		query = pm.newQuery(Action.class);
		query.setFilter("buyerID == " + buyer.getId() + " && boughtID == " + buy.getId() + " && date > lastAllowed");
		query.declareParameters("java.util.Date lastAllowed");
		List<Action> result2 = (List<Action>) query.execute(lastAllowed);
		query.closeAll();
		if (!result2.isEmpty()) {
			pm.close();
			log.severe("You are not allowed to buy this user");
			return false;
		}
		pm.close();
		return true;
	}

	@SuppressWarnings("unchecked")
	private void actionHistory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Action.class);
		query.setFilter("buyerID == " + userID);
		query.setOrdering("date desc");
		List<Action> result1 = (List<Action>) query.execute();
		query.closeAll();
		query = pm.newQuery(Action.class);
		query.setFilter("boughtID == " + userID);
		query.setOrdering("date desc");
		List<Action> result2 = (List<Action>) query.execute();
		query.closeAll();
		int i = 0, j = 0;
		ArrayList<ActionInfo> actions = new ArrayList<ActionInfo>();
		while (true)
			if (i < result1.size()) {
				if (j < result2.size()) {
					if (result1.get(i).getDate().after(result2.get(j).getDate())) {
						Action action = result1.get(i);
						User user;
						try {
							user = pm.getObjectById(User.class, action.getBoughtID());
							actions.add(new ActionInfo(user, true, action.getDate()));
						} catch (JDOObjectNotFoundException e) {
							log.severe("User " + action.getBoughtID() + " doesn't exist");
						}
						i++;
					} else {
						Action action = result2.get(j);
						User user;
						try {
							user = pm.getObjectById(User.class, action.getBuyerID());
							actions.add(new ActionInfo(user, false, action.getDate()));
						} catch (JDOObjectNotFoundException e) {
							log.severe("User " + action.getBuyerID() + " doesn't exist");
						}
						j++;
					}
				} else {
					Action action = result1.get(i);
					User user;
					try {
						user = pm.getObjectById(User.class, action.getBoughtID());
						actions.add(new ActionInfo(user, true, action.getDate()));
					} catch (JDOObjectNotFoundException e) {
						log.severe("User " + action.getBoughtID() + " doesn't exist");
					}
					i++;
				}
			} else if (j < result2.size()) {
				Action action = result2.get(j);
				User user;
				try {
					user = pm.getObjectById(User.class, action.getBuyerID());
					actions.add(new ActionInfo(user, false, action.getDate()));
				} catch (JDOObjectNotFoundException e) {
					log.severe("User " + action.getBuyerID() + " doesn't exist");
				}
				j++;
			} else
				break;
		pm.close();
		response.getWriter().println(new Gson().toJson(actions));
	}
}
