package com.rickster.findmykid.model;

public class Connection {
	
	private static final String TAG = "Connection";
	private long mId;
	private long mTracker;
	private long mTracking;
	
	public Connection(){
		mId = mTracker = mTracking = -1;
	}
	
	public Connection(long id, long tracker, long tracking){
		mId = id;
		mTracker = tracker;
		mTracking = tracking;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public long getTracker() {
		return mTracker;
	}

	public void setTracker(long tracker) {
		mTracker = tracker;
	}

	public long getTracking() {
		return mTracking;
	}

	public void setTracking(long tracking) {
		mTracking = tracking;
	}
	
	
	
}
