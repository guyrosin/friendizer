package com.teamagly.friendizer.activities;

import org.json.JSONArray;

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
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.User.FBQueryType;
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
		updateListType(list_type);

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
		Log.d(TAG, "PeopleRadar onResume()");
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
		class NearbyUsersTask extends AsyncTask<Long, Void, FriendizerUser[]> {

			protected FriendizerUser[] doInBackground(Long... userIDs) {
				try {
					return ServerFacade.nearbyUsers(userIDs[0]);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
					return new FriendizerUser[] {};
				}
			}

			protected void onPostExecute(final FriendizerUser[] nearbyUsers) {
				TextView empty = (TextView) activity.findViewById(R.id.empty);
				if (nearbyUsers.length == 0) {
					activity.setSupportProgressBarIndeterminateVisibility(false);
					empty.setVisibility(View.VISIBLE);
				} else {
					empty.setVisibility(View.GONE);
					new Thread(new Runnable() {
						@Override
						public void run() {
							// Build a comma separated string of all the users' IDs
							StringBuilder IDsBuilder = new StringBuilder();
							for (int i = 0; i < nearbyUsers.length - 1; i++)
								IDsBuilder.append(nearbyUsers[i].getId() + ",");
							IDsBuilder.append(nearbyUsers[nearbyUsers.length - 1].getId());
							Bundle params = new Bundle();
							try {
								// Request the details of each nearby user
								// Note: must order by uid (same as ownList servlet) so the next for loop will work!
								String query = "SELECT name, uid, pic_square, sex, birthday_date from user where uid in ("
										+ IDsBuilder.toString() + ") order by uid";
								params.putString("method", "fql.query");
								params.putString("query", query);
								String response = Utility.getInstance().facebook.request(params);
								JSONArray jsonArray = new JSONArray(response);
								int len = jsonArray.length();
								for (int i = 0; i < len; i++) {
									usersList.add(new User(nearbyUsers[i], new FacebookUser(jsonArray.getJSONObject(i),
											FBQueryType.FQL)));
								}
							} catch (Exception e) {
								Log.e(TAG, e.getMessage());
							} finally {
								handler.post(new Runnable() {
									@Override
									public void run() {
										activity.setSupportProgressBarIndeterminateVisibility(false);
										friendsAdapter.notifyDataSetChanged(); // Notify the adapter
									}
								});
							}
						}
					}).start();
				}
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
			startActivity(new Intent(activity, NearbyMapActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
