package com.teamagly.friendizer.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.teamagly.friendizer.Notifications;

@PersistenceCapable
public class ChatMessage {

	@PrimaryKey
	@Persistent(
			valueStrategy = IdGeneratorStrategy.IDENTITY)
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

	public ChatMessage(JSONObject obj) throws JSONException {
		this.id = obj.getLong("id");
		this.source = obj.getLong("source");
		this.destination = obj.getLong("destination");
		this.text = obj.getString("text");
		this.unread = obj.getBoolean("unread");
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		try {
			this.time = format.parse(obj.getString("time"));
		} catch (ParseException e) {
			throw new JSONException("JSONObject[\"time\"] is not a date.");
		}
	}

	@Override
	public String toString() {
		return toJSONObject().toString();
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", id);
			obj.put("source", source);
			obj.put("destination", destination);
			obj.put("text", text);
			obj.put("unread", unread);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			obj.put("time", format.format(time));
		} catch (JSONException e) {
		}
		return obj;
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
