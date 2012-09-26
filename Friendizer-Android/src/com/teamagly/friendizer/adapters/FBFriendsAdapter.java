/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;

public class FBFriendsAdapter extends FriendsAdapter {
	@SuppressWarnings("unused")
	private final String TAG = getClass().getName();

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public FBFriendsAdapter(Context context, int textViewResourceId, List<User> objects, MenuItem filterMenuItem) {
		super(context, textViewResourceId, objects, filterMenuItem);
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

		User userInfo = getItem(position);
		ViewHolder holder = (ViewHolder) hView.getTag();
		ImageLoader.getInstance().displayImage(userInfo.getPicURL(), holder.profile_pic);
		holder.name.setText(userInfo.getName());
		return hView;
	}

	class ViewHolder {
		ImageView profile_pic;
		TextView name;
	}

}
