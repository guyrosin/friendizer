package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class OwnsFragment extends AbstractFriendsListFragment {
	private final String TAG = getClass().getName();
	protected FriendsTask task = new FriendsTask();

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity.setContentView(R.layout.connections_layout);
		ActionBar actionBar = activity.getSupportActionBar();
		actionBar.setTitle("Own List");

		TextView empty = (TextView) activity.findViewById(R.id.empty);
		empty.setText("Forever Alone! (you don't own anyone. Go buy someone!)");
		gridView = (GridView) activity.findViewById(R.id.gridview);
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
				return ServerFacade.ownList(userIDs[0]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			return new ArrayList<User>();
		}

		@Override
		protected void onPostExecute(final List<User> friends) {
			if (isCancelled())
				return;
			friendsAdapter.clear();
			if (friends != null)
				friendsAdapter.addAll(friends);
			if (friends == null || friends.isEmpty())
				gridView.setEmptyView(activity.findViewById(R.id.empty));
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
	}
}
