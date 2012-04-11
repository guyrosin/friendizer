/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.R.id;
import com.teamagly.friendizer.R.layout;
import com.teamagly.friendizer.model.UserInfo;
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
public class FriendsListAdapter extends FriendsAdapter {

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public FriendsListAdapter(Context context, int textViewResourceId, List<UserInfo> objects) {
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
	    hView = inflater.inflate(R.layout.connection_list_item, null);
	    ViewHolder holder = new ViewHolder();
	    holder.profile_pic = (ImageView) hView.findViewById(R.id.profile_pic);
	    holder.name = (TextView) hView.findViewById(R.id.name);
	    holder.gender = (TextView) hView.findViewById(R.id.gender);
	    holder.age = (TextView) hView.findViewById(R.id.age);
	    holder.ageTitle = (TextView) hView.findViewById(R.id.age_title);
	    holder.value = (TextView) hView.findViewById(R.id.value);
	    holder.valueTitle = (TextView) hView.findViewById(R.id.value_title);
	    // holder.online_presence = (TextView) hView.findViewById(R.id.online_presence);
	    hView.setTag(holder);
	}

	UserInfo userInfo = getItem(position);
	ViewHolder holder = (ViewHolder) hView.getTag();
	Utility.getInstance().imageLoader.displayImage(userInfo.picURL, holder.profile_pic);
	holder.name.setText(userInfo.name);
	holder.gender.setText(userInfo.gender);
	holder.age.setText(userInfo.age);
	if (userInfo.age.length() == 0)
	    holder.ageTitle.setText("");
	if (userInfo.value > 0) // If value==0 don't show it (it means the user object still isn't loaded)
	    holder.value.setText(String.valueOf(userInfo.value));
	return hView;
    }

    class ViewHolder {
	ImageView profile_pic;
	TextView name;
	TextView gender;
	TextView age;
	TextView ageTitle;
	TextView value;
	TextView valueTitle;
    }

}
