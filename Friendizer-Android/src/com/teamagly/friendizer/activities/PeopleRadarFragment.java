package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class PeopleRadarFragment extends AbstractFriendsListFragment {
	private final String TAG = getClass().getName();

	// Variables for the "shake to reload" feature
	private SensorManager mSensorManager;
	private float mAccel; // acceleration apart from gravity
	private float mAccelCurrent; // current acceleration including gravity
	private float mAccelLast; // last acceleration including gravity
	private final SensorEventListener mSensorListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent se) {
			float x = se.values[0];
			float y = se.values[1];
			float z = se.values[2];
			mAccelLast = mAccelCurrent;
			mAccelCurrent = FloatMath.sqrt(x * x + y * y + z * z);
			float delta = mAccelCurrent - mAccelLast;
			mAccel = mAccel * 0.9f + delta; // perform low-cut filter

			// Check if the device is shaken
			if (mAccel > 2.3) {
				// Reload the data
				activity.setSupportProgressBarIndeterminateVisibility(true);
				requestFriends();
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
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

		// Shake to reload functionality
		mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;
	}

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.AbstractFriendsListActivity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mSensorListener);
	}

	/**
	 * Clears the current users list and request the information from Facebook
	 */
	@Override
	protected void requestFriends() {
		usersList.clear();
		friendsAdapter.notifyDataSetChanged(); // Notify the adapter
		class NearbyUsersTask extends AsyncTask<Long, Void, List<User>> {

			protected List<User> doInBackground(Long... userIDs) {
				try {
					return ServerFacade.nearbyUsers(Utility.getInstance().userInfo.getId());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				return new ArrayList<User>();
			}

			protected void onPostExecute(final List<User> nearbyUsers) {
				TextView empty = (TextView) activity.findViewById(R.id.empty);
				if (nearbyUsers.size() == 0) {
					empty.setVisibility(View.VISIBLE);
				} else {
					for (User user : nearbyUsers)
						usersList.add(user);
					empty.setVisibility(View.GONE);
					friendsAdapter.notifyDataSetChanged(); // Notify the adapter
				}
				activity.setSupportProgressBarIndeterminateVisibility(false);
			}
		}

		new NearbyUsersTask().execute(Utility.getInstance().userInfo.getId());
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

}
