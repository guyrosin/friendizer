/**
 * 
 */
package com.teamagly.friendizer.activities;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.User;
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
		    Utility.getInstance().userInfo.updateFriendizerData(ServerFacade.userDetails(Utility.getInstance().userInfo
			    .getId()));
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

	if (Utility.getInstance().userInfo.getOwnerID() > 0) {
	    // Get the owner's name and picture from Facebook
	    params = new Bundle();
	    params.putString("fields", "name, picture");
	    Utility.getInstance().mAsyncRunner.request(String.valueOf(Utility.getInstance().userInfo.getOwnerID()), params,
		    new OwnerRequestListener());
	}
    }

    protected void updateViews() {
	updateFriendizerViews();
	updateFacebookViews();
    }

    protected void updateFriendizerViews() {
	User userInfo = Utility.getInstance().userInfo;
	value.setText(String.valueOf(userInfo.getValue()));
	money.setText(String.valueOf(userInfo.getMoney()));
	if (userInfo.getOwnsList() != null)
	    owns.setText(String.valueOf(userInfo.getOwnsList().length));
	showLoadingIcon(false);
    }

    protected void updateFacebookViews() {
	User userInfo = Utility.getInstance().userInfo;
	Utility.getInstance().imageLoader.displayImage(userInfo.getPicURL(), userPic, Type.ROUND_CORNERS);
	userName.setText(userInfo.getName());
	age.setText(userInfo.getAge());
	gender.setText(userInfo.getGender());
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
		final FacebookUser fbUserInfo = new FacebookUser(jsonObject);
		// Update the user's details from Facebook
		Utility.getInstance().userInfo.updateFacebookData(fbUserInfo);
		// Update the views (has to be done from the main thread)
		getActivity().runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			updateFacebookViews();
		    }
		});
	    } catch (Exception e) {
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
			ownerPic.setOnClickListener(new OnClickListener() {
			    @Override
			    public void onClick(View v) {
				// Move to the owner's profile
				Intent intent = new Intent().setClass(getActivity(), FriendProfileActivity.class);
				intent.putExtra("userID", Utility.getInstance().userInfo.getOwnerID());
				startActivity(intent);
			    }
			});
		    }
		});
	    } catch (Exception e) {
		Log.w(TAG, "", e);
	    }
	}
    }
}
