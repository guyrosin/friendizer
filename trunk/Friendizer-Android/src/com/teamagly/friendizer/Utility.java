package com.teamagly.friendizer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class Utility extends Application {

    Facebook facebook;
    AsyncFacebookRunner mAsyncRunner;
    UserInfo userInfo;
    ImageLoader imageLoader;

    // private static int MAX_IMAGE_DIMENSION = 720;
    public static final String PREFS_NAME = "FriendizerPreferences";
    public static int DEFAULT_DISTANCE = 1000;

    private Utility() {
    }

    private static class SingletonHolder {
	public static final Utility instance = new Utility();
    }

    public static Utility getInstance() {
	return SingletonHolder.instance;
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
}
