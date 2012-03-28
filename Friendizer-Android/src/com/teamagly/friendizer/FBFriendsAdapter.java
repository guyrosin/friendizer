/**
 * 
 */
package com.teamagly.friendizer;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Guy
 * 
 */

public class FBFriendsAdapter extends FriendsAdapter {

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public FBFriendsAdapter(Context context, int textViewResourceId, List<UserInfo> objects) {
	super(context, textViewResourceId, objects);
    }

    /*
     * (non-Javadoc)
     * @see com.teamagly.friendizer.FriendsAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	View hView = convertView;
	if (convertView == null) {
	    hView = inflater.inflate(R.layout.friend_list_item, null);
	    ViewHolder holder = new ViewHolder();
	    holder.profile_pic = (ImageView) hView.findViewById(R.id.profile_pic);
	    holder.name = (TextView) hView.findViewById(R.id.name);
	    hView.setTag(holder);
	}

	UserInfo userInfo = getItem(position);
	ViewHolder holder = (ViewHolder) hView.getTag();
	Utility.getInstance().imageLoader.displayImage(userInfo.picURL, holder.profile_pic);
	holder.name.setText(userInfo.name);
	return hView;
    }

    class ViewHolder {
	ImageView profile_pic;
	TextView name;
    }

}
