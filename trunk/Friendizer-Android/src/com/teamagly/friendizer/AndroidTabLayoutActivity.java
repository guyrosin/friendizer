package com.teamagly.friendizer;

import com.teamagly.friendizer.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class AndroidTabLayoutActivity extends TabActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TabHost tabHost = getTabHost();

		// Tab for People Radar
		TabSpec peopleRadarSpec = tabHost.newTabSpec("People Radar");
		peopleRadarSpec.setIndicator("People Radar", getResources().getDrawable(R.drawable.icon_peopleradar_tab));
		Intent peopleRadarIntent = new Intent(this, PeopleRadarActivity.class);
		peopleRadarSpec.setContent(peopleRadarIntent);

		// Tab for Friends
		TabSpec friendsSpec = tabHost.newTabSpec("Friends");
		// setting Title and Icon for the Tab
		friendsSpec.setIndicator("Friends", getResources().getDrawable(R.drawable.icon_friends_tab));
		Intent friendsIntent = new Intent(this, FriendsActivity.class);
		friendsSpec.setContent(friendsIntent);

		// Tab for My Profile
		TabSpec myProfileSpec = tabHost.newTabSpec("My Profile");
		myProfileSpec.setIndicator("My Profile", getResources().getDrawable(R.drawable.icon_myprofile_tab));
		Intent myProfileIntent = new Intent(this, MyProfileActivity.class);
		myProfileSpec.setContent(myProfileIntent);

		// Adding all TabSpec to TabHost
		tabHost.addTab(peopleRadarSpec); // Adding People Radar tab
		tabHost.addTab(friendsSpec); // Adding Friends tab
		tabHost.addTab(myProfileSpec); // Adding My Profile tab
	}
}