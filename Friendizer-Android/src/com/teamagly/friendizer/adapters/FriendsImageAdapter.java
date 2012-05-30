/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.utils.ImageLoader.Type;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

public class FriendsImageAdapter extends FriendsAdapter {

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public FriendsImageAdapter(Context context, int textViewResourceId, List<User> objects) {
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
	    imageView.setPadding(3, 3, 3, 3);
	    imageView.setBackgroundResource(R.drawable.image_border);
	} else
	    imageView = (ImageView) convertView;

	User userInfo = getItem(position);
	Utility.getInstance().imageLoader.displayImage(userInfo.getPicURL(), imageView, Type.ROUND_CORNERS);
	return imageView;
    }

}
