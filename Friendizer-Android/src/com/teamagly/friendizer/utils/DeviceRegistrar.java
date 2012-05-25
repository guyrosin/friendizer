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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

/**
 * Register/unregister with friendizer server
 */
public class DeviceRegistrar {
    private static final String TAG = "DeviceRegistrar";

    public static final int REGISTERED_STATUS = 1;
    public static final int AUTH_ERROR_STATUS = 2;
    public static final int UNREGISTERED_STATUS = 3;
    public static final int ERROR_STATUS = 4;

    private static final String REGISTER_PATH = "/register";
    private static final String UNREGISTER_PATH = "/unregister";

    public static void registerWithServer(final Context context, final String deviceRegistrationID) {

	new Thread(new Runnable() {
	    @Override
	    public void run() {
		Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);
		try {
		    HttpResponse res = makeRequest(context, deviceRegistrationID, REGISTER_PATH);
		    if (res.getStatusLine().getStatusCode() == 200) {
			SharedPreferences settings = Util.getSharedPreferences(context);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(Util.DEVICE_REGISTRATION_ID, deviceRegistrationID);
			editor.commit();
			updateUIIntent.putExtra(Util.CONNECTION_STATUS, REGISTERED_STATUS);
		    } else if (res.getStatusLine().getStatusCode() == 400) {
			updateUIIntent.putExtra(Util.CONNECTION_STATUS, AUTH_ERROR_STATUS);
		    } else {
			Log.w(TAG, "Registration error " + String.valueOf(res.getStatusLine().getStatusCode()));
			updateUIIntent.putExtra(Util.CONNECTION_STATUS, ERROR_STATUS);
		    }
		    final SharedPreferences prefs = Util.getSharedPreferences(context);
		    updateUIIntent.putExtra(Util.ACCOUNT_NAME, prefs.getString(Util.ACCOUNT_NAME, "Unknown"));
		    context.sendBroadcast(updateUIIntent);
		} catch (AppEngineClient.PendingAuthException pae) {
		    // Get setup activity to ask permission from user.
		    Intent authIntent = (Intent) pae.getAccountManagerBundle().get(AccountManager.KEY_INTENT);
		    if (authIntent != null) {
			context.startActivity(authIntent);
		    }
		} catch (Exception e) {
		    Log.w(TAG, "Registration error " + e.getMessage());
		    updateUIIntent.putExtra(Util.CONNECTION_STATUS, ERROR_STATUS);
		    context.sendBroadcast(updateUIIntent);
		}
	    }
	}).start();
    }

    public static void unregisterWithServer(final Context context, final String deviceRegistrationID) {
	new Thread(new Runnable() {
	    public void run() {
		Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);
		try {
		    HttpResponse res = makeRequest(context, deviceRegistrationID, UNREGISTER_PATH);
		    if (res.getStatusLine().getStatusCode() != 200) {
			Log.w(TAG, "Unregistration error " + String.valueOf(res.getStatusLine().getStatusCode()));
		    }
		} catch (Exception e) {
		    Log.w(TAG, "Unregistration error " + e.getMessage());
		} finally {
		    SharedPreferences settings = Util.getSharedPreferences(context);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.remove(Util.DEVICE_REGISTRATION_ID);
		    editor.remove(Util.ACCOUNT_NAME);
		    editor.commit();
		    updateUIIntent.putExtra(Util.CONNECTION_STATUS, UNREGISTERED_STATUS);
		}

		// Update dialog activity
		context.sendBroadcast(updateUIIntent);
	    }
	}).start();
    }

    private static HttpResponse makeRequest(Context context, String deviceRegistrationID, String urlPath) throws Exception {
	SharedPreferences settings = Util.getSharedPreferences(context);
	String accountName = settings.getString(Util.ACCOUNT_NAME, null);

	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair(Util.DEVICE_REGISTRATION_ID, deviceRegistrationID));

	String deviceID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	if (deviceID != null) {
	    params.add(new BasicNameValuePair("deviceID", deviceID));
	}

	AppEngineClient client = new AppEngineClient(context, accountName);
	return client.makeRequest(urlPath, params);
    }

}
