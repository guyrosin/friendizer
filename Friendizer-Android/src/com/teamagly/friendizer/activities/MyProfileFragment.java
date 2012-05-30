package com.teamagly.friendizer.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.widgets.SegmentedRadioGroup;

public class MyProfileFragment extends SherlockFragment implements OnCheckedChangeListener {
    SegmentedRadioGroup segmentedControl;
    protected SherlockFragmentActivity activity;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onActivityCreated(savedInstanceState);
	activity = getSherlockActivity();
	activity.setContentView(R.layout.profile_layout);
	segmentedControl = (SegmentedRadioGroup) activity.findViewById(R.id.segment_text);
	segmentedControl.setOnCheckedChangeListener(this);
	navigateTo(new MyInfoFragment());
    }

    /*
     * (non-Javadoc)
     * @see android.widget.RadioGroup.OnCheckedChangeListener#onCheckedChanged(android.widget.RadioGroup, int)
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
	if (checkedId == R.id.button_one) {
	    navigateTo(new MyInfoFragment());
	} else if (checkedId == R.id.button_two) {
	    navigateTo(new AchievmentsFragment());
	} else if (checkedId == R.id.button_three) {
	    navigateTo(new ActionHistoryFragment());
	}
    }

    /**
     * Navigates to a new fragment, which is added in the fragment container view.
     * 
     * @param newFragment
     */
    protected void navigateTo(SherlockFragment newFragment) {
	FragmentManager manager = activity.getSupportFragmentManager();
	FragmentTransaction ft = manager.beginTransaction();
	ft.replace(R.id.content, newFragment);
	ft.commit();
    }
}
