package com.rickster.findmykid.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.rickster.findmykid.model.Constants;

public class SMSReceiver extends BroadcastReceiver {	
	
	private static final String TAG = "SMSReceiver";	
	@Override
	public void onReceive(Context context, Intent intent) {	
		Log.i(TAG, "Received Text");
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String str = "";
		if (bundle != null)
		{
			//---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i=0; i<msgs.length; i++){
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				str += msgs[i].getMessageBody().toString();
				str += "<" 	+ Constants.RETURN_TEXT + " " 
						+ Constants.RETURN_NUMBER + "="						
						+ quoteWrap(msgs[i].getOriginatingAddress()) + "/>";				
			}
			if(ourMessage(str)) {
				abortBroadcast();
				Intent i = new Intent(context, SMSService.class);
				i.putExtra(Constants.TEXT_SERVICE_MESSAGE, str);
				context.startService(i);
			}								
		}
	}
	
	private boolean ourMessage(String str){
		Log.i(TAG, "Checking if it's our message: " + str);
		if(str.startsWith(Constants.TEXT_BEGINNING_CODE))return true;
		else return false;
	}
	
	private String quoteWrap(String value){
		return "\"" + value + "\"";
	}
}
