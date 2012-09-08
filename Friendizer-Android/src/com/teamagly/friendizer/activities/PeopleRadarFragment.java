package com.teamagly.friendizer.activities;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.squareup.seismic.ShakeDetector;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeopleRadarFragment extends AbstractFriendsListFragment implements ShakeDetector.Listener {
	private final String TAG = getClass().getName();
	private ShakeDetector shakeDetector;
	protected NearbyUsersTask task = new NearbyUsersTask();

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		shakeDetector = new ShakeDetector(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity.setContentView(R.layout.connections_layout);
		TextView empty = (TextView) activity.findViewById(R.id.empty);
		empty.setText("Forever Alone! (no people nearby)");
		gridView = (GridView) activity.findViewById(R.id.gridview);
	}

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.AbstractFriendsListActivity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		SensorManager sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		shakeDetector.start(sensorManager);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		task.cancel(true);
		shakeDetector.stop();
	}

	/*
	 * (non-Javadoc)
	 * @see com.squareup.seismic.ShakeDetector.Listener#hearShake()
	 */
	@Override
	public void hearShake() {
		// Reload the data
		activity.setSupportProgressBarIndeterminateVisibility(true);
		requestFriends();
	}

	/**
	 * Clears the current users list and request the information from Facebook
	 */
	@Override
	protected void requestFriends() {
		usersList.clear();
		task = new NearbyUsersTask();
		task.execute(Utility.getInstance().userInfo.getId());
	}

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onCreateOptionsMenu(com.actionbarsherlock.view.Menu,
	 * com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.people_radar_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_map: // Move to the map activity
			startActivity(new Intent(activity, NearbyMapActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION));
			activity.overridePendingTransition(0, 0);
			activity.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class NearbyUsersTask extends AsyncTask<Long, Void, List<User>> {

		@Override
		protected List<User> doInBackground(Long... userIDs) {
			try {
				return ServerFacade.nearbyUsers(userIDs[0]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			return new ArrayList<User>();
		}

		@Override
		protected void onPostExecute(final List<User> nearbyUsers) {
			TextView empty = (TextView) activity.findViewById(R.id.empty);
			if (nearbyUsers.size() == 0)
				empty.setVisibility(View.VISIBLE);
			else {
				usersList.addAll(nearbyUsers);
				sort();
				empty.setVisibility(View.GONE);
			}
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
	}
}
