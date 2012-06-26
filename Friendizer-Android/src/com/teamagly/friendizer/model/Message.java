package com.teamagly.friendizer.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.utils.Utility;

import java.util.Date;

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

	public Message(JSONObject obj) throws JSONException {
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
