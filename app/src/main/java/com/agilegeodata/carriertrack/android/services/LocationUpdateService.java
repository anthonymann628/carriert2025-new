package com.agilegeodata.carriertrack.android.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Broadcasts gps updates using a message Runs the broadcast every
 * GlobalConstants.SERVICE_RUN_GPSTASK_EVERY_MILLIS (2 seconds) Broadcasts the
 * following information:
 * intent.putExtra(GlobalConstants.PREF_DEVICE_CURRENT_GEOCODE,
 * deviceLocationListener.getmGeoCode()); intent.putExtra(GlobalConstants.EXTRA_CURRENT_SPEED,
 * deviceLocationListener.getmSpeed()); intent.putExtra(GlobalConstants.PREF_CURRENT_ACCURACY,
 * deviceLocationListener.getmAccuracy()); intent.putExtra(GlobalConstants.EXTRA_CURRENT_BEARING,
 * deviceLocationListener.getmBearing()); intent.putExtra(GlobalConstants.PREF_LASTGPS_SYNC,
 * deviceLocationListener.getmTimestamp());
 * <p>
 * uses DeviceLocationListener for the location Listener is set to run every 2
 * seconds or zero distance traveled. Basically it'll ask for updates all the
 * time.
 */
public class LocationUpdateService extends Service{
	public static final String TAG = LocationUpdateService.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	protected TextToSpeech textToSpeech = null;
	private GpsTask mGPSTask; // do work in this task....
	private Timer mGPSTaskTimer;
	private Intent intent;

	@Override
	public IBinder onBind(final Intent intent){
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		logger.info("STARTED");

		//=== TEXT TO SPEECH
		if(textToSpeech == null){
			textToSpeech = new TextToSpeech(CTApp.appContext, new TextToSpeech.OnInitListener(){
				@Override
				public void onInit(int status){
					if(status != TextToSpeech.ERROR){
						textToSpeech.setLanguage(Locale.ENGLISH);
					}
				}
			});
		}

		mGPSTaskTimer = new Timer();
		mGPSTask = new GpsTask();
		startLocationListener();
	}

	private void startLocationListener(){
//		logger.info("locationManager = " + (locationManager == null ? "NULL" : locationManager.toString()));

		if(CTApp.getSingletonLocationManager() == null){
			logger.info("locationManager is NULL");

			if(ActivityCompat.checkSelfPermission(LocationUpdateService.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
			   != PackageManager.PERMISSION_GRANTED){
				logger.info("DID NOT START deviceLocationListener");
				//=== Check Permissions Now
				//=== this was done in HomeFragment so alert user and
				//=== go back to HomeFragment
				Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.deviceFeatureAccessLocationNotGranted), Toast.LENGTH_LONG).show();
				logger.debug("location access not granted");
			}
			else{
				//=== permission has been granted, continue as usual
				logger.info("STARTED deviceLocationListener");
				List<String> providers = CTApp.getSingletonLocationManager().getProviders(false);

				if(providers.contains(LocationManager.GPS_PROVIDER)){
					CTApp.getSingletonLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, GlobalConstants.DEF_MIN_UPDATE_TIME, 0, CTApp.getDeviceLocationListener());  //TOO FAST
				}
				else if(providers.contains(LocationManager.FUSED_PROVIDER)){
					CTApp.getSingletonLocationManager().requestLocationUpdates(LocationManager.FUSED_PROVIDER, GlobalConstants.DEF_MIN_UPDATE_TIME, 0, CTApp.getDeviceLocationListener());  //TOO FAST
				}
				else{
					logger.error("location providers 'gps' and 'fused' notavailable");
				}

				CTApp.setDeviceLocationListener(new DeviceLocationListener());
				CTApp.getDeviceLocationListener().setMContext(this.getApplicationContext());
				CTApp.getDeviceLocationListener().setPref(getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE));
			}
		}
	}

	private void stopListening(){
//		logger.info("STOPPED");
		if(CTApp.getSingletonLocationManager() != null){
			CTApp.getSingletonLocationManager().removeUpdates(CTApp.getDeviceLocationListener());
		}

		mGPSTaskTimer.cancel();
		mGPSTaskTimer = null;
		mGPSTask.cancel();
		mGPSTask = null;
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId){
//		logger.debug("ENTER");
		int result = super.onStartCommand(intent, flags, startId);

		try{
			mGPSTaskTimer.schedule(mGPSTask, 0, GlobalConstants.SERVICE_RUN_GPSTASK_EVERY_MILLIS);  //TOO FAST
		}
		catch(Exception e){
			logger.debug("EXCEPTION : starting Gps service" + e);
		}

		return result;
	}

	@Override
	public boolean onUnbind(final Intent intent){
		super.onUnbind(intent);
		stopListening();

		return false;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		stopListening();
	}

	/**
	 * Implementation of the timer task.
	 */
	private class GpsTask extends TimerTask{
		public void run(){
			logger.info("STARTED LOCATIONUPDATE SERVICE RUN");

			intent = new Intent(GlobalConstants.SERVICE_LOCATION);
			intent.setPackage(getApplicationContext().getPackageName());

			if(!CTApp.getSingletonLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER) & !CTApp.getSingletonLocationManager().isProviderEnabled(LocationManager.FUSED_PROVIDER)){
				logger.info("GpsTask.run() NO GPS");
				intent.putExtra(GlobalConstants.EXTRA_DEVICE_GPS_ENABLED, false);

				//=== give user some feedback gps is down
				CTApp.getDeviceLocationListener().setHasGPsFix(false);
				textToSpeech.speak("Alert: GPS is not enabled.", TextToSpeech.QUEUE_ADD, null, null);
			}
			else{
				logger.info("GpsTask.run() GOT GPS");
				CTApp.getDeviceLocationListener().setHasGPsFix(true);
				intent.putExtra(GlobalConstants.EXTRA_DEVICE_GPS_ENABLED, DeviceLocationListener.getHasGPsFix());
			}

			intent.putExtra(GlobalConstants.EXTRA_CURRENT_SPEED, CTApp.getDeviceLocationListener().getSpeed());
			intent.putExtra(GlobalConstants.EXTRA_CURRENT_BEARING, CTApp.getDeviceLocationListener().getBearing());

			intent.putExtra(GlobalConstants.PREF_LASTGPS_SYNC, CTApp.getDeviceLocationListener().getTimestamp());

			intent.putExtra(GlobalConstants.HAS_GPS_FIX, DeviceLocationListener.getHasGPsFix());

			logger.info("GpsTask.run() BROADCASTING A GlobalConstants.SERVICE_LOCATION");

			intent.setPackage(getPackageName());//"com.agilegeodata.carriertrack");
			sendBroadcast(intent);
//			logger.info("ENDED LOCATIONUPDATE SERVICE RUN");
		}
	}
}