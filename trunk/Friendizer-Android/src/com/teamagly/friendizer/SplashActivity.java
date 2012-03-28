package com.teamagly.friendizer;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.teamagly.friendizer.R;

public class SplashActivity extends Activity {
    public static final String APP_ID = "273844699335189";
    private Handler mHandler;
    ProgressDialog dialog;
    UserRequestListener userRequestListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.splash);
	Intent intent = getIntent();
	if (intent.getBooleanExtra("logout", false))
	    logout();

	mHandler = new Handler();
	Utility.getInstance().imageLoader = new ImageLoader(getApplicationContext());
	// Create the Facebook object using the app ID.
	Utility.getInstance().facebook = new Facebook(APP_ID);
	// Instantiate the asyncrunner object for asynchronous api calls.
	Utility.getInstance().mAsyncRunner = new AsyncFacebookRunner(Utility.getInstance().facebook);

	// Listener for the login button
	final ImageView loginButton = (ImageView) findViewById(R.id.loginButton);
	loginButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		onResume();
	    }
	});
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();

	if (!isOnline()) {
	    ((TextView) findViewById(R.id.status)).setText("No Internet connection, please try again later");
	    return;
	}
	/*
	 * Get existing access_token if any
	 */
	String access_token = getSharedPreferences(Utility.PREFS_NAME, MODE_PRIVATE).getString("access_token", null);
	long expires = getSharedPreferences(Utility.PREFS_NAME, MODE_PRIVATE).getLong("access_expires", 0);
	if (access_token != null) {
	    Utility.getInstance().facebook.setAccessToken(access_token);
	}
	if (expires != 0) {
	    Utility.getInstance().facebook.setAccessExpires(expires);
	}

	/*
	 * Only call authorize if the access_token has expired.
	 */
	if (!Utility.getInstance().facebook.isSessionValid()) {

	    Utility.getInstance().facebook.authorize(this, new String[] { "user_activities", "user_checkins", "user_interests",
		    "user_likes", "user_birthday", "friends_online_presence", "friends_birthday" }, new DialogListener() {
		@Override
		public void onComplete(Bundle values) {
		    SharedPreferences.Editor editor = getSharedPreferences(Utility.PREFS_NAME, MODE_PRIVATE).edit();
		    editor.putString("access_token", Utility.getInstance().facebook.getAccessToken());
		    editor.putLong("access_expires", Utility.getInstance().facebook.getAccessExpires());
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
     * Logs out the user from Facebook
     */
    private void logout() {
	try {
	    // Clear the preferences (access token) and logout
	    Editor e = getSharedPreferences(Utility.PREFS_NAME, MODE_PRIVATE).edit();
	    e.clear();
	    e.commit();
	    Utility.getInstance().facebook.logout(getBaseContext());
	} catch (Exception e) {
	    Log.v("Splash", "ERROR: " + e.getMessage());
	}
    }

    /**
     * @return true iff the user is connected to the Internet
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
	Utility.getInstance().facebook.authorizeCallback(requestCode, resultCode, data);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
	// Quit the app
	moveTaskToBack(true);
    }

    /*
     * Request user details from Facebook
     */
    public void requestUserData() {
	dialog = ProgressDialog.show(this, "", getString(R.string.please_wait), true, true); // Show a loading dialog
	Bundle params = new Bundle();
	params.putString("fields", "name, first_name, picture, birthday, gender, inspirational_people, likes");
	// Send a new request only if there are none currently
	if ((userRequestListener == null) || (!userRequestListener.completed))
	    userRequestListener = new UserRequestListener();
	Utility.getInstance().mAsyncRunner.request("me", params, userRequestListener);
    }

    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {
	public boolean completed = false;

	@Override
	public void onComplete(final String response, final Object state) {
	    JSONObject jsonObject;
	    try {
		jsonObject = new JSONObject(response);
		final UserInfo userInfo = new UserInfo(jsonObject);
		Utility.getInstance().userInfo = userInfo;
		mHandler.post(new Runnable() {
		    @Override
		    public void run() {
			Toast.makeText(getApplicationContext(), "Welcome " + userInfo.name + "!", Toast.LENGTH_LONG).show();
		    }
		});

		// Register/login
		ServerFacade.register(userInfo.id);

		dialog.dismiss(); // Dismiss the loading dialog
		// Continue to the main activity
		Intent intent = new Intent().setClass(SplashActivity.this, FriendizerActivity.class);
		startActivity(intent);
		completed = true;
		finish();

	    } catch (Exception e) {
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
