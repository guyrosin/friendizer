package com.teamagly.friendizer.model;

import java.text.SimpleDateFormat;

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
	JSONObject obj = new JSONObject();
	try {
	    obj.put("id", user.getId());
	    obj.put("owner", user.getOwnerID());
	    obj.put("value", user.getValue());
	    obj.put("money", user.getMoney());
	    obj.put("latitude", user.getLatitude());
	    obj.put("longitude", user.getLongitude());
	    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	    obj.put("since", format.format(user.getSince()));
	} catch (JSONException e) {
	}
	return obj;
    }

    @Override
    public String toString() {
	return toJSONObject().toString();
    }
}
