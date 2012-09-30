/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.Achievement;

public class AchievementsAdapter extends ArrayAdapter<Achievement> {
	private final String TAG = getClass().getName();
	protected static LayoutInflater inflater = null;

	public AchievementsAdapter(Context context, int textViewResourceId, List<Achievement> objects) {
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
			hView = inflater.inflate(R.layout.achievements_list_item, null);
			ViewHolder holder = new ViewHolder();
			holder.icon = (ImageView) hView.findViewById(R.id.achievement_icon);
			holder.title = (TextView) hView.findViewById(R.id.achievement_title);
			holder.description = (TextView) hView.findViewById(R.id.achievement_description);
			holder.reward = (TextView) hView.findViewById(R.id.achievement_reward);
			hView.setTag(holder);
		}

		Achievement achievement = getItem(position);
		ViewHolder holder = (ViewHolder) hView.getTag();

		// Load the image resource
		String uri = "drawable/" + achievement.getIconRes();
		int imageResource = getContext().getResources().getIdentifier(uri, null, getContext().getPackageName());
		try {
			Drawable image = getContext().getResources().getDrawable(imageResource);
			// if (!achievement.isEarned()) // If the achievement is earned, display a grayscale icon
			// image = Utility.convertToGrayscale(image);
			holder.icon.setImageDrawable(image);
		} catch (NotFoundException e) { // The image wasn't found
			Log.e(TAG, e.getMessage());
		}

		String titleWithEarned = "";
		if (achievement.isEarned())
			titleWithEarned = "<b><font color='#00CC00'>&#10003;<font></b> " + achievement.getTitle();
		else
			titleWithEarned = "<b><font color='#5C0000'>&#10006;<font></b> " + achievement.getTitle();
		holder.title.setText(Html.fromHtml(titleWithEarned));

		holder.description.setText(achievement.getDescription());
		holder.reward.setText(String.valueOf(achievement.getReward()));
		return hView;
	}

	class ViewHolder {
		ImageView icon;
		TextView title;
		TextView description;
		TextView reward;
	}
}
