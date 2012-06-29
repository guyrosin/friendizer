/**
 * 
 */
package com.teamagly.friendizer;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class FriendizerApp extends Application {

	private static FriendizerApp instance;

	public FriendizerApp() {
		instance = this;
	}

	/**
	 * @return the app's context
	 */
	public static Context getContext() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		// Initialize the Image Loader
		ImageLoader imageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).threadPoolSize(5)
				.threadPriority(Thread.MAX_PRIORITY).build();
		imageLoader.init(config);
	}
}
