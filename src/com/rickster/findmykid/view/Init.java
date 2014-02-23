package com.rickster.findmykid.view;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rickster.findmykid.R;
import com.rickster.findmykid.controller.Lab;
import com.rickster.findmykid.model.Constants;
import com.rickster.findmykid.model.User;

public class Init extends FragmentActivity {
	
	private static final String TAG = "Init";
	
	GoogleCloudMessaging gcm;
	String regId;
	AtomicInteger msgId = new AtomicInteger();
	Context mContext;
	SharedPreferences mPrefs;	
	String mPhoneNumber;
	String mName = "";
	EditText mNameEdit;
	ImageButton mSubmit;
	
	//private static final int SELECT_PICTURE = 1;
	//private String selectedImagePath;
    //ADDED
    //private String filemanagerstring;
	//private ImageView mImage;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init);
		
		mContext = getApplicationContext();		
		mPrefs = mContext.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
		mSubmit = (ImageButton) findViewById(R.id.init_submit);
		//mImage = (ImageView) findViewById(R.id.init_image);
		
		mSubmit.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mName.length() != 0){
					if(Lab.hasConnection(getApplicationContext())){					
						mPhoneNumber = getPhoneNumber();
						startGCMRegistration();										
					}else{
						Lab.get(getApplicationContext()).showToast(getString(R.string.connection_register));
					}
				}else{
					Lab.get(getApplicationContext()).showToast(getString(R.string.form_fill_message));
				}			
			}
		});
				
		mNameEdit = (EditText) findViewById(R.id.init_name);		
		mNameEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				String name = s.toString();
				if(!name.isEmpty()) {
					mName = name;
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub				
			}		
		});				
	}
	
	public void onResume(){
		super.onResume();
	}		
	
	//get primary phone number of the device	
	public String getPhoneNumber(){
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNum = (!tm.getLine1Number().isEmpty()) ? tm.getLine1Number() : Constants.DEFAULT_PHONE_NUMBER;
		Log.i(TAG, "Phone Number Retrieved: " + phoneNum);		
		return phoneNum;
	}
	
	public void startGCMRegistration(){
		if(checkPlayServices()){
			gcm = GoogleCloudMessaging.getInstance(mContext);
			regId = getRegistrationId();			
			if(regId.isEmpty()){				
				registerInBackground(); //has to be on background thread
			}else{
				Log.i(TAG, "Registration Id Exists: " + regId);
				sendRegistrationToBackEnd();
			}
		}else{
			Log.i(TAG, "You have to have google play servcies");
		}
	}
	
	private void registerInBackground(){
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				if(gcm == null)
					gcm = GoogleCloudMessaging.getInstance(mContext);				
				try {
					regId = gcm.register(Constants.SENDER_ID);
					Log.i(TAG, "Device is registered: " + regId);
					msg = "Device is now registered " + regId;					
				} catch (IOException e) {
					e.printStackTrace();
				}				
				return msg;
			}			
			@Override
			protected void onPostExecute(String msg){
				sendRegistrationToBackEnd();
			}			
		}.execute(null, null, null);
	}	
	
	private void sendRegistrationToBackEnd(){
		if(!mName.isEmpty() && !regId.isEmpty() && !mPhoneNumber.isEmpty()){
			User user = new User(mName, regId, mPhoneNumber);
			Lab.get(mContext).registerUser(user);			
		}else{
			DialogFragment fragment = new DialogFragment();			
			FragmentManager fm = this.getSupportFragmentManager();
			fragment.show(fm, Constants.FILL_EVERYTHING);
		}			
	}
	
	private String getRegistrationId(){	
		String registrationId = mPrefs.getString(Constants.REGISTRATION_ID, "");
		if(registrationId.isEmpty()){
			Log.i(TAG, "Registration not found");
			return "";
		}		
		int oldVersion = mPrefs.getInt(Constants.APP_VERSION, Integer.MIN_VALUE);
		int newVersion = getAppVersion(mContext);
		if(oldVersion != newVersion){
			Log.i(TAG, "Versions don't match");
			return "";
		}
		return registrationId;		
	}
	
	private int getAppVersion(Context c){
		try {
			PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}	
}
