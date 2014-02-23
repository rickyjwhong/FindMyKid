package com.rickster.findmykid.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

import com.rickster.findmykid.R;
import com.rickster.findmykid.model.Constants;

public class AlertFragment extends DialogFragment {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){	
		String mName = (String) getArguments().getSerializable(Constants.NAME_EXTRA);
		TextView tv = new TextView(getActivity());	
		tv.setPadding(20, 20, 20, 20);
		if(mName != null) tv.setText(getString(R.string.text_switch_question, mName));
		//Actual Fragment that is getting returned
		return new AlertDialog.Builder(getActivity())
			.setView(tv)
			.setTitle(R.string.alternative_text_method)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					sendResult(true);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                sendResult(false);
	            }
	        })
			.create();
	}
	
	//sending result back to CrimeFragment
	private void sendResult(boolean result){
		if(getTargetFragment() == null)
			return;		
		Intent i = new Intent();
		i.putExtra(Constants.ALERT_RESULT, result);
		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
	}
	
    
    public static AlertFragment newInstance(String name){
    	Bundle args = new Bundle();
		args.putSerializable(Constants.NAME_EXTRA, name);
		
		AlertFragment fragment = new AlertFragment();
		fragment.setArguments(args);		
		return fragment;
    }
    
}
