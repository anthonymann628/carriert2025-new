package com.agilegeodata.carriertrack.android.activities;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentActivity;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.fragments.RouteDetailsLeftSideFragment;
import com.agilegeodata.carriertrack.android.fragments.RouteDetailsRightSideFragmentMerged;
import com.agilegeodata.carriertrack.android.herenavigation.MyHereNavigationFragment;
import com.agilegeodata.carriertrack.android.objects.DeliveryItem;
import com.agilegeodata.carriertrack.android.objects.Route;
import com.agilegeodata.carriertrack.android.services.ConnectionStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  Route Details Container
 */
public class RouteDetailsActivity extends FragmentActivity{
	public static final String TAG = RouteDetailsActivity.class.getSimpleName();
	public static final String LEFT_SIDE_FRAGMENT_TAG = "leftSideFragmentTag";
	public static final String RIGHT_SIDE_FRAGMENT_TAG = "rightSideFragmentTag";
	public static final String NAVIGATION_FRAGMENT_TAG = "navigationFragmentTag";
	public static RouteDetailsActivity instance = null;
	protected static Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	public GlobalConstants.NAVIGATION_MODE navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_OFF;
	public String routeType = "";
	public MyHereNavigationFragment navigationFragment = null;
	int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	LinearLayout navContainer = null;
	private RouteDetailsLeftSideFragment leftSideFragment = null;
	private RouteDetailsRightSideFragmentMerged rightSideFragment = null;

	@Override
	public void onConfigurationChanged(Configuration newConfiguration){
		super.onConfigurationChanged(newConfiguration);

		orientation = this.getResources().getConfiguration().orientation;
		logger.debug(">>>>onCreate() : orientation " + (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? "PORTRAIT" : (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "UNKNOWN")));

		if(orientation == Configuration.ORIENTATION_PORTRAIT){
			if(navContainer != null){
				navContainer.setVisibility(View.GONE);
			}

			if(navigationFragment != null){
				getSupportFragmentManager()
						.beginTransaction()
						.remove(navigationFragment)
						.commit();

				navigationFragment.onDestroy();
				navigationFragment = null;
			}

			RouteDetailsRightSideFragmentMerged.getPauseMode();
			RouteDetailsRightSideFragmentMerged.isInPauseMode = RouteDetailsRightSideFragmentMerged.saveIsPaused;
			rightSideFragment.restorePauseButtonState();
			rightSideFragment.pausedChanged();

			navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_SOLO;
		}
		else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
			if(routeType.equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
				if(navContainer != null){
					navContainer.setVisibility(View.VISIBLE);
				}

				navigationFragment = new MyHereNavigationFragment();
				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.fragmentContainerViewNavigation, navigationFragment, NAVIGATION_FRAGMENT_TAG)
						.commit();

				rightSideFragment.savePauseButtonState();
				rightSideFragment.setPageButtonsEnabled(false);

				rightSideFragment.setPauseButtonsEnabled(false);
				rightSideFragment.setPauseButtonsActivated(false);

				RouteDetailsRightSideFragmentMerged.saveIsPaused = RouteDetailsRightSideFragmentMerged.isInPauseMode;
				rightSideFragment.setPauseMode(false);

				navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_SPLIT_SCREEN;
			}
			else if(routeType.equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
				if(navContainer != null){
					navContainer.setVisibility(View.GONE);
				}

				if(navigationFragment != null){
					getSupportFragmentManager()
							.beginTransaction()
							.remove(navigationFragment)
							.commit();
					navigationFragment.onDestroy();
					navigationFragment = null;
				}

				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

				navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_OFF;
			}
			else if(routeType.equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
				if(navContainer != null){
					navContainer.setVisibility(View.GONE);
				}

				if(navigationFragment != null){
					getSupportFragmentManager()
							.beginTransaction()
							.remove(navigationFragment)
							.commit();
					navigationFragment.onDestroy();
					navigationFragment = null;
				}

				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

				navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_OFF;
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		logger.debug(">>>>onCreate() : STARTED : savedInstanceState IS NULL = " + (savedInstanceState == null));

		routeType = (String) this.getIntent().getSerializableExtra(GlobalConstants.EXTRA_ROUTE_TYPE);

		//===ONLY SEQUENCED ROUTES USE MAPBOX NAVIGATION SO DO NOT NEED TO HANDLE SCREEN ROTATION (I.E. SPLIT SCREEN) IF A RANDOM ROUTE
		orientation = this.getResources().getConfiguration().orientation;
		logger.debug(">>>>onCreate() : orientation " + (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? "PORTRAIT" : (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "UNKNOWN")));

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.routedetails_container);

		navContainer = findViewById(R.id.routeDetailsFragmentContainer2);

		//=== saved instance state, fragment may exist
		if(savedInstanceState != null){
			// look up the instance that already exists by tag
			leftSideFragment = (RouteDetailsLeftSideFragment)
					getSupportFragmentManager().findFragmentByTag(LEFT_SIDE_FRAGMENT_TAG);

			rightSideFragment = (RouteDetailsRightSideFragmentMerged)
					getSupportFragmentManager().findFragmentByTag(RIGHT_SIDE_FRAGMENT_TAG);

			navigationFragment = (MyHereNavigationFragment)
					getSupportFragmentManager().findFragmentByTag(NAVIGATION_FRAGMENT_TAG);
		}

		//===WE ALWAYS NEED THE LEFT AND RIGHT SIDE FRAGMENTS
		if(leftSideFragment == null){
			// only create fragment if they haven't been instantiated already
			leftSideFragment = new RouteDetailsLeftSideFragment();
		}

		if(rightSideFragment == null){
			// only create fragment if they haven't been instantiated already
			rightSideFragment = new RouteDetailsRightSideFragmentMerged();
		}

		if(!leftSideFragment.isInLayout()){
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragmentContainerViewLeftSide, leftSideFragment, LEFT_SIDE_FRAGMENT_TAG)
					.commit();
		}

		if(!rightSideFragment.isInLayout()){
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragmentContainerViewRightSide, rightSideFragment, RIGHT_SIDE_FRAGMENT_TAG)
					.commit();
		}

		//===DETERMINE IF WE NEED THE HERE NAVIGATION PANEL

		//===IF RANDOM DELIVERY, RE-NUMBERING OR RE-SEQUENCING REMOVE NAVIGATION FUNCTIONALITY
		if(routeType.equalsIgnoreCase(Route.RouteJobType.RANDOM.name()) || //
			   CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING) ||
				   CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
			if(navContainer != null){
				navContainer.setVisibility(View.GONE);
			}

			if(navigationFragment != null){
				getSupportFragmentManager()
						.beginTransaction()
						.remove(navigationFragment)
						.commit();

				navigationFragment.onDestroy();
				navigationFragment = null;
			}

			navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_OFF;
		}
		//===SEQUENCED DELIVERY OR UNSEQ SEQUENCING OR RENUMBERING
		//=== CHECK FOR LANDSCAPE (I.E. SPLIT SCREEN DISPLAY)
		else{
			if(orientation == Configuration.ORIENTATION_PORTRAIT){
				if(navContainer != null){
					navContainer.setVisibility(View.GONE);
				}
				if(navigationFragment != null){
					getSupportFragmentManager()
							.beginTransaction()
							.remove(navigationFragment)
							.commit();

					navigationFragment.onDestroy();
					navigationFragment = null;
				}

				navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_OFF;
			}
			else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
				if(navContainer != null){
					navContainer.setVisibility(View.VISIBLE);
				}

				navigationFragment = new MyHereNavigationFragment();
				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.fragmentContainerViewNavigation, navigationFragment, NAVIGATION_FRAGMENT_TAG)
						.commit();

				navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_SPLIT_SCREEN;
			}
		}

		//===IF DISPLAYING HERE NAVIGATION WE NEED INTERNET COMMUNICATIONS
		if(navigationFragment != null){
			ConnectionStatus.ConnectionState connectionState = ConnectionStatus.getConnectivityStatus();
			if(!connectionState.isConnected){
				logger.info("****" + connectionState.descriptiveText + "****");

				if(navContainer != null){
					navContainer.setVisibility(View.GONE);
				}
				if(navigationFragment != null){
					getSupportFragmentManager()
							.beginTransaction()
							.remove(navigationFragment)
							.commit();
					navigationFragment.onDestroy();
					navigationFragment = null;

					navigationMode = GlobalConstants.NAVIGATION_MODE.NAVIGATION_OFF;
				}

				final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle("Navigation Unavailable").setMessage("Internet connect is required. Reverting to delivery list mode.")
					  .setPositiveButton(getResources().getString(R.string.dialogOk), new DialogInterface.OnClickListener(){
						  @Override
						  public void onClick(DialogInterface paramDialogInterface, int paramInt){
							  //=== DO NOTHING;
						  }
					  });
				dialog.show();

				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}

		instance = this;
	}

	@Override
	protected void onStart(){
		logger.debug("!!!!!!!!: CALLED");

		super.onStart();
	}

	@Override
	public void onResume(){
		logger.debug("!!!!!!!!: CALLED");

		orientation = this.getResources().getConfiguration().orientation;
		logger.debug(">>>>onCreate() : orientation " + (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? "PORTRAIT" : (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "UNKNOWN")));

		//===PORTRAIT TO LANDSCAPE, SO ADD THE NAV FRAGMENT
		if(orientation == Configuration.ORIENTATION_LANDSCAPE){
			//=== ALWAYS START IN DELIVERY ON MODE
			rightSideFragment.setPauseMode(false);
		}

		super.onResume();
	}

	@Override
	protected void onPause(){
		logger.debug("!!!!!!!!: CALLED");
		super.onPause();
	}

	@Override
	public void onStop(){
		logger.debug("!!!!!!!!: CALLED");
		super.onStop();
	}

	@Override
	public void onDestroy(){
		logger.debug("!!!!!!!!: CALLED");
		instance = null;
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch(requestCode){
			//===CAMERA
			case 9999:
				super.onActivityResult(requestCode, resultCode, data);
				int recordId = -1;
				long deliveryId = -1;
				int detailId = -1;

				if(data != null){
					//===BUG FIX FROM DOUG MAXWELL REPORT
					recordId = data.getIntExtra(GlobalConstants.EXTRA_JOBRECORDID, -1);

					deliveryId = data.getLongExtra(GlobalConstants.EXTRA_DELIVERYID, -1);
					detailId = data.getIntExtra(GlobalConstants.EXTRA_JOBDETAILID, -1);
				}
				//logger.debug("record id from activity result = " + recordId);

				if(recordId > -1){
					switch(resultCode){
						case GlobalConstants.RESULT_CANCEL_PICTURE:
							// do nothing they want to retake the picture
							break;
						case GlobalConstants.RESULT_REJECT_PICTURE:
							// do nothing they want to retake the picture
							break;
						case GlobalConstants.RESULT_CONFIRM_PICTURE:
							DeliveryItem rd = null;

							for(int index = 0; index < rightSideFragment.mDataDisplayList.size(); index++){
								DeliveryItem test = rightSideFragment.mDataDisplayList.get(index);
								if(test.getId() == recordId){
									rd = test;
									break;
								}
							}

							//==SOMETIMES NOT FINDING THE route detail for RECORD ID
							if(rd != null){
								rd.setPhotoTaken(true);

								runOnUiThread(
										new Runnable(){
											@Override
											public void run(){
												rightSideFragment.mRouteDetailListAdapterCommon.notifyDataSetChanged();
											}
										}
											 );
							}
							else{
								logger.error("COULD NOT FIND ROUTE DETAIL FOR recordID " + recordId);
								logger.error("COULD NOT FIND ROUTE DETAIL FOR routeDetailID " + detailId);
								logger.error("COULD NOT FIND ROUTE DETAIL FOR routeDeliveryID " + deliveryId);
							}

							break;
					}
				}

				Intent intentP = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
				intentP.setPackage(getPackageName());//"com.agilegeodata.carriertrack");
				intentP.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, false);
				logger.debug("SENDING PAUSE INTENT FROM route details activity 1");
				this.sendBroadcast(intentP);
				break;
			case 9998:
			default:
				Intent intentP2 = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
				intentP2.setPackage(getPackageName());//"com.agilegeodata.carriertrack");
				intentP2.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, false);
				logger.debug("SENDING PAUSE INTENT FROM route details activity 2");
				this.sendBroadcast(intentP2);
				break;
		}
	}

	@Override
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
		//logger.debug(">>>>onAttachedToWindow() : STARTED");

		try{
			KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
			KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
			lock.disableKeyguard();
		}
		catch(Exception e){
			logger.error("EXCEPTION : ", e);
		}
	}

//	@Override
//	public void onBackPressed(){
//		//super.onBackPressed();
//	}
}