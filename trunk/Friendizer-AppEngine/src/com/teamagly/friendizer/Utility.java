package com.teamagly.friendizer;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.exception.FacebookException;
import com.restfb.json.JsonObject;
import com.restfb.util.StringUtils;
import com.teamagly.friendizer.model.User;

public class Utility {
	private static final Logger log = Logger.getLogger(Utility.class.getName());

	static final String APP_ID = "273844699335189"; // Facebook app ID
	static final String APP_SECRET = "b2d90b5989dfdf082742e12d365053b9"; // Facebook app secret
	static final String BASE_URL = "http://friendizer.appspot.com/";

	static void requestFBData(String userID, List<String> fields) {
		User user;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			user = pm.getObjectById(User.class, Long.parseLong(userID));
		} catch (JDOObjectNotFoundException e) {
			log.severe("User doesn't exist");
			pm.close();
			return;
		}

		FacebookClient facebook = new DefaultFacebookClient(user.getToken());
		// Request those fields from Facebook
		// Note: using JsonObject instead of User object in case we want the profile picture
		try {
			log.info("Requesting update for uid " + userID + " for " + StringUtils.join(fields));
			JsonObject jsonObject = facebook.fetchObject(userID, JsonObject.class,
					Parameter.with("fields", StringUtils.join(fields)));
			user.updateFacebookData(jsonObject);
			pm.makePersistent(user);
			pm.close();
		} catch (FacebookException e) {
			user.setFbUpdate(true); // Mark that this user's data has not been updated yet
			pm.makePersistent(user);
			pm.close();
			log.info("Couldn't get data for uid " + userID + ": " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static void updateUsers() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		List<User> users = (List<User>) query.execute();
		query.closeAll();
		for (User user : users)
			try {
				requestFBData(String.valueOf(user.getId()),
						Arrays.asList("name,gender,birthday,picture,email".split("\\s*,\\s*")));
			} catch (Exception e) {
				log.info(e.getMessage());
			}
		pm.close();
	}

	@SuppressWarnings("unchecked")
	static void extendAccessTokens() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		List<User> users = (List<User>) query.execute();
		query.closeAll();
		for (User user : users)
			// Request an extended access token
			try {
				FacebookClient facebook = new DefaultFacebookClient(user.getToken());
				AccessToken accessToken = facebook.obtainExtendedAccessToken(Utility.APP_ID, Utility.APP_SECRET, user.getToken());
				user.setToken(accessToken.getAccessToken());
			} catch (Exception e) {
				log.info("Error when extending access token for " + user.getId() + ": " + e.getMessage());
			}
		pm.close();
	}

	/**
	 * @param user
	 *            a detached User object
	 */
	static void extendAccessToken(User user) {
		// Request an extended access token
		try {
			FacebookClient facebook = new DefaultFacebookClient(user.getToken());
			AccessToken accessToken = facebook.obtainExtendedAccessToken(Utility.APP_ID, Utility.APP_SECRET, user.getToken());
			user.setToken(accessToken.getAccessToken());
			user.setTokenExpires(accessToken.getExpires().getTime());
			log.info("Extended the access token: " + accessToken.getAccessToken() + ", expires: " + accessToken.getExpires());
		} catch (Exception e) {
			log.info("Error when extending access token for " + user.getId() + ": " + e.getMessage());
		}
	}
}
