package com.agilegeodata.carriertrack.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RouteActivityViewListAdapter extends BaseAdapter{
	public static final String TAG = RouteActivityViewListAdapter.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private final Context mContext;
	private List<String> mElements = new ArrayList<String>();

	public RouteActivityViewListAdapter(Context context, List<String> elements){
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
		String ti = mElements.get(position);

		return generateRow(ti, convertView);
	}

	public View generateRow(String ti, View view){
		String[] bits = ti.split("\\|\\|\\|");
		final String descriptionString = bits[0].trim();
		final String dateString = bits[1].trim();

		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		view = inflater.inflate(R.layout.route_activity_list_view_item, null);

		TextView descriptionItem = view.findViewById(R.id.descriptionTextView);
		TextView dateItem = view.findViewById(R.id.dateTextView);

		descriptionItem.setText("  " + descriptionString);
		dateItem.setText("  " + dateString);

		if(descriptionString.toUpperCase().contains("START")){
			descriptionItem.setBackgroundResource(R.color.activitygreen);
		}
		else{
			descriptionItem.setBackgroundResource(R.color.activityred);
		}

		LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams3.gravity = Gravity.CENTER_VERTICAL;

		layoutParams3.height = 72;
		view.setLayoutParams(layoutParams3);

		return view;
	}
}

