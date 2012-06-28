package com.teamagly.friendizer.utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.teamagly.friendizer.FriendizerApp;
import com.teamagly.friendizer.activities.AchievementsActivity;
import com.teamagly.friendizer.activities.ChatActivity;
import com.teamagly.friendizer.activities.FriendizerActivity;
import com.teamagly.friendizer.activities.GiftsUserActivity;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.User;

/**
 * Handles incoming C2DM messages
 */
public class MessageHandler {
	private final static String TAG = "MessageHandler";

	public static enum NotificationType {
		CHAT,
		BUY,
		GFT,
		UPD,
		ACH;
	}

	public void handleMessage(Intent intent) {
		Context context = FriendizerApp.getContext();
		NotificationType type = NotificationType.valueOf(intent.getStringExtra("type"));
		String userIDStr = intent.getStringExtra(Util.USER_ID);
		Long userID = Long.valueOf(userIDStr);
		Log.d(TAG, "Got a message from" + userID + ", type=" + type);
		if (type == NotificationType.CHAT) { // Chat message
			String chatMsg = intent.getStringExtra("text");
			User userInfo = new User();
			userInfo.setId(userID);
			// Load the user's details from Facebook
			Bundle params = new Bundle();
			params.putString("fields", "name, picture");
			Utility.getInstance().mAsyncRunner.request(userIDStr, params, new ChatRequestListener(context, userInfo, chatMsg));
		} else if (type == NotificationType.ACH) { // Achievement earned
			String title = intent.getStringExtra("title");
			// Show a status bar notification
			Intent notificationIntent = new Intent(context, AchievementsActivity.class);
			notificationIntent.putExtra("user", Utility.getInstance().userInfo);
			Util.generateNotification(context, "You've reached an achievement: " + title, notificationIntent);
			playNotificationSound(context);
		} else if (type == NotificationType.BUY) { // Bought by someone
			User userInfo = new User();
			userInfo.setId(userID);
			// Load the user's details from Facebook
			Bundle params = new Bundle();
			params.putString("fields", "name, picture");
			Utility.getInstance().mAsyncRunner.request(userIDStr, params, new BoughtRequestListener(context, userInfo));
		} else if (type == NotificationType.GFT) { // Received a gift
			String giftName = intent.getStringExtra("giftName");
			User userInfo = new User();
			userInfo.setId(userID);
			// Load the user's details from Facebook
			Bundle params = new Bundle();
			params.putString("fields", "name");
			Utility.getInstance().mAsyncRunner.request(userIDStr, params, new GiftRequestListener(context, userInfo, giftName));
		}
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

	public class ChatRequestListener extends BaseRequestListener {
		String text;
		User userInfo;
		Context context;

		public ChatRequestListener(Context context, User userInfo, String text) {
			this.text = text;
			this.userInfo = userInfo;
			this.context = context;
		}

		@Override
		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);
				final FacebookUser newUserInfo = new FacebookUser(jsonObject);
				// Update the user's details from Facebook
				userInfo.updateFacebookData(newUserInfo);

				// Send a broadcast intent to the chat activity
				Intent broadcastIntent = new Intent(ChatActivity.ACTION_UPDATE_CHAT);
				broadcastIntent.putExtra("userID", userInfo.getId());
				broadcastIntent.putExtra("text", text);
				context.sendOrderedBroadcast(broadcastIntent, null, new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {
						int result = getResultCode();
						if (result == Activity.RESULT_CANCELED) { // Activity didn't catch it
							// Show a status bar notification
							Intent notificationIntent = new Intent(context, ChatActivity.class);
							notificationIntent.putExtra("user", userInfo);
							Util.generateNotification(context, "Message from " + userInfo.getName() + ": " + text,
									notificationIntent);
							playNotificationSound(context);
						}
					}
				}, null, Activity.RESULT_CANCELED, null, null);
			} catch (JSONException e) {
				Log.e(TAG, "", e);
			}
		}
	}

	public class GiftRequestListener extends BaseRequestListener {
		User userInfo;
		Context context;
		String giftName;

		public GiftRequestListener(Context context, User userInfo, String giftName) {
			this.userInfo = userInfo;
			this.context = context;
			this.giftName = giftName;
		}

		@Override
		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);
				final FacebookUser newUserInfo = new FacebookUser(jsonObject);
				// Update the user's details from Facebook
				userInfo.updateFacebookData(newUserInfo);

				// Show a status bar notification
				// TODO: put the gift ID in the intent...
				Intent notificationIntent = new Intent(context, GiftsUserActivity.class);
				notificationIntent.putExtra("user", Utility.getInstance().userInfo);
				Util.generateNotification(context, "Received a " + giftName + " from " + userInfo.getName(), notificationIntent);
				playNotificationSound(context);
			} catch (JSONException e) {
				Log.e(TAG, "", e);
			}
		}
	}

	public class BoughtRequestListener extends BaseRequestListener {
		User userInfo;
		Context context;

		public BoughtRequestListener(Context context, User userInfo) {
			this.userInfo = userInfo;
			this.context = context;
		}

		@Override
		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);
				final FacebookUser newUserInfo = new FacebookUser(jsonObject);
				// Update the user's details from Facebook
				userInfo.updateFacebookData(newUserInfo);

				// Show a status bar notification
				// TODO: should redirect to the buyer's profile (problem is the user object is partial)
				Intent notificationIntent = new Intent(context, FriendizerActivity.class);
				notificationIntent.putExtra("user", Utility.getInstance().userInfo);
				Util.generateNotification(context, "You've been bought by " + userInfo.getName(), notificationIntent);
				playNotificationSound(context);
			} catch (JSONException e) {
				Log.e(TAG, "", e);
			}
		}
	}

}
