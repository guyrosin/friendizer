/**
 * 
 */
package com.teamagly.friendizer;

import com.teamagly.friendizer.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Guy
 * 
 */
public class FriendProfileActivity extends Activity {
    private long fbid;
    private ImageView userPic;
    private TextView name;
    private TextView age;
    private TextView ageTitle;
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
	ageTitle = (TextView) findViewById(R.id.age_title);
	gender = (TextView) findViewById(R.id.gender);

	UserInfo userInfo = (UserInfo) intent.getSerializableExtra("user");
	Utility.getInstance().imageLoader.displayImage(userInfo.picURL, userPic);
	name.setText(userInfo.name);
	String genderStr = "";
	genderStr = userInfo.gender;
	// Capitalize the first letter
	if (genderStr.equals("male"))
	    genderStr = "Male";
	else if (genderStr.equals("female"))
	    genderStr = "Female";
	else
	    ageTitle.setText("age: "); // No gender -> remove the separator from the age title
	gender.setText(genderStr);
	age.setText(userInfo.age);
	if (age.getText().length() == 0)
	    ageTitle.setText("");
	fbid = userInfo.id;

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
			ServerFacade.buy(Utility.getInstance().userInfo.id, fbid);
		    } catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Couldn't buy " + name, Toast.LENGTH_SHORT).show();
		    }
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

}
