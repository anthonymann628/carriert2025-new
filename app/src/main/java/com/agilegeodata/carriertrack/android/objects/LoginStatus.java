/*---------------------------------------------------------------------------
 
    @file LoginStatus.java

    (C) Copyright 2012 by Agile Geo Data, LLC.

    The information contained herein is confidential, proprietary
    to Agile Geo Data, LLC. Use of this information by anyone other than
    authorized employees of Agile Geo Data, LLC is granted only
    under a written non-disclosure agreement, expressly prescribing
    the scope and manner of such use.
 
---------------------------------------------------------------------------*/

package com.agilegeodata.carriertrack.android.objects;

import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;

import java.util.Calendar;

public class LoginStatus{

	public static boolean hasExpired(){
		String str = DBHelper.getInstance().fetchItemValueByItemName_Common(GlobalConstants.PREF_LASTTIME_LOGIN);

		if(str != null){
			try{
				long lastTimeLoggedIn = Long.parseLong(str);
				Calendar now = Calendar.getInstance();
				now.add(Calendar.HOUR, GlobalConstants.DEF_LOGIN_EXPIRED_VALUE);

				if(lastTimeLoggedIn < now.getTimeInMillis()){
					return true;
				}
			}
			catch(NumberFormatException nfe){
				nfe.printStackTrace();
			}
		}

		return false;
	}
}
