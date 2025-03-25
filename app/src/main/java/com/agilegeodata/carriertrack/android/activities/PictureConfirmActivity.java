package com.agilegeodata.carriertrack.android.activities;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.objects.PhotoDetail;
import com.agilegeodata.carriertrack.android.objects.SignatureDetail;
import com.agilegeodata.carriertrack.android.utils.FileUtils;
import com.github.gcacace.signaturepad.views.SignaturePad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Confirm the picture Container
 */
public class PictureConfirmActivity extends Activity{

	private static final String TAG = PictureConfirmActivity.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	Calendar cal;
	SignaturePad signaturePad;
	CheckBox signatureCheckBox;
	private ImageView mImagePreview = null;
	private byte[] mPhotoData;
	private byte[] mSignatureData;
	private int mJobDetailId;
	private long mDeliveryId;
	private double mLat;
	private double mLong;
	private String mFileName;
	private String mSignatureFileName;
	private EditText mNotes;
	private String mNotesStr = null;
	private String mAddress;
	private ImageButton mAcceptBtn;
	private ImageButton mRejectBtn;
	private ImageButton mCancelBtn;
	private int widthX;
	private int heightY;
	private int mRecordId;

	private final View.OnClickListener onButtonClick = new View.OnClickListener(){
		public void onClick(View v){
			Intent returnIntent = new Intent();

			switch(v.getId()){
				case R.id.acceptButton:{
					try{
						returnIntent.putExtra(GlobalConstants.EXTRA_JOBRECORDID, mRecordId);
						logger.debug("record id in extras for activity result = " + mRecordId);

						if(ActivityCompat.checkSelfPermission(PictureConfirmActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
						   != PackageManager.PERMISSION_GRANTED){
							// Check Permissions Now
							//this was done in HomeFragment so alert user and
							//go back to HomeFragment
							Toast.makeText(CTApp.getCustomAppContext(), getResources().getString(R.string.deviceFeatureAccessLocationNotGranted), Toast.LENGTH_LONG).show();

							setResult(GlobalConstants.RESULT_REJECT_PICTURE, returnIntent);

							finish();

							break;
						}
						else{
							//=== PROCESS A PHOTO
							mPhotoData = FileUtils.readFileToByteArray(mFileName);
							SharedPreferences prefs = getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
							String dd = prefs.getString(FileUtils.getAppDirectoryForSavedFiles(), "");
							int fileSize = mPhotoData.length;
							long time = System.currentTimeMillis();

							String fileName = String.format(dd + time + "_" + fileSize + ".jpg");
							FileOutputStream outStream = new FileOutputStream(fileName);
							outStream.write(mPhotoData);
							outStream.close();

							mNotesStr = mNotes.getText().toString();

							//	logger.debug("mNotesStr: " + mNotesStr);

							Location location = CTApp.getLocation();
							logger.debug("LOCATION LATITUDE = " + location.getLatitude());
							logger.debug("LOCATION LONGITUDE = " + location.getLongitude());

							PhotoDetail p = new PhotoDetail();
							p.setFilePath(fileName);
							p.setDeliveryId(mDeliveryId);
							p.setJobDetailId(mJobDetailId);
							p.setLat(location != null ? location.getLatitude() : 0);
							p.setLon(location != null ? location.getLongitude() : 0);
							String returnStr = System.getProperty("line.separator");

							if(mNotesStr != null && mNotesStr.length() > 0){
								mNotesStr = mNotesStr.replace(returnStr, " ");
								mNotesStr = mNotesStr.replace("\r", " ");
							}

							p.setPhotoNotes(mNotesStr);
							int tZOffSet = prefs.getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);

							long gmtTimestamp = cal.getTimeInMillis() + tZOffSet;
							p.setPhotoDate(gmtTimestamp);

							ContentValues initialValues = p.createIntialValues();

							DBHelper.getInstance().createRecord_Common(initialValues, DBHelper.DB_T_PHOTOS, DBHelper.KEY_ID, false);

							ContentValues iVals = new ContentValues();
							iVals.put(DBHelper.KEY_DELIVERYID, mDeliveryId);
							iVals.put(DBHelper.KEY_PHOTOTAKEN, 1);

							DBHelper.getInstance().createRecord_Common(iVals,
																	   DBHelper.DB_T_ADDRESSDETAILLIST,
																	   DBHelper.KEY_DELIVERYID, true);

							DBHelper.getInstance().updateRouteDetailsListPhotoTaken_Common(mRecordId, 1);

							//=================================ADD SIGNATURE SAVE HERE========================================
							if(signatureCheckBox.isChecked()){
								//===WRITE TO TEMP FILE
								FileOutputStream signatureOutStream = new FileOutputStream(mSignatureFileName);
								Bitmap signatureBitmap = signaturePad.getSignatureBitmap();

								signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 80, signatureOutStream);
								signatureOutStream.flush();
								signatureOutStream.close();

								//===WRITE TO PERM FILE
								mSignatureData = FileUtils.readFileToByteArray(mSignatureFileName);
								SharedPreferences prefs2 = getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
								String dd2 = prefs2.getString(FileUtils.getAppDirectoryForSavedFiles(), "");
								int fileSize2 = mSignatureData.length;
								long time2 = System.currentTimeMillis();

								String fileName2 = String.format(dd2 + time2 + "_" + fileSize2 + ".jpg");
								FileOutputStream outStream2 = new FileOutputStream(fileName2);
								outStream2.write(mSignatureData);
								outStream2.close();

								SignatureDetail signatureDetail = new SignatureDetail();
								signatureDetail.setFilePath(fileName2);
								signatureDetail.setDeliveryId(mDeliveryId);
								signatureDetail.setJobDetailId(mJobDetailId);
								signatureDetail.setLat(location != null ? location.getLatitude() : 0);
								signatureDetail.setLon(location != null ? location.getLongitude() : 0);

								int tZOffSet2 = prefs.getInt(GlobalConstants.PREF_LOCAL_TIME_ZONE_OFFSET, 0);

								long gmtTimestamp2 = cal.getTimeInMillis() + tZOffSet2;
								signatureDetail.setSignatureDate(gmtTimestamp);

								ContentValues initialValues2 = signatureDetail.createIntialValues();

								DBHelper.getInstance().createRecord_Common(initialValues2, DBHelper.DB_T_SIGNATURES, DBHelper.KEY_ID, false);

								ContentValues iVals2 = new ContentValues();
								iVals2.put(DBHelper.KEY_DELIVERYID, mDeliveryId);
								iVals2.put(DBHelper.KEY_SIGNATURETAKEN, 1);

								DBHelper.getInstance().createRecord_Common(iVals2,
																		   DBHelper.DB_T_ADDRESSDETAILLIST,
																		   DBHelper.KEY_DELIVERYID, true);

								DBHelper.getInstance().updateRouteDetailsListSignatureTaken_Common(mRecordId, 1);
							}

							setResult(GlobalConstants.RESULT_CONFIRM_PICTURE, returnIntent);
						}
					}
					catch(Exception e){
						setResult(GlobalConstants.RESULT_CANCEL_PICTURE, returnIntent);
						e.printStackTrace();
						logger.error("EXCEPTION", e);
					}

					finish();

					break;
				}
				case R.id.rejectButton:{
					//logger.info("+++++++++++"+mLat,"+++++++++++"+mLong);
					setResult(GlobalConstants.RESULT_REJECT_PICTURE, returnIntent);

					finish();
					break;
				}
				case R.id.cancelButton:{
					//logger.info("+++++++++++"+mLat,"+++++++++++"+mLong);
					setResult(GlobalConstants.RESULT_CANCEL_PICTURE, returnIntent);
					finish();
				}
			}
		}
	};

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		cal = Calendar.getInstance();
		windowSize();

		mAddress = savedInstanceState != null ? savedInstanceState.getString(GlobalConstants.EXTRA_CURRENT_ADDRESS) : null; // required

		if(mAddress == null){
			mAddress = extras != null ? extras.getString(GlobalConstants.EXTRA_CURRENT_ADDRESS) : null;
		}

		mJobDetailId = savedInstanceState != null ? savedInstanceState.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0; // required

		if(mJobDetailId == 0){
			mJobDetailId = extras != null ? extras.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0;
		}

		mRecordId = savedInstanceState != null ? savedInstanceState.getInt(GlobalConstants.EXTRA_JOBRECORDID) : 0; // required
		logger.debug("record id from saved instance state = " + mRecordId);

		if(mRecordId == 0){
			mRecordId = extras != null ? extras.getInt(GlobalConstants.EXTRA_JOBRECORDID) : 0;
			logger.debug("record id from extras = " + mRecordId);
		}

		mDeliveryId = savedInstanceState != null ? savedInstanceState.getLong(GlobalConstants.EXTRA_DELIVERYID) : 0; // required

		if(mDeliveryId == 0){
			mDeliveryId = extras != null ? extras.getLong(GlobalConstants.EXTRA_DELIVERYID) : 0;
		}

		mLat = savedInstanceState != null ? savedInstanceState.getDouble(GlobalConstants.EXTRA_LATITUDE) : 0d; // required

		if(mLat == 0){
			mLat = extras != null ? extras.getDouble(GlobalConstants.EXTRA_LATITUDE) : 0d;
		}
		logger.debug("PICTURE CONFIRM LATITUDE = " + mLat);

		mLong = savedInstanceState != null ? savedInstanceState.getDouble(GlobalConstants.EXTRA_LONGITUDE) : 0d; // required

		if(mLong == 0){
			mLong = extras != null ? extras.getDouble(GlobalConstants.EXTRA_LONGITUDE) : 0d;
		}
		logger.debug("PICTURE CONFIRM LONGITUDE = " + mLong);

		mFileName = savedInstanceState != null ? savedInstanceState.getString(GlobalConstants.EXTRA_FILENAME) : null;// required

		if(mFileName == null){
			mFileName = extras != null ? extras.getString(GlobalConstants.EXTRA_FILENAME) : null;
		}

		mSignatureFileName = savedInstanceState != null ? savedInstanceState.getString(GlobalConstants.EXTRA_SIGNATUREFILENAME) : null;// required

		if(mSignatureFileName == null){
			mSignatureFileName = extras != null ? extras.getString(GlobalConstants.EXTRA_SIGNATUREFILENAME) : null;
		}

		//logger.info(""+android.os.Build.BRAND.toLowerCase(Locale.ENGLISH),"-----------");

		setContentView(R.layout.pictureconfirm);

		TextView addTv = findViewById(R.id.notesAddress);
		addTv.setText(mAddress);

		mNotes = findViewById(R.id.notesET);
		mImagePreview = findViewById(R.id.imagePreview);

		signaturePad = findViewById(R.id.signaturePad);
		signaturePad.clear();

		signatureCheckBox = findViewById(R.id.signatureCheckBox);

		TextView tv = findViewById(R.id.notesTV);
		tv.setFocusable(true);
		tv.setFocusableInTouchMode(true);
		tv.requestFocus();

		TextView tvImage = findViewById(R.id.setTheDate);
		tvImage.setText(getDateCurrentTimeZone());
		LinearLayout ll = findViewById(R.id.contentContainer);

		// Setup UI
		mAcceptBtn = findViewById(R.id.acceptButton);
		mAcceptBtn.setOnClickListener(onButtonClick);
		mRejectBtn = findViewById(R.id.rejectButton);
		mRejectBtn.setOnClickListener(onButtonClick);
		mCancelBtn = findViewById(R.id.cancelButton);
		mCancelBtn.setOnClickListener(onButtonClick);
		toggleButtonVisibility(true);

		if(mFileName != null){
			if(FileUtils.doesFileExist(mFileName)){
				Bitmap bm = BitmapFactory.decodeFile(mFileName);

				if(bm.getWidth() < bm.getHeight()){
					mImagePreview.setImageBitmap(bm);
					float ratio = (float) bm.getWidth() / (float) bm.getHeight();

					LayoutParams params = new LayoutParams(
							(int) (widthX * ratio),    //width
							widthX);                //height
					mImagePreview.setLayoutParams(params);
				}
				else{
					mImagePreview.setImageBitmap(bm);
					float ratio = (float) bm.getHeight() / (float) bm.getWidth();

					LayoutParams params = new LayoutParams(
							widthX,                        //width
							(int) (widthX * ratio));    //height
					mImagePreview.setLayoutParams(params);
				}

				ll.invalidate();
			}
		}
	}

	public String getDateCurrentTimeZone(){
		try{
			SimpleDateFormat s = new SimpleDateFormat(GlobalConstants.DEFAULT_DATETIME_FORMAT_PICTURE,
													  java.util.Locale.US);
			return s.format(new Date());
		}
		catch(Exception e){
		}

		return "";
	}

	public void windowSize(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		widthX = dm.widthPixels;
		heightY = dm.heightPixels;
	}

	private void toggleButtonVisibility(boolean makeVisible){
		if(makeVisible){
			mRejectBtn.setVisibility(View.VISIBLE);
			mRejectBtn.setClickable(true);
			mAcceptBtn.setVisibility(View.VISIBLE);
			mAcceptBtn.setClickable(true);
			mCancelBtn.setVisibility(View.VISIBLE);
			mCancelBtn.setClickable(true);
		}
		else{
			mRejectBtn.setVisibility(View.INVISIBLE);
			mRejectBtn.setClickable(false);
			mAcceptBtn.setVisibility(View.INVISIBLE);
			mAcceptBtn.setClickable(false);
			mCancelBtn.setVisibility(View.INVISIBLE);
			mCancelBtn.setClickable(false);
		}
	}

	@Override
	public void onAttachedToWindow(){
		super.onAttachedToWindow();

		//	logger.debug("Entry On Attached to window....");

		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
		lock.disableKeyguard();
	}
}