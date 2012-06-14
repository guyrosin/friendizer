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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.AchievementsAdapter;
import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class FriendAchievementsActivity extends SherlockActivity {

	private final String TAG = getClass().getName();
	ActionBar actionBar;
	AchievementsAdapter adapter;
	ArrayList<Achievement> achievements = new ArrayList<Achievement>();
	ListView listView;
	User userInfo;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.achievements_layout);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		listView = (ListView) findViewById(R.id.achievements_list);
		userInfo = (User) getIntent().getSerializableExtra("user");
		actionBar.setTitle(userInfo.getName());
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		setSupportProgressBarIndeterminateVisibility(true);
		achievements.clear();
		adapter = new AchievementsAdapter(this, R.layout.achievements_list_item, achievements);
		listView.setAdapter(adapter);

		// Get the user's achievements in the background
		new Thread(new Runnable() {
			public void run() {
				try {
					Achievement[] achvs = ServerFacade.getAchievements(userInfo.getId());
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
	 * @see com.actionbarsherlock.app.SherlockActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: // Move to the user's profile
			Intent intent = new Intent(this, FriendProfileActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("user", userInfo);
			startActivity(intent);
			return true;
		case R.id.menu_refresh:
			onResume();
			return true;
		case R.id.menu_feedback:
			return Utility.startFeedback(this);
		case R.id.menu_settings: // Move to the settings activity
			startActivity(new Intent(this, FriendsPrefs.class));
			return true;
		case R.id.menu_invite: // Show the Facebook invitation dialog
			Bundle params = new Bundle();
			params.putString("message", getString(R.string.invitation_msg));
			Utility.getInstance().facebook.dialog(this, "apprequests", params, new BaseDialogListener());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
