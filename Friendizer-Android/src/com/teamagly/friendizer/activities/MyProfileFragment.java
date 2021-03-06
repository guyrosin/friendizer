/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.io.IOException;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.widgets.TextProgressBar;

public class MyProfileFragment extends SherlockFragment {

	private final String TAG = getClass().getName();
	private AQuery aq;
	protected SherlockFragmentActivity activity;
	private TextProgressBar xpBar;

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
		aq = new AQuery(activity);
		activity.setContentView(R.layout.profile_info_layout);
		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);

		xpBar = (TextProgressBar) activity.findViewById(R.id.xp_bar);

		aq.find(R.id.status).clicked(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showStatusDialog();
			}
		});

		aq.find(R.id.btn_owns_layout).clicked(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(activity, BaseFragmentActivity.class).putExtra("fragment", OwnsFragment.class.getName()));
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		activity.setSupportProgressBarIndeterminateVisibility(true);

		updateViews();
		initButtons();
		// Reload the user's details from our servers (in the background)
		new FriendizerTask().execute(Utility.getInstance().userInfo.getId());
	}

	protected void initButtons() {
		// Define the achievements button
		aq.find(R.id.btn_achievements).clicked(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, AchievementsActivity.class);
				intent.putExtra("user", Utility.getInstance().userInfo);
				startActivity(intent);
			}
		});
		// Define the gifts button
		aq.find(R.id.btn_gifts).clicked(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, GiftsUserActivity.class);
				intent.putExtra("user", Utility.getInstance().userInfo);
				startActivity(intent);
			}
		});
		// Define the action history button
		aq.find(R.id.btn_action_history).clicked(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(activity, BaseFragmentActivity.class).putExtra("fragment",
						ActionHistoryFragment.class.getName()));
			}
		});
		// Define the status change button
		aq.find(R.id.btn_change_status).clicked(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showStatusDialog();
			}
		});
	}

	protected void updateViews() {
		User userInfo = Utility.getInstance().userInfo;
		aq.find(R.id.level).text("Level " + userInfo.getLevel());
		int earnedPointsThisLevel = userInfo.getEarnedPointsThisLevel();
		int currentLevelPoints = userInfo.getLevelPoints();
		xpBar.setMax(currentLevelPoints);
		xpBar.setProgress(earnedPointsThisLevel);
		xpBar.setText(earnedPointsThisLevel + " / " + currentLevelPoints);

		aq.find(R.id.points).text(String.valueOf(userInfo.getPoints()));
		aq.find(R.id.money).text(String.valueOf(userInfo.getMoney()));
		if (userInfo.getStatus() != null && userInfo.getStatus().length() > 0)
			aq.find(R.id.status).text("\"" + userInfo.getStatus() + "\"").visible();
		else
			aq.find(R.id.status).gone();
		aq.find(R.id.owns).text(String.valueOf(userInfo.getOwnsNum()));

		if (userInfo.getPicURL() != null && userInfo.getPicURL().length() > 0)
			ImageLoader.getInstance().displayImage(userInfo.getPicURL(), aq.id(R.id.user_pic).getImageView());
		aq.find(R.id.name).text(userInfo.getName());
		aq.find(R.id.age).text(userInfo.getAge());
		aq.find(R.id.gender).text(userInfo.getGender());
	}

	/**
	 * 
	 */
	protected void showStatusDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

		dialogBuilder.setTitle("Enter Your Status");

		// Set an EditText view to get user input
		final EditText input = new EditText(activity);
		final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				else
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
		});
		dialogBuilder.setView(input);
		dialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				final String newStatus = input.getText().toString();
				new UpdateStatusTask().execute(newStatus);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			}
		});
		dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			}
		});
		dialogBuilder.show();
	}

	protected class UpdateStatusTask extends AsyncTask<String, Void, Void> {
		String status;

		@Override
		protected Void doInBackground(String... statuses) {
			status = statuses[0];
			try {
				ServerFacade.updateStatus(status);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			// Update the view
			if (status.length() > 0) {
				aq.find(R.id.status).text("\"" + status + "\"");
				aq.find(R.id.status).visible();
			} else
				aq.find(R.id.status).gone();
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
				final String picURL = jsonObject.optJSONObject("picture").optJSONObject("data").optString("url");

				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ImageLoader.getInstance().displayImage(picURL, aq.find(R.id.owner_pic).getImageView());
						aq.find(R.id.btn_owner_layout).clicked(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// Move to the owner's profile
								Intent intent = new Intent(activity, FriendProfileActivity.class);
								intent.putExtra("userID", Utility.getInstance().userInfo.getOwnerID());
								startActivity(intent);
							}
						});
						activity.setSupportProgressBarIndeterminateVisibility(false);
					}
				});
			} catch (Exception e) {
				Log.w(TAG, "", e);
			}
		}
	}

	class FriendizerTask extends AsyncTask<Long, Void, User> {

		@Override
		protected User doInBackground(Long... userIDs) {
			try {
				return ServerFacade.userDetails(userIDs[0]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				return null;
			}
		}

		@Override
		protected void onPostExecute(final User newUserInfo) {
			// Request the user's details from friendizer and update the views accordingly
			try {
				Utility.getInstance().userInfo = newUserInfo;
				if (newUserInfo.getOwnerID() > 0) {
					// Get the owner's name and picture from Facebook
					Bundle params = new Bundle();
					params.putString("fields", "name, picture");
					Utility.getInstance().mAsyncRunner.request(String.valueOf(newUserInfo.getOwnerID()), params,
							new OwnerRequestListener());
				} else
					activity.setSupportProgressBarIndeterminateVisibility(false);
			} catch (Exception e) {
				Log.w(TAG, "", e);
			}
			// Update the views
			updateViews();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.my_profile_menu, menu);
		inflater.inflate(R.menu.main_menu, menu);
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
		case R.id.menu_leaderboard: // Move to the leaderboard fragment
			Intent intent = new Intent(activity, BaseFragmentActivity.class).putExtra("fragment",
					LeaderboardFragment.class.getName());
			startActivity(intent);
			return true;
		case R.id.menu_block_list: // Move to the block list fragment
			intent = new Intent(activity, BaseFragmentActivity.class).putExtra("fragment", BlockListFragment.class.getName());
			startActivity(intent);
			return true;
		case R.id.menu_settings: // Move to the settings activity
			startActivity(new Intent(activity, FriendsPrefs.class));
			return true;
		case R.id.menu_feedback:
			return Utility.startFeedback(activity);
		case R.id.menu_invite: // Show the Facebook invitation dialog
			Bundle params = new Bundle();
			params.putString("message", getString(R.string.invitation_msg));
			Utility.getInstance().facebook.dialog(activity, "apprequests", params, new BaseDialogListener());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
