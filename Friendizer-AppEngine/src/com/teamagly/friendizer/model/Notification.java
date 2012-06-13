package com.teamagly.friendizer.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Notification {
	
	private long source;
	
	private String text;
	
	@Override
	public String toString() {
		return toJSONObject().toString();
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("source", source);
			obj.put("text", text);
		} catch (JSONException e) {
		}
		return obj;
	}
	
	public String toC2DMMessage() throws JSONException {
		JSONObject c2dm = new JSONObject();
		c2dm.put("userID", source);
		c2dm.put("text", text);
		c2dm.put("type", "NOT");
		return c2dm.toString();
	}

}
