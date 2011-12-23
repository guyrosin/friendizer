package com.teamagly.friendizer;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {
    public static final String APP_ID = "273844699335189";
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.splash);
	if (!isOnline()){
	    ((TextView)findViewById(R.id.status)).setText("Not Internet Connection");
	}
	mHandler = new Handler();
	// Create the Facebook object using the app ID.
	Utility.facebook = new Facebook(APP_ID);
	// Instantiate the asyncrunner object for asynchronous api calls.
	Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.facebook);

	/*
	 * Get existing access_token if any
	 */
	String access_token = getSharedPreferences(Utility.PREFS_NAME, MODE_PRIVATE).getString("access_token", null);
	long expires = getSharedPreferences(Utility.PREFS_NAME, MODE_PRIVATE).getLong("access_expires", 0);
	if (access_token != null) {
	    Utility.facebook.setAccessToken(access_token);
	}
	if (expires != 0) {
	    Utility.facebook.setAccessExpires(expires);
	}

	// Toast.makeText(getApplicationContext(), "Session is " + Utility.facebook.isSessionValid(), Toast.LENGTH_SHORT).show();
	/*
	 * Only call authorize if the access_token has expired.
	 */
	if (!Utility.facebook.isSessionValid()) {

	    Utility.facebook.authorize(this, new String[] { "user_activities", "user_checkins", "user_interests", "user_likes",
		    "user_birthday", "friends_online_presence", "friends_birthday" }, new DialogListener() {
		@Override
		public void onComplete(Bundle values) {
		    SharedPreferences.Editor editor = getSharedPreferences(Utility.PREFS_NAME, MODE_PRIVATE).edit();
		    editor.putString("access_token", Utility.facebook.getAccessToken());
		    editor.putLong("access_expires", Utility.facebook.getAccessExpires());
		    editor.commit();
		    requestUserData();
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
	} else
	    requestUserData();
    }

    /**
     * @return true iff the user is connected to the internet
     */
    public boolean isOnline() {
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo netInfo = cm.getActiveNetworkInfo();
	if (netInfo != null && netInfo.isConnected()) {
	    return true;
	}
	return false;
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
	// Continue to the main activity
	Intent intent = new Intent().setClass(SplashActivity.this, FriendizerActivity.class);
	startActivity(intent);
	finish();
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
		mHandler.post(new Runnable() {
		    @Override
		    public void run() {
			Toast.makeText(getApplicationContext(), "Welcome " + Utility.userName + "!", Toast.LENGTH_LONG).show();
		    }
		});
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
		// mHandler.post(new Runnable() {
		// @Override
		// public void run() {
		// Toast.makeText(getApplicationContext(), "Failed to request user data", Toast.LENGTH_SHORT).show();
		// }
		// });
	    }
	}
    }

}
