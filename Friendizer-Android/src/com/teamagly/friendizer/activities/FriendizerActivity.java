package com.teamagly.friendizer.activities;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.widgets.ActionBar;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class FriendizerActivity extends TabActivity {
    private final String TAG = getClass().getName();
    private TabHost tabHost;
    ActionBar actionBar;

    // Provides access to the system location services
    private LocationManager locationManager;
    // Listener for network location updates
    private LocationListener networkLocationListener;
    // Listener for GPS location updates
    private LocationListener gpsLocationListener;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	// Acquire a reference to the system Location Manager
	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

	// Create a location listener that get updated by the network provider
	networkLocationListener = new ProviderLocationListener("Network");
	// Create a location listener that get updated by the GPS provider
	gpsLocationListener = new ProviderLocationListener("GPS");

	actionBar = (ActionBar) findViewById(R.id.actionbar);
	actionBar.mRefreshBtn.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		refreshCurrentTab();
	    }
	});

	setTabs();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
	super.onStart();
	// Register the listener with the Location Manager to receive location updates from the network provider
	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, networkLocationListener);
	// Register the listener with the Location Manager to receive location updates from the GPS provider
	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, gpsLocationListener);

    }

    /*
     * (non-Javadoc)
     * @see android.app.ActivityGroup#onStop()
     */
    @Override
    protected void onStop() {
	super.onStop();
	// Unregister the location listeners
	locationManager.removeUpdates(networkLocationListener);
	locationManager.removeUpdates(gpsLocationListener);
    }

    /*
     * (non-Javadoc)
     * @see android.app.ActivityGroup#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();

	int to = getIntent().getIntExtra("to", 0);
	if (to != 0) {
	    Intent i = new Intent().setClass(FriendizerActivity.this, getClass(to));
	    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    getLocalActivityManager().startActivity(getString(to), i);
	}

	// TODO use in the background (and not here)
	// if (!Utility.getInstance().facebook.isSessionValid()) {
	// Util.showAlert(this, "Warning", "You must first log in.");
	// }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
	// Check for a loading message
	if (intent.hasExtra("loading")) {
	    if (intent.getBooleanExtra("loading", false))
		actionBar.setProgressBarVisibility(View.VISIBLE);
	    else
		actionBar.setProgressBarVisibility(View.GONE);
	}
    }

    private Class<?> getClass(int tag) {
	if (tag == R.string.people_radar)
	    return PeopleRadarActivity.class;
	if (tag == R.string.connections)
	    return ConnectionsActivity.class;
	if (tag == R.string.my_profile)
	    return MyProfileActivity.class;
	return null;
    }

    private void setTabs() {
	tabHost = getTabHost();

	addTab(R.string.people_radar, R.drawable.icon_peopleradar_tab, PeopleRadarActivity.class);
	addTab(R.string.connections, R.drawable.icon_friends_tab, ConnectionsActivity.class);
	addTab(R.string.my_profile, R.drawable.icon_myprofile_tab, MyProfileActivity.class);
    }

    private void addTab(int labelId, int drawableId, Class<?> target) {
	Intent intent = new Intent(this, target);
	TabHost.TabSpec spec = tabHost.newTabSpec(getString(labelId));

	View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);

	TextView title = (TextView) tabIndicator.findViewById(R.id.title);
	title.setText(labelId);
	ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
	icon.setImageResource(drawableId);

	spec.setIndicator(tabIndicator);
	spec.setContent(intent);
	tabHost.addTab(spec);
    }

    protected void refreshCurrentTab() {
	// Tell the current child activity to refresh
	Intent i = new Intent().setClass(FriendizerActivity.this, getCurrentActivity().getClass());
	i.putExtra("refresh", true);
	i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	getLocalActivityManager().startActivity(tabHost.getCurrentTabTag(), i);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main_menu, menu);
	return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.refresh:
	    refreshCurrentTab();
	    return true;
	case R.id.settings_title:
	    startActivity(new Intent(this, FriendsPrefs.class));
	    return true;
	case R.id.invite:
	    Bundle params = new Bundle();
	    params.putString("message", getString(R.string.invitation_msg));
	    Utility.getInstance().facebook.dialog(this, "apprequests", params, new BaseDialogListener());
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
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
	public void onLocationChanged(Location location) {

	    final Location curLocation = location;
	    /*
	     * If there is a new location, update in the background
	     */
	    if (location != null) {
		new Thread(new Runnable() {
		    public void run() {
			try {
			    // Update the server with the new location
			    ServerFacade.changeLocation(Utility.getInstance().userInfo.getId(), curLocation.getLatitude(),
				    curLocation.getLongitude());
			} catch (Exception e) {
			    Log.e(TAG, "Can't update the server with the new location", e);
			}

			// Display the new location information on the screen
			// Toast.makeText(getBaseContext(),"Location changed : (" + curLocation.getLatitude() + "," +
			// curLocation.getLongitude()+ ")	Provided by " + provider, Toast.LENGTH_SHORT).show();
		    }
		}).start();
	    }
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

}