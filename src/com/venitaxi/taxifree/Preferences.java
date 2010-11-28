package com.venitaxi.taxi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.venitaxi.taxi.MyLocation.LocationResult;
 

public class Preferences extends PreferenceActivity {
	
	public JSONObject getJSONFromString(String jsonString) {
		JSONObject cities = null;
		try {
			InputStream is = new ByteArrayInputStream(jsonString.getBytes("UTF-8"));//this.getResources().openRawResource(R.raw.taxiratesjson);
		    byte[] buffer;
			buffer = new byte[is.available()];
		    while (is.read(buffer) != -1);
		    String jsontext = new String(buffer);
		    cities = new JSONObject(jsontext);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cities;
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.preferences); 
		getPreferenceManager()
				.setSharedPreferencesName("taxiCustomPreferences");
		addPreferencesFromResource(R.layout.preferences);
	}
	@Override
    protected void onStop(){
       super.onStop();
        try {
            SharedPreferences customSharedPreference = getSharedPreferences( "taxiCustomPreferences", MODE_PRIVATE);
            String citySelected = customSharedPreference.getString("listCityLocale", "nothing");
			//Toast.makeText(getBaseContext(), "pulled out: " + citySelected, Toast.LENGTH_LONG).show(); 
			JSONObject city = getJSONFromString(citySelected);
	        SharedPreferences.Editor editor = customSharedPreference.edit();
	        /*
	        add_charge_per_mile
	        add_increments_miles
	        charge_per_increment
	        city
	        initial_increment_miles
	        meter_drop
	        wait_time_charge
	        */
			editor.putString("add_charge_per_mile", city.getString("add_charge_per_mile"));
			editor.putString("add_increments_miles", city.getString("add_increments_miles"));
	        editor.putString("charge_per_increment", city.getString("charge_per_increment"));
			editor.putString("meter_drop", city.getString("meter_drop"));
			editor.putString("initial_increment_miles", city.getString("initial_increment_miles"));
	        editor.putString("wait_time_charge", String.valueOf(Double.parseDouble(city.getString("wait_time_charge"))/60.0));
		    // Commit the edits!
		    editor.commit();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO fix this catchall
			e.printStackTrace();
		}
    }

	public void updateLocation() {
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(this, locationResult);
	}

	public LocationResult locationResult = new LocationResult(){
	    @Override
	    public void gotLocation(final Location location){
	    	/*
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
			//Updater locationUpdater = new Updater(taxiUpdateServer);
			//int responseCode = locationUpdater.updateLocation(currentLocation, nameValuePairs);
            //Toast.makeText(getBaseContext(), "server: " + locationUpdater.updateServer + ", response code from server: " + responseCode, Toast.LENGTH_LONG).show();
	        //Got the location!
	         
	         */
	        };
	    };
        
}
