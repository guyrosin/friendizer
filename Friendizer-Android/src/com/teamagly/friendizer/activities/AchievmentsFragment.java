/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.AchievementsAdapter;
import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class AchievmentsFragment extends SherlockFragment {

    private final String TAG = getClass().getName();
    AchievementsAdapter adapter;
    ArrayList<Achievement> achievements = new ArrayList<Achievement>();
    ListView listView;
    protected SherlockFragmentActivity activity;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
	super.onActivityCreated(savedInstanceState);
	activity = getSherlockActivity();
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	// Inflate the layout for this fragment
	View view = inflater.inflate(R.layout.achievements_layout, container, false);
	listView = (ListView) view.findViewById(R.id.achievements_list);
	return view;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
	super.onResume();
	activity.setSupportProgressBarIndeterminateVisibility(true);
	achievements.clear();
	adapter = new AchievementsAdapter(activity, R.layout.achievements_list_item, achievements);
	listView.setAdapter(adapter);

	new Thread(new Runnable() {
	    public void run() {
		try {
		    Achievement[] achvs = ServerFacade.getAchievements(Utility.getInstance().userInfo.getId());
		    for (Achievement achv : achvs)
			achievements.add(achv);
		} catch (Exception e) {
		    Log.e(TAG, e.getMessage());
		}
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			adapter.notifyDataSetChanged();
			activity.setSupportProgressBarIndeterminateVisibility(false);
		    }
		});
	    }
	}).start();

    }
}
