/***
 * Copyright (c) 2011 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.teamagly.friendizer.widgets;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.activities.FriendProfileActivity;
import com.teamagly.friendizer.activities.FriendizerActivity;
import com.teamagly.friendizer.utils.Utility;

public class CustomItemizedOverlay extends BalloonItemizedOverlay<CustomOverlayItem> {

	private ArrayList<CustomOverlayItem> mOverlays = new ArrayList<CustomOverlayItem>();
	private Context c;

	public CustomItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		c = mapView.getContext();
		populate();
	}

	public void addOverlay(CustomOverlayItem overlay) {
		mOverlays.add(overlay);
		setLastFocusedIndex(-1);
		populate();
	}

	public void populateNow() {
		populate();
	}

	@Override
	protected CustomOverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void clear() {
		mOverlays.clear();
		setLastFocusedIndex(-1);
		populate();
	}

	@Override
	protected boolean onBalloonTap(int index, CustomOverlayItem item) {
		Intent intent = null;
		if (item.getUserID() != Utility.getInstance().userInfo.getId()) {
			// Move to the user's profile
			intent = new Intent().setClass(c, FriendProfileActivity.class);
			intent.putExtra("userID", item.getUserID());
		} else {
			intent = new Intent().setClass(c, FriendizerActivity.class);
			intent.putExtra("tab", R.string.my_profile);
		}
		c.startActivity(intent);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay#hideBalloon()
	 */
	@Override
	public void hideBalloon() {
		if (currentFocusedItem != null)
			currentFocusedItem.showMarker(); // Restore the marker
		super.hideBalloon();
	}

	/*
	 * (non-Javadoc)
	 * @see com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay#createAndDisplayBalloonOverlay()
	 */
	@Override
	protected boolean createAndDisplayBalloonOverlay() {
		boolean result = super.createAndDisplayBalloonOverlay();
		if (currentFocusedItem != null)
			currentFocusedItem.setMarker(getMapView().getResources().getDrawable(R.drawable.stub)); // Remove the marker
		return result;
	}

	@Override
	protected BalloonOverlayView<CustomOverlayItem> createBalloonOverlayView() {
		return new CustomBalloonOverlayView<CustomOverlayItem>(getMapView().getContext(), getBalloonBottomOffset());
	}
}
