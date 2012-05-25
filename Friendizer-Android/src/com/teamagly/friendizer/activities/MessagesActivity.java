/**
 * 
 */
package com.teamagly.friendizer.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.adapters.MessagesAdapter;
import com.teamagly.friendizer.model.Message;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.ServerFacade;
import com.teamagly.friendizer.widgets.ActionBar;

/**
 * @author Guy
 * 
 */
public class MessagesActivity extends Activity {

    private final String TAG = getClass().getName();
    ActionBar actionBar;

    // Layout Views
    private ListView messagesView;
    private EditText newMsgText;
    private Button sendButton;

    private MessagesAdapter messagesAdapter;
    private ArrayList<Message> messages;
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
	setContentView(R.layout.messages_layout);
	actionBar = (ActionBar) findViewById(R.id.actionbar);
	actionBar.mRefreshBtn.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		onResume();
	    }
	});

	destUser = (User) getIntent().getSerializableExtra("user");
	actionBar.setTitle(destUser.getName());

	// Initialize the compose field with a listener for the return key
	newMsgText = (EditText) findViewById(R.id.new_msg_text);
	// mOutEditText.setOnEditorActionListener(mWriteListener);

	// Initialize the send button with a listener that for click events
	sendButton = (Button) findViewById(R.id.button_send);
	sendButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		sendMessage(newMsgText.getText().toString());
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
	showLoadingIcon(true);
	new Thread(new Runnable() {
	    public void run() {
		try {
		    messages = ServerFacade.getConversation(destUser.getId(), 0, 20);
		} catch (Exception e) {
		    messages = new ArrayList<Message>();
		    runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    showLoadingIcon(false);
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
			showLoadingIcon(false);
		    }
		});
	    }
	}).start();
    }

    /**
     * Sends a message.
     * 
     * @param text
     *            A string of text to send.
     */
    private void sendMessage(String text) {
	// Check that there's actually something to send
	if (text.length() > 0) {
	    Message newMsg = new Message(destUser.getId(), text);
	    try {
		ServerFacade.sendMessage(newMsg);
		newMsgText.setText(""); // Clear the edit text field
		messages.add(newMsg);
		messagesAdapter = new MessagesAdapter(this, R.id.message, messages);
		messagesView.setAdapter(messagesAdapter);
	    } catch (Exception e) {
		Log.w(TAG, e.getMessage());
	    }
	}
    }

    // TODO: Usage with C2DM
    // Use an AsyncTask to avoid blocking the UI thread
//    new AsyncTask<Void, Void, String>() {
//        private String message;
//
//        @Override
//        protected String doInBackground(Void... arg0) {
//            MyRequestFactory requestFactory = Util.getRequestFactory(mContext,
//                    MyRequestFactory.class);
//            final HelloWorldRequest request = requestFactory.helloWorldRequest();
//            Log.i(TAG, "Sending request to server");
//            request.getMessage().fire(new Receiver<String>() {
//                @Override
//                public void onFailure(ServerFailure error) {
//                    message = "Failure: " + error.getMessage();
//                }
//
//                @Override
//                public void onSuccess(String result) {
//                    message = result;
//                }
//            });
//            return message;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            helloWorld.setText(result);
//            sayHelloButton.setEnabled(true);
//        }
//    }.execute();
    protected void showLoadingIcon(boolean show) {
	try {
	    actionBar.showProgressBar(show);
	} catch (Exception e) {
	}
    }

}
