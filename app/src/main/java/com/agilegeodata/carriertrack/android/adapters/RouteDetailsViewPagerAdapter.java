package com.agilegeodata.carriertrack.android.adapters;

import android.view.View;

import androidx.viewpager.widget.PagerAdapter;

import com.agilegeodata.carriertrack.android.R;

public class RouteDetailsViewPagerAdapter extends PagerAdapter{
	private int count = 2;

	public Object instantiateItem(View collection, int position){

		int resId = 0;
		switch(position){
			case 0:
				resId = R.id.listPage;
				break;
			case 1:
				resId = R.id.mapPage;
				break;
		}

		return collection.findViewById(resId);
	}

	@Override
	public int getCount(){
		return count;
	}

	public void setCount(int newCount){
		if(newCount > 2){
			count = 2;
		}
		else{
			count = newCount;
		}
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1){
		return arg0 == arg1;
	}
}
