package com.teamagly.friendizer.utils;

import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.teamagly.friendizer.FriendizerApp;
import com.teamagly.friendizer.R;

/**
 * Utility methods for getting the base URL for client-server communication and retrieving shared preferences.
 */
public class Util {

	/**
	 * Key for user ID in shared preferences.
	 */
	public static final String USER_ID = "userID";

	/**
	 * Key for connection status in shared preferences.
	 */
	public static final String REGISTRATION_STATUS = "registrationStatus";

	/**
	 * Value for {@link #CONNECTION_STATUS} key.
	 */
	public static final String STATUS_SUCCESS = "success";

	/**
	 * Value for {@link #CONNECTION_STATUS} key.
	 */
	public static final String STATUS_ERROR = "error";

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
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();

		// Notification.Builder builder=new Notification.Builder(context);
		// builder.
		Notification notification = new Notification(icon, message, when);
		notification.setLatestEventInfo(context, "friendizer", message,
				PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		SharedPreferences settings = Util.getSharedPreferences();
		int notificatonID = settings.getInt("notificationID", 0);

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(notificatonID, notification);

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("notificationID", ++notificatonID % 32);
		editor.commit();
	}

	/**
	 * Helper method to get a SharedPreferences instance.
	 */
	public static SharedPreferences getSharedPreferences() {
		return FriendizerApp.getContext().getSharedPreferences(SHARED_PREFS, 0);
	}

	/**
	 * Returns the package name of this class.
	 */
	private static String getPackageName() {
		return Util.class.getPackage().getName();
	}
}
