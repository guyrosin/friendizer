/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.MapView;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.widgets.CustomItemizedOverlay;
import com.teamagly.friendizer.widgets.CustomOverlayItem;

public class MapAdapter extends FriendsAdapter {
	@SuppressWarnings("unused")
	private final String TAG = getClass().getName();
	protected CustomItemizedOverlay nearbyUsersItemizedOverlay;
	LinearLayout markerLayout;
	MapView mapView;

	public MapAdapter(Context context, int textViewResourceId, List<User> objects, MenuItem filterMenuItem, MapView mapView,
			CustomItemizedOverlay nearbyUsersItemizedOverlay, LinearLayout markerLayout) {
		super(context, textViewResourceId, objects, filterMenuItem, false);
		this.mapView = mapView;
		this.nearbyUsersItemizedOverlay = nearbyUsersItemizedOverlay;
		this.markerLayout = markerLayout;
	}

	/*
	 * (non-Javadoc)
	 * @see com.teamagly.friendizer.FriendsAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return convertView;
	}

	@Override
	public void clear() {
		allUsersList.clear();
		filteredUsersList.clear();
		nearbyUsersItemizedOverlay.hideAllBalloons();
		nearbyUsersItemizedOverlay.clear();
	}

	@Override
	public void notifyDataSetChanged() {
		nearbyUsersItemizedOverlay.clear();
		for (User user : filteredUsersList) { // Create overlays for the filtered users
			CustomOverlayItem overlayItem = new CustomOverlayItem(user, markerLayout);
			nearbyUsersItemizedOverlay.addOverlay(overlayItem);
		}
		mapView.invalidate();
	}
}
