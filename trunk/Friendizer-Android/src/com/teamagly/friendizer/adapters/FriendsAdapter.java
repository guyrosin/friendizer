package com.teamagly.friendizer.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.Comparators;
import com.teamagly.friendizer.utils.Utility;

public abstract class FriendsAdapter extends ArrayAdapter<User> implements Filterable {

	public static String SORT_BY = "sort";
	protected LayoutInflater inflater;
	private List<User> allUsersList;
	private List<User> filteredUsersList;
	private UserFilter filter;
	private int sortBy;

	public FriendsAdapter(Context context, int textViewResourceId, List<User> objects) {
		super(context, textViewResourceId, objects);

		SharedPreferences settings = Utility.getSharedPreferences();
		sortBy = settings.getInt(SORT_BY, -1);

		allUsersList = new ArrayList<User>();
		allUsersList.addAll(objects);
		filteredUsersList = new ArrayList<User>();
		filteredUsersList.addAll(allUsersList);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		getFilter();
	}

	public FriendsAdapter(Context context, int textViewResourceId, List<User> objects, boolean sort) {
		this(context, textViewResourceId, objects);
		if (!sort)
			sortBy = -1;
	}

	public void setSortBy(int sortBy) {
		this.sortBy = sortBy;
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		if (filter == null)
			filter = new UserFilter();
		return filter;
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

	@Override
	public User getItem(int position) {
		return filteredUsersList.get(position);
	}

	@Override
	public int getCount() {
		return filteredUsersList.size();
	}

	@Override
	public void remove(User object) {
		allUsersList.remove(object);
		filteredUsersList.remove(object);
		notifyDataSetChanged();
	}

	@Override
	public void clear() {
		allUsersList.clear();
		filteredUsersList.clear();
		notifyDataSetChanged();
	}

	@Override
	public void add(User object) {
		allUsersList.add(object);
		filteredUsersList.add(object);
		notifyDataSetChanged();
	}

	@Override
	public void addAll(Collection<? extends User> collection) {
		for (User u : collection)
			allUsersList.add(u);
		filteredUsersList.addAll(collection);
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		switch (sortBy) {
		case -1: // Don't sort
			break;
		case 0:
			Collections.sort(allUsersList, (new Comparators()).new AlphabetComparator());
			Collections.sort(filteredUsersList, (new Comparators()).new AlphabetComparator());
			break;
		case 1:
			Collections.sort(allUsersList, (new Comparators()).new ValueComparator());
			Collections.sort(filteredUsersList, (new Comparators()).new ValueComparator());
			break;
		}
		super.notifyDataSetChanged();
	}

	private class UserFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			constraint = constraint.toString().toLowerCase();
			FilterResults result = new FilterResults();
			if (constraint != null && constraint.toString().length() > 0) {
				ArrayList<User> filteredItems = new ArrayList<User>();

				for (int i = 0, l = allUsersList.size(); i < l; i++) {
					User m = allUsersList.get(i);
					if (m.getName().toLowerCase().contains(constraint))
						filteredItems.add(m);
				}
				result.count = filteredItems.size();
				result.values = filteredItems;
			} else
				synchronized (this) {
					result.values = allUsersList;
					result.count = allUsersList.size();
				}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filteredUsersList = (ArrayList<User>) results.values;
			notifyDataSetChanged();
		}
	}
}