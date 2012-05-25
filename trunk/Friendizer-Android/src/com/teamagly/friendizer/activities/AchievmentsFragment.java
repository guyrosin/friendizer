/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.AchievementsAdapter;
import com.teamagly.friendizer.model.Achievement;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.widgets.ActionBar;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * @author Guy
 * 
 */
public class AchievmentsFragment extends Fragment {

    private final String TAG = getClass().getName();
    AchievementsAdapter adapter;
    ArrayList<Achievement> achievements = new ArrayList<Achievement>();
    ListView listView;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	// Inflate the layout for this fragment
	View view = inflater.inflate(R.layout.achievements_layout, container, false);
	ActionBar actionBar = (ActionBar) view.findViewById(R.id.actionbar);
	actionBar.setVisibility(View.GONE);
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
	showLoadingIcon(true);
	achievements.clear();
	adapter = new AchievementsAdapter(getActivity(), R.layout.achievements_list_item, achievements);
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
		getActivity().runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			adapter.notifyDataSetChanged();
			showLoadingIcon(false);
		    }
		});
	    }
	}).start();

    }

    /**
     * @param show
     *            whether to show or hide the loading icon (in the parent activity)
     */
    protected void showLoadingIcon(boolean show) {
	try {
	    Activity parent = getActivity().getParent();
	    if (parent != null)
		((FriendizerActivity) parent).actionBar.showProgressBar(show);
	} catch (Exception e) {
	}
    }

}
