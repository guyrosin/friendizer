/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamagly.friendizer.utils;

import android.content.Context;

public class SessionStore {

	private static final String ID_KEY = "facebook_id";

	public static final String PREFERENCE_NAME = "friendizer_preferences"; // Preferences file name
	public static final String SECURE_PREFS_KEY = "friendizer_key"; // Secure key for the preferences

	public static void saveID(long userID, Context context) {
		SecurePreferences preferences = new SecurePreferences(context, PREFERENCE_NAME, SECURE_PREFS_KEY, true);
		preferences.put(ID_KEY, String.valueOf(userID));
	}

	public static long restoreID(Context context) {
		SecurePreferences preferences = new SecurePreferences(context, PREFERENCE_NAME, SECURE_PREFS_KEY, true);
		String userID = preferences.getString(ID_KEY);
		if (userID == null || userID.length() == 0)
			return 0;
		return Long.parseLong(userID);
	}
}
