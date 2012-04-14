/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.Achievement;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author Guy
 * 
 */
public class AchievmentsFragment extends Fragment {

    private final String TAG = getClass().getName();
    ArrayAdapter<Achievement> adapter;
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
	adapter = new ArrayAdapter<Achievement>(getActivity(), R.layout.connection_list_item, achievements);

	new Thread(new Runnable() {
	    public void run() {
		// TODO: server facade -> get achievements
		adapter.notifyDataSetChanged();
		showLoadingIcon(false);
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
