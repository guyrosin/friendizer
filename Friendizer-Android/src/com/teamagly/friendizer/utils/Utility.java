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
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.google.android.maps.GeoPoint;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.teamagly.friendizer.FriendizerApp;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.FriendsAdapter;
import com.teamagly.friendizer.filters.UserFilter;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.widgets.RangeSeekBar;
import com.teamagly.friendizer.widgets.RangeSeekBar.OnRangeSeekBarChangeListener;

public class Utility extends Application {

	public Facebook facebook;
	public AsyncFacebookRunner mAsyncRunner;
	public User userInfo;
	public LocationInfo locationInfo;

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
		locationInfo = new LocationInfo(FriendizerApp.getContext()); // Initialize the location with the latest known one
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
	public static final String PREFER_NEARBY_MAP = "prefer_nearby_map";

	/**
	 * Helper method to get a SharedPreferences instance.
	 */
	public static SharedPreferences getSharedPreferences() {
		return FriendizerApp.getContext().getSharedPreferences(SHARED_PREFS, 0);
	}

	public GeoPoint getLocation() {
		if (locationInfo == null)
			return null;
		int lat = (int) (locationInfo.lastLat * 1E6);
		int lng = (int) (locationInfo.lastLong * 1E6);
		return new GeoPoint(lat, lng);
	}

	/**
	 * Returns the package name of this class.
	 */
	public static String getThisPackageName() {
		return Utility.class.getPackage().getName();
	}

	/**
	 * Converts the given Drawable to a new greyscale Drawable
	 */
	public static Drawable convertToGrayscale(final Drawable drawable) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.setSaturation(0);
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
		Drawable d = drawable.mutate();
		d.setColorFilter(filter);
		return d;
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

		if (cursor.getCount() != 1)
			return -1;

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

	@SuppressWarnings("deprecation")
	public static String calcAge(String birthdayStr) {
		if (birthdayStr == null)
			return "";
		Calendar dob = Calendar.getInstance();
		dob.setTime(new Date(birthdayStr));
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

	public static void showFiltersDialog(Context context, final FriendsAdapter adapter) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View dialogLayout = inflater.inflate(R.layout.user_filter_dialog, null);
		// Create RangeSeekBar as Integer range between 0 and 100
		RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(0, 100, context);
		seekBar.setNotifyWhileDragging(true);

		UserFilter currentFilter = adapter.getCurrentFilter();
		TextView ageFromView = (TextView) dialogLayout.findViewById(R.id.age_from);
		ageFromView.setText(String.valueOf(currentFilter.getMinAgeValue()));
		seekBar.setSelectedMinValue(currentFilter.getMinAgeValue());
		TextView ageToView = (TextView) dialogLayout.findViewById(R.id.age_to);
		ageToView.setText(String.valueOf(currentFilter.getMaxAgeValue()));
		seekBar.setSelectedMaxValue(currentFilter.getMaxAgeValue());
		TextView nameView = (TextView) dialogLayout.findViewById(R.id.name_edit_text);
		nameView.setText(currentFilter.getName());
		RadioGroup genderRadio = (RadioGroup) dialogLayout.findViewById(R.id.gender_radio_group);
		if (currentFilter.getGender().equals("all"))
			genderRadio.check(R.id.gender_all);
		else if (currentFilter.getGender().equals("male"))
			genderRadio.check(R.id.gender_male);
		else
			genderRadio.check(R.id.gender_female);

		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
				// handle changed range values
				TextView ageFromView = (TextView) dialogLayout.findViewById(R.id.age_from);
				TextView ageToView = (TextView) dialogLayout.findViewById(R.id.age_to);
				ageFromView.setText(String.valueOf(minValue));
				ageToView.setText(String.valueOf(maxValue));
			}
		});
		// add RangeSeekBar to pre-defined layout
		ViewGroup layout = (ViewGroup) dialogLayout.findViewById(R.id.age_layout);
		layout.addView(seekBar);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(dialogLayout);
		builder.setNegativeButton("Reset", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				adapter.resetFilter();
				dialog.cancel();
			}
		});
		builder.setTitle("Filters").setPositiveButton("Filter", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				TextView ageFromView = (TextView) dialogLayout.findViewById(R.id.age_from);
				String ageFrom = ageFromView.getText().toString();
				TextView ageToView = (TextView) dialogLayout.findViewById(R.id.age_to);
				String ageTo = ageToView.getText().toString();
				TextView nameView = (TextView) dialogLayout.findViewById(R.id.name_edit_text);
				String name = nameView.getText().toString();
				RadioGroup genderRadio = (RadioGroup) dialogLayout.findViewById(R.id.gender_radio_group);
				RadioButton selectedButton = (RadioButton) dialogLayout.findViewById(genderRadio.getCheckedRadioButtonId());
				String gender = selectedButton.getText().toString();
				UserFilter filter = new UserFilter(name, gender, Integer.valueOf(ageFrom), Integer.valueOf(ageTo));
				adapter.filter(filter);
			}
		});
		builder.show();
	}
}
