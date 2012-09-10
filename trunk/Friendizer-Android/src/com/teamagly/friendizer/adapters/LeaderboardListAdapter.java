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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;

public class LeaderboardListAdapter extends FriendsAdapter {
	@SuppressWarnings("unused")
	private final String TAG = getClass().getName();
	private String leaderboardType;

	public LeaderboardListAdapter(Context context, int textViewResourceId, List<User> objects, String leaderboardType) {
		super(context, textViewResourceId, objects);
		this.leaderboardType = leaderboardType;
	}

	public void setLeaderboardType(String leaderboardType) {
		this.leaderboardType = leaderboardType;
	}

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.adapters.FriendsAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View hView = convertView;
		if (convertView == null) {
			hView = inflater.inflate(R.layout.leaderboard_list_item, null);
			ViewHolder holder = new ViewHolder();
			holder.profile_pic = (ImageView) hView.findViewById(R.id.profile_pic);
			holder.name = (TextView) hView.findViewById(R.id.name);
			holder.field = (TextView) hView.findViewById(R.id.field);
			holder.fieldTitle = (TextView) hView.findViewById(R.id.field_title);
			hView.setTag(holder);
		}

		User userInfo = getItem(position);
		ViewHolder holder = (ViewHolder) hView.getTag();
		ImageLoader.getInstance().displayImage(userInfo.getPicURL(), holder.profile_pic);
		holder.name.setText(userInfo.getName());

		String field = "", fieldTitle = "";
		if (leaderboardType.equals("points")) {
			fieldTitle = "Points: ";
			field = String.valueOf(userInfo.getPoints());
		} else if (leaderboardType.equals("money")) {
			fieldTitle = "Money: ";
			field = String.valueOf(userInfo.getMoney());
		}
		holder.fieldTitle.setText(fieldTitle);
		holder.field.setText(field);
		return hView;
	}

	class ViewHolder {
		ImageView profile_pic;
		TextView name;
		TextView field;
		TextView fieldTitle;
	}
}
