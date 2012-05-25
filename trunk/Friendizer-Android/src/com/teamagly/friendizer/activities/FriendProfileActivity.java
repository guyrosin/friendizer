/**
 * 
 */
package com.teamagly.friendizer.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.utils.ImageLoader.Type;
import com.teamagly.friendizer.widgets.ActionBar;
import com.teamagly.friendizer.widgets.SegmentedRadioGroup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Guy
 * 
 */
public class FriendProfileActivity extends Activity {

    private final String TAG = getClass().getName();
    ActionBar actionBar;
    SegmentedRadioGroup segmentedControl;
    User userInfo;

    private ImageView userPic;
    private TextView name;
    private TextView status;
    private TextView age;
    private TextView ageTitle;
    private TextView gender;
    private TextView value;
    private TextView money;
    private TextView owns;
    // private TextView matching;
    private TextView ownerName;
    private ImageView ownerPic;
    private TextView mutualFriends;
    private Button btn1;
    private Button btn2;
    final Handler handler = new Handler();

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Intent intent = getIntent();
	setContentView(R.layout.friend_profile_layout);
	actionBar = (ActionBar) findViewById(R.id.actionbar);
	actionBar.mRefreshBtn.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		onResume();
	    }
	});

	userPic = (ImageView) findViewById(R.id.user_pic);
	name = (TextView) findViewById(R.id.name);
	status = (TextView) findViewById(R.id.status);
	age = (TextView) findViewById(R.id.age);
	ageTitle = (TextView) findViewById(R.id.age_title);
	gender = (TextView) findViewById(R.id.gender);
	value = (TextView) findViewById(R.id.value);
	money = (TextView) findViewById(R.id.money);
	owns = (TextView) findViewById(R.id.owns);
	// matching = (TextView) findViewById(R.id.matching);
	ownerName = (TextView) findViewById(R.id.owner_name);
	ownerPic = (ImageView) findViewById(R.id.owner_pic);
	mutualFriends = (TextView) findViewById(R.id.mutual_friends);
	btn1 = (Button) findViewById(R.id.btn1);
	btn2 = (Button) findViewById(R.id.btn2);

	userInfo = (User) intent.getSerializableExtra("user");
	if (userInfo == null) {
	    // If passed the user's ID, fetching the details will be done in onResume()
	    if (intent.getLongExtra("userID", 0) > 0) {
		userInfo = new User();
		userInfo.setId(intent.getLongExtra("userID", 0));
	    } else {
		Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT);
		finish();
	    }
	} else {
	    updateViews();
	    updateButtons();
	}
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	showLoadingIcon(true);
	// Reload the user's details from Facebook
	Bundle params = new Bundle();
	params.putString("fields", "name, first_name, picture, birthday, gender");
	Utility.getInstance().mAsyncRunner.request(String.valueOf(userInfo.getId()), params, new UserRequestListener());

	// Reload the mutual friends number
	params = new Bundle();
	Utility.getInstance().mAsyncRunner.request("me/mutualfriends/" + String.valueOf(userInfo.getId()), params,
		new MutualFriendsListener());

	// Reload the user's details from our servers (in the background)
	new Thread(new FriendizerRunnable()).start();

    }

    protected void showLoadingIcon(boolean show) {
	try {
	    actionBar.showProgressBar(show);
	} catch (Exception e) {
	}
    }

    protected void updateViews() {
	updateFriendizerViews();
	updateFacebookViews();
    }

    protected void updateFriendizerViews() {
	value.setText(String.valueOf(userInfo.getValue()));
	money.setText(String.valueOf(userInfo.getMoney()));
	if (userInfo.getOwnsList() != null)
	    owns.setText(String.valueOf(userInfo.getOwnsList().length));
	if (userInfo.getStatus().length() > 0) {
	    status.setText("\"" + userInfo.getStatus() + "\"");
	    status.setVisibility(View.VISIBLE);
	} else
	    status.setVisibility(View.GONE);
	// matching.setText(userInfo.getMatching());
    }

    protected void updateFacebookViews() {
	Utility.getInstance().imageLoader.displayImage(userInfo.getPicURL(), userPic, Type.ROUND_CORNERS);
	name.setText(userInfo.getName());
	age.setText(userInfo.getAge());
	if (userInfo.getAge().length() == 0)
	    ageTitle.setVisibility(View.GONE);
	else
	    ageTitle.setVisibility(View.VISIBLE);
	String genderStr = userInfo.getGender();
	// Capitalize the first letter
	gender.setText(Character.toUpperCase(genderStr.charAt(0)) + genderStr.substring(1));
    }

    protected void updateButtons() {
	btn1.setVisibility(View.VISIBLE);
	if (userInfo.getOwnerID() == Utility.getInstance().userInfo.getId()) { // If I own this user
	    // Define the first button
	    btn1.setText("Chat");
	    btn1.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    // Move to the chat activity
		    Intent intent = new Intent().setClass(FriendProfileActivity.this, MessagesActivity.class);
		    intent.putExtra("user", userInfo);
		    startActivity(intent);
		}
	    });
	} else {
	    // Define the first button
	    btn1.setText("Buy");
	    btn1.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    try {
			ServerFacade.buy(Utility.getInstance().userInfo.getId(), userInfo.getId());
		    } catch (Exception e) {
			Log.w(TAG, "", e);
			Toast.makeText(getBaseContext(), "Couldn't buy " + name, Toast.LENGTH_SHORT).show();
		    }
		    onResume(); // Refresh
		}
	    });
	}

	btn2.setVisibility(View.VISIBLE);
	// Define the second button
	btn2.setText("Achievements");
	btn2.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		// Move to the achievements activity
		Intent intent = new Intent().setClass(FriendProfileActivity.this, FriendAchievementsActivity.class);
		intent.putExtra("user", userInfo);
		startActivity(intent);
	    }
	});
    }

    /*
     * Callback for fetching mutual friends from Facebook
     */
    public class MutualFriendsListener extends BaseRequestListener {

	@Override
	public void onComplete(final String response, final Object state) {
	    JSONObject jsonObject;
	    try {
		jsonObject = new JSONObject(response);
		final JSONArray friends = jsonObject.getJSONArray("data");
		// Update the view
		handler.post(new Runnable() {
		    @Override
		    public void run() {
			mutualFriends.setText(String.valueOf(friends.length()));
		    }
		});
	    } catch (JSONException e) {
		Log.e(TAG, "", e);
	    }
	}
    }

    /*
     * Callback for fetching user's details from Facebook
     */
    public class UserRequestListener extends BaseRequestListener {

	@Override
	public void onComplete(final String response, final Object state) {
	    JSONObject jsonObject;
	    try {
		jsonObject = new JSONObject(response);
		final FacebookUser newUserInfo = new FacebookUser(jsonObject);
		// Update the user's details from Facebook
		userInfo.updateFacebookData(newUserInfo);
		// Update the views (has to be done from the main thread)
		handler.post(new Runnable() {
		    @Override
		    public void run() {
			updateFacebookViews();
		    }
		});
	    } catch (JSONException e) {
		Log.e(TAG, "", e);
	    }
	}
    }

    /*
     * Callback for fetching owner's details from Facebook
     */
    public class OwnerRequestListener extends BaseRequestListener {

	@Override
	public void onComplete(final String response, final Object state) {
	    try {
		JSONObject jsonObject = new JSONObject(response);

		final String ownerNameStr = jsonObject.getString("name");
		final String picURL = jsonObject.getString("picture");

		handler.post(new Runnable() {
		    @Override
		    public void run() {
			ownerName.setText(ownerNameStr);
			Utility.getInstance().imageLoader.displayImage(picURL, ownerPic, Type.ROUND_CORNERS);
			// Add a listener for the owner's pic
			ownerPic.setOnClickListener(new OnClickListener() {
			    @Override
			    public void onClick(View v) {
				Intent intent = null;
				// Move to the owner's profile
				if (userInfo.getOwnerID() != Utility.getInstance().userInfo.getId()) {
				    intent = new Intent().setClass(FriendProfileActivity.this, FriendProfileActivity.class);
				    intent.putExtra("userID", userInfo.getOwnerID());
				} else {
				    // Move to my profile
				    intent = new Intent().setClass(v.getContext(), FriendizerActivity.class);
				    intent.putExtra("to", R.string.my_profile);
				}
				startActivity(intent);
			    }
			});
		    }
		});
	    } catch (Exception e) {
		Log.e(TAG, "", e);
	    }
	}
    }

    protected class FriendizerRunnable implements Runnable {
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	    // Request the user's details from friendizer and update the views accordingly
	    try {
		userInfo.updateFriendizerData(ServerFacade.userDetails(userInfo.getId()));
		if (userInfo.getOwnerID() > 0) {
		    // Get the owner's name and picture from Facebook
		    Bundle params = new Bundle();
		    params.putString("fields", "name, picture");
		    Utility.getInstance().mAsyncRunner.request(String.valueOf(userInfo.getOwnerID()), params,
			    new OwnerRequestListener());
		}
	    } catch (Exception e) {
		Log.w(TAG, "", e);
	    }
	    // Update the views from the main thread
	    handler.post(new Runnable() {
		@Override
		public void run() {
		    updateFriendizerViews();
		    updateButtons();
		    showLoadingIcon(false); // Done loading the data (roughly...)
		}
	    });
	}
    }

}
