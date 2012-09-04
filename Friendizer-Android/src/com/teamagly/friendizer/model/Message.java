package com.teamagly.friendizer.model;

import java.io.Serializable;
import java.util.Date;

import com.teamagly.friendizer.utils.Utility;

public class Message implements Serializable {

	private Long id;

	private long source;

	private long destination;

	private String text;

	private Date time;

	private boolean unread;

	private static final long serialVersionUID = 9125316871449587189L;

	public Message(long source, long destination, String text) {
		this.source = source;
		this.destination = destination;
		this.text = text;
		this.unread = true;
		this.time = new Date();
	}

	/**
	 * Create a new message from the current user
	 * 
	 * @param destination
	 * @param text
	 *            content of the message
	 */
	public Message(long destination, String text) {
		this.source = Utility.getInstance().userInfo.getId();
		this.destination = destination;
		this.text = text;
		this.unread = true;
		this.time = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long key) {
		this.id = key;
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
