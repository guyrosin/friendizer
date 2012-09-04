package com.teamagly.friendizer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.teamagly.friendizer.utils.Utility;

public class User implements Serializable {

	/*
	 * Facebook data
	 */

	private Long id; // Facebook ID

	private String name; // Full name

	private String gender;

	private String birthday;

	private String picture;

	private String token; // Facebook access token

	/*
	 * Friendizer data
	 */

	private long owner;

	private long points;

	private int level;

	private long money;

	private double latitude;

	private double longitude;

	private Date since; // When the user was last seen

	private String status;

	List<User> ownsList = new ArrayList<User>();

	long distance = 0;

	int matching = 0;

	public enum FBQueryType {
		GRAPH,
		FQL
	}

	private static final long serialVersionUID = 8643574985757595599L;

	/**
	 * @return the user's locationInfo as a GeoPoint
	 */
	public GeoPoint getGeoPoint() {
		int lat = (int) (latitude * 1E6);
		int lng = (int) (longitude * 1E6);
		return new GeoPoint(lat, lng);
	}

	public final int getLevelPoints() {
		double threshold = 200 * Math.pow(level, 1.5);
		double prevLevelThreshold = 200 * Math.pow(level - 1, 1.5);
		double currentLevelPoints = threshold - prevLevelThreshold;
		return (int) currentLevelPoints;
	}

	public final int getEarnedPointsThisLevel() {
		double prevLevelThreshold = 200 * Math.pow(level - 1, 1.5);
		double earnedPointsThisLevel = points - prevLevelThreshold;
		return (int) earnedPointsThisLevel;
	}

	// Getters and setters

	/**
	 * @return the birthday
	 */
	public String getBirthday() {
		return birthday;
	}

	/**
	 * @param birthday
	 *            the birthday to set
	 */
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	/**
	 * @return the picture
	 */
	public String getPicture() {
		return picture;
	}

	/**
	 * @param picture
	 *            the picture to set
	 */
	public void setPicture(String picture) {
		this.picture = picture;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the owner
	 */
	public long getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(long owner) {
		this.owner = owner;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return name.substring(0, name.indexOf(" "));
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender
	 *            the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return the age
	 */
	public String getAge() {
		return Utility.calcAge(birthday);
	}

	/**
	 * @return the picture
	 */
	public String getPicURL() {
		return picture;
	}

	/**
	 * @param picture
	 *            the picture to set
	 */
	public void setPicURL(String picURL) {
		this.picture = picURL;
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
	public long getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(int level) {
		this.points = level;
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
	 * @return the owner
	 */
	public long getOwnerID() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwnerID(long ownerID) {
		this.owner = ownerID;
	}

	/**
	 * @return the ownsList
	 */
	public List<User> getOwnsList() {
		return ownsList;
	}

	/**
	 * @param ownsList
	 *            the ownsList to set
	 */
	public void setOwnsList(List<User> ownsList) {
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

	public void updateFacebookData(JSONObject jsonObject) {
		if (jsonObject.has("name"))
			try {
				name = jsonObject.getString("name");
			} catch (JSONException e) {
			}
		if (jsonObject.has("birthday"))
			try {
				birthday = jsonObject.getString("birthday");
			} catch (JSONException e) {
			}
		if (jsonObject.has("gender"))
			try {
				gender = jsonObject.getString("gender");
			} catch (JSONException e) {
			}
		if (jsonObject.has("picture"))
			try {
				picture = jsonObject.getString("picture");
			} catch (JSONException e) {
			}
	}
}
