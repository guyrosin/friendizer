package com.teamagly.friendizer.activities;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.model.User.FBQueryType;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class FBFriendsFragment extends AbstractFriendsListFragment {
	private final String TAG = getClass().getName();
	protected static JSONArray jsonArray;

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity.setContentView(R.layout.friends_layout);

		TextView empty = (TextView) activity.findViewById(R.id.empty);
		empty.setText("Forever Alone! (you have no Facebook friends)");
		gridView = (GridView) activity.findViewById(R.id.gridview);
		updateListType(list_type);
	}

	/**
	 * Clears the current users list and request the information from Facebook
	 */
	protected void requestFriends() {
		Bundle params = new Bundle();
		// Query for the friends who are using Friendizer, ordered by uid
		// Note: must order by uid (same as ownList servlet) so the next for loop will work!
		String query = "select name, uid, pic_square, sex, birthday_date, is_app_user from user where uid in (select uid2 from friend where uid1=me()) and is_app_user=1 order by uid";
		params.putString("method", "fql.query");
		params.putString("query", query);
		Utility.getInstance().mAsyncRunner.request(null, params, new FriendsRequestListener());
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
		User userInfo = usersList.get(position);
		// Create an intent with the friend's data
		Intent intent = new Intent().setClass(activity, FriendProfileActivity.class);
		intent.putExtra("user", userInfo);
		startActivity(intent);
	}

	/*
	 * Callback for fetching current user's name, picture, uid.
	 */
	public class FriendsRequestListener extends BaseRequestListener {
		FBFriendsFragment curActivity;

		@Override
		public void onComplete(final String response, final Object state) {
			try {
				jsonArray = new JSONArray(response);
			} catch (JSONException e) {
				Log.e(TAG, "", e);
				return;
			}
			usersList.clear();
			final TextView empty = (TextView) activity.findViewById(R.id.empty);
			final int len = jsonArray.length();
			handler.post(new Runnable() {
				public void run() {
					if (len == 0)
						empty.setVisibility(View.VISIBLE);
					else
						empty.setVisibility(View.GONE);
				}
			});

			for (int i = 0; i < len; i++) {
				User userInfo = null;
				try {
					userInfo = new User(new FacebookUser(jsonArray.getJSONObject(i), FBQueryType.FQL));
					usersList.add(userInfo);
					FriendizerUser fzUser = ServerFacade.userDetails(userInfo.getId());
					if (fzUser != null) {
						userInfo.updateFriendizerData(fzUser);
						handler.post(new Runnable() {
							public void run() {
								friendsAdapter.notifyDataSetChanged(); // Notify the adapter
							}
						});
					} else
						usersList.remove(userInfo);
				} catch (Exception e) {
					usersList.remove(userInfo);
					Log.w(TAG, "", e);
				}
			}
			handler.post(new Runnable() {
				public void run() {
					activity.setSupportProgressBarIndeterminateVisibility(false); // Done loading the data (roughly...)
				}
			});
		}
	}
}
