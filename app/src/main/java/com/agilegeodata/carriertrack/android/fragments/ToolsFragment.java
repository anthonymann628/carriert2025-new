package com.agilegeodata.carriertrack.android.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.ScanListActivity;
import com.agilegeodata.carriertrack.android.activities.ToolsActivity;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/*
 * Tools screen
 * Displayed from the route details screen
 */
public class ToolsFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{
	protected static final String TAG = "ToolsFragment";
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static boolean gpsRadioButtonIsChecked = false;
	private final Handler mHandler = new Handler();
	public String routeType = "";
	AppCompatRadioButton mGpsStatusRadioButton;
	Spinner autoComplete;
	EditText otherText;
	private int mJobDetailId;
	private long mDeliveryId;
	private View viewer;
	private boolean mIsProcessing;
	private String mAddress;
	private int mTZOffSet;
	private boolean mLocationSet;
	private Button mScanButton;
	private Button mUpdateButton;
	private final Handler gpsReceiverHandler = new Handler(){

		public void handleMessage(Message msg){
			mIsProcessing = false;
			updateTopNav();

			if(!mLocationSet){
				Location location = CTApp.getLocation();
				if(location != null){
					mLocationSet = true;
					mUpdateButton.setText(getResources().getString(
							R.string.toolsButton, mAddress));
					mUpdateButton.setOnClickListener(new View.OnClickListener(){
						public void onClick(View v){
							if(!(getActivity().isFinishing())){
								showUpdateGPSDialog(getResources().getString(
										R.string.successDialogTitle), getResources().getString(
										R.string.toolsButtonConfirm));
							}
						}
					});
				}
				else{
					mUpdateButton.setText(getResources().getString(
							R.string.toolsButtonNA));
				}
			}
		}

	};
	private final BroadcastReceiver gpsReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			if(!mIsProcessing){
				mIsProcessing = true;
				//=== CRASHLYTICS BUG 3DEC18
				if(!ToolsFragment.this.isDetached()){
					gpsReceiverHandler.sendEmptyMessage(0);
				}
			}
		}
	};
	private Button mUpdateDeliveryButton;
	private Button mReconcileButton;
	private RadioGroup radioDeliveryGroup;
	private boolean mIsKeyboardShowing = false;
	private String delinfostatus;
	private String delinfoplacement;
	private String tempPlacement;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		logger.debug("Entering  Fragment on Create");
		routeType = (String) this.getActivity().getIntent().getSerializableExtra(GlobalConstants.EXTRA_ROUTE_TYPE);

		setRetainInstance(true);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			ContextCompat.registerReceiver(getContext(), gpsReceiver, new IntentFilter(GlobalConstants.SERVICE_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED);
		}
		else{
			getContext().registerReceiver(gpsReceiver, new IntentFilter(GlobalConstants.SERVICE_LOCATION));
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		logger.debug("Entry On  Create View");

		//=== THIS FILE SHOULD BE THE DISPLAY FILE
		viewer = inflater.inflate(R.layout.tools, container, false);
		return viewer;
	}

	@Override
	public void onActivityCreated(Bundle savedState){
		super.onActivityCreated(savedState);

		DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, ToolsActivity.class.getName());

		Bundle extras = getActivity().getIntent().getExtras();
		SharedPreferences prefs = getActivity().getSharedPreferences(
				GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

		mTZOffSet = prefs.getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);

		mAddress = savedState != null ? savedState.getString(GlobalConstants.EXTRA_CURRENT_ADDRESS) : null; // required

		if(mAddress == null){
			mAddress = extras != null ? extras.getString(GlobalConstants.EXTRA_CURRENT_ADDRESS) : null;
		}

		mJobDetailId = savedState != null ? savedState.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0; // required

		if(mJobDetailId == 0){
			mJobDetailId = extras != null ? extras.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0;
		}

		mDeliveryId = savedState != null ? savedState.getLong(GlobalConstants.EXTRA_DELIVERYID) : 0; // required

		if(mDeliveryId == 0){
			mDeliveryId = extras != null ? extras.getLong(GlobalConstants.EXTRA_DELIVERYID) : 0;
		}

		logger.debug("JobId: " + mJobDetailId + " DeliveryID: " + mDeliveryId);

		updateTopNav();

//		logger.debug("GPS Location is: " + mGeoCode);

		mScanButton = getActivity().findViewById(R.id.scanCodeButton);
		mScanButton.setText(getResources().getString(R.string.toolsScanButton, mAddress));
		mScanButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				if(!(getActivity().isFinishing())){
					//=== launch barcode list activity.
					Intent intent = new Intent(ToolsFragment.this.getActivity(), ScanListActivity.class);
					intent.putExtra(GlobalConstants.EXTRA_DELIVERYID, mDeliveryId);

					startActivity(intent);
				}

			}
		});

		mUpdateButton = getActivity().findViewById(R.id.btnUpdateGPS);
		Location location = CTApp.getLocation();
		if(location != null){
			mLocationSet = true;
			mUpdateButton.setText(getResources().getString(R.string.toolsButton, mAddress));
			mUpdateButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					if(!(getActivity().isFinishing())){
						showUpdateGPSDialog(getResources().getString(R.string.successDialogTitle),
											getResources().getString(R.string.toolsButtonConfirm));
					}

				}
			});
		}
		else{
			mUpdateButton.setText(getResources().getString(R.string.toolsButtonNA));
		}

		mUpdateDeliveryButton = getActivity().findViewById(R.id.btnUpdateDelivery);
		mUpdateDeliveryButton.setText(getResources().getString(R.string.toolsDeliveryButton, mAddress));
		mUpdateDeliveryButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				if(!(getActivity().isFinishing())){
					updateDropLocationDialog(getResources().getString(R.string.toolsDeliveryButton, mAddress));
				}
			}
		});

		mReconcileButton = getActivity().findViewById(R.id.btnReconcile);
		mReconcileButton.setText(getResources().getString(R.string.toolsReconciliationButton, mAddress));

		logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
		mReconcileButton.setText(getResources().getString(R.string.toolsReconciliationButton, mAddress));
		mReconcileButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				logger.debug("Tools Reconcile button pressed");
				if(!(getActivity().isFinishing())){
					Calendar cal = Calendar.getInstance();
					long gmtTimestamp = cal.getTimeInMillis() + mTZOffSet;

					Location thisLocation = CTApp.getLocation();
					if(thisLocation != null){

						if(thisLocation != null){
							double latitude = thisLocation.getLatitude();
							double longitude = thisLocation.getLongitude();

							DBHelper.getInstance().reconcileRouteDetailsDeliveredStatus_Common(mDeliveryId, gmtTimestamp, latitude, longitude);
							Toast.makeText(CTApp.getCustomAppContext(), "Forced reconciliation successful.", Toast.LENGTH_LONG).show();
							RouteDetailsRightSideFragmentMerged.instance.forceReconciliationForDrop(mDeliveryId, gmtTimestamp, latitude, longitude);
						}
						else{
							Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.toolsReconciliationUnsuccessful), Toast.LENGTH_LONG).show();
						}
					}
					else{
						Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.toolsReconciliationUnsuccessful), Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		LinearLayout homell = getActivity().findViewById(R.id.bottombar);
		homell.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				getActivity().onBackPressed();
			}
		});
	}

	public void onCheckedChanged(RadioGroup arg0, int arg1){
		if(arg1 == R.id.radioNotpickup){
			autoComplete.setVisibility(View.GONE);
			otherText.setVisibility(View.GONE);
			delinfostatus = GlobalConstants.DELINFOSTATUS_NO_PICKUP;
			hideKeyBoard();
		}

		if(arg1 == R.id.radioPlace){
			autoComplete.setVisibility(View.VISIBLE);
			delinfostatus = GlobalConstants.DELINFOSTATUS_PLACE;
			otherText.setVisibility(View.VISIBLE);
			if(autoComplete.getSelectedItem().toString() == "Other"){
				showKeyboardOnOtherText();
			}
		}
	}

	private void hideKeyBoard(){
		if(mIsKeyboardShowing){
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
			mIsKeyboardShowing = false;
		}
	}

	private void showKeyboardOnOtherText(){
		mHandler.post(new Runnable(){
			public void run(){
				InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(otherText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
				otherText.requestFocus();
				mIsKeyboardShowing = true;
			}
		});
	}

	private void updateDropLocationDialog(String titleStr){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.deliveryinfo_dialog, getActivity().findViewById(R.id.layout_root));

		radioDeliveryGroup = layout.findViewById(R.id.radiodelivery);
		autoComplete = layout.findViewById(R.id.myautocomplete);
		autoComplete.setVisibility(View.GONE);
		otherText = layout.findViewById(R.id.otherPlace);
		otherText.setVisibility(View.GONE);

		delinfoplacement = "";
		tempPlacement = "Driveway";

		String[] arr = getResources().getStringArray(R.array.deliveryoption);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, arr);
		autoComplete.setPrompt(getResources().getString(R.string.chooseLocation));
		autoComplete.setAdapter(adapter);

		autoComplete.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
				if(i == 5) // Other
				{
					autoComplete.clearFocus();
					otherText.setVisibility(View.VISIBLE);
					otherText.setHint(getResources().getString(R.string.toolsEnterOtherText));
					showKeyboardOnOtherText();
				}
				else{
					otherText.setHint("");
					otherText.setText("");
					otherText.setVisibility(View.GONE);
					hideKeyBoard();
				}
			}

			public void onNothingSelected(AdapterView<?> adapterView){
			}
		});

		radioDeliveryGroup.setOnCheckedChangeListener(this);
		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText(titleStr);
		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);
		StringBuilder info = new StringBuilder();
		StringBuilder status = new StringBuilder();

		if(DBHelper.getInstance().getAddressDetailListDeliveryInfo_Common(mDeliveryId, info, status)){
			delinfostatus = status.toString();
			delinfoplacement = info.toString();

			if(delinfostatus.equals(GlobalConstants.DELINFOSTATUS_NO_PICKUP)){
				radioDeliveryGroup.check(R.id.radioNotpickup);

			}
			else if(delinfostatus.equals(GlobalConstants.DELINFOSTATUS_PLACE)){
				radioDeliveryGroup.check(R.id.radioPlace);

				boolean selected = false;
				for(int i = 0; i < arr.length; i++){
					if(delinfoplacement.equals(arr[i])){
						autoComplete.setVisibility(View.VISIBLE);
						autoComplete.setSelection(i);
						selected = true;
					}
				}

				if(!selected){
					autoComplete.setVisibility(View.VISIBLE);
					autoComplete.setSelection(5);
					otherText.setVisibility(View.VISIBLE);
					otherText.setText(delinfoplacement);
				}
			}
		}

		builder.setView(layout);
		final AlertDialog updateDialog = builder.create();

		updateDialog.setButton(getResources().getString(R.string.save),
							   new DialogInterface.OnClickListener(){
								   public void onClick(DialogInterface dialog, int id){
								   }
							   });

		updateDialog.setButton2(getResources().getString(R.string.dialogClose),
								new DialogInterface.OnClickListener(){
									public void onClick(DialogInterface dialog, int id){
										dialog.cancel();
										hideKeyBoard();
									}
								});

		updateDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog){
				// TODO Auto-generated method stub
				Button save = updateDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				save.setOnClickListener(new View.OnClickListener(){

					public void onClick(View v){
						// TODO Auto-generated method stub
						boolean okToExit = true;
						tempPlacement = String.valueOf(autoComplete.getSelectedItem());

						if(!tempPlacement.equals("Select Location")){
							if(delinfostatus.equals(GlobalConstants.DELINFOSTATUS_PLACE)){
								if(tempPlacement.length() > 1){
									delinfoplacement = tempPlacement;
									String[] arr = getResources().getStringArray(R.array.deliveryoption);
									String[] engArray = {"Select Location", "Driveway", "Garage", "Porch", "Skyhook", "Other"};

									for(int i = 0; i < arr.length; i++){
										if(tempPlacement.equals(arr[i])){
											delinfoplacement = engArray[i];
										}
									}
								}
								else{
									delinfoplacement = "Driveway";
								}
							}

							if(delinfoplacement.equals("Other")){
								delinfoplacement = otherText.getText().toString();
								if(otherText.getText().length() == 0){
									okToExit = false;
								}
							}
						}

						// If it's not placement, blank it out
						if(!delinfostatus.equals(GlobalConstants.DELINFOSTATUS_PLACE)){
							delinfoplacement = null;
						}

						if(okToExit){
							Calendar cal = Calendar.getInstance();
							long gmtTimestamp = cal.getTimeInMillis() + mTZOffSet;

							DBHelper.getInstance().updateAddressDetailListDeliveryInfo_Common(mDeliveryId, delinfostatus, delinfoplacement, gmtTimestamp);

							updateDialog.cancel();
							getActivity().finish();
							hideKeyBoard();
						}
						else{
							Toast toast = Toast.makeText(CTApp.getCustomAppContext(), "Other " + getResources().getString(R.string.toolsMustHaveValue), Toast.LENGTH_LONG);

							toast.show();
						}
					}
				});
			}
		});

		updateDialog.show();
	}

	private void showUpdateGPSDialog(String titleStr, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog, getActivity().findViewById(R.id.layout_toproot));

		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText(titleStr);
		TextView text = layout.findViewById(R.id.text);
		text.setText(message);
		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);

		builder.setView(layout);
		AlertDialog errorDialog = builder.create();

		errorDialog.setButton(getResources().getString(R.string.dialogYes),
							  new DialogInterface.OnClickListener(){
								  public void onClick(DialogInterface dialog, int id){
									  Location location = CTApp.getLocation();
									  if(location != null){
										  String[] gps = {location.getLatitude() + "", location.getLongitude() + ""};
										  logger.debug("Updating the location in the database");

										  Calendar cal = Calendar.getInstance();
										  long gmtTimestamp = cal.getTimeInMillis() + mTZOffSet;
										  DBHelper.getInstance().updateAddressDetailListLatitudeLongitude_Common(
												  mDeliveryId, gps[0], gps[1], gmtTimestamp);
									  }
									  else{
										  logger.debug("************ NOT Updating the location in the database");
									  }

									  dialog.cancel();
									  getActivity().finish();
								  }
							  });

		errorDialog.setButton2(getResources().getString(R.string.dialogNo), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				dialog.cancel();
			}
		});

		errorDialog.show();
	}

	@SuppressLint("RestrictedApi")
	private void updateTopNav(){
		try{
			TextView mTopNavLine1 = getActivity().findViewById(R.id.topNavLine1);
			mTopNavLine1.setText(getResources().getString(R.string.toolsTitle));

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
		catch(Exception e){
			logger.debug("Ex:" + e);
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy(){
		try{
			getActivity().unregisterReceiver(gpsReceiver);
		}
		catch(Exception e){
			logger.error("Exception", e);
		}

		super.onDestroy();
	}

	@Override
	public void onStop(){
		super.onStop();
	}

	@Override
	public void onResume(){
		super.onResume();
	}
}