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

import com.teamagly.friendizer.activities.ChatActivity;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.User;

/**
 * Handles incoming C2DM messages
 */
public class MessageHandler {
	private final static String TAG = "MessageHandler";

	public void displayMessage(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			try {
				JSONObject message = new JSONObject(extras.getString("message"));
				long userID = message.getLong("userID");
				User userInfo = new User();
				userInfo.setId(userID);
				String text = message.getString("text");
				String type = message.getString("type");

				if (type.equals("MSG")) { // Chat message
					// Load the user's details from Facebook
					Bundle params = new Bundle();
					params.putString("fields", "name, picture");
					Utility.getInstance().mAsyncRunner.request(String.valueOf(userID), params, new UserRequestListener(context,
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

	/*
	 * Callback for fetching user's details from Facebook
	 */
	public class UserRequestListener extends BaseRequestListener {
		String text;
		User userInfo;
		Context context;

		public UserRequestListener(Context context, User userInfo, String text) {
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

}
