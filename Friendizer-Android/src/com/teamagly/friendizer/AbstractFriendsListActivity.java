/**
 * 
 */
package com.teamagly.friendizer;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Guy
 * 
 */
public abstract class AbstractFriendsListActivity extends ListActivity implements OnItemClickListener {
    private final String TAG = getClass().getName();
    ProgressDialog dialog;
    protected boolean list_type;
    protected GridView gridView;
    protected FriendsAdapter friendsAdapter;
    protected ArrayList<UserInfo> usersList = new ArrayList<UserInfo>();
    protected int sortBy = 0;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	dialog = ProgressDialog.show(this, "", getString(R.string.please_wait), true, true);
	boolean type = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("friends_list_type", false);
	list_type = type;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	boolean type = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("friends_list_type", false);
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
	    friendsAdapter = new FriendsListAdapter(this, R.layout.connection_list_item, usersList);
	    getListView().setOnItemClickListener(this);
	    getListView().setAdapter(friendsAdapter);
	} else { // == show in a GridView
	    getListView().setAdapter(null);
	    friendsAdapter = new FriendsImageAdapter(this, 0, usersList);
	    gridView.setAdapter(friendsAdapter);
	    gridView.setOnItemClickListener(this);
	}
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
	super.onPause();
	dialog.dismiss(); // Dismiss the dialog
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
	// Check for a refresh message
	if (intent.getBooleanExtra("refresh", false))
	    requestFriends();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	menu.clear(); // Clear the main activity's menu
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.connections_menu, menu);
	return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.settings: // Move to the settings activity
	    startActivity(new Intent(this, FriendsPrefs.class));
	    return true;
	case R.id.invite: // Show the Facebook invitation dialog
	    Bundle params = new Bundle();
	    params.putString("message", getString(R.string.invitation_msg));
	    Utility.getInstance().facebook.dialog(this, "apprequests", params, new BaseDialogListener());
	    return true;
	case R.id.facebook_friends: // Move to my Facebook friends activity
	    Intent intent = new Intent().setClass(this, FBFriendsActivity.class);
	    startActivity(intent);
	    return true;
	case R.id.sort:
	    final String[] options = { "Alphabeth", "Value", "Matching" };
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Sort By");
	    builder.setItems(options, new DialogInterface.OnClickListener() {
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
			runOnUiThread(new Runnable() {
			    public void run() {
				friendsAdapter.notifyDataSetChanged(); // Notify the adapter (must be done from the main thread)
			    }
			});
		    }
		}
	    });
	    AlertDialog optionsDialog = builder.create();
	    optionsDialog.show();
	    return true;
	case R.id.refresh:
	    requestFriends();
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
	UserInfo userInfo = usersList.get(position);
	// Create an intent with the friend's data
	Intent intent = new Intent().setClass(this, FriendProfileActivity.class);
	intent.putExtra("user", userInfo);
	startActivity(intent);
    }

    protected void updateUsersFromFriendizer() {
	// Load each user's details from our servers in the background
	new Thread(new Runnable() {
	    public void run() {
		for (int i = 0; i < usersList.size(); i++) {
		    try {
			usersList.get(i).updateFriendizerData(ServerFacade.userDetails(usersList.get(i).id));
		    } catch (Exception e) {
			Log.e(TAG, "", e);
		    }
		}
		runOnUiThread(new Runnable() {
		    public void run() {
			friendsAdapter.notifyDataSetChanged(); // Notify the adapter (must be done from the main thread)
		    }
		});
	    }
	}).start();
    }
}
