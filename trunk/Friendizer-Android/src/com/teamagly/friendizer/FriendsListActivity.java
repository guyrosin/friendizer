package com.teamagly.friendizer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
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

public class FriendsListActivity extends ListActivity implements OnItemClickListener {
    ProgressDialog dialog;
    private Handler mHandler;
    private boolean list_type;
    protected static JSONArray jsonArray;
    private GridView gridview;
    private ListAdapter listAdapter;

    /*
     * Layout the friends' list
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.friends_layout);

	dialog = ProgressDialog.show(this, "", getString(R.string.please_wait), true, true);
	Bundle params = new Bundle();
	params.putString("fields", "name, picture, birthday, gender");
	String response = "";
	try {
	    response = Utility.facebook.request("me/friends", params);
	    dialog.dismiss();
	} catch (MalformedURLException e) {
	    dialog.dismiss();
	    Toast.makeText(getApplicationContext(), "Facebook Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
	} catch (IOException e) {
	    dialog.dismiss();
	    Toast.makeText(getApplicationContext(), "Facebook Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
	}

	mHandler = new Handler();
	gridview = (GridView) findViewById(R.id.gridview);

	try {
	    jsonArray = new JSONObject(response).getJSONArray("data");
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	if (Utility.model == null)
	    Utility.model = new FriendsGetProfilePics(); // Load the profile pictures
	list_type = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("friends_list_type", true);
	if (list_type) { // -> show in a list
	    gridview.setAdapter(null);
	    listAdapter = new FriendListAdapter(this);
	    getListView().setOnItemClickListener(this);
	    getListView().setAdapter(listAdapter);
	} else { // -> show in a gridView
	    getListView().setAdapter(null);
	    listAdapter = new FriendImageAdapter(this);
	    gridview.setAdapter(listAdapter);
	    gridview.setOnItemClickListener(this);
	}
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	boolean type = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("friends_list_type", true);
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

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
	try {
	    JSONObject array = jsonArray.getJSONObject(position);
	    showToast("You have chosen " + array.getString("name"));
	    // Create an intent with the friend's data
	    Intent intent = new Intent().setClass(FriendsListActivity.this, FriendProfileActivity.class);
	    intent.putExtra("fbid", array.getLong("id"));
	    intent.putExtra("name", array.getString("name"));
	    intent.putExtra("gender", array.getString("gender"));
	    intent.putExtra("picture", array.getString("picture"));
	    try {
		String age = Utility.calcAge(new Date(array.getString("birthday")));
		intent.putExtra("age", age);
	    } catch (Exception e) {
		intent.putExtra("age", "");
	    }
	    startActivity(intent);
	} catch (JSONException e) {
	    showToast("Error: " + e.getMessage());
	}
    }

    public void showToast(final String msg) {
	mHandler.post(new Runnable() {
	    @Override
	    public void run() {
		Toast toast = Toast.makeText(FriendsListActivity.this, msg, Toast.LENGTH_LONG);
		toast.show();
	    }
	});
    }

    /**
     * Definition of the list adapter
     */
    public class FriendListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	FriendsListActivity friendsList;

	public FriendListAdapter(FriendsListActivity friendsList) {
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
		hView = mInflater.inflate(R.layout.friend_list_item, null);
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
	    try {
		holder.profile_pic.setImageBitmap(Utility.model.getImage(jsonObject.getString("id"),
			jsonObject.getString("picture")));
		holder.name.setText(jsonObject.getString("name"));
	    } catch (JSONException e) {
		holder.name.setText("");
	    }
	    // try {
	    // holder.online_presence.setText(jsonObject.getString("friends_online_presence")); // TODO: not working
	    // } catch (JSONException e) {
	    // holder.online_presence.setText("");
	    // }
	    try {
		holder.gender.setText(jsonObject.getString("gender"));
	    } catch (JSONException e) {
		holder.gender.setText("");
	    }
	    try {
		holder.age.setText(Utility.calcAge(new Date(jsonObject.getString("birthday"))));
	    } catch (Exception e) {
		holder.age.setText("");
		holder.ageTitle.setText("");
	    }
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
	    if (Utility.model == null) {
		Utility.model = new FriendsGetProfilePics();
	    }
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

	    try {
		imageView.setImageBitmap(Utility.model.getImage(jsonObject.getString("id"), jsonObject.getString("picture")));
	    } catch (Exception e) {
	    }
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
}
