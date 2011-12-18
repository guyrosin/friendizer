package com.teamagly.friendizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.teamagly.friendizer.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
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
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

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
	Intent friendsIntent = new Intent(this, FriendsActivity.class);
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

    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main_menu, menu);
	return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.settings_title:
	    startActivity(new Intent(this, Prefs.class));
	    return true;
	case R.id.about_title:
	    Toast.makeText(getApplicationContext(), "Made by Team AGLY (F**k Yeah!)", Toast.LENGTH_SHORT).show();
	    return true;
	case R.id.logout_title:
	    Utility.mAsyncRunner.logout(getBaseContext(), new RequestListener() {

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
		}

		@Override
		public void onIOException(IOException e, Object state) {
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
		}

		@Override
		public void onComplete(String response, Object state) {
		}
	    });
	    // No matter what happens, just quit the app
	    finish();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

}