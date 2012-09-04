package com.teamagly.friendizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.teamagly.friendizer.model.UserDevice;

/**
 * Send a message using GCM
 */
public class SendMessage {

	private static final Logger log = Logger.getLogger(SendMessage.class.getName());

	public static void sendMessage(long userIDParam, Message msg) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(UserDevice.class);
		q.setFilter("userID == userIDParam");
		q.declareParameters("long userIDParam");
		@SuppressWarnings("unchecked")
		List<UserDevice> results = (List<UserDevice>) q.execute(userIDParam);
		if (!results.isEmpty()) {
			Sender sender = new Sender(Util.SENDER_ID);
			if (results.size() == 1)
				sendSingleMessage(msg, results.get(0).getRegID(), sender);
			else {
				List<String> regIDs = new ArrayList<String>();
				for (UserDevice device : results)
					regIDs.add(device.getRegID());
				sendMulticastMessage(msg, regIDs, sender);
			}
		} else {
			log.warning("no devices for user" + userIDParam);
		}
		pm.close();
	}

	private static void sendMulticastMessage(Message message, List<String> regIDs, Sender sender) {
		// Recover registration ids from datastore
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
					long regID = new Long(regIDs.get(i));
					// same device has more than on registration id: update it
					log.finest("canonicalRegId " + canonicalRegId);
					try {
						UserDevice device = pm.getObjectById(UserDevice.class, regID);
						device.setRegID(canonicalRegId);
						pm.makePersistent(device);
					} catch (Exception e) {
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
				if (error != null) {
					String regId = regIDs.get(i);
					log.warning("Got error (" + error + ") for regId " + regId);
					if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
						// The app has been removed from device, so unregister it
						try {
							Query query = pm.newQuery(UserDevice.class);
							query.setFilter(Util.REG_ID + " == regIDParam && " + Util.USER_ID + " == userIDParam");
							query.declareParameters("String regIDParam, String userIDParam");
							query.deletePersistentAll();
						} catch (Exception e) {
							log.severe("Error unregistering device: " + e.getMessage());
						}
					}
				}
			}
			pm.close();
		}
	}

	private static void sendSingleMessage(Message message, String regIDParam, Sender sender) {
		log.info("Sending message to device " + regIDParam);
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
				// same device has more than on registration id: update it
				log.finest("canonicalRegId " + canonicalRegId);
				PersistenceManager pm = PMF.get().getPersistenceManager();
				UserDevice device;
				try {
					device = pm.getObjectById(UserDevice.class, new Long(regIDParam));
					device.setRegID(canonicalRegId);
					pm.makePersistent(device);
				} catch (Exception e) {
					log.warning(e.getMessage());
				}
				pm.close();
			}
		} else {
			String error = result.getErrorCodeName();
			if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
				PersistenceManager pm = PMF.get().getPersistenceManager();
				try {
					// The app has been removed from device, so unregister it
					Query query = pm.newQuery(UserDevice.class);
					query.setFilter(Util.REG_ID + " == regIDParam && " + Util.USER_ID + " == userIDParam");
					query.declareParameters("String regIDParam, String userIDParam");
					query.deletePersistentAll();
				} catch (Exception e) {
					log.severe("Error unregistering device: " + e.getMessage());
				} finally {
					pm.close();
				}
			} else {
				log.severe("Error sending message to device " + regIDParam + ": " + error);
			}
		}
	}

}
