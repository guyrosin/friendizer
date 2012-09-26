package com.teamagly.friendizer.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.OverlayItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;
import com.teamagly.friendizer.R;

public class CustomBalloonOverlayView<Item extends OverlayItem> extends BalloonOverlayView<CustomOverlayItem> {

	private TextView title;
	private TextView snippet;
	private ImageView image;

	public CustomBalloonOverlayView(Context context, int balloonBottomOffset) {
		super(context, balloonBottomOffset);
	}

	@Override
	protected void setupView(Context context, final ViewGroup parent) {

		// inflate our custom layout into parent
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.map_balloon_layout, parent);

		// setup our fields
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);
		image = (ImageView) v.findViewById(R.id.balloon_item_image);

	}

	@Override
	protected void setBalloonData(CustomOverlayItem item, ViewGroup parent) {
		title.setText(item.getTitle());
		snippet.setText(item.getSnippet());
		ImageLoader.getInstance().displayImage(item.getImageURL(), image);
	}
}
