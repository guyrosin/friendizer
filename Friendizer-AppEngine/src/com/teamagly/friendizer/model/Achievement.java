package com.teamagly.friendizer.model;

import javax.jdo.annotations.*;

@PersistenceCapable
public class Achievement {
	@PrimaryKey
	@Persistent
	private Long id;
	
	@Persistent
    private String title;
	
	@Persistent
    private String description;
	
	@Persistent
    private String iconRes;
    
	@Persistent
	private int reward;

	public Achievement(long id, String title, String description, String iconRes, int reward) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.iconRes = iconRes;
		this.reward = reward;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIconRes() {
		return iconRes;
	}

	public void setIconRes(String iconRes) {
		this.iconRes = iconRes;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}
}
