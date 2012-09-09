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
import android.widget.ImageView;
import android.widget.TextView;

import com.teamagly.friendizer.model.Action;

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
		/*if (convertView == null) {
			hView = inflater.inflate(R.layout.action_item, null);
			ViewHolder holder = new ViewHolder();
			holder.icon = (ImageView) hView.findViewById(R.id.achievement_icon);
			holder.title = (TextView) hView.findViewById(R.id.achievement_title);
			holder.description = (TextView) hView.findViewById(R.id.achievement_description);
			holder.reward = (TextView) hView.findViewById(R.id.achievement_reward);
			hView.setTag(holder);
		}

		Action action = getItem(position);
		ViewHolder holder = (ViewHolder) hView.getTag();

		// Load the image resource
		String uri = "drawable/" + action.getIconRes();
		int imageResource = getContext().getResources().getIdentifier(uri, null, getContext().getPackageName());
		try {
			Drawable image = getContext().getResources().getDrawable(imageResource);
			if (!action.isEarned()) // If the achievement is earned, display a grayscale icon
				image = Utility.convertToGrayscale(image);
			holder.icon.setImageDrawable(image);
		} catch (NotFoundException e) { // The image wasn't found
			Log.e(TAG, e.getMessage());
		}

		holder.title.setText(action.getTitle());
		holder.description.setText(action.getDescription());
		holder.reward.setText(String.valueOf(action.getReward()));*/
		return hView;
	}

	class ViewHolder {
		ImageView icon;
		TextView title;
		TextView description;
		TextView reward;
	}
}
