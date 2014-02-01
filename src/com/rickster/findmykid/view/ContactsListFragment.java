package com.rickster.findmykid.view;

import com.rickster.findmykid.controller.SendGCMTask;

import android.support.v4.app.Fragment;
import android.view.View;

public class ContactsListFragment extends Fragment {
	
	//get content provider, 
	//list of name, email, phone number with checkboxes
	//use viewholders - good practice
	
	
	
	
	public void sendGCMMessage(final View view){
		final String message = "Test";
		SendGCMTask sendTask = new SendGCMTask(getActivity());
		String targetId = "This is the Target Id";
		//get it from Lab
		sendTask.execute(message, targetId);
	}
	
	
	
	
	
}
