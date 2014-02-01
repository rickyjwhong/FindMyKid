package com.rickster.findmykid.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class ContactsListActivity extends SingleFragmentActivity {
	
	private static final String TAG = "ConnectList";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
	}

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new ContactsListFragment();
	}
	
}
