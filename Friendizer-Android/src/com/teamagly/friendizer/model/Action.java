package com.teamagly.friendizer.model;

import java.util.Date;

public class Action {
	@SuppressWarnings("unused")
	private Long id;
	private long buyerID;
	private long boughtID;
	private Date date;

	public Action(long buyerID, long boughtID, Date date) {
		this.buyerID = buyerID;
		this.boughtID = boughtID;
		this.date = date;
	}

	public long getBuyerID() {
		return buyerID;
	}

	public void setBuyerID(long buyerID) {
		this.buyerID = buyerID;
	}

	public long getBoughtID() {
		return boughtID;
	}

	public void setBoughtID(long boughtID) {
		this.boughtID = boughtID;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
