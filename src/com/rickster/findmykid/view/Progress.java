package com.rickster.findmykid.view;

import android.widget.TextView;

public class Progress {
	
	private static final String TAG = "Progress";
	
	private String mText;
	private boolean mCompleted;
	private int mOrder;
	private TextView tv;
	
	public TextView getTv() {
		return tv;
	}

	public void setTv(TextView tv) {
		this.tv = tv;
	}

	public Progress(String t, boolean c, int o){
		mText = t;
		mCompleted = c;
		mOrder = o;
	}
	
	public String getText() {
		return mText;
	}
	
	public void setText(String text) {
		mText = text;
	}
	
	public boolean isCompleted() {
		return mCompleted;
	}
	
	public void setCompleted(boolean completed) {
		mCompleted = completed;
	}
	
	public int getOrder() {
		return mOrder;
	}
	
	public void setOrder(int order) {
		mOrder = order;
	}
	
	
	
}
