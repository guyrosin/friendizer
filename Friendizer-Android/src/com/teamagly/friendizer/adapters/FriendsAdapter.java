package com.teamagly.friendizer.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.teamagly.friendizer.model.User;

public abstract class FriendsAdapter extends ArrayAdapter<User> implements Filterable {

	protected LayoutInflater inflater;
	private List<User> allUsersList;
	private List<User> filteredUsersList;
	private UserFilter filter;

	public FriendsAdapter(Context context, int textViewResourceId, List<User> objects) {
		super(context, textViewResourceId, objects);
		allUsersList = new ArrayList<User>();
		allUsersList.addAll(objects);
		filteredUsersList = new ArrayList<User>();
		filteredUsersList.addAll(allUsersList);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		getFilter();
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
		super.remove(object);
		filteredUsersList.remove(object);
	}

	@Override
	public void clear() {
		super.clear();
		filteredUsersList.clear();
	}

	@Override
	public void add(User object) {
		super.add(object);
		filteredUsersList.add(object);
	}

	@Override
	public void addAll(Collection<? extends User> collection) {
		for (User u : collection)
			super.add(u);
		filteredUsersList.addAll(collection);
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
			//			clear();
			//			for (int i = 0, l = filteredUsersList.size(); i < l; i++)
			//				add(filteredUsersList.get(i));
			//			notifyDataSetInvalidated();
		}
	}
}