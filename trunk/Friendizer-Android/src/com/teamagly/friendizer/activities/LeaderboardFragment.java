package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.LeaderboardListAdapter;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;

public class LeaderboardFragment extends SherlockFragment implements OnNavigationListener, OnItemClickListener {
	private final String TAG = getClass().getName();
	protected LeaderboardTask task;
	private String[] types;
	private String selectedType;
	SherlockFragmentActivity activity;
	LeaderboardListAdapter adapter;
	protected ListView listView;
	protected List<User> usersList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		types = new String[] { "points", "money" };
		usersList = new ArrayList<User>();
		selectedType = types[0];
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getSherlockActivity();

		listView = (ListView) activity.findViewById(R.id.leaderboard_list);

		ActionBar actionBar = activity.getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);

		Context context = actionBar.getThemedContext();
		ArrayAdapter<CharSequence> navListAdapter = ArrayAdapter.createFromResource(context, R.array.leaderboard_types_titles, com.actionbarsherlock.R.layout.sherlock_spinner_item);
		navListAdapter.setDropDownViewResource(com.actionbarsherlock.R.layout.sherlock_spinner_dropdown_item);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(navListAdapter, this);

		adapter = new LeaderboardListAdapter(activity, 0, usersList, types[0]);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (!selectedType.equals(types[itemPosition])) {
			selectedType = types[itemPosition];
			adapter.setLeaderboardType(selectedType);
			onResume();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.leaderboard_layout, container, false);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		activity.setSupportProgressBarIndeterminateVisibility(true);
		requestLeaderboard();
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		task.cancel(true);
		ImageLoader.getInstance().stop(); // Stop loading the images
	}

	protected void requestLeaderboard() {
		task = new LeaderboardTask();
		task.execute(selectedType);
	}

	class LeaderboardTask extends AsyncTask<String, Void, List<User>> {
		@Override
		protected List<User> doInBackground(String... types) {
			try {
				return ServerFacade.getLeaderboard(types[0]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			return new ArrayList<User>();
		}

		@Override
		protected void onPostExecute(List<User> users) {
			if (isCancelled())
				return;
			adapter.clear();
			adapter.addAll(users);
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		User userInfo = adapter.getItem(position);
		// Create an intent with the friend's data
		Intent intent = new Intent().setClass(activity, FriendProfileActivity.class);
		intent.putExtra("user", userInfo);
		startActivity(intent);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			onResume();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
