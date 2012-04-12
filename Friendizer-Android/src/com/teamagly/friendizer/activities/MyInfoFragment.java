/**
 * 
 */
package com.teamagly.friendizer.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.UserInfo;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.utils.ImageLoader.Type;

/**
 * @author Guy
 * 
 */
public class MyInfoFragment extends Fragment {

    private final String TAG = getClass().getName();
    private ImageView userPic;
    private TextView userName;
    private TextView age;
    private TextView gender;
    private TextView value;
    private TextView money;
    private TextView owns;
    private TextView ownerName;
    private ImageView ownerPic;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	// Inflate the layout for this fragment
	View view = inflater.inflate(R.layout.profile_info_layout, container, false);
	userPic = (ImageView) view.findViewById(R.id.user_pic);
	userName = (TextView) view.findViewById(R.id.name);
	age = (TextView) view.findViewById(R.id.age);
	gender = (TextView) view.findViewById(R.id.gender);
	value = (TextView) view.findViewById(R.id.value);
	money = (TextView) view.findViewById(R.id.money);
	owns = (TextView) view.findViewById(R.id.owns);
	ownerName = (TextView) view.findViewById(R.id.owner_name);
	ownerPic = (ImageView) view.findViewById(R.id.owner_pic);
	updateViews();
	return view;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
	super.onResume();
	showLoadingIcon(true);
	// Reload the user's details from Facebook
	Bundle params = new Bundle();
	params.putString("fields", "name, first_name, picture, birthday, gender");
	Utility.getInstance().mAsyncRunner.request("me", params, new UserRequestListener());
	// Reload the user's details from our servers (in the background)
	new Thread(new Runnable() {
	    public void run() {
		try {
		    Utility.getInstance().userInfo.updateFriendizerData(ServerFacade.userDetails(Utility.getInstance().userInfo.id));
		    // Update the view from the main thread
		    getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    updateFriendizerViews();
			}
		    });
		} catch (Exception e) {
		    Log.w(TAG, "", e);
		}
	    }
	}).start();

	if (Utility.getInstance().userInfo.ownerID > 0) {
	    // Get the owner's name and picture from Facebook
	    params = new Bundle();
	    params.putString("fields", "name, picture");
	    Utility.getInstance().mAsyncRunner.request(String.valueOf(Utility.getInstance().userInfo.ownerID), params,
		    new OwnerRequestListener());
	}
    }

    protected void updateViews() {
	updateFriendizerViews();
	updateFacebookViews();
    }

    protected void updateFriendizerViews() {
	UserInfo userInfo = Utility.getInstance().userInfo;
	value.setText(String.valueOf(userInfo.value));
	money.setText(String.valueOf(userInfo.money));
	if (userInfo.ownsList != null)
	    owns.setText(String.valueOf(userInfo.ownsList.length));
	showLoadingIcon(false);
    }

    protected void updateFacebookViews() {
	UserInfo userInfo = Utility.getInstance().userInfo;
	Utility.getInstance().imageLoader.displayImage(userInfo.picURL, userPic, Type.ROUND_CORNERS);
	userName.setText(userInfo.name);
	age.setText(userInfo.age);
	gender.setText(userInfo.gender);
    }

    /**
     * @param show
     *            whether to show or hide the loading icon (in the parent activity)
     */
    protected void showLoadingIcon(boolean show) {
	try {
	    Activity parent = getActivity().getParent();
	    if (parent != null)
		((FriendizerActivity) parent).actionBar.showProgressBar(show);
	} catch (Exception e) {
	}
    }

    /*
     * Callback for fetching user's details from Facebook
     */
    public class UserRequestListener extends BaseRequestListener {

	@Override
	public void onComplete(final String response, final Object state) {
	    JSONObject jsonObject;
	    try {
		jsonObject = new JSONObject(response);
		final UserInfo userInfo = new UserInfo(jsonObject);
		// Update the user's details from Facebook
		Utility.getInstance().userInfo.updateFacebookData(userInfo);
		// Update the views (has to be done from the main thread)
		getActivity().runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			updateFacebookViews();
		    }
		});
	    } catch (JSONException e) {
		Log.w(TAG, "", e);
	    }
	}
    }

    /*
     * Callback for fetching owner's details from Facebook
     */
    public class OwnerRequestListener extends BaseRequestListener {

	@Override
	public void onComplete(final String response, final Object state) {
	    JSONObject jsonObject;
	    try {
		jsonObject = new JSONObject(response);

		final String ownerNameStr = jsonObject.getString("name");
		final String picURL = jsonObject.getString("picture");

		getActivity().runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			ownerName.setText(ownerNameStr);
			Utility.getInstance().imageLoader.displayImage(picURL, ownerPic, Type.ROUND_CORNERS);
		    }
		});
	    } catch (Exception e) {
		Log.w(TAG, "", e);
	    }
	}
    }
}
