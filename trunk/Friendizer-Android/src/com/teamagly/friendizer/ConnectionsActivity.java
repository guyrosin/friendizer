package com.teamagly.friendizer;

import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.R;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class ConnectionsActivity extends ListActivity implements OnItemClickListener {
    ProgressDialog dialog;
    private boolean list_type;
    private GridView gridView;
    private FriendsAdapter friendsAdapter;
    private ArrayList<FBUserInfo> usersList = new ArrayList<FBUserInfo>();

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.connections_layout);
	gridView = (GridView) findViewById(R.id.gridview);

	dialog = ProgressDialog.show(this, "", getString(R.string.please_wait), true, true);
	boolean type = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("friends_list_type", false);
	list_type = type;
	updateListType(type);
	Bundle params = new Bundle();
	params.putString("fields", "name, picture, birthday, gender");
	Utility.getInstance().mAsyncRunner.request("me/friends", params, new UserRequestListener());
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
	    Intent intent = new Intent().setClass(ConnectionsActivity.this, FBFriendsActivity.class);
	    startActivity(intent);
	    return true;
	case R.id.sort:
	    Dialog dialog = new Dialog(this);
	    // TODO: options to sort the friends
	    dialog.setContentView(R.layout.about_layout);
	    dialog.setTitle("About Us");
	    dialog.show();
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
	FBUserInfo userInfo = usersList.get(position);
	// Create an intent with the friend's data
	Intent intent = new Intent().setClass(ConnectionsActivity.this, FriendProfileActivity.class);
	intent.putExtra("fbid", userInfo.id);
	intent.putExtra("name", userInfo.name);
	intent.putExtra("gender", userInfo.gender);
	intent.putExtra("picture", userInfo.picURL);
	intent.putExtra("age", userInfo.age);
	startActivity(intent);
    }

    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {

	@Override
	public void onComplete(final String response, final Object state) {
	    try {
		JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
		// Convert the JSON array to a regular Java list
		usersList.clear();
		int len = jsonArray.length();
		for (int i = 0; i < len; i++)
		    usersList.add(new FBUserInfo(jsonArray.getJSONObject(i)));
		// Sort the list alphabetically
		Collections.sort(usersList, (new Comparators()).new AlphabetComparator());
		runOnUiThread(new Runnable() {
		    public void run() {
			friendsAdapter.notifyDataSetChanged(); // Notify the adapter (must do from the main thread)
		    }
		});
		dialog.dismiss(); // Dismiss the loading dialog

	    } catch (JSONException e) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
		    @Override
		    public void run() {
			Toast.makeText(getApplicationContext(), "Failed to request user data", Toast.LENGTH_SHORT).show();
		    }
		});
	    }
	}
    }
}
