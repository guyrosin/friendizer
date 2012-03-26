/**
 * 
 */
package com.teamagly.friendizer;

import java.util.Date;

import org.json.JSONObject;

/**
 * @author Guy
 * 
 */
public class FBUserInfo {
    long id;
    String firstName;
    String name;
    String gender;
    String age;
    String picURL;

    public FBUserInfo(final JSONObject jsonObject) {
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
}
