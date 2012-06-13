package com.teamagly.friendizer.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

public class FriendizerUser implements Serializable {

	long id = 0;
	long points = 0;
	int level = 0;
	long money = 0;
	long ownerID = 0;
	FriendizerUser[] ownsList = {};
	double latitude;
	double longitude;
	Date since;
	long distance = 0;
	int matching = 0;
	String status = "";

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
		points = obj.getLong("points");
		level = obj.getInt("level");
		money = obj.getLong("money");
		ownerID = obj.getLong("owner");
		latitude = obj.getDouble("latitude");
		longitude = obj.getDouble("longitude");
		matching = obj.optInt("matching");
		status = obj.optString("status");
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
		points = fzUser.points;
		level = fzUser.level;
		money = fzUser.money;
		ownerID = fzUser.ownerID;
		ownsList = fzUser.ownsList;
		latitude = fzUser.latitude;
		longitude = fzUser.longitude;
		since = fzUser.since;
		distance = fzUser.distance;
		matching = fzUser.matching;
		status = fzUser.status;
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", id);
			obj.put("points", points);
			obj.put("level", level);
			obj.put("money", money);
			obj.put("owner", ownerID);
			obj.put("latitude", latitude);
			obj.put("longitude", longitude);
			obj.put("matching", matching);
			obj.put("status", status);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			obj.put("since", format.format(since));
		} catch (JSONException e) {
		}
		return obj;
	}

	public FriendizerUser() {
	}

	/**
	 * @return the user's location as a GeoPoint
	 */
	public GeoPoint getGeoPoint() {
		int lat = (int) (latitude * 1E6);
		int lng = (int) (longitude * 1E6);
		return new GeoPoint(lat, lng);
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
	 * @return the points
	 */
	public long getPoints() {
		return points;
	}

	/**
	 * @param points
	 *            the points to set
	 */
	public void setPoints(long points) {
		this.points = points;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
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
	public FriendizerUser[] getOwnsList() {
		return ownsList;
	}

	/**
	 * @param ownsList
	 *            the ownsList to set
	 */
	public void setOwnsList(FriendizerUser[] ownsList) {
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

	/**
	 * @return the matching
	 */
	public int getMatching() {
		return matching;
	}

	/**
	 * @param matching
	 *            the matching to set
	 */
	public void setMatching(int matching) {
		this.matching = matching;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
}
