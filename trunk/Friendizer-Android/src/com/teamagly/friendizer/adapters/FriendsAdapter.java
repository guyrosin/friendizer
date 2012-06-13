package com.teamagly.friendizer.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.teamagly.friendizer.model.User;

public abstract class FriendsAdapter extends ArrayAdapter<User> {

	protected LayoutInflater inflater;
	protected List<User> usersList;

	public FriendsAdapter(Context context, int textViewResourceId, List<User> objects) {
		super(context, textViewResourceId, objects);
		usersList = objects;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

}