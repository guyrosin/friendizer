package com.teamagly.friendizer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.model.Action;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.Gift;
import com.teamagly.friendizer.model.Message;

public final class ServerFacade {
    private static final String fullServerAddress = "http://friendizer.appspot.com/";
    private static final String scheme = "http";
    private static final String serverAddress = "friendizer.appspot.com";

    private ServerFacade() {
    }

    public static FriendizerUser login(long userID, String accessToken, String deviceRegistrationID, Context context)
	    throws JSONException, IOException {
	URL url = new URL(fullServerAddress + "login?userID=" + userID + "&accessToken=" + accessToken + "&"
		+ Util.DEVICE_REGISTRATION_ID + "=" + deviceRegistrationID);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	FriendizerUser user = new FriendizerUser(new JSONObject(in.readLine()));
	in.close();
	user.setOwnsList(ownList(userID));
	return user;
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
}
