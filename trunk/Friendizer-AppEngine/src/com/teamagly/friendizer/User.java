package com.teamagly.friendizer;

import java.text.*;
import java.util.Date;

import javax.jdo.annotations.*;

import org.json.*;

@PersistenceCapable
public class User {
	@PrimaryKey
	@Persistent
	private Long id;

	@Persistent
	private long owner;

	@Persistent
	private long value;

	@Persistent
	private long money;

	@Persistent
	private double latitude;

	@Persistent
	private double longitude;

	@Persistent
	private Date since;

	public User(long id, long owner, long value, long money, double latitude, double longitude, Date since) {
		this.id = id;
		this.owner = owner;
		this.value = value;
		this.money = money;
		this.latitude = latitude;
		this.longitude = longitude;
		this.since = since;
	}
	
	public User(String jsonString) throws JSONException {
		JSONObject obj = new JSONObject(jsonString);
		this.id = obj.getLong("id");
		this.owner = obj.getLong("owner");
		this.value = obj.getLong("value");
		this.money = obj.getLong("money");
		this.latitude = obj.getDouble("latitude");
		this.longitude = obj.getDouble("longitude");
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		try {
			this.since = format.parse(obj.getString("since"));
		} catch (ParseException e) {
			throw new JSONException("JSONObject[\"since\"] is not a date.");
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getOwner() {
		return owner;
	}

	public void setOwner(long owner) {
		this.owner = owner;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public long getMoney() {
		return money;
	}

	public void setMoney(long money) {
		this.money = money;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public Date getSince() {
		return since;
	}

	public void setSince(Date since) {
		this.since = since;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", id);
			obj.put("owner", owner);
			obj.put("value", value);
			obj.put("money", money);
			obj.put("latitude", latitude);
			obj.put("longitude", longitude);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			obj.put("since", format.format(since));
		} catch (JSONException e) {
		}
		return obj;
	}
	
	@Override
	public String toString() {
		return toJSONObject().toString();
	}
}