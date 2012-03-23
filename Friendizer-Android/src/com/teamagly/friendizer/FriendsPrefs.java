/**
 * 
 */
package com.teamagly.friendizer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * @author Guy
 * 
 */
public class FriendsPrefs extends PreferenceActivity {
    SharedPreferences prefs;
    OnSharedPreferenceChangeListener listener;
    ProgressDialog dialog;

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
		}
	    }
	};
	Preference logoutBtn = (Preference) findPreference("logout");
	logoutBtn.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	    public boolean onPreferenceClick(Preference preference) {
		Intent intent = new Intent(FriendsPrefs.this, SplashActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
		Bundle bundle = new Bundle();
		bundle.putBoolean("logout", true);
		intent.putExtras(bundle);
		startActivity(intent);
		finish();
		return true;
	    }
	});
    }

}
