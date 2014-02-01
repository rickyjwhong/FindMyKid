package com.rickster.findmykid.view;

import java.util.Enumeration;
import java.util.Properties;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rickster.findmykid.R;
import com.rickster.findmykid.controller.Lab;

public class FeedBackActivity extends Activity {

	private static final String TAG = "ErrorActivity";
	private EditText mMessage;
	private ImageButton mSubmitButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ActionBar bar = getActionBar();
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		bar.setCustomView(R.layout.custom_action_bar);
		setContentView(R.layout.activity_feedback);
		mMessage = (EditText) findViewById(R.id.feedback_message);
		mSubmitButton = (ImageButton) findViewById(R.id.feedback_submit);
		mSubmitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String message = mMessage.getText().toString();
				if(message.length() != 0){
					try {
						message += addDebugInfosToErrorMessage(FeedBackActivity.this);
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.i(TAG, "Sending Message: " + message);
					new SendMessageTask().execute(message);
				}else{
					Toast.makeText(getApplicationContext(), R.string.form_fill_message, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	private class SendMessageTask extends AsyncTask<String, Void, Void>{
		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			Lab.get(FeedBackActivity.this).sendFeedback(params[0]);
			return null;
		}		
		@Override
		protected void onPostExecute(Void x){
			Toast.makeText(getApplicationContext(), R.string.feedback_thankyou, Toast.LENGTH_LONG).show();
			killActivity();
		}
	}
	
	public void killActivity(){
		this.finish();
	}
	
	private static String addDebugInfosToErrorMessage(Activity a) throws NameNotFoundException {

        String s = "";

        PackageInfo pInfo = a.getPackageManager().getPackageInfo(a.getPackageName(), PackageManager.GET_META_DATA);
        s += " | APP Package Name: " + a.getPackageName();
        s += " | APP Version Name: " + pInfo.versionName;
        s += " | APP Version Code: " + pInfo.versionCode;
        s += " |  OS Version: " + System.getProperty("os.version") + " ("
                        + android.os.Build.VERSION.INCREMENTAL + ")";
        s += " |  OS API Level: " + android.os.Build.VERSION.SDK;
        s += " |  Device: " + android.os.Build.DEVICE;
        s += " |  Model (and Product): " + android.os.Build.MODEL + " ("
                        + android.os.Build.PRODUCT + ")";
        // TODO add application version!

        // more from
        // http://developer.android.com/reference/android/os/Build.html :
        s += " |  Manufacturer: " + android.os.Build.MANUFACTURER;
        s += " |  Other TAGS: " + android.os.Build.TAGS;

        s += " |  screenWidth: "
                        + a.getWindow().getWindowManager().getDefaultDisplay()
                                        .getWidth();
        s += " | screenHeigth: "
                        + a.getWindow().getWindowManager().getDefaultDisplay()
                                        .getHeight();
        s += " |  Keyboard available: "
                        + (a.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS);

        s += " | Trackball available: "
                        + (a.getResources().getConfiguration().navigation == Configuration.NAVIGATION_TRACKBALL);
        s += " | SD Card state: " + Environment.getExternalStorageState();

        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        String key = "";
        while (keys.hasMoreElements()) {
                key = (String) keys.nextElement();
                s += " |  > " + key + " = " + (String) p.get(key);
        }
        
        LocationManager lm = (LocationManager) a.getSystemService(Context.LOCATION_SERVICE);
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        s += " | Lat: " + loc.getLatitude() + " Long: " + loc.getLongitude() + " Alt: " + loc.getAltitude() + "Time: " + loc.getTime();
        s += " | User Id: " + Lab.get(a).getCurrentUser().getId();

        return s;
}
	
	
}
