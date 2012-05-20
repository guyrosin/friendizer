package com.teamagly.friendizer.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.Overlay;
import com.readystatesoftware.maps.OnSingleTapListener;
import com.readystatesoftware.maps.TapControlledMapView;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.User.FBQueryType;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.widgets.CustomItemizedOverlay;
import com.teamagly.friendizer.widgets.CustomOverlayItem;

public class MapLocationActivity extends MapActivity {

    private final String TAG = "MapLocationActivity";
    protected ArrayList<User> usersList = new ArrayList<User>();
    protected final Handler handler = new Handler();

    protected TapControlledMapView mapView;
    protected List<Overlay> mapOverlays;
    protected Drawable myDrawable;
    protected Drawable usersDrawable;
    protected CustomItemizedOverlay<CustomOverlayItem> myItemizedOverlay;
    protected CustomItemizedOverlay<CustomOverlayItem> nearbyUsersItemizedOverlay;

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
	    mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
	    float delta = mAccelCurrent - mAccelLast;
	    mAccel = mAccel * 0.9f + delta; // perform low-cut filter

	    // Check if the device is shaken
	    if (mAccel > 2.3) {
		// Reload the data
		showLoadingIcon(true);
		requestFriends();
	    }
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// main.xml contains a MapView
	setContentView(R.layout.map_layout);
	myDrawable = this.getResources().getDrawable(R.drawable.map_marker);
	usersDrawable = this.getResources().getDrawable(R.drawable.map_marker2);

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
	// Create an overlay for my location
	myItemizedOverlay = new CustomItemizedOverlay<CustomOverlayItem>(myDrawable, mapView);
	// set iOS behavior attributes for overlay
	myItemizedOverlay.setShowClose(false);
	myItemizedOverlay.setShowDisclosure(true);
	myItemizedOverlay.setSnapToCenter(false);

	// Create overlays for nearby users
	nearbyUsersItemizedOverlay = new CustomItemizedOverlay<CustomOverlayItem>(usersDrawable, mapView);
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

    /*
     * (non-Javadoc)
     * @see com.google.android.maps.MapActivity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	showLoadingIcon(true);

	nearbyUsersItemizedOverlay.clear();
	myItemizedOverlay.clear();

	if (Utility.getInstance().location != null) {
	    GeoPoint myLocationPoint = locationToGeoPoint(Utility.getInstance().location);
	    mapView.getController().setCenter(myLocationPoint);
	    mapView.getController().setZoom(17);
	    CustomOverlayItem overlayitem = new CustomOverlayItem(myLocationPoint, Utility.getInstance().userInfo);
	    myItemizedOverlay.addOverlay(overlayitem);
	}

//	mapView.postInvalidate();

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
	usersList.clear();
	try {
	    final FriendizerUser[] nearbyUsers = ServerFacade.nearbyUsers(Utility.getInstance().userInfo.getId());
	    if (nearbyUsers.length == 0) {
		showLoadingIcon(false);
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
			    String query = "SELECT name, uid, pic_square, sex, birthday_date from user where uid in ("
				    + IDsBuilder.toString() + ") order by name";
			    params.putString("method", "fql.query");
			    params.putString("query", query);
			    String response = Utility.getInstance().facebook.request(params);
			    JSONArray jsonArray = new JSONArray(response);
			    int len = jsonArray.length();
			    for (int i = 0; i < len; i++) {
				User userInfo = new User(nearbyUsers[i], new FacebookUser(jsonArray.getJSONObject(i),
					FBQueryType.FQL));
				usersList.add(userInfo);
				CustomOverlayItem overlayitem = new CustomOverlayItem(userInfo.getGeoPoint(), userInfo);
				nearbyUsersItemizedOverlay.addOverlay(overlayitem);
			    }
			} catch (Exception e) {
			    Log.e(TAG, e.getMessage());
			} finally {
			    handler.post(new Runnable() {
				@Override
				public void run() {
				    showLoadingIcon(false);
				}
			    });
			}
		    }
		}).start();
	    }
	} catch (Exception e) {
	    Log.w(TAG, e.getMessage());
	    showLoadingIcon(false);
	}
    }

    /**
     * @param show
     *            whether to show or hide the loading icon (in the parent activity)
     */
    protected void showLoadingIcon(boolean show) {
	Log.d(TAG, "showLoadingIcon: " + show);
	try {
	    Activity parent = getParent();
	    if (parent != null)
		((FriendizerActivity) parent).actionBar.showProgressBar(show);
	} catch (Exception e) {
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