package com.teamagly.friendizer.model;

import java.util.Date;

import javax.jdo.annotations.*;

@PersistenceCapable
public class Action {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private long buyerID;

	@Persistent
	private long boughtID;

	@Persistent
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
