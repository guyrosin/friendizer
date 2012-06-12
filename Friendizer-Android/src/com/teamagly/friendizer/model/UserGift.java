package com.teamagly.friendizer.model;

public class UserGift {
	@SuppressWarnings("unused")
	private Long id;
	private long userID;
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
