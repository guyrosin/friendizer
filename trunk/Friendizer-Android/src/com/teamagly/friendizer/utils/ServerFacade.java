package com.teamagly.friendizer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teamagly.friendizer.FriendizerApp;
import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.model.AchievementInfo;
import com.teamagly.friendizer.model.Action;
import com.teamagly.friendizer.model.Gift;
import com.teamagly.friendizer.model.GiftCount;
import com.teamagly.friendizer.model.Message;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.UserMatching;

public final class ServerFacade {
	private final static String TAG = "ServerFacade";
	private static final String fullServerAddress = "http://friendizer.appspot.com/";
	private static final String scheme = "http";
	private static final String serverAddress = "friendizer.appspot.com";
	private static final String REG_ID = "regID";
	private static final String USER_ID = "userID";
	private static final String ACCESS_TOKEN = "accessToken";

	private ServerFacade() {
	}

	private static final int MAX_ATTEMPTS = 3;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	/**
	 * Unregister this account/device pair within the server.
	 */
	public static void unregister(final String regID) {
		Log.i(TAG, "unregistering device (regId = " + regID + ")");
		String serverUrl = fullServerAddress + "unregister";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REG_ID, regID));
		try {
			post(serverUrl, params);
			GCMRegistrar.setRegisteredOnServer(FriendizerApp.getContext(), false);
		} catch (IOException e) {
			// At this point the device is unregistered from GCM, but still
			// registered in the server.
			// We could try to unregister again, but it is not necessary:
			// if the server tries to send a message to the device, it will get
			// a "NotRegistered" error message and should unregister the device.
		}
	}

	/**
	 * Issue a POST request to the server.
	 * 
	 * @param endpoint
	 *            POST address.
	 * @param params
	 *            request parameters.
	 * 
	 * @throws IOException
	 *             propagated from POST.
	 */
	private static String post(String endpoint, List<NameValuePair> params) throws IOException {
		HttpPost post = new HttpPost(endpoint);
		post.setEntity(new UrlEncodedFormEntity(params));

		HttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = client.execute(post);
		HttpEntity entity = httpResponse.getEntity();

		int status = httpResponse.getStatusLine().getStatusCode();
		if (status != 200)
			throw new IOException("Post failed with error code " + status);
		String response = "";
		if (entity != null)
			response = EntityUtils.toString(entity);
		return response;
	}

	public static User login(long userID, String accessToken, String regID) {

		String serverUrl = fullServerAddress + "login";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REG_ID, regID));
		params.add(new BasicNameValuePair(ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(USER_ID, String.valueOf(userID)));
		String res = "";
		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			Log.d(TAG, "Attempt #" + i + " to login");
			try {
				res = post(serverUrl, params);
				GCMRegistrar.setRegisteredOnServer(FriendizerApp.getContext(), true);
				User user = new Gson().fromJson(res, User.class);
				user.setOwnsList(ownList(userID));
				return user;
			} catch (IOException e) {
				// Here we are simplifying and retrying on any error; in a real
				// application, it should retry only on unrecoverable errors
				// (like HTTP error code 503).
				Log.e(TAG, "Failed to register on attempt " + i, e);
				if (i == MAX_ATTEMPTS) {
					break;
				}
				try {
					Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// Activity finished before we complete - exit.
					Log.d(TAG, "Thread interrupted: abort remaining retries!");
					Thread.currentThread().interrupt();
					return null;
				}
				// Increase backoff exponentially
				backoff *= 2;
			}
		}
		return null;
	}

	public static User userDetails(long userID) throws IOException {
		URL url = new URL(fullServerAddress + "userDetails?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		UserMatching userMatching = new Gson().fromJson(in.readLine(), UserMatching.class);
		// Convert from the server's UserMatching type to User
		User user = userMatching.getUser();
		user.setMatching(userMatching.getMatching());
		in.close();
		user.setOwnsList(ownList(userID));
		return user;
	}

	public static List<User> ownList(long userID) throws IOException {
		URL url = new URL(fullServerAddress + "ownList?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<User> users = new Gson().fromJson(in.readLine(), new TypeToken<List<User>>() {
		}.getType());
		in.close();
		return users;
	}

	public static void buy(long userID, long buyID) throws IOException {
		URL url = new URL(fullServerAddress + "buy?userID=" + userID + "&buyID=" + buyID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		in.close();
	}

	public static void changeLocation(long userID, double latitude, double longitude) throws IOException {
		URL url = new URL(fullServerAddress + "changeLocation?userID=" + userID + "&latitude=" + latitude + "&longitude="
				+ longitude);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		in.close();
	}

	public static int matching(long userID1, long userID2) throws IOException {
		URL url = new URL(fullServerAddress + "matching?userID1=" + userID1 + "&userID2=" + userID2);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		int matching = Integer.parseInt(in.readLine());
		in.close();
		return matching;
	}

	public static List<User> nearbyUsers(long userID) throws IOException {
		URL url = new URL(fullServerAddress + "nearbyUsers?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<User> users = new Gson().fromJson(in.readLine(), new TypeToken<List<User>>() {
		}.getType());
		in.close();
		return users;
	}

	public static void sendMessage(Message msg) throws IOException, URISyntaxException {
		String params = "src=" + Utility.getInstance().userInfo.getId() + "&dest=" + msg.getDestination() + "&text="
				+ msg.getText();
		// Use URI to escape characters (whitespace and non-ASCII characters)
		URI uri = new URI(scheme, serverAddress, "/send", params, null);
		URL url = new URL(uri.toASCIIString());
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		in.close();
	}

	/**
	 * @return all the unread messages sent to the current user
	 * @throws IOException
	 * @throws JSONException
	 * @throws Exception
	 */
	public static List<Message> getUnread() throws IOException {
		URL url = new URL(fullServerAddress + "getUnread?userID=" + Utility.getInstance().userInfo.getId());
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<Message> messages = new Gson().fromJson(in.readLine(), new TypeToken<List<Message>>() {
		}.getType());
		in.close();
		return messages;
	}

	/**
	 * @param userID
	 *            ID of a friend
	 * @param from
	 * @param to
	 * @return all the messages between the current user and the given one
	 * @throws IOException
	 * @throws JSONException
	 * @throws Exception
	 */
	public static List<Message> getConversation(long userID, long from, long to) throws IOException {
		URL url = new URL(fullServerAddress + "getConversation?user1=" + Utility.getInstance().userInfo.getId() + "&user2="
				+ userID + "&from=" + from + "&to=" + to);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<Message> messages = new Gson().fromJson(in.readLine(), new TypeToken<List<Message>>() {
		}.getType());
		in.close();
		Collections.reverse(messages);
		return messages;

	}

	public static List<Achievement> getAchievements(long userID) throws IOException {
		URL url = new URL(fullServerAddress + "achievements?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<AchievementInfo> achvInfos = new Gson().fromJson(in.readLine(), new TypeToken<List<AchievementInfo>>() {
		}.getType());
		in.close();
		// Convert from the server's AchievementInfo type to Achievement
		List<Achievement> achvs = new ArrayList<Achievement>();
		for (AchievementInfo achvInfo : achvInfos) {
			achvInfo.getAchv().setEarned(achvInfo.isEarned());
			achvs.add(achvInfo.getAchv());
		}
		return achvs;
	}

	public static void updateStatus(String status) throws IOException, URISyntaxException {
		String params = "userID=" + Utility.getInstance().userInfo.getId() + "&status=" + status;
		// Use URI to escape characters (whitespace and non-ASCII characters)
		URI uri = new URI(scheme, serverAddress, "/updateStatus", params, null);
		URL url = new URL(uri.toASCIIString());
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		in.close();
	}

	public static List<Action> actionHistory(long userID) throws IOException {
		URL url = new URL(fullServerAddress + "actionHistory?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<Action> actions = new Gson().fromJson(in.readLine(), new TypeToken<List<Action>>() {
		}.getType());
		in.close();
		return actions;
	}

	public static List<Gift> getAllGifts() throws IOException {
		URL url = new URL(fullServerAddress + "allGifts");
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<Gift> gifts = new Gson().fromJson(in.readLine(), new TypeToken<List<Gift>>() {
		}.getType());
		in.close();
		return gifts;
	}

	public static List<GiftCount> getUserGifts(long userID) throws IOException {
		URL url = new URL(fullServerAddress + "userGifts?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<GiftCount> gifts = new Gson().fromJson(in.readLine(), new TypeToken<List<GiftCount>>() {
		}.getType());
		in.close();
		if (gifts == null)
			gifts = new ArrayList<GiftCount>();
		return gifts;
	}

	public static void sendGift(long senderID, long receiverID, long giftID) throws IOException {
		URL url = new URL(fullServerAddress + "sendGift?senderID=" + senderID + "&receiverID=" + receiverID + "&giftID=" + giftID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		in.close();
	}

	public static void block(long userID, long blockedID) throws IOException {
		URL url = new URL(fullServerAddress + "block?userID=" + userID + "&blockedID=" + blockedID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		in.close();
	}

	public static List<User> blockList(long userID) throws IOException {
		URL url = new URL(fullServerAddress + "blockList?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<User> blockedUsers = new Gson().fromJson(in.readLine(), new TypeToken<List<User>>() {
		}.getType());
		in.close();
		return blockedUsers;
	}
}
