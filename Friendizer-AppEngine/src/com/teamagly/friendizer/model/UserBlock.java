package com.teamagly.friendizer.model;

import javax.jdo.annotations.*;

@PersistenceCapable
public class UserBlock {
	@SuppressWarnings("unused")
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private long userID;
	
	@Persistent
	private long blockedID;

	public UserBlock(long userID, long blockedID) {
		this.userID = userID;
		this.blockedID = blockedID;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public long getBlockedID() {
		return blockedID;
	}

	public void setBlockedID(long blockedID) {
		this.blockedID = blockedID;
	}
}
