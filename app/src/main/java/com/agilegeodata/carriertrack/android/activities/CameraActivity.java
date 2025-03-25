package com.agilegeodata.carriertrack.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
/*
Deprecated
Use android. view. WindowManager. LayoutParams. FLAG_DISMISS_KEYGUARD
and/ or android. view. WindowManager. LayoutParams. FLAG_SHOW_WHEN_LOCKED instead;
this allows you to seamlessly hide the keyguard as your application moves in and out
 of the foreground and does not require that any special permissions be requested.
*/
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
/*
We recommend using the new android. hardware. camera2 API
*/
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.os.Bundle;
/*Deprecated
Implicitly choosing a Looper during Handler construction can lead to bugs
where operations are silently lost (if the Handler is not expecting new tasks and quits),
 crashes (if a handler is sometimes created on a thread without a Looper active),
 or race conditions, where the thread a handler is associated with is not what the author anticipated.
 Instead, use an java. util. concurrent. Executor or specify the Looper explicitly, using Looper. getMainLooper,
*/
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.adapters.ListItemRouteDetailsAdapterCommon;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends Activity implements Callback{
	private static final String TAG = CameraActivity.class.getSimpleName();
	private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
	private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
	private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
	private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	public static int degrees_rotation;
	private static byte pictureAvailableCountdown = 0;
	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	FrameLayout frameLayout;
	boolean previewing = false;
	private OrientationEventListener mOrientationEventListener;
	private int mOrientation = -1;
	private boolean safeToTakePicture = false;
	private int mJobDetailId;
	private long mDeliveryId;
	private int mRecordId;
	private double mLat;
	private double mLong;
	private String mAddress;
	PictureCallback myPictureCallback_JPG = new PictureCallback(){
		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1){
			SharedPreferences prefs = getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
			String dd = prefs.getString(FileUtils.getAppDirectoryForSavedFiles(), "");
			String fileName = String.format(dd + GlobalConstants.TEMP_PICTURE_FILE, System.currentTimeMillis());
			String signatureFileName = String.format(dd + GlobalConstants.TEMP_SIGNATURE_FILE, System.currentTimeMillis());

			try{
				FileOutputStream outStream = new FileOutputStream(fileName);
				Bitmap compressBitmap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);

				float ratio = 0;
				Bitmap scaledBM;

				ratio = (float) compressBitmap.getHeight() / (float) compressBitmap.getWidth();
				scaledBM = Bitmap.createScaledBitmap(compressBitmap, 1000,
													 (int) (1000f * ratio), false);

				switch(mOrientation){
					case ORIENTATION_PORTRAIT_NORMAL:
						scaledBM = RotateBitmap(scaledBM, 90);
						break;
					case ORIENTATION_LANDSCAPE_NORMAL:
						scaledBM = RotateBitmap(scaledBM, 180);
						break;
					case ORIENTATION_PORTRAIT_INVERTED:
						scaledBM = RotateBitmap(scaledBM, 270);
						break;
					case ORIENTATION_LANDSCAPE_INVERTED:
						scaledBM = RotateBitmap(scaledBM, 0);
						break;
				}

				scaledBM.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
				outStream.flush();
				outStream.close();
				Intent intent = new Intent(CameraActivity.this, PictureConfirmActivity.class);

				logger.debug("About to launch sub-activity");

				intent.putExtra(GlobalConstants.EXTRA_CURRENT_ADDRESS, mAddress);
				intent.putExtra(GlobalConstants.EXTRA_FILENAME, fileName);
				intent.putExtra(GlobalConstants.EXTRA_SIGNATUREFILENAME, signatureFileName);
				intent.putExtra(GlobalConstants.EXTRA_JOBDETAILID, mJobDetailId);
				intent.putExtra(GlobalConstants.EXTRA_DELIVERYID, mDeliveryId);
				intent.putExtra(GlobalConstants.EXTRA_JOBRECORDID, mRecordId);
				logger.debug("record id in extras to confirm picture activity = " + mRecordId);
				intent.putExtra(GlobalConstants.EXTRA_LATITUDE, mLat);
				intent.putExtra(GlobalConstants.EXTRA_LONGITUDE, mLong);

				startActivityForResult(intent, 1);
				scaledBM.recycle();
				compressBitmap.recycle();
			}
			catch(Exception e){
				if(!CameraActivity.this.isFinishing()){
					//show dialog
					showDialog(getResources().getString(R.string.errorDialogTitle), getResources().getString(R.string.errorUnableToSavePicture));
				}
				logger.error("EXCEPTION", e);

				//finished saving picture
				safeToTakePicture = true;
			}

			//finished saving picture
			safeToTakePicture = true;
		}
	};
	private ImageButton mTakePictureBtn;

	private ImageButton mCancelBtn;
	private final View.OnClickListener onButtonClick = new View.OnClickListener(){

		public void onClick(View v){
			if(previewing){
				if(pictureAvailableCountdown == 0){
					toggleButtonVisibility(false);

					switch(v.getId()){
						case R.id.takepicture:{
							if(safeToTakePicture){
								//===CRASHLYTICS BUG REPORTED HERE
								camera.takePicture(null, null, myPictureCallback_JPG);
								safeToTakePicture = false;
							}

							break;
						}
						case R.id.cancelButton:{
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
							setResult(GlobalConstants.RESULT_CANCEL_PICTURE, null);
							finish();

							ListItemRouteDetailsAdapterCommon.CAMERA_BUTTON_TIMER = 8;
							ListItemRouteDetailsAdapterCommon.pauseCameraButton();
						}
					}
				}
				else{
					pictureAvailableCountdown--;
					if(pictureAvailableCountdown < 0){
						pictureAvailableCountdown = 0;
					}
				}
			}
		}
	};

	public static Bitmap RotateBitmap(Bitmap source, float angle){
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);

		return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
								   source.getHeight(), matrix, true);
	}

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		logger.debug("CAMERA ACTIVITY ONCREATE");

		pictureAvailableCountdown = 5;
		pauseCameraButton();

		Bundle extras = getIntent().getExtras();

		mAddress = savedInstanceState != null ? savedInstanceState.getString(GlobalConstants.EXTRA_CURRENT_ADDRESS) : null; // required

		if(mAddress == null){
			mAddress = extras != null ? extras.getString(GlobalConstants.EXTRA_CURRENT_ADDRESS) : null;
		}

		mJobDetailId = savedInstanceState != null ? savedInstanceState.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0; // required

		if(mJobDetailId == 0){
			mJobDetailId = extras != null ? extras.getInt(GlobalConstants.EXTRA_JOBDETAILID) : 0;
		}

		mDeliveryId = savedInstanceState != null ? savedInstanceState.getLong(GlobalConstants.EXTRA_DELIVERYID) : 0; // required

		if(mDeliveryId == 0){
			mDeliveryId = extras != null ? extras.getLong(GlobalConstants.EXTRA_DELIVERYID) : 0;
		}

		mRecordId = savedInstanceState != null ? savedInstanceState.getInt(GlobalConstants.EXTRA_JOBRECORDID) : 0; // required
		logger.debug("record id from saved instance state = " + mRecordId);

		if(mRecordId == 0){
			mRecordId = extras != null ? extras.getInt(GlobalConstants.EXTRA_JOBRECORDID) : 0;
			logger.debug("record id from extras = " + mRecordId);
		}

		mLat = savedInstanceState != null ? savedInstanceState.getDouble(GlobalConstants.EXTRA_LATITUDE) : 0d; // required

		if(mLat == 0){
			mLat = extras != null ? extras.getDouble(GlobalConstants.EXTRA_LATITUDE) : 0d;
		}
		logger.debug("CAMERA LATITUDE = " + mLat);

		mLong = savedInstanceState != null ? savedInstanceState.getDouble(GlobalConstants.EXTRA_LONGITUDE) : 0d; // required

		if(mLong == 0){
			mLong = extras != null ? extras.getDouble(GlobalConstants.EXTRA_LONGITUDE) : 0d;
		}
		logger.debug("CAMERA LONGITUDE = " + mLong);

		setContentView(R.layout.cameradisplay);

		setupPictureSurface();

		// Setup UI
		frameLayout = findViewById(R.id.camera_preview);

		mTakePictureBtn = findViewById(R.id.takepicture);
		mTakePictureBtn.setOnClickListener(onButtonClick);

		mCancelBtn = findViewById(R.id.cancelButton);
		mCancelBtn.setOnClickListener(onButtonClick);
		toggleButtonVisibility(true);

		setUpListener();
	}

	private void toggleButtonVisibility(boolean makeVisible){
		if(makeVisible){
			mTakePictureBtn.setVisibility(View.VISIBLE);
			mTakePictureBtn.setClickable(true);
			mCancelBtn.setVisibility(View.VISIBLE);
			mCancelBtn.setClickable(true);
		}
		else{
			mTakePictureBtn.setVisibility(View.INVISIBLE);
			mTakePictureBtn.setClickable(false);
			mCancelBtn.setVisibility(View.INVISIBLE);
			mCancelBtn.setClickable(false);
		}
	}

	private void setupPictureSurface(){
		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = findViewById(R.id.camera_surface_previewa);

		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void showDialog(String titleStr, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog, findViewById(R.id.layout_toproot));

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		logger.debug("CAMERA ACTIVITY ONACTIVITYRESULT");

		toggleButtonVisibility(true);

		int recordId = -1;
		if(data != null){
			recordId = data.getIntExtra(GlobalConstants.EXTRA_JOBRECORDID, -1);
		}
		logger.debug("record id from activity result = " + recordId);

		switch(resultCode){
			case GlobalConstants.RESULT_CANCEL_PICTURE:
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				setResult(GlobalConstants.RESULT_CANCEL_PICTURE, data);

				finish();
				break;
			case GlobalConstants.RESULT_REJECT_PICTURE:
				// do nothing they want to retake the picture
				setResult(GlobalConstants.RESULT_REJECT_PICTURE, data);
				break;
			case GlobalConstants.RESULT_CONFIRM_PICTURE:
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				setResult(GlobalConstants.RESULT_CONFIRM_PICTURE, data);
				finish();
				break;
		}

		//finished saving picture
		safeToTakePicture = true;
	}

	@Override
	public void onAttachedToWindow(){
		super.onAttachedToWindow();

		logger.debug("CAMERA ACTIVITY ONATTACHTOWINDOW");

		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
		lock.disableKeyguard();
	}

	private void pauseCameraButton(){
		if(pictureAvailableCountdown != 0){
			(new Handler()).postDelayed(new Runnable(){
				public void run(){
					pictureAvailableCountdown = 0;
				}
			}, 1200);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
		if(previewing){
			camera.stopPreview();
			previewing = false;
		}

		setPreviewing();
		safeToTakePicture = true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder){
		openCameraPreview();
	}

	private void openCameraPreview(){
		if(camera == null){
			try{
				camera = Camera.open();
			}
			catch(Exception e){
				e.printStackTrace();
				try{
					camera = Camera.open();
				}
				catch(Exception ee){
					ee.printStackTrace();
				}
			}
		}

		//==========CRASHLYTICS BUG REPORTED HERE
		Camera.Parameters myCameraParameters = camera.getParameters();

		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
			myCameraParameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
		}
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)){
			myCameraParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		}

		List<Size> supportedSizes = camera.getParameters().getSupportedPictureSizes();

		int height = 0;
		int width = 0;

		int largestSmallDimension = 0;
		for(int i = 0; i < supportedSizes.size(); i++){
			Size s = supportedSizes.get(i);
			int smallestDimension = 0;

			if(s.height == s.width){
				if(s.height > largestSmallDimension){
					largestSmallDimension = s.height;
				}
			}
		}

		height = largestSmallDimension;
		width = largestSmallDimension;

		if(height > 0){
			myCameraParameters.setPictureFormat(ImageFormat.JPEG);
			myCameraParameters.setPictureSize(width, height);
		}

		//=== ASSUME PORTRAIT MODE ???
		degrees_rotation = 90;

		myCameraParameters.setRotation(degrees_rotation);

		camera.setDisplayOrientation(degrees_rotation);
		camera.setParameters(myCameraParameters);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder){
		if(camera != null){
			camera.stopPreview();
			camera.lock();
			camera.release();
			camera = null;
			previewing = false;
		}
	}

	/*
	 * Sets actual preview in the right direction and returns the right
	 * orientation for the actual camera
	 */
	public void closeCamera(){
		if(camera != null){
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.lock();
			camera.release();
			camera = null;
			previewing = false;
		}
	}

	private void setPreviewing(){
		if(camera != null){
			try{
				Camera.Parameters parameters = camera.getParameters();
				List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
				Camera.Size previewSize = previewSizes.get(0);

				parameters.setPreviewSize(previewSize.width, previewSize.height);
				camera.setParameters(parameters);
				pictureAvailableCountdown = 5;
				pauseCameraButton();

				camera.setPreviewDisplay(surfaceHolder);
				camera.startPreview();
				previewing = true;
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDestroy(){
		// TODO Auto-generated method stub
		closeCamera();
		super.onDestroy();

		//===CRASHLYTICS FIX 3DEC18
		mOrientationEventListener.disable();
		mOrientationEventListener = null;
	}

	@Override
	protected void onPause(){
		super.onPause();
	}

	@Override
	protected void onRestart(){
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume(){
		super.onResume();
	}

	private void setUpListener(){
		if(mOrientationEventListener == null){
			mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL){
				@Override
				public void onOrientationChanged(int orientation){
					// determine our orientation based on sensor response
					int lastOrientation = mOrientation;

					if(orientation >= 315 || orientation < 45){
						if(mOrientation != ORIENTATION_PORTRAIT_NORMAL){
							mOrientation = ORIENTATION_PORTRAIT_NORMAL;
						}
					}
					else if(orientation < 315 && orientation >= 225){
						if(mOrientation != ORIENTATION_LANDSCAPE_INVERTED){
							mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
						}
					}
					else if(orientation < 225 && orientation >= 135){
						if(mOrientation != ORIENTATION_PORTRAIT_INVERTED){
							mOrientation = ORIENTATION_PORTRAIT_INVERTED;
						}
					}
					else if(orientation < 135 && orientation > 45){
						if(mOrientation != ORIENTATION_LANDSCAPE_NORMAL){
							mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
						}
					}

					if(lastOrientation != mOrientation){
						changeRotationForButtons(mOrientation, lastOrientation);
					}
				}
			};
		}

		if(mOrientationEventListener.canDetectOrientation()){
			mOrientationEventListener.enable();
		}
	}

	private void changeRotationForButtons(int orientation, int lastOrientation){
		switch(orientation){
			case ORIENTATION_PORTRAIT_NORMAL:
				mTakePictureBtn.setImageResource(R.drawable.shutter_button);

				break;
			case ORIENTATION_LANDSCAPE_NORMAL:
				mTakePictureBtn.setImageDrawable(getRotatedImage(
						R.drawable.shutter_button, 270));

				break;
			case ORIENTATION_PORTRAIT_INVERTED:
				mTakePictureBtn.setImageDrawable(getRotatedImage(
						R.drawable.shutter_button, 180));

				break;
			case ORIENTATION_LANDSCAPE_INVERTED:
				mTakePictureBtn.setImageDrawable(getRotatedImage(R.drawable.shutter_button, 90));

				break;
		}
	}

	private Drawable getRotatedImage(int drawableId, int degrees){
		Bitmap original = BitmapFactory.decodeResource(getResources(),
													   drawableId);
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);

		Bitmap rotated = Bitmap.createBitmap(original, 0, 0,
											 original.getWidth(), original.getHeight(), matrix, true);

		return new BitmapDrawable(rotated);
	}

	@Override
	protected void onStart(){
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop(){
		// TODO Auto-generated method stub
		super.onStop();
	}
}