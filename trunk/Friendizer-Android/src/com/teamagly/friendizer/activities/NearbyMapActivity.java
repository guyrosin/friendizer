package com.teamagly.friendizer.activities;

import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.readystatesoftware.maps.OnSingleTapListener;
import com.readystatesoftware.maps.TapControlledMapView;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.User.FBQueryType;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.widgets.CustomItemizedOverlay;
import com.teamagly.friendizer.widgets.CustomOverlayItem;

public class NearbyMapActivity extends SherlockMapActivity {

	private final String TAG = "NearbyMapActivity";
	ActionBar actionBar;

	protected TapControlledMapView mapView;
	protected List<Overlay> mapOverlays;
	protected Drawable stub;
	protected CustomItemizedOverlay myItemizedOverlay;
	protected CustomItemizedOverlay nearbyUsersItemizedOverlay;
	LinearLayout markerLayout;
	GeoPoint myLocationPoint;

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
				setSupportProgressBarIndeterminateVisibility(true);
				requestFriends();
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.map_layout);
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		stub = getResources().getDrawable(R.drawable.stub);
		ImageView meButton = (ImageView) findViewById(R.id.meButton);
		meButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomMyLocation();
			}
		});

		// extract MapView from layout
		mapView = (TapControlledMapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		// dismiss balloon upon single tap of MapView (iOS behavior)
		mapView.setOnSingleTapListener(new OnSingleTapListener() {
			@Override
			public boolean onSingleTap(MotionEvent e) {
				myItemizedOverlay.hideAllBalloons();
				nearbyUsersItemizedOverlay.hideAllBalloons();
				return true;
			}
		});

		mapOverlays = mapView.getOverlays();
		markerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.map_marker_layout, null);
		// Create an overlay for my location
		myItemizedOverlay = new CustomItemizedOverlay(stub, mapView);
		// set iOS behavior attributes for overlay
		myItemizedOverlay.setShowClose(false);
		myItemizedOverlay.setShowDisclosure(true);
		myItemizedOverlay.setSnapToCenter(false);

		// Create overlays for nearby users
		nearbyUsersItemizedOverlay = new CustomItemizedOverlay(stub, mapView);
		// set iOS behavior attributes for overlay
		nearbyUsersItemizedOverlay.setShowClose(false);
		nearbyUsersItemizedOverlay.setShowDisclosure(true);
		nearbyUsersItemizedOverlay.setSnapToCenter(false);

		mapOverlays.add(nearbyUsersItemizedOverlay);
		mapOverlays.add(myItemizedOverlay);

		// Shake to reload functionality
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;
	}

	protected void zoomMyLocation() {
		if (myLocationPoint != null) {
			mapView.getController().animateTo(myLocationPoint);
			mapView.getController().setZoom(17);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		setSupportProgressBarIndeterminateVisibility(true);

		nearbyUsersItemizedOverlay.clear();
		myItemizedOverlay.clear();

		if (Utility.getInstance().location != null) {
			myLocationPoint = locationToGeoPoint(Utility.getInstance().location);
			CustomOverlayItem myOverlayItem = new CustomOverlayItem(myLocationPoint, Utility.getInstance().userInfo, markerLayout);
			myItemizedOverlay.addOverlay(myOverlayItem);
			zoomMyLocation();
		}
		myItemizedOverlay.populateNow();
		mapView.invalidate();

		mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);

		requestFriends();
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mSensorListener);
	}

	/**
	 * Clears the current users list and request the information from Facebook
	 */
	protected void requestFriends() {
		class NearbyUsersTask extends AsyncTask<Long, Void, FriendizerUser[]> {

			protected FriendizerUser[] doInBackground(Long... userIDs) {
				try {
					return ServerFacade.nearbyUsers(Utility.getInstance().userInfo.getId());
				} catch (Exception e) {
					return new FriendizerUser[] {};
				}
			}

			protected void onPostExecute(final FriendizerUser[] nearbyUsers) {
				if (nearbyUsers.length == 0) {
					setSupportProgressBarIndeterminateVisibility(false);
				} else {
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
									final User userInfo = new User(nearbyUsers[i], new FacebookUser(jsonArray.getJSONObject(i),
											FBQueryType.FQL));
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											CustomOverlayItem overlayitem = new CustomOverlayItem(userInfo.getGeoPoint(),
													userInfo, markerLayout);
											nearbyUsersItemizedOverlay.addOverlay(overlayitem);
										}
									});
								}
								nearbyUsersItemizedOverlay.populateNow();
								mapView.invalidate();
							} catch (Exception e) {
								Log.e(TAG, e.getMessage());
							} finally {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										setSupportProgressBarIndeterminateVisibility(false);
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
	 * @see com.actionbarsherlock.app.SherlockMapActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		inflater.inflate(R.menu.nearby_map_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockMapActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: // Go home (up)
			Intent intent = new Intent(this, FriendizerActivity.class).putExtra("tab", R.string.nearby);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.menu_list: // Move to the nearby users list activity
			intent = new Intent(this, FriendizerActivity.class).putExtra("tab", R.string.nearby);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	protected GeoPoint locationToGeoPoint(Location location) {
		int lat = (int) (location.getLatitude() * 1E6);
		int lng = (int) (location.getLongitude() * 1E6);
		return new GeoPoint(lat, lng);
	}
}