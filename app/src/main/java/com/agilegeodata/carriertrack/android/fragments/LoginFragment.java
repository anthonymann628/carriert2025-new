package com.agilegeodata.carriertrack.android.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.HomeActivity;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.objects.LoginDetail;
import com.agilegeodata.carriertrack.android.services.CleanupIntentService;
import com.agilegeodata.carriertrack.android.services.ConnectionStatus;
import com.agilegeodata.carriertrack.android.services.DownloadService;
import com.agilegeodata.carriertrack.android.services.UploadService;
import com.agilegeodata.carriertrack.android.utils.DateUtil;
import com.agilegeodata.carriertrack.android.utils.FormValidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

/*
 * Login screen
 */
public class LoginFragment extends Fragment{
	protected static final String TAG = LoginFragment.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static ProgressDialog checkProgressDialog;
	private View viewer;
	private EditText mPassword;
	private final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case 1:
					try{
						LoginDetail l = new LoginDetail();
						SharedPreferences prefs = getActivity().getSharedPreferences(
								GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
						int tZOffSet = prefs.getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);

						Calendar cal = Calendar.getInstance();
						long gmtTimestamp = cal.getTimeInMillis() + tZOffSet;

						l.setLoginDate(gmtTimestamp);
						l.setStatus(GlobalConstants.DEF_STATUS_LOGGEDIN);
						l.setUploaded(GlobalConstants.DEF_UPLOADED_FALSE);

						DBHelper.getInstance().createRecord_Common(l.createIntialValues(), DBHelper.DB_T_LOGINS, DBHelper.KEY_ID, false);

						String lastLoginTime = DBHelper.getInstance().fetchItemValueByItemName_Common(GlobalConstants.PREF_LASTTIME_LOGIN);
						boolean isOutdated = false;
						if(lastLoginTime != null){
							Long resolvedLastLoginTime = Long.valueOf(lastLoginTime);

							Calendar loginTime24HoursPriorToNow = Calendar.getInstance();
							loginTime24HoursPriorToNow.add(Calendar.HOUR, GlobalConstants.DEF_LOGIN_EXPIRED_VALUE);
							if(resolvedLastLoginTime < loginTime24HoursPriorToNow.getTimeInMillis()){
								logger.info("Login has expired");
								isOutdated = true;
							}
						}
						else{
							isOutdated = true;
						}

						Calendar c = Calendar.getInstance();
						DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.PREF_LASTTIME_LOGIN, "" + c.getTimeInMillis());

						//logger.debug("Setting the GMT login time to: "+ gmtTimestamp);

						Editor edit = prefs.edit();
						edit.putLong(GlobalConstants.PREF_LASTTIME_LOGIN, c.getTimeInMillis());
						edit.commit();

						//=== CHECK FOR 24 HOUR LOGIN, DO FORCED DOWNLOAD IF GREATER
						if(isOutdated){
							doMessage(getResources().getString(R.string.manualSyncPleaseWaitDownloadData));
						}
						else{
							Intent intent = new Intent(getActivity(), HomeActivity.class);
							startActivity(intent);
						}
					}
					catch(Exception e){
						logger.debug("Exception caught" + e);
					}

					break;
				default:
					mPassword.setText("");

					if(!(getActivity().isFinishing())){
						showDialog(getResources().getString(R.string.errorDialogTitle), getResources().getString(R.string.loginErrorInvalidPasswordMatch));
					}

					break;
			}
		}

	};
	private Button mSubmitButton;
	private String mUserNameStr = null;
	private String mEnteredPasswordStr = null;
	private LinearLayout downloadOverlay;

	//=== FOR FORCED DOWNLOAD
	private boolean mHaveError;

	private final Handler uploadDownloadMessageHandler = new Handler(){
		public void handleMessage(Message msg){
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

				//===UPLOADS NOT DONE IN LOGIN FRAGMENT
				if(type == GlobalConstants.MANUAL_UPLOAD){
					statusMessageE.setText(getResources().getString(
							R.string.manualSyncSuccessUploadData));
				}
				else if(type == GlobalConstants.MANUAL_DOWNLOAD){
					statusMessageE.setText(getResources().getString(
							R.string.manualSyncSuccessDownloadData));

					new Handler().post(new Runnable(){
						@Override
						public void run(){
							Intent myr = new Intent(CTApp.appContext, HomeActivity.class);
							myr.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							CTApp.appContext.startActivity(myr);
						}
					});
				}
			}
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

	@Override
	public void onStop(){
		super.onStop();

		if(downloadStatusReceiver != null){
			getActivity().unregisterReceiver(downloadStatusReceiver);
		}
	}

	@Override
	public void onStart(){
		super.onStart();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			ContextCompat.registerReceiver(getContext(), downloadStatusReceiver,
										   new IntentFilter(GlobalConstants.SERVICE_DOWNLOAD), ContextCompat.RECEIVER_NOT_EXPORTED);
		}
		else{
			getContext().registerReceiver(downloadStatusReceiver,
										  new IntentFilter(GlobalConstants.SERVICE_DOWNLOAD));
		}
	}

	@Override
	public void onResume(){
		super.onResume();
	}

	public void onDestroy(){
		super.onDestroy();
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		logger.debug("Entering Login Fragment on Create");

		setRetainInstance(true);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		logger.debug("Entry On Login Create View");

		//=== THIS FILE SHOULD BE THE DISPLAY FILE
		viewer = inflater.inflate(R.layout.login, container, false);

		downloadOverlay = viewer.findViewById(R.id.downloadOverlay);
		downloadOverlay.setVisibility(View.GONE);

		return viewer;
	}

	private void doMessage(String string){
		serverCheckDialog(true);

		ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();

		if(!connectionState.isConnected){
			serverCheckDialog(false);
			logger.info("**** CONNECTION STATE" + connectionState.descriptiveText + "****");

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

			new LoginFragment.LoadingAsync().execute(GlobalConstants.MANUAL_DOWNLOAD);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedState){
		super.onActivityCreated(savedState);

		//logger.debug("------- Entry Login onActivityCreated");

		initializeVariables();

		TextView tv = getActivity().findViewById(R.id.loginTitle);
		tv.setFocusable(true);
		tv.setFocusableInTouchMode(true);
		tv.requestFocus();

		SharedPreferences prefs = getActivity().getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		Calendar curCal = Calendar.getInstance();
		String timeZ = DateUtil.getUserTimeZone(curCal.getTimeInMillis(), TimeZone.SHORT, Locale.US);
		int timeOffSet = DateUtil.getUserTimeZoneOffset(curCal.getTimeInMillis());
		boolean inDST = DateUtil.isUserTimeZoneInDST(curCal.getTimeInMillis());

		//logger.debug("Is in daylight savings time?"+inDST);

		editor.putString(GlobalConstants.PREF_LOCAL_TIME_ZONE, timeZ);
		boolean isWestOfGMT = timeOffSet < 1;

		if(isWestOfGMT){
			//logger.debug("We are west of GMT");

			editor.putInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, Math.abs(timeOffSet));
		}
		else{
			//logger.debug("We are east of GMT");

			editor.putInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, timeOffSet);
		}

		//logger.debug("------------------------- Current timezone: " + timeZ
		//	+ " Reported OFFSET: " + timeOffSet + " in hours:  "
		//	+ timeOffSet / 1000 / 60 / 60 + " inDST: " + inDST);

		editor.putBoolean(GlobalConstants.PREF_IS_DST, inDST);
		editor.apply();

		mPassword = getActivity().findViewById(R.id.loginPasswordET);

		mSubmitButton = getActivity().findViewById(R.id.btnLogin);
		mSubmitButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				// StartActivity cleanup intent service
				Intent cleanupService = new Intent(getActivity(), CleanupIntentService.class);
				Context ctx = LoginFragment.this.getActivity();
				ctx.startService(cleanupService);

				Vector<String> errVect = new Vector<String>();

				//logger.debug("Submit button was clicked");

				errVect = validateForm();

				if(errVect != null && errVect.size() > 0){
					//logger.debug("Error with Form");

					//=== we have an error with the form and show the
					//=== dialog....
					Iterator<String> it = errVect.iterator();
					String err = "";

					while(it.hasNext()){
						err = err + System.getProperty("line.separator") + it.next();
					}

					errVect.removeAllElements();

					if(!(getActivity().isFinishing())){
						showDialog(getResources().getString(R.string.errorDialogTitle), err);
					}
				}
				else{
					//logger.debug("logger.infong in");

					doLoginByThread();
				}
			}

		});
	}

	private void initializeVariables(){
		SharedPreferences prefs = getActivity().getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
		String lang = DBHelper.getInstance().fetchItemValueByItemName_Common(GlobalConstants.PREF_LANGUAGE);

		//logger.debug("language is: " + lang);

		if(lang == null){
			lang = GlobalConstants.ENGLISH_LANGUAGE;
		}

		Locale locale = new Locale(lang);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		getActivity().getBaseContext().getResources().updateConfiguration(config, metrics);
		mUserNameStr = prefs.getString(GlobalConstants.PREF_DEVICE_ID, null);
	}

	private void showDialog(String titleStr, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog, getActivity().findViewById(R.id.layout_toproot));

		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText(titleStr);
		TextView text = layout.findViewById(R.id.text);
		text.setText(message);
		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.erroricon);

		builder.setView(layout);
		AlertDialog errorDialog = builder.create();

		errorDialog.setButton(getResources().getString(R.string.dialogClose),
							  new DialogInterface.OnClickListener(){
								  public void onClick(DialogInterface dialog, int id){
									  dialog.cancel();
								  }
							  });

		errorDialog.show();
	}

	private void doLoginByThread(){
		try{
			//logger.debug("Attempting to login");

			//logger.debug("Comparing Saved: " + getResources().getString(R.string.loginPasswordValue) +"  with Entered: " +mEnteredPasswordStr);

			if(getResources().getString(R.string.loginPasswordValue).equalsIgnoreCase(mEnteredPasswordStr)){ // they've succesfully logged in
				handler.sendEmptyMessage(1);
			}
			else{
				handler.sendEmptyMessage(-1);
			}
		}
		catch(Exception e){
			logger.debug("Exception " + e);
			handler.sendEmptyMessage(-2);
		}
	}

	/**
	 * Validates all fields in register form
	 * @return
	 */
	private Vector<String> validateForm(){
		Vector<String> vect = new Vector<String>();
		mEnteredPasswordStr = mPassword.getText().toString();

		//logger.debug("Validating the form:   Password: " + mEnteredPasswordStr);

		boolean isValid = false;

		try{
			isValid = FormValidation.requiredField(mEnteredPasswordStr,
												   GlobalConstants.MIN_PASSWORD_LENGTH);

			if(!isValid){
				vect.add(getResources().getString(R.string.loginErrorInvalidPassword));
			}
			else{
				isValid = FormValidation.validateStringByRule(mEnteredPasswordStr,
															  GlobalConstants.PASSWORD_VALIDATIONS);

				if(!isValid){
					vect.add(getResources().getString(R.string.loginErrorInvalidPassword));
				}
			}
		}
		catch(Exception e){
			vect.add(getResources().getString(R.string.loginErrorInvalidPassword));
		}

		return vect;
	}

	private void serverCheckDialog(boolean isOpening){
		if(isOpening){ // true is opening dialog
			checkProgressDialog = ProgressDialog.show(getActivity(),
													  getResources().getString(R.string.manualSyncPleaseWait), getResources().getString(R.string.manualSyncCheckingConnection));
		}
		else{ // false is closing dialog
			if(checkProgressDialog.isShowing() && checkProgressDialog != null){
				checkProgressDialog.dismiss();
			}
		}
	}

	private void setLoadingMessage(final int direction){
		getActivity().runOnUiThread(new Runnable(){

			public void run(){
				downloadOverlay.setVisibility(View.VISIBLE);
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

	/**
	 * Checks for server connection and then uploads or downloads data if
	 * connected
	 */
	private class LoadingAsync extends AsyncTask<Integer, Void, Integer>{
		@Override
		protected Integer doInBackground(Integer... type){
			//=== UPLOADS NOT DONE IN LOGIN FRAGMENT
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