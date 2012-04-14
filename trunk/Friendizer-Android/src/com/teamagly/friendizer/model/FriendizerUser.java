package com.teamagly.friendizer.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class FriendizerUser implements Serializable {

    long id = 0;
    long value = 0;
    long money = 0;
    long ownerID = 0;
    long[] ownsList = {};
    double latitude;
    double longitude;
    Date since;
    long distance = 0;

    private static final long serialVersionUID = -7874788252593417090L;

    /**
     * Constructor
     * 
     * @param jsonString
     *            a JSON string describing the user
     * @throws JSONException
     */
    public FriendizerUser(JSONObject obj) throws JSONException {
	id = obj.getLong("id");
	value = obj.getLong("value");
	money = obj.getLong("money");
	ownerID = obj.getLong("owner");
	latitude = obj.getDouble("latitude");
	longitude = obj.getDouble("longitude");
	SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	try {
	    since = format.parse(obj.getString("since"));
	} catch (ParseException e) {
	    throw new JSONException("JSONObject[\"since\"] is not a date.");
	}
    }

    /**
     * Copy constructor
     * 
     * @param fzUser
     *            an existing user
     */
    public FriendizerUser(FriendizerUser fzUser) {
	id = fzUser.id;
	value = fzUser.value;
	money = fzUser.money;
	ownerID = fzUser.ownerID;
	ownsList = fzUser.ownsList;
	latitude = fzUser.latitude;
	longitude = fzUser.longitude;
	since = fzUser.since;
	distance = fzUser.distance;
    }

    public JSONObject toJSONObject() {
	JSONObject obj = new JSONObject();
	try {
	    obj.put("id", id);
	    obj.put("value", value);
	    obj.put("money", money);
	    obj.put("owner", ownerID);
	    obj.put("latitude", latitude);
	    obj.put("longitude", longitude);
	    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	    obj.put("since", format.format(since));
	} catch (JSONException e) {
	}
	return obj;
    }

    /**
     * @return the id
     */
    public long getId() {
	return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
	this.id = id;
    }

    /**
     * @return the value
     */
    public long getValue() {
	return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(long value) {
	this.value = value;
    }

    /**
     * @return the money
     */
    public long getMoney() {
	return money;
    }

    /**
     * @param money
     *            the money to set
     */
    public void setMoney(long money) {
	this.money = money;
    }

    /**
     * @return the ownerID
     */
    public long getOwnerID() {
	return ownerID;
    }

    /**
     * @param ownerID
     *            the ownerID to set
     */
    public void setOwnerID(long ownerID) {
	this.ownerID = ownerID;
    }

    /**
     * @return the ownsList
     */
    public long[] getOwnsList() {
	return ownsList;
    }

    /**
     * @param ownsList
     *            the ownsList to set
     */
    public void setOwnsList(long[] ownsList) {
	this.ownsList = ownsList;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
	return latitude;
    }

    /**
     * @param latitude
     *            the latitude to set
     */
    public void setLatitude(double latitude) {
	this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
	return longitude;
    }

    /**
     * @param longitude
     *            the longitude to set
     */
    public void setLongitude(double longitude) {
	this.longitude = longitude;
    }

    /**
     * @return the since
     */
    public Date getSince() {
	return since;
    }

    /**
     * @param since
     *            the since to set
     */
    public void setSince(Date since) {
	this.since = since;
    }

    /**
     * @return the distance
     */
    public long getDistance() {
	return distance;
    }

    /**
     * @param distance
     *            the distance to set
     */
    public void setDistance(long distance) {
	this.distance = distance;
    }
}
