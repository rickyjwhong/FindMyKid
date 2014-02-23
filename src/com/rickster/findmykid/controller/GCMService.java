package com.rickster.findmykid.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.rickster.findmykid.R;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.Location;

public class GCMService extends GCMBaseIntentService {
	
	private static final String LOG = GCMService.class.getSimpleName();
	
	private static final String TYPE_LOCATION_REQUEST = "location_request";
	private static final String TYPE_LOCATION_RESPONSE = "location_response";
	private long mRequestId;
	public Handler mHandler = new Handler(Looper.getMainLooper());
	
	private Handler mLocationRequestHandler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			mRequestId = Long.valueOf((String) msg.obj);
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			android.location.Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if((System.currentTimeMillis() - loc.getTime()) > Constants.TIME_DIFFERENCE_LOCATION){
				//use request location updates				
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
			}else{
				//use stale last known location
				Log.i(TAG, "Using stale Location, last known location");
				sendLocationBack(loc);
			}			
			return true;
		}
	});
	
	private void sendLocationBack(android.location.Location loc){
		Location location = new Location();
		location.setLatitude(loc.getLatitude());
		location.setLongitude(loc.getLongitude());
		location.setAltitude(loc.getAltitude());
		location.setTime(loc.getTime());
		location.setRequestId(mRequestId);			
		location.setAddress(getNearByLocation(loc));
		new LocationResponseTask().execute(location);
	}
	
	
	private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {
		@Override
		public void onLocationChanged(android.location.Location loc) {
			// TODO Auto-generated method stub
			Log.i(TAG, "Location Retrieval Triggered");
			sendLocationBack(loc);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public boolean hasConnection(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
	}
	
	public String getNearByLocation(android.location.Location loc){
		
		if(!hasConnection()) return getString(R.string.location_unknown);
		
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		StringBuilder sb = new StringBuilder();
		try {
			List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);	
			
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
	
	private Handler mLocationResponseHandler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			Intent i = new Intent(Constants.ACTION_LOCATION_RECEIVED);
			i.putExtra(Constants.LOCATION_ID_EXTRA, Long.valueOf((String) msg.obj));
			sendBroadcast(i);
			
			return false;
		}
	});
	
	private class LocationResponseTask extends AsyncTask<Location, Void, Void>{
		@Override
		protected Void doInBackground(Location... params) {
			// TODO Auto-generated method stub
			Location location = params[0];
			Lab.get(getApplicationContext()).respondLocation(location);
			return null;
		}		
		@Override
		protected void onPostExecute(Void x){
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.removeUpdates(mLocationListener);
		}
	}
	
	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Something went wrong");
	}

	@Override
	protected void onMessage(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Message received");
		if(intent.hasExtra(TYPE_LOCATION_REQUEST)){
			Log.i(TAG, "Location Request obtained");
			Message msg = mLocationRequestHandler.obtainMessage(1, -1, -1, intent.getStringExtra(TYPE_LOCATION_REQUEST));
			mLocationRequestHandler.sendMessage(msg);
		}else if(intent.hasExtra(TYPE_LOCATION_RESPONSE)){
			Log.i(TAG, "Location Response Obtained");
			Message msg = mLocationResponseHandler.obtainMessage(1, -1, -1, intent.getStringExtra(TYPE_LOCATION_RESPONSE));
			mLocationResponseHandler.sendMessage(msg);
		}	
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Registered");
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub

	}

}
