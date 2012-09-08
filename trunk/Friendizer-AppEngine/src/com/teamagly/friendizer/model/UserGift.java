package com.teamagly.friendizer.model;

import javax.jdo.annotations.*;

@PersistenceCapable
public class UserGift {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private long receiverID;

	@Persistent
	private long senderID;

	@Persistent
	private long giftID;

	public UserGift(long receiverID, long senderID, long giftID) {
		this.receiverID = receiverID;
		this.senderID = senderID;
		this.giftID = giftID;
	}

	public long getGiftID() {
		return giftID;
	}

	public long getSenderID() {
		return senderID;
	}

	public long getReceiverID() {
		return receiverID;
	}
}
