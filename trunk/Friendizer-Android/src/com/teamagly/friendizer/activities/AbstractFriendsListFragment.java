/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.FriendsAdapter;
import com.teamagly.friendizer.adapters.FriendsImageAdapter;
import com.teamagly.friendizer.adapters.FriendsListAdapter;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.Comparators;
import com.teamagly.friendizer.utils.Utility;

/**
 * Note: the child class has to call activity.setSupportProgressBarIndeterminateVisibility(false) after it's done reloading the
 * data in order to stop the loading indicator
 */
public abstract class AbstractFriendsListFragment extends SherlockListFragment implements OnItemClickListener {
	private final String TAG = "AbstractFriendsListFragment";
	protected boolean list_type;
	protected GridView gridView;
	protected FriendsAdapter friendsAdapter;
	protected ArrayList<User> usersList = new ArrayList<User>();
	protected int sortBy = 0;
	final Handler handler = new Handler();
	protected SherlockFragmentActivity activity;

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
		boolean type = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("friends_list_type", false);
		list_type = type;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		activity.setSupportProgressBarIndeterminateVisibility(true);
		requestFriends();
		boolean type = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("friends_list_type", false);
		if (type != list_type) { // A change occurred -> redraw the view
			list_type = type;
			updateListType(type);
		}
	}

	/**
	 * Clears the current users list and request the information from Facebook
	 */
	protected abstract void requestFriends();

	protected void updateListType(boolean type) {
		if (list_type) { // => show in a list
			gridView.setAdapter(null);
			gridView.setVisibility(View.GONE);
			getListView().setVisibility(View.VISIBLE);
			friendsAdapter = new FriendsListAdapter(activity, 0, usersList);
			getListView().setAdapter(friendsAdapter);
			getListView().setOnItemClickListener(this);
		} else { // => show in a GridView
			getListView().setAdapter(null);
			gridView.setVisibility(View.VISIBLE);
			getListView().setVisibility(View.GONE);
			friendsAdapter = new FriendsImageAdapter(activity, 0, usersList);
			gridView.setAdapter(friendsAdapter);
			gridView.setOnItemClickListener(this);
		}
	}

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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			onResume();
			return true;
		case R.id.menu_facebook_friends: // Move to my Facebook friends activity
			startActivity(new Intent(activity, FBFriendsActivity.class));
			return true;
		case R.id.menu_sort:
			final String[] options = { "Alphabeth", "Value", "Matching" };
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle("Sort by");
			builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (item != sortBy) {
						switch (item) {
						case 0:
							Collections.sort(usersList, (new Comparators()).new AlphabetComparator());
							break;
						case 1:
							Collections.sort(usersList, (new Comparators()).new ValueComparator());
							break;
						case 2:
							Collections.sort(usersList, (new Comparators()).new MatchingComparator());
							break;
						}
						sortBy = item;
						// TODO
						// friendsAdapter.notifyDataSetChanged(); // Notify the adapter
					}
					dialog.dismiss();
				}
			});
			builder.show();
			return true;
		case R.id.menu_view_by:
			// Show a dialog
			final String[] items = { "Grid", "List" };
			builder = new AlertDialog.Builder(activity);
			builder.setTitle("View by");
			int i_list_type = 0;
			if (list_type)
				i_list_type = 1;
			builder.setSingleChoiceItems(items, i_list_type, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext())
							.edit();
					boolean choice = false; // Default is grid
					if (item == 1) // The user chose list
						choice = true;
					if (list_type != choice) { // If the user changed the type, redraw the view
						list_type = choice;
						updateListType(choice);
						// Update the preference
						editor.putBoolean("friends_list_type", choice);
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
		User userInfo = usersList.get(position);
		// Create an intent with the friend's data
		Intent intent = new Intent().setClass(activity, FriendProfileActivity.class);
		intent.putExtra("user", userInfo);
		startActivity(intent);
	}
}
