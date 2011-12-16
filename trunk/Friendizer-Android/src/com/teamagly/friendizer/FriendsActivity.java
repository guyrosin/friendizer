package com.teamagly.friendizer;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class FriendsActivity extends ListActivity {
	static final ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We'll define a custom screen layout here (the one shown above), but
		// typically, you could just use the standard ListActivity layout.
		// setContentView(R.layout.friends_layout);

		// Query for all people contacts using the Contacts.People convenience class.
		// Put a managed wrapper around the retrieved cursor so we don't have to worry about
		// requerying or closing it as the activity changes state.
		// mCursor = this.getContentResolver().query(People.CONTENT_URI, null, null, null, null);
		// startManagingCursor(mCursor);
		//
		// // Now create a new list adapter bound to the cursor.
		// // SimpleListAdapter is designed for binding to a Cursor.
		// ListAdapter adapter = new SimpleCursorAdapter(
		// this, // Context.
		// R.layout.friend_list_item, // Specify the row template to use (here, two columns bound to the two retrieved cursor rows).
		// mCursor, // Pass in the cursor to bind to.
		// new String[]{}, //new String[] {People.pic, People.name, People.age, People.value}, // Array of cursor columns to bind to.
		// new int[] {android.R.id.text1, android.R.id.text2}); // Parallel array of which template objects to bind to those columns.
		//
		// // Bind to our new adapter.
		// setListAdapter(adapter);

		setContentView(R.layout.friends_layout);
		SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.friend_list_item,
				new String[] { "name", "age", "value", "matching" }, new int[] { R.id.name, R.id.age, R.id.value, R.id.matching }

		);
		populateList();
		setListAdapter(adapter);
	}

	private void populateList() {
		HashMap<String, String> temp = new HashMap<String, String>();
		temp.put("name", "Guy Rosin");
		temp.put("age", "20");
		temp.put("value", "20,000");
		temp.put("matching", "70%");
		list.add(temp);
		HashMap<String, String> temp1 = new HashMap<String, String>();
		temp1.put("name", "Aviv Charikar");
		temp1.put("age", "20");
		temp1.put("value", "20,000");
		temp1.put("matching", "70%");
		list.add(temp1);
		HashMap<String, String> temp2 = new HashMap<String, String>();
		temp2.put("name", "Leon Pruger");
		temp2.put("age", "21");
		temp2.put("value", "20,000");
		temp2.put("matching", "70%");
		list.add(temp2);
		HashMap<String, String> temp3 = new HashMap<String, String>();
		temp3.put("name", "Yarden Ron");
		temp3.put("age", "20");
		temp3.put("value", "20,000");
		temp3.put("matching", "70%");
		list.add(temp3);
		HashMap<String, String> temp4 = new HashMap<String, String>();
		temp4.put("name", "Trollo Lol");
		temp4.put("age", "99");
		temp4.put("value", "99,999");
		temp4.put("matching", "100%");
		list.add(temp4);
		HashMap<String, String> temp5 = new HashMap<String, String>();
		temp5.put("name", "Forever Alone");
		temp5.put("age", "30");
		temp5.put("value", "1");
		temp5.put("matching", "1%");
		list.add(temp5);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Object o = this.getListAdapter().getItem(position);
		String dude = o.toString();
		Toast.makeText(this, "You have chosen " + dude, Toast.LENGTH_LONG).show();
	}
}