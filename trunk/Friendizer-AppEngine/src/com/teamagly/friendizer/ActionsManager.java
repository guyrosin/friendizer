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

	/**
	 * Buy a user.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void buy(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		long buyID = Long.parseLong(request.getParameter("buyID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Get the buyer
		User buyer;
		try {
			buyer = pm.getObjectById(User.class, userID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
		// Get the user bought
		User buy;
		try {
			buy = pm.getObjectById(User.class, buyID);
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("The user you want to buy doesn't exist");
			return;
		}
		// Check if the buyer can buy the user
		if (!isPurchaseLegal(buyer, buy)) {
			pm.close();
			return;
		}
		// Decrease the buyer money
		buyer.setMoney(buyer.getMoney() - buy.getPoints());
		// Increase the buyer points
		buyer.setPoints(buyer.getPoints() + 10);
		// Check for level up
		buyer.setLevel(UsersManager.calculateLevel(buyer.getLevel(), buyer.getPoints()));
		// Increase the number of users the buyer owns
		buyer.setOwnsNum(buyer.getOwnsNum() + 1);
		// Check if user bought has an owner
		if (buy.getOwner() > 0) {
			try {
				User preOwner = pm.getObjectById(User.class, buy.getOwner());
				// Increase the previous owner money
				preOwner.setMoney(preOwner.getMoney() + buy.getPoints());
				// Decrease the previous owner owns number
				preOwner.setOwnsNum(preOwner.getOwnsNum() - 1);
			} catch (JDOObjectNotFoundException e) {
				log.severe("User " + buy.getOwner() + " doesn't exist");
			}
		}
		// Increase the user bought points
		buy.setPoints(buy.getPoints() + 20);
		// Check for level up
		buy.setLevel(UsersManager.calculateLevel(buy.getLevel(), buy.getPoints()));
		// Set the new owner to be the buyer
		buy.setOwner(userID);
		// Add the action to the DB
		pm.makePersistent(new Action(userID, buyID, new Date()));
		// Give the achievements
		AchievementsManager.userValueIncreased(pm.detachCopy(buyer));
		AchievementsManager.userValueIncreased(pm.detachCopy(buy));
		AchievementsManager.purchaseMade(pm.detachCopy(buyer), pm.detachCopy(buy));
		pm.close();
		response.getWriter().println("Purchase Done");
		// Send notification to the user bought device
		Builder msg = new Builder();
		msg.addData("type", NotificationType.BUY.toString());
		msg.addData("userID", String.valueOf(userID));
		SendMessage.sendMessage(buyID, msg.build());
	}

	/**
	 * Check if the buyer can buy the user bought.
	 * 
	 * @param buyer
	 *            The buyer
	 * @param buy
	 *            The user bought
	 * @return true if it is legal, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	private boolean isPurchaseLegal(User buyer, User buy) {
		// Check if the buyer is already the owner
		if (buy.getOwner() == buyer.getId()) {
			log.severe("You already own the user you want to buy");
			return false;
		}
		// Check if the buyer has enough money
		if (buyer.getMoney() < buy.getPoints()) {
			log.severe("You don't have enough money to buy this user");
			return false;
		}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Check if the user bought hasn't blocked the buyer
		Query query = pm.newQuery(UserBlock.class);
		query.setFilter("userID == " + buy.getId() + " && blockedID == " + buyer.getId());
		List<UserBlock> result1 = (List<UserBlock>) query.execute();
		query.closeAll();
		if (!result1.isEmpty()) {
			pm.close();
			log.severe("You are not allowed to buy this user");
			return false;
		}
		// Check if the buyer bought the same user an hour ago
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
		// Passed all the tests
		return true;
	}

	/**
	 * Get the action history of a user.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void actionHistory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Get the actions in which the user is the buyer
		Query query = pm.newQuery(Action.class);
		query.setFilter("buyerID == " + userID);
		query.setOrdering("date desc");
		List<Action> result1 = (List<Action>) query.execute();
		query.closeAll();
		// Get the acions in which the user is being bought
		query = pm.newQuery(Action.class);
		query.setFilter("boughtID == " + userID);
		query.setOrdering("date desc");
		List<Action> result2 = (List<Action>) query.execute();
		query.closeAll();
		// Sort all the actions by the date
		int i = 0, j = 0;
		ArrayList<ActionInfo> actions = new ArrayList<ActionInfo>();
		while (true) {
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
		}
		pm.close();
		response.getWriter().println(new Gson().toJson(actions));
	}
}
