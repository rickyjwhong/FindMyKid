package com.rickster.findmykid.view;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.nullwire.trace.ExceptionHandler;
import com.rickster.findmykid.R;
import com.rickster.findmykid.R.color;
import com.rickster.findmykid.model.Constants;

public class MainActivity extends FragmentActivity implements ControlFragment.Callbacks{
	
	public static final String TAG = "MainActivity";
	
	private FragmentManager fm;
	private long locationId;
	private ImageButton mResearchButton;
	private View mControlView;
	private SharedPreferences mPref;
	private boolean mFirstTime;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ExceptionHandler.register(this, Constants.URL_STACKTRACE);
		ActionBar bar = getActionBar();
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		bar.setCustomView(R.layout.custom_action_bar);
		
		setContentView(R.layout.activity_home);	
		mPref = this.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
		mFirstTime = mPref.getBoolean(Constants.FIRST_TIME, true);		
		if(mFirstTime){
			Intent i = new Intent(MainActivity.this,  Init.class);
			startActivity(i);
		}else{
			fm = getSupportFragmentManager();
			mResearchButton = (ImageButton) findViewById(R.id.research_button);
			mResearchButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					setUpControl();
				}
			});
			
			mControlView = findViewById(R.id.control_frame);
			locationId = -1;
			setUpMap(locationId, false);
			setUpControl();
		}	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.menu_add_tracking:
				Intent i = new Intent(MainActivity.this, SettingActivity.class);
				startActivity(i);
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setUpMap(long locationId, boolean text){
		Fragment oldFragment = fm.findFragmentById(R.id.map_frame);
		Fragment newFragment = MapFragment.newInstance(locationId, text);
		if(oldFragment != null) fm.beginTransaction().remove(oldFragment).commit();
		fm.beginTransaction().add(R.id.map_frame, newFragment).commit();
	}
	
	private void setUpControl(){
		mControlView.setVisibility(View.VISIBLE);
		Fragment oldFragment = fm.findFragmentById(R.id.control_frame);
		Fragment newFragment = new ControlFragment();
		mResearchButton.setVisibility(View.INVISIBLE);
		if(oldFragment != null) fm.beginTransaction().remove(oldFragment).commit();
		fm.beginTransaction().add(R.id.control_frame, newFragment).commit();
	}
	
	private void removeControl(){
		Fragment oldFragment = fm.findFragmentById(R.id.control_frame);
		fm.beginTransaction().remove(oldFragment).commit();
		mControlView.setVisibility(View.GONE);
		mResearchButton.setVisibility(View.VISIBLE);
	}

	@Override
	public void locationRetrieved(long id, boolean text) {
		// TODO Auto-generated method stub
		removeControl();
		Log.i(TAG, "" + id);
		setUpMap(id, text);
	}
	
	
	
}
