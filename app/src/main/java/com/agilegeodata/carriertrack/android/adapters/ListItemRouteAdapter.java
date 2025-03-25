package com.agilegeodata.carriertrack.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.objects.Route;
import com.agilegeodata.carriertrack.android.utils.DataUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ListItemRouteAdapter extends BaseAdapter{
	public static final String TAG = ListItemRouteAdapter.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private final Context mContext;
	private final List<Route> mElements;

	public ListItemRouteAdapter(Context context, List<Route> elements){
		super();
		mContext = context;
		mElements = elements;
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
		Route ti = mElements.get(position);

		return generateRow(ti, convertView);
	}

	public View generateRow(Route routeIn, View view){
		final Route routeInfo = routeIn;

		if(routeIn.getNumAddress() > 0 || routeIn.getDownloaded() > 0){
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			view = inflater.inflate(R.layout.main_item_routeselect, null);

			TableLayout listItem = view.findViewById(R.id.tableLayout);
			if(routeIn.getRouteJobType() != null & routeIn.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.RANDOM.toString())){
				listItem.setBackgroundResource(R.drawable.button_state_square_random);
			}
			else if(routeIn.getRouteJobType() != null & routeIn.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.UNSEQ.toString())){
				listItem.setBackgroundResource(R.drawable.button_state_square_unsequenced);
			}
			else if(routeIn.getRouteJobType() != null & routeIn.getRouteJobType().equalsIgnoreCase(Route.RouteJobType.SEQUENCED.toString())){
				listItem.setBackgroundResource(R.drawable.button_state_square_sequenced);
			}
			else{
				//===DEFAULT TO RANDOM, LET THE USER RESOLVE WITH MANAGEMENT IF
				//===ROUTE IS UNDEFINED
				listItem.setBackgroundResource(R.drawable.button_state_square_random);
			}

			ImageView iv = view.findViewById(R.id.liImage);
			// logger.debug("Comparing routeIn.getLastStarted() :" +routeIn.getLastStarted()+ "  routeIn.getNumAddress()== routeIn.getNumDelivered() : "+ routeIn.getNumAddress()+ " " +routeIn.getNumDelivered());
			if(routeIn.getLastStarted() != 0){
				iv.setImageResource(R.drawable.icon_routeselect);
			}
			else if(routeIn.getNumAddress() > 0 && routeIn.getNumAddress() == routeIn.getNumDelivered()){
				iv.setImageResource(R.drawable.icon_routeselect_completed);
			}

			TextView topRow = view.findViewById(R.id.liTopRow);
			topRow.setText(routeIn.getRouteId());
			TextView middleRow = view.findViewById(R.id.liMiddleRow);

			try{
				middleRow.setText(routeIn.getJobId());
			}
			catch(Exception e){
				logger.error("EXCEPTION", e);
			}

			TextView bottomRow = view.findViewById(R.id.liBottomRow);
			TextView bottomRowa = view.findViewById(R.id.liBottomRowa);

			if(routeIn.getLastStarted() > 0){
				bottomRowa.setVisibility(View.VISIBLE);
				bottomRowa.setText(mContext.getResources().getString(R.string.routeSelectLastStartedNew));

				bottomRow.setVisibility(View.VISIBLE);
				bottomRow.setText(DataUtils.calcLastPerformed(routeIn.getLastStarted(), mContext.getResources(),
															  GlobalConstants.DEFAULT_DATETIME_FORMAT_SHORT));
			}

			TextView topRowRight = view.findViewById(R.id.liTopRowRightVal);

			topRowRight.setText(routeIn.getNumAddress() + "");

			TextView middleRowRight = view.findViewById(R.id.liMiddleRowRightVal);
			middleRowRight.setText(routeIn.getNumDelivered() + "");
			TextView middleBottomRowRight = view.findViewById(R.id.liMiddleBottomRowRightVal);
			middleBottomRowRight.setText(routeIn.getNumPhotos() + "");
			TextView bottomRowRight = view.findViewById(R.id.liBottomRowRightVal);
			bottomRowRight.setText(routeIn.getNumPhotosUploaded() + "");

			return view;
		}
		else{
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			view = inflater.inflate(R.layout.main_item_routedownload, null);

			ImageView iv = view.findViewById(R.id.liImage);
			// logger.debug("Comparing routeIn.getLastStarted() :" +routeIn.getLastStarted()+ "  routeIn.getNumAddress()== routeIn.getNumDelivered() : "+ routeIn.getNumAddress()+ " " +routeIn.getNumDelivered());

			if(routeIn.getLastStarted() != 0){
				iv.setImageResource(R.drawable.icon_routeselect);
			}
			else if(routeIn.getNumAddress() > 0 && routeIn.getNumAddress() == routeIn.getNumDelivered()){
				iv.setImageResource(R.drawable.icon_routeselect_completed);
			}

			TextView topRow = view.findViewById(R.id.liTopRow);
			topRow.setText(routeIn.getRouteId());
			TextView middleRow = view.findViewById(R.id.liMiddleRow);

			try{
				middleRow.setText(routeIn.getJobId());
			}
			catch(Exception e){
				logger.error("EXCEPTION", e);
			}

			Button downloadButton = view.findViewById(R.id.btnDownloadButton);
			downloadButton.setText(mContext.getResources().getString(R.string.routedDownloadbtn));
			downloadButton.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
                    //logger.info("ListItemRouteAdapter:btnDownloadButton");

					final Intent intentP = new Intent(GlobalConstants.DOWNLOAD_ROUTE);
					intentP.putExtra(GlobalConstants.ROUTE_ID, routeInfo.getRouteId());
					intentP.putExtra(GlobalConstants.JOB_ID, routeInfo.getJobDetailId());
					intentP.setPackage(ListItemRouteAdapter.this.mContext.getPackageName());//"com.agilegeodata.carriertrack");
					mContext.sendBroadcast(intentP);
				}
			});

			return view;
		}
	}
}

