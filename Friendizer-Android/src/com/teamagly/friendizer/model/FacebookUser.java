package com.teamagly.friendizer.model;

import java.io.Serializable;

import org.json.JSONObject;

import com.teamagly.friendizer.model.User.FBQueryType;
import com.teamagly.friendizer.utils.Utility;

public class FacebookUser implements Serializable {

	Long id;
	String firstName;
	String name;
	String gender;
	String age;
	String picURL;

	private static final long serialVersionUID = -4018182836851756167L;

	/**
	 * Copy constructor
	 * 
	 * @param fbUser
	 *            an existing user
	 */
	public FacebookUser(FacebookUser fbUser) {
		id = fbUser.id;
		firstName = fbUser.firstName;
		name = fbUser.name;
		gender = fbUser.gender;
		age = fbUser.age;
		picURL = fbUser.picURL;
	}

	public FacebookUser() {
	}

	// Just to make life easier, because most of the queries will be using the Graph API
	public FacebookUser(final JSONObject jsonObject) {
		this(jsonObject, FBQueryType.GRAPH);
	}

	public FacebookUser(final JSONObject jsonObject, FBQueryType type) {
		if (type == FBQueryType.GRAPH) {
			id = jsonObject.optLong("id");
			picURL = jsonObject.optJSONObject("picture").optJSONObject("data").optString("url");
			firstName = jsonObject.optString("first_name");
			name = jsonObject.optString("name");
			gender = jsonObject.optString("gender");

			try {
				age = Utility.calcAge(jsonObject.optString("birthday"));
			} catch (IllegalArgumentException e) {
				age = "";
			}
		} else { // FQL Query
			id = jsonObject.optLong("uid");
			picURL = jsonObject.optString("pic_square");
			firstName = jsonObject.optString("first_name");
			name = jsonObject.optString("name");
			gender = jsonObject.optString("sex");

			try {
				age = Utility.calcAge(jsonObject.optString("birthday_date"));
			} catch (IllegalArgumentException e) {
				age = "";
			}
		}
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
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
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
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
		return age;
	}

	/**
	 * @param age
	 *            the age to set
	 */
	public void setAge(String age) {
		this.age = age;
	}

	/**
	 * @return the picURL
	 */
	public String getPicURL() {
		return picURL;
	}

	/**
	 * @param picURL
	 *            the picURL to set
	 */
	public void setPicURL(String picURL) {
		this.picURL = picURL;
	}
}
