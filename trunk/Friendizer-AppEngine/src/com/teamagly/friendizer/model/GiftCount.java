package com.teamagly.friendizer.model;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class GiftCount {

	private Gift gift;
	private int count;

	public GiftCount(Gift gift, int count) {
		this.gift = gift;
		this.count = count;
	}

	public GiftCount(Gift gift) {
		this.gift = gift;
		this.count = 0;
	}

	public Gift getGift() {
		return gift;
	}

	public int getCount() {
		return count;
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", gift.id);
			obj.put("name", gift.name);
			obj.put("iconRes", gift.iconRes);
			obj.put("value", gift.value);
			obj.put("count", count);
		} catch (JSONException e) {
		}
		return obj;
	}
}
