package com.agilegeodata.carriertrack.android.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
/*
Deprecated
Use the Support Library androidx. fragment. app. ListFragment
*/
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.RouteDetailsActivity;
import com.agilegeodata.carriertrack.android.activities.RouteSelectActivity;
import com.agilegeodata.carriertrack.android.adapters.ListItemRouteAdapter;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;
import com.agilegeodata.carriertrack.android.objects.Route;
import com.agilegeodata.carriertrack.android.services.ConnectionStatus;
import com.agilegeodata.carriertrack.android.services.RunningServices;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

/*
 * Route Select
 * Enables the selection of the route
 */
public class RouteSelectFragment extends ListFragment{
	protected static final String TAG = RouteSelectFragment.class.getSimpleName();
	private static final int ROUTELIST_MSG_REFRESH = 1;
	private static final int ROUTELIST_REFRESH_PERIOD = 60 * 1000;
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static ProgressDialog pd;
	private static boolean gpsRadioButtonIsChecked = false;
	TextView mTopNavLine1;
	TextView mTopNavLine2;
	AppCompatRadioButton mGpsStatusRadioButton;
	ListItemRouteAdapter mAdapter;
	private ArrayList<Route> mDataList;
	private final Handler routeDownloadFinishedHandler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
				case 0:
					refreshScreen();
					if(pd != null){
						pd.dismiss();
					}
					break;
				case -1:
					refreshScreen();
					if(pd != null){
						pd.dismiss();
					}
					break;
				case 1:
					refreshScreen();
					if(pd != null){
						pd.dismiss();
					}
					break;
				default:
					refreshScreen();
					if(pd != null){
						pd.dismiss();
					}
					break;
			}
		}
	};
	private EditText mSearchET;
	private View viewer;
	private boolean mIsProcessing;
	private final Handler handlerGPS = new Handler(){

		public void handleMessage(Message msg){
			mIsProcessing = false;

			if(getActivity() != null){
				//===causing crashes as activity is sometimes null
				updateTopNav();
			}
		}

	};
	private float mBearing;
	private final BroadcastReceiver gpsReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
			if(!mIsProcessing){
				mIsProcessing = true;
				Bundle b = intent.getExtras();
				mBearing = b.getFloat(GlobalConstants.EXTRA_CURRENT_BEARING, 0);
				// logger.debug("mGeoCode : " + mGeoCode);
				handlerGPS.sendEmptyMessage(0);
			}
		}
	};
	private String mUserNameStr;
	private boolean mDatabaseUpdated;
	private HashMap<Integer, Integer> latestCounts = null;
	private String mVCode;
	private final Handler routeDownloadHandler = new Handler(){
		public void handleMessage(Message msg){
			mDatabaseUpdated = false;
			String routeID = (String) msg.obj;
			downloadRoute(routeID, msg.arg1);
		}
	};
	private final BroadcastReceiver routeDownloadReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			Bundle b = intent.getExtras();

			int jobID = b.getInt(GlobalConstants.JOB_ID);
			String routeID = b.getString(GlobalConstants.ROUTE_ID);
			Message msg = new Message();
			msg.arg1 = jobID;
			msg.obj = routeID;

			routeDownloadHandler.sendMessage(msg);
		}
	};
	private int deliveryTargetZoneSelection = GlobalConstants.DEF_DELIVERY_QUADS_LEFT_FRONT_AND_RIGHT_FRONT;
	private GlobalConstants.OPERATIONS_MODE tempRouteOpType = CTApp.operationsMode;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// logger.debug("Entering RouteSelect Fragment on Create");
		setRetainInstance(true);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			ContextCompat.registerReceiver(getContext(), gpsReceiver,
										   new IntentFilter(GlobalConstants.SERVICE_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED);

			ContextCompat.registerReceiver(getContext(), routeDownloadReceiver,
										   new IntentFilter(GlobalConstants.DOWNLOAD_ROUTE), ContextCompat.RECEIVER_NOT_EXPORTED);
		}
		else{
			getContext().registerReceiver(gpsReceiver,
										  new IntentFilter(GlobalConstants.SERVICE_LOCATION));

			getContext().registerReceiver(routeDownloadReceiver,
										  new IntentFilter(GlobalConstants.DOWNLOAD_ROUTE));
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		// logger.debug("Entry On RouteSelect Create View");
		super.onCreateView(inflater, container, savedInstanceState);
		viewer = inflater.inflate(R.layout.routeselect, container, false);

		return viewer;
	}

	@Override
	public void onActivityCreated(Bundle savedState){
		super.onActivityCreated(savedState);
		// logger.debug("---------------- Entry onActivityCreated");

		DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, RouteSelectActivity.class.getName());

		mSearchET = getActivity().findViewById(R.id.searchVal);

		mDataList = new ArrayList<Route>();

		updateTopNav();

		// SENDS US BACK TO THE HOME SCREEN IF PUSHED
		LinearLayout homell = getActivity().findViewById(R.id.bottombar);
		homell.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				getActivity().onBackPressed();
			}
		});

		mAdapter = new ListItemRouteAdapter(getActivity(), mDataList);
		setListAdapter(mAdapter);

		mSearchET.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence s, int start, int before, int count){
				String filterText = mSearchET.getText().toString().toLowerCase();

				setListAdapter(mAdapter);

				try{
					// logger.debug("Entering the text listner changed");
					if("".equals(filterText)){
						mDataList.clear();
						mDataList.addAll(DBHelper.getInstance().fetchAllRoutes_Common());
					}
					else if(filterText.length() < GlobalConstants.ROUTE_CHARS_MIN_SEARCH){
						//=== do nothing
					}
					else{
						mDataList.clear();
						mDataList.addAll(DBHelper.getInstance().fetchAllRoutesByFilter_Common(
								filterText));
					}

					mAdapter = new ListItemRouteAdapter(getActivity(), mDataList);
					setListAdapter(mAdapter);
				}
				catch(Exception e){
					// logger.debug("Exception e" + e);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after){
			}

			public void afterTextChanged(Editable s){
			}
		});
	}

	public void onListItemClick(ListView l, View v, int position, long id){
		super.onListItemClick(l, v, position, id);

		Route route = mDataList.get(position);

		if(route.getNumAddress() > 0){
			SharedPreferences prefs = getActivity().getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

			Calendar cal = Calendar.getInstance();

			ContentValues iVals = new ContentValues();
			int tZOffSet = prefs.getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);
			long gmtTimestamp = cal.getTimeInMillis() + tZOffSet;
			iVals.put(DBHelper.KEY_JOBDETAILID, route.getJobDetailId());
			CTApp.setJobDetailId(route.getJobDetailId());
			iVals.put(DBHelper.KEY_STARTDATE, gmtTimestamp);
			iVals.put(DBHelper.KEY_ENDDATE, 0);
			iVals.put(DBHelper.KEY_UPLOADED, GlobalConstants.DEF_UPLOADED_FALSE);
			iVals.put(DBHelper.KEY_UPLOADBATCHID, GlobalConstants.DEF_UPLOADED_BATCH_ID);

			DBHelper.getInstance().createRecord_Common(iVals, DBHelper.DB_T_ROUTELISTACTIVITY, DBHelper.KEY_ID, false);
			DBHelper.getInstance().updateRouteStartOrEndDate_Common(route.getJobDetailId(), false, 0);

			logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
			if(route.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.UNSEQ.toString())){
				tempRouteOpType = CTApp.operationsMode;
				CTApp.operationsMode = GlobalConstants.OPERATIONS_MODE.SEQUENCING;
			}

			logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());

			displayUsageDisclaimerDialog("Usage Disclaimer", route);
		}
	}

	private void continuePostRouteTargetZoneSelection(Route route){
		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
			displayRouteTargetZoneSelectorDialog(getResources().getString(R.string.routeSelectDeliveryZoneSelect), route);
		}
		else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
			displayRouteTargetZoneSelectorDialog("Select a Discovery Zone for Route Sequencing", route);
		}
		else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
			justStartTheRouteDetailsActivity(route);
		}
		else{
			//=== DEFAULT IF ROUTE JOBTYPE CANNOT BE DETERMINED, LET USER
			//=== AND MANAGEMENT RESOLVE THIS IN THE DATA DOWNLOADED
			route.setRouteJobType(Route.RouteJobType.RANDOM.toString());
			displayRouteTargetZoneSelectorDialog(getResources().getString(R.string.routeSelectDeliveryZoneSelect), route);
		}
	}

	private void displayUsageDisclaimerDialog(String titleStr, Route route){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.app_usage_disclaimer_dialog, getActivity().findViewById(R.id.layout_root));

		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText(titleStr);

		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);

		final TextView disclaimer = layout.findViewById(R.id.disclaimerTextView);
		String disclaimerText = "\nThis route list is computer-generated and is provided to " +
								"indicate the current delivery order and current special delivery requests from subscribers. " +
								"Contractor is free to adjust the list and to deal with the subscriber " +
								"in a reasonable manner consistent with the contract.\n\n" +
								"Contractor is responsible for obeying all traffic ordinances and " +
								"should always maintain awareness around their vehicle during delivery.\n";
		disclaimer.setText(disclaimerText);
		disclaimer.setTag(R.id.key_for_route_delivery_target_zone, route);

		builder.setView(layout);
		builder.setPositiveButton("Continue", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt){
				paramDialogInterface.dismiss();
				Route selectedRoute = (Route) disclaimer.getTag(R.id.key_for_route_delivery_target_zone);
				RouteSelectFragment.this.continuePostRouteTargetZoneSelection(selectedRoute);
			}
		});

		builder.setNegativeButton("Exit App", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt){
				logger.debug("**** KILLING SERVICES and EXIT APP");
				paramDialogInterface.dismiss();
				RunningServices.killAllServicesIfRunning(RouteSelectFragment.this.getActivity());
				RouteSelectFragment.this.getActivity().finishAffinity();
				Runtime.getRuntime().exit(0); // Kill kill kill!
			}
		});

		AlertDialog disclaimerDialog = builder.create();

		disclaimerDialog.show();
		//=== Change the alert dialog background color
		disclaimerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

		//=== Get the alert dialog buttons reference
		Button positiveButton = disclaimerDialog.getButton(AlertDialog.BUTTON_POSITIVE);

		//=== Change the alert dialog buttons text and background color
		positiveButton.setTextColor(Color.BLACK);
		positiveButton.setBackgroundColor(Color.CYAN);

		//=== Get the alert dialog buttons reference
		Button negativeButton = disclaimerDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

		//=== Change the alert dialog buttons text and background color
		negativeButton.setTextColor(Color.BLACK);
		negativeButton.setBackgroundColor(Color.CYAN);

		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
		layoutParams.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
		layoutParams.gravity = Gravity.NO_GRAVITY;
		layoutParams.leftMargin = 10;
		layoutParams.rightMargin = 10;
		layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		layoutParams.weight = 1;
		positiveButton.setLayoutParams(layoutParams);

		LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) negativeButton.getLayoutParams();
		layoutParams2.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
		layoutParams2.gravity = Gravity.NO_GRAVITY;
		layoutParams2.leftMargin = 10;
		layoutParams2.rightMargin = 10;
		layoutParams2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		layoutParams2.weight = 1;
		negativeButton.setLayoutParams(layoutParams2);
	}

	private void justStartTheRouteDetailsActivity(Route selectedRoute){
		//=== START RANDOM DELIVERY
		Intent brse = null;
		brse = new Intent(getActivity(), RouteDetailsActivity.class);

		brse.putExtra(GlobalConstants.EXTRA_ROUTE_ID, selectedRoute.getRouteId());
		brse.putExtra(GlobalConstants.EXTRA_BEARING, mBearing);
		brse.putExtra(GlobalConstants.EXTRA_JOBDETAILID, selectedRoute.getJobDetailId());
		brse.putExtra(GlobalConstants.EXTRA_DELIVERY_QUADS, deliveryTargetZoneSelection);

		brse.putExtra(GlobalConstants.EXTRA_ROUTE_TYPE, selectedRoute.getRouteJobType());

		startActivity(brse);

	}

	private void displayRouteTargetZoneSelectorDialog(String titleStr, Route route){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.route_target_zone_selection_dialog, getActivity().findViewById(R.id.layout_root));

		final RadioGroup radioDeliveryZoneGroup = layout.findViewById(R.id.radioDeliveryZone);
		radioDeliveryZoneGroup.setTag(R.id.key_for_route_delivery_target_zone, route);

		radioDeliveryZoneGroup.check(R.id.radioDefaultLeftAndRightFront);

		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText(titleStr);
		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);

		builder.setView(layout);
		final AlertDialog targetZoneSelectDialog = builder.create();

		targetZoneSelectDialog.setButton(AlertDialog.BUTTON_POSITIVE,
										 getResources().getString(R.string.dialogContinue),//getResources().getString(R.string.save),
										 new DialogInterface.OnClickListener(){
											 public void onClick(DialogInterface dialog, int id){
											 }
										 });

		targetZoneSelectDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog){
				Button continueButton = targetZoneSelectDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				continueButton.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v){
						int targetZoneSelection = radioDeliveryZoneGroup.getCheckedRadioButtonId();

						if(targetZoneSelection == R.id.radioDefaultLeftAndRightFront){
							deliveryTargetZoneSelection = GlobalConstants.DEF_DELIVERY_QUADS_LEFT_FRONT_AND_RIGHT_FRONT;
						}
						else if(targetZoneSelection == R.id.radioLeftFrontOnly){
							deliveryTargetZoneSelection = GlobalConstants.DEF_DELIVERY_QUADS_LEFT_FRONT;
						}
						else if(targetZoneSelection == R.id.radioRightFrontOnly){
							deliveryTargetZoneSelection = GlobalConstants.DEF_DELIVERY_QUADS_RIGHT_FRONT;
						}

						Route selectedRoute = (Route) radioDeliveryZoneGroup.getTag(R.id.key_for_route_delivery_target_zone);
						targetZoneSelectDialog.dismiss();

						//=== START RANDOM DELIVERY
						Intent brse = null;
						brse = new Intent(getActivity(), RouteDetailsActivity.class);

						brse.putExtra(GlobalConstants.EXTRA_ROUTE_ID, selectedRoute.getRouteId());
						brse.putExtra(GlobalConstants.EXTRA_BEARING, mBearing);
						brse.putExtra(GlobalConstants.EXTRA_JOBDETAILID, selectedRoute.getJobDetailId());
						brse.putExtra(GlobalConstants.EXTRA_DELIVERY_QUADS, deliveryTargetZoneSelection);

						brse.putExtra(GlobalConstants.EXTRA_ROUTE_TYPE, selectedRoute.getRouteJobType());

						startActivity(brse);
					}
				});
			}
		});

		targetZoneSelectDialog.show();
		//=== Change the alert dialog background color
		targetZoneSelectDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

		//=== Get the alert dialog buttons reference
		Button positiveButton = targetZoneSelectDialog.getButton(AlertDialog.BUTTON_POSITIVE);

		//=== Change the alert dialog buttons text and background color
		positiveButton.setTextColor(Color.BLACK);
		positiveButton.setBackgroundColor(Color.CYAN);

		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
		layoutParams.leftMargin = 5;
		layoutParams.rightMargin = 5;
		positiveButton.setLayoutParams(layoutParams);
	}

	private void refreshScreen(){
		mDataList.clear();

		mDataList.addAll(DBHelper.getInstance().fetchAllRoutes_Common());//numAddresses, numDeliveredAddresses, numPhotos, numPhotosUploaded));

		mAdapter.notifyDataSetChanged();
	}

	@SuppressLint("RestrictedApi")
	private void updateTopNav(){
		mTopNavLine1 = getActivity().findViewById(R.id.topNavLine1);
		String line1 = "Delivering";

		logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
			line1 = "Delivering";
		}
		else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
			line1 = "Re-sequencing";
		}
		else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
			line1 = "Re-numbering";
		}

		mTopNavLine1.setText(line1);

		mTopNavLine2 = getActivity().findViewById(R.id.topNavLine2);
		mTopNavLine2.setText(getResources().getString(R.string.routeSelectDesc));

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
//		logger.debug("SET GPS BUTTON TO (DeviceLocationListener.isGPSFix) = " + DeviceLocationListener.isGPSFix);

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

	public void downloadRoute(final String routeId, final int jobId){
		ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();

		if(!connectionState.isConnected){
			logger.info("****" + connectionState.descriptiveText + "****");
		}
		else{
			pd = ProgressDialog.show(getActivity(), getResources().getString(R.string.routeSelectDownloadingTitle),
									 getResources().getString(R.string.routeSelectDownloadingMessage), true, false);

			Thread t = new Thread(){

				public void run(){
					try{
						String data = "";
						SharedPreferences prefs = getActivity()
								.getSharedPreferences(
										GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
						String mSaveDir = prefs.getString(
								FileUtils.getAppDirectoryForSavedFiles(), "");

						doData(data, mSaveDir, routeId, jobId);

						HashMap<String, String> map = new HashMap<String, String>();
						map.put(GlobalConstants.URLPARAM_DEVICEID, mUserNameStr);
						map.put(GlobalConstants.URLPARAM_ANDROIDVERSION,
								android.os.Build.VERSION.RELEASE);
						map.put(GlobalConstants.URLPARAM_CTVERSION, mVCode);

						StringBuffer buf = new StringBuffer();

						if(mDatabaseUpdated){
							Set<Integer> keys = latestCounts.keySet();

							Iterator<Integer> it = keys.iterator();
							while(it.hasNext()){
								Integer key = it.next();

								buf.append(key
										   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
										   + latestCounts.get(key)
										   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
										   + GlobalConstants.DEF_NOT_DELETED);
								buf.append(System.getProperty("line.separator"));
							}

							map.put(GlobalConstants.URLPARAM_DATA, buf.toString());

							try{
								sendConfData(map);
							}
							catch(Exception e){
								routeDownloadFinishedHandler.sendEmptyMessage(-1);
							}
						}

						routeDownloadFinishedHandler.sendEmptyMessage(0);
					}
					catch(Exception e){
						logger.debug("Exception : " + e);

						routeDownloadFinishedHandler.sendEmptyMessage(-1);
					}
				}
			};

			t.start();
		}
	}

	private void doData(String data, String saveDir, String routeId, int jobId){
		try{
			PackageInfo manager = getActivity().getPackageManager()
											   .getPackageInfo(getActivity().getPackageName(), 0);
			mVCode = manager.versionName;
		}
		catch(PackageManager.NameNotFoundException e){
			e.printStackTrace();
		}

		SharedPreferences prefs = getActivity().getSharedPreferences(
				GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
		mUserNameStr = prefs.getString(GlobalConstants.PREF_DEVICE_ID, null);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(GlobalConstants.URLPARAM_CTVERSION, mVCode);
		map.put(GlobalConstants.URLPARAM_DEVICEID, mUserNameStr);
		map.put(GlobalConstants.URLPARAM_ANDROIDVERSION,
				android.os.Build.VERSION.RELEASE);
		map.put(GlobalConstants.URLPARAM_JOBID, Integer.toString(jobId));
		map.put(GlobalConstants.URLPARAM_ROUTEID, routeId);

		try{
			sendData(map, saveDir, true);
		}
		catch(ClientProtocolException cpe){
		}
		catch(ZipException ze){
		}
		catch(IOException ioe){
		}
	}

	/*
	 * Requires:
	 *
	 * deviceId: device id from properties, should be the same as login
	 */
	private void sendConfData(final HashMap<String, String> map)
			throws IOException{

		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					GlobalConstants.URL_DOWNLOADCONFIRM_DATA);
			httppost.setHeader("Content-Transfer-Encoding", "8bit");
			httpclient.getParams().setParameter("http.socket.timeout",
												Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
			httppost.getParams().setParameter("http.socket.timeout",
											  Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
			List<NameValuePair> nvps = Utils.buildURLParamsPost(map);

			httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			HttpResponse res = httpclient.execute(httppost);
			InputStream stream = res.getEntity().getContent();
			// logger.debug(TAG,Utils.inputStreamToString(stream));

			try{
				stream.close();
			}
			catch(Exception e){
			}
		}
		catch(Exception e){
		}
	}

	/*
	 * Requires:
	 *
	 * deviceId: device id from properties, should be the same as login
	 */
	private void sendData(final HashMap<String, String> map,
						  final String saveDir, boolean isZip)
			throws IOException{
		mDatabaseUpdated = false;

		try{

			HttpClient httpclient = new DefaultHttpClient();

			if(isZip){
				HttpPost httppost = new HttpPost(GlobalConstants.URL_DOWNLOAD_ZIP_NEW);

				httppost.setHeader("Content-Transfer-Encoding", "8bit");
				httpclient.getParams().setParameter("http.socket.timeout",
													Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
				httppost.getParams().setParameter("http.socket.timeout",
												  Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));

				List<NameValuePair> nvps = Utils.buildURLParamsPost(map);

				httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

				HttpResponse res = httpclient.execute(httppost);
				InputStream stream = res.getEntity().getContent();
				int contentLength = (int) res.getEntity().getContentLength();
				logger.debug("zip content length = " + contentLength);
				latestCounts = DBHelper.getInstance().executeDatabaseUpdatesZip_Common(stream, saveDir);

				if(latestCounts != null){
					mDatabaseUpdated = true;
				}

				try{
					stream.close();
				}
				catch(Exception e){
				}
			}
			else{
				HttpPost httppost = new HttpPost(GlobalConstants.URL_DOWNLOAD_DATA);

				httppost.setHeader("Content-Transfer-Encoding", "8bit");
				httpclient.getParams().setParameter("http.socket.timeout",
													Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
				httppost.getParams().setParameter("http.socket.timeout",
												  Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
				List<NameValuePair> nvps = Utils.buildURLParamsPost(map);

				httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

				HttpResponse res = httpclient.execute(httppost);
				InputStream stream = res.getEntity().getContent();

				latestCounts = DBHelper.getInstance().executeDatabaseUpdatesZip_Common(stream,
																					   saveDir);
				if(latestCounts != null){
					mDatabaseUpdated = true;
				}

				try{
					stream.close();
				}
				catch(Exception e){
				}
			}
		}
		catch(Exception e){
		}
	}

	@Override
	public void onPause(){
		super.onPause();
		refreshHandler.removeMessages(ROUTELIST_MSG_REFRESH);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();

		try{
			getActivity().unregisterReceiver(gpsReceiver);
			getActivity().unregisterReceiver(routeDownloadReceiver);
		}
		catch(Exception e){
			logger.error("EXCEPTION : " + e.getMessage());
		}
	}

	@Override
	public void onStop(){
		super.onStop();
	}

	@Override
	public void onResume(){
		try{

			CTApp.operationsMode = tempRouteOpType;
			mDataList.clear();

			mDataList.addAll(DBHelper.getInstance().fetchAllRoutes_Common());//numAddresses, numDeliveredAddresses, numPhotos, numPhotosUploaded));

			mAdapter.notifyDataSetChanged();

			refreshHandler.sendEmptyMessageDelayed(ROUTELIST_MSG_REFRESH, ROUTELIST_REFRESH_PERIOD);
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}

		super.onResume();
	}

	/**
	 * Refreshes the list view
	 */
	private final Handler refreshHandler = new Handler(new Handler.Callback(){
		public boolean handleMessage(Message msg){
			// Refresh list
			refreshScreen();

			logger.debug("refreshScreen");

			refreshHandler.sendEmptyMessageDelayed(ROUTELIST_MSG_REFRESH,
												   ROUTELIST_REFRESH_PERIOD);
			return true;
		}
	});
}