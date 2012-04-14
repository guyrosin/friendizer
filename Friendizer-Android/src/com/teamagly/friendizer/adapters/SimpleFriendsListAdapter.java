/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.Utility;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    public SimpleFriendsListAdapter(Context context, int textViewResourceId, List<User> objects) {
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

	User userInfo = getItem(position);
	ViewHolder holder = (ViewHolder) hView.getTag();
	Utility.getInstance().imageLoader.displayImage(userInfo.getPicURL(), holder.profile_pic);
	holder.name.setText(userInfo.getName());
	return hView;
    }

    class ViewHolder {
	ImageView profile_pic;
	TextView name;
	TextView matching;
	TextView matchingTitle;
    }

}
