package com.rickster.findmykid.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageView;

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
	String mName;
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
		switchSubmitButton(false);
		
		mSubmit.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mPhoneNumber = getPhoneNumber();
				startGCMRegistration();
			}
		});
		/*
		mImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
			}
		});
		*/
				
		mNameEdit = (EditText) findViewById(R.id.init_name);		
		mNameEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				String name = s.toString();
				if(!name.isEmpty()) {
					switchSubmitButton(true);
					mName = name;
				}
				else switchSubmitButton(false);
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
	
	/*
	public void showImage(){
		if(selectedImagePath != null){
			//String s = mContext.getFileStreamPath(selectedImagePath).getAbsolutePath();
			
			//mImage.setImageBitmap(bm);
		}			
	}
	*/
	
	public void onResume(){
		super.onResume();
		//showImage();
	}
		
	//UPDATED
	/*
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
            	Uri selectedImage = data.getData();
                InputStream imageStream = null;
				try {
					imageStream = getContentResolver().openInputStream(selectedImage);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                mImage.setImageBitmap(yourSelectedImage);
                //DEBUG PURPOSE - you can delete this if you want
                if(selectedImagePath!=null)
                    System.out.println(selectedImagePath);
                else System.out.println("selectedImagePath is null");
                if(filemanagerstring!=null)
                    System.out.println(filemanagerstring);
                else System.out.println("filemanagerstring is null");

                //NOW WE HAVE OUR WANTED STRING
                if(selectedImagePath!=null)
                    System.out.println("selectedImagePath is the right one for you!");
                else
                    System.out.println("filemanagerstring is the right one for you!");
            }
        }
    }
    
    
    public String getPath(Uri uri) {
        String selectedImagePath;
        //1:MEDIA GALLERY --- query from MediaStore.Images.Media.DATA
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor != null){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            selectedImagePath = cursor.getString(column_index);
        }else{
            selectedImagePath = null;
        }

        if(selectedImagePath == null){
            //2:OI FILE Manager --- call method: uri.getPath()
            selectedImagePath = uri.getPath();
        }
        return selectedImagePath;
    }
	*/
	public void switchSubmitButton(boolean on){
		mSubmit.setEnabled(on);
		mSubmit.setFocusable(on);
		mSubmit.setClickable(on);
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
			this.finish();
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
