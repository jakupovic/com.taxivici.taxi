package com.taxiveni.taxifree;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.maps.MapActivity;
import com.taxiveni.taxifree.R;

public class Cost extends MapActivity {
    JSONArray taxiCompanies = null;
    JSONArray phoneNumbers = null;
	int ENTNUM = 4;

	private static class EfficientAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public EfficientAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return labels.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.listview, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView
						.findViewById(R.id.TextView01);
				holder.text2 = (TextView) convertView
						.findViewById(R.id.TextView02);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(labels[position]);
			holder.text2.setText(totals[position]);

			return convertView;
		}

		static class ViewHolder {
			TextView text;
			TextView text2;
		}
	}
	public static JSONObject getJSONObjectFromString(String jsonString) {
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cost);
        final SharedPreferences customSharedPreference = getSharedPreferences( "taxiCustomPreferences", MODE_PRIVATE);
		
		Double distance = null;
        Double minutes = null;
		JSONObject cabs;
	    int numEntries = 0;
	    
        distance = getIntent().getDoubleExtra("distanceValue", 0.0) /1609.0;
        String distanceText = getIntent().getStringExtra("distanceText");
        minutes = getIntent().getDoubleExtra("durationValue", 0.0) / 60.0;
        String durationText = getIntent().getStringExtra("durationText");
        String fromAddress = getIntent().getStringExtra("fromAddress");
        String toAddress = getIntent().getStringExtra("toAddress");
        /*
        add_charge_per_mile
        add_increments_miles
        charge_per_increment
        city
        initial_increment_miles
        meter_drop
        wait_time_charge
        */

        Double add_charge_per_mile = Double.valueOf(customSharedPreference.getString("add_charge_per_mile", "2.00"));
        Double add_increments_miles = Double.valueOf(customSharedPreference.getString("add_increments_miles", "2.00"));
        Double charge_per_increment = Double.valueOf(customSharedPreference.getString("charge_per_increment", "2.00"));
        Double initial_increment_miles = Double.valueOf(customSharedPreference.getString("initial_increment_miles", "2.00"));
        Double meter_drop = Double.valueOf(customSharedPreference.getString("meter_drop", "2.50"));
        Double wait_time_charge = Double.valueOf(customSharedPreference.getString("wait_time_charge", "0.50"));
        
		Double fare = meter_drop; 
		if (distance > initial_increment_miles) {
			fare += ((distance - initial_increment_miles) / add_increments_miles) * charge_per_increment;
		}
		fare += minutes * wait_time_charge; // is this right?
		Double tip = fare * 0.15;
		Double total = tip + fare;
		
		try {
			/* if we cannot pull any cab data that is not the end as we still
			 * have our favorite cab
			 */
			numEntries = 0;
			try {
		        String citySelected = customSharedPreference.getString("listCityLocale", "Other");
				JSONObject cityData = getJSONObjectFromString(citySelected);
				cabs = cityData.getJSONObject("cabs");
				if(cabs != null) {
					taxiCompanies = cabs.names();
					phoneNumbers = cabs.toJSONArray(taxiCompanies);
					numEntries = cabs.length();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			labels = new String[ENTNUM + numEntries];
			totals = new String[ENTNUM + numEntries];
			labels[0] = "Fare:    ";
			labels[1] = "Tip:      ";
			labels[2] = "Total:   ";
			labels[3] = "Call Favorite Taxi";
			totals[0] = new DecimalFormat("$0.##").format((double)fare);
			totals[1] = new DecimalFormat("$0.##").format((double)tip);
			totals[2] = new DecimalFormat("$0.##").format((double)total);
	        for(int i=0; taxiCompanies != null && i<taxiCompanies.length(); i++) {
				labels[ENTNUM + i] = "Call " + taxiCompanies.getString(i);
				totals[ENTNUM + i] = "";
	        }
	        // set the current address and destination info
			TextView from = (TextView) findViewById(R.id.fromTextView);
			from.setText("From: " + fromAddress);
			TextView to = (TextView) findViewById(R.id.toTextView);
			to.setText("To: " + toAddress);
			TextView distTime = (TextView) findViewById(R.id.distTimeTextView);
			distTime.setText("Info (est.): " + distanceText + ", " + durationText);
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (Exception e) {
			// FIXME: should not catch all exceptions
			e.printStackTrace();
		}

		ListView l1 = (ListView) findViewById(R.id.ListView01);
		l1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// only do an action if the user clicked on a cab item, which all come after ENTNUM 
				String phoneNumber = null;
				// did we pick our favorite taxi
				// FIXME: this is hackier than shit, need to fix
				if(arg2 == ENTNUM - 1) {
					phoneNumber = customSharedPreference.getString("myTaxi", "555-1212");
				} else if(arg2 > ENTNUM - 1) {
					try {
						phoneNumber = phoneNumbers.getString(arg2-ENTNUM);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						//FIXME: should not catch all exceptions
						e.printStackTrace();
					}
				}
				if(phoneNumber != null) {
					Intent myIntent;
					myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phoneNumber));
					startActivity(myIntent);
				}
		}});
		l1.setAdapter(new EfficientAdapter(this));
	}

	private static String[] labels;
	private static String[] totals; 
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
