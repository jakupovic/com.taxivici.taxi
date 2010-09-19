package com.venitaxi.taxi;

import java.io.IOException;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.venitaxi.taxi.Preferences;

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

    private void getPrefs() {
            // Get the xml/preferences.xml preferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext()); 
            /*
           SharedPreferences customSharedPreference = getSharedPreferences( "taxiCustomPreferences", MODE_PRIVATE);
           SharedPreferences.Editor editor = customSharedPreference.edit();
           */
            CheckboxPreference = prefs.getBoolean("checkboxPref", true);
            ListPreference = prefs.getString("listStateAvailability", "0");
            userName = prefs.getString("userName", "XXXX");
            homeAddress = prefs.getString("homeAddress", "");
            taxiUpdateServer = prefs.getString("taxiUpdateServer", "http://taxivici.com");
            // Get the custom preference
            SharedPreferences mySharedPreferences = getSharedPreferences(
                            "taxiCustomPreferences", MODE_PRIVATE);
            updateNow = mySharedPreferences.getString("updateNow", "");
    }

	public void setDirections(GeoPoint start, GeoPoint end) {
		DrivingDirections.Mode mode = Mode.DRIVING; // or Mode.WALKING
		DrivingDirections directions = DrivingDirectionsFactory
			.createDrivingDirections();
		directions.driveTo(start, end, mode, TaxiMain.this);
	}
/*
	@Override
	public void onStart() {
		getPrefs();
		
	}
	*/
	@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			customSharedPreference = getSharedPreferences( "taxiCustomPreferences", MODE_PRIVATE);
			// setContentView(R.layout.main);
			setContentView(R.layout.main);
			final Button button = (Button) findViewById(R.id.button);
			button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
					// Perform action on clicks

					// GeoPoint point = new GeoPoint(19240000,-99120000);
					if(currentLocation != null) {
						Toast.makeText(TaxiMain.this, "current location",
							Toast.LENGTH_SHORT).show();
						OverlayItem overlayitem = new OverlayItem(currentLocation,
							"Hola, Mundo!", "I'm in Mexico City!");
	
						itemizedoverlay.addOverlay(overlayitem);
						mapController.animateTo(currentLocation);
						mapController.setZoom(16);
						mapView.invalidate();
					} else {
						Toast.makeText(TaxiMain.this, "current location not available, make sure your gps is on", Toast.LENGTH_SHORT).show();
					}

					}
					});

			mapView = (MapView) findViewById(R.id.mapview);
			mapController = mapView.getController();
			mapView.setBuiltInZoomControls(true);

			List<Overlay> mapOverlays = mapView.getOverlays();
			Drawable drawable = TaxiMain.this.getResources().getDrawable(
					R.drawable.ltblu_pushpin);
			itemizedoverlay = new TaxiItemizedOverlay(drawable);
			GeoPoint point = new GeoPoint(19240000, -99120000);
			OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!",
					"I'm in Mexico City!");

			itemizedoverlay.addOverlay(overlayitem);
			mapOverlays.add(itemizedoverlay);

			// Acquire a reference to the system Location Manager
			LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

			// Define a listener that responds to location updates
			LocationListener locationListener = new LocationListener() {
				public void onLocationChanged(Location location) {
					// Called when a new location is found by the network location
					// provider.
					Double latitude = location.getLatitude() * 1E6;
					Double longitude = location.getLongitude() * 1E6;

					currentLocation = new GeoPoint(latitude.intValue(), longitude
							.intValue());
					// good stuff
					// http://mobiforge.com/developing/story/using-google-maps-android

				}

				public void onStatusChanged(String provider, int status,
						Bundle extras) {
				}

				public void onProviderEnabled(String provider) {
				}

				public void onProviderDisabled(String provider) {
				}
			};
			final EditText edittext = (EditText) findViewById(R.id.edittext);
			edittext.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) { 
					try {
						Geocoder geoCoder = new Geocoder(getBaseContext(), Locale .getDefault()); 
						List<Address> addresses = geoCoder.getFromLocationName(
							edittext.getText().toString(), 5);
						if (addresses.size() > 0) {
							GeoPoint p = new GeoPoint((int) (addresses.get(0)
									.getLatitude() * 1E6), (int) (addresses
									.get(0).getLongitude() * 1E6));
							// hide the keyboard
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(edittext .getWindowToken(), 0);
							
							mapController.animateTo(p);
							
							/* add the marker for destination */
							OverlayItem overlayitem = new OverlayItem(p, "Destination", "Hi there.");
							itemizedoverlay.addOverlay(overlayitem);
							/* add the marker for current location */
							OverlayItem currentLocationItem = new OverlayItem(currentLocation, "Current Location", "Click me");
							itemizedoverlay.addOverlay(currentLocationItem);
							
							// draw the path between current location and destination
							setDirections(currentLocation, p);
							
							mapView.invalidate();
							return true;
						} else {
							Toast.makeText(
									TaxiMain.this,
									"address: '" + edittext.getText()
									+ "' not found, please try again",
									Toast.LENGTH_SHORT).show();
							return true;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;
				}
				return false;
				}
			});
			// Register the listener with the Location Manager to receive location // updates 
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20, 100, locationListener); 
	}
	@Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("silentMode", userName);

      // Commit the edits!
      editor.commit();
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
	public double calculateFare(double distance, double minutes) {
		double fare = 2.50; // initial fare
		if (distance > 1) {
			fare += (distance - 1) * 2.0;
		}
		fare += minutes * 0.5; // is this right?
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
			double distance = Double.parseDouble(route.getTotalDistance().split(" ")[0]);
			String time = route.getTotalDistance().split(" ")[3]; // time in minutes
			/* FIXME: need to check what the returned values are if not 'mins' or 'miles' convert */

			Toast.makeText(
					TaxiMain.this,
					"distance: " + distance + ", time: "
					+ time + ", fare: $"
					+ calculateFare(distance, Double.parseDouble(time)),
					Toast.LENGTH_LONG).show();
		} catch(Exception e) {

		}
	}

	@Override
	public void onDirectionsNotAvailable() {
		// TODO Auto-generated method stub
		Toast.makeText(TaxiMain.this, "directions NOT available",
				Toast.LENGTH_SHORT).show();

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
		Toast.makeText(TaxiMain.this, item.getTitle(), Toast.LENGTH_SHORT).show();
		String title = item.getTitle().toString();
		/*try {*/
			if(title.equals("home")) {
                homeAddress = customSharedPreference.getString("homeAddress", null);
                GeoPoint p = null;
                if(homeAddress != null) {
					Geocoder geoCoder = new Geocoder(getBaseContext(), Locale .getDefault()); 
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
			}
	   return super.onOptionsItemSelected(item);
	}
}
