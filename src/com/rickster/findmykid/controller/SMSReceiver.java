package com.rickster.findmykid.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
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
				str += "<return number=\"" + msgs[i].getOriginatingAddress() + "\" />";
				str += msgs[i].getMessageBody().toString();
			}
			
			
			
			
			//check if the contained code has permission
			//store it in location
			//
			
			
			//---display the new SMS message---
			Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
		}
	}
}
