package com.venitaxi.taxi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amelie.driving.DrivingDirections;
import com.amelie.driving.DrivingDirectionsFactory;
import com.amelie.driving.Route;
import com.amelie.driving.DrivingDirections.IDirectionsListener;
import com.amelie.driving.DrivingDirections.Mode;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.venitaxi.taxi.MyLocation.LocationResult;

//git test
public class TaxiMain extends MapActivity implements IDirectionsListener {
	LinearLayout linearLayout;
	MapView mapView;
	MapController mapController;
	GeoPoint currentLocation;
	Address currentAddress = null;
	String currentAddressString = null;
	TaxiItemizedOverlay itemizedoverlay;
	public boolean CheckboxPreference;
    public static String updateNow;
    public static String homeAddress;
    public static SharedPreferences customSharedPreference;
    public static Updater locationUpdater;
	public static Geocoder geoCoder;
	
	public LocationResult locationResult = new LocationResult() {
	    @Override
	    public void gotLocation(final Location location){
	        //Got the location!
			Double latitude = location.getLatitude() * 1E6;
			Double longitude = location.getLongitude() * 1E6;
			currentLocation = new GeoPoint(latitude.intValue(), longitude.intValue()); 
			try {
				List<Address> addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
            	//Toast.makeText(TaxiMain.this, "geocoding ...", Toast.LENGTH_SHORT).show();
				if (addresses.size() > 0) {
					currentAddress = addresses.get(0);
					String address = "";
					for(int i = 0; i < currentAddress.getMaxAddressLineIndex(); i++) {
						address += " " + currentAddress.getAddressLine(i);
					}
					currentAddressString = address;
					//Toast.makeText(TaxiMain.this, "got an address: " + address , Toast.LENGTH_SHORT).show();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        };
	 };

	@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			customSharedPreference = getSharedPreferences( "taxiCustomPreferences", MODE_PRIVATE);
			geoCoder = new Geocoder(getBaseContext(), Locale.getDefault()); 
			setContentView(R.layout.main);
			
			mapView = (MapView) findViewById(R.id.mapview);
			mapController = mapView.getController();
			mapView.setBuiltInZoomControls(true);
			/* FIXME: this may need to be ran continuously, as the current implementation 
			 * will stop listening after first location is found */
			MyLocation myLocation = new MyLocation();
			myLocation.getLocation(this, locationResult);

			List<Overlay> mapOverlays = mapView.getOverlays();
			Drawable drawable = TaxiMain.this.getResources().getDrawable( R.drawable.ltblu_pushpin);
			itemizedoverlay = new TaxiItemizedOverlay(drawable);
			
			/* FIXME: this is needed for some reason as if there are no markers on the layer it crashes :( */
			GeoPoint p = new GeoPoint(0, 0);
			OverlayItem overlayitem = new OverlayItem(p,"Hola, Mundo!", "I'm in Mexico City!");
			itemizedoverlay.addOverlay(overlayitem);
			mapOverlays.add(itemizedoverlay);
			
			final EditText edittext = (EditText) findViewById(R.id.edittext);
            String  homeAddress = customSharedPreference.getString("homeAddress", "");
            edittext.setText(homeAddress);
			edittext.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) { 
					String destinationAddress = edittext.getText().toString();
					overlayRouteInformationFromAddress(destinationAddress);
					// hide the keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
					return true;
				}
				return false;
				}
			});
			/* disabled for now 
			String taxiUpdateServer = customSharedPreference.getString("taxiUpdateServer", "taxivici.com");
			locationUpdater = new Updater(taxiUpdateServer);
			*/
			/* setup screen FIXME */
            String  cityLocale = customSharedPreference.getString("listCityLocale", "");
            if(cityLocale.equals("")) {
            	Toast.makeText(TaxiMain.this, "Please choose your city in Menu/Preferences, or select Other and then use Fare preferences to set taxi charges", Toast.LENGTH_LONG).show();
            }
            if(homeAddress.equals("")) {
            	Toast.makeText(TaxiMain.this, "Please provide your home address in the Preferences menu item.", Toast.LENGTH_LONG).show();
            }

	}
	
	public void overlayRouteInformationFromAddress(String destinationAddress) {
		try {
			List<Address> addresses = geoCoder.getFromLocationName(destinationAddress, 5);
			if (addresses.size() > 0) {
				GeoPoint destination = new GeoPoint((int) (addresses.get(0)
						.getLatitude() * 1E6), (int) (addresses
								.get(0).getLongitude() * 1E6));

				/* add the marker for destination */
				OverlayItem overlayitem = new OverlayItem(destination, "Destination", "Hi there.");
				itemizedoverlay.addOverlay(overlayitem);
				/* add the marker for current location */
				/*
				MyLocation myLocation = new MyLocation();
				myLocation.getLocation(this, locationResult);
				*/
				OverlayItem currentLocationItem = new OverlayItem(currentLocation, "Current Location", "Click me");
				itemizedoverlay.addOverlay(currentLocationItem);

				// draw the path between current location and destination
				setDirections(currentLocation, destination);
				mapController.animateTo(destination);
				mapView.invalidate();
			} else {
				Toast.makeText(
						TaxiMain.this,
						"address: '" + destinationAddress
						+ "' not found, please try again and maybe add city/state?",
						Toast.LENGTH_SHORT).show();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setDirections(GeoPoint start, GeoPoint end) {
		DrivingDirections.Mode mode = Mode.DRIVING; // or Mode.WALKING
		DrivingDirections directions = DrivingDirectionsFactory.createDrivingDirections();
		directions.driveTo(start, end, mode, TaxiMain.this);
	}
	
	@Override
    protected void onStop(){
       super.onStop();
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onDirectionsAvailable(Route route, Mode mode) {
		/* this should draw the route */
		itemizedoverlay.route = route;
		itemizedoverlay.mode = mode;
		mapController.setZoom(14);
		mapView.invalidate();
		try {
			Double distance = Double.parseDouble(route.getTotalDistance().split(" ")[0]);
			String unitDistance = route.getTotalDistance().split(" ")[3]; // time in minutes
			Double minutes = Double.parseDouble(route.getTotalDistance().split(" ")[3]); // time in minutes
			String unitTime = route.getTotalDistance().split(" ")[4]; // time in minutes
			unitTime = unitTime.substring(0, unitTime.length() - 1);
			if(unitTime.equals("hours")) {
				minutes = minutes * 60.0;
			}
			/* FIXME: need to check what the returned values are if not 'mins' or 'miles' convert */

			Intent myIntent = new Intent();
			
			//Toast.makeText(TaxiMain.this, "sending: " + currentAddressString , Toast.LENGTH_SHORT).show();
			myIntent.putExtra("currentAddress", currentAddressString);
			myIntent.putExtra("distance", distance);
			myIntent.putExtra("minutes", minutes);
			myIntent.putExtra("unitDistance", unitDistance);
			myIntent.putExtra("unitTime", unitTime);
			myIntent.setClassName("com.venitaxi.taxi", "com.venitaxi.taxi.Cost");
			myIntent.setPackage("com.venitaxi.taxi");
			/* show the cost screen */
			startActivity(myIntent);
		} catch(Exception e) {

		}
	}

	@Override
	public void onDirectionsNotAvailable() {
		// TODO Auto-generated method stub
		Toast.makeText(TaxiMain.this, "sorry cannot find your location, try adding city/state?", Toast.LENGTH_SHORT).show();

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.taxi_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String title = item.getTitle().toString();
		if(title.equals("go home")) {
            homeAddress = customSharedPreference.getString("homeAddress", null);
            GeoPoint p = null;
            if(homeAddress != null) {
				List<Address> addresses;
				try {
					addresses = geoCoder.getFromLocationName( homeAddress, 5);
					if (addresses.size() > 0) {
						p = new GeoPoint((int) (addresses.get(0)
								.getLatitude() * 1E6), (int) (addresses
								.get(0).getLongitude() * 1E6));
					}
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(TaxiMain.this, "cannot find: " + homeAddress + ", try being more specific, add city/state?", Toast.LENGTH_SHORT).show();
				} 
            }
			if(p != null && currentLocation != null) {
				OverlayItem overlayitem = new OverlayItem(p,"Hola, Mundo!", "I'm in Mexico City!");
				itemizedoverlay.addOverlay(overlayitem);
				setDirections(currentLocation, p);
				mapView.invalidate();
			} else {
				Toast.makeText(TaxiMain.this, "cannot resolve coordinates for home, is the GPS on?", Toast.LENGTH_SHORT).show();
			}
		} else if(title.equals("preferences")) { 
			Intent myIntent = new Intent();
			myIntent.setClassName("com.venitaxi.taxi", "com.venitaxi.taxi.Preferences");
			myIntent.setPackage("com.venitaxi.taxi");
			startActivity(myIntent);
		} else if(title.equals("here")) { 
				if(currentLocation != null) {
					Toast.makeText(TaxiMain.this, "You are here, or close to: " + currentAddressString, Toast.LENGTH_LONG).show();
					OverlayItem overlayitem = new OverlayItem(currentLocation, "Hola, Mundo!", "I'm in Mexico City!");
					itemizedoverlay.addOverlay(overlayitem);
					mapController.animateTo(currentLocation);
					mapController.setZoom(16);
					mapView.invalidate();
				} else {
					Toast.makeText(TaxiMain.this, "GPS is warming up, please try again ..", Toast.LENGTH_SHORT).show();
				}
		}
   return super.onOptionsItemSelected(item);
	}
	public void updateLocation() {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);  
            String userName = customSharedPreference.getString("userName", null);
            String availability = customSharedPreference.getString("listStateAvailability", null);
			nameValuePairs.add(new BasicNameValuePair("name", userName));  
			nameValuePairs.add(new BasicNameValuePair("state", availability)); 
			String timeStamp = Long.toString(Calendar.getInstance().getTimeInMillis()/1000);
			nameValuePairs.add(new BasicNameValuePair("timestamp", timeStamp));  
			int responseCode = locationUpdater.updateLocation(currentLocation, nameValuePairs);
			Toast.makeText(TaxiMain.this, "update response code: " + responseCode + ", server: " + locationUpdater.updateServer, Toast.LENGTH_SHORT).show();
	}
public class ReverseGeocodeLookupTask extends AsyncTask <Void, Void, GeoPoint> {
		
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			this.progressDialog = ProgressDialog.show(
					TaxiMain.this,
					"Please wait...contacting Google!", // title
					"Requesting reverse geocode lookup", // message
					true // indeterminate
			);
		}

		@Override
		protected GeoPoint doInBackground(Void... params) {
			return new TaxiGeoCoder().reverseGeoCode("seattle, wa", TaxiMain.this);
		}

		@Override
		protected void onPostExecute(GeoPoint result) {
			this.progressDialog.cancel();
			Toast.makeText(TaxiMain.this, result.toString(), Toast.LENGTH_LONG).show();			
		}
		
	}
}
