package com.agilegeodata.carriertrack.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.objects.DeliveryItemProduct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScanListItemAdapter extends BaseAdapter{
	public static final String TAG = ScanListItemAdapter.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private final Context mContext;
	private final List<DeliveryItemProduct> mElements;

	public ScanListItemAdapter(Context context, List<DeliveryItemProduct> elements){
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
		DeliveryItemProduct ti = mElements.get(position);

		return generateRow(ti, convertView);
	}

	public View generateRow(DeliveryItemProduct deliveryItemProduct, View view){
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		view = inflater.inflate(R.layout.list_item_scanlist_select, null);

		TableLayout listItem = view.findViewById(R.id.tableLayout);
		ImageView iv = view.findViewById(R.id.liImage);
		TextView topRow = view.findViewById(R.id.liTopRow);
		TextView bottomRow = view.findViewById(R.id.liBottomRow);
		listItem.setEnabled(true);
		if(deliveryItemProduct.getDeliveryDate() > 0){
			listItem.setBackgroundResource(R.drawable.medium_green_shape);
			iv.setImageResource(R.drawable.icon_routeselect_completed);
		}
		else{
			listItem.setBackgroundResource(R.drawable.medium_red_shape);
			iv.setImageResource(R.drawable.icon_routeselect);
		}

		topRow.setText(deliveryItemProduct.getProductType() + " " + deliveryItemProduct.getProductCode());
		bottomRow.setText(deliveryItemProduct.getScanCode());

		return view;
	}
}

