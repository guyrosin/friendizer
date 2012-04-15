package com.teamagly.friendizer.activities;

import org.json.JSONArray;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.User.FBQueryType;
import com.teamagly.friendizer.utils.Utility;

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
    }

    /**
     * Clears the current users list and request the information from Facebook
     */
    @Override
    protected void requestFriends() {
	LinearLayout empty = (LinearLayout) findViewById(R.id.empty);
	usersList.clear();
	final FriendizerUser[] ownsList = Utility.getInstance().userInfo.getOwnsList();
	if (ownsList.length == 0) {
	    showLoadingIcon(false);
	    empty.setVisibility(View.VISIBLE);
	} else {
	    empty.setVisibility(View.GONE);
	    new Thread(new Runnable() {
		@Override
		public void run() {
		    // Build a comma separated string of all the users' IDs
		    StringBuilder IDsBuilder = new StringBuilder();
		    for (int i = 0; i < ownsList.length - 1; i++)
			IDsBuilder.append(ownsList[i].getId() + ",");
		    IDsBuilder.append(ownsList[ownsList.length - 1].getId());
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
			    usersList.add(new User(ownsList[i], new FacebookUser(jsonArray.getJSONObject(i), FBQueryType.FQL)));
			}
		    } catch (Exception e) {
			Log.e(TAG, e.getMessage());
		    } finally {
			runOnUiThread(new Runnable() {
			    @Override
			    public void run() {
				friendsAdapter.notifyDataSetChanged(); // Notify the adapter
				showLoadingIcon(false);
			    }
			});
		    }
		}
	    }).start();
	}
    }
}
