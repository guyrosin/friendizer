package com.teamagly.friendizer.model;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.restfb.json.JsonObject;

@PersistenceCapable(detachable = "true")
public class User {

	/*
	 * Facebook data
	 */

	@PrimaryKey
	@Persistent
	private Long id; // Facebook ID

	@Persistent
	private String name; // Full name

	@Persistent
	private String gender;

	@Persistent
	private String birthday;

	@Persistent
	private String picture;

	@Persistent
	private String token; // Facebook access token

	/*
	 * Friendizer data
	 */

	@Persistent
	private long owner;

	@Persistent
	private long points;

	@Persistent
	private int level;

	@Persistent
	private long money;

	@Persistent
	private double latitude;

	@Persistent
	private double longitude;

	@Persistent
	private Date since; // When the user was last seen

	@Persistent
	private String status;

	@Persistent
	private boolean fbUpdate;
	
	@Persistent
	private Integer ownsNum;

	/**
	 * Constructor for a new user
	 */
	public User(long id, String token) {
		this.id = id;
		owner = 0;
		points = 100;
		level = 1;
		money = 1000;
		latitude = -1;
		longitude = -1;
		since = new Date();
		this.token = token;
		status = "";
		fbUpdate = false;
		ownsNum = 0;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	public long getOwner() {
		return owner;
	}

	public void setOwner(long owner) {
		this.owner = owner;
	}

	public long getPoints() {
		return points;
	}

	public void setPoints(long points) {
		this.points = points;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
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

	/**
	 * @return the fbUpdate
	 */
	public boolean isFbUpdate() {
		return fbUpdate;
	}

	/**
	 * @param fbUpdate
	 *            the fbUpdate to set
	 */
	public void setFbUpdate(boolean fbUpdate) {
		this.fbUpdate = fbUpdate;
	}

	public void updateFacebookData(JsonObject jsonObject) {
		if (jsonObject.has("name"))
			name = jsonObject.getString("name");
		if (jsonObject.has("picture"))
			try {
				picture = jsonObject.getJsonObject("picture").getJsonObject("data").optString("url");
			} catch (Exception e) {
				picture = jsonObject.getString("picture"); // Old picture format
			}
		if (jsonObject.has("gender"))
			gender = jsonObject.getString("gender");
		if (jsonObject.has("birthday"))
			birthday = jsonObject.getString("birthday");
	}
	
	public Integer getOwnsNum() {
		return ownsNum;
	}
	
	public void setOwnsNum(Integer ownsNum) {
		this.ownsNum = ownsNum;
	}
}
