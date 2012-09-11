/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.FriendsAdapter;
import com.teamagly.friendizer.adapters.FriendsImageAdapter;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.Utility;

/**
 * Note: the child class has to call activity.setSupportProgressBarIndeterminateVisibility(false) after it's done reloading the
 * data in order to stop the loading indicator
 */
public abstract class AbstractFriendsListFragment extends SherlockFragment implements OnItemClickListener {
	protected static String SORT_BY = FriendsAdapter.SORT_BY;
	@SuppressWarnings("unused")
	private final String TAG = "AbstractFriendsListFragment";
	protected GridView gridView;
	protected FriendsAdapter friendsAdapter;
	protected ArrayList<User> usersList = new ArrayList<User>();
	protected int sortBy = 0;
	final Handler handler = new Handler();
	protected SherlockFragmentActivity activity;
	int savedPosition = -1;

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getSherlockActivity();

		SharedPreferences settings = Utility.getSharedPreferences();
		sortBy = settings.getInt(SORT_BY, 0);

		friendsAdapter = new FriendsImageAdapter(activity, 0, usersList);

		// Restore scroll position
		if (savedInstanceState != null)
			savedPosition = savedInstanceState.getInt("savedPosition");
		// int savedListTop = savedInstanceState.getInt("savedListTop");
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		activity.setSupportProgressBarIndeterminateVisibility(true);
		gridView.setAdapter(friendsAdapter);
		gridView.setOnItemClickListener(this);
		if (savedPosition >= 0) // initialized to -1
			gridView.setSelection(savedPosition);
		requestFriends();
	}

	@Override
	public void onPause() {
		super.onPause();
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

	/**
	 * Clears the current users list and request the information from Facebook
	 */
	protected abstract void requestFriends();

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockListFragment#onCreateOptionsMenu(com.actionbarsherlock.view.Menu,
	 * com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.list_menu, menu);
		inflater.inflate(R.menu.main_menu, menu);
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockListFragment#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			onResume();
			return true;
		case R.id.menu_facebook_friends: // Move to my Facebook friends fragment
			startActivity(new Intent(activity, BaseFragmentActivity.class).putExtra("fragment", FBFriendsFragment.class.getName()));
			return true;
		case R.id.menu_sort:
			final String[] options = { "Alphabeth", "Value" };
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle("Sort by");
			builder.setSingleChoiceItems(options, sortBy, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					if (item != sortBy) {
						sortBy = item;
						friendsAdapter.setSortBy(sortBy);
						// Save the new order
						SharedPreferences settings = Utility.getSharedPreferences();
						SharedPreferences.Editor editor = settings.edit();
						editor.putInt(SORT_BY, sortBy);
						editor.commit();
					}
					dialog.dismiss();
				}
			});
			builder.show();
			return true;
		case R.id.menu_settings: // Move to the settings activity
			startActivity(new Intent(activity, FriendsPrefs.class));
			return true;
		case R.id.menu_feedback:
			return Utility.startFeedback(activity);
		case R.id.menu_invite: // Show the Facebook invitation dialog
			Bundle params = new Bundle();
			params.putString("message", getString(R.string.invitation_msg));
			Utility.getInstance().facebook.dialog(activity, "apprequests", params, new BaseDialogListener());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
		User userInfo = friendsAdapter.getItem(position);
		// Create an intent with the friend's data
		Intent intent = new Intent().setClass(activity, FriendProfileActivity.class);
		intent.putExtra("user", userInfo);
		startActivity(intent);
	}
}
