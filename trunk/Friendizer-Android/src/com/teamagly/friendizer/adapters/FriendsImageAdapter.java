/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;

public class FriendsImageAdapter extends FriendsAdapter {
	@SuppressWarnings("unused")
	private final String TAG = getClass().getName();

	public FriendsImageAdapter(Context context, int textViewResourceId, List<User> objects, MenuItem filterMenuItem) {
		super(context, textViewResourceId, objects, filterMenuItem);
	}

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.FriendsAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null)
			imageView = (ImageView) inflater.inflate(R.layout.connection_grid_item, parent, false);
		else
			imageView = (ImageView) convertView;

		User userInfo = getItem(position);
		ImageLoader.getInstance().displayImage(userInfo.getPicURL(), imageView);
		return imageView;
	}

}
