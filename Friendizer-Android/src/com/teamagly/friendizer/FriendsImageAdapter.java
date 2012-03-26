/**
 * 
 */
package com.teamagly.friendizer;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * @author Guy
 * 
 */
public class FriendsImageAdapter extends FriendsAdapter {

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public FriendsImageAdapter(Context context, int textViewResourceId, List<FBUserInfo> objects) {
	super(context, textViewResourceId, objects);
    }

    /*
     * (non-Javadoc)
     * @see com.teamagly.friendizer.FriendsAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	ImageView imageView;
	if (convertView == null) { // if it's not recycled, initialize some attributes
	    imageView = new ImageView(getContext());
	    imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
	    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	    imageView.setPadding(5, 5, 5, 5);
	} else
	    imageView = (ImageView) convertView;

	FBUserInfo user = getItem(position);
	Utility.getInstance().imageLoader.displayImage(user.picURL, imageView);
	return imageView;
    }

}
