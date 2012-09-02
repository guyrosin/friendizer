package com.teamagly.friendizer.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.Overlay;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
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

public class NearbyMapActivity extends SherlockMapActivity implements ActionBar.TabListener {

	private final String TAG = getClass().getName();
	public static final String ACTION_UPDATE_LOCATION = "com.teamagly.friendizer.UPDATE_LOCATION";

	ActionBar actionBar;
	ArrayList<Integer> tabs = new ArrayList<Integer>();
	protected TapControlledMapView mapView;
	protected List<Overlay> mapOverlays;
	protected Drawable stub;
	protected CustomItemizedOverlay myItemizedOverlay;
	protected CustomItemizedOverlay nearbyUsersItemizedOverlay;
	LinearLayout markerLayout;

	// Variables for the "shake to reload" feature
	private SensorManager mSensorManager;
	private float mAccel; // acceleration apart from gravity
	private float mAccelCurrent; // current acceleration including gravity
	private float mAccelLast; // last acceleration including gravity
	private final SensorEventListener accelerometerListener = new SensorEventListener() {
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
		setTabs();

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			buildAlertMessageNoGPS();

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
		// Create an overlay for my locationInfo
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
		if (Utility.getInstance().getLocation() != null) {
			mapView.getController().animateTo(Utility.getInstance().getLocation());
			mapView.getController().setZoom(17);
		} else
			Toast.makeText(this, "Waiting for location...", Toast.LENGTH_SHORT).show();
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		setSupportProgressBarIndeterminateVisibility(true);

		myItemizedOverlay.clear();
		nearbyUsersItemizedOverlay.clear();
		mapView.invalidate();

		if (Utility.getInstance().getLocation() != null) {
			CustomOverlayItem myOverlayItem = new CustomOverlayItem(Utility.getInstance().getLocation(),
					Utility.getInstance().userInfo, markerLayout);
			myItemizedOverlay.addOverlay(myOverlayItem);
			zoomMyLocation();
		}
		mapView.invalidate();

		mSensorManager.registerListener(accelerometerListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);

		registerReceiver(locationReceiver, new IntentFilter(ACTION_UPDATE_LOCATION));

		requestFriends();
	}

	public BroadcastReceiver locationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Got a location update");
			onResume();
		}
	};

	private void setTabs() {
		tabs = new ArrayList<Integer>();
		tabs.add(R.string.nearby);
		tabs.add(R.string.friends);
		tabs.add(R.string.my_profile);

		// Create the tabs
		actionBar.addTab(actionBar.newTab().setText(R.string.nearby).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.friends).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.my_profile).setTabListener(this));
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(accelerometerListener);
		unregisterReceiver(locationReceiver);
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish(); // Quit
	}

	/**
	 * Clears the current users list and request the information from Facebook
	 */
	protected void requestFriends() {
		class NearbyUsersTask extends AsyncTask<Long, Void, JSONArray> {
			FriendizerUser[] nearbyUsers;

			protected JSONArray doInBackground(Long... userIDs) {
				try {
					nearbyUsers = ServerFacade.nearbyUsers(Utility.getInstance().userInfo.getId());
					// Build a comma separated string of all the users' IDs
					StringBuilder IDsBuilder = new StringBuilder();
					for (int i = 0; i < nearbyUsers.length - 1; i++)
						IDsBuilder.append(nearbyUsers[i].getId() + ",");
					IDsBuilder.append(nearbyUsers[nearbyUsers.length - 1].getId());
					Bundle params = new Bundle();
					// Request the details of each nearby user
					// Note: must order by uid (same as ownList servlet) so the next for loop will work!
					String query = "SELECT name, uid, pic_square, sex, birthday_date from user where uid in ("
							+ IDsBuilder.toString() + ") order by uid";
					params.putString("method", "fql.query");
					params.putString("query", query);
					try {
						String response = Utility.getInstance().facebook.request(params);
						return new JSONArray(response);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
				} catch (Exception e) {
					return new JSONArray();
				}
				return new JSONArray();
			}

			protected void onPostExecute(final JSONArray jsonArray) {
				if (jsonArray.length() == 0)
					setSupportProgressBarIndeterminateVisibility(false);
				else {
					try {
						int len = jsonArray.length();
						for (int i = 0; i < len; i++) {
							User userInfo = new User(nearbyUsers[i],
									new FacebookUser(jsonArray.getJSONObject(i), FBQueryType.FQL));
							CustomOverlayItem overlayItem = new CustomOverlayItem(userInfo.getGeoPoint(), userInfo, markerLayout);
							nearbyUsersItemizedOverlay.addOverlay(overlayItem);
						}
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					} finally {
						mapView.invalidate();
						setSupportProgressBarIndeterminateVisibility(false);
					}
					// for (int i = 0; i < nearbyUsersItemizedOverlay.size(); i++)
					// Log.e(TAG, "yo: " + nearbyUsersItemizedOverlay.getItem(i).getTitle() + ", "
					// + nearbyUsersItemizedOverlay.getItem(i).getPoint());
				}
			}
		}

		new NearbyUsersTask().execute(Utility.getInstance().userInfo.getId());
	}

	/**
	 * Shows an alert dialog in case the GPS is disabled
	 */
	private void buildAlertMessageNoGPS() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?").setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				}).setNegativeButton("Skip", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
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
		case R.id.menu_list: // Move to the nearby users list activity
			Intent intent = new Intent(this, FriendizerActivity.class).putExtra("nearby_list", true);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
			return true;
		case R.id.menu_refresh:
			LocationLibrary.forceLocationUpdate(this); // Force a locationInfo update
			Toast.makeText(this, "Waiting for location...", Toast.LENGTH_LONG).show();
			setSupportProgressBarIndeterminateVisibility(true); // Show a loading indicator
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

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabSelected(com.actionbarsherlock.app.ActionBar.Tab,
	 * android.support.v4.app.FragmentTransaction)
	 */
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int selectedTabID = tabs.get(tab.getPosition());
		if (selectedTabID != R.string.nearby) {
			startActivity(new Intent(this, FriendizerActivity.class).putExtra("tab", selectedTabID).setFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION));
			overridePendingTransition(0, 0);
			finish();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabUnselected(com.actionbarsherlock.app.ActionBar.Tab,
	 * android.support.v4.app.FragmentTransaction)
	 */
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabReselected(com.actionbarsherlock.app.ActionBar.Tab,
	 * android.support.v4.app.FragmentTransaction)
	 */
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
}