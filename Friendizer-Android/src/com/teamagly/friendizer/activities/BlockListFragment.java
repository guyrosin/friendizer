package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class BlockListFragment extends AbstractFriendsListFragment {
	private final String TAG = getClass().getName();
	protected FriendsTask task = new FriendsTask();

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ActionBar actionBar = activity.getSupportActionBar();
		actionBar.setTitle("Block List");

		TextView empty = (TextView) activity.findViewById(R.id.empty);
		empty.setText("You haven't blocked anyone");
		empty.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0); // Remove the drawable
		gridView = (GridView) activity.findViewById(R.id.gridview);
		gridView.setEmptyView(null);
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

	@Override
	public void onResume() {
		super.onResume();
		gridView.setOnItemClickListener(this);
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
		task = new FriendsTask();
		task.execute(Utility.getInstance().userInfo.getId());
	}

	class FriendsTask extends AsyncTask<Long, Void, List<User>> {

		@Override
		protected List<User> doInBackground(Long... userIDs) {
			try {
				return ServerFacade.blockList(userIDs[0]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			return new ArrayList<User>();
		}

		@Override
		protected void onPostExecute(final List<User> blockedUsers) {
			if (isCancelled())
				return;
			friendsAdapter.clear();
			if (blockedUsers != null)
				friendsAdapter.addAll(blockedUsers);
			gridView.setEmptyView(activity.findViewById(R.id.empty));
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
		User userInfo = friendsAdapter.getItem(position);
		// Create an intent with the friend's data
		Intent intent = new Intent().setClass(activity, FriendProfileActivity.class);
		intent.putExtra("user", userInfo);
		intent.putExtra("blocked", true);
		startActivity(intent);
	}
}
