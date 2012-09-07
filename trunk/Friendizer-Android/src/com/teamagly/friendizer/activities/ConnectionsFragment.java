package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
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
		usersList.clear();
		friendsAdapter.notifyDataSetChanged(); // Notify the adapter
		class NearbyUsersTask extends AsyncTask<Long, Void, List<User>> {

			protected List<User> doInBackground(Long... userIDs) {
				try {
					return ServerFacade.ownList(userIDs[0]);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				return new ArrayList<User>();
			}

			protected void onPostExecute(final List<User> friends) {
				TextView empty = (TextView) activity.findViewById(R.id.empty);
				if (friends.size() == 0) {
					empty.setVisibility(View.VISIBLE);
				} else {
					usersList.addAll(friends);
					empty.setVisibility(View.GONE);
					friendsAdapter.notifyDataSetChanged(); // Notify the adapter
				}
				activity.setSupportProgressBarIndeterminateVisibility(false);
			}
		}

		new NearbyUsersTask().execute(Utility.getInstance().userInfo.getId());
	}

}
