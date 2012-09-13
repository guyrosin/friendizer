package com.teamagly.friendizer.activities;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.android.gcm.GCMRegistrar;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.teamagly.friendizer.FriendizerApp;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.SessionStore;
import com.teamagly.friendizer.utils.Utility;

/**
 * The login flow is as follows: GCM registration -> if FB user ID is saved, friendizer login. Else, FB login to acquire the user
 * ID and then friendizer login
 */
public class SplashActivity extends SherlockActivity {
	private final String TAG = getClass().getName();
	private ImageView loginButton;
	UserRequestListener userRequestListener;
	private String requestID;
	private Context context = this;
	private ProgressDialog dialogFriendizer;
	private long userID;
	private String regID;

	private void afterGCM() {
		// Get the registrationID
		regID = GCMRegistrar.getRegistrationId(context);
		if (regID == null || regID.length() == 0)
			return;

		userID = SessionStore.restoreID(context);
		Log.w(TAG, "User ID from prefs = " + userID);
		if (userID == 0)
			if (loginButton.getVisibility() == View.GONE) {
				loginButton.setVisibility(View.VISIBLE);
				return;
			}
		// Force login
		loginToFacebook();
	}

	private void continueToFriendizer() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				dialogFriendizer.dismiss(); // Dismiss the progress dialog
				String firstName = Utility.getInstance().userInfo.getFirstName();
				try {
					firstName = new String(firstName.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e1) { // Skip the encoding
				}
				Toast.makeText(context, "Welcome " + Utility.getInstance().userInfo.getFirstName() + "!", Toast.LENGTH_LONG)
				.show();
			}
		});
		// Continue to the main activity
		Intent intent = new Intent();
		SharedPreferences settings = Utility.getSharedPreferences();
		if (settings.getBoolean(Utility.PREFER_NEARBY_MAP, true))
			intent.setClass(SplashActivity.this, NearbyMapActivity.class);
		else
			intent.setClass(SplashActivity.this, FriendizerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	/**
	 * A {@link BroadcastReceiver} to receive the response from a register or unregister request, and to update the UI.
	 */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			afterGCM();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide(); // Hide the action bar
		setContentView(R.layout.splash);

		if (!isOnline()) {
			TextView txtStatus = (TextView) findViewById(R.id.status);
			txtStatus.setText("No Internet connection!");
			txtStatus.setVisibility(View.VISIBLE);
			return;
		}

		// Register a receiver to provide register/unregister notifications
		registerReceiver(mHandleMessageReceiver, new IntentFilter(Utility.REGISTRATION_INTENT));

		Intent intent = getIntent();
		// Parse any incoming Facebook notifications and save
		Uri intentUri = getIntent().getData();
		if (intentUri != null) {
			String requestIdParam = intentUri.getQueryParameter("request_ids");
			if (requestIdParam != null) {
				String array[] = requestIdParam.split(",");
				requestID = array[0];
			}
		}

		// Listener for the login button
		loginButton = (ImageView) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loginToFacebook();
			}
		});

		if (intent.getBooleanExtra("logout", false)) {
			loginButton.setVisibility(View.VISIBLE);
			logout();
			return;
		}

		dialogFriendizer = new ProgressDialog(context);
		dialogFriendizer.setMessage("Logging in, please wait...");

		// Force a locationInfo update
		LocationLibrary.forceLocationUpdate(FriendizerApp.getContext());

		dialogFriendizer.show();
		/*
		 * Login/Register to GCM
		 */
		GCMRegistrar.checkDevice(context);
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		// GCMRegistrar.checkManifest(context);
		String regID = GCMRegistrar.getRegistrationId(context);
		if (regID.equals("")) // Need to register to GCM
			GCMRegistrar.register(context, Utility.SENDER_ID);
		else
			// Continue
			afterGCM();
	}

	protected void loginToFacebook() {
		Log.w(TAG, "Logging in to Facebook");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// if (!Utility.getInstance().facebook.isSessionValid() || fbRequestFailed)
				// Authorize
				Utility.getInstance().facebook.authorize(SplashActivity.this, new String[] { "user_activities", "user_interests",
						"user_likes", "user_birthday", "user_relationships", "email" }, 0, new LoginDialogListener());
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public void onDestroy() {
		// if (friendizerLoginTask != null)
		// friendizerLoginTask.cancel(true);
		unregisterReceiver(mHandleMessageReceiver);
		GCMRegistrar.onDestroy(this);
		super.onDestroy();
	}

	protected void showErrorDialog(String errorMsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(errorMsg + ". Please restart the app")
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		})
		.show();
	}

	/**
	 * Logs out the user from Facebook
	 */
	private void logout() {
		try {
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(Utility.getInstance().facebook);
			asyncRunner.logout(getBaseContext(), null);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 * @return true iff the user is connected to the Internet
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected())
			return true;
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Utility.getInstance().facebook.authorizeCallback(requestCode, resultCode, data);
	}

	/*
	 * Request user details from Facebook
	 */
	public void requestFacebookUserID() {
		Bundle params = new Bundle();
		params.putString("fields", "id");
		userRequestListener = new UserRequestListener();
		Utility.getInstance().mAsyncRunner.request("me", params, userRequestListener);
	}

	/*
	 * Callback for fetching the current user's uid
	 */
	public class UserRequestListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);
				userID = Long.parseLong(jsonObject.getString("id"));
				Log.w(TAG, "User ID = " + userID);
				SessionStore.saveID(userID, SplashActivity.this);
				Utility.getInstance().facebook.extendAccessTokenIfNeeded(context, null);
			} catch (JSONException e) {
				Log.e(TAG, "Error when requesting FB ID: " + response);
				Log.e(TAG, e.getMessage());
				return;
			}
			// Login to friendizer (with the access token)
			User userInfo = ServerFacade.login(userID, Utility.getInstance().facebook.getAccessToken(), regID);
			if (userInfo != null) {
				Utility.getInstance().userInfo = userInfo;
				continueToFriendizer();
			} else {
				Log.e(TAG, "User is null");
				showErrorDialog("Couldn't connect to friendizer");
			}
		}
	}

	private final class LoginDialogListener implements DialogListener {
		@Override
		public void onComplete(Bundle values) {
			requestFacebookUserID();
			// Process any available request
			if (requestID != null) {
				// Just delete the request
				// Toast.makeText(getApplicationContext(), "Incoming request", Toast.LENGTH_SHORT).show();
				Bundle params = new Bundle();
				params.putString("method", "delete");
				Utility.getInstance().mAsyncRunner.request(requestID, params, new RequestIDDeleteRequestListener());
			}

		}

		@Override
		public void onFacebookError(FacebookError error) {
			Log.e(TAG, error.getMessage());
		}

		@Override
		public void onError(DialogError error) {
			Log.e(TAG, error.getMessage());
		}

		@Override
		public void onCancel() {
		}
	}

	public class RequestIDDeleteRequestListener extends BaseRequestListener {
		@Override
		public void onComplete(final String response, Object state) {
		};
	}
}
