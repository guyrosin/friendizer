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

public class FriendizerActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
	@SuppressWarnings("unused")
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
		// Try to restore the previous selected tab from the state/intent
		int selectedTab = 0;
		if (savedInstanceState != null)
			selectedTab = savedInstanceState.getInt("tab");
		if (selectedTab == 0) {
			selectedTab = getIntent().getIntExtra("tab", selectedTab);
			if (getIntent().getBooleanExtra("nearby_list", false))
				selectedTab = -1;
		}
		setTabs(selectedTab);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (getIntent().getBooleanExtra("nearby_list", false)) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, Fragment.instantiate(this, PeopleRadarFragment.class.getName()));
			ft.commit();
			getIntent().removeExtra("nearby_list"); // Reset
		} else {
			int tab = getIntent().getIntExtra("tab", 0);
			if (tab > 0) // Move to the given tab
				try {
					actionBar.setSelectedNavigationItem(tabs.indexOf(tab));
				} catch (ArrayIndexOutOfBoundsException e) {
				}
			getIntent().removeExtra("tab"); // Clear the intent
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Save the current tab
		int selectedIndex = actionBar.getSelectedNavigationIndex();
		if ((selectedIndex >= 0) && (selectedIndex < tabs.size()))
			outState.putInt("tab", tabs.get(selectedIndex));
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish(); // Quit
	}

	private void setTabs(int selectedTab) {
		tabs = new ArrayList<Integer>();
		tabs.add(R.string.nearby);
		tabs.add(R.string.friends);
		tabs.add(R.string.my_profile);
		if (selectedTab == 0) // Set the default tab
			selectedTab = R.string.nearby;

		// Create the tabs
		actionBar.addTab(actionBar.newTab().setText(R.string.nearby).setTabListener(this), (selectedTab == R.string.nearby));
		actionBar.addTab(
				actionBar
						.newTab()
						.setText(R.string.friends)
						.setTabListener(
								new TabListener<ConnectionsFragment>(this, getResources().getString(R.string.friends),
										ConnectionsFragment.class)), (selectedTab == R.string.friends));
		actionBar.addTab(
				actionBar
						.newTab()
						.setText(R.string.my_profile)
						.setTabListener(
								new TabListener<MyProfileFragment>(this, getResources().getString(R.string.my_profile),
										MyProfileFragment.class)), (selectedTab == R.string.my_profile));
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

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (mFragment == null) {
				mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
				ft.add(android.R.id.content, mFragment, mTag);
			} else
				ft.attach(mFragment);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null)
				ft.detach(mFragment);
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabSelected(com.actionbarsherlock.app.ActionBar.Tab,
	 * android.support.v4.app.FragmentTransaction)
	 */
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// Important: we use this listener only for the nearby tab!
		// (It's a necessary hack, since a MapFragment isn't possible ATM...)
		startActivity(new Intent(this, NearbyMapActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
		overridePendingTransition(0, 0);
		finish();
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabUnselected(com.actionbarsherlock.app.ActionBar.Tab,
	 * android.support.v4.app.FragmentTransaction)
	 */
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabReselected(com.actionbarsherlock.app.ActionBar.Tab,
	 * android.support.v4.app.FragmentTransaction)
	 */
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

}