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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		pagesList = new ArrayList<Page>();
	}

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

		ActionBar actionBar = activity.getSupportActionBar();
		actionBar.setTitle(user.getName());
		actionBar.setSubtitle("Mutual Likes");

		adapter = new PageImageAdapter(activity, 0, pagesList);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
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
		gridView.setEmptyView(null);
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

	protected void requestMutualLikes() {
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
			adapter.clear();
			if (pages != null)
				adapter.addAll(pages);
			adapter.notifyDataSetChanged();
			if (pages == null || pages.isEmpty())
				gridView.setEmptyView(activity.findViewById(R.id.empty));
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Page page = adapter.getItem(position);
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
