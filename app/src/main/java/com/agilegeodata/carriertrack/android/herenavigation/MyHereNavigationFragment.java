package com.agilegeodata.carriertrack.android.herenavigation;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
/*
Deprecated
Use the standard java. util. concurrent or Kotlin concurrency utilities  instead.
*/
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.EmbeddedNavigationActivity;
import com.agilegeodata.carriertrack.android.activities.RouteDetailsActivity;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.fragments.RouteDetailsRightSideFragmentMerged;
import com.agilegeodata.carriertrack.android.objects.DeliveryItem;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.Metadata;
import com.here.sdk.core.UnitSystem;
import com.here.sdk.core.engine.SDKNativeEngine;
import com.here.sdk.core.engine.SDKOptions;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.location.LocationAccuracy;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapFeatureModes;
import com.here.sdk.mapview.MapFeatures;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.here.sdk.navigation.DestinationReachedListener;
import com.here.sdk.navigation.DimensionRestrictionType;
import com.here.sdk.navigation.DistanceType;
import com.here.sdk.navigation.JunctionViewLaneAssistance;
import com.here.sdk.navigation.JunctionViewLaneAssistanceListener;
import com.here.sdk.navigation.Lane;
import com.here.sdk.navigation.LaneRecommendationState;
import com.here.sdk.navigation.ManeuverNotificationListener;
import com.here.sdk.navigation.ManeuverNotificationOptions;
import com.here.sdk.navigation.ManeuverProgress;
import com.here.sdk.navigation.MapMatchedLocation;
import com.here.sdk.navigation.Milestone;
import com.here.sdk.navigation.MilestoneStatus;
import com.here.sdk.navigation.MilestoneStatusListener;
import com.here.sdk.navigation.NavigableLocation;
import com.here.sdk.navigation.NavigableLocationListener;
import com.here.sdk.navigation.RoadTextsListener;
import com.here.sdk.navigation.RouteDeviation;
import com.here.sdk.navigation.RouteDeviationListener;
import com.here.sdk.navigation.RouteProgress;
import com.here.sdk.navigation.RouteProgressListener;
import com.here.sdk.navigation.SectionProgress;
import com.here.sdk.navigation.SpeedLimitOffset;
import com.here.sdk.navigation.SpeedWarningOptions;
import com.here.sdk.navigation.TruckRestrictionWarning;
import com.here.sdk.navigation.TruckRestrictionsWarningListener;
import com.here.sdk.navigation.VisualNavigator;
import com.here.sdk.navigation.WeightRestrictionType;
import com.here.sdk.prefetcher.RoutePrefetcher;
import com.here.sdk.routing.RoadTexts;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Section;
import com.here.sdk.routing.Waypoint;
import com.here.sdk.routing.WaypointType;
import com.here.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyHereNavigationFragment extends Fragment{
	public static final String TAG = MyHereNavigationFragment.class.getSimpleName();
	public static final int DEFAULT_DISTANCE_IN_METERS = 1000 * 2;
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static final int NAV_GROUP_MAX_LIMIT = CTApp.NAVIGATION_ROUTE_SIZE;//CURRENTLY 40
	public static GeoCoordinates DEFAULT_MAP_CENTER = null;//new GeoCoordinates(52.520798, 13.409408);
	private final List<MapMarker> mapMarkerList = new ArrayList<>();
	private final List<MapPolyline> mapPolylines = new ArrayList<>();
	private final String lastDeviationMessage = "";
	private final long lastRouteDeviationEventTime = System.currentTimeMillis();
	public boolean isSoloScreen = false;
	ArrayList<String> voiceAnnouncements = new ArrayList<String>();
	private MapView mapView = null;
	protected BroadcastReceiver mapPinUpdateReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			int[] sequenceNumbers = intent.getIntArrayExtra("SEQUENCE_NUMBERS");
			logger.debug("UPDATE MAP PIN RECEIVER : calling updateDeliveryMapPins() : sequenceNumbers is null " + (sequenceNumbers == null));
			updateDeliveryMapPins(sequenceNumbers);
		}
	};
	private RouteCalculator routeCalculator = null;
	private TextView messageView = null;
	private VisualNavigator visualNavigator = null;
	private HEREPositioningProvider herePositioningProvider = null;
	private HEREPositioningSimulator herePositioningSimulator = null;
	//private DynamicRoutingEngine dynamicRoutingEngine;
	private VoiceAssistant voiceAssistant = null;
	private int previousManeuverIndex = -1;
	private MapMatchedLocation lastMapMatchedLocation;
	private int NAV_GROUP_QUERY_COUNT = 0;
	private LinkedHashMap<Integer, DeliveryItem> CURRENT_NAVIGATION_ROUTE_POINTS;
	private int STARTING_POINT_SEQUENCE_NUMBER_CURRENT_PAGE;
	private int ENDING_POINT_SEQUENCE_NUMBER;
	private int SEQUENCE_INTERVAL;
//	private boolean instructionListShown = false;
	private int JOB_DETAIL_ID;
	//private GestureMapAnimator gestureMapAnimator;
	private boolean listModeUseText = false;
	private RouteDetailsRightSideFragmentMerged routeDetailsRightSideFragmentMerged;
	private boolean newPageCalled = false;
	private Route currentRoute = null;
	private RouteProgress currentRouteProgress = null;
	private RoutePrefetcher routePrefetcher = null;
	private int SEQUENCE_ID_OF_LAST_ROUTE_ITEM = -1;
	private int SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM = -1;
	private Button navSequenceAlertButton = null;
	private LinearLayout navSequenceAlertPanel = null;
	private int deviationCounter = 0;
	private boolean isReturningToRoute = false;
	private RoutingEngine routingEngine = null;
	private long lastNavigableLocationEventTime = System.currentTimeMillis();
	private boolean isVoiceAssistantPlaying = false;
	protected BroadcastReceiver skippedDropReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			Bundle b = intent.getExtras();
			//String alert = b.getString("ALERT_MESSAGE", "Possibly missed/skipped a sequenced delivery.");
			Integer lowestSkippedSequenceNumber = b.getInt("ALERT_DATA");

			if(lowestSkippedSequenceNumber != null && lowestSkippedSequenceNumber > 0){
				if(isSoloScreen){
					logger.info("**** SKIPPED DROP ALERT DISPLAYED FOR drop list");

					navSequenceAlertPanel.setVisibility(View.VISIBLE);
					addVoiceMessageToQueue(true, "Delivery sequence may have been broken.");
					playVoiceMessagesInQueue();
				}
				else{
					logger.info("**** SKIPPED DISPLAYING DROP ALERT FOR DELIVERY list AS NAVIGATION IS NOT SOLO");
				}
			}

			logger.info("**** SKIPPED DROP RECEIVER CALLED");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState){
		logger.info("===--->>> onCreate() : ENTER");

		//=== Usually, you need to initialize the HERE SDK only once during the lifetime of an application.
		initializeHERESDK();

		GeoCoordinates currentLocationInit = null;
		int permissionAccessFineLocation = ContextCompat.checkSelfPermission(MyHereNavigationFragment.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
		if(permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED){
			final AlertDialog.Builder dialog = new AlertDialog.Builder(MyHereNavigationFragment.this.getActivity());
			dialog.setMessage("Please allow access to location services to proceed.")
				  .setPositiveButton("OK", new DialogInterface.OnClickListener(){
					  @Override
					  public void onClick(DialogInterface paramDialogInterface, int paramInt){
						  MyHereNavigationFragment.this.getActivity().finish();
					  }
				  });
			dialog.show();
			return;
		}
		else{
			Location location = CTApp.getLocation();
			currentLocationInit = new GeoCoordinates(location.getLongitude(), location.getLatitude());
		}

		DEFAULT_MAP_CENTER = currentLocationInit;
		this.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			ContextCompat.registerReceiver(getContext(), mapPinUpdateReceiver, new IntentFilter(GlobalConstants.INTENT_UPDATE_NAV_MAP_PINS), ContextCompat.RECEIVER_NOT_EXPORTED);

			ContextCompat.registerReceiver(getContext(), skippedDropReceiver, new IntentFilter(GlobalConstants.INTENT_SKIPPED_DELIVERY), ContextCompat.RECEIVER_NOT_EXPORTED);
		}
		else{
			getContext().registerReceiver(mapPinUpdateReceiver, new IntentFilter(GlobalConstants.INTENT_UPDATE_NAV_MAP_PINS));

			getContext().registerReceiver(skippedDropReceiver, new IntentFilter(GlobalConstants.INTENT_SKIPPED_DELIVERY));
		}

		super.onCreate(savedInstanceState);
	}

/*
	private void setDoubleTapGestureHandler(MapView mapView) {
		mapView.getGestures().setDoubleTapListener(new DoubleTapListener() {
			@Override
			public void onDoubleTap(@NonNull Point2D touchPoint) {
				GeoCoordinates geoCoordinates = mapView.viewToGeoCoordinates(touchPoint);
				Log.d(TAG, "Default zooming in is disabled. DoubleTap at: " + geoCoordinates);

				// Start our custom zoom in animation.
				gestureMapAnimator.zoomIn(touchPoint);
			}
		});
	}

	private void setTwoFingerTapGestureHandler(MapView mapView) {
		mapView.getGestures().setTwoFingerTapListener(new TwoFingerTapListener() {
			@Override
			public void onTwoFingerTap(@NonNull Point2D touchCenterPoint) {
				GeoCoordinates geoCoordinates = mapView.viewToGeoCoordinates(touchCenterPoint);
				Log.d(TAG, "Default zooming in is disabled. TwoFingerTap at: " + geoCoordinates);

				// Start our custom zoom out animation.
				gestureMapAnimator.zoomOut(touchCenterPoint);
			}
		});
	}
*/

	@Override
	public void onPause(){
		logger.info("===--->>> onPause() : ENTER");

		//=== FROM HERE MainActivity ====================
		mapView.onPause();

		voiceAssistant.flushQueue();

		super.onPause();
	}

	@Override
	public void onStop(){
		logger.info("===--->>> onStop() : ENTER");

		if(!isSoloScreen){
			routeDetailsRightSideFragmentMerged.useListTextToSpeech = listModeUseText;
		}

		super.onStop();
	}

	@Override
	public void onDestroy(){
		//=== FROM MainActivity.java
		detach();

		mapView.onDestroy();
		this.visualNavigator.stopRendering();

		disposeHERESDK();

		logger.info("===--->>> onDestroy() : ENTER");

		if(mapPinUpdateReceiver != null){
			getActivity().unregisterReceiver(mapPinUpdateReceiver);
			mapPinUpdateReceiver = null;
		}

		if(skippedDropReceiver != null){
			getActivity().unregisterReceiver(skippedDropReceiver);
			skippedDropReceiver = null;
		}

		if(MyHereNavigationFragment.this.getActivity().isFinishing()){
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
		}

		if(isSoloScreen){
			RouteDetailsActivity.instance.navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_OFF;
		}

		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		logger.info("===--->>> onCreateView() : ENTER");

		super.onCreateView(inflater, container, savedInstanceState);

		View fragmentLayout = inflater.inflate(R.layout.activity_embedded_navigation, container, false);

		return fragmentLayout;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		logger.info("===--->>> onViewCreated() : ENTER");

		try{
			routingEngine = new RoutingEngine();
		}
		catch(InstantiationErrorException e){
			throw new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
		}

		//==== HOOK UP BUTTONS HERE INSTEAD OF XML LAYOUT
		navSequenceAlertButton = this.getActivity().findViewById(R.id.navigationAlertButton);
		navSequenceAlertButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				navSequenceAlertPanel.setVisibility(View.GONE);
			}
		});

		navSequenceAlertPanel = this.getActivity().findViewById(R.id.navSequenceAlertPanel);
		navSequenceAlertPanel.setVisibility(View.GONE);

		TextView isInSimulationTextView = this.getActivity().findViewById(R.id.isInSimulation_view);
		isInSimulationTextView.setText(CTApp.isInSimulation ? "Simulation On" : "Simulation Off");
		isInSimulationTextView.setVisibility(CTApp.isInSimulation ? View.VISIBLE : View.GONE);

		// Get a MapView instance from layout.
		mapView = this.getActivity().findViewById(R.id.map_view);
		mapView.onCreate(savedInstanceState);

		//=== Get a TextView instance from layout to show selected log messages.
		messageView = this.getActivity().findViewById(R.id.message_view);
		//=== Making the textView scrollable.
		messageView.setMovementMethod(new ScrollingMovementMethod());

		//=== SETUP ZOOM TAPS
/*
		gestureMapAnimator = new GestureMapAnimator(mapView.getCamera());
		mapView.getGestures().disableDefaultAction(GestureType.DOUBLE_TAP);
		mapView.getGestures().disableDefaultAction(GestureType.TWO_FINGER_TAP);
//		setDoubleTapGestureHandler(mapView);
//		setTwoFingerTapGestureHandler(mapView);

		mapView.getGestures().setDoubleTapListener(new DoubleTapListener() {
			@Override
			public void onDoubleTap(@NonNull Point2D touchPoint) {
				// Start our custom zoom in animation.
				MapCamera camera = mapView.getCamera();
				double zoom = camera.getState().zoomLevel;
				MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, mapView.getCamera().getState().zoomLevel + 1d);
				mapView.getCamera().lookAt(mapView.getCamera().getState().targetCoordinates, mapMeasureZoom);
//				gestureMapAnimator.zoomIn(touchPoint);
			}
		});

		mapView.getGestures().setTwoFingerTapListener(new TwoFingerTapListener() {
			@Override
			public void onTwoFingerTap(@NonNull Point2D touchCenterPoint) {
				// Start our custom zoom out animation.
				MapCamera camera = mapView.getCamera();
				double zoom = camera.getState().zoomLevel;
				MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, mapView.getCamera().getState().zoomLevel - 1d);
				mapView.getCamera().lookAt(mapView.getCamera().getState().targetCoordinates, mapMeasureZoom);
//				gestureMapAnimator.zoomOut(touchCenterPoint);
			}
		});
*/

		loadMapScene();

		MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, DEFAULT_DISTANCE_IN_METERS * 2);
		mapView.getCamera().lookAt(DEFAULT_MAP_CENTER, mapMeasureZoom);

		routeCalculator = new RouteCalculator();

		//=== A class to receive real location events.
		herePositioningProvider = new HEREPositioningProvider();
		//=== A class to receive simulated location events.
		herePositioningSimulator = new HEREPositioningSimulator();

		try{
			//=== Without a route set, this starts tracking mode.
			visualNavigator = new VisualNavigator();
		}
		catch(InstantiationErrorException e){
			throw new RuntimeException("Initialization of VisualNavigator failed: " + e.error.name());
		}

		//=== A helper class for TTS.
		voiceAssistant = new VoiceAssistant(this.getActivity());

		if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
		   ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
			logger.debug("no location permissions");
		}
		else{
			routePrefetcher = new RoutePrefetcher(SDKNativeEngine.getSharedInstance());

			Location location = CTApp.getLocation();
			GeoCoordinates currentLocationInit = new GeoCoordinates(location.getLatitude(), location.getLongitude());
			routePrefetcher.prefetchAroundLocation(currentLocationInit);
		}

		//=== This enables a navigation view including a rendered navigation arrow.
//		CameraSettings cameraSettings = visualNavigator.getCameraSettings();
		visualNavigator.startRendering(mapView);
		//=== By default, this is enabled.
//		startCameraTracking();

		//=== USED PRIMARILY FOR RE-ROUTING TO LESS TRAFFIC
//createDynamicRoutingEngine();

		setupVoiceGuidance();
		setupListeners();

		startLocationProvider();

/*
		deliveryInstructionsPanel = view.findViewById(R.id.delivery_instructions_panel);
		deliveryInstructionsPanel.setVisibility(View.GONE);

		deliveryInstructions = view.findViewById(R.id.delivery_instructions);
		deliveryInstructions.setMovementMethod(new ScrollingMovementMethod());

		anchorDeliveryInstructionsCheckButton = view.findViewById(R.id.instructionsCheckButton);
		anchorDeliveryInstructionsCheckButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				if(isChecked){
					deliveryInstructions.setLines(8);
				}
				else{
					deliveryInstructions.setLines(3);
				}
			}
		});
*/

		onViewCreatedContinue();

		messageView.setText("Initialization completed.");

		List<Waypoint> route = fetchRoute(JOB_DETAIL_ID, STARTING_POINT_SEQUENCE_NUMBER_CURRENT_PAGE);

		calculateRouteWithWaypoints(route);
	}

	private void onViewCreatedContinue(){
		logger.info("===--->>> onViewCreatedContinue() : ENTER");

		Bundle extras = this.getActivity().getIntent().getExtras();
		isSoloScreen = this.getActivity() instanceof EmbeddedNavigationActivity;

		if(isSoloScreen){
			SEQUENCE_INTERVAL = extras.getInt(GlobalConstants.EXTRA_SEQUENCE_INTERVAL);
			STARTING_POINT_SEQUENCE_NUMBER_CURRENT_PAGE = extras.getInt(GlobalConstants.EXTRA_JOBDETAILSEQUENCE);
			JOB_DETAIL_ID = extras.getInt(GlobalConstants.EXTRA_JOBDETAILID);
		}
		else{
			routeDetailsRightSideFragmentMerged = (RouteDetailsRightSideFragmentMerged) this.getFragmentManager()/*.getParentFragmentManager()*/.findFragmentByTag(RouteDetailsActivity.RIGHT_SIDE_FRAGMENT_TAG);

			SEQUENCE_INTERVAL = routeDetailsRightSideFragmentMerged.SEQUENCE_ID_INTERVAL;
			STARTING_POINT_SEQUENCE_NUMBER_CURRENT_PAGE = routeDetailsRightSideFragmentMerged.SEQUENCE_ID_OF_LAST_DELIVERY;
			JOB_DETAIL_ID = routeDetailsRightSideFragmentMerged.mJobDetailId;

			listModeUseText = routeDetailsRightSideFragmentMerged.useListTextToSpeech;
			routeDetailsRightSideFragmentMerged.useListTextToSpeech = false;
		}
	}

	public void updateDeliveryMapPins(int[] sequenceNumbers){
		//=== TO REMOVE MAP PINS FOR WAYPOINTS THAT HAVE BEEN PROCESSED
		logger.info("$$$ HERE UPDATE DELIVERY MAP PINS for sequence #s : sequenceNumbers.length = " + (sequenceNumbers == null ? "NULL" : sequenceNumbers.length));

		final int[] sequences = sequenceNumbers;
		this.getActivity().runOnUiThread(
				new Runnable(){
					@Override
					public void run(){
						(new Handler()).postDelayed(new Runnable(){
							public void run(){
								logger.info("$$$ HERE UPDATE DELIVERY MAP PINS thread : sequences.length = " + (sequences == null ? "NULL" : sequences.length));
								//logger.info("$$$ HERE UPDATE DELIVERY MAP PINS thread : isSoloScreen = " + isSoloScreen);

								//=== CRASHLYTICS SAYS ACTIVITY MAY NOT BE AVAILABLE
								try{
									// === RESET ONLY MAP PINS THAT WERE MARKED AS DELIVERED
									logger.info("$$$ HERE delayed UPDATE MAP PINS : CHANGING MAP PINS");

									for(int index1 = 0; index1 < sequences.length; index1++){
										int sequenceForChangedDrop = sequences[index1];
										//logger.info("$$$ HERE delayed UPDATE MAP PINS thread : CHANGING MAP PINS : sequenceForChangedDrop # = " + sequenceForChangedDrop);

										for(int index = 0; index < MyHereNavigationFragment.this.mapMarkerList.size() - 1; index++){
											MapMarker mapMarker = MyHereNavigationFragment.this.mapMarkerList.get(index);
											int sequence = mapMarker.getMetadata().getInteger("SEQUENCE_NUMBER").intValue();
											//logger.info("$$$ HERE delayed UPDATE MAP PINS thread : CHANGING MAP PINS : mapmarker sequence = " + sequence);

											if(sequence == sequenceForChangedDrop){
												//=== POSSIBLE ADD PACKAGES ETC. HERE FOR RETRIEVAL ON MAP PIN UPDATE FOR VOICE CONTENT
												String address = mapMarker.getMetadata().getString("STREET_ADDRESS");
												logger.info("$$$ HERE delayed UPDATE MAP PINS thread : CHANGING MAP PINS : mapmarker address = " + address);
												String products = mapMarker.getMetadata().getString("PRODUCTS");
												logger.info("$$$ HERE delayed UPDATE MAP PINS thread : CHANGING MAP PINS : mapmarker products = " + products);

												//String deliveryType = mapMarker.getMetadata().getString("DELIVERY_TYPE");
												//logger.info("$$$ HERE delayed UPDATE MAP PINS thread : CHANGING MAP PINS : mapmarker deliveryType = " + deliveryType);

												String deliveryInstructions = mapMarker.getMetadata().getString("DELIVERY_INSTRUCTIONS");
												logger.info("$$$ HERE delayed UPDATE MAP PINS thread : CHANGING MAP PINS : mapmarker deliveryInstructions = " + deliveryInstructions);

												mapView.getMapScene().removeMapMarker(mapMarker);
												MapImage mapImage = MapImageFactory.fromResource(MyHereNavigationFragment.this.getResources(), R.drawable.map_marker_delivered);
												mapMarker.setImage(mapImage);
												mapView.getMapScene().addMapMarker(mapMarker);

												MyHereNavigationFragment.this.mapMarkerList.set(index, mapMarker);
												//logger.info("$$$ HERE delayed UPDATE MAP PINS thread : CHANGING MAP PIN FOUND for index " + index + " : sequenceForChangedDrop # = " + sequenceForChangedDrop);
												break;
											}
										}
									}
								}
								catch(Exception e){
									logger.error("EXCEPTION : " + e.getMessage());
								}
							}
						}, 3000);   //=== WAIT SO IT IS NOT CHANGED IMMEDIATELY,
						//=== after you drive by it and is an arrival
					}
				}
			);

		//logger.info("$$$ HERE UPDATE DELIVERY MAP PINS thread EXIT : isSoloScreen = " + isSoloScreen);
	}

//===============================================
//=== HOOK THIS INTO HERE LISTENERS SOMEWHERE
//===============================================
//	@SuppressLint("RestrictedApi")
//	@Override
/*
	public void onInstructionListVisibilityChanged(boolean shown){
		instructionListShown = shown;
		//speedWidget.setVisibility(shown ? View.GONE : View.VISIBLE);

		if(instructionListShown){
			//fabNightModeToggle.hide();
			deliveryInstructionsPanel.setVisibility(View.GONE);
		}
		else{// if(bottomSheetVisible){
			//fabNightModeToggle.show();
			deliveryInstructionsPanel.setVisibility(View.VISIBLE);
		}
	}
*/

//	private long timer = 0;
//	private CheckBox anchorDeliveryInstructionsCheckButton = null;
//	private LinearLayout deliveryInstructionsPanel = null;
//	private TextView deliveryInstructions = null;

/*
	private void handleDeliveryInstructions(String instructions){
		long currentTime = System.currentTimeMillis();
		float timerDiff = (currentTime - timer) / 1000f;
		String currentInstructions = deliveryInstructions.getText().toString();
		boolean isPreviousInstruction = currentInstructions.contains(instructions);
		boolean isVisible = (deliveryInstructionsPanel.getVisibility() == View.VISIBLE);
		boolean isAnchored = anchorDeliveryInstructionsCheckButton.isChecked();

		try{
//			logger.info("$$$ MAPBOX handleDeliveryInstructions() timerDiff = " + timerDiff);
//			logger.info("$$$ MAPBOX handleDeliveryInstructions() isVisible = " + isVisible);
//			logger.info("$$$ MAPBOX handleDeliveryInstructions() isPreviousInstruction = " + isPreviousInstruction);
//			logger.info("$$$ MAPBOX handleDeliveryInstructions() isAnchored = " + isAnchored);

			if(!isPreviousInstruction){
//				logger.info("$$$ MAPBOX handleDeliveryInstructions() new instructions = " + instructions);

				timer = System.currentTimeMillis();
				deliveryInstructionsPanel.setVisibility(View.VISIBLE);

				MyHereNavigationFragment.this.getActivity().runOnUiThread(
						new Runnable(){
							@Override
							public void run(){
								deliveryInstructions.setText(new StringBuilder().append(instructions)
																				.append("------------------------------------")
																				.append("\n")
																				.append(deliveryInstructions.getText()).toString());
								deliveryInstructions.setScrollY(0);
							}
						});
			}
			else{
				if(isVisible){
					if(timerDiff > 8.0f){
						if(isPreviousInstruction){
							if(!isAnchored){
								MyHereNavigationFragment.this.getActivity().runOnUiThread(
										new Runnable(){
											@Override
											public void run(){
												deliveryInstructionsPanel.setVisibility(View.INVISIBLE);
											}
										});
//								logger.info("$$$ MAPBOX handleDeliveryInstructions() : HIDING : is visible, timerDiff > 8.0, is previous instruction, not anchored");
							}
							else{
//								logger.info("$$$ MAPBOX handleDeliveryInstructions() : LEAVING AS IS : is visible, timerDiff > 8.0, not previous instruction, anchored");
							}

							timer = System.currentTimeMillis();
						}
						else{
//							logger.info("$$$ MAPBOX handleDeliveryInstructions() : LEAVING AS IS : is visible, timerDiff > 8.0, not previous instruction");
						}
					}
					else{
//						logger.info("$$$ MAPBOX handleDeliveryInstructions() : LEAVING AS IS : is visible, timerDiff < 8.0");
					}
				}
				else{
					if(timerDiff > 8.0f){
						MyHereNavigationFragment.this.getActivity().runOnUiThread(
								new Runnable(){
									@Override
									public void run(){
										deliveryInstructionsPanel.setVisibility(View.VISIBLE);
									}
								});

						timer = System.currentTimeMillis();

//						logger.info("$$$ MAPBOX handleDeliveryInstructions() : SHOWING : not visible, timerDiff > 8.0");
					}
					else{
//						logger.info("$$$ MAPBOX handleDeliveryInstructions() : LEAVING AS IS : not visible, timerDiff < 8.0");
					}
				}
			}
		}
		catch(Exception e){
			logger.info("$$$ HERE handleDeliveryInstructions() EXCEPTION = " + e.getMessage());
		}
	}
*/

//===============================================
//=== HOOK THIS INTO HERE LISTENERS SOMEWHERE
//===============================================
//	@Override
/*
	public BannerInstructions willDisplay(BannerInstructions instructions){
		//===THIS IS CALLED APPROX. EVERY SECOND
		String deliveryText = null;
		BannerInstructions newBannerInstructions = instructions;

		try{
			if(instructions != null){
				logger.info("$$$ HERE willDisplay triggered $$$$$$$$$$ INCOMING $$$$$$$$$$$$$$$$" +
							"\nPRIMARY " + ((instructions == null || instructions.primary() == null) ? " is NULL" : instructions.primary().text()) +
							"\nSECONDARY" + ((instructions == null || instructions.secondary() == null) ? " is NULL" : instructions.secondary().text()) +
							"\nSUB" + ((instructions == null || instructions.sub() == null) ? " is NULL" : instructions.sub().text()));

				if(instructions.primary() == null){
					logger.info("$$$ HERE will display instructions.primary() is NULL ");
				}
				else{
					//===HANDLING DELIVERY INSTRUCTIONS IN A DIFFERENT WINDOW
					//===SO EXTRACT IT HERE AND PASS IT ON.
					if(!instructions.primary().text().contains("\u00A0")){
						logger.info("$$$ HERE will display instructions.primary().text() DOES NOT CONTAIN split char");
					}
					else{
						String[] splits = instructions.primary().text().split("\u00A0");
						logger.info("$$$ HERE will display instructions primary splits count is " + splits.length);
						for(int i = 0; i < splits.length; i++){
							logger.info("$$$ HERE will display instructions primary split[" + i + "] = " + splits[i]);
						}

						if(splits.length <= 3){
							logger.info("$$$ HERE will display instructions primary splits count is TOO LOW");
						}
						else{
							String addressText = splits[0] == null ? "" : splits[0];
							String jobTypeText = splits[1] == null ? "" : splits[1];
							String productsText = splits[2] == null ? "" : splits[2].equals("\u002c") ? "" : splits[2];
							String instructionsText = splits[3] == null ? "" : splits[3].equals("\u002c") ? "" : splits[3];

							deliveryText = jobTypeText + " at " + addressText + " " + (productsText.isEmpty() ? "" : productsText) + (instructionsText.isEmpty() ? "\n" : instructionsText + "\n");

							List bannerComponents = instructions.primary().components();
							if(bannerComponents == null || bannerComponents.size() < 1){
								logger.info("$$$ HERE will display primary bannerComponents.size() is TOO LOW");
							}
							else{
								BannerComponents bannerComponent = instructions.primary().components().get(0).toBuilder().text(addressText).build();//.abbreviation(addressText).build(); DOES NOT WORK, AS DOCUMENTED IN MAPBOX pr #1233

								if(bannerComponent == null){
									logger.info("$$$ HERE will display bannerComponent is NULL");
								}
								else{
									bannerComponents.set(0, bannerComponent);

									//===PRIMARY TEXT IS NEVER ALTERED/CHANGED, TKV, 3MAR20
									instructions.primary().toBuilder().components(bannerComponents).text(addressText).build();
									newBannerInstructions = instructions.toBuilder().build();

									logger.info("$$$ HERE willDisplay triggered $$$$$$$$$ OUTGOING $$$$$$$$$$$$$$$$$" +
												"\nPRIMARY " + ((newBannerInstructions == null || newBannerInstructions.primary() == null) ? " is NULL" : newBannerInstructions.primary().text()) +
												"\nSECONDARY" + ((newBannerInstructions == null || newBannerInstructions.secondary() == null) ? " is NULL" : newBannerInstructions.secondary().text()) +
												"\nSUB" + ((newBannerInstructions == null || newBannerInstructions.sub() == null) ? " is NULL" : newBannerInstructions.sub().text()));

									logger.info("$$$ MAPBOX will display deliveryText = \n" + (deliveryText == null ? "NULL" : deliveryText));
									this.handleDeliveryInstructions(deliveryText);
								}
							}
						}
					}
				}
			}
			else{
				logger.info("$$$ HERE will display instructions is NULL");
			}
		}
		catch(Exception e){
			logger.info("$$$ HERE will display EXCEPTION = " + e.getMessage());
		}

		return newBannerInstructions;
	}
*/

	public void navigationRouteFinished(){
		logger.info("$$$ HERE navigationRouteFinished() : ENTER : SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM=" + SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM + " ENDING_POINT_SEQUENCE_NUMBER=" + ENDING_POINT_SEQUENCE_NUMBER + " SEQUENCE_ID_OF_LAST_ROUTE_ITEM=" + SEQUENCE_ID_OF_LAST_ROUTE_ITEM);

		try{
			//=== IF NOT ALREADY PROCESSING A PAGE FETCH
			if(newPageCalled){
				logger.info("HERE NAVIGATION IS PROCESSING A PAGE FETCH : newPageCalled already so SKIP PAGING");
			}
			//=== IF NOT HAVE ALREADY CALLED A PAGE FETCH
			//=== onArrival may be called several times for a given waypoint
			//=== so do not call additional page fetches
			else{
				if(MyHereNavigationFragment.this.getActivity().isFinishing()){
					logger.info("HERE NAVIGATION IS FINISHING : SKIP PAGING");

					newPageCalled = false;
				}
				else{
					logger.info("HERE NAVIGATION END OF ROUTE REACHED");
					String message = "End of navigation route. Navigation is halted.";
					messageView.setText(message);

					addVoiceMessageToQueue(true, message);
					playVoiceMessagesInQueue();

					newPageCalled = false;

					stopNavigation();
				}
			}
		}
		catch(Exception e){
			logger.error("HERE EXCEPTION " + e.getMessage());
		}
	}

/*
	private String getJobTypeMarkerName(int jobType){
		String markerName = "";

		switch(jobType){
			case 1:
				markerName = "map_marker_vip_do_not_deliver";
				break;
			case 2:
				markerName = "map_marker_subscriber";
				break;
			case 3:
				markerName = "map_marker_do_not_deliver";
				break;
			case 4:
				markerName = "map_marker_cannot_deliver";
				break;
			case 5:
				markerName = "map_marker_must_deliver";
				break;
			case 6:
				markerName = "map_marker_deliver";
				break;
		}

		return markerName;
	}
*/

	public void getNewNavigationPage(){
		logger.info("$$$ HERE getNewNavigationPage() : ENTER : SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM=" + SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM + " ENDING_POINT_SEQUENCE_NUMBER=" + ENDING_POINT_SEQUENCE_NUMBER + " SEQUENCE_ID_OF_LAST_ROUTE_ITEM=" + SEQUENCE_ID_OF_LAST_ROUTE_ITEM);

		try{
			//=== IF NOT ALREADY PROCESSING A PAGE FETCH
			if(newPageCalled){
				logger.info("HERE NAVIGATION IS PROCESSING A PAGE FETCH : newPageCalled already so SKIP PAGING");
			}
			//=== IF NOT HAVE ALREADY CALLED A PAGE FETCH
			//=== onArrival may be called several times for a given waypoint
			//=== so do not call additional page fetches
			else{
				if(MyHereNavigationFragment.this.getActivity().isFinishing()){
					logger.info("HERE NAVIGATION IS FINISHING : SKIP PAGING");

					newPageCalled = false;
					logger.info("HERE newPageCalled =  " + newPageCalled);
				}
				else{
					String message = "Please stop while the next navigation set is loaded.";
					messageView.setText(message);

					addVoiceMessageToQueue(true, message);
					playVoiceMessagesInQueue();

					//=== MET ALL CONDITIONS, GET A NEW PAGE/ROUTE
					logger.info("HERE NAVIGATION PAGING : getting new page");
					newPageCalled = true;

					STARTING_POINT_SEQUENCE_NUMBER_CURRENT_PAGE = ENDING_POINT_SEQUENCE_NUMBER + SEQUENCE_INTERVAL;
					logger.info("HERE NEW WAYPOINT PAGE : fetchRoute() CALLED");

					clearMapKeepNavigation();
					List<Waypoint> route = fetchRoute(JOB_DETAIL_ID, STARTING_POINT_SEQUENCE_NUMBER_CURRENT_PAGE);
					calculateRouteWithWaypoints(route);
				}
			}
		}
		catch(Exception e){
			logger.error("HERE EXCEPTION " + e.getMessage());
		}
	}

	@Override
	public void onStart(){
		logger.info("===--->>> onStart() : ENTER");

		super.onStart();
	}

	@Override
	public void onResume(){
		logger.info("===--->>> onResume() : ENTER");

		mapView.onResume();

		String announcement = "Navigation is on.";

		addVoiceMessageToQueue(false, announcement);
		playVoiceMessagesInQueue();

		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
	}

	public List<Waypoint> fetchRoute(int jobDetailId, int startingRouteDetailSequenceId){
		logger.info("$$$ HERE fetch route called");
		logger.info("newPageCalled =  " + newPageCalled);

		GeoCoordinates currentLocationInit = null;
		int permissionAccessFineLocation = ContextCompat.checkSelfPermission(MyHereNavigationFragment.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
		if(permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED){
			final AlertDialog.Builder dialog = new AlertDialog.Builder(MyHereNavigationFragment.this.getActivity());
			dialog.setMessage("Please allow access to location services to proceed.")
				  .setPositiveButton("OK", new DialogInterface.OnClickListener(){
					  @Override
					  public void onClick(DialogInterface paramDialogInterface, int paramInt){
						  MyHereNavigationFragment.this.getActivity().finish();
					  }
				  });
			dialog.show();
			return null;
		}
		else{
			Location location = CTApp.getLocation();
			currentLocationInit = new GeoCoordinates(location.getLatitude(), location.getLongitude());
		}

		CURRENT_NAVIGATION_ROUTE_POINTS = DBHelper.getInstance().fetchRouteDetailsForNavigationForJobDetailIdStartAtSequence_Sequenced(jobDetailId, startingRouteDetailSequenceId - 1, NAV_GROUP_MAX_LIMIT, NAV_GROUP_MAX_LIMIT, currentLocationInit);

		//=== points in CURRENT_NAVIGATION_ROUTE_POINTS
		ArrayList<Waypoint> wayPoints = new ArrayList<Waypoint>();

		if(CURRENT_NAVIGATION_ROUTE_POINTS == null || CURRENT_NAVIGATION_ROUTE_POINTS.size() <= 0){
			logger.info("$$$ HERE fetch route NO POINTS FETCHED for starting sequence of " + startingRouteDetailSequenceId);
		}
		else{
			try{
				NAV_GROUP_QUERY_COUNT = CURRENT_NAVIGATION_ROUTE_POINTS.size();

				Collection<DeliveryItem> routeDetails = CURRENT_NAVIGATION_ROUTE_POINTS.values();
				ArrayList<DeliveryItem> navPoints = new ArrayList<DeliveryItem>(routeDetails);

				DeliveryItem endPoint = navPoints.get(NAV_GROUP_QUERY_COUNT - 1);
				ENDING_POINT_SEQUENCE_NUMBER = endPoint.getSequence();
				logger.info("$$$ HERE fetch route STARTING_POINT_SEQUENCE_NUMBER_CURRENT_PAGE = " + startingRouteDetailSequenceId + " ENDING_POINT_SEQUENCE_NUMBER = " + ENDING_POINT_SEQUENCE_NUMBER);

				SEQUENCE_ID_OF_LAST_ROUTE_ITEM = DBHelper.getInstance().fetchLargestSequenceForJobDetailId_Sequenced(jobDetailId);

				clearWaypointMapMarker();

				//=== ADD CURRENT LOCATION AS FIRST WAYPOINT
				Metadata metaData = new Metadata();
				metaData.setInteger("SEQUENCE_NUMBER", 0);
				metaData.setString("STREET_ADDRESS", "starting point");
				metaData.setString("PRODUCTS", "");
				metaData.setString("DELIVERY_TYPE", "no delivery");
				metaData.setString("DELIVERY_INSTRUCTIONS", "no delivery");

				int resourceId = getJobTypeMarkerResourceId(8);    //location pin
				addDeliveryMapMarker(currentLocationInit, resourceId, metaData);

				//=== ADD CURRENT LOCATION AS FIRST WAYPOINT
				Waypoint originPoint = new Waypoint(currentLocationInit);
				//=== MUST BE STOPOVER FOR FIRST WAYPOINT
				originPoint.type = WaypointType.STOPOVER;
//				originPoint.sideOfStreetHint = currentLocationInit;
//				originPoint.matchSideOfStreet = MatchSideOfStreet.ALWAYS;
//				originPoint.minCourseDistanceInMeters = 15;
//				originPoint.transitRadiusInMeters = 30;
				//=== DURATION >0 CAUSES A ROUTE CALCULATION ERROR for the first point i.e. origin
				originPoint.duration = Duration.ofSeconds(0L);

				wayPoints.add(0, originPoint);

				//=== add the points from CURRENT_NAVIGATION_ROUTE_POINTS
				for(int index = 0; index < navPoints.size(); index++){
					//=== ADD WAYPOINTS TO ROUTE BUILDER
					GeoCoordinates newCoords2 = new GeoCoordinates(navPoints.get(index).getLocation().getLatitude(), navPoints.get(index).getLocation().getLongitude());
					Waypoint newWaypoint = new Waypoint(newCoords2);
					//=== MUST BE STOPOVER TYPE FOR MILESTONE EVENTS TO BE SENT
					newWaypoint.type = WaypointType.STOPOVER;
//					newWaypoint.sideOfStreetHint = newCoords2;
//					newWaypoint.matchSideOfStreet = MatchSideOfStreet.ALWAYS;
//					newWaypoint.minCourseDistanceInMeters = 15;
//					newWaypoint.transitRadiusInMeters = 30;
					//=== THIS CAUSES A ROUTE CALCULATION ERROR for the first point i.e. origin
					newWaypoint.duration = Duration.ofSeconds(1L);

					//=== ADD MAP PIN MARKERS TO MAP
					int jobType = navPoints.get(index).getJobType();

					int resourceId2 = getJobTypeMarkerResourceId(jobType);

					//=== CHANGE MAP PIN FOR LAST DROP/DESTINATION
					if(index == navPoints.size() - 1){
						//=== MUST BE STOPOVER FOR LAST DESTINATION WAYPOINT
						//newWaypoint.type = WaypointType.STOPOVER;
						//=== THIS CAUSES A ROUTE CALCULATION ERROR for the first point i.e. origin
						newWaypoint.duration = Duration.ofSeconds(0L);

						resourceId2 = getJobTypeMarkerResourceId(7);    //DESTINATION PIN
					}

					wayPoints.add(newWaypoint);

					Metadata metaData2 = new Metadata();
					metaData2.setInteger("SEQUENCE_NUMBER", Integer.valueOf(navPoints.get(index).getSequence()));
					//=== POSSIBLE ADD PACKAGES ETC. HERE FOR RETRIEVAL ON MAP PIN UPDATE FOR VOICE CONTENT
					metaData2.setString("STREET_ADDRESS", navPoints.get(index).getGpsLocationAddressNumber() + " " + navPoints.get(index).getGpsLocationAddressStreet());
					metaData2.setString("PRODUCTS", navPoints.get(index).getProductsAsText());
//logger.debug("fetchRoute() : products = " + navPoints.get(index).getProductsAsText());
					metaData2.setString("DELIVERY_TYPE", getJobTypeText(navPoints.get(index).getJobType()));
					metaData2.setString("DELIVERY_INSTRUCTIONS", navPoints.get(index).getNotes());
//logger.debug("fetchRoute() : deliveryInstructions = " + navPoints.get(index).getNotes());

					addDeliveryMapMarker(newCoords2, resourceId2, metaData2);
				}

				logger.info("$$$ HERE fetch route : GET NEW ROUTE CALLED");
			}
			catch(Exception e){
				logger.error("$$$ EXCEPTION HERE fetch route : " + e.getMessage());
			}
		}

		return wayPoints;
	}

	private int getJobTypeMarkerResourceId(int jobType){
		int resourceId = 0;

		switch(jobType){
			case 1:
				resourceId = R.drawable.map_marker_vip_do_not_deliver;
				break;
			case 2:
				resourceId = R.drawable.map_marker_subscriber;
				break;
			case 3:
				resourceId = R.drawable.map_marker_do_not_deliver;
				break;
			case 4:
				resourceId = R.drawable.map_marker_cannot_deliver;
				break;
			case 5:
				resourceId = R.drawable.map_marker_must_deliver;
				break;
			case 6:
				resourceId = R.drawable.map_marker_deliver;
				break;
			case 7:
				resourceId = R.drawable.map_marker_destination;
				break;
			case 8:
				resourceId = R.drawable.location;
				break;
		}

		return resourceId;
	}

	private String getJobTypeText(int jobType){
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
			text = "VIP do not deliver";
		}

		return text;
	}

	//=== Calculate the static route that is represented by a list of Waypoints.
	public void calculateRouteWithWaypoints(List<Waypoint> cannedRoute){
		routeCalculator.calculateRouteWithWaypoints(cannedRoute, (routingError, routes) -> {
			if(routingError == null){
				currentRoute = routes.get(0);
				//logRouteDetails(currentRoute);
				showRouteOnMap(currentRoute);
				startNavigation(currentRoute, CTApp.isInSimulation);

				newPageCalled = false;
			}
			else{
				showDialog("Error while calculating a route with waypoints:", routingError.toString());
			}
		});
	}

	private void logRouteProgress(RouteProgress routeProgress){
		int sectionIndex = routeProgress.sectionIndex;

		logger.debug("ROUTE PROGRESS : section index = " + sectionIndex);

		for(int i = 0; i < routeProgress.sectionProgress.size(); i++){
			SectionProgress sectionProgress = routeProgress.sectionProgress.get(i);
			logger.debug("SECTION PROGRESS " + i + " remaining Distance In feet = " + sectionProgress.remainingDistanceInMeters);
		}
	}

	private void logRouteDetails(Route route){
		//=== long estimatedTravelTimeInSeconds = route.getDuration().getSeconds();
		int lengthInMeters = route.getLengthInMeters();

		logger.debug("ROUTE DETAILS : length = " + lengthInMeters);

		for(int i = 0; i < route.getSections().size(); i++){
			Section section = route.getSections().get(i);
			logger.debug("SECTION " + i + " length = " + (section.getLengthInMeters() * 3.28) + " feet.");
			logger.debug("SECTION " + i + " arrival place = " + section.getArrivalPlace().name);
		}
	}

	public void importRoute(List<Waypoint> cannedRoute){
		List<com.here.sdk.core.Location> locationList = new ArrayList<com.here.sdk.core.Location>();
		for(int i = 0; i < cannedRoute.size(); i++){
			Waypoint wp = cannedRoute.get(i);
			com.here.sdk.core.Location loc = new com.here.sdk.core.Location(new GeoCoordinates(wp.coordinates.latitude, wp.coordinates.longitude));
			locationList.add(loc);
		}
		routeCalculator.importRoute(locationList, (routingError, routes) -> {
			if(routingError == null){
				currentRoute = routes.get(0);
				showRouteOnMap(currentRoute);
				startNavigation(currentRoute, CTApp.isInSimulation);

				newPageCalled = false;
			}
			else{
				showDialog("Error while calculating a route with waypoints:", routingError.toString());
			}
		});
	}

	private void showRouteOnMap(Route route){
		//=== Show route as polyline.
		GeoPolyline routeGeoPolyline = route.getGeometry();
		float widthInPixels = 24;
		MapPolyline routeMapPolyline = new MapPolyline(routeGeoPolyline,
													   widthInPixels,
													   Color.valueOf(0, 0.56f, 0.54f, 0.63f)); // RGBA
		mapView.getMapScene().addMapPolyline(routeMapPolyline);
		mapPolylines.add(routeMapPolyline);
	}

	public void clearMapStopNavigation(){
		clearWaypointMapMarker();
		clearRoute();

		stopNavigation();
	}

	/*
	private void setLongPressGestureHandler() {
		mapView.getGestures().setLongPressListener((gestureState, touchPoint) -> {
			GeoCoordinates geoCoordinates = mapView.viewToGeoCoordinates(touchPoint);
			if (geoCoordinates == null) {
				return;
			}
			if (gestureState == GestureState.BEGIN) {
				clearWaypointMapMarker();
				clearRoute();
				destinationWaypoint = new Waypoint(geoCoordinates);
				addCircleMapMarker(geoCoordinates, R.drawable.green_dot);
				isLongpressDestination = true;
				messageView.setText("New long press destination set.");
			}
		});
	}
	*/

	public void clearMapKeepNavigation(){
		clearWaypointMapMarker();
		clearRoute();
	}

	private void clearWaypointMapMarker(){
		for(MapMarker mapMarker : mapMarkerList){
			mapView.getMapScene().removeMapMarker(mapMarker);
		}
		mapMarkerList.clear();
	}

	private void clearRoute(){
		for(MapPolyline mapPolyline : mapPolylines){
			mapView.getMapScene().removeMapPolyline(mapPolyline);
		}
		mapPolylines.clear();
	}

	private GeoCoordinates getMapViewCenter(){
		return mapView.getCamera().getState().targetCoordinates;
	}

	private void addDeliveryMapMarker(GeoCoordinates geoCoordinates, int resourceId, Metadata metadata){
		MapImage mapImage = MapImageFactory.fromResource(this.getActivity().getResources(), resourceId);
		MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
		mapMarker.setMetadata(metadata);

		mapView.getMapScene().addMapMarker(mapMarker);
		mapMarkerList.add(mapMarker);
	}

	private void showDialog(String title, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		AlertDialog thisDialog = builder.setTitle(title)
										.setMessage(message).setPositiveButton("Close", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						if(which == Dialog.BUTTON_POSITIVE){
							dialog.dismiss();
						}
					}
				})
										.show();
	}

	public void detach(){
		//=== Disables TBT guidance (if running) and enters tracking mode.
		stopNavigation();
		//=== Disables positioning.
		stopLocating();
		//=== Disables rendering.
		stopRendering();
	}

	//=== Usually, you need to initialize the HERE SDK only once during the lifetime of an application.
	private void initializeHERESDK(){
		//=== Set your credentials for the HERE SDK.
		//=== THESE ARE THE ORIGINAL TEMP KEYS FROM AUG 2022
		//String accessKeyID = "btdNB7KiUHcM8gAocrRO_Q";
		//String accessKeySecret = "c0Ni-D50P2VZNwuuo0njIHKjmvgt2gbjcSZgbouHxxNks7SAsTFEqLyB4Xk0T3B33LAjGlbqFRJz_gixUYDQiA";
		//==== HESE ARE THE NEW KEYS FROM THE CONTRACT OF APR 2023
		String accessKeyID = "pm-T9UUexnYg4KEZtvFH2Q";
		String accessKeySecret = "_0Gkz8eolnd_W50rUHt9fUe_4c5OmvS0UDX0ZVpWW2wrMj2_Xxu-2qI0TRycNRqYQ8zvvV8T12In8YsGOK_HQA";
		//=== Specify credentials and keep default cache path by setting an empty string.
		SDKOptions options = new SDKOptions(accessKeyID, accessKeySecret, "");
		//=== Defaults to SDKOptions.ActionOnCacheLock.WAIT_LOCKING_APP_FINISH.
		options.actionOnCacheLock = SDKOptions.ActionOnCacheLock.KILL_LOCKING_APP;

		try{
			Context context = this.getActivity();
			SDKNativeEngine.makeSharedInstance(context, options);
		}
		catch(InstantiationErrorException e){
			throw new RuntimeException("Initialization of HERE SDK failed: " + e.error.name());
		}
	}

/*
private void createDynamicRoutingEngine(){
	DynamicRoutingEngineOptions dynamicRoutingOptions = new DynamicRoutingEngineOptions();
	//=== We want an update for each poll iteration, so we specify 0 difference.
	dynamicRoutingOptions.minTimeDifference = Duration.ofSeconds(0);
	dynamicRoutingOptions.minTimeDifferencePercentage = 0.0;
	dynamicRoutingOptions.pollInterval = Duration.ofMinutes(5);

	try{
		//=== With the dynamic routing engine you can poll the HERE backend services to search for routes with less traffic.
		//=== THis can happen during guidance - or you can periodically update a route that is shown in a route planner.
		dynamicRoutingEngine = new DynamicRoutingEngine(dynamicRoutingOptions);
	}
	catch(InstantiationErrorException e){
		throw new RuntimeException("Initialization of DynamicRoutingEngine failed: " + e.error.name());
	}
}
*/

	private void disposeHERESDK(){
		//=== Free HERE SDK resources before the application shuts down.
		//=== Usually, this should be called only on application termination.
		//=== Afterwards, the HERE SDK is no longer usable unless it is initialized again.
		SDKNativeEngine sdkNativeEngine = SDKNativeEngine.getSharedInstance();
		if(sdkNativeEngine != null){
			sdkNativeEngine.dispose();
			//=== For safety reasons, we explicitly set the shared instance to null to avoid situations,
			//=== where a disposed instance is accidentally reused.
			SDKNativeEngine.setSharedInstance(null);
		}
	}

	private void loadMapScene(){
		mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, new MapScene.LoadSceneCallback(){
			@Override
			public void onLoadScene(@Nullable MapError mapError){
				if(mapError == null){
					//=== Enable traffic flows by default.
					Map<String, String> mapFeatures = new HashMap<>();
					//mapFeatures.put(MapFeatures.BUILDING_FOOTPRINTS, MapFeatureModes.BUILDING_FOOTPRINTS_ALL);
					mapFeatures.put(MapFeatures.EXTRUDED_BUILDINGS, MapFeatureModes.EXTRUDED_BUILDINGS_ALL);
					mapView.getMapScene().enableFeatures(mapFeatures);
				}
				else{
					logger.debug(TAG, "Loading map failed: " + mapError.name());
				}
			}
		});
	}

	public void startLocationProvider(){
		//=== Set navigator as listener to receive locations from HERE Positioning
		//=== and choose the best accuracy for the tbt navigation use case.
		herePositioningProvider.startLocating(visualNavigator, LocationAccuracy.NAVIGATION);
	}

	private void setupListeners(){
		//=== Notifies on the progress along the route including maneuver instructions.
		visualNavigator.setRouteProgressListener(new RouteProgressListener(){
			@Override
			public void onRouteProgressUpdated(@NonNull RouteProgress routeProgress){
				//=== routeProgress contains the current section being navigated to the last section
				//=== not the start section to the last section
				try{
					currentRouteProgress = routeProgress;
					//=== called every 1 seconds
					//logger.debug("HERE onRouteProgressUpdated() : routeProgress.sectionIndex = " + routeProgress.sectionIndex);
					//logRouteProgress(currentRouteProgress);

					//=== Contains the progress for the next maneuver ahead and the next-next maneuvers, if any.
					List<ManeuverProgress> ManeuverProgressList = routeProgress.maneuverProgress;

					ManeuverProgress maneuverProgress = ManeuverProgressList.get(0);
					if(maneuverProgress == null){
						logger.debug("HERE onRouteProgressUpdated() : Next maneuver progress NOT available so RETURNING.");
						return;
					}

					int maneuverIndex = maneuverProgress.maneuverIndex;

					//=== THIS FOR UNDERSTANDING OF WHAT IS DELIVERED IN ROUTEPROGRESS
					Route route = visualNavigator.getRoute();

					//=== ROUTE PROGRESS ONLY CONTAINS SECTION PROGRESS ON THE CURRENT SECTION BEING
					//=== NAVIGATED TO THE LAST SECTION OF THE ROUTE, NOT SECTIONS ALREADY TRAVELED OR COMPLETED
					SectionProgress currentSectionProgress = routeProgress.sectionProgress.get(0);
					//=== THIS IS FEET AS WE SET THE UNIT TO IMPERIAL_US
					int remainingSectionDistanceInMeters = currentSectionProgress.remainingDistanceInMeters;

					//=== THE FIRST WAYPOINT index 0 IS OUR START LOCATION SO
					//=== THE FIRST ROUTE SECTION IS STARTED BY OUR CURRENT LOCATION index 0 AND ENDED BY THE FIRST WAYPOINT i.e. INDEX 1
					int sectionArrivalWaypointIndex = route.getSections().get(routeProgress.sectionIndex).getArrivalPlace().waypointIndex;

					//=== ASSUMING THAT THE FIRST WAYPOINT IS THE CURRENT LOCATION START POINT
					MapMarker arrivalWaypointMapMarker = MyHereNavigationFragment.this.mapMarkerList.get(sectionArrivalWaypointIndex);
					logger.debug("HERE onRouteProgressUpdated() : sectionArrivalWaypoint index = " + sectionArrivalWaypointIndex + " address = " + arrivalWaypointMapMarker.getMetadata().getString("STREET_ADDRESS") + " in " + remainingSectionDistanceInMeters + " feet.");

					SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM = arrivalWaypointMapMarker.getMetadata().getInteger("SEQUENCE_NUMBER");

					int destinationArrivalDistanceThreshhold = 25;

					//=== IF ON LAST ROUTE DELIVERY AND BELOW DISTANCE THRESHOLD
					if(SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM == SEQUENCE_ID_OF_LAST_ROUTE_ITEM && remainingSectionDistanceInMeters < destinationArrivalDistanceThreshhold){
						logger.debug("HERE onRouteProgressUpdated() : AT LAST ROUTE DELIVERY AND BELOW DISTANCE THRESHOLD : SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM (" + SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM + ") equals SEQUENCE_ID_OF_LAST_ROUTE_ITEM(" + SEQUENCE_ID_OF_LAST_ROUTE_ITEM + ")");

						logger.debug("HERE onRouteProgressUpdated() : CALLING navigationRouteFinished()");
						navigationRouteFinished();
					}
					else{
						//=== IF ON LAST ROUTE DELIVERY OF CURRENT NAVIGATION PAGE AND BELOW DISTANCE THRESHOLD
						if(SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM == ENDING_POINT_SEQUENCE_NUMBER && remainingSectionDistanceInMeters < destinationArrivalDistanceThreshhold){
							logger.debug("HERE onRouteProgressUpdated() : AT LAST DELIVERY OF ROUTE AND BELOW DISTANCE THRESHOLD : SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM (" + SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM + ") equals ENDING_POINT_SEQUENCE_NUMBER(" + ENDING_POINT_SEQUENCE_NUMBER + ")");
							logger.debug("HERE onRouteProgressUpdated() : CALLING getNewNavigationPage()");
							getNewNavigationPage();
						}
						//=== EVERYTHING ELSE
						else{
							if(SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM == SEQUENCE_ID_OF_LAST_ROUTE_ITEM){
								logger.debug("HERE onRouteProgressUpdated() : AT LAST DELIVERY FOR CURRENT NAVIGATION PAGE : SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM (" + SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM + ") equals SEQUENCE_ID_OF_LAST_ROUTE_ITEM(" + SEQUENCE_ID_OF_LAST_ROUTE_ITEM + ")");
								logger.debug("HERE onRouteProgressUpdated() : WAITING FOR DISTANCE (" + remainingSectionDistanceInMeters + ") TO BE < " + destinationArrivalDistanceThreshhold + " FEET");
							}
							else{
								logger.debug("HERE onRouteProgressUpdated() : NOT AT LAST DELIVERY FOR CURRENT NAVIGATION PAGE : SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM (" + SEQUENCE_ID_OF_ROUTE_PROGRESS_ITEM + ") NOT equals SEQUENCE_ID_OF_LAST_ROUTE_ITEM(" + SEQUENCE_ID_OF_LAST_ROUTE_ITEM + ")");
							}

							try{
								if(remainingSectionDistanceInMeters > 0){
									//=== THIS NEEDS TO BE THE DROP ADDRESS
									//=== i.e. "Delivery in 30 meters at 123 Oak st for one gannet on front porch"
									String products = arrivalWaypointMapMarker.getMetadata().getString("PRODUCTS");

									//=== MANUEVER UPDATES SET TO FEET
									String logMessage = "Delivery in " + remainingSectionDistanceInMeters + " feet at " +
														arrivalWaypointMapMarker.getMetadata().getString("STREET_ADDRESS") +
														(products == null || products.isEmpty() ? "" : " for " + products) + " " +
														arrivalWaypointMapMarker.getMetadata().getString("DELIVERY_INSTRUCTIONS");

									logger.debug("HERE onRouteProgressUpdated() new maneuver, logMessage : " + logMessage);

									String newText = "";
									if(previousManeuverIndex != maneuverIndex){
										logger.debug("HERE onRouteProgressUpdated() new maneuver, SPEAKING : " + logMessage);

										newText = "NEW DELIVERY PROGRESS:\n\n" + logMessage;

										addVoiceMessageToQueue(false, logMessage);
										playVoiceMessagesInQueue();
									}
									else{
										// A maneuver update contains a different distance to reach the next maneuver.
										newText = "DELIVERY PROGRESS:\n\n" + logMessage;
									}

									newText = newText.replace("_", " - ");
									messageView.setText(newText);
									logger.debug("HERE onRouteProgressUpdated() : " + newText);

									previousManeuverIndex = maneuverIndex;
								}
							}
							catch(Exception e){
								logger.error("EXCEPTION : " + e.getMessage());
								throw new RuntimeException(e);
							}

	/*===MAINLY FOR TRAFFIC
							if(lastMapMatchedLocation != null){
								// Update the route based on the current location of the driver.
								// We periodically want to search for better traffic-optimized routes.
								//logger.debug("HERE onRouteProgressUpdated() : CALLING dynamicRoutingEngine.updateCurrentLocation()");
								dynamicRoutingEngine.updateCurrentLocation(lastMapMatchedLocation, routeProgress.sectionIndex);
							}
	*/
						}
					}
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}
			}
		});

		//=== Notifies when the destination of the route is reached.
		visualNavigator.setDestinationReachedListener(new DestinationReachedListener(){
			@Override
			public void onDestinationReached(){
				//=== ODDLY ENOUGH, THIS HAPPENS BEFORE ALL THE OTHER LISTENERS, SO NOT SO USEFUL
				try{
					String message = "End of route has been reached.";
					logger.debug("HERE onDestinationReached() : " + message);
					messageView.setText(message);

					stopNavigation();

					AsyncTask.execute(new Runnable(){
						@Override
						public void run(){
							voiceAssistant.speak(message);
						}
					});

//				onArrival();
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}
			}
		});

		//=== Notifies when a waypoint on the route is reached or missed.
		//=== THIS IS ESSENTIALLY UNUSABLE AS IT APPEARS QUITE SOME TIME AFTER
		//=== ACTUALLY PASSING THE MILESTONE
		visualNavigator.setMilestoneStatusListener(new MilestoneStatusListener(){
			@Override
			public void onMilestoneStatusUpdated(@NonNull Milestone milestone, @NonNull MilestoneStatus milestoneStatus){
				try{
					//logger.debug("HERE onMilestoneStatusUpdated() : ENTER");
					String message = "";

					if(milestone.waypointIndex != null && milestoneStatus == MilestoneStatus.REACHED){
						MapMarker arrivalWaypointMapMarker = MyHereNavigationFragment.this.mapMarkerList.get(milestone.waypointIndex);

						logger.debug("HERE onMilestoneStatusUpdated() : A user-defined delivery was reached, index of delivery: " + milestone.waypointIndex + " at " + arrivalWaypointMapMarker.getMetadata().getString("STREET_ADDRESS") + " SEQUENCE# = " + arrivalWaypointMapMarker.getMetadata().getInteger("SEQUENCE_NUMBER"));

						if(CTApp.isInSimulation){
							int[] simulatedDeliveries = new int[]{arrivalWaypointMapMarker.getMetadata().getInteger("SEQUENCE_NUMBER")};
							updateDeliveryMapPins(simulatedDeliveries);
						}
					}
					else if(milestone.waypointIndex != null && milestoneStatus == MilestoneStatus.MISSED){
						MapMarker arrivalWaypointMapMarker = MyHereNavigationFragment.this.mapMarkerList.get(milestone.waypointIndex);

						message = "Delivery missed at " + arrivalWaypointMapMarker.getMetadata().getString("STREET_ADDRESS");

						logger.debug("HERE onMilestoneStatusUpdated() : A user-defined delivery was missed, index of delivery: " + milestone.waypointIndex + " at " + arrivalWaypointMapMarker.getMetadata().getString("STREET_ADDRESS") + " SEQUENCE# = " + arrivalWaypointMapMarker.getMetadata().getInteger("SEQUENCE_NUMBER"));
						logger.debug("HERE onMilestoneStatusUpdated() : SPEAKING : " + message);

						addVoiceMessageToQueue(true, message);
						playVoiceMessagesInQueue();
					}
					else if(milestone.waypointIndex == null && milestoneStatus == MilestoneStatus.REACHED){
						//=== For example, when transport mode changes due to a ferry a system-defined waypoint may have been added.
						logger.debug("HERE onMilestoneStatusUpdated() : A system-defined waypoint was reached at: " + milestone.mapMatchedCoordinates);
					}
					else if(milestone.waypointIndex == null && milestoneStatus == MilestoneStatus.MISSED){
						//=== For example, when transport mode changes due to a ferry a system-defined waypoint may have been added.
						logger.debug("HERE onMilestoneStatusUpdated() : A system-defined waypoint was missed at: " + milestone.mapMatchedCoordinates);
					}
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}
			}
		});

/*
		// Notifies when the current speed limit is exceeded.
		visualNavigator.setSpeedWarningListener(new SpeedWarningListener(){
			@Override
			public void onSpeedWarningStatusChanged(@NonNull SpeedWarningStatus speedWarningStatus){
//				logger.debug("HERE onSpeedWarningStatusChanged() : ENTER");
				if(speedWarningStatus == SpeedWarningStatus.SPEED_LIMIT_EXCEEDED){
					// Driver is faster than current speed limit (plus an optional offset).
					// Play a notification sound to alert the driver.
					// Note that this may not include temporary special speed limits, see SpeedLimitListener.
					Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					Ringtone ringtone = RingtoneManager.getRingtone(MyHereNavigationFragment.this.getActivity(), ringtoneUri);
					ringtone.play();
				}

//				if(speedWarningStatus == SpeedWarningStatus.SPEED_LIMIT_RESTORED){
//					logger.debug("HERE onSpeedWarningStatusChanged() : Driver is again slower than current speed limit (plus an optional offset).");
//				}
			}
		});

		// Notifies on the current speed limit valid on the current road.
		visualNavigator.setSpeedLimitListener(new SpeedLimitListener(){
			@Override
			public void onSpeedLimitUpdated(@NonNull SpeedLimit speedLimit){
//				logger.debug("HERE onSpeedLimitUpdated() : ENTER");
				Double currentSpeedLimit = getCurrentSpeedLimit(speedLimit);

				if(currentSpeedLimit == null){
					logger.debug("HERE onSpeedLimitUpdated() : Warning: Speed limits unknown, data could not be retrieved.");
				}
				else if(currentSpeedLimit == 0){
					logger.debug("HERE onSpeedLimitUpdated() : No speed limits on this road! Drive as fast as you feel safe ...");
				}
				else{
					logger.debug("HERE onSpeedLimitUpdated() : Current speed limit (m/s):" + currentSpeedLimit);
				}
			}
		});
*/

		// Notifies on the current map-matched location and other useful information while driving or walking.
		visualNavigator.setNavigableLocationListener(new NavigableLocationListener(){
			@Override
			public void onNavigableLocationUpdated(@NonNull NavigableLocation currentNavigableLocation){
				try{
					//=== called every 1 seconds
					//logger.debug("HERE onNavigableLocationUpdated() : ENTER");
					lastMapMatchedLocation = currentNavigableLocation.mapMatchedLocation;
					if(lastMapMatchedLocation == null){

						long currentTime = System.currentTimeMillis();
						if(currentTime - lastNavigableLocationEventTime > 30000L){
							lastNavigableLocationEventTime = currentTime;

							String message = "Are you off-road?";
							addVoiceMessageToQueue(true, message);
							playVoiceMessagesInQueue();
							logger.debug("HERE onNavigableLocationUpdated() : The currentNavigableLocation could not be map-matched. SPEAKING : Are you off-road?");
						}

					}
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}
			}
		});

		//=== Notifies on a possible deviation from the route.
		visualNavigator.setRouteDeviationListener(new RouteDeviationListener(){
			@Override
			public void onRouteDeviation(@NonNull RouteDeviation routeDeviation){
				try{
					Route route = visualNavigator.getRoute();

					if(route == null){
						logger.debug("HERE onRouteDeviation() : route is NULL so RETURNING");
						//=== May happen in rare cases when route was set to null in between.
						return;
					}

					//=== Get current geographic coordinates.
					MapMatchedLocation currentMapMatchedLocation = routeDeviation.currentLocation.mapMatchedLocation;
					GeoCoordinates currentGeoCoordinates = currentMapMatchedLocation == null ?
														   routeDeviation.currentLocation.originalLocation.coordinates : currentMapMatchedLocation.coordinates;

					//=== Get last geographic coordinates on route.
					GeoCoordinates lastGeoCoordinatesOnRoute;
					if(routeDeviation.lastLocationOnRoute != null){
						logger.debug("HERE onRouteDeviation() : lastLocationOnRoute is not null");
						MapMatchedLocation lastMapMatchedLocationOnRoute = routeDeviation.lastLocationOnRoute.mapMatchedLocation;
						lastGeoCoordinatesOnRoute = lastMapMatchedLocationOnRoute == null ?
													routeDeviation.lastLocationOnRoute.originalLocation.coordinates : lastMapMatchedLocationOnRoute.coordinates;
					}
					else{
						logger.debug("HERE onRouteDeviation() : User was never following the route. So, we take the start of the route instead.");
						lastGeoCoordinatesOnRoute = route.getSections().get(0).getDeparturePlace().originalCoordinates;
					}

					int distanceInMeters = (int) currentGeoCoordinates.distanceTo(lastGeoCoordinatesOnRoute);
					logger.debug("RouteDeviation in meters is " + distanceInMeters);

					//=== Decide if rerouting should happen and if yes, then return to the original route.
					handleRerouting(routeDeviation, distanceInMeters, currentGeoCoordinates, currentMapMatchedLocation);

	/*
					long currentTime = System.currentTimeMillis();
					if(currentTime - lastRouteDeviationEventTime > 10000l){
						int distanceInFeet = (int) (currentGeoCoordinates.distanceTo(lastGeoCoordinatesOnRoute) * 3.28084);
						logger.debug("HERE onRouteDeviation() : Route Deviation in feet is " + distanceInFeet);

						lastRouteDeviationEventTime = currentTime;

						final String thisDeviationMessage = "Possible route deviation by " + distanceInFeet + " feet.";

						if(distanceInFeet > 200){
							if(!thisDeviationMessage.equalsIgnoreCase(lastDeviationMessage)){
								lastDeviationMessage = thisDeviationMessage;

								addVoiceMessageToQueue(true, thisDeviationMessage);
								playVoiceMessagesInQueue();
							}
						}
					}
	*/
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}
			}
		});

		//=== Notifies on voice maneuver messages.
		visualNavigator.setManeuverNotificationListener(new ManeuverNotificationListener(){
			@Override
			public void onManeuverNotification(@NonNull String voiceText){
				try{
					int routeSectionIndex = currentRouteProgress.sectionIndex;
					Route route = visualNavigator.getRoute();

					if(routeSectionIndex > route.getSections().size()){
						logger.debug("most likely a new page called and this is a maneuver from the previous page");
					}
					else{
						//=== ONLY CALLED WHEN NEEDED

						//===************ THIS WILL ANNOUNCE DESTINATION ARRIVAL *****************
						//*************** HERE onManeuverNotification() : ENTER : You have reached your destination.
						//***************                                 ENTER : You have reached your waypoint.
						//***************                                         Now turn left and then you will reach your waypoint.

						//=== FOR THE SAME WAYPOINT THESE DISTANCES REPORTED DO NOT AGREE
						//onRouteProgressUpdated() : Distance remaining to current drop in FEET: 304
						//onManeuverNotification() : speaking : after 1000 feet you will reach your delivery. the delivery will be on your right.
						//LOOKS LIKE AFTER SETTING UNITS TO ENGLISH IMPERIAL THE DISTANCE (WHICH APPARENTLY WAS SET TO FEET IS
						//BEING CONVERTED TO FEET AGAIN.

						//=== THIS IS ONLY SPOKEN SO ALL LOWER CASE DOES NOT MATTER
						voiceText = voiceText.toLowerCase();
						voiceText = voiceText.replace("meters", "feet");
						voiceText = voiceText.replace("waypoint", "delivery");

						//=== THIS WILL REVERT THE APPARENT DOUBLE FEET CONVERSION
						String[] splits = voiceText.split(" ");
						String feetUnitString = "";
						for(int index = 0; index < splits.length; index++){
							if(splits[index].equalsIgnoreCase("feet")){
								feetUnitString = splits[index - 1];
								logger.debug("found value before 'feet' = " + feetUnitString);
								int feetUnitNumber = Integer.valueOf(feetUnitString).intValue();
								logger.debug("int value before 'feet' = " + feetUnitNumber);
								int convertedFeetUnitNumber = (int) (feetUnitNumber / 3.2804);
								voiceText = voiceText.replace(feetUnitString, convertedFeetUnitNumber + "");

								break;
							}
						}

						voiceText = voiceText.replace("you have reached your delivery. it's", "delivery");

						voiceText = voiceText.replace("now turn", "turn");
						voiceText = voiceText.replace("and then you will reach", "to reach");

						//=== THE FIRST WAYPOINT index 0 IS OUR CURRENT LOCATION SO
						//=== THE FIRST ROUTE SECTION IS STARTED BY OUR CURRENT LOCATION index 0 AND ENDED BY THE FIRST WAYPOINT i.e. INDEX 1
						int sectionArrivalWaypointIndex = route.getSections().get(routeSectionIndex).getArrivalPlace().waypointIndex;
						//=== ASSUMING THAT THE FIRST WAYPOINT IS THE CURRENT LOCATION START POINT
						MapMarker arrivalWaypointMapMarker = MyHereNavigationFragment.this.mapMarkerList.get(sectionArrivalWaypointIndex);
						logger.debug("HERE onManeuverNotification() : sectionArrivalWaypoint index = " + sectionArrivalWaypointIndex + " address = " + arrivalWaypointMapMarker.getMetadata().getString("STREET_ADDRESS"));

						logger.debug("HERE onManeuverNotification() : speaking : " + voiceText);
						addVoiceMessageToQueue(false, voiceText);
						playVoiceMessagesInQueue();
					}
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}
			}
		});

/*
		// Notifies which lane(s) lead to the next (next) maneuvers.
		visualNavigator.setManeuverViewLaneAssistanceListener(new ManeuverViewLaneAssistanceListener(){
			@Override
			public void onLaneAssistanceUpdated(@NonNull ManeuverViewLaneAssistance maneuverViewLaneAssistance){
				//=== ONLY CALLED WHEN NEEDED
				logger.debug("HERE onLaneAssistanceUpdated() : ENTER");
				// This lane list is guaranteed to be non-empty.
				List<Lane> lanes = maneuverViewLaneAssistance.lanesForNextManeuver;
				logLaneRecommendations(lanes);

				List<Lane> nextLanes = maneuverViewLaneAssistance.lanesForNextNextManeuver;
				if(!nextLanes.isEmpty()){
					logger.debug("HERE onLaneAssistanceUpdated() : Attention, the next next maneuver is very close.");
					logger.debug("HERE onLaneAssistanceUpdated() : Please take the following lane(s) after the next maneuver: ");
					logLaneRecommendations(nextLanes);
				}
			}
		});
*/

		//=== Notifies which lane(s) allow to follow the route.
		visualNavigator.setJunctionViewLaneAssistanceListener(new JunctionViewLaneAssistanceListener(){
			@Override
			public void onLaneAssistanceUpdated(@NonNull JunctionViewLaneAssistance junctionViewLaneAssistance){
				try{
					logger.debug("HERE onLaneAssistanceUpdated() : ENTER");
					List<Lane> lanes = junctionViewLaneAssistance.lanesForNextJunction;
					if(lanes.isEmpty()){
						logger.debug("HERE onLaneAssistanceUpdated() : You have passed the complex junction.");
					}
					else{
						logger.debug("HERE onLaneAssistanceUpdated() : Attention, a complex junction is ahead.");
						logLaneRecommendations(lanes);
					}
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}
			}
		});

/*
		// Notifies on the attributes of the current road including usage and physical characteristics.
		visualNavigator.setRoadAttributesListener(new RoadAttributesListener(){
			@Override
			public void onRoadAttributesUpdated(@NonNull RoadAttributes roadAttributes){
				// This is called whenever any road attribute has changed.
				// If all attributes are unchanged, no new event is fired.
				// Note that a road can have more than one attribute at the same time.

//				logger.debug("HERE onRoadAttributesUpdated() : Received road attributes update.");

				if(roadAttributes.isBridge){
					// Identifies a structure that allows a road, railway, or walkway to pass over another road, railway,
					// waterway, or valley serving map display and route guidance functionalities.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: This is a bridge.");
				}
				if(roadAttributes.isControlledAccess){
					// Controlled access roads are roads with limited entrances and exits that allow uninterrupted
					// high-speed traffic flow.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: This is a controlled access road.");
				}
				if(roadAttributes.isDirtRoad){
					// Indicates whether the navigable segment is paved.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: This is a dirt road.");
				}
				if(roadAttributes.isDividedRoad){
					// Indicates if there is a physical structure or painted road marking intended to legally prohibit
					// left turns in right-side driving countries, right turns in left-side driving countries,
					// and U-turns at divided intersections or in the middle of divided segments.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: This is a divided road.");
				}
				if(roadAttributes.isNoThrough){
					// Identifies a no through road.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: This is a no through road.");
				}
				if(roadAttributes.isPrivate){
					// Private identifies roads that are not maintained by an organization responsible for maintenance of
					// public roads.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: This is a private road.");
				}
				if(roadAttributes.isRamp){
					// Range is a ramp: connects roads that do not intersect at grade.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: This is a ramp.");
				}
				if(roadAttributes.isRightDrivingSide){
					// Indicates if vehicles have to drive on the right-hand side of the road or the left-hand side.
					// For example, in New York it is always true and in London always false as the United Kingdom is
					// a left-hand driving country.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: isRightDrivingSide = " + roadAttributes.isRightDrivingSide);
				}
				if(roadAttributes.isRoundabout){
					// Indicates the presence of a roundabout.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: This is a roundabout.");
				}
				if(roadAttributes.isTollway){
					// Identifies a road for which a fee must be paid to use the road.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes change: This is a road with toll costs.");
				}
				if(roadAttributes.isTunnel){
					// Identifies an enclosed (on all sides) passageway through or under an obstruction.
					logger.debug("HERE onRoadAttributesUpdated() : Road attributes: This is a tunnel.");
				}
			}
		});
*/

		// Notifies truck drivers on road restrictions ahead.
		// For example, there can be a bridge ahead not high enough to pass a big truck
		// or there can be a road ahead where the weight of the truck is beyond it's permissible weight.
		// This event notifies on truck restrictions in general,
		// so it will also deliver events, when the transport type was set to a non-truck transport type.
		// The given restrictions are based on the HERE database of the road network ahead.
		visualNavigator.setTruckRestrictionsWarningListener(new TruckRestrictionsWarningListener(){
			@Override
			public void onTruckRestrictionsWarningUpdated(@NonNull List<TruckRestrictionWarning> list){
				try{
					logger.debug("HERE onTruckRestrictionsWarningUpdated() : ENTER");
					//=== The list is guaranteed to be non-empty.
					for(TruckRestrictionWarning truckRestrictionWarning : list){

						if(truckRestrictionWarning.distanceType == DistanceType.AHEAD){
							logger.debug("HERE onTruckRestrictionsWarningUpdated() : TruckRestrictionWarning ahead in: " + truckRestrictionWarning.distanceInMeters + " meters.");
						}
						else if(truckRestrictionWarning.distanceType == DistanceType.PASSED){
							logger.debug("HERE onTruckRestrictionsWarningUpdated() : A restriction just passed.");
						}

						//=== One of the following restrictions applies ahead, if more restrictions apply at the same time,
						//=== they are part of another TruckRestrictionWarning element contained in the list.
						if(truckRestrictionWarning.weightRestriction != null){
							//=== For now only one weight type (= truck) is exposed.
							WeightRestrictionType type = truckRestrictionWarning.weightRestriction.type;
							int value = truckRestrictionWarning.weightRestriction.valueInKilograms;
							logger.debug("HERE onTruckRestrictionsWarningUpdated() : TruckRestriction for weight (kg): " + type.name() + ": " + value);
						}

						if(truckRestrictionWarning.dimensionRestriction != null){
							//=== Can be either a length, width or height restriction of the truck. For example, a height
							//=== restriction can apply for a tunnel. Other possible restrictions are delivered in
							//=== separate TruckRestrictionWarning objects contained in the list, if any.
							DimensionRestrictionType type = truckRestrictionWarning.dimensionRestriction.type;
							int value = truckRestrictionWarning.dimensionRestriction.valueInCentimeters;
							logger.debug("HERE onTruckRestrictionsWarningUpdated() : TruckRestriction for dimension: " + type.name() + ": " + value);
						}
					}
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}
			}
		});

		// Notifies whenever any textual attribute of the current road changes, i.e., the current road texts differ
		// from the previous one. This can be useful during tracking mode, when no maneuver information is provided.
		visualNavigator.setRoadTextsListener(new RoadTextsListener(){
			@Override
			public void onRoadTextsUpdated(@NonNull RoadTexts roadTexts){
				try{
					String currentRoadName = roadTexts.names.getDefaultValue();
					//String currentRoadNumber = roadTexts.numbers.getDefaultValue();

					logger.debug("HERE onRoadTextsUpdated() : SPEAKING Now on " + currentRoadName + ".");
					//=== See getRoadName() how to get the current road name from the provided RoadTexts.

					addVoiceMessageToQueue(false, "Now on " + currentRoadName + ".");
					playVoiceMessagesInQueue();
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}
			}
		});
	}

	private void handleRerouting(RouteDeviation routeDeviation,
								 int distanceInMeters,
								 GeoCoordinates currentGeoCoordinates,
								 MapMatchedLocation currentMapMatchedLocation){
		logger.debug("handleRerouting() : ENTER");

		//=== Counts the number of received deviation events. When the user is following a route, no deviation
		//=== event will occur.
		//=== It is recommended to await at least 3 deviation events before deciding on an action.
		deviationCounter++;

		if(isReturningToRoute){
			//=== Rerouting is ongoing.
			logger.debug("handleRerouting() : IGNORE, Rerouting is ongoing ...");
			return;
		}

		//=== When user has deviated more than distanceThresholdInMeters. Now we try to return to the original route.
		int distanceThresholdInMeters = 75;
		if(deviationCounter < 3){
			logger.debug("handleRerouting() : IGNORE : deviationCounter < 3");
		}

		if(distanceInMeters < distanceThresholdInMeters){
			logger.debug("handleRerouting() : IGNORE : " + distanceInMeters + " < " + distanceThresholdInMeters);
		}
		else{
			logger.debug("handleRerouting() : PROCESSING REROUTE");

			String newText = "Rerouting in progress, please wait.";
			messageView.setText(newText);

			addVoiceMessageToQueue(true, "Rerouting in progress.");
			playVoiceMessagesInQueue();

			isReturningToRoute = true;

			//=== Use current location as new starting point for the route.
			Waypoint newStartingPoint = new Waypoint(currentGeoCoordinates);

			//=== Improve the route calculation by setting the heading direction.
			if(currentMapMatchedLocation != null && currentMapMatchedLocation.bearingInDegrees != null){
				newStartingPoint.headingInDegrees = currentMapMatchedLocation.bearingInDegrees;
			}

			//=== In general, the return.to-route algorithm will try to find the fastest way back to the original route,
			//=== but it will also respect the distance to the destination. The new route will try to preserve the shape
			//=== of the original route if possible and it will use the same route options.
			//=== When the user can now reach the destination faster than with the previously chosen route, a completely new
			//=== route is calculated.
			logger.debug("handleRerouting() :  Calculating a new route.");

			routingEngine.returnToRoute(currentRoute,
										newStartingPoint,
										routeDeviation.lastTraveledSectionIndex,
										routeDeviation.traveledDistanceOnLastSectionInMeters,
										(routingError, list) -> {
											// For simplicity, we use the same route handling.
											// The previous route will be still visible on the map for reference.
											handleRouteResults(routingError, list);
											// Instruct the navigator to follow the calculated route (which will be the new one if no error occurred).
//											visualNavigator.setRoute(currentRoute);
											// Reset flag and counter.
											isReturningToRoute = false;
											deviationCounter = 0;
											logger.debug("handleRerouting() : returnToRoute() : Rerouting: New route set.");

											addVoiceMessageToQueue(true, "Rerouted navigation started.");
											playVoiceMessagesInQueue();

											String text = "Rerouted navigation started.";
											messageView.setText(text);
										});
//			TaskHandle	returnToRoute​(Route route, Waypoint startingPoint, double routeFractionTraveled, CalculateRouteCallback callback)
//			Deprecated.
//			Will be removed in v4.18.0, please use version with section index and distance.
//
//			TaskHandle	returnToRoute​(Route route, Waypoint startingPoint, int lastTraveledSectionIndex, int traveledDistanceOnLastSectionInMeters, CalculateRouteCallback callback)
//			Asynchronously calculates a new route that leads back to the original route.
		}
	}

	/*
	private String getRoadName(Maneuver maneuver){
		RoadTexts currentRoadTexts = maneuver.getRoadTexts();
		RoadTexts nextRoadTexts = maneuver.getNextRoadTexts();

		String currentRoadName = currentRoadTexts.names.getDefaultValue();
		String currentRoadNumber = currentRoadTexts.numbers.getDefaultValue();
		String nextRoadName = nextRoadTexts.names.getDefaultValue();
		String nextRoadNumber = nextRoadTexts.numbers.getDefaultValue();

		String roadName = nextRoadName == null ? nextRoadNumber : nextRoadName;

		// On highways, we want to show the highway number instead of a possible road name,
		// while for inner city and urban areas road names are preferred over road numbers.
		if(maneuver.getNextRoadType() == RoadType.HIGHWAY){
			roadName = nextRoadNumber == null ? nextRoadName : nextRoadNumber;
		}

		if(maneuver.getAction() == ManeuverAction.ARRIVE){
			// We are approaching the destination, so there's no next road.
			roadName = currentRoadName == null ? currentRoadNumber : currentRoadName;
		}

		if(roadName == null){
			// Happens only in rare cases, when also the fallback is null.
			roadName = "unnamed road";
		}

		return roadName;
	}
*/

	private void handleRouteResults(RoutingError routingError, List<Route> routes){
		logger.debug("handleRouteResults() : ENTER");

		if(routingError != null){
			addVoiceMessageToQueue(true, "Rerouting experienced an error.");
			playVoiceMessagesInQueue();

			logger.debug("handleRouteResults() : ERROR : " + routingError);
			showDialog("Error while calculating a route: ", routingError.toString());
//			return;
//		}

			// Reset previous text, if any.
//		lastRoadShieldText = "";

			// When routingError is nil, routes is guaranteed to contain at least one route.
//		currentRoute = routes.get(0);

//		Color routeColor = Color.valueOf(0, 0.6f, 1, 1); // RGBA
//		int routeWidthInPixels = 30;
//		showRouteOnMap(currentRoute, routeColor, routeWidthInPixels);

			currentRoute = routes.get(0);
			//logRouteDetails(currentRoute);
			showRouteOnMap(currentRoute);
			startNavigation(currentRoute, CTApp.isInSimulation);

			newPageCalled = false;
		}
	}

/*
	private Double getCurrentSpeedLimit(SpeedLimit speedLimit){
		// Note that all values can be null if no data is available.

		// The regular speed limit if available. In case of unbounded speed limit, the value is zero.
		//logger.debug(TAG, "speedLimitInMetersPerSecond: " + speedLimit.speedLimitInMetersPerSecond);

		// A conditional school zone speed limit as indicated on the local road signs.
		//logger.debug(TAG, "schoolZoneSpeedLimitInMetersPerSecond: " + speedLimit.schoolZoneSpeedLimitInMetersPerSecond);

		// A conditional time-dependent speed limit as indicated on the local road signs.
		// It is in effect considering the current local time provided by the device's clock.
		//logger.debug(TAG, "timeDependentSpeedLimitInMetersPerSecond: " + speedLimit.timeDependentSpeedLimitInMetersPerSecond);

		// A conditional non-legal speed limit that recommends a lower speed,
		// for example, due to bad road conditions.
		//logger.debug(TAG, "advisorySpeedLimitInMetersPerSecond: " + speedLimit.advisorySpeedLimitInMetersPerSecond);

		// A weather-dependent speed limit as indicated on the local road signs.
		// The HERE SDK cannot detect the current weather condition, so a driver must decide
		// based on the situation if this speed limit applies.
		//logger.debug(TAG, "fogSpeedLimitInMetersPerSecond: " + speedLimit.fogSpeedLimitInMetersPerSecond);
		//logger.debug(TAG, "rainSpeedLimitInMetersPerSecond: " + speedLimit.rainSpeedLimitInMetersPerSecond);
		//logger.debug(TAG, "snowSpeedLimitInMetersPerSecond: " + speedLimit.snowSpeedLimitInMetersPerSecond);

		// For convenience, this returns the effective (lowest) speed limit between
		// - speedLimitInMetersPerSecond
		// - schoolZoneSpeedLimitInMetersPerSecond
		// - timeDependentSpeedLimitInMetersPerSecond
		return speedLimit.effectiveSpeedLimitInMetersPerSecond();
	}
*/

	private void logLaneRecommendations(List<Lane> lanes){
		//=== The lane at index 0 is the leftmost lane adjacent to the middle of the road.
		//=== The lane at the last index is the rightmost lane.
		int laneNumber = 0;
		for(Lane lane : lanes){
			//=== This state is only possible if maneuverViewLaneAssistance.lanesForNextNextManeuver is not empty.
			//=== For example, when two lanes go left, this lanes leads only to the next maneuver,
			//=== but not to the maneuver after the next maneuver, while the highly recommended lane also leads
			//=== to this next next maneuver.
			if(lane.recommendationState == LaneRecommendationState.RECOMMENDED){
				logger.debug(TAG, "Lane " + laneNumber + " leads to next maneuver, but not to the next next maneuver.");
			}

			//=== If laneAssistance.lanesForNextNextManeuver is not empty, this lane leads also to the
			//=== maneuver after the next maneuver.
			if(lane.recommendationState == LaneRecommendationState.HIGHLY_RECOMMENDED){
				logger.debug(TAG, "Lane " + laneNumber + " leads to next maneuver and eventually to the next next maneuver.");
			}

			if(lane.recommendationState == LaneRecommendationState.NOT_RECOMMENDED){
				logger.debug(TAG, "Do not take lane " + laneNumber + " to follow the route.");
			}

			laneNumber++;
		}
	}

/*
private void startDynamicSearchForBetterRoutes(Route route){
	try{
		dynamicRoutingEngine.start(route, new DynamicRoutingListener(){
			// Notifies on traffic-optimized routes that are considered better than the current route.
			@Override
			public void onBetterRouteFound(@NonNull Route newRoute, int etaDifferenceInSeconds, int distanceDifferenceInMeters){
				logger.debug(TAG, "DynamicRoutingEngine: Calculated a new route.");
				logger.debug(TAG, "DynamicRoutingEngine: etaDifferenceInSeconds: " + etaDifferenceInSeconds + ".");
				logger.debug(TAG, "DynamicRoutingEngine: distanceDifferenceInMeters: " + distanceDifferenceInMeters + ".");

				String logMessage = "Calculated a new route. etaDifferenceInSeconds: " + etaDifferenceInSeconds +
									" distanceDifferenceInMeters: " + distanceDifferenceInMeters;

				// An implementation can decide to switch to the new route:
				if(true){//useDynamicRouting){
					messageView.setText("DynamicRoutingEngine update: " + logMessage);
					visualNavigator.setRoute(newRoute);
				}
			}

			@Override
			public void onRoutingError(@NonNull RoutingError routingError){
				logger.debug(TAG, "Error while dynamically searching for a better route: " + routingError.name());
			}
		});
	}
	catch(DynamicRoutingEngine.StartException e){
		throw new RuntimeException("Start of DynamicRoutingEngine failed. Is the RouteHandle missing?");
	}
}
*/

	public void startNavigation(Route route, boolean isInSimulation){
		setupSpeedWarnings();

		//=== Switches to navigation mode when no route was set before, otherwise navigation mode is kept.
		visualNavigator.setRoute(route);

		if(isInSimulation){
			enableRoutePlayback(route);
			messageView.setText("Starting simulated navigation.");
		}
		else{
			enableDevicePositioning();
			messageView.setText("Starting navigation.");
		}

		//=== PRIMARILY FOR TRAFFIC RE-ROUTING
		//startDynamicSearchForBetterRoutes(route);

		routePrefetcher.prefetchAroundRouteOnIntervals(visualNavigator);
	}

	public void stopNavigation(){
		//=== Switches to tracking mode when a route was set before, otherwise tracking mode is kept.
		//=== Without a route the navigator will only notify on the current map-matched location
		//=== including info such as speed and current street name.
		visualNavigator.setRoute(null);
		enableDevicePositioning();
		messageView.setText("Tracking device's location.");
	}

	//=== Provides simulated location updates based on the given route.
	public void enableRoutePlayback(Route route){
		herePositioningProvider.stopLocating();

		herePositioningSimulator.startLocating(visualNavigator, route);
	}

//	public void startCameraTracking(){
//		visualNavigator.setCameraMode(CameraTrackingMode.ENABLED);
//	}
//
//	public void stopCameraTracking(){
//		visualNavigator.setCameraMode(CameraTrackingMode.DISABLED);
//	}

	//=== Provides location updates based on the device's GPS sensor.
	public void enableDevicePositioning(){
		herePositioningSimulator.stopLocating();
		herePositioningProvider.startLocating(visualNavigator, LocationAccuracy.NAVIGATION);
	}

	@Nullable
	public com.here.sdk.core.Location getLastKnownLocation(){
		return herePositioningProvider.getLastKnownLocation();
	}

	private void setupSpeedWarnings(){
		SpeedLimitOffset speedLimitOffset = new SpeedLimitOffset();
		speedLimitOffset.lowSpeedOffsetInMetersPerSecond = 2;
		speedLimitOffset.highSpeedOffsetInMetersPerSecond = 4;
		speedLimitOffset.highSpeedBoundaryInMetersPerSecond = 25;

		visualNavigator.setSpeedWarningOptions(new SpeedWarningOptions(speedLimitOffset));
	}

	private void setupVoiceGuidance(){
		try{
			LanguageCode ttsLanguageCode = getLanguageCodeForDevice(VisualNavigator.getAvailableLanguagesForManeuverNotifications());
			visualNavigator.setManeuverNotificationOptions(new ManeuverNotificationOptions(ttsLanguageCode, UnitSystem.IMPERIAL_US));
			logger.debug(TAG, "LanguageCode for maneuver notifications: " + ttsLanguageCode);

			//=== Set language to our TextToSpeech engine.
			Locale locale = LanguageCodeConverter.getLocale(ttsLanguageCode);
			if(voiceAssistant.setLanguage(locale)){
				logger.debug(TAG, "TextToSpeech engine uses this language: " + locale);
			}
			else{
				Log.e(TAG, "TextToSpeech engine does not support this language: " + locale);
			}
		}
		catch(Exception e){
			logger.error("Exception : " + e.getMessage());
		}
	}

	//=== Get the language preferably used on this device.
	private LanguageCode getLanguageCodeForDevice(List<LanguageCode> supportedVoiceSkins){

		//=== 1. Determine if preferred device language is supported by our TextToSpeech engine.
		Locale localeForCurrentDevice = Locale.getDefault();
		if(!voiceAssistant.isLanguageAvailable(localeForCurrentDevice)){
			Log.e(TAG, "TextToSpeech engine does not support: " + localeForCurrentDevice + ", falling back to EN_US.");
			localeForCurrentDevice = new Locale("en", "US");
		}

		//=== 2. Determine supported voice skins from HERE SDK.
		LanguageCode languageCodeForCurrentDevice = LanguageCodeConverter.getLanguageCode(localeForCurrentDevice);
		if(!supportedVoiceSkins.contains(languageCodeForCurrentDevice)){
			Log.e(TAG, "No voice skins available for " + languageCodeForCurrentDevice + ", falling back to EN_US.");
			languageCodeForCurrentDevice = LanguageCode.EN_US;
		}

		return languageCodeForCurrentDevice;
	}

	public void stopLocating(){
		herePositioningProvider.stopLocating();
	}

	public void stopRendering(){
		//=== It is recommended to stop rendering before leaving an activity.
		//=== This also removes the current location marker.
		visualNavigator.stopRendering();
	}

	private void playVoiceMessagesInQueue(){
		AsyncTask.execute(new Runnable(){
			@Override
			public void run(){
				//=== SKIP IF ALREADY PLAYING
				if(!isVoiceAssistantPlaying){
					isVoiceAssistantPlaying = true;

					while(isVoiceAssistantPlaying){
						while(voiceAnnouncements.size() > 0){
							String announcement = voiceAnnouncements.get(0);
							voiceAnnouncements.remove(0);
							voiceAssistant.speak(announcement);
							logger.debug("playVoiceMessagesInQueue() : new announcement = " + announcement);
						}

						isVoiceAssistantPlaying = false;
					}
				}
			}
		});
	}

	private void addVoiceMessageToQueue(boolean addToTopOfList, String message){
		boolean tempIsVoiceAssistantPlaying = isVoiceAssistantPlaying;

		logger.debug("addVoiceMessageToQueue() : new message = " + message);

		if(isVoiceAssistantPlaying){
			isVoiceAssistantPlaying = false;
		}

		if(addToTopOfList){
			voiceAnnouncements.add(0, message);
		}
		else{
			voiceAnnouncements.add(message);
		}

		if(tempIsVoiceAssistantPlaying){
			playVoiceMessagesInQueue();
		}

		isVoiceAssistantPlaying = tempIsVoiceAssistantPlaying;
	}
}