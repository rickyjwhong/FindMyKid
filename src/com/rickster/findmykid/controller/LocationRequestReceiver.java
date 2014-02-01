package com.rickster.findmykid.controller;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class LocationRequestReceiver extends GCMBroadcastReceiver {	
	@Override
	protected String getGCMIntentServiceClassName(Context context){
		return GCMService.class.getCanonicalName();
	}
	
}
