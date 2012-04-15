package com.teamagly.friendizer.model;

import org.json.JSONException;
import org.json.JSONObject;

public class UserMatching {

    private User user;
    private int matching;

    public UserMatching(User user, int matching) {
	this.user = user;
	this.matching = matching;
    }

    /**
     * @param obj
     *            a JSON object contains data from Friendizer (including matching with the current user)
     * @throws JSONException
     */
    public UserMatching(JSONObject obj) throws JSONException {
	user = new User(new FriendizerUser(obj));
	matching = obj.getInt("matching");
    }

    public User getUser() {
	return user;
    }

    public void setUser(User user) {
	this.user = user;
    }

    public int getMatching() {
	return matching;
    }

    public void setMatching(int matching) {
	this.matching = matching;
    }

    public JSONObject toJSONObject() {
	JSONObject obj = user.fz.toJSONObject();
	try {
	    obj.put("matching", matching);
	} catch (JSONException e) {
	}
	return obj;
    }

    @Override
    public String toString() {
	return toJSONObject().toString();
    }
}
