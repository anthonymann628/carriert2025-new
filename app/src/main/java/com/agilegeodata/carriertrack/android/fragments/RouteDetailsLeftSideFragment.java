package com.agilegeodata.carriertrack.android.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.RouteDetailsActivity;
import com.agilegeodata.carriertrack.android.adapters.RouteActivityViewListAdapter;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;
import com.agilegeodata.carriertrack.android.objects.Route;
import com.agilegeodata.carriertrack.android.utils.FormValidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

/*
 * RouteDetailsLeftSide
 * Enables search, route details
 */
public class RouteDetailsLeftSideFragment extends Fragment{
	public static final String TAG = RouteDetailsLeftSideFragment.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static boolean gpsRadioButtonIsChecked = false;
	//=== DONE IN RIGHT SIDE FRAGMENT
	AppCompatRadioButton mGpsStatusRadioButton;
	private ImageButton mLeaveRouteButton;
	private String mJobId;
	private String mRouteId;
	private int mResolvedCount;
	private int totalDeliveries = 0;
	private int mUploadCount;
	private int mUnresolvedCount;
	private int mPhotosRequiredCount;
	private int mPhotosUploadedCount;
	private int mPhotosTakenCount;
	private View leftSideViewer;
	private EditText mSearchET;
	private ImageButton mSearchButton;
	private String mSearchValStr;
	private boolean mIsSearchOpen = false;
	private TableLayout mSearchTable;
	private ImageView mSClearButton;
	private ImageView mSearchEnable;
	private RouteDetailsRightSideFragmentMerged rightSideViewer = null;
	private boolean mIsPauseMode = true;
	private TextView mRouteIdTV;
	private TextView mJobIdTV;
	private int mJobDetailId;
	private Integer mDeliveryQuads;
	private TextView mTotalValTV;
	private TextView mPicturesReqTV;
	private TextView mRemainingValTV;
	private TextView mPicturesTakenTV;
	private TextView mCompletedValTV;
	private TextView mPhotosUploadedTV;
	private TextView mUploadedTV;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		logger.info("===--->>> onCreate() : ENTER");
	}

	@Override
	public void onStart(){
		super.onStart();
		logger.info("===--->>> onStart() : ENTER");
	}

	@Override
	public void onStop(){
		super.onStop();
		logger.info("===--->>> onStop() : ENTER");
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		logger.info("===--->>> onDestroy() : ENTER");
	}

	@Override
	public void onPause(){
		super.onPause();
		logger.info("===--->>> onPause() : ENTER");
	}

	@Override
	public void onResume(){
		logger.info("===--->>> onResume() : ENTER");
		try{
			updateTopNav();
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}

		super.onResume();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		logger.info("===--->>> onCreateView() : ENTER");
		try{
			leftSideViewer = inflater.inflate(R.layout.routedetails_leftside, container, false);
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}

		return leftSideViewer;
	}

	@Override
	public void onViewCreated(View view, Bundle savedState){
		super.onViewCreated(view, savedState);
		logger.info("===--->>> onViewCreated() : ENTER");

		try{
			Bundle extras = getActivity().getIntent().getExtras();

			mRouteId = savedState != null ? savedState.getString(GlobalConstants.EXTRA_ROUTE_ID) : null; // required

			if(mRouteId == null){
				mRouteId = extras != null ? extras.getString(GlobalConstants.EXTRA_ROUTE_ID) : null;
			}

			mJobId = savedState != null ? savedState.getString(GlobalConstants.EXTRA_JOB_ID) : null; // required

			if(mJobId == null){
				mJobId = extras != null ? extras.getString(GlobalConstants.EXTRA_JOB_ID) : null;
			}

			mJobDetailId = savedState != null ? savedState.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0; // required

			if(mJobDetailId == 0){
				mJobDetailId = extras != null ? extras.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0;
			}

			mDeliveryQuads = savedState != null ? savedState
					.getInt(GlobalConstants.EXTRA_DELIVERY_QUADS) : GlobalConstants.DEF_DELIVERY_QUADS_NONE; // required

			if(mDeliveryQuads == GlobalConstants.DEF_DELIVERY_QUADS_NONE){
				mDeliveryQuads = extras != null ? extras
						.getInt(GlobalConstants.EXTRA_DELIVERY_QUADS)
												: GlobalConstants.DEF_DELIVERY_QUADS_LEFT_FRONT_AND_RIGHT_FRONT;
			}

			TextView tv = leftSideViewer.findViewById(R.id.routeDetailsTitle);
			tv.setFocusable(true);
			tv.setFocusableInTouchMode(true);
			tv.requestFocus();

			//=== SEQUENCING ONLY
			logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
			if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
				tv.setText(R.string.deliveryTitle);
			}
			else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
				tv.setText(R.string.sequenceTitle);
			}
			else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
				tv.setText("Renumbering");
			}

			mSearchTable = leftSideViewer.findViewById(R.id.routeDetailsSearchBox);
			mSearchEnable = leftSideViewer.findViewById(R.id.btnRouteDetailsSearchEnabled);

			Route mRoute = DBHelper.getInstance().fetchRoute_Common(mJobDetailId);

			//=== RANDOM ROUTE
			if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.RANDOM.name()) ||
			   CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
				mSearchEnable.setVisibility(View.VISIBLE);
				mSearchEnable.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v){
						if(rightSideViewer == null){
							rightSideViewer = (RouteDetailsRightSideFragmentMerged) getParentFragmentManager()
									.findFragmentById(R.id.fragmentContainerViewRightSide);
						}

						//=== CLOSE SEARCH BAR
						if(mIsSearchOpen){
							mSearchEnable.setVisibility(View.VISIBLE);
							mIsSearchOpen = false;
							mSearchTable.setVisibility(View.GONE);

							rightSideViewer.pauseButton.setVisibility(View.VISIBLE);
						}
						//=== OPEN SEARCH BAR
						else{
							mSearchEnable.setVisibility(View.INVISIBLE);
							mIsSearchOpen = true;
							mSearchTable.setVisibility(View.VISIBLE);

							rightSideViewer.setPauseMode(true);
							rightSideViewer.pauseButton.setVisibility(View.GONE);
						}
					}
				});
			}
			else{
				mSearchEnable.setVisibility(View.GONE);
			}

			//=== LEAVE ROUTE BUTTON
			mLeaveRouteButton = leftSideViewer.findViewById(R.id.btnLeaveRoute);
			mLeaveRouteButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					getActivity();
					LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.leave_route_dialog, getActivity().findViewById(R.id.layout_toproot));

					TextView title = layout.findViewById(R.id.headerTitle);
					title.setText(getResources().getString(R.string.deliveryAreYouSure));

					TextView completionRateText = layout.findViewById(R.id.completionRateTextView);
					int dropsDelivered = DBHelper.getInstance().fetchTotalDeliveryCountByJobDetailIdForDeliverablesOnly_Common(mJobDetailId);
					int totalDrops = DBHelper.getInstance().fetchTotalDropCountByJobDetailIdForDeliverablesOnly_Common(mJobDetailId);

					//==== 14sep19, getting a crashlytics error here on totalDrops = 0
					String percentageText = "";
					if(totalDrops == 0){
						percentageText = "*** ERROR determining Route Delivery Rate Percentage *** ";
					}
					else{
						int percentage = 100 * dropsDelivered / totalDrops;
						percentageText = "*** " + percentage + "% Route Delivery Rate " + percentage + "% *** ";
					}

					completionRateText.setText(percentageText);

					TextView text = layout.findViewById(R.id.text);

					//=== ADD A LISTING OR COUNT OF UNRECONCILED ADDRESSES HERE
					String cannedMessage = getResources().getString(R.string.deliveryResetRouteConf);

					if(totalDrops - dropsDelivered > 0){
						cannedMessage = "\n" + getResources().getString(R.string.deliveryUnreconciledMessage1) + " " + (totalDrops - dropsDelivered) + " " + getResources().getString(R.string.deliveryUnreconciledMessage2) + "\n\n" + cannedMessage;
					}
					text.setText(cannedMessage);

					ImageView image = layout.findViewById(R.id.image);
					image.setImageResource(R.drawable.erroricon);

					builder.setView(layout);
					builder.setCancelable(false);

					final AlertDialog errorDialog = builder.create();

					//=== NO BUTTON
					errorDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
										  getText(R.string.dialogNo), new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog,
													int whichButton){
									errorDialog.cancel();
								}
							});

					//=== YES BUTTON
					errorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getText(R.string.dialogYes), new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog,
											int whichButton){
							errorDialog.cancel();

							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							getActivity();
							LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

							View layout = inflater.inflate(R.layout.custom_dialog, getActivity().findViewById(R.id.layout_toproot));

							TextView title = layout.findViewById(R.id.headerTitle);
							title.setText(getResources().getString(
									R.string.areYouFinished));

							TextView text = layout.findViewById(R.id.text);
							if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
								text.setText(getResources().getString(
										R.string.areYouFinishedDelivering));
							}
							else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
								text.setText("Are you finished sequencing this route?");
							}
							else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
								text.setText("Are you finished renumbering this route?");
							}

							ImageView image = layout.findViewById(R.id.image);
							image.setImageResource(R.drawable.iconalert48);

							builder.setView(layout);
							final AlertDialog finishDialog = builder.create();

							//=== YES BUTTON
							finishDialog.setButton(
									getText(R.string.dialogYes),
									new DialogInterface.OnClickListener(){
										public void onClick(DialogInterface dialog, int whichButton){
											SharedPreferences prefs = getActivity()
													.getSharedPreferences(
															GlobalConstants.DEFAULT_PREF_FILE,
															Context.MODE_PRIVATE);

											RouteDetailsRightSideFragmentMerged rightSideViewer;
											rightSideViewer = (RouteDetailsRightSideFragmentMerged) getParentFragmentManager()
													.findFragmentByTag(RouteDetailsActivity.RIGHT_SIDE_FRAGMENT_TAG);

											if(rightSideViewer != null){
												//=== CRASHLYTICS REPORTED CRASH ON NULL getCurRouteMap()
												rightSideViewer.mDataDisplayList.clear();
												getActivity().runOnUiThread(
														new Runnable(){
															@Override
															public void run(){
																rightSideViewer.mRouteDetailListAdapterCommon.notifyDataSetChanged();
															}
														}
																		   );
											}

											CTApp.setJobDetailId(-1);

											int tZOffSet = prefs.getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);
											Calendar cal = Calendar.getInstance();
											long gmtTimestamp = cal.getTimeInMillis() + tZOffSet;
											ContentValues iVals = new ContentValues();
											iVals.put(DBHelper.KEY_JOBDETAILID, mJobDetailId);
											iVals.put(DBHelper.KEY_ENDDATE, gmtTimestamp);

											//=== set it back to 0 so that it goes over again with the end date
											iVals.put(DBHelper.KEY_UPLOADED, GlobalConstants.DEF_UPLOADED_FALSE);

											/*
											 * update routelistactivity record:
											 * routetype = routelist.routetype
											 * routefinished = 1
											 * ttladdresses = calc from delivery screen
											 * ttlremaining = calc from delivery screen
											 * update routelist record:
											 * routefinished = 1
											 */
											Route route = DBHelper.getInstance().fetchRoute_Common(mJobDetailId);
											iVals.put(DBHelper.KEY_ROUTETYPE, route.getRouteJobType());
											iVals.put(DBHelper.KEY_ROUTEFINISHED, 1);
											iVals.put(DBHelper.KEY_TTLADDRESSES, totalDeliveries);//mPhysicalDeliveredCount);
											iVals.put(DBHelper.KEY_TTLREMAINING, mUnresolvedCount);

											DBHelper.getInstance().createRecord_Common(
													iVals,
													DBHelper.DB_T_ROUTELISTACTIVITY,
													DBHelper.KEY_JOBDETAILID,
													true);

											DBHelper.getInstance().updateRouteStartOrEndDate_Common(mJobDetailId, true, 1);

											//=== DELIVERING SHOULD ALREADY HAVE RECORDS UPLOADED SET TO ZERO
											//=== IF DONE RENUMBERING THEN SET ALL RECORDS TO UPLOADED ZERO
											if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
												DBHelper.getInstance().setAllSequencingOrRenumberingRecordsToUploadForJobDetailId(mJobDetailId);
											}
											else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
												DBHelper.getInstance().setAllSequencingOrRenumberingRecordsToUploadForJobDetailId(mJobDetailId);
											}

											finishDialog.cancel();
											getActivity().finish();
										}

									});

							//=== NO BUTTON
							finishDialog.setButton(
									DialogInterface.BUTTON_NEGATIVE,
									getText(R.string.dialogNo),
									new DialogInterface.OnClickListener(){
										public void onClick(DialogInterface dialog, int whichButton){
											SharedPreferences prefs = getActivity()
													.getSharedPreferences(
															GlobalConstants.DEFAULT_PREF_FILE,
															Context.MODE_PRIVATE);
											DBHelper.getInstance().setLastJobDetailIdUsed_Common(mJobDetailId);

											CTApp.setJobDetailId(-1);

											int tZOffSet = prefs.getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);
											Calendar cal = Calendar.getInstance();
											long gmtTimestamp = cal.getTimeInMillis() + tZOffSet;
											ContentValues iVals = new ContentValues();
											iVals.put(DBHelper.KEY_JOBDETAILID, mJobDetailId);
											iVals.put(DBHelper.KEY_ENDDATE, gmtTimestamp);

											// set it back to 0 so that it goes over again with the end date
											iVals.put(DBHelper.KEY_UPLOADED, GlobalConstants.DEF_UPLOADED_FALSE);

											/*
											 * update routelistactivity record:
											 * routetype = routelist.routetype
											 * routefinished = 1
											 * ttladdresses = calc from delivery screen
											 * ttlremaining = calc from delivery screen
											 * update routelist record:
											 * routefinished = 1
											 */
											Route route = DBHelper.getInstance().fetchRoute_Common(mJobDetailId);
											iVals.put(DBHelper.KEY_ROUTETYPE, route.getRouteJobType());
											iVals.put(DBHelper.KEY_TTLADDRESSES, totalDeliveries);//mPhysicalDeliveredCount);
											iVals.put(DBHelper.KEY_TTLREMAINING, mUnresolvedCount);

											DBHelper.getInstance().createRecord_Common(
													iVals,
													DBHelper.DB_T_ROUTELISTACTIVITY,
													DBHelper.KEY_JOBDETAILID,
													true);

											DBHelper.getInstance().updateRouteStartOrEndDate_Common(mJobDetailId, true, 0);

											//=== IF DONE RENUMBERING THEN SET ALL RECORDS TO UPLOADED ZERO
											if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
												DBHelper.getInstance().setAllSequencingOrRenumberingRecordsToNotUploadForJobDetailId(mJobDetailId);
											}
											else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
												DBHelper.getInstance().setAllSequencingOrRenumberingRecordsToNotUploadForJobDetailId(mJobDetailId);
											}

											finishDialog.cancel();
											getActivity().finish();
										}
									});

							finishDialog.show();
							//=== Change the alert dialog background color
							finishDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

							//=== Get the alert dialog buttons reference
							Button positiveButton = finishDialog.getButton(AlertDialog.BUTTON_POSITIVE);
							Button negativeButton = finishDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

							//=== Change the alert dialog buttons text and background color
							positiveButton.setTextColor(Color.BLACK);
							positiveButton.setBackgroundColor(Color.CYAN);

							negativeButton.setTextColor(Color.BLACK);
							negativeButton.setBackgroundColor(Color.CYAN);

							LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
							layoutParams.weight = 10;
							layoutParams.leftMargin = 5;
							layoutParams.rightMargin = 5;
							positiveButton.setLayoutParams(layoutParams);
							negativeButton.setLayoutParams(layoutParams);
						}

					});

					//=== CRASHLYTICS ERROR HERE
					errorDialog.show();
					//=== Change the alert dialog background color
					errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

					//=== Get the alert dialog buttons reference
					Button positiveButton = errorDialog.getButton(AlertDialog.BUTTON_POSITIVE);
					Button negativeButton = errorDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
					Button neutralButton = errorDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

					//=== Change the alert dialog buttons text and background color
					positiveButton.setTextColor(Color.BLACK);
					positiveButton.setBackgroundColor(Color.CYAN);

					negativeButton.setTextColor(Color.BLACK);
					negativeButton.setBackgroundColor(Color.CYAN);

					neutralButton.setTextColor(Color.BLACK);
					neutralButton.setBackgroundColor(Color.CYAN);

					LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
					layoutParams.weight = 10;
					layoutParams.leftMargin = 5;
					layoutParams.rightMargin = 5;
					positiveButton.setLayoutParams(layoutParams);
					negativeButton.setLayoutParams(layoutParams);
					neutralButton.setLayoutParams(layoutParams);
				}
			});

			showListView(GlobalConstants.ROUTEDETAILS);

			mSearchET = leftSideViewer.findViewById(R.id.searchVal);
			mSearchButton = leftSideViewer.findViewById(R.id.btnRouteDetailsSearchForAddress);

			mSearchButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					Vector<String> errVect = new Vector<String>();

					//=== skipping validation to enable reseting the route
					mSearchValStr = mSearchET.getText().toString();
					//=== checks if null or too short, returns error message if so
					errVect = validateForm();

					//=== If there was an error in the search or text from it
					if(errVect != null && errVect.size() > 0){
						Iterator<String> it = errVect.iterator();
						String err = "";

						while(it.hasNext()){
							err = err + System.getProperty("line.separator") + it.next();
						}

						errVect.removeAllElements();
						showDialog(getResources().getString(R.string.errorDialogTitle), err);

					}
					//=== Search returned legit value to search for
					else{
						if(rightSideViewer == null){
							rightSideViewer = (RouteDetailsRightSideFragmentMerged) getParentFragmentManager()
									.findFragmentById(R.id.fragmentContainerViewRightSide);
						}

						mIsPauseMode = true;

						rightSideViewer.setPauseMode(mIsPauseMode);

						rightSideViewer.updateListBySearch(mSearchValStr, GlobalConstants.DEF_DELIVERY_QUADS_ALL_LEFT_RIGHT_FRONT_REAR);
					}
				}
			});

			mSClearButton = leftSideViewer.findViewById(R.id.btnRouteDetailsSearchClearForAddress);

			mSClearButton.setOnClickListener(new View.OnClickListener(){

				public void onClick(View v){
					closeSearchBar();
				}

			});
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}
	}

	private void showDialog(String titleStr, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog,
									   getActivity().findViewById(R.id.layout_root));
		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText(titleStr);
		TextView text = layout.findViewById(R.id.text);
		text.setText(message);
		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.erroricon);

		builder.setView(layout);
		AlertDialog errorDialog = builder.create();

		errorDialog.setButton(DialogInterface.BUTTON_POSITIVE,
							  getResources().getString(R.string.dialogClose),
							  new DialogInterface.OnClickListener(){
								  public void onClick(DialogInterface dialog, int id){

									  dialog.cancel();

								  }
							  });

		errorDialog.show();
	}

	/**
	 * Validates all fields in register form
	 * @return
	 */
	private Vector<String> validateForm(){
		Vector<String> vect = new Vector<String>();
		boolean isValid = false;

		try{// Checks if edit text string is empty or less than 2 characters
			isValid = FormValidation.requiredField(mSearchValStr,
												   GlobalConstants.ROUTE_CHARS_MIN_SEARCH);
			if(!isValid){
				vect.add(getResources().getString(R.string.searchErrorMinChars));
			}

		}
		catch(Exception e){
			vect.add(getResources().getString(R.string.searchErrorUnknown));
		}

		return vect;
	}

	protected void closeSearchBar(){
		if(mIsPauseMode){
			if(rightSideViewer == null){
				rightSideViewer = (RouteDetailsRightSideFragmentMerged) getParentFragmentManager()
						.findFragmentById(R.id.fragmentContainerViewRightSide);
			}

			mSearchET.setText("");
			mSearchValStr = null;

			mIsPauseMode = false;
			rightSideViewer.pauseButton.setVisibility(View.VISIBLE);
			rightSideViewer.setPauseMode(mIsPauseMode);

			Route mRoute = DBHelper.getInstance().fetchRoute_Common(mJobDetailId);
			if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
				rightSideViewer.updateListFromHandlerSequenced(mSearchValStr, mDeliveryQuads, true);//final String sFilter, final int deliveryQuadsMode, boolean forceUpdate
			}
			else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
				rightSideViewer.mDataDisplayList.clear();
				rightSideViewer.updateListFromHandlerRandom(mSearchValStr, mDeliveryQuads, true);//final String sFilter, final int deliveryQuadsMode, boolean forceUpdate
			}
			else if(mRoute.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
				rightSideViewer.mDataDisplayList.clear();
				rightSideViewer.updateListFromHandlerRandom(mSearchValStr, mDeliveryQuads, true);//final String sFilter, final int deliveryQuadsMode, boolean forceUpdate
			}

			mSearchEnable.setVisibility(View.VISIBLE);
			mSearchEnable.setBackgroundResource(R.drawable.searching_enable);
			mIsSearchOpen = false;
			mSearchTable.setVisibility(View.GONE);
		}
		else{
			mSearchET.setText("");
			mSearchValStr = null;

			mSearchEnable.setBackgroundResource(R.drawable.searching_enable);
			mIsSearchOpen = false;
			mSearchTable.setVisibility(View.GONE);
		}
	}

	//=== POP UP VIEW SHOWING TOTAL ADDRESSES ETC AND PHOTO STATS
	private void showListView(int listToShow){
		final LinearLayout routeDetailsStatisticsContainer = leftSideViewer.findViewById(R.id.routeDetailsStatisticsContainer);

		final ImageView routeDetailsPlusMinus = leftSideViewer.findViewById(R.id.routeDetailsPlusMinus);

		//=== OPENS ROUTE DETAILS AND SETS IMAGE TO NEGATIVE ICON
		routeDetailsPlusMinus.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				if(routeDetailsStatisticsContainer.getVisibility() != View.VISIBLE){
					updateRouteMetrics();
					routeDetailsStatisticsContainer.bringToFront();
					routeDetailsStatisticsContainer.setVisibility(View.VISIBLE);
					routeDetailsPlusMinus.setImageResource(R.drawable.blueminus);
				}
			}
		});

		//=== CLOSES ROUTE DETAILS AND SETS IMAGE TO PLUS ICON
		routeDetailsStatisticsContainer.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				if(view.getVisibility() == View.VISIBLE){
					view.setVisibility(View.GONE);

					routeDetailsPlusMinus.setImageResource(R.drawable.blueplus);
				}
			}
		});

		final ImageView routeActivityButton = leftSideViewer.findViewById(R.id.routeDetailsActivity);
		Route thisRoute = DBHelper.getInstance().fetchRoute_Common(mJobDetailId);
		if(thisRoute.isContractor()){
			routeActivityButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View view){
					displayRouteActivityMenu();
				}
			});
		}
		else{
			routeActivityButton.setVisibility(View.GONE);
		}
	}

	private void displayRouteActivityMenu(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.route_activity_action_menu_dialog, getActivity().findViewById(R.id.layout_root));

		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText("Route Activity Menu");
		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);

		builder.setView(layout);
		final AlertDialog routeActivityMenuDialog = builder.create();

		routeActivityMenuDialog.setButton(AlertDialog.BUTTON_POSITIVE,
										  "Cancel",
										  new DialogInterface.OnClickListener(){
											  public void onClick(DialogInterface dialog, int id){
												  routeActivityMenuDialog.dismiss();
											  }
										  });

		routeActivityMenuDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
										  "Add Activity",
										  new DialogInterface.OnClickListener(){
											  public void onClick(DialogInterface dialog, int id){
												  routeActivityMenuDialog.dismiss();

												  //DISPLAY LIST OF ACTIVITIES for this route
												  addRouteActivityListDialog();
											  }
										  });

		routeActivityMenuDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
										  "View Activities",
										  new DialogInterface.OnClickListener(){
											  public void onClick(DialogInterface dialog, int id){
												  routeActivityMenuDialog.dismiss();

												  //DISPLAY THIS ROUTES ACTIVITIES
												  showRouteActivityListDialog();
											  }
										  });

		routeActivityMenuDialog.show();
		//=== Change the alert dialog background color
		routeActivityMenuDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

		//=== Get the alert dialog buttons reference
		Button positiveButton = routeActivityMenuDialog.getButton(AlertDialog.BUTTON_POSITIVE);

		//=== Change the alert dialog buttons text and background color
		positiveButton.setTextColor(Color.BLACK);
		positiveButton.setBackgroundColor(Color.CYAN);

		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
		layoutParams.leftMargin = 5;
		layoutParams.rightMargin = 5;
		layoutParams.weight = 1;
		positiveButton.setLayoutParams(layoutParams);

		//=== Get the alert dialog buttons reference
		Button nuetralButton = routeActivityMenuDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

		//=== Change the alert dialog buttons text and background color
		nuetralButton.setTextColor(Color.BLACK);
		nuetralButton.setBackgroundColor(Color.CYAN);

		LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) nuetralButton.getLayoutParams();
		layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;
		layoutParams2.leftMargin = 5;
		layoutParams2.rightMargin = 5;
		layoutParams2.weight = 1;
		nuetralButton.setLayoutParams(layoutParams2);

		//=== Get the alert dialog buttons reference
		Button negativeButton = routeActivityMenuDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

		//=== Change the alert dialog buttons text and background color
		negativeButton.setTextColor(Color.BLACK);
		negativeButton.setBackgroundColor(Color.CYAN);

		LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) negativeButton.getLayoutParams();
		layoutParams3.gravity = Gravity.CENTER_HORIZONTAL;
		layoutParams3.leftMargin = 5;
		layoutParams3.rightMargin = 5;
		layoutParams3.weight = 1;
		negativeButton.setLayoutParams(layoutParams3);
	}

	public void addRouteActivityListDialog(){
		Object[][] activity = {{"Wharehouse Start", 0},
							   {"Wharehouse Stop", 1},
							   {"Sorting Start", 2},
							   {"Sorting Stop", 3},
							   {"Loading Start", 4},
							   {"Loading Stop", 5},
							   {"Break Start", 6},
							   {"Break Stop", 7},
							   {"Lunch Start", 8},
							   {"Lunch Stop", 9},
							   {"Administrative Start", 10},
							   {"Administrative Stop", 11}};

		//=== setup the alert builder
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.route_activity_action_add_select_dialog, getActivity().findViewById(R.id.layout_root));

		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText("Route Activity List");
		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);

		final RadioGroup radioGroup = layout.findViewById(R.id.activitySelectRadioGroup);

		for(int i = 0; i < activity.length; i++){
			String activityDescription = (String) activity[i][0];
			Integer activityId = (Integer) activity[i][1];

			RadioButton radioButton = new RadioButton(radioGroup.getContext());
			radioButton.setBackgroundColor(getResources().getColor(R.color.white));
			LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams4.weight = 1;
			radioButton.setLayoutParams(layoutParams4);
			radioButton.setText(activityDescription);
			radioButton.setTextColor(getResources().getColor(R.color.black));
			radioButton.setId(i);
			radioButton.setTag(activityId);

			ColorStateList colorStateList = new ColorStateList(
					new int[][]{
							new int[]{-android.R.attr.state_checked},
							new int[]{android.R.attr.state_checked}
					},
					new int[]{
							Color.BLACK,
							Color.WHITE
					}
			);

			radioButton.setButtonTintList(colorStateList);

			if(activityDescription.toUpperCase().contains("START")){
				radioButton.setBackgroundColor(getResources().getColor(R.color.activitygreen));
			}
			else{
				radioButton.setBackgroundColor(getResources().getColor(R.color.activityred));
			}

			radioGroup.addView(radioButton);
		}

		builder.setView(layout);

		builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		});

		builder.setNegativeButton("Add", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();

				//ADD CODE TO ADD A RECORD FOR THIS ROUTE TO THE ROUTE ACTIVITIES TABLE
				int id = radioGroup.getCheckedRadioButtonId();
				if(id < 0){
					logger.warn("SELECTED WORK ACTIVITY FROM RADIO GROUP IS < 0");
				}
				else{
					int activityId = (Integer) activity[id][1];
					String activityDescription = (String) activity[id][0];

					DBHelper.getInstance().addRouteActivityForJobDetailId(mJobDetailId, activityId, activityDescription);
				}
			}
		});

		//=== create and show the alert dialog
		final AlertDialog dialog = builder.create();

		dialog.show();

		//=== Change the alert dialog background color
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

		//=== Get the alert dialog buttons reference
		Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

		//=== Change the alert dialog buttons text and background color
		negativeButton.setTextColor(Color.BLACK);
		negativeButton.setBackgroundColor(Color.CYAN);

		LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) negativeButton.getLayoutParams();
		layoutParams3.gravity = Gravity.CENTER_HORIZONTAL;
		layoutParams3.leftMargin = 5;
		layoutParams3.rightMargin = 5;
		layoutParams3.weight = 1;
		negativeButton.setLayoutParams(layoutParams3);

		//=== Get the alert dialog buttons reference
		Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

		//=== Change the alert dialog buttons text and background color
		positiveButton.setTextColor(Color.BLACK);
		positiveButton.setBackgroundColor(Color.CYAN);

		LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
		layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;
		layoutParams2.leftMargin = 5;
		layoutParams2.rightMargin = 5;
		layoutParams2.weight = 1;
		positiveButton.setLayoutParams(layoutParams2);
	}

	public void showRouteActivityListDialog(){
		//=== setup the alert builder
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.route_activity_action_view_dialog, getActivity().findViewById(R.id.layout_root));

		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText("Route Activity List");
		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);

		builder.setView(layout);

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		});

		//=== GET LIST FROM DBHelper CALL FOR ACTIVITY DESCRIPTION AND DATE FROM ROUTEACTIVITIES TABLE
		String[] activities = DBHelper.getInstance().fetchRouteActivitiesForJobDetailId(this.mJobDetailId);

		ListView activityListView = layout.findViewById(R.id.activityListView);
		RouteActivityViewListAdapter activityListAdapter = new RouteActivityViewListAdapter(this.getActivity(), new ArrayList<String>(Arrays.asList(activities)));
		activityListView.setAdapter(activityListAdapter);

		//=== create and show the alert dialog
		final AlertDialog dialog = builder.create();

		dialog.show();

		//=== Change the alert dialog background color
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

		// Get the alert dialog buttons reference
		Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

		//=== Change the alert dialog buttons text and background color
		negativeButton.setTextColor(Color.BLACK);
		negativeButton.setBackgroundColor(Color.CYAN);
	}

	private void updateRouteMetrics(){
		//=== GET ALL DELIVERIES FOR THIS ROUTE
		totalDeliveries = DBHelper.getInstance().fetchCountByQuery_Common("select count(*) from addressdetaillist "
																		  + "where jobdetailid = " + mJobDetailId);

		//=== GET ALL DELIVERED PLUS UNDELIVERABLES AS A LOGICAL RESOLVED COUNT
		mResolvedCount = DBHelper.getInstance().fetchLogicalTotalDeliveredCountByJobDetailId_Common(mJobDetailId);

		//=== GET COUNT ON UPLOADED DELIVERIES
		mUploadCount = DBHelper.getInstance().fetchTotalUploadedCountByJobDetailId_Common(mJobDetailId);

		mUnresolvedCount = totalDeliveries - mResolvedCount;

		mPhotosRequiredCount = DBHelper.getInstance().fetchCountPhotosRequested_Common(mJobDetailId);

		mPhotosTakenCount = DBHelper.getInstance().fetchCountPhotosTaken_Common(mJobDetailId);

		mPhotosUploadedCount = DBHelper.getInstance().fetchCountPhotosUploaded_Common(mJobDetailId);

		mTotalValTV = leftSideViewer.findViewById(R.id.routeDetailsTotalVal);
		mTotalValTV.setText("" + totalDeliveries);//mPhysicalDeliveredCount);

		mRemainingValTV = leftSideViewer.findViewById(R.id.routeDetailsRemainingVal);
		mRemainingValTV.setText("" + mUnresolvedCount);

		mCompletedValTV = leftSideViewer.findViewById(R.id.routeDetailsCompletedVal);
		mCompletedValTV.setText("" + mResolvedCount);

		mUploadedTV = leftSideViewer.findViewById(R.id.routeDetailsUploadedVal);
		mUploadedTV.setText("" + mUploadCount);

		mPicturesTakenTV = leftSideViewer.findViewById(R.id.routeDetailsPhotosTakenVal);
		mPicturesTakenTV.setText("" + mPhotosTakenCount);

		mPicturesReqTV = leftSideViewer.findViewById(R.id.routeDetailsPhotosRequestedVal);
		mPicturesReqTV.setText("" + mPhotosRequiredCount);

		mPhotosUploadedTV = leftSideViewer.findViewById(R.id.routeDetailsPhotosUploadedVal);
		mPhotosUploadedTV.setText("" + mPhotosUploadedCount);
	}

	@SuppressLint("RestrictedApi")
	//=== POPULATES VALUES IN POP UP VIEW SHOWING TOTAL ADDRESSES ETC AND PHOTO STATS
	private void updateTopNav(){
		try{
			if(getActivity() != null){
				mJobIdTV = leftSideViewer.findViewById(R.id.routeDetailsJobIdVal);
				mJobIdTV.setText(mJobId);

				mRouteIdTV = leftSideViewer.findViewById(R.id.routeDetailsRouteIdVal);
				mRouteIdTV.setText(mRouteId);

				updateRouteMetrics();

				mGpsStatusRadioButton = leftSideViewer.findViewById(R.id.gpsRadioButton2);
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
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}
	}
}