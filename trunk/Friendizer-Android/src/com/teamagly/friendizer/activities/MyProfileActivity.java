package com.teamagly.friendizer.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.widgets.SegmentedRadioGroup;

/**
 * @author Guy
 * 
 */
public class MyProfileActivity extends FragmentActivity implements OnCheckedChangeListener {
    SegmentedRadioGroup segmentedControl;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.profile_layout);
	segmentedControl = (SegmentedRadioGroup) findViewById(R.id.segment_text);
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
    protected void navigateTo(Fragment newFragment) {
	FragmentManager manager = getSupportFragmentManager();
	FragmentTransaction ft = manager.beginTransaction();
	ft.replace(R.id.content, newFragment);
	ft.commit();
    }
}
