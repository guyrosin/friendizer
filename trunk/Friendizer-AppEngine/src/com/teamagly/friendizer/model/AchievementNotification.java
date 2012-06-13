package com.teamagly.friendizer.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.Notifications.NotificationType;

public class AchievementNotification extends Notification {
	
	
	private String title;
	
	private String iconRes;
	
	
	public AchievementNotification() {
		super();
	}
	
	
	public AchievementNotification(Achievement achievement, long source, String text) {
		super(source,text,NotificationType.ACH);
		title = achievement.getTitle();
		iconRes = achievement.getIconRes();
	}
	
	public String toC2DMMessage() throws JSONException {
		JSONObject c2dm = new JSONObject();
		c2dm.put("userID", source);
		c2dm.put("text", text);
		c2dm.put("type", type);
		c2dm.put("title", title);
		c2dm.put("iconRes", iconRes);
		return c2dm.toString();
	}
	

	public String getIconRes() {
		return iconRes;
	}

	public void setIconRes(String iconRes) {
		this.iconRes = iconRes;
	}


	public String getTilte() {
		return title;
	}


	public void setTilte(String tilte) {
		this.title = tilte;
	}

}
