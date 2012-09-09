package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.PageImageAdapter;
import com.teamagly.friendizer.model.Page;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class MutualLikesFragment extends SherlockFragment implements OnItemClickListener {
	private final String TAG = getClass().getName();
	protected MutualLikesTask task = new MutualLikesTask();
	SherlockFragmentActivity activity;
	PageImageAdapter adapter;
	protected GridView gridView;
	protected List<Page> pagesList;
	protected User user;
	int savedPosition = -1;

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.activities.AbstractFriendsListFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getSherlockActivity();
		TextView empty = (TextView) activity.findViewById(R.id.empty);
		empty.setText("No mutual likes");
		gridView = (GridView) activity.findViewById(R.id.gridview);
		Bundle args = getArguments();
		if (args != null)
			user = (User) args.getSerializable("user");
		pagesList = new ArrayList<Page>();
		adapter = new PageImageAdapter(activity, 0, pagesList);
		adapter.setNotifyOnChange(true);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setTitle(user.getName());
		actionBar.setSubtitle("Mutual Likes");

		// Restore scroll position
		//		if (savedInstanceState != null)
		//			savedPosition = savedInstanceState.getInt("savedPosition");
		// int savedListTop = savedInstanceState.getInt("savedListTop");
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
		//		if (savedPosition >= 0) // initialized to -1
		//			gridView.setSelection(savedPosition);
		requestMutualLikes();
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

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Save scroll position
		int savedPosition = gridView.getFirstVisiblePosition();
		View firstVisibleView = gridView.getChildAt(0);
		int savedListTop = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();
		outState.putInt("savedListTop", savedListTop);
		outState.putInt("savedPosition", savedPosition);
	}

	protected void requestMutualLikes() {
		pagesList.clear();

		task = new MutualLikesTask();
		task.execute(user.getId());
	}

	class MutualLikesTask extends AsyncTask<Long, Void, List<Page>> {

		@Override
		protected List<Page> doInBackground(Long... userIDs) {
			try {
				return ServerFacade.mutualLikes(Utility.getInstance().userInfo.getId(), userIDs[0]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			return new ArrayList<Page>();
		}

		@Override
		protected void onPostExecute(final List<Page> pages) {
			if (isCancelled())
				return;
			TextView empty = (TextView) activity.findViewById(R.id.empty);
			if (pages.size() == 0)
				empty.setVisibility(View.VISIBLE);
			else {
				pagesList.addAll(pages);
				empty.setVisibility(View.GONE);
			}
			adapter.notifyDataSetChanged();
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Page page = pagesList.get(position);
		// Show a dialog with info
		Dialog dialog = new Dialog(activity);

		dialog.setContentView(R.layout.page_info_dialog);
		ImageView image = (ImageView) dialog.findViewById(R.id.page_pic);
		ImageLoader.getInstance().displayImage(page.getPicURL(), image);
		dialog.setTitle(page.getName());
		TextView giftValue = (TextView) dialog.findViewById(R.id.page_type);
		giftValue.setText(String.valueOf(page.getType()));
		dialog.show();
	}
}
