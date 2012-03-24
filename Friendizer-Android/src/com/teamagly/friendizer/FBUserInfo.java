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

    public FBUserInfo(final JSONObject jsonObject) {
	// Note we don't load the pic here! (It has to be done in the main UI thread)
	id = jsonObject.optString("id");
	picURL = jsonObject.optString("picture");
	// new Handler(Looper.getMainLooper()).post(new Runnable() { // -- It's not working
	// @Override
	// public void run() {
	// pic = Utility.model.getImage(jsonObject.optString("id"), picURL);
	// }
	// });
	name = jsonObject.optString("name");
	gender = jsonObject.optString("gender");

	try {
	    age = Utility.calcAge(new Date(jsonObject.optString("birthday")));
	} catch (IllegalArgumentException e) {
	    age = "";
	}

    }
}
