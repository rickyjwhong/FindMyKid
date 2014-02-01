package com.rickster.findmykid.view;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.nullwire.trace.ExceptionHandler;
import com.rickster.findmykid.R;
import com.rickster.findmykid.model.Constants;

public class SettingActivity extends SingleFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ExceptionHandler.register(this, Constants.URL_STACKTRACE);
		ActionBar bar = getActionBar();
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		bar.setCustomView(R.layout.custom_action_bar);
	}
	
	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new SettingFragment();
	}

}
