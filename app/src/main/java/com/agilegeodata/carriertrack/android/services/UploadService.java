package com.agilegeodata.carriertrack.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.objects.ServerAccessActivity;
import com.agilegeodata.carriertrack.android.objects.UploadLog;
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Upload Service Upload service will run every
 * GlobalConstants.SERVICE_RUN_UPLOADTASK_EVERY_MILLIS (60 seconds). If
 * there is a wifi connection and the service has not run in the past 10
 * minutes, then the service will try to upload. Otherwise it will wait
 * <p>
 * It will only try to upload if connectivity is active
 */
public class UploadService extends Service{
	public static final String TAG = UploadService.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static UploadTask mUploadTask; // do work in this task....
	boolean mHasError = false;
	String mErrorMessage;
	private Timer mTimer;
	private String mUserNameStr;
	private boolean mIsManualUpload;
	private boolean mIsRunning;
	private String mVCode;

	private UploadLog createUploadLogConfirmed(long id, int numRecordsConf,
											   int numFilesConf, int numNewAddressesConf){
		UploadLog log = new UploadLog();
		log.setId(id);
		log.setNumFilesConf(numFilesConf);
		log.setNumNewAddressesConf(numNewAddressesConf);
		log.setNumRecordsConf(numRecordsConf);
		log.setStatus(GlobalConstants.UPLOAD_STATUS_CONFIRMED);

		return log;
	}

	@Override
	public IBinder onBind(final Intent intent){
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();

		SharedPreferences prefs = this.getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
		mUserNameStr = prefs.getString(GlobalConstants.PREF_DEVICE_ID, null);

		mTimer = new Timer();

		mUploadTask = new UploadTask();
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId){
		int result = super.onStartCommand(intent, flags, startId);
		logger.debug("UploadService : onStartCommand().super result = " + result);

		if(intent != null){
			try{
				if(intent.getExtras() != null){
					Bundle extras = intent.getExtras();
					mIsManualUpload = extras != null && extras
							.getBoolean(GlobalConstants.EXTRA_MANUAL_UPLOAD);
				}
			}
			catch(Exception e){
				logger.error("EXCEPTION : " + e.getMessage());
			}
		}

		try{
			if(!mIsManualUpload && !mIsRunning){
				//=== WAIT 2 MINUTES, THEN DO EVERY 5 MINUTES
				mTimer.schedule(mUploadTask,
								GlobalConstants.SERVICE_DELAY_FIRST_UPLOADTASK_MILLIS,
								GlobalConstants.SERVICE_RUN_UPLOADTASK_EVERY_MILLIS);
			}
			else{
				if(!mIsRunning){
					mTimer.cancel();
					mTimer = new Timer();
					mUploadTask = new UploadTask();
					//=== DONT WAIT, DO EVERY 5 MINUTES
					mTimer.schedule(mUploadTask, 0, GlobalConstants.SERVICE_RUN_UPLOADTASK_EVERY_MILLIS);
				}
				else{
					mIsManualUpload = true;
				}
			}
		}
		catch(Exception e){
			logger.error("Upload Service StartActivity Error", e);
		}

		return result;
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

	private class UploadTask extends TimerTask{
		public void run(){
			//logger.info("STARTED");

			if(mIsRunning){
				logger.info("****UPLOAD SERVICE CURRENTLY RUNNING skipping for now****");
			}
			else{
				logger.info("****UPLOAD SERVICE NOT RUNNING****");

				ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();

				if(!connectionState.isConnected){
					logger.info("****UPLOAD SERVICE NOT CONNECTED skipping for now " + connectionState.descriptiveText + " ****");
				}
				else{
					Location geocode = CTApp.getLocation();

					if(geocode == null){
						logger.info("****UPLOAD SERVICE NO GEOCODE skipping for now****");
					}
					else{
						try{
							PackageInfo manager = getPackageManager().getPackageInfo(getPackageName(), 0);
							mVCode = manager.versionName;
						}
						catch(NameNotFoundException e){
							// TODO Auto-generated catch block
							logger.info("EXCEPTION PackageInfo : ", e);
							e.printStackTrace();

							logger.info("****UPLOAD SERVICE NO APP VERSION CODE skipping for now****");
							return;
						}

						//=== if this is a manual upload or wifi status changed, then make
						//=== sure that the service will try to upload by reseting the manual upload

						mIsRunning = true;
						mErrorMessage = GlobalConstants.DEF_ERROR_MESSAGE;

						String data = "";

						//*************************************************************************
						//===UPLOAD THE LOGINS
						//*************************************************************************
						logger.info("****UPLOAD processing LOGINS");
						data = DBHelper.getInstance().fetchAllLoginsForUpload_Common();
						data = data.trim();

						//=== if it is less than 5, then
						//=== there was nothing to be
						//=== done....
						if(data.length() > 0){
							try{
								logger.info("****UPLOAD processing LOGINS : DATA = " + data);
								doData(data, GlobalConstants.URLPARAM_FILETYPE_LOGINS, mIsManualUpload);
							}
							catch(Exception e){
								mHasError = true;
								logger.debug("Exception doData() Logins: ", e);
							}
						}
						else{
							logger.debug("NO LOGIN ACTIVITY TO UPLOAD ");
						}

						//*************************************************************************
						//===UPLOAD THE ROUTE ACTIVITY
						//*************************************************************************
						logger.info("****UPLOAD processing ROUTE LIST ACTIVITY");
						data = DBHelper.getInstance().fetchAllRouteListActivityForUpload_Common();
						data = data.trim();

						//=== if it is less than 5, then
						//=== there was nothing to be
						//=== done....
						if(data.length() > 0){
							try{
								logger.info("****UPLOAD processing ROUTE LIST ACTIVITY : DATA = " + data);
								doData(data, GlobalConstants.URLPARAM_FILETYPE_ROUTELISTACTIVITY, mIsManualUpload);
							}
							catch(Exception e){
								mHasError = true;
								logger.debug("Exception doData() RouteListActivity: ", e);
							}
						}
						else{
							logger.debug("NO ROUTE ACTIVITY TO UPLOAD ");
						}

						//*************************************************************************
						//===UPLOAD THE ROUTE WORK ACTIVITIES
						//*************************************************************************
						logger.info("****UPLOAD processing WORK ACTIVITY");
						data = DBHelper.getInstance().fetchRouteWorkActivitiesForUpload();
						data = data.trim();

						//=== if it is less than 5, then
						//=== there was nothing to be
						//=== done....
						if(data.length() > 0){
							try{
								logger.info("****UPLOAD processing WORK ACTIVITY : DATA = " + data);
								doData(data, GlobalConstants.URLPARAM_FILETYPE_ROUTEWORKACTIVITY, mIsManualUpload);
							}
							catch(Exception e){
								mHasError = true;
								logger.debug("Exception doData() RouteWorkActivity: ", e);
							}
						}
						else{
							logger.debug("NO ROUTE WORK ACTIVITY TO UPLOAD ");
						}

						//*************************************************************************
						//===UPLOAD THE ADDRESS DETAILS
						//*************************************************************************
						logger.info("****UPLOAD processing ADDRESS DETAILS");

						data = DBHelper.getInstance().fetchAllAddressDetailsForUpload_Common();

						data = data.trim();

						//=== if it is less than 5, then
						//=== there was nothing to be
						//=== done....
						if(data.length() > 0){
							try{
								logger.info("****UPLOAD processing ADDRESS DETAILS : " + data);
								doData(data,
									   GlobalConstants.URLPARAM_FILETYPE_ADDRESSDETAILIST,
									   mIsManualUpload);
							}
							catch(Exception e){
								mHasError = true;
								logger.debug("Exception doData() AddressDetails: ", e);
							}
						}
						else{
							logger.debug("NO ADDRESS DETAILS TO UPLOAD ");
						}

						//*************************************************************************
						//===UPLOAD THE PRODUCT TABLE (BARCODES SCANNED)
						//*************************************************************************
						logger.info("****UPLOAD processing PRODUCT SCANS");
						data = DBHelper.getInstance().fetchAllProductScansForUpload_Common();
						data = data.trim();
						logger.debug("product scan data=\n" + data);

						//=== if it is less than 5, then
						//=== there was nothing to be
						//=== done....
						if(data.length() > 0){
							try{
								logger.info("****UPLOAD processing PRODUCT SCANS " + data);
								doData(data,
									   GlobalConstants.URLPARAM_FILETYPE_SCANS,
									   mIsManualUpload);
							}
							catch(Exception e){
								mHasError = true;
								logger.debug("Exception doData() product scans: ", e);
							}
						}
						else{
							logger.debug("NO product scan TO UPLOAD ");
						}

						//*************************************************************************
						//===UPLOAD THE PHOTOS
						//*************************************************************************
						logger.info("****UPLOAD processing PHOTOS");
						data = DBHelper.getInstance().fetchAllPhotosForUpload_Common();
						data = data.trim();
						logger.debug("??????? UPLOAD photos : DATA SIZE = " + data.length());
						logger.debug("??????? UPLOAD photos : data.endsWith(\"::::0\") = " + data.endsWith("::::0"));

						//=== if  it is less  than 5, then there was nothing to be  done....
						if(data.length() > 0 && !data.endsWith("::::0")){
							try{
								logger.info("****UPLOAD processing PHOTOS : " + data);
								doData(data, GlobalConstants.URLPARAM_FILETYPE_PHOTOS, mIsManualUpload);
							}
							catch(Exception e){
								mHasError = true;
								logger.debug("Exception doData() : ", e);
							}

							ArrayList<String> list = DBHelper.getInstance().fetchAllPhotosForSyncing_Common();
							try{
								doPhotoData(list);
							}
							catch(Exception e){
								mHasError = true;
								logger.debug("Exception doPhotoData() photos: ", e);
							}
						}
						else{
							logger.debug("NO PHOTOS TO UPLOAD ");
						}

						//*************************************************************************
						//===UPLOAD THE SIGNATURES
						//*************************************************************************
						logger.info("****UPLOAD processing SIGNATURES");
						data = DBHelper.getInstance().fetchAllSignaturesForUpload_Common();
						data = data.trim();

						//=== if  it is less  than 5, then there was nothing to be  done....
						if(data.length() > 0 && !data.endsWith("::::0")){
							try{
								logger.info("****UPLOAD processing SIGNATURES : " + data);
								doData(data, GlobalConstants.URLPARAM_FILETYPE_SIGNATURES, mIsManualUpload);
							}
							catch(Exception e){
								mHasError = true;
								logger.debug("Exception doData() : ", e);
							}

							ArrayList<String> list = DBHelper.getInstance().fetchAllSignaturesForSyncing_Common();
							try{
								doSignatureData(list);
							}
							catch(Exception e){
								mHasError = true;
								logger.debug("Exception doSignatureData() signatures: ", e);
							}
						}
						else{
							logger.debug("NO SIGNATURES TO UPLOAD ");
						}

						//*************************************************************************
						//===UPLOAD THE BREADCRUMBS
						//*************************************************************************
						try{
							logger.info("****UPLOAD processing BREADCRUMBS");
							data = DBHelper.getInstance().fetchAllBreadcrumbsForUpload_Common();

							//=== if it is less than 5, then
							//=== there was nothing to be
							//=== done....
							if(data.length() > 5){
								logger.info("****UPLOAD processing BREADCRUMBS : DATA = " + data);
								int loc = data.indexOf(GlobalConstants.UPLOAD_COUNT_DELIMINATOR);
								String count = data.substring(loc + 4);

								int numRecordsSent = Integer.valueOf(count);

								//=== Throttle back
								//=== breadcrumbs so they
								//=== get sent about every
								//=== 10 minutes.....
								if(numRecordsSent > 15){
									//=== FS#40 - Strange continuous
									//=== breadcrumb/uploadlog posts after good data
									//=== sync
									try{
										doData(data,
											   GlobalConstants.URLPARAM_FILETYPE_BREADCRUMB,
											   mIsManualUpload);
									}
									catch(Exception e){
										logger.debug("Exception doData() Breadcrumbs: ", e);
									}
								}
							}
							else{
								logger.debug("NO BREADCRUMBS TO UPLOAD ");
							}
						}
						catch(Exception e){
							mHasError = true;
							logger.debug("Exception: ", e);
						}

						//*************************************************************************
						//===UPLOAD THE UPLOAD LOGS
						//*************************************************************************
						logger.info("****UPLOAD processing UPLOAD LOGS");
						data = DBHelper.getInstance().fetchAllUploadLogsForUpload_Common();
						data = data.trim();

						//=== if it is less than 5, then
						//=== there was nothing to be
						//=== done....
						if(data.length() > 5){
							logger.info("****UPLOAD processing UPLOAD LOGS : DATA = " + data);
							int loc = data.indexOf(GlobalConstants.UPLOAD_COUNT_DELIMINATOR);
							String count = data.substring(loc + 4);

							int numRecordsSent = Integer.valueOf(count);

							if(numRecordsSent > 15){
								try{
									doData(data, GlobalConstants.URLPARAM_FILETYPE_UPLOADLOG, mIsManualUpload);
								}
								catch(Exception e){
									mHasError = true;
									logger.debug("Exception doData() UploadLogs: ", e);
								}
							}
						}
						else{
							logger.debug("NO UPLOAD LOGS TO UPLOAD ");
						}

						if(mIsManualUpload){
							Intent intent = new Intent(GlobalConstants.SERVICE_UPLOAD);
							intent.setPackage("com.ans.ctt.mobile");
							intent.putExtra(GlobalConstants.EXTRA_UPLOAD_STATUS, mHasError);
							intent.putExtra(GlobalConstants.EXTRA_ERROR_MESSAGE, mErrorMessage);
							intent.setPackage(getPackageName());//"com.agilegeodata.carriertrack");
							sendBroadcast(intent);
						}

						ServerAccessActivity.updateServerTimestamp(getApplicationContext(), "\u25B2");
					}    // if geocode

					mIsManualUpload = false;
					mIsRunning = false;

				}    //ELSE if (connectivityStatus.isConnected())
			}    //ELSE if (1mIsRunning)

			logger.info("****UPLOAD SERVICE ENDED****");
		}

		private void doSignatureData(ArrayList<String> data){
			logger.info("****UPLOADSERVICE doSignatureData() ENTER");
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(GlobalConstants.URLPARAM_CTVERSION, mVCode);
			map.put(GlobalConstants.URLPARAM_ANDROIDVERSION, android.os.Build.VERSION.RELEASE);
			map.put(GlobalConstants.URLPARAM_DEVICEID, mUserNameStr);
			map.put(GlobalConstants.URLPARAM_FILETYPE, GlobalConstants.URLPARAM_FILETYPE_SIGNATUREJPG);
			map.put(GlobalConstants.URLPARAM_ANDROIDVERSION, android.os.Build.VERSION.RELEASE);

			String uploadType = GlobalConstants.URLPARAM_UPLOAD_AUTO;

			logger.info("****UPLOAD SERVICE doSignatureData # of signatures is " + data.size());

			for(int i = 0; i < data.size(); i++){
				String fileName = data.get(i);

				UploadLog adLog = this.createUploadLogAttempt(1, 0, 0,
															  GlobalConstants.URLPARAM_FILETYPE_SIGNATUREJPG, uploadType);

				long val = DBHelper.getInstance().createRecord_Common(adLog.createContentValues(),
																	  DBHelper.DB_T_UPLOADLOG, DBHelper.KEY_ID, false);
				adLog.setId(val);

				try{
					logger.info("****UPLOAD SERVICE doSignatureData UPLOADING SIGNATURE " + fileName);
					sendSignatureData(map, adLog, fileName);
					mHasError = false;
					mErrorMessage = "-";
				}
				catch(ClientProtocolException cpe){
					mHasError = true;
					mErrorMessage = getResources().getString(R.string.downloadServiceErrorCodeNoConnectivity);
				}
				catch(IOException ioe){
					//	if (CTApp.DEBUG ) logger.debug("Error: " + ioe.getMessage());
					logger.debug("EXCEPTION : " + ioe.getMessage());
					mHasError = true;
					mErrorMessage = getResources().getString(R.string.downloadServiceUnableToWrite);
				}
			}
		}

		private void doPhotoData(ArrayList<String> data){
			logger.info("****UPLOAD SERVICE doPhotoData() ENTER");
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(GlobalConstants.URLPARAM_CTVERSION, mVCode);
			map.put(GlobalConstants.URLPARAM_ANDROIDVERSION, android.os.Build.VERSION.RELEASE);
			map.put(GlobalConstants.URLPARAM_DEVICEID, mUserNameStr);
			map.put(GlobalConstants.URLPARAM_FILETYPE, GlobalConstants.URLPARAM_FILETYPE_PHOTOJPG);
			map.put(GlobalConstants.URLPARAM_ANDROIDVERSION, android.os.Build.VERSION.RELEASE);

			String uploadType = GlobalConstants.URLPARAM_UPLOAD_AUTO;

			logger.info("****UPLOAD SERVICE doPhotoData # of photos is " + data.size());

			for(int i = 0; i < data.size(); i++){
				String fileName = data.get(i);

				UploadLog uploadLogItem = this.createUploadLogAttempt(1, 0, 0,
																	  GlobalConstants.URLPARAM_FILETYPE_PHOTOJPG, uploadType);

				long recordNumber = DBHelper.getInstance().createRecord_Common(uploadLogItem.createContentValues(),
																			   DBHelper.DB_T_UPLOADLOG, DBHelper.KEY_ID, false);
				uploadLogItem.setId(recordNumber);

				try{
					logger.info("****UPLOAD SERVICE doPhotoData UPLOADING PHOTO " + fileName);
					sendPhotoData(map, uploadLogItem, fileName);
					mHasError = false;
					mErrorMessage = "-";
				}
				catch(ClientProtocolException cpe){
					mHasError = true;
					mErrorMessage = getResources().getString(R.string.downloadServiceErrorCodeNoConnectivity);
				}
				catch(IOException ioe){
					//	if (CTApp.DEBUG ) logger.debug("Error: " + ioe.getMessage());
					logger.debug("EXCEPTION : " + ioe.getMessage());
					mHasError = true;
					mErrorMessage = getResources().getString(R.string.downloadServiceUnableToWrite);
				}
			}
		}

		private void sendSignatureData(final HashMap<String, String> map,
									   UploadLog uploadLogItem, String fileName) throws IOException{
			logger.info("****UPLOAD SERVICE sendSignatureData() ENTER for" + fileName);
			HttpURLConnection connection = null;
			DataOutputStream outputStream = null;

			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";

			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 2 * 1024 * 1024;

			try{
				//	if (CTApp.DEBUG ) logger.debug("File Name for Upload is: " + fileName);
				FileInputStream fileInputStream = new FileInputStream(new File(fileName));

				URL url = new URL(GlobalConstants.URL_UPLOAD_DATASIGNATURE);

				connection = (HttpURLConnection) url.openConnection();

				// Allow Inputs & Outputs
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);

				//=== Enable POST method
				connection.setRequestMethod("POST");

				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				List<NameValuePair> nvps = Utils.buildURLParamsPost(map);
				//String charset = "UTF-8";

				String query = "";
				for(int i = 0; i < nvps.size(); i++){
					query = query + nvps.get(i).getName() + "=" + nvps.get(i).getValue() + "&";
				}

				query = query.substring(0, query.length() - 1);

				outputStream = new DataOutputStream(connection.getOutputStream());
				outputStream.writeBytes(query + lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
										+ fileName + "\"" + lineEnd);
				outputStream.writeBytes(lineEnd);

				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				//=== Read file
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while(bytesRead > 0){
					outputStream.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				InputStream stream = connection.getInputStream();
				try{
					String success = Utils.inputStreamToString(stream);

					success = success.trim();
					if(success.endsWith("1")){
						logger.debug("SIGNATURE file uploaded " + fileName);
						uploadLogItem.setStatus("1");
						DBHelper.getInstance().loadUpdateVerifySignatureFiles_Common(fileName, uploadLogItem);
					}
					else{
						logger.debug("SIGNATURE file not uploaded " + fileName);
					}
				}
				catch(Exception e){
					logger.debug("Exception:", e);
				}

				stream.close();

				fileInputStream.close();
				outputStream.flush();
				outputStream.close();
			}
			catch(Exception ex){
				logger.debug("Exception:", ex);
			}
		}

		private void sendPhotoData(final HashMap<String, String> map,
								   UploadLog uploadLogItem, String fileName) throws IOException{
			logger.info("****UPLOAD SERVICE sendPhotoData() ENTER for" + fileName);
			HttpURLConnection connection = null;
			DataOutputStream outputStream = null;

			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";

			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 2 * 1024 * 1024;

			try{
				FileInputStream fileInputStream = new FileInputStream(new File(fileName));

				URL url = new URL(GlobalConstants.URL_UPLOAD_DATAPHOTO);

				connection = (HttpURLConnection) url.openConnection();

				//=== Allow Inputs & Outputs
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);

				//=== Enable POST method
				connection.setRequestMethod("POST");

				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				List<NameValuePair> nvps = Utils.buildURLParamsPost(map);
				//String charset = "UTF-8";

				String query = "";
				for(int i = 0; i < nvps.size(); i++){
					query = query + nvps.get(i).getName() + "=" + nvps.get(i).getValue() + "&";
				}

				query = query.substring(0, query.length() - 1);
				// query= URLEncoder.encode(query, charset);

				outputStream = new DataOutputStream(connection.getOutputStream());
				outputStream.writeBytes(query + lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
										+ fileName + "\"" + lineEnd);
				outputStream.writeBytes(lineEnd);

				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				//=== Read file
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while(bytesRead > 0){
					outputStream.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				InputStream stream = connection.getInputStream();
				try{
					String success = Utils.inputStreamToString(stream);

					success = success.trim();
					if(success.endsWith("1")){
						uploadLogItem.setStatus("1");
						logger.debug("PHOTO file successfully uploaded " + fileName);
						DBHelper.getInstance().loadUpdateVerifyPhotoFiles_Common(fileName, uploadLogItem);
					}
					else{
						logger.debug("PHOTO file not uploaded " + fileName);
					}
				}
				catch(Exception e){
					logger.debug("Exception:", e);
				}

				stream.close();

				fileInputStream.close();
				outputStream.flush();
				outputStream.close();
			}
			catch(Exception ex){
				logger.debug("Exception:", ex);
			}
		}

		private void doData(String data, String fileType, boolean isManual){
			logger.debug("******doData() ENTER");
			HashMap<String, String> map = new HashMap<String, String>();
			// map.put(GlobalConstants.URLPARAM_DATA, data);
			map.put(GlobalConstants.URLPARAM_CTVERSION, mVCode);
			map.put(GlobalConstants.URLPARAM_ANDROIDVERSION, android.os.Build.VERSION.RELEASE);
			//map.put(GlobalConstants.URLPARAM_DEVICETYPE, (android.os.Build.BRAND+android.os.Build.MODEL)); //ADDED 1/27
			map.put(GlobalConstants.URLPARAM_DEVICEID, mUserNameStr);

			int loc = data.indexOf(GlobalConstants.UPLOAD_COUNT_DELIMINATOR);
			String count = data.substring(loc + 4);
			logger.debug("******doData() record count to send = " + count);

			data = data.substring(0, loc - 1);

			int numRecordsToSend = Integer.valueOf(count);
			logger.debug("******doData() numRecordsToSend = " + numRecordsToSend);

			//=== For each datatype, only post data if
			//=== there is data to post.
			if(numRecordsToSend > 0){
				//logger.debug(data);

				map.put(GlobalConstants.URLPARAM_DATA, data);
				map.put(GlobalConstants.URLPARAM_FILETYPE, fileType);
				map.put("UPLOAD_RECORD_COUNT", numRecordsToSend + "");

				UploadLog uploadLog = this.createUploadLogAttempt(0, numRecordsToSend, 0, fileType, isManualUploadType(isManual));
				logger.debug("doData() : initial uploadLog = " + uploadLog);
				long recordId = DBHelper.getInstance().createRecord_Common(uploadLog.createContentValues(), DBHelper.DB_T_UPLOADLOG, DBHelper.KEY_ID, false);

				uploadLog.setId(recordId);

				try{
					logger.debug(">>>>>>>>> UPLOAD : DATA FOR " + fileType);
					mUploadTask.sendData(map, uploadLog, UploadService.this);
					mHasError = false;
					mErrorMessage = "-";
				}
				catch(ClientProtocolException cpe){
					mHasError = true;
					mErrorMessage = getResources().getString(R.string.downloadServiceErrorCodeNoConnectivity);
				}
				catch(IOException ioe){
					logger.debug("Error: " + ioe.getMessage());

					mHasError = true;
					mErrorMessage = getResources().getString(R.string.downloadServiceUnableToWrite);
				}
			}
			else{
				logger.debug("NO RECORDS TO UPLOAD FOR " + fileType);
			}
		}

		private String isManualUploadType(boolean isManual){
			if(isManual){
				return GlobalConstants.URLPARAM_UPLOAD_MANUAL;
			}
			else{
				return GlobalConstants.URLPARAM_UPLOAD_AUTO;
			}
		}

		private UploadLog createUploadLogAttempt(int numFilesSent, int numRecordsSent, int numNewAddressesSent,
												 String uploadDataType, String uploadType){
			UploadLog log = new UploadLog();

			SharedPreferences prefs = getSharedPreferences(
					GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
			int tZOffSet = prefs.getInt(
					GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);
			Calendar gmtCal = Calendar.getInstance();

			long gmtTimestamp = gmtCal.getTimeInMillis() + tZOffSet;

			log.setTimestamp(gmtTimestamp);

			String[] latlong = {CTApp.getLocation().getLatitude() + "", CTApp.getLocation().getLongitude() + ""};

//===CRASHLYTICS ERROR, CONNECTIVITY ISSUES AND GOOGLE ERRORS HERE
//			Location loc = GPSUtils.convertToLocationFromGeoCode(geocode);
//			Address address = null;
//			try{
//				address = GPSUtils.getAddrByGeoLoc(getApplicationContext(), loc.getLatitude(), loc.getLongitude());
//			}
//			catch(Exception e){
//				address = null;
//				logger.error("EXCEPTION HANDLED", e);
//			}
			String addr = CTApp.getCustomAppContext().getResources().getString(R.string.uploadServiceAddressNotAvailable);

//			if (address != null) {
//				addr = address.getAddressLine(0) + " "
//						+ address.getLocality() + " " + address.getAdminArea();
//			}

			log.setAddress(addr);

			try{
				log.setLatitude(new Double(latlong[0]));
				log.setLongitude(new Double(latlong[1]));
			}
			catch(Exception e){
				logger.debug("Exception" + e);
			}

			log.setNewAddressesToSendCount(numNewAddressesSent);
			log.setRecordsToSendCount(numRecordsSent);
			log.setFilesSentCount(numFilesSent);
			log.setStatus(GlobalConstants.UPLOAD_STATUS_CREATE);
			//=== timestamp should be GMT
			Calendar cal = Calendar.getInstance();

			log.setTimestampLocal(cal.getTimeInMillis());
			log.setUploadDataType(uploadDataType);
			log.setUploadType(uploadType);

			return log;
		}

		/*
		 * Requires: fileType: indicates breadcrumb, photo,UploadLog,Logins,
		 * RouteListActivity, AddressDetailList deviceId: device id from properties,
		 * should be the same as login data: record you are sending delimitated
		 * fieldName fieldValue pairs and value
		 * fieldName1*:*fieldValue1*^*fieldName2
		 * *:fieldValue2*^*fieldName3*:*fieldValue
		 */
		private void sendData(final HashMap<String, String> map, UploadLog log, UploadService uploadService)
				throws IOException{
			logger.debug("*******sendData() ENTER");

			try{
				HttpClient httpclient = new DefaultHttpClient();
				logger.debug("*******sendData() A");

				HttpPost httppost = new HttpPost(GlobalConstants.URL_UPLOAD_DATA);
				logger.debug("*******sendData() B");

				httppost.setHeader("Content-Transfer-Encoding", "8bit");
				logger.debug("*******sendData() C");

				httpclient.getParams().setParameter("http.socket.timeout",
													Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
				logger.debug("*******sendData() D");
				httppost.getParams().setParameter("http.socket.timeout",
												  Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
				logger.debug("*******sendData() E");

				List<NameValuePair> nameValuePairs = Utils.buildURLParamsPost(map);
				for(int i = 0; i < nameValuePairs.size(); i++){
					logger.debug("*******sendData() : nameValuePair = " + nameValuePairs.get(i).toString());
				}

//String fileType = map.get(GlobalConstants.URLPARAM_FILETYPE);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

//String recordsSent = map.get("UPLOAD_RECORD_COUNT");
//				logger.debug("*******sendData() recordsToUploadCount = " + recordsToUploadCount == null ? "NULL" : recordsToUploadCount);
//				int numRecordsToUpload = Integer.decode(recordsToUploadCount);
//				logger.debug("NUMBER OF RECORDS TO UPLOAD = " + numRecordsToUpload);
				logger.debug("sendData() : uploadLog.getNumRecordsSent() = " + log.getRecordsToSendCount());

				HttpResponse res = httpclient.execute(httppost);
				InputStream stream = res.getEntity().getContent();

				String confirmedRecordIds = Utils.inputStreamToString(stream);
//2023-05-26 11:40:30.120 19019-19060 <AGD>
// com.ans.ctt.mobile
// D  LOGCAT 26May23-11:40:30 prod 7031 DEBUG UploadService$UploadTask.sendData : 838 =>
// CONFIRMED RECORDS SENT IDS = pushdata.php:158:You have an error in your SQL syntax;
// check the manual that corresponds to your MariaDB server version for the right syntax to use
// near '= '',internalPhoneId = '5924',deliveryKey = '5924SCAN',batchId='81892614'' at line 1
// FOR BARCODE SCANS
				logger.debug("CONFIRMED RECORDS SENT IDS = " + confirmedRecordIds);

				//=== THIS WRITES THE UPLOADED VALUE TO "1"
				//=== always returns "0"
				int numRecordsConf = DBHelper.getInstance().loadUpdateVerifyRecords_Common(confirmedRecordIds, log.getUploadDataType());
				log.setNumRecordsConf(numRecordsConf);
				logger.debug("UPLOADED RECORD DELTA = " + (log.getRecordsToSendCount() - log.getNumRecordsConf()));

				if(log.getNumRecordsConf() > 0){
					log.setStatus(GlobalConstants.UPLOAD_STATUS_CONFIRMED);
//					UploadLog logC = uploadService.createUploadLogConfirmed(log.getId(), numRecordsConf, 0, 0);
					logger.debug("sendData() : confirmed uploadLog = " + log.toString());

					DBHelper.getInstance().createRecord_Common(log.createContentValues(), DBHelper.DB_T_UPLOADLOG, DBHelper.KEY_ID, true);
				}
				else{
					logger.debug("NO CONFIRMED RECORDS WRITTEN");
				}

				try{
					stream.close();
				}
				catch(Exception e){
					logger.debug("EXCEPTION sendData()1: " + e);
				}
			}
			catch(Exception e){
				logger.debug("EXCEPTION sendData()2: " + e);
			}
		}
	}
}