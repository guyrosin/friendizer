package com.teamagly.friendizer.activities;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.Utility;

public class ConnectionsFragment extends AbstractFriendsListFragment {
	@SuppressWarnings("unused")
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
		final List<User> ownsList = Utility.getInstance().userInfo.getOwnsList();
		usersList.addAll(ownsList);
		if (ownsList.size() == 0)
			empty.setVisibility(View.VISIBLE);
		else
			empty.setVisibility(View.GONE);
		activity.setSupportProgressBarIndeterminateVisibility(false);
	}
}
