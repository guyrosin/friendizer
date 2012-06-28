package com.teamagly.friendizer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.teamagly.friendizer.utils.MessageHandler;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Util;
import com.teamagly.friendizer.utils.Utility;

public class GCMIntentService extends GCMBaseIntentService {
	protected String senderID;

	public GCMIntentService() {
		super(Utility.SENDER_ID);
	}

	private final String TAG = getClass().getName();

	/*
	 * (non-Javadoc)
	 * @see com.google.android.gcm.GCMBaseIntentService#onError(android.content.Context, java.lang.String)
	 */
	@Override
	protected void onError(Context context, String errorId) {
		if ("SERVICE_NOT_AVAILABLE".equals(errorId)) {
			// optionally retry using exponential back-off
			// (see Advanced Topics)
		} else {
			// Unrecoverable error, log it
			Log.i(TAG, "Received error: " + errorId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.gcm.GCMBaseIntentService#onMessage(android.content.Context, android.content.Intent)
	 */
	@Override
	protected void onMessage(Context context, Intent intent) {
		new MessageHandler().handleMessage(intent);
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.gcm.GCMBaseIntentService#onRegistered(android.content.Context, java.lang.String)
	 */
	@Override
	protected void onRegistered(Context context, final String regId) {
		// Store registration ID on shared preferences
		SharedPreferences settings = Util.getSharedPreferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(GCMRegistrar.getRegistrationId(context), regId);
		editor.commit();
		getBaseContext().sendBroadcast(new Intent(Util.UPDATE_UI_INTENT));
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.gcm.GCMBaseIntentService#onUnregistered(android.content.Context, java.lang.String)
	 */
	@Override
	protected void onUnregistered(Context context, final String regId) {
		// Notify 3rd-party server about the unregistered ID
		ServerFacade.unregister(regId);
	}
}