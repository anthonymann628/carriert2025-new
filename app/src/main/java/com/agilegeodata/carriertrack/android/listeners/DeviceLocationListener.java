package com.agilegeodata.carriertrack.android.listeners;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.objects.Breadcrumb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Location listener
 * Updates the preferences with the gps data
 * Will record every 15th Breadcrumb to the database
 */
public class DeviceLocationListener implements LocationListener{
	public static final String TAG = DeviceLocationListener.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static double latitude;
	private static double longitude;
	private static boolean mHasGPsFix = false;
	private SharedPreferences pref;
	private Context mContext;
	private String mGeoCode;
	private float mSpeed;
	private float mBearing;
	private long mTimestamp;
	private long mTimestampLocal;
	private long mTimestampLocalGpsDifference;
	private long mPreviousTimestamp;
	private float mAccuracy;
	private int mCount = 0;

	public static boolean getHasGPsFix(){
		return mHasGPsFix;
	}

	public void setHasGPsFix(boolean isFixed){
		mHasGPsFix = isFixed;
	}

	public void onLocationChanged(Location arg0){
		//logger.info("DeviceLocationListener.onLocationChanged() : START arg0 = " + arg0.toString());

		latitude = arg0.getLatitude();
		longitude = arg0.getLongitude();
		mSpeed = arg0.getSpeed();
		mBearing = arg0.getBearing();

		mPreviousTimestamp = mTimestamp;
		mTimestamp = arg0.getTime(); // UTC
		mTimestampLocal = System.currentTimeMillis();
		mTimestampLocalGpsDifference = mTimestampLocal - mTimestamp;

		mAccuracy = arg0.getAccuracy();

		setHasGPsFix(true);

		//logger.info("!!!!!!! DeviceLocationListener.mHasGPsFix = " + mHasGPsFix);

		try{
			Editor editor = getPref().edit();
			editor.putFloat(GlobalConstants.EXTRA_CURRENT_BEARING, mBearing);

			editor.putLong(GlobalConstants.PREF_LASTGPS_SYNC, mTimestamp);

			editor.apply();
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}

		try{
			//=== record a breadcrumb at a rate of two per minute...
			if(mCount > GlobalConstants.BREADCRUMB_COUNTER){
				mCount = 0;

				String deliveryMode = getPref().getString(GlobalConstants.PREF_CUR_DELIVERY_MODE, GlobalConstants.DEF_DELIVERYSTATUS_NONE);

				//=== mService has shown up as null on random occasions
				int jobDetailId = 0;
				jobDetailId = CTApp.getJobDetailId();
				if(jobDetailId <= 0){
					jobDetailId = DBHelper.getInstance().getLastJobDetailIdUsed_Common();
				}
				//logger.info("jobDetailId = " + jobDetailId);

				Breadcrumb breadCrumb = new Breadcrumb();
				breadCrumb.setDeliveryMode(deliveryMode);
				breadCrumb.setDirection(mBearing);
				breadCrumb.setJobDetailId(jobDetailId);
				breadCrumb.setLat(latitude);
				breadCrumb.setLon(longitude);
				breadCrumb.setResolution(mAccuracy);
				breadCrumb.setSpeed(mSpeed);

				String address = CTApp.getCustomAppContext().getResources().getString(R.string.notAvailable);

//				try {
//					Location location = GPSUtils.convertToLocationFromGeoCode(mGeoCode);
//					//logger.debug("Location is" +  location.getLatitude()+"," +location.getLongitude());
//					Address addr = GPSUtils.getAddrByLocation(mContext, location);
//
//					if(addr != null){
//						address = addr.getAddressLine(0) + " " + addr.getLocality() + " " + addr.getAdminArea();
//					}
//				}
//				catch (Exception e){
//					logger.debug("EXCEPTION could not get address for breadcrumb " + e.getMessage());
//				}

				breadCrumb.setAddress(address);

				int tZOffSet = getPref().getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);
				long gmtTimestamp = mTimestamp + tZOffSet;
				breadCrumb.setTimestamp(gmtTimestamp);
				breadCrumb.setTimestampLocal(mTimestamp);
				breadCrumb.setUploadBatchId(GlobalConstants.DEF_UPLOADED_BATCH_ID);
				breadCrumb.setUploaded(GlobalConstants.DEF_UPLOADED_FALSE);

				ContentValues initialValues = breadCrumb.createIntialValues();
				DBHelper.getInstance().createRecord_Common(initialValues, DBHelper.DB_T_BREADCRUMBS, DBHelper.KEY_ID, false);
			}
			else{
				mCount++;
			}
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}
	}

	// @Override
	public void onProviderDisabled(String provider){
	}

	// @Override
	public void onProviderEnabled(String provider){
	}

	// @Override
	public void onStatusChanged(String provider, int status, Bundle extras){
	}

	public Context getMContext(){
		return mContext;
	}

	public void setMContext(Context mContext){
		logger.debug("ENTER");
		this.mContext = mContext;
//		startJobDetailIdService();
	}

	public double getLatitude(){
		return latitude;
	}

	public double getLongitude(){
		return longitude;
	}

	public SharedPreferences getPref(){
		return pref;
	}

	public void setPref(SharedPreferences pref){
		this.pref = pref;
	}

	public String getGeoCode(){
		return mGeoCode;
	}

	public void setGeoCode(String mGeoCode){
		this.mGeoCode = mGeoCode;
	}

	public float getSpeed(){
		return mSpeed;
	}

	public void setSpeed(float mSpeed){
		this.mSpeed = mSpeed;
	}

	public float getBearing(){
		return mBearing;
	}

	public void setBearing(float mBearing){
		this.mBearing = mBearing;
	}

	public long getTimestamp(){
		return mTimestamp;
	}

	public void setTimestamp(long mTimestamp){
		this.mTimestamp = mTimestamp;
	}

	public float getAccuracy(){
		return mAccuracy;
	}

	public void setAccuracy(float mAccuracy){
		this.mAccuracy = mAccuracy;
	}
}
