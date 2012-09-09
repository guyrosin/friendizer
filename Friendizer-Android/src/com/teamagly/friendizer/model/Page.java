package com.teamagly.friendizer.model;

public class Page {
	private String id;

	private String picURL;

	private String name;

	private String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPicURL() {
		return picURL;
	}

	public void setPicURL(String picURL) {
		this.picURL = picURL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		type = type.toLowerCase();
		type = type.substring(0, 1).toUpperCase() + type.substring(1);
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
