/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.AchievementsAdapter;
import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class AchievementsActivity extends SherlockActivity implements OnItemClickListener {

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
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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
		listView.setOnItemClickListener(this);
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
			@Override
			public void run() {
				try {
					List<Achievement> achvs = ServerFacade.getAchievements(user.getId());
					achievements.addAll(achvs);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Achievement achv = achievements.get(position);
		// Show a dialog with info
		Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.achievement_info_dialog);
		dialog.setTitle(achv.getTitle());

		TextView description = (TextView) dialog.findViewById(R.id.achv_desc);
		description.setText(achv.getDescription());
		TextView reward = (TextView) dialog.findViewById(R.id.achv_reward);
		reward.setText(String.valueOf(achv.getReward()));
		TextView txtEarned = (TextView) dialog.findViewById(R.id.achv_earned);
		String earned = "";
		if (user.getId() == Utility.getInstance().userInfo.getId())
			earned = achv.isEarned() ? "&#10003; You've earned it!" : "&#10007; You've not earned it yet.";
		else
			earned = achv.isEarned() ? "&#10003; " + user.getFirstName() + " has earned this achievement!" : "&#10007; " + user.getFirstName() + " hasn't earned it yet.";
		txtEarned.setText(Html.fromHtml(earned));
		ImageView image = (ImageView) dialog.findViewById(R.id.achv_icon);

		// Load the image resource
		String uri = "drawable/" + achv.getIconRes();
		int imageResource = getResources().getIdentifier(uri, null, getPackageName());
		try {
			Drawable drawable = getResources().getDrawable(imageResource);
			image.setImageDrawable(drawable);
		} catch (NotFoundException e) { // The image wasn't found
			Log.e(TAG, e.getMessage());
		}
		dialog.show();
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
				intent.putExtra("tab", R.string.my_profile);
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
