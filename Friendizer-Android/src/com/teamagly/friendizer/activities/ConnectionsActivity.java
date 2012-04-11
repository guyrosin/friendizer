package com.teamagly.friendizer.activities;

import org.json.JSONArray;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.UserInfo;
import com.teamagly.friendizer.model.UserInfo.FBQueryType;
import com.teamagly.friendizer.utils.*;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class ConnectionsActivity extends AbstractFriendsListActivity {
	private final String TAG = getClass().getName();

	/*
	 * (non-Javadoc)
	 * 
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
		usersList.clear();
		final long[] ownsList = Utility.getInstance().userInfo.ownsList;
		LinearLayout empty = (LinearLayout) findViewById(R.id.empty);
		if (ownsList.length == 0) {
			showLoadingIcon(false);
			empty.setVisibility(View.VISIBLE);
		} else {
			empty.setVisibility(View.GONE);
			// TODO: this can be done faster (?) if we request data from
			// Facebook and Friendizer at the same time, in separate threads, using a concurrent map:
			// ConcurrentHashMap<Long, UserInfo> usersMap = new ConcurrentHashMap<Long, UserInfo>();
			new Thread(new Runnable() {
				@Override
				public void run() {
					// Build a comma separated string of all the users' IDs
					StringBuilder IDsBuilder = new StringBuilder();
					for (int i = 0; i < ownsList.length - 1; i++)
						IDsBuilder.append(ownsList[i] + ",");
					IDsBuilder.append(ownsList[ownsList.length - 1]);
					Bundle params = new Bundle();
					try {
						// Request the details of each user I own
						String query = "SELECT name, uid, pic_square, sex, birthday_date from user where uid in (" + IDsBuilder.toString() + ") order by name";
						params.putString("method", "fql.query");
						params.putString("query", query);
						String response = Utility.getInstance().facebook.request(params);
						JSONArray jsonArray = new JSONArray(response);
						int len = jsonArray.length();
						for (int i = 0; i < len; i++) {
							UserInfo userInfo = new UserInfo(jsonArray.getJSONObject(i), FBQueryType.FQL);
							usersList.add(userInfo);
							userInfo.updateFriendizerData(ServerFacade.userDetails(userInfo.id));
							handler.post(new Runnable() {
								@Override
								public void run() {
									friendsAdapter.notifyDataSetChanged(); // Notify the adapter
								}
							});
						}
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					} finally {
						handler.post(new Runnable() {
							@Override
							public void run() {
								showLoadingIcon(false);
							}
						});
					}
				}
			}).start();
		}
	}
}
