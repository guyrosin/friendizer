package com.teamagly.friendizer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

import javax.jdo.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.gson.stream.JsonReader;
import com.restfb.*;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.exception.FacebookException;
import com.restfb.json.JsonObject;
import com.restfb.util.StringUtils;
import com.teamagly.friendizer.model.User;

@SuppressWarnings("serial")
public class FacebookSubscriptionsManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());
	private static final String APP_ID = "273844699335189"; // Facebook app ID
	private static final String APP_SECRET = "b2d90b5989dfdf082742e12d365053b9"; // Facebook app secret
	private static final String BASE_URL = "http://friendizer.appspot.com/";
	private static final String VERIFY_TOKEN = "FRIENDIZER";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String address = request.getRequestURI();
		String servlet = address.substring(address.lastIndexOf("/") + 1);
		if (servlet.intern() == "addSubscription")
			;
		// addSubscription("name,gender,birthday,picture");
		else if (servlet.intern() == "deleteSubscriptions")
			;
		// deleteSubscriptions();
		else if (servlet.intern() == "listSubscriptions")
			listSubscriptions(response.getWriter());
		else if (servlet.intern() == "updateUsers")
			updateUsers();
		else if (servlet.intern() == "extendAccessTokens")
			extendAccessTokens();
		else {
			// Subscription Verification
			String mode = request.getParameter("hub.mode"); // This is always the string "subscribe"
			String challenge = request.getParameter("hub.challenge"); // This is a random string
			String verifyToken = request.getParameter("hub.verify_token");
			if ((verifyToken.equals(VERIFY_TOKEN)) && (mode.equals("subscribe")))
				response.getWriter().write(challenge); // Return the challenge value
		}
	}

	@SuppressWarnings("unused")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Change Notifications
		ArrayList<String> fields = new ArrayList<String>();
		// Read and log the whole request
		/*
		 * BufferedReader reader2 = req.getReader();
		 * String line;
		 * while ((line = reader2.readLine()) != null) {
		 * log.severe(line);
		 * }
		 * reader2.close();
		 */

		// Decode
		JsonReader reader = new JsonReader(request.getReader());
		reader.beginObject();
		String objectName = reader.nextName();
		String object = reader.nextString();
		if (object.equals("user")) { // User object update
			String entryName = reader.nextName();
			if (entryName.equals("entry")) {
				reader.beginArray();
				while (reader.hasNext()) {
					reader.beginObject();
					String uidName = reader.nextName();
					String uid = reader.nextString();
					String idName = reader.nextName();
					String id = reader.nextString();
					String timeName = reader.nextName();
					String time = reader.nextString();
					String changed_fieldsName = reader.nextName();
					if (changed_fieldsName.equals("changed_fields")) {
						reader.beginArray();
						while (reader.hasNext())
							fields.add(reader.nextString());
						reader.endArray();
						reader.endObject();
						// Request the changed data from Facebook
						requestFBData(uid, fields);
					} else {
						reader.close();
						log.severe("unexpected changed_fieldsName: " + changed_fieldsName);
						return;
					}
				}
			}
			reader.endArray();
			reader.endObject();
		}
		reader.close();
	}

	private void requestFBData(String userID, List<String> fields) {
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
			JsonObject jsonObject = facebook.fetchObject(userID, JsonObject.class, Parameter.with("fields", StringUtils.join(fields)));
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

	public void addSubscription(String fields) {
		FacebookClient facebook = new DefaultFacebookClient();
		AccessToken accessToken = facebook.obtainAppAccessToken(APP_ID, APP_SECRET);
		String url = "https://graph.facebook.com/" + APP_ID + "/subscriptions";
		String charset = "UTF-8";
		String object = "user";
		String callbackURL = BASE_URL + "facebookSubscriptions";
		OutputStream output = null;
		try {
			String query = String.format("access_token=%s&object=%s&fields=%s&callback_url=%s&verify_token=%s&method=post", URLEncoder.encode(accessToken.getAccessToken(), charset), URLEncoder.encode(object, charset), URLEncoder.encode(fields, charset), URLEncoder.encode(callbackURL, charset), URLEncoder.encode(VERIFY_TOKEN, charset));
			url = url + "?" + query;

			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoOutput(true); // Triggers POST.
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			// Fire the request
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				log.severe("Failed to add a subscription to Facebook: " + connection.getResponseCode());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException logOrIgnore) {
				}
			}
		}
	}

	public void deleteSubscriptions() {
		FacebookClient facebook = new DefaultFacebookClient();
		AccessToken accessToken = facebook.obtainAppAccessToken(APP_ID, APP_SECRET);
		String url = "https://graph.facebook.com/" + APP_ID + "/subscriptions";
		String charset = "UTF-8";
		try {
			url = url + "?access_token=" + URLEncoder.encode(accessToken.getAccessToken(), charset);
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("DELETE");
			connection.setRequestProperty("Accept-Charset", charset);
			// connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			// Fire the request
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				log.severe("Failed to delete subscriptions Facebook: " + connection.getResponseCode());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void listSubscriptions(PrintWriter writer) {
		FacebookClient facebook = new DefaultFacebookClient();
		AccessToken accessToken = facebook.obtainAppAccessToken(APP_ID, APP_SECRET);
		try {
			URL url = new URL("https://graph.facebook.com/" + APP_ID + "/subscriptions?access_token=" + accessToken.getAccessToken());
			writer.println("URL: " + url.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;

			while ((line = reader.readLine()) != null)
				writer.println(line);
			reader.close();

		} catch (MalformedURLException e) {
			// ...
		} catch (IOException e) {
			// ...
		}
	}

	@SuppressWarnings("unchecked")
	public void updateUsers() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		List<User> users = (List<User>) query.execute();
		query.closeAll();
		for (User user : users) {
			try {
				requestFBData(String.valueOf(user.getId()), Arrays.asList("name,gender,birthday,picture".split("\\s*,\\s*")));
			} catch (Exception e) {
				log.info(e.getMessage());
			}
		}
		pm.close();
	}

	@SuppressWarnings("unchecked")
	private void extendAccessTokens() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		List<User> users = (List<User>) query.execute();
		query.closeAll();
		for (User user : users) {
			// Request an extended access token
			try {
				FacebookClient facebook = new DefaultFacebookClient(user.getToken());
				AccessToken accessToken = facebook.obtainExtendedAccessToken(APP_ID, APP_SECRET, user.getToken());
				user.setToken(accessToken.getAccessToken());
			} catch (Exception e) {
				log.info("Error when extending access token for " + user.getId() + ": " + e.getMessage());
			}
		}
		pm.close();
	}
}
