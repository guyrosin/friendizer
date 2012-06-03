package com.teamagly.friendizer.widgets;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.Utility;

public class CustomOverlayItem extends OverlayItem {

    private ViewGroup markerLayout;
    protected String title;
    protected String snippet;
    protected String imageURL;
    protected long userID;
    protected Drawable drawable;

    /**
     * Constructs an overlay with the default marker
     * 
     * @param point
     * @param userInfo
     */
    public CustomOverlayItem(GeoPoint point, User userInfo) {
	super(point, "", "");
	title = userInfo.getName();
	snippet = "";
	imageURL = userInfo.getPicURL();
	userID = userInfo.getId();
    }

    /**
     * Constructs an overlay with a custom marker (using the default layout)
     * 
     * @param point
     * @param userInfo
     * @param markerLayout
     */
    public CustomOverlayItem(GeoPoint point, User userInfo, ViewGroup markerLayout) {
	super(point, "", "");
	title = userInfo.getName();
	snippet = "";
	imageURL = userInfo.getPicURL();
	userID = userInfo.getId();
	this.markerLayout = markerLayout;
	setMarker(generateMarker());
    }

    public Drawable generateMarker() {

	Bitmap viewCapture = null;

	// make sure our marker layout isn't null
	if (markerLayout != null) {

	    ImageView imageView = (ImageView) markerLayout.findViewById(R.id.pic);
	    imageView.setImageBitmap(Utility.getInstance().imageLoader.getImage(imageURL));

	    // we need to enable the drawing cache
	    markerLayout.setDrawingCacheEnabled(true);

	    // this is the important code
	    // Without it the view will have a dimension of 0,0 and the bitmap
	    // will be null
	    markerLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
		    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
	    markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

	    // we need to build our drawing cache
	    markerLayout.buildDrawingCache(true);

	    if (markerLayout.getDrawingCache() != null) {
		viewCapture = Bitmap.createBitmap(markerLayout.getDrawingCache());
		if (viewCapture != null) {
		    markerLayout.setDrawingCacheEnabled(false);
		    drawable = new BitmapDrawable(viewCapture);
		    // Bound to "boundCenterBottom"
		    int dWidth = drawable.getIntrinsicWidth();
		    int dHeight = drawable.getIntrinsicHeight();
		    drawable.setBounds(-dWidth / 2, -dHeight, dWidth / 2, 0);
		    return drawable;
		}
	    }
	}
	return null;
    }

    public Drawable getDrawable() {
	if (drawable == null)
	    return generateMarker();
	else
	    return drawable;
    }

    public void showMarker() {
	setMarker(getDrawable());
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
