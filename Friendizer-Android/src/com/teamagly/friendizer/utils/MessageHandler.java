package com.teamagly.friendizer.utils;

import java.io.IOException;
import java.net.URL;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat.Builder;
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

	private long userID;
	private NotificationType type;
	private Context context;
	private Builder builder;
	private User user;

	public enum NotificationType {
		CHAT,
		BUY,
		GFT,
		UPD,
		ACH,
		NEARBY
	}

	public void handleMessage(Intent intent) {
		context = FriendizerApp.getContext();
		type = NotificationType.valueOf(intent.getStringExtra("type"));
		String userIDStr = intent.getStringExtra(Utility.USER_ID);
		userID = Long.valueOf(userIDStr);
		if (type == NotificationType.CHAT) { // Chat message
			String chatMsg = intent.getStringExtra("text");
			chat(chatMsg);
		} else if (type == NotificationType.ACH) { // Achievement earned
			String title = intent.getStringExtra("title");
			// Show a status bar notification
			Intent notificationIntent = new Intent(context, AchievementsActivity.class);
			notificationIntent.putExtra("user", Utility.getInstance().userInfo);
			generateNotification("Achievement Earned", title, null, notificationIntent, "Achievement earned!");
		} else if (type == NotificationType.BUY)
			bought(context, userID);
		else if (type == NotificationType.GFT) { // Received a gift
			String giftName = intent.getStringExtra("giftName");
			gift(giftName);
		} else if (type == NotificationType.NEARBY) { // There's a nearby friend
			String text = intent.getStringExtra("text");
			nearby(text);
		}
	}

	/**
	 * Display a notification containing the given string.
	 */
	public void generateNotification(String title, String message, String picURL, Intent intent, String tickerText) {
		final long when = System.currentTimeMillis();
		builder = new Builder(context);
		builder.setContentTitle(title);
		builder.setContentText(message);
		builder.setSmallIcon(APP_ICON_RES_ID);
		builder.setTicker(tickerText);
		int requestCode = 0;
		if (type == NotificationType.CHAT) {
			requestCode = Long.valueOf(userID).hashCode(); // Create a unique code for chat notifications
			builder.setContentIntent(PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT
					| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		} else
			builder.setContentIntent(PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT
					| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		builder.setWhen(when);
		builder.setAutoCancel(true);

		if (picURL != null && picURL.length() > 0)
			try {
				// Fetch the image
				URL url = new URL(picURL);
				new FetchImageTask(this).execute(url);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		else
			notify(context, builder);
	}

	static class FetchImageTask extends AsyncTask<URL, Void, Bitmap> {
		MessageHandler messageHandler;

		public FetchImageTask(MessageHandler messageHandler) {
			this.messageHandler = messageHandler;
		}

		@Override
		protected Bitmap doInBackground(URL... urls) {
			try {
				return BitmapFactory.decodeStream(urls[0].openConnection().getInputStream());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			try {
				messageHandler.builder.setLargeIcon(bitmap);
				messageHandler.notify(messageHandler.context, messageHandler.builder);
			} catch (Exception e) {
				Log.w(TAG, "", e);
			}
		}
	}

	private void notify(Context context, Builder builder) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int notificationID;
		if (type == NotificationType.CHAT) { // For chat notifications, stack all messages from one user
			notificationID = Long.valueOf(userID).hashCode(); // Convert the long ID to a unique integer
			nm.cancel(notificationID); // Cancel the old notification (because we want the new ticker to appear)
		} else { // For other notifications, just use a unique ID
			SharedPreferences settings = Utility.getSharedPreferences();
			notificationID = settings.getInt("notificationID", 0);
		}
		nm.notify(notificationID, builder.build());

		if (type != NotificationType.CHAT) {
			SharedPreferences settings = Utility.getSharedPreferences();
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("notificationID", ++notificationID % 32);
			editor.commit();
		}
		playNotificationSound();
	}

	private void playNotificationSound() {
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (uri != null) {
			Ringtone rt = RingtoneManager.getRingtone(context, uri);
			if (rt != null) {
				rt.setStreamType(AudioManager.STREAM_NOTIFICATION);
				rt.play();
			}
		}
	}

	private void chat(final String msg) {
		try {
			// Load the user's details
			user = ServerFacade.userDetails(userID);
			// Send a broadcast intent to the chat activity
			Intent broadcastIntent = new Intent(ChatActivity.ACTION_UPDATE_CHAT);
			broadcastIntent.putExtra("userID", userID);
			broadcastIntent.putExtra("text", msg);
			context.sendOrderedBroadcast(broadcastIntent, null, new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					int result = getResultCode();
					if (result == Activity.RESULT_CANCELED) { // Activity didn't catch it
						// Show a status bar notification
						Intent notificationIntent = new Intent(context, ChatActivity.class);
						notificationIntent.putExtra("user", user);
						generateNotification(user.getName(), msg, user.getPicURL(), notificationIntent, "New message from "
								+ user.getFirstName());
					}
				}
			}, null, Activity.RESULT_CANCELED, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void gift(String giftName) {
		// Load the user's details
		try {
			User user = ServerFacade.userDetails(userID);
			// Show a status bar notification
			// TODO: put the gift ID in the intent and show a cool dialog...
			Intent notificationIntent = new Intent(context, GiftsUserActivity.class);
			notificationIntent.putExtra("user", Utility.getInstance().userInfo);
			generateNotification("Received a " + giftName, "From " + user.getName(), user.getPicURL(), notificationIntent,
					"New gift from " + user.getFirstName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void nearby(String text) {
		// Load the user's details
		try {
			user = ServerFacade.userDetails(userID);
			// Show a status bar notification
			Intent notificationIntent = new Intent(context, FriendProfileActivity.class);
			notificationIntent.putExtra("user", Utility.getInstance().userInfo);
			generateNotification(user.getName(), text, user.getPicURL(), notificationIntent, user.getFirstName() + " is nearby!");
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
	}

	private void bought(Context context, long userID) {
		// Load the user's details
		try {
			user = ServerFacade.userDetails(userID);
			// Show a status bar notification
			Intent notificationIntent = new Intent(context, FriendProfileActivity.class);
			notificationIntent.putExtra("user", user);
			generateNotification("You've been bought in friendizer", "By " + user.getName(), user.getPicURL(),
					notificationIntent, user.getFirstName() + " has just bought you!");
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
	}
}
