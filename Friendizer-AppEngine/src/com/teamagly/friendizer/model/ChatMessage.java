package com.teamagly.friendizer.model;

import java.util.Date;

import javax.jdo.annotations.*;

@PersistenceCapable
public class ChatMessage {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private long source;

	@Persistent
	private long destination;

	@Persistent
	private String text;

	@Persistent
	private Date time;

	@Persistent
	private boolean unread;

	public ChatMessage(long source, long destination, String text, Date time) {
		this.source = source;
		this.destination = destination;
		this.text = text;
		this.unread = true;
		this.time = time;
	}

	public long getSource() {
		return source;
	}

	public void setSource(long source) {
		this.source = source;
	}

	public long getDestination() {
		return destination;
	}

	public void setDestination(long destination) {
		this.destination = destination;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isUnread() {
		return unread;
	}

	public void setUnread(boolean unread) {
		this.unread = unread;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
}
