package com.teamagly.friendizer;

import com.teamagly.friendizer.R;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

/**
 * @author Guy
 * 
 */
public class FriendizerActivity extends TabActivity {

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

	// Register the listener with the Location Manager to receive location updates from the network provider
	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, networkLocationListener);
	// Register the listener with the Location Manager to receive location updates from the GPS provider
	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, gpsLocationListener);

	TabHost tabHost = getTabHost();

	// Tab for People Radar
	TabSpec peopleRadarSpec = tabHost.newTabSpec("People Radar");
	peopleRadarSpec.setIndicator("People Radar", getResources().getDrawable(R.drawable.icon_peopleradar_tab));
	Intent peopleRadarIntent = new Intent(this, PeopleRadarActivity.class);
	peopleRadarSpec.setContent(peopleRadarIntent);

	// Tab for Connections
	TabSpec connectionsSpec = tabHost.newTabSpec("Connections");
	connectionsSpec.setIndicator("Connections", getResources().getDrawable(R.drawable.icon_friends_tab));
	Intent connectionsIntent = new Intent(this, ConnectionsActivity.class);
	connectionsSpec.setContent(connectionsIntent);

	// Tab for My Profile
	TabSpec myProfileSpec = tabHost.newTabSpec("My Profile");
	myProfileSpec.setIndicator("My Profile", getResources().getDrawable(R.drawable.icon_myprofile_tab));
	Intent myProfileIntent = new Intent(this, MyProfileActivity.class);
	myProfileSpec.setContent(myProfileIntent);

	// Adding all TabSpec to TabHost
	tabHost.addTab(peopleRadarSpec); // Adding People Radar tab
	tabHost.addTab(connectionsSpec); // Adding Friends tab
	tabHost.addTab(myProfileSpec); // Adding My Profile tab
    }

    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main_menu, menu);
	return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
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

	    /*
	     * If there is a new location
	     */
	    if (location != null) {
		try {
		    // Update the server with the new location
		    ServerFacade.changeLocation(Utility.getInstance().userInfo.getId(), location.getLatitude(), location.getLongitude());
		} catch (Exception e) {
		    System.out.println("Can't update the server with the new location");
		}

		// Display the new location information on the screen
		Toast.makeText(
			getBaseContext(),
			"Location changed : (" + location.getLatitude() + "," + location.getLongitude() + ")	Provided by "
				+ provider, Toast.LENGTH_SHORT).show();
	    }
	}

	@Override
	public void onProviderDisabled(String provider) {
	    // TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
	    // TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	    // TODO Auto-generated method stub
	}
    }

}