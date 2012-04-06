package com.teamagly.friendizer;

import java.io.IOException;
import org.json.JSONArray;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.UserInfo.FBQueryType;

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
    }

    /**
     * Clears the current users list and request the information from Facebook
     */
    @Override
    protected void requestFriends() {
	LinearLayout empty = (LinearLayout) findViewById(R.id.empty);
	usersList.clear();

	try {
	    final long[] nearbyUsers = ServerFacade.nearbyUsers(Utility.getInstance().userInfo.id, Utility.DEFAULT_DISTANCE);
	    if (nearbyUsers.length == 0) {
		showLoadingIcon(false);
		empty.setVisibility(View.VISIBLE);
	    } else {
		empty.setVisibility(View.GONE);
		new Thread(new Runnable() {
		    @Override
		    public void run() {
			// Build a comma separated string of all the users' IDs
			StringBuilder IDsBuilder = new StringBuilder();
			for (int i = 0; i < nearbyUsers.length - 1; i++)
			    IDsBuilder.append(nearbyUsers[i] + ",");
			IDsBuilder.append(nearbyUsers[nearbyUsers.length - 1]);
			Bundle params = new Bundle();
			try {
			    // Request the details of each user I own
			    String query = "SELECT name, uid, pic_square, sex, birthday_date from user where uid in ("
				    + IDsBuilder.toString() + ") order by name";
			    params.putString("method", "fql.query");
			    params.putString("query", query);
			    String response = Utility.getInstance().facebook.request(params);
			    JSONArray jsonArray = new JSONArray(response);
			    int len = jsonArray.length();
			    for (int i = 0; i < len; i++) {
				UserInfo userInfo = new UserInfo(jsonArray.getJSONObject(i), FBQueryType.FQL);
				usersList.add(userInfo);
				userInfo.updateFriendizerData(ServerFacade.userDetails(userInfo.id));
				runOnUiThread(new Runnable() {
				    public void run() {
					friendsAdapter.notifyDataSetChanged(); // Notify the adapter (must be done from the main
									       // thread)
				    }
				});
			    }
			} catch (Exception e) {
			    Log.e(TAG, e.getMessage());
			    showToast("An error occured");
			}
		    }
		}).start();
	    }
	} catch (IOException e) {
	    empty.setVisibility(View.VISIBLE);
	} finally {
	    handler.post(new Runnable() {
		public void run() {
		    showLoadingIcon(false);
		}
	    });
	}
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
}
