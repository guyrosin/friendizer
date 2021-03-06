/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.GiftsUserAdapter;
import com.teamagly.friendizer.model.GiftCount;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class GiftsUserActivity extends SherlockActivity implements OnItemClickListener {

	private final String TAG = getClass().getName();
	GiftsUserAdapter adapter;
	protected GridView gridView;
	protected List<GiftCount> giftsList;
	protected User user;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ActionBar actionBar = getSupportActionBar();
		user = ((User) getIntent().getSerializableExtra("user"));
		if (user.getId() == Utility.getInstance().userInfo.getId()) // If the user is the current one
			actionBar.setTitle("My Gifts");
		else {
			actionBar.setTitle(user.getName());
			actionBar.setSubtitle("Gifts");
		}
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.gifts_layout);
		gridView = (GridView) findViewById(R.id.gridview);
		giftsList = new ArrayList<GiftCount>();
		adapter = new GiftsUserAdapter(this, 0, giftsList);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		setSupportProgressBarIndeterminateVisibility(true);
		new AsyncTask<Void, Void, List<GiftCount>>() {
			@Override
			protected List<GiftCount> doInBackground(Void... params) {
				try {
					return ServerFacade.getUserGifts(user.getId());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				return new ArrayList<GiftCount>();
			}

			@Override
			protected void onPostExecute(List<GiftCount> gifts) {
				giftsList.clear();
				giftsList.addAll(gifts);
				adapter.notifyDataSetChanged();
				setSupportProgressBarIndeterminateVisibility(false);
			}
		}.execute();
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		GiftCount gift = giftsList.get(position);
		// Show a dialog with info
		Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.gift_info_dialog);
		dialog.setTitle(gift.getGift().getName());

		TextView giftValue = (TextView) dialog.findViewById(R.id.gift_value);
		giftValue.setText(String.valueOf(gift.getGift().getValue()));
		TextView giftCount = (TextView) dialog.findViewById(R.id.gift_count);
		giftCount.setText(String.valueOf(gift.getCount()));
		TextView giftCountTitlePre = (TextView) dialog.findViewById(R.id.gift_count_title1);
		String title_pre = user.getId() == Utility.getInstance().userInfo.getId() ? "You've got " : user.getFirstName() + " has got ";
		giftCountTitlePre.setText(title_pre);
		ImageView image = (ImageView) dialog.findViewById(R.id.gift_icon);

		// Load the image resource
		String uri = "drawable/" + gift.getGift().getIconRes();
		int imageResource = getResources().getIdentifier(uri, null, getPackageName());
		try {
			Drawable drawable = getResources().getDrawable(imageResource);
			image.setImageDrawable(drawable);
		} catch (NotFoundException e) { // The image wasn't found
			Log.e(TAG, e.getMessage());
		}
		dialog.show();
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
			Intent intent;
			if (user.getId() == Utility.getInstance().userInfo.getId()) { // If the user is the current one
				intent = new Intent(this, FriendizerActivity.class);
				intent.putExtra("tab", R.string.my_profile);
			} else {
				intent = new Intent(this, FriendProfileActivity.class);
				intent.putExtra("user", user);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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