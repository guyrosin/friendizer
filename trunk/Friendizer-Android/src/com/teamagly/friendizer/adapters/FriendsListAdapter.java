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

public class FriendsListAdapter extends FriendsAdapter {
	private final String TAG = getClass().getName();

	public FriendsListAdapter(Context context, int textViewResourceId, List<User> objects) {
		super(context, textViewResourceId, objects);
	}

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.adapters.FriendsAdapter#getView(int, android.view.View, android.view.ViewGroup)
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

		User userInfo = usersList.get(position);
		ViewHolder holder = (ViewHolder) hView.getTag();
		ImageLoader.getInstance().displayImage(userInfo.getPicURL(), holder.profile_pic);
		holder.name.setText(userInfo.getName());
		holder.gender.setText(userInfo.getGender());
		holder.age.setText(userInfo.getAge());
		if (userInfo.getAge().length() == 0)
			holder.ageTitle.setText("");
		if (userInfo.getPoints() > 0) // If count==0 don't show it (it means the user object still isn't loaded)
			holder.value.setText(String.valueOf(userInfo.getPoints()));
		if (userInfo.getPoints() > 0) // If matching==0 don't show it
			holder.matching.setText(String.valueOf(userInfo.getMatching()));
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
