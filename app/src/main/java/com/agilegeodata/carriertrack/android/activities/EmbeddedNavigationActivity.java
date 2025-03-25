package com.agilegeodata.carriertrack.android.activities;

import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;

/*
 * Route Select Container
 */
public class EmbeddedNavigationActivity extends AppCompatActivity{

	public static final String TAG = EmbeddedNavigationActivity.class.getSimpleName();
	//private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	@Override
	public void onCreate(Bundle savedInstanceState){
		//logger.info("STARTED");

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.embedded_navigation_container);
	}

	@Override
	public void onResume(){
		super.onResume();

		//logger.info("STARTED");

		DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, EmbeddedNavigationActivity.class.getName());
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