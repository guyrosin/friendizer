package com.teamagly.friendizer;

import org.json.JSONObject;

import com.teamagly.friendizer.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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
    ImageView ownerPic;

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

	if (userPic != null)
	    userPic.setImageBitmap(Utility.userPic);
	userName.setText(Utility.userName);
	age.setText(Utility.age);
	gender.setText(Utility.gender);
	if (Utility.userInfo != null) {
	    value.setText(String.valueOf(Utility.userInfo.getValue()));
	    money.setText(String.valueOf(Utility.userInfo.getMoney()));
	    owns.setText(String.valueOf(Utility.userInfo.getOwnsNum()));

	    // Get the owner's name and picture from Facebook
	    Bundle params = new Bundle();
	    params.putString("fields", "name, picture");
	    Utility.mAsyncRunner.request(String.valueOf(Utility.userInfo.getOwner()), params, new UserRequestListener());
	}
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	// Update the views (if necessary)
	if (userPic != null)
	    userPic.setImageBitmap(Utility.userPic);
	userName.setText(Utility.userName);
	age.setText(Utility.age);
	gender.setText(Utility.gender);

	if (Utility.userInfo != null) {
	    value.setText(String.valueOf(Utility.userInfo.getValue()));
	    money.setText(String.valueOf(Utility.userInfo.getMoney()));
	    owns.setText(String.valueOf(Utility.userInfo.getOwnsNum()));

	    // Get the owner's name and picture from Facebook
	    Bundle params = new Bundle();
	    params.putString("fields", "name, picture");
	    Utility.mAsyncRunner.request(String.valueOf(Utility.userInfo.getOwner()), params, new UserRequestListener());
	}
    }

    /*
     * Callback for fetching owner's details
     */
    public class UserRequestListener extends BaseRequestListener {

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
			ownerPic.setImageBitmap(Utility.getBitmap(picURL));
		    }
		});

	    } catch (Exception e) {
	    }
	}
    }

}
