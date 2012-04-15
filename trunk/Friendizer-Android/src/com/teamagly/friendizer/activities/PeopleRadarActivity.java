package com.teamagly.friendizer.activities;

import org.json.JSONArray;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.User.FBQueryType;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PeopleRadarActivity extends AbstractFriendsListActivity {
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
	    mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
	    float delta = mAccelCurrent - mAccelLast;
	    mAccel = mAccel * 0.9f + delta; // perform low-cut filter

	    // Check if the device is shaken
	    if (mAccel > 2)
		onResume();
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
    };

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.connections_layout);
	gridView = (GridView) findViewById(R.id.gridview);
	TextView empty = (TextView) findViewById(R.id.forever_alone_text);
	empty.setText("Forever Alone! (no people nearby)");
	updateListType(list_type);

	// Shake to reload functionality
	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		SensorManager.SENSOR_DELAY_NORMAL);
	mAccel = 0.00f;
	mAccelCurrent = SensorManager.GRAVITY_EARTH;
	mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    /*
     * (non-Javadoc)
     * @see com.teamagly.friendizer.AbstractFriendsListActivity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
	super.onStop();
	mSensorManager.unregisterListener(mSensorListener);
    }

    /**
     * Clears the current users list and request the information from Facebook
     */
    @Override
    protected void requestFriends() {
	LinearLayout empty = (LinearLayout) findViewById(R.id.empty);
	usersList.clear();
	try {
	    final FriendizerUser[] nearbyUsers = ServerFacade.nearbyUsers(Utility.getInstance().userInfo.getId());
	    if (nearbyUsers.length == 0) {
		showLoadingIcon(false);
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
			    // Request the details of each user I own
			    String query = "SELECT name, uid, pic_square, sex, birthday_date from user where uid in ("
				    + IDsBuilder.toString() + ") order by name";
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
				    showLoadingIcon(false);
				    friendsAdapter.notifyDataSetChanged(); // Notify the adapter
				}
			    });
			}
		    }
		}).start();
	    }
	} catch (Exception e) {
	    empty.setVisibility(View.VISIBLE);
	    showLoadingIcon(false);
	}
    }
}
