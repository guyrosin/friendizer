package com.teamagly.friendizer.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.Utility;

public class FriendizerActivity extends SherlockFragmentActivity {
	private final String TAG = getClass().getName();
	ActionBar actionBar;
	ArrayList<Integer> tabs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);

		setTabs();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.ActivityGroup#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		int tab = getIntent().getIntExtra("tab", 0);
		if (tab > 0) { // Move to the given tab
			for (int i = 0; i < tabs.size(); i++)
				if (tabs.get(i) == tab)
					actionBar.setSelectedNavigationItem(i);
		}

		// TODO use in the background (and not here)
		// if (!Utility.getInstance().facebook.isSessionValid()) {
		// Util.showAlert(this, "Warning", "You must first log in.");
		// }
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// Stop the location polling and quit
		Utility.getInstance().stopLocation();
		finish();
	}

	private void setTabs() {
		tabs = new ArrayList<Integer>();
		tabs.add(R.string.nearby);
		tabs.add(R.string.friends);
		tabs.add(R.string.my_profile);
		int tab = getIntent().getIntExtra("tab", 0); // Get the selected tab (if exists)
		if (tab == 0) // Set the default tab
			tab = R.string.nearby;

		// Create the tabs
		actionBar.addTab(
				actionBar
						.newTab()
						.setText(R.string.nearby)
						.setTabListener(
								new TabListener<PeopleRadarFragment>(this, getResources().getString(R.string.nearby),
										PeopleRadarFragment.class)), (tab == R.string.nearby));
		actionBar.addTab(
				actionBar
						.newTab()
						.setText(R.string.friends)
						.setTabListener(
								new TabListener<ConnectionsFragment>(this, getResources().getString(R.string.friends),
										ConnectionsFragment.class)), (tab == R.string.friends));
		actionBar.addTab(
				actionBar
						.newTab()
						.setText(R.string.my_profile)
						.setTabListener(
								new TabListener<MyProfileFragment>(this, getResources().getString(R.string.my_profile),
										MyProfileFragment.class)), (tab == R.string.my_profile));
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_feedback:
			return Utility.startFeedback(this);
		case R.id.menu_settings: // Move to the settings activity
			startActivity(new Intent(this, FriendsPrefs.class));
			return true;
		case R.id.menu_invite: // Show the Facebook invitation dialog
			Bundle params = new Bundle();
			params.putString("message", getString(R.string.invitation_msg));
			Utility.getInstance().facebook.dialog(this, "apprequests", params, new BaseDialogListener());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
		private final SherlockFragmentActivity mActivity;
		private final String mTag;
		private final Class<T> mClass;
		private final Bundle mArgs;
		private Fragment mFragment;

		public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
			this(activity, tag, clz, null);
		}

		public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz, Bundle args) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
			mArgs = args;

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state. If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
			if (mFragment != null && !mFragment.isDetached()) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
				ft.detach(mFragment);
				ft.commit();
			}
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (mFragment == null) {
				mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
				ft.add(android.R.id.content, mFragment, mTag);
			} else
				ft.attach(mFragment);
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null)
				ft.detach(mFragment);
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	}

}