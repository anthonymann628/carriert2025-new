package com.agilegeodata.carriertrack.android.fragments;

import static android.content.Context.RECEIVER_NOT_EXPORTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.EmbeddedNavigationActivity;
import com.agilegeodata.carriertrack.android.activities.RouteDetailsActivity;
import com.agilegeodata.carriertrack.android.adapters.ListItemRouteDetailsAdapterCommon;
import com.agilegeodata.carriertrack.android.adapters.RouteDetailsViewPagerAdapter;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.herenavigation.MyHereNavigationFragment;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;
import com.agilegeodata.carriertrack.android.objects.DeliveryItem;
import com.agilegeodata.carriertrack.android.objects.DeliveryItemProduct;
import com.agilegeodata.carriertrack.android.objects.Route;
import com.agilegeodata.carriertrack.android.objects.StreetSummaryRandom;
import com.agilegeodata.carriertrack.android.services.ConnectionStatus;
import com.agilegeodata.carriertrack.android.utils.GPSUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

public class RouteDetailsRightSideFragmentMerged extends Fragment{
	//==============================================================
	//=== COMMON VARIABLES BETWEEN RANDOM AND SEQUENCED MODE
	//==============================================================
	public static final String TAG = RouteDetailsRightSideFragmentMerged.class.getSimpleName();
	public static RouteDetailsRightSideFragmentMerged instance = null;
	public static boolean isInPauseMode;
	public static boolean saveIsPaused = isInPauseMode;
	public static int DELIVERY_LIST_LIMIT = 200;
	protected static Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	static protected boolean useGoogleMaps = false;
	static float lastBearing = 0f;
	static boolean flashPauseButton = true;
	private static boolean gpsRadioButtonIsChecked = false;
	public ArrayList<DeliveryItem> mDataDisplayList = null;
	public ListItemRouteDetailsAdapterCommon mRouteDetailListAdapterCommon = null;
	public boolean useListTextToSpeech = false;
	public int mJobDetailId;
	public int SEQUENCE_ID_OF_LAST_ROUTE_ITEM = -1;
	public int SEQUENCE_ID_OF_LAST_DELIVERY = -1;
	public int SEQUENCE_ID_INTERVAL = -1;
	protected float mGPSSpeed = 0f;
	protected RouteDetailsViewPagerAdapter routeDetailsRightSideViewPagerAdapter = new RouteDetailsViewPagerAdapter();
	protected ListView mRouteDetailListView;
	protected int mListPositionOfClosestItem;
	private final Handler refreshScreenHandler = new Handler(){

		public void handleMessage(Message msg){

			mRouteDetailListAdapterCommon.notifyDataSetChanged();
			if(mRouteDetailListView.getCount() > 0){
				mRouteDetailListView.setSelection(mListPositionOfClosestItem);
			}

		}
	};
	protected HashMap<Integer, StreetSummaryRandom> mStreetSummaryHashMap;
	protected HashMap<Integer, Integer> mCustSrvMap;
	protected HashMap<Integer, Integer> mDoNotDeliverMap;
	protected View routeDetailsRightSideView;
	protected ViewPager routeDetailsRightSideViewPager;
	protected LinearLayout routeDetailsRightSideViewListPage;
	protected RelativeLayout googleMapPage;
	protected GoogleMap googleMap;
	protected TextToSpeech textToSpeech = null;
	protected String mRouteId;
	protected int mDeliveryQuads;
	protected Bundle currentSavedState = null;
	protected boolean isProcessingDelivery = false;
	protected Route mRoute;
	protected int mTZOffSet;
	protected double mDeviceAzimuth;
	protected float mDeviceForwardAzimuth;
	protected ImageButton pauseButton;
	protected float mLastDeviceForwardAzimuth;

	//==============================================================
	//=== DIVERGENT VARIABLES BETWEEN RANDOM AND SEQUENCED MODE
	//==============================================================
	protected FloatingActionButton googleMapSlideButton;
	protected int mDeliveryAreaProjectedFrontDistance = 40;
	protected int mDeliveryAreaProjectedSideDistance = 40;
	//=== SEQUENCED OPERATION VARIABLES
	protected int sound1;
	protected SoundPool soundPool;
	protected int mListPositionOfLastDelivery;
	//=== USED TO MAINTAIN STATE OF PROCESSING DELIVERIES
	protected int GREATEST_SEQUENCE_ID_PROCESSED = -1;
	protected int SEQUENCE_ID_OF_LAST_LIST_ITEM = -1;
	protected int SEQUENCE_ID_OF_NEXT_DELIVERY = -1;
	protected int LAST_DELIVERY_RECORD_ID = -1;
	protected int NEXT_DELIVERY_RECORD_ID = -1;
	protected int CURRENT_DELIVERY_LIST_SIZE = -1;
	protected int CURRENT_DELIVERY_LIST_PAGE = 0;
	//=== RANDOM OPERATION VARIABLES
	protected String mSearchValStr;
	protected int mExtendedAreaProjectedFrontDistance = 160;
	protected int mExtendedAreaProjectedSideDistance = 80;
	protected GpsReceiver gpsReceiver = new GpsReceiver();
	ArrayList<DeliveryItem> addressesWithinDeliveryArea = null;
	ArrayList<String> voiceAnnouncements = new ArrayList<String>();
	private boolean pauseButtonEnabled = true;
	private boolean pauseButtonActivated = false;
	private ImageButton navButtonIcon;
	private ImageButton deliveryPagePreviousButton;
	private ImageButton deliveryPageNextButton;
	private TextView pageNumberText;
	protected Handler gpsReceiverHandler = new Handler(){
		public void handleMessage(Message msg){
			logger.debug("gpsReceiverHandler() : ENTER");
			logger.debug("gpsReceiverHandler() : CTApp.operationsMode = " + CTApp.operationsMode.toString());
			logger.debug("gpsReceiverHandler() : mRoute.getRouteJobType() = " + mRoute.getRouteJobType());

			if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
				if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
					updateListFromHandlerRandom(null, mDeliveryQuads, false);
				}
				else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
					updateListFromHandlerRandom(null, mDeliveryQuads, false);
				}
				else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
					//=== SHOULD NEVER GET HERE
					logger.error("SEQUENCING ERROR CTApp.operationsMode = " + CTApp.operationsMode.toString());
					logger.error("SEQUENCING ERROR mRoute.routeType = " + mRoute.getRouteJobType());
				}
			}
			else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
				logger.debug("WE DON'T NEED TO UPATE AS WE ARE NOT DELIVERING OR SEQUENCING");
			}
			else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
				if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
					updateListFromHandlerSequenced(null, mDeliveryQuads, false);
				}
				else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
					//=== SHOULD NEVER GET HERE
					logger.error("DELIVERY ERROR CTApp.operationsMode = " + CTApp.operationsMode.toString());
					logger.error("DELIVERY ERROR mRoute.routeType = " + mRoute.getRouteJobType());
				}
				else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
					updateListFromHandlerRandom(null, mDeliveryQuads, false);
				}
			}
			else{
				//=== SHOULD NEVER GET HERE
				logger.error("ERROR CTApp.operationsMode = " + CTApp.operationsMode);
			}
		}
	};
	protected BroadcastReceiver pauseReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			Bundle b = intent.getExtras();
			isInPauseMode = b.getBoolean(GlobalConstants.EXTRA_PAUSE_MODE, true);

			logger.info("**** PAUSE RECEIVER CALLED, PAUSE = " + isInPauseMode);
			pausedChanged();
		}
	};
	private DeliveryItem mClosestItem;
	private int mDeliveryMode; // 0 if both, 1 if left 2 if right
	private Button navSequenceAlertButton = null;
	private LinearLayout navSequenceAlertPanel = null;
	protected BroadcastReceiver skippedDropReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			logger.info("**** SKIPPED DROP RECEIVER CALLED");

			Bundle b = intent.getExtras();
			//String alert = b.getString("ALERT_MESSAGE", "Possibly missed/skipped a sequenced delivery.");
			Integer lowestSkippedSequenceNumber = b.getInt("ALERT_DATA");

			if(lowestSkippedSequenceNumber != null && lowestSkippedSequenceNumber > 0){
				if(RouteDetailsActivity.instance.navigationMode == GlobalConstants.NAVIGATION_MODE.NAVIGATION_OFF ||
				   RouteDetailsActivity.instance.navigationMode == GlobalConstants.NAVIGATION_MODE.NAVIGATION_SPLIT_SCREEN){
					logger.info("**** 'SKIPPED DROP ALERT' NAVIGATION OFF OR SPLIT SCREEN DISPLAYED FOR drop list");

					navSequenceAlertPanel.setVisibility(View.VISIBLE);

					voiceAnnouncements.add("Delivery sequence may have been broken.");
					new Handler().postDelayed(new Runnable(){
						@Override
						public void run(){
							playVoiceMessagesInQueue();
						}
					}, 100);
				}
				else{
					logger.info("**** SKIPPED DISPLAYING DROP ALERT FOR DELIVERY list AS NAVIGATION IS ACTIVE");
				}
			}
		}
	};

	//==============================================================
	//=== COMMON METHODS BETWEEN RANDOM AND SEQUENCED MODE
	//==============================================================

	public static boolean getPauseMode(){
		return isInPauseMode;
	}

	public void setPauseMode(boolean inPauseMode){
		isInPauseMode = inPauseMode;
		pausedChanged();
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		logger.info("===--->>> onCreate() : ENTER");

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			logger.debug("pause and gps receivers registered API 33+");
			ContextCompat.registerReceiver(getContext(), pauseReceiver, new IntentFilter(GlobalConstants.INTENT_PAUSE_MODE), ContextCompat.RECEIVER_NOT_EXPORTED);
			ContextCompat.registerReceiver(getContext(), gpsReceiver, new IntentFilter(GlobalConstants.SERVICE_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED);

			ContextCompat.registerReceiver(getContext(), skippedDropReceiver, new IntentFilter(GlobalConstants.INTENT_SKIPPED_DELIVERY), ContextCompat.RECEIVER_NOT_EXPORTED);
		}
		else{
			logger.debug("pause and gps receivers registered API <=32");
			getContext().registerReceiver(pauseReceiver, new IntentFilter(GlobalConstants.INTENT_PAUSE_MODE));
			getContext().registerReceiver(gpsReceiver, new IntentFilter(GlobalConstants.SERVICE_LOCATION));

			getContext().registerReceiver(skippedDropReceiver, new IntentFilter(GlobalConstants.INTENT_SKIPPED_DELIVERY));
		}

		isInPauseMode = true;
		saveIsPaused = isInPauseMode;
		//logger.debug("STARTED");

		if(savedInstanceState != null){
			GREATEST_SEQUENCE_ID_PROCESSED = savedInstanceState.getInt("GREATEST_SEQUENCE_ID_PROCESSED");
			SEQUENCE_ID_OF_LAST_ROUTE_ITEM = savedInstanceState.getInt("SEQUENCE_ID_OF_LAST_ROUTE_ITEM");
			SEQUENCE_ID_OF_LAST_LIST_ITEM = savedInstanceState.getInt("SEQUENCE_ID_OF_LAST_LIST_ITEM");
			SEQUENCE_ID_OF_NEXT_DELIVERY = savedInstanceState.getInt("SEQUENCE_ID_OF_NEXT_DELIVERY");
			SEQUENCE_ID_OF_LAST_ROUTE_ITEM = savedInstanceState.getInt("SEQUENCE_ID_OF_LAST_ROUTE_ITEM");
			SEQUENCE_ID_OF_LAST_DELIVERY = savedInstanceState.getInt("SEQUENCE_ID_OF_LAST_DELIVERY");
			SEQUENCE_ID_INTERVAL = savedInstanceState.getInt("SEQUENCE_ID_INTERVAL");

			LAST_DELIVERY_RECORD_ID = savedInstanceState.getInt("LAST_DELIVERY_RECORD_ID");
			NEXT_DELIVERY_RECORD_ID = savedInstanceState.getInt("NEXT_DELIVERY_RECORD_ID");
			DELIVERY_LIST_LIMIT = savedInstanceState.getInt("DELIVERY_LIST_LIMIT");
			CURRENT_DELIVERY_LIST_SIZE = savedInstanceState.getInt("CURRENT_DELIVERY_LIST_SIZE");
			CURRENT_DELIVERY_LIST_PAGE = savedInstanceState.getInt("CURRENT_DELIVERY_LIST_PAGE");
		}

		//=== LOAD THE ROUTE DETAILS FOR THE LAST ROUTE LEFT UNFINISHED IF IT MATCHES THIS ROUTE ID
		Bundle extras = getActivity().getIntent().getExtras();
		mJobDetailId = savedInstanceState != null ? savedInstanceState.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0; // required
		if(mJobDetailId == 0){
			mJobDetailId = extras != null ? extras.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0;
		}

		DBHelper.getInstance().setLastJobDetailIdUsed_Common(mJobDetailId);

		mRoute = DBHelper.getInstance().fetchRoute_Common(mJobDetailId);

		useGoogleMaps = DBHelper.getInstance().getUseMaps_Common();
		useListTextToSpeech = DBHelper.getInstance().getUseSpeech_Common();

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

		AudioAttributes audioAttributes = new AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.build();

		soundPool = new SoundPool.Builder()
				.setMaxStreams(1)
				.setAudioAttributes(audioAttributes)
				.build();

		sound1 = soundPool.load(this.getActivity().getApplicationContext(), R.raw.chime3, 1);
	}

	@Override
	public void onStop(){
		logger.info("===--->>> onStop() : ENTER");
		//logger.debug("%%%% mRouteDetailCache size = " + mRouteDetailCache.getCurRouteMap().values().size());

		super.onStop();
	}

	@Override
	public void onPause(){
		super.onPause();

		logger.info("===--->>> onPause() : ENTER : isInPauseMode = " + isInPauseMode);
		//logger.debug("%%%% mRouteDetailCache size = " + mRouteDetailCache.getCurRouteMap().values().size());
	}

	@Override
	public void onResume(){
		logger.info("===--->>> onResume() : ENTER");
		//logger.debug("%%%% mRouteDetailCache size = " + (mRouteDetailCache == null ? "NULL" : mRouteDetailCache.getCurRouteMap().values().size()+""));

		try{
			useGoogleMaps = DBHelper.getInstance().getUseMaps_Common();

			useListTextToSpeech = DBHelper.getInstance().getUseSpeech_Common();

			isProcessingDelivery = false;

			if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
				if(isInPauseMode){
					voiceAnnouncements.add("Delivery pause is on.");
					new Handler().postDelayed(new Runnable(){
						@Override
						public void run(){
							playVoiceMessagesInQueue();
						}
					}, 1000);
				}
			}
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}

		super.onResume();
	}

	@Override
	public void onStart(){
		super.onStart();
		logger.info("===--->>> onStart() : ENTER");

		//=== IF IN SOLO SCREEN MODE
		int orientation = this.getResources().getConfiguration().orientation;
		logger.debug(">>>>onCreate() : orientation " + (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? "PORTRAIT" : (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "UNKNOWN")));

		if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
			if(orientation == Configuration.ORIENTATION_PORTRAIT){
				RouteDetailsRightSideFragmentMerged.isInPauseMode = RouteDetailsRightSideFragmentMerged.saveIsPaused;

				if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
					pauseButton.setActivated(false);
					pauseButton.setPressed(false);
					pauseButton.setEnabled(true);
				}

				//=== navButtonIcon never set for RENUMBERING
				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
					navButtonIcon.setVisibility(View.VISIBLE);
					navButtonIcon.setEnabled(isInPauseMode);
				}

				pausedChanged();
			}
			else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
					navButtonIcon.setVisibility(View.GONE);
				}
				isInPauseMode = false;
				navButtonIcon.setEnabled(false);
				pausedChanged();
			}
		}
		else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
			isInPauseMode = false;

			if(orientation == Configuration.ORIENTATION_PORTRAIT){
				pausedChanged();
			}
			else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
				//=== SHOULD NOT BE HERE IN RANDOM ROUTE MODE
				pausedChanged();
			}
		}
		else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
			isInPauseMode = false;

			if(orientation == Configuration.ORIENTATION_PORTRAIT){
				pausedChanged();
			}
			else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
				//=== SHOULD NOT BE HERE IN RANDOM ROUTE MODE
				pausedChanged();
			}
		}

		ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();
		if(!connectionState.isConnected){
			logger.info("****" + connectionState.descriptiveText + "****");

			navButtonIcon.setVisibility(View.GONE);
			final AlertDialog.Builder dialog = new AlertDialog.Builder(this.getActivity());
			dialog.setTitle("Navigation is Unavailable").setMessage("Internet connect is required. The navigation button is not available.")
				  .setPositiveButton(getResources().getString(R.string.dialogOk), new DialogInterface.OnClickListener(){
					  @Override
					  public void onClick(DialogInterface paramDialogInterface, int paramInt){
						  //=== DO NOTHING;
					  }
				  });
			dialog.show();
		}
	}

	@Override
	public void onDestroy(){
		logger.info("===--->>> onDestroy() : ENTER");
		//logger.debug("%%%% mRouteDetailCache size = " + mRouteDetailCache.getCurRouteMap().values().size());

		soundPool.release();
		soundPool = null;

		useGoogleMaps = false;
		useListTextToSpeech = false;

		try{
			if(gpsReceiver != null){
				getActivity().unregisterReceiver(gpsReceiver);
			}
			if(pauseReceiver != null){
				getActivity().unregisterReceiver(pauseReceiver);
			}
			if(skippedDropReceiver != null){
				getActivity().unregisterReceiver(skippedDropReceiver);
			}
		}
		catch(Exception e){
			logger.error("Exception", e);
		}

		super.onDestroy();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		logger.info("===--->>> onViewCreated() : ENTER");

		navSequenceAlertButton = this.getActivity().findViewById(R.id.navigationAlertButton);
		navSequenceAlertButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				navSequenceAlertPanel.setVisibility(View.GONE);
			}
		});

		navSequenceAlertPanel = this.getActivity().findViewById(R.id.navSequenceAlertPanel);
		navSequenceAlertPanel.setVisibility(View.GONE);

		onViewCreatedContinue(savedInstanceState);
	}

	private void onViewCreatedContinue(Bundle savedState){
		logger.info("===--->>> onViewCreatedContinue() : ENTER");
		currentSavedState = savedState;

		if(useGoogleMaps){
			switch(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity())){
				case ConnectionResult.SUCCESS:
					final MapView googleMapView = googleMapPage.findViewById(R.id.map);
					googleMapView.onCreate(savedState);

					//=== Gets to GoogleMap from the MapView and does initialization stuff
					if(googleMapView != null){
						googleMapView.getMapAsync(new OnMapReadyCallback(){
							@Override
							public void onMapReady(GoogleMap googleMapIn){
								if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
								   != PackageManager.PERMISSION_GRANTED){
									//=== Check Permissions Now
									//=== this was done in HomeFragment so alert user and
									//=== go back to HomeFragment
									Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.deviceFeatureAccessLocationNotGranted), Toast.LENGTH_LONG).show();
									logger.debug("map access not granted");
									getActivity().finish();
								}
								else{
									//=== permission has been granted, continue as usual
									googleMap = googleMapIn;

									try{
										int mapInitResult = MapsInitializer.initialize(getActivity(), MapsInitializer.Renderer.LATEST,
																					   new OnMapsSdkInitializedCallback(){
																						   @Override
																						   public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer){
																							   logger.debug("TAG", "onMapsSdkInitialized: ");
																						   }
																					   });
										if(mapInitResult != 0){
											Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.deviceFeatureAccessMapNotInitialized), Toast.LENGTH_LONG).show();
											logger.debug(getResources().getString(R.string.deviceFeatureAccessMapNotInitialized));
										}
									}
									catch(Exception e){
										logger.error("EXCEPTION", e);
										Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.deviceFeatureAccessMapNotInitialized), Toast.LENGTH_LONG).show();
									}

									googleMap.setMyLocationEnabled(true);

									Location lastKnownLocation = CTApp.getLocation();
									if(lastKnownLocation != null){
										CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 15);
										googleMap.animateCamera(cameraUpdate);
									}

									googleMap.setMinZoomPreference(16);
									UiSettings uiSettings = googleMap.getUiSettings();
									uiSettings.setMyLocationButtonEnabled(true);
									uiSettings.setMapToolbarEnabled(true);
									uiSettings.setCompassEnabled(true);
									uiSettings.setZoomControlsEnabled(true);

									googleMapView.onResume();

									//=== INITIAL DISPLAY OF DROPS ON GOOGLE MAP
									updateGoogleMap(lastKnownLocation, mDataDisplayList);
								}
							}
						});
					}
					break;
				case ConnectionResult.SERVICE_MISSING:
					Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.googlePlayServicesMissingService), Toast.LENGTH_SHORT).show();
					logger.debug(getResources().getString(R.string.googlePlayServicesMissingService));

					getActivity().finish();
					break;
				case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
					Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.googlePlayServicesUpdateRequired), Toast.LENGTH_SHORT).show();
					logger.debug("Google Maps Service Update required");

					getActivity().finish();

					final AlertDialog.Builder dialog = new AlertDialog.Builder(this.getActivity());
					dialog.setMessage("Please update google play services to proceed.")
						  .setPositiveButton("Update", new DialogInterface.OnClickListener(){
							  @Override
							  public void onClick(DialogInterface paramDialogInterface, int paramInt){
								  //PACKAGE NAME OF GOOGLE PLAY SERVICES
								  final String appPackageName = "com.google.android.gms";
								  try{
									  //=== IT WILL OPEN IN GOOGLE PLAY STORE
									  RouteDetailsRightSideFragmentMerged.this.getActivity().finish();
									  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
								  }
								  catch(android.content.ActivityNotFoundException anfe){
									  //=== IF GOOGLE PLAY STORE APP NOT FOUND IN DEVICE IT WILL OPEN IN WEB BROWSER
									  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
								  }
							  }
						  })
						  .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
							  @Override
							  public void onClick(DialogInterface paramDialogInterface, int paramInt){
								  Toast.makeText(getActivity(), getResources().getString(R.string.googlePlayServicesMissingService), Toast.LENGTH_SHORT).show();
								  RouteDetailsRightSideFragmentMerged.this.getActivity().finish();
							  }
						  });
					dialog.show();

					break;
				default:
					Toast.makeText(CTApp.getCustomAppContext(), GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(CTApp.getCustomAppContext()), Toast.LENGTH_SHORT).show();
			}
		}

		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
			if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
				continueSetupSequenced(currentSavedState);
			}
			else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
				continueSetupRandom(currentSavedState);
			}
			else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
				continueSetupRandom(currentSavedState);
			}
		}
		//=== WHEN IN RENUMBERING MODE ALL ROUTES GO TO continueSetupRandom()
		else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
			if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
				continueSetupRandom(currentSavedState);
			}
			else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
				logger.debug("RENUMBERING/RANDOM NOT ALLOWED");
			}
			else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
				continueSetupRandom(currentSavedState);
			}
		}
		//=== WHEN IN SEQUENCING MODE ALL ROUTES GO TO continueSetupRandom()
		else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
			if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
				continueSetupRandom(currentSavedState);
			}
			else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
				logger.debug("SEQUENCING/RANDOM NOT ALLOWED");
			}
			else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
				continueSetupRandom(currentSavedState);
			}
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		logger.info("===--->>> onCreateView() : ENTER");
		routeDetailsRightSideView = inflater.inflate(R.layout.routedetails_rightside, container, false);

		//=== THIS LAYOUT HAS BOTH RANDOM AND SEQUENCED UI ITEMS
		routeDetailsRightSideViewPager = routeDetailsRightSideView.findViewById(R.id.pager);

		if(useGoogleMaps){
			googleMapPage = (RelativeLayout) routeDetailsRightSideViewPagerAdapter.instantiateItem(routeDetailsRightSideView, 1);
			routeDetailsRightSideViewPagerAdapter.setCount(2);
		}
		else{
			routeDetailsRightSideViewPagerAdapter.setCount(1);
		}

		routeDetailsRightSideViewPager.setAdapter(routeDetailsRightSideViewPagerAdapter);
		routeDetailsRightSideViewListPage = (LinearLayout) routeDetailsRightSideViewPagerAdapter.instantiateItem(routeDetailsRightSideView, 0);

		return routeDetailsRightSideView;
	}

	/*
	 * UpdateList can be called from the search form on the right side
	 */
	public void updateListBySearch(String searchFilter, int deliveryMode){
		mDeliveryMode = deliveryMode;
		mSearchValStr = searchFilter;

		//=== show all because it's from a filter...
		Route r = DBHelper.getInstance().fetchRouteForSearch(mJobDetailId, true, mDeliveryMode, mSearchValStr, mStreetSummaryHashMap);

		Location loc = CTApp.getLocation();
		processSearchList(r, loc);
		refreshScreenHandler.sendEmptyMessage(0);
	}

	private void processSearchList(Route r, Location loc){
		mDataDisplayList.clear();

		HashMap<Integer, Integer> remMap = DBHelper.getInstance().fetchRemainingByJobDetailIdGroupByStreetSummary_Random(r.getJobDetailId());
		int count = r.getStreetSummaries().size();

		try{
			mClosestItem = new DeliveryItem();
			mClosestItem.setId(-1);
			mClosestItem.setDistance(100000000);
		}
		catch(Exception e){
			logger.debug(TAG, "Unable to initialize closest item");
		}

		for(int i = 0; i < count; i++){
			StreetSummaryRandom s = r.getStreetSummaries().get(i);
			logger.debug(TAG, "Processing Street Summay:" + s.getStreetName());

			DeliveryItem rd = new DeliveryItem();
			rd.setStatus(GlobalConstants.ROUTE_DETAIL_STATUS_STREETSUMMARY);
			int dnd = 0;
			int remaining = 0;
			int cSrv = 0;

			if(mDoNotDeliverMap.containsKey(s.getSummaryId())){
				dnd = mDoNotDeliverMap.get(s.getSummaryId());
			}

			if(mCustSrvMap.containsKey(s.getSummaryId())){
				cSrv = mCustSrvMap.get(s.getSummaryId());
			}

			if(remMap.containsKey(s.getSummaryId())){
				remaining = remMap.get(s.getSummaryId());
			}

			rd.setNumCustSvc(cSrv);
			rd.setNumDND(dnd);
			rd.setQuantity(remaining);

			String str = s.getStreetName();

			try{
				str = str.replaceFirst("0 ", "");
				if(str.endsWith("0")){
					int len = str.length();
					str = str.substring(0, len - 1);
				}
				str = str.replaceFirst(" 0", " ");
			}
			catch(Exception e){
			}
			rd.setGpsLocationAddressStreet(str);

			mDataDisplayList.add(rd);
			ArrayList<DeliveryItem> rds = s.getRouteDetails();
			int countS = rds.size();

			for(int j = 0; j < countS; j++){
				boolean addItem = true;

				DeliveryItem rd1 = rds.get(j);
				Location locDet = GPSUtils.getLocationByGeoCode(rd1.getGpsLocationLatitude() + "," + rd1.getGpsLocationLongitude());

				if(addItem){

					logger.debug(TAG, "Distance is: " + loc.distanceTo(locDet));
					rd1.setDistance(loc.distanceTo(locDet));

					mDataDisplayList.add(rd1);
				}
			}
		}

		Collections.sort(mDataDisplayList);
	}

	public void notifyDeliveryDataChanged(){
		refreshScreenHandler.handleMessage(null);
	}

	void updateGoogleMap(Location locationOfDevice, ArrayList<DeliveryItem> routeDetailsMapPoints){
		//=== NEED TO RUN THIS ON THE ui THREAD
		if(routeDetailsMapPoints != null && routeDetailsMapPoints.size() > 0){
			getActivity().runOnUiThread(
					new Runnable(){
						@Override
						public void run(){
							logger.debug("UPDATING GOOGLE MAP PINS : list size = " + routeDetailsMapPoints.size());
							try{
								//=== CRASHLYTICS BUG HERE 2.045
								googleMap.clear();

								for(int index = 0; index < routeDetailsMapPoints.size(); index++){
									//=== CRASHLYTICS BUG REPORTED HERE
									DeliveryItem item = routeDetailsMapPoints.get(index);

									//logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
									if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
										setMapPoint(item.getGpsLocationLatitude(),
													item.getGpsLocationLongitude(),
													colorizeMapPinForDelivery(item));
									}
									else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
										setMapPoint(item.getGpsLocationLatitude(),
													item.getGpsLocationLongitude(),
													colorizeMapPinForSequencing(item));
									}
									else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
										setMapPoint(item.getGpsLocationLatitude(),
													item.getGpsLocationLongitude(),
													colorizeMapPinForSequencing(item));
									}
								}

								LatLng mapCenter = new LatLng(locationOfDevice.getLatitude(), locationOfDevice.getLongitude());
								float bearing = locationOfDevice.getBearing();

								//=== THIS WILL ELIMINATE THE ROTATING OF THE MAP IF NOT MOVING
								if(mGPSSpeed < 0.5){
									bearing = lastBearing;
								}

								CameraPosition newCamPos = new CameraPosition(mapCenter,
																			  googleMap.getCameraPosition().zoom,
																			  googleMap.getCameraPosition().tilt,
																			  bearing);
								googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos), 1500, null);
							}
							catch(Exception e){
								logger.error("EXCEPTION : " + e.getMessage());
							}
						}
					}
									   );
		}
	}

	@SuppressLint("RestrictedApi")
	private void updateTopNav(){
		final AppCompatRadioButton mGpsStatusRadioButton;

		Activity thisActivity = getActivity();
		if(thisActivity != null){
			mGpsStatusRadioButton = thisActivity.findViewById(R.id.gpsRadioButton2);

			if(mGpsStatusRadioButton != null){
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
				mGpsStatusRadioButton.setChecked(DeviceLocationListener.getHasGPsFix());
//				logger.debug("SET GPS BUTTON TO (DeviceLocationListener.isGPSFix) = " + DeviceLocationListener.isGPSFix);

				//=== LETS USER CLICK TO TURN ON GPS
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
		}
	}

	//=== Sets Image Based on status of delivery and
	private void setMapPoint(double lat, double lon, int image){
		switch(image){
			case 0:
				image = R.drawable.location;
				break;
			case 1:
				image = R.drawable.map_marker_vip_do_not_deliver;
				break;
			case 2:
				image = R.drawable.map_marker_subscriber;
				break;
			case 3:
				image = R.drawable.map_marker_do_not_deliver;
				break;
			case 4:
				image = R.drawable.map_marker_cannot_deliver;
				break;
			case 5:
				image = R.drawable.map_marker_must_deliver;
				break;
			case 6:
				image = R.drawable.map_marker_deliver;
				break;
			case 7:
				image = R.drawable.map_marker_invalid_address;
				break;
			case 8:
				image = R.drawable.map_marker_delivered;
				break;
		}

		googleMap.addMarker(new MarkerOptions()
									.icon(BitmapDescriptorFactory.fromResource(image))
									.anchor(0.5f, 0.5f) // Anchor market bottom center
									.position(new LatLng(lat, lon)));
	}

	//=== FOR SEQUENCING ONLY
	private int colorizeMapPinForSequencing(DeliveryItem rd){
		int pinResult = 8;

		if(rd.getSequenceNew() > 0){
			pinResult = 6;
		}

		return pinResult;
	}

	//=== Checks and Returns proper pins for googleMap
	private int colorizeMapPinForDelivery(DeliveryItem rd){
		int pinType = 0;

		if(rd.getJobType() == GlobalConstants.JOB_TYPE.Delivery.ordinal()){//6
			pinType = 6;
		}
		else if(rd.getJobType() == GlobalConstants.JOB_TYPE.Must_Deliver.ordinal()){//5
			pinType = 5;
		}
		else if(rd.getJobType() == GlobalConstants.JOB_TYPE.Cannot_Deliver.ordinal()){//4
			pinType = 4;
		}
		else if(rd.getJobType() == GlobalConstants.JOB_TYPE.Do_Not_Deliver.ordinal()){//3
			pinType = 3;
		}
		else if(rd.getJobType() == GlobalConstants.JOB_TYPE.Subscriber.ordinal()){//2
			pinType = 2;
		}
		else if(rd.getJobType() == GlobalConstants.JOB_TYPE.VIP_Do_Not_Deliver.ordinal()){//1
			pinType = 1;
		}

		if(rd.getIsInvalidAddress()){
			pinType = 7;
		}

		return pinType;
	}

	public boolean forceReconciliationForDrop(long mDeliveryId, long gmtTimestamp, double latitude, double longitude){
		boolean success = false;
		DeliveryItem rd = null;

		int index = 0;
		for(index = 0; index < mDataDisplayList.size(); index++){
			DeliveryItem test = mDataDisplayList.get(index);
			if(test.getDeliveryId() == mDeliveryId){
				rd = test;

				rd.setDelivered(1);
				rd.setGpsLocationLatitude(latitude);
				rd.setGpsLocationLongitude(longitude);
				rd.setWasReconciled(1);
				rd.setDeliveredTime(gmtTimestamp);

				getActivity().runOnUiThread(
						new Runnable(){
							@Override
							public void run(){
								logger.debug("$$$MONEY : forceReconciliationForDrop() calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
								mRouteDetailListAdapterCommon.notifyDataSetChanged();
							}
						}
										   );

				success = true;
				break;
			}
		}

		return success;
	}

	public void pausedChanged(){
		this.getActivity().runOnUiThread(
				new Runnable(){
					@Override
					public void run(){
						if(((RouteDetailsActivity) RouteDetailsRightSideFragmentMerged.this.getActivity()).routeType.equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
							resetDeliveryOperationsButtons();
						}
					}
				});
	}

	private void displayUnresolvedAddressDialog(String titleStr, int unresolvedAddressCount){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.app_usage_disclaimer_dialog, getActivity().findViewById(R.id.layout_root));

		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText(titleStr);

		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);

		final TextView disclaimer = layout.findViewById(R.id.disclaimerTextView);
		String disclaimerText = "\nThis route has " + unresolvedAddressCount + " unresolved delivery points.\n\n" +
								"It is highly likely that the street name and/or street address was entered incorrectly. " +
								"You can identify these addresses in the delivery list as the one(s) with a yellow background. " +
								"You may have to page through the entire list to find each unresolved address delivery point.\n\n" +
								"You have two actions you can take to resolve the address(es): \n" +
								"1. You can select the tools button for an unresolved address delivery point and request a location change or\n" +
								"2. You can contact your route administrator to correct the address delivery point in question.\n\n" +
								"NOTE: You can also choose to 'reconcile' (i.e. mark as delivered) an unresolved address delivery point using the tools option.\n\n" +
								"NOTE: THE UNRESOLVED ADDRESS POINTS WILL NOT BE INCLUDED IN THE TURN BY TURN NAVIGATION AS THEY DO NOT HAVE VALID GEO COORDINATES.";
		disclaimer.setText(disclaimerText);
		disclaimer.setTextColor(Color.BLACK);

		builder.setView(layout);
		builder.setPositiveButton("Continue", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt){
				paramDialogInterface.dismiss();
			}
		});

		AlertDialog alertDialog = builder.create();

		alertDialog.show();
		//=== Change the alert dialog background color
		alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

		//=== Get the alert dialog buttons reference
		Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

		//=== Change the alert dialog buttons text and background color
		positiveButton.setTextColor(Color.BLACK);
		positiveButton.setBackgroundColor(Color.CYAN);
	}

	//==============================================================
	//=== DIVERGENT METHODS BETWEEN RANDOM AND SEQUENCED MODE
	//==============================================================

	protected String getJobTypeText(int jobType){
		String text = "";

		if(jobType == GlobalConstants.JOB_TYPE.Delivery.ordinal()){
			text = "delivery";
		}
		else if(jobType == GlobalConstants.JOB_TYPE.Must_Deliver.ordinal()){
			text = "must deliver";
		}
		else if(jobType == GlobalConstants.JOB_TYPE.Cannot_Deliver.ordinal()){
			text = "cannot deliver";
		}
		else if(jobType == GlobalConstants.JOB_TYPE.Do_Not_Deliver.ordinal()){
			text = "do not deliver";
		}
		else if(jobType == GlobalConstants.JOB_TYPE.Subscriber.ordinal()){
			text = "subscriber";
		}
		else if(jobType == GlobalConstants.JOB_TYPE.VIP_Do_Not_Deliver.ordinal()){
			text = "vip do not deliver";
		}

		return text;
	}

	protected void createDisplayListContentsWithStreetSummaryRandom(Location deviceLoc){
		logger.debug("ENTER");
		//logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());

		try{
			//=== CREATE EXISTING DISPLAY LIST
			if(mDataDisplayList == null){
				mDataDisplayList = new ArrayList<DeliveryItem>();
			}

			//=== GATHER CLOSEST ITEM INFO
			int closestId = -1;
			int closestSummaryId = -1;
			double closestDistance = 1000000;

			//logger.debug("%%%% mRouteDetailCache size = " + mRouteDetailCache.getCurRouteMap().values().size());

			//=== GET THE EXISTING LIST
			Collection<DeliveryItem> currentlyDisplayedRouteDetails = (Collection<DeliveryItem>) mDataDisplayList.clone();
			mDataDisplayList.clear();

			//=== HOLDER FOR NEWLY PROCESSED LIST ITEMS
			ArrayList<DeliveryItem> processedRouteDetails = new ArrayList<DeliveryItem>();

			//=== Loop through the currently displayed items and update distances to determine which is closest
			Iterator<DeliveryItem> currentlyDisplayedRouteDetailsIterator = currentlyDisplayedRouteDetails.iterator();
			while(currentlyDisplayedRouteDetailsIterator.hasNext()){
				DeliveryItem currentlyDisplayedRouteDetail = currentlyDisplayedRouteDetailsIterator.next();

				//=== SEE IF THIS ITEM IS CLOSER
				if(currentlyDisplayedRouteDetail.getDistance() < closestDistance){
					closestId = currentlyDisplayedRouteDetail.getId();
					closestDistance = currentlyDisplayedRouteDetail.getDistance();
					closestSummaryId = currentlyDisplayedRouteDetail.getSummaryId();
				}

				//=== ADD TO THE NEWLY PROCESSED LIST
				processedRouteDetails.add(currentlyDisplayedRouteDetail);
			}

			//=== Sort addresses by summaryId I.E. BY STREET
			Collections.sort(processedRouteDetails, DeliveryItem.ORDERING_SUMMARYID);

			//=== STREET COLLECTION TALLY ITEMS
			HashMap<Integer, DeliveryItem> processedStreetSummaryMap = new HashMap<Integer, DeliveryItem>();

			//=== STREET ITEMS GROUPED PER COLLECTION TALLY ITEM
			HashMap<Integer, ArrayList<DeliveryItem>> processedDeliveryItemsGroupedByStreetMap = new HashMap<Integer, ArrayList<DeliveryItem>>();

			//=== REMAINING DELIVERY ITEMS PER STREET
			HashMap<Integer, Integer> remainingSummaryIdCountForNotDeliveredMap
					= DBHelper.getInstance().fetchRemainingByJobDetailIdGroupByStreetSummary_Random(mRoute.getJobDetailId());

			//=== LOOP THROUGH PROCESSED ADDRESS POINTS
			int processedRouteDetailsSize = processedRouteDetails.size();
			int lastProcessedSummaryId = 0;

			//=== COLLECT DELIVERY ITEMS AND PUT IN ASSOCIATED STREET GROUPING
			for(int processedRouteDetailsListPosition = 0; processedRouteDetailsListPosition < processedRouteDetailsSize; processedRouteDetailsListPosition++){
				DeliveryItem processedRouteDetail = processedRouteDetails.get(processedRouteDetailsListPosition);

				int processedSummaryId = processedRouteDetail.getSummaryId();

				//=== BUILD A NEW SUMMARY ROUTE DETAIL LIST ITEM FOR EVERY NEW SUMMARY ID IN LIST
				if(lastProcessedSummaryId != processedSummaryId){
					DeliveryItem summaryRouteDetailItem =
							buildRouteDetailFromSummaryRandom(processedSummaryId,
															  mCustSrvMap,
															  remainingSummaryIdCountForNotDeliveredMap,
															  mDoNotDeliverMap,
															  processedRouteDetail.getDistance(),
															  deviceLoc);

					lastProcessedSummaryId = processedSummaryId;
					processedStreetSummaryMap.put(processedSummaryId, summaryRouteDetailItem);
				}

				//=== ADD THE ROUTE DETAIL TO THE ASSOCIATED STREET GROUP OR CREATE A NEW STREET GROUP
				//=== processedDeliveryItemsGroupedByStreetMap is a HASHMAP of RouteDetail ARRAYS
				if(processedDeliveryItemsGroupedByStreetMap.containsKey(processedSummaryId)){
					processedDeliveryItemsGroupedByStreetMap.get(processedSummaryId).add(processedRouteDetail);
				}
				else{
					ArrayList<DeliveryItem> newSummaryArrayForRouteDetails = new ArrayList<DeliveryItem>();
					newSummaryArrayForRouteDetails.add(processedRouteDetail);
					processedDeliveryItemsGroupedByStreetMap.put(processedSummaryId, newSummaryArrayForRouteDetails);
				}
			}

			Collection<Integer> deliveryPointGroupedByStreetMapKeySet = processedDeliveryItemsGroupedByStreetMap.keySet();
			Iterator<Integer> deliveryPointGroupedByStreetMapKeySetIterator = deliveryPointGroupedByStreetMapKeySet.iterator();

			ArrayList<DeliveryItem> summaryHeaders = new ArrayList<DeliveryItem>();
			while(deliveryPointGroupedByStreetMapKeySetIterator.hasNext()){
				Integer groupedByStreetMapSummaryIdKey = deliveryPointGroupedByStreetMapKeySetIterator.next();
				ArrayList<DeliveryItem> deliveriesForAStreet = processedDeliveryItemsGroupedByStreetMap.get(groupedByStreetMapSummaryIdKey);

				//=== Loop through and order addresses in processedRouteDetails by distance
				Collections.sort(deliveriesForAStreet, DeliveryItem.ORDERING_DISTANCE_ASC);
				DeliveryItem routeDetailFromProcessedSummaryIdMap = processedStreetSummaryMap.get(groupedByStreetMapSummaryIdKey);

				//=== add processedRouteDetails into position for all the lists
				if(groupedByStreetMapSummaryIdKey != closestSummaryId){
					double d = deliveriesForAStreet.get(0).getDistance();
					routeDetailFromProcessedSummaryIdMap.setDistance(d - .01);
					deliveriesForAStreet.add(0, routeDetailFromProcessedSummaryIdMap);
				}
				else{
					int len = deliveriesForAStreet.size();
					for(int x = 0; x < len; x++){
						int id = deliveriesForAStreet.get(x).getId();

						if(id == closestId){
							double d = deliveriesForAStreet.get(x).getDistance();
							routeDetailFromProcessedSummaryIdMap.setDistance(d - .01);
							deliveriesForAStreet.add(x, routeDetailFromProcessedSummaryIdMap);
							break;
						}
					}
				}

				processedDeliveryItemsGroupedByStreetMap.put(groupedByStreetMapSummaryIdKey, deliveriesForAStreet);
				summaryHeaders.add(routeDetailFromProcessedSummaryIdMap);
			}

			//== find closest address and set position
			Collections.sort(summaryHeaders, DeliveryItem.ORDERING_DISTANCE_ASC);
			for(int s = 0; s < summaryHeaders.size(); s++){
				int sHId = summaryHeaders.get(s).getSummaryId();

				//logger.debug("adding addresses for: " + processedDeliveryItemsGroupedByStreetMap.get(sHId));

				mDataDisplayList.addAll(processedDeliveryItemsGroupedByStreetMap.get(sHId));
			}

			if(!mDataDisplayList.isEmpty()){
				int len = mDataDisplayList.size();
				for(int i = 0; i < len; i++){
					//logger.debug(">>>>createDisplayListContentsNew() : " + len + " Looking for closest id: "
					//		+ closestId + " Comparing to: "
					//		+ mDataDisplayList.get(i).getId());

					if(closestId == mDataDisplayList.get(i).getId()){
						//logger.debug(">>>>createDisplayListContentsNew() : closest id Found!!! at " + i);

						mListPositionOfClosestItem = i - 2;
						break;
					}
				}

				if(mListPositionOfClosestItem < 0){
					mListPositionOfClosestItem = 0;
				}
			}

			//=== TEXT TO SPEECH
			if(useListTextToSpeech){
				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
					if(mDataDisplayList.size() > 0){
						DeliveryItem closestDrop = mDataDisplayList.get(mListPositionOfClosestItem);

						if(closestDrop.getStatus() == GlobalConstants.ROUTE_DETAIL_STATUS_STREETSUMMARY){
							if(mDataDisplayList.size() > 1){
								closestDrop = mDataDisplayList.get(mListPositionOfClosestItem + 1);
							}
							else{
								closestDrop = null;
							}
						}

						if(closestDrop != null && closestDrop.getDistance() <= 40){//32){
							int jobType = closestDrop.getJobType();
							//=== IF NOT DELIVERY TYPE
							if(jobType != GlobalConstants.JOB_TYPE.Delivery.ordinal()){
								//=== IF NOT DELIVERED
								//if(closestDrop.getDelivered() != 1){
								String jobTypeText = getJobTypeText(jobType);
								int feet = (int) (closestDrop.getDistance() * 3.281);
//========================== MAY NEED TO PUT THIS BACK IN============================================
//								final String toSpeak = feet + " feet to " + jobTypeText + " at " + closestDrop.getGpsLocationAddressNumber() + " " + closestDrop.getGpsLocationAddressStreet();
//								voiceAnnouncements.add(toSpeak);
//								playVoiceMessagesInQueue();
							}
						}
					}
				}
			}
		}
		catch(Exception e){
			logger.debug("EXCEPTION", e);
		}

		//logger.debug("EXIT");
	}

	//=== SEQUENCED
	@Override
	public void onLowMemory(){
		super.onLowMemory();
	}

	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);

		outState.putInt("GREATEST_SEQUENCE_ID_PROCESSED", GREATEST_SEQUENCE_ID_PROCESSED);
		outState.putInt("SEQUENCE_ID_OF_LAST_ROUTE_ITEM", SEQUENCE_ID_OF_LAST_ROUTE_ITEM);
		outState.putInt("SEQUENCE_ID_OF_LAST_LIST_ITEM", SEQUENCE_ID_OF_LAST_LIST_ITEM);
		outState.putInt("SEQUENCE_ID_OF_NEXT_DELIVERY", SEQUENCE_ID_OF_NEXT_DELIVERY);
		outState.putInt("SEQUENCE_ID_OF_LAST_ROUTE_ITEM", SEQUENCE_ID_OF_LAST_ROUTE_ITEM);
		outState.putInt("SEQUENCE_ID_OF_LAST_DELIVERY", SEQUENCE_ID_OF_LAST_DELIVERY);
		outState.putInt("SEQUENCE_ID_INTERVAL", SEQUENCE_ID_INTERVAL);
		outState.putInt("LAST_DELIVERY_RECORD_ID", LAST_DELIVERY_RECORD_ID);
		outState.putInt("NEXT_DELIVERY_RECORD_ID", NEXT_DELIVERY_RECORD_ID);
		outState.putInt("DELIVERY_LIST_LIMIT", DELIVERY_LIST_LIMIT);
		outState.putInt("CURRENT_DELIVERY_LIST_SIZE", CURRENT_DELIVERY_LIST_SIZE);
		outState.putInt("CURRENT_DELIVERY_LIST_PAGE", CURRENT_DELIVERY_LIST_PAGE);
	}

	public boolean resetStartingPointHard(int sequenceId){
		logger.debug("ENTER");
		boolean didReset = false;
		for(int index = 0; index < mDataDisplayList.size(); index++){
			mDataDisplayList.get(index).setStartingPoint(false);

			if(mDataDisplayList.get(index).getSequence() == sequenceId){
				mDataDisplayList.get(index).setStartingPoint(true);
				SEQUENCE_ID_OF_LAST_DELIVERY = sequenceId;
				LAST_DELIVERY_RECORD_ID = mDataDisplayList.get(index).getId();
				didReset = true;

				logger.debug("RESET SEQUENCE ID TO " + SEQUENCE_ID_OF_LAST_DELIVERY);
			}
		}

		return didReset;
	}

	protected int determineListPositionOfLastDelivery(int lastDeliveryRecId){
		int listPositionOfLastDelivery = -1;

		//logger.debug("ENTER LAST POINT DELIVERED lastDeliveryRecId = " + lastDeliveryRecId);
		if(!mDataDisplayList.isEmpty()){
			//logger.debug("ENTER LAST POINT DELIVERED mDataDisplayList not empty");
			listPositionOfLastDelivery = -1;

			int len = mDataDisplayList.size();
			for(int i = 0; i < len; i++){
				if(lastDeliveryRecId == mDataDisplayList.get(i).getId()){
					//=== +2 TO MOVE THIS ITEM CLOSER TO THE SCREEN CENTER
					listPositionOfLastDelivery = i + 2;
					break;
				}
			}

			//logger.debug("ENTER LAST POINT DELIVERED listPositionOfLastDelivery = " + listPositionOfLastDelivery);
			if(listPositionOfLastDelivery < 0){
				//logger.debug("ENTER LAST POINT DELIVERED listPositionOfLastDelivery < 0 " + listPositionOfLastDelivery);
				listPositionOfLastDelivery = 0;
			}
			else if(listPositionOfLastDelivery >= CURRENT_DELIVERY_LIST_SIZE){
				//logger.debug("ENTER LAST POINT DELIVERED listPositionOfLastDelivery  >= CURRENT_DELIVERY_LIST_SIZE(" + CURRENT_DELIVERY_LIST_SIZE + ") " + listPositionOfLastDelivery);
				listPositionOfLastDelivery = CURRENT_DELIVERY_LIST_SIZE - 1;
			}
		}
		else{
			logger.debug("ENTER LAST POINT DELIVERED mDataDisplayList EMPTY");
		}

		logger.debug("EXIT LAST POINT DELIVERED list index (listPositionOfLastDelivery) = " + listPositionOfLastDelivery);
		return listPositionOfLastDelivery;
	}

	protected void announceDeliverySequenced(ArrayList<DeliveryItem> addressesInDeliveryArea){
		logger.info("$$$$$ useTextToSpeech = " + useListTextToSpeech);
		logger.info("$$$$$ textToSpeech.isSpeaking() = " + textToSpeech.isSpeaking());
		logger.info("$$$$$ mDataDisplayList.size()=" + mDataDisplayList.size());

		//logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());

		if(addressesInDeliveryArea == null || addressesInDeliveryArea.size() <= 0){
			logger.info("NO DROPS TO MAKE ANNOUNCEMENTS ON");
		}
		else{
			if(useListTextToSpeech){
				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
					for(int x = 0; x < addressesInDeliveryArea.size(); x++){
						DeliveryItem nextDrop = addressesInDeliveryArea.get(x);
						if(nextDrop != null){// && nextDrop.getDistance() <= adjustedDistance){
							int jobType = nextDrop.getJobType();
							if(jobType >= GlobalConstants.JOB_TYPE.Must_Deliver.ordinal()){    //=== ONLY ANNOUNCE DELIVERY JOB TYPES 5 AND 6
								logger.info("$$$$$ nextDrop.getDistance() = " + nextDrop.getDistance());

								//logger.info("$$$$$ closestDrop distance=" + closestDrop.getDistance());

								String jobTypeText = getJobTypeText(jobType);
								int feet = (int) (nextDrop.getDistance() * 3.281);
								String streetAddress = nextDrop.getGpsLocationAddressNumber() + " " + nextDrop.getGpsLocationAddressStreet();
								String customerInstructions = (!nextDrop.getNotes().isEmpty() ? "\u00A0" + nextDrop.getNotes() : "");

								String productList = "";
								LinkedHashMap<Integer, DeliveryItemProduct> products = nextDrop.getProducts();
								Collection<DeliveryItemProduct> products2 = products.values();

								DeliveryItemProduct[] products3 = new DeliveryItemProduct[0];
								products3 = products2.toArray(products3);
								if(products3 != null && products3.length > 0){
									productList = " ";
									for(int index = 0; index < products3.length; index++){
										productList += products3[index].getQuantity() + " " + products3[index].getProductCode() + " and ";
									}
									productList = productList.substring(0, productList.length() - 4);
								}

								String toSpeak = feet + " feet to " +
												 jobTypeText +
												 " " +
												 streetAddress +
												 productList +
												 customerInstructions;

								logger.info("$$$$$ announceDeliverySequenced() : adding = " + toSpeak);
								voiceAnnouncements.add(toSpeak);
							}
						}
					}

					playVoiceMessagesInQueue();
				}
			}
		}
	}

	private void playVoiceMessagesInQueue(){
		AsyncTask.execute(new Runnable(){
			@Override
			public void run(){
				while(voiceAnnouncements.size() > 0){
					String announcement = voiceAnnouncements.get(0);

					if(announcement.contains("\u00A0")){
						announcement.replace("\u00A0", "");
					}

					if(!textToSpeech.isSpeaking()){

						//=== SOUND ALERT BELL IF CUSTOMER INSTRUCTIONS ARE PRESENT
						try{
							soundPool.play(sound1, 1, 1, 0, 0, 1);
						}
						catch(Exception e){
							e.printStackTrace();
						}

						voiceAnnouncements.remove(0);
						textToSpeech.speak(announcement, TextToSpeech.QUEUE_ADD, null, null);
					}
//logger.debug("playVoiceMessagesInQueue() : message = " + announcement);
				}
			}
		});

	}

	public void updateListFromHandlerSequenced(final String sFilter, final int deliveryQuadsMode, boolean forceUpdate){
//		logger.debug("updateListFromHandlerSequenced() : ENTER");
//		logger.debug("\n++++++SEQUENCED DELIVERY " + (isInPauseMode ? "PAUSED " : "NOT PAUSED ") +
//		"AND FORCED UPDATE " + (forceUpdate ? "ON" : "OFF\n"));

		Activity thisActivity = getActivity();

		if(thisActivity == null){
			//=== THIS IS CAUSING NULL OBJECT EXCEPTIONS
			//=== APPARENTLY BEING CALLED BEFORE FRAGMENT MANAGER IS FULLY INITIALIZED
			logger.debug("\n++++++ACTIVITY IS NULL so SKIP UPDATE");
		}
		else{
			if(isInPauseMode && !forceUpdate){
				flashPauseButton = !flashPauseButton;
//				logger.debug("\n++++++SEQUENCED DELIVERY PAUSED and NOT FORCED UPDATE so blink the pause button");

				getActivity().runOnUiThread(
						new Runnable(){
							@Override
							public void run(){
								boolean flashIt = !flashPauseButton;

								setPauseButtonsEnabled(flashIt);
							}
						});
			}
			else{
				//=== THIS ONLY UPDATES THE DISPLAY IF WE ARE NOT CURRENTLY PROCESSING GPS ACTIVITY?????
//				logger.debug("++++++PROCESSING sequenced GPS IS " + (!isProcessingDelivery ? "HAPPENING" : "NOT HAPPENING so skipping"));

				if(isProcessingDelivery){
					logger.debug("isProcessingDelivery = TRUE so skipping");
				}
				else{
					logger.debug("isProcessingDelivery = FALSE so processing");

					try{
//						logger.debug("++++++START thread UPDATE of SEQUENCED DELIVERY");
						isProcessingDelivery = true;

						mDeliveryQuads = deliveryQuadsMode;

						Location deviceLoc = CTApp.getLocation();
						deviceLoc.setBearing(mDeviceForwardAzimuth);

						Calendar cal = Calendar.getInstance();
						final long gmtTimestamp = cal.getTimeInMillis() + mTZOffSet;

						//=== GET NEW SET OF ROUTE DETAILS BASED ON TARGET AREA AND PROCESS WITH CURRENT ROUTE MAP
						logger.debug("SEQUENCE_ID_OF_LAST_DELIVERY = " + SEQUENCE_ID_OF_LAST_DELIVERY);
						//logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
						if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
							addressesWithinDeliveryArea =
									DBHelper.getInstance().fetchRouteDetailsWithinDeliveryArea_Sequenced(
											mRoute.getJobDetailId(),
											mDeliveryAreaProjectedFrontDistance,
											mDeliveryAreaProjectedSideDistance,
											mExtendedAreaProjectedFrontDistance,
											mExtendedAreaProjectedSideDistance,
											deliveryQuadsMode,
											deviceLoc,
											mDeviceAzimuth,
											gmtTimestamp,
											SEQUENCE_ID_OF_LAST_DELIVERY,
											SEQUENCE_ID_INTERVAL);
						}
						else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
							//=== THIS ONLY SETS sequencenew and seqmodenew values, it does not determine if delivered
							addressesWithinDeliveryArea =
									//======================================================================================================
									//=== PROBABLY NEED A DIFFERENT DATABASE CALL FOR DBHelper.fetchRouteDetailsWithinDeliveryAreaForSequencing_Random(
									//=== SEQUENCING FOR A SEQUENCED ROUTE
									//======================================================================================================
									DBHelper.getInstance().fetchRouteDetailsWithinDeliveryArea_Sequencing_As_Random(
											mRoute.getJobDetailId(),
											mDeliveryAreaProjectedFrontDistance,
											mDeliveryAreaProjectedSideDistance,
											mRoute.getLookaheadForward(),
											mRoute.getLookaheadSide(),
											mDataDisplayList,
											deliveryQuadsMode,
											deviceLoc,
											mDeviceAzimuth);
						}
						else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
							//=== THIS ONLY SETS sequencenew and seqmodenew values, it does not determine if delivered
							addressesWithinDeliveryArea =
									//======================================================================================================
									//=== PROBABLY NEED A DIFFERENT DATABASE CALL FOR DBHelper.fetchRouteDetailsWithinDeliveryAreaForSequencing_Random(
									//=== SEQUENCING FOR A SEQUENCED ROUTE
									//======================================================================================================
									DBHelper.getInstance().fetchAllRouteDetailsForJobDetailId_Sequenced(mRoute.getJobDetailId());
						}

						if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
							//=== if we tagged some deliveries then update the list
							NEXT_DELIVERY_RECORD_ID = -1;

							if(addressesWithinDeliveryArea == null || addressesWithinDeliveryArea.size() <= 0){
								logger.info("++++++NO DROPS IN LOOKAHEAD - SKIPPING UPDATE");
							}
							else{
								final int selectedIndex = updateDisplayListSequenced(addressesWithinDeliveryArea);

								//logger.info("++++++CALLING determineLogicalLastDeliveryRecordId()");
								NEXT_DELIVERY_RECORD_ID = determineLogicalLastDeliveryRecordId(addressesWithinDeliveryArea, SEQUENCE_ID_OF_LAST_DELIVERY);

								if(NEXT_DELIVERY_RECORD_ID != -1 && NEXT_DELIVERY_RECORD_ID != LAST_DELIVERY_RECORD_ID){
									logger.info("++++++NEXT_DELIVERY_RECORD_ID has changed = " + NEXT_DELIVERY_RECORD_ID);
									SEQUENCE_ID_OF_LAST_DELIVERY = SEQUENCE_ID_OF_NEXT_DELIVERY;
									LAST_DELIVERY_RECORD_ID = NEXT_DELIVERY_RECORD_ID;
									boolean didReset = resetStartingPointSoft(SEQUENCE_ID_OF_LAST_DELIVERY);
								}
								else{
									logger.info("++++++NEXT_DELIVERY_RECORD_ID either -1 or not equal to LAST_DELIVERY_RECORD_ID = " + NEXT_DELIVERY_RECORD_ID);
								}

								int[] sequenceNumbers = new int[addressesWithinDeliveryArea.size()];
								for(int index = 0; index < addressesWithinDeliveryArea.size(); index++){
									logger.debug("SENDING INTENT_UPDATE_NAV_MAP_PINS TO NAV FRAGMENT add sequence # for " + addressesWithinDeliveryArea.get(index).getSequence());
									sequenceNumbers[index] = addressesWithinDeliveryArea.get(index).getSequence();
								}

								logger.debug("SENDING INTENT_UPDATE_NAV_MAP_PINS TO NAV FRAGMENT");
								Intent intentP = new Intent(GlobalConstants.INTENT_UPDATE_NAV_MAP_PINS);
								intentP.putExtra("SEQUENCE_NUMBERS", sequenceNumbers);
								intentP.setPackage(thisActivity.getPackageName());//"com.agilegeodata.carriertrack");
								getActivity().sendBroadcast(intentP);

								getActivity().runOnUiThread(
										new Runnable(){
											@Override
											public void run(){
												if(mRouteDetailListAdapterCommon != null){
													logger.info("++++++NOTIFYING LIST ADAPTER OF DATA CHANGE");
													//logger.debug("mDataDisplayList.size(1) = " + mDataDisplayList.size());
													//logger.debug("$$$MONEY : updateListFromHandlerSequenced(1) calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
													mRouteDetailListAdapterCommon.notifyDataSetChanged();
												}
											}
										});

								//=== THIS ONLY UPDATES THE DISPLAY IF WE ARE NOT CURRENTLY PROCESSING GPS ACTIVITY
								//logger.debug("++++++PROCESSING sequenced GPS IS " + (isProcessingDelivery ? "HAPPENING" : "NOT HAPPENING so skipping"));
								if(NEXT_DELIVERY_RECORD_ID == -1){
									logger.info("++++++SKIPPING, NEXT_DELIVERY_RECORD_ID == -1 : SEQUENCE_ID_OF_LAST_DELIVERY = " + SEQUENCE_ID_OF_LAST_DELIVERY);
								}
								else{
									final Location locationOfDevice = deviceLoc;
									final int nextDeliveryRecId2 = NEXT_DELIVERY_RECORD_ID;

									mListPositionOfLastDelivery = determineListPositionOfLastDelivery(nextDeliveryRecId2);

									if(mRouteDetailListAdapterCommon != null){
										//logger.info("++++++NOTIFYING LIST ADAPTER OF DATA CHANGE");
										//logger.debug("mDataDisplayList.size(1) = " + mDataDisplayList.size());

										if(mRouteDetailListView.getCount() > 0){
											//logger.info("++++++SCROLLING TO POSITION " + mListPositionOfLastDelivery);
											//=== NEED TO RUN THIS ON THE ui THREAD
											getActivity().runOnUiThread(
													new Runnable(){
														@Override
														public void run(){
															try{
																//logger.debug("$$$MONEY : updateListFromHandlerSequenced() calling mRouteDetailListView.smoothScrollToPosition()");
																mRouteDetailListView.smoothScrollToPosition(mListPositionOfLastDelivery);
															}
															catch(Exception e){
																logger.error("EXCEPTION " + e.getMessage());
															}
														}
													}
																	   );
										}
									}

									//=== TEXT TO SPEECH
									//logger.info("useTextToSpeech=" + useListTextToSpeech);
									if(useListTextToSpeech){
										if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
											MyHereNavigationFragment navigationFragment = (MyHereNavigationFragment) RouteDetailsRightSideFragmentMerged.this.getActivity().
																																							 getSupportFragmentManager().findFragmentByTag(RouteDetailsActivity.NAVIGATION_FRAGMENT_TAG);

											//=== IF IN SOLO SCREEN MODE
											if(navigationFragment == null){
												logger.info("++++++CALLING announceDeliverySequenced()");
												announceDeliverySequenced(addressesWithinDeliveryArea);
											}
										}
									}

									//logger.info("useGoogleMaps=" + useGoogleMaps);
									if(useGoogleMaps){
										//logger.debug("CALLING updateGoogleMap() FROM updateListFromHandlerSequenced()");

										updateGoogleMap(locationOfDevice, mDataDisplayList);
									}

									//=== NEED TO CHECK IF LAST DROP WAS MADE FOR THIS PAGE
									//=== AND GET THE NEXT PAGE IF SO
									boolean didDeliverLastPageDrop = false;
									for(int i = 0; i < addressesWithinDeliveryArea.size(); i++){
										if(addressesWithinDeliveryArea.get(i).getSequence() == SEQUENCE_ID_OF_LAST_LIST_ITEM){
											didDeliverLastPageDrop = true;
											logger.info("++++++LAST LIST ITEM PAGE DROP WAS DELIVERED FOR " + addressesWithinDeliveryArea.get(i).getSequence() + " SEQUENCE_ID_OF_LAST_DELIVERY = " + SEQUENCE_ID_OF_LAST_DELIVERY);
											break;
										}
									}

									logger.info("LAST LIST ITEM PAGE DROP DELIVERED IS " + didDeliverLastPageDrop);

									if(didDeliverLastPageDrop){
										//logger.info("DID NOT DELIVER LAST PAGE DROP");
										logger.info("DELIVERED LAST LIST PAGE DROP");

										//=== IF ON LAST DROP OF LAST PAGE
										if(GREATEST_SEQUENCE_ID_PROCESSED == SEQUENCE_ID_OF_LAST_ROUTE_ITEM){
											logger.info("ROUTE IS FINISHED - NO AUTO PAGE FORWARD (i.e. last route drop)");

											if(useListTextToSpeech){
												voiceAnnouncements.add("Route delivery is finished.");
												playVoiceMessagesInQueue();
											}

											int orientation = this.getResources().getConfiguration().orientation;
											logger.debug(">>>>onCreate() : orientation " + (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? "PORTRAIT" : (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "UNKNOWN")));

											if(orientation == Configuration.ORIENTATION_PORTRAIT){
												final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RouteDetailsRightSideFragmentMerged.this.getActivity());
												dialogBuilder.setMessage("This route is finished.")
															 .setPositiveButton("OK", new DialogInterface.OnClickListener(){
																 @Override
																 public void onClick(DialogInterface paramDialogInterface, int paramInt){
																	 RouteDetailsRightSideFragmentMerged.this.getActivity().finish();
																 }
															 });

												AlertDialog alertDialog = dialogBuilder.create();
												Window window = alertDialog.getWindow();
												window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
																WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
												window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

												alertDialog.show();
											}

											isInPauseMode = true;
											resetDeliveryOperationsButtons();
										}
										//=== ELSE NOT ON LAST ROUTE DROP
										else{
											logger.info("ROUTE IS NOT FINISHED - ATTEMPTING AUTO PAGE FORWARD");

											if(useListTextToSpeech){
												voiceAnnouncements.add("Please wait while the next page of deliveries is loaded.");
												playVoiceMessagesInQueue();
											}

											getActivity().runOnUiThread(
													new Runnable(){
														@Override
														public void run(){
															try{
																Toast pageToast = Toast.makeText(CTApp.getCustomAppContext(), "Please wait while the next page of deliveries is loaded.", Toast.LENGTH_LONG);
																pageToast.setGravity(Gravity.CENTER, 0, 0);
																pageToast.show();

																boolean saveIsInPaused = isInPauseMode;
																isInPauseMode = true;

																getAPageOfDeliveryItemsSequenced(mRoute,
																								 SEQUENCE_ID_OF_LAST_LIST_ITEM - SEQUENCE_ID_INTERVAL,
																								 DELIVERY_LIST_LIMIT,
																								 mDeviceAzimuth);

																//logger.debug("$$$MONEY : updateListFromHandlerSequenced(2) calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
																mRouteDetailListAdapterCommon.notifyDataSetChanged();

																isInPauseMode = saveIsInPaused;

																//=== HAVE TO CALL THIS AGAIN AS isInPausedMode changed
																resetDeliveryOperationsButtons();
															}
															catch(Exception e){
																logger.error("EXCEPTION " + e.getMessage());
															}
														}
													}
																	   );
										}
									}
								}
							}
						}
						else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
							//logger.debug("addressesWithinDeliveryArea has " + addressesWithinDeliveryArea.values().size() + " items");

							//===NOT SURE IF THIS IS NEEDED HERE, 03MAY21, TKV
/*
							int[] sequenceNumbers = new int[addressesWithinDeliveryArea.size()];
							for(int index = 0; index < addressesWithinDeliveryArea.size(); index++){
								//logger.debug("SENDING INTENT_UPDATE_NAV_MAP_PINS TO NAV FRAGMENT add sequence # for " + addressesWithinDeliveryArea.get(index).getSequence());
								sequenceNumbers[index] = addressesWithinDeliveryArea.get(index).getSequence();
							}
							logger.debug("SEND BROADCAST for INTENT_UPDATE_NAV_MAP_PINS to NAV FRAGMENT for SEQUENCING mode");

							Intent intentP = new Intent(GlobalConstants.INTENT_UPDATE_NAV_MAP_PINS);
							intentP.putExtra("SEQUENCE_NUMBERS", sequenceNumbers);
							intentP.setPackage(thisActivity.getPackageName());//"com.agilegeodata.carriertrack");
							getActivity().sendBroadcast(intentP);
*/

							//=== NEED TO RUN THIS ON THE ui THREAD
							getActivity().runOnUiThread(new Runnable(){
								@Override
								public void run(){
									mDataDisplayList.clear();
									mDataDisplayList.addAll(addressesWithinDeliveryArea);
									Location locationOfDevice = deviceLoc;

									//=== UI DOES NOT MAKE SENSE TO HAVE STREET SUMMARIES
									//=== WHEN RE-SEQUENCING
									createDisplayListContentsWithStreetSummaryRandom(locationOfDevice);

									if(mRouteDetailListAdapterCommon != null){
										//logger.debug("NOTIFYING LIST ADAPTER OF DATA CHANGE");
										//logger.debug("mDataDisplayList.size(1) = " + mDataDisplayList.size());
										logger.debug("$$$MONEY : updateListFromHandlerSequenced(3) calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
										mRouteDetailListAdapterCommon.notifyDataSetChanged();

										if(mRouteDetailListView.getCount() > 0){
											//logger.debug("mRouteDetailListView.getCount() = " + mRouteDetailListView.getCount());
											//logger.debug("mDataDisplayList.size(2) = " + mDataDisplayList.size());
											mRouteDetailListView.setSelection(0);
										}
									}

									if(useGoogleMaps){
										updateGoogleMap(locationOfDevice, mDataDisplayList);
									}
								}
							});
						}
						else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
							//logger.debug("addressesWithinDeliveryArea has " + addressesWithinDeliveryArea.values().size() + " items");

							//===NOT SURE IF THIS IS NEEDED HERE, 03MAY21, TKV
/*
							int[] sequenceNumbers = new int[addressesWithinDeliveryArea.size()];
							for(int index = 0; index < addressesWithinDeliveryArea.size(); index++){
								//logger.debug("SENDING INTENT_UPDATE_NAV_MAP_PINS TO NAV FRAGMENT add sequence # for " + addressesWithinDeliveryArea.get(index).getSequence());
								sequenceNumbers[index] = addressesWithinDeliveryArea.get(index).getSequence();
							}
							logger.debug("SEND BROADCAST for INTENT_UPDATE_NAV_MAP_PINS to NAV FRAGMENT for SEQUENCING mode");

							Intent intentP = new Intent(GlobalConstants.INTENT_UPDATE_NAV_MAP_PINS);
							intentP.putExtra("SEQUENCE_NUMBERS", sequenceNumbers);
							intentP.setPackage(thisActivity.getPackageName());//"com.agilegeodata.carriertrack");
							getActivity().sendBroadcast(intentP);
*/

							//=== NEED TO RUN THIS ON THE ui THREAD
							getActivity().runOnUiThread(new Runnable(){
								@Override
								public void run(){
									mDataDisplayList.clear();
									mDataDisplayList.addAll(addressesWithinDeliveryArea);
									Location locationOfDevice = deviceLoc;

									//=== UI DOES NOT MAKE SENSE TO HAVE STREET SUMMARIES
									//=== WHEN RE-SEQUENCING
									final int selectedIndex = updateDisplayListSequenced(addressesWithinDeliveryArea);

									if(mRouteDetailListAdapterCommon != null){
										//logger.debug("NOTIFYING LIST ADAPTER OF DATA CHANGE");
										//logger.debug("mDataDisplayList.size(1) = " + mDataDisplayList.size());
										logger.debug("$$$MONEY : updateListFromHandlerSequenced(3) calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
										mRouteDetailListAdapterCommon.notifyDataSetChanged();

										if(mRouteDetailListView.getCount() > 0){
											//logger.debug("mRouteDetailListView.getCount() = " + mRouteDetailListView.getCount());
											//logger.debug("mDataDisplayList.size(2) = " + mDataDisplayList.size());
											mRouteDetailListView.setSelection(0);
										}
									}

									if(useGoogleMaps){
										updateGoogleMap(locationOfDevice, mDataDisplayList);
									}
								}
							});
						}

						isProcessingDelivery = false;
					}
					catch(Exception e){
						isProcessingDelivery = false;
						logger.error("++++++EXCEPTION", e);
					}
				}
			}
		}
	}

	protected int updateDisplayListSequenced(ArrayList<DeliveryItem> addressesWithinDeliveryArea){
		int returnListPosition = -1;

		for(int index = 0; index < addressesWithinDeliveryArea.size(); index++){
			DeliveryItem processedRouteDetail = addressesWithinDeliveryArea.get(index);
			int processedSequenceId = processedRouteDetail.getSequence();

			for(int index2 = 0; index2 < mDataDisplayList.size(); index2++){
				DeliveryItem displayRouteDetail = mDataDisplayList.get(index2);
				int displaySequenceId = displayRouteDetail.getSequence();

				if(processedSequenceId == displaySequenceId){
					processedRouteDetail.setStartingPoint(displayRouteDetail.getStartingPoint());
					mDataDisplayList.set(index2, processedRouteDetail);
					logger.debug("UPDATED DROP HAS DISTANCE OF " + mDataDisplayList.get(index2).getDistance());

					returnListPosition = index2;

					break;
				}
			}
		}

		return returnListPosition;
	}

	protected int determineLogicalLastDeliveryRecordId(ArrayList<DeliveryItem> addressesWithinDeliveryArea, int lastSequenceIdDelivered){
		int lastRecId = -1;

		logger.info("addressesWithinDeliveryArea SIZE IS " + addressesWithinDeliveryArea.size());
		logger.info("CURRENT LAST DELIVERED SEQUENCE ID IS " + lastSequenceIdDelivered);
		//logger.info("mDataDisplayList size = " + mDataDisplayList.size());

		//=== IF ON THE LAST DELIVERED DROP IN LIST, A SEARCH DROP GROUP MAY HAVE A DROP WITH
		//=== A SEQUENCE NUMBER NOT FOUND IN THE CURRENT DELIVERY DROP LIST SO listIndexForLastSequenceIdDelivered
		//=== WILL REMAIN -1
		int listIndexForLastSequenceIdDelivered = -1;
		for(int i = 0; i < mDataDisplayList.size(); i++){
			logger.info("mDataDisplayList.get(" + i + ").getSequence() = " + mDataDisplayList.get(i).getSequence());
			if(mDataDisplayList.get(i).getSequence() == lastSequenceIdDelivered){
				listIndexForLastSequenceIdDelivered = i;
				break;
			}
		}
		logger.info("INDEX INTO DISPLAY LIST IS " + listIndexForLastSequenceIdDelivered);

		int greatestSequenceIdOfAddresses = addressesWithinDeliveryArea.get(addressesWithinDeliveryArea.size() - 1).getSequence();
		GREATEST_SEQUENCE_ID_PROCESSED = greatestSequenceIdOfAddresses > GREATEST_SEQUENCE_ID_PROCESSED ? greatestSequenceIdOfAddresses : GREATEST_SEQUENCE_ID_PROCESSED;
		logger.info("GREATEST SEQUENCE ID IN GROUP IS " + greatestSequenceIdOfAddresses + " STARTING AT " + lastSequenceIdDelivered);

		for(int index2 = listIndexForLastSequenceIdDelivered; index2 < mDataDisplayList.size(); index2++){
			//=== WE MUST INCLUDE ALL DROPS TO DETERMINE THE LAST DROP PROCESSED DURING DELIVERY
			//=== SO INCLUDE THE DO NOT DELIVERS WITH A DELIVERED VALUE OF ZERO WHICH INDICATES
			//=== THIS DO NOT DELIVER WAS PROCESSED (i.e. physically passed by device)

			//*****************GETTING A CRASH HERE index2 is -1, 28nov22
			DeliveryItem aRouteDetail = mDataDisplayList.get(index2);
			//*****************GETTING A CRASH HERE index2 is -1

			//logger.info("LOOKING AT SEQUENCE ID " + aRouteDetail.getSequence() + " DELIVERED " + aRouteDetail.getDelivered());
			//logger.info("LOOKING AT SEQUENCE ID " + aRouteDetail.getSequence() + " JOBTYPE " + aRouteDetail.getJobType());

			if(aRouteDetail.getSequence() <= greatestSequenceIdOfAddresses){
				//=== FORCE AN UNRESOLVED ADDRESS TO ACT AS IF IT WAS DELIVERED
				if(aRouteDetail.getDelivered() == 1 ||
				   aRouteDetail.getDelivered() == 0 ||
				   aRouteDetail.getIsInvalidAddress()){
					lastRecId = aRouteDetail.getId();
					logger.info("forcing NEW LAST DELIVERED RECORD ID IS " + lastRecId + " OLD LAST DELIVERED RECORD ID IS " + LAST_DELIVERY_RECORD_ID);

					SEQUENCE_ID_OF_NEXT_DELIVERY = aRouteDetail.getSequence();
					logger.info("forcing NEXT DELIVERED SEQUENCE ID IS " + SEQUENCE_ID_OF_NEXT_DELIVERY + " LAST DELIVERED SEQUENCE IS " + SEQUENCE_ID_OF_LAST_DELIVERY);
				}
				else{
					//logger.info("DONE WITH CONTIGUOUS SEQUENCING - past last continuous processed drop");
					break;
				}
			}
			else{
				logger.info("DONE WITH CONTIGUOUS SEQUENCING - exceeded greatest sequence id in group");
				break;
			}
		}

		logger.info("SEQUENCE_ID_OF_NEXT_DELIVERY = " + SEQUENCE_ID_OF_NEXT_DELIVERY);
		//logger.info("EXIT LAST REC ID IS " + lastRecId);

		return lastRecId;
	}

	public void savePauseButtonState(){
		pauseButtonEnabled = pauseButton.isEnabled();
		pauseButtonActivated = pauseButton.isActivated();
	}

	public void restorePauseButtonState(){
		pauseButton.setEnabled(pauseButtonEnabled);
		pauseButton.setActivated(pauseButtonActivated);
	}

	public void resetDeliveryOperationsButtons(){
		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
			pageNumberText.setText("Page\n" + (CURRENT_DELIVERY_LIST_PAGE + 1));
		}

		if(isInPauseMode){
			if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
				navButtonIcon.setEnabled(true);

				deliveryPagePreviousButton.setEnabled(CURRENT_DELIVERY_LIST_PAGE != 0);
				deliveryPageNextButton.setEnabled(CURRENT_DELIVERY_LIST_SIZE == DELIVERY_LIST_LIMIT);
			}
		}
		else{
			if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
				navButtonIcon.setEnabled(false);
				setPageButtonsEnabled(false);
			}
		}
	}

	public void setPageButtonsEnabled(boolean enabled){
		if(isInPauseMode){
			deliveryPagePreviousButton.setEnabled(enabled);
			deliveryPageNextButton.setEnabled(enabled);
		}
		else{
			deliveryPagePreviousButton.setEnabled(false);
			deliveryPageNextButton.setEnabled(false);
		}
	}

	public void setPauseButtonsActivated(boolean activated){
		pauseButton.setActivated(activated);
	}

	public void setPauseButtonsEnabled(boolean enabled){
		pauseButton.setEnabled(enabled);
	}

	public void continueSetupSequenced(Bundle savedState){
		try{
			SharedPreferences prefs = getActivity().getSharedPreferences(
					GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

			//=== INITIALIZE PERSISTED ITEMS FIRST
			mTZOffSet = prefs.getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);

			mCustSrvMap = new HashMap<Integer, Integer>();

			mDoNotDeliverMap = new HashMap<Integer, Integer>();
			mStreetSummaryHashMap = new HashMap<Integer, StreetSummaryRandom>();

			//=== LOAD THE ROUTE DETAILS FOR THE LAST ROUTE LEFT UNFINISHED IF IT MATCHES THIS ROUTE ID
			Bundle extras = getActivity().getIntent().getExtras();

			mRouteId = savedState != null ? savedState.getString(GlobalConstants.EXTRA_ROUTE_ID) : null; // required
			if(mRouteId == null){
				mRouteId = extras != null ? extras.getString(GlobalConstants.EXTRA_ROUTE_ID) : null;
			}

			mDeviceForwardAzimuth = savedState != null ? savedState.getFloat(GlobalConstants.EXTRA_BEARING) : 0f; // required
			if(mDeviceForwardAzimuth == 0f){
				mDeviceForwardAzimuth = extras != null ? extras.getFloat(GlobalConstants.EXTRA_BEARING) : 0f;
			}

			mDeliveryQuads = savedState != null ? savedState.getInt(GlobalConstants.EXTRA_DELIVERY_QUADS) : GlobalConstants.DEF_DELIVERY_QUADS_NONE; // required

			if(mDeliveryQuads == GlobalConstants.DEF_DELIVERY_QUADS_NONE){
				mDeliveryQuads = extras != null ? extras.getInt(GlobalConstants.EXTRA_DELIVERY_QUADS) : GlobalConstants.DEF_DELIVERY_QUADS_LEFT_FRONT_AND_RIGHT_FRONT;
			}

			//=== INITIALIZE BUTTONS ETC. NOW
			if(useGoogleMaps){
				googleMapSlideButton = googleMapPage.findViewById(R.id.slideButton);
				googleMapSlideButton.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v){
						routeDetailsRightSideViewPager.setCurrentItem(0);
					}
				});
			}

			pauseButton = routeDetailsRightSideViewListPage.findViewById(R.id.pauseButton);
			pauseButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					isInPauseMode = !isInPauseMode;

					if(isInPauseMode){
						setPauseButtonsEnabled(true);
						setPauseButtonsActivated(false);
					}
					else{
						setPauseButtonsEnabled(true);
						setPauseButtonsActivated(true);
					}

					Intent intent = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
					intent.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, isInPauseMode);
					intent.setPackage(RouteDetailsRightSideFragmentMerged.this.getContext().getPackageName());//"com.agilegeodata.carriertrack");
					logger.debug("SENDING PAUSE INTENT FROM route details rightside fragmented merged 1");
					getActivity().sendBroadcast(intent);
				}
			});

			navButtonIcon = routeDetailsRightSideViewListPage.findViewById(R.id.iconNavButton);
			navButtonIcon.setVisibility(View.VISIBLE);
			navButtonIcon.setImageDrawable(CTApp.appContext.getResources().getDrawable(R.drawable.nav_button_states));

			if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING) ||
			   !CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
				navButtonIcon.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v){
						savePauseButtonState();
						RouteDetailsRightSideFragmentMerged.saveIsPaused = RouteDetailsRightSideFragmentMerged.isInPauseMode;
						RouteDetailsRightSideFragmentMerged.isInPauseMode = false;
						setPauseButtonsActivated(false);

						pausedChanged();

						logger.info("DELIVERY PAUSE FORCED OFF FOR TURN BY TURN NAVIGATION");
						useListTextToSpeech = false;

						Intent intent = new Intent(CTApp.appContext, EmbeddedNavigationActivity.class);
						intent.putExtra(GlobalConstants.EXTRA_LASTJOBDETAILSEQUENCE, SEQUENCE_ID_OF_LAST_ROUTE_ITEM);
						intent.putExtra(GlobalConstants.EXTRA_JOBDETAILSEQUENCE, SEQUENCE_ID_OF_LAST_DELIVERY);
						intent.putExtra(GlobalConstants.EXTRA_JOBDETAILID, mJobDetailId);
						intent.putExtra(GlobalConstants.EXTRA_SEQUENCE_INTERVAL, SEQUENCE_ID_INTERVAL);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

						getActivity().startActivity(intent);
						((RouteDetailsActivity) RouteDetailsRightSideFragmentMerged.this.getActivity()).navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_SOLO;
					}
				});
			}
			deliveryPagePreviousButton = routeDetailsRightSideViewListPage.findViewById(R.id.pageBackButton);
			deliveryPagePreviousButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					logger.info("previous page button pressed");

					CURRENT_DELIVERY_LIST_PAGE--;

					//=== SEQUENCE_ID_OF_LAST_LIST_ITEM will be the first item of the current page
					int startSequenceIdForPreviousPage = SEQUENCE_ID_OF_LAST_LIST_ITEM - (((DELIVERY_LIST_LIMIT * 2) - 1) * SEQUENCE_ID_INTERVAL) - SEQUENCE_ID_INTERVAL;
					getAPageOfDeliveryItemsSequenced(mRoute,
													 startSequenceIdForPreviousPage - SEQUENCE_ID_INTERVAL,
													 DELIVERY_LIST_LIMIT,
													 mDeviceAzimuth);

					getActivity().runOnUiThread(
							new Runnable(){
								@Override
								public void run(){
									logger.debug("$$$MONEY : continueSetupSequenced(1) calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
									mRouteDetailListAdapterCommon.notifyDataSetChanged();
								}
							});
				}
			});

			deliveryPageNextButton = routeDetailsRightSideViewListPage.findViewById(R.id.pageForwardButton);
			deliveryPageNextButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					logger.info("next page button pressed");

					CURRENT_DELIVERY_LIST_PAGE++;

					getAPageOfDeliveryItemsSequenced(mRoute,
													 SEQUENCE_ID_OF_LAST_LIST_ITEM,
													 DELIVERY_LIST_LIMIT,
													 mDeviceAzimuth);

					getActivity().runOnUiThread(
							new Runnable(){
								@Override
								public void run(){
									logger.debug("$$$MONEY : continueSetupSequenced(2) calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
									mRouteDetailListAdapterCommon.notifyDataSetChanged();
								}
							});
				}
			});

			pageNumberText = routeDetailsRightSideViewListPage.findViewById(R.id.pageNumberText);

			//=== INITIALIZE ROUTE SPECIFIC ITEMS NOW
			mDeliveryAreaProjectedFrontDistance = mRoute.getDeliveryForward();//int)GPSUtils.getLookAheadDistanceForGivenSpeed((int) mDeviceSpeedInMetersPerSecond);
			mDeliveryAreaProjectedSideDistance = mRoute.getDeliverySide();//GlobalConstants.DEF_LOOKAHEAD_SIDE_DISTANCE;
			mExtendedAreaProjectedFrontDistance = mRoute.getLookaheadForward();//int)(mDeliveryAreaProjectedFrontDistance * 4);
			mExtendedAreaProjectedSideDistance = mRoute.getLookaheadSide();//int)(mExtendedAreaProjectedFrontDistance / 2);

			mRouteId = mRoute.getRouteId();

			mRouteDetailListView = routeDetailsRightSideViewListPage.findViewById(R.id.dataLV);
			mRouteDetailListView.setVerticalScrollBarEnabled(false);

			//=== THESE ARE USED ONLY BY A STREET SUMMARY LIST ITEM IN BOTH RANDOM ROUTES AND SEQUENCED ROUTES WHEN RE-SEQUENCING
			mCustSrvMap = DBHelper.getInstance().fetchCustSrvByJobDetailIdGroupByStreetSummary_Random(mRoute.getJobDetailId());

			mDoNotDeliverMap = DBHelper.getInstance().fetchDNDByJobDetailIdGroupByStreetSummary_Random(mRoute.getJobDetailId());

			//=== this does not set the numRemaining, numDND, numCustSvc
			mStreetSummaryHashMap = DBHelper.getInstance().fetchStreetSummariesByJobDetailId_Random(mRoute.getJobDetailId());
			mRoute.setStreetSummaryMap(mStreetSummaryHashMap);
			//=== THESE ARE USED ONLY BY A STREET SUMMARY LIST ITEM IN BOTH RANDOM ROUTES AND SEQUENCED ROUTES WHEN RE-SEQUENCING

			SEQUENCE_ID_OF_LAST_ROUTE_ITEM = DBHelper.getInstance().fetchLargestSequenceForJobDetailId_Sequenced(mRoute.getJobDetailId());

			//=== NEGATIVE SEQUENCE
			SEQUENCE_ID_INTERVAL = DBHelper.getInstance().fetchSequenceIntervalForJobDetailId_Sequenced(mRoute.getJobDetailId());

			//=== INITIALIZE THE SKIPPED DROPS CHECK INTERVAL
			DBHelper.getInstance().setlastMissedDropCheck(System.currentTimeMillis());

			//=== INITIALIZE THE DELIVERY LIST
			getAPageOfDeliveryItemsSequenced(mRoute,
											 -SEQUENCE_ID_INTERVAL, //ALWAYS INCLUDE THE STARTING POINT
											 DELIVERY_LIST_LIMIT,
											 mDeviceAzimuth);

			//logger.debug("???????? mDataDisplayList size = " + mDataDisplayList == null ? " SI NULL" : mDataDisplayList.size()+"");
			mRouteDetailListAdapterCommon = new ListItemRouteDetailsAdapterCommon(getActivity(), mRoute.getRouteJobType(), mDataDisplayList, /*myDataOperationsMode,*/ this);
			mRouteDetailListAdapterCommon.setTimeZoneOffset(mTZOffSet);
			mRouteDetailListView.setAdapter(mRouteDetailListAdapterCommon);

			int[] sequenceListPosition = getFirstValidSequenceFromDataDisplayList(mDataDisplayList, CTApp.getLocation());

			if(SEQUENCE_ID_OF_LAST_DELIVERY > 0){
				sequenceListPosition = getDataDisplayListItemNumberForSequenceId(SEQUENCE_ID_OF_LAST_DELIVERY);
			}

			SEQUENCE_ID_OF_LAST_DELIVERY = sequenceListPosition[0];
			resetStartingPointSoft(sequenceListPosition[0]);

			final int[] sequenceListPositionFinal = sequenceListPosition;

			getActivity().runOnUiThread(
					new Runnable(){
						@Override
						public void run(){
							logger.debug("$$$MONEY : continueSetupSequenced(3) calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
							mRouteDetailListAdapterCommon.notifyDataSetChanged();
							logger.info("++++++SCROLLING TO POSITION " + sequenceListPositionFinal[1]);
							logger.debug("$$$MONEY : continueSetupSequenced() calling mRouteDetailListView.smoothScrollToPosition()");
							mRouteDetailListView.smoothScrollToPosition(sequenceListPositionFinal[1]);
						}
					});

			DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, RouteDetailsActivity.class.getName());

			int unresolvedAddressCount = DBHelper.getInstance().getUnresolvedAddressCountForJobDetailId_Common(mJobDetailId);
			if(unresolvedAddressCount > 0){
				//=== ALERT THE USER OF QUESTIONABLE DROPS
				displayUnresolvedAddressDialog("Unresolved Address(s)", unresolvedAddressCount);
			}
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}

		instance = this;

		Intent intent = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
		intent.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, isInPauseMode);
		intent.setPackage(RouteDetailsRightSideFragmentMerged.this.getContext().getPackageName());//"com.agilegeodata.carriertrack");
		logger.debug("SENDING PAUSE INTENT FROM route details rightside fragmented merged 2");
		getActivity().sendBroadcast(intent);
	}

	protected void getAPageOfDeliveryItemsSequenced(Route route,
													int baseSequenceId,
													int deliveryListLimit,
													double deviceAzimuth){
		try{
			if(mDataDisplayList == null){
				mDataDisplayList = new ArrayList<DeliveryItem>();
			}
			else{
				mDataDisplayList.clear();
			}

			//logger.debug("$$$$$$$$$$$$ STARTING NEW ROUTE");
			//logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
			if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
				LinkedHashMap<Integer, DeliveryItem> routeDetails =
						DBHelper.getInstance().fetchRouteDetailsForJobDetailIdStartAtSequence_Sequenced(
								route.getJobDetailId(),
								baseSequenceId,
								deliveryListLimit,
								CTApp.getLocation(),
								deviceAzimuth);

				mDataDisplayList.addAll(routeDetails.values());

				mDataDisplayList.get(0).setStartingPoint(true);

				int[] sequenceListPosition = getFirstValidSequenceFromDataDisplayList(mDataDisplayList, CTApp.getLocation());
				resetStartingPointSoft(SEQUENCE_ID_OF_LAST_DELIVERY);

				getActivity().runOnUiThread(
						new Runnable(){
							@Override
							public void run(){
								if(mRouteDetailListAdapterCommon != null){
									//logger.info("++++++NOTIFYING LIST ADAPTER OF DATA CHANGE");
									//logger.debug("mDataDisplayList.size(1) = " + mDataDisplayList.size());
									logger.debug("$$$MONEY : getAPageOfDeliveryItemsSequenced() calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
									mRouteDetailListAdapterCommon.notifyDataSetChanged();

									if(mRouteDetailListView.getCount() > 0){
										logger.info("++++++SCROLLING TO POSITION " + sequenceListPosition[1]);
										logger.debug("$$$MONEY : getAPageOfDeliveryItemsSequenced() calling mRouteDetailListView.smoothScrollToPosition()");
										mRouteDetailListView.smoothScrollToPosition(sequenceListPosition[1]);
									}
								}
							}
						});

				LAST_DELIVERY_RECORD_ID = mDataDisplayList.get(0).getId();
				CURRENT_DELIVERY_LIST_SIZE = routeDetails.size();
				SEQUENCE_ID_OF_LAST_LIST_ITEM = mDataDisplayList.get(CURRENT_DELIVERY_LIST_SIZE - 1).getSequence();

				logger.info("SEQUENCE_ID_OF_LAST_DELIVERY = " + SEQUENCE_ID_OF_LAST_DELIVERY + ", SEQUENCE_ID_INTERVAL = " + SEQUENCE_ID_INTERVAL + ", DELIVERY_LIST_LIMIT = " + DELIVERY_LIST_LIMIT);
			}

			resetDeliveryOperationsButtons();
		}
		catch(Exception e){
			logger.error("EXCEPTION " + e.getMessage());
		}
	}

	private int[] getFirstValidSequenceFromDataDisplayList(ArrayList<DeliveryItem> dataDisplayList, Location currentLocation){
		int sequenceNum = -1;
		int listPosition = -1;

		for(int x = 0; x < dataDisplayList.size() - 1; x++){
			DeliveryItem rd = dataDisplayList.get(x);
			double distance = GPSUtils.distFromAsLocation(rd.getGpsLocationLatitude(), rd.getGpsLocationLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude());

			//=== one hundred miles in meters
			if(rd.getSequence() > 0 && distance < GlobalConstants.BAD_LOCATION_SANITY_DISTANCE_CHECK_METERS){
				sequenceNum = rd.getSequence();
				listPosition = x;
				break;
			}
		}

		return new int[]{sequenceNum, listPosition};
	}

	private int[] getDataDisplayListItemNumberForSequenceId(int sequenceId){
		int sequenceNum = -1;
		int listPosition = -1;

		for(int x = 0; x < mDataDisplayList.size() - 1; x++){
			DeliveryItem rd = mDataDisplayList.get(x);

			//=== one hundred miles in meters
			if(rd.getSequence() == sequenceId){
				sequenceNum = rd.getSequence();
				listPosition = x;
				break;
			}
		}

		return new int[]{sequenceNum, listPosition};
	}

	public boolean resetStartingPointSoft(int sequenceId){
		logger.debug("ENTER");
		boolean didReset = false;
		for(int index = 0; index < mDataDisplayList.size(); index++){
			mDataDisplayList.get(index).setStartingPoint(false);

			if(mDataDisplayList.get(index).getSequence() == sequenceId){
				mDataDisplayList.get(index).setStartingPoint(true);
				SEQUENCE_ID_OF_LAST_DELIVERY = sequenceId;
				didReset = true;

				logger.debug("RESET SEQUENCE ID TO " + SEQUENCE_ID_OF_LAST_DELIVERY);
			}
		}

		return didReset;
	}

	//++++++++++++++RANDOM
	public void updateListFromHandlerRandom(final String sFilter, final int deliveryQuadsMode, boolean forceUpdate){
//		logger.debug("\n++++++RANDOM DELIVERY PAUSE " + (isInPauseMode ? "ON" : "OFF\n"));
//		logger.debug("\n++++++RANDOM DELIVERY FORCED UPDATE " + (forceUpdate ? "ON" : "OFF\n"));
		//logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());

		if(isInPauseMode && !forceUpdate){
			//java.lang.NullPointerException: Attempt to invoke virtual method 'void androidx.fragment.app.FragmentActivity.runOnUiThread(java.lang.Runnable)' on a null object reference
			FragmentActivity activity = getActivity();
			if(activity != null){
				flashPauseButton = !flashPauseButton;
				getActivity().runOnUiThread(
						new Runnable(){
							@Override
							public void run(){
								setPauseButtonsEnabled(flashPauseButton);
							}
						});
			}
		}
		else{
			//=== THIS ONLY UPDATES THE DISPLAY IF WE ARE CURRENTLY PROCESSING GPS ACTIVITY?????
//			logger.debug("++++++PROCESSING random GPS IS " + (!isProcessingDelivery ? "HAPPENING" : "NOT HAPPENING so skipping"));
			if(!isProcessingDelivery){
				Thread t = new Thread(){
					public void run(){
						try{
							isProcessingDelivery = true;
							logger.debug("++++++START UPDATE of RANDOM DELIVERY");
							//mSearchValStr = sFilter; // sFilter should be null to reset the list....

							mDeliveryQuads = deliveryQuadsMode;

							Location deviceLoc = CTApp.getLocation();
							deviceLoc.setBearing(mDeviceForwardAzimuth);

							Calendar cal = Calendar.getInstance();
							final long gmtTimestamp = cal.getTimeInMillis() + mTZOffSet;

							//=== GET NEW SET OF ROUTE DETAILS BASED ON TARGET AREA AND PROCESS WITH CURRENT ROUTE MAP
							//logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
							if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
								addressesWithinDeliveryArea =
										DBHelper.getInstance().fetchRouteDetailsWithinDeliveryArea_Random(
												mRoute.getJobDetailId(),
												mDeliveryAreaProjectedFrontDistance,
												mDeliveryAreaProjectedSideDistance,
												mExtendedAreaProjectedFrontDistance,
												mExtendedAreaProjectedSideDistance,
												mDataDisplayList,
												deliveryQuadsMode,
												deviceLoc,
												mDeviceAzimuth,
												gmtTimestamp);
							}
							else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
								//=== THIS ONLY SETS sequencenew value, it does not determine if delivered
								addressesWithinDeliveryArea =
										DBHelper.getInstance().fetchRouteDetailsWithinDeliveryArea_Sequencing_As_Random(
												mRoute.getJobDetailId(),
												mDeliveryAreaProjectedFrontDistance,
												mDeliveryAreaProjectedSideDistance,
												mExtendedAreaProjectedFrontDistance,
												mExtendedAreaProjectedSideDistance,
												mDataDisplayList,
												deliveryQuadsMode,
												deviceLoc,
												mDeviceAzimuth);
							}
							else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
								//=== THIS ONLY SETS sequencenew value, it does not determine if delivered
								addressesWithinDeliveryArea =
										DBHelper.getInstance().fetchAllRouteDetailsForJobDetailId_Sequenced(mRoute.getJobDetailId());
							}

							//logger.debug("addressesWithinDeliveryArea has " + addressesWithinDeliveryArea.values().size() + " items");
							mDataDisplayList.clear();
							mDataDisplayList.addAll(addressesWithinDeliveryArea);

							final Location locationOfDevice = deviceLoc;

							//=== NEED TO RUN THIS ON THE ui THREAD
							getActivity().runOnUiThread(new Runnable(){
								@Override
								public void run(){
									logger.debug("++++++START thread of RANDOM DELIVERY");

									//=== UI DOES NOT MAKE SENSE TO HAVE STREET SUMMARIES
									//=== WHEN RE-SEQUENCING
									createDisplayListContentsWithStreetSummaryRandom(locationOfDevice);

									if(mRouteDetailListAdapterCommon != null){
										//logger.debug("NOTIFYING LIST ADAPTER OF DATA CHANGE");
										//logger.debug("mDataDisplayList.size(1) = " + mDataDisplayList.size());
										logger.debug("$$$MONEY : updateListFromHandlerRandom() calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
										mRouteDetailListAdapterCommon.notifyDataSetChanged();

										if(mRouteDetailListView.getCount() > 0){
											//logger.debug("mRouteDetailListView.getCount() = " + mRouteDetailListView.getCount());
											//logger.debug("mDataDisplayList.size(2) = " + mDataDisplayList.size());
											mRouteDetailListView.setSelection(mListPositionOfClosestItem);
										}
									}

									if(useGoogleMaps){
										ArrayList<DeliveryItem> mapPoints = DBHelper.getInstance().fetchRouteDetailsForJobDetailIdAsArrayList_Random(mJobDetailId);
										updateGoogleMap(locationOfDevice, mapPoints);
									}
								}
							});

							isProcessingDelivery = false;
						}
						catch(Exception e){
							isProcessingDelivery = false;
							logger.error("EXCEPTION", e);
						}
					}
				};

				t.start();
			}
		}
	}

	public void updateFullListFromHandlerRandom(){
		Thread t = new Thread(){
			public void run(){
				try{
					logger.debug("++++++START UPDATE of FULL LIST RANDOM DELIVERY");
					addressesWithinDeliveryArea = DBHelper.getInstance().fetchRouteDetailsForJobDetailIdAsArrayList_Random(mJobDetailId);
					//logger.debug("addressesWithinDeliveryArea has " + addressesWithinDeliveryArea.values().size() + " items");

					mDataDisplayList.clear();
					mDataDisplayList.addAll(addressesWithinDeliveryArea);

					Location deviceLoc = CTApp.getLocation();
					deviceLoc.setBearing(mDeviceForwardAzimuth);

					final Location locationOfDevice = deviceLoc;

					//=== NEED TO RUN THIS ON THE ui THREAD
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run(){
							logger.debug("++++++START thread of RANDOM DELIVERY");

							//=== UI DOES NOT MAKE SENSE TO HAVE STREET SUMMARIES
							//=== WHEN RE-SEQUENCING or RE-NUMBERING
							if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING) &&
							   !CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
								createDisplayListContentsWithStreetSummaryRandom(locationOfDevice);
							}

							if(mRouteDetailListAdapterCommon != null){
								//logger.debug("NOTIFYING LIST ADAPTER OF DATA CHANGE");
								//logger.debug("mDataDisplayList.size(1) = " + mDataDisplayList.size());
								logger.debug("$$$MONEY : updateListFromHandlerRandom() calling mRouteDetailListAdapterCommon.notifyDataSetChanged()");
								mRouteDetailListAdapterCommon.notifyDataSetChanged();

								if(mRouteDetailListView.getCount() > 0){
									//logger.debug("mRouteDetailListView.getCount() = " + mRouteDetailListView.getCount());
									//logger.debug("mDataDisplayList.size(2) = " + mDataDisplayList.size());
									mRouteDetailListView.setSelection(mListPositionOfClosestItem);
								}
							}

							if(useGoogleMaps){
								ArrayList<DeliveryItem> mapPoints = DBHelper.getInstance().fetchRouteDetailsForJobDetailIdAsArrayList_Random(mJobDetailId);
								updateGoogleMap(locationOfDevice, mapPoints);
							}
						}
					});
				}
				catch(Exception e){
					logger.error("EXCEPTION", e);
				}
			}
		};

		t.start();
	}

	private DeliveryItem buildRouteDetailFromSummaryRandom(int summaryId,
														   HashMap<Integer, Integer> custSrvMap,
														   HashMap<Integer, Integer> remainingNotDeliveredMap,
														   HashMap<Integer, Integer> doNotDeliverMap,
														   double dist, Location deviceLoc){
		DeliveryItem routeDetail = null;
		try{
			StreetSummaryRandom streetSummary = mStreetSummaryHashMap.get(summaryId);

			if(streetSummary == null){
				mStreetSummaryHashMap.clear();
				mStreetSummaryHashMap.putAll(DBHelper.getInstance().fetchStreetSummariesByJobDetailId_Random(mJobDetailId));
			}

			//logger.debug("Building Route Summary from " + summaryId);

			routeDetail = new DeliveryItem();
			routeDetail.setStatus(GlobalConstants.ROUTE_DETAIL_STATUS_STREETSUMMARY);

			routeDetail.setGpsPhotoLocationLatitude(deviceLoc.getLatitude());
			routeDetail.setGpsPhotoLocationLongitude(deviceLoc.getLongitude());

			//logger.info("photo location = " + routeDetail.getGpsPhotoLocationLatitude() + ", " + routeDetail.getGpsPhotoLocationLongitude());
			int dnd = 0;
			int remaining = 0;
			int cSrv = 0;
			routeDetail.setSummaryId(summaryId);

			//logger.debug(TAG, "Current getJobDetailId: " + mRoute.getJobDetailId());

			if(doNotDeliverMap == null || doNotDeliverMap.isEmpty()){
				//logger.debug("DoNotDeliver MAP WAS NULL!!!!!");

				doNotDeliverMap = DBHelper.getInstance().fetchDNDByJobDetailIdGroupByStreetSummary_Random(mRoute.getJobDetailId());
			}

			//logger.debug("SummaryID : " + summaryId);

			if(doNotDeliverMap.containsKey(summaryId)){
				dnd = doNotDeliverMap.get(summaryId);
			}

			if(custSrvMap.containsKey(summaryId)){
				cSrv = custSrvMap.get(summaryId);
			}

			if(remainingNotDeliveredMap.containsKey(summaryId)){
				remaining = remainingNotDeliveredMap.get(summaryId);
			}

			routeDetail.setDistance(dist - .01);
			routeDetail.setNumCustSvc(cSrv);
			routeDetail.setNumDND(dnd);

			//=== DOES REMAINING AND QUANTITY MEAN THE SAME THING???????????????
			routeDetail.setQuantity(remaining);
			String str = "";

			if(streetSummary != null){
				str = streetSummary.getStreetName();

				try{
					str = str.replaceFirst("0 ", "");
					if(str.endsWith("0")){
						int len = str.length();
						str = str.substring(0, len - 1);
					}

					str = str.replaceFirst(" 0", " ");
				}
				catch(Exception e){
					logger.error("EXCEPTION", e);
				}
			}

			routeDetail.setGpsLocationAddressStreet(str);
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}

		return routeDetail;
	}

	public void continueSetupRandom(Bundle savedState){
		try{
			DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, RouteDetailsActivity.class.getName());

			SharedPreferences prefs = getActivity().getSharedPreferences(
					GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

			mTZOffSet = prefs.getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);

			mCustSrvMap = new HashMap<Integer, Integer>();

			mDoNotDeliverMap = new HashMap<Integer, Integer>();

			if(mDataDisplayList == null){
				mDataDisplayList = new ArrayList<DeliveryItem>();
			}
			else{
				mDataDisplayList.clear();
			}

			//=== LOAD THE ROUTE DETAILS FOR THE LAST ROUTE LEFT UNFINISHED IF IT MATCHES THIS ROUTE ID
			Bundle extras = getActivity().getIntent().getExtras();

			mRouteId = savedState != null ? savedState.getString(GlobalConstants.EXTRA_ROUTE_ID) : null; // required

			if(mRouteId == null){
				mRouteId = extras != null ? extras.getString(GlobalConstants.EXTRA_ROUTE_ID) : null;
			}

			mDeviceForwardAzimuth = savedState != null ? savedState.getFloat(GlobalConstants.EXTRA_BEARING) : 0f; // required

			if(mDeviceForwardAzimuth == 0f){
				mDeviceForwardAzimuth = extras != null ? extras.getFloat(GlobalConstants.EXTRA_BEARING) : 0f;
			}

			mStreetSummaryHashMap = new HashMap<Integer, StreetSummaryRandom>();

			mSearchValStr = savedState != null ? savedState.getString(GlobalConstants.EXTRA_SEARCH_FILTER) : null; // required

			if(mSearchValStr == null){
				mSearchValStr = extras != null ? extras.getString(GlobalConstants.EXTRA_SEARCH_FILTER) : null;
			}

			mDeliveryQuads = savedState != null ? savedState.getInt(GlobalConstants.EXTRA_DELIVERY_QUADS) : GlobalConstants.DEF_DELIVERY_QUADS_NONE; // required

			if(mDeliveryQuads == GlobalConstants.DEF_DELIVERY_QUADS_NONE){
				mDeliveryQuads = extras != null ? extras.getInt(GlobalConstants.EXTRA_DELIVERY_QUADS) : GlobalConstants.DEF_DELIVERY_QUADS_LEFT_FRONT_AND_RIGHT_FRONT;
			}

			mDeliveryAreaProjectedFrontDistance = mRoute.getDeliveryForward();//int)GPSUtils.getLookAheadDistanceForGivenSpeed((int) mDeviceSpeedInMetersPerSecond);
			mDeliveryAreaProjectedSideDistance = mRoute.getDeliverySide();//GlobalConstants.DEF_LOOKAHEAD_SIDE_DISTANCE;
			mExtendedAreaProjectedFrontDistance = mRoute.getLookaheadForward();//int)(mDeliveryAreaProjectedFrontDistance * 4);
			mExtendedAreaProjectedSideDistance = mRoute.getLookaheadSide();//int)(mExtendedAreaProjectedFrontDistance / 2);

			mRouteId = mRoute.getRouteId();

			mCustSrvMap = DBHelper.getInstance().fetchCustSrvByJobDetailIdGroupByStreetSummary_Random(mRoute.getJobDetailId());

			mDoNotDeliverMap = DBHelper.getInstance().fetchDNDByJobDetailIdGroupByStreetSummary_Random(mRoute.getJobDetailId());

			//=== this does not set the numRemaining, numDND, numCustSvc
			mStreetSummaryHashMap = DBHelper.getInstance().fetchStreetSummariesByJobDetailId_Random(mRoute.getJobDetailId());
			mRoute.setStreetSummaryMap(mStreetSummaryHashMap);

			//logger.debug(">>>>onActivityCreated() : Route : " + mRouteId + " has "
			//		+ mRoute.getStreetSummaryMap().size() + " street summaries");

			//=== THIS LAYOUT HAS UI ITEM FOR BOTH RANDOM AND SEQUENCED SO TURN THESE OFF HERE
			LinearLayout navigationButtons = routeDetailsRightSideView.findViewById(R.id.navigationPageButtons);
			navigationButtons.setVisibility(View.GONE);

			mRouteDetailListView = routeDetailsRightSideView.findViewById(R.id.dataLV);
			mRouteDetailListView.setVerticalScrollBarEnabled(false);

			//logger.debug("???????? mDataDisplayList size = " + mDataDisplayList == null ? " SI NULL" : mDataDisplayList.size()+"");
			mRouteDetailListAdapterCommon = new ListItemRouteDetailsAdapterCommon(getActivity(), mRoute.getRouteJobType(), mDataDisplayList, /*myDataOperationsMode,*/
																				  this);

			mRouteDetailListAdapterCommon.setTimeZoneOffset(mTZOffSet);

			mRouteDetailListView.setAdapter(mRouteDetailListAdapterCommon);

			if(useGoogleMaps){
				googleMapSlideButton = googleMapPage.findViewById(R.id.slideButton);
				googleMapSlideButton.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v){
						routeDetailsRightSideViewPager.setCurrentItem(0);
					}
				});
			}

			pauseButton = routeDetailsRightSideView.findViewById(R.id.pauseButton);
			if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
				pauseButton.setVisibility(View.GONE);
			}
			else if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
				pauseButton.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v){
						isInPauseMode = !isInPauseMode;

						if(isInPauseMode){
							setPauseButtonsEnabled(true);
							setPauseButtonsActivated(false);
						}
						else{
							setPauseButtonsEnabled(true);
							setPauseButtonsActivated(true);
						}

						Intent intent = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
						intent.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, isInPauseMode);
						intent.setPackage(RouteDetailsRightSideFragmentMerged.this.getContext().getPackageName());//"com.agilegeodata.carriertrack");
						logger.debug("SENDING PAUSE INTENT FROM route details rightside fragmented merged 3");
						getActivity().sendBroadcast(intent);
					}
				});
			}

			updateFullListFromHandlerRandom();

			int unresolvedAddressCount = DBHelper.getInstance().getUnresolvedAddressCountForJobDetailId_Common(mJobDetailId);
			if(unresolvedAddressCount > 0){
				//=== ALERT THE USER OF QUESTIONABLE DROPS
				displayUnresolvedAddressDialog("Unresolved Address(s)", unresolvedAddressCount);
			}
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}

		instance = this;

		Intent intent = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
		intent.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, isInPauseMode);
		intent.setPackage(RouteDetailsRightSideFragmentMerged.this.getContext().getPackageName());//"com.agilegeodata.carriertrack");
		logger.debug("SENDING PAUSE INTENT FROM route details rightside fragmented merged 4");
		getActivity().sendBroadcast(intent);
	}

	public class GpsReceiver extends BroadcastReceiver{
		public GpsReceiver(){
		}

		@Override
		public void onReceive(Context context, Intent intent){
			logger.debug("\n**** gpsReceiver.onReceive() : ENTER");
			updateTopNav();

			try{
				if(!isProcessingDelivery){
					logger.debug("\n**** GPS RECEIVED in ROUTEDETAILSRIGHTSIDE, sending broadcast");

					Bundle b = intent.getExtras();
					mGPSSpeed = b.getFloat(GlobalConstants.EXTRA_CURRENT_SPEED, 0);

					mDeviceForwardAzimuth = b.getFloat(GlobalConstants.EXTRA_CURRENT_BEARING, 0);

					if(mLastDeviceForwardAzimuth == 0){
						mLastDeviceForwardAzimuth = mDeviceForwardAzimuth;
					}

					mDeviceAzimuth = mDeviceForwardAzimuth;
//					logger.debug("**** GPS RECEIVED, mDeviceAzimuth = " + mDeviceAzimuth + "\n");

					gpsReceiverHandler.sendEmptyMessage(0);
				}
				else{
					logger.debug("\n**** GPS RECEIVED, PROCESSING delivery SO SKIPPING ****\n");
				}
			}
			catch(Exception e){
				logger.error("EXCEPTION", e);
			}
		}
	}
}
