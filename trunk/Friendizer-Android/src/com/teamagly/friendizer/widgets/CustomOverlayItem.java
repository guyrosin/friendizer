package com.teamagly.friendizer.widgets;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.teamagly.friendizer.model.User;

public class CustomOverlayItem extends OverlayItem {

    protected String title;
    protected String snippet;
    protected String imageURL;
    protected long userID;

    public CustomOverlayItem(GeoPoint point, User userInfo) {
	super(point, "", "");
	title = userInfo.getName();
	snippet = "";
	imageURL = userInfo.getPicURL();
	userID = userInfo.getId();
    }

    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @return the snippet
     */
    public String getSnippet() {
	return snippet;
    }

    /**
     * @return the imageURL
     */
    public String getImageURL() {
	return imageURL;
    }

    /**
     * @return the userID
     */
    public long getUserID() {
	return userID;
    }
}
