package com.teamagly.friendizer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;

import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.UserInfo;

public final class ServerFacade {
    private static final String serverAddress = "http://friendizer.appspot.com/";

    private ServerFacade() {
    }

    public static void register(long userID) throws Exception {
	URL url = new URL(serverAddress + "register?userID=" + userID);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	in.close();
	// Get the user's details and update the userInfo object
	if (Utility.getInstance().userInfo == null)
	    Utility.getInstance().userInfo = userDetails(userID);
	else
	    Utility.getInstance().userInfo.updateFriendizerData(userDetails(userID));
    }

    public static UserInfo userDetails(long userID) throws Exception {
	URL url = new URL(serverAddress + "userDetails?userID=" + userID);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	User user = new User(in.readLine());
	in.close();
	return new UserInfo(userID, user.getValue(), user.getMoney(), user.getOwner(), ownList(userID));
    }

    public static long[] ownList(long userID) throws Exception {
	URL url = new URL(serverAddress + "ownList?userID=" + userID);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	JSONArray users = new JSONArray(in.readLine());
	in.close();
	long[] usersID = new long[users.length()];
	for (int i = 0; i < users.length(); i++) {
	    User user = new User(users.getString(i));
	    usersID[i] = user.getId();
	}
	return usersID;
    }

    public static void buy(long userID, long buyID) throws Exception {
	URL url = new URL(serverAddress + "buy?userID=" + userID + "&buyID=" + buyID);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	in.close();
    }

    public static void changeLocation(long userID, double latitude, double longitude) throws Exception {
	URL url = new URL(serverAddress + "changeLocation?userID=" + userID + "&latitude=" + latitude + "&longitude=" + longitude);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	in.close();
    }

    public static long[] nearbyUsers(long userID) throws JSONException, IOException {
	URL url = new URL(serverAddress + "nearbyUsers?userID=" + userID);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	JSONArray users = new JSONArray(in.readLine());
	in.close();
	long[] usersID = new long[users.length()];
	for (int i = 0; i < users.length(); i++) {
	    User user = new User(users.getString(i));
	    usersID[i] = user.getId();
	}
	return usersID;
    }
}
