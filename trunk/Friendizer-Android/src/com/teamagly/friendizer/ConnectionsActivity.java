package com.teamagly.friendizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectionsActivity extends ListActivity implements OnItemClickListener {
    ProgressDialog dialog;
    private boolean list_type;
    protected static JSONArray jsonArray;
    private GridView gridview;
    private ListAdapter listAdapter;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.connections_layout);

	dialog = ProgressDialog.show(this, "", getString(R.string.please_wait), true, true);
	Bundle params = new Bundle();
	params.putString("fields", "name, picture, birthday, gender");
	Utility.mAsyncRunner.request("me/friends", params, new UserRequestListener(this));
	boolean type = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("friends_list_type", false);
	list_type = type; // Initialize with the user's chosen type
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	gridview = (GridView) findViewById(R.id.gridview);
	boolean type = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("friends_list_type", false);
	if (type != list_type) { // A change occurred -> redraw the view
	    list_type = type;
	    if (list_type) { // => show in a list
		gridview.setAdapter(null);
		listAdapter = new FriendListAdapter(this);
		getListView().setOnItemClickListener(this);
		getListView().setAdapter(listAdapter);
	    } else { // == show in a GridView
		getListView().setAdapter(null);
		listAdapter = new FriendImageAdapter(this);
		gridview.setAdapter(listAdapter);
		gridview.setOnItemClickListener(this);
	    }
	}
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
	    Utility.facebook.dialog(this, "apprequests", params, new BaseDialogListener());
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
	try {
	    JSONObject jsonObject = jsonArray.getJSONObject(position);
	    // Create an intent with the friend's data
	    Intent intent = new Intent().setClass(ConnectionsActivity.this, FriendProfileActivity.class);
	    FBUserInfo userInfo = new FBUserInfo(jsonObject);
	    intent.putExtra("fbid", userInfo.id);
	    intent.putExtra("name", userInfo.name);
	    intent.putExtra("gender", userInfo.gender);
	    intent.putExtra("picture", userInfo.picURL);
	    intent.putExtra("age", userInfo.age);
	    startActivity(intent);
	} catch (JSONException e) {
	    showToast("Error: " + e.getMessage());
	    return;
	}
    }

    /**
     * Shows a toast
     * 
     * @param msg
     *            a message to show
     */
    public void showToast(final String msg) {
	new Handler().post(new Runnable() {
	    @Override
	    public void run() {
		Toast toast = Toast.makeText(ConnectionsActivity.this, msg, Toast.LENGTH_LONG);
		toast.show();
	    }
	});
    }

    /**
     * Definition of the list adapter
     */
    public class FriendListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	ConnectionsActivity friendsList;

	public FriendListAdapter(ConnectionsActivity friendsList) {
	    Utility.model.setListener(this);
	    this.friendsList = friendsList;
	    mInflater = LayoutInflater.from(friendsList.getBaseContext());
	}

	@Override
	public int getCount() {
	    return jsonArray.length();
	}

	@Override
	public Object getItem(int position) {
	    return null;
	}

	@Override
	public long getItemId(int position) {
	    return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    JSONObject jsonObject = null;
	    try {
		jsonObject = jsonArray.getJSONObject(position);
	    } catch (JSONException e1) {
		e1.printStackTrace();
	    }
	    View hView = convertView;
	    if (convertView == null) {
		hView = mInflater.inflate(R.layout.connection_list_item, null);
		ViewHolder holder = new ViewHolder();
		holder.profile_pic = (ImageView) hView.findViewById(R.id.profile_pic);
		holder.name = (TextView) hView.findViewById(R.id.name);
		holder.gender = (TextView) hView.findViewById(R.id.gender);
		holder.age = (TextView) hView.findViewById(R.id.age);
		holder.ageTitle = (TextView) hView.findViewById(R.id.age_title);
		// holder.online_presence = (TextView) hView.findViewById(R.id.online_presence);
		hView.setTag(holder);
	    }

	    ViewHolder holder = (ViewHolder) hView.getTag();
	    FBUserInfo userInfo = new FBUserInfo(jsonObject);
	    holder.profile_pic.setImageBitmap(userInfo.pic);
	    holder.name.setText(userInfo.name);
	    holder.gender.setText(userInfo.gender);
	    holder.age.setText(userInfo.age);
	    if (userInfo.age.length() == 0)
		holder.ageTitle.setText("");
	    return hView;
	}

    }

    /**
     * Definition of the list adapter
     */
    public class FriendImageAdapter extends BaseAdapter {
	private Context mContext;

	public FriendImageAdapter(Context c) {
	    mContext = c;
	    Utility.model.setListener(this);
	}

	@Override
	public int getCount() {
	    return jsonArray.length();
	}

	@Override
	public Object getItem(int position) {
	    return null;
	}

	@Override
	public long getItemId(int position) {
	    return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    JSONObject jsonObject = null;
	    try {
		jsonObject = jsonArray.getJSONObject(position);
	    } catch (JSONException e1) {
		e1.printStackTrace();
	    }
	    ImageView imageView;
	    if (convertView == null) { // if it's not recycled, initialize some attributes
		imageView = new ImageView(mContext);
		imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setPadding(5, 5, 5, 5);
	    } else {
		imageView = (ImageView) convertView;
	    }

	    imageView.setImageBitmap(Utility.model.getImage(jsonObject.optString("id"), jsonObject.optString("picture")));
	    return imageView;
	}

    }

    class ViewHolder {
	ImageView profile_pic;
	TextView name;
	TextView gender;
	TextView age;
	TextView ageTitle;
	// TextView online_presence;
    }

    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {
	ConnectionsActivity curActivity;

	public UserRequestListener(ConnectionsActivity curActivity) {
	    this.curActivity = curActivity;
	}

	@Override
	public void onComplete(final String response, final Object state) {
	    try {
		jsonArray = new JSONObject(response).getJSONArray("data");
		if (Utility.model == null)
		    Utility.model = new FriendsGetProfilePics(); // Load the profile pictures
		// Load the list type - list / grid (default is grid)
		list_type = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
			.getBoolean("friends_list_type", false);
		if (list_type) { // -> show in a list
		    listAdapter = new FriendListAdapter(curActivity);
		    // Update the view (must do it from the main thread)
		    runOnUiThread(new Runnable() {
			public void run() {
			    getListView().setOnItemClickListener(curActivity);
			    getListView().setAdapter(listAdapter);
			}
		    });
		} else { // -> show in a gridView
		    listAdapter = new FriendImageAdapter(curActivity);
		    // Update the view (must do it from the main thread)
		    runOnUiThread(new Runnable() {
			public void run() {
			    gridview.setAdapter(listAdapter);
			    gridview.setOnItemClickListener(curActivity);
			}
		    });
		}
		dialog.dismiss(); // Dismiss the loading dialog

	    } catch (Exception e) {
		new Handler().post(new Runnable() {
		    @Override
		    public void run() {
			Toast.makeText(getApplicationContext(), "Failed to request user data", Toast.LENGTH_SHORT).show();
		    }
		});
	    }
	}
    }
}
