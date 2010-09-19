package com.venitaxi.taxi;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import com.google.android.maps.GeoPoint;

public class Updater {
    HttpClient httpclient;
    HttpPost httppost;  
	List<NameValuePair> nameValuePairs;  
	String updateServer;
	
    public Updater(String server) {
	    HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
	 	int timeoutSocket = 5000;
	 	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	    httpclient = new DefaultHttpClient(httpParameters);  
	    updateServer = server;
	    httppost = new HttpPost(updateServer);  
	    //httppost = new HttpPost("http://71.231.141.95:2080");  
    }
	public void run() {
	}
	public int updateLocation(GeoPoint p, List<NameValuePair> l) {  
	    // Create a new HttpClient and Post Header  
		Double latitude = p.getLatitudeE6() / 1E6;
		Double longitude = p.getLongitudeE6() / 1E6;
		l.add(new BasicNameValuePair("latitude", Double.toString(latitude)));
		l.add(new BasicNameValuePair("longitude", Double.toString(longitude)));
		nameValuePairs = l;
	    try {  
	        // Add your data  
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
	        // Execute HTTP Post Request  
	        HttpResponse response = httpclient.execute(httppost);  
	        return 200;
	    } catch (ClientProtocolException e) {  
	        // TODO Auto-generated catch block  
	    	return 500;
	    } catch (IOException e) {  
	        // TODO Auto-generated catch block  
	    	return 500;
	    }  
	}
	
}
