/**
 * 
 */
package com.teamagly.friendizer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.Utility;

public class BaseFragmentActivity extends SherlockFragmentActivity {

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.blank_layout);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String fragmentName = getIntent().getStringExtra("fragment");
		SherlockFragment newFragment = null;
		if (fragmentName.equals(FBFriendsFragment.class.getName()))
			newFragment = new FBFriendsFragment();
		else if (fragmentName.equals(OwnsFragment.class.getName()))
			newFragment = new OwnsFragment();
		else if (fragmentName.equals(MutualLikesFragment.class.getName())) {
			newFragment = new MutualLikesFragment();
			Bundle bundle = new Bundle();
			bundle.putSerializable("user", getIntent().getSerializableExtra("user"));
			newFragment.setArguments(bundle);
		} else if (fragmentName.equals(LeaderboardFragment.class.getName()))
			newFragment = new LeaderboardFragment();
		else if (fragmentName.equals(BlockListFragment.class.getName()))
			newFragment = new BlockListFragment();
		else if (fragmentName.equals(ActionHistoryFragment.class.getName()))
			newFragment = new ActionHistoryFragment();

		if (newFragment != null) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.add(R.id.fragment_container, newFragment);
			transaction.commit();
		}
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
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
			// case R.id.menu_refresh:
			// onResume();
			// return true;
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
}
