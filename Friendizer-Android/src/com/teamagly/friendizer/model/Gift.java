package com.teamagly.friendizer.model;


public class Gift {
	private Long id;
	private String name;
	private String iconRes;
	private int value;

	public Gift(String name, String iconRes, int value) {
		this.name = name;
		this.iconRes = iconRes;
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIconRes() {
		return iconRes;
	}

	public void setIconRes(String iconRes) {
		this.iconRes = iconRes;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
