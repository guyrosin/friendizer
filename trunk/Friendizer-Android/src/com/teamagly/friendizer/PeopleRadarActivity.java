package com.teamagly.friendizer;

import java.util.Collections;

import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.R;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PeopleRadarActivity extends AbstractFriendsListActivity {
    private final String TAG = getClass().getName();

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.connections_layout);
	gridView = (GridView) findViewById(R.id.gridview);
	TextView empty = (TextView) findViewById(R.id.forever_alone_text);
	empty.setText("Forever Alone! (no people nearby)");
	updateListType(list_type);
	requestFriends();
    }

    /**
     * Clears the current users list and request the information from Facebook
     */
    @Override
    protected void requestFriends() {
	LinearLayout empty = (LinearLayout) findViewById(R.id.empty);
	usersList.clear();
	try {
	    long[] nearbyUsers = ServerFacade.nearbyUsers(Utility.getInstance().userInfo.id, Utility.DEFAULT_DISTANCE);
	    if (nearbyUsers.length == 0)
		empty.setVisibility(View.VISIBLE);
	    else
		empty.setVisibility(View.GONE);
	    for (long fbid : nearbyUsers) { // Request the details of each nearby user
		Bundle params = new Bundle();
		params.putString("fields", "name, picture, birthday, gender");
		Utility.getInstance().mAsyncRunner.request(String.valueOf(fbid), params, new UserRequestListener(this));
	    }
	} catch (Exception e) {
	    empty.setVisibility(View.VISIBLE);
	    showToast("An error occured");
	    // Log.e(TAG, "", e);
	}
	dialog.dismiss();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
	// Create an intent with the dude's data
	Intent intent = new Intent().setClass(PeopleRadarActivity.this, FriendProfileActivity.class);
	UserInfo userInfo = usersList.get(position);
	intent.putExtra("user", userInfo);
	startActivity(intent);
    }

    /**
     * Shows a toast
     * 
     * @param msg
     *            a message to show
     */
    public void showToast(final String msg) {
	new Handler(Looper.getMainLooper()).post(new Runnable() {
	    @Override
	    public void run() {
		Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG);
		toast.show();
	    }
	});
    }

    /*
     * Callback for fetching a user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {
	PeopleRadarActivity curActivity;

	public UserRequestListener(PeopleRadarActivity curActivity) {
	    this.curActivity = curActivity;
	}

	@Override
	public void onComplete(final String response, final Object state) {
	    try {
		JSONObject jsonObject = new JSONObject(response);
		UserInfo userInfo = new UserInfo(jsonObject);
		usersList.add(userInfo);
		// Sort the list alphabetically
		Collections.sort(usersList, (new Comparators()).new AlphabetComparator());
		runOnUiThread(new Runnable() {
		    public void run() {
			friendsAdapter.notifyDataSetChanged(); // Notify the adapter (must do from the main thread)
		    }
		});
	    } catch (JSONException e) {
		Log.e(TAG, "", e);
	    }
	}
    }
}
