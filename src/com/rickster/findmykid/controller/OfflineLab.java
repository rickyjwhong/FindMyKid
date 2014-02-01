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

import com.rickster.findmykid.R;
import com.rickster.findmykid.model.Connection;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.HttpData;
import com.rickster.findmykid.model.Location;
import com.rickster.findmykid.model.OfflineData;
import com.rickster.findmykid.model.User;

public class OfflineLab  {
	
	private static final String TAG = "OfflineLab";
	private static OfflineLab sOfflineLab;
	private Context mContext;
	private SharedPreferences mPrefs;
	private ArrayList<User> mTrackings;
	private ArrayList<User> mTrackers;
	private User mCurrentUser;
	private OfflineData mOfflineData;
	
	public OfflineLab(Context c){
		mContext = c;
		mPrefs = c.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
		mCurrentUser = loadUser();
		mOfflineData = new OfflineData(c);
	}
	
	public boolean hasConnection(){
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isAvailable() && info.isConnected();
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
		showToast(mContext.getString(R.string.connection_internet));
		return false;
	}
	
	public boolean deleteTracker(User user){
		showToast(mContext.getString(R.string.connection_internet));
		return false;
	}	
	
	public ArrayList<User> loadTrackers(){			
		return mOfflineData.getTrackers(getCurrentUser());
	}
	
	public ArrayList<User> loadTrackings(){	
		return mOfflineData.getTrackings(getCurrentUser());
	}
	
	public boolean hasPermission(User targetUser){
		return HttpData.isConnected(mCurrentUser.getId(), targetUser.getId());
	}
	
	public ArrayList<Connection> connectUser(String code){
		Log.i(TAG, "Trying to connect: " + getCurrentUser().getCode() + " and " + code);
		showToast(mContext.getString(R.string.connection_internet));
		return new ArrayList<Connection>();
	}
	
	public User userExists(String code){
		Log.i(TAG, "Seeing if user exists: " + code);
		return HttpData.checkUserExist(code).get(0);
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
	
	public void registerUser(final User user){
		Log.i(TAG, "Registering User: " + user.getName() + " with RegId: " + user.getGCM());
		showToast(mContext.getResources().getString(R.string.connection_register));
		//showToast(mContext.getResources().getString(R.string.))		
	}
	
	public boolean alreadyConnected(User trackerUser){
		//do it from sqlite
		return false;
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
	
	public void updateLocalData(ArrayList<Connection> connections){
		mOfflineData.updateData(connections);
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
	
	public static OfflineLab get(Context c){
		if(sOfflineLab == null) sOfflineLab = new OfflineLab(c.getApplicationContext());
		return sOfflineLab;
	}
}
