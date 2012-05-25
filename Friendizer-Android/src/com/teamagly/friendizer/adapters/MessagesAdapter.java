/**
 * 
 */
package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.teamagly.friendizer.R;
import com.teamagly.friendizer.model.Message;
import com.teamagly.friendizer.utils.Utility;

public class MessagesAdapter extends ArrayAdapter<Message> {
    protected static LayoutInflater inflater = null;
    private long myID = Utility.getInstance().userInfo.getId();

    public MessagesAdapter(Context context, int textViewResourceId, List<Message> objects) {
	super(context, textViewResourceId, objects);
	inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /*
     * (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	Message msg = getItem(position);
	View hView = convertView;
	if (convertView == null) {
	    // Check who sent the message
	    if (msg.getSource() == myID)
		hView = inflater.inflate(R.layout.message_outgoing_layout, null);
	    else
		hView = inflater.inflate(R.layout.message_incoming_layout, null);
	    ViewHolder holder = new ViewHolder();

	    holder.message = (TextView) hView.findViewById(R.id.message);
	    hView.setTag(holder);
	}

	ViewHolder holder = (ViewHolder) hView.getTag();
	holder.message.setText(msg.getText());
	return hView;
    }

    class ViewHolder {
	TextView message;
    }
}
