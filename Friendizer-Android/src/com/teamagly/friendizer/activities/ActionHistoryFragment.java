package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.ActionsAdapter;
import com.teamagly.friendizer.model.Action;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class ActionHistoryFragment extends SherlockFragment {
	private final String TAG = getClass().getName();
	protected ActionHistoryTask task = new ActionHistoryTask();
	SherlockFragmentActivity activity;
	ActionsAdapter adapter;
	protected GridView gridView;
	protected List<Action> ActionsList;
	protected User user;
	int savedPosition = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		ActionsList = new ArrayList<Action>();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getSherlockActivity();

		TextView empty = (TextView) activity.findViewById(R.id.empty);
		empty.setText("No history");
		gridView = (GridView) activity.findViewById(R.id.gridview);
		user = Utility.getInstance().userInfo;

		ActionBar actionBar = activity.getSupportActionBar();
		actionBar.setTitle(user.getName());
		actionBar.setSubtitle("Mutual Likes");

		adapter = new ActionsAdapter(activity, 0, ActionsList);
		gridView.setAdapter(adapter);
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
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		activity.setSupportProgressBarIndeterminateVisibility(true);
		requestHistory();
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

	protected void requestHistory() {
		task = new ActionHistoryTask();
		task.execute(user.getId());
	}

	class ActionHistoryTask extends AsyncTask<Long, Void, List<Action>> {

		@Override
		protected List<Action> doInBackground(Long... userIDs) {
			try {
				return ServerFacade.actionHistory(userIDs[0]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			return new ArrayList<Action>();
		}

		@Override
		protected void onPostExecute(final List<Action> Actions) {
			if (isCancelled())
				return;
			adapter.clear();
			if (Actions != null) {
				adapter.addAll(Actions);
				adapter.notifyDataSetChanged();
			}
			gridView.setEmptyView(activity.findViewById(R.id.empty));
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
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
