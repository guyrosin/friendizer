package com.teamagly.friendizer.adapters;

import java.util.List;

import com.teamagly.friendizer.model.User;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class FriendsAdapter extends ArrayAdapter<User> {

    protected static LayoutInflater inflater = null;

    public FriendsAdapter(Context context, int textViewResourceId, List<User> objects) {
	super(context, textViewResourceId, objects);
	inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);
}