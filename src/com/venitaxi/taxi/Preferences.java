package com.venitaxi.taxi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;
 
public class Preferences extends PreferenceActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                getPreferenceManager().setSharedPreferencesName("taxiCustomPreferences");
                addPreferencesFromResource(R.layout.preferences);
                // Get the custom preference
                Preference customPref = (Preference) findPreference("updateNow");
                customPref
                                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
                                        public boolean onPreferenceClick(Preference preference) {
                                                Toast.makeText(getBaseContext(),
                                                                "Updating server with current location",
                                                                Toast.LENGTH_LONG).show();
                                                SharedPreferences customSharedPreference = getSharedPreferences(
                                                                "taxiCustomPreferences", Activity.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = customSharedPreference.edit();
                                                editor.putString("updateNow", "Updating server"); 
                                                editor.commit();
                                                return true;
                                        }
 
                                });
        }
}
