package com.venitaxi.taxi;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.maps.GeoPoint;

public class TaxiGeoCoder {
	public GeoPoint reverseGeoCode(String address, Context context) {
		Geocoder geoCoder = new Geocoder(context, Locale.getDefault()); 
		List<Address> addresses;
		try {
			addresses = geoCoder.getFromLocationName(address, 5);
			GeoPoint p = new GeoPoint((int) (addresses.get(0)
					.getLatitude() * 1E6), (int) (addresses
					.get(0).getLongitude() * 1E6));
			return p;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
