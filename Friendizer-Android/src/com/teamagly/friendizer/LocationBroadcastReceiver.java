/**
 * 
 */
package com.teamagly.friendizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
import com.teamagly.friendizer.activities.NearbyMapActivity;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class LocationBroadcastReceiver extends BroadcastReceiver {
	private final String TAG = getClass().getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.d(TAG, "onReceive: received location update");

		final LocationInfo locationInfo = (LocationInfo) intent
				.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
		Utility.getInstance().locationInfo = locationInfo;
		new Thread(new Runnable() {
			public void run() {
				try {
					// Update the server with the new location
					ServerFacade.changeLocation(Utility.getInstance().userInfo.getId(), locationInfo.lastLat,
							locationInfo.lastLong);
				} catch (Exception e) {
					Log.e(TAG, "Can't update the server with the new location", e);
				}
			}
		}).start();

		// Send a broadcast intent to the map activity
		Intent broadcastIntent = new Intent(NearbyMapActivity.ACTION_UPDATE_LOCATION);
		context.sendBroadcast(broadcastIntent);
	}
}