package com.teamagly.friendizer.utils;

import java.io.*;
import java.net.URL;

import org.json.*;

import com.teamagly.friendizer.model.*;

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
	User user = new User(new JSONObject(in.readLine()));
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
	    User user = new User(users.getJSONObject(i));
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
	    User user = new User(users.getJSONObject(i));
	    usersID[i] = user.getId();
	}
	return usersID;
    }
    
    public static void sendMessage(long source, long destination, String text) throws Exception {
		URL url = new URL(serverAddress + "send?src=" + source
				+ "&dest=" + destination + "&text=" + text);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		in.close();
	}
    
    public static Message[] readMessages(long source, long destination,
    		long from, long to) throws Exception {
    	
		URL url = new URL(serverAddress + "read?src=" + source
				+ "&dest=" + destination + "&from=" + from + "&to=" + to);
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
    
    public static Message[] readUnreaded(long source) throws Exception {
		URL url = new URL(serverAddress + "unread?src=" + source);
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
    
    public static AchievementInfo[] achievements(long userID) throws Exception {
    	URL url = new URL(serverAddress + "nearbyUsers?userID=" + userID);
    	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
    	JSONArray userAchvs = new JSONArray(in.readLine());
    	in.close();
    	AchievementInfo[] achvs = new AchievementInfo[userAchvs.length()];
    	for (int i = 0; i < userAchvs.length(); i++)
    		achvs[i] = new AchievementInfo(userAchvs.getJSONObject(i));
    	return achvs;
    }
}
