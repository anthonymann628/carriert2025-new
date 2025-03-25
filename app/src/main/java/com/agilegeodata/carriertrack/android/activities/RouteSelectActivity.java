package com.agilegeodata.carriertrack.android.activities;

import android.os.Bundle;
import android.view.Window;

import androidx.fragment.app.FragmentActivity;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Route Select Container
 */
public class RouteSelectActivity extends FragmentActivity{

	public static final String TAG = RouteSelectActivity.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//logger.info("STARTED");

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.routeselect_container);

	}

	@Override
	public void onResume(){
		super.onResume();

		//logger.info("STARTED");

		DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, RouteSelectActivity.class.getName());
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