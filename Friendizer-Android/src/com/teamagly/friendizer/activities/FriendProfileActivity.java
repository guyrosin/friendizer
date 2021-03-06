/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.io.IOException;

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
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.androidquery.AQuery;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseRequestListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;
import com.teamagly.friendizer.widgets.TextProgressBar;

public class FriendProfileActivity extends SherlockFragmentActivity {

	private final String TAG = getClass().getName();
	private AQuery aq;
	SherlockFragmentActivity activity = this;
	ActionBar actionBar;
	User userInfo;
	long userID;
	private boolean blocked = false;

	private Menu menu;
	private TextView txtMatching;
	private TextProgressBar xpBar;
	final Handler handler = new Handler();

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
		aq = new AQuery(this);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		txtMatching = aq.find(R.id.matching).getTextView();
		xpBar = (TextProgressBar) findViewById(R.id.xp_bar);

		blocked = intent.getBooleanExtra("blocked", false);
		Object userObject = intent.getSerializableExtra("user");
		if (userObject == null) {
			// If passed the user's ID, fetching the details will be done in onResume()
			if (intent.getLongExtra("userID", 0) > 0)
				userID = intent.getLongExtra("userID", 0);
			else {
				Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
				finish();
			}
		} else {
			userInfo = (User) userObject;
			updateViews();
			updateButtons();
		}

		aq.find(R.id.btn_matching_layout).clicked(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (userInfo.getMatching() <= 0) {
					if (txtMatching.getText().length() == 0)
						return;
					userInfo.setMatching(Integer.parseInt(txtMatching
							.getText()
							.toString()
							.substring(0, txtMatching.getText().length() - 1)));
				}
				startActivity(new Intent(activity, BaseFragmentActivity.class).putExtra("fragment",
						MutualLikesFragment.class.getName()).putExtra("user", userInfo));
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		setSupportProgressBarIndeterminateVisibility(true);

		if (userInfo == null) { // Reload the user's details from our servers
			new FriendizerTask().execute(userID);
			// Get the matching with this user
			new MatchingTask().execute(userID, Utility.getInstance().userInfo.getId());
			fetchMutualFriends();
		} else { // Load only what's missing
			if (userInfo.getMatching() <= 0) // Fetch the matching only if needed
				new MatchingTask().execute(userInfo.getId(), Utility.getInstance().userInfo.getId());
			else
				txtMatching.setText(String.valueOf(userInfo.getMatching()) + "%");
			fetchOwnerInfo();
			fetchMutualFriends();
		}
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
		this.menu = menu;
		MenuItem blockItem = menu.findItem(R.id.menu_block);
		if (blockItem != null)
			blockItem.setTitle(blocked ? "Unblock" : "Block");
		return true;
	}

	protected void refreshAll() {
		setSupportProgressBarIndeterminateVisibility(true);
		long id = userInfo != null ? userInfo.getId() : userID;
		new FriendizerTask().execute(id);
		// Get the matching with this user
		new MatchingTask().execute(id, Utility.getInstance().userInfo.getId());
		fetchMutualFriends();
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
			refreshAll();
			return true;
		case R.id.menu_settings: // Move to the settings activity
			startActivity(new Intent(this, FriendsPrefs.class));
			return true;
		case R.id.menu_block: // Show a confirmation dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			if (blocked)
				builder.setTitle("Unblock " + userInfo.getName() + "?")
				.setMessage("You'll be able to get connected again")
				.setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						new UnblockTask().execute();
					}
				});
			else
				builder.setTitle("Block " + userInfo.getName() + "?")
				.setMessage("You won't hear anything from " + userInfo.getFirstName())
				.setPositiveButton("Block", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						new BlockTask().execute();
					}
				});
			builder.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected class BlockTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... v) {
			try {
				ServerFacade.block(Utility.getInstance().userInfo.getId(), userInfo.getId());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(getBaseContext(), userInfo.getName() + " has been blocked!", Toast.LENGTH_LONG).show();
				blocked = true;
			} else
				Toast.makeText(getBaseContext(), "Couldn't block " + userInfo.getName(), Toast.LENGTH_SHORT).show();
		}
	}

	protected class UnblockTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... v) {
			try {
				ServerFacade.unblock(Utility.getInstance().userInfo.getId(), userInfo.getId());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(getBaseContext(), userInfo.getName() + " has been unblocked!", Toast.LENGTH_LONG).show();
				blocked = false;
			} else
				Toast.makeText(getBaseContext(), "Couldn't block " + userInfo.getName(), Toast.LENGTH_SHORT).show();
		}
	}

	protected void updateViews() {
		aq.find(R.id.level).text("Level " + userInfo.getLevel());
		int earnedPointsThisLevel = userInfo.getEarnedPointsThisLevel();
		int currentLevelPoints = userInfo.getLevelPoints();
		xpBar.setMax(currentLevelPoints);
		xpBar.setProgress(earnedPointsThisLevel);
		xpBar.setText(earnedPointsThisLevel + " / " + currentLevelPoints);

		aq.find(R.id.points).text(String.valueOf(userInfo.getPoints()));
		aq.find(R.id.owns).text(String.valueOf(userInfo.getOwnsNum()));
		if (userInfo.getStatus() != null && userInfo.getStatus().length() > 0)
			aq.find(R.id.status).text("\"" + userInfo.getStatus() + "\"").visible();
		else
			aq.find(R.id.status).gone();

		if (userInfo.getPicURL() != null && userInfo.getPicURL().length() > 0)
			ImageLoader.getInstance().displayImage(userInfo.getPicURL(), aq.find(R.id.user_pic).getImageView());
		aq.find(R.id.name).text(userInfo.getName());
		aq.find(R.id.age).text(userInfo.getAge());
		if (userInfo.getGender() != null && userInfo.getGender().length() > 0)
			aq.find(R.id.gender).text(userInfo.getGender());
		if (menu != null) {
			MenuItem blockItem = menu.findItem(R.id.menu_block);
			if (blockItem != null)
				blockItem.setTitle(blocked ? "Unblock" : "Block");
		}
	}

	protected void updateButtons() {
		// Check if I bought this user
		if ((userInfo.getOwnerID() == Utility.getInstance().userInfo.getId())) {
			// Show the relevant buttons layout
			aq.find(R.id.buttons_friend).visible();
			findViewById(R.id.buttons_stranger).setVisibility(View.GONE);
			// Define the chat button
			findViewById(R.id.btn_friend_chat).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent().setClass(FriendProfileActivity.this, ChatActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
			// Define the achievements button
			findViewById(R.id.btn_friend_achievements).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent().setClass(FriendProfileActivity.this, AchievementsActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
			// Define the send gift button
			findViewById(R.id.btn_friend_send_gift).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent().setClass(FriendProfileActivity.this, GiftsSendActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
			// Define the gifts button
			findViewById(R.id.btn_friend_gifts).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent().setClass(FriendProfileActivity.this, GiftsUserActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
		} else {
			// Show the relevant buttons layout
			aq.find(R.id.buttons_stranger).visible();
			findViewById(R.id.buttons_friend).setVisibility(View.GONE);
			// Define the buy button
			findViewById(R.id.btn_stranger_buy).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new BuyTask().execute();
				}
			});
			// Define the achievements button
			findViewById(R.id.btn_stranger_achievements).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Move to the achievements activity
					Intent intent = new Intent().setClass(FriendProfileActivity.this, AchievementsActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
			// Define the gift button
			findViewById(R.id.btn_stranger_chat).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Move to the chat activity
					Intent intent = new Intent().setClass(FriendProfileActivity.this, ChatActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
			// Define the gifts button
			findViewById(R.id.btn_stranger_gifts).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent().setClass(FriendProfileActivity.this, GiftsUserActivity.class);
					intent.putExtra("user", userInfo);
					startActivity(intent);
				}
			});
		}
	}

	protected class BuyTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... v) {
			try {
				ServerFacade.buy(Utility.getInstance().userInfo.getId(), userInfo.getId());
			} catch (IOException e) {
				Log.w(TAG, "", e);
				Toast.makeText(getBaseContext(), "Couldn't buy", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			refreshAll(); // Refresh
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
						aq.find(R.id.mutual_friends).text(String.valueOf(friends.length()));
						setSupportProgressBarIndeterminateVisibility(false); // Done loading the data (roughly...)
					}
				});
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
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
				final String picURL = jsonObject.optJSONObject("picture").optJSONObject("data").optString("url");

				handler.post(new Runnable() {
					@Override
					public void run() {
						ImageLoader.getInstance().displayImage(picURL, aq.find(R.id.owner_pic).getImageView());
						// Add a listener for the owner's pic
						aq.find(R.id.btn_owner_layout).clicked(new OnClickListener() {
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
				userInfo = newUserInfo;
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
			// Update the views
			updateViews();
			updateButtons();
		}
	}

	class MatchingTask extends AsyncTask<Long, Void, Integer> {

		@Override
		protected Integer doInBackground(Long... userIDs) {
			try {
				return ServerFacade.matching(userIDs[0], userIDs[1]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				return 0;
			}
		}

		@Override
		protected void onPostExecute(Integer matching) {
			if (matching > 0) {
				if (userInfo != null)
					userInfo.setMatching(matching);
				// Update the view
				txtMatching.setText(String.valueOf(matching) + "%");
			} else
				txtMatching.setText("");
		}
	}

	protected void fetchMutualFriends() {
		// Fetch the mutual friends number
		long id = userInfo != null ? userInfo.getId() : userID;
		Bundle params = new Bundle();
		Utility.getInstance().mAsyncRunner.request("me/mutualfriends/" + String.valueOf(id), params, new MutualFriendsListener());
	}

	protected void fetchOwnerInfo() {
		if (userInfo.getOwnerID() > 0) {
			// Get the owner's name and picture from Facebook
			Bundle params = new Bundle();
			params.putString("fields", "name, picture");
			Utility.getInstance().mAsyncRunner.request(String.valueOf(userInfo.getOwnerID()), params, new OwnerRequestListener());
		}
	}

}
