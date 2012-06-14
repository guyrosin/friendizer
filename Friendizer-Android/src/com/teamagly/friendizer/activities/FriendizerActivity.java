package com.teamagly.friendizer.activities;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class FriendizerActivity extends SherlockFragmentActivity {
	private final String TAG = getClass().getName();
	ActionBar actionBar;
	ArrayList<Integer> tabs;

	// Provides access to the system location services
	private LocationManager locationManager;
	// Listener for network location updates
	private LocationListener networkLocationListener;
	// Listener for GPS location updates
	private LocationListener gpsLocationListener;
	private static final int ONE_MINUTE = 1000 * 60;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "FriendizerActivity onCreate!");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			buildAlertMessageNoGps();
		Location gpsLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location networkLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (isBetterLocation(gpsLastLocation, networkLastLocation))
			updateLocation(gpsLastLocation);
		else
			updateLocation(networkLastLocation);

		// Create a location listener that get updated by the network provider
		networkLocationListener = new ProviderLocationListener("Network");
		// Create a location listener that get updated by the GPS provider
		gpsLocationListener = new ProviderLocationListener("GPS");

		setTabs();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		try {
			// Register the listener with the Location Manager to receive location updates from the network provider
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ONE_MINUTE, 20, networkLocationListener);
			// Register the listener with the Location Manager to receive location updates from the GPS provider
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ONE_MINUTE, 20, gpsLocationListener);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.ActivityGroup#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		try {
			// Unregister the location listeners
			locationManager.removeUpdates(networkLocationListener);
			locationManager.removeUpdates(gpsLocationListener);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.ActivityGroup#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "FriendizerActivity onResume!");

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

	private void setTabs() {
		tabs = new ArrayList<Integer>();
		tabs.add(R.string.nearby);
		tabs.add(R.string.friends);
		tabs.add(R.string.my_profile);
		actionBar.addTab(actionBar
				.newTab()
				.setText(R.string.nearby)
				.setTabListener(
						new TabListener<PeopleRadarFragment>(this, getResources().getString(R.string.nearby),
								PeopleRadarFragment.class)));
		actionBar.addTab(actionBar
				.newTab()
				.setText(R.string.friends)
				.setTabListener(
						new TabListener<ConnectionsFragment>(this, getResources().getString(R.string.friends),
								ConnectionsFragment.class)));
		actionBar.addTab(actionBar
				.newTab()
				.setText(R.string.my_profile)
				.setTabListener(
						new TabListener<MyProfileFragment>(this, getResources().getString(R.string.my_profile),
								MyProfileFragment.class)));
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

	/**
	 * 
	 * Describe a ProviderLocationListener object: Used for receiving notifications from the LocationManager when the location has
	 * changed. provider - the provider of the location updates
	 * 
	 * @author Yarden Ron
	 * 
	 */
	private class ProviderLocationListener implements LocationListener {

		private String provider;

		/**
		 * 
		 * The constructor creates a ProviderLocationListener object
		 * 
		 * @param provider
		 *            - the provider of the location updates
		 */
		public ProviderLocationListener(String provider) {

			this.provider = provider;

		}

		@Override
		public void onLocationChanged(final Location newLocation) {
			updateLocation(newLocation);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null)
			// A new location is always better than no location
			return true;
		if (location == null)
			return false;

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
		boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	public void updateLocation(final Location newLocation) {
		// If the new location is better than the current one, update in the background
		if ((newLocation != null) && (isBetterLocation(newLocation, Utility.getInstance().location))) {
			new Thread(new Runnable() {
				public void run() {
					try {
						// Update the server with the new location
						ServerFacade.changeLocation(Utility.getInstance().userInfo.getId(), newLocation.getLatitude(),
								newLocation.getLongitude());
					} catch (Exception e) {
						Log.e(TAG, "Can't update the server with the new location", e);
					}

					// Display the new location information on the screen
					// Toast.makeText(getBaseContext(),"Location changed : (" + curLocation.getLatitude() + "," +
					// curLocation.getLongitude()+ ")	Provided by " + provider, Toast.LENGTH_SHORT).show();
				}
			}).start();
			// Save the new location
			Utility.getInstance().location = newLocation;
		}
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?").setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				}).setNegativeButton("Skip", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}
}