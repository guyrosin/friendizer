/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
		MSG,
		BUY,
		GFT,
		UPD,
		ACH;
	}

	public void displayMessage(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			try {
				JSONObject message = new JSONObject(extras.getString("message"));
				long userID = message.getLong("userID");
				String text = message.optString("text");
				NotificationType type = NotificationType.valueOf(message.getString("type"));
				Log.d(TAG, "Got C2DM from" + userID + ", type=" + type);
				if (type == NotificationType.MSG) { // Chat message
					User userInfo = new User();
					userInfo.setId(userID);
					// Load the user's details from Facebook
					Bundle params = new Bundle();
					params.putString("fields", "name, picture");
					Utility.getInstance().mAsyncRunner.request(String.valueOf(userID), params, new ChatRequestListener(context,
							userInfo, text));
				} else if (type == NotificationType.ACH) { // Achievement earned
					String title = message.optString("title");
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
					Utility.getInstance().mAsyncRunner.request(String.valueOf(userID), params, new BoughtRequestListener(context,
							userInfo, text));
				} else if (type == NotificationType.GFT) { // Received a gift
					User userInfo = new User();
					userInfo.setId(userID);
					// Load the user's details from Facebook
					Bundle params = new Bundle();
					params.putString("fields", "name");
					Utility.getInstance().mAsyncRunner.request(String.valueOf(userID), params, new GiftRequestListener(context,
							userInfo, text));
				}
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
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
		String text;
		User userInfo;
		Context context;

		public GiftRequestListener(Context context, User userInfo, String text) {
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

				// Show a status bar notification
				// TODO: put the gift ID in the intent...
				Intent notificationIntent = new Intent(context, GiftsUserActivity.class);
				notificationIntent.putExtra("user", Utility.getInstance().userInfo);
				// Util.generateNotification(context, "Received a gift from " + userInfo.getName(), notificationIntent); TODO
				Util.generateNotification(context, "Received a new gift", notificationIntent);
				playNotificationSound(context);
			} catch (JSONException e) {
				Log.e(TAG, "", e);
			}
		}
	}

	public class BoughtRequestListener extends BaseRequestListener {
		String text;
		User userInfo;
		Context context;

		public BoughtRequestListener(Context context, User userInfo, String text) {
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
