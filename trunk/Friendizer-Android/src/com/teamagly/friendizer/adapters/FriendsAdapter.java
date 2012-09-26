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

import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.teamagly.friendizer.R;
import com.teamagly.friendizer.filters.UserFilter;
import com.teamagly.friendizer.model.User;
import com.teamagly.friendizer.utils.Comparators;
import com.teamagly.friendizer.utils.Utility;

public abstract class FriendsAdapter extends ArrayAdapter<User> implements Filterable {

	public static String SORT_BY = "sort";
	public static String LAST_FILTER = "last_user_filter";
	protected LayoutInflater inflater;
	private List<User> allUsersList;
	private List<User> filteredUsersList;
	private AdapterFilter adapterFilter;
	private UserFilter userFilter;
	private int sortBy;
	private MenuItem filterMenuItem;

	public FriendsAdapter(Context context, int textViewResourceId, List<User> objects, MenuItem filterMenuItem) {
		super(context, textViewResourceId, objects);

		SharedPreferences prefs = Utility.getSharedPreferences();
		sortBy = prefs.getInt(SORT_BY, -1);
		this.filterMenuItem = filterMenuItem;

		allUsersList = new ArrayList<User>();
		allUsersList.addAll(objects);
		filteredUsersList = new ArrayList<User>();
		filteredUsersList.addAll(allUsersList);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		getFilter();
		// Retrieve the last used filter from the preferences
		String lastFilterStr = prefs.getString(LAST_FILTER, "");
		if (lastFilterStr.length() > 0)
			try {
				userFilter = new Gson().fromJson(lastFilterStr, UserFilter.class);
			} catch (JsonSyntaxException e) {
				userFilter = new UserFilter();
			}
		else
			userFilter = new UserFilter();
		filter(userFilter);
	}

	public FriendsAdapter(Context context, int textViewResourceId, List<User> objects, MenuItem filterMenuItem, boolean sort) {
		this(context, textViewResourceId, objects, filterMenuItem);
		if (!sort)
			sortBy = -1;
	}

	public void setSortBy(int sortBy) {
		this.sortBy = sortBy;
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		if (adapterFilter == null)
			adapterFilter = new AdapterFilter();
		return adapterFilter;
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
		filter(userFilter);
		notifyDataSetChanged();
	}

	@Override
	public void addAll(Collection<? extends User> collection) {
		allUsersList.addAll(collection);
		filter(userFilter);
		notifyDataSetChanged();
	}

	@Override
	public boolean isEmpty() {
		return filteredUsersList.isEmpty();
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

	public void filter(UserFilter filter) {
		userFilter = filter;
		getFilter().filter(new Gson().toJson(filter).toString());
		// Save the new filter
		SharedPreferences.Editor editor = Utility.getSharedPreferences().edit();
		editor.putString(LAST_FILTER, new Gson().toJson(filter));
		editor.commit();
		if (filterMenuItem != null) // Update the menu item
			filterMenuItem.setIcon(isFiltered() ? R.drawable.ic_action_filter_on : R.drawable.ic_action_filter);
	}

	public void resetFilter() {
		userFilter = new UserFilter();
		getFilter().filter("RESET");
		// Save the new filter
		SharedPreferences.Editor editor = Utility.getSharedPreferences().edit();
		editor.putString(LAST_FILTER, "");
		editor.commit();
		if (filterMenuItem != null) // Update the menu item
			filterMenuItem.setIcon(R.drawable.ic_action_filter);
	}

	public UserFilter getCurrentFilter() {
		return userFilter;
	}

	private class AdapterFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence jsonConstraint) {
			FilterResults result = new FilterResults();
			if (jsonConstraint.equals("RESET")) {
				synchronized (this) {
					result.values = allUsersList;
					result.count = allUsersList.size();
				}
				return result;
			}
			ArrayList<User> filteredItems = new ArrayList<User>();
			UserFilter filter = new Gson().fromJson(jsonConstraint.toString(), UserFilter.class);
			for (User user : allUsersList)
				if (filter.satistfies(user))
					filteredItems.add(user);
			synchronized (this) {
				result.count = filteredItems.size();
				result.values = filteredItems;
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

	/**
	 * @return true iff the adapter is filtered
	 */
	public boolean isFiltered() {
		return !userFilter.isBlank();
	}
}