/**
 * 
 */
package com.teamagly.friendizer.activities;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class MyProfileFragment extends SherlockFragment {

	private final String TAG = getClass().getName();
	protected SherlockFragmentActivity activity;
	private ImageView userPic;
	private TextView name;
	private TextView status;
	private TextView age;
	private TextView gender;
	private TextView value;
	private TextView money;
	private TextView owns;
	private ImageView ownerPic;
	private TextView txtLevel;
	private ProgressBar xpBar;

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getSherlockActivity();
		activity.setContentView(R.layout.profile_info_layout);
		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		userPic = (ImageView) activity.findViewById(R.id.user_pic);
		name = (TextView) activity.findViewById(R.id.name);
		status = (TextView) activity.findViewById(R.id.status);
		age = (TextView) activity.findViewById(R.id.age);
		gender = (TextView) activity.findViewById(R.id.gender);
		value = (TextView) activity.findViewById(R.id.value);
		money = (TextView) activity.findViewById(R.id.money);
		owns = (TextView) activity.findViewById(R.id.owns);
		ownerPic = (ImageView) activity.findViewById(R.id.owner_pic);
		txtLevel = (TextView) activity.findViewById(R.id.level);
		xpBar = (ProgressBar) activity.findViewById(R.id.xp_bar);

		status.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showStatusDialog();
			}
		});
		// updateViews();
	}

	/**
     * 
     */
	protected void showStatusDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

		dialogBuilder.setTitle("Enter Your Status");

		// Set an EditText view to get user input
		final EditText input = new EditText(activity);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (hasFocus)
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				else
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
		});
		dialogBuilder.setView(input);
		dialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				final String newStatus = input.getText().toString();
				// Update the DB
				try {
					ServerFacade.updateStatus(newStatus);
				} catch (Exception e) {
					Log.w(TAG, e.getMessage());
				}
				// Update the view
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						if (newStatus.length() > 0) {
							status.setText("\"" + newStatus + "\"");
							status.setVisibility(View.VISIBLE);
						} else
							status.setVisibility(View.GONE);
					}
				});
			}
		});
		dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			}
		});
		dialogBuilder.show();
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		activity.setSupportProgressBarIndeterminateVisibility(true);
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
					activity.runOnUiThread(new Runnable() {
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
		// Initialize the buttons
		initButtons();
	}

	protected void updateViews() {
		updateFriendizerViews();
		updateFacebookViews();
	}

	protected void initButtons() {
		// Define the achievements button
		activity.findViewById(R.id.btn_achievements).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(activity, AchievementsActivity.class);
				intent.putExtra("user", Utility.getInstance().userInfo);
				startActivity(intent);
			}
		});
		// Define the gifts button
		activity.findViewById(R.id.btn_gifts).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(activity, GiftsUserActivity.class);
				intent.putExtra("user", Utility.getInstance().userInfo);
				startActivity(intent);
			}
		});
		// Define the action history button
		activity.findViewById(R.id.btn_action_history).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO
				Toast.makeText(activity, "Coming soon!", Toast.LENGTH_SHORT).show();
				// Intent intent = new Intent(activity, ActionHistoryActivity.class);
				// intent.putExtra("user", Utility.getInstance().userInfo);
				// startActivity(intent);
			}
		});
	}

	protected void updateFriendizerViews() {
		User userInfo = Utility.getInstance().userInfo;
		txtLevel.setText("Level " + userInfo.getLevel());
		xpBar.setProgress(65); // TODO
		value.setText(String.valueOf(userInfo.getPoints()));
		money.setText(String.valueOf(userInfo.getMoney()));
		if (userInfo.getStatus().length() > 0) {
			status.setText("\"" + userInfo.getStatus() + "\"");
			status.setVisibility(View.VISIBLE);
		} else
			status.setVisibility(View.GONE);
		if (userInfo.getOwnsList() != null)
			owns.setText(String.valueOf(userInfo.getOwnsList().length));
		activity.setSupportProgressBarIndeterminateVisibility(false);
	}

	protected void updateFacebookViews() {
		User userInfo = Utility.getInstance().userInfo;
		ImageLoader.getInstance().displayImage(userInfo.getPicURL(), userPic);
		name.setText(userInfo.getName());
		age.setText(userInfo.getAge());
		gender.setText(userInfo.getGender());
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
				activity.runOnUiThread(new Runnable() {
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
				final String picURL = jsonObject.getString("picture");

				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ImageLoader.getInstance().displayImage(picURL, ownerPic);
						ownerPic.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// Move to the owner's profile
								Intent intent = new Intent().setClass(activity, FriendProfileActivity.class);
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

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.my_profile_menu, menu);
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			onResume();
			return true;
		case R.id.menu_change_status:
			showStatusDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
