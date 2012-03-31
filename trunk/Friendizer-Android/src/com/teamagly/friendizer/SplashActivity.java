package com.teamagly.friendizer;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.*;
import com.facebook.android.Facebook.DialogListener;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.SessionEvents.AuthListener;
import com.teamagly.friendizer.SessionEvents.LogoutListener;

public class SplashActivity extends Activity {
    private final String TAG = getClass().getName();
    private Handler mHandler;
    ProgressDialog dialog;
    UserRequestListener userRequestListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.splash);
	mHandler = new Handler();
	Intent intent = getIntent();
	// Listener for the login button
	final ImageView loginButton = (ImageView) findViewById(R.id.loginButton);

	// Utility.getInstance().imageLoader = new ImageLoader(this);

	loginButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		if (!Utility.getInstance().facebook.isSessionValid()) {
		    // Show a loading dialog and authorize
		    dialog = ProgressDialog.show(SplashActivity.this, "", getString(R.string.please_wait), true, true);
		    Utility.getInstance().facebook.authorize(SplashActivity.this, new String[] { "user_activities",
			    "user_checkins", "user_interests", "user_likes", "user_birthday", "friends_online_presence",
			    "friends_birthday" }, 0, new LoginDialogListener());
		}
	    }
	});

	if (intent.getBooleanExtra("logout", false)) {
	    logout();
	    return;
	}

	// Restore session if one exists
	SessionStore.restore(Utility.getInstance().facebook, this);
	SessionEvents.addAuthListener(new FBLoginListener());
	SessionEvents.addLogoutListener(new FBLogoutListener());

	if (Utility.getInstance().facebook.isSessionValid())
	    requestUserData();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();

	if (!isOnline()) {
	    ((TextView) findViewById(R.id.status)).setText("No Internet connection, please try again later");
	    return;
	}
	if (Utility.getInstance().facebook != null)
	    if (Utility.getInstance().facebook.isSessionValid())
		Utility.getInstance().facebook.extendAccessTokenIfNeeded(this, null);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
	super.onPause();
	if ((dialog != null) && (dialog.isShowing()))
	    dialog.dismiss(); // Dismiss the dialog
    }

    /**
     * Logs out the user from Facebook
     */
    private void logout() {
	try {
	    SessionEvents.onLogoutBegin();
	    AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(Utility.getInstance().facebook);
	    asyncRunner.logout(getBaseContext(), new LogoutRequestListener());
	} catch (Exception e) {
	    Log.e(TAG, "", e);
	}
    }

    /**
     * @return true iff the user is connected to the Internet
     */
    public boolean isOnline() {
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo netInfo = cm.getActiveNetworkInfo();
	if (netInfo != null && netInfo.isConnected()) {
	    return true;
	}
	return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	Utility.getInstance().facebook.authorizeCallback(requestCode, resultCode, data);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
	// Quit the app
	moveTaskToBack(true);
    }

    /*
     * Request user details from Facebook
     */
    public void requestUserData() {
	Bundle params = new Bundle();
	params.putString("fields", "name, first_name, picture, birthday, gender, inspirational_people, likes");
	// Send a new request only if there are none currently
	if ((userRequestListener == null) || (!userRequestListener.completed))
	    userRequestListener = new UserRequestListener();
	Utility.getInstance().mAsyncRunner.request("me", params, userRequestListener);
    }

    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {
	public boolean completed = false;

	@Override
	public void onComplete(final String response, final Object state) {
	    JSONObject jsonObject;
	    try {
		jsonObject = new JSONObject(response);
		final UserInfo userInfo = new UserInfo(jsonObject);
		Utility.getInstance().userInfo = userInfo;
		mHandler.post(new Runnable() {
		    @Override
		    public void run() {
			Toast.makeText(SplashActivity.this, "Welcome " + userInfo.name + "!", Toast.LENGTH_LONG).show();
		    }
		});

		// Register/login
		ServerFacade.register(userInfo.id);

		if ((dialog != null) && (dialog.isShowing()))
		    dialog.dismiss(); // Dismiss the loading dialog
		// Continue to the main activity
		Intent intent = new Intent().setClass(SplashActivity.this, FriendizerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
		startActivity(intent);
		completed = true;
		finish();

	    } catch (Exception e) {
		Log.e(TAG, "", e);
	    }
	}
    }

    /*
     * The Callback for notifying the application when authorization succeeds or fails.
     */

    public class FBLoginListener implements AuthListener {

	@Override
	public void onAuthSucceed() {
	    SessionStore.save(Utility.getInstance().facebook, SplashActivity.this);
	    requestUserData();
	}

	@Override
	public void onAuthFail(String error) {
	}
    }

    /*
     * The Callback for notifying the application when log out starts and finishes.
     */
    public class FBLogoutListener implements LogoutListener {
	@Override
	public void onLogoutBegin() {
	    SessionStore.clear(SplashActivity.this);
	}

	@Override
	public void onLogoutFinish() {
	}
    }

    private final class LoginDialogListener implements DialogListener {
	@Override
	public void onComplete(Bundle values) {
	    SessionEvents.onLoginSuccess();
	}

	@Override
	public void onFacebookError(FacebookError error) {
	    SessionEvents.onLoginError(error.getMessage());
	}

	@Override
	public void onError(DialogError error) {
	    SessionEvents.onLoginError(error.getMessage());
	}

	@Override
	public void onCancel() {
	    SessionEvents.onLoginError("Action Canceled");
	}
    }

    private class LogoutRequestListener extends BaseRequestListener {
	@Override
	public void onComplete(String response, final Object state) {
	    /*
	     * callback should be run in the original thread, not the background thread
	     */
	    mHandler.post(new Runnable() {
		@Override
		public void run() {
		    SessionEvents.onLogoutFinish();
		}
	    });
	}
    }
}
