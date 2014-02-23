package com.rickster.findmykid.model;

public class Request {
	
	private static final String TAG = "Request";
	private String mId;
	private String mSenderCode;
	private String mNumber;
	
	public Request(String a, String b, String c){
		mId = a;
		mSenderCode = b;
		mNumber = c;
	}
	
	public String getNumber() {
		return mNumber;
	}

	public void setNumber(String number) {
		mNumber = number;
	}
	
	public String getId() {
		return mId;
	}
	
	public void setId(String id) {
		mId = id;
	}

	public String getSenderCode() {
		return mSenderCode;
	}

	public void setSenderCode(String senderCode) {
		mSenderCode = senderCode;
	}	
	
}
