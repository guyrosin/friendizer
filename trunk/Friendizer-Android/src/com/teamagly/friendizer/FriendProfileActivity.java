/**
 * 
 */
package com.teamagly.friendizer;

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
    private TextView value;
    private TextView money;

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
	value = (TextView) findViewById(R.id.value);
	money = (TextView) findViewById(R.id.money);

	if (userPic != null)
	    userPic.setImageBitmap(Utility.getBitmap(intent.getStringExtra("picture")));
	name.setText(intent.getStringExtra("name"));
	age.setText(intent.getStringExtra("age"));
	if (age.getText() == "")
	    ageTitle.setText("");
	String genderStr = intent.getStringExtra("gender");
	// Capitalize the first letter
	if (genderStr.equals("male"))
	    genderStr = "Male";
	else if (genderStr.equals("female"))
	    genderStr = "Female";
	gender.setText(genderStr);
	fbid = intent.getLongExtra("fbid", 0);

	value.setText(String.valueOf(10000));
	money.setText(String.valueOf(20000));

	UserInfo userInfo;
	try {
	    userInfo = ServerFacade.userDetails(fbid);
	    if (userInfo != null) {
		value.setText(String.valueOf(userInfo.getValue()));
		money.setText(String.valueOf(userInfo.getMoney()));
	    }
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	if ((Utility.userInfo != null) && (Utility.userInfo.getOwnsList().contains(fbid))) {

	    // Listener for the Poke button
	    final Button pokeButton = (Button) findViewById(R.id.poke_btn);
	    pokeButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		}
	    });
	} else {
	    // Listener for the Buy button
	    final Button buyButton = (Button) findViewById(R.id.buy_btn);
	    buyButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    try {
			ServerFacade.buy(Utility.userInfo.getId(), fbid);
		    } catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Couldn't buy " + e.getMessage(), Toast.LENGTH_SHORT).show();
		    }
		}
	    });
	}

    }

}
