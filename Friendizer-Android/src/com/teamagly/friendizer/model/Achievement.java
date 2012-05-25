/**
 * 
 */
package com.teamagly.friendizer.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Achievement {
    private Long id;

    private String title;

    private String description;

    private String iconRes;

    private int reward;

    public boolean earned;

    public Achievement(JSONObject obj) throws JSONException {
	id = obj.getLong("id");
	title = obj.getString("title");
	description = obj.getString("description");
	iconRes = obj.getString("iconRes");
	reward = obj.getInt("reward");
	earned = obj.getBoolean("earned");
    }

    public JSONObject toJSONObject() {
	JSONObject obj = new JSONObject();
	try {
	    obj.put("id", id);
	    obj.put("title", title);
	    obj.put("description", description);
	    obj.put("iconRes", iconRes);
	    obj.put("reward", reward);
	    obj.put("earned", earned);
	} catch (JSONException e) {
	}
	return obj;
    }

    @Override
    public String toString() {
	return toJSONObject().toString();
    }

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getIconRes() {
	return iconRes;
    }

    public void setIconRes(String iconRes) {
	this.iconRes = iconRes;
    }

    public int getReward() {
	return reward;
    }

    public void setReward(int reward) {
	this.reward = reward;
    }

    /**
     * @return the earned
     */
    public boolean isEarned() {
	return earned;
    }

    /**
     * @param earned
     *            the earned to set
     */
    public void setEarned(boolean earned) {
	this.earned = earned;
    }

}
