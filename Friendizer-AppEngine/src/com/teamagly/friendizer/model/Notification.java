package com.teamagly.friendizer.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.Notifications;
import com.teamagly.friendizer.Notifications.notificationType;

public class Notification {
	
	public Notification(long source, String text, notificationType type) {
		super();
		this.source = source;
		this.text = text;
		this.type = type;
	}
	
	public Notification() {
		
	}
	

	protected long source;
	
	protected String text;
	
	protected notificationType type;
	

	
	@Override
	public String toString() {
		return toJSONObject().toString();
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("source", source);
			obj.put("text", text);
			obj.put("type", type);
		} catch (JSONException e) {
		}
		return obj;
	}
	
	public String toC2DMMessage() throws JSONException {
		JSONObject c2dm = new JSONObject();
		c2dm.put("userID", source);
		c2dm.put("text", text);
		c2dm.put("type", type);
		return c2dm.toString();
	}

	public long getSource() {
		return source;
	}

	public void setSource(long source) {
		this.source = source;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public notificationType getType() {
		return type;
	}

	public void setType(notificationType type) {
		this.type = type;
	}

}
