package com.teamagly.friendizer.activities;

import org.json.JSONArray;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.User.FBQueryType;
import com.teamagly.friendizer.utils.Utility;

public class ConnectionsFragment extends AbstractFriendsListFragment {
	private final String TAG = getClass().getName();

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity.setContentView(R.layout.connections_layout);
		TextView empty = (TextView) activity.findViewById(R.id.empty);
		empty.setText("Forever Alone! (you have no connections)");
		gridView = (GridView) activity.findViewById(R.id.gridview);
	}

	/**
	 * Clears the current users list and request the information from Facebook
	 */
	@Override
	protected void requestFriends() {
		TextView empty = (TextView) activity.findViewById(R.id.empty);
		usersList.clear();
		friendsAdapter.notifyDataSetChanged(); // Notify the adapter
		final FriendizerUser[] ownsList = Utility.getInstance().userInfo.getOwnsList();
		if (ownsList.length == 0) {
			activity.setSupportProgressBarIndeterminateVisibility(false);
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
						// Note: must order by uid (same as ownList servlet) so the next for loop will work!
						String query = "SELECT name, uid, pic_square, sex, birthday_date from user where uid in ("
								+ IDsBuilder.toString() + ") order by uid";
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
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								friendsAdapter.notifyDataSetChanged(); // Notify the adapter
								activity.setSupportProgressBarIndeterminateVisibility(false);
							}
						});
					}
				}
			}).start();
		}
	}
}
