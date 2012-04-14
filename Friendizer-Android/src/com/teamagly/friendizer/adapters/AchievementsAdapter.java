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

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.Achievement;

/**
 * @author Guy
 * 
 */
public class AchievementsAdapter extends ArrayAdapter<Achievement> {
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
	holder.icon.setTag("badge_" + achievement.getIconRes());
	holder.title.setText(achievement.getTitle());
	holder.description.setText(achievement.getDescription());
	holder.reward.setText(achievement.getReward());
	return hView;
    }

    class ViewHolder {
	ImageView icon;
	TextView title;
	TextView description;
	TextView reward;
    }
}
