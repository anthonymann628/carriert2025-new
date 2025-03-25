package com.agilegeodata.carriertrack.android.activities;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;
import com.agilegeodata.carriertrack.android.utils.FileUtils;
import com.agilegeodata.carriertrack.android.utils.LoggerFileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class CTApp extends Application{
	static public String TAG = CTApp.class.getSimpleName();
	//=======================================================
	//=== THIS MUST CHANGE ANYTIME A MODIFICATION IS MADE
	//=== TO THE APP DATABASE. MAKE IT THE SAME AS THE
	//=== APP VERSION NUMBER
	//=======================================================
	static public int DATABASE_VERSION = 7050;
	static public boolean isInSimulation = false;

	//===WITH THE CURRENT WAYPOINT PARAMETER LIST BEING USED
	//===TO CALCULATE THE ROUTE, 49 WAYPOINT COUNT WILL CAUSE
	//===A SERVER INTERNAL ERROR DUE TO THE API CALL TEXT
	//===CONTENT BEING TO LARGE. STAY WITH 40 WAYPOINTS TO
	//===GIVE A BUFFER IN CASE THE WAYPOINT CHARACTERISTICS CHANGE
	static public int NAVIGATION_ROUTE_SIZE = 40;//49;

	static public GlobalConstants.OPERATIONS_MODE operationsMode = GlobalConstants.OPERATIONS_MODE.DELIVERING;
	public static Context appContext;
	private static Logger logger;
	static private LocationManager singletonLocationManager = null;
	static private DeviceLocationListener deviceLocationListener = null;
	static private int jobDetailId = 0;
	SharedPreferences prefs;

	static public int getJobDetailId(){
		return jobDetailId;
	}

	static public void setJobDetailId(int newJobDetailId){
		jobDetailId = newJobDetailId;
	}

	//=== CURRENTLY NOT USED
	//private FirebaseAnalytics mFirebaseAnalytics;

	static public LocationManager getSingletonLocationManager(){
		return singletonLocationManager;
	}

	static public Location getLocation(){
		Location location = null;
		if(ActivityCompat.checkSelfPermission(CTApp.getCustomAppContext(), Manifest.permission.ACCESS_FINE_LOCATION)
		   != PackageManager.PERMISSION_GRANTED){
			// Check Permissions Now
			//this was done in HomeFragment so alert user and
			//go back to HomeFragment
			logger.debug("LOCATION ACCESS NOT GRANTED");

			return null;
		}
		else{
			List<String> providers = CTApp.singletonLocationManager.getProviders(false);

			if(providers.contains(LocationManager.GPS_PROVIDER)){
				location = CTApp.singletonLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			else if(providers.contains(LocationManager.FUSED_PROVIDER)){
				location = CTApp.singletonLocationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
			}
			else{
				logger.error("location providers 'gps' and 'fused' notavailable");
			}

			if(location == null){
				logger.debug("LOCATION RETURNED A NULL");
			}
		}

		return location;
	}

	static public DeviceLocationListener getDeviceLocationListener(){
		return deviceLocationListener;
	}

	static public void setDeviceLocationListener(DeviceLocationListener listener){
		deviceLocationListener = listener;
	}

	public static Context getCustomAppContext(){
		return appContext;
	}

	public SharedPreferences getPrefs(){
		return prefs;
	}

	public void setPrefs(SharedPreferences prefs){
		this.prefs = prefs;
	}

	@Override
	public void onTerminate(){
		super.onTerminate();
		DBHelper.getInstance().close();
	}

	@Override
	public void onCreate(){
		super.onCreate();
		appContext = getApplicationContext();

		singletonLocationManager = (LocationManager) CTApp.appContext.getSystemService(Context.LOCATION_SERVICE);

		deviceLocationListener = new DeviceLocationListener();
		deviceLocationListener.setMContext(this.getApplicationContext());
		deviceLocationListener.setPref(getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE));

		//=== Obtain the FirebaseAnalytics instance.
		//mFirebaseAnalytics. = FirebaseAnalytics.getInstance(this.getApplicationContext());

		//=== this configures and creates log files
		LoggerFileUtils.init();
		logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

		int appVersionNumber = -1;
		try{
			PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			appVersionNumber = pinfo.versionCode;
			logger.info("appVersionNumber = " + appVersionNumber);
		}
		catch(PackageManager.NameNotFoundException nnf){
			logger.error("EXCEPTION : could not get app version", nnf);
		}

		//=== THIS IS NEVER USED BUT ONLY TO INSTANTIATE DBHELPER
		DBHelper dbh = DBHelper.getInstance();

		//=== CHECKS PHYSICAL EXISTENCE OF DATABASE
		boolean dbExists = DBHelper.getInstance().checkDataBase_Common();

		if(!dbExists){
			logger.info("database DOES NOT exist");
			removeOldFiles();
		}

		logger.info("STARTED deviceLocationListener");
		logger.debug("CTApp.onCreate() : singletonLocationManager = " + singletonLocationManager);
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			logger.debug("NO PERMISSIONS FOR LOCATION");
		}
		else{
			List<String> providers = CTApp.singletonLocationManager.getProviders(false);

			if(providers.contains(LocationManager.GPS_PROVIDER)){
				CTApp.singletonLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GlobalConstants.DEF_MIN_UPDATE_TIME, 0, deviceLocationListener);  //TOO FAST
			}
			else if(providers.contains(LocationManager.FUSED_PROVIDER)){
				CTApp.singletonLocationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, GlobalConstants.DEF_MIN_UPDATE_TIME, 0, deviceLocationListener);  //TOO FAST
			}
			else{
				logger.error("location providers 'gps' and 'fused' notavailable");
			}
		}

		String tempAdminMode = DBHelper.getInstance().fetchItemValueByItemName_Common(GlobalConstants.PREF_ADMIN_MODE);

		if(tempAdminMode != null && tempAdminMode.contains("ctseq")){
			operationsMode = GlobalConstants.OPERATIONS_MODE.SEQUENCING;
		}
		else if(tempAdminMode != null && tempAdminMode.contains("ctrenum")){
			operationsMode = GlobalConstants.OPERATIONS_MODE.RENUMBERING;
		}
		else{
			operationsMode = GlobalConstants.OPERATIONS_MODE.DELIVERING;
		}
		logger.debug("--->operationsMode = " + CTApp.operationsMode);
	}

	private void removeOldFiles(){
		//logger.info("STARTED");

		String logPath = FileUtils.getAppDirectoryForLogFiles();
		String savePath = FileUtils.getAppDirectoryForSavedFiles();
		//logger.info("savePath = " + savePath);

		// Get the list of files in the directory
		File saveDir = new File(savePath);

		//logger.info("dir.isDirectory() = " + dir.isDirectory());
		if(saveDir.isDirectory()){
			File[] files = saveDir.listFiles();
			//logger.info("files.length = " + files.length);

			if(files != null){
				// Loop through all files
				for(File f : files){
					f.delete();
				}
			}
		}
		// Get the list of files in the directory
		File logDir = new File(logPath);

		//logger.info("dir.isDirectory() = " + dir.isDirectory());
		if(logDir.isDirectory()){
			File[] files = logDir.listFiles();
			//logger.info("files.length = " + files.length);

			if(files != null){
				// Loop through all files
				for(File f : files){
					f.delete();
				}
			}
		}
	}
}
