/**
 * 
 */
package com.teamagly.friendizer.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.FacebookUser;
import com.teamagly.friendizer.model.FriendizerUser;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.widgets.TextProgressBar;

public class FriendProfileActivity extends SherlockFragmentActivity {

	private final String TAG = getClass().getName();
	ActionBar actionBar;
	User userInfo;

	private ImageView userPic;
	private TextView txtName;
	private TextView txtStatus;
	private TextView txtAge;
	private TextView txtGender;
	private TextView txtValue;
	private TextView txtMatching;
	private TextView txtOwns;
	private ImageView imgOwnerPic;
	private TextView txtMutualFriends;
	private TableLayout buttonsTable;
	private TextView txtLevel;
	private TextProgressBar xpBar;
	final Handler handler = new Handler();
	boolean connectedFlag = false; // Whether I'm connected to this user

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		Intent intent = getIntent();
		setContentView(R.layout.friend_profile_layout);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		userPic = (ImageView) findViewById(R.id.user_pic);
		txtName = (TextView) findViewById(R.id.name);
		txtStatus = (TextView) findViewById(R.id.status);
		txtAge = (TextView) findViewById(R.id.age);
		txtGender = (TextView) findViewById(R.id.gender);
		txtValue = (TextView) findViewById(R.id.value);
		txtMatching = (TextView) findViewById(R.id.matching);
		txtOwns = (TextView) findViewById(R.id.owns);
		imgOwnerPic = (ImageView) findViewById(R.id.owner_pic);
		txtMutualFriends = (TextView) findViewById(R.id.mutual_friends);
		buttonsTable = (TableLayout) findViewById(R.id.buttons_friend);
		txtLevel = (TextView) findViewById(R.id.level);
		xpBar = (TextProgressBar) findViewById(R.id.xp_bar);

		userInfo = (User) intent.getSerializableExtra("user");
		if (userInfo == null) {
			// If passed the user's ID, fetching the details will be done in onResume()
			if (intent.getLongExtra("userID", 0) > 0) {
				userInfo = new User();
				userInfo.setId(intent.getLongExtra("userID", 0));
			} else {
				Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
				finish();
			}
		} else {
			// actionBar.setTitle(userInfo.getName());
			updateViews();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		setSupportProgressBarIndeterminateVisibility(true);
		// Reload the user's details from Facebook
		Bundle params = new Bundle();
		params.putString("fields", "name, first_name, picture, birthday, gender");
		Utility.getInstance().mAsyncRunner.request(String.valueOf(userInfo.getId()), params, new UserRequestListener());

		// Reload the mutual friends number
		params = new Bundle();
		Utility.getInstance().mAsyncRunner.request("me/mutualfriends/" + String.valueOf(userInfo.getId()), params,
				new MutualFriendsListener());

		// Reload the user's details from our servers (in the background)
		new FriendizerTask().execute(userInfo.getId());
		// Get the matching with this user (in the background)
		new MatchingTask().execute(userInfo.getId(), Utility.getInstance().userInfo.getId());
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.friend_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: // Move up to the main activity
			Intent intent = new Intent(this, FriendizerActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.menu_refresh:
			onResume();
			return true;
		case R.id.menu_settings: // Move to the settings activity
			startActivity(new Intent(this, FriendsPrefs.class));
			return true;
		case R.id.menu_block: // Show a confirmation dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Block " + userInfo.getName() + "?").setMessage("You'll automatically ignore every action from him")
					.setCancelable(false).setPositiveButton("Block", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new BlockTask().execute();
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).setIcon(android.R.drawable.ic_dialog_alert);
			builder.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected class BlockTask extends AsyncTask<Void, Void, Boolean> {

		protected Boolean doInBackground(Void... v) {
			try {
				ServerFacade.block(Utility.getInstance().userInfo.getId(), userInfo.getId());
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (result)
				Toast.makeText(getBaseContext(), userInfo.getName() + " has been blocked!", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(getBaseContext(), "Couldn't buy", Toast.LENGTH_SHORT).show();
		}
	}

	protected void updateViews() {
		updateFriendizerViews();
		updateFacebookViews();
	}

	protected void updateFriendizerViews() {
		txtLevel.setText("Level " + userInfo.getLevel());
		int earnedPointsThisLevel = userInfo.getEarnedPointsThisLevel();
		int currentLevelPoints = userInfo.getLevelPoints();
		xpBar.setMax(currentLevelPoints);
		xpBar.setProgress(earnedPointsThisLevel);
		xpBar.setText(earnedPointsThisLevel + " / " + currentLevelPoints);

		txtValue.setText(String.valueOf(userInfo.getPoints()));
		if (userInfo.getOwnsList() != null)
			txtOwns.setText(String.valueOf(userInfo.getOwnsList().length));
		if (userInfo.getStatus().length() > 0) {
			txtStatus.setText("\"" + userInfo.getStatus() + "\"");
			txtStatus.setVisibility(View.VISIBLE);
		} else
			txtStatus.setVisibility(View.GONE);
	}

	protected void updateFacebookViews() {
		ImageLoader.getInstance().displayImage(userInfo.getPicURL(), userPic);
		txtName.setText(userInfo.getName());
		txtAge.setText(userInfo.getAge());
		if (userInfo.getGender().length() > 0) {
			String genderStr = userInfo.getGender();
			// Capitalize the first letter
			// txtGender.setText(Character.toUpperCase(genderStr.charAt(0)) + genderStr.substring(1));
			txtGender.setText(genderStr);
		}
	}

	protected void updateButtons() {
		if (connectedFlag) {
			// Show the relevant buttons layout
			buttonsTable = (TableLayout) findViewById(R.id.buttons_friend);
			buttonsTable.setVisibility(View.VISIBLE);
			findViewById(R.id.buttons_stranger).setVisibility(View.GONE);
			// Define the chat button
			findViewById(R.id.btn_friend_chat).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent().setClass(FriendProfileActivity.this, ChatActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
			// Define the achievements button
			findViewById(R.id.btn_friend_achievements).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent().setClass(FriendProfileActivity.this, AchievementsActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
			// Define the gift button
			findViewById(R.id.btn_friend_gift).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent().setClass(FriendProfileActivity.this, GiftsSendActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
		} else {
			// Show the relevant buttons layout
			buttonsTable = (TableLayout) findViewById(R.id.buttons_stranger);
			buttonsTable.setVisibility(View.VISIBLE);
			findViewById(R.id.buttons_friend).setVisibility(View.GONE);
			// Define the buy button
			findViewById(R.id.btn_stranger_buy).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					new BuyTask().execute();
				}
			});
			// Define the achievements button
			findViewById(R.id.btn_stranger_achievements).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// Move to the achievements activity
					Intent intent = new Intent().setClass(FriendProfileActivity.this, FriendAchievementsActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
			// Define the gift button
			findViewById(R.id.btn_stranger_chat).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// Move to the chat activity
					Intent intent = new Intent().setClass(FriendProfileActivity.this, ChatActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
		}
	}

	protected class BuyTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... v) {
			try {
				ServerFacade.buy(Utility.getInstance().userInfo.getId(), userInfo.getId());
			} catch (Exception e) {
				Log.w(TAG, "", e);
				Toast.makeText(getBaseContext(), "Couldn't buy", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		protected void onPostExecute(Void v) {
			onResume(); // Refresh
		}
	}

	/*
	 * Callback for fetching mutual friends from Facebook
	 */
	public class MutualFriendsListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(response);
				final JSONArray friends = jsonObject.getJSONArray("data");
				// Update the view
				handler.post(new Runnable() {
					@Override
					public void run() {
						txtMutualFriends.setText(String.valueOf(friends.length()));
					}
				});
			} catch (JSONException e) {
				Log.e(TAG, "", e);
			}
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
				final FacebookUser newUserInfo = new FacebookUser(jsonObject);
				// Update the user's details from Facebook
				userInfo.updateFacebookData(newUserInfo);
				// Update the views (has to be done from the main thread)
				handler.post(new Runnable() {
					@Override
					public void run() {
						// actionBar.setTitle(userInfo.getName());
						updateFacebookViews();
					}
				});
			} catch (JSONException e) {
				Log.e(TAG, "", e);
			}
		}
	}

	/*
	 * Callback for fetching owner's details from Facebook
	 */
	public class OwnerRequestListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				final String picURL = jsonObject.getString("picture");

				handler.post(new Runnable() {
					@Override
					public void run() {
						ImageLoader.getInstance().displayImage(picURL, imgOwnerPic);
						// Add a listener for the owner's pic
						imgOwnerPic.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = null;
								// Move to the owner's profile
								if (userInfo.getOwnerID() != Utility.getInstance().userInfo.getId()) {
									intent = new Intent(v.getContext(), FriendProfileActivity.class);
									intent.putExtra("userID", userInfo.getOwnerID());
								} else {
									// Move to my profile
									intent = new Intent(v.getContext(), FriendizerActivity.class);
									intent.putExtra("tab", R.string.my_profile);
								}
								startActivity(intent);
							}
						});
					}
				});
			} catch (Exception e) {
				Log.e(TAG, "", e);
			}
		}
	}

	class FriendizerTask extends AsyncTask<Long, Void, FriendizerUser> {

		protected FriendizerUser doInBackground(Long... userIDs) {
			try {
				return ServerFacade.userDetails(userIDs[0]);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				return null;
			}
		}

		protected void onPostExecute(final FriendizerUser newUserInfo) {
			// Request the user's details from friendizer and update the views accordingly
			try {
				userInfo.updateFriendizerData(newUserInfo);
				if (userInfo.getOwnerID() > 0) {
					// Get the owner's name and picture from Facebook
					Bundle params = new Bundle();
					params.putString("fields", "name, picture");
					Utility.getInstance().mAsyncRunner.request(String.valueOf(userInfo.getOwnerID()), params,
							new OwnerRequestListener());
				}
			} catch (Exception e) {
				Log.w(TAG, "", e);
			}
			// Check if I'm connected to this user
			if ((userInfo.getOwnerID() == Utility.getInstance().userInfo.getId())
					|| (userInfo.getId() == Utility.getInstance().userInfo.getOwnerID()))
				connectedFlag = true;
			// Update the views
			updateFriendizerViews();
			updateButtons();
		}
	}

	class MatchingTask extends AsyncTask<Long, Void, Integer> {

		protected Integer doInBackground(Long... userIDs) {
			try {
				return ServerFacade.matching(userIDs[0], userIDs[1]);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				return 0;
			}
		}

		protected void onPostExecute(Integer matching) {
			userInfo.setMatching(matching);
			// Update the view
			txtMatching.setText(String.valueOf(matching) + "%");
			setSupportProgressBarIndeterminateVisibility(false); // Done loading the data (roughly...)
		}
	}

}
