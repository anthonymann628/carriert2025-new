package com.agilegeodata.carriertrack.android.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.agilegeodata.carriertrack.android.BuildConfig;
import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.DeviceStatusContainer;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;
import com.agilegeodata.carriertrack.android.objects.UploadLog;
import com.agilegeodata.carriertrack.android.services.ConnectionStatus;
import com.agilegeodata.carriertrack.android.utils.DateUtil;
import com.agilegeodata.carriertrack.android.utils.FileUtils;
import com.agilegeodata.carriertrack.android.utils.Utils;
import com.agilegeodata.carriertrack.android.utils.ZipUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
 * Device status screen
 * Displays GPS Status, Radio status, Available memory, available sdspace, log viewer
 * and data reports
 */
public class DeviceStatusFragment extends Fragment{
	protected static final String TAG = "DeviceStatusFragment";
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static ProgressDialog cleanProgressDialog;
	private static boolean gpsRadioButtonIsChecked = false;
	TextView mTopNavLine1;
	TextView mTopNavLine2;
	AppCompatRadioButton mGpsStatusRadioButton;
	ToggleButton simulationButton = null;
	ToggleButton testDataButton = null;
	private ArrayList<UploadLog> mDataReports;
	private TableLayout mDataList;
	private View viewer;
	private long mTimeStamp;
	private final Handler gpsReceiverHandler = new Handler(){

		public void handleMessage(Message msg){
			updateTopNav();

			try{
				String dateLastSync = getResources().getString(R.string.notAvailable);

				if(mTimeStamp > 0){
					dateLastSync = DateUtil.calcDateFromTime(mTimeStamp, GlobalConstants.DEFAULT_DATETIME_FORMAT);
				}

//				logger.debug("Date last sync: " + dateLastSync);

				TextView gpsStatus = getActivity().findViewById(R.id.deviceStatusGPSStatusVal);
				gpsStatus.setText(dateLastSync);

			}
			catch(Exception e){
				logger.error("DeviceStatusFragment", e);
			}
		}
	};
	private final BroadcastReceiver receiverGPS = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
			Bundle b = intent.getExtras();
			mTimeStamp = b.getLong(GlobalConstants.PREF_LASTGPS_SYNC, 0);
			gpsReceiverHandler.sendEmptyMessage(0);
		}
	};
	private boolean mIsProcessing;
	private final BroadcastReceiver receiverUpload = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
			addDeviceStatusItems();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//logger.debug(TAG, "Entering  Fragment on Create");
		setRetainInstance(true);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			ContextCompat.registerReceiver(getContext(), receiverGPS, new IntentFilter(GlobalConstants.SERVICE_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED);
			ContextCompat.registerReceiver(getContext(), receiverUpload, new IntentFilter(GlobalConstants.SERVICE_UPLOAD), ContextCompat.RECEIVER_NOT_EXPORTED);
		}
		else{
			getContext().registerReceiver(receiverGPS, new IntentFilter(GlobalConstants.SERVICE_LOCATION));
			getContext().registerReceiver(receiverUpload, new IntentFilter(GlobalConstants.SERVICE_UPLOAD));
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		//logger.debug("Entry On  Create View");
		super.onCreateView(inflater, container, savedInstanceState);
		viewer = inflater.inflate(R.layout.devicestatus, container, false);

		return viewer;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedState){
		super.onActivityCreated(savedState);

		//logger.info("DeviceStatusFragment:onActivityCreated");

		DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, DeviceStatusContainer.class.getName());

		TextView tvVersion = getActivity().findViewById(R.id.topNavVersion);
		String vName = "?";

		try{
			PackageInfo manager = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			vName = manager.versionName;

		}
		catch(NameNotFoundException e){
			logger.debug("Exception", e);
		}

		tvVersion.setText(getResources().getString(R.string.homeAppVersion, vName));
		tvVersion.invalidate();

		TextView infoView = getActivity().findViewById(R.id.deviceStatusAvailDSSpaceVal);
		String trafficTotal = DBHelper.getInstance().fetchItemValueByItemName_Common(GlobalConstants.PREF_RUNNINGDATAUSAGE);

		String info = getResources().getString(R.string.deviceStatusDataUsage) + " " +
					  (trafficTotal == null || trafficTotal.isEmpty() ? getResources().getString(R.string.deviceStatusNoTraffic) : trafficTotal);
		infoView.setText(info);

		TextView versionView = getActivity().findViewById(R.id.homeAppVersionTV);
		Date buildDate = BuildConfig.buildTime;
		String bDate = SimpleDateFormat.getInstance().format(buildDate);
		versionView.setText(getResources().getString(R.string.deviceStatusLastAppUpdate) + bDate);

		mDataReports = new ArrayList<UploadLog>();

		LinearLayout homell = getActivity().findViewById(R.id.bottombar);
		homell.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				getActivity().onBackPressed();

			}
		});

		//=== DELETE THE LOG FILE ON DEVICE
		Button deleteButton = getActivity().findViewById(R.id.btnDelete);
		deleteButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				logger.info("DeviceStatusFragment:deleteButton");

				//=== sync with logger path
				try{
					String logfilePath = FileUtils.getAppDirectoryForLogFiles() + GlobalConstants.LOGGER_FILENAME + ".txt";
					FileUtils.deleteFile(logfilePath);
				}
				catch(Exception e){
					logger.error("DeviceStatusFragment", e);
					Utils.showAlertMessage(CTApp.getCustomAppContext(), getResources().getString(R.string.errorUnableToDelete, e));

				}
			}
		});

		Button cleanFilesButton = getActivity().findViewById(R.id.btnCleanFiles);

		logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
		if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
			cleanFilesButton.setVisibility(View.GONE);
		}

		cleanFilesButton.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v){
				// logger.info("DeviceStatusFragment:cleanFilesButton");

				new CleanFolder().execute("");
			}

		});

		//=== SET SIMULATION MODE FOR NAVIGATION
		simulationButton = getActivity().findViewById(R.id.btnSimulation);
		logger.info("DeviceStatusFragment:simulationButton");

		simulationButton.setOnClickListener(v -> {
			logger.info("DeviceStatusFragment:simulationButton");

			if(simulationButton.isChecked()){
				simulationButtonOnClicked();
			}
			else{
				simulationButtonOffClicked();
			}
		});

		//=== SET SIMULATION MODE FOR NAVIGATION
		testDataButton = getActivity().findViewById(R.id.btnTestData);
		logger.info("DeviceStatusFragment:testDataButton");

		testDataButton.setOnClickListener(v -> {
			logger.info("DeviceStatusFragment:testDataButton");

			if(testDataButton.isChecked()){
				testDataButtonOnClicked();
			}
			else{
				testDataButtonOffClicked();
			}
		});

//=== turn it off for production =========================================================
//testDataButton.setVisibility(View.GONE);
//========================================================================================

		//=== UPLOAD THE DB
		Button uploadDBButton = getActivity().findViewById(R.id.btnUploadDb);
		uploadDBButton.setText(getResources().getString(R.string.uploadDbtext));
		uploadDBButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				logger.info("DeviceStatusFragment:uploadDBButton");

				ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();

				if(!connectionState.isConnected){
					logger.info(">>>>DeviceStatusFragment.uploadDBButton.onClick() : ****" + connectionState.descriptiveText + "****");
				}
				else{

					new Thread(){
						public void run(){
							String dir = FileUtils.getAppDirectoryForDataBaseFiles();
							String dbFileName = dir + GlobalConstants.DATABASE_NAME;

							//=== Get log files
							ArrayList<String> files_list = new ArrayList<String>();

							String logdir = FileUtils.getAppDirectoryForLogFiles();
							String[] logfiles = getLogFiles(logdir);

							if(logfiles != null){
								for(String f : logfiles){
									files_list.add(logdir + f);
								}
							}

							String downloaddir = FileUtils.getAppDirectoryForDownloadFiles();
							String[] downloadfiles = getDownloadFiles(downloaddir);

							if(downloadfiles != null){
								for(String f : downloadfiles){
									files_list.add(downloaddir + f);
								}
							}

							//=== Add other files
							files_list.add(dbFileName);

							String[] files = new String[files_list.size()];
							files = files_list.toArray(files);

							String dir2 = FileUtils.getAppDirectoryForLogFiles();
							ZipUtil c = new ZipUtil(files, dir2 + "devicedata.ctz");
							c.zip();
							//=== EMAIL IT HERE
							File zipFile = new File(dir2 + "/devicedata.ctz");
							Uri uri = FileProvider.getUriForFile(DeviceStatusFragment.this.getActivity().getApplicationContext(), DeviceStatusFragment.this.getActivity().getApplicationContext().getPackageName() + ".provider", new File(zipFile.getAbsolutePath()));
							String mVCode = "unknown";
							PackageInfo manager = null;
							try{
								manager = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
								mVCode = manager.versionName;
							}
							catch(NameNotFoundException e){
								e.printStackTrace();
							}
							String mDeviceId = android.provider.Settings.System.getString(DeviceStatusFragment.this.getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
							Intent email = new Intent(Intent.ACTION_SEND);
							email.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
							email.putExtra(Intent.EXTRA_EMAIL, new String[]{"dougk@ansnewspapers.com", "tkvalentine56@gmail.com"});
							email.putExtra(Intent.EXTRA_SUBJECT, "log/DB upload");
							email.putExtra(Intent.EXTRA_TEXT, "Device ID : " + mDeviceId + "\n" +
															  "App version : " + mVCode + "\n" +
															  "Issue description : \n" +
															  "Issue time/date : ");
							email.putExtra(Intent.EXTRA_STREAM, uri);
							email.setType("message/rfc822");
							startActivity(email);
						}
					}.start();
				}
			}
		});

		updateTopNav();
	}

	public void simulationButtonOnClicked(){
		//=== By default app startup, this is disabled.
		CTApp.isInSimulation = true;
	}

	public void simulationButtonOffClicked(){
		CTApp.isInSimulation = false;
	}

	public void testDataButtonOnClicked(){
		//=== By default app startup, this is disabled.
		DBHelper.getInstance().setUseTestData_Common(true);
	}

	public void testDataButtonOffClicked(){
		DBHelper.getInstance().setUseTestData_Common(false);
	}

	private void cleanFolder(){
		(new Handler()).postDelayed(new Runnable(){
			public void run(){
				SharedPreferences prefs = getActivity()
						.getSharedPreferences(
								GlobalConstants.DEFAULT_PREF_FILE,
								Context.MODE_PRIVATE);
				String directory = prefs.getString(FileUtils.getAppDirectoryForSavedFiles(), null);
				File parent = new File(directory);

				FileUtils.deleteDirectory(parent);
			}
		}, 0);
	}

	public String[] getLogFiles(String logdir){
		File dir = new File(logdir);
		FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File dir, String filename){
				return filename.startsWith(GlobalConstants.LOGGER_FILENAME);
			}
		};

		String[] logFiles = dir.list(filter);

		return logFiles;
	}

	public String[] getDownloadFiles(String downloaddir){
		File dir = new File(downloaddir);
		FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File dir, String filename){
				return filename.endsWith(".txt");
			}
		};

		String[] downloadFiles = dir.list(filter);

		return downloadFiles;
	}

	@SuppressLint("RestrictedApi")
	private void updateTopNav(){
		mTopNavLine1 = getActivity().findViewById(R.id.topNavLine1);
		mTopNavLine1.setText(getResources().getString(R.string.deviceStatusTitle));

		mTopNavLine2 = getActivity().findViewById(R.id.topNavLine2);
		mTopNavLine2.setText(" ");

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
		mGpsStatusRadioButton.setChecked(DeviceLocationListener.getHasGPsFix());//.setImageDrawable(DataUtils.determineGPSStatusImagePortrait(getResources()));
		//logger.debug("SET GPS BUTTON TO (DeviceLocationListener.isGPSFix) = " + DeviceLocationListener.isGPSFix);

		// LETS USER CLICK TO TURN ON GPS
		mGpsStatusRadioButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				if(!gpsRadioButtonIsChecked && !DeviceLocationListener.getHasGPsFix()){
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					gpsRadioButtonIsChecked = DeviceLocationListener.getHasGPsFix();
					mGpsStatusRadioButton.setChecked(DeviceLocationListener.getHasGPsFix());
				}
			}
		});
	}

	private void addDeviceStatusItems(){
		if(!mIsProcessing){
			TableLayout row5TL = getActivity().findViewById(R.id.row5TL);
			row5TL.removeAllViews();

			mIsProcessing = true;
			mDataReports.clear();
			mDataReports.addAll(DBHelper.getInstance().fetchAllUploadLogsByStatus_Common(
					GlobalConstants.UPLOAD_STATUS_CONFIRMED, 20));

			// add the header row...
			addRow(null, true);

			for(int i = 0; i < mDataReports.size(); i++){
				addRow(mDataReports.get(i), false);
			}
		}

		mIsProcessing = false;
	}

	private void addRow(UploadLog uploadLog, boolean isHeader){
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.devicestatus_item, null);

		TextView batchHeader = view.findViewById(R.id.batchHeader);
		TextView numAddressHeader = view.findViewById(R.id.numAddressHeader);
		TextView numPhotosUploadedHeader = view.findViewById(R.id.numPhotosUploadedHeader);
		TextView numBreadCrumbHeader = view.findViewById(R.id.numBreadCrumbHeader);
		TextView gpsLocationOfUploadHeader = view.findViewById(R.id.gpsLocationOfUploadHeader);

		boolean addView = false;

		if(isHeader){
			addView = true;
			batchHeader.setText(getResources().getString(R.string.deviceStatusBatchId));
			numAddressHeader.setText(getResources().getString(R.string.deviceStatusNumAddress));
			numPhotosUploadedHeader.setText(getResources().getString(R.string.deviceStatusNumPhotosUploaded));
			numBreadCrumbHeader.setText(getResources().getString(R.string.deviceStatusNumBreadCrumbs));
			gpsLocationOfUploadHeader.setText(getResources().getString(R.string.deviceStatusAddress));
		}
		else if(uploadLog != null){
			//	logger.debug("" + uploadLog.toString());

			batchHeader.setText(uploadLog.getId() + "");

//			logger.debug(uploadLog.toString());
//			logger.debug("d.getUploadDataType()" + uploadLog.getUploadDataType());

			if(uploadLog.getUploadDataType().equals(GlobalConstants.URLPARAM_FILETYPE_BREADCRUMB)){
				numAddressHeader.setText("0");
				numPhotosUploadedHeader.setText("0");
				numBreadCrumbHeader.setText("" + uploadLog.getRecordsToSendCount());
				addView = true;
			}
			else if(uploadLog.getUploadDataType().equals(GlobalConstants.URLPARAM_FILETYPE_PHOTOS)){
				numAddressHeader.setText("0");
				numPhotosUploadedHeader.setText("" + uploadLog.getRecordsToSendCount());
				numBreadCrumbHeader.setText("0");
				addView = true;
			}
			else if(uploadLog.getUploadDataType().equals(GlobalConstants.URLPARAM_FILETYPE_ADDRESSDETAILIST)){
				numAddressHeader.setText("" + uploadLog.getRecordsToSendCount());
				numPhotosUploadedHeader.setText("0");
				numBreadCrumbHeader.setText("0");
				addView = true;
			}

			if(uploadLog.getAddress() != null && uploadLog.getAddress().length() > GlobalConstants.MAX_DATA_REPORT_ADDRESS_LEN){
				gpsLocationOfUploadHeader.setText(uploadLog.getAddress().substring(0, GlobalConstants.MAX_DATA_REPORT_ADDRESS_LEN - 1) + "...");
			}
			else{
				gpsLocationOfUploadHeader.setText(uploadLog.getAddress());
			}
		}

		if(addView){
			mDataList.addView(view);
		}
	}

	@Override
	public void onResume(){
		super.onResume();

		simulationButton.setChecked(false);
		if(CTApp.isInSimulation){
			simulationButton.setChecked(true);
		}

		testDataButton.setChecked(false);
		if(DBHelper.getInstance().getUseTestData_Common()){
			testDataButton.setChecked(true);
		}

//		loadData();
	}

	@Override
	public void onStop(){
		super.onStop();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();

		try{
			getActivity().unregisterReceiver(receiverUpload);
			getActivity().unregisterReceiver(receiverGPS);
		}
		catch(Exception e){
			logger.error("Exception", e);
		}
	}

	@Override
	public void onPause(){
		super.onPause();
	}

	private class CleanFolder extends AsyncTask<String, Void, String>{
		@Override
		protected String doInBackground(String... params){
			SharedPreferences prefs = getActivity().getSharedPreferences(
					GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
			//=== THIS IS THE DOWNLOADED CONTENT FILES
			String directory = prefs.getString(FileUtils.getAppDirectoryForSavedFiles(), null);

			File parent = new File(directory);
			FileUtils.deleteDirectory(parent);

			return "Executed";
		}

		@Override
		protected void onPostExecute(String result){
			if(cleanProgressDialog.isShowing() && cleanProgressDialog != null){
				cleanProgressDialog.dismiss();
			}
		}

		@Override
		protected void onPreExecute(){
			cleanProgressDialog = ProgressDialog.show(getActivity(),
													  getResources().getString(R.string.deviceStatusPleaseWait),
													  getResources().getString(R.string.deviceStatusCleaningFolder));
		}

		@Override
		protected void onProgressUpdate(Void... values){
		}
	}
}