/**
 * 
 */
package com.teamagly.friendizer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * @author Guy
 * 
 */
public class FriendsPrefs extends PreferenceActivity {
    SharedPreferences prefs;
    OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.settings);
	prefs = PreferenceManager.getDefaultSharedPreferences(this);

	listener = new OnSharedPreferenceChangeListener() {
	    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		SharedPreferences.Editor editor = prefs.edit();
		if (key.equals("friends_list_type")) {
		    CheckBoxPreference friendsListType = (CheckBoxPreference) getPreferenceScreen().findPreference(key);
		    editor.putBoolean(key, friendsListType.isChecked());
		    editor.commit();
		} else if (key.equals("troll")) {
		    ;
		}
	    }
	};
    }
}
