package com.venitaxi.taxi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.venitaxi.taxi.MyLocation.LocationResult;
 
public class Preferences extends PreferenceActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                getPreferenceManager().setSharedPreferencesName("taxiCustomPreferences");
                addPreferencesFromResource(R.layout.preferences);
                // Get the custom preference
                Preference customPref = (Preference) findPreference("updateNow");
                customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                            //Toast.makeText(getBaseContext(), "Updating server with current location", Toast.LENGTH_LONG).show();
                    		updateLocation();
                            return true;
                            }
                    });
        }
	public void updateLocation() {
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(this, locationResult);
	}

	public LocationResult locationResult = new LocationResult(){
	    @Override
	    public void gotLocation(final Location location){
			Double latitude = location.getLatitude() * 1E6;
			Double longitude = location.getLongitude() * 1E6;
			GeoPoint currentLocation = new GeoPoint(latitude.intValue(), longitude.intValue()); 
            SharedPreferences customSharedPreference = getSharedPreferences( "taxiCustomPreferences", MODE_PRIVATE);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);  
            String userName = customSharedPreference.getString("userName", null);
            String availability = customSharedPreference.getString("listStateAvailability", null);
			nameValuePairs.add(new BasicNameValuePair("name", userName));  
			nameValuePairs.add(new BasicNameValuePair("state", availability)); 
			String timeStamp = Long.toString(Calendar.getInstance().getTimeInMillis()/1000);
			nameValuePairs.add(new BasicNameValuePair("timestamp", timeStamp));  
			String taxiUpdateServer = customSharedPreference.getString("taxiUpdateServer", "taxivici.com");
			Updater locationUpdater = new Updater(taxiUpdateServer);
			int responseCode = locationUpdater.updateLocation(currentLocation, nameValuePairs);
            Toast.makeText(getBaseContext(), "server: " + locationUpdater.updateServer + ", response code from server: " + responseCode, Toast.LENGTH_LONG).show();
	        //Got the location!
	        };
	    };
        
}
