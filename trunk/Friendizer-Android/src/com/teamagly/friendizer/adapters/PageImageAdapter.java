/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.Page;

public class PageImageAdapter extends ArrayAdapter<Page> {
	@SuppressWarnings("unused")
	private final String TAG = getClass().getName();
	protected LayoutInflater inflater;
	protected List<Page> pagesList;

	public PageImageAdapter(Context context, int textViewResourceId, List<Page> objects) {
		super(context, textViewResourceId, objects);
		pagesList = objects;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		Page page = getItem(position);
		ImageLoader.getInstance().displayImage(page.getPicURL(), imageView);
		return imageView;
	}

}
