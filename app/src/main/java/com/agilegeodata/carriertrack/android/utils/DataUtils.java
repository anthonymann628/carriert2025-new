package com.agilegeodata.carriertrack.android.utils;

import android.content.res.Resources;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class DataUtils{
	public static final String TAG = DataUtils.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	public static String calcLastPerformed(long dateLong, Resources resources, String dateFormat){
		Calendar now = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		String date = resources.getString(R.string.notAvailable);
		if(dateLong > 0){
			last.setTimeInMillis(dateLong);
			boolean isSameDate = DateUtil.isSameDate(now, last);

			if(isSameDate){
				date = DateUtil.calcDateFromTime(dateLong, GlobalConstants.DEFAULT_TODAYDATE_FORMAT);
			}
			else{
				date = DateUtil.calcDateFromTime(dateLong, dateFormat);
			}
		}
		//logger.debug("Date is: "+ date);

		return date;
	}
}
