package com.venitaxi.taxi;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
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
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class TaxiMain extends MapActivity implements IDirectionsListener {
	LinearLayout linearLayout;
	MapView mapView;
	MapController mapController;
	GeoPoint currentLocation;
	TaxiItemizedOverlay itemizedoverlay;


	public void setDirections(GeoPoint start, GeoPoint end) {
		DrivingDirections.Mode mode = Mode.DRIVING; // or Mode.WALKING
		DrivingDirections directions = DrivingDirectionsFactory
			.createDrivingDirections();
		directions.driveTo(start, end, mode, TaxiMain.this);
	}

	@Override
		public void onCreate(Bundle savedInstanceState) {


			super.onCreate(savedInstanceState);
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
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
					&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
				Geocoder geoCoder = new Geocoder(getBaseContext(), Locale
					.getDefault());
				try {
				List<Address> addresses = geoCoder.getFromLocationName(
					edittext.getText().toString(), 5);
				if (addresses.size() > 0) {
				GeoPoint p = new GeoPoint((int) (addresses.get(0)
						.getLatitude() * 1E6), (int) (addresses
						.get(0).getLongitude() * 1E6));
				// hide the keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(edittext
					.getWindowToken(), 0);

				// Toast.makeText(TaxiMain.this, "going to: '" +
				// edittext.getText(), Toast.LENGTH_SHORT).show();
				mapController.animateTo(p);

				OverlayItem overlayitem = new OverlayItem(p,
						"Destination", "Hi there.");
				itemizedoverlay.addOverlay(overlayitem);
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

			// Register the listener with the Location Manager to receive location
			// updates
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					20, 0, locationListener);
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
		/*Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		  try {
		  List<Address> addresses = geoCoder.getFromLocationName("1806 8th ave, 91810", 5);
		  if (addresses.size() > 0) {
		 */
		GeoPoint p = new GeoPoint((int) (47.61390 * 1E6), (int) (-122.33406 * 1E6));
		if(p != null && currentLocation != null) {
			Toast.makeText(TaxiMain.this, "go home", Toast.LENGTH_SHORT).show();
			OverlayItem overlayitem = new OverlayItem(p,"Hola, Mundo!", "I'm in Mexico City!");
			itemizedoverlay.addOverlay(overlayitem);
			setDirections(currentLocation, p);
			mapView.invalidate();

		} else {
			Toast.makeText(TaxiMain.this, "cannot resolve coordinates for home, try again later", Toast.LENGTH_SHORT).show();
		}
		/*
		   } 
		   } catch(Exception e) {
		   }
		 */
		return super.onOptionsItemSelected(item);
	}
}
