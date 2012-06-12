package com.teamagly.friendizer.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class UserGift {
	@SuppressWarnings("unused")
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private long userID;
	
	@Persistent
	private long giftID;

	public UserGift(long userID, long giftID) {
		this.userID = userID;
		this.giftID = giftID;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public long getGiftID() {
		return giftID;
	}

	public void setGiftID(long giftID) {
		this.giftID = giftID;
	}
}
