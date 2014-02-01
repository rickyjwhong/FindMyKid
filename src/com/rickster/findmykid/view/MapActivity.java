package com.rickster.findmykid.view;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rickster.findmykid.R;
import com.rickster.findmykid.controller.Lab;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.Location;

public class MapActivity extends FragmentActivity {

	private static final String TAG = "MapActivity";
	private FragmentManager fm;
	private long mLocationId;
	private Location mLocation;
	private View mDetailProgressBar;
	private TextView mTime, mLatitude, mLongitude, mAltitude, mLastLocation;

	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		mTime = (TextView) this.findViewById(R.id.ll_time);
		mLatitude = (TextView) this.findViewById(R.id.ll_latitude);
		mLongitude = (TextView) this.findViewById(R.id.ll_longitude);
		mAltitude = (TextView) this.findViewById(R.id.ll_altitude);
		mLastLocation = (TextView) this.findViewById(R.id.ll_known_location);
		mDetailProgressBar = findViewById(R.id.mapDetailProgressContainer);
		mDetailProgressBar.setVisibility(View.INVISIBLE);		
		fm = this.getSupportFragmentManager();
		
		mLocationId = getIntent().getLongExtra(Constants.LOCATION_ID_EXTRA, -1);		
		if(mLocationId != -1){
			new LocationFetchTask().execute(mLocationId);
		}		
	}
	
	private void updateMap(){
		Fragment oldFragment = fm.findFragmentById(R.id.mapFragmentContainer);
		Fragment newFragment = MapFragment.newInstance(mLocationId);
		if(oldFragment != null) fm.beginTransaction().remove(oldFragment).commit();
		fm.beginTransaction().add(R.id.mapFragmentContainer, newFragment).commit();		
	}
	
	private void updateDetail(){
		if(mLocation == null) return;
		Log.i(TAG, "Location xx" + mLocation.getAddress());
		mTime.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(mLocation.getTime()));
		mLatitude.setText(String.valueOf(mLocation.getLatitude()));
		mLongitude.setText(String.valueOf(mLocation.getLongitude()));
		mAltitude.setText(String.valueOf(mLocation.getAltitude()));		
		if(mLocation.getAddress().equalsIgnoreCase(getString(R.string.location_unknown))){
			mLastLocation.setText(getNearByLocation(mLocation.getLatitude(), mLocation.getLongitude()));
		}else{
			mLastLocation.setText(mLocation.getAddress());
		}
		
	}
	
	private boolean isNetworkAvailable(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
	}
	
	public String getNearByLocation(double lat, double lon){
		
		if(!isNetworkAvailable()) return getString(R.string.location_unknown);
		
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
	
	private class LocationFetchTask extends AsyncTask<Long, Void, ArrayList<Location>>{
		
		@Override
		protected void onPreExecute(){
			if(mDetailProgressBar != null) mDetailProgressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected ArrayList<Location> doInBackground(Long... params) {
			// TODO Auto-generated method stub
			return Lab.get(MapActivity.this).getLocation(params[0]);
		}		
		@Override
		protected void onPostExecute(ArrayList<Location> location){
			mDetailProgressBar.setVisibility(View.INVISIBLE);
			if(location.size() != 0){
				mLocation = location.get(0);
				updateMap();
				updateDetail();
				//Toast.makeText(getApplicationContext(), location.get(0).getAddress(), Toast.LENGTH_LONG).show();				
			}			
		}		
	}
	

	
}
