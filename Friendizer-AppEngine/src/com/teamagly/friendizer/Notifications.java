package com.teamagly.friendizer;

public class Notifications {
	
	public static final String BEEN_BOUGHT_MSG = "You've been bough by ";
	public static final String HAD_BOUGHT_MSG = "You've bough ";
	public static final String ACHIEVMENT_MSG = "You've reached an achievment ";
	public static final String GIFT_MSG = "Gift was sent for you";
	
	public static enum notificationType {
		MSG, BUY, GFT, UPD, ACH;
	}
}
