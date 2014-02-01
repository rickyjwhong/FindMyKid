package com.rickster.findmykid.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.PendingIntent;
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
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gms.location.LocationListener;
import com.rickster.findmykid.R;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.Location;
import com.rickster.findmykid.view.MapActivity;

public class GCMService extends GCMBaseIntentService {
	
	private static final String LOG = GCMService.class.getSimpleName();
	
	private static final String TYPE_LOCATION_REQUEST = "location_request";
	private static final String TYPE_LOCATION_RESPONSE = "location_response";
	public Handler mHandler = new Handler(Looper.getMainLooper());
	
	private Handler mLocationRequestHandler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
			android.location.Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Location location = new Location();
			location.setLatitude(loc.getLatitude());
			location.setLongitude(loc.getLongitude());
			location.setAltitude(loc.getAltitude());
			location.setTime(loc.getTime());
			location.setRequestId(Long.valueOf((String) msg.obj));
			
			location.setAddress(getNearByLocation(loc));
			//Toast.makeText(GCMService.this, (String) msg.obj, Toast.LENGTH_LONG).show();
			new LocationResponseTask().execute(location);
			return true;
		}
	});
	
	private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {
		@Override
		public void onLocationChanged(android.location.Location arg0) {
			// TODO Auto-generated method stub
			
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
			// TODO Auto-generated method stub
			//Toast.makeText(getApplicationContext(),(String) msg.obj, Toast.LENGTH_LONG).show();	
			/*
			Intent i = new Intent(GCMService.this, MapActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra(Constants.LOCATION_ID_EXTRA, Long.valueOf((String) msg.obj));
			startActivity(i);
			*/
			//new LocationFetchTask().execute((String) msg.obj);
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
