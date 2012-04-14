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
public class FriendsListAdapter extends FriendsAdapter {

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public FriendsListAdapter(Context context, int textViewResourceId, List<User> objects) {
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
	    holder.matching = (TextView) hView.findViewById(R.id.matching);
	    // holder.online_presence = (TextView) hView.findViewById(R.id.online_presence);
	    hView.setTag(holder);
	}

	User userInfo = getItem(position);
	ViewHolder holder = (ViewHolder) hView.getTag();
	Utility.getInstance().imageLoader.displayImage(userInfo.getPicURL(), holder.profile_pic);
	holder.name.setText(userInfo.getName());
	holder.gender.setText(userInfo.getGender());
	holder.age.setText(userInfo.getAge());
	if (userInfo.getAge().length() == 0)
	    holder.ageTitle.setText("");
	if (userInfo.getValue() > 0) // If value==0 don't show it (it means the user object still isn't loaded)
	    holder.value.setText(String.valueOf(userInfo.getValue()));
//	holder.matching.setText(String.valueOf(userInfo.getMatching()));
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
	TextView matching;
    }
}
