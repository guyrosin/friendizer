package com.teamagly.friendizer.utils;

import java.io.IOException;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.teamagly.friendizer.FriendizerApp;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.activities.AchievementsActivity;
import com.teamagly.friendizer.activities.ChatActivity;
import com.teamagly.friendizer.activities.FriendProfileActivity;
import com.teamagly.friendizer.activities.GiftsUserActivity;
import com.teamagly.friendizer.model.User;

/**
 * Handles incoming GCM messages
 */
public class MessageHandler {
	private final static String TAG = "MessageHandler";
	private final static int APP_ICON_RES_ID = R.drawable.ic_launcher;

	public static enum NotificationType {
		CHAT,
		BUY,
		GFT,
		UPD,
		ACH,
		NEARBY
	}

	public void handleMessage(Intent intent) {
		Context context = FriendizerApp.getContext();
		NotificationType type = NotificationType.valueOf(intent.getStringExtra("type"));
		String userIDStr = intent.getStringExtra(Utility.USER_ID);
		Long userID = Long.valueOf(userIDStr);
		if (type == NotificationType.CHAT) { // Chat message
			String chatMsg = intent.getStringExtra("text");
			chat(context, userID, chatMsg);
		} else if (type == NotificationType.ACH) { // Achievement earned
			String title = intent.getStringExtra("title");
			// Show a status bar notification
			Intent notificationIntent = new Intent(context, AchievementsActivity.class);
			notificationIntent.putExtra("user", Utility.getInstance().userInfo);
			generateNotification(context, "Achievement Earned", title, APP_ICON_RES_ID, notificationIntent);
			playNotificationSound(context);
		} else if (type == NotificationType.BUY) { // Bought by someone
			bought(context, userID);
		} else if (type == NotificationType.GFT) { // Received a gift
			String giftName = intent.getStringExtra("giftName");
			gift(context, userID, giftName);
		} else if (type == NotificationType.NEARBY) { // There's a nearby friend
			String text = intent.getStringExtra("text");
			nearby(context, userID, text);
		}
	}

	/**
	 * Display a notification containing the given string.
	 */
	@SuppressWarnings("deprecation")
	public static void generateNotification(Context context, String title, String message, int smallIconResID, Intent intent) {
		long when = System.currentTimeMillis();

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setContentTitle(title)
				.setContentText(message).setWhen(when).setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
				.setAutoCancel(true);
		if (smallIconResID > 0)
			builder.setSmallIcon(smallIconResID);
		// if (bigIconResID >0)
		// builder.setLargeIcon(context.getResources().getDrawable(bigIconResID));

		SharedPreferences settings = Utility.getSharedPreferences();
		int notificatonID = settings.getInt("notificationID", 0);

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(notificatonID, builder.getNotification());

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("notificationID", ++notificatonID % 32);
		editor.commit();
	}

	private void playNotificationSound(Context context) {
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (uri != null) {
			Ringtone rt = RingtoneManager.getRingtone(context, uri);
			if (rt != null) {
				rt.setStreamType(AudioManager.STREAM_NOTIFICATION);
				rt.play();
			}
		}
	}

	private void chat(Context context, long userID, final String msg) {
		try {
			// Load the user's details
			final User user = ServerFacade.userDetails(userID);
			// Send a broadcast intent to the chat activity
			Intent broadcastIntent = new Intent(ChatActivity.ACTION_UPDATE_CHAT);
			broadcastIntent.putExtra("userID", user.getId());
			broadcastIntent.putExtra("text", msg);
			context.sendOrderedBroadcast(broadcastIntent, null, new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					int result = getResultCode();
					if (result == Activity.RESULT_CANCELED) { // Activity didn't catch it
						// Show a status bar notification
						Intent notificationIntent = new Intent(context, ChatActivity.class);
						notificationIntent.putExtra("user", user);
						generateNotification(context, user.getName(), msg, APP_ICON_RES_ID, notificationIntent);
						playNotificationSound(context);
					}
				}
			}, null, Activity.RESULT_CANCELED, null, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void gift(Context context, long userID, String giftName) {
		// Load the user's details
		try {
			User user = ServerFacade.userDetails(userID);
			// Show a status bar notification
			// TODO: put the gift ID in the intent...
			Intent notificationIntent = new Intent(context, GiftsUserActivity.class);
			notificationIntent.putExtra("user", Utility.getInstance().userInfo);
			generateNotification(context, "Received a " + giftName, "From " + user.getName(), APP_ICON_RES_ID, notificationIntent);
			playNotificationSound(context);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void nearby(Context context, long userID, String text) {
		// Load the user's details
		try {
			User user = ServerFacade.userDetails(userID);
			// Show a status bar notification
			Intent notificationIntent = new Intent(context, FriendProfileActivity.class);
			notificationIntent.putExtra("user", Utility.getInstance().userInfo);
			generateNotification(context, user.getName(), text, APP_ICON_RES_ID, notificationIntent);
			playNotificationSound(context);
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
	}

	private void bought(Context context, long userID) {
		// Load the user's details
		try {
			User user = ServerFacade.userDetails(userID);
			// Show a status bar notification
			Intent notificationIntent = new Intent(context, FriendProfileActivity.class);
			notificationIntent.putExtra("user", Utility.getInstance().userInfo);
			generateNotification(context, "You've been bought in friendizer", "By " + user.getName(), APP_ICON_RES_ID,
					notificationIntent);
			playNotificationSound(context);
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
	}
}
