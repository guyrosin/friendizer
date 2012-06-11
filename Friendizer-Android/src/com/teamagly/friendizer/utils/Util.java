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
package com.teamagly.friendizer.utils;

import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.teamagly.friendizer.R;

/**
 * Utility methods for getting the base URL for client-server communication and retrieving shared preferences.
 */
public class Util {

    /**
     * Tag for logging.
     */
    private static final String TAG = "Util";

    /**
     * Key for account name in shared preferences.
     */
    public static final String ACCOUNT_NAME = "accountName";

    /**
     * Key for user ID in shared preferences.
     */
    public static final String USER_ID = "userID";

    /**
     * Key for auth cookie name in shared preferences.
     */
    public static final String AUTH_COOKIE = "authCookie";

    /**
     * Key for connection status in shared preferences.
     */
    public static final String CONNECTION_STATUS = "connectionStatus";

    /**
     * Value for {@link #CONNECTION_STATUS} key.
     */
    public static final String CONNECTED = "connected";

    /**
     * Value for {@link #CONNECTION_STATUS} key.
     */
    public static final String CONNECTING = "connecting";

    /**
     * Value for {@link #CONNECTION_STATUS} key.
     */
    public static final String DISCONNECTED = "disconnected";

    /**
     * Key for device registration id in shared preferences.
     */
    public static final String DEVICE_REGISTRATION_ID = "deviceRegistrationID";

    /**
     * An intent name for receiving registration/unregistration status.
     */
    public static final String UPDATE_UI_INTENT = getPackageName() + ".UPDATE_UI";

    public static final String AUTH_PERMISSION_ACTION = getPackageName() + ".AUTH_PERMISSION";

    /**
     * Key for shared preferences.
     */
    private static final String SHARED_PREFS = "friendizer".toUpperCase(Locale.ENGLISH) + "_PREFS";

    /**
     * Display a notification containing the given string.
     */
    public static void generateNotification(Context context, String message, Intent intent) {
	int icon = R.drawable.status_icon;
	long when = System.currentTimeMillis();

	// Notification.Builder builder=new Notification.Builder(context);
	// builder.
	Notification notification = new Notification(icon, message, when);
	notification.setLatestEventInfo(context, "friendizer", message,
		PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
	notification.flags |= Notification.FLAG_AUTO_CANCEL;

	SharedPreferences settings = Util.getSharedPreferences(context);
	int notificatonID = settings.getInt("notificationID", 0);

	NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	nm.notify(notificatonID, notification);

	SharedPreferences.Editor editor = settings.edit();
	editor.putInt("notificationID", ++notificatonID % 32);
	editor.commit();
    }

    /**
     * Returns the (debug or production) URL associated with the registration service.
     */
    public static String getBaseUrl() {
	// String url = "http://10.0.2.2:8888"; // DEBUG URL
	String url = Setup.PROD_URL; // PRODUCTION URL
	return url;
    }

    /**
     * Helper method to get a SharedPreferences instance.
     */
    public static SharedPreferences getSharedPreferences(Context context) {
	return context.getSharedPreferences(SHARED_PREFS, 0);
    }

    /**
     * Returns true if we are running against a dev mode appengine instance.
     */
    public static boolean isDebug(Context context) {
	return !Setup.PROD_URL.equals(getBaseUrl());
    }

    /**
     * Returns the package name of this class.
     */
    private static String getPackageName() {
	return Util.class.getPackage().getName();
    }
}
