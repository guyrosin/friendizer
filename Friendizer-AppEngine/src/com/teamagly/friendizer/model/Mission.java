package com.teamagly.friendizer.model;

import javax.jdo.annotations.*;

@PersistenceCapable
public class Mission {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private long userID;
	
	@Persistent
	private int type;

	public Mission(long userID, int type) {
		this.userID = userID;
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
