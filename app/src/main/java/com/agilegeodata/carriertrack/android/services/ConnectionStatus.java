package com.agilegeodata.carriertrack.android.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
/*
Deprecated
Callers should instead use the ConnectivityManager. NetworkCallback API
to learn about connectivity changes,
or switch to use ConnectivityManager. getNetworkCapabilities or
ConnectivityManager. getLinkProperties to get information synchronously
*/
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionStatus{
	public static final String TAG = ConnectionStatus.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	static private final ConnectivityManager cm =
			(ConnectivityManager) CTApp.getCustomAppContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

	public ConnectionStatus(){
	}

	static public ConnectionState getConnectivityStatus(){
		logger.debug("STARTED CONNECTION STATUS");

		ConnectionState status = new ConnectionState();

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

		if(activeNetwork != null){
			status.isConnected = activeNetwork.isConnected();//activeNetwork.isConnectedOrConnecting();
			status.connectionType = activeNetwork.getType();//GlobalConstants.CONNECTIVITY_NONE;
			status.descriptiveText = "Active network available.";

			if(!status.isConnected){
				status.descriptiveText = "Active network available, not connected.";
			}
			else{
				boolean isWiFi = status.connectionType == ConnectivityManager.TYPE_WIFI;
				boolean isCell = status.connectionType == ConnectivityManager.TYPE_MOBILE;

				if(isWiFi){
					WifiManager wifiMgr = (WifiManager) CTApp.getCustomAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
					status.signalStrength = wifiMgr.getConnectionInfo().getRssi();
					status.descriptiveText = "Active network available, connected to WIFI, signal=" + status.signalStrength + ".";

					if(status.signalStrength >= GlobalConstants.MIN_WIFI_SIGNAL){
						status.descriptiveText += " Signal is above minimum of " + GlobalConstants.MIN_WIFI_SIGNAL + ".";
					}
					else{
						status.descriptiveText += " Signal is below minimum of " + GlobalConstants.MIN_WIFI_SIGNAL + ".";
					}
				}
				else if(isCell){
					//=== Check Permissions Now
					if(ActivityCompat.checkSelfPermission(CTApp.getCustomAppContext().getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
					   != PackageManager.PERMISSION_GRANTED){
						status.descriptiveText = "Active network available, connected to CELL, no read phone state, signal=" + status.signalStrength + ".";
					}
					else{
						//=== permission has been granted, continue as usual
						TelephonyManager telephonyManager = (TelephonyManager) CTApp.appContext.getSystemService(Context.TELEPHONY_SERVICE);

						Object cellInfoObject = null;
						try{
							cellInfoObject = telephonyManager.getAllCellInfo().get(0);
						}
						catch(Exception e){
							//=== crashlytics reported error, so the next code has an object to work with
							cellInfoObject = new Object();
						}
						if(cellInfoObject instanceof CellInfoGsm){
							status.signalStrength = ((CellInfoGsm) cellInfoObject).getCellSignalStrength().getDbm();
							status.descriptiveText = "Active network available, connected to CELL GSM, read phone state granted, signal=" + status.signalStrength + ".";
						}
						else if(cellInfoObject instanceof CellInfoCdma){
							status.signalStrength = ((CellInfoCdma) cellInfoObject).getCellSignalStrength().getDbm();
							status.descriptiveText = "Active network available, connected to CELL CDMA, read phone state granted, signal=" + status.signalStrength + ".";
						}
						else if(cellInfoObject instanceof CellInfoLte){
							status.signalStrength = ((CellInfoLte) cellInfoObject).getCellSignalStrength().getDbm();
							status.descriptiveText = "Active network available, connected to CELL LTE, read phone state granted, signal=" + status.signalStrength + ".";
						}
						else if(cellInfoObject instanceof CellInfoWcdma){
							status.signalStrength = ((CellInfoWcdma) cellInfoObject).getCellSignalStrength().getDbm();
							status.descriptiveText = "Active network available, connected to CELL WCDMA, read phone state granted, signal=" + status.signalStrength + ".";
						}
						else{
							status.signalStrength = GlobalConstants.CONNECTIVITY_SIGNAL_STRENGTH_ZERO;
							status.descriptiveText = "Active network available, connected to CELL UNKNOWN TYPE, read phone state granted, signal=" + status.signalStrength + ".";
						}

						if(status.signalStrength >= GlobalConstants.MIN_CELL_SIGNAL){
							status.descriptiveText += " Signal is above minimum of " + GlobalConstants.MIN_CELL_SIGNAL + ".";
						}
						else{
							status.descriptiveText += " Signal is below minimum of " + GlobalConstants.MIN_CELL_SIGNAL + ".";
						}
					}
				}
				else{
					status.descriptiveText += " UNKNOWN connection type, not WIFI or CELL.";
				}
			}

		}

		logger.debug(status.descriptiveText);
		//logger.debug("ENDED CONNECTION STATUS");
		return status;
	}

	static public class ConnectionState{
		public boolean isConnected = false;
		public int connectionType = GlobalConstants.CONNECTIVITY_NONE;
		public int signalStrength = GlobalConstants.CONNECTIVITY_SIGNAL_STRENGTH_ZERO;
		public String descriptiveText = "No active Network.";

		public ConnectionState(){
		}
	}

}
