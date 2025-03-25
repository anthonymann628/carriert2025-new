package com.agilegeodata.carriertrack.android.fragments;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
/*
Deprecated
Use the Jetpack Fragment Library androidx. fragment. app. Fragment
*/
import android.app.Fragment;
/*
Deprecated
ProgressDialog is a modal dialog, which prevents the user from
interacting with the app. Instead of using this class,
you should use a progress indicator like ProgressBar
*/
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.ChangeSettingsContainer;
import com.agilegeodata.carriertrack.android.activities.StartActivity;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.listeners.DeviceLocationListener;
import com.agilegeodata.carriertrack.android.services.ConnectionStatus;
import com.agilegeodata.carriertrack.android.services.RunningServices;
import com.agilegeodata.carriertrack.android.utils.FormValidation;
import com.agilegeodata.carriertrack.android.utils.Utils;

//=== THIS CLASS DEPRECATED
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/*
 * Change settings screen
 */
public class ChangeSettingsFragment extends Fragment{
	protected static final String TAG = ChangeSettingsFragment.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static ProgressDialog checkProgressDialog;
	private static boolean gpsRadioButtonIsChecked = false;
	private final Handler postSettingsHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case 1:
					try{
						Utils.showAlertMessage(CTApp.getCustomAppContext(), getResources().getString(R.string.tabletRequestSent));
					}
					catch(Exception e){
					}
					break;
				default:
					try{
						Utils.showAlertMessage(CTApp.getCustomAppContext(), getResources().getString(R.string.tabletRequestSentError));
					}
					catch(Exception e){
					}
					break;
			}
		}

	};
	private RadioButton rE;
	private RadioButton rS;
	private int mRadioEnglishId;
	private int mRadioSpanishId;
	private Button mSaveLanguageButton;
	private RadioGroup mRadioGroupLanguage;
	private Button mSaveUseMapsButton;
	private int mRadioUseMapsTrueId;
	private int mRadioUseMapsFalseId;
	private RadioGroup mRadioGroupUseMaps;
	private RadioButton umT;
	private RadioButton umF;
	private Button mSaveUseSpeechButton;
	private int mRadioUseSpeechTrueId;
	private int mRadioUseSpeechFalseId;
	private RadioGroup mRadioGroupUseSpeech;
	private RadioButton usT;
	private RadioButton usF;
	private TextView mTopNavLine1;
	private TextView mTopNavLine2;
	private AppCompatRadioButton mGpsStatusRadioButton;
	private View viewer;
	private boolean mIsProcessing;
	private final Handler gpsReceiverHandler = new Handler(){

		public void handleMessage(Message msg){
			mIsProcessing = false;
			updateTopNav();
		}

	};
	private BroadcastReceiver gpsReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent){
			if(!mIsProcessing){
				mIsProcessing = true;

				gpsReceiverHandler.sendEmptyMessage(0);
			}
		}
	};
	private Button mRegisterTabletButton;
	private String mUserNameStr;
	private String mDeviceDescStr;
	private EditText mDeviceDescET;
	private EditText mPassword;
	private Button mAdminSubmitButton;
	private String mEnteredPasswordStr;
	private String tempAdminMode;
	//	private boolean mInAdminMode;
	private String mVCode;
	private Button mAdminLogoutButton;
	private final Handler handlerAdminPass = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case 1:
					resetAdminLayout("ctseq");
					CTApp.operationsMode = GlobalConstants.OPERATIONS_MODE.SEQUENCING;
					break;
				case 2:
					CTApp.operationsMode = GlobalConstants.OPERATIONS_MODE.RENUMBERING;
					resetAdminLayout("ctrenum");
					break;
				case 3:
					CTApp.operationsMode = GlobalConstants.OPERATIONS_MODE.DELIVERING;
					resetAdminLayout(null);    // BACK TO DEFAULT OF DELIVERING
					if(!(getActivity().isFinishing())){
						showDialog(getResources().getString(R.string.successDialogTitle),
								   "Special control set.");
					}

					mPassword.setText("");
					break;
				default:
					mPassword.setText("");

					if(!(getActivity().isFinishing())){
						showDialog(
								getResources().getString(R.string.errorDialogTitle),
								getResources().getString(R.string.loginErrorInvalidPasswordMatch));
					}

					break;
			}
		}

	};
	private Button mAdminLogoutButtonUser;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//logger.debug(TAG, "Entering  Fragment on Create");
		setRetainInstance(true);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			ContextCompat.registerReceiver(getContext(), gpsReceiver, new IntentFilter(GlobalConstants.SERVICE_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED);
		}
		else{
			getContext().registerReceiver(gpsReceiver, new IntentFilter(GlobalConstants.SERVICE_LOCATION));
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){

		viewer = inflater.inflate(R.layout.changesettings, container, false);

		return viewer;
	}

	@Override
	public void onActivityCreated(Bundle savedState){
		super.onActivityCreated(savedState);

		try{
			PackageInfo manager = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			mVCode = manager.versionName;
		}
		catch(NameNotFoundException e){
			e.printStackTrace();
		}

		DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN, ChangeSettingsContainer.class.getName());

		updateTopNav();

		mDeviceDescET = getActivity().findViewById(R.id.deviceDescET);

		mRadioGroupLanguage = getActivity().findViewById(R.id.changeSettingsLanguageRG);
		rE = getActivity().findViewById(R.id.changeSettingsOptionEnglish);
		mRadioEnglishId = rE.getId(); // set the id for later...
		rS = getActivity().findViewById(R.id.changeSettingsOptionSpanish);
		mRadioSpanishId = rS.getId(); // set the id for later...

		mRadioGroupUseMaps = getActivity().findViewById(R.id.useMapsRadioGroup);
		umT = getActivity().findViewById(R.id.useMapsTrueRadiobutton);
		mRadioUseMapsTrueId = umT.getId(); // set the id for later...
		umF = getActivity().findViewById(R.id.useMapsFalseRadiobutton);
		mRadioUseMapsFalseId = umF.getId(); // set the id for later...

		mSaveUseMapsButton = getActivity().findViewById(R.id.btnSaveUseMaps);
		mSaveUseMapsButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//	logger.debug("Save button was clicked");

				try{
					DBHelper.getInstance().setUseMaps_Common(mRadioUseMapsFalseId != mRadioGroupUseMaps.getCheckedRadioButtonId());
				}
				catch(Exception e){
					logger.error("EXCEPTION", e);
				}
			}

		});

		mRadioGroupUseSpeech = getActivity().findViewById(R.id.useSpeechRadioGroup);
		usT = getActivity().findViewById(R.id.useSpeechTrueRadiobutton);
		mRadioUseSpeechTrueId = usT.getId(); // set the id for later...
		usF = getActivity().findViewById(R.id.useSpeechFalseRadiobutton);
		mRadioUseSpeechFalseId = usF.getId(); // set the id for later...

		mSaveUseSpeechButton = getActivity().findViewById(R.id.btnSaveUseSpeech);
		mSaveUseSpeechButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//	logger.debug("Save button was clicked");

				try{
					DBHelper.getInstance().setUseSpeech_Common(mRadioUseSpeechFalseId != mRadioGroupUseSpeech.getCheckedRadioButtonId());
				}
				catch(Exception e){
					logger.error("EXCEPTION", e);
				}
			}

		});

		loadData();

		mPassword = getActivity().findViewById(R.id.loginPasswordET);

		//=== SENDS US BACK TO THE HOME SCREEN IF PUSHED
		LinearLayout homell = getActivity().findViewById(R.id.bottombar);
		homell.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v){
				getActivity().onBackPressed();
			}

		});

		mSaveLanguageButton = getActivity().findViewById(R.id.btnSave);
		mSaveLanguageButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//	logger.debug("Save button was clicked");

				try{
					if(mRadioEnglishId == mRadioGroupLanguage.getCheckedRadioButtonId()){
						DBHelper.getInstance().createItemValueRecord_Common(
								GlobalConstants.PREF_LANGUAGE,
								GlobalConstants.ENGLISH_LANGUAGE);

						logger.debug("Setting preference to english");

						Locale myLocale = new Locale(GlobalConstants.SPANISH_LANGUAGE);
						Resources res = getResources();
						DisplayMetrics dm = res.getDisplayMetrics();
						Configuration conf = res.getConfiguration();
						conf.locale = myLocale;
						res.updateConfiguration(conf, dm);
					}
					else{
						logger.debug("Setting preference to spanish");

						DBHelper.getInstance().createItemValueRecord_Common(
								GlobalConstants.PREF_LANGUAGE,
								GlobalConstants.SPANISH_LANGUAGE);

						Locale myLocale = new Locale(GlobalConstants.SPANISH_LANGUAGE);
						Resources res = getResources();
						DisplayMetrics dm = res.getDisplayMetrics();
						Configuration conf = res.getConfiguration();
						conf.locale = myLocale;
						res.updateConfiguration(conf, dm);
					}

					Intent nextIntent = new Intent(CTApp.getCustomAppContext(), StartActivity.class);
					triggerRebirth(CTApp.getCustomAppContext(), nextIntent);
				}
				catch(Exception e){
					logger.error("EXCEPTION", e);

					if(!(getActivity().isFinishing())){
						showDialog(
								getResources().getString(R.string.errorDialogTitle),
								getResources().getString(R.string.changeSettingsLanguageSavedError));
					}
				}
			}

		});

		mRegisterTabletButton = getActivity().findViewById(R.id.btnRegisterTablet);

		mRegisterTabletButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				mSaveLanguageButton.setVisibility(View.INVISIBLE);
				mRegisterTabletButton.setVisibility(View.INVISIBLE);
				mAdminLogoutButton.setVisibility(View.INVISIBLE);
				serverCheckDialog(true);

				ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();

				if(!connectionState.isConnected){
					logger.info("****" + connectionState.descriptiveText + "****");
				}
				else{
					Message m = new Message();
					m.what = 1;
					doSubmit(m);
				}
			}

		});

		mAdminLogoutButton = getActivity().findViewById(R.id.btnLogout);

		logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());
		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING) ||
		   CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
			TextView title1 = getActivity().findViewById(R.id.rowAdminTV);
			title1.setText(getResources().getString(R.string.removeFromAdminMode));
			mPassword.setVisibility(View.GONE);
			mAdminLogoutButton.setVisibility(View.VISIBLE);
		}
		else{
			mAdminLogoutButton.setVisibility(View.GONE);
		}

		mAdminLogoutButton.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v){
				logger.debug("Admin logout clicked....");

				showLogOutDialog(
						getResources().getString(R.string.changeSettingsLogout),
						getResources().getString(R.string.changeSettingsAreYouSure), false);
			}

		});

		mAdminSubmitButton = getActivity().findViewById(R.id.btnLogin);
		mAdminSubmitButton.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v){
				Vector<String> errVect = new Vector<String>();

				logger.debug("Submit button was clicked: " + CTApp.operationsMode);

				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING) ||
				   CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
					logger.debug("Saving Admin preference to false....");

					DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.PREF_ADMIN_MODE, "");
					CTApp.operationsMode = GlobalConstants.OPERATIONS_MODE.DELIVERING;
					logger.debug("--->operationsMode = " + CTApp.operationsMode);
					mPassword.setVisibility(View.VISIBLE);
					mAdminLogoutButton.setVisibility(View.GONE);
					TextView title = getActivity().findViewById(R.id.rowAdminTV);
					title.setText(getResources().getString(R.string.enterAdminPassword));
				}
				else{
					errVect = validateLoginForm();

					if(errVect != null && errVect.size() > 0){
						logger.debug("Error with Form");

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
						logger.debug("logger.infong in");

						doLoginByThread();
					}
				}
			}

		});

		mAdminLogoutButtonUser = getActivity().findViewById(R.id.btnLogoutUser);

		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING) ||
		   CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
			if(tempAdminMode != null && tempAdminMode.contains("user")){
				mAdminLogoutButtonUser.setVisibility(View.VISIBLE);

			}
			else{
				mAdminLogoutButtonUser.setVisibility(View.GONE);
			}
		}

		mAdminLogoutButtonUser.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				showLogOutDialog(
						getResources().getString(R.string.changeSettingsLogout),
						getResources().getString(R.string.ctChangeSettingsAreYouSure), true);
			}
		});
	}

	/**
	 * Call to restart the application process using the specified intents.
	 * <p>
	 * Behavior of the current process after invoking this method is undefined.
	 */
	public void triggerRebirth(Context context, Intent... nextIntents){
		Intent intent = new Intent(context, StartActivity.class);
		intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // In case we are called with non-Activity context.
		context.startActivity(intent);

		if(context instanceof Activity){
			((Activity) context).finishAffinity();
		}
		logger.debug("**** ChangeSettingsFragment.triggerRebirth KILLING SERVICES");
		RunningServices.killAllServicesIfRunning(this.getActivity());
		Runtime.getRuntime().exit(0); // Kill kill kill!
	}

	private void serverCheckDialog(boolean isOpening){
		if(isOpening){ // true is opening dialog
			checkProgressDialog = ProgressDialog.show(getActivity(),
													  getResources().getString(R.string.changeSettingsPleaseWait),
													  getResources().getString(R.string.changeSettingsCheckingConnection));
		}
		else{ // false is closing dialog
			if(checkProgressDialog.isShowing() && checkProgressDialog != null){
				checkProgressDialog.dismiss();
			}
		}
	}

	private void doSubmit(Message msg){
		serverCheckDialog(false);
		mSaveLanguageButton.setVisibility(View.VISIBLE);
		mRegisterTabletButton.setVisibility(View.VISIBLE);
		mAdminLogoutButton.setVisibility(View.VISIBLE);

		if(msg.what != 1){
			Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.errorTextNoServerConnectivity), Toast.LENGTH_LONG).show();

		}
		else{
			Vector<String> errVect = new Vector<String>();

			logger.debug("Submit button was clicked");

			errVect = validateForm();

			if(errVect != null && errVect.size() > 0){
				logger.debug("Error with Form");

				//=== we have an error with the form and show the
				//=== dialog....
				Iterator<String> it = errVect.iterator();
				String err = "";

				while(it.hasNext()){
					err = err + System.getProperty("line.separator")
						  + it.next();
				}

				errVect.removeAllElements();

				if(!(getActivity().isFinishing())){
					showDialog(getResources().getString(R.string.errorDialogTitle), err);
				}
			}
			else{
				doSubmitOnThread();
			}
		}
	}

	private void doLoginByThread(){
		int messageIdForHandler = -1;
		try{
			// they've succesfully logged in
			if("ctseq".equalsIgnoreCase(mEnteredPasswordStr)){
				messageIdForHandler = 1;
			}
			else if("ctrenum".equalsIgnoreCase(mEnteredPasswordStr)){
				messageIdForHandler = 2;
			}
			else if(mEnteredPasswordStr.startsWith("route#")){
				String[] splits = mEnteredPasswordStr.split("#");
				if(splits == null || splits.length != 2){
					logger.debug("BAD ROUTE SIZE RESET");
				}
				else{
					try{
						int navSize = Integer.decode(splits[1]);
						if(navSize > 0 && navSize < 100){
							CTApp.NAVIGATION_ROUTE_SIZE = navSize;
							messageIdForHandler = 3;
						}
						else{
							messageIdForHandler = 1;
						}
					}
					catch(Exception e){
						logger.debug("ERROR TRYING TO DECODE ROUTE SIZE");
					}
				}
			}
		}
		catch(Exception e){
			logger.debug("Exception " + e);
			messageIdForHandler = -2;
		}

		handlerAdminPass.sendEmptyMessage(messageIdForHandler);
	}

	/**
	 * Validates all fields in register form
	 * @return
	 */
	private Vector<String> validateLoginForm(){
		Vector<String> vect = new Vector<String>();
		mEnteredPasswordStr = mPassword.getText().toString();

		logger.debug("Validating the form:   Password: "
					 + mEnteredPasswordStr);

		boolean isValid = false;

		try{
			isValid = FormValidation.requiredField(mEnteredPasswordStr, GlobalConstants.MIN_PASSWORD_LENGTH);

			if(!isValid){
				vect.add(getResources().getString(R.string.loginErrorInvalidPassword));
			}
			else{
				isValid = FormValidation.validateStringByRule(
						mEnteredPasswordStr,
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

	protected void resetAdminLayout(String string){
		try{
			logger.debug("Setting the admin mode to " + string);

			DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.PREF_ADMIN_MODE, string);

			logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());

			mAdminLogoutButton.setVisibility(View.VISIBLE);

			if(!(getActivity().isFinishing())){
				showDialog(getResources().getString(R.string.successDialogTitle),
						   getResources().getString(R.string.adminModeIsSet));
			}

			mPassword.setText("");
			mPassword.setVisibility(View.GONE);
			TextView title = getActivity().findViewById(R.id.rowAdminTV);
			title.setText(getResources().getString(R.string.removeFromAdminMode));
		}
		catch(Exception e){
			logger.debug("Exception caught" + e);
		}
	}

	/**
	 * Validates all fields in register form
	 * @return
	 */
	private Vector<String> validateForm(){
		Vector<String> vect = new Vector<String>();
		mDeviceDescStr = mDeviceDescET.getText().toString();

		//logger.debug("Validating the form:   mDeviceDescStr = " + mDeviceDescStr);

		boolean isValid = false;

		try{
			isValid = FormValidation.requiredField(mDeviceDescStr, 1);

			if(!isValid){
				vect.add(getResources().getString(R.string.errorDescriptionFieldIsRequired));
			}

		}
		catch(Exception e){
			vect.add(getResources().getString(R.string.errorDescriptionFieldIsRequired));
		}

		return vect;
	}

	private void doSubmitOnThread(){
		ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();

		if(!connectionState.isConnected){
			logger.info("****" + connectionState.descriptiveText + "****");
		}
		else{
			Thread mT = new Thread(){

				public void run(){
					try{
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(GlobalConstants.URLPARAM_CTVERSION, mVCode);
						map.put(GlobalConstants.URLPARAM_ANDROIDVERSION, android.os.Build.VERSION.RELEASE);
						map.put(GlobalConstants.URLPARAM_DEVICEID, mUserNameStr);
						map.put(GlobalConstants.URLPARAM_FILETYPE, GlobalConstants.URLPARAM_FILETYPE_PROVISIONDEVICE);
						map.put(GlobalConstants.URLPARAM_DEVICEDESC, mDeviceDescET.getEditableText().toString());

						logger.debug("String is: " + mDeviceDescET.getEditableText().toString());

						HttpClient httpclient = new DefaultHttpClient();

						HttpPost httppost = new HttpPost(GlobalConstants.URL_PROVISION_DEVICE);
						httppost.setHeader("Content-Transfer-Encoding", "8bit");
						httpclient.getParams().setParameter("http.socket.timeout",
															Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
						httppost.getParams().setParameter("http.socket.timeout",
														  Integer.valueOf(GlobalConstants.HTTP_TIMEOUT));
						List<NameValuePair> nvps = Utils.buildURLParamsPost(map);

						httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

						HttpResponse res = httpclient.execute(httppost);

						//===WHAT DOES THIS DO, res IS NEVER USED
						InputStream stream = res.getEntity().getContent();

						logger.debug(TAG, " Before Response");
						String result = Utils.inputStreamToString(stream);
						logger.debug(TAG, result);

						postSettingsHandler.sendEmptyMessage(1);
					}
					catch(Exception e){
						postSettingsHandler.sendEmptyMessage(-1);
					}
				}
			};

			mT.start();
		}
	}

	private void showLogOutDialog(String titleStr, String message, final boolean userExiting){
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

		errorDialog.setButton(getResources().getString(R.string.dialogYes),
							  new DialogInterface.OnClickListener(){
								  public void onClick(DialogInterface dialog, int id){
									  DBHelper.getInstance().deleteFromTable_Common(DBHelper.DB_T_ITEMVALUES,
																					DBHelper.KEY_ITEMNAME, GlobalConstants.DB_TABLE_COLUMN_LAST_SCREEN);
									  DBHelper.getInstance().deleteFromTable_Common(DBHelper.DB_T_ITEMVALUES,
																					DBHelper.KEY_ITEMNAME, GlobalConstants.PREF_LASTTIME_LOGIN);

									  //=== kills app for user admin exit
									  if(userExiting){
										  getActivity().moveTaskToBack(true);
										  getActivity().finish();
									  }
									  else{
										  //=== removes app from admin mode
//										  DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.PREF_ADMIN_MODE, "false");
										  DBHelper.getInstance().createItemValueRecord_Common(GlobalConstants.PREF_ADMIN_MODE, "");
										  CTApp.operationsMode = GlobalConstants.OPERATIONS_MODE.DELIVERING;
										  logger.debug("--->operationsMode = " + CTApp.operationsMode);
										  mAdminLogoutButtonUser.setVisibility(View.GONE);

										  Intent myra = new Intent(getActivity(), StartActivity.class);
										  myra.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);

										  startActivity(myra);
									  }

									  dialog.cancel();
								  }
							  });

		errorDialog.setButton2(getResources().getString(R.string.dialogNo), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				dialog.cancel();
			}
		});

		errorDialog.show();
	}

	private void showDialog(String titleStr, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog,
									   getActivity().findViewById(R.id.layout_toproot));
		TextView title = layout.findViewById(R.id.headerTitle);
		title.setText(titleStr);
		TextView text = layout.findViewById(R.id.text);
		text.setText(message);
		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.erroricon);

		builder.setView(layout);
		AlertDialog errorDialog = builder.create();

		errorDialog.setButton(getResources().getString(R.string.dialogClose), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				dialog.cancel();
			}
		});

		errorDialog.show();
	}

	private void loadData(){
		SharedPreferences prefs = getActivity().getSharedPreferences(
				GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

		mUserNameStr = prefs.getString(GlobalConstants.PREF_DEVICE_ID, null);
		tempAdminMode = DBHelper.getInstance().fetchItemValueByItemName_Common(GlobalConstants.PREF_ADMIN_MODE);

		if(tempAdminMode != null && tempAdminMode.contains("ctseq")){
			CTApp.operationsMode = GlobalConstants.OPERATIONS_MODE.SEQUENCING;
		}
		else if(tempAdminMode != null && tempAdminMode.contains("ctrenum")){
			CTApp.operationsMode = GlobalConstants.OPERATIONS_MODE.RENUMBERING;
		}
		else{
			CTApp.operationsMode = GlobalConstants.OPERATIONS_MODE.DELIVERING;
		}

		String languagePref = DBHelper.getInstance().fetchItemValueByItemName_Common(GlobalConstants.PREF_LANGUAGE);
		TextView tv = getActivity().findViewById(R.id.tabletIdentifier);
		tv.setText(getResources().getString(R.string.tabletIdentifier, mUserNameStr));

		try{
			if(languagePref == null || languagePref.equals(GlobalConstants.ENGLISH_LANGUAGE)){
				//logger.debug("langpref is english");

				rE.setChecked(true);
				rS.setChecked(false);
			}
			else{
				//logger.debug("langpref is spanish");

				rE.setChecked(false);
				rS.setChecked(true);
			}
		}
		catch(Exception e){
			logger.debug("Exc: " + e);

			rE.setChecked(true);
			rS.setChecked(true);
		}

		boolean useMapsPref = DBHelper.getInstance().getUseMaps_Common();

		try{
			if(useMapsPref){
				umT.setChecked(true);
				umF.setChecked(false);
			}
			else{
				umT.setChecked(false);
				umF.setChecked(true);
			}
		}
		catch(Exception e){
			logger.debug("Exc: " + e);

			umT.setChecked(true);
			umF.setChecked(true);
		}

		boolean useSpeechPref = DBHelper.getInstance().getUseSpeech_Common();

		try{
			if(useSpeechPref){
				usT.setChecked(true);
				usF.setChecked(false);
			}
			else{
				usT.setChecked(false);
				usF.setChecked(true);
			}
		}
		catch(Exception e){
			logger.debug("Exc: " + e);

			umT.setChecked(true);
			umF.setChecked(true);
		}
	}

	@SuppressLint("RestrictedApi")
	private void updateTopNav(){
		if(getActivity() != null){
			mTopNavLine1 = getActivity().findViewById(R.id.topNavLine1);
			mTopNavLine1.setText(getResources().getString(R.string.changeSettingsTitle));

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

	@Override
	public void onStop(){

		super.onStop();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(gpsReceiver != null){
			getActivity().unregisterReceiver(gpsReceiver);
			gpsReceiver = null;
		}
	}

	@Override
	public void onResume(){
		super.onResume();
	}
}