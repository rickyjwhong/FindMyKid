package com.rickster.findmykid.controller;

import java.util.ArrayList;

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
	private boolean mOnline;
	
	public Lab(Context c){
		mContext = c;
		mPrefs = c.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
		mCurrentUser = loadUser();
		if(hasConnection(c)){
			sOnlineLab = OnlineLab.get(c.getApplicationContext());
			mOnline = true;
		}else{
			sOfflineLab = OfflineLab.get(c.getApplicationContext());
			mOnline = false;
		}
	}
	
	public static boolean hasConnection(Context c){
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
	}	
	
	public boolean alreadyConnected(User user){
		if(mOnline) return sOnlineLab.alreadyConnected(user);
		else return sOfflineLab.alreadyConnected(user);
	}
	
	public void showToast(String msg){
		if(msg != null) Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
	}
	
	public User getCurrentUser(){
		return mCurrentUser;
	}
	
	public void sendFeedback(String message){
		if(mOnline) sOnlineLab.sendFeedback(message);
		else sOfflineLab.sendFeedback(message);
	}	
	
	public boolean deleteTracking(User user){
		if(mOnline) return sOnlineLab.deleteTracking(user);
		else return sOfflineLab.deleteTracking(user);		
	}
	
	public boolean deleteTracker(User user){
		if(mOnline) return sOnlineLab.deleteTracker(user);
		else return sOfflineLab.deleteTracker(user);		
	}	
	
	public ArrayList<User> loadTrackers(){			
		if(mOnline) return sOnlineLab.loadTrackers();
		else return sOfflineLab.loadTrackers();
	}
	
	public ArrayList<User> loadTrackings(){	
		if(mOnline) return sOnlineLab.loadTrackings();
		else return sOfflineLab.loadTrackings();	
	}
	
	public boolean hasPermission(User targetUser){
		if(mOnline) return sOnlineLab.hasPermission(targetUser);
		else return sOfflineLab.hasPermission(targetUser);		
	}
	
	public ArrayList<Connection> connectUser(String code){
		if(mOnline) return sOnlineLab.connectUser(code);
		else return sOfflineLab.connectUser(code);
	}
	
	public User userExists(String code){
		if(mOnline) return sOnlineLab.userExists(code);
		else return sOfflineLab.userExists(code);
	}
	
	public void respondLocation(Location loc){
		if(mOnline) sOnlineLab.respondLocation(loc);
		else sOfflineLab.respondLocation(loc);		
	}
	
	public void sendTrackingRequest(User targetUser){
		if(mOnline) sOnlineLab.sendTrackingRequest(targetUser);
		else sOfflineLab.sendTrackingRequest(targetUser);
	}
	
	public ArrayList<Location> getLocation(long id){
		if(mOnline) return sOnlineLab.getLocation(id);
		else return sOfflineLab.getLocation(id);
	}
	
	public void registerUser(final User user){
		if(mOnline) sOnlineLab.registerUser(user);
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
