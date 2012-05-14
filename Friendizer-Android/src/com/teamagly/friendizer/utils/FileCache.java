package com.teamagly.friendizer.utils;

import java.io.File;
import android.content.Context;

public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
	try {
	    // Find the dir to save cached images
	    if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "friendizer");
	    else
		cacheDir = context.getCacheDir();
	    if (!cacheDir.exists())
		cacheDir.mkdirs();
	} catch (Exception e) {
	    cacheDir = null;
	}
    }

    public File getFile(String url) {
	if (cacheDir == null)
	    return null;
	// I identify images by hashcode. Not a perfect solution, good for the demo.
	String filename = String.valueOf(url.hashCode());
	// Another possible solution (thanks to grantland)
	// String filename = URLEncoder.encode(url);
	File f = new File(cacheDir, filename);
	return f;

    }

    public void clear() {
	File[] files = cacheDir.listFiles();
	if (files == null)
	    return;
	for (File f : files)
	    f.delete();
    }

}