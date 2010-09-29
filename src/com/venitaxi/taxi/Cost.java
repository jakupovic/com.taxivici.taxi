package com.venitaxi.taxi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
 
public class Cost extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.cost);
                
                Double distance = null;
                Double minutes = null;
                String unitDistance = getIntent().getStringExtra("unitDistance");
                String unitTime = getIntent().getStringExtra("unitTime");
                distance = getIntent().getDoubleExtra("distance", 0.0);
                minutes = getIntent().getDoubleExtra("minutes", 0.0);
                
                SharedPreferences customSharedPreference = getSharedPreferences( "taxiCustomPreferences", MODE_PRIVATE);
		        Double meterDrop = Double.valueOf(customSharedPreference.getString("taxiMeterDropCost", "2.50"));
		        Double taxiUnitDistanceCost = Double.valueOf(customSharedPreference.getString("taxiUnitDistanceCost", "2.00"));
		        Double taxiMinuteWaitCost = Double.valueOf(customSharedPreference.getString("taxiMinuteWaitCost", "0.50"));
				Double fare = meterDrop; 
				
				if (distance > 1) {
					fare += (distance - 1) * taxiUnitDistanceCost;
				}
				fare += minutes * taxiMinuteWaitCost; // is this right?
                
                final TextView distanceText = (TextView) findViewById(R.id.textViewDistance);
                distanceText.setText(distance.toString() + " " + unitDistance);
                final TextView time = (TextView) findViewById(R.id.textViewTime);
                time.setText(minutes.toString() + " " + unitTime);
                final TextView fareText = (TextView) findViewById(R.id.textViewFare);
                fareText.setText("$ " + fare.toString() );
                final TextView eastSideForHire = (TextView) findViewById(R.id.textEastSideForHire);
                eastSideForHire.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:2062426200"));
						startActivity(myIntent);
					}
                });
                final TextView yellowCab = (TextView) findViewById(R.id.textYelloCab);
                yellowCab.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:2066226500"));
						startActivity(myIntent);
					}
                });
                final TextView orangeCab = (TextView) findViewById(R.id.textOrangeCab);
                orangeCab.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:2069570838"));
						startActivity(myIntent);
					}
                });
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
}

