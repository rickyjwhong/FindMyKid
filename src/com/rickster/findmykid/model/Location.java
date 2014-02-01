package com.rickster.findmykid.model;

public class Location {
	
	private double mLatitude;
	private double mLongitude;
	private double mAltitude;
	private long mTime;
	private long mId;
	private long mRequestId;
	private String mAddress;
	
	public Location(){
		mId = -1;
	}
	
	public Location(long a, double b, double c, double d, long e, long f, String g){
		mId = a;
		mLatitude = b;
		mLongitude = c;
		mAltitude = d;
		mTime = e;
		mRequestId =f ;
		mAddress = g;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public double getAltitude() {
		return mAltitude;
	}

	public void setAltitude(double altitude) {
		mAltitude = altitude;
	}

	public long getTime() {
		return mTime;
	}

	public void setTime(long time) {
		mTime = time;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public long getRequestId() {
		return mRequestId;
	}

	public void setRequestId(long requestId) {
		mRequestId = requestId;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String address) {
		mAddress = address;
	}		

	
	
	
}
