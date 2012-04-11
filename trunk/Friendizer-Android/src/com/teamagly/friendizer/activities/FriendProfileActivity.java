/**
 * 
 */
package com.teamagly.friendizer.activities;

import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.UserInfo;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.utils.ImageLoader.Type;
import com.teamagly.friendizer.widgets.ActionBar;

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
    UserInfo userInfo;
    private ImageView userPic;
    private TextView name;
    private TextView age;
    private TextView ageTitle;
    private TextView gender;
    private TextView value;
    private TextView money;
    private TextView owns;
    private TextView ownerName;
    private ImageView ownerPic;
    ActionBar actionBar;
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
		age = (TextView) findViewById(R.id.age);
		ageTitle = (TextView) findViewById(R.id.age_title);
		gender = (TextView) findViewById(R.id.gender);
		value = (TextView) findViewById(R.id.value);
		money = (TextView) findViewById(R.id.money);
		owns = (TextView) findViewById(R.id.owns);
		ownerName = (TextView) findViewById(R.id.owner_name);
		ownerPic = (ImageView) findViewById(R.id.owner_pic);
	
		userInfo = (UserInfo) intent.getSerializableExtra("user");
		updateViews();
	
		final Button btn1 = (Button) findViewById(R.id.btn1);
		final Button btn2 = (Button) findViewById(R.id.btn2);
	
		if (userInfo.ownerID == Utility.getInstance().userInfo.id) { // If I own this user
		    // Define the first button
		    btn1.setText("Chat");
		    btn1.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
				    // TODO: move to the chat activity
				}
		    });
		} else {
		    // Define the first button
		    btn1.setText("Buy");
		    btn1.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
				    try {
				    	ServerFacade.buy(Utility.getInstance().userInfo.id, userInfo.id);
				    } catch (Exception e) {
				    	Log.w(TAG, "", e);
				    	Toast.makeText(getBaseContext(), "Couldn't buy " + name, Toast.LENGTH_SHORT).show();
				    }
				    onResume(); // Refresh
				}
		    });
		}
	
		// Define the second button
		btn2.setText("Poke");
		btn2.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    }
		});
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
		Utility.getInstance().mAsyncRunner.request(String.valueOf(userInfo.id), params, new UserRequestListener());
	
		// Reload the user's details from our servers (in the background)
		new Thread(new Runnable() {
		    public void run() {
				try {
				    userInfo.updateFriendizerData(ServerFacade.userDetails(userInfo.id));
				    if (userInfo.ownerID > 0) {
						// Get the owner's name and picture from Facebook
						Bundle params = new Bundle();
						params.putString("fields", "name, picture");
						Utility.getInstance().mAsyncRunner.request(String.valueOf(userInfo.ownerID), params, new OwnerRequestListener());
				    }
				} catch (Exception e) {
				    Log.e(TAG, "", e);
				}
				// Update the views from the main thread
				handler.post(new Runnable() {
				    @Override
				    public void run() {
						updateFriendizerViews();
						showLoadingIcon(false); // Done loading the data (roughly...)
				    }
				});
		    }
		}).start();

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
		value.setText(String.valueOf(userInfo.value));
		money.setText(String.valueOf(userInfo.money));
		if (userInfo.ownsList != null)
		    owns.setText(String.valueOf(userInfo.ownsList.length));
    }

    protected void updateFacebookViews() {
		Utility.getInstance().imageLoader.displayImage(userInfo.picURL, userPic, Type.ROUND_CORNERS);
		name.setText(userInfo.name);
		age.setText(userInfo.age);
		if (userInfo.age.length() == 0)
		    ageTitle.setVisibility(View.GONE);
		else
		    ageTitle.setVisibility(View.VISIBLE);
		String genderStr = userInfo.gender;
		// Capitalize the first letter
		gender.setText(Character.toUpperCase(genderStr.charAt(0)) + genderStr.substring(1));
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
				final UserInfo newUserInfo = new UserInfo(jsonObject);
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
				    }
				});
		    } catch (Exception e) {
		    	Log.e(TAG, "", e);
		    }
		}
    }

}
