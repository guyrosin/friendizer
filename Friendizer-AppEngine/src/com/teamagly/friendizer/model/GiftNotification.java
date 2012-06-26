package com.teamagly.friendizer.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.Notifications.NotificationType;

public class GiftNotification extends Notification {

	private long giftID;

	public GiftNotification() {
		super();
	}

	public GiftNotification(UserGift gift, String text) {
		super(gift.getSenderID(), text, NotificationType.GFT);
		giftID = gift.getGiftID();
	}

	public String toC2DMMessage() throws JSONException {
		JSONObject c2dm = new JSONObject();
		c2dm.put("userID", source);
		c2dm.put("text", text);
		c2dm.put("type", type);
		c2dm.put("giftID", giftID);
		return c2dm.toString();
	}
}
