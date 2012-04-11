package com.teamagly.friendizer.activities;

import com.teamagly.friendizer.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class DashboardActivity extends Activity {

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
	
		// Attach event handlers
		findViewById(R.id.home_btn_me).setOnClickListener(new OnClickListener() {
		    public void onClick(View view) {
				Intent intent = new Intent(getBaseContext(), FriendizerActivity.class);
				intent.putExtra("to", R.string.my_profile);
				startActivity(intent);
		    }
	
		});
	
		// Attach event handlers
		findViewById(R.id.home_btn_me).setOnClickListener(new OnClickListener() {
		    public void onClick(View view) {
				Intent intent = new Intent(getBaseContext(), FriendizerActivity.class);
				intent.putExtra("to", R.string.my_profile);
				startActivity(intent);
		    }
	
		});
	
		// Attach event handlers
		findViewById(R.id.home_btn_me).setOnClickListener(new OnClickListener() {
		    public void onClick(View view) {
				Intent intent = new Intent(getBaseContext(), FriendizerActivity.class);
				intent.putExtra("to", R.string.my_profile);
				startActivity(intent);
		    }
	
		});
	
		// Attach event handlers
		findViewById(R.id.home_btn_me).setOnClickListener(new OnClickListener() {
		    public void onClick(View view) {
				Intent intent = new Intent(getBaseContext(), FriendizerActivity.class);
				intent.putExtra("to", R.string.my_profile);
				startActivity(intent);
		    }
	
		});
	
		// Attach event handlers
		findViewById(R.id.home_btn_me).setOnClickListener(new OnClickListener() {
		    public void onClick(View view) {
				Intent intent = new Intent(getBaseContext(), FriendizerActivity.class);
				intent.putExtra("to", R.string.my_profile);
				startActivity(intent);
		    }
	
		});

    }
}