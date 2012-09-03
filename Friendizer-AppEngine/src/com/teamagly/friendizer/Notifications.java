package com.teamagly.friendizer;

public class Notifications {
	
	public static final String BEEN_BOUGHT_MSG = "You've been bought by ";
	public static final String ACHIEVMENT_MSG = "You've reached an achievement: ";
	public static final String GIFT_MSG = "Received a gift from ";
	public static final String NEARBY_MSG = "is nearby. Buy him again and get friendize!";
	
	public static enum NotificationType {
		CHAT, BUY, GFT, UPD, ACH, NEARBY;
	}
}
