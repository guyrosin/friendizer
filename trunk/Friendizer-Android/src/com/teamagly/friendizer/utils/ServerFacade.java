package com.teamagly.friendizer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.teamagly.friendizer.FriendizerApp;
import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.model.Action;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.Gift;
import com.teamagly.friendizer.model.Message;

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

	public static FriendizerUser login(long userID, String accessToken, String regID) {

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
				FriendizerUser user = new FriendizerUser(new JSONObject(res));
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
			} catch (JSONException e) {
				Log.e(TAG, "JSON exception while logging in", e);
				return null;
			}
		}
		return null;
	}

	public static FriendizerUser userDetails(long userID) throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "userDetails?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		FriendizerUser user = new FriendizerUser(new JSONObject(in.readLine()));
		in.close();
		user.setOwnsList(ownList(userID));
		return user;
	}

	public static FriendizerUser[] ownList(long userID) throws JSONException, IOException {
		URL url = new URL(fullServerAddress + "ownList?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		JSONArray users = new JSONArray(in.readLine());
		in.close();
		FriendizerUser[] fzUsers = new FriendizerUser[users.length()];
		for (int i = 0; i < users.length(); i++) {
			FriendizerUser user = new FriendizerUser(users.getJSONObject(i));
			fzUsers[i] = user;
		}
		return fzUsers;
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

	public static FriendizerUser[] nearbyUsers(long userID) throws JSONException, IOException {
		URL url = new URL(fullServerAddress + "nearbyUsers?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		JSONArray users = new JSONArray(in.readLine());
		in.close();
		FriendizerUser[] fzUsers = new FriendizerUser[users.length()];
		for (int i = 0; i < users.length(); i++) {
			FriendizerUser user = new FriendizerUser(users.getJSONObject(i));
			fzUsers[i] = user;
		}
		return fzUsers;
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
	public static Message[] getUnread() throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "getUnread?userID=" + Utility.getInstance().userInfo.getId());
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		JSONArray JsonMessages = new JSONArray(in.readLine());
		in.close();

		Message[] messages = new Message[JsonMessages.length()];

		for (int i = 0; i < JsonMessages.length(); i++) {
			Message message = new Message(JsonMessages.getJSONObject(i));
			messages[i] = message;
		}

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
	public static ArrayList<Message> getConversation(long userID, long from, long to) throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "getConversation?user1=" + Utility.getInstance().userInfo.getId() + "&user2="
				+ userID + "&from=" + from + "&to=" + to);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		JSONArray JsonMessages = new JSONArray(in.readLine());
		in.close();

		ArrayList<Message> messages = new ArrayList<Message>(JsonMessages.length());

		for (int i = JsonMessages.length() - 1; i >= 0; i--) {
			Message message = new Message(JsonMessages.getJSONObject(i));
			messages.add(message);
		}

		return messages;

	}

	public static Achievement[] getAchievements(long userID) throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "achievements?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		JSONArray userAchvs = new JSONArray(in.readLine());
		in.close();
		Achievement[] achvs = new Achievement[userAchvs.length()];
		for (int i = 0; i < userAchvs.length(); i++)
			achvs[i] = new Achievement(userAchvs.getJSONObject(i));
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

	public static Action[] actionHistory(long userID) throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "actionHistory?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		JSONArray actions = new JSONArray(in.readLine());
		in.close();
		Action[] actionsArray = new Action[actions.length()];
		for (int i = 0; i < actions.length(); i++)
			actionsArray[i] = new Action(actions.getJSONObject(i));
		return actionsArray;
	}

	public static Gift[] getAllGifts() throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "allGifts");
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		JSONArray gifts = new JSONArray(in.readLine());
		in.close();
		Gift[] giftsArray = new Gift[gifts.length()];
		for (int i = 0; i < gifts.length(); i++)
			giftsArray[i] = new Gift(gifts.getJSONObject(i));
		return giftsArray;
	}

	public static Gift[] getUserGifts(long userID) throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "userGifts?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		JSONArray gifts = new JSONArray(in.readLine());
		in.close();
		Gift[] giftsArray = new Gift[gifts.length()];
		for (int i = 0; i < gifts.length(); i++)
			giftsArray[i] = new Gift(gifts.getJSONObject(i));
		return giftsArray;
	}

	public static void sendGift(long senderID, long receiverID, long giftID) throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "sendGift?senderID=" + senderID + "&receiverID=" + receiverID + "&giftID=" + giftID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		in.close();
	}

	public static void block(long userID, long blockedID) throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "block?userID=" + userID + "&blockedID=" + blockedID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		in.close();
	}

	public static FriendizerUser[] blockList(long userID) throws IOException, JSONException {
		URL url = new URL(fullServerAddress + "blockList?userID=" + userID);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		JSONArray blocked = new JSONArray(in.readLine());
		in.close();
		FriendizerUser[] blockedArray = new FriendizerUser[blocked.length()];
		for (int i = 0; i < blocked.length(); i++)
			blockedArray[i] = new FriendizerUser(blocked.getJSONObject(i));
		return blockedArray;
	}
}
