package com.teamagly.friendizer;

import org.json.JSONException;
import org.json.JSONObject;

import com.teamagly.friendizer.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class MyProfileActivity extends Activity {
    private ImageView userPic;
    private TextView userName;
    private TextView age;
    private TextView gender;
    private TextView value;
    private TextView money;
    private TextView owns;
    private TextView ownerName;
    private ImageView ownerPic;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.my_profile_layout);
	userPic = (ImageView) MyProfileActivity.this.findViewById(R.id.user_pic);
	userName = (TextView) MyProfileActivity.this.findViewById(R.id.name);
	age = (TextView) MyProfileActivity.this.findViewById(R.id.age);
	gender = (TextView) MyProfileActivity.this.findViewById(R.id.gender);
	value = (TextView) MyProfileActivity.this.findViewById(R.id.value);
	money = (TextView) MyProfileActivity.this.findViewById(R.id.money);
	owns = (TextView) MyProfileActivity.this.findViewById(R.id.owns);
	ownerName = (TextView) MyProfileActivity.this.findViewById(R.id.owner_name);
	ownerPic = (ImageView) MyProfileActivity.this.findViewById(R.id.owner_pic);

	UserInfo userInfo = Utility.getInstance().userInfo;
	Utility.getInstance().imageLoader.displayImage(userInfo.picURL, userPic);
	userName.setText(userInfo.name);
	age.setText(userInfo.age);
	gender.setText(userInfo.gender);
	value.setText(String.valueOf(userInfo.value));
	money.setText(String.valueOf(userInfo.money));
	if (userInfo.ownsList != null)
	    owns.setText(String.valueOf(userInfo.ownsList.length));
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	// Reload the user's details from our servers (in the background)
	new Handler().post(new Runnable() {
	    @Override
	    public void run() {
		try {
		    Utility.getInstance().userInfo.updateFriendizerData(ServerFacade.userDetails(Utility.getInstance().userInfo.id));
		} catch (Exception e) {
		    e.printStackTrace();
		}
		// Update the views
		UserInfo userInfo = Utility.getInstance().userInfo;
		value.setText(String.valueOf(userInfo.value));
		money.setText(String.valueOf(userInfo.money));
		if (userInfo.ownsList != null)
		    owns.setText(String.valueOf(userInfo.ownsList.length));
	    }
	});

	// Get the owner's name and picture from Facebook
	Bundle params = new Bundle();
	params.putString("fields", "name, picture");
	Utility.getInstance().mAsyncRunner.request(String.valueOf(Utility.getInstance().userInfo.ownerID), params,
		new OwnerRequestListener());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.refresh:
	    // Reload the user's details from Facebook
	    Bundle params = new Bundle();
	    params.putString("fields", "name, first_name, picture, birthday, gender");
	    Utility.getInstance().mAsyncRunner.request("me", params, new UserRequestListener());
	    onResume();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
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
		final UserInfo userInfo = new UserInfo(jsonObject);
		// Update the user's details from Facebook
		Utility.getInstance().userInfo.updateFacebookData(userInfo);
		// Update the views (has to be done from the main thread)
		runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			Utility.getInstance().imageLoader.displayImage(userInfo.picURL, userPic);
			userName.setText(userInfo.name);
			age.setText(userInfo.age);
			gender.setText(userInfo.gender);
		    }
		});
	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	}
    }

    /*
     * Callback for fetching owner's details from Facebook
     */
    public class OwnerRequestListener extends BaseRequestListener {

	@Override
	public void onComplete(final String response, final Object state) {
	    JSONObject jsonObject;
	    try {
		jsonObject = new JSONObject(response);

		ownerName.setText(jsonObject.getString("name"));
		final String picURL = jsonObject.getString("picture");

		new Handler().post(new Runnable() {
		    @Override
		    public void run() {
			Utility.getInstance().imageLoader.displayImage(picURL, ownerPic);
		    }
		});

	    } catch (Exception e) {
	    }
	}
    }

}
