package com.teamagly.friendizer.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.teamagly.friendizer.model.User;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;

public class Utility extends Application {

    public Facebook facebook;
    public AsyncFacebookRunner mAsyncRunner;
    public User userInfo;
    public ImageLoader imageLoader;
    public Location location;

    // private static int MAX_IMAGE_DIMENSION = 720;
    public static final String PREFS_NAME = "FriendizerPreferences";
    public static int DEFAULT_DISTANCE = 1000;
    public static final String APP_ID = "273844699335189";

    private Utility() {
	// Create the Facebook object using the app ID.
	facebook = new Facebook(APP_ID);
	// Instantiate the asyncrunner object for asynchronous api calls.
	mAsyncRunner = new AsyncFacebookRunner(facebook);
	imageLoader = new ImageLoader(this);
    }

    private static class SingletonHolder {
	public static final Utility instance = new Utility();
    }

    public static Utility getInstance() {
	return SingletonHolder.instance;
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
