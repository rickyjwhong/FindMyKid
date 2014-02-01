package com.rickster.findmykid.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rickster.findmykid.R;
import com.rickster.findmykid.controller.Lab;
import com.rickster.findmykid.model.Connection;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.User;

public class SettingFragment extends Fragment {
	
	private static final String TAG = "AddTrackingFragment";
	
	private ListView mTrackers;
	private ListView mTracking;
	
	private TextView mCode;
	private ImageButton mConnectButton;
	private EditText mConnectEditText;
	private String mConnectText;
	private SharedPreferences mPrefs;
	private Lab sLab;
	private ImageButton mFeedBackButton;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);		
		mPrefs = getActivity().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
		sLab = Lab.get(getActivity());
		new getTrackingTask().execute(sLab.getCurrentUser());
		new getTrackerTask().execute(sLab.getCurrentUser());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		
		View v = inflater.inflate(R.layout.setting, parent, false);		
		
		mCode = (TextView) v.findViewById(R.id.add_tracking_code);
		String code = sLab.getUniqueCode();
		mCode.setText(code);		
		
		mFeedBackButton = (ImageButton) v.findViewById(R.id.feedBackButton);
		mFeedBackButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getActivity(), FeedBackActivity.class);
				startActivity(i);
			}
		});
		
		mConnectEditText = (EditText) v.findViewById(R.id.add_tracking_connect);
		mConnectEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				mConnectText = s.toString();
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub				
			}			
		});
		
		mConnectButton = (ImageButton) v.findViewById(R.id.add_tracking_connect_button);
		mConnectButton.setOnClickListener(new ConnectButtonListener());
		
		mTrackers = (ListView) v.findViewById(R.id.add_tracking_trackers);		
		mTracking = (ListView) v.findViewById(R.id.add_tracking_tracking);
		
		return v;		
	}
	
	private void showToast(String msg){
		Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
	}
	
	private class ConnectButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(mConnectText != null && mConnectText.length() != 0){
				new checkUserExistsTask().execute();				
			}else{
				showToast("Fill out the form");
			}
		}		
	}
	
	private class checkUserExistsTask extends AsyncTask<Void, Void, Void>{
		private User mUser;
		private boolean mConnected;
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			mUser  = sLab.userExists(mConnectText);
			if(mUser != null){
				mConnected = sLab.alreadyConnected(mUser);
			}
			return null;
		}		
		@Override
		protected void onPostExecute(Void v){
			if(mUser == null){
				showToast("User does not exist");
			}else if(mConnected){
				showToast("Already connected");
			}else{
				new connectTask().execute();				
			}
		}
	}	
	
	private class connectTask extends AsyncTask<Void, Void, ArrayList<Connection>>{
		@Override
		protected ArrayList<Connection> doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return sLab.connectUser(mConnectText);
		}		
		@Override
		protected void onPostExecute(ArrayList<Connection> connections){
			if(connections.size() > 0){
				mConnectEditText.setText("");
				showToast("Connection Successful!");	
				new getTrackerTask().execute(sLab.getCurrentUser());
			}
		}
	}
	
	private class getTrackingTask extends AsyncTask<User, Void, ArrayList<User>>{	
		@Override
		protected ArrayList<User> doInBackground(User... params) {
			// TODO Auto-generated method stub
			return sLab.loadTrackings();
		}		
		@Override
		protected void onPostExecute(ArrayList<User> users){
			mTracking.setAdapter(new TrackAdapter(getActivity(), users, true));
			
		}
	}
	
	private class getTrackerTask extends AsyncTask<User, Void, ArrayList<User>>{
		@Override
		protected void onPreExecute(){
			
		}	
		@Override
		protected ArrayList<User> doInBackground(User... params) {
			// TODO Auto-generated method stub
			return sLab.loadTrackers();
		}
		@Override
		protected void onPostExecute(ArrayList<User> users){
			mTrackers.setAdapter(new TrackAdapter(getActivity(), users, false));
		}
	}
	
	private class deleteTrackerTask extends AsyncTask<User, Void, Void>{
		@Override
		protected Void doInBackground(User... params) {
			// TODO Auto-generated method stub
			User user = params[0];
			sLab.deleteTracker(user);			
			return null;
		}		
		@Override
		protected void onPostExecute(Void x){
			new getTrackerTask().execute(sLab.getCurrentUser());
		}
	}
	
	private class deleteTrackingTask extends AsyncTask<User, Void, Void>{	
		@Override
		protected Void doInBackground(User... params) {
			// TODO Auto-generated method stub
			User user = params[0];
			sLab.deleteTracking(user);				
			return null;
		}			
		@Override
		protected void onPostExecute(Void x){
			new getTrackingTask().execute(sLab.getCurrentUser());		
		}
	}
	
	private class TrackAdapter extends ArrayAdapter<User>{		
		private ArrayList<User> mUsers;
		private Context mContext;
		private LayoutInflater mInflater;
		private boolean isTracking;
		public TrackAdapter(Context c, ArrayList<User> users, boolean t){
			super(getActivity(), 0, users);
			mContext = c;
			mUsers = users;
			mInflater = LayoutInflater.from(c);
			isTracking = t;
		}		
		@Override
		public int getCount(){
			return mUsers.size();
		}		
		@Override
		public View getView(int pos, View view, ViewGroup parent){
			ViewHolder holder;
			View v = view;
			if(v == null || v.getTag() == null){
				v = mInflater.inflate(R.layout.track_row, parent, false);				
				holder = new ViewHolder();
				holder.mName = (TextView) v.findViewById(R.id.track_row_name);
				holder.mButton = (ImageButton) v.findViewById(R.id.track_row_button);
				v.setTag(holder);
			}else{
				holder = (ViewHolder) v.getTag();
			}			
			final User user = getItem(pos);			
			holder.mName.setText(user.getName());
			holder.mButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(isTracking){
						Log.i(TAG, "Delete tracking: " + user.getName());
						new deleteTrackingTask().execute(user);
					}else{
						Log.i(TAG, "Delete tracker: " + user.getName());
						new deleteTrackerTask().execute(user);
					}
					
					
				}
			});
			v.setTag(holder);			
			return v;			
		}
		
		public class ViewHolder{
			TextView mName;
			ImageButton mButton;
		}		
		//user get view but with viewholder		
	}	
	
	
	
}
