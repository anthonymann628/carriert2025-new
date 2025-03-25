package com.agilegeodata.carriertrack.android.objects;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerAccessActivity{

	static public void updateServerTimestamp(Context context, String directionData){
		String s = "";

		long milliseconds = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
		Date resultdate = new Date(milliseconds);
		s = directionData + sdf.format(resultdate);

		SharedPreferences prefs = context.getSharedPreferences(
				GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor1 = prefs.edit();
		editor1.putString(GlobalConstants.LAST_CONNECTION_TO_SERVER, s);
		editor1.apply();
	}

	/**
	 * Returns last save server connection
	 */
	static public String getServerTimestamp(Activity act){
		SharedPreferences prefs = act.getSharedPreferences(
				GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);

		return prefs.getString(GlobalConstants.LAST_CONNECTION_TO_SERVER, "");
	}
}
