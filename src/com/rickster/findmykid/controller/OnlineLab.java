package com.rickster.findmykid.controller;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.rickster.findmykid.model.Connection;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.HttpData;
import com.rickster.findmykid.model.Location;
import com.rickster.findmykid.model.OfflineData;
import com.rickster.findmykid.model.User;

public class OnlineLab {
	
	private static final String TAG = "OnlineLab";
	private static OnlineLab sOnlineLab;
	private Context mContext;
	private SharedPreferences mPrefs;
	private ArrayList<User> mTrackings;
	private ArrayList<User> mTrackers;
	private User mCurrentUser;
	
	public OnlineLab(Context c){
		mContext = c;
		mPrefs = c.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
		mCurrentUser = loadUser();
	}
	
	public boolean hasConnection(){
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
	}	
	
	public ArrayList<Connection> getConnections(){
		return HttpData.getConnections(mCurrentUser.getId());
	}
	
	public void showToast(String msg){
		if(msg != null) Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
	}
	
	public User getCurrentUser(){
		return mCurrentUser;
	}
	
	public void sendFeedback(String message){
		HttpData.sendFeedback(message);
	}	
	
	public boolean deleteTracking(User user){
		return HttpData.deleteConnection(mCurrentUser.getId(), user.getId());
	}
	
	public boolean deleteTracker(User user){
		return HttpData.deleteConnection(user.getId(), mCurrentUser.getId());
	}	
	
	public ArrayList<User> loadTrackers(){			
		return HttpData.getTrackers(mCurrentUser);
	}
	
	public ArrayList<User> loadTrackings(){	
		return HttpData.getTrackings(mCurrentUser);		
	}
	
	public boolean hasPermission(User targetUser){
		return HttpData.isConnected(mCurrentUser.getId(), targetUser.getId());
	}
	
	public void updateWithLocalConnections(ArrayList<Connection> connections){
		OfflineLab.get(mContext).updateLocalData(connections);
	}
	
	public ArrayList<Connection> connectUser(String code){
		Log.i(TAG, "Trying to connect: " + getCurrentUser().getCode() + " and " + code);
		return HttpData.connectUser(getCurrentUser().getCode(), code);
	}
	
	public User userExists(String code){
		Log.i(TAG, "Seeing if user exists: " + code);
		if(HttpData.checkUserExist(code).size() == 0){
			return null;
		}else{
			return HttpData.checkUserExist(code).get(0);
		}		
	}
	
	public void respondLocation(Location loc){
		Log.i(TAG, "Returning Location for Request # " + loc.getRequestId());
		HttpData.respondLocation(loc);		
	}
	
	public void sendTrackingRequest(User targetUser){
		Log.i(TAG, "Requesting Location for User: " + targetUser.getName());
		HttpData.requestLocation(mCurrentUser, targetUser);		
	}
	
	public ArrayList<Location> getLocation(long id){
		return HttpData.getLocation(id);
	}
	
	public String getUniqueCode(){
		String code = mPrefs.getString(Constants.PREF_USER_CODE, null);
		return code;		
	}	
	
	public String getPhoneNumber(){
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNum = tm.getLine1Number();
		if(!phoneNum.isEmpty()) Log.i(TAG, "Phone Number retrieved: " + phoneNum);
		else phoneNum = "0000000000";
		return phoneNum;
	}		
	
	public boolean alreadyConnected(User user){
		return HttpData.isConnected(user.getId(), mCurrentUser.getId());
	}
	
	public void registerUser(final User user){
		Log.i(TAG, "Registering User: " + user.getName() + " with RegId: " + user.getGCM());
		new AsyncTask<User, Void, ArrayList<User>>() {	
			@Override
			protected ArrayList<User> doInBackground(User... params) {
				return HttpData.registerUser(user);
			}			
			@Override
			protected void onPostExecute(ArrayList<User> users){
				if(users.size() != 0) saveUser(users.get(0));				
			}			
		}.execute();		
	}
	
	public void saveUser(User user){
		Log.i(TAG, "Saving User: " + user.getId());
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putLong(Constants.PREF_USER_ID, user.getId()).commit();
		editor.putString(Constants.PREF_USER_CODE, user.getCode()).commit();
		editor.putString(Constants.PREF_USER_NAME, user.getName()).commit();
		editor.putString(Constants.PREF_USER_GCM, user.getGCM()).commit();
		editor.putString(Constants.PREF_USER_PHONE_NUMBER, user.getPhoneNumber()).commit();
		editor.putBoolean(Constants.FIRST_TIME, false).commit();
		mCurrentUser = loadUser();
	}
	
	public User loadUser(){
		Log.i(TAG, "Retrieving current User");
		long id = mPrefs.getLong(Constants.PREF_USER_ID, 0);
		String name = mPrefs.getString(Constants.PREF_USER_NAME, "");
		String code = mPrefs.getString(Constants.PREF_USER_CODE, "");
		String gcm = mPrefs.getString(Constants.PREF_USER_GCM, "");
		String phone_number = mPrefs.getString(Constants.PREF_USER_PHONE_NUMBER, "");		 
		return new User(id, name, gcm, phone_number, code);
	}
	
	public static OnlineLab get(Context c){
		if(sOnlineLab == null) sOnlineLab = new OnlineLab(c.getApplicationContext());
		return sOnlineLab;
	}
	
	
}
