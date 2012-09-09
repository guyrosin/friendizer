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

	/**
	 * 
	 * The constructor creates a Like object
	 * 
	 * @param id
	 * @param name
	 * @param category
	 * @param created_time
	 */
	public Like(String id, String name, String category) {
		this.id = id;
		this.name = name;
		this.category = category;
	}

	/**
	 * The default constructor creates a Like object
	 */
	public Like() {
		id = "";
		name = "";
		category = "";
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
}
