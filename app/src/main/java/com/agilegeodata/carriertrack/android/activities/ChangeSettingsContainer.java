package com.agilegeodata.carriertrack.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * ChangeSettings Container
 */
public class ChangeSettingsContainer extends Activity{
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static final String TAG = ChangeSettingsContainer.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.changesettings_container);

		DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, ChangeSettingsContainer.class.getName());
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