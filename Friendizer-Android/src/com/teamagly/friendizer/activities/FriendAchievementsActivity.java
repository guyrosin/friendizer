/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.AchievementsAdapter;
import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.widgets.ActionBar;

/**
 * @author Guy
 * 
 */
public class FriendAchievementsActivity extends Activity {

    private final String TAG = getClass().getName();
    ActionBar actionBar;
    AchievementsAdapter adapter;
    ArrayList<Achievement> achievements = new ArrayList<Achievement>();
    ListView listView;
    User userInfo;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.achievements_layout);
	actionBar = (ActionBar) findViewById(R.id.actionbar);
	actionBar.mRefreshBtn.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		onResume();
	    }
	});
	listView = (ListView) findViewById(R.id.achievements_list);
	userInfo = (User) getIntent().getSerializableExtra("user");
	actionBar.setTitle(userInfo.getName());
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
	super.onResume();
	showLoadingIcon(true);
	achievements.clear();
	adapter = new AchievementsAdapter(this, R.layout.achievements_list_item, achievements);
	listView.setAdapter(adapter);

	// Get the user's achievements in the background
	new Thread(new Runnable() {
	    public void run() {
		try {
		    Achievement[] achvs = ServerFacade.getAchievements(Utility.getInstance().userInfo.getId());
		    for (Achievement achv : achvs)
			achievements.add(achv);
		} catch (Exception e) {
		    Log.e(TAG, e.getMessage());
		}
		runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			adapter.notifyDataSetChanged();
			showLoadingIcon(false);
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

}
