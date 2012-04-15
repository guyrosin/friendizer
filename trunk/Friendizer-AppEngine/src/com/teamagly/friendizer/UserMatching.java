package com.teamagly.friendizer;

import java.util.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

public class UserMatching {

	private User user;
	private int matching;
	
	public UserMatching(User user, int matching) 
	{
		this.user = user;
		this.matching = matching;
	}
	
	public UserMatching(JSONObject obj) throws JSONException {
		long id = obj.getLong("id");
		long owner = obj.getLong("owner");
		long value = obj.getLong("value");
		long money = obj.getLong("money");
		double latitude = obj.getDouble("latitude");
		double longitude = obj.getDouble("longitude");
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		try {
			Date since = format.parse(obj.getString("since"));
			user = new User(id, owner, value, money, latitude, longitude, since);
			matching = obj.getInt("matching");
		} catch (ParseException e) {
			throw new JSONException("JSONObject[\"since\"] is not a date.");
		}
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public int getMatching() {
		return matching;
	}
	
	public void setMatching(int matching) {
		this.matching = matching;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", user.getId());
			obj.put("owner", user.getOwner());
			obj.put("value", user.getValue());
			obj.put("money", user.getMoney());
			obj.put("latitude", user.getLatitude());
			obj.put("longitude", user.getLongitude());
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			obj.put("since", format.format(user.getSince()));
			obj.put("matching", matching);
		} catch (JSONException e) {
		}
		return obj;
	}
	
	@Override
	public String toString() {
		return toJSONObject().toString();
	}
}
