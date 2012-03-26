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
public class FriendsListAdapter extends FriendsAdapter {

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public FriendsListAdapter(Context context, int textViewResourceId, List<FBUserInfo> objects) {
	super(context, textViewResourceId, objects);
	// TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * @see com.teamagly.friendizer.FriendsAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	View hView = convertView;
	if (convertView == null) {
	    hView = inflater.inflate(R.layout.connection_list_item, null);
	    ViewHolder holder = new ViewHolder();
	    holder.profile_pic = (ImageView) hView.findViewById(R.id.profile_pic);
	    holder.name = (TextView) hView.findViewById(R.id.name);
	    holder.gender = (TextView) hView.findViewById(R.id.gender);
	    holder.age = (TextView) hView.findViewById(R.id.age);
	    holder.ageTitle = (TextView) hView.findViewById(R.id.age_title);
	    // holder.online_presence = (TextView) hView.findViewById(R.id.online_presence);
	    hView.setTag(holder);
	}

	FBUserInfo userInfo = getItem(position);
	ViewHolder holder = (ViewHolder) hView.getTag();
	Utility.getInstance().imageLoader.displayImage(userInfo.picURL, holder.profile_pic);
	holder.name.setText(userInfo.name);
	holder.gender.setText(userInfo.gender);
	holder.age.setText(userInfo.age);
	if (userInfo.age.length() == 0)
	    holder.ageTitle.setText("");
	return hView;
    }

    class ViewHolder {
	ImageView profile_pic;
	TextView name;
	TextView gender;
	TextView age;
	TextView ageTitle;
    }

}
