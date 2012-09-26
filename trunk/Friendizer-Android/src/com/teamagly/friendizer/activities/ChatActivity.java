/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.MessagesAdapter;
import com.teamagly.friendizer.model.Message;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.BaseDialogListener;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.utils.Utility;

public class ChatActivity extends SherlockActivity {

	private final String TAG = getClass().getName();
	public static final String ACTION_UPDATE_CHAT = "com.teamagly.friendizer.ChatActivity.UPDATE";
	ActionBar actionBar;

	// Layout Views
	private ListView messagesView;
	private EditText newMsgText;
	private ImageView sendButton;

	private MessagesAdapter messagesAdapter;
	private List<Message> messages;
	private User destUser;

	// The action listener for the EditText widget, to listen for the return key
	// private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
	// public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
	// // If the action is a key-up event on the return key, send the message
	// if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
	// String message = view.getText().toString();
	// sendMessage(message);
	// }
	// return true;
	// }
	// };

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.messages_layout);

		destUser = (User) getIntent().getSerializableExtra("user");
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(destUser.getName());

		// Initialize the compose field with a listener for the return key
		newMsgText = (EditText) findViewById(R.id.new_msg_text);
		// mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		sendButton = (ImageView) findViewById(R.id.btn_send);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage(newMsgText.getText().toString());
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getSerializableExtra("user") != null) {
			User user = (User) intent.getSerializableExtra("user");
			if (user != null)
				destUser = user;
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					messages = ServerFacade.getConversation(destUser.getId(), 0, 20);
				} catch (Exception e) {
					messages = new ArrayList<Message>();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setSupportProgressBarIndeterminateVisibility(false);
						}
					});
				}
				// Initialize the array adapter for the conversation thread
				messagesAdapter = new MessagesAdapter(getBaseContext(), R.id.message, messages);
				messagesView = (ListView) findViewById(R.id.log);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						messagesView.setAdapter(messagesAdapter);
						setSupportProgressBarIndeterminateVisibility(false);
					}
				});
			}
		}).start();

		registerReceiver(resetReceiver, new IntentFilter(ACTION_UPDATE_CHAT));
	}

	public BroadcastReceiver resetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			long userID = intent.getLongExtra("userID", 0);
			Log.d(TAG, "Got a new chat message from " + userID);
			if (userID == destUser.getId()) {
				String msg = intent.getStringExtra("text");
				Message newMsg = new Message(userID, Utility.getInstance().userInfo.getId(), msg, new Date());
				messages.add(newMsg);
				// messagesAdapter.notifyDataSetChanged(); not working, so recreate the adapter instead
				messagesAdapter = new MessagesAdapter(context, R.id.message, messages);
				messagesView.setAdapter(messagesAdapter);
				setResultCode(Activity.RESULT_OK); // Set the result code to indicate the broadcast intent has been received
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(resetReceiver);
	};

	/**
	 * Sends a message.
	 * 
	 * @param text
	 *            A string of text to send.
	 */
	private void sendMessage(String text) {
		// Check that there's actually something to send
		if (text.length() > 0) {
			Message newMsg = new Message(destUser.getId(), text, new Date());
			new SendTask().execute(newMsg);
		}
	}

	protected class SendTask extends AsyncTask<Message, Void, Void> {
		Message msg;

		@Override
		protected Void doInBackground(Message... msgs) {
			msg = msgs[0];
			try {
				ServerFacade.sendMessage(msg);
			} catch (Exception e) {
				Log.e(TAG, "", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			newMsgText.setText(""); // Clear the edit text field
			messages.add(msg);
			// messagesAdapter.notifyDataSetChanged(); not working, so recreate the adapter instead
			messagesAdapter = new MessagesAdapter(getBaseContext(), R.id.message, messages);
			messagesView.setAdapter(messagesAdapter);
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
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
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
