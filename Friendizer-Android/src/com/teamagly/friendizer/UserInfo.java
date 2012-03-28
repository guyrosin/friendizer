package com.teamagly.friendizer;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONObject;

public class UserInfo implements Serializable {

    // Data from our servers
    long id;
    long value;
    long money;
    long ownerID;
    long[] ownsList;
    long distance;
    int matching;

    // Data from Facebook
    String firstName;
    String name;
    String gender;
    String age;
    String picURL;

    private static final long serialVersionUID = 8643574985757595599L;

    public UserInfo(long id, long value, long money, long owner, long[] ownsList) {
	this.id = id;
	this.value = value;
	this.money = money;
	this.ownerID = owner;
	this.ownsList = ownsList;
    }

    public UserInfo(final JSONObject jsonObject) {
	id = jsonObject.optLong("id");
	picURL = jsonObject.optString("picture");
	firstName = jsonObject.optString("first_name");
	name = jsonObject.optString("name");
	gender = jsonObject.optString("gender");

	try {
	    age = Utility.calcAge(new Date(jsonObject.optString("birthday")));
	} catch (IllegalArgumentException e) {
	    age = "";
	}
    }

    // Updates the Facebook data from the given UserInfo object
    public void updateFacebookData(UserInfo u) {
	firstName = u.firstName;
	name = u.name;
	gender = u.gender;
	age = u.age;
	picURL = u.picURL;
    }

    // Updates the Friendizer data from the given UserInfo object
    public void updateFriendizerData(UserInfo u) {
	value = u.value;
	money = u.money;
	ownerID = u.ownerID;
	ownsList = u.ownsList;
	distance = u.distance;
	matching = u.matching;
    }
}