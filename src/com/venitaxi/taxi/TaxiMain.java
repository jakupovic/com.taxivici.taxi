package com.venitaxi.taxi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

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
	String currentAddressString = "";
	String destinationAddressString = "";
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
			List<Overlay> mapOverlays = mapView.getOverlays();
			Drawable drawable = TaxiMain.this.getResources().getDrawable( R.drawable.ltblu_pushpin);
			itemizedoverlay = new TaxiItemizedOverlay(drawable);
			
			/* FIXME: this is needed for some reason as if there are no markers on the layer it crashes :( */
			GeoPoint p = new GeoPoint(0, 0);
			OverlayItem overlayitem = new OverlayItem(p,"Hola, Mundo!", "I'm in Mexico City!");
			itemizedoverlay.addOverlay(overlayitem);
			mapOverlays.add(itemizedoverlay);
			
			/* FIXME: this may need to be ran continuously, as the current implementation 
			 * will stop listening after first location is found */
			MyLocation myLocation = new MyLocation();
			myLocation.getLocation(this, locationResult);

			
			final EditText edittext = (EditText) findViewById(R.id.edittext);
            String  homeAddress = customSharedPreference.getString("homeAddress", "");
            edittext.setText(homeAddress);
			edittext.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) { 
					// hide the keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
					
					destinationAddressString = edittext.getText().toString();
					goToDestinationAddress(destinationAddressString);
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

	public void	showCostScreen(String from, String to, Double distanceValue, String distanceText, Double durationValue, String durationText) {
		try {
			Intent myIntent = new Intent();
			//Toast.makeText(TaxiMain.this, "sending: " + currentAddressString , Toast.LENGTH_SHORT).show();
			myIntent.putExtra("fromAddress", currentAddressString);
			myIntent.putExtra("toAddress", destinationAddressString);
			myIntent.putExtra("distanceValue", distanceValue);
			myIntent.putExtra("distanceText", distanceText);
			myIntent.putExtra("durationValue", durationValue);
			myIntent.putExtra("durationText", durationText);
			myIntent.setClassName("com.venitaxi.taxi", "com.venitaxi.taxi.Cost");
			//myIntent.setPackage("com.venitaxi.taxi");
			/* show the cost screen */
			startActivity(myIntent);
		} catch(Exception e) {

		}
		
	}
	@Override
	public void onDirectionsAvailable(Route route, Mode mode) {
		/* this should draw the route */
		itemizedoverlay.route = route;
		itemizedoverlay.mode = mode;
		mapController.setZoom(14);
		mapView.invalidate();
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
	
   public GeoPoint geoCodeAddress(String address) {
		List<Address> addresses;
		try {
			//addresses = geoCoder.getFromLocationName( homeAddress, 5);
			Double curLatitude = currentLocation.getLatitudeE6()/1E6;
			Double curLongitude = currentLocation.getLongitudeE6()/1E6;
			addresses = geoCoder.getFromLocationName( address, 5, curLatitude + 2.0, curLongitude + 2.0, curLatitude - 2.0, curLongitude - 2.0);
			if (addresses.size() > 0) {
				GeoPoint p = new GeoPoint((int) (addresses.get(0)
						.getLatitude() * 1E6), (int) (addresses
						.get(0).getLongitude() * 1E6));
				return p;
			}
		} catch (Exception e) {
			//FIXME: have sensible error catching here
		}
		return null;
   }

   public void getRouteInfo(GeoPoint start, GeoPoint destination) {
		
		Double curLatitude = start.getLatitudeE6()/1E6;
		Double curLongitude = start.getLongitudeE6()/1E6;
		Double dstLatitude = destination.getLatitudeE6()/1E6;
		Double dstLongitude = destination.getLongitudeE6()/1E6;
		
		String url = "http://maps.google.com/maps/api/directions/json?&mode=driving&sensor=false" +
		"&origin=" + curLatitude + "," + curLongitude + 
		"&destination=" + dstLatitude + "," + dstLongitude;
		HttpRetriever hr = new HttpRetriever();
		String response = hr.retrieve(url);
		try {
			JSONObject routeData = Cost.getJSONObjectFromString(response);
			//routes/#0/legs/#0/distance/value
			JSONObject routes = (JSONObject) routeData.getJSONArray("routes").get(0);
			JSONObject legs = (JSONObject) routes.getJSONArray("legs").get(0);
		    /* distance is in meters*/	
			Double distanceValue = legs.getJSONObject("distance").getDouble("value");
			String distanceText = legs.getJSONObject("distance").getString("text");
			
			/* duration is in seconds */
			Double durationValue = legs.getJSONObject("duration").getDouble("value");
			String durationText = legs.getJSONObject("duration").getString("text");
			
			showCostScreen(currentAddressString, destinationAddressString, distanceValue, distanceText, durationValue, durationText);
		} catch (Exception e) {
			e.printStackTrace();
		}
   
   }
   public void goToDestinationAddress(String address) {
		GeoPoint dstPoint = geoCodeAddress(address);
		getRouteInfo(currentLocation, dstPoint);
		//System.out.println(response);
		if(dstPoint != null) {
			OverlayItem overlayitem = new OverlayItem(dstPoint,"Hola, Mundo!", "I'm in Mexico City!");
			itemizedoverlay.addOverlay(overlayitem);
			setDirections(currentLocation, dstPoint);
			mapView.invalidate();
		} else {
			Toast.makeText(TaxiMain.this, "cannot resolve coordinates for home, is the GPS on?", Toast.LENGTH_SHORT).show();
		} 
	   
   }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String title = item.getTitle().toString();
		//FIXME: what happens if there is no current location
		if(title.equals("go home") && currentLocation != null) {
            homeAddress = customSharedPreference.getString("homeAddress", null);
            if(homeAddress != null) {
            	destinationAddressString = homeAddress;
            	goToDestinationAddress(destinationAddressString);
            }
		} else if(title.equals("preferences")) { 
			Intent myIntent = new Intent();
			myIntent.setClassName("com.venitaxi.taxi", "com.venitaxi.taxi.Preferences");
			//myIntent.setPackage("com.venitaxi.taxi");
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
	protected void performReverseGeocodingInBackground() {
		//showCurrentLocation();
		new ReverseGeocodeLookupTask().execute((Void[])null);
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
			//Toast.makeText(TaxiMain.this, "update response code: " + responseCode + ", server: " + locationUpdater.updateServer, Toast.LENGTH_SHORT).show();
	}
public class ReverseGeocodeLookupTask extends AsyncTask <Void, Void, GeoCodeResult> {
		
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
		protected GeoCodeResult doInBackground(Void... params) {
			return new TaxiGeoCoder().reverseGeoCode(currentLocation.getLatitudeE6() / 1E6, currentLocation.getLongitudeE6() / 1E6);
		}

		@Override
		protected void onPostExecute(GeoCodeResult result) {
			this.progressDialog.cancel();
			//Toast.makeText(TaxiMain.this, "done geocoding", Toast.LENGTH_LONG).show();			
			if(result != null) {
				//Toast.makeText(TaxiMain.this, "got a result: " + result.toString(), Toast.LENGTH_LONG).show();			
			}
		}
		
	}
}
