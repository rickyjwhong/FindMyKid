package com.rickster.findmykid.model;

public class Feedback {
	private long mId;
	private String mMessage;
	
	public Feedback(long a, String m){
		mId = a;
		mMessage = m;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		mMessage = message;
	}
	
}
