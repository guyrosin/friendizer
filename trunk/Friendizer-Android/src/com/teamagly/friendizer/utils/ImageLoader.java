package com.teamagly.friendizer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;

public class ImageLoader {

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;

    BaseAdapter listener;
    int runningCount = 0;
    Stack<PhotoToLoad> queue;
    /*
     * 15 max async tasks at any given time.
     */
    final static int MAX_ALLOWED_TASKS = 15;

    public enum Type {
	REGULAR, ROUND_CORNERS
    }

    public ImageLoader(Context context) {
	fileCache = new FileCache(context);
	queue = new Stack<PhotoToLoad>();
    }

    /*
     * Inform the listener when the image has been downloaded.
     */
    public void setListener(BaseAdapter listener) {
	this.listener = listener;
	reset();
    }

    /**
     * Fetches the image, from the cache if possible
     * 
     * @param url
     *            URL of an image
     */
    public Bitmap getImage(String url) {
	return getImage(url, Type.REGULAR);
    }

    /**
     * Fetches the image, from the cache if possible
     * 
     * @param url
     *            URL of an image
     * @param imageView
     * @param type
     *            whether to show the original image or not
     */
    public Bitmap getImage(String url, Type type) {
	// Check if it exists in the cache
	Bitmap bitmap = memoryCache.get(url);
	if (bitmap != null) {
	    if (type == Type.ROUND_CORNERS)
		bitmap = Utility.getRoundedCornerBitmap(bitmap);
	    return bitmap;
	} else {
	    if (runningCount >= MAX_ALLOWED_TASKS) {
		queue.push(new PhotoToLoad(url, type));
	    } else {
		runningCount++;
		new GetProfilePicAsyncTask().execute(url, type);
	    }
	}
	return null;
    }

    private Bitmap getBitmap(String url) {
	File f = fileCache.getFile(url);

	if (f == null) // If the cache is unavailable, just fetch the image and return it
	    try {
		URL newurl = new URL(url);
		return BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
	    } catch (Exception e) {
		return null;
	    }

	// from SD cache
	Bitmap b = decodeFile(f);
	if (b != null)
	    return b;

	// from web
	try {
	    Bitmap bitmap = null;
	    URL imageUrl = new URL(url);
	    HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
	    conn.setConnectTimeout(30000);
	    conn.setReadTimeout(30000);
	    conn.setInstanceFollowRedirects(true);
	    InputStream is = conn.getInputStream();
	    OutputStream os = new FileOutputStream(f);
	    Utility.CopyStream(is, os);
	    os.close();
	    bitmap = decodeFile(f);
	    return bitmap;
	} catch (Exception ex) {
	    Log.e("ImageLoader", "", ex);
	    ex.printStackTrace();
	    return null;
	}
    }

    // decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
	try {
	    // decode image size
	    BitmapFactory.Options o = new BitmapFactory.Options();
	    o.inJustDecodeBounds = true;
	    BitmapFactory.decodeStream(new FileInputStream(f), null, o);

	    // Find the correct scale value. It should be the power of 2.
	    final int REQUIRED_SIZE = 70;
	    int width_tmp = o.outWidth, height_tmp = o.outHeight;
	    int scale = 1;
	    while (true) {
		if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
		    break;
		width_tmp /= 2;
		height_tmp /= 2;
		scale *= 2;
	    }

	    // decode with inSampleSize
	    BitmapFactory.Options o2 = new BitmapFactory.Options();
	    o2.inSampleSize = scale;
	    return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	} catch (FileNotFoundException e) {
	}
	return null;
    }

    public void getNextImage() {
	if (!queue.isEmpty()) {
	    PhotoToLoad item = queue.pop();
	    new GetProfilePicAsyncTask().execute(item.url, item.type);
	}
    }

    /*
     * Start a AsyncTask to fetch the request
     */
    private class GetProfilePicAsyncTask extends AsyncTask<Object, Void, Bitmap> {
	String url;
	Type type;

	@Override
	protected Bitmap doInBackground(Object... params) {
	    url = (String) params[0];
	    type = (Type) params[1];
	    Bitmap bitmap = getBitmap(url);
	    return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
	    runningCount--;
	    if (result != null) {
		memoryCache.put(url, result); // Note we put the original version in the cache
		if (type == Type.ROUND_CORNERS)
		    result = Utility.getRoundedCornerBitmap(result);
		listener.notifyDataSetChanged();
		getNextImage();
	    }
	}
    }

    // Task for the queue
    private class PhotoToLoad {
	public String url;
	public Type type;

	public PhotoToLoad(String u, Type t) {
	    url = u;
	    type = t;
	}
    }

    public void reset() {
	runningCount = 0;
	queue.clear();
    }

    public void clearCache() {
	memoryCache.clear();
	fileCache.clear();
    }
}
