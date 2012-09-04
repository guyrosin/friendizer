/**
 * 
 */
package com.teamagly.friendizer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.stream.JsonReader;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.json.JsonObject;
import com.restfb.util.StringUtils;
import com.teamagly.friendizer.model.User;

@SuppressWarnings("serial")
public class FacebookSubscriptionsManager extends HttpServlet {
	private static final Logger log = Logger.getLogger(FacebookSubscriptionsManager.class.getName());

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
		if (servlet.intern() == "updateUsers")
			updateUsers();
		else {

			/*
			 * Subscription Verification
			 */
			String mode = request.getParameter("hub.mode"); // This is always the string "subscribe"
			String challenge = request.getParameter("hub.challenge"); // This is a random string
			String verifyToken = request.getParameter("hub.verify_token");
			if ((verifyToken.equals(VERIFY_TOKEN)) && (mode.equals("subscribe")))
				response.getWriter().write(challenge); // Return the challenge value
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		/*
		 * Change Notifications
		 */
		ArrayList<String> fields = new ArrayList<String>();
		JsonReader reader = new JsonReader(req.getReader());

		// Decode
		reader.beginObject();
		String object = reader.nextString();
		if (object.equals("user")) { // User object update
			String entry = reader.nextString();
			if (entry.equals("entry")) {
				reader.beginArray();
				String uid = reader.nextString();
				String changed_fields = reader.nextString();
				if (changed_fields.equals("changed_fields")) {
					reader.beginArray();
					while (reader.hasNext())
						fields.add(reader.nextString());
					reader.endArray();
				}
				@SuppressWarnings("unused")
				String time = reader.nextString();
				// Request the changed data from Facebook
				requestChangedData(uid, fields);
				reader.endArray();
			}
		}
		reader.close();
	}

	private void requestChangedData(String userID, List<String> fields) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		User user = pm.getObjectById(User.class, Long.parseLong(userID));
		pm.close();
		if (user == null) {
			log.severe("This user doesn't exist");
			return;
		}

		FacebookClient facebook = new DefaultFacebookClient(user.getToken());
		// Request those fields from Facebook
		// Note: using JsonObject instead of User object in case we want the profile picture
		JsonObject jsonObject = facebook
				.fetchObject(userID, JsonObject.class, Parameter.with("fields", StringUtils.join(fields)));
		user.updateFacebookData(jsonObject);
		pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(user);
		} finally {
			pm.close();
		}
	}

	public void addSubscription(String fields) {
		FacebookClient facebook = new DefaultFacebookClient();
		AccessToken accessToken = facebook.obtainAppAccessToken(Util.APP_ID, Util.APP_SECRET);
		String url = "https://graph.facebook.com/" + Util.APP_ID + "/subscriptions";
		String charset = "UTF-8";
		String object = "user";
		String callbackURL = Util.BASE_URL + "facebookSubscriptions";
		OutputStream output = null;
		try {
			String query = String.format("access_token=%s&object=%s&fields=%s&callback_url=%s&verify_token=%s&method=post",
					URLEncoder.encode(accessToken.getAccessToken(), charset), URLEncoder.encode(object, charset),
					URLEncoder.encode(fields, charset), URLEncoder.encode(callbackURL, charset),
					URLEncoder.encode(VERIFY_TOKEN, charset));
			url = url + "?" + query;
			// log.severe("URL: " + url);
			// log.severe("access token: " + accessToken.getAccessToken());

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
			if (output != null)
				try {
					output.close();
				} catch (IOException logOrIgnore) {
				}
		}
	}

	public void deleteSubscriptions() {
		FacebookClient facebook = new DefaultFacebookClient();
		facebook.obtainAppAccessToken(Util.APP_ID, Util.APP_SECRET);
		String url = "https://graph.facebook.com/" + Util.APP_ID + "/subscriptions";
		String charset = "UTF-8";
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoOutput(true); // Triggers POST.
			connection.setRequestMethod("DELETE");
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
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

	public void updateUsers() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		@SuppressWarnings("unchecked")
		List<User> users = (List<User>) query.execute();
		for (User user : users) {
			try {
				requestChangedData(String.valueOf(user.getId()), Arrays.asList("name,gender,birthday,picture".split("\\s*,\\s*")));
			} catch (Exception e) {
				log.severe(e.getMessage());
			}
		}
	}
}
