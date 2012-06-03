/**
 * 
 */
package com.teamagly.friendizer.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ImageLoader.Type;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.widgets.SegmentedRadioGroup;

public class FriendProfileActivity extends SherlockActivity {

    private final String TAG = getClass().getName();
    ActionBar actionBar;
    SegmentedRadioGroup segmentedControl;
    User userInfo;

    private ImageView userPic;
    private TextView txtName;
    private TextView txtStatus;
    private TextView txtAge;
    private TextView txtAgeTitle;
    private TextView txtGender;
    private TextView txtValue;
    private TextView txtMatching;
    private TextView txtOwns;
    private TextView txtOwnerName;
    private ImageView txtOwnerPic;
    private TextView txtMutualFriends;
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
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	Intent intent = getIntent();
	setContentView(R.layout.friend_profile_layout);
	actionBar = getSupportActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);

	userPic = (ImageView) findViewById(R.id.user_pic);
	txtName = (TextView) findViewById(R.id.name);
	txtStatus = (TextView) findViewById(R.id.status);
	txtAge = (TextView) findViewById(R.id.age);
	txtAgeTitle = (TextView) findViewById(R.id.age_title);
	txtGender = (TextView) findViewById(R.id.gender);
	txtValue = (TextView) findViewById(R.id.value);
	txtMatching = (TextView) findViewById(R.id.matching);
	txtOwns = (TextView) findViewById(R.id.owns);
	txtOwnerName = (TextView) findViewById(R.id.owner_name);
	txtOwnerPic = (ImageView) findViewById(R.id.owner_pic);
	txtMutualFriends = (TextView) findViewById(R.id.mutual_friends);
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
	    actionBar.setTitle(userInfo.getName());
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
	setSupportProgressBarIndeterminateVisibility(true);
	// Reload the user's details from Facebook
	Bundle params = new Bundle();
	params.putString("fields", "name, first_name, picture, birthday, gender");
	Utility.getInstance().mAsyncRunner.request(String.valueOf(userInfo.getId()), params, new UserRequestListener());

	// Reload the mutual friends number
	params = new Bundle();
	Utility.getInstance().mAsyncRunner.request("me/mutualfriends/" + String.valueOf(userInfo.getId()), params,
		new MutualFriendsListener());

	// Reload the user's details from our servers (in the background)
	new FriendizerTask().execute(userInfo.getId());
	// Get the matching with this user (in the background)
	new MatchingTask().execute(userInfo.getId(), Utility.getInstance().userInfo.getId());
    }

    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	MenuInflater inflater = getSupportMenuInflater();
	inflater.inflate(R.menu.main_menu, menu);
	return true;
    }

    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
	    finish(); // Just go back
	    return true;
	case R.id.menu_refresh:
	    onResume();
	    return true;
	case R.id.menu_settings: // Move to the settings activity
	    startActivity(new Intent(this, FriendsPrefs.class));
	    return true;
	case R.id.menu_invite: // Show the Facebook invitation dialog
	    Bundle params = new Bundle();
	    params.putString("message", getString(R.string.invitation_msg));
	    Utility.getInstance().facebook.dialog(this, "apprequests", params, new BaseDialogListener());
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    protected void updateViews() {
	updateFriendizerViews();
	updateFacebookViews();
    }

    protected void updateFriendizerViews() {
	txtValue.setText(String.valueOf(userInfo.getValue()));
	if (userInfo.getOwnsList() != null)
	    txtOwns.setText(String.valueOf(userInfo.getOwnsList().length));
	if (userInfo.getStatus().length() > 0) {
	    txtStatus.setText("\"" + userInfo.getStatus() + "\"");
	    txtStatus.setVisibility(View.VISIBLE);
	} else
	    txtStatus.setVisibility(View.GONE);
    }

    protected void updateFacebookViews() {
	userPic.setImageBitmap(Utility.getInstance().imageLoader.getImage(userInfo.getPicURL(), Type.ROUND_CORNERS));
	txtName.setText(userInfo.getName());
	txtAge.setText(userInfo.getAge());
	if (userInfo.getAge().length() == 0)
	    txtAgeTitle.setVisibility(View.GONE);
	else
	    txtAgeTitle.setVisibility(View.VISIBLE);
	String genderStr = userInfo.getGender();
	// Capitalize the first letter
	txtGender.setText(Character.toUpperCase(genderStr.charAt(0)) + genderStr.substring(1));
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
			Toast.makeText(getBaseContext(), "Couldn't buy " + txtName, Toast.LENGTH_SHORT).show();
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
			txtMutualFriends.setText(String.valueOf(friends.length()));
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
			actionBar.setTitle(userInfo.getName());
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
			txtOwnerName.setText(ownerNameStr);
			txtOwnerPic.setImageBitmap(Utility.getInstance().imageLoader.getImage(picURL, Type.ROUND_CORNERS));
			// Add a listener for the owner's pic
			txtOwnerPic.setOnClickListener(new OnClickListener() {
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

    class FriendizerTask extends AsyncTask<Long, Void, FriendizerUser> {

	protected FriendizerUser doInBackground(Long... userIDs) {
	    try {
		return ServerFacade.userDetails(userIDs[0]);
	    } catch (Exception e) {
		Log.e(TAG, e.getMessage());
		return null;
	    }
	}

	protected void onPostExecute(final FriendizerUser newUserInfo) {
	    // Request the user's details from friendizer and update the views accordingly
	    try {
		userInfo.updateFriendizerData(newUserInfo);
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
	    // Update the views
	    updateFriendizerViews();
	    updateButtons();
	}
    }

    class MatchingTask extends AsyncTask<Long, Void, Integer> {

	protected Integer doInBackground(Long... userIDs) {
	    try {
		return ServerFacade.matching(userIDs[0], userIDs[1]);
	    } catch (Exception e) {
		Log.e(TAG, e.getMessage());
		return 0;
	    }
	}

	protected void onPostExecute(Integer matching) {
	    userInfo.setMatching(matching);
	    // Update the view
	    txtMatching.setText(String.valueOf(matching));
	    setSupportProgressBarIndeterminateVisibility(false); // Done loading the data (roughly...)
	}
    }

}
