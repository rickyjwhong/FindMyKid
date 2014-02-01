package com.rickster.findmykid.model;

import android.util.Log;

public class User {
	
	private static final String TAG = "User";
	private long mId;
	private String mName;
	private String mGCM;
	private String mPhoneNumber;
	private String mCode;
	private String mPhotoUrl;
	
	public User(){
		mCode = mName = mGCM = mPhoneNumber = "";		
	}
	
	public User(String n, String gcm, String pn){
		Log.i(TAG, "Creating User: " + n + " | " + gcm + " | "  + pn);
		mName = n;
		mGCM = gcm;
		mPhoneNumber = pn;
		mCode = "";
	}
	
	public User(long id, String n, String gcm, String pn, String code){
		Log.i(TAG, "Creating User: " + id + " | " + n + " | " + gcm + " | "  + pn + " | " + code);
		mId = id;
		mName = n;
		mGCM = gcm;
		mPhoneNumber = pn;
		mCode = code;
	}	
	
	@Override
	public String toString(){
		return mName;
	}
	
	public String getPhotoUrl() {
		return mPhotoUrl;
	}
	
	public void setPhotoUrl(String photoUrl) {
		mPhotoUrl = photoUrl;
	}
	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		mCode = code;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getGCM() {
		return mGCM;
	}

	public void setGCM(String gCM) {
		mGCM = gCM;
	}

	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}

	
	
}
