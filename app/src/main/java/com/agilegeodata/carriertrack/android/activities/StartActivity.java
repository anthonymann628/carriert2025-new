package com.agilegeodata.carriertrack.android.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Window;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.utils.DateUtil;
import com.agilegeodata.carriertrack.android.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/*
 * StartActivity Opening screen
 * Listens for download message that indicates that the database has been updated with the most recent information
 * This message is dispatched by the download service and triggers the redirect to the LoginContainer
 * Kicks off services found in getResources().getStringArray(R.array.bootServices);
 */

public class StartActivity extends Activity{
	private static final String TAG = StartActivity.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	//===HANDLE ALL PERMISSIONS FOR ACCESS UP FRONT
	private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1000;

	private String mDeviceId;

	public static boolean isLocationEnabled(Context context){
		int locationMode = 0;
		String locationProviders;

		try{
			locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
		}
		catch(Settings.SettingNotFoundException e){
			logger.error("EXCEPTION", e);
			return false;
		}

		return locationMode != Settings.Secure.LOCATION_MODE_OFF;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		logger.info(">>>>onCreate() : START");

		doBootstrap();

		SharedPreferences prefs = getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

		if(!prefs.getBoolean(GlobalConstants.PREF_HAVE_SHORTCUT, false)){
			setHomeIcon();
		}

		//    logger.info("Activity class is null");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putBoolean(GlobalConstants.PREF_HAVE_SHORTCUT, true);

		Calendar curCal = Calendar.getInstance();
		String userTimeZone = DateUtil.getUserTimeZone(curCal.getTimeInMillis(), TimeZone.SHORT, Locale.US);
		int timeZoneDifference = DateUtil.getUserTimeZoneOffset(curCal.getTimeInMillis());
		boolean isDaylightSavingsTime = DateUtil.isUserTimeZoneInDST(curCal.getTimeInMillis());

		editor.putString(GlobalConstants.PREF_LOCAL_TIME_ZONE, userTimeZone);

		boolean isWestOfGMT = timeZoneDifference < 1;

		if(isWestOfGMT){
			editor.putInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, Math.abs(timeZoneDifference));
		}
		else{
			editor.putInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, timeZoneDifference);
		}

		editor.putBoolean(GlobalConstants.PREF_IS_DST, isDaylightSavingsTime);
		editor.apply();

		setContentView(R.layout.start);
	}

	@Override
	public void onStop(){
		logger.info("START");
		super.onStop();
	}

	@Override
	protected void onStart(){
		logger.info("START");
		super.onStart();

		if(!isLocationEnabled(this)){
			gpsAlert();
		}
		else{
			locationPrivacyDialog();
		}
	}

	@Override
	public void onResume(){
		super.onResume();

		logger.info("START");

		String lang = DBHelper.getInstance().fetchItemValueByItemName_Common(GlobalConstants.PREF_LANGUAGE);

		if(lang == null){
			lang = GlobalConstants.ENGLISH_LANGUAGE;
		}

		Locale locale = new Locale(lang);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		getBaseContext().getResources().updateConfiguration(config, metrics);
	}

	@Override
	public void onDestroy(){
		logger.info("START");
		super.onDestroy();
	}

	private boolean checkAndRequestAppPermissions(){
		int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
		int permissionAccessFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

		List<String> listPermissionsNeeded = new ArrayList<>();

		if(permissionCamera != PackageManager.PERMISSION_GRANTED){
			listPermissionsNeeded.add(Manifest.permission.CAMERA);
		}
		if(permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED){
			listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
		}

		if(!listPermissionsNeeded.isEmpty()){
			ActivityCompat.requestPermissions(this,
											  listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
											  REQUEST_ID_MULTIPLE_PERMISSIONS);

			return false;
		}
		else{
			checkPassThroughToHomeScreen();
		}

		return true;
	}
	//===HANDLE ALL PERMISSIONS FOR ACCESS UP FRONT

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
		//logger.debug("STARTED Permission callback");

		switch(requestCode){
			case REQUEST_ID_MULTIPLE_PERMISSIONS:{
				// Fill with actual results from user
				if(grantResults.length > 0){
					boolean allPermissionsGranted = true;
					for(int i = 0; i < grantResults.length; i++){

						if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
							allPermissionsGranted = false;
							break;
						}
					}

					// Check for any non-granted permissions
					if(allPermissionsGranted){
						logger.debug("onRequestPermissionsResult() : all permissions granted");

						checkPassThroughToHomeScreen();
					}
					else{
						//logger.debug("Some permissions are not granted ask again ");

						//show the dialog saying its necessary and try again otherwise terminate app.
						showDialogOkCancel(getResources().getString(R.string.deviceFeaturePermissionsRequiredForThisApp),
										   new DialogInterface.OnClickListener(){
											   @Override
											   public void onClick(DialogInterface dialog, int which){
												   switch(which){
													   case DialogInterface.BUTTON_POSITIVE:
														   String packageName = getPackageName();
														   Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null));
														   intent.addCategory(Intent.CATEGORY_DEFAULT);
														   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
														   startActivity(intent);
														   break;
													   case DialogInterface.BUTTON_NEGATIVE:
														   // proceed with logic by disabling the related features or quit the app.
														   dialog.cancel();
														   finish();
														   break;
												   }
											   }
										   });
					}
				}
			}
			break;
		}
	}

	private void showDialogOkCancel(String message, DialogInterface.OnClickListener okListener){
		new AlertDialog.Builder(this).setCancelable(false)
									 .setMessage(message)
									 .setPositiveButton(getResources().getString(R.string.dialogOk), okListener)
									 .setNegativeButton(getResources().getString(R.string.dialogCancel), okListener)
									 .create()
									 .show();
	}

	private void locationPrivacyDialog(){
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage("This app collects detailed location information in both foreground and background modes and\n" +
						  "delivers it to an external site that processes it.\n" +
						  "This location information is stored on the device until it is delivered and uploaded to the external site\n" +
						  "at which time it will be deleted from this device.")
			  .setPositiveButton("Continue", new DialogInterface.OnClickListener(){
				  @Override
				  public void onClick(DialogInterface paramDialogInterface, int paramInt){
					  checkAndRequestAppPermissions();
				  }
			  });
		dialog.show();
	}

	private void gpsAlert(){
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(getResources().getString(R.string.deviceFeatureAccessGpsNotGrantedTerminateApp))
			  .setPositiveButton(getResources().getString(R.string.dialogOk), new DialogInterface.OnClickListener(){
				  @Override
				  public void onClick(DialogInterface paramDialogInterface, int paramInt){
					  finish();
				  }
			  });
		dialog.show();
	}

	private void setHomeIcon(){
		Intent shortcutIntent = new Intent(getApplicationContext(), StartActivity.class);
		shortcutIntent.setAction(Intent.ACTION_MAIN);

		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.cts_logo));
		addIntent.putExtra(GlobalConstants.EXTRA_DUPLICATE, false);
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

		addIntent.setPackage(getPackageName());//"com.agilegeodata.carriertrack");
		getApplicationContext().sendBroadcast(addIntent);
	}

	/*
	 * Bootstraps information into the sharedpreferences used by the app
	 * including device id current geocode (sets to null), sdcard path If
	 * getResources().getString(R.string.useTestLoc).equals("true") database
	 * queries to reset address upload and delivered statuses will be run.
	 */
	private void doBootstrap(){
		//logger.info("START");

		SharedPreferences prefs = this.getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

		mDeviceId = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		logger.info("Current Device id: " + mDeviceId);

		// edit preferences

		String savePath = FileUtils.getAppDirectoryForSavedFiles();
		String sdcard = FileUtils.getAppDirectoryForFiles();
		//logger.info("savePath = " + GlobalConstants.PREF_SAVE_DATA_DIRECTORY);

		Calendar c = Calendar.getInstance();

		Editor edit = prefs.edit();
		edit.putString(FileUtils.getAppDirectoryForFiles(), sdcard);
		edit.putString(FileUtils.getAppDirectoryForSavedFiles(), savePath);
		edit.putString(GlobalConstants.PREF_DEVICE_ID, mDeviceId);
		logger.info("DEVICE ID: " + mDeviceId);
		edit.commit();

		//    logger.info("Files will be saved to: "	+ prefs.getString(GlobalConstants.PREF_SAVE_DATA_DIRECTORY, null));
	}

	private void checkPassThroughToHomeScreen(){
		// Check to make sure that the upgrade of the database has occurred.
		//logger.info("START");

		new Handler().postDelayed(new Runnable(){
			@Override
			public void run(){
				Intent myr = new Intent(getApplicationContext(), LoginContainer.class);
				myr.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myr);
			}
		}, 4000);
	}
}