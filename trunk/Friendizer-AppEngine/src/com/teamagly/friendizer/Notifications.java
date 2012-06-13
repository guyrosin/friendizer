package com.teamagly.friendizer;

public class Notifications {
	
	public static final String BEEN_BOUGHT_MSG = "You've been bought by ";
	public static final String HAD_BOUGHT_MSG = "You've bought ";
	public static final String ACHIEVMENT_MSG = "You've reached an achievement: ";
	public static final String GIFT_MSG = "Received a gift from ";
	
	public static enum NotificationType {
		MSG, BUY, GFT, UPD, ACH;
	}
}
