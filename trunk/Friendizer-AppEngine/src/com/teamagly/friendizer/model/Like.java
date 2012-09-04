package com.teamagly.friendizer.model;

import com.restfb.Facebook;

/**
 * 
 * Describe a Like object: id - Facebook ID of the page
 * name - name of the page
 * category - category of the page
 * created_time - ISO-8601 datetime representing when the User was connected to the Page
 * 
 */
public class Like {

	@Facebook
	String id;

	@Facebook
	String name;

	@Facebook
	String category;

	@Facebook
	String created_time;

	/**
	 * 
	 * The constructor creates a Like object
	 * 
	 * @param id
	 * @param name
	 * @param category
	 * @param created_time
	 */
	public Like(String id, String name, String category, String created_time) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.created_time = created_time;
	}

	/**
	 * The default constructor creates a Like object
	 */
	public Like() {
		this.id = "";
		this.name = "";
		this.category = "";
		this.created_time = "";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCreated_time() {
		return created_time;
	}

	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}
}
