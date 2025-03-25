package com.agilegeodata.carriertrack.android.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.ScanListActivity;
import com.agilegeodata.carriertrack.android.adapters.ScanListItemAdapter;
import com.agilegeodata.carriertrack.android.barcodereader.BarcodeCaptureActivity;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;
import com.agilegeodata.carriertrack.android.objects.DeliveryItemProduct;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/*
 * Route Select
 * Enables the selection of the route
 */
public class ScanListFragment extends ListFragment{
	protected static final String TAG = ScanListFragment.class.getSimpleName();
	private static final int RC_BARCODE_CAPTURE = 9001;
	private static final int ROUTELIST_MSG_REFRESH = 1;
	private static final int ROUTELIST_REFRESH_PERIOD = 60 * 1000;
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static boolean gpsRadioButtonIsChecked = false;
	TextView mTopNavLine1;
	TextView mTopNavLine2;
	AppCompatRadioButton mGpsStatusRadioButton;
	ScanListItemAdapter mAdapter;
	private ArrayList<DeliveryItemProduct> mDataList;
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
	private final BroadcastReceiver gpsReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
			if(!mIsProcessing){
				mIsProcessing = true;
				// logger.debug("mGeoCode : " + mGeoCode);
				handlerGPS.sendEmptyMessage(0);
			}
		}
	};
	private long mDeliveryId;
	private String CURRENT_SCAN_CODE = "";

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
		}
		else{
			getContext().registerReceiver(gpsReceiver,
										  new IntentFilter(GlobalConstants.SERVICE_LOCATION));
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		// logger.debug("Entry On RouteSelect Create View");
		super.onCreateView(inflater, container, savedInstanceState);
		viewer = inflater.inflate(R.layout.scanlistselect, container, false);

		return viewer;
	}

	@Override
	public void onActivityCreated(Bundle savedState){
		super.onActivityCreated(savedState);

		// logger.debug("---------------- Entry onActivityCreated");
		Bundle extras = getActivity().getIntent().getExtras();

		DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, ScanListActivity.class.getName());

		mDeliveryId = savedState != null ? savedState.getLong(GlobalConstants.EXTRA_DELIVERYID) : 0; // required

		if(mDeliveryId == 0){
			mDeliveryId = extras != null ? extras.getLong(GlobalConstants.EXTRA_DELIVERYID) : 0;
		}

		ArrayList<DeliveryItemProduct> products = DBHelper.getInstance().fetchAllDeliveryItemScanProductForDeliveryId(mDeliveryId);
		mDataList = products;

		updateTopNav();

		//=== SENDS US BACK TO THE HOME SCREEN IF PUSHED
		LinearLayout homell = getActivity().findViewById(R.id.bottombar);
		homell.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v){
				getActivity().onBackPressed();

			}

		});

		mAdapter = new ScanListItemAdapter(getActivity(), mDataList);
		setListAdapter(mAdapter);
	}

	public void onListItemClick(ListView l, View v, int position, long id){
		super.onListItemClick(l, v, position, id);

		DeliveryItemProduct di = mDataList.get(position);
		CURRENT_SCAN_CODE = di.getScanCode();

		//=== launch barcode activity.
		Intent intent = new Intent(ScanListFragment.this.getActivity(), BarcodeCaptureActivity.class);
		intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
		intent.putExtra(BarcodeCaptureActivity.UseFlash, true);

		startActivityForResult(intent, RC_BARCODE_CAPTURE);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == RC_BARCODE_CAPTURE){
			if(resultCode == CommonStatusCodes.SUCCESS){
				if(data == null){
					//=== do an alert dialog SCAN FAILURE
					final AlertDialog.Builder dialog4 = new AlertDialog.Builder(this.getActivity());
					dialog4.setTitle("Barcode Failure").setMessage("The scan for this barcode failed.")
						   .setPositiveButton(getResources().getString(R.string.dialogOk), new DialogInterface.OnClickListener(){
							   @Override
							   public void onClick(DialogInterface paramDialogInterface, int paramInt){
								   paramDialogInterface.dismiss();
							   }
						   });
					dialog4.show();
					Log.d(TAG, "No barcode captured, scan intent data is null");
				}
				else{
					Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

					String barCodeToCheck = CURRENT_SCAN_CODE;

					if(!barcode.displayValue.equalsIgnoreCase(barCodeToCheck)){
						//=== do an alert dialog BARCODE MISMATCH
						final AlertDialog.Builder dialog2 = new AlertDialog.Builder(this.getActivity());
						dialog2.setTitle("Barcode Failure").setMessage("This delivery's barcode id does not match the scanned barcode id.")
							   .setPositiveButton(getResources().getString(R.string.dialogOk), new DialogInterface.OnClickListener(){
								   @Override
								   public void onClick(DialogInterface paramDialogInterface, int paramInt){
									   paramDialogInterface.dismiss();
								   }
							   });
						dialog2.show();
					}
					else{
						double latitude = 0;
						double longitude = 0;

						Location thisLocation = CTApp.getLocation();

						latitude = thisLocation.getLatitude();
						longitude = thisLocation.getLongitude();

						DBHelper.getInstance().updateAddressDetailProductsBarcode_Common(this.mDeliveryId, barcode.displayValue, latitude, longitude);

						RouteDetailsRightSideFragmentMerged.instance.notifyDeliveryDataChanged();

						//=== do an alert dialog SUCCESS
						final AlertDialog.Builder dialog1 = new AlertDialog.Builder(this.getActivity());
						dialog1.setTitle("Barcode Success").setMessage("You have successfully matched this delivery's barcode id.")
							   .setPositiveButton(getResources().getString(R.string.dialogOk), new DialogInterface.OnClickListener(){
								   @Override
								   public void onClick(DialogInterface paramDialogInterface, int paramInt){
									   paramDialogInterface.dismiss();
								   }
							   });
						dialog1.show();

						Log.d(TAG, "Successful Barcode read/match: " + barcode.displayValue);
					}
				}
			}
		}
		else{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void refreshScreen(){
		mDataList.clear();

		ArrayList<DeliveryItemProduct> products = DBHelper.getInstance().fetchAllDeliveryItemScanProductForDeliveryId(mDeliveryId);
		mDataList.addAll(products);

		mAdapter.notifyDataSetChanged();
	}

	@SuppressLint("RestrictedApi")
	private void updateTopNav(){
		mTopNavLine1 = getActivity().findViewById(R.id.topNavLine1);
		String line1 = "Delivery Scan(s)";

		mTopNavLine1.setText(line1);

		mTopNavLine2 = getActivity().findViewById(R.id.topNavLine2);
		mTopNavLine2.setText("Tap list item to scan.");

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
			mDataList.clear();

			ArrayList<DeliveryItemProduct> products = DBHelper.getInstance().fetchAllDeliveryItemScanProductForDeliveryId(mDeliveryId);
			mDataList.addAll(products);

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