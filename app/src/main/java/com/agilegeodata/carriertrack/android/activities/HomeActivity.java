package com.agilegeodata.carriertrack.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  Home Container
 */
public class HomeActivity extends Activity{
	static final String TAG = HomeActivity.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//logger.debug("ENTER");

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.home_container);
	}

	@Override
	public void onStop(){
		super.onStop();
	}

	@Override
	protected void onStart(){
		super.onStart();
	}
}