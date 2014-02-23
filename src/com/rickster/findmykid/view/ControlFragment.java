package com.rickster.findmykid.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.rickster.findmykid.R;
import com.rickster.findmykid.controller.Lab;
import com.rickster.findmykid.controller.OfflineLab;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.User;

public class ControlFragment extends Fragment {
	
	private static final String TAG = "HomeFragment";
	
	private View mProgressBar;
	private ImageButton mStartTrackingButton;
	private User mSelectedTracking;
	private Lab sLab;
	private ArrayList<User> mTrackings;
	private TextView mProgressStatus;
	private Spinner mSpinner;
	private ArrayAdapter<User> mAdapter;
	private Callbacks mCallbacks;
	private boolean mLocationReceived = false;
	private boolean mTextLocationReceived = false;
	private long mLocationId;
	
	public interface Callbacks{
		void locationRetrieved(long id, boolean text);
	}
	
	@Override
	public void onAttach(Activity a){
		super.onAttach(a);
		mCallbacks = (Callbacks) a;
	}
	
	@Override
	public void onDetach(){
		super.onDetach();
		mCallbacks = null;
	}
	
	private BroadcastReceiver mResultReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context c, Intent i) {
			Log.i(TAG, "Location has been retrieved");
			if(mTextLocationReceived) return;
			mLocationReceived = true;
			gotLocation(i.getLongExtra(Constants.LOCATION_ID_EXTRA, -1));			
		}		
	};
	
	private BroadcastReceiver mTextLocationReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			mTextLocationReceived = true;
			gotLocation(intent.getLongExtra(Constants.LOCATION_ID_EXTRA, -1));
		}		
	};
	
	private BroadcastReceiver mTextSwitchReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {		
			Log.i(TAG, "Received text switch command");
			if(mLocationReceived) return;
			FragmentManager fm = getActivity().getSupportFragmentManager();
			AlertFragment fragment = AlertFragment.newInstance(mSelectedTracking.getName());
			fragment.setTargetFragment(ControlFragment.this, Constants.ALERT_REQUEST);
			fragment.show(fm, Constants.ALERT_FRAGMENT);			
		}		
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode != Activity.RESULT_OK) return;
		if(requestCode == Constants.ALERT_REQUEST){
			boolean result = data.getBooleanExtra(Constants.ALERT_RESULT, false);
			if(result){
				mProgressStatus.setText(R.string.status_1);	
				OfflineLab.get(getActivity()).sendTrackingRequest(mSelectedTracking);
			}else{
				clearUI();
			}			
		}
	}
	
	public void clearUI(){
		mSelectedTracking = new User();
		mProgressStatus.setText(R.string.progress_0);
		mStartTrackingButton.setEnabled(true);
		mStartTrackingButton.setClickable(true);
		mStartTrackingButton.setImageResource(R.drawable.progress_0);
	}
	
	
	@Override
	public void onResume(){
		super.onResume();
		getActivity().registerReceiver(mResultReceiver, new IntentFilter(Constants.ACTION_LOCATION_RECEIVED));
		getActivity().registerReceiver(mTextSwitchReceiver, new IntentFilter(Constants.LOCATION_RETRIEVAL_STARTED));
		getActivity().registerReceiver(mTextLocationReceiver, new IntentFilter(Constants.TEXT_LOCATION_RECEIVED));
		if(mAdapter != null) new GetTrackingTask().execute();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		getActivity().unregisterReceiver(mResultReceiver);
		getActivity().unregisterReceiver(mTextSwitchReceiver);
		getActivity().unregisterReceiver(mTextLocationReceiver);
	}
	
	private void giveSleep(long value){
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void gotConnection(boolean status){		
		if(status){
			mStartTrackingButton.setImageResource(R.drawable.progress_1);
			mProgressStatus.setText(R.string.progress_2);
		}else{
			mStartTrackingButton.setImageResource(R.drawable.progress_1);
			mProgressStatus.setText(R.string.status_1);
		}		
		giveSleep(1000);
	}
	
	public void gotPermission(boolean status){
		if(status){			
			mStartTrackingButton.setImageResource(R.drawable.progress_2);
			mProgressStatus.setText(R.string.progress_3);
			giveSleep(1500);
			startTracking();
		}else{
			mProgressStatus.setText(R.string.status_2);
		}		
		giveSleep(1500);
	}
	
	public void gotLocation(long id){	
		mLocationId = id;
		mStartTrackingButton.setImageResource(R.drawable.progress_3);
		mProgressStatus.setText(R.string.progress_4);	
		new Handler().postDelayed(new Runnable(){
		    public void run() {
		    	startMap();
		    }
		}, 1500);		
	}
	
	public void startMap(){	
		mStartTrackingButton.setImageResource(R.drawable.progress_4);
		mProgressStatus.setText(R.string.progress_5);	
		new Handler().postDelayed(new Runnable(){
		    public void run() {
		    	mCallbacks.locationRetrieved(mLocationId, mTextLocationReceived);
		    }
		}, 1000);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		sLab = Lab.get(getActivity());	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		
		View v = inflater.inflate(R.layout.home_grid, parent, false);	
		
		mProgressStatus = (TextView) v.findViewById(R.id.progressMessage);		
		mSpinner = (Spinner) v.findViewById(R.id.trackingSpinner);		
		mProgressBar = v.findViewById(R.id.progressContainer);
		mProgressBar.setVisibility(View.INVISIBLE);		
		mStartTrackingButton = (ImageButton) v.findViewById(R.id.startTrackingButton);
		mStartTrackingButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mSelectedTracking != null){
					//start tracking
					mStartTrackingButton.setClickable(false);
					beforeTracking();
				}
			}
		});		
		if(mTrackings == null) new GetTrackingTask().execute();		
		return v;		
	}	
	
	private void beforeTracking(){
		new AsyncTask<Void, Integer , Void>(){
			@Override
			protected void onPreExecute(){				
				mProgressStatus.setText(R.string.progress_1);
			}			
			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub								
				publishProgress((Lab.hasConnection(getActivity()) ? 1 : 3));	
				publishProgress((sLab.hasPermission(mSelectedTracking) ? 2 : 4));				
				return null;
			}			
			protected void onProgressUpdate(Integer... progress){
				int s = progress[0];				
				switch(s){				
					case 1:						
						gotConnection(true);
						Log.i(TAG, "Got Permission this");
						break;
					case 2:								
						gotPermission(true);
						break;	
					case 3:
						gotConnection(false);
						break;
					case 4:
						gotPermission(false);
						break;
					default:
						mStartTrackingButton.setImageResource(R.drawable.progress_inactive);						
				}
			}			
			@Override
			protected void onPostExecute(Void x){
				//mStartTrackingButton.setImageResource(R.drawable.progress_4);
			}			
		}.execute();
	}
	
	private void startTracking(){
		new AsyncTask<Void, Integer , Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				mTextLocationReceived = false;
				mLocationReceived = false;
				sLab.sendTrackingRequest(mSelectedTracking);
				return null;
			}		
			@Override
			protected void onPostExecute(Void x){				
				startLocationCountdown();				
			}
		}.execute();
	}
	
	private void startLocationCountdown(){
		new Handler().postDelayed(new Runnable(){
		    public void run() {
		    	if(!mLocationReceived){
		    		Intent i = new Intent(Constants.LOCATION_RETRIEVAL_STARTED);
					getActivity().sendBroadcast(i);
		    	}		    	
		    }
		}, Constants.DATA_TEXT_SWITCH_DURATION);
	}
	
	private void setUpAdapter(){
		if(getActivity() == null || mTrackings == null) return;
		mAdapter = new ArrayAdapter<User>(getActivity(), R.layout.spinner_item, mTrackings);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(mAdapter);
		mSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
	}
	
	public class CustomOnItemSelectedListener implements OnItemSelectedListener {		 
		  public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			  mSelectedTracking = (User) parent.getAdapter().getItem(pos);
			  startNewTracking();
		  }		 
		  @Override
		  public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		  }		 
	}	
	
	private void startNewTracking(){
		mStartTrackingButton.setImageResource(R.drawable.progress_0);
		mProgressStatus.setText(R.string.click_button_text);
	}
	
	private class GetTrackingTask extends AsyncTask<Void, Void, ArrayList<User>>{
		@Override
		protected void onPreExecute(){
			mProgressBar.setVisibility(View.VISIBLE);
		}
		@Override
		protected ArrayList<User> doInBackground(Void... params) {
			return sLab.loadTrackings();
		}
		@Override
		protected void onPostExecute(ArrayList<User> users){
			mTrackings = users;
			setUpAdapter();
			mProgressBar.setVisibility(View.INVISIBLE);
		}	
	}		
	
}
