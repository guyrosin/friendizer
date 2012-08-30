package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.GiftCount;

public class GiftsUserAdapter extends ArrayAdapter<GiftCount> {
	private final String TAG = getClass().getName();

	protected LayoutInflater inflater;
	protected List<GiftCount> giftsList;

	public GiftsUserAdapter(Context context, int textViewResourceId, List<GiftCount> objects) {
		super(context, textViewResourceId, objects);
		giftsList = objects;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View hView = convertView;
		if (convertView == null) {
			hView = inflater.inflate(R.layout.gift_item_layout, null);
			ViewHolder holder = new ViewHolder();
			holder.icon = (ImageView) hView.findViewById(R.id.gift_icon);
			holder.count = (TextView) hView.findViewById(R.id.gift_value);
			hView.setTag(holder);
		}
		GiftCount gift = getItem(position);
		ViewHolder holder = (ViewHolder) hView.getTag();

		// Load the image resource
		String uri = "drawable/" + gift.getGift().getIconRes();
		int imageResource = getContext().getResources().getIdentifier(uri, null, getContext().getPackageName());
		try {
			Drawable image = getContext().getResources().getDrawable(imageResource);
			holder.icon.setImageDrawable(image);
		} catch (NotFoundException e) { // The image wasn't found
			Log.e(TAG, e.getMessage());
		}

		holder.count.setText(String.valueOf(gift.getCount()));
		return hView;
	}

	class ViewHolder {
		ImageView icon;
		TextView name;
		TextView count;
	}
}