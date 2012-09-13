package com.teamagly.friendizer;

import java.io.IOException;
import java.util.*;
import java.util.logging.*;

import javax.jdo.*;

import com.google.android.gcm.server.*;
import com.google.android.gcm.server.Constants;
import com.teamagly.friendizer.model.UserDevice;

/**
 * Send a message using GCM
 */
public class SendMessage {
	private static final Logger log = Logger.getLogger(SendMessage.class.getName());
	private static final String SENDER_ID = "AIzaSyA52pp613NNTl8BncGIh0wDbCDPb78y5X0"; // GCM sender ID

	@SuppressWarnings("unchecked")
	public static void sendMessage(long userIDParam, Message msg) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(UserDevice.class);
		q.setFilter("userID == userIDParam");
		q.declareParameters("long userIDParam");
		List<UserDevice> results = (List<UserDevice>) q.execute(userIDParam);
		if (!results.isEmpty()) {
			Sender sender = new Sender(SENDER_ID);
			if (results.size() == 1)
				sendSingleMessage(msg, results.get(0).getRegID(), userIDParam, sender);
			else {
				List<String> regIDs = new ArrayList<String>();
				for (UserDevice device : results)
					regIDs.add(device.getRegID());
				sendMulticastMessage(msg, regIDs, userIDParam, sender);
			}
		} else
			log.warning("no devices for user" + userIDParam);
		pm.close();
	}

	private static void sendMulticastMessage(Message message, List<String> regIDs, long userIDParam, Sender sender) {
		log.info("Sending message to uid " + userIDParam);
		MulticastResult multicastResult;
		try {
			multicastResult = sender.send(message, regIDs, 3);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception posting " + message, e);
			return;
		}
		// check if any registration id must be updated
		if (multicastResult.getCanonicalIds() != 0) {
			List<Result> results = multicastResult.getResults();
			PersistenceManager pm = PMF.get().getPersistenceManager();
			for (int i = 0; i < results.size(); i++) {
				String canonicalRegId = results.get(i).getCanonicalRegistrationId();
				if (canonicalRegId != null) {
					String regIDParam = regIDs.get(i);
					try {
						pm.deletePersistent(pm.getObjectById(UserDevice.class, regIDParam)); // Delete the regID
					} catch (Exception e) {
						log.severe("Couldn't delete old reg ID: " + regIDParam);
					}
					log.info("updating reg id: " + regIDParam + " for uid " + userIDParam);
					// same device has more than on registration id: update it
					// Replace the current regID with the canonical one if it doesn't already exists
					UserDevice device = null;
					try {
						device = pm.getObjectById(UserDevice.class, canonicalRegId);
						if (device.getUserID() != userIDParam) // Update the user ID if necessary
							device.setUserID(userIDParam);
						device.setUserID(userIDParam); // Update the user ID
					} catch (JDOObjectNotFoundException e) { // Add the canonical ID
						device = new UserDevice(canonicalRegId, userIDParam);
						pm.makePersistent(device);
					}
				}
			}
			pm.close();
		}
		if (multicastResult.getFailure() != 0) {
			// there were failures, check if any could be retried
			List<Result> results = multicastResult.getResults();
			PersistenceManager pm = PMF.get().getPersistenceManager();
			for (int i = 0; i < results.size(); i++) {
				String error = results.get(i).getErrorCodeName();
				if (error != null)
					if (error.equals(Constants.ERROR_NOT_REGISTERED) || error.equals(Constants.ERROR_INVALID_REGISTRATION)) {
						// The app has been removed from device, so unregister it
						String regIDParam = regIDs.get(i);
						try {
							pm.deletePersistent(pm.getObjectById(UserDevice.class, regIDParam)); // Delete the regID
						} catch (Exception e) {
							log.severe("Couldn't delete old reg ID: " + regIDParam);
						}
					} else
						log.warning("Got an unexpected error: " + error);
			}
			pm.close();
		}
	}

	private static void sendSingleMessage(Message message, String regIDParam, long userIDParam, Sender sender) {
		log.info("Sending message to uid " + userIDParam + ", to device " + regIDParam);
		Result result;
		try {
			result = sender.send(message, regIDParam, 3);
		} catch (IOException e) {
			log.severe("Exception posting " + message + ", " + e.getMessage());
			return;
		}
		if (result == null)
			return;
		if (result.getMessageId() != null) {
			log.info("Succesfully sent message to device " + regIDParam);
			String canonicalRegId = result.getCanonicalRegistrationId();
			if (canonicalRegId != null) {
				log.finest("canonicalRegId " + canonicalRegId);
				// same device has more than one registration id: delete the old one and add the canonical one
				PersistenceManager pm = PMF.get().getPersistenceManager();
				try {
					pm.deletePersistent(pm.getObjectById(UserDevice.class, regIDParam)); // Delete the regID
				} catch (Exception e) {
					log.severe("Couldn't delete old reg ID: " + regIDParam);
				}
				try {
					UserDevice device = pm.getObjectById(UserDevice.class, canonicalRegId);
					if (device.getUserID() != userIDParam) // Update the user ID if necessary
						device.setUserID(userIDParam);
				} catch (JDOObjectNotFoundException e) {
					// Add the canonical ID
					UserDevice device = new UserDevice(canonicalRegId, userIDParam);
					pm.makePersistent(device);
				} finally {
					pm.close();
				}
			}
		} else {
			String error = result.getErrorCodeName();
			if (error.equals(Constants.ERROR_NOT_REGISTERED) || error.equals(Constants.ERROR_INVALID_REGISTRATION)) {
				PersistenceManager pm = PMF.get().getPersistenceManager();
				// The app has been removed from device, so unregister it
				try {
					pm.deletePersistent(pm.getObjectById(UserDevice.class, regIDParam)); // Delete the regID
				} catch (Exception e) {
					log.severe("Couldn't delete old reg ID: " + regIDParam);
				} finally {
					pm.close();
				}
			} else
				log.warning("Got an unexpected error: " + error);
		}
	}
}
