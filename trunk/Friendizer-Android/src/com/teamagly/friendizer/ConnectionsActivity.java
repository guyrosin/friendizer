package com.teamagly.friendizer;

import java.util.Collections;
import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.R;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConnectionsActivity extends AbstractFriendsListActivity {
    private final String TAG = getClass().getName();

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.connections_layout);
	TextView empty = (TextView) findViewById(R.id.forever_alone_text);
	empty.setText("Forever Alone! (you have no connections)");
	gridView = (GridView) findViewById(R.id.gridview);
	updateListType(list_type);

	requestFriends();
    }

    /**
     * Clears the current users list and request the information from Facebook
     */
    @Override
    protected void requestFriends() {
	usersList.clear();
	long[] ownsList = Utility.getInstance().userInfo.ownsList;
	LinearLayout empty = (LinearLayout) findViewById(R.id.empty);
	if (ownsList.length == 0)
	    empty.setVisibility(View.VISIBLE);
	else
	    empty.setVisibility(View.GONE);
	for (long fbid : ownsList) { // Request the details of each user I own
	    Bundle params = new Bundle();
	    params.putString("fields", "name, picture, birthday, gender");
	    Utility.getInstance().mAsyncRunner.request(String.valueOf(fbid), params, new UserRequestListener());
	}
    }

    protected void updateUsersFromFriendizer() {
	// Load each user's details from our servers in the background
	new Thread(new Runnable() {
	    public void run() {
		for (int i = 0; i < usersList.size(); i++) {
		    try {
			usersList.get(i).updateFriendizerData(ServerFacade.userDetails(usersList.get(i).id));
		    } catch (Exception e) {
			Log.e(TAG, "", e);
		    }
		}
		runOnUiThread(new Runnable() {
		    public void run() {
			friendsAdapter.notifyDataSetChanged(); // Notify the adapter (must be done from the main thread)
		    }
		});
	    }
	}).start();
    }

    /*
     * Callback for fetching a user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {

	@Override
	public void onComplete(final String response, final Object state) {
	    try {
		JSONObject jsonObject = new JSONObject(response);
		UserInfo userInfo = new UserInfo(jsonObject);
		usersList.add(userInfo);
		// Sort the list alphabetically
		Collections.sort(usersList, (new Comparators()).new AlphabetComparator());
		updateUsersFromFriendizer();
		runOnUiThread(new Runnable() {
		    public void run() {
			friendsAdapter.notifyDataSetChanged(); // Notify the adapter (must be done from the main thread)
		    }
		});
		dialog.dismiss(); // Dismiss the loading dialog

	    } catch (JSONException e) {
		Log.e(TAG, "", e);
	    }
	}
    }
}
