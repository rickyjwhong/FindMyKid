package com.rickster.findmykid.view;

import java.text.DateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rickster.findmykid.controller.Lab;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.Location;

public class MapFragment extends SupportMapFragment {
	
	private static final String TAG = "RunMapFragment";
	
	private GoogleMap mGoogleMap;
	private Location mLocation;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if(args != null){
			long locationId = args.getLong(Constants.LOCATION_ID_EXTRA, -1);
			if(locationId != -1){	
				//get location
				new LocationFetchTask().execute(locationId);	
			}
		}		
	}
	
	private class LocationFetchTask extends AsyncTask<Long, Void, ArrayList<Location>>{		
		
		@Override
		protected ArrayList<Location> doInBackground(Long... params) {
			// TODO Auto-generated method stub
			return Lab.get(getActivity()).getLocation(params[0]);
		}		
		@Override
		protected void onPostExecute(ArrayList<Location> location){
			if(location.size() != 0){
				mLocation = location.get(0);
				if(isNetworkAvailable()) updateUI();
				//Toast.makeText(getApplicationContext(), location.get(0).getAddress(), Toast.LENGTH_LONG).show();				
			}			
		}		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		
		mGoogleMap = getMap();
		mGoogleMap.setMyLocationEnabled(true);			
		
		return v;
	}
	

	public void updateUI(){	
		if(mGoogleMap == null || mLocation == null) return;
		
		PolylineOptions line = new PolylineOptions();
		LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
		
		LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

		String date = DateFormat.getTimeInstance(DateFormat.SHORT).format(mLocation.getTime());
		String text = mLocation.getAddress();
		MarkerOptions markerOption = new MarkerOptions().position(latLng).title(text).snippet(date);
		mGoogleMap.addMarker(markerOption).showInfoWindow();
		
		line.add(latLng);
		latLngBuilder.include(latLng);
		
		mGoogleMap.addPolyline(line);
		
		LatLngBounds latLngBounds = latLngBuilder.build();	
		final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(latLngBounds, 10);
		//mGoogleMap.moveCamera(cu);
		CameraPosition cameraPosition = new CameraPosition.Builder()
		.target(latLng)      
	    .zoom(16)                   
	    .bearing(0)               
	    .tilt(30)  
	    .build();
		mGoogleMap.setBuildingsEnabled(true);
		mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		mGoogleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition arg0) {
				// TODO Auto-generated method stub				
				mGoogleMap.setOnCameraChangeListener(null);
			}			
		});
	}
	
	private boolean isNetworkAvailable(){
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
	}
	
	public static MapFragment newInstance(long locationId){
		Bundle args = new Bundle();
		args.putLong(Constants.LOCATION_ID_EXTRA, locationId);
		MapFragment fragment = new MapFragment();
		fragment.setArguments(args);
		return fragment;
	}	
}
