/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.ActionInfo;
import com.teamagly.friendizer.model.User;

public class ActionsAdapter extends ArrayAdapter<ActionInfo> {
	@SuppressWarnings("unused")
	private final String TAG = getClass().getName();
	protected static LayoutInflater inflater = null;

	public ActionsAdapter(Context context, int textViewResourceId, List<ActionInfo> objects) {
		super(context, textViewResourceId, objects);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View hView = convertView;
		if (convertView == null) {
			hView = inflater.inflate(R.layout.action_list_item, null);
			ViewHolder holder = new ViewHolder();
			holder.pic = (ImageView) hView.findViewById(R.id.profile_pic);
			holder.date = (TextView) hView.findViewById(R.id.date);
			holder.preTitle = (TextView) hView.findViewById(R.id.pre_title);
			holder.userBought = (TextView) hView.findViewById(R.id.title);
			hView.setTag(holder);
		}

		ActionInfo actionInfo = getItem(position);
		ViewHolder holder = (ViewHolder) hView.getTag();

		User userInfo = getItem(position).getUser();
		ImageLoader.getInstance().displayImage(userInfo.getPicURL(), holder.pic);
		if (actionInfo.isYouBoughtHim())
			holder.preTitle.setText("Bought ");
		else
			holder.preTitle.setText("Got bought by ");
		holder.date.setText(DateFormat.format("yyyy-MM-dd hh:mm", actionInfo.getDate()));
		holder.userBought.setText(String.valueOf(actionInfo.getUser().getName()));
		return hView;
	}

	class ViewHolder {
		ImageView pic;
		TextView date;
		TextView preTitle;
		TextView userBought;
	}
}
