package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class FBFriendsFragment extends AbstractFriendsListFragment {
	private final String TAG = getClass().getName();
	protected static JSONArray jsonArray;
	protected FriendsTask task = new FriendsTask();

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		TextView empty = (TextView) activity.findViewById(R.id.empty);
		empty.setText("Forever Alone! (None of your Facebook friends are using friendizer!)");
		gridView = (GridView) activity.findViewById(R.id.gridview);

		ActionBar actionBar = activity.getSupportActionBar();
		actionBar.setTitle("FB Friends");
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.connections_layout, container, false);
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		task.cancel(true);
	}

	/**
	 * Clears the current users list and request the information from Facebook
	 */
	@Override
	protected void requestFriends() {
		Bundle params = new Bundle();
		// Query for the friends who are using Friendizer
		String query = "select uid, is_app_user, name from user where uid in (select uid2 from friend where uid1=me()) and is_app_user=1 order by name";
		params.putString("method", "fql.query");
		params.putString("query", query);
		Utility.getInstance().mAsyncRunner.request(null, params, new FriendsRequestListener());
	}

	/*
	 * Callback for fetching current user's name, picture, uid.
	 */
	public class FriendsRequestListener extends BaseRequestListener {

		@SuppressWarnings("unchecked")
		@Override
		public void onComplete(final String response, final Object state) {
			try {
				jsonArray = new JSONArray(response);
			} catch (JSONException e) {
				Log.e(TAG, "", e);
				return;
			}
			final int len = jsonArray.length();
			List<Long> ids = new ArrayList<Long>();
			for (int i = 0; i < len; i++)
				try {
					long userID = jsonArray.getJSONObject(i).optLong("uid");
					if (userID > 0)
						ids.add(userID);
				} catch (Exception e) {
					Log.w(TAG, e.getMessage());
				}
			task = new FriendsTask();
			task.execute(ids);
		}
	}

	class FriendsTask extends AsyncTask<List<Long>, Void, Void> {

		@Override
		protected Void doInBackground(List<Long>... userIDsPass) {
			final List<Long> userIDs = userIDsPass[0];
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					friendsAdapter.clear();
					if (userIDs == null || userIDs.isEmpty())
						gridView.setEmptyView(activity.findViewById(R.id.empty));
				}
			});
			for (long userID : userIDs) {
				if (isCancelled())
					return null;
				try {
					final User user = ServerFacade.userDetails(userID);
					if (user != null)
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								friendsAdapter.add(user);
							}
						});
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			activity.setSupportProgressBarIndeterminateVisibility(false); // Done loading the data
		}
	}

}
