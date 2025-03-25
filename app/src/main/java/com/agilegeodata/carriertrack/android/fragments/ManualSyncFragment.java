package com.agilegeodata.carriertrack.android.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.ManualSyncContainer;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;
import com.agilegeodata.carriertrack.android.objects.ServerAccessActivity;
import com.agilegeodata.carriertrack.android.services.ConnectionStatus;
import com.agilegeodata.carriertrack.android.services.DownloadService;
import com.agilegeodata.carriertrack.android.services.UploadService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Enables the manual upload and download of data
 */
public class ManualSyncFragment extends Fragment{
	public static final String TAG = ManualSyncFragment.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static ProgressDialog checkProgressDialog;
	private static boolean gpsRadioButtonIsChecked = false;
	TextView mTopNavLine1;
	TextView mTopNavLine2;
	TextView mLastServerConnection;
	AppCompatRadioButton mGpsStatusRadioButton;
	private boolean mHaveError;
	private RelativeLayout row1RL;
	private RelativeLayout row2RL;
	private final Handler uploadDownloadMessageHandler = new Handler(){
		public void handleMessage(Message msg){
			row1RL.setClickable(true);
			row2RL.setClickable(true);
			Bundle data = msg.getData();
			mHaveError = data.getBoolean(GlobalConstants.EXTRA_UPLOAD_STATUS, false);
			String error = data.getString(GlobalConstants.EXTRA_ERROR_MESSAGE);
			ImageView iv1 = getActivity().findViewById(R.id.spinnerView);
			// logger.debug("Entered the uploadDownloadMessageHandler" + mHaveError);

			if(mHaveError){
				// logger.debug("Was Not successful");
				Drawable d = getResources().getDrawable(R.drawable.iconalert);
				float width = d.getMinimumWidth() / 2;
				float height = d.getMinimumHeight() / 2;
				RotateAnimation a = new RotateAnimation(0, 360, width, height);
				a.setDuration(0);
				a.setRepeatCount(0);
				iv1.startAnimation(a);
				iv1.setImageDrawable(d);
				TextView statusMessageE = getActivity().findViewById(R.id.statusMessage);
				statusMessageE.setText(error);
			}
			else{
				// logger.debug("Was successful");

				int type = data.getInt(GlobalConstants.EXTRA_TASK_TYPE);
				Drawable d = getResources().getDrawable(R.drawable.status_delivered);
				float width = d.getMinimumWidth() / 2;
				float height = d.getMinimumHeight() / 2;
				RotateAnimation a = new RotateAnimation(0, 360, width, height);
				a.setDuration(0);
				a.setRepeatCount(0);
				iv1.startAnimation(a);
				iv1.setImageDrawable(d);
				TextView statusMessageE = getActivity().findViewById(R.id.statusMessage);
				statusMessageE.setText(error);

				if(type == GlobalConstants.MANUAL_UPLOAD){
					statusMessageE.setText(getResources().getString(
							R.string.manualSyncSuccessUploadData));
				}
				else if(type == GlobalConstants.MANUAL_DOWNLOAD){
					statusMessageE.setText(getResources().getString(
							R.string.manualSyncSuccessDownloadData));
				}
			}
		}
	};
	private final BroadcastReceiver uploadStatusReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
			Bundle data = intent.getExtras();
			mHaveError = data.getBoolean(GlobalConstants.EXTRA_UPLOAD_STATUS, false);

			data.putInt(GlobalConstants.EXTRA_TASK_TYPE, GlobalConstants.MANUAL_UPLOAD);
			// logger.debug("Success: " + mHaveError +"  error message: " +
			// error);
			Message msg = new Message();
			msg.setData(data);

			uploadDownloadMessageHandler.sendMessage(msg);
		}
	};
	private final BroadcastReceiver downloadStatusReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
			Bundle data = intent.getExtras();
			mHaveError = data.getBoolean(GlobalConstants.EXTRA_UPLOAD_STATUS, false);

			data.putInt(GlobalConstants.EXTRA_TASK_TYPE, GlobalConstants.MANUAL_DOWNLOAD);
			// logger.debug("Success: " + mHaveError +"  error message: " +
			// error);
			Message msg = new Message();
			msg.setData(data);

			uploadDownloadMessageHandler.sendMessage(msg);
		}
	};
	private View viewer;
	private boolean mIsProcessing;
	private final Handler gpsReceiverHandler = new Handler(){

		public void handleMessage(Message msg){
			mIsProcessing = false;

			updateTopNav();
		}

	};
	private final BroadcastReceiver gpsReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			if(!mIsProcessing){
				mIsProcessing = true;

				mLastServerConnection.setText(getResources().getString(R.string.manualSyncLastConnection)
											  + ServerAccessActivity.getServerTimestamp(getActivity()));

				mLastServerConnection.invalidate();

				gpsReceiverHandler.sendEmptyMessage(0);
			}
		}
	};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		logger.debug("Entering Left Side Fragment on Create");
		setRetainInstance(true);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			ContextCompat.registerReceiver(getContext(), uploadStatusReceiver,
										   new IntentFilter(GlobalConstants.SERVICE_UPLOAD), ContextCompat.RECEIVER_NOT_EXPORTED);
			ContextCompat.registerReceiver(getContext(), downloadStatusReceiver,
										   new IntentFilter(GlobalConstants.SERVICE_DOWNLOAD), ContextCompat.RECEIVER_NOT_EXPORTED);
			ContextCompat.registerReceiver(getContext(), gpsReceiver,
										   new IntentFilter(GlobalConstants.SERVICE_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED);
		}
		else{
			getContext().registerReceiver(uploadStatusReceiver,
										  new IntentFilter(GlobalConstants.SERVICE_UPLOAD));
			getContext().registerReceiver(downloadStatusReceiver,
										  new IntentFilter(GlobalConstants.SERVICE_DOWNLOAD));
			getContext().registerReceiver(gpsReceiver,
										  new IntentFilter(GlobalConstants.SERVICE_LOCATION));
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		logger.debug("Entry On Left Side Create View");
		//=== THIS FILE SHOULD BE THE DISPLAY FILE
		viewer = inflater.inflate(R.layout.manualsync, container, false);

		return viewer;
	}

	@Override
	public void onActivityCreated(Bundle savedState){
		super.onActivityCreated(savedState);
		logger.debug("---------------- Entry onActivityCreated");

		DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, ManualSyncContainer.class.getName());

		if(mLastServerConnection == null){
			mLastServerConnection = getActivity().findViewById(R.id.lastServerSync);
		}

		updateTopNav();

		//=== SENDS US BACK TO THE HOME SCREEN IF PUSHED
		LinearLayout homell = getActivity().findViewById(R.id.bottombar);
		homell.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v){
				getActivity().onBackPressed();
			}
		});

		RelativeLayout rl2a = getActivity().findViewById(R.id.statusSpinnerRL);
		rl2a.setVisibility(View.INVISIBLE);
		row1RL = getActivity().findViewById(R.id.homeScreenDeliverVerifyContainer);
		row1RL.setClickable(true);
		row1RL.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				// logger.debug("Clicked: upload");
				doMessage(
						getResources().getString(R.string.manualSyncPleaseWaitUploadData),
						GlobalConstants.MANUAL_UPLOAD);

			}
		});

		row2RL = getActivity().findViewById(R.id.row2RL);
		row2RL.setClickable(true);
		row2RL.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				// logger.debug("Clicked: download");
				doMessage(
						getResources().getString(R.string.manualSyncPleaseWaitDownloadData),
						GlobalConstants.MANUAL_DOWNLOAD);

			}
		});
	}

	private void doMessage(String string, int type){
		row1RL.setClickable(false);
		row2RL.setClickable(false);

		if(type == GlobalConstants.MANUAL_UPLOAD){
			serverCheckDialog(true);

			ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();

			if(!connectionState.isConnected){
				serverCheckDialog(false);
				logger.info("****CONNECTION STATE " + connectionState.descriptiveText + "****");
				if(row1RL != null && row2RL != null){// && row3RL != null){
					row1RL.setClickable(true);
					row2RL.setClickable(true);
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				getActivity();
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.custom_dialog, getActivity().findViewById(R.id.layout_toproot));

				TextView title = layout.findViewById(R.id.headerTitle);
				title.setText(getResources().getString(R.string.errorDialogNoConnectivityTitle));
				TextView text = layout.findViewById(R.id.text);
				text.setText(getResources().getString(R.string.manualSyncUploadErrorText));
				ImageView image = layout.findViewById(R.id.image);
				image.setImageResource(R.drawable.erroricon);

				builder.setView(layout);
				final AlertDialog errorDialog = builder.create();
				errorDialog.setButton2(getText(R.string.dialogOk), new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog,
										int whichButton){
						errorDialog.cancel();
					}
				});

				errorDialog.show();
			}
			else{
				serverCheckDialog(false);
				new LoadingAsync().execute(GlobalConstants.MANUAL_UPLOAD);
			}
		}
		else{
			serverCheckDialog(true);

			ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();

			if(!connectionState.isConnected){
				serverCheckDialog(false);
				logger.info("**** CONNECTION STATE" + connectionState.descriptiveText + "****");

				if(row1RL != null && row2RL != null){// && row3RL != null){
					row1RL.setClickable(true);
					row2RL.setClickable(true);
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				getActivity();
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.custom_dialog, getActivity().findViewById(R.id.layout_toproot));

				TextView title = layout.findViewById(R.id.headerTitle);
				title.setText(getResources().getString(R.string.errorDialogNoConnectivityTitle));
				TextView text = layout.findViewById(R.id.text);
				text.setText(getResources().getString(R.string.manualSyncDownloadErrorText));
				ImageView image = layout.findViewById(R.id.image);
				image.setImageResource(R.drawable.erroricon);

				builder.setView(layout);
				final AlertDialog errorDialog = builder.create();
				errorDialog.setButton2(getText(R.string.dialogOk), new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog,
										int whichButton){
						errorDialog.cancel();
					}
				});

				errorDialog.show();
			}
			else{
				serverCheckDialog(false);

				new LoadingAsync().execute(GlobalConstants.MANUAL_DOWNLOAD);
			}
		}
	}

	private void setLoadingMessage(final int direction){
		getActivity().runOnUiThread(new Runnable(){
			public void run(){
				RelativeLayout rl2a = getActivity().findViewById(R.id.statusSpinnerRL);
				rl2a.setVisibility(View.VISIBLE);
				ImageView iv = getActivity().findViewById(R.id.spinnerView);
				Drawable d = getResources().getDrawable(R.drawable.loading);
				iv.setImageDrawable(d);
				float width = d.getMinimumWidth() / 2;
				float height = d.getMinimumHeight() / 2;
				TextView tv = getActivity().findViewById(R.id.statusMessage);
				tv.setVisibility(View.VISIBLE);

				if(direction == GlobalConstants.MANUAL_DOWNLOAD){
					tv.setText(getResources().getString(R.string.manualSyncPleaseWaitDownloadData));
				}
				else{
					tv.setText(getResources().getString(R.string.manualSyncPleaseWaitUploadData));
				}

				RotateAnimation a = new RotateAnimation(0, 360, width, height);
				a.setDuration(3000);
				a.setRepeatCount(RotateAnimation.INFINITE);
				a.setInterpolator(new LinearInterpolator());
				a.setRepeatMode(RotateAnimation.RESTART);
				iv.startAnimation(a);
			}
		});
	}

	private void serverCheckDialog(boolean isOpening){
		if(isOpening){
			checkProgressDialog = ProgressDialog.show(getActivity(),
													  getResources().getString(R.string.manualSyncPleaseWait), getResources().getString(R.string.manualSyncCheckingConnection));
		}
		else{
			if(checkProgressDialog.isShowing() && checkProgressDialog != null){
				checkProgressDialog.dismiss();
			}
		}
	}

	@SuppressLint("RestrictedApi")
	private void updateTopNav(){
		if(getActivity() != null){
			mTopNavLine1 = getActivity().findViewById(R.id.topNavLine1);
			mTopNavLine1.setText(getResources().getString(R.string.manualSyncTitle));

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
			mGpsStatusRadioButton.setChecked(DeviceLocationListener.getHasGPsFix());//.setImageDrawable(DataUtils.determineGPSStatusImagePortrait(getResources()));
			gpsRadioButtonIsChecked = DeviceLocationListener.getHasGPsFix();
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
	}

	public void onDestroy(){
		super.onDestroy();

		if(uploadStatusReceiver != null){
			getActivity().unregisterReceiver(uploadStatusReceiver);
		}

		if(downloadStatusReceiver != null){
			getActivity().unregisterReceiver(downloadStatusReceiver);
		}

		if(gpsReceiver != null){
			getActivity().unregisterReceiver(gpsReceiver);
		}
	}

	@Override
	public void onResume(){
		super.onResume();
	}

	/**
	 * Checks for server connection and then uploads or downloads data if
	 * connected
	 */
	private class LoadingAsync extends AsyncTask<Integer, Void, Integer>{
		@Override
		protected Integer doInBackground(Integer... type){
			//=== THESE SHOULD BE ALREADY RUNNING ???, TKV, 7JUN18
			if(type[0] == GlobalConstants.MANUAL_UPLOAD){
				Intent intent = new Intent(getActivity(), UploadService.class);
				intent.putExtra(GlobalConstants.EXTRA_MANUAL_UPLOAD, true);
				logger.debug("STARTING MANUAL UPLOAD");
				getActivity().startService(intent);
			}
			else{
				Intent intent = new Intent(getActivity(), DownloadService.class);
				intent.putExtra(GlobalConstants.EXTRA_MANUAL_DOWNLOAD, true);
				logger.debug("STARTING MANUAL DOWNLOAD");
				getActivity().startService(intent);
			}

			return type[0];
		}

		@Override
		protected void onPostExecute(final Integer result){
			if(result == GlobalConstants.MANUAL_UPLOAD){
				setLoadingMessage(GlobalConstants.MANUAL_UPLOAD);
			}
			else{
				setLoadingMessage(GlobalConstants.MANUAL_DOWNLOAD);
			}
		}

		@Override
		protected void onPreExecute(){
		}

		@Override
		protected void onProgressUpdate(Void... values){
		}
	}
}