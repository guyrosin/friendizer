/**
 * 
 */
package com.teamagly.friendizer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Guy
 *
 */
public class Prefs extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
    }
}
