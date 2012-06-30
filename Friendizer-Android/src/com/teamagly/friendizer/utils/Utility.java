package com.teamagly.friendizer.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.teamagly.friendizer.FriendizerApp;
import com.teamagly.friendizer.model.User;

public class Utility extends Application {

	public Facebook facebook;
	public AsyncFacebookRunner mAsyncRunner;
	public User userInfo;
	public Location location;
	// Provides access to the system location services
	private LocationManager locationManager;
	// Listener for network location updates
	private LocationListener networkLocationListener;
	// Listener for GPS location updates
	private LocationListener gpsLocationListener;
	private static final int ONE_MINUTE = 1000 * 60;

	public static final String APP_ID = "273844699335189"; // Facebook app ID
	public static final String SENDER_ID = "386379587736"; // GCM sender ID

	public static final String USER_ID = "userID"; // Key for user ID
	// An intent name for receiving registration/unregistration status.
	public static final String REGISTRATION_INTENT = getThisPackageName() + ".UPDATE_UI";
	public static final String AUTH_PERMISSION_ACTION = getThisPackageName() + ".AUTH_PERMISSION";

	private Utility() {
		// Create the Facebook object using the app ID.
		facebook = new Facebook(APP_ID);
		// Instantiate the asyncrunner object for asynchronous api calls.
		mAsyncRunner = new AsyncFacebookRunner(facebook);
	}

	private static class SingletonHolder {
		public static final Utility instance = new Utility();
	}

	public static Utility getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Key for shared preferences.
	 */
	private static final String SHARED_PREFS = "friendizer".toUpperCase(Locale.ENGLISH) + "_PREFS";

	/**
	 * Helper method to get a SharedPreferences instance.
	 */
	public static SharedPreferences getSharedPreferences() {
		return FriendizerApp.getContext().getSharedPreferences(SHARED_PREFS, 0);
	}

	public void initLocation(Context context) {
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			buildAlertMessageNoGPS(context);
		Location gpsLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location networkLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (isBetterLocation(gpsLastLocation, networkLastLocation))
			updateLocation(gpsLastLocation);
		else
			updateLocation(networkLastLocation);

		// Create a location listener that get updated by the network provider
		networkLocationListener = new ProviderLocationListener();
		// Create a location listener that get updated by the GPS provider
		gpsLocationListener = new ProviderLocationListener();

		try {
			// Register the listener with the Location Manager to receive location updates from the network provider
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ONE_MINUTE / 2, 20, networkLocationListener);
			// Register the listener with the Location Manager to receive location updates from the GPS provider
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ONE_MINUTE * 2, 20, gpsLocationListener);
		} catch (Exception e) {
			Log.e("", e.getMessage());
		}
	}

	public void stopLocation() {
		try {
			// Unregister the location listeners
			locationManager.removeUpdates(networkLocationListener);
			locationManager.removeUpdates(gpsLocationListener);
		} catch (Exception e) {
			Log.e("", e.getMessage());
		}
	}

	/**
	 * Receives notifications from the LocationManager when the location has changed.
	 */
	private class ProviderLocationListener implements LocationListener {

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
						Log.e("", "Can't update the server with the new location", e);
					}
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

	private void buildAlertMessageNoGPS(Context context) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

	/**
	 * Returns the package name of this class.
	 */
	public static String getThisPackageName() {
		return Utility.class.getPackage().getName();
	}

	/**
	 * Converts the given Drawable (image) to greyscale
	 */
	public static Drawable convertToGrayscale(Drawable drawable) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.setSaturation(0);
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
		drawable.setColorFilter(filter);
		return drawable;
	}

	/**
	 * Lets the user choose an email client and send us feedback
	 */
	public static boolean startFeedback(Context context) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { "friendizer.team@gmail.com" });
		i.putExtra(Intent.EXTRA_SUBJECT, "Friendizer Feedback");
		i.setType("message/rfc822");
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(Intent.createChooser(i, "Choose an Email client:"));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(context, "There are no email clients installed", Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	public static int getOrientation(Context context, Uri photoUri) {
		/* it's on the external media. */
		Cursor cursor = context.getContentResolver().query(photoUri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
				null, null, null);

		if (cursor.getCount() != 1) {
			return -1;
		}

		cursor.moveToFirst();
		return cursor.getInt(0);
	}

	// Taken from LazyList
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	public static String calcAge(Date birthday) {
		Calendar dob = Calendar.getInstance();
		dob.setTime(birthday);
		Calendar today = Calendar.getInstance();
		int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
		if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR))
			age--;
		return String.valueOf(age);
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		int pixels = 4;
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

}
