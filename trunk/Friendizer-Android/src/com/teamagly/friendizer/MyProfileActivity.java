package com.teamagly.friendizer;

import com.teamagly.friendizer.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MyProfileActivity extends Activity {
    private ImageView userPic;
    private TextView userName;
    private TextView age;
    private TextView gender;

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

	if (userPic != null)
	    userPic.setImageBitmap(Utility.userPic);
	userName.setText(Utility.userName);
	age.setText(Utility.age);
	gender.setText(Utility.gender);
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
    }

}
