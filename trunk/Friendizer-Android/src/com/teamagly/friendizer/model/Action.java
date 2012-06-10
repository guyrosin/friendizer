package com.teamagly.friendizer.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Action {
	private Long id;
	private long buyerID;
	private long boughtID;
	private Date date;

	public Action(long buyerID, long boughtID, Date date) {
		this.buyerID = buyerID;
		this.boughtID = boughtID;
		this.date = date;
	}
	
	public Action(JSONObject obj) throws JSONException {
		id = obj.getLong("id");
		buyerID = obj.getLong("buyerID");
		boughtID = obj.getLong("boughtID");
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		try {
		    date = format.parse(obj.getString("date"));
		} catch (ParseException e) {
		    throw new JSONException("JSONObject[\"date\"] is not a date.");
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", id);
			obj.put("buyerID", buyerID);
			obj.put("boughtID", boughtID);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		    obj.put("date", format.format(date));
		} catch (JSONException e) {
		}
		return obj;
	}
	
	@Override
	public String toString() {
		return toJSONObject().toString();
	}
}
