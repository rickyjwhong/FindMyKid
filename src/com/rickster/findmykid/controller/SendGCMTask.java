package com.rickster.findmykid.controller;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

public class SendGCMTask extends AsyncTask<String, Void, Boolean> {
	
	private static final int MAX_RETRY = 5;
	private static final String LOG = "SendGCMTask";
	private Context mContext;
	private String mApiKey = "AIzaSyA6FqIedV28R1HWj4uHHNrVVkmvZ3pcK44";

	public SendGCMTask(final Context context){
		mContext = context;
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		final String message = params[0];
		final String targetId = params[1];
		
		Sender sender = new Sender(mApiKey);
		Message gcmMessage = new Message.Builder().addData("msg", message).build();
		Result result;
		try	{
			result = sender.sendNoRetry(gcmMessage, targetId);
			return true;
		}catch(IOException e){
			e.printStackTrace();
		}
		return false;
	}

}
