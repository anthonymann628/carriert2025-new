package com.agilegeodata.carriertrack.android.services;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunningServices{
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	static public ActivityManager.RunningServiceInfo getRunningService(Activity a, Class<?> serviceClass){
		ActivityManager manager = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
		for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
			if(serviceClass.getName().equals(service.service.getClassName())){
				return service;
			}
		}
		return null;
	}

	static public boolean isMyServiceRunning(Activity a, Class<?> serviceClass){
		ActivityManager manager = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
		for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
			if(serviceClass.getName().equals(service.service.getClassName())){
				return true;
			}
		}
		return false;
	}

	static public void startAllServicesIfNotrunning(Activity a){
		for(int i = 0; i < GlobalConstants.ALL_APP_SERVICES.length; i++){
			logger.debug("CHECKING service : " + GlobalConstants.ALL_APP_SERVICES[i].getSimpleName());

			if(RunningServices.isMyServiceRunning(a, GlobalConstants.ALL_APP_SERVICES[i])){
				logger.debug("STARTING service : " + GlobalConstants.ALL_APP_SERVICES[i].getSimpleName() + " IS ALREADY RUNNING");
			}
			else{
				logger.debug("STARTING service : " + GlobalConstants.ALL_APP_SERVICES[i].getSimpleName());

				Intent theIntent = new Intent(a.getApplicationContext(), GlobalConstants.ALL_APP_SERVICES[i]);

				//=== CRASHLYTICS BUG REPORTED HERE
				a.startService(theIntent);
			}
		}
	}

	static public void killAllServicesIfRunning(Activity a){
		for(int i = 0; i < GlobalConstants.ALL_APP_SERVICES.length; i++){
			logger.debug("CHECKING service : " + GlobalConstants.ALL_APP_SERVICES[i].getSimpleName());

			if(RunningServices.isMyServiceRunning(a, GlobalConstants.ALL_APP_SERVICES[i])){
				logger.debug("KILLING service : " + GlobalConstants.ALL_APP_SERVICES[i].getSimpleName());

				Intent theIntent = new Intent(a.getApplicationContext(), GlobalConstants.ALL_APP_SERVICES[i]);
				a.stopService(theIntent);
			}
		}
	}

}
