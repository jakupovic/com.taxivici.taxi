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
	TaxiItemizedOverlay itemizedoverlay;
	public static final String PREFS_NAME = "MyPrefsFile";
	public static String userName;
	public boolean CheckboxPreference;
    public static String ListPreference;
    public static String ringtonePreference;
    public static String taxiUpdateServer;
    public static String updateNow;
    public static String homeAddress;
    public static SharedPreferences customSharedPreference;
    public static Updater locationUpdater;
	private Geocoder geoCoder;
	
	public LocationResult locationResult = new LocationResult() {
	    @Override
	    public void gotLocation(final Location location){
	        //Got the location!
			Double latitude = location.getLatitude() * 1E6;
			Double longitude = location.getLongitude() * 1E6;
			currentLocation = new GeoPoint(latitude.intValue(), longitude.intValue()); 
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
			mapOverlays.add(itemizedoverlay);

			// Acquire a reference to the system Location Manager
			/*
			LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			Location loc = locationManager.getLastKnownLocation(Context.LOCATION_SERVICE);
			this.setCurrrentLocation(loc);

 			// Define a listener that responds to location updates
			// Called when a new location is found by the network location
			// provider.
			LocationListener locationListener = new LocationListener() {
				public void onLocationChanged(Location location) {
					Double latitude = location.getLatitude() * 1E6;
					Double longitude = location.getLongitude() * 1E6;
					currentLocation = new GeoPoint(latitude.intValue(), longitude.intValue()); 
					// good stuff http://mobiforge.com/developing/story/using-google-maps-android
				}
				public void onStatusChanged(String provider, int status, Bundle extras) {}
				public void onProviderEnabled(String provider) {}
				public void onProviderDisabled(String provider) {}
			};
			// Register the listener with the Location Manager to receive location // updates 
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20, 100, locationListener); 
			*/
			
			final EditText edittext = (EditText) findViewById(R.id.edittext);
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
			
			String taxiUpdateServer = customSharedPreference.getString("taxiUpdateServer", "taxivici.com");
			locationUpdater = new Updater(taxiUpdateServer);

	}
	
	public void overlayRouteInformationFromAddress(String destinationAddress) {
		try {
			//Toast.makeText(TaxiMain.this, "looking for: " + destinationAddress, Toast.LENGTH_SHORT).show();
			List<Address> addresses = geoCoder.getFromLocationName(destinationAddress, 5);
			if (addresses.size() > 0) {
				GeoPoint destination = new GeoPoint((int) (addresses.get(0)
						.getLatitude() * 1E6), (int) (addresses
								.get(0).getLongitude() * 1E6));

				/* add the marker for destination */
				OverlayItem overlayitem = new OverlayItem(destination, "Destination", "Hi there.");
				itemizedoverlay.addOverlay(overlayitem);
				/* add the marker for current location */
				MyLocation myLocation = new MyLocation();
				myLocation.getLocation(this, locationResult);
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
						+ "' not found, please try again",
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

	/*
	 * Approximate Metered Rates
	 * 
	 * $2.50 for the first 1 mile or less $2.00 for each additional mile $0.50
	 * for wait time per minute $0.50 for each passenger above two.
	 * 
	 * Flat Rates
	 * 
	 * Downtown to Sea-Tax Airport: $32.00
	 */
	public static double calculateFare(double distance, double minutes) {
        Double meterDrop = Double.valueOf(customSharedPreference.getString("taxiMeterDropCost", "2.50"));
        Double taxiUnitDistanceCost = Double.valueOf(customSharedPreference.getString("taxiUnitDistanceCost", "2.00"));
        Double taxiMinuteWaitCost = Double.valueOf(customSharedPreference.getString("taxiMinuteWaitCost", "0.50"));
		double fare = meterDrop; 
		if (distance > 1) {
			fare += (distance - 1) * taxiUnitDistanceCost;
		}
		fare += minutes * taxiMinuteWaitCost; // is this right?
		return fare;
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

			/*Toast.makeText(
					TaxiMain.this,
					"distance: " + distance + ", time: "
					+ minutes + ", fare: $"
					+ calculateFare(distance, minutes),
					Toast.LENGTH_LONG).show();
					*/
			/* show the cost screen */
			Intent myIntent = new Intent();
			myIntent.putExtra("distance", distance);
			myIntent.putExtra("minutes", minutes);
			myIntent.putExtra("unitDistance", unitDistance);
			myIntent.putExtra("unitTime", unitTime);
			myIntent.setClassName("com.venitaxi.taxi", "com.venitaxi.taxi.Cost");
			myIntent.setPackage("com.venitaxi.taxi");
			startActivity(myIntent);
		} catch(Exception e) {

		}
	}

	@Override
	public void onDirectionsNotAvailable() {
		// TODO Auto-generated method stub
		Toast.makeText(TaxiMain.this, "directions NOT available", Toast.LENGTH_SHORT).show();

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
		/*try {*/
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
						Toast.makeText(TaxiMain.this, "cannot find address: " + homeAddress, Toast.LENGTH_SHORT).show();
					} 
                }
				if(p != null && currentLocation != null) {
					OverlayItem overlayitem = new OverlayItem(p,"Hola, Mundo!", "I'm in Mexico City!");
					itemizedoverlay.addOverlay(overlayitem);
					setDirections(currentLocation, p);
					mapView.invalidate();
				} else {
					Toast.makeText(TaxiMain.this, "cannot resolve coordinates for home, try again later", Toast.LENGTH_SHORT).show();
				}
			} else if(title.equals("preferences")) { 
				Intent myIntent = new Intent();
				myIntent.setClassName("com.venitaxi.taxi", "com.venitaxi.taxi.Preferences");
				myIntent.setPackage("com.venitaxi.taxi");
				startActivity(myIntent);
				/*
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
				startActivity(myIntent);
				Toast.makeText(TaxiMain.this, "Hello person", Toast.LENGTH_SHORT).show();
				*/
			} else if(title.equals("here")) { 
					Toast.makeText(TaxiMain.this, "current location", Toast.LENGTH_SHORT).show();
					OverlayItem overlayitem = new OverlayItem(currentLocation, "Hola, Mundo!", "I'm in Mexico City!");
					itemizedoverlay.addOverlay(overlayitem);
					mapController.animateTo(currentLocation);
					mapController.setZoom(16);
					mapView.invalidate();
					updateLocation();
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
