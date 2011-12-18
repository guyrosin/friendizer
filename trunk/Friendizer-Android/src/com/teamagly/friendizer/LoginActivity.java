package com.teamagly.friendizer;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.R;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

/**
 * @author Guy
 * 
 */
public class LoginActivity extends Activity {
    public static final String APP_ID = "273844699335189";
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	mHandler = new Handler();

	// Create the Facebook Object using the app id.
	Utility.facebook = new Facebook(APP_ID);
	// Instantiate the asynrunner object for asynchronous api calls.
	Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.facebook);

	/*
	 * Get existing access_token if any
	 */
	Utility.prefs = getPreferences(MODE_PRIVATE);
	String access_token = Utility.prefs.getString("access_token", null);
	long expires = Utility.prefs.getLong("access_expires", 0);
	if (access_token != null) {
	    Utility.facebook.setAccessToken(access_token);
	}
	if (expires != 0) {
	    Utility.facebook.setAccessExpires(expires);
	}

	/*
	 * Only call authorize if the access_token has expired.
	 */
	if (!Utility.facebook.isSessionValid()) {

	    Utility.facebook.authorize(this, new String[] { "user_activities", "user_checkins", "user_interests", "user_likes",
		    "user_location", "user_birthday" }, new DialogListener() {
		@Override
		public void onComplete(Bundle values) {
		    SharedPreferences.Editor editor = Utility.prefs.edit();
		    editor.putString("access_token", Utility.facebook.getAccessToken());
		    editor.putLong("access_expires", Utility.facebook.getAccessExpires());
		    editor.commit();
		}

		@Override
		public void onFacebookError(FacebookError error) {
		}

		@Override
		public void onError(DialogError e) {
		}

		@Override
		public void onCancel() {
		}
	    });
	}
	requestUserData();
	Intent intent = new Intent(getBaseContext(), FriendizerActivity.class);
	startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);

	Utility.facebook.authorizeCallback(requestCode, resultCode, data);
    }

    /*
     * Request user name, and picture to show on the main screen.
     */
    public void requestUserData() {
	Bundle params = new Bundle();
	params.putString("fields", "name, first_name, picture, birthday, gender");
	Utility.mAsyncRunner.request("me", params, new UserRequestListener());
    }

    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {

	@Override
	public void onComplete(final String response, final Object state) {
	    JSONObject jsonObject;
	    try {
		jsonObject = new JSONObject(response);

		final String picURL = jsonObject.getString("picture");
		Utility.userName = jsonObject.getString("name");
		Utility.firstName = jsonObject.getString("first_name");
		Utility.userUID = jsonObject.getString("id");
		String birthday = jsonObject.getString("birthday");
		Utility.gender = jsonObject.getString("gender");
		Utility.age = Utility.calcAge(new Date(birthday));

		mHandler.post(new Runnable() {
		    @Override
		    public void run() {
			Utility.userPic = Utility.getBitmap(picURL);
		    }
		});

	    } catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

}
