package com.venitaxi.taxi;



public class TaxiGeoCoder {
	
	private static final String YAHOO_API_BASE_URL = "http://where.yahooapis.com/geocode?q=%1$s,+%2$s&gflags=R&appid=Mrca0I7e";
	
	private HttpRetriever httpRetriever = new HttpRetriever();
	private XmlParser xmlParser = new XmlParser();
	
	public GeoCodeResult reverseGeoCode(double latitude, double longitude) {
		
		String url = String.format(YAHOO_API_BASE_URL, String.valueOf(latitude), String.valueOf(longitude));		
		System.out.println(url);
		String response = httpRetriever.retrieve(url);
		return xmlParser.parseXmlResponse(response);
		
	}

}
/*
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
			if(addresses.size() > 0) {
				GeoPoint p = new GeoPoint((int) (addresses.get(0)
						.getLatitude() * 1E6), (int) (addresses
						.get(0).getLongitude() * 1E6));
				return p;
			} else {
				//did not find anything
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
*/