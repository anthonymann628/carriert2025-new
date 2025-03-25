package com.agilegeodata.carriertrack.android.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.ChangeSettingsContainer;
import com.agilegeodata.carriertrack.android.activities.DeviceStatusContainer;
import com.agilegeodata.carriertrack.android.activities.ManualSyncContainer;
import com.agilegeodata.carriertrack.android.activities.RouteSelectActivity;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;
import com.agilegeodata.carriertrack.android.objects.LoginStatus;
import com.agilegeodata.carriertrack.android.objects.ServerAccessActivity;
import com.agilegeodata.carriertrack.android.services.RunningServices;
import com.agilegeodata.carriertrack.android.utils.DataUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Main screen fragment
 */
public class HomeFragment extends Fragment{
	static final String TAG = HomeFragment.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static boolean gpsRadioButtonIsChecked = false;
	private final Handler uploadDownloadMessageHandler = new Handler(){
		public void handleMessage(Message msg){
			Bundle data = msg.getData();
			boolean mHaveError = data.getBoolean(GlobalConstants.EXTRA_UPLOAD_STATUS, false);
			String error = data.getString(GlobalConstants.EXTRA_ERROR_MESSAGE);
			// logger.debug("Entered the uploadDownloadMessageHandler" + mHaveError);

			logger.debug((data.getInt(GlobalConstants.EXTRA_TASK_TYPE) == GlobalConstants.MANUAL_DOWNLOAD ? "DOWNLOAD is " : "UPLOAD is ") + (mHaveError ? "UNSUCCESSFUL " : "SUCCESSFUL ") + error);
		}
	};
	private final BroadcastReceiver uploadStatusReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
			Bundle data = intent.getExtras();
			boolean mHaveError = data.getBoolean(GlobalConstants.EXTRA_UPLOAD_STATUS, false);

			data.putInt(GlobalConstants.EXTRA_TASK_TYPE, GlobalConstants.MANUAL_UPLOAD);
			// logger.debug("Success: " + mHaveError +"  error message: " +
			// error);
			Message msg = new Message();
			msg.setData(data);

			uploadDownloadMessageHandler.sendMessage(msg);
		}
	};
	private final BroadcastReceiver downloadStatusReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
			Bundle data = intent.getExtras();
			boolean mHaveError = data.getBoolean(GlobalConstants.EXTRA_UPLOAD_STATUS, false);

			data.putInt(GlobalConstants.EXTRA_TASK_TYPE, GlobalConstants.MANUAL_DOWNLOAD);
			// logger.debug("Success: " + mHaveError +"  error message: " +
			// error);
			Message msg = new Message();
			msg.setData(data);

			uploadDownloadMessageHandler.sendMessage(msg);
		}
	};
	AppCompatRadioButton mGpsStatusRadioButton;
	TextView mTopNavLine1;
	TextView mTopNavLine2;
	TextView mLastServerConnection = null;
	RelativeLayout deliverButton;
	RelativeLayout sequencingButton;
	RelativeLayout renumberingButton;
	GlobalConstants.OPERATIONS_MODE tempOpMode = CTApp.operationsMode;
	private TextView tvLastSync;
	private View viewer;
	private boolean mIsHandlingGps;
	private final Handler gpsReceiverHandler = new Handler(){
		public void handleMessage(Message msg){
			logger.debug(">>>>gpsReceiverHandler.handleMessage() : STARTED : mIsHandlingGps = " + mIsHandlingGps);
			if(!isDetached()){
				updateTopNav();
				mIsHandlingGps = false;
			}
		}
	};
	private final BroadcastReceiver gpsReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
		logger.debug(">>>>gpsReceiver.onReceive() : STARTED : mIsHandlingGps = " + mIsHandlingGps);
		mIsHandlingGps = true;

		mLastServerConnection.setText(getResources().getString(R.string.homeLastConnection) + ServerAccessActivity.getServerTimestamp(getActivity()));

		mLastServerConnection.invalidate();
		gpsReceiverHandler.sendEmptyMessage(0);
		}
	};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
//		logger.debug(">>>>>>>>>>> homeactivity.onCreate STARTING SERVICES");

		RunningServices.startAllServicesIfNotrunning(this.getActivity());

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			ContextCompat.registerReceiver(getContext(), gpsReceiver,
										   new IntentFilter(GlobalConstants.SERVICE_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED);
			ContextCompat.registerReceiver(getContext(), uploadStatusReceiver,
										   new IntentFilter(GlobalConstants.SERVICE_UPLOAD), ContextCompat.RECEIVER_NOT_EXPORTED);
			ContextCompat.registerReceiver(getContext(), downloadStatusReceiver,
										   new IntentFilter(GlobalConstants.SERVICE_DOWNLOAD), ContextCompat.RECEIVER_NOT_EXPORTED);
		}
		else{
			getContext().registerReceiver(gpsReceiver,
										  new IntentFilter(GlobalConstants.SERVICE_LOCATION));
			getContext().registerReceiver(uploadStatusReceiver,
										  new IntentFilter(GlobalConstants.SERVICE_UPLOAD));
			getContext().registerReceiver(downloadStatusReceiver,
										  new IntentFilter(GlobalConstants.SERVICE_DOWNLOAD));
		}

		setRetainInstance(true);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
//		logger.debug("STARTED");

		viewer = inflater.inflate(R.layout.home, container, false);

		return viewer;
	}

	@Override
	public void onActivityCreated(Bundle savedState){
		super.onActivityCreated(savedState);

//		logger.debug(">>>>onActivityCreated() : STARTED");

		setLastSyncView();

		//logger.debug("datelong: "+ dateLong+ " DateFriendly: " + dateFriendly);

		if(mLastServerConnection == null){
			mLastServerConnection = getActivity().findViewById(R.id.lastServerSync);
		}

		TextView tvVersion = getActivity().findViewById(R.id.topNavVersion);
		String vName = "?";
		try{
			PackageInfo manager = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			vName = manager.versionName;
		}
		catch(NameNotFoundException e){
			logger.debug("Exception : " + e);
		}

		tvVersion.setText(getResources().getString(R.string.homeAppVersion, vName));
		tvVersion.invalidate();

		updateTopNav();

		deliverButton = getActivity().findViewById(R.id.homeScreenDeliverVerifyContainer);
		sequencingButton = getActivity().findViewById(R.id.homeScreenResequenceContainer);
		renumberingButton = getActivity().findViewById(R.id.homeScreenRenumberContainer);

		setClicking();
	}

	/**
	 * Sets last sync of download from manual
	 **/
	private void setLastSyncView(){
//		logger.debug("STARTED");

		SharedPreferences prefs = getActivity().getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

		String dateFriendly = getResources().getString(R.string.notAvailable);

		if(tvLastSync == null){
			tvLastSync = getActivity().findViewById(R.id.rowLastSync);
		}
		long dateLong = prefs.getLong(GlobalConstants.PREF_LASTTIME_DOWNLOAD_SYNC, 0);

		if(dateLong != 0){
			dateFriendly = DataUtils.calcLastPerformed(dateLong, getResources(), GlobalConstants.DEFAULT_DATETIME_FORMAT);
		}

		String text = getResources().getString(R.string.homeLastSuccessfulSync, dateFriendly);
		tvLastSync.setText(text);
		tvLastSync.invalidate();
	}

	@SuppressLint("RestrictedApi")
	private void updateTopNav(){
//		logger.debug("STARTED");

		try{
			Activity thisActivity = getActivity();
			if(thisActivity != null){
				SharedPreferences prefs = getActivity().getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

				String userFirstName = prefs.getString(
						GlobalConstants.PREF_USERFIRSTNAME, null);

				mTopNavLine1 = getActivity().findViewById(R.id.topNavLine1);
				String line1 = getResources().getString(R.string.homeWelcome);

				if(userFirstName != null){
					line1 = getResources().getString(R.string.homeWelcomePlusName, userFirstName);
				}

				mTopNavLine1.setText(line1);

				mTopNavLine2 = getActivity().findViewById(R.id.topNavLine2);
				mTopNavLine2.setText(getResources().getString(R.string.homeSelectTask));

				mGpsStatusRadioButton = getActivity().findViewById(R.id.gpsRadioButton);
				ColorStateList colorStateList = new ColorStateList(
						new int[][]{
								new int[]{android.R.attr.state_checked}, // checked
								new int[]{android.R.attr.state_enabled} // unchecked
						},
						new int[]{
								Color.GREEN, // checked
								Color.RED   // unchecked
						}
				);
				mGpsStatusRadioButton.setSupportButtonTintList(colorStateList);
				gpsRadioButtonIsChecked = DeviceLocationListener.getHasGPsFix();
				mGpsStatusRadioButton.setChecked(DeviceLocationListener.getHasGPsFix());
//				logger.debug("SET GPS BUTTON TO (DeviceLocationListener.isGPSFix) = " + DeviceLocationListener.isGPSFix);

				//=== LETS USER CLICK TO TURN ON GPS
				mGpsStatusRadioButton.setOnClickListener(new View.OnClickListener(){
					public void onClick(View view){
						if(!gpsRadioButtonIsChecked && !DeviceLocationListener.getHasGPsFix()){
							startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							gpsRadioButtonIsChecked = DeviceLocationListener.getHasGPsFix();
							mGpsStatusRadioButton.setChecked(DeviceLocationListener.getHasGPsFix());//.setImageDrawable(DataUtils.determineGPSStatusImagePortrait(getResources()));
						}
					}
				});
			}
		}
		catch(NullPointerException e){
			Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.fail), Toast.LENGTH_SHORT).show();

		}
	}

	/**
	 * Renews login after expiration
	 */
	private void renewLogin(){
//		logger.debug("STARTED");

		Class<?> activityClass;
		try{
			activityClass = Class.forName("com.agilegeodata.carriertrack.android.activities.StartActivity");
			Intent r = new Intent(getActivity().getApplicationContext(), activityClass);
			r.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(r);

			// Orderly cleanup of this activity
			if(gpsReceiver != null){
				getActivity().unregisterReceiver(gpsReceiver);
			}

			getActivity().finish();
		}
		catch(ClassNotFoundException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
//		logger.debug("STARTED");

		if(requestCode == 1111){
			if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
				// We can now safely use the API we requested access to
				doClicking(getActivity().findViewById(R.id.homeScreenDeliverVerifyContainer),
						   new Intent(getActivity(), RouteSelectActivity.class));
			}
			else{
				//=== Permission was denied or request was cancelled
			}
		}
	}

	/**
	 * HANDLE CLICKS HERE
	 **/
	private void setClicking(){
//		logger.debug("STARTED");

		//=== DELIVERY:VERIFY
		doClicking(getActivity().findViewById(R.id.homeScreenDeliverVerifyContainer),
				   new Intent(getActivity(), RouteSelectActivity.class));

		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
			getActivity().findViewById(R.id.homeScreenResequenceContainer).setVisibility(View.VISIBLE);
			getActivity().findViewById(R.id.homeScreenRenumberContainer).setVisibility(View.GONE);
		}
		else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
			getActivity().findViewById(R.id.homeScreenResequenceContainer).setVisibility(View.GONE);
			getActivity().findViewById(R.id.homeScreenRenumberContainer).setVisibility(View.VISIBLE);
		}
		else{
			getActivity().findViewById(R.id.homeScreenResequenceContainer).setVisibility(View.GONE);
			getActivity().findViewById(R.id.homeScreenRenumberContainer).setVisibility(View.GONE);
		}

		//=== RE-NUMBERING
		logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
		doClicking(getActivity().findViewById(R.id.homeScreenRenumberContainer),
				   new Intent(getActivity(), RouteSelectActivity.class));
		//=== RE-SEQUENCING
		logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
		doClicking(getActivity().findViewById(R.id.homeScreenResequenceContainer),
				   new Intent(getActivity(), RouteSelectActivity.class));
		//=== DEVICE STATUS
		doClicking(getActivity().findViewById(R.id.row2RL),
				   new Intent(getActivity(), DeviceStatusContainer.class));
		//=== SETTINGS
		doClicking(getActivity().findViewById(R.id.row3RL),
				   new Intent(getActivity(), ChangeSettingsContainer.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		//=== MANUAL SYNC
		doClicking(getActivity().findViewById(R.id.row4RL),
				   new Intent(getActivity(), ManualSyncContainer.class));
	}

	private void doClicking(RelativeLayout rl, final Intent theReceivingActivity){
//		logger.debug("STARTED");

		rl.setClickable(true);

		rl.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				if(LoginStatus.hasExpired()){
					renewLogin();
					return;
				}

				startActivity(theReceivingActivity);
			}
		});
	}

	@Override
	public void onPause(){
		super.onPause();
	}

	@Override
	public void onStop(){
		super.onStop();
	}

	@Override
	public void onDestroy(){
		logger.debug(">>>>>>>>>>> homeactivity.onDestroy KILLING SERVICES");

		try{
			if(gpsReceiver != null){
				getActivity().unregisterReceiver(gpsReceiver);
			}
			if(downloadStatusReceiver != null){
				getActivity().unregisterReceiver(downloadStatusReceiver);
			}
			if(uploadStatusReceiver != null){
				getActivity().unregisterReceiver(uploadStatusReceiver);
			}
		}
		catch(Exception e){
			logger.error("EXCEPTION : " + e.getMessage());
		}

		//=== KILL THE SERVICES HERE, THEY ARE ONLY STARTED IN THIS ACTIVITY
		//=== THIS IS JUST A PASS THROUGH TO THE REST OF THE APP
		RunningServices.killAllServicesIfRunning(this.getActivity());

		super.onDestroy();
	}

	@Override
	public void onResume(){
//		logger.debug("STARTED");

		logger.debug(">>>>>>>>>>> homeactivity.onResume STARTING SERVICES");

		String lang = DBHelper.getInstance().fetchItemValueByItemName_Common(GlobalConstants.PREF_LANGUAGE);

		if(lang == null){
			lang = GlobalConstants.ENGLISH_LANGUAGE;
		}

		setLastSyncView();

		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
			deliverButton.setVisibility(View.GONE);
			sequencingButton.setVisibility(View.VISIBLE);
			renumberingButton.setVisibility(View.GONE);
		}
		else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
			deliverButton.setVisibility(View.GONE);
			sequencingButton.setVisibility(View.GONE);
			renumberingButton.setVisibility(View.VISIBLE);
		}
		else{
			deliverButton.setVisibility(View.VISIBLE);
			sequencingButton.setVisibility(View.GONE);
			renumberingButton.setVisibility(View.GONE);
		}

		super.onResume();
	}
}