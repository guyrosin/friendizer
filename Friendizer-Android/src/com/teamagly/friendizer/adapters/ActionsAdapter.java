/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.Action;
import com.teamagly.friendizer.utils.Utility;

public class ActionsAdapter extends ArrayAdapter<Action> {
	@SuppressWarnings("unused")
	private final String TAG = getClass().getName();
	protected static LayoutInflater inflater = null;

	public ActionsAdapter(Context context, int textViewResourceId, List<Action> objects) {
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
			holder.date = (TextView) hView.findViewById(R.id.date);
			holder.preTitle = (TextView) hView.findViewById(R.id.pre_title);
			holder.userBought = (TextView) hView.findViewById(R.id.user_bought);
			hView.setTag(holder);
		}

		Action action = getItem(position);
		ViewHolder holder = (ViewHolder) hView.getTag();

		if (action.getBoughtID() == Utility.getInstance().userInfo.getId())
			holder.preTitle.setText("Got bought by ");
		else
			holder.preTitle.setText("Bought ");
		holder.date.setText(action.getDate().toString());
		holder.userBought.setText(String.valueOf(action.getBoughtID()));
		return hView;
	}

	class ViewHolder {
		TextView date;
		TextView preTitle;
		TextView userBought;
	}
}
