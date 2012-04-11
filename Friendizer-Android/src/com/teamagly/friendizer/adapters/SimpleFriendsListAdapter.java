/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.UserInfo;
import com.teamagly.friendizer.utils.Utility;

import android.content.Context;
import android.view.*;
import android.widget.*;

/**
 * @author Guy
 * 
 */
public class SimpleFriendsListAdapter extends FriendsAdapter {

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public SimpleFriendsListAdapter(Context context, int textViewResourceId, List<UserInfo> objects) {
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
		    holder.matching = (TextView) hView.findViewById(R.id.matching);
		    holder.matchingTitle = (TextView) hView.findViewById(R.id.matching_title);
		    // holder.online_presence = (TextView) hView.findViewById(R.id.online_presence);
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
		TextView matching;
		TextView matchingTitle;
    }

}
