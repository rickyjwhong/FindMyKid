package com.rickster.findmykid.controller;

import java.util.ArrayList;

import com.rickster.findmykid.R;
import com.rickster.findmykid.model.Connection;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.HttpData;
import com.rickster.findmykid.model.Location;
import com.rickster.findmykid.model.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class Lab {	
	
	private static final String TAG = "Lab";
	
	private static Lab sLab;
	private static OnlineLab sOnlineLab;
	private static OfflineLab sOfflineLab;
	private Context mContext;
	private SharedPreferences mPrefs;
	private ArrayList<User> mTrackings;
	private ArrayList<User> mTrackers;
	private User mCurrentUser;
	
	public Lab(Context c){
		mContext = c;
		mPrefs = c.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
		mCurrentUser = loadUser();

		sOnlineLab = OnlineLab.get(c.getApplicationContext());
		sOfflineLab = OfflineLab.get(c.getApplicationContext());		
	}
	
	public static boolean hasConnection(Context c){
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
	}	
	
	public boolean alreadyConnected(User user){
		if(hasConnection(mContext)) return sOnlineLab.alreadyConnected(user);
		else return sOfflineLab.alreadyConnected(user);
	}
	
	public void showToast(String msg){
		if(msg != null) Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
	}
	
	public User getCurrentUser(){
		return mCurrentUser;
	}
	
	public void sendFeedback(String message){
		if(hasConnection(mContext)) sOnlineLab.sendFeedback(message);
		else sOfflineLab.sendFeedback(message);
	}	
	
	public boolean deleteTracking(User user){
		if(hasConnection(mContext)) return sOnlineLab.deleteTracking(user);
		else return sOfflineLab.deleteTracking(user);		
	}
	
	public boolean deleteTracker(User user){
		if(hasConnection(mContext)) return sOnlineLab.deleteTracker(user);
		else return sOfflineLab.deleteTracker(user);		
	}	
	
	public ArrayList<User> loadTrackers(){			
		if(hasConnection(mContext)) return sOnlineLab.loadTrackers();
		else return sOfflineLab.loadTrackers();
	}
	
	public ArrayList<User> loadTrackings(){	
		Log.i(TAG, "Using: " + hasConnection(mContext) + " Lab");
		if(hasConnection(mContext)) return sOnlineLab.loadTrackings();
		else return sOfflineLab.loadTrackings();	
	}
	
	public boolean hasPermission(User targetUser){
		if(hasConnection(mContext)) return sOnlineLab.hasPermission(targetUser);
		else return sOfflineLab.hasPermissionToSend(targetUser);		
	}
	
	public ArrayList<Connection> connectUser(String code){
		if(hasConnection(mContext)) return sOnlineLab.connectUser(code);
		else return sOfflineLab.connectUser(code);
	}
	
	public User userExists(String code){
		if(hasConnection(mContext)) return sOnlineLab.userExists(code);
		else return sOfflineLab.userExists(code);
	}
	
	public void respondLocation(Location loc){
		if(hasConnection(mContext)) sOnlineLab.respondLocation(loc);	
	}
	
	public void sendTrackingRequest(User targetUser){
		if(hasConnection(mContext)) sOnlineLab.sendTrackingRequest(targetUser);
		//else showToast(mContext.getString(R.string.connection_internet));
	}
	
	public ArrayList<Location> getLocation(long id){
		if(hasConnection(mContext)) return sOnlineLab.getLocation(id);
		else return sOfflineLab.getLocation(id);
	}
	
	public void registerUser(final User user){
		Log.i(TAG, "Registering user: " + user.getName() + " Using " + hasConnection(mContext) + " Lab");
		if(hasConnection(mContext)) sOnlineLab.registerUser(user);
		else sOfflineLab.registerUser(user);			
	}
	
	//SHARED PREFERENCES	
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
	
	public static Lab get(Context c){
		if(sLab == null) sLab = new Lab(c.getApplicationContext());
		return sLab;
	}
	
}
