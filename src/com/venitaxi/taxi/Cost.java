package com.venitaxi.taxi;

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
	public JSONObject getJSONObjectFromString(String jsonString) {
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
		
		Double distance = null;
        Double minutes = null;
        String unitDistance = getIntent().getStringExtra("unitDistance");
        String unitTime = getIntent().getStringExtra("unitTime");
        distance = getIntent().getDoubleExtra("distance", 0.0);
        minutes = getIntent().getDoubleExtra("minutes", 0.0);
        
        final SharedPreferences customSharedPreference = getSharedPreferences( "taxiCustomPreferences", MODE_PRIVATE);
        String city  = customSharedPreference.getString("listCityLocale", "Seattle");
        Double meterDrop = Double.valueOf(customSharedPreference.getString("taxiMeterDropCost", "2.50"));
        Double taxiUnitDistanceCost = Double.valueOf(customSharedPreference.getString("taxiUnitDistanceCost", "2.00"));
        Double taxiMinuteWaitCost = Double.valueOf(customSharedPreference.getString("taxiMinuteWaitCost", "0.50"));
		Double fare = meterDrop; 
		
		if (distance > 1) {
			fare += (distance - 1) * taxiUnitDistanceCost;
		}
		fare += minutes * taxiMinuteWaitCost; // is this right?
		Double tip = new Double(fare * 0.15);
		Double total = tip + fare;
	    JSONObject entries = null;
	    int numEntries = 0;
	    /*
		try {
			InputStream is = this.getResources().openRawResource(R.raw.cabdatajson);
		    byte[] buffer;
			buffer = new byte[is.available()];
		    while (is.read(buffer) != -1);
		    String jsontext = new String(buffer);
		    entries = new JSONObject(jsontext);
		    numEntries = entries.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
        String citySelected = customSharedPreference.getString("listCityLocale", "nothing");
		//Toast.makeText(getBaseContext(), "pulled out: " + citySelected, Toast.LENGTH_LONG).show(); 
		JSONObject cityData = getJSONObjectFromString(citySelected);
		JSONObject cabs;
		try {
			cabs = cityData.getJSONObject("cabs");
			taxiCompanies = cabs.names();
			phoneNumbers = cabs.toJSONArray(taxiCompanies);
			numEntries = cabs.length();
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//JSONArray cabCompanies = cabs.names();
        //SharedPreferences.Editor editor = customSharedPreference.edit();
		//editor.putString("taxiMeterDropCost", city.getString("meter_drop"));
		labels = new String[ENTNUM + numEntries];
		totals = new String[ENTNUM + numEntries];
		labels[0] = "Fare:";
		labels[1] = "Tip:";
		labels[2] = "Total:";
		labels[3] = "Call Favorite Taxi";
		totals[0] = new DecimalFormat("$0.##").format((double)fare);
		totals[1] = new DecimalFormat("$0.##").format((double)tip);
		totals[2] = new DecimalFormat("$0.##").format((double)total);
		/*
        taxiCompanies = entries.names();
		try {
			phoneNumbers = entries.toJSONArray(taxiCompanies);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
        for(int i=0; taxiCompanies != null && i<taxiCompanies.length(); i++)
        {
			try {
				labels[ENTNUM + i] = "Call " + taxiCompanies.getString(i);
				totals[ENTNUM + i] = "";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
					}
				}
				Intent myIntent;
				myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phoneNumber));
				startActivity(myIntent);
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
