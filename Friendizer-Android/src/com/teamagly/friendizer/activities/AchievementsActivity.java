/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.AchievementsAdapter;
import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class AchievementsActivity extends SherlockActivity {

	private final String TAG = getClass().getName();
	AchievementsAdapter adapter;
	ArrayList<Achievement> achievements = new ArrayList<Achievement>();
	ListView listView;
	ActionBar actionBar;
	protected User user;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user = ((User) getIntent().getSerializableExtra("user"));
		actionBar = getSupportActionBar();
		if (user.getId() == Utility.getInstance().userInfo.getId()) // If the user is the current one
			actionBar.setTitle("My Achievements");
		else {
			actionBar.setTitle(user.getName());
			actionBar.setSubtitle("Achievements");
		}
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.achievements_layout);
		listView = (ListView) findViewById(R.id.achievements_list);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		setSupportProgressBarIndeterminateVisibility(true);
		achievements.clear();
		adapter = new AchievementsAdapter(this, R.layout.achievements_list_item, achievements);
		listView.setAdapter(adapter);

		new Thread(new Runnable() {
			public void run() {
				try {
					Achievement[] achvs = ServerFacade.getAchievements(user.getId());
					for (Achievement achv : achvs)
						achievements.add(achv);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
						setSupportProgressBarIndeterminateVisibility(false);
					}
				});
			}
		}).start();
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: // Move to the user's profile
			Intent intent;
			if (user.getId() == Utility.getInstance().userInfo.getId()) { // If the user is the current one
				intent = new Intent(this, FriendizerActivity.class);
				// intent.putExtra("tab", R.string.my_profile);
			} else {
				intent = new Intent(this, FriendProfileActivity.class);
				intent.putExtra("user", user);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
