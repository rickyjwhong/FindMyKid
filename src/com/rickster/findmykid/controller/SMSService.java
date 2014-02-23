package com.rickster.findmykid.controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.rickster.findmykid.R;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.Location;
import com.rickster.findmykid.model.OfflineData;
import com.rickster.findmykid.model.Request;

public class SMSService extends Service {

	private static final String TAG = "SMSService";
	private String mText;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		mText = intent.getStringExtra(Constants.TEXT_SERVICE_MESSAGE);
		if(mText == null) return 0;
		Log.i(TAG, "Starting SMS Service: " + mText);
		XmlPullParserFactory x;
		XmlPullParser parser;
		try {
			x = XmlPullParserFactory.newInstance();
			parser = x.newPullParser();
			parser.setInput(new StringReader(mText));
			parseText(mText, parser);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}
	
	
	
	private void parseText(String msg, XmlPullParser parser) throws XmlPullParserException, IOException{	
		String type = "";
		Log.i(TAG, "Parsing Text: " + msg);
		int eventType = parser.getEventType();
		while(eventType != XmlPullParser.END_DOCUMENT){
			if(eventType == XmlPullParser.START_TAG){				
				if(parser.getName().equalsIgnoreCase(Constants.INFO_TEXT)){
					//INFO SECTION
					type = parser.getAttributeValue(null, Constants.INFO_TYPE);		
					if(type.equalsIgnoreCase(Constants.REQUEST_TEXT)) handleRequest(parser);
					else handleResponse(parser);				
				}						
			}
			eventType = parser.next();
		}
	}
	
	private void handleRequest(XmlPullParser parser) throws XmlPullParserException, IOException{
		Log.i(TAG, "Handling Request");
		String id = "", senderCode = "", returnNumber = "";
		int eventType = parser.next();
		while(eventType != XmlPullParser.END_DOCUMENT){
			if(parser.getName().equalsIgnoreCase(Constants.DATA_TEXT)){
				id = parser.getAttributeValue(null, Constants.DATA_ID);
				senderCode = parser.getAttributeValue(null, Constants.DATA_SENDER_CODE);				
			}else if(parser.getName().equalsIgnoreCase(Constants.RETURN_TEXT)){					
				returnNumber = parser.getAttributeValue(null, Constants.RETURN_NUMBER); //RETURN SECTION
			}
			eventType = parser.next();
		}	
		Log.i(TAG, "Request: id " + id + " " + senderCode + " " + returnNumber);
		Request request = new Request(id, senderCode, returnNumber);
		OfflineLab.get(getApplicationContext()).sendTrackingResponse(request);
	}
	
	private void handleResponse(XmlPullParser parser) throws XmlPullParserException, IOException{
		String lat = "", lon = "", alt = "", time = "", id = "", returnNumber = "";
		int eventType = parser.next();
		while(eventType != XmlPullParser.END_DOCUMENT){
			if(parser.getName().equalsIgnoreCase(Constants.DATA_TEXT)){
				lat = parser.getAttributeValue(null, Constants.DATA_LAT);
				lon = parser.getAttributeValue(null, Constants.DATA_LON);
				alt = parser.getAttributeValue(null, Constants.DATA_ALT);
				time = parser.getAttributeValue(null, Constants.DATA_TIME);
				id = parser.getAttributeValue(null, Constants.DATA_REQUEST_ID);
			}else if(parser.getName().equalsIgnoreCase(Constants.RETURN_TEXT)){					
				returnNumber = parser.getAttributeValue(null, Constants.RETURN_NUMBER); //RETURN SECTION
			}
			eventType = parser.next();
		}	
		Location location = new Location();
		location.setLatitude(Double.parseDouble(lat));
		location.setLongitude(Double.parseDouble(lon));
		location.setAltitude(Double.parseDouble(alt));
		location.setTime(Long.valueOf(time));
		location.setRequestId(Long.valueOf(id));
		location.setAddress(getNearByLocation(Double.parseDouble(lat), Double.parseDouble(lon)));
		OfflineData o = new OfflineData(getApplicationContext());
		long locId = o.insertLocation(location);
		Intent i = new Intent(Constants.TEXT_LOCATION_RECEIVED);
		i.putExtra(Constants.LOCATION_ID_EXTRA, locId);
		getApplicationContext().sendBroadcast(i);		
	}
	
	public boolean hasConnection(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
	}
	
	public String getNearByLocation(double lat, double lon){
		
		if(!hasConnection()) return getString(R.string.location_unknown);
		
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		StringBuilder sb = new StringBuilder();
		try {
			List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);	
			
			String address = addresses.get(0).getAddressLine(0);
			String locality = addresses.get(0).getLocality();
			String state = addresses.get(0).getAdminArea();
			
			sb.append(address).append(" , ").append(locality).append(" , ").append(state);			
				//Log.i(TAG, "Got some addresses: " + a.getLocality() + " - " + a.getFeatureName() + " - " + a.getMaxAddressLineIndex() + " - " + a.getSubThoroughfare() + " - " + a.getSubThoroughfare());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(TAG, "Address attained: " + sb.toString());
		return sb.toString();
	}
	
	
	
	
	
	
	
	

}
