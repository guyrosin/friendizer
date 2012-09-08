package com.teamagly.friendizer.model;

import java.util.Date;

import javax.jdo.annotations.*;

import com.google.appengine.labs.repackaged.org.json.*;
import com.teamagly.friendizer.Notifications;

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

	public ChatMessage() {

	}

	public ChatMessage(long source, long destination, String text) {
		super();
		this.source = source;
		this.destination = destination;
		this.text = text;
		this.unread = true;
		this.time = new Date();
	}

	public String toC2DMMessage() throws JSONException {
		JSONObject c2dm = new JSONObject();
		c2dm.put("userID", source);
		c2dm.put("text", text);
		c2dm.put("type", Notifications.NotificationType.CHAT);
		return c2dm.toString();
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
