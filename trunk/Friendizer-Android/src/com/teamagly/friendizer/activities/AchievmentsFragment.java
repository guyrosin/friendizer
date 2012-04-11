/**
 * 
 */
package com.teamagly.friendizer.activities;

import com.teamagly.friendizer.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;

/**
 * @author Guy
 * 
 */
public class AchievmentsFragment extends Fragment {

    private final String TAG = getClass().getName();

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.achievements_layout, container, false);
		return view;
    }
}
