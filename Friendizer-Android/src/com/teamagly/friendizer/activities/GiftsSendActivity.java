/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.GiftsAdapter;
import com.teamagly.friendizer.model.Gift;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class GiftsSendActivity extends SherlockActivity implements OnItemClickListener {

	private final String TAG = getClass().getName();
	GiftsAdapter adapter;
	protected GridView gridView;
	protected List<Gift> giftsList;
	protected User destUser;
	protected GiftsSendActivity activity;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Send a Gift");
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.gifts_layout);
		destUser = ((User) getIntent().getSerializableExtra("user"));
		gridView = (GridView) findViewById(R.id.gridview);
		giftsList = new ArrayList<Gift>();
		activity = this;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		setSupportProgressBarIndeterminateVisibility(true);
		new Thread(new Runnable() {
			public void run() {
				try {
					Gift[] gifts = ServerFacade.getAllGifts();
					giftsList = Arrays.asList(gifts);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						adapter = new GiftsAdapter(activity, 0, giftsList);
						gridView.setAdapter(adapter);
						gridView.setOnItemClickListener(activity);
						setSupportProgressBarIndeterminateVisibility(false);
					}
				});
			}
		}).start();
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
		final Gift gift = giftsList.get(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Send " + gift.getName() + " to " + destUser.getName() + " for " + gift.getValue() + " coins?")
				.setCancelable(false).setPositiveButton("Send", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						new SendGiftTask().execute(gift);
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.show();
	}

	protected class SendGiftTask extends AsyncTask<Gift, Void, Void> {

		protected Void doInBackground(Gift... gifts) {
			try {
				ServerFacade.sendGift(Utility.getInstance().userInfo.getId(), destUser.getId(), gifts[0].getId());
			} catch (Exception e) {
				Log.w(TAG, "", e);
				Toast.makeText(getBaseContext(), "Couldn't buy", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		protected void onPostExecute(Void v) {
			Toast.makeText(activity, "Gift sent!", Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: // Move to the user's profile
			Intent intent = new Intent(this, FriendProfileActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("user", destUser);
			startActivity(intent);
			return true;
		case R.id.menu_refresh:
			onResume();
			return true;
		case R.id.menu_feedback:
			return Utility.startFeedback(this);
		case R.id.menu_settings: // Move to the settings activity
			startActivity(new Intent(this, FriendsPrefs.class));
			return true;
		case R.id.menu_invite: // Show the Facebook invitation dialog
			Bundle params = new Bundle();
			params.putString("message", getString(R.string.invitation_msg));
			Utility.getInstance().facebook.dialog(this, "apprequests", params, new BaseDialogListener());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
