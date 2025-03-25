package com.agilegeodata.carriertrack.android.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.activities.CameraActivity;
import com.agilegeodata.carriertrack.android.activities.ToolsActivity;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.fragments.RouteDetailsRightSideFragmentMerged;
import com.agilegeodata.carriertrack.android.objects.DeliveryItem;
import com.agilegeodata.carriertrack.android.objects.DeliveryItemProduct;
import com.agilegeodata.carriertrack.android.objects.Route;
import com.agilegeodata.carriertrack.android.utils.DateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class ListItemRouteDetailsAdapterCommon extends BaseAdapter{
	public static final String TAG = ListItemRouteDetailsAdapterCommon.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	public static byte CAMERA_BUTTON_TIMER = 0;

	//===COLORS
	static int darkOrangeStartDelivered = 0xFFCC8040;
	static int lightOrangeStartDelivered = 0xFFFF8040;
	static int darkGrayDeliveredOrProcessed = 0xFFA0A0A0;
	static int lightGrayUndeliveredUnprocessed = 0xFFE0E0E0;
	static int redMessageProductsBackground = 0xFFF20000;
	static int lightRedUndeliveredUnprocessed = 0xFFF0B0B0;
	static int darkRedUndeliveredProcessed = 0xFFDC7878;
	static int greenMessageProductsBackground = 0xFF40AA40;
	static int lightGreenUndeliveredOrUnprocessed = 0xFF70F070;
	static int darkGreenUndeliveredOrProcessed = 0xFF14C814;
	static int yellowUnresolvedAddressBackground = 0xFFFFFF80;
	static int darkYellowUnresolvedAddressBackground = 0xFFD6D680;//golden rod color
	static int invalidAddressStartingPointBackground = 0xfff8ce75;
	static int streetSummaryBackground = 0xffddfdff;

	private final Context mContext;
	String mRouteType = null;
	RouteDetailsRightSideFragmentMerged rightSideFragment = null;
	private List<DeliveryItem> mElements;
	private int mTimeZoneOffset; // offset for the time zone from GMT
	private int comment_Top_Margin = -4;

	{
		mElements = new ArrayList<>();
	}

	public ListItemRouteDetailsAdapterCommon(Context context,
											 String routeType,
											 List<DeliveryItem> elements,
											 RouteDetailsRightSideFragmentMerged theRightSideFragment){
		super();

		rightSideFragment = theRightSideFragment;

		mRouteType = routeType;

		mContext = context;
		mElements = elements;
		setConstantMargin();
	}

	public static void pauseCameraButton(){
		(new Handler()).postDelayed(new Runnable(){
			public void run(){
				CAMERA_BUTTON_TIMER = 0;
			}
		}, 1500);
	}

	private void setConstantMargin(){
		float dips = 4.8f;
		float scale = mContext.getResources().getDisplayMetrics().density;
		comment_Top_Margin = (Math.round(dips * scale));
	}

	public int getCount(){
		return mElements.size();
	}

	public Object getItem(int arg0){
		return mElements.get(arg0);
	}

	public long getItemId(int arg0){
		return arg0;
	}

	public View getView(int position, View convertView, ViewGroup parent){
		if(mElements != null && mElements.size() > 0){//&& position < mElements.size() && position >= 0){
			//=== crashlytics error reported here : Fatal Exception: java.lang.IndexOutOfBoundsException
			//Index: 11, Size: 11
			if(position < mElements.size() && position >= 0){
				DeliveryItem ti = mElements.get(position);

				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
					if(mRouteType.equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
						return generateRowRandom(ti, convertView);
					}
					else if(mRouteType.equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
						return generateRowRandom(ti, convertView);
					}
					else if(mRouteType.equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
						//=== SHOULD NEVER GET HERE
						logger.error("SEQUENCING ERROR CTApp.operationsMode = " + CTApp.operationsMode.toString());
						logger.error("SEQUENCING ERROR mRoute.routeType = " + mRouteType);
					}
				}
				else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
					if(mRouteType.equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
						return generateRowRandom(ti, convertView);
					}
					else if(mRouteType.equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
						return generateRowRandom(ti, convertView);
					}
					else if(mRouteType.equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
						//=== SHOULD NEVER GET HERE
						logger.error("SEQUENCING ERROR CTApp.operationsMode = " + CTApp.operationsMode.toString());
						logger.error("SEQUENCING ERROR mRoute.routeType = " + mRouteType);
					}
				}
				else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.DELIVERING)){
					if(mRouteType.equalsIgnoreCase(Route.RouteJobType.SEQUENCED.name())){
						return generateRowSequenced(ti, convertView);
					}
					else if(mRouteType.equalsIgnoreCase(Route.RouteJobType.UNSEQ.name())){
						//=== SHOULD NEVER GET HERE
						logger.error("DELIVERY ERROR CTApp.operationsMode = " + CTApp.operationsMode.toString());
						logger.error("DELIVERY ERROR mRouteType = " + mRouteType);
					}
					else if(mRouteType.equalsIgnoreCase(Route.RouteJobType.RANDOM.name())){
						return generateRowRandom(ti, convertView);
					}
				}
				else{
					//=== SHOULD NEVER GET HERE
					logger.error("ERROR CTApp.operationsMode = " + CTApp.operationsMode);
					logger.error("DELIVERY ERROR mRouteType = " + mRouteType);
				}
			}
			else{
				return null;
			}
		}

		return null;
	}

	private void instantiateRouteDetailViewHolderSequenced(RouteDetailViewHolderCommon routeDetailViewHolderCommon, View view){
		routeDetailViewHolderCommon.addressStreet = view.findViewById(R.id.liTopRowLeft);
		routeDetailViewHolderCommon.streetAddress = view.findViewById(R.id.liTopRowRight);

		routeDetailViewHolderCommon.messageNotes = view.findViewById(R.id.liBottomRowMessage);

		routeDetailViewHolderCommon.productsList = view.findViewById(R.id.productsListTextView);

		routeDetailViewHolderCommon.cameraButtonIcon = view.findViewById(R.id.iconCameraButton);

		routeDetailViewHolderCommon.toolsButtonIcon = view.findViewById(R.id.iconToolButton);

		routeDetailViewHolderCommon.timeActionButton = view.findViewById(R.id.deliveryListItemTimeActionButton);

		routeDetailViewHolderCommon.mainLayoutContainer = view.findViewById(R.id.dataRowContainer);
	}

	public View generateRowSequenced(DeliveryItem routeDetail, View view){
		RouteDetailViewHolderCommon routeDetailViewHolderCommon = null;
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		//logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());

		if(view == null){
			//logger.debug("creating a NEW VIEW");

			view = inflater.inflate(R.layout.main_item_deliver_status_sequenced, null);
			routeDetailViewHolderCommon = new RouteDetailViewHolderCommon();
			instantiateRouteDetailViewHolderSequenced(routeDetailViewHolderCommon, view);
			routeDetailViewHolderCommon.isStreetSummary = GlobalConstants.ROUTE_DETAIL_STATUS_LIST_ITEM;

			view.setTag(routeDetailViewHolderCommon);
		}
		//=== if not null
		else{
			//logger.debug("recycling a VIEW");

			view = inflater.inflate(R.layout.main_item_deliver_status_sequenced, null);

			routeDetailViewHolderCommon = new RouteDetailViewHolderCommon();
			instantiateRouteDetailViewHolderSequenced(routeDetailViewHolderCommon, view);

			routeDetailViewHolderCommon.isStreetSummary = GlobalConstants.ROUTE_DETAIL_STATUS_LIST_ITEM;

			view.setTag(routeDetailViewHolderCommon);
		}

		//=== SEQUENCED ITEMS
		routeDetailViewHolderCommon.jobDetailId = routeDetail.getJobDetailId();

		routeDetailViewHolderCommon.isInvalidAddress = routeDetail.getIsInvalidAddress();
		routeDetailViewHolderCommon.isStartingPoint = routeDetail.getStartingPoint();
		routeDetailViewHolderCommon.routeDetailSequenceId = routeDetail.getSequence();

		routeDetailViewHolderCommon.streetAddress.setVisibility(View.VISIBLE);

		//=== COMMON ITEMS
		routeDetailViewHolderCommon.timeActionButton.setVisibility(View.VISIBLE);
		//=== default starting color
		routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);

		//=== GATHER DELIVERY TYPE VARIABLES
		boolean hasCustomerServiceData = (routeDetail.getNumCustSvc() > 0) || (routeDetail.getNotes() != null && routeDetail.getNotes().length() > 1);
		boolean photoIsTaken = routeDetail.isPhotoTaken();
		boolean photoRequired = routeDetail.isPhotoRequired();
		long deliveryTime = routeDetail.getDeliveredTime() - mTimeZoneOffset;

		//=== THESE JOB TYPES ARE USUALLY NOT DOWNLOADED WITH SEQUENCED/WEEKLY ROUTES
		if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.VIP_Do_Not_Deliver.ordinal()//1
		   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Subscriber.ordinal()//2
		   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Do_Not_Deliver.ordinal()//3
		   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Cannot_Deliver.ordinal()){//4

			routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_dnd));

			if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
				if(routeDetail.getSequenceNew() > 0){
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
				}
				else{
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
				}
			}
			else{
				//=== DELIVERED
				if(routeDetail.getDelivered() == 1){
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkRedUndeliveredProcessed);
				}
				//=== PROCESSED NOT DELIVERED
				else if(routeDetail.getDelivered() == 0){
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkRedUndeliveredProcessed);
				}
				//=== NOT PROCESSED, NOT DELIVERED
				else if(routeDetail.getDelivered() == -1){
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightRedUndeliveredUnprocessed);
				}
			}
		}
		//=== NOT A STREET SUMMARY LIST ITEM
		//=== DELIVERY JOB TYPE OR MUST DELIVER JOB TYPES
		else if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Delivery.ordinal()//6
				|| routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Must_Deliver.ordinal()){
			//=== WAS DELIVERED
			if(routeDetail.getDelivered() == 1){
				//=== DEFAULTS
				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
					if(routeDetail.getSequenceNew() > 0){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
					}
					else{
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
					}
				}
				else{
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
				}

				routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_delivered));
				routeDetailViewHolderCommon.timeActionButton.setText(DateUtil.calcDateFromTime(deliveryTime, "H:mm"));

				if(photoRequired){
					routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_special_delivered));

					if(photoIsTaken){
					}
					else{
					}
				}
				else{
					routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_delivered));

					if(photoIsTaken){
					}
					else{
					}
				}
			}
			//=== SHOULD NEVER HIT THIS CASE ???
			else if(routeDetail.getDelivered() == 0){
				//=== DEFAULTS
				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
					if(routeDetail.getSequenceNew() > 0){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
					}
					else{
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
					}
				}
				else{
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
				}
				routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_not_yet_delivered));
				routeDetailViewHolderCommon.timeActionButton.setText("");

				if(photoRequired){
					routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_special));

					if(photoIsTaken){
					}
					else{
					}
				}
				else{
					routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_not_yet_delivered));

					if(photoIsTaken){
					}
					else{
					}
				}

				if(hasCustomerServiceData){
					if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGreenUndeliveredOrProcessed);
					}
				}
				else{
					//=== NOT A STREET SUMMARY LIST ITEM
					//=== DELIVERY JOB TYPE OR MUST DELIVER JOB TYPES
					//=== NO CUSTOMER SERVICE DATA
					if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
					}
				}
			}
			//=== NOT A STREET SUMMARY LIST ITEM
			//=== DELIVERY JOB TYPE OR MUST DELIVER JOB TYPES
			//=== NOT PROCESSED OR DELIVERED
			else if(routeDetail.getDelivered() == -1){
				//=== DEFAULTS
				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
					if(routeDetail.getSequenceNew() > 0){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
					}
					else{
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
					}
				}
				else{
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);//lighter gray
				}

				routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_not_yet_delivered));
				routeDetailViewHolderCommon.timeActionButton.setText("");

				if(photoRequired){
					routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_special));

					if(photoIsTaken){
					}
					else{
					}
				}
				else{
					routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_not_yet_delivered));

					if(photoIsTaken){
					}
					else{
					}
				}

				if(hasCustomerServiceData){
					if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGreenUndeliveredOrUnprocessed);
					}
				}
				else{
					//=== NOT A STREET SUMMARY LIST ITEM
					//=== DELIVERY JOB TYPE OR MUST DELIVER JOB TYPES
					//=== NO CUSTOMER SERVICE DATA
					if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
					}
				}
			}
		}

		//=== FINAL CHECK FOR STARTING POINT
		if(routeDetail.getStartingPoint()){
			if(routeDetail.getDelivered() > -1){
				routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkOrangeStartDelivered);
			}
			else{
				routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightOrangeStartDelivered);
			}
		}

		if(routeDetail.getIsInvalidAddress()){
			if(routeDetail.getStartingPoint()){
				routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(invalidAddressStartingPointBackground);
			}
			else{
				if(routeDetail.getDelivered() == 0){
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkYellowUnresolvedAddressBackground);    // dark yellow
				}
				else{
					routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(yellowUnresolvedAddressBackground);    //yellow
				}
			}
		}

		//=== SETS NUMERIC ADDRESS ON LIST ITEM
		routeDetailViewHolderCommon.addressStreet.setTextAppearance(CTApp.getCustomAppContext(), R.style.LargeBoldBlueText);
		routeDetailViewHolderCommon.addressStreet.setText(routeDetail.getGpsLocationAddressNumber());
		//logger.debug("ADDRESS STREET(getGpsLocationAddressNumber) = " + routeDetail.getGpsLocationAddressNumber());

		//=== SETS street NAME ON LIST ITEM
		//logger.debug("ADDRESS STREET(getGpsLocationAddressStreet) = " + routeDetail.getGpsLocationAddressStreet());
		if(routeDetail.getGpsLocationAddressStreet() != null){
			String str = routeDetail.getGpsLocationAddressStreet();

			routeDetailViewHolderCommon.streetAddress.setSingleLine(false);

			routeDetailViewHolderCommon.streetAddress.setText(str);
		}
		else{
			routeDetailViewHolderCommon.streetAddress.setText("");
		}

		if(routeDetail.isPhotoTaken()){
			routeDetailViewHolderCommon.cameraButtonIcon.setImageResource(R.drawable.button_photo_completed);
		}
		else if(routeDetail.isPhotoRequired()){
			routeDetailViewHolderCommon.cameraButtonIcon.setImageResource(R.drawable.button_photo_req);
		}
		else{
			routeDetailViewHolderCommon.cameraButtonIcon.setImageResource(R.drawable.camera_button_states);
		}

		routeDetailViewHolderCommon.cameraButtonIcon.setVisibility(View.VISIBLE);
		routeDetailViewHolderCommon.cameraButtonIcon.setTag(routeDetail);

		try{
			String notes = routeDetail.getNotes();

			routeDetailViewHolderCommon.messageNotes.setBackgroundColor(redMessageProductsBackground);    //light red
			routeDetailViewHolderCommon.productsList.setBackgroundColor(redMessageProductsBackground);    //light red

			if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.VIP_Do_Not_Deliver.ordinal()){//1) {
				notes = "VIP DND: " + notes;
			}
			else if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Subscriber.ordinal()//2
					|| routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Do_Not_Deliver.ordinal()//3
					|| routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Cannot_Deliver.ordinal()){//4) {
				notes = "DND: " + notes;
			}
			else if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Must_Deliver.ordinal()){//5) {
				notes = "MUST DEL: " + notes;
				routeDetailViewHolderCommon.messageNotes.setBackgroundColor(greenMessageProductsBackground);    //light green
				routeDetailViewHolderCommon.productsList.setBackgroundColor(greenMessageProductsBackground);    //light green
			}
			else if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Delivery.ordinal()){//6) {
				routeDetailViewHolderCommon.messageNotes.setBackgroundColor(greenMessageProductsBackground);    //light green
				routeDetailViewHolderCommon.productsList.setBackgroundColor(greenMessageProductsBackground);    //light green
			}

			if(notes != null && notes.length() > 1){
				try{
					notes = notes.trim();
				}
				catch(Exception e){
				}

				if(notes.endsWith(":")){
					notes = notes.replace(":", "");
				}

				if(notes.contains("#_")){
					//logger.debug("^^^^^^^^^^ 1 routeDetailViewHolderCommon.messageNotes = " + routeDetailViewHolderCommon.messageNotes == null ? "NULL" : "HAS VALUE");
					notes = notes.replace("#_", "\n");
					LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) routeDetailViewHolderCommon.messageNotes.getLayoutParams();

					layoutParams.leftMargin = comment_Top_Margin;
					routeDetailViewHolderCommon.messageNotes.setPadding(routeDetailViewHolderCommon.messageNotes.getPaddingLeft(),
																		routeDetailViewHolderCommon.messageNotes.getPaddingTop(), -2,
																		routeDetailViewHolderCommon.messageNotes.getPaddingBottom());

					routeDetailViewHolderCommon.messageNotes.setLayoutParams(layoutParams);
				}

				routeDetailViewHolderCommon.messageNotes.setText(notes.toUpperCase());
				routeDetailViewHolderCommon.messageNotes.setVisibility(View.VISIBLE);
			}
			else{
				routeDetailViewHolderCommon.messageNotes.setVisibility(View.INVISIBLE);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

		//=== BUILD THE PRODUCTS TEXT
		String productList = "";
		LinkedHashMap<Integer, DeliveryItemProduct> products = routeDetail.getProducts();
		Collection<DeliveryItemProduct> products2 = products.values();

		DeliveryItemProduct[] products3 = new DeliveryItemProduct[0];
		products3 = products2.toArray(products3);
		if(products3 != null && products3.length > 0){
			productList = "";
			for(int index = 0; index < products3.length; index++){
				productList += "(" + products3[index].getQuantity() + ") " + products3[index].getProductCode() + ",   ";
			}
			productList = productList.substring(0, productList.length() - 4);
		}

		if(productList == null || productList.isEmpty()){
			routeDetailViewHolderCommon.productsList.setVisibility(View.INVISIBLE);
		}
		else{
			routeDetailViewHolderCommon.productsList.setText(productList);
			routeDetailViewHolderCommon.productsList.setVisibility(View.VISIBLE);
		}

		//logger.debug("CAMERA BUTTON ONCLICK ADDED");

		routeDetailViewHolderCommon.cameraButtonIcon.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				logger.debug("CAMERA BUTTON CLICKED");
				if(CAMERA_BUTTON_TIMER == 0){
					logger.debug("CAMERA BUTTON TIMER = 0");
					CAMERA_BUTTON_TIMER = 8;
					pauseCameraButton();

					Intent intent = new Intent(mContext, CameraActivity.class);

					DeliveryItem rd = (DeliveryItem) v.getTag();
					intent.putExtra(GlobalConstants.EXTRA_JOBDETAILID, rd.getJobDetailId());
					int recId = rd.getId();
					intent.putExtra(GlobalConstants.EXTRA_JOBRECORDID, recId);
					intent.putExtra(GlobalConstants.EXTRA_DELIVERYID, rd.getDeliveryId());
					intent.putExtra(GlobalConstants.EXTRA_LATITUDE, rd.getGpsPhotoLocationLatitude());
					intent.putExtra(GlobalConstants.EXTRA_LONGITUDE, rd.getGpsPhotoLocationLongitude());
					intent.putExtra(GlobalConstants.EXTRA_CURRENT_ADDRESS, rd.getGpsLocationAddressNumber() + " "
																		   + rd.getGpsLocationAddressStreet());
					((Activity) mContext).startActivityForResult(intent, 9999);

					Intent intentP = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
					intentP.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, true);
					intentP.setPackage(mContext.getPackageName());//"com.agilegeodata.carriertrack");
					logger.debug("SENDING PAUSE INTENT FROM CAMERA BUTTON CLICK");
					mContext.sendBroadcast(intentP);
				}
				else{
					logger.debug("CAMERA BUTTON TIMER DECREMENTED");
					CAMERA_BUTTON_TIMER--;

					if(CAMERA_BUTTON_TIMER < 0){
						CAMERA_BUTTON_TIMER = 0;
					}
				}
			}

		});

		routeDetailViewHolderCommon.toolsButtonIcon.setVisibility(View.VISIBLE);
		routeDetailViewHolderCommon.toolsButtonIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tool_button_states));
		routeDetailViewHolderCommon.toolsButtonIcon.setTag(routeDetail);

		//logger.debug("TOOLS BUTTON ONCLICK ADDED");

		routeDetailViewHolderCommon.toolsButtonIcon.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				logger.debug("TOOLS BUTTON CLICKED");

				Intent intent = new Intent(mContext, ToolsActivity.class);
				DeliveryItem rd = (DeliveryItem) v.getTag();

				intent.putExtra(GlobalConstants.EXTRA_JOBDETAILID, rd.getJobDetailId());
				intent.putExtra(GlobalConstants.EXTRA_DELIVERYID, rd.getDeliveryId());
				intent.putExtra(GlobalConstants.EXTRA_LATITUDE, rd.getGpsLocationLatitude());
				intent.putExtra(GlobalConstants.EXTRA_LONGITUDE, rd.getGpsLocationLongitude());
				intent.putExtra(
						GlobalConstants.EXTRA_CURRENT_ADDRESS,
						rd.getGpsLocationAddressNumber() + " "
						+ rd.getGpsLocationAddressStreet());
//				intent.putExtra(GlobalConstants.EXTRA_DATA_OPERATIONS_MODE, myDataOperationsMode);
				intent.putExtra(GlobalConstants.EXTRA_ROUTE_TYPE, mRouteType);

				mContext.startActivity(intent);

				Intent intentP = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
				intentP.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, true);
				intentP.setPackage(mContext.getPackageName());//"com.agilegeodata.carriertrack");
				logger.debug("SENDING PAUSE INTENT FROM TOOLS BUTTON CLICK");
				mContext.sendBroadcast(intentP);
			}

		});

		routeDetailViewHolderCommon.timeActionButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				ViewParent parent = v.getParent().getParent();
				RouteDetailViewHolderCommon viewHolder = (RouteDetailViewHolderCommon) ((RelativeLayout) parent).getTag();
				if(!viewHolder.isInvalidAddress){
					boolean didReset = rightSideFragment.resetStartingPointHard(viewHolder.routeDetailSequenceId);
					rightSideFragment.getActivity().runOnUiThread(
							new Runnable(){
								@Override
								public void run(){
									rightSideFragment.mRouteDetailListAdapterCommon.notifyDataSetChanged();
								}
							}
																 );
				}
				else{
					Toast.makeText(mContext, "Cannot set an invalid delivery to the starting point for navigation.", Toast.LENGTH_LONG).show();
				}
			}
		});

		return view;
	}

	private View buildNewRandomListItemView(){
		//logger.debug("creating a NEW VIEW");
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

		View view = inflater.inflate(R.layout.main_item_deliver_status_random, null);

		RouteDetailViewHolderCommon routeDetailViewHolderCommon = new RouteDetailViewHolderCommon();

		routeDetailViewHolderCommon.addressStreet = view.findViewById(R.id.liTopRowLeft);
		routeDetailViewHolderCommon.addressStreet.setTextAppearance(mContext, R.style.MediumSmallerBoldBlackTextTablet);

		routeDetailViewHolderCommon.addressCount = view.findViewById(R.id.liTopRowRight);
		routeDetailViewHolderCommon.addressCount.setTextAppearance(mContext, R.style.VerySmallBlackText);

		routeDetailViewHolderCommon.addressCountValue = view.findViewById(R.id.liBottomRowQty);
		routeDetailViewHolderCommon.addressCountValue.setTextAppearance(mContext, R.style.VerySmallBlackText);

		routeDetailViewHolderCommon.dndCount = view.findViewById(R.id.liBottomRowDND);
		routeDetailViewHolderCommon.dndCount.setTextAppearance(mContext, R.style.VerySmallBlackText);

		routeDetailViewHolderCommon.dndCountValue = view.findViewById(R.id.liBottomRowDNDVal);
		routeDetailViewHolderCommon.dndCountValue.setTextAppearance(mContext, R.style.VerySmallBlackText);

		routeDetailViewHolderCommon.remainingCount = view.findViewById(R.id.liBottomRowRemaining);
		routeDetailViewHolderCommon.remainingCount.setTextAppearance(mContext, R.style.VerySmallBlackText);

		routeDetailViewHolderCommon.remainingCountValue = view.findViewById(R.id.liBottomRowRemainingVal);
		routeDetailViewHolderCommon.remainingCountValue.setTextAppearance(mContext, R.style.VerySmallBlackText);

		routeDetailViewHolderCommon.custServiceCount = view.findViewById(R.id.liBottomRowCustService);
		routeDetailViewHolderCommon.custServiceCount.setTextAppearance(mContext, R.style.VerySmallBlackText);

		routeDetailViewHolderCommon.custServiceCountValue = view.findViewById(R.id.liBottomRowCustServiceVal);
		routeDetailViewHolderCommon.custServiceCountValue.setTextAppearance(mContext, R.style.VerySmallBlackText);

		routeDetailViewHolderCommon.messageNotes = view.findViewById(R.id.liBottomRowMessage);
		routeDetailViewHolderCommon.messageNotes.setTextAppearance(mContext, R.style.VerySmallBlackText);

		routeDetailViewHolderCommon.cameraButtonIcon = view.findViewById(R.id.iconCameraButton);
		routeDetailViewHolderCommon.toolsButtonIcon = view.findViewById(R.id.iconToolButton);

		routeDetailViewHolderCommon.timeActionButton = view.findViewById(R.id.deliveryListItemTimeActionButton);

		routeDetailViewHolderCommon.streetCompass = view.findViewById(R.id.liStreetCompass);

		routeDetailViewHolderCommon.mainLayoutContainer = view.findViewById(R.id.dataRowContainer);
		routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(streetSummaryBackground);

		routeDetailViewHolderCommon.statisticsContainer = view.findViewById(R.id.bottomRow);

		view.setTag(routeDetailViewHolderCommon);

		return view;
	}

	private void populateStreetSummaryListItemRandom(RouteDetailViewHolderCommon routeDetailViewHolderCommon, DeliveryItem routeDetail){
		//=== VISIBLE ITEMS
		routeDetailViewHolderCommon.statisticsContainer.setVisibility(View.VISIBLE);
		routeDetailViewHolderCommon.streetCompass.setVisibility(View.VISIBLE);

		routeDetailViewHolderCommon.addressStreet.setText(routeDetail.getGpsLocationAddressStreet());

		routeDetailViewHolderCommon.dndCount.setVisibility(View.VISIBLE);
		routeDetailViewHolderCommon.dndCount.setText(mContext.getResources().getString(R.string.deliveryDnd));

		routeDetailViewHolderCommon.dndCountValue.setText(" " + routeDetail.getNumDND() + " ");
		routeDetailViewHolderCommon.dndCountValue.setVisibility(View.VISIBLE);

		routeDetailViewHolderCommon.remainingCount.setVisibility(View.VISIBLE);
		routeDetailViewHolderCommon.remainingCount.setText(mContext.getResources().getString(R.string.deliveryRemaining));

		routeDetailViewHolderCommon.remainingCountValue.setText(" " + routeDetail.getNumRemaining() + " ");
		routeDetailViewHolderCommon.remainingCountValue.setVisibility(View.VISIBLE);

		routeDetailViewHolderCommon.custServiceCount.setVisibility(View.VISIBLE);
		routeDetailViewHolderCommon.custServiceCount.setText(mContext.getResources().getString(R.string.deliveryCustSvc));

		routeDetailViewHolderCommon.custServiceCountValue.setText(" " + routeDetail.getNumCustSvc() + " ");
		routeDetailViewHolderCommon.custServiceCountValue.setVisibility(View.VISIBLE);

		//=== INVISIBLE ITEMS
		routeDetailViewHolderCommon.timeActionButton.setVisibility(View.INVISIBLE);
		routeDetailViewHolderCommon.timeActionButton.setClickable(false);

		routeDetailViewHolderCommon.addressCount.setVisibility(View.INVISIBLE);
		routeDetailViewHolderCommon.addressCountValue.setVisibility(View.INVISIBLE);

		routeDetailViewHolderCommon.messageNotes.setVisibility(View.INVISIBLE);

		routeDetailViewHolderCommon.cameraButtonIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.camera_button_states));
		routeDetailViewHolderCommon.cameraButtonIcon.setVisibility(View.INVISIBLE);
		routeDetailViewHolderCommon.cameraButtonIcon.setClickable(false);

		routeDetailViewHolderCommon.toolsButtonIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tool_button_states));
		routeDetailViewHolderCommon.toolsButtonIcon.setVisibility(View.INVISIBLE);
		routeDetailViewHolderCommon.toolsButtonIcon.setClickable(false);
	}

	public View generateRowRandom(DeliveryItem routeDetail, View view){
		RouteDetailViewHolderCommon routeDetailViewHolderCommon = null;
		//logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());

		if(view == null){
			//logger.debug("creating a NEW VIEW");
			view = buildNewRandomListItemView();
			routeDetailViewHolderCommon = (RouteDetailViewHolderCommon) view.getTag();
		}
		else{
			//logger.debug("recycling a VIEW");
			routeDetailViewHolderCommon = (RouteDetailViewHolderCommon) view.getTag();
			routeDetailViewHolderCommon.resetViewToDefaultNewRouteDetailStyleAndContentRandom();
		}

		routeDetailViewHolderCommon.routeDetailSequenceId = routeDetail.getSequence();

		//=== ONE LAYOUT FOR BOTH LIST ITEM TYPES, TURN THINGS INVISIBLE OR SHOW DEPENDING ON WHICH
		if(routeDetail.getStatus() == GlobalConstants.ROUTE_DETAIL_STATUS_STREETSUMMARY){
			//logger.debug("Processing a STREET SUMMARY");
			routeDetailViewHolderCommon.isStreetSummary = GlobalConstants.ROUTE_DETAIL_STATUS_STREETSUMMARY;

			populateStreetSummaryListItemRandom(routeDetailViewHolderCommon, routeDetail);
			routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(streetSummaryBackground);
		}
		else{
			//logger.debug("Processing an ADDRESS");
			routeDetailViewHolderCommon.isStreetSummary = GlobalConstants.ROUTE_DETAIL_STATUS_LIST_ITEM;

			routeDetailViewHolderCommon.timeActionButton.setVisibility(View.VISIBLE);

			routeDetailViewHolderCommon.addressCount.setVisibility(View.VISIBLE);
			routeDetailViewHolderCommon.addressCountValue.setVisibility(View.VISIBLE);

			routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);

			//===INVISIBLE ITEMS
			routeDetailViewHolderCommon.streetCompass.setVisibility(View.GONE);

			//=== GATHER DELIVERY TYPE VARIABLES
			boolean hasCustomerServiceData = (routeDetail.getNumCustSvc() > 0) || (routeDetail.getNotes() != null && routeDetail.getNotes().length() > 1);
			boolean photoIsTaken = routeDetail.isPhotoTaken();
			boolean photoRequired = routeDetail.isPhotoRequired();
			long deliveryTime = routeDetail.getDeliveredTime() - mTimeZoneOffset;

			//=== THESE JOB TYPES ARE USUALLY NOT DOWNLOADED WITH SEQUENCED/WEEKLY ROUTES
			if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.VIP_Do_Not_Deliver.ordinal()//1
			   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Subscriber.ordinal()//2
			   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Do_Not_Deliver.ordinal()//3
			   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Cannot_Deliver.ordinal()){//4

				routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_dnd));

				if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
					if(routeDetail.getSequenceNew() > 0){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
					}
					else{
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
					}
				}
				else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
					if(routeDetail.getSequenceNew() > 0){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
					}
					else{
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
					}
				}
				else{
					//=== DELIVERED
					if(routeDetail.getDelivered() == 1){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkRedUndeliveredProcessed);
					}
					//=== PROCESSED NOT DELIVERED
					else if(routeDetail.getDelivered() == 0){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkRedUndeliveredProcessed);
					}
					//=== NOT PROCESSED, NOT DELIVERED
					else if(routeDetail.getDelivered() == -1){
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightRedUndeliveredUnprocessed);
					}
				}
			}
			//=== NOT A STREET SUMMARY LIST ITEM
			//=== DELIVERY JOB TYPE OR MUST DELIVER JOB TYPES
			else if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Delivery.ordinal()//6
					|| routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Must_Deliver.ordinal()){//5) {
				//=== WAS DELIVERED
				if(routeDetail.getDelivered() == 1){
					//=== DEFAULTS
					if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
						if(routeDetail.getSequenceNew() > 0){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
						}
						else{
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
						}
					}
					else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
						if(routeDetail.getSequenceNew() > 0){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
						}
						else{
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
						}
					}
					else{
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
					}

					routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_delivered));
					routeDetailViewHolderCommon.timeActionButton.setText(DateUtil.calcDateFromTime(deliveryTime, "H:mm"));

					if(photoRequired){
						routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_special_delivered));

						if(photoIsTaken){
						}
						else{
						}
					}
					else{
						routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_delivered));

						if(photoIsTaken){
						}
						else{
						}
					}
				}
				//=== SHOULD NEVER HIT THIS CASE ???
				else if(routeDetail.getDelivered() == 0){
					//=== DEFAULTS
					if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
						if(routeDetail.getSequenceNew() > 0){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
						}
						else{
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
						}
					}
					else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
						if(routeDetail.getSequenceNew() > 0){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
						}
						else{
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
						}
					}
					else{
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
					}
					routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_not_yet_delivered));
					routeDetailViewHolderCommon.timeActionButton.setText("");

					if(photoRequired){
						routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_special));

						if(photoIsTaken){
						}
						else{
						}
					}
					else{
						routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_not_yet_delivered));

						if(photoIsTaken){
						}
						else{
						}
					}

					if(hasCustomerServiceData){
						if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGreenUndeliveredOrProcessed);
						}
						else if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGreenUndeliveredOrProcessed);
						}
					}
					else{
						//=== NOT A STREET SUMMARY LIST ITEM
						//=== DELIVERY JOB TYPE OR MUST DELIVER JOB TYPES
						//=== NO CUSTOMER SERVICE DATA
						if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
						}
						else if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
						}
					}
				}
				//=== NOT A STREET SUMMARY LIST ITEM
				//=== DELIVERY JOB TYPE OR MUST DELIVER JOB TYPES
				//=== NOT PROCESSED OR DELIVERED
				else if(routeDetail.getDelivered() == -1){
					//=== DEFAULTS
					if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
						if(routeDetail.getSequenceNew() > 0){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
						}
						else{
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
						}
					}
					else if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
						if(routeDetail.getSequenceNew() > 0){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(darkGrayDeliveredOrProcessed);
						}
						else{
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
						}
					}
					else{
						routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);//lighter gray
					}

					routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_not_yet_delivered));
					routeDetailViewHolderCommon.timeActionButton.setText("");

					if(photoRequired){
						routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_special));

						if(photoIsTaken){
						}
						else{
						}
					}
					else{
						routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getResources().getDrawable(R.drawable.status_not_yet_delivered));

						if(photoIsTaken){
						}
						else{
						}
					}

					if(hasCustomerServiceData){
						if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGreenUndeliveredOrUnprocessed);
						}
						else if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGreenUndeliveredOrUnprocessed);
						}
					}
					else{
						//=== NOT A STREET SUMMARY LIST ITEM
						//=== DELIVERY JOB TYPE OR MUST DELIVER JOB TYPES
						//=== NO CUSTOMER SERVICE DATA
						if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
						}
						else if(!CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
							routeDetailViewHolderCommon.mainLayoutContainer.setBackgroundColor(lightGrayUndeliveredUnprocessed);
						}
					}
				}
			}

			/*---SETS NUMERIC ADDRESS ON LIST ITEM---*/
			routeDetailViewHolderCommon.addressStreet.setTextAppearance(CTApp.getCustomAppContext(), R.style.LargeBoldBlueText);
			routeDetailViewHolderCommon.addressStreet.setText(routeDetail.getGpsLocationAddressNumber() + " " + routeDetail.getGpsLocationAddressStreet());
			//logger.debug("ADDRESS STREET(getGpsLocationAddressNumber) = " + routeDetail.getGpsLocationAddressNumber());

			/*---SETS street NAME ON LIST ITEM---*/
			//logger.debug("ADDRESS STREET(getGpsLocationAddressStreet) = " + routeDetail.getGpsLocationAddressStreet());
			if(routeDetail.getGpsLocationAddressStreet() != null){
				String str = routeDetail.getGpsLocationAddressStreet();

				routeDetailViewHolderCommon.addressCount.setSingleLine(false);

// ????			routeDetailViewHolderCommon.addressCount.setText(str);
			}
			else{
				routeDetailViewHolderCommon.addressCount.setText("");
			}

			if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING) ||
			   CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.RENUMBERING)){
				routeDetailViewHolderCommon.addressCountValue.setVisibility(View.GONE);
				routeDetailViewHolderCommon.remainingCount.setVisibility(View.GONE);
				routeDetailViewHolderCommon.dndCount.setVisibility(View.GONE);
				routeDetailViewHolderCommon.dndCountValue.setVisibility(View.GONE);
				routeDetailViewHolderCommon.custServiceCount.setVisibility(View.GONE);
				routeDetailViewHolderCommon.messageNotes.setVisibility(View.GONE);
				routeDetailViewHolderCommon.statisticsContainer.setVisibility(View.GONE);
				routeDetailViewHolderCommon.cameraButtonIcon.setVisibility(View.GONE);
				routeDetailViewHolderCommon.toolsButtonIcon.setVisibility(View.GONE);
				routeDetailViewHolderCommon.streetCompass.setVisibility(View.GONE);

				routeDetailViewHolderCommon.timeActionButton.setBackground(mContext.getDrawable(/*).getResources().getDrawable(*/R.drawable.button_state_square_sequenced));
				routeDetailViewHolderCommon.timeActionButton.setTextColor(Color.WHITE);
				routeDetailViewHolderCommon.timeActionButton.setTextSize(40);
				routeDetailViewHolderCommon.timeActionButton.setText(routeDetailViewHolderCommon.routeDetailSequenceId + "");

				final RouteDetailViewHolderCommon routeDetailViewHolderCommonFinal = routeDetailViewHolderCommon;

				routeDetailViewHolderCommon.timeActionButton.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v){
						logger.debug("TAPPED TIME ACTION BUTTON ON list item route details adapter common 4");
						displayRenumberingDialog("Re-numbering", routeDetail);
					}

				});
			}
			else{
				int qty = routeDetail.getQuantity();
				if(qty > 1){
					routeDetailViewHolderCommon.addressCountValue.setText(mContext.getResources().getString(
							R.string.deliveryQty, qty));
					routeDetailViewHolderCommon.addressCountValue.setVisibility(View.VISIBLE);
				}
				else{
					routeDetailViewHolderCommon.addressCountValue.setText("");
					routeDetailViewHolderCommon.addressCountValue.setVisibility(View.GONE);
				}

				routeDetailViewHolderCommon.remainingCount.setText("" + routeDetail.getNumRemaining());
				routeDetailViewHolderCommon.remainingCount.setVisibility(View.GONE);

				try{
					routeDetailViewHolderCommon.remainingCountValue.setText("");
					routeDetailViewHolderCommon.remainingCountValue.setVisibility(View.GONE);
				}
				catch(Exception e){

				}

				routeDetailViewHolderCommon.dndCount.setText("");
				routeDetailViewHolderCommon.dndCount.setVisibility(View.GONE);
				routeDetailViewHolderCommon.dndCountValue.setVisibility(View.GONE);

				routeDetailViewHolderCommon.custServiceCount.setText("");
				routeDetailViewHolderCommon.custServiceCount.setVisibility(View.GONE);

				try{
					routeDetailViewHolderCommon.custServiceCountValue.setText("");
					routeDetailViewHolderCommon.custServiceCountValue.setVisibility(View.GONE);
				}
				catch(Exception e){
				}

				try{
					String notes = routeDetail.getNotes();

					if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.VIP_Do_Not_Deliver.ordinal()){//1) {
						notes = "VIP DND: " + notes;
					}
					else if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Subscriber.ordinal()//2
							|| routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Do_Not_Deliver.ordinal()//3
							|| routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Cannot_Deliver.ordinal()){//4) {
						notes = "DND: " + notes;
					}
					else if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Must_Deliver.ordinal()){//5) {
						notes = "MUST DEL: " + notes;
					}

					if(notes != null && notes.length() > 1){
						try{
							notes = notes.trim();
						}
						catch(Exception e){
						}

						if(notes.endsWith(":")){
							notes = notes.replace(":", "");
						}

						routeDetailViewHolderCommon.statisticsContainer.setVisibility(View.VISIBLE);
						if(notes.contains("#_")){
							logger.debug("^^^^^^^^^^ 2 routeDetailViewHolderCommon.messageNotes = " + routeDetailViewHolderCommon.messageNotes == null ? "NULL" : "HAS VALUE");
							notes = notes.replace("#_", "\n");
							LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) routeDetailViewHolderCommon.messageNotes.getLayoutParams();

							layoutParams.leftMargin = comment_Top_Margin;
							routeDetailViewHolderCommon.messageNotes.setPadding(routeDetailViewHolderCommon.messageNotes.getPaddingLeft(),
																				routeDetailViewHolderCommon.messageNotes.getPaddingTop(), -2,
																				routeDetailViewHolderCommon.messageNotes.getPaddingBottom());

							routeDetailViewHolderCommon.messageNotes.setLayoutParams(layoutParams);
						}

						routeDetailViewHolderCommon.messageNotes.setText(notes);
						routeDetailViewHolderCommon.messageNotes.setVisibility(View.VISIBLE);
					}
					else{
						routeDetailViewHolderCommon.statisticsContainer.setVisibility(View.GONE);
						routeDetailViewHolderCommon.messageNotes.setVisibility(View.GONE);
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}

				routeDetailViewHolderCommon.cameraButtonIcon.setVisibility(View.VISIBLE);
				routeDetailViewHolderCommon.cameraButtonIcon.setTag(routeDetail);

				if(routeDetail.isPhotoTaken()){
					routeDetailViewHolderCommon.cameraButtonIcon.setImageResource(R.drawable.button_photo_completed);
				}
				else if(routeDetail.isPhotoRequired()){
					if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.VIP_Do_Not_Deliver.ordinal()//1
					   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Subscriber.ordinal()//2
					   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Do_Not_Deliver.ordinal()//3
					   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Cannot_Deliver.ordinal()){//4) {
						routeDetailViewHolderCommon.cameraButtonIcon.setImageResource(R.drawable.button_photo_req_dnd);
					}
					else{
						routeDetailViewHolderCommon.cameraButtonIcon.setImageResource(R.drawable.button_photo_req);
					}
				}
				else{
					routeDetailViewHolderCommon.cameraButtonIcon.setImageResource(R.drawable.camera_button_states);
				}

				routeDetailViewHolderCommon.cameraButtonIcon.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v){
						if(CAMERA_BUTTON_TIMER == 0){
							CAMERA_BUTTON_TIMER = 8;
							pauseCameraButton();

							Intent intent = new Intent(mContext, CameraActivity.class);

							DeliveryItem rd = (DeliveryItem) v.getTag();
							intent.putExtra(GlobalConstants.EXTRA_JOBDETAILID, rd.getJobDetailId());
							int recId = rd.getId();
							intent.putExtra(GlobalConstants.EXTRA_JOBRECORDID, recId);
							intent.putExtra(GlobalConstants.EXTRA_DELIVERYID, rd.getDeliveryId());
							//logger.info("+++tttt++++"+rd.getGpsPhotoLocationLatitude(),"++++tttt++++"+rd.getGpsLocationLongitude());
							intent.putExtra(GlobalConstants.EXTRA_LATITUDE, rd.getGpsPhotoLocationLatitude());
							intent.putExtra(GlobalConstants.EXTRA_LONGITUDE, rd.getGpsPhotoLocationLongitude());
							intent.putExtra(GlobalConstants.EXTRA_CURRENT_ADDRESS, rd.getGpsLocationAddressNumber() + " "
																				   + rd.getGpsLocationAddressStreet());
							//logger.debug("%%%% mRouteDetailCache size = " + ListItemRouteDetailsAdapter.this.rightSideFragment.mRouteDetailCache.getCurRouteMap().values().size());
							((Activity) mContext).startActivityForResult(intent, 9999);

							Intent intentP = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
							intentP.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, true);
							intentP.setPackage(mContext.getPackageName());//"com.agilegeodata.carriertrack");
							logger.debug("SENDING PAUSE INTENT FROM list item route details adapter common 3");
							mContext.sendBroadcast(intentP);
						}
						else{
							CAMERA_BUTTON_TIMER--;

							if(CAMERA_BUTTON_TIMER < 0){
								CAMERA_BUTTON_TIMER = 0;
							}
						}
					}

				});

				routeDetailViewHolderCommon.toolsButtonIcon.setVisibility(View.VISIBLE);
				routeDetailViewHolderCommon.toolsButtonIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.tool_button_states));
				routeDetailViewHolderCommon.toolsButtonIcon.setTag(routeDetail);
				routeDetailViewHolderCommon.toolsButtonIcon.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v){
						// logger.info("ListItemRouteDetailsAdapter:Tools");

						Intent intent = new Intent(mContext, ToolsActivity.class);
						DeliveryItem rd = (DeliveryItem) v.getTag();

						intent.putExtra(GlobalConstants.EXTRA_JOBDETAILID, rd.getJobDetailId());
						intent.putExtra(GlobalConstants.EXTRA_DELIVERYID, rd.getDeliveryId());
						intent.putExtra(GlobalConstants.EXTRA_LATITUDE, rd.getGpsLocationLatitude());
						intent.putExtra(GlobalConstants.EXTRA_LONGITUDE, rd.getGpsLocationLongitude());
						intent.putExtra(
								GlobalConstants.EXTRA_CURRENT_ADDRESS,
								rd.getGpsLocationAddressNumber() + " "
								+ rd.getGpsLocationAddressStreet());
						intent.putExtra(GlobalConstants.EXTRA_ROUTE_TYPE, mRouteType);

						mContext.startActivity(intent);

						Intent intentP = new Intent(GlobalConstants.INTENT_PAUSE_MODE);
						intentP.putExtra(GlobalConstants.EXTRA_PAUSE_MODE, true);
						intentP.setPackage(mContext.getPackageName());//"com.agilegeodata.carriertrack");
						logger.debug("SENDING PAUSE INTENT FROM list item route details adapter common 4");
						mContext.sendBroadcast(intentP);
					}

				});
			}
			// logger.debug(" Done processing the row");
		}

		return view;
	}

	private void displayRenumberingDialog(String titleStr, DeliveryItem routeDetail){
		AlertDialog.Builder builder = new AlertDialog.Builder(rightSideFragment.getActivity());
		LayoutInflater inflater = (LayoutInflater) rightSideFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.renumbering_dialog, rightSideFragment.getActivity().findViewById(R.id.layout_root));

		int sequenceNumberMoving = routeDetail.getSequence();

		ImageView image = layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.icon);

		builder.setView(layout);

		AlertDialog alertDialog = builder.create();

		alertDialog.show();

		TextView prompt = alertDialog.findViewById(R.id.promptText);
		prompt.setText("Select Method of Move for sequence # " + sequenceNumberMoving +
					   "\nat " + routeDetail.getGpsLocationAddressNumber() +
					   " " + routeDetail.getGpsLocationAddressStreet());

		Button cancel = alertDialog.findViewById(R.id.renumberCancel);
		cancel.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				alertDialog.dismiss();
			}
		});

		TextView inputText = alertDialog.findViewById(R.id.atPositionTextEdit);
		inputText.setText("");
		Button doIt = alertDialog.findViewById(R.id.renumberContinue);
		doIt.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//=== RENUMBER HERE
				RadioGroup radioGroup = alertDialog.findViewById(R.id.renumberoptions);
				int checkedRadioButton = radioGroup.getCheckedRadioButtonId();
				TextView sequenceInput = inputText;

				int sequenceNumberToMoveTo = 0;
				int sequenceNumberMovingFrom = sequenceNumberMoving;

				int sequenceInterval = DBHelper.getInstance().fetchSequenceIntervalForJobDetailId_Sequenced(routeDetail.getJobDetailId());

				switch(checkedRadioButton){
					case R.id.radiofirst:
						//=== PUT SEQUENCE # TAPPED IN FIRST POSITION
						DeliveryItem firstDeliveryItem = DBHelper.getInstance().fetchFirstRouteDetailForJobDetailId_Sequenced(routeDetail.getJobDetailId());
						sequenceNumberToMoveTo = firstDeliveryItem.getSequence();
						DBHelper.getInstance().renumberSequenceNumbers(sequenceNumberMovingFrom, sequenceNumberToMoveTo, sequenceInterval, routeDetail.getJobDetailId());
						RouteDetailsRightSideFragmentMerged.instance.updateFullListFromHandlerRandom();

						alertDialog.dismiss();
						break;
					case R.id.radiolast:
						//=== PUT SEQUENCE # TAPPED IN LAST POSITION
						sequenceNumberToMoveTo = DBHelper.getInstance().fetchLargestSequenceForJobDetailId_Sequenced(routeDetail.getJobDetailId());

						DBHelper.getInstance().renumberSequenceNumbers(sequenceNumberMovingFrom, sequenceNumberToMoveTo, sequenceInterval, routeDetail.getJobDetailId());
						RouteDetailsRightSideFragmentMerged.instance.updateFullListFromHandlerRandom();

						alertDialog.dismiss();
						break;
					case R.id.radioPosition:
						//=== PUT SEQUENCE # TAPPED IN ENTERED POSITION
						String input = sequenceInput.getText().toString().trim();
						if(input.isEmpty()){
							//=== WARN USER HERE
							logger.debug("RENUMBER INPUT IS EMPTY");
						}
						else{
							sequenceNumberToMoveTo = (Integer.valueOf(input)).intValue();
							DBHelper.getInstance().renumberSequenceNumbers(sequenceNumberMovingFrom, sequenceNumberToMoveTo, sequenceInterval, routeDetail.getJobDetailId());

							RouteDetailsRightSideFragmentMerged.instance.updateFullListFromHandlerRandom();
							//logger.debug("addressesWithinDeliveryArea has " + addressesWithinDeliveryArea.values().size() + " items");

							alertDialog.dismiss();
							break;
						}

						break;
				}
			}
		});
	}

	public void setTimeZoneOffset(int mTimeZoneOffset){
		this.mTimeZoneOffset = mTimeZoneOffset;
	}

	//===STUFF TO SPEED UP AND BE MORE EFFICIENT
	static class RouteDetailViewHolderCommon{
		//=== COMMON VARIABLES
		TextView addressStreet;
		TextView messageNotes;
		ImageView cameraButtonIcon;
		ImageView toolsButtonIcon;
		TextView timeActionButton;
		RelativeLayout mainLayoutContainer;
		int isStreetSummary = GlobalConstants.ROUTE_DETAIL_STATUS_LIST_ITEM;

		//=== RANDOM VARIABLES
		TextView addressCount;
		TextView addressCountValue;
		TextView dndCount;
		TextView dndCountValue;
		TextView remainingCount;
		TextView remainingCountValue;
		TextView custServiceCount;
		TextView custServiceCountValue;
		LinearLayout statisticsContainer;
		TextView streetCompass;

		//=== SEQUENCED VARIABLES
		TextView streetAddress;
		TextView productsList;
		boolean isStartingPoint = false;
		int routeDetailSequenceId = -1;
		int jobDetailId = -1;

		boolean isInvalidAddress = false;

		public void resetViewToDefaultNewRouteDetailStyleAndContentRandom(){
			//=== COMMON VARIABLES
			addressStreet.setText(null);
			messageNotes.setText(null);
			timeActionButton.setText(null);
			isInvalidAddress = false;

			//=== RANDOM VARIABLES
			addressCount.setText(null);
			addressCountValue.setText(null);
			dndCountValue.setText(null);
			remainingCountValue.setText(null);
			custServiceCountValue.setText(null);

			//=== SEQUENCED VARIABLES
			isStartingPoint = false;
			routeDetailSequenceId = -1;
			jobDetailId = -1;
			isStreetSummary = GlobalConstants.ROUTE_DETAIL_STATUS_LIST_ITEM;
		}
	}
}
