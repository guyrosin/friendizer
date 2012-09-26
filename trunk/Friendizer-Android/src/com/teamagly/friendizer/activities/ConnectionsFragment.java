package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class ConnectionsFragment extends AbstractFriendsListFragment {
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
		TextView empty = (TextView) activity.findViewById(R.id.empty);
		empty.setText("Forever Alone! (you have no connections)");
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
				return ServerFacade.getFriends(userIDs[0]);
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
			if (friends != null) {
				for (User friend : friends)
					friendsAdapter.add(friend);
			}
			if (friends == null || friends.isEmpty())
				gridView.setEmptyView(activity.findViewById(R.id.empty));
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onCreateOptionsMenu(com.actionbarsherlock.view.Menu,
	 * com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.friends_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_facebook_friends: // Move to my Facebook friends fragment
			startActivity(new Intent(activity, BaseFragmentActivity.class)
			.putExtra("fragment", FBFriendsFragment.class.getName()));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
