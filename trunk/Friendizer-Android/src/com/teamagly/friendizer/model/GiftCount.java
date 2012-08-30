package com.teamagly.friendizer.model;

import org.json.JSONException;
import org.json.JSONObject;

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

	public GiftCount(JSONObject obj) {
		try {
			gift = new Gift(obj);
			count = obj.getInt("count");
		} catch (JSONException e) {
		}
	}

	public Gift getGift() {
		return gift;
	}

	public int getCount() {
		return count;
	}
}
