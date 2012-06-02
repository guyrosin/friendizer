package com.teamagly.friendizer.model;

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
    
    @Persistent
    private String token;

    @Persistent
    private String status;

    public User(long id, long owner, long value, long money, double latitude, double longitude, Date since, String token, String status) {
	this.id = id;
	this.owner = owner;
	this.value = value;
	this.money = money;
	this.latitude = latitude;
	this.longitude = longitude;
	this.since = since;
	this.token = token;
	this.status = status;
    }

    /**
     * Constructor for a new user
     */
    public User(long id, String token) {
	this.id = id;
	this.owner = 0;
	this.value = 100;
	this.money = 1000;
	this.latitude = -1;
	this.longitude = -1;
	this.since = new Date();
	this.token = token;
	this.status = "";
    }

    public User(JSONObject obj) throws JSONException {
	id = obj.getLong("id");
	owner = obj.getLong("owner");
	value = obj.getLong("value");
	money = obj.getLong("money");
	latitude = obj.getDouble("latitude");
	longitude = obj.getDouble("longitude");
	token = obj.getString("token");
	status = obj.getString("status");
	SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	try {
	    since = format.parse(obj.getString("since"));
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
    
    public String getToken() {
		return token;
	}
    
    public void setToken(String token) {
		this.token = token;
	}

    public String getStatus() {
	return status;
    }

    public void setStatus(String status) {
	this.status = status;
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
	    obj.put("token", token);
	    obj.put("status", status);
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
