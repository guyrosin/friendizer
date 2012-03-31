package com.teamagly.friendizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

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
	long value = Long.parseLong(in.readLine());
	long money = Long.parseLong(in.readLine());
	long owner = Long.parseLong(in.readLine());
	in.close();
	return new UserInfo(userID, value, money, owner, ownList(userID));
    }

    public static long[] ownList(long userID) throws Exception {
	URL url = new URL(serverAddress + "ownList?userID=" + userID);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	ArrayList<Long> list = new ArrayList<Long>();
	String inputLine = in.readLine();
	while (inputLine != null) {
	    list.add(Long.parseLong(inputLine));
	    inputLine = in.readLine();
	}
	in.close();
	long[] retArray = new long[list.size()];
	int i = 0;
	for (Long user : list) {
	    retArray[i] = user;
	    i++;
	}
	return retArray;
    }

    public static void buy(long userID, long buyID) throws Exception {
	URL url = new URL(serverAddress + "buy?userID=" + userID + "&buyID=" + buyID);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	in.close();
    }

    public static void changeLocation(long userID, double xCord, double yCord) throws Exception {
	URL url = new URL(serverAddress + "changeLocation?userID=" + userID + "&xCord=" + xCord + "&yCord=" + yCord);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	in.close();
    }

    public static void exit(long userID) throws Exception {
	URL url = new URL(serverAddress + "exit?userID=" + userID);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	in.close();
    }

    public static long[] nearbyUsers(long userID, double distance) throws Exception {
	URL url = new URL(serverAddress + "nearbyUsers?userID=" + userID + "&distance=" + distance);
	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	ArrayList<Long> list = new ArrayList<Long>();
	String inputLine = in.readLine();
	try {
	    while (inputLine != null) {
		list.add(Long.parseLong(inputLine));
		inputLine = in.readLine();
	    }
	} catch (Exception e) { // If an error occurred (e.g. user doesn't exist), ignore it
	}
	in.close();
	long[] retArray = new long[list.size()];
	int i = 0;
	for (Long user : list) {
	    retArray[i] = user;
	    i++;
	}
	return retArray;
    }
}
