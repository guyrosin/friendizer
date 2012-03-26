package com.teamagly.friendizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FBFriendsActivity extends ListActivity implements OnItemClickListener {
    ProgressDialog dialog;
    protected static JSONArray jsonArray;
    private ListAdapter listAdapter;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.friends_layout);
	dialog = ProgressDialog.show(this, "", getString(R.string.please_wait), true, true);
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
	listAdapter = new FriendListAdapter(this);
	getListView().setOnItemClickListener(this);
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
	    Intent intent = new Intent().setClass(FBFriendsActivity.this, FriendProfileActivity.class);
	    FBUserInfo userInfo = new FBUserInfo(jsonObject);
	    intent.putExtra("fbid", userInfo.id);
	    intent.putExtra("name", userInfo.name);
	    intent.putExtra("picture", userInfo.picURL);
	    intent.putExtra("gender", userInfo.gender);
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
		Toast toast = Toast.makeText(FBFriendsActivity.this, msg, Toast.LENGTH_LONG);
		toast.show();
	    }
	});
    }

    /**
     * Definition of the list adapter
     */
    public class FriendListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	FBFriendsActivity friendsList;

	public FriendListAdapter(FBFriendsActivity friendsList) {
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
		hView.setTag(holder);
	    }

	    ViewHolder holder = (ViewHolder) hView.getTag();
	    FBUserInfo userInfo = new FBUserInfo(jsonObject);
	    Utility.getInstance().imageLoader.displayImage(userInfo.picURL, holder.profile_pic);
	    holder.name.setText(userInfo.name);
	    return hView;
	}
    }

    class ViewHolder {
	ImageView profile_pic;
	TextView name;
    }

    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {
	FBFriendsActivity curActivity;

	@Override
	public void onComplete(final String response, final Object state) {
	    try {
		jsonArray = new JSONObject(response).getJSONArray("data");
	    } catch (JSONException e) {
		showToast(e.getMessage());
		return;
	    }
	    // Update the view (must do it from the main thread)
	    runOnUiThread(new Runnable() {
		public void run() {
		    getListView().setAdapter(listAdapter);
		}
	    });
	    dialog.dismiss(); // Dismiss the loading dialog
	}
    }
}
