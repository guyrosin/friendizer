package com.teamagly.friendizer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.teamagly.friendizer.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

/**
 * @author Guy
 * 
 */
public class FriendizerActivity extends TabActivity {
    public static final String APP_ID = "273844699335189";
    private Handler mHandler;

    /** Called when the activity is first created. */
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
	String access_token = getPreferences(MODE_PRIVATE).getString("access_token", null);
	long expires = getPreferences(MODE_PRIVATE).getLong("access_expires", 0);
	if (access_token != null) {
	    Utility.facebook.setAccessToken(access_token);
	}
	if (expires != 0) {
	    Utility.facebook.setAccessExpires(expires);
	}

	Toast.makeText(getApplicationContext(), "Session is " + Utility.facebook.isSessionValid(), Toast.LENGTH_SHORT).show();
	/*
	 * Only call authorize if the access_token has expired.
	 */
	if (!Utility.facebook.isSessionValid()) {

	    Utility.facebook.authorize(this, new String[] { "user_activities", "user_checkins", "user_interests", "user_likes",
		    "user_birthday", "friends_online_presence", "friends_birthday" }, new DialogListener() {
		@Override
		public void onComplete(Bundle values) {
		    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
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

	TabHost tabHost = getTabHost();

	// Tab for People Radar
	TabSpec peopleRadarSpec = tabHost.newTabSpec("People Radar");
	peopleRadarSpec.setIndicator("People Radar", getResources().getDrawable(R.drawable.ic_tab_people_radar_selected));
	Intent peopleRadarIntent = new Intent(this, PeopleRadarActivity.class);
	peopleRadarSpec.setContent(peopleRadarIntent);

	// Tab for Friends
	TabSpec friendsSpec = tabHost.newTabSpec("Friends");
	// setting Title and Icon for the Tab
	friendsSpec.setIndicator("Friends", getResources().getDrawable(R.drawable.ic_tab_friends_unselected));
	Intent friendsIntent = new Intent(this, FriendsListActivity.class);
	friendsSpec.setContent(friendsIntent);

	// Tab for My Profile
	TabSpec myProfileSpec = tabHost.newTabSpec("My Profile");
	myProfileSpec.setIndicator("My Profile", getResources().getDrawable(R.drawable.ic_tab_my_profile_unselected));
	Intent myProfileIntent = new Intent(this, MyProfileActivity.class);
	myProfileSpec.setContent(myProfileIntent);

	// Adding all TabSpec to TabHost
	tabHost.addTab(peopleRadarSpec); // Adding People Radar tab
	tabHost.addTab(friendsSpec); // Adding Friends tab
	tabHost.addTab(myProfileSpec); // Adding My Profile tab
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);

	Utility.facebook.authorizeCallback(requestCode, resultCode, data);
	requestUserData();
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
		mHandler.post(new Runnable() {
		    @Override
		    public void run() {
			Toast.makeText(getApplicationContext(), "Your name: " + Utility.userName, Toast.LENGTH_SHORT).show();
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
		mHandler.post(new Runnable() {
		    @Override
		    public void run() {
			Toast.makeText(getApplicationContext(), "Failed to request user data", Toast.LENGTH_SHORT).show();
		    }
		});
	    }
	}

    }

    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main_menu, menu);
	return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.settings_title:
	    startActivity(new Intent(this, FriendsPrefs.class));
	    return true;
	case R.id.about_title:
	    Toast.makeText(getApplicationContext(), "Made by Team AGLY (F**k Yeah!)", Toast.LENGTH_SHORT).show();
	    return true;
	case R.id.logout_title:
	    try {
		// Clear the preferences (access token) and logout
		Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		Utility.facebook.logout(this);
	    } catch (MalformedURLException e1) {
		e1.printStackTrace();
	    } catch (IOException e1) {
		e1.printStackTrace();
	    }
	    // No matter what happened, just quit the app
	    finish();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

}