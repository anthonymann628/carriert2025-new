package com.agilegeodata.carriertrack.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.database.DBUpdate;
import com.agilegeodata.carriertrack.android.objects.ServerAccessActivity;
import com.agilegeodata.carriertrack.android.utils.FileUtils;
import com.agilegeodata.carriertrack.android.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DownloadService extends Service{
	private static final String TAG = DownloadService.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static boolean mIsManual;
	private static boolean mIsRunning;
	boolean mHaveError = false;
	String mErrorMessage;
	private DownloadTask mTask; // do work in this task....
	private Timer mTimer;
	private String mUserNameStr;
	private boolean mDatabaseUpdated;
	private HashMap<Integer, Integer> latestCounts = null;
	private String mVCode;

	@Override
	public IBinder onBind(final Intent intent){
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();

//	    logger.debug(">>>>onCreate() : STARTED");

		SharedPreferences prefs = this.getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
		mUserNameStr = prefs.getString(GlobalConstants.PREF_DEVICE_ID, CTApp.getCustomAppContext().getResources().getString(R.string.downloadServiceNoDeviceId));//null);

		mTimer = new Timer();

		mTask = new DownloadTask();
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId){
		int result = super.onStartCommand(intent, flags, startId);

//	    logger.debug(">>>>onStart() : STARTED");

		if(intent != null){
			try{
				if(intent.getExtras() != null){
					Bundle extras = intent.getExtras();
					mIsManual = extras != null && extras.getBoolean(GlobalConstants.EXTRA_MANUAL_DOWNLOAD);

//					logger.debug(">>>>onStart() : mIsManual = " + mIsManual);
				}
			}
			catch(Exception e){
				logger.debug("EXCEPTION", e);
			}
		}

		mHaveError = false;

//        logger.debug(">>>>onStart() : Entry Download SERVICE: is Manual? " + mIsManual);

		boolean alreadyScheduled = false;
		try{
			if(!mIsManual && !mIsRunning){
//				logger.debug(">>>>onStart() : download task STARTED not MANUAL");
				mTimer.schedule(mTask, GlobalConstants.SERVICE_DELAY_FIRST_DOWNLOADTASK_MILLIS, GlobalConstants.SERVICE_RUN_DOWNLOADTASK_EVERY_MILLIS);
			}
			else{
				if(!mIsRunning){
					mTimer.cancel();
					mTimer = new Timer();
					mTask = new DownloadTask();
					mTimer.schedule(mTask, 0, GlobalConstants.SERVICE_RUN_DOWNLOADTASK_EVERY_MILLIS);

//					logger.debug(">>>>onStart() : download task STARTED MANUAL");
				}
				else{
//                    logger.debug(">>>>onStart() : download task Update is currently in progress. No need to restart for manual");

					mIsManual = true;
				}

//                logger.debug(">>>>onStart() : Download SERVICE Scheduled");
			}
		}
		catch(IllegalStateException e){
			alreadyScheduled = true;

			logger.error("EXCEPTION", e);
		}

		if(alreadyScheduled){
//            logger.debug(">>>>onStart() : Rescheduling timer");

			mTimer.cancel();
			mTimer = new Timer();
			mTask = new DownloadTask();
			mTimer.schedule(mTask, GlobalConstants.SERVICE_DELAY_FIRST_DOWNLOADTASK_MILLIS,
							GlobalConstants.SERVICE_RUN_DOWNLOADTASK_EVERY_MILLIS);
		}

		return result;
//		logger.debug(">>>>onStart() : EXITING : mIsManual = " + mIsManual);
	}

	@Override
	public boolean onUnbind(final Intent intent){
		super.onUnbind(intent);

		return false;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}

	/**
	 * Implementation of the timer task.
	 */
	private class DownloadTask extends TimerTask{
		public void run(){
			//logger.info("STARTED");

			if(mIsRunning){
				logger.info("****DOWNLOAD SERVICE RUNNING do nothing****");
			}
			else{
				ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();

				if(!connectionState.isConnected){
					logger.info("****DOWNLOAD SERVICE " + connectionState.descriptiveText + "****");
				}
				else{
					logger.info("****DOWNLOAD SERVICE STARTING****");

					try{
						PackageInfo manager = getPackageManager().getPackageInfo(getPackageName(), 0);
						mVCode = manager.versionName;

//						logger.info(">>>>DownloadTask.run() : mVCode =" + mVCode);
					}
					catch(NameNotFoundException e){
						logger.debug("EXCEPTION PackageInfo : ", e);
					}

					mErrorMessage = GlobalConstants.DEF_ERROR_MESSAGE;

//						logger.info(">>>>DownloadTask.run() : Download Service is running: mIsManual? " + mIsManual);

					mIsRunning = true;

					SharedPreferences prefs = getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
					String mSaveDir = prefs.getString(FileUtils.getAppDirectoryForSavedFiles(), "");
					logger.info("mSaveDir =" + mSaveDir);

					boolean isDST = prefs.getBoolean(GlobalConstants.PREF_IS_DST, false);
					String isDSTBoolean = "0";

					if(isDST){
						isDSTBoolean = "1";
					}

					int count = DBHelper.getInstance().fetchCountFromTable_Common(DBHelper.DB_T_ROUTELIST);
//					logger.info("currently have " + count + " routes in DB");

					if(mIsManual){
						logger.info("starting MANUAL");
					}
					else if(count < 1){   // if for some reason there is nothing in the route table, get it all....
						logger.info("No routes have been loaded yet, running full manual build");

						mIsManual = true;
					}

					Calendar calLoc = Calendar.getInstance(Locale.getDefault());

					try{
						doData(mSaveDir);
					}
					catch(Exception e){
						mHaveError = true;

						logger.error("Exception mSaveDir : ", e);
					}

					ArrayList<String> deletedRoutes = cleanData(mSaveDir);

					HashMap<String, String> map = new HashMap<String, String>();
					map.put(GlobalConstants.URLPARAM_DEVICEID, mUserNameStr);
					map.put(GlobalConstants.URLPARAM_ANDROIDVERSION, android.os.Build.VERSION.RELEASE);
					//PREF_LOCAL_TIME_ZONE_OFFSET
					map.put(GlobalConstants.URLPARAM_DEVICETYPE, (android.os.Build.BRAND + android.os.Build.MODEL)); //ADDED 1/27 //working here
					map.put(GlobalConstants.URLPARAM_LOCALTIMESTAMP, new SimpleDateFormat("HH:mm", Locale.US).format(calLoc.getTime())); //ADDED 3/11
					map.put(GlobalConstants.URLPARAM_ISDST, isDSTBoolean); //ADDED 3/11
					map.put(GlobalConstants.URLPARAM_CTVERSION, mVCode);

					//=== deleted jobs should be returned with a count of 0 and deleted flag =1
					//=== jobdetailid*^*numRecords*^*deleted
					//=== 102902*^*584*^*0

					StringBuffer buf = new StringBuffer();
					buf.append(DBHelper.KEY_JOBDETAILID + GlobalConstants.UPLOAD_FIELD_DELIMINATOR +
							   GlobalConstants.DCONF_NUMRECORDS + GlobalConstants.UPLOAD_FIELD_DELIMINATOR +
							   DBHelper.KEY_DELETED);
					buf.append(System.getProperty("line.separator"));

					for(int i = 0; i < deletedRoutes.size(); i++){
						buf.append(deletedRoutes.get(i) + GlobalConstants.UPLOAD_FIELD_DELIMINATOR +
								   GlobalConstants.DCONF_NUM_DEL_CNT + GlobalConstants.UPLOAD_FIELD_DELIMINATOR +
								   GlobalConstants.DEF_DELETED);
						buf.append(System.getProperty("line.separator"));
					}

					logger.info("database is updated = " + mDatabaseUpdated);
					if(mDatabaseUpdated){
						Set<Integer> keys = latestCounts.keySet();

						Iterator<Integer> it = keys.iterator();
						while(it.hasNext()){
							Integer key = it.next();

							buf.append(key + GlobalConstants.UPLOAD_FIELD_DELIMINATOR +
									   latestCounts.get(key) + GlobalConstants.UPLOAD_FIELD_DELIMINATOR +
									   GlobalConstants.DEF_NOT_DELETED);
							buf.append(System.getProperty("line.separator"));
						}

						map.put(GlobalConstants.URLPARAM_DATA, buf.toString());

						try{
							sendConfData(map);
						}
						catch(Exception e){
							mHaveError = true;

							logger.error("Exception sendConfData() : ", e);
						}
					}
					else{
						logger.debug("database NOT updated");
					}

					if(!mHaveError){
						Editor ed = prefs.edit();
						Calendar cal = Calendar.getInstance();
						ed.putLong(GlobalConstants.PREF_LASTTIME_DOWNLOAD_SYNC, cal.getTimeInMillis());
						ed.commit();
					}

					mIsManual = false;
					mIsRunning = false;
				}    //ELSE if (connectivityStatus.isConnected())
			}    //ELSE if (!mIsRunning)

			ServerAccessActivity.updateServerTimestamp(getApplicationContext(), "\u25BC");

			//=== BROADCAST TO APP
			Intent intent = new Intent(GlobalConstants.SERVICE_DOWNLOAD);
			intent.setPackage("com.ans.ctt.mobile");
			intent.putExtra(GlobalConstants.EXTRA_DOWNLOAD_STATUS, mHaveError);
			intent.putExtra(GlobalConstants.EXTRA_ERROR_MESSAGE, mErrorMessage);

			logger.info("****DOWNLOAD SERVICE ENDED****");
			logger.debug("RESTART DOWNLOAD SERVICE CYCLE");

			intent.setPackage(getPackageName());//"com.agilegeodata.carriertrack");
			sendBroadcast(intent);
		}

		private ArrayList<String> cleanData(String mSaveDir){
			/**************  THIS IS WHERE ONE OF THE MAJOR ERRORS IS COMING FROM, TAGGED TO FIX   *******************/
			ArrayList<String> deletedRoutesJobDetailsList = DBHelper.getInstance().fetchAllDeletedRoutesJobDetails_Common();
			/*********************************************************************************************************/

			for(int i = 0; i < deletedRoutesJobDetailsList.size(); i++){
				//*********************** DELETE FROM PRODUCTS TABLE ***********************************

				DBHelper.getInstance().cleanAddressDetailsForUploadedOrNullDeliveryRecords_Common(deletedRoutesJobDetailsList.get(i));
				int removedAddressDetailListRecs = DBHelper.getInstance().fetchCountByQuery_Common("select count(*) from addressdetaillist where jobdetailid='" + deletedRoutesJobDetailsList.get(i) + "' ");

				DBHelper.getInstance().cleanAddressDetailsProductsForUploadedRecords_Common(deletedRoutesJobDetailsList.get(i));
				int removedAddressDetailsProductRecords = DBHelper.getInstance().fetchCountByQuery_Common("select count(*) from routelistactivity where jobdetailid='" + deletedRoutesJobDetailsList.get(i) + "' ");

				DBHelper.getInstance().cleanSignaturesForUploadedRecords_Common(deletedRoutesJobDetailsList.get(i));
				int removedSignatureRecords = DBHelper.getInstance().fetchCountByQuery_Common("select count(*) from signatures where jobdetailid='" + deletedRoutesJobDetailsList.get(i) + "' ");

				DBHelper.getInstance().cleanUploadedRouteListActivityRecords_Common(deletedRoutesJobDetailsList.get(i));
				int removedRouteListActivityRecords = DBHelper.getInstance().fetchCountByQuery_Common("select count(*) from routelistactivity where jobdetailid='" + deletedRoutesJobDetailsList.get(i) + "' ");

				DBHelper.getInstance().cleanBreadCrumbRecords_Common(deletedRoutesJobDetailsList.get(i));
				int removedBreadCrumbRecords = DBHelper.getInstance().fetchCountByQuery_Common("select count(*) from breadcrumbs where jobdetailid='" + deletedRoutesJobDetailsList.get(i) + "' ");

				DBHelper.getInstance().cleanWorkActivityForUploadedRecords_Common(deletedRoutesJobDetailsList.get(i));
				int removedWorkAactivityRecords = DBHelper.getInstance().fetchCountByQuery_Common("select count(*) from workactivity where jobdetailid='" + deletedRoutesJobDetailsList.get(i) + "' ");

				ArrayList<String> pList = DBHelper.getInstance().cleanPhotoRecords_Common(deletedRoutesJobDetailsList.get(i));
				int removedPhotoRecords = DBHelper.getInstance().fetchCountByQuery_Common("select count(*) from photos where jobdetailid='" + deletedRoutesJobDetailsList.get(i) + "' ");
				for(int x = 0; x < pList.size(); x++){
					try{
						FileUtils.deleteFile(pList.get(x));
					}
					catch(Exception e){
						logger.debug("Exception", e);
					}
				}

				//===UPLOADLOG TABLE HAS NO UPLOADED COLUMN
				//===LOGINS TABLE HAS UPLOADED COLUMN BUT NO JOBDETAIL COLUMN

				DBHelper.getInstance().cleanLoginRecords_Common();
				int removedloginRecords = DBHelper.getInstance().fetchCountByQuery_Common("select count(*) from logins where uploaded=1 ");

				//=== If RemainingRecordCount = 0 AND RemainingRouteActivityCount = 0 then:
				//=== delete from routelist where jobdetailid='deletedJobDetailId'
				//=== if all child records have been cleared, then we can physically delete the header record.
				if((removedAddressDetailListRecs == 0) &&
				   (removedRouteListActivityRecords == 0) &&
				   (removedAddressDetailsProductRecords == 0) &&
				   (removedBreadCrumbRecords == 0) &&
				   (removedPhotoRecords == 0) &&
				   (removedSignatureRecords == 0) &&
				   (removedWorkAactivityRecords == 0) &&
				   (removedloginRecords == 0)){
					//=== Delete from StreetSummaryList where JobDetailId = RouteList.JobDetailId
					DBHelper.getInstance().cleanStreetSummaryRecords_Common(deletedRoutesJobDetailsList.get(i));
					DBHelper.getInstance().cleanRouteListRecords_Common(deletedRoutesJobDetailsList.get(i));
				}
			}  // end of for

			return deletedRoutesJobDetailsList;
		}

		private void doData(String saveDir){
			//logger.debug("STARTED");
			//logger.debug("saveDir = " + saveDir);

			SharedPreferences prefs = getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
			Calendar calLoc = Calendar.getInstance(Locale.getDefault());

			boolean isDST = prefs.getBoolean(GlobalConstants.PREF_IS_DST, false);

			String isDSTBoolean = "0";
			if(isDST){
				isDSTBoolean = "1";
			}

			HashMap<String, String> map = new HashMap<String, String>();
			//logger.debug("MAP CTVERSION = " + mVCode);
			map.put(GlobalConstants.URLPARAM_CTVERSION, mVCode);
			//logger.debug("MAP DEVICEID = " + mUserNameStr);
			map.put(GlobalConstants.URLPARAM_DEVICEID, mUserNameStr);

			//logger.debug("MAP ANDROIDVERSION = " + android.os.Build.VERSION.RELEASE);
			map.put(GlobalConstants.URLPARAM_ANDROIDVERSION, android.os.Build.VERSION.RELEASE);

			String deviceModel = (android.os.Build.BRAND + android.os.Build.MODEL);
			if(deviceModel != null){
				//logger.debug("MAP DEVICETYPE = " + android.os.Build.BRAND + android.os.Build.MODEL);
				map.put(GlobalConstants.URLPARAM_DEVICETYPE, (android.os.Build.BRAND + android.os.Build.MODEL));
			}

			//logger.debug("MAP LOCALTIMESTAMP = " + new SimpleDateFormat("HH:mm", Locale.US).format(calLoc.getTime()));
			map.put(GlobalConstants.URLPARAM_LOCALTIMESTAMP, new SimpleDateFormat("HH:mm", Locale.US).format(calLoc.getTime())); //ADDED 3/11
			//logger.debug("MAP ISDST = " + isDSTBoolean);
			map.put(GlobalConstants.URLPARAM_ISDST, isDSTBoolean); //ADDED 3/11

			if(mIsManual){
				//logger.debug("MAP FORCERESET = " + GlobalConstants.URLPARAM_FORCERESET_TRUE);
				map.put(GlobalConstants.URLPARAM_FORCERESET, GlobalConstants.URLPARAM_FORCERESET_TRUE);
			}

			try{
				sendData(map, saveDir, true);
				mHaveError = false;
				mErrorMessage = "-";
			}
			catch(ClientProtocolException cpe){
				logger.debug("EXCEPTION", cpe);

				mHaveError = true;
				mErrorMessage = getResources().getString(R.string.downloadServiceErrorCodeNoConnectivity);
			}
			catch(ZipException ze){
				logger.error("EXCEPTION", ze);

				try{
					sendData(map, saveDir, false);
				}
				catch(ClientProtocolException cpe2){
					logger.error("EXCEPTION", cpe2);

					mHaveError = true;
					mErrorMessage = getResources().getString(R.string.downloadServiceErrorCodeNoConnectivity);
				}
				catch(IOException ioe2){
					logger.error("EXCEPTION", ioe2);

					mHaveError = true;
					mErrorMessage = getResources().getString(R.string.downloadServiceUnableToWrite);
				}
			}
			catch(IOException ioe){
				logger.debug("EXCEPTION", ioe);

				mHaveError = true;
				mErrorMessage = getResources().getString(R.string.downloadServiceUnableToWrite);
			}
		}

		/*
		 * Requires:
		 *
		 * deviceId: device id from properties, should be the same as login
		 */
		private void sendConfData(final HashMap<String, String> map) throws IOException{
			//logger.debug("STARTED");

			try{
				HttpClient httpclient = new DefaultHttpClient();

				//    logger.debug("Url: " + GlobalConstants.URL_DOWNLOADCONFIRM_DATA);

				HttpPost httppost = new HttpPost(GlobalConstants.URL_DOWNLOADCONFIRM_DATA);
				httppost.setHeader("Content-Transfer-Encoding", "8bit");

				httpclient.getParams().setParameter("http.socket.timeout",
													Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
				httppost.getParams().setParameter("http.socket.timeout",
												  Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));

				List<NameValuePair> nvps = Utils.buildURLParamsPost(map);

				httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

				HttpResponse res = httpclient.execute(httppost);
				InputStream stream = res.getEntity().getContent();

				try{
					stream.close();
				}
				catch(Exception e){
					logger.error("EXCEPTION", e);
				}
			}
			catch(Exception e){
				logger.error("EXCEPTION", e);
			}
		}

		/*
		 * Requires:
		 *
		 * deviceId: device id from properties, should be the same as login
		 */
		private void sendData(final HashMap<String, String> map, final String saveDir, boolean isZip)
				throws IOException{
			logger.debug("$$$$$$$$$ sendData() STARTED");

			logger.debug("map values = " + map.toString());

			mDatabaseUpdated = false;

			try{
				//=== REPLACED WITH OKHTTP========
				OkHttpClient okHttpClient = new OkHttpClient();
//				logger.debug("CREATED OKHTTPCLIENT");

				if(isZip){
					logger.debug("IS ZIP : using Url: " + GlobalConstants.URL_DOWNLOAD_ZIP_NEW);

					HttpUrl.Builder urlBuilder = HttpUrl.parse(GlobalConstants.URL_DOWNLOAD_ZIP_NEW).newBuilder();

					String url = urlBuilder.build().toString();
					logger.debug("OKHttp url = " + url);

					/*
					forcereset=1,
					ctVersion=1.934,
					deviceId=63bd05e950ce4aad,
					localTime=19:58,
					isDST=1,
					androidVersion=7.0,
					deviceType=VerizonSM-G920V
					*/

					MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

					for(Object aMappedDataEntry : map.entrySet()){
						Map.Entry mapEntry = (Map.Entry) aMappedDataEntry;
						String keyValue = (String) mapEntry.getKey();
						String value = (String) mapEntry.getValue();
						requestBodyBuilder.addFormDataPart(keyValue, value);
					}
					MultipartBody requestBody = requestBodyBuilder.build();

					Request request = new Request.Builder()
							.url(url)
							.header("Content-Transfer-Encoding", "8bit")
							.post(requestBody) //postEntityString))
							.build();

					int httpReturnCode = 200;
					InputStream is = null;
					String fileName = null;
					long zipFileSize = 25;
					File zipFile = null;

					Response response = okHttpClient.newCall(request).execute();
					//logger.debug("OKHttp response = " + response);

					httpReturnCode = response.code();
					logger.debug("OKHttp httpReturnCode = " + httpReturnCode);

					if(!response.isSuccessful()){
						logger.debug("OKHttp response NOT successful = " + response);
					}

					long datetime = Calendar.getInstance().getTimeInMillis();
					fileName = datetime + "_" + GlobalConstants.DBUPDATE_ZIPFILENAME;
					logger.debug("OKHttp zipFile path = " + saveDir + fileName);

//================TEST ONLY REMOVE FOR PRODUCTION ========================
					logger.error("DBHelper.getUseTestData_Common() = " + DBHelper.getInstance().getUseTestData_Common());
					if(DBHelper.getInstance().getUseTestData_Common()){//useTestRouteInput){
						try{
							is = CTApp.appContext.getAssets().open("test_dbfile.zip");
						}
						catch(final Throwable tx){
							logger.error("TEST ROUTE INPUT FILE FAILED");
						}
					}
					else{
//================TEST ONLY REMOVE FOR PRODUCTION ========================
						is = response.body().source().inputStream();
//================TEST ONLY REMOVE FOR PRODUCTION ========================
					}
//================TEST ONLY REMOVE FOR PRODUCTION ========================

					FileUtils.copyFile(is, saveDir, fileName);

					zipFile = new File(saveDir + fileName);

					if(!zipFile.exists()){
						logger.debug("OKHttp zipFile path NOT EXISTING");
					}

					zipFileSize = zipFile.length();
					logger.debug("OKHttp zipFileSize = " + zipFileSize);

					if(httpReturnCode == HttpURLConnection.HTTP_OK){
						//=== COULD GET TEXT RESPONSE OF ^*^0^*^ INDICATING NO ROUTES AVAILABLE
						//=== SO CHECK FOR LENGTH GREATER THAN 10 TO BE SURE
						if(zipFileSize > 21){    //===ZIP FILE WITH NO ENTRIES IS 22 BYTES IN SIZE, TKV, 20JUN18
							logger.debug("OKHttp zipFileSize >= 22 (" + zipFileSize + ") SO CALLING executeDatabaseUpdatesZipNew()");
							DBUpdate db = new DBUpdate();

							InputStream zipStream = new FileInputStream(zipFile);
							logger.debug("OKHttp zipStream " + (zipStream == null ? "is NULL" : " is " + zipStream.available() + " bytes in size"));
							latestCounts = db.executeDatabaseUpdatesZipNew(zipStream, saveDir + fileName);  ///HERE IS AN ISSUE from original code
							logger.debug("OKHttp latestCounts = " + latestCounts);

							if(latestCounts != null){
								mDatabaseUpdated = true;
							}
							logger.debug("OKHttp mDatabaseUpdated = " + mDatabaseUpdated);

							try{
								is.close();
							}
							catch(Exception e){
								logger.debug("EXCEPTION", e);
							}
						}
						else{
							//=== MAYBE LET USER KNOW
							logger.debug("OKHttp okHttpContentLength < 22 SO IGNORE RESULTS");
						}
					}
					else{
						//=== MAYBE LET USER KNOW
						logger.debug("OKHttp httpReturnCode NOT 200 SO IGNORE RESULTS");
					}
				}
				else{
					//=== REPLACED WITH OKHTTP========
					logger.debug("IS NOT ZIP : Url: " + GlobalConstants.URL_DOWNLOAD_DATA);

					HttpUrl.Builder urlBuilder = HttpUrl.parse(GlobalConstants.URL_DOWNLOAD_DATA).newBuilder();

					Set<Map.Entry<String, String>> paramsSet = map.entrySet();
					Iterator<Map.Entry<String, String>> iterate = paramsSet.iterator();

					/*
					forcereset=1,
					ctVersion=1.934,
					deviceId=63bd05e950ce4aad,
					localTime=19:58,
					isDST=1,
					androidVersion=7.0,
					deviceType=VerizonSM-G920V
					 */

					String postEntityString = "";
					while(iterate.hasNext()){
						Map.Entry<String, String> entry = iterate.next();

						postEntityString += (entry.getKey() + "=" + entry.getValue() + "&");
					}
					postEntityString = postEntityString.substring(0, postEntityString.length() - 1);
//					logger.debug("OKHttp post body = " + postEntityString);

					String url = urlBuilder.build().toString();
//					logger.debug("OKHttp url = " + url);

					final MediaType TEXT = MediaType.parse("application/text; charset=utf-8");

					RequestBody body = RequestBody.create(TEXT, postEntityString);

					Request request = new Request.Builder()
							.header("Content-Transfer-Encoding", "8bit")
							.post(body)
							.url(url)
							.build();

					Response response = okHttpClient.newCall(request).execute();
					String serverResponse = response.body().string();
//					logger.debug("OKHttp serverResponse = " + serverResponse);

					int httpReturnCode = response.code();
//					logger.debug("OKHttp httpReturnCode = " + httpReturnCode);

					long okHttpContentLength = response.body().contentLength();
//					logger.debug("OKHttp response length is " + okHttpContentLength);

					if(httpReturnCode == HttpURLConnection.HTTP_OK){
						//latestCounts = DBHelper.executeDatabaseUpdatesZip(stream, saveDir);
//						logger.debug("OKHttp httpReturnCode = 200");

						if(okHttpContentLength > 10){
//							logger.debug("OKHttp okHttpContentLength > 10 SO CALLING executeDatabaseUpdatesZip()");
							DBUpdate db = new DBUpdate();
							InputStream is = new ByteArrayInputStream(serverResponse.getBytes());
							latestCounts = db.executeDatabaseUpdatesZip(is, saveDir);  ///HERE IS AN ISSUE from original code

							if(latestCounts != null){
								mDatabaseUpdated = true;
							}

							try{
								is.close();
							}
							catch(Exception e){
								logger.debug("EXCEPTION", e);
							}
						}
						else{
							//=== MAYBE LET USER KNOW
							logger.debug("OKHttp okHttpContentLength <= 10 SO IGNORE RESULTS");
						}
					}
					else{
						//=== MAYBE LET USER KNOW
						logger.debug("OKHttp httpReturnCode NOT 200 SO IGNORE RESULTS");
					}
				}
			}
			catch(Exception e){
				logger.debug("EXCEPTION", e);

				e.printStackTrace();
			}
		}
	}
}