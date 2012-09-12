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
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());

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
		// Check for level up
		buyer.setLevel(calculateLevel(buyer.getLevel(), buyer.getPoints()));
		if (buy.getOwner() > 0) {
			try {
				User preOwner = pm.getObjectById(User.class, buy.getOwner());
				preOwner.setMoney(preOwner.getMoney() + buy.getPoints());
			} catch (JDOObjectNotFoundException e) {
			}
		}
		buy.setPoints(buy.getPoints() + 20);
		// Check for level up
		buy.setLevel(calculateLevel(buy.getLevel(), buy.getPoints()));
		buy.setOwner(userID);
		pm.makePersistent(new Action(userID, buyID, new Date()));
		giveAchievements(pm.detachCopy(buyer), pm.detachCopy(buy));
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
	private void giveAchievements(User buyer, User buy) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + buyer.getId() + " && achievementID == 28001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		
		if (result.isEmpty()) {
			// Get the achievement from the database
			Achievement achv;
			try {
				achv = pm.getObjectById(Achievement.class, 28001);
			} catch (JDOObjectNotFoundException e) {
				pm.close();
				log.severe("This achievement doesn't exist");
				return;
			}
			
			// Reward the user with money
			buyer.setMoney(buyer.getMoney() + achv.getReward());
			// Reward the user with points
			buyer.setPoints(buyer.getPoints() + achv.getPoints());
			// check for level up
			buyer.setLevel(calculateLevel(buyer.getLevel(), buyer.getPoints()));
			pm.makePersistent(buyer); // Necessary since the user is detached
			
			// Check for another achievement
			userValueIncreased(buyer);
			
			pm.makePersistent(new UserAchievement(buyer.getId(), achv.getId()));
			notificate(buyer, achv);
		}
		
		query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + buy.getId() + " && achievementID == 30001");
		result = (List<UserAchievement>) query.execute();
		query.closeAll();
		
		if (result.isEmpty()) {
			Achievement achv;
			// Get the achievement from the database
			try {
				achv = pm.getObjectById(Achievement.class, 30001);
			} catch (JDOObjectNotFoundException e) {
				pm.close();
				log.severe("This achievement doesn't exist");
				return;
			}
			
			// Reward the user with money
			buy.setMoney(buy.getMoney() + achv.getReward());
			// Reward the user with points
			buy.setPoints(buy.getPoints() + achv.getPoints());
			// check for level up
			buy.setLevel(calculateLevel(buy.getLevel(), buy.getPoints()));
			pm.makePersistent(buy); // Necessary since the user is detached
			
			// Check for another achievement
			userValueIncreased(buy);
			
			pm.makePersistent(new UserAchievement(buy.getId(), achv.getId()));
			notificate(buy, achv);
		}
		
		pm.close();
	}
	
	@SuppressWarnings("unchecked")
	private void userValueIncreased(User user) {
		if (user.getPoints() < 1000)
			return;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Query query = pm.newQuery(UserAchievement.class);
		query.setFilter("userID == " + user.getId() + " && achievementID == 29001");
		List<UserAchievement> result = (List<UserAchievement>) query.execute();
		query.closeAll();
		
		if (result.isEmpty()) {
			// Get the achievement from the database
			Achievement achv;
			try {
				achv = pm.getObjectById(Achievement.class, 29001);
			} catch (JDOObjectNotFoundException e) {
				pm.close();
				log.severe("This achievement doesn't exist");
				return;
			}
	
			// Reward the user with money
			user.setMoney(user.getMoney() + achv.getReward());
			// Reward the user with points
			user.setPoints(user.getPoints() + achv.getPoints());
			// check for level up
			user.setLevel(calculateLevel(user.getLevel(), user.getPoints()));
			pm.makePersistent(user); // Necessary since the user is detached
	
			pm.makePersistent(new UserAchievement(user.getId(), achv.getId()));
			notificate(user, achv);
		}
		
		pm.close();
	}
	
	/**
	 * 
	 * The function calculates the level according to the given points
	 * 
	 * @param currentLevel
	 *            - the current level of the user
	 * @param points
	 *            - the updated points of the user
	 * @return the new level of the user (his current level if the points are not enough and the next level otherwise)
	 */
	private static int calculateLevel(int currentLevel, long points) {
		// Calculate the threshold for next level
		double threshold = 200 * Math.pow(currentLevel, 1.5);

		// If the user has enough points for the next level - return the next level
		if (points >= threshold)
			return currentLevel + 1;
		else
			return currentLevel;
	}
	
	private static void notificate(User user, Achievement achv) {
		Builder msg = new Builder();
		msg.addData("type", NotificationType.ACH.toString());
		msg.addData("userID", String.valueOf(user.getId()));
		msg.addData("title", achv.getTitle());
		msg.addData("iconRes", achv.getIconRes());
		SendMessage.sendMessage(user.getId(), msg.build());
	}
	
	@SuppressWarnings("unchecked")
	private void actionHistory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long userID = Long.parseLong(request.getParameter("userID"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.getObjectById(User.class, userID); // Check if the user exists
		} catch (JDOObjectNotFoundException e) {
			pm.close();
			log.severe("User doesn't exist");
			return;
		}
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
						}
						i++;
					} else {
						Action action = result2.get(j);
						User user;
						try {
							user = pm.getObjectById(User.class, action.getBuyerID());
							actions.add(new ActionInfo(user, false, action.getDate()));
						} catch (JDOObjectNotFoundException e) {
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
				}
				j++;
			} else
				break;
		}
		pm.close();
		response.getWriter().println(new Gson().toJson(actions));
	}
}
