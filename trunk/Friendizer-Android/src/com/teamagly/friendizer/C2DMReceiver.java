/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *******************************************************************************/
package com.teamagly.friendizer;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.teamagly.friendizer.utils.DeviceRegistrar;
import com.teamagly.friendizer.utils.MessageDisplay;
import com.teamagly.friendizer.utils.Setup;
import com.teamagly.friendizer.utils.Util;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Receive a push message from the Cloud to Device Messaging (C2DM) service. This class should be modified to include
 * functionality specific to your application. This class must have a no-arg constructor and pass the sender id to the superclass
 * constructor. Note: Must be inside the default package!!!
 */
public class C2DMReceiver extends C2DMBaseReceiver {
    private final String TAG = getClass().getName();

    public C2DMReceiver() {
	super(Setup.SENDER_ID);
    }

    /**
     * Called when a registration token has been received.
     * 
     * @param context
     *            the Context
     * @param registrationId
     *            the registration id as a String
     * @throws IOException
     *             if registration cannot be performed
     */
    @Override
    public void onRegistered(Context context, String registration) {
	Log.d(TAG, "onRegistered()");
	DeviceRegistrar.registerWithServer(context, registration);
    }

    /**
     * Called when the device has been unregistered.
     * 
     * @param context
     *            the Context
     */
    @Override
    public void onUnregistered(Context context) {
	Log.d(TAG, "onUnregistered()");
	SharedPreferences prefs = Util.getSharedPreferences(context);
	String deviceRegistrationID = prefs.getString(Util.DEVICE_REGISTRATION_ID, null);
	DeviceRegistrar.unregisterWithServer(context, deviceRegistrationID);
    }

    /**
     * Called on registration error. This is called in the context of a Service - no dialog or UI.
     * 
     * @param context
     *            the Context
     * @param errorId
     *            an error message, defined in {@link C2DMBaseReceiver}
     */
    @Override
    public void onError(Context context, String errorId) {
	Log.d(TAG, "onError()");
	context.sendBroadcast(new Intent(Util.UPDATE_UI_INTENT));
    }

    /**
     * Called when a cloud message has been received.
     */
    @Override
    public void onMessage(Context context, Intent intent) {
	Log.d(TAG, "onMessage()");
	/*
	 * Replace this with your application-specific code
	 */
	MessageDisplay.displayMessage(context, intent);
    }
    // CTP onMessage
    /*
     * @Override public void onMessage(Context context, Intent intent) { Bundle extras = intent.getExtras(); if (extras != null) {
     * String url = (String) extras.get("url"); String title = (String) extras.get("title"); String sel = (String)
     * extras.get("sel"); String debug = (String) extras.get("debug"); if (debug != null) { // server-controlled debug - the
     * server wants to know // we received the message, and when. This is not user-controllable, // we don't want extra traffic on
     * the server or phone. Server may // turn this on for a small percentage of requests or for users // who report issues.
     * DefaultHttpClient client = new DefaultHttpClient(); HttpGet get = new HttpGet(AppEngineClient.BASE_URL + "/debug?id=" +
     * extras.get("collapse_key")); // No auth - the purpose is only to generate a log/confirm delivery // (to avoid overhead of
     * getting the token) try { client.execute(get); } catch (ClientProtocolException e) { // ignore } catch (IOException e) { //
     * ignore } } if (title != null && url != null && url.startsWith("http")) { SharedPreferences settings = Prefs.get(context);
     * Intent launchIntent = LauncherUtils.getLaunchIntent(context, title, url, sel); // Notify and optionally start activity if
     * (settings.getBoolean("launchBrowserOrMaps", true) && launchIntent != null) { try { context.startActivity(launchIntent);
     * LauncherUtils.playNotificationSound(context); } catch (ActivityNotFoundException e) { return; } } else {
     * LauncherUtils.generateNotification(context, url, title, launchIntent); } // Record history (for link/maps only) if
     * (launchIntent != null && launchIntent.getAction().equals(Intent.ACTION_VIEW)) {
     * HistoryDatabase.get(context).insertHistory(title, url); } } } }
     */
}
