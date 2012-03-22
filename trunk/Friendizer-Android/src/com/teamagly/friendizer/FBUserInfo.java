/**
 * 
 */
package com.teamagly.friendizer;

import java.util.Date;

import org.json.JSONObject;

import android.graphics.Bitmap;

/**
 * @author Guy
 * 
 */
public class FBUserInfo {
    String id;
    String name;
    String gender;
    String age;
    Bitmap pic;
    String picURL;

    public FBUserInfo(JSONObject jsonObject) {
	// TODO Auto-generated constructor stub
	id = jsonObject.optString("id");
	picURL = jsonObject.optString("picture");
	pic = Utility.model.getImage(jsonObject.optString("id"), picURL);
	name = jsonObject.optString("name");
	gender = jsonObject.optString("gender");

	try {
	    age = Utility.calcAge(new Date(jsonObject.optString("birthday")));
	} catch (IllegalArgumentException e) {
	    age = "";
	}

    }
}
