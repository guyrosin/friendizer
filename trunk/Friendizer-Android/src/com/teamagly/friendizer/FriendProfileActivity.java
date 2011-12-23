/**
 * 
 */
package com.teamagly.friendizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Guy
 * 
 */
public class FriendProfileActivity extends Activity {
    private long fbid;
    private ImageView userPic;
    private TextView name;
    private TextView age;
    private TextView gender;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Intent intent = getIntent();
	setContentView(R.layout.friend_profile_layout);
	userPic = (ImageView) findViewById(R.id.user_pic);
	name = (TextView) findViewById(R.id.name);
	age = (TextView) findViewById(R.id.age);
	gender = (TextView) findViewById(R.id.gender);

	if (userPic != null)
	    userPic.setImageBitmap(Utility.getBitmap(intent.getStringExtra("picture")));
	name.setText(intent.getStringExtra("name"));
	age.setText(intent.getStringExtra("age"));
	String genderStr = intent.getStringExtra("gender");
	// Capitalize the first letter
	if (genderStr.equals("male"))
	    genderStr = "Male";
	else if (genderStr.equals("female"))
	    genderStr = "Female";
	gender.setText(genderStr);
	fbid = intent.getLongExtra("fbid", 0);
    }

}
