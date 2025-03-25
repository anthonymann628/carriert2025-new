package com.agilegeodata.carriertrack.android.database;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.objects.BoundingPointMatrix;
import com.agilegeodata.carriertrack.android.objects.DeliveryItem;
import com.agilegeodata.carriertrack.android.objects.DeliveryItemProduct;
import com.agilegeodata.carriertrack.android.objects.ItemValue;
import com.agilegeodata.carriertrack.android.objects.PhotoDetail;
import com.agilegeodata.carriertrack.android.objects.Route;
import com.agilegeodata.carriertrack.android.objects.StreetSummaryRandom;
import com.agilegeodata.carriertrack.android.objects.UploadLog;
import com.agilegeodata.carriertrack.android.utils.DateUtil;
import com.agilegeodata.carriertrack.android.utils.FileUtils;
import com.agilegeodata.carriertrack.android.utils.GPSUtils;
import com.here.sdk.core.GeoCoordinates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class DBHelper extends SQLiteOpenHelper{
	public static final String DB_T_ADDRESSDETAILLIST = "addressdetaillist";
	public static final String DB_T_ADDRESSDETAILPRODUCTS = "addressdetailproducts";
	public static final String DB_T_ROUTELIST = "routelist";
	public static final String DB_T_ROUTELISTACTIVITY = "routelistactivity";
	public static final String DB_T_STREETSUMMARYLIST = "streetsummarylist";
	public static final String DB_T_PHOTOS = "photos";
	public static final String DB_T_SIGNATURES = "signatures";
	public static final String DB_T_BREADCRUMBS = "breadcrumbs";
	public static final String DB_T_UPLOADLOG = "uploadlog";
	public static final String DB_T_ITEMVALUES = "itemvalues";
	public static final String DB_T_LOGINS = "logins";
	public static final String DB_T_WORKACTIVITY = "workactivity";
	public static final String KEY_QTY = "qty";
	public static final String KEY_ITEMNAME = "itemname";
	public static final String KEY_LATDELIVERED = "latdelivered";
	public static final String KEY_LONGDELIVERED = "longdelivered";
	public static final String KEY_DELIVERY_LATITUDE = "deliverylatitude";
	public static final String KEY_DELIVERY_LONGITUDE = "deliverylongitude";
	public static final String KEY_DNDWASPROCESSED = "dndwasprocessed";
	public static final String KEY_LISTDISPLAYTIME = "listdisplaytime";
	public static final String KEY_LATNEW = "latnew";
	public static final String KEY_DElINFOPLACEMENT = "delinfoplacement";
	public static final String KEY_CUSTID = "custid";
	public static final String KEY_SEQUENCE = "sequence";
	public static final String KEY_LONGNEW = "longnew";
	public static final String KEY_JOBTYPE = "jobtype";
	public static final String KEY_SEQUENCENEW = "sequencenew";
	public static final String KEY_SEQMODENEW = "seqmodenew";
	public static final String KEY_PHOTOTAKEN = "phototaken";
	public static final String KEY_SIGNATURETAKEN = "signaturetaken";
	public static final String KEY_DELIVERYID = "deliveryid";
	public static final String KEY_SCANCODE = "scancode";
	public static final String KEY_PHOTOREQUIRED = "photorequired";
	public static final String KEY_STATUSUPDATED = "statusupdated";
	public static final String KEY_DELIVERYDATE = "deliverydate";
	public static final String KEY_STATUSCURRENT = "statuscurrent";
	public static final String KEY_NUMRECOVERED = "numrecovered";
	public static final String KEY_VERIFIED = "verified";
	public static final String KEY_VERIFYDATE = "verifydate";
	public static final String KEY_ID = "_id";
	public static final String KEY_JOBDETAILID = "jobdetailid";
	public static final String KEY_ROUTEID = "routeid";
	public static final String KEY_JOBID = "jobid";
	public static final String KEY_DELINFOSTATUS = "delinfostatus";
	public static final String KEY_PROJECTED = "projected";
	public static final String KEY_DOWNLOADED = "downloaded";
	public static final String KEY_LOOKAHEADFORWARD = "lookaheadforward";
	public static final String KEY_LOOKAHEADSIDE = "lookaheadside";
	public static final String KEY_DELIVERYFORWARD = "deliveryforward";
	public static final String KEY_DELIVERYSIDE = "deliveryside";
	public static final String KEY_INTERFACETYPE = "interfacetype";
	public static final String KEY_DATEVALIDFROM = "datevalidfrom";
	public static final String KEY_DATEVALIDTO = "datevalidto";
	public static final String KEY_LASTSTARTDATE = "laststartdate";
	public static final String KEY_LASTENDDATE = "lastenddate";
	public static final String KEY_STARTDATE = "startdate";
	public static final String KEY_ENDDATE = "enddate";
	public static final String KEY_UPLOADED = "uploaded";
	public static final String KEY_UPLOADBATCHID = "uploadbatchid";
	public static final String KEY_SUMMARYID = "summaryid";
	public static final String KEY_STREETNAME = "streetname";
	public static final String KEY_RESOLVEDADDRESS = "resolvedaddress";
	public static final String KEY_SEARCHADDRESS = "searchaddress";
	public static final String KEY_LAT = "lat";
	public static final String KEY_LONG = "long";
	public static final String KEY_DELIVERYLATITUDE = "deliverylatitude";
	public static final String KEY_DELIVERYLONGITUDE = "deliverylongitude";
	public static final String KEY_WASRECONCILED = "wasreconciled";
	public static final String KEY_PHOTONOTES = "photonotes";
	public static final String KEY_PHOTODATE = "photodate";
	public static final String KEY_FILEPATH = "filepath";
	public static final String KEY_RESOLUTION = "resolution";
	public static final String KEY_DIRECTION = "direction";
	public static final String KEY_SPEED = "speed";
	public static final String KEY_DELIVERYMODE = "deliverymode";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_TIMESTAMPLOCAL = "timestamplocal";
	public static final String KEY_STATUS = "status";
	public static final String KEY_UPLOADTYPE = "uploadtype";
	public static final String KEY_UPLOADDATATYPE = "uploaddatatype";
	public static final String KEY_NUMRECORDSSENT = "numrecordssent";
	public static final String KEY_NUMRECORDSCONF = "numrecordsconf";
	public static final String KEY_NUMFILESSENT = "numfilessent";
	public static final String KEY_NUMFILESCONF = "numfilesconf";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_ADDRESS_NUMBER = "addressnumber";
	public static final String KEY_NUMNEWADDRESSESSENT = "numnewaddressessent";
	public static final String KEY_NUMNEWADDRESSESCONF = "numnewaddressesconf";
	public static final String KEY_LOGINDATE = "logindate";
	public static final String KEY_STREETADDRESS = "streetaddress";
	public static final String KEY_NOTES = "notes";
	public static final String KEY_CUSTSVC = "custsvc";
	public static final String KEY_NUMDELIVERED = "numdelivered";
	public static final String KEY_DELIVERED = "delivered";
	public static final String KEY_QUANTITY = "qty";
	public static final String KEY_PRODUCTTYPE = "producttype";
	public static final String KEY_PRODUCTCODE = "productcode";
	public static final String KEY_DELETED = "deleted";
	public static final String KEY_ROUTETYPE = "routetype";
	public static final String KEY_ROUTEFINISHED = "routefinished";
	public static final String KEY_TTLADDRESSES = "ttladdresses";
	public static final String KEY_TTLREMAINING = "ttlremaining";
	public static final String KEY_CARRIER_TYPE = "carriertype";
	public static final String KEY_WORK_ACTIVITY_DESCRIPTION = "description";
	public static final String KEY_WORK_ACTIVITY_ID = "activityid";
	public static final String KEY_WORK_ACTIVITY_DATE = "date";
	public static final int MAX_DB_RETURN = 100000;
	public static final int MAX_DB_RETURN_UPLOAD = 10000;
	private static final String TAG = DBHelper.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	private static final String QUERY_NUM_PHOTOS_REQUESTED_BY_JDI = "select count(*) from addressdetaillist "
																	+ "where jobdetailid = ";
	private static final String QUERY_NUM_PHOTOS_REQUESTED_BY_JDI_2 = " and photorequired=1";
	private static final String QUERY_NUM_PHOTOS_UPLOADED_BY_JDI = "select count(*) from photos "
																   + " where  uploaded=2 AND jobdetailid = ";
	private static final String QUERY_NUM_PHOTOS_TAKEN_BY_JDI = "select count(*) from photos "
																+ " where  jobdetailid = ";
	private static final String QUERY_NUM_ADDRESSES_BY_JOBDETAILID = "select jobdetailid, count(*) as count from addressdetaillist "
																	 + "where jobdetailid = " + "%jdid%";
	private static final String QUERY_NUM_DELIVEREDADDRESSES_BY_JOBDETAILID = "select count(*) as count from addressdetaillist "
																			  + "where jobdetailid = " + "%jdid%" + " and delivered = 1 group by jobdetailid";
	private static final String QUERY_NUM_PHOTOS_BY_JOBDETAILID = "select jobdetailid, count(*) as count from photos "
																  + "where jobdetailid = " + "%jdid%";
	private static final String QUERY_NUM_UPLOADEDPHOTOS_BY_JOBDETAILID = "select jobdetailid, count(*) as count from photos "
																		  + "where uploaded=2 and jobdetailid = %jdid%";
	//=== The Android's default system path of your application database.
	public static String DB_PATH_DBFILE = null;
	public static String DB_PATH = null;
	private static DBHelper mInstance;
	protected final Context myContext;
	private long lastMissedDropCheck = System.currentTimeMillis();
	private int SEQUENCE_ID_DIVISOR = 1;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 */
	public DBHelper(Context context){
		super(context, GlobalConstants.DATABASE_NAME, null, CTApp.DATABASE_VERSION);

		this.myContext = context;
	}

	public static synchronized DBHelper getInstance(){
		Context context = CTApp.appContext;

		if(DB_PATH == null){
			DB_PATH = "/data/data/" + context.getApplicationContext().getPackageName() + "/databases";
			DB_PATH_DBFILE = DB_PATH + "/" + GlobalConstants.DATABASE_NAME;
		}

		if(mInstance == null){
			mInstance = new DBHelper(context.getApplicationContext());
		}

		return mInstance;
	}

	@Override
	public synchronized void close(){
		if(getWritableDatabase() != null){
			//db.disableWriteAheadLogging();
			getWritableDatabase().close();
		}

		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		try{
			//=== Open your local db create sql input stream
			InputStream inputStream = this.myContext.getAssets().open("database_creation.sql");

			byte[] buffer = new byte[inputStream.available()];
			String queries = "";
			try{
				inputStream.read(buffer);
			}
			catch(IOException e){
				logger.debug("EXCEPTION : " + e.getMessage());
			}

			inputStream.close();
			queries = new String(buffer);

			for(String query : queries.split(";")){
				logger.debug("EXECUTE SQL STATEMENT : " + query);
				db.execSQL(query);
			}

			db.setVersion(CTApp.DATABASE_VERSION);
		}
		catch(Exception e){
			logger.debug(">>>>copyDataBase() : EXCEPTION : " + e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		logger.debug("Upgrading the db. oldVersion = " + oldVersion + " newVersion = " + newVersion);

		//=== DB WAY TO OLD TO DEAL WITH SO RECREATE
		if(oldVersion < 7027){
			//=== PUT UP AN ALERT AND GIVE THE USER THE OPTION TO QUIT
			//=== AS THIS WILL NOT SAVE EXISTING DATA
			try{
				//=== DROP ALL TABLES, INDICES WILL GO WITH THE TABLE
				InputStream inputStream = this.myContext.getAssets().open("database_reset.sql");

				byte[] buffer = new byte[inputStream.available()];
				String queries = "";
				try{
					inputStream.read(buffer);
				}
				catch(IOException e){
					logger.debug("EXCEPTION : " + e.getMessage());
				}

				inputStream.close();
				queries = new String(buffer);

				for(String query : queries.split(";")){
					logger.debug("EXECUTE SQL db update initiate STATEMENT : " + query);
					db.execSQL(query);
				}

				//=== NOW CALL onCreate TO CREATE THE DB FOR VERSION 7 OF THIS APP
				onCreate(db);
			}
			catch(Exception e){
				logger.debug(">>>>onUpgrade() : EXCEPTION : " + e.getMessage());
			}
		}
		else if(oldVersion < CTApp.DATABASE_VERSION){
			//=== LOOK IN ASSETS FOR UPGRADE SQL FILES. SHOULD BE NAMED LIKE
			//=== 7027_to_7030. dbupgrade after the app version in which it was changed
			logger.debug("updating DATABASE");

			// You will not need to modify this unless you need to do some android specific things.
			// When upgrading the database, all you need to do is add a file to the assets folder and name it:
			// 7027_to_7030.dbupdate, 7030_to_7032.dbupdate, 7032_to_7040.dbupdate etc. with the version
			// that you are upgrading to as the last version.
			try{
				AssetManager assetManager = this.myContext.getAssets();

				int targetNewVersion = oldVersion + 1;
				for(int targetOldVersion = oldVersion; targetNewVersion <= newVersion; ++targetNewVersion){
					String migrationName = String.format("%d_to_%d.dbupdate", targetOldVersion, targetNewVersion);
					logger.debug("Looking for migration file: " + migrationName);

					try{
						assetManager.open(migrationName);
					}
					catch(IOException e){
						logger.debug("no SQL script file for " + migrationName);
						continue;
					}

					readAndExecuteSQLScript(db, this.myContext, migrationName);

					targetOldVersion = targetNewVersion;
				}

				db.setVersion(newVersion);
			}
			catch(Exception exception){
				logger.error("Exception running upgrade script:", exception.getMessage());
			}

		}
	}

	private boolean readAndExecuteSQLScript(SQLiteDatabase db, Context ctx, String fileName){
		if(TextUtils.isEmpty(fileName)){
			logger.debug("SQL script file name is empty");
			return false;
		}

		logger.debug("Script found. Executing...");
		AssetManager assetManager = ctx.getAssets();
		BufferedReader reader = null;

		try{
			InputStream is = assetManager.open(fileName);
			InputStreamReader isr = new InputStreamReader(is);
			reader = new BufferedReader(isr);
			executeSQLScript(db, reader);
		}
		catch(IOException e){
			logger.error("IOException:", e);
		}
		finally{
			if(reader != null){
				try{
					reader.close();
				}
				catch(IOException e){
					logger.error("IOException:", e);
				}
			}
		}

		return true;
	}

	private void executeSQLScript(SQLiteDatabase db, BufferedReader reader) throws IOException{
		String line;
		StringBuilder statement = new StringBuilder();

		while((line = reader.readLine()) != null){
			statement.append(line);
			statement.append("\n");

			if(line.endsWith(";")){
				db.execSQL(statement.toString());
				statement = new StringBuilder();
			}
		}
	}

	public final Route fetchRouteForSearch(int jobDetailId, boolean loadSummaries,
										   int mDeliveryMode, String searchFilter,
										   HashMap<Integer, StreetSummaryRandom> map){
		//Log.d(TAG,, "Entry fetchRoute");
		Cursor cur = null;
		Route r = new Route();

		Cursor cur1 = null;
		try{
			//=== GET THE ROUTE
			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_ROUTELIST, DBHelper.KEY_JOBDETAILID, Integer.valueOf(jobDetailId).toString(),
					DBHelper.KEY_ROUTEID, "asc", 1000);

			while(cur.moveToNext()){
				r = new Route(cur);
			}

			if(loadSummaries){
				//Log.d(TAG,, "Loading summaries");
				if(map.size() < 1){
					map = fetchStreetSummariesByJobDetailId_Random(r.getJobDetailId());
				}

				//Log.d(TAG,"Search Summary Size: " + map.size());
				ArrayList<StreetSummaryRandom> ss = new ArrayList<StreetSummaryRandom>();
				if(searchFilter == null){
					//Log.d(TAG,, "search filter is null;");
					//Log.d(TAG,"Search Filter is Null");
					cur1 = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
							DBHelper.DB_T_ADDRESSDETAILLIST,
							DBHelper.KEY_JOBDETAILID, "" + r.getJobDetailId(),
							DBHelper.KEY_JOBDETAILID, "asc", MAX_DB_RETURN);
				}
				else{
					//Log.d(TAG,, "search filter is not null;");
					//Log.d(TAG,"Search Filter is not null");
					final String[] fieldNames = {DBHelper.KEY_JOBDETAILID,
												 DBHelper.KEY_SEARCHADDRESS};
					final boolean[] isWildCard = {false, true};
					String[] parameters = {
							Integer.valueOf(r.getJobDetailId()).toString(),
							searchFilter};

					cur1 = fetchAllFromTableByFieldNamesAndParametersInOrderWildCard_Random(
							DBHelper.DB_T_ADDRESSDETAILLIST, fieldNames,
							parameters, isWildCard, DBHelper.KEY_JOBDETAILID,
							"asc", MAX_DB_RETURN);
				}

				if(cur1 != null){
					//Log.d(TAG,, "Cur1 has " + cur1.getCount());
					HashMap<Integer, ArrayList<DeliveryItem>> rds = createRouteDetailListBySummary_Random(cur1);
					Set<Integer> set = rds.keySet();
					Iterator<Integer> it = set.iterator();
					while(it.hasNext()){
						Integer key = it.next();
						//Log.d(TAG,								" StreetSummary Looking in the map for a key: "										+ key);
						if(map.containsKey(key)){
							StreetSummaryRandom s = map.get(key);
							s.getRouteDetails().clear();
							s.getRouteDetails().addAll(rds.get(key));
							ss.add(s);
						}

						r.setStreetSummaries(ss);
					}
				}
				else{
					//Log.d(TAG,, "Cur1 is null");
				}
			}

		}
		catch(SQLException e){
			//Log.d(TAG, "" + e.getMessage());

		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}

			if(cur1 != null && !cur1.isClosed()){
				cur1.close();
			}

		}

		return r;
	}

	public HashMap<Integer, Integer> fetchNumPhotosByJobDetailId_Common(int numPhotos){
		int count = 0;
		Integer key = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		Cursor cur = null;

		try{
			//logger.debug(TAG,, QUERY_NUM_PHOTOS_BY_JOB);
			cur = getWritableDatabase().rawQuery(QUERY_NUM_PHOTOS_BY_JOBDETAILID.replace("%jdid%", numPhotos + ""), null);

			while(cur.moveToNext()){
				key = cur.getInt(0);
				count = cur.getInt(1);
				map.put(key, count);
				// logger.debug("KEY: " + key + "  COUNT: " + count);
			}

		}
		catch(Exception e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return map;
	}

	public HashMap<Integer, Integer> fetchNumDeliveredAddressesByJobDetailId_Common(int jobDetailId){
		int count = 0;
		//logger.debug("Entry: fetchNumDeliveredAddressesGroupedByJob()" );
		Integer key = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		Cursor cur = null;

		try{
			//logger.debug(TAG,, QUERY_NUM_DELIVEREDADDRESSES);
			cur = getWritableDatabase().rawQuery(QUERY_NUM_DELIVEREDADDRESSES_BY_JOBDETAILID.replace("%jdid%", jobDetailId + ""), null);

			//===SHOULD ONLY BE ONE ROW, MULTIPLE ROWS WILL RETURN LAST ROW COUNT
			while(cur.moveToNext()){
				key = jobDetailId;
				count = cur.getInt(0);
				//logger.debug("KEY: " + key +"  COUNT: " + count);

				map.put(key, count);
			}
		}
		catch(Exception e){
			logger.error("EXCEPTION", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return map;
	}

	public HashMap<Integer, Integer> fetchNumUploadedPhotosByJobDetailId_Common(int jobDetailId){
		int count = 0;
		//logger.debug("Entry:  fetchNumUploadedPhotosGroupedByJob()");
		Integer key = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		Cursor cur = null;

		try{
			//logger.debug(TAG,, QUERY_NUM_UPLOADEDPHOTOS_BY_JOB);
			String query = QUERY_NUM_UPLOADEDPHOTOS_BY_JOBDETAILID.replace("%jdid%", jobDetailId + "");
			cur = getWritableDatabase().rawQuery(query, null);

			while(cur.moveToNext()){
				key = cur.getInt(0);
				count = cur.getInt(1);
				map.put(key, count);
			}

		}
		catch(Exception e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return map;
	}

	public boolean doExecQuery_Common(String query){
		//logger.info(">>>>doExecQuery() : START");

		try{
			//logger.debug(">>>>doExecQuery() : QUERY : \"" + query + "\"");
			getWritableDatabase().execSQL(query);
		}
		catch(Exception e){
			logger.debug(">>>>doExecQuery() : QUERY : \"" + query + "\"");
			logger.error("EXCEPTION ", e);
			return false;
		}

		return true;
	}

	public final String fetchAllUploadLogsForUpload_Common(){
		// logger.debug("Entry: fetchAllUploadLogsForUpload");
		Cursor cur = null;
		StringBuffer buf = new StringBuffer();
		buf.append(DBHelper.KEY_ID + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_STATUS
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_UPLOADTYPE
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_UPLOADDATATYPE
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_NUMRECORDSCONF
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_NUMRECORDSSENT
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_NUMFILESCONF
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_NUMFILESSENT
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_NUMNEWADDRESSESCONF
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_NUMNEWADDRESSESSENT
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_ADDRESS
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_LATITUDE
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_LONGITUDE
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_TIMESTAMP
				   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
				   + DBHelper.KEY_TIMESTAMPLOCAL);

		buf.append(System.getProperty("line.separator"));
		int count = 0;

		try{
			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_UPLOADLOG, DBHelper.KEY_STATUS, "0",
					DBHelper.KEY_ID, "desc", MAX_DB_RETURN_UPLOAD);

			if(cur != null){
				count = cur.getCount();
				//logger.debug("CURSOR COUNT: " + count);

				while(cur.moveToNext()){
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_STATUS))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADTYPE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADDATATYPE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMRECORDSCONF))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMRECORDSSENT))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMFILESCONF))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMFILESSENT))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMNEWADDRESSESCONF))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMNEWADDRESSESSENT))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					String addr = cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_ADDRESS));
					//logger.debug("Address: " + addr);
					buf.append(addr + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LATITUDE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONGITUDE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					long timestampD = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_TIMESTAMP));

					String formattedTS = "";
					formattedTS = DateUtil.calcDateFromTime(timestampD, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
					buf.append(formattedTS + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					long timestampL = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_TIMESTAMPLOCAL));
					formattedTS = DateUtil.calcDateFromTime(timestampL, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
					buf.append(formattedTS);

					buf.append(System.getProperty("line.separator"));
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		buf.append(GlobalConstants.UPLOAD_COUNT_DELIMINATOR + count);
		return buf.toString();
	}

	public int fetchCountPhotosRequested_Common(int jDID){
		int count = fetchCountByQuery_Common(QUERY_NUM_PHOTOS_REQUESTED_BY_JDI + jDID
											 + QUERY_NUM_PHOTOS_REQUESTED_BY_JDI_2);
		// logger.debug("Photos Requested: " + count);
		return count;
	}

	public int fetchCountPhotosTaken_Common(int jDID){
		int count = fetchCountByQuery_Common(QUERY_NUM_PHOTOS_TAKEN_BY_JDI + jDID);
		// logger.debug("Photos Taken: " + count);
		return count;
	}

	public int fetchCountPhotosUploaded_Common(int jDID){
		int count = fetchCountByQuery_Common(QUERY_NUM_PHOTOS_UPLOADED_BY_JDI + jDID);
		// logger.debug("Photos Uploaded: " + count);
		return count;
	}

	public final String fetchAllLoginsForUpload_Common(){
		//logger.debug("Entry: fetchAllLoginsForUpload");

		Cursor cur = null;
		int count = 0;
		StringBuffer buf = new StringBuffer();

		updateTableUploadBatchId_Common(DBHelper.DB_T_LOGINS);

		try{
			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_LOGINS, DBHelper.KEY_UPLOADED, "0",
					DBHelper.KEY_ID, "desc", MAX_DB_RETURN_UPLOAD);

			if(cur != null){
				count = cur.getCount();
				buf.append(DBHelper.KEY_ID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_LOGINDATE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_STATUS
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_UPLOADBATCHID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_UPLOADED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(System.getProperty("line.separator"));

				while(cur.moveToNext()){
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					long timestampL = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_LOGINDATE));

					String formattedTS = "";

					if(timestampL != 0){
						formattedTS = DateUtil.calcDateFromTime(timestampL, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
					}

					buf.append(formattedTS + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_STATUS))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADBATCHID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADED)));
					buf.append(System.getProperty("line.separator"));
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		buf.append(GlobalConstants.UPLOAD_COUNT_DELIMINATOR + count);

		return buf.toString();
	}

	public final ArrayList<String> fetchAllPhotosForSyncing_Common(){
		ArrayList<String> list = new ArrayList<String>();
		// logger.debug("Entry: fetchAllPhotosForSyncing");
		Cursor cur = null;

		try{
			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_PHOTOS, DBHelper.KEY_UPLOADED, "1",
					DBHelper.KEY_ID, "desc", MAX_DB_RETURN_UPLOAD);
			logger.debug("Count of photo files for upload: " + cur.getCount());

			if(cur != null){
				while(cur.moveToNext()){
					list.add(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_FILEPATH)));
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return list;
	}

	public final String fetchAllPhotosForUpload_Common(){
		//logger.debug("Entry: fetchAllPhotosForUpload");
		Cursor cur = null;
		StringBuffer buf = new StringBuffer();
		int count = 0;

		try{
			updateTableUploadBatchId_Common(DBHelper.DB_T_PHOTOS);

			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_PHOTOS, DBHelper.KEY_UPLOADED, "0",
					DBHelper.KEY_ID, "desc", MAX_DB_RETURN_UPLOAD);

			if(cur != null){
				count = cur.getCount();
				buf.append(DBHelper.KEY_ID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_LAT
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_LONG
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_PHOTODATE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_PHOTONOTES
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_FILEPATH
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_DELIVERYID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_JOBDETAILID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_UPLOADBATCHID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_UPLOADED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(System.getProperty("line.separator"));

				while(cur.moveToNext()){
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					long timestamp = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_PHOTODATE));
					String formattedTS = "";
					formattedTS = DateUtil.calcDateFromTime(timestamp, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);

					buf.append(formattedTS + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					String photonotes = cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_PHOTONOTES));
					String returnStr = System.getProperty("line.separator");

					if(photonotes != null && photonotes.length() > 0){
						photonotes = photonotes.replace(returnStr, " ");
						photonotes = photonotes.replace("\r", " ");
					}
					// logger.debug("PhotoNotes: " + photonotes);

					buf.append(photonotes
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_FILEPATH))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADBATCHID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADED)));
					buf.append(System.getProperty("line.separator"));
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		buf.append(GlobalConstants.UPLOAD_COUNT_DELIMINATOR + count);
		return buf.toString();
	}

	public final String fetchAllSignaturesForUpload_Common(){
		//logger.debug("Entry: fetchAllSignaturesForUpload_Common");
		Cursor cur = null;
		StringBuffer buf = new StringBuffer();
		int count = 0;

		try{
			updateTableUploadBatchId_Common(DBHelper.DB_T_SIGNATURES);

			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_SIGNATURES, DBHelper.KEY_UPLOADED, "0",
					DBHelper.KEY_ID, "desc", MAX_DB_RETURN_UPLOAD);

			if(cur != null){
				count = cur.getCount();
				buf.append(DBHelper.KEY_ID
//						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
//						   + DBHelper.KEY_SIGNATUREDATE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_DELIVERYID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_JOBDETAILID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_LAT
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_LONG
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_FILEPATH
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_UPLOADED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_UPLOADBATCHID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						  );
				buf.append(System.getProperty("line.separator"));

				while(cur.moveToNext()){
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_FILEPATH))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADED)));
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADBATCHID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(System.getProperty("line.separator"));

//					long timestamp = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_PHOTODATE));
//					String formattedTS = "";
//					formattedTS = DateUtil.calcDateFromTime(timestamp, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
//
//					buf.append(formattedTS + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
//
//					String photonotes = cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_PHOTONOTES));
//					String returnStr = System.getProperty("line.separator");
//
//					if(photonotes != null && photonotes.length() > 0){
//						photonotes = photonotes.replace(returnStr, " ");
//						photonotes = photonotes.replace("\r", " ");
//					}
//					// logger.debug("PhotoNotes: " + photonotes);
//
//					buf.append(photonotes
//							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		buf.append(GlobalConstants.UPLOAD_COUNT_DELIMINATOR + count);
		return buf.toString();
	}

	public final ArrayList<String> fetchAllSignaturesForSyncing_Common(){
		ArrayList<String> list = new ArrayList<String>();
		// logger.debug("Entry: fetchAllPhotosForSyncing");
		Cursor cur = null;

		try{
			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_SIGNATURES, DBHelper.KEY_UPLOADED, "1",
					DBHelper.KEY_ID, "desc", MAX_DB_RETURN_UPLOAD);
			logger.debug("Count of signature files for upload: " + cur.getCount());

			if(cur != null){
				while(cur.moveToNext()){
					list.add(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_FILEPATH)));
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return list;
	}

	public final String fetchAllRouteListActivityForUpload_Common(){
		//logger.debug("Entry: fetchAllRouteListActivityForUpload");

		Cursor cur = null;
		int count = 0;
		StringBuffer buf = new StringBuffer();

		try{

			updateTableUploadBatchId_Common(DBHelper.DB_T_ROUTELISTACTIVITY);

			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_ROUTELISTACTIVITY, DBHelper.KEY_UPLOADED,
					"0", DBHelper.KEY_ID, "desc", MAX_DB_RETURN_UPLOAD);

			if(cur != null){
				count = cur.getCount();
				buf.append(DBHelper.KEY_ID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_STARTDATE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_ENDDATE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_JOBDETAILID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_UPLOADBATCHID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_UPLOADED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_ROUTETYPE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_ROUTEFINISHED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_TTLADDRESSES
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_TTLREMAINING);

				buf.append(System.getProperty("line.separator"));

				while(cur.moveToNext()){
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					long timestampSD = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_STARTDATE));
					String formattedTS = DateUtil.calcDateFromTime(timestampSD, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
					buf.append(formattedTS + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					long timestampED = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_ENDDATE));
					formattedTS = "";

					if(timestampED != 0){
						formattedTS = DateUtil.calcDateFromTime(timestampED, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
					}

					buf.append(formattedTS + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADBATCHID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ROUTETYPE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ROUTEFINISHED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_TTLADDRESSES))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_TTLREMAINING)));

					buf.append(System.getProperty("line.separator"));
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		buf.append(GlobalConstants.UPLOAD_COUNT_DELIMINATOR + count);

		return buf.toString();
	}

	public final String fetchAllAddressDetailsForUpload_Common(){
		logger.debug("Entry: fetchAllAddressDetailsForUpload");
		Cursor cur = null;
		int count = 0;
		StringBuffer buf = new StringBuffer();
		logger.debug("--->operationsMode = " + CTApp.operationsMode.toString());

		try{

			updateTableUploadBatchId_Common(DBHelper.DB_T_ADDRESSDETAILLIST);

			logger.debug("CTApp.operationsMode = " + CTApp.operationsMode);

			cur = this.fetchAllAddressesForUpload_Common();

			if(cur != null){
				buf.append(DBHelper.KEY_ID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_DELIVERYID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_QTY
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_LATDELIVERED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_LONGDELIVERED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_LATNEW
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_LONGNEW
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_SEQUENCE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_SEQUENCENEW
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_SEQMODENEW
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_PHOTOTAKEN
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_DELIVERED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_JOBDETAILID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_DELIVERYDATE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_STATUSUPDATED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_STATUSCURRENT
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_NUMRECOVERED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_NUMDELIVERED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_VERIFIED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_VERIFYDATE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_UPLOADBATCHID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_DELINFOSTATUS
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_DElINFOPLACEMENT
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_CUSTID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   + DBHelper.KEY_WASRECONCILED
						  );
				buf.append(System.getProperty("line.separator"));

				count = cur.getCount();
				while(cur.moveToNext()){
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_QTY))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LATDELIVERED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONGDELIVERED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LATNEW))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONGNEW))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_SEQUENCE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_SEQUENCENEW))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_SEQMODENEW))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_PHOTOTAKEN))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					long timestampD = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYDATE));

					String formattedTS2 = "";
					formattedTS2 = DateUtil.calcDateFromTime(timestampD,
															 GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);

					if(formattedTS2.length() > 1){
						buf.append(formattedTS2);
						buf.append(GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					}
					else{
						buf.append("0");
						buf.append(GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					}

					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_STATUSUPDATED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_STATUSCURRENT))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMRECOVERED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMDELIVERED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_VERIFIED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					String formattedTS3 = "";
					long timestampV = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_VERIFYDATE));

					if(timestampV != 0){
						formattedTS3 = DateUtil.calcDateFromTime(timestampV, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);

					}

					if(formattedTS3.length() > 1){
						buf.append(formattedTS3);
						buf.append(GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					}
					else{
						buf.append("0");
						buf.append(GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					}

					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADBATCHID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_DELINFOSTATUS))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_DElINFOPLACEMENT))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_CUSTID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_WASRECONCILED)));

					buf.append(System.getProperty("line.separator"));
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		buf.append(GlobalConstants.UPLOAD_COUNT_DELIMINATOR + count);

		return buf.toString();
	}

	public final ArrayList<UploadLog> fetchAllUploadLogsByStatus_Common(
			String status, int limit){

		Cursor cur = null;
		ArrayList<UploadLog> rs = new ArrayList<UploadLog>();

		try{
			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_UPLOADLOG, DBHelper.KEY_STATUS, status,
					DBHelper.KEY_TIMESTAMP, "desc", limit);

			while(cur.moveToNext()){
				UploadLog r = new UploadLog();
				r.setNumFilesConf(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMFILESCONF)));
				r.setFilesSentCount(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMFILESSENT)));
				r.setNumNewAddressesConf(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMNEWADDRESSESCONF)));
				r.setNewAddressesToSendCount(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMNEWADDRESSESSENT)));
				r.setRecordsToSendCount(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMRECORDSSENT)));
				r.setNumRecordsConf(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMRECORDSCONF)));
				r.setStatus(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_STATUS)));
				r.setAddress(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_ADDRESS)));
				r.setLatitude(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LATITUDE)));
				r.setLongitude(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONGITUDE)));
				r.setTimestamp(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_TIMESTAMP)));
				r.setTimestampLocal(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_TIMESTAMPLOCAL)));
				r.setId(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID)));
				r.setUploadType(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADTYPE)));
				r.setUploadDataType(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADDATATYPE)));

				rs.add(r);
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return rs;
	}

	public final String fetchAllBreadcrumbsForUpload_Common(){
		Cursor cur = null;
		StringBuffer buf = new StringBuffer();
		int count = 0;

		try{

			updateTableUploadBatchId_Common(DBHelper.DB_T_BREADCRUMBS);

			buf.append(DBHelper.KEY_ID
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_DELIVERYMODE
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_DIRECTION
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_JOBDETAILID
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_LAT
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_LONG
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_RESOLUTION
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_SPEED
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_TIMESTAMP
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_TIMESTAMPLOCAL
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_UPLOADBATCHID
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_UPLOADED
					   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
					   + DBHelper.KEY_RESOLVEDADDRESS);
			buf.append(System.getProperty("line.separator"));

			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_BREADCRUMBS, DBHelper.KEY_UPLOADED, "0",
					DBHelper.KEY_TIMESTAMP, "asc", MAX_DB_RETURN_UPLOAD);
			count = cur.getCount();

			while(cur.moveToNext()){
				buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYMODE))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_DIRECTION))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_RESOLUTION))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_SPEED))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

				long timestamp = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_TIMESTAMP));
				String formattedTS = "";
				formattedTS = DateUtil.calcDateFromTime(timestamp,
														GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
				buf.append(formattedTS
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

				formattedTS = "";

				long timestampLocal = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_TIMESTAMPLOCAL));

				if(timestampLocal != 0){
					formattedTS = DateUtil.calcDateFromTime(timestampLocal,
															GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
				}

				buf.append(formattedTS
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

				buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADBATCHID))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADED))
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
				buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_RESOLVEDADDRESS)));
				buf.append(System.getProperty("line.separator"));
			}
		}
		catch(Exception e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		buf.append(GlobalConstants.UPLOAD_COUNT_DELIMINATOR + count);
		//logger.debug("Buffer is : " +buf);

		return buf.toString();
	}

	private void updateTableUploadBatchId_Common(String tableName){
		int key = fetchMaxIdFromTable_Common(DBHelper.DB_T_UPLOADLOG) + 1;

		String query = "update " + tableName + " set "
					   + DBHelper.KEY_UPLOADBATCHID + " =" + key + " where "
					   + DBHelper.KEY_UPLOADED + "=0";

		doExecQuery_Common(query);
	}

	public final ArrayList<Route> fetchAllRoutesByFilter_Common(String filter){
		ArrayList<Route> rs = new ArrayList<Route>();
		Cursor cur = null;

		try{
			//logger.debug("Searching for: "+ filter);
			cur = fetchAllFromTableByFieldNameAndParameterInOrderWildCard_Common(
					DBHelper.DB_T_ROUTELIST, DBHelper.KEY_ROUTEID,
					filter.toUpperCase(), DBHelper.KEY_JOBID, "asc");

			if(cur != null && cur.getCount() > 0){
				while(cur.moveToNext()){
					Route r = new Route(cur);

					Date currentDate = new Date();
					Long validToTime = Long.parseLong(r.getDateValidTo()) * 1000;
					Long currentTime = currentDate.getTime();

					if(validToTime > currentTime){
						rs.add(r);
					}
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return rs;
	}

	public HashMap<Integer, Integer> executeDatabaseUpdatesZip_Common(
			InputStream data, String saveDir){
		logger.debug("$$$$$$$ STARTED");
		int errorCode = GlobalConstants.ERROR_CODE_NONE;

		try{
			long datetime = Calendar.getInstance().getTimeInMillis();

			String fileName = saveDir + datetime + "_"
							  + GlobalConstants.DBUPDATE_FILENAME;
			FileUtils.copyFile(data, saveDir, datetime + "_"
											  + GlobalConstants.DBUPDATE_ZIPFILENAME);
			FileUtils.deflateContentSingleFile(saveDir, datetime + "_"
														+ GlobalConstants.DBUPDATE_ZIPFILENAME, datetime + "_"
																								+ GlobalConstants.DBUPDATE_FILENAME);

			long len = FileUtils.getFileSize(fileName);

			boolean isFirst = true;
			try{
				//=== Open the file that is the first command line parameter
				FileInputStream fstream = new FileInputStream(fileName);
				//=== Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				String userInput;

				//=== Read File Line By Line
				while((userInput = br.readLine()) != null){
					if(isFirst){
						//=== should look like this ^*^303690^*^
						String uI = userInput.substring(3,
														userInput.length() - 3);
						isFirst = false;
						int dataLength = Integer.valueOf(uI);
						logger.debug("first read on zip stream: data length = " + dataLength);

						if(dataLength != len){
							logger.error("Stream Len: " + len + " Reported Len: " + dataLength);
							logger.error("first read on zip stream: file size does not match content size");
							errorCode = GlobalConstants.ERROR_CODE_DOWNLOAD_BAD_RESPONSE;
							break;
						}
					}
					else{
						doExecQuery_Common(userInput);
					}
				}

				//=== Close the input stream
				in.close();
			}
			catch(Exception e){// Catch exception if any
				errorCode = GlobalConstants.ERROR_CODE_DOWNLOAD_DATABASE_LOADERROR;
				//logger.error("Error: " + e.getMessage());
			}
		}
		catch(Exception e){// Catch exception if any
			errorCode = GlobalConstants.ERROR_CODE_DOWNLOAD_DATABASE_LOADERROR;
			//logger.error("Error: " + e.getMessage());
		}

		if(errorCode == GlobalConstants.ERROR_CODE_NONE){
			return fetchRouteCountByJobDetailsId_Common();
		}
		else{
			return null;
		}
	}

	public int fetchCountFromTableByParameter(String tableName,
											  String fieldName, String parameter){
		int count = 0;
		Cursor cur = null;
		try{
			cur = getWritableDatabase().rawQuery("select count(*) as count from " + tableName
												 + " where " + fieldName + "='" + parameter + "'", null);
			while(cur.moveToNext()){
				count = cur.getInt(0);
			}

		}
		catch(SQLException e){
			logger.debug(TAG, "exception " + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return count;
	}

	/*
	 * filetype*:*breadcrumb*^*data*:*1,2,3,4,5,6,7,8
	 * filetype*:*uploadlog*^*data*:*1,2,3,4,5,6,7,8
	 * filetype*:*addressdetaillist*^*data*:*1,2,3,4,5,6,7,8
	 * filetype*:*routelistactivity*^*data*:*1,2,3,4,5,6,7,8
	 * filetype*:*photos*^*data*:*1,2,3,4,5,6,7,8
	 */
	public int loadUpdateVerifyRecords_Common(String dataVal, String fileType)
			throws IOException{
		//=== read in a line, determine what type it is....
		int preQueryRecordCount = 0;
		int postQueryRecordCount = 0;
		int confirmedCount = 0;

		try{
			if(dataVal != null && dataVal.length() > 0){
				String table = null;
				String column = null;
				String columnValue = null;

				if(fileType.contains(GlobalConstants.URLPARAM_FILETYPE_BREADCRUMB)){
					table = GlobalConstants.URLPARAM_FILETYPE_BREADCRUMB;
					column = DBHelper.KEY_UPLOADED;
					columnValue = "1";

					//count = fetchCountFromTableByParameter(DBHelper.DB_T_BREADCRUMBS, DBHelper.KEY_UPLOADED,"1");
					//logger.debug(TAG, "Initial Count of breadcrumb: " + count);
				}
				else if(fileType.contains(GlobalConstants.URLPARAM_FILETYPE_LOGINS)){
					table = GlobalConstants.URLPARAM_FILETYPE_LOGINS;
					column = DBHelper.KEY_UPLOADED;
					columnValue = "1";

					//count = fetchCountFromTableByParameter("logins", DBHelper.KEY_UPLOADED, "1");
					//logger.debug(TAG, "Initial Count of logins: " + count);
				}
				else if(fileType.contains(GlobalConstants.URLPARAM_FILETYPE_SIGNATURES)){
					table = GlobalConstants.URLPARAM_FILETYPE_SIGNATURES;
					column = DBHelper.KEY_UPLOADED;
					columnValue = "1";

					//count = fetchCountFromTableByParameter(DBHelper.DB_T_PHOTOS, DBHelper.KEY_UPLOADED, "1");
					//logger.debug(TAG, "Initial Count of PHOTOS: " + count);
				}
				else if(fileType.contains(GlobalConstants.URLPARAM_FILETYPE_SIGNATUREJPG)){
					table = GlobalConstants.URLPARAM_FILETYPE_SIGNATUREJPG;
					column = DBHelper.KEY_UPLOADED;
					columnValue = "2";

					//count = fetchCountFromTableByParameter(DBHelper.DB_T_PHOTOS, DBHelper.KEY_UPLOADED, "2");
					//logger.debug(TAG, "Initial Count of Photos: " + count);
				}
				else if(fileType.contains(GlobalConstants.URLPARAM_FILETYPE_PHOTOS)){
					table = GlobalConstants.URLPARAM_FILETYPE_PHOTOS;
					column = DBHelper.KEY_UPLOADED;
					columnValue = "1";

					//count = fetchCountFromTableByParameter(DBHelper.DB_T_PHOTOS, DBHelper.KEY_UPLOADED, "1");
					//logger.debug(TAG, "Initial Count of PHOTOS: " + count);
				}
				else if(fileType.contains(GlobalConstants.URLPARAM_FILETYPE_PHOTOJPG)){
					table = GlobalConstants.URLPARAM_FILETYPE_PHOTOJPG;
					column = DBHelper.KEY_UPLOADED;
					columnValue = "2";

					//count = fetchCountFromTableByParameter(DBHelper.DB_T_PHOTOS, DBHelper.KEY_UPLOADED, "2");
					//logger.debug(TAG, "Initial Count of Photos: " + count);
				}
				else if(fileType.contains(GlobalConstants.URLPARAM_FILETYPE_ROUTELISTACTIVITY)){
					table = GlobalConstants.URLPARAM_FILETYPE_ROUTELISTACTIVITY;
					column = DBHelper.KEY_UPLOADED;
					columnValue = "1";

					//count = fetchCountFromTableByParameter(DBHelper.DB_T_ROUTELISTACTIVITY, DBHelper.KEY_UPLOADED, "1");
					//logger.debug(TAG, "Initial Count of ROUTELISTACTIVITY: " + count);
				}
				else if(fileType.contains(GlobalConstants.URLPARAM_FILETYPE_ADDRESSDETAILIST)){
					table = GlobalConstants.URLPARAM_FILETYPE_ADDRESSDETAILIST;
					column = DBHelper.KEY_UPLOADED;
					columnValue = "1";

					//count = fetchCountFromTableByParameter(DBHelper.DB_T_ADDRESSDETAILLIST, DBHelper.KEY_UPLOADED, "1");
					//logger.debug(TAG, "Initial Count of ADDRESSDETAILIST: " + count);
				}
				else if(fileType.contains(GlobalConstants.URLPARAM_FILETYPE_UPLOADLOG)){
					table = GlobalConstants.URLPARAM_FILETYPE_UPLOADLOG;
					column = DBHelper.KEY_STATUS;
					columnValue = "1";

					//count = fetchCountFromTableByParameter(DBHelper.DB_T_UPLOADLOG, DBHelper.KEY_UPLOADED, "1");
					//logger.debug(TAG, "Initial Count of UPLOADLOG: " + count);
				}
				logger.debug("loadUpdateVerifyRecords_Common() : fileType = " + fileType + " table = " + table + " column = " + column + " columnValue = " + columnValue);

				if(table != null){
					String query = null;
					preQueryRecordCount = fetchCountFromTableByParameter(table, column, columnValue);
					logger.debug("Initial Count of records: " + preQueryRecordCount);

					query = "update " + table + " set " + column + "=" + columnValue + " where _id in ( "
							+ dataVal + " )";

					logger.debug("---------- Query: " + query);

					doExecQuery_Common(query);
					postQueryRecordCount = fetchCountFromTableByParameter(table, column, columnValue);
					logger.debug("Post Count of records: " + postQueryRecordCount);

					confirmedCount = postQueryRecordCount - preQueryRecordCount;
					logger.debug("Confirmed Count of records: " + confirmedCount);
				}
				else{
					logger.error("QUERY IS NULL");
					return 0;
				}
			}
		}
		catch(Exception e){
			logger.error("Database Load Exception caught" + e);
			return 0;
		}

		//logger.debug("Exiting Database Load");
		return confirmedCount;
	}

	public void setAllSequencingOrRenumberingRecordsToUploadForJobDetailId(int jobDetailId){
		String routeQuery = "udate routelist set routefinished = 1 where jobdetailid = " + jobDetailId;
		String addressDetailListQuery = "update addressdetaillist set uploaded = 0 where jobdetailid = " + jobDetailId;
		//=== THE FOLLOWING WILL ALSO HANDLE PRODUCT SCANS
		String addressDetailProductsQuery = "update addressdetailproducts set uploaded = 0 where jobdetailid = " + jobDetailId;
		String photosQuery = "update photos set uploaded = 0 where jobdetailid = " + jobDetailId;
		String signaturesQuery = "update signatures set uploaded = 0 where jobdetailid = " + jobDetailId;

		doExecQuery_Common(routeQuery);
		doExecQuery_Common(addressDetailListQuery);
		doExecQuery_Common(addressDetailProductsQuery);
		doExecQuery_Common(photosQuery);
		doExecQuery_Common(signaturesQuery);
	}

	public void setAllSequencingOrRenumberingRecordsToNotUploadForJobDetailId(int jobDetailId){
		String routeQuery = "udate routelist set routefinished = null where jobdetailid = " + jobDetailId;
		String addressDetailListQuery = "update addressdetaillist set uploaded = null where jobdetailid = " + jobDetailId;
		//=== THE FOLLOWING WILL ALSO HANDLE PRODUCT SCANS
		String addressDetailProductsQuery = "update addressdetailproducts set uploaded = null where jobdetailid = " + jobDetailId;
		String photosQuery = "update photos set uploaded = null where jobdetailid = " + jobDetailId;
		String signaturesQuery = "update signatures set uploaded = null where jobdetailid = " + jobDetailId;

		doExecQuery_Common(routeQuery);
		doExecQuery_Common(addressDetailListQuery);
		doExecQuery_Common(addressDetailProductsQuery);
		doExecQuery_Common(photosQuery);
		doExecQuery_Common(signaturesQuery);
	}

	public int loadUpdateVerifySignatureFiles_Common(String fileName, UploadLog uploadLogItem) throws IOException{
		//=== read in a line, determine what type it is....

		int confirmedCount = 0;

		try{
			if(fileName != null){
				String query = "update signatures set uploaded='2', fileuploaded='1' where "
							   + DBHelper.KEY_FILEPATH + " like '" + fileName + "' ";

				if(query != null){
					doExecQuery_Common(query);

				}
				query = "update uploadlog set numfilesconf='1', status = '1' where "
						+ DBHelper.KEY_ID + " = '" + uploadLogItem.getId() + "' ";

				if(query != null){
					doExecQuery_Common(query);

				}
			}
		}
		catch(Exception e){
			logger.error(TAG, "Database Load Exception caught" + e);
			return 0;
		}

		return confirmedCount;
	}

	public int loadUpdateVerifyPhotoFiles_Common(String fileName, UploadLog uploadLogItem) throws IOException{
		//=== read in a line, determine what type it is....

		int confirmedCount = 0;

		try{
			if(fileName != null){
				String query = "update photos set uploaded='2', fileuploaded='1' where "
							   + DBHelper.KEY_FILEPATH + " like '" + fileName + "' ";

				if(query != null){
					doExecQuery_Common(query);

				}
				query = "update uploadlog set numfilesconf='1', status = '1' where "
						+ DBHelper.KEY_ID + " = '" + uploadLogItem.getId() + "' ";

				if(query != null){
					doExecQuery_Common(query);

				}
			}
		}
		catch(Exception e){
			logger.error(TAG, "Database Load Exception caught" + e);
			return 0;
		}

		return confirmedCount;
	}

	public HashMap<Integer, Integer> fetchRouteCountByJobDetailsId_Common(){
		HashMap<Integer, Integer> numAddr = new HashMap<Integer, Integer>();
		Cursor cur = null;
		cur = fetchAllFromTableInOrder_Common(DBHelper.DB_T_ROUTELIST,
											  DBHelper.KEY_JOBID, "asc");

		try{
			while(cur.moveToNext()){
				Integer jDID = cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID));
				int count = fetchCountByQuery_Common("select count(*) from addressdetaillist where jobdetailid='"
													 + jDID + "'");
				numAddr.put(jDID, count);
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return numAddr;
	}

	public final ArrayList<Route> fetchAllRoutes_Common(){
		Cursor cur = null;
		ArrayList<Route> rs = new ArrayList<Route>();

		try{
			cur = fetchAllActiveRoutes_Common();

			while(cur.moveToNext()){
				Route r = new Route(cur);

				Date currentDate = new Date();
				Long validToTime = Long.parseLong(r.getDateValidTo()) * 1000;
				Long currentTime = currentDate.getTime();

				if(validToTime > currentTime){
					rs.add(r);
				}
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return rs;
	}

	public final void updateRouteDetailsListDisplayTimeNew_Random(int _id, long listDisplayTime){
		String query = "update addressdetaillist set listdisplaytime=" + listDisplayTime + " where _id=" + _id;

		try{
			doExecQuery_Common(query);
		}
		catch(SQLException e){
			//===  IF FAILS TRY ONE MORE TIME
			try{
				doExecQuery_Common(query);
			}
			catch(SQLException ee){
			}
		}
	}

	public final void updateRouteDetailsListSignatureTaken_Common(int _id, int signatureTaken){
		String query = "update addressdetaillist set signaturetaken=" + signatureTaken + " where _id=" + _id;

		try{
			doExecQuery_Common(query);
		}
		catch(SQLException e){
			//===  IF FAILS TRY ONE MORE TIME
			try{
				doExecQuery_Common(query);
			}
			catch(SQLException ee){
				logger.error("EXCEPTION", ee);
			}
		}
	}

	public final void updateRouteDetailsListPhotoTaken_Common(int _id, int photoTaken){
		String query = "update addressdetaillist set phototaken=" + photoTaken + " where _id=" + _id;

		try{
			doExecQuery_Common(query);
		}
		catch(SQLException e){
			//===  IF FAILS TRY ONE MORE TIME
			try{
				doExecQuery_Common(query);
			}
			catch(SQLException ee){
				logger.error("EXCEPTION", ee);
			}
		}
	}

	public final void reconcileRouteDetailsDeliveredStatus_Common(long deliveryId, long date, double lat, double longitude){

		String query = "update addressdetaillist set "
					   + "delivered=1, "
					   + "latdelivered=" + lat + ", "
					   + "longdelivered=" + longitude + ", "
					   + "wasreconciled=1, "
					   + "uploaded=0, "
					   + "deliverydate=" + date
					   + " where deliveryid=" + deliveryId;

		boolean successful = false;
		try{
			successful = doExecQuery_Common(query);

			logger.debug(TAG, "QUERY : is successful = " + successful + " " + query);
		}
		catch(SQLException e){
			//===  IF FAILS TRY ONE MORE TIME
			try{
				doExecQuery_Common(query);
			}
			catch(SQLException ee){
			}
		}
	}

	public final void updateRouteStartOrEndDate_Common(int jDetailId,
													   boolean updateLastEndDate, int finished){
		//logger.debug(TAG, "Entry: updateRouteStartOrEndDate");
		long date = Calendar.getInstance().getTimeInMillis();
		String query = "";
		//=== FINISHED ONLY IF THEY SAY YES....
		if(updateLastEndDate){
			if(finished > 0){
				query = "update routelist set routefinished=" + finished
						+ ", lastenddate=" + date + " where "
						+ DBHelper.KEY_JOBDETAILID + "=" + jDetailId;
			}
			else{
				query = "update routelist set  lastenddate=" + date + " where "
						+ DBHelper.KEY_JOBDETAILID + "=" + jDetailId;
			}
		}
		else{
			query = "update routelist set laststartdate=" + date
					+ ", lastenddate=null where " + DBHelper.KEY_JOBDETAILID
					+ "=" + jDetailId;
		}

		try{
			doExecQuery_Common(query);
		}
		catch(SQLException e){

		}
	}

	public final Cursor fetchRouteDetailsBySequenceSet_Sequenced(
			int jobDetailId,
			int lastDeliveredSequenceId,
			int sequenceIdInterval,
			int sequenceIdLookAheadLimit){
		String sequenceIdBetweenPhrase = "sequence between " +
										 lastDeliveredSequenceId +
										 " and " +
										 (lastDeliveredSequenceId + (sequenceIdLookAheadLimit * sequenceIdInterval));

		String query = "select * from addressdetaillist where jobdetailid=" + jobDetailId +
					   " and " +
					   sequenceIdBetweenPhrase +
					   " order by sequence asc";

		Cursor cur = null;

		try{
			cur = getWritableDatabase().rawQuery(query, null);
		}
		catch(SQLException e){
			logger.error("EXCEPTION++++++++++", e);
		}

		return cur;
	}

	public boolean addRouteActivityForJobDetailId(int mJobDetailId, int activityId, String activityDescription){
		boolean result = false;
		long dateLong = System.currentTimeMillis();

		if(ActivityCompat.checkSelfPermission(CTApp.appContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
		   PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CTApp.appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			Toast.makeText(CTApp.appContext, "Location permission not granted.", Toast.LENGTH_SHORT).show();
			return false;
		}

		Location locationGPS = CTApp.getLocation();
		if(locationGPS != null){
			double lat = locationGPS.getLatitude();
			double lon = locationGPS.getLongitude();
			String latitude = String.valueOf(lat);
			String longitude = String.valueOf(lon);

			String query = "INSERT OR IGNORE INTO workactivity(jobdetailid, date, lat, long, activityid, description, uploaded) " +
						   "values ('" + mJobDetailId + "','" + dateLong + "','" + latitude + "','" + longitude + "','" + activityId + "','" + activityDescription + "','0')";

			result = doExecQuery_Common(query);
		}
		else{
			Toast.makeText(CTApp.appContext, "Unable to find location.", Toast.LENGTH_SHORT).show();
		}

		return result;
	}

	public String fetchRouteWorkActivitiesForUpload(){

		Cursor cur = null;
		int count = 0;
		StringBuffer buf = new StringBuffer();

		try{

			updateTableUploadBatchId_Common(DBHelper.DB_T_WORKACTIVITY);

			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_WORKACTIVITY, DBHelper.KEY_UPLOADED,
					"0", DBHelper.KEY_ID, "desc", MAX_DB_RETURN_UPLOAD);

			if(cur != null){
				count = cur.getCount();
				buf.append(DBHelper.KEY_ID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_JOBDETAILID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_WORK_ACTIVITY_DATE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_LAT
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_LONG
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_WORK_ACTIVITY_ID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_WORK_ACTIVITY_DESCRIPTION
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_UPLOADBATCHID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_UPLOADED
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_TTLREMAINING);

				buf.append(System.getProperty("line.separator"));

				while(cur.moveToNext()){
					//=== RECORD ID
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== JOBDETAILID
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== DATE
					long timestampSD = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_WORK_ACTIVITY_DATE));
					String formattedTS = DateUtil.calcDateFromTime(timestampSD, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
					buf.append(formattedTS + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== LAT
					buf.append(cur.getFloat(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== LONG
					buf.append(cur.getFloat(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== DESCRIPTION
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_WORK_ACTIVITY_DESCRIPTION))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== ACTIVITY ID
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_WORK_ACTIVITY_ID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== UPLOADBATCHID
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADBATCHID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== UPLOADED
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADED))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(System.getProperty("line.separator"));
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		buf.append(GlobalConstants.UPLOAD_COUNT_DELIMINATOR + count);

		return buf.toString();
	}

	public ArrayList<String> fetchAllScanCodesForDeliveryId(long deliveryId){
		ArrayList<String> scanCodesList = new ArrayList<String>();
		Cursor cur = null;

		try{
			String query = "SELECT scancode FROM addressdetailproducts WHERE productcode='SCAN' AND deliveryid=" + deliveryId;
			cur = getWritableDatabase().rawQuery(query, null);

			if(cur != null){
				while(cur.moveToNext()){
					//SCAN CODE
					String code = cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_SCANCODE));

					if(code != null && !code.isEmpty()){
						scanCodesList.add(code);
					}
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return scanCodesList;
	}

	public ArrayList<DeliveryItemProduct> fetchAllDeliveryItemScanProductForDeliveryId(long deliveryId){
		ArrayList<DeliveryItemProduct> scanCodesList = new ArrayList<DeliveryItemProduct>();
		Cursor cur = null;

		try{
			String query = "SELECT * FROM addressdetailproducts WHERE producttype='SCAN' AND deliveryid=" + deliveryId;
			cur = getWritableDatabase().rawQuery(query, null);

			if(cur != null){
				while(cur.moveToNext()){
					//=== SCAN CODE
					DeliveryItemProduct dip = new DeliveryItemProduct(cur);

					scanCodesList.add(dip);
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return scanCodesList;
	}

	public String fetchAllProductScansForUpload_Common(){

		Cursor cur = null;
		int count = 0;
		StringBuffer buf = new StringBuffer();

		try{

			updateTableUploadBatchId_Common(DBHelper.DB_T_ADDRESSDETAILPRODUCTS);

			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_ADDRESSDETAILPRODUCTS, DBHelper.KEY_UPLOADED,
					"0", DBHelper.KEY_ID, "desc", MAX_DB_RETURN_UPLOAD);

			if(cur != null){
				count = cur.getCount();
				buf.append(DBHelper.KEY_PRODUCTTYPE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_PRODUCTCODE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_QTY
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_DELIVERYID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_JOBDETAILID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_SCANCODE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_DELIVERYDATE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_DELIVERYLATITUDE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR

						   + DBHelper.KEY_DELIVERYLONGITUDE
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						   //===NO NEED TO UPLOAD THE 'UPLOADED' FIELD
						   + DBHelper.KEY_UPLOADBATCHID
						   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR
						  );

				buf.append(System.getProperty("line.separator"));

				while(cur.moveToNext()){
					//===PRODUCT TYPE
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_PRODUCTTYPE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);
					//=== PRODUCT CODE
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_PRODUCTCODE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== QUANTITY
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_QTY))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== DELIVERY ID
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== JOB DETAIL ID
					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== SCAN CODE
					buf.append(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_SCANCODE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== DATE
					long timestampSD = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYDATE));
					String formattedTS = DateUtil.calcDateFromTime(timestampSD, GlobalConstants.DEFAULT_DATETIME_FORMAT_SQL);
					buf.append(formattedTS + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== LAT
					buf.append(cur.getFloat(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYLATITUDE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					//=== LONG
					buf.append(cur.getFloat(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYLONGITUDE))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADBATCHID))
							   + GlobalConstants.UPLOAD_FIELD_DELIMINATOR);

					buf.append(System.getProperty("line.separator"));
				}
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION ", e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		buf.append(GlobalConstants.UPLOAD_COUNT_DELIMINATOR + count);

		return buf.toString();
	}

	public String[] fetchRouteActivitiesForJobDetailId(int jobDetailId){
		String[] activities = new String[]{};

		String query = "select * from workactivity " +
					   "where jobdetailid='" + jobDetailId + "'";

		Cursor cur = null;
		ArrayList<String> activityDetails = new ArrayList<String>();

		try{
			cur = getWritableDatabase().rawQuery(query, null);
			//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");

			while(cur.moveToNext()){
				String description = cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_WORK_ACTIVITY_DESCRIPTION));
				long dateLong = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_WORK_ACTIVITY_DATE));
				SimpleDateFormat formatter = new SimpleDateFormat("E-MMM-yyyy  HH:mm");
				//=== Create a calendar object that will convert the date and time value in
				//=== milliseconds to date.
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(dateLong);
				String dateString = formatter.format(calendar.getTime());

				activityDetails.add(description + "|||" + dateString);
			}
		}
		catch(SQLException e){
			logger.debug(e.getMessage(), e);
		}

		return activityDetails.toArray(new String[activityDetails.size()]);
	}

	public final Route fetchRoute_Common(int jobDetailsId){
		// logger.debug("Entry fetchRoute");
		Cursor cur = null;
		Route r = new Route();

		try{
			//=== GET THE ROUTE
			cur = fetchAllFromTableByFieldNameAndParameterInOrder_Common(
					DBHelper.DB_T_ROUTELIST, DBHelper.KEY_JOBDETAILID,
					Integer.valueOf(jobDetailsId).toString(), DBHelper.KEY_ROUTEID,
					"asc", 1000);

			while(cur.moveToNext()){
				r = new Route(cur);
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return r;
	}

	public final int getUnresolvedAddressCountForJobDetailId_Common(int jobDetailId){
		String query = "select * from addressdetaillist " +
					   "where jobdetailid = " + jobDetailId +
					   " order by sequence";
		Cursor cur = null;
		LinkedHashMap<Integer, DeliveryItem> sequencedHashMap = new LinkedHashMap<Integer, DeliveryItem>();

		int unresolvedAddressCount = 0;
		double startLatitude = 0;
		double startLongitude = 0;
		boolean haveFoundFirstGoodAddress = false;

		try{
			cur = getWritableDatabase().rawQuery(query, null);
			//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");

			while(cur.moveToNext()){
				double thisLatitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT));
				double thisLongitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG));

				//=== NEGATIVE SEQUENCE
				int sequence = cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_SEQUENCE));

				if(!haveFoundFirstGoodAddress){
					startLatitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT));
					startLongitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG));

					//=== NEGATIVE SEQUENCE
					if(sequence <= 0 || startLatitude == 0 && startLongitude == 0){
						unresolvedAddressCount++;
						continue;
					}
					else{
						haveFoundFirstGoodAddress = true;
					}
				}

				double distance = GPSUtils.distFromAsLocation(thisLatitude, thisLongitude, startLatitude, startLongitude);
				if(distance > GlobalConstants.BAD_LOCATION_SANITY_DISTANCE_CHECK_METERS || (thisLatitude == 0 && thisLongitude == 0)){
					//=== COUNT THIS DROP
					unresolvedAddressCount++;
				}
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return unresolvedAddressCount;

	}

	public final LinkedHashMap<Integer, DeliveryItem> fetchRouteDetailsForNavigationForJobDetailIdStartAtSequence_Sequenced(
			int jobDetailId,
			int sequenceId,
			int limitNumber,
			int maxPageNumber,
			GeoCoordinates deviceLocation){
		//logger.debug("$$$$$$$$$$$$ START");
		String query = "select * from addressdetaillist " +
					   "where jobdetailid = " + jobDetailId +
					   " and jobtype > 4" +        // ONLY MUST DELIVER AND DELIVER
					   " and sequence > " + sequenceId +
					   " order by sequence asc limit " + maxPageNumber;

		Cursor cur = null;
		LinkedHashMap<Integer, DeliveryItem> sequencedHashMap = new LinkedHashMap<Integer, DeliveryItem>();

		double startLatitude = deviceLocation.latitude;
		double startLongitude = deviceLocation.longitude;

		try{
			cur = getWritableDatabase().rawQuery(query, null);
			//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");

			int count = 0;
			boolean foundFirstValidAddress = false;

			while(cur.moveToNext()){
				if(count == limitNumber){
					break;
				}

				DeliveryItem deliveryItem = new DeliveryItem(cur);

				//=== NEGATIVE SEQUENCE
				if(deliveryItem.getSequence() <= 0){
					continue;
				}

				if(deliveryItem.getGpsLocationLatitude() == 0 && deliveryItem.getGpsLocationLongitude() == 0){
					//=== IGNORE THIS DROP
					continue;
				}

				//=== NEGATIVE SEQUENCE
				if(CTApp.isInSimulation && !foundFirstValidAddress){
					startLatitude = deliveryItem.getGpsLocationLatitude();
					startLongitude = deliveryItem.getGpsLocationLongitude();
					foundFirstValidAddress = true;
				}

				double distance = GPSUtils.distFromAsLocation(deliveryItem.getGpsLocationLatitude(), deliveryItem.getGpsLocationLongitude(), startLatitude, startLongitude);
				if(distance > GlobalConstants.BAD_LOCATION_SANITY_DISTANCE_CHECK_METERS){
					//deliveryItem.setIsInvalidAddress(true);
					//=== IGNORE THIS DROP
					continue;
				}

				count++;

				long listDisplayTimeFromRecord = cur.getLong(cur.getColumnIndexOrThrow("listdisplaytime"));
				if(listDisplayTimeFromRecord > 0){
					listDisplayTimeFromRecord = System.currentTimeMillis();
				}
				deliveryItem.setListDisplayTime(listDisplayTimeFromRecord);

				sequencedHashMap.put(deliveryItem.getId(), deliveryItem);
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return sequencedHashMap;
	}

	public int fetchTotalDeliveryCountByJobDetailIdForDeliverablesOnly_Common(int jobDetailId){
		int count = 0;
		Cursor cur = null;

		try{
			String str = "SELECT count(*) FROM addressdetaillist WHERE jobDetailId="
						 + jobDetailId + " and delivered=1 and jobtype in (5,6)";
			cur = getWritableDatabase().rawQuery(str, null);

			while(cur.moveToNext()){
				count = cur.getInt(0);
			}
		}
		catch(SQLException e){
			logger.error("exception " + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return count;
	}

	public int fetchTotalDropCountByJobDetailIdForDeliverablesOnly_Common(int jobDetailId){
		int count = 0;
		Cursor cur = null;

		try{
			String str = "SELECT count(*) FROM addressdetaillist WHERE jobDetailId="
						 + jobDetailId + " and jobtype in (5,6)";
			cur = getWritableDatabase().rawQuery(str, null);

			while(cur.moveToNext()){
				count = cur.getInt(0);
			}
		}
		catch(SQLException e){
			logger.error("exception " + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return count;
	}

	public final LinkedHashMap<Integer, DeliveryItemProduct> fetchProductsForDeliveryItem_Common(int jobDetailId, long deliveryId){
		//logger.debug("$$$$$$$$$$$$ START");

		String query = "select * from addressdetailproducts " +
					   "where jobdetailid = " + jobDetailId +
					   " and deliveryid = " + deliveryId;

		Cursor cur = null;
		LinkedHashMap<Integer, DeliveryItemProduct> sequencedHashMap = new LinkedHashMap<Integer, DeliveryItemProduct>();

		try{
			cur = getWritableDatabase().rawQuery(query, null);
			//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");

			while(cur.moveToNext()){
				DeliveryItemProduct product = new DeliveryItemProduct();

				product.setId(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID)));
				product.setProductCode(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_PRODUCTCODE)));
				product.setQuantity(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_QUANTITY)));
				product.setDeliveryId(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYID)));
				product.setJobDetailId(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID)));

				sequencedHashMap.put(product.getId(), product);
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return sequencedHashMap;
	}

	public final DeliveryItem fetchRouteDetailForSequenceIdForJobDetailId_Sequenced(int jobDetailId, int sequenceId){
		//logger.debug("$$$$$$$$$$$$ START");

		String query = "select * from addressdetaillist " +
					   "where jobdetailid = " + jobDetailId +
					   " and sequence = " + sequenceId;

		Cursor cur = null;
		DeliveryItem deliveryItem = new DeliveryItem();
		try{
			cur = getWritableDatabase().rawQuery(query, null);
			cur.moveToFirst();

			deliveryItem = new DeliveryItem(cur);

		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return deliveryItem;
	}

	public final DeliveryItem fetchFirstRouteDetailForJobDetailId_Sequenced(int jobDetailId){
		//logger.debug("$$$$$$$$$$$$ START");

		String query = "select * from addressdetaillist " +
					   "where jobdetailid = " + jobDetailId +
					   " order by sequence asc limit 1";

		Cursor cur = null;
		DeliveryItem deliveryItem = null;
		try{
			cur = getWritableDatabase().rawQuery(query, null);
			cur.moveToFirst();

			deliveryItem = new DeliveryItem(cur);

			//=== NEGATIVE SEQUENCE
			if(deliveryItem.getSequence() <= 0 || (deliveryItem.getGpsLocationLatitude() == 0 && deliveryItem.getGpsLocationLongitude() == 0)){
				deliveryItem.setIsInvalidAddress(true);
			}

			long listDisplayTimeFromRecord = deliveryItem.getListDisplayTime();
			if(listDisplayTimeFromRecord > 0){
				listDisplayTimeFromRecord = System.currentTimeMillis();
			}
			deliveryItem.setListDisplayTime(listDisplayTimeFromRecord);
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return deliveryItem;
	}

	public final int fetchRouteDetailFirstSequenceIdForJobDetailId_Sequenced(int jobDetailId){
		//logger.debug("$$$$$$$$$$$$ START");

		String query = "select sequence from addressdetaillist " +
					   "where jobdetailid = " + jobDetailId +
					   " order by sequence asc " +
					   " limit 1";

		Cursor cur = null;
		int firstSequenceId = -999;
		try{
			cur = getWritableDatabase().rawQuery(query, null);
			cur.moveToFirst();
			firstSequenceId = cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_SEQUENCE));
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return firstSequenceId;
	}

	public final LinkedHashMap<Integer, DeliveryItem> fetchRouteDetailsForJobDetailIdStartAtSequence_Sequenced(
			int jobDetailId,
			int sequenceId,
			int limit,
			Location deviceLocation,
			double deviceForwardAzimuth){
		//logger.debug("$$$$$$$$$$$$ START");

		String query = "select * from addressdetaillist " +
					   "where jobdetailid = " + jobDetailId +
					   " and sequence > " + sequenceId +
					   " order by sequence asc limit " + limit;

		Cursor cur = null;
		LinkedHashMap<Integer, DeliveryItem> sequencedHashMap = new LinkedHashMap<Integer, DeliveryItem>();

		try{
			cur = getWritableDatabase().rawQuery(query, null);
			//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");

			double startLatitude = deviceLocation.getLatitude();
			double startLongitude = deviceLocation.getLongitude();

			cur.moveToFirst();
			DeliveryItem deliveryItem = null;
			while(!cur.isAfterLast()){
				deliveryItem = new DeliveryItem(cur);

				if(CTApp.isInSimulation && cur.isFirst()){
					startLatitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT));
					startLongitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG));
				}

				double distance = GPSUtils.distFromAsLocation(deliveryItem.getGpsLocationLatitude(), deliveryItem.getGpsLocationLongitude(), startLatitude, startLongitude);

				if(deliveryItem.getSequence() <= 0 || distance > GlobalConstants.BAD_LOCATION_SANITY_DISTANCE_CHECK_METERS || (deliveryItem.getGpsLocationLatitude() == 0 && deliveryItem.getGpsLocationLongitude() == 0)){
					deliveryItem.setIsInvalidAddress(true);
				}

				double deviceToThisDistance = deviceLocation.distanceTo(deliveryItem.getLocation());
				deliveryItem.setDistance(deviceToThisDistance);

				sequencedHashMap.put(deliveryItem.getId(), deliveryItem);

				cur.moveToNext();
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return sequencedHashMap;
	}

	public final int fetchLargestSequenceForJobDetailId_Sequenced(int jobDetailId){
		//logger.debug("$$$$$$$$$$$$ START");

		String query = "select max(sequence) as sequence from addressdetaillist " +
					   "where jobdetailid = " + jobDetailId;

		Cursor cur = null;
		int maxSequence = -1;

		try{
			cur = getWritableDatabase().rawQuery(query, null);
			cur.moveToFirst();
			//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");
			maxSequence = cur.getInt(cur.getColumnIndexOrThrow("sequence"));
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return maxSequence;
	}

	public void renumberSequenceNumbers(int sequenceNumberMovingFrom, int sequenceNumberToMoveTo, int sequenceInterval, int jobDetailId){
		String queryMoveAhead1 = "update addressdetaillist set sequence = -1234 where sequence = " + sequenceNumberMovingFrom;
		String queryMoveAhead2 = "update addressdetaillist set sequence = sequence + " + sequenceInterval + " where sequence >= " + sequenceNumberToMoveTo + " and " + "sequence <= " + sequenceNumberMovingFrom;
		String queryMoveAhead3 = "update addressdetaillist set sequence = " + sequenceNumberToMoveTo + " where sequence = -1234";

		String queryMoveBehind1 = "update addressdetaillist set sequence = -1234 where sequence = " + sequenceNumberMovingFrom;
		String queryMoveBehind2 = "update addressdetaillist set sequence = sequence - " + sequenceInterval + " where sequence <= " + sequenceNumberToMoveTo + " and " + "sequence > " + sequenceNumberMovingFrom;
		String queryMoveBehind3 = "update addressdetaillist set sequence = " + sequenceNumberToMoveTo + " where sequence = -1234";

		if(sequenceNumberToMoveTo < sequenceNumberMovingFrom){
			doExecQuery_Common(queryMoveAhead1);
			doExecQuery_Common(queryMoveAhead2);
			doExecQuery_Common(queryMoveAhead3);
		}
		else if(sequenceNumberToMoveTo > sequenceNumberMovingFrom){
			doExecQuery_Common(queryMoveBehind1);
			doExecQuery_Common(queryMoveBehind2);
			doExecQuery_Common(queryMoveBehind3);
		}
	}

	public final int fetchSequenceIntervalForJobDetailId_Sequenced(int jobDetailId){
		//logger.debug("$$$$$$$$$$$$ START");
		int interval = -1;

		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
			interval = 0;
		}
		else{
			String query = "select sequence from addressdetaillist " +
						   "where jobdetailid = " + jobDetailId + " and sequence > 0 order by sequence asc limit 2";
			logger.debug("fetchSequenceIntervalForJobDetailId_Sequenced() : " + query);

			Cursor cur = null;
			int minSequence = -1;
			int nextSequence = -1;

			try{
				cur = getWritableDatabase().rawQuery(query, null);
				cur.moveToFirst();
				//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");
				minSequence = cur.getInt(cur.getColumnIndexOrThrow("sequence"));
				cur.moveToNext();
				//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");
				nextSequence = cur.getInt(cur.getColumnIndexOrThrow("sequence"));
				interval = nextSequence - minSequence;
			}
			catch(SQLException e){
				logger.error(e.getMessage(), e);
			}
			finally{
				if(cur != null && !cur.isClosed()){
					cur.close();
				}
			}
		}

		return interval;
	}

	public final void setlastMissedDropCheck(long time){
		lastMissedDropCheck = time;
	}

	public final ArrayList<DeliveryItem> fetchSkippedRouteDetailsForJobDetailIdInCurrentDeliveryZone_Sequenced(
			int jobDetailId,
			double extendedAreaFrontDistance,
			double extendedAreaSideDistance,
			int deliveryTargetQuads,
			Location deviceLocation,
			double deviceForwardAzimuth,
			int lastDeliveredSequenceId,
			int sequenceIdInterval){
		logger.debug("ENTER>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		double longestEllipseAxis = extendedAreaFrontDistance < extendedAreaSideDistance ?
									extendedAreaSideDistance :
									extendedAreaFrontDistance;
		SEQUENCE_ID_DIVISOR = sequenceIdInterval;

		//====================================================================================================================
		//=== Loop through the cursor, determine if the point is in the extended look ahead area or the target delivery area.
		//=== Then determine if the point is in the correct quadrant, has an acceptable range distance, and should be displayed or removed from display
		//====================================================================================================================

		//=== GET THE TARGET LOOK AHEAD AREA GEO-BOUNDED BOX FOR POSSIBLE DELIVERY ADDRESSES
		Cursor lookAheadSkippedRouteDetailCursor = fetchRouteDetailsBySequenceSet_Sequenced(jobDetailId,
																							(lastDeliveredSequenceId + SEQUENCE_ID_DIVISOR),// * 4)),
																							sequenceIdInterval,
																							6);//SEQUENCE_ID_LOOKAHEAD_LIMIT);
		logger.debug("SKIPPED SUPER SET COUNT has " + lookAheadSkippedRouteDetailCursor.getCount() + " items");

		ArrayList<DeliveryItem> newSkippedRouteDetailsInDeliveryAreaToAdd = new ArrayList<DeliveryItem>();

		logger.debug("SKIPPED SUPER SET LAST DELIVERED SEQUENCE = " + lastDeliveredSequenceId);

		int deliveredFromRecord = -1;
		String lookAheadStreetNumber = "";
		String lookAheadStreetName = "";
		double lookAheadLatitude = 0d;
		double lookAheadLongitude = 0d;
		Location lookAheadLocation = null;
		double deviceToLookAheadDistance = 0d;
		boolean isInvalidAddress = false;
		float deviceToLookAheadAzimuth = 0f;
		boolean isThisAddressPointInTheTargetQuadrantSweep = false;
		int lookAheadQuadrantForRecord = GlobalConstants.DEF_DELIVERY_QUADS_NONE;
		String lookAheadQuadrantForRecordLabel = "";
		String lookAheadQuadrantForDeliverLabel = "";
		double relativeEllipseAzimuthTheta = 0d;
		double distanceToTargetEllipseCircumferenceOnAzimuthTheta = 0d;

		while(lookAheadSkippedRouteDetailCursor.moveToNext()){
			DeliveryItem routeDetail = new DeliveryItem(lookAheadSkippedRouteDetailCursor);

			//lookAheadStreetNumber = routeDetail.getGpsLocationAddressNumber();
			//lookAheadStreetName = routeDetail.getGpsLocationAddressStreet();
			//logger.debug("SKIPPED SUPER SET lookAhead ADDRESS = " + lookAheadStreetNumber + " " + lookAheadStreetName);
			//logger.debug("SKIPPED SUPER SET lookAhead SEQUENCE = " + routeDetail.getSequence());

			deliveredFromRecord = routeDetail.getDelivered();
			//logger.debug("DELIVERED FROM RECORD IS " + deliveredFromRecord);

			//=== WE MUST EXCLUDE ALL DELIVERED and PROCESSED DROPS
			//=== A NON-DELIVERY DROP and INVALID addresses WILL GET A DELIVERED VALUE OF ZERO WHEN PROCESSED
			if(deliveredFromRecord == 1 || deliveredFromRecord == 0){
				//WE ONLY WANT TO MARK AND MAP PLOT UNDELIVERED or invalid ADDRESSES
				//logger.debug("SKIPPED SUPER SET SKIPPING - THIS DROP IS ALREADY PROCESSED - deliveredFromRecord = " + deliveredFromRecord);
				//logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				continue;
			}

			lookAheadLatitude = routeDetail.getGpsLocationLatitude();
			lookAheadLongitude = routeDetail.getGpsLocationLongitude();

			lookAheadLocation = GPSUtils.convertToLocationFromGeoCode(lookAheadLatitude + "," + lookAheadLongitude);

			deviceToLookAheadDistance = deviceLocation.distanceTo(lookAheadLocation);

			isInvalidAddress = false;
			//=== this is a gross check to weed out any outliers
			if(deviceToLookAheadDistance > longestEllipseAxis){
				if(deviceToLookAheadDistance < GlobalConstants.BAD_LOCATION_SANITY_DISTANCE_CHECK_METERS){
					//=== IGNORE, IT IS OUTSIDE OF THE TARGET AREA
					//logger.debug("SKIPPED SUPER SET SKIPPING - (GROSS CHECK) - THIS DROP IS FURTHER " + deviceToLookAheadDistance + " THAN LONGEST DELIVERY ELLIPSE DISTANCE " + longestEllipseAxis);
					//logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
					continue;
				}
				else{
					//logger.debug("SKIPPED SUPER SET SKIPPING - (GROSS CHECK) - THIS DROP IS LIKELY A BAD ADDRESS - distance from current location = " + (deviceToLookAheadDistance / 1000.00) + "KM");
					continue;
				}
			}
			//logger.debug("KEEPING (GROSS CHECK) - THIS DROP " + deviceToLookAheadDistance + " IS WITHIN THE LONGEST DELIVERY ELLIPSE DISTANCE " + longestEllipseAxis);

			//=== NEGATIVE SEQUENCE
			if(routeDetail.getSequence() <= 0){
				//isInvalidAddress = true;
				//logger.debug("SKIPPED SUPER SET SKIPPING - (GROSS CHECK) - THIS DROP IS LIKELY A BAD ADDRESS");
				continue;
			}

			//=== TARGET AREA IS NOW AN ELLIPSOID WHERE THE MAIN AXIS IS extendedAreaFrontDistance
			//=== AND THE MINOR AXIS IS extendedAreaSideDistance. THE ELLIPSOID IS ORIENTED WITH THE MAJOR AXIS
			//=== PARALLEL TO THE DEVICE AZIMUTH
			deviceToLookAheadAzimuth = deviceLocation.bearingTo(lookAheadLocation);
			//===CONVERT BEARING TO AZIMUTH
			deviceToLookAheadAzimuth = GPSUtils.normalizeBearingToAzimuth(deviceToLookAheadAzimuth);

			isThisAddressPointInTheTargetQuadrantSweep = false;

			//=== QUADRANT IS RELATIVE TO DEVICE AZIMUTH, ZERO QUAD IS ERRANT
			lookAheadQuadrantForRecord = GPSUtils.determineRelativeQuadrant_Common(deviceForwardAzimuth, deviceToLookAheadAzimuth);
			lookAheadQuadrantForRecordLabel = GPSUtils.getRelativeQuadrantEnglishString(lookAheadQuadrantForRecord);
			//logger.debug("SUPER SET RELATIVE look ahead drop quadrant is " + lookAheadQuadrantForRecordLabel);
			lookAheadQuadrantForDeliverLabel = GPSUtils.getRelativeQuadrantEnglishString(deliveryTargetQuads);
			//logger.debug("SUPER SET RELATIVE look ahead delivery quadrant is " + lookAheadQuadrantForDeliverLabel);

			if((deliveryTargetQuads & lookAheadQuadrantForRecord) > 0){
				isThisAddressPointInTheTargetQuadrantSweep = true;
			}

			//=== FILTER OUT ANYTHING NOT IN OUR CURRENT QUADRANTs FOR LOOK AHEAD DIRECTION
			if(!isThisAddressPointInTheTargetQuadrantSweep){
				//===IGNORE THIS ONE
				//logger.debug("SKIPPED SUPER SET SKIPPING - THIS DROP'S QUAD " + lookAheadQuadrantForRecordLabel + "IS NOT IN THE DELIVERY QUADs " + lookAheadQuadrantForDeliverLabel);
				//logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");

				continue;
			}
			logger.debug("SKIPPED SUPER SET KEEPING - THIS DROP'S QUAD " + lookAheadQuadrantForRecordLabel + "IS WITHIN THE DELIVERY QUADs " + lookAheadQuadrantForDeliverLabel);

			relativeEllipseAzimuthTheta = Math.abs(deviceToLookAheadAzimuth - deviceForwardAzimuth);

			relativeEllipseAzimuthTheta = GPSUtils.normalizeAzimuthToEllipseThetaAngle(relativeEllipseAzimuthTheta);
			distanceToTargetEllipseCircumferenceOnAzimuthTheta = GPSUtils.getDistanceToPointOnEllipseCircumferenceWithBearingUsingMajorMinorAxis(relativeEllipseAzimuthTheta, extendedAreaFrontDistance, extendedAreaSideDistance);

			//=== SEE IF THIS DELIVERY ADDRESS IS WITHIN THE DISTANCE TO THE TARGET/DELIVERY ELLIPSE CIRCUMFERENCE AT A GIVEN THETA BEARING
			if(distanceToTargetEllipseCircumferenceOnAzimuthTheta < deviceToLookAheadDistance){
				//=== POINT IS OUTSIDE THE ELLIPSE TARGET AREA
				//logger.debug("SKIPPED SUPER SET SKIPPING - THIS DROP IS OUTSIDE " + deviceToLookAheadDistance +
				//			 " THE DELIVERY ELLIPSE THETA DISTANCE " + distanceToTargetEllipseCircumferenceOnAzimuthTheta);
				//logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				continue;
			}

			lookAheadStreetNumber = routeDetail.getGpsLocationAddressNumber();
			lookAheadStreetName = routeDetail.getGpsLocationAddressStreet();
			logger.debug("SKIPPED SUPER SET : ADDING lookAhead ADDRESS = " + lookAheadStreetNumber + " " + lookAheadStreetName);
			logger.debug("SKIPPED SUPER SET : ADDING lookAhead SEQUENCE = " + routeDetail.getSequence());

			routeDetail.setDistance(deviceToLookAheadDistance);

			routeDetail.setIsInvalidAddress(isInvalidAddress);

			newSkippedRouteDetailsInDeliveryAreaToAdd.add(routeDetail);

			//logger.debug("SKIPPED SUPER SET KEEPING - THIS DROP IS " + deviceToLookAheadDistance +
			//			 " INSIDE THE DELIVERY ELLIPSE THETA DISTANCE " + distanceToTargetEllipseCircumferenceOnAzimuthTheta);
		}    //end top while loop

		lookAheadSkippedRouteDetailCursor.close();

		Collections.sort(newSkippedRouteDetailsInDeliveryAreaToAdd, DeliveryItem.ORDERING_SEQUENCEID);

		return newSkippedRouteDetailsInDeliveryAreaToAdd;
	}

	public final ArrayList<DeliveryItem> fetchRouteDetailsWithinDeliveryArea_Sequenced(
			int jobDetailId,
			double targetAreaFrontDistance,
			double targetAreaSideDistance,
			double extendedAreaFrontDistance,
			double extendedAreaSideDistance,
			int deliveryTargetQuads,
			Location deviceLocation,
			double deviceForwardAzimuth,
			long deliveryTimeStampParameterIn,
			int lastDeliveredSequenceId,
			int sequenceIdInterval){
		logger.debug("ENTER>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		double longestTargetEllipseAxis = targetAreaFrontDistance < targetAreaSideDistance ?
										  targetAreaSideDistance :
										  targetAreaFrontDistance;

		double longestExtendedEllipseAxis = extendedAreaFrontDistance < extendedAreaSideDistance ?
											extendedAreaSideDistance :
											extendedAreaFrontDistance;
		SEQUENCE_ID_DIVISOR = sequenceIdInterval;

		//=== ON START OF ROUTE THE FIRST DROP IS NOT DELIVERED SO ADJUST FOR THE SEQUENCE IF SO
		boolean isFirstDropDelivered = false;
		Cursor lastDeliveredRouteDetailCursor = null;//fetchRouteDetailsBySequenceSet_Sequenced(jobDetailId, lastDeliveredSequenceId, sequenceIdInterval, SEQUENCE_ID_LOOKAHEAD_LIMIT);

		String query1 = "select * from addressdetaillist where jobdetailid=" + jobDetailId +
						" and " +
						"sequence = " + lastDeliveredSequenceId;
		logger.debug("QUERY = " + query1);

		try{
			lastDeliveredRouteDetailCursor = getWritableDatabase().rawQuery(query1, null);

			if(lastDeliveredRouteDetailCursor == null){
				logger.debug("lastDeliveredRouteDetailCursor = NULL");
			}
			else{
				logger.debug("lastDeliveredRouteDetailCursor.getColumnCount() = " + lastDeliveredRouteDetailCursor.getColumnCount());
			}

			lastDeliveredRouteDetailCursor.moveToFirst();//.moveToNext();
			DeliveryItem lastDelivered = new DeliveryItem(lastDeliveredRouteDetailCursor);

			if(lastDelivered.getDelivered() == 1 || lastDelivered.getDelivered() == 0){
				isFirstDropDelivered = true;
			}
		}
		catch(SQLException e){
			logger.error("EXCEPTION++++++++++", e);
		}

		//====================================================================================================================
		//=== Loop through the cursor, determine if the point is in the extended look ahead area or the target delivery area.
		//=== Then determine if the point is in the correct quadrant, has an acceptable range distance, and should be displayed or removed from display
		//====================================================================================================================

		//=== GET THE TARGET LOOK AHEAD AREA GEO-BOUNDED BOX FOR POSSIBLE DELIVERY ADDRESSES
		Cursor lookAheadRouteDetailCursor = null;//fetchRouteDetailsBySequenceSet_Sequenced(jobDetailId, lastDeliveredSequenceId, sequenceIdInterval, SEQUENCE_ID_LOOKAHEAD_LIMIT);

		int nextDropSequence = lastDeliveredSequenceId + (isFirstDropDelivered ? sequenceIdInterval : 0);
		String query2 = "select * from addressdetaillist where jobdetailid=" + jobDetailId +
						" and " +
						"sequence = " + nextDropSequence;

		try{
			lookAheadRouteDetailCursor = getWritableDatabase().rawQuery(query2, null);
		}
		catch(SQLException e){
			logger.error("EXCEPTION++++++++++", e);
		}

		//logger.debug("SUPER SET COUNT has " + lookAheadRouteDetailCursor.getCount() + " items");

		ArrayList<DeliveryItem> newRouteDetailsInDeliveryAreaToAdd = new ArrayList<DeliveryItem>();

		logger.debug("SUPER SET LAST DELIVERED SEQUENCE = " + lastDeliveredSequenceId);

		int deliveredFromRecord = -1;
		String lookAheadStreetNumber = "";
		String lookAheadStreetName = "";
		double lookAheadLatitude = 0d;
		double lookAheadLongitude = 0d;
		Location lookAheadLocation = null;
		double deviceToLookAheadDistance = 0d;
		boolean isInvalidAddress = false;
		float deviceToLookAheadAzimuth = 0f;
		boolean isThisAddressPointInTheTargetQuadrantSweep = false;
		int lookAheadQuadrantForRecord = GlobalConstants.DEF_DELIVERY_QUADS_NONE;
		String lookAheadQuadrantForRecordLabel = "";
		String lookAheadQuadrantForDeliverLabel = "";
		double relativeEllipseAzimuthTheta = 0d;
		double distanceToTargetEllipseCircumferenceOnAzimuthTheta = 0d;
		boolean foundNextSequenceOutsideTaget = false;

		while(lookAheadRouteDetailCursor.moveToNext()){
			DeliveryItem routeDetail = new DeliveryItem(lookAheadRouteDetailCursor);
			logger.debug(routeDetail.getDeliveryResequencingInfo());

			foundNextSequenceOutsideTaget = false;
			isInvalidAddress = false;

			lookAheadStreetNumber = routeDetail.getGpsLocationAddressNumber();
			lookAheadStreetName = routeDetail.getGpsLocationAddressStreet();
			logger.debug("SUPER SET lookAhead ADDRESS = " + lookAheadStreetNumber + " " + lookAheadStreetName);
			logger.debug("SUPER SET lookAhead SEQUENCE = " + routeDetail.getSequence());

			deliveredFromRecord = routeDetail.getDelivered();
			//logger.debug("DELIVERED FROM RECORD IS " + deliveredFromRecord);

			//=== WE MUST EXCLUDE ALL DELIVERED and PROCESSED DROPS
			//=== A NON-DELIVERY DROP and INVALID addresses WILL GET A DELIVERED VALUE OF ZERO WHEN PROCESSED
			if(deliveredFromRecord == 1 || deliveredFromRecord == 0){
				//WE ONLY WANT TO MARK AND MAP PLOT UNDELIVERED or invalid ADDRESSES
				logger.debug("SKIPPING - THIS DROP IS ALREADY PROCESSED - deliveredFromRecord = " + deliveredFromRecord);
				//logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				continue;
			}

			if(routeDetail.getIsInvalidAddress()){
				logger.debug("SKIPPING - (GROSS CHECK) - THIS DROP IS LIKELY A BAD DROP - failed isInvalidAddress");
				continue;
			}

			lookAheadLatitude = routeDetail.getGpsLocationLatitude();
			lookAheadLongitude = routeDetail.getGpsLocationLongitude();

			lookAheadLocation = GPSUtils.convertToLocationFromGeoCode(lookAheadLatitude + "," + lookAheadLongitude);

			//=== TARGET AREA IS NOW AN ELLIPSOID WHERE THE MAIN AXIS IS targetAreaFrontDistance
			//=== AND THE MINOR AXIS IS targetAreaSideDistance. THE ELLIPSOID IS ORIENTED WITH THE MAJOR AXIS
			//=== PARALLEL TO THE DEVICE AZIMUTH
			deviceToLookAheadAzimuth = deviceLocation.bearingTo(lookAheadLocation);
			//=== CONVERT BEARING TO AZIMUTH
			deviceToLookAheadAzimuth = GPSUtils.normalizeBearingToAzimuth(deviceToLookAheadAzimuth);

			isThisAddressPointInTheTargetQuadrantSweep = false;

			//=== QUADRANT IS RELATIVE TO DEVICE AZIMUTH, ZERO QUAD IS ERRANT
			lookAheadQuadrantForRecord = GPSUtils.determineRelativeQuadrant_Common(deviceForwardAzimuth, deviceToLookAheadAzimuth);
			lookAheadQuadrantForRecordLabel = GPSUtils.getRelativeQuadrantEnglishString(lookAheadQuadrantForRecord);
			//logger.debug("SUPER SET RELATIVE look ahead drop quadrant is " + lookAheadQuadrantForRecordLabel);
			lookAheadQuadrantForDeliverLabel = GPSUtils.getRelativeQuadrantEnglishString(deliveryTargetQuads);
			//logger.debug("SUPER SET RELATIVE look ahead delivery quadrant is " + lookAheadQuadrantForDeliverLabel);

			if((deliveryTargetQuads & lookAheadQuadrantForRecord) > 0){
				isThisAddressPointInTheTargetQuadrantSweep = true;
			}

			//=== FILTER OUT ANYTHING NOT IN OUR CURRENT QUADRANTs FOR LOOK AHEAD DIRECTION
			if(!isThisAddressPointInTheTargetQuadrantSweep){
				//===IGNORE THIS ONE
				logger.debug("SKIPPING - THIS DROP'S QUAD " + lookAheadQuadrantForRecordLabel + "IS NOT IN THE DELIVERY QUADs " + lookAheadQuadrantForDeliverLabel);
				//logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");

				continue;
			}
			else{
				logger.debug("KEEPING - THIS DROP'S QUAD " + lookAheadQuadrantForRecordLabel + "IS WITHIN THE DELIVERY QUADs " + lookAheadQuadrantForDeliverLabel);
			}

			deviceToLookAheadDistance = deviceLocation.distanceTo(lookAheadLocation);

			if(deviceToLookAheadDistance > GlobalConstants.BAD_LOCATION_SANITY_DISTANCE_CHECK_METERS){
				//=== IGNORE, IT IS OUTSIDE OF THE GROSS ERROR DISTANCE CHECK
				logger.debug("SKIPPING - (GROSS CHECK) - THIS DROP IS FURTHER " + deviceToLookAheadDistance + " THAN THE GROSS ERROR CHECK DISTANCE " + GlobalConstants.BAD_LOCATION_SANITY_DISTANCE_CHECK_METERS);
				//logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				//isInvalidAddress = true;
				continue;
			}
			else{
				if(deviceToLookAheadDistance > longestTargetEllipseAxis && deviceToLookAheadDistance < longestExtendedEllipseAxis){
					logger.debug("SKIPPING - (GROSS CHECK) - THIS DROP's DISTANCE " + deviceToLookAheadDistance + " IS FURTHER THAN TARGET ELLIPSE DISTANCE " + longestTargetEllipseAxis + " AND SHORTER THAN LONGEST EXTENDED ELLIPSE DISTANCE " + longestExtendedEllipseAxis);
					//logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
					foundNextSequenceOutsideTaget = true;
					continue;
				}
				else{
					relativeEllipseAzimuthTheta = Math.abs(deviceToLookAheadAzimuth - deviceForwardAzimuth);

					relativeEllipseAzimuthTheta = GPSUtils.normalizeAzimuthToEllipseThetaAngle(relativeEllipseAzimuthTheta);
					distanceToTargetEllipseCircumferenceOnAzimuthTheta = GPSUtils.getDistanceToPointOnEllipseCircumferenceWithBearingUsingMajorMinorAxis(relativeEllipseAzimuthTheta, targetAreaFrontDistance, targetAreaSideDistance);

					//=== SEE IF THIS DELIVERY ADDRESS IS WITHIN THE DISTANCE TO THE TARGET/DELIVERY ELLIPSE CIRCUMFERENCE AT A GIVEN THETA BEARING
					if(distanceToTargetEllipseCircumferenceOnAzimuthTheta < deviceToLookAheadDistance){
						//=== POINT IS OUTSIDE THE ELLIPSE TARGET AREA
						logger.debug("SKIPPING - THIS DROP IS FURTHER " + deviceToLookAheadDistance +
									 " THAN DELIVERY ELLIPSE THETA DISTANCE " + distanceToTargetEllipseCircumferenceOnAzimuthTheta);
						//logger.debug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
						continue;
					}

					logger.debug("KEEPING - THIS DROP IS " + deviceToLookAheadDistance +
								 " WITHIN DELIVERY ELLIPSE THETA DISTANCE " + distanceToTargetEllipseCircumferenceOnAzimuthTheta);
				}
			}

			//=======================================================================================
			//=== ADD THE DELIVERED DROP TO THE RETURN ARRAY AND UPDATE THE DB FOR THIS ROUTE
			//=======================================================================================
			routeDetail.setDistance(deviceToLookAheadDistance);

			routeDetail.setIsInvalidAddress(isInvalidAddress);

			int deliveredFinal = deliveredFromRecord;
			int dndWasProcessedFinal = routeDetail.getDndWasProcessed();
			long deliveryTimeFinal = deliveredFromRecord;
			long listDisplayTimeFinal = routeDetail.getListDisplayTime();

			if(routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Must_Deliver.ordinal()//5
			   || routeDetail.getJobType() == GlobalConstants.JOB_TYPE.Delivery.ordinal())//6
			{
				//=== THIS IS A DELIVER ITEM THAT WAS PROCESSED TO RESIDE IN OUR TARGET DELIVERY ZONE
				deliveredFinal = isInvalidAddress ? 0 : 1;
				dndWasProcessedFinal = 0;
				listDisplayTimeFinal = System.currentTimeMillis();

				//=== DO NOT OVER WRITE THE DELIVERY DATE IF PREVIOUSLY DELIVERED
				if(deliveredFromRecord == 1){
					deliveryTimeFinal = deliveredFromRecord;
				}
				else{
					deliveryTimeFinal = deliveryTimeStampParameterIn;
				}
			}
			else{
				//=== THIS IS A DO NOT DELIVER ITEM THAT WAS PROCESSED TO RESIDE IN OUR TARGET DELIVERY ZONE
				//=== THE SAME AS A DELIVERY ITEM WOULD BE
				deliveredFinal = 0;
				dndWasProcessedFinal = 1;
				listDisplayTimeFinal = System.currentTimeMillis();
				deliveryTimeFinal = 0;

/*
				//=== DO NOT OVER WRITE THE DELIVERY DATE IF PREVIOUSLY DELIVERED
				if(routeDetail.getDndWasProcessed() == 1){
					if(deliveredFromRecord == 0){
						deliveryTimeFinal = deliveryTimeStampParameterIn;
					}
					else{
						deliveryTimeFinal = deliveredFromRecord;
					}
				}
				else{
					deliveryTimeFinal = deliveryTimeStampParameterIn;
				}
*/
			}

//			logger.debug("ADDING THIS DROP AS DELIVERED - deliveredFinal = " + deliveredFinal);
//			logger.debug("ADDING THIS DROP AS DELIVERED - deliveryTimeFinal = " + deliveryTimeFinal);
//			logger.debug("ADDING THIS DROP AS DELIVERED - dndWasProcessedFinal = " + dndWasProcessedFinal);
//			logger.debug("ADDING THIS DROP AS DELIVERED - route.getSequenceNew() = " + routeDetail.getSequenceNew());

			logger.debug("KEEPING - UPDATING DROP delivery AS " + deliveredFinal);

			routeDetail.setDeliveredTime(deliveryTimeFinal);
			routeDetail.setDelivered(deliveredFinal);
			routeDetail.setListDisplayTime(listDisplayTimeFinal);
			routeDetail.setDndWasProcessed(dndWasProcessedFinal);
			//=== IT HAS CHANGED SO SET IT TO UPLOAD
			routeDetail.setUploaded(0);
			routeDetail.setDeliveredLatitude(deviceLocation.getLatitude());
			routeDetail.setDeliveredLongitude(deviceLocation.getLongitude());

			routeDetail.buildProductTable();

			logger.debug("UPDATING SEQUENCING ROUTE DETAIL ITEM = " + routeDetail.getDeliveryResequencingInfo());

			routeDetail.updateDatabaseRecord();

			newRouteDetailsInDeliveryAreaToAdd.add(routeDetail);
		}    //end top while loop

		lookAheadRouteDetailCursor.close();

		Collections.sort(newRouteDetailsInDeliveryAreaToAdd, DeliveryItem.ORDERING_SEQUENCEID);

		if(foundNextSequenceOutsideTaget){
			lastMissedDropCheck = System.currentTimeMillis();
			logger.debug("FOUND NEXT DROP OUTSIDE OF TARGET DELIVERY AREA INSIDE EXTENDED LOOKAHEAD");
		}
		else if(newRouteDetailsInDeliveryAreaToAdd == null || newRouteDetailsInDeliveryAreaToAdd.size() < 1){
			logger.debug("NO DROPS FOR DELIVERY/NO NEXT DROP FOUND, CHECKING MISSED/SKIPPED DROPS");

			int lookAheadSkippedSequence = 0;
			long currentTimeInMillis = System.currentTimeMillis();
			if(currentTimeInMillis - lastMissedDropCheck > 60 * 1000){
				logger.debug("SEQUENCE NUMBER MISSED/SKIPPED DROPS TIMER EXPIRED - CHECKING NOW");

				ArrayList<DeliveryItem> skippedDrops = fetchSkippedRouteDetailsForJobDetailIdInCurrentDeliveryZone_Sequenced(
						jobDetailId,
						extendedAreaFrontDistance,
						extendedAreaSideDistance,
						deliveryTargetQuads,
						deviceLocation,
						deviceForwardAzimuth,
						lastDeliveredSequenceId,
						sequenceIdInterval);

				if(skippedDrops != null && skippedDrops.size() > 0){
					logger.debug("SEQUENCE NUMBER FOUND MISSED/SKIPPED DROPS");

					//=== MAY HAVE TO LOOK AT THE RETURNED SKIPPED DROPS TO DETERMINE
					//=== WHAT IS GOING ON

					lookAheadSkippedSequence = skippedDrops.get(0).getSequence();
					logger.debug("LOWEST SEQUENCE NUMBER FOUND MISSED/SKIPPED DROPS = " + lookAheadSkippedSequence);

					if(lookAheadSkippedSequence == lastDeliveredSequenceId){
						logger.debug("LOWEST SEQUENCE NUMBER FOUND = LAST DELIVERED SEQUENCE NUMBER SO WE HAVEN'T MISSED IT, HAVE NOT GOTTEN TO IT YET");
					}
					else if(lookAheadSkippedSequence >= lastDeliveredSequenceId + (sequenceIdInterval * 2)){
						logger.debug("LOWEST SEQUENCE NUMBER FOUND HAS SKIPPED AT LEAST ONE DROP PAST LAST DELIVERED SEQUENCE NUMBER");

						//===SEND A BROADCAST TO RouteDetailsRightSideFragmentMerged saying that the driver
						//===may have missed some points in the sequence of route drops
						logger.debug("SENDING SEQUENCE NUMBER SKIPPED ALERT FOR LOWEST SEQUENCE = " + lookAheadSkippedSequence + " TO LIST OF LOOK AHEADS AS POSSIBLE SKIP FROM LAST DELIVERED SEQUENCE NUMBER " + lastDeliveredSequenceId);
						Intent dropSkippedIntent = new Intent(GlobalConstants.INTENT_SKIPPED_DELIVERY);
						dropSkippedIntent.putExtra("ALERT_MESSAGE", "test");
						dropSkippedIntent.putExtra("ALERT_DATA", lookAheadSkippedSequence);
						dropSkippedIntent.setPackage(CTApp.appContext.getPackageName());//"com.agilegeodata.carriertrack");

						CTApp.appContext.sendBroadcast(dropSkippedIntent);
					}
				}
				else{
					logger.debug("SKIPPED SEQUENCE NUMBER NOT FOUND MISSED/SKIPPED DROPS");
				}

				lastMissedDropCheck = currentTimeInMillis;
			}
			else{
				logger.debug("SEQUENCE NUMBER MISSED/SKIPPED DROPS TIMER NOT EXPIRED - NO CHECK");
			}
		}
		else{
			logger.debug("NO SEQUENCE NUMBER SKIPPED TO ALERT FOR (2)");
			lastMissedDropCheck = System.currentTimeMillis();
		}

		return newRouteDetailsInDeliveryAreaToAdd;
	}

	public final ArrayList<DeliveryItem> fetchAllRouteDetailsForJobDetailId_Sequenced(int jobDetailId){
		logger.debug("ENTER>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		String query1 = "select * from addressdetaillist where jobdetailid=" + jobDetailId +
						" order by sequence asc ";
		logger.debug("QUERY = " + query1);

		Cursor routeDetailCursor = null;
		try{
			routeDetailCursor = getWritableDatabase().rawQuery(query1, null);

			if(routeDetailCursor == null){
				logger.debug("routeDetailCursor = NULL");
			}
			else{
				logger.debug("routeDetailCursor.getColumnCount() = " + routeDetailCursor.getColumnCount());
			}

			routeDetailCursor.moveToFirst();//.moveToNext();
		}
		catch(SQLException e){
			logger.error("EXCEPTION++++++++++", e);
		}

		ArrayList<DeliveryItem> routeDetailsArrayList = new ArrayList<DeliveryItem>();

		while(routeDetailCursor.moveToNext()){
			DeliveryItem routeDetail = new DeliveryItem(routeDetailCursor);
//			logger.debug(routeDetail.getDeliveryResequencingInfo());

			routeDetailsArrayList.add(routeDetail);
		}    //end top while loop

		routeDetailCursor.close();

		return routeDetailsArrayList;
	}

	public HashMap<Integer, Integer> fetchNumAddressesByJobDetailId_Common(int jobDetailId){
		int count = 0;
		Integer key = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		Cursor cur = null;

		try{
			cur = getWritableDatabase().rawQuery(QUERY_NUM_ADDRESSES_BY_JOBDETAILID.replace("%jdid%", jobDetailId + ""), null);

			while(cur.moveToNext()){
				key = cur.getInt(0);
				count = cur.getInt(1);
				map.put(key, count);
			}
		}
		catch(SQLException e){
			//logger.error("exception "+ e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return map;
	}

	public ArrayList<String> fetchAllDeletedRoutesJobDetails_Common(){
		// Select jobdetailid from routelist where deleted=1
		ArrayList<String> list = new ArrayList<String>();
		Cursor cur = fetchSingleFieldFromTableByFieldNameAndParameterInOrder_Common(
				DBHelper.DB_T_ROUTELIST, KEY_JOBDETAILID, KEY_DELETED,
				GlobalConstants.DEF_DELETED, KEY_JOBDETAILID, "asc", 1000);

		if(cur != null){
			while(cur.moveToNext()){
				list.add(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID)));
			}

			cur.close();
		}

		return list;
	}

	public ArrayList<String> fetchAllPhotosToBeDeletedByJobDetails_Common(
			String jDID, long time){
		ArrayList<String> list = new ArrayList<String>();
		Cursor cur = null;
		String query = "select " + KEY_FILEPATH
					   + " from photos where jobdetailid='" + jDID + "' AND "
					   + KEY_TIMESTAMP + "=" + time + " AND uploaded=2";

		try{
			// logger.debug(query);
			cur = getWritableDatabase().rawQuery(query, null);
		}
		catch(Exception e){
		}

		if(cur != null){
			while(cur.moveToNext()){
				list.add(cur.getString(cur.getColumnIndexOrThrow(KEY_FILEPATH)));
			}

			cur.close();
		}

		return list;
	}

	public PhotoDetail fetchPhotoByJobDetailAndDeliveryId_Common(
			String jDID, String deliveryid){
		ArrayList<String> list = new ArrayList<String>();
		Cursor cur = null;
		String query = "select * "
					   + " from photos where jobdetailid='" + jDID + "' AND "
					   + KEY_DELIVERYID + "=" + deliveryid;

		PhotoDetail photoDetail = new PhotoDetail();
		try{
			// logger.debug(query);
			cur = getWritableDatabase().rawQuery(query, null);

			if(cur != null){
				//=== SHOULD ONLY BE ONE
				cur.moveToNext();
				int count = cur.getCount();
//				logger.debug("CURSOR COUNT: " + count);

				if(count > 0){
					photoDetail.setDeliveryId(cur.getInt(cur.getColumnIndexOrThrow(KEY_DELIVERYID)));
					photoDetail.setFilePath(cur.getString(cur.getColumnIndexOrThrow(KEY_FILEPATH)));
					photoDetail.setId(cur.getInt(cur.getColumnIndexOrThrow(KEY_ID)));
					photoDetail.setJobDetailId(cur.getInt(cur.getColumnIndexOrThrow(KEY_JOBDETAILID)));
					photoDetail.setLat(cur.getDouble(cur.getColumnIndexOrThrow(KEY_LAT)));
					photoDetail.setLon(cur.getDouble(cur.getColumnIndexOrThrow(KEY_LONG)));
					photoDetail.setPhotoDate(cur.getLong(cur.getColumnIndexOrThrow(KEY_PHOTODATE)));
					photoDetail.setPhotoNotes(cur.getString(cur.getColumnIndexOrThrow(KEY_PHOTONOTES)));
					photoDetail.setUploadBatchId(cur.getInt(cur.getColumnIndexOrThrow(KEY_UPLOADBATCHID)));
					photoDetail.setUploaded(cur.getInt(cur.getColumnIndexOrThrow(KEY_UPLOADED)));
				}

				cur.close();
			}
		}
		catch(Exception e){
			logger.error("EXCEPTION : " + e.getMessage());
		}

		return photoDetail;
	}

	public void cleanBreadCrumbRecords_Common(String jDID){
		Calendar now = Calendar.getInstance();
		now.set(Calendar.DAY_OF_YEAR, -15);
		long time = now.getTimeInMillis();

		// logger.debug("Deleting records before : " +
		// DateUtil.calcDateFromTime(time, null));

		doExecQuery_Common("delete from breadcrumbs where jobdetailid='" + jDID
						   + "' AND " + KEY_TIMESTAMP + "=" + time + " AND (uploaded=1)");
	}

	public ArrayList<String> cleanPhotoRecords_Common(String jDID){
		Calendar now = Calendar.getInstance();
		ArrayList<String> list = new ArrayList<String>();
		now.set(Calendar.DAY_OF_YEAR, -15);
		long time = now.getTimeInMillis();

		list = fetchAllPhotosToBeDeletedByJobDetails_Common(jDID, time);
		doExecQuery_Common("delete from photos where jobdetailid='" + jDID + "' AND "
						   + "photodate" + "=" + time + " AND (uploaded=2)");

		return list;
	}

	public void cleanLoginRecords_Common(){
		doExecQuery_Common("delete from logins where uploaded=1");
	}

	public void cleanAddressDetailsForUploadedOrNullDeliveryRecords_Common(String jDID){
		int count = fetchCountByQuery_Common("select count(*) from addressdetaillist where jobdetailid='"
											 + jDID + "' AND (uploaded=1 OR (delivered is null or delivered=0))");
		// logger.debug("Count for jobdetail id before delete is : " + count);

		doExecQuery_Common("delete from addressdetaillist where jobdetailid='" + jDID
						   + "' AND uploaded=1");
	}

	public void cleanAddressDetailsProductsForUploadedRecords_Common(String jDID){
		int count = fetchCountByQuery_Common("select count(*) from addressdetailproducts where jobdetailid='"
											 + jDID + "' AND uploaded=1");
		// logger.debug("Count for jobdetail id before delete is : " + count);

		doExecQuery_Common("delete from addressdetaillist where jobdetailid='" + jDID
						   + "' AND (uploaded=1 OR (delivered is null or delivered=0))");
	}

	public void cleanWorkActivityForUploadedRecords_Common(String jDID){
		int count = fetchCountByQuery_Common("select count(*) from workactivity where jobdetailid='"
											 + jDID + "' AND uploaded=1");
		// logger.debug("Count for jobdetail id before delete is : " + count);

		doExecQuery_Common("delete from addressdetaillist where jobdetailid='" + jDID
						   + "' AND (uploaded=1 OR (delivered is null or delivered=0))");
	}

	public void cleanSignaturesForUploadedRecords_Common(String jDID){
		int count = fetchCountByQuery_Common("select count(*) from signatures where jobdetailid='"
											 + jDID + "' AND uploaded=1");
		// logger.debug("Count for jobdetail id before delete is : " + count);

		doExecQuery_Common("delete from addressdetaillist where jobdetailid='" + jDID
						   + "' AND (uploaded=1 OR (delivered is null or delivered=0))");
	}

	public void cleanUploadedRouteListActivityRecords_Common(String jDID){
		doExecQuery_Common("Delete from routelistactivity where jobdetailid='" + jDID
						   + "' AND uploaded=1");
	}

	public void cleanStreetSummaryRecords_Common(String jDID){
		doExecQuery_Common("delete from " + DB_T_STREETSUMMARYLIST
						   + " where jobdetailid='" + jDID + "'");
	}

	public void cleanRouteListRecords_Common(String jDID){
		doExecQuery_Common("delete from routelist where jobdetailid='" + jDID + "'");
	}

//	public void updateAddressDetailListBarcode_Common(long deliveryId, String barcode){
//		String query = "update addressdetaillist set uploaded=0 "
//					   + ", barcode='"
//					   + barcode
//					   + "'"
//					   + " where deliveryid='" + deliveryId + "'";
//		// logger.debug("------------------- Tools Query is: " + query);
//
//		doExecQuery_Common(query);
//	}

	public void updateAddressDetailProductsBarcode_Common(long deliveryId, String scancode, double latitude, double longitude){
		String query = "update addressdetailproducts set uploaded=0 "
					   + ", deliverydate='" + System.currentTimeMillis() + "'"

					   + ", deliverylatitude='" + latitude + "'"
					   + ", deliverylongitude='" + longitude + "'"

					   + " where deliveryid='" + deliveryId + "'"
					   + " and producttype = 'SCAN'"
					   + " and scancode='" + scancode + "'";
		logger.debug("------------------- Tools Query is: " + query);

		doExecQuery_Common(query);
	}

	public void updateAddressDetailListLatitudeLongitude_Common(long deliveryId,
																String lat, String longitude, long date){
		String query = "update addressdetaillist set uploaded=0 "
					   //+ date
					   + ", latnew='"
					   + lat
					   + "' , longnew='"
					   + longitude
					   + "' where deliveryid='" + deliveryId + "'";
		// logger.debug("------------------- Tools Query is: " + query);

		doExecQuery_Common(query);
	}

	public void updateAddressDetailListDeliveryInfo_Common(long deliveryId,
														   String delinfostatus2, String delinfoplacement2, long date){

		String query = "update addressdetaillist set uploaded=0, delinfostatus='"
					   + delinfostatus2
					   + "' , delinfoplacement='"
					   + delinfoplacement2
					   + "' where deliveryid='" + deliveryId + "'";
		// logger.debug("------------------- Tools Query is: " + query);

		doExecQuery_Common(query);
	}

	public void resetRouteDetailsSequenceNewToDefaultForJobDetailId_Common(int mJobId){

		String query = "update addressdetaillist set sequencenew=0" + " where jobdetailid='" + mJobId + "'";
		// logger.debug("------------------- Tools Query is: " + query);

		doExecQuery_Common(query);
	}

	public boolean getAddressDetailListDeliveryInfo_Common(long deliveryId,
														   StringBuilder info, StringBuilder Status){
		Cursor cur = null;
		boolean bRet = false;

		try{
			String query = "select * from addressdetaillist "
						   + "where deliveryid='" + deliveryId + "'";
			cur = getWritableDatabase().rawQuery(query, null);

			while(cur.moveToNext()){
				String strStatus = cur.getString(cur
														 .getColumnIndexOrThrow(DBHelper.KEY_DELINFOSTATUS));

				String strInfo = cur.getString(cur
													   .getColumnIndexOrThrow(DBHelper.KEY_DElINFOPLACEMENT));
				info.append(strInfo);
				Status.append(strStatus);
				bRet = true;
				break;
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return bRet;
	}

	public int fetchCountByQuery_Common(String query){
		int count = 0;
		Cursor cur = null;

		try{
			// logger.debug("Query: " + query);
			cur = getWritableDatabase().rawQuery(query, null);

			while(cur.moveToNext()){
				count = cur.getInt(0);
			}

		}
		catch(SQLException e){
			logger.error("exception " + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return count;
	}

	public int fetchLogicalTotalDeliveredCountByJobDetailId_Common(int jobDetailId){
		int count = 0;
		Cursor cur = null;

		try{
			String query = "select count(*) as count " +
						   "from addressdetaillist " +
						   "where jobDetailId=" + jobDetailId +
						   " and " +
						   "(((jobtype in (5, 6)) and delivered = 1)" +
						   " or " +
						   "(jobtype in (1, 2, 3, 4))" +
						   " or " +
						   "(sequence <= 0))";

			// logger.debug("Query: " + query);
			cur = getWritableDatabase().rawQuery(query, null);

			while(cur.moveToNext()){
				count = cur.getInt(0);
			}
		}
		catch(SQLException e){
			logger.error("exception " + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return count;
	}

	public int fetchTotalUploadedCountByJobDetailId_Common(int jobDetailId){
		int count = 0;
		Cursor cur = null;

		try{
			String query = "select count(*) as count from addressdetaillist where jobDetailId ="
						   + jobDetailId + " and " + " uploaded=1 ";
			// logger.debug("Query: " + query);
			cur = getWritableDatabase().rawQuery(query, null);

			while(cur.moveToNext()){
				count = cur.getInt(0);
			}
		}
		catch(SQLException e){
			count = -1;
			logger.error("------------------- exception " + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return count;
	}

	public String fetchItemValueByItemName_Common(String itemName){
		String value = null;
		Cursor cur = null;

		try{
			cur = getWritableDatabase().rawQuery(
					"select itemvalue from itemvalues where  itemname like '"
					+ itemName + "'", null);

			while(cur.moveToNext()){
				value = cur.getString(0);
			}
		}
		catch(SQLException e){
			logger.error("fetchItemValueByItemName() : exception : " + e);
		}
		catch(Exception e){
			logger.error("fetchItemValueByItemName() : exception : " + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return value;
	}

	public int fetchCountFromTable_Common(String tableName){
		int count = 0;
		Cursor cur = null;

		try{
			cur = getWritableDatabase().rawQuery("select count(*) as count from " + tableName,
												 null);

			while(cur.moveToNext()){
				count = cur.getInt(0);
			}

		}
		catch(SQLException e){
			e.printStackTrace();
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return count;
	}

	public int fetchMaxIdFromTable_Common(String tableName){
		int id = -1;
		Cursor cur = null;

		try{
			cur = getWritableDatabase().rawQuery("select max(_id) as maxid from " + tableName,
												 null);

			while(cur.moveToNext()){
				id = cur.getInt(cur.getColumnIndexOrThrow("maxid"));
				break;
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return id;
	}

	public void deleteFromTable_Common(String tableName, String key,
									   String value){
		try{
			String[] args = new String[1];
			args[0] = value;
			key = key + "=?";
			int num = getWritableDatabase().delete(tableName, key, args);

			logger.debug("deleteFromTable() : Number deleted: " + num);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Return a Cursor over the string list of items from a single field in a
	 * table if fieldname and parameter is null then it will not have a where
	 * clause.
	 * @return Cursor
	 */
	public Cursor fetchSingleFieldFromTableByFieldNameAndParameterInOrder_Common(
			String tableName, String returnField, String fieldName,
			String parameter, String orderby, String ascOrDesc, int limit){

		String query = "select " + returnField + " from " + tableName
					   + " where " + fieldName + "='" + parameter + "' order by "
					   + orderby + " " + ascOrDesc + " limit " + limit;

		if(fieldName == null || parameter == null){
			query = "select " + returnField + " from " + tableName
					+ " order by " + orderby + " " + ascOrDesc + " limit "
					+ limit;
		}

		Cursor c = null;

		try{
			// logger.debug(query);
			c = getWritableDatabase().rawQuery(query, null);
			// //logger.debug("Cursor is: " + c.getCount());
			return c;
		}
		catch(Exception e){
			return null;
		}
	}

	/**
	 * Returns only active routes
	 * @return Cursor
	 */
	public Cursor fetchAllActiveRoutes_Common(){
		// TODO: Brad, all fields must be filled with default values
		String query;
		if(CTApp.operationsMode.equals(GlobalConstants.OPERATIONS_MODE.SEQUENCING)){
			query = "SELECT * FROM routelist WHERE (deleted IS NULL OR deleted !=1) and routetype = 'SEQUENCED' or routetype = 'UNSEQ' ORDER BY "
					+ DBHelper.KEY_JOBID + " " + "asc";
		}
		else{
			query = "SELECT * FROM routelist WHERE (deleted IS NULL OR deleted !=1) ORDER BY "
					+ DBHelper.KEY_JOBID + " " + "asc";
		}

		Cursor c = getWritableDatabase().rawQuery(query, null);

		return c;
	}

	/**
	 * Return a Cursor over the list of items
	 * @return Cursor
	 */

	public Cursor fetchAllFromTableInOrder_Common(String tableName,
												  String orderby, String ascOrDesc){
		if(ascOrDesc == null){
			ascOrDesc = "asc";
		}

		String query = "select * from " + tableName + " order by " + orderby
					   + " " + ascOrDesc;
		//logger.debug(TAG,, query);
		Cursor c = getWritableDatabase().rawQuery(query, null);

		return c;
	}

	public Cursor fetchAllFromTableByFieldNameAndParameterInOrderWildCard_Common(
			String tableName, String fieldName, String parameter,
			String orderby, String ascOrDesc){
		String query = "select * from " + tableName + " where " + fieldName
					   + " like '%" + parameter + "%' order by " + orderby + " "
					   + ascOrDesc;

		if(fieldName == null || parameter == null){
			query = "select * from " + tableName + " order by " + orderby + " "
					+ ascOrDesc;
		}

		Cursor c = null;

		try{
			//logger.debug(TAG,, query);
			c = getWritableDatabase().rawQuery(query, null);
			// logger.debug("Cursor is: " + c.getCount());
		}
		catch(Exception e){

		}

		return c;
	}

	public Cursor fetchAllFromTableByFieldNameAndParameterInOrder_Common(
			String tableName, String fieldName, String parameter,
			String orderby, String ascOrDesc, int limit){

		logger.debug("CTApp.operationsMode = " + CTApp.operationsMode);
		String query = "select * from " + tableName + " where " + fieldName
					   + "='" + parameter + "' order by " + orderby + " " + ascOrDesc
					   + " limit " + limit;

		if(fieldName == null || parameter == null){
			query = "select * from " + tableName + " order by " + orderby + " "
					+ ascOrDesc + " limit " + limit;
		}
		logger.debug("query = " + query);

		Cursor c = null;

		try{
			//logger.debug(query);

			c = getWritableDatabase().rawQuery(query, null);

			//logger.debug("Cursor is: " + c.getCount());
		}
		catch(Exception e){
			logger.error("e: " + e);
		}

		return c;
	}

	public Cursor fetchAllAddressesForUpload_Common(){

		logger.debug("CTApp.operationsMode = " + CTApp.operationsMode);

		String query = "select * from addressdetaillist " +
					   "where uploaded is not NULL and uploaded = 0 " +
					   "order by sequencenew asc limit 100";

		Cursor c = null;

		try{
			//logger.debug(query);

			c = getWritableDatabase().rawQuery(query, null);

			//logger.debug("Cursor is: " + c.getCount());
		}
		catch(Exception e){
			logger.error("e: " + e);
		}

		return c;
	}

	public Cursor fetchAllFromTableByFieldNamesAndParametersInOrderWildCard_Random(
			String tableName, String[] fieldName, String[] parameter,
			boolean[] isWildCard, String orderby, String ascOrDesc, int limit){

		String query = "select * from " + tableName + " where ";
		String whereClause = "";

		for(int i = 0; i < fieldName.length; i++){
			if(isWildCard[i]){
				whereClause = whereClause + " " + fieldName[i] + " like '%"
							  + parameter[i] + "%' and ";
			}
			else{
				whereClause = whereClause + " " + fieldName[i] + "='"
							  + parameter[i] + "' and ";
			}

		}

		if(whereClause.length() > 4){
			whereClause = whereClause.substring(0, whereClause.length() - 4);
		}

		query = query + whereClause + " order by " + orderby + " " + ascOrDesc
				+ " limit " + limit;

		Cursor c = null;

		try{
			// //logger.debug(TAG,, query);
			c = getWritableDatabase().rawQuery(query, null);
			// logger.debug("Cursor is: " + c.getCount());
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return c;
	}

	/**
	 * create or update a record by table
	 * @param initialValues
	 * @param tableName     //@param tableKey
	 *                      //@param lookup
	 * @return _id
	 */

	public long updateRecordWitInternalId_CheckUsage_Common(ContentValues initialValues,
															String tableName, int id){
		long val = -1;

		try{
			//=== if you need to do the lookup, check to see if the item exists, if

			//=== we're doing an update...
			//logger.debug("updateRecordWitInternalId() : Doing update");

			val = getWritableDatabase().update(tableName, initialValues, DBHelper.KEY_ID + "="
																		 + id, null);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return val;
	}

	/**
	 * create or update a record by table
	 * @param initialValues
	 * @param tableName
	 * @param tableKey
	 * @param lookup
	 * @return _id
	 */

	public long createRecord_Common(ContentValues initialValues, String tableName, String tableKey, boolean lookup){

		long rowId = -1;

		try{
			//=== if you need to do the lookup, check to see if the item exists, if
			//=== so then do an update...
			//logger.debug(TAG,"createRecord() : Initial Values ID That we are looking for: "
			//					+ initialValues.get(tableKey));
			int iLid = -1;

			if(lookup){
				iLid = fetchIntIdForTableById_Common(tableName, tableKey,
													 initialValues.getAsInteger(tableKey));
			}

			if(iLid == -1){
				//logger.debug("createRecord() : Doing insert");

				initialValues.remove(DBHelper.KEY_ID);
				rowId = getWritableDatabase().insert(tableName, null, initialValues);
			}
			else{
				//=== we're doing an update...
				//logger.debug("createRecord() : Doing update");

				rowId = getWritableDatabase().update(tableName, initialValues, DBHelper.KEY_ID + "="
																			   + iLid, null);
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}

		return rowId;
	}

	/**
	 * @param tblName
	 * @param fldName
	 * @param externId
	 * @return
	 */
	public int fetchIntIdForTableById_Common(String tblName, String fldName,
											 long externId){
		int _id = -1;
		Cursor cur = null;

		try{
			cur = getWritableDatabase().query(tblName, new String[]{DBHelper.KEY_ID}, fldName
																					  + "=" + externId, null, null, null, null);

			while(cur.moveToNext()){
				_id = cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID));
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return _id;
	}

	/**
	 * @param tblName
	 * @param fldName    //* @param externId
	 * @param fieldValue
	 * @return
	 */
	public int fetchIntIdForTableByStringField_CheckUsage(String tblName,
														  String fldName, String fieldValue){
		int _id = -1;
		Cursor cur = null;

		try{
			tblName.trim();
			fldName.trim();
			fieldValue.trim();

			if(tblName.length() > 1 && fldName.length() > 1 && fieldValue.length() > 1){
				cur = getWritableDatabase().query(tblName, new String[]{DBHelper.KEY_ID},
												  fldName + " like '" + fieldValue + "'", null, null,
												  null, null);
			}

			while(cur.moveToNext()){
				_id = cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID));
			}
		}
		catch(SQLException e){
			logger.error(e.getMessage(), e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return _id;
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	public boolean checkDataBase_Common(){
		String dir = FileUtils.getAppDirectoryForDataBaseFiles();
		String myPath = dir + GlobalConstants.DATABASE_NAME;
		File dbfile = new File(myPath);
		return dbfile.exists();
	}

	public int getLastJobDetailIdUsed_Common(){
		int lastJobDetailIdUsed = -1;
		String result = fetchItemValueByItemName_Common(GlobalConstants.PREF_LAST_NOT_FINISHED_JOBDETAIL_ID);
		if(result != null){
			lastJobDetailIdUsed = Integer.valueOf(result);
		}
		else{
			lastJobDetailIdUsed = 0;
		}

		return lastJobDetailIdUsed;
	}

	public void setLastJobDetailIdUsed_Common(int lastJobDetailIdUsed){
		String value = lastJobDetailIdUsed + "";
		createItemValueRecord_Common(GlobalConstants.PREF_LAST_NOT_FINISHED_JOBDETAIL_ID, value);
	}

	public boolean getUseTestData_Common(){
		String useTestDataString = fetchItemValueByItemName_Common(GlobalConstants.PREF_USE_TEST_DATA);
		boolean useTestData = false;
		if(useTestDataString == null){
			useTestData = false;
			setUseTestData_Common(useTestData);
		}
		else if(useTestDataString.equals("true".toLowerCase())){
			useTestData = true;
		}

		return useTestData;
	}

	public void setUseTestData_Common(boolean useTestData){
		String useTestDataString = "false";
		if(useTestData){
			useTestDataString = "true";
		}
		createItemValueRecord_Common(GlobalConstants.PREF_USE_TEST_DATA, useTestDataString);
	}

	public boolean getUseMaps_Common(){
		String useMapsString = fetchItemValueByItemName_Common(GlobalConstants.PREF_USE_MAPS);
		boolean useMaps = false;
		if(useMapsString == null){
			useMaps = true;
		}
		else if(useMapsString.equals("true".toLowerCase())){
			useMaps = true;
		}

		return useMaps;
	}

	public void setUseMaps_Common(boolean useMaps){
		String useMapsString = "false";
		if(useMaps){
			useMapsString = "true";
		}
		createItemValueRecord_Common(GlobalConstants.PREF_USE_MAPS, useMapsString);
	}

	public boolean getUseSpeech_Common(){
		String useSpeechString = fetchItemValueByItemName_Common(GlobalConstants.PREF_USE_SPEECH);
		boolean useSpeech = false;
		if(useSpeechString == null){
			useSpeech = true;
		}
		else if(useSpeechString.equals("true".toLowerCase())){
			useSpeech = true;
		}

		return useSpeech;
	}

	public void setUseSpeech_Common(boolean useSpeech){
		String useSpeechString = "false";
		if(useSpeech){
			useSpeechString = "true";
		}
		createItemValueRecord_Common(GlobalConstants.PREF_USE_SPEECH, useSpeechString);
	}

	public void createItemValueRecord_Common(String itemName, String itemValue){
		try{
			ItemValue itemVal = new ItemValue();
			itemVal.setItemName(itemName);
			itemVal.setItemValue(itemValue);

			// logger.debug("Count before update: "
			// +DBHelper.fetchCountFromTable(DB_T_ITEMVALUES));
			int id = fetchIntIdForTableByStringField_CheckUsage(DB_T_ITEMVALUES, DBHelper.KEY_ITEMNAME, itemName);
			itemVal.setId(id);

			if(id == -1){
				createRecord_Common(itemVal.createIntialValues(), DB_T_ITEMVALUES,
									DBHelper.KEY_ITEMNAME, false);
			}
			else{
				updateRecordWitInternalId_CheckUsage_Common(itemVal.createIntialValues(),
															DB_T_ITEMVALUES, id);
			}

			// logger.debug("Count after update: "
			// +DBHelper.fetchCountFromTable(DB_T_ITEMVALUES));
		}
		catch(Exception e){
			logger.error("exception " + e);
		}
	}

	//================================================================================================
	//=== STUFF ADDED FROM VERSION 2.X
	//================================================================================================
	private HashMap<Integer, ArrayList<DeliveryItem>> createRouteDetailListBySummary_Random(Cursor cur){
		HashMap<Integer, ArrayList<DeliveryItem>> map = new HashMap<Integer, ArrayList<DeliveryItem>>();
		ArrayList<DeliveryItem> rs = new ArrayList<DeliveryItem>();

		while(cur.moveToNext()){
			DeliveryItem r = new DeliveryItem(cur);

			rs.add(r);
		}

		int size = rs.size();
		for(int i = 0; i < size; i++){
			if(map.containsKey(rs.get(i).getSummaryId())){
				// //logger.debug("Map already contains the key"
				// + rs.get(i).getSummaryId());
				map.get(rs.get(i).getSummaryId()).add(rs.get(i));
			}
			else{
				// //logger.debug("Adding a new key to the map"
				// + rs.get(i).getSummaryId());
				ArrayList<DeliveryItem> rdNew = new ArrayList<DeliveryItem>();
				rdNew.add(rs.get(i));
				map.put(rs.get(i).getSummaryId(), rdNew);
			}
		}

		return map;
	}

	public final Cursor fetchRouteDetailsByGeoBoxAsCursor_Random(
			int jobDetailId,
			BoundingPointMatrix boundingBox){

		String latBetweenPhrase = " and lat between ";
		if(boundingBox.getTopLeft().getLatitude() <= boundingBox.getBottomRight().getLatitude()){
			latBetweenPhrase = latBetweenPhrase + boundingBox.getTopLeft().getLatitude() + " and "
							   + boundingBox.getBottomRight().getLatitude();
		}
		else{
			latBetweenPhrase = latBetweenPhrase + boundingBox.getBottomRight().getLatitude()
							   + " and " + boundingBox.getTopLeft().getLatitude();
		}

		String longBetweenPhrase = " and long between ";
		if(boundingBox.getTopLeft().getLongitude() <= boundingBox.getBottomRight().getLongitude()){
			longBetweenPhrase = longBetweenPhrase + boundingBox.getTopLeft().getLongitude()
								+ " and " + boundingBox.getBottomRight().getLongitude();
		}
		else{
			longBetweenPhrase = longBetweenPhrase + boundingBox.getBottomRight().getLongitude()
								+ " and " + boundingBox.getTopLeft().getLongitude();
		}

		String query = "select * from addressdetaillist where jobdetailid="
					   + jobDetailId + latBetweenPhrase + " " + longBetweenPhrase;

		logger.debug(query);

		Cursor cur = null;

		try{
			cur = getWritableDatabase().rawQuery(query, null);
		}
		catch(SQLException e){
			logger.debug(e.getMessage(), e);
		}

		return cur;
	}

	public final ArrayList<DeliveryItem> fetchRouteDetailsByGeoBoxAsArrayList_Random(
			int jobDetailId,
			BoundingPointMatrix boundingBox,
			Location deviceLocation,
			float deviceForwardAzimuth){
		ArrayList<DeliveryItem> newRouteDetailsInDeliveryAreaToAdd = new ArrayList<DeliveryItem>();

		String latBetweenPhrase = " and lat between ";
		if(boundingBox.getTopLeft().getLatitude() <= boundingBox.getBottomRight().getLatitude()){
			latBetweenPhrase = latBetweenPhrase + boundingBox.getTopLeft().getLatitude() + " and "
							   + boundingBox.getBottomRight().getLatitude();
		}
		else{
			latBetweenPhrase = latBetweenPhrase + boundingBox.getBottomRight().getLatitude()
							   + " and " + boundingBox.getTopLeft().getLatitude();
		}

		String longBetweenPhrase = " and long between ";
		if(boundingBox.getTopLeft().getLongitude() <= boundingBox.getBottomRight().getLongitude()){
			longBetweenPhrase = longBetweenPhrase + boundingBox.getTopLeft().getLongitude()
								+ " and " + boundingBox.getBottomRight().getLongitude();
		}
		else{
			longBetweenPhrase = longBetweenPhrase + boundingBox.getBottomRight().getLongitude()
								+ " and " + boundingBox.getTopLeft().getLongitude();
		}

		String query = "select * from addressdetaillist where jobdetailid="
					   + jobDetailId + latBetweenPhrase + " " + longBetweenPhrase;

		logger.debug(query);

		try{
			Cursor lookAheadRouteDetailCursor = getWritableDatabase().rawQuery(query, null);

			while(lookAheadRouteDetailCursor.moveToNext()){
				DeliveryItem route = new DeliveryItem(lookAheadRouteDetailCursor);

				double lookAheadLatitude = lookAheadRouteDetailCursor.getDouble(lookAheadRouteDetailCursor.getColumnIndexOrThrow(DBHelper.KEY_LAT));
				double lookAheadLongitude = lookAheadRouteDetailCursor.getDouble(lookAheadRouteDetailCursor.getColumnIndexOrThrow(DBHelper.KEY_LONG));
				Location lookAheadLocation = GPSUtils.convertToLocationFromGeoCode(lookAheadLatitude + "," + lookAheadLongitude);
				route.setLocation(lookAheadLocation);

				double deviceToLookAheadDistance = deviceLocation.distanceTo(lookAheadLocation);
				route.setDistance(deviceToLookAheadDistance);

				newRouteDetailsInDeliveryAreaToAdd.add(route);
			}    //end top while loop
		}
		catch(SQLException e){
			logger.debug(e.getMessage(), e);
		}

		Collections.sort(newRouteDetailsInDeliveryAreaToAdd, DeliveryItem.ORDERING_DISTANCE_ASC);

		return newRouteDetailsInDeliveryAreaToAdd;
	}

	public final int getSequencingLastOrdinalSetForARouteWithJobDetailId_Random(int jobDetailId){
		String query = "select MAX(sequencenew) as sequencenew from addressdetaillist where jobdetailid=" + jobDetailId;
		//logger.debug(TAG,, query);

		Cursor cur = null;
		int lastSequencingOrdinal = 0;

		try{
			cur = getWritableDatabase().rawQuery(query, null);
			cur.moveToFirst();
			lastSequencingOrdinal = cur.getInt(cur.getColumnIndexOrThrow("sequencenew"));
			lastSequencingOrdinal = lastSequencingOrdinal < 1 ? 0 : lastSequencingOrdinal;
		}
		catch(SQLException e){
			logger.debug(e.getMessage(), e);
		}

		return lastSequencingOrdinal;
	}

	public final ArrayList<DeliveryItem> fetchRouteDetailsForJobDetailIdAsArrayList_Random(int jobDetailId){
		//logger.debug("$$$$$$$$$$$$ START");

		String query = "select * from addressdetaillist " +
					   "where jobdetailid=" + jobDetailId + " order by sequence asc";

		Cursor cur = null;
		ArrayList<DeliveryItem> deliveredOrDndWasProcessedLeftRouteDetails = new ArrayList<DeliveryItem>();

		try{
			cur = getWritableDatabase().rawQuery(query, null);
			//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");

			while(cur.moveToNext()){
				DeliveryItem route = new DeliveryItem(cur);

				double thisLatitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT));
				double thisLongitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG));
				Location thisLocation = GPSUtils.convertToLocationFromGeoCode(thisLatitude + "," + thisLongitude);
				route.setLocation(thisLocation);

				long listDisplayTimeFromRecord = cur.getLong(cur.getColumnIndexOrThrow("listdisplaytime"));
				if(listDisplayTimeFromRecord > 0){
					listDisplayTimeFromRecord = System.currentTimeMillis();
				}
				route.setListDisplayTime(listDisplayTimeFromRecord);

				deliveredOrDndWasProcessedLeftRouteDetails.add(route);
			}
		}
		catch(SQLException e){
			logger.debug(e.getMessage(), e);
		}

		return deliveredOrDndWasProcessedLeftRouteDetails;
	}

	public final LinkedHashMap<Integer, DeliveryItem> fetchRouteDetailsForJobDetailId_Random(int jobDetailId){
		//logger.debug("$$$$$$$$$$$$ START");

		String query = "select * from addressdetaillist " +
					   "where jobdetailid=" + jobDetailId;

		Cursor cur = null;
		LinkedHashMap<Integer, DeliveryItem> deliveredOrDndWasProcessedLeftRouteDetails = new LinkedHashMap<Integer, DeliveryItem>();

		try{
			cur = getWritableDatabase().rawQuery(query, null);
			//logger.debug("$$$$$$$$$$$$ FOUND " + cur.getCount() + " ITEMS");

			while(cur.moveToNext()){
				DeliveryItem route = new DeliveryItem(cur);

				double thisLatitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT));
				double thisLongitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG));
				Location thisLocation = GPSUtils.convertToLocationFromGeoCode(thisLatitude + "," + thisLongitude);
				route.setLocation(thisLocation);

				long listDisplayTimeFromRecord = cur.getLong(cur.getColumnIndexOrThrow("listdisplaytime"));
				if(listDisplayTimeFromRecord > 0){
					listDisplayTimeFromRecord = System.currentTimeMillis();
				}
				route.setListDisplayTime(listDisplayTimeFromRecord);

				deliveredOrDndWasProcessedLeftRouteDetails.put(route.getId(), route);
			}
		}
		catch(SQLException e){
			logger.debug(e.getMessage(), e);
		}

		return deliveredOrDndWasProcessedLeftRouteDetails;
	}

	public final ArrayList<DeliveryItem> fetchRouteDetailsWithinDeliveryArea_Random(
			int jobDetailId,
			double targetAreaFrontDistance,
			double targetAreaSideDistance,
			double targetAreaExtendedFrontDistance,
			double targetAreaExtendedSideDistance,
			ArrayList<DeliveryItem> cachedRouteMap,
			int deliveryTargetQuadrants,
			Location deviceLocation,
			double deviceForwardAzimuth,
			long deliveryTimeStampParameterIn){
//		logger.info(">>>>>>>>>>>>>>>>>>>>ENTER>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		LinkedHashMap<Integer, DeliveryItem> cachedRouteMapAsHashMap = new LinkedHashMap<Integer, DeliveryItem>();

		//==============================================================================================
		//=== IGNORE THESES SUMMARY LIST ITEMS for processing =========================
		//==============================================================================================
		Collection<DeliveryItem> cachedCurrentRouteItems = new ArrayList<>();
		for(int i = 0; i < cachedRouteMap.size(); i++){
			if(cachedRouteMap.get(i).getStatus() != GlobalConstants.ROUTE_DETAIL_STATUS_STREETSUMMARY){
				cachedCurrentRouteItems.add(cachedRouteMap.get(i));
				cachedRouteMapAsHashMap.put(cachedRouteMap.get(i).getId(), cachedRouteMap.get(i));
			}
		}
		Iterator<DeliveryItem> iteratorForCachedCurrentRouteItems = cachedCurrentRouteItems.iterator();
		//logger.info("ENTER : cachedCurrentRouteItems has " + cachedCurrentRouteItems.size() + " items");

		//====================================================================================================================
		//=== PRE-PROCESS THE CURRENT MASTER DISPLAY LIST
		//=== Loop through the current display elements and modify their distances and quadrants
		//====================================================================================================================
		while(iteratorForCachedCurrentRouteItems.hasNext()){
			//logger.info("##############CURRENT ROUTE DETAIL top of loop######################");

			DeliveryItem routeDetailFromDisplayList = iteratorForCachedCurrentRouteItems.next();
			logger.debug("VIRGIN drop - " + routeDetailFromDisplayList.getDeliveryResequencingInfo());
			//logger.info("CURRENT ROUTE DETAIL point address = " + routeDetailFromDisplayList.getGpsLocationAddressNumber() + " " + routeDetailFromDisplayList.getGpsLocationAddressStreet());

			double distanceToRouteDetailFromDeviceLocation = deviceLocation.distanceTo(routeDetailFromDisplayList.getLocation());

//			logger.info("CURRENT ROUTE DETAIL DEVICE Forward AZIMUTH = " + deviceForwardAzimuth);
//			logger.info("CURRENT ROUTE DETAIL AZIMUTH FromDeviceToThis = " + routeDetailFromDisplayList.getAzimuthFromDeviceToThis());
			logger.info("CURRENT ROUTE DETAIL distanceToRouteDetailFromDeviceLocation = " + distanceToRouteDetailFromDeviceLocation);

			//=== UPDATE CURRENT AND LAST RECORDED DISTANCES FROM THE DEVICE
			routeDetailFromDisplayList.setDistance(distanceToRouteDetailFromDeviceLocation);

			//=== PROCESS THE POINTS IN THE EXTENDED LOOK AHEAD REGION TO DETERMINE IF THEY SHOULD BE DISPLAYED.
			int dndWasProcessed = routeDetailFromDisplayList.getDndWasProcessed();
//			logger.info("CURRENT ROUTE DETAIL dndWasProcessed = " + dndWasProcessed);
			if(dndWasProcessed == 1){
				//=== IGNORE AND KEEP IN DISPLAY LIST
//				logger.info("CURRENT ROUTE DETAIL keeping a DND item in the display list");
				continue;
			}

			//=== NON-DELIVERED POINTS NOT IN THE TARGET QUADRANTS WILL BE REMOVED AFTER 30 seconds
			if(routeDetailFromDisplayList.getDelivered() == 1){
//				logger.info("CURRENT ROUTE DETAIL keeping a DELIVERED ITEM in the display list");
				continue;
			}

//			logger.info("CURRENT ROUTE DETAIL item is NOT DELIVERD and NOT a DND");

//			//===t his is actually the 'bearing from' the device location to this route detail location
			float deviceAzimuthToTargetLocation = deviceLocation.bearingTo(routeDetailFromDisplayList.getLocation());

			//=== CONVERT TO AZIMUTH
			deviceAzimuthToTargetLocation = GPSUtils.normalizeBearingToAzimuth(deviceAzimuthToTargetLocation);
//			//logger.info("CURRENT ROUTE DETAIL AZIMUTH FromDeviceToThis = " + deviceAzimuthToTargetLocation);

			//=== QUADRANT IS RELATIVE TO DEVICE AZIMUTH, ZERO QUAD IS ERRANT
			int lookAheadQuadrantForRecord = GPSUtils.determineRelativeQuadrant_Common(deviceForwardAzimuth, deviceAzimuthToTargetLocation);
			String lookAheadQuadrantForRecordLabel = GPSUtils.getRelativeQuadrantEnglishString(lookAheadQuadrantForRecord);
			logger.debug("DISPLAY SET RELATIVE drop quadrant is " + lookAheadQuadrantForRecordLabel);
			String lookAheadQuadrantForDeliverLabel = GPSUtils.getRelativeQuadrantEnglishString(deliveryTargetQuadrants);
			logger.debug("DISPLAY SET RELATIVE delivery quadrant(s) is " + lookAheadQuadrantForDeliverLabel);

			boolean isThisAddressPointInTheTargetQuadrantSweep = (deliveryTargetQuadrants & lookAheadQuadrantForRecord) > 0;

			//=== NOT dndWasProcessed AND
			//=== NOT delivered
			if(!isThisAddressPointInTheTargetQuadrantSweep){
				//=== THIS WILL KEEP A NON-DELIVERED POINT IN THE EXTENDED AREA ON THE SCREEN FOR 15 SECONDS
				//=== BEFORE REMOVING FROM DISPLAY
				long displayStart = routeDetailFromDisplayList.getListDisplayTime();
//					logger.info("CURRENT ROUTE DETAIL item displayStart = " + displayStart);
				long displayAge = System.currentTimeMillis() - displayStart;
//				logger.info("CURRENT ROUTE DETAIL item displayAge = " + displayAge);

				if(displayAge > 30000){
					//=== THIS ADDRESS POINT IS UNDELIVERED AND not a non-delivery item that was processed and
					//=== NOW NOT IN OUR TARGET QUADRANTs
//					logger.info("CURRENT ROUTE DETAIL item displayAge = " + displayAge + " SO ITEM IS REMOVED FROM DISPLAY LIST");

					updateRouteDetailsListDisplayTimeNew_Random(routeDetailFromDisplayList.getId(), 0);
					iteratorForCachedCurrentRouteItems.remove();
				}
			}
		}

//		logger.info("##############CURRENT ROUTE DETAIL loop finished######################");
		logger.info("ENTER : cachedCurrentRouteItems NOW has " + cachedCurrentRouteItems.size() + " items");

		//====================================================================================================================
		//=== Loop through the cursor, determine if the point is in the extended look ahead area or the target delivery area.
		//=== Then determine if the point is in the correct quadrant, has an acceptable range distance, and should be displayed or removed from display
		//====================================================================================================================

		//=== INCREASE targetAreaFrontDistance TO ENLARGE THE LOOK AHEAD DISTANCE TO GET ADDRESSES BEYOND THE DELIVERY TARGET ZONE OF targetAreaFrontDistance
		BoundingPointMatrix lookAheadBoundingBox =
				new BoundingPointMatrix(deviceLocation.getLatitude() + "," + deviceLocation.getLongitude(),
										(int) targetAreaExtendedFrontDistance,
										(int) targetAreaExtendedSideDistance,
										(float) deviceForwardAzimuth);

//		logger.info("SUPER SET lookAheadBoundingBox = topRIGHT=" + lookAheadBoundingBox.getTopRight().toString() +
//						" topLEFT=" + lookAheadBoundingBox.getTopLeft().toString() +
//						" bottomLEFT=" + lookAheadBoundingBox.getBottomLeft().toString() +
//						" bottomRIGHT=" + lookAheadBoundingBox.getBottomRight().toString()	);

		//=== GET A LARGER THAN THE TARGET LOOK AHEAD AREA GEO-BOUNDED BOX TO NARROW THE SET OF POSSIBLE DELIVERY ADDRESSES
		Cursor lookAheadRouteDetailCursor = fetchRouteDetailsByGeoBoxAsCursor_Random(jobDetailId, lookAheadBoundingBox);
		logger.info("lookAheadRouteDetailCursor SUPER SET COUNT has " + lookAheadRouteDetailCursor.getCount() + " items");

		ArrayList<DeliveryItem> newRouteDetailsInDeliveryAreaToAdd = new ArrayList<DeliveryItem>();

//		logger.info("##############SUPER SET loop START ######################");
		while(lookAheadRouteDetailCursor.moveToNext()){
//			logger.info("##############SUPER SET top of loop ######################");
			int recordIdFromRecord = lookAheadRouteDetailCursor.getInt(lookAheadRouteDetailCursor.getColumnIndexOrThrow(DBHelper.KEY_ID));
//			logger.info("SUPER SET recordIdFromRecord = " + recordIdFromRecord);

			String lookAheadStreetNumber = lookAheadRouteDetailCursor.getString(lookAheadRouteDetailCursor.getColumnIndexOrThrow(DBHelper.KEY_ADDRESS_NUMBER));
			String lookAheadStreetName = lookAheadRouteDetailCursor.getString(lookAheadRouteDetailCursor.getColumnIndexOrThrow(DBHelper.KEY_STREETADDRESS));
			logger.info("SUPER SET lookAhead ADDRESS = " + lookAheadStreetNumber + " " + lookAheadStreetName);

			double lookAheadLatitude = lookAheadRouteDetailCursor.getDouble(lookAheadRouteDetailCursor.getColumnIndexOrThrow(DBHelper.KEY_LAT));
			double lookAheadLongitude = lookAheadRouteDetailCursor.getDouble(lookAheadRouteDetailCursor.getColumnIndexOrThrow(DBHelper.KEY_LONG));
			Location lookAheadLocation = GPSUtils.convertToLocationFromGeoCode(lookAheadLatitude + "," + lookAheadLongitude);
//			logger.info("SUPER SET lookAheadLocation = " + lookAheadLocation);
//			logger.info("SUPER SET deviceLocation = " + deviceLocation);

			double deviceToLookAheadDistance = deviceLocation.distanceTo(lookAheadLocation);
//			logger.info("SUPER SET deviceToLookAheadDistance = " + deviceToLookAheadDistance);

			if(deviceToLookAheadDistance > targetAreaExtendedFrontDistance){
				//=== IGNORE, IT IS OUTSIDE OF THE EXTENDED LOOK AHEAD TARGET AREA
//				logger.info("SUPER SET IGNORE (an outlier) ADDRESS IS OUTSIDE EXTENDED TARGET AREA");
				continue;
			}

			//=== LOOK AHEAD AREA IS NOW AN ELLIPSOID WHERE THE MAIN AXIS IS targetAreaExtendedFrontDistance
			//=== AND THE MINOR AXIS IS targetAreaExtendedSideDistance. THE ELLIPSOID IS ORIENTED WITH THE MAJOR AXIS
			//=== PARALLEL TO THE DEVICE AZIMUTH
			//=== THE EXTENDED LOOK AHEAD AREA IS THE SAME WITH A GREATER MAJOR AXIS DISTANCE
			float deviceToLookAheadAzimuth = deviceLocation.bearingTo(lookAheadLocation);
			//=== CONVERT BEARING TO AZIMUTH
			deviceToLookAheadAzimuth = GPSUtils.normalizeBearingToAzimuth(deviceToLookAheadAzimuth);
//			logger.info("SUPER SET deviceToLookAheadAzimuth = " + deviceToLookAheadAzimuth);

			double relativeEllipseAzimuthTheta = Math.abs(deviceToLookAheadAzimuth - deviceForwardAzimuth);
//				logger.info("SUPER SET RAW EllipseAzimuthTheta = " + relativeEllipseAzimuthTheta);

			relativeEllipseAzimuthTheta = GPSUtils.normalizeAzimuthToEllipseThetaAngle(relativeEllipseAzimuthTheta);
			double distanceToTargetEllipseCircumferenceOnAzimuthTheta = GPSUtils.getDistanceToPointOnEllipseCircumferenceWithBearingUsingMajorMinorAxis(relativeEllipseAzimuthTheta, targetAreaFrontDistance, targetAreaSideDistance);

//			logger.info("SUPER SET relativeEllipseAzimuthTheta = " + relativeEllipseAzimuthTheta);
//			logger.info("SUPER SET distanceToTargetEllipseCircumferenceOnAzimuthTheta = " + distanceToTargetEllipseCircumferenceOnAzimuthTheta);

			//=== SEE IF THIS DELIVERY ADDRESS IS WITHIN THE DISTANCE TO THE TARGET/DELIVERY ELLIPSE CIRCUMFERENCE AT A GIVEN THETA BEARING
			boolean isThisAddressPointInExtendedDistanceRange = false;
			boolean isThisAddressPointInDeliveryDistanceRange = false;

			if(distanceToTargetEllipseCircumferenceOnAzimuthTheta >= deviceToLookAheadDistance){
				isThisAddressPointInDeliveryDistanceRange = true;
				//=== POINT IS INSIDE THE ELLIPSE TARGET AREA
//				logger.info("SUPER SET ADDRESS IS INSIDE THE DELIVERY ELLIPSE CIRCUMFERENCE AT AZIMUTH THETA");
			}
			//=== SEE IF THIS DELIVERY ADDRESS IS WITHIN THE DISTANCE TO THE EXTENDED ELLIPSE CIRCUMFERENCE AT A GIVEN THETA BEARING
			else{
//				logger.info("SUPER SET ADDRESS IS not INSIDE THE DELIVERY ELLIPSE CIRCUMFERENCE AT AZIMUTH THETA");

				double distanceToExtendedEllipseCircumferenceOnAzimuthTheta = GPSUtils.getDistanceToPointOnEllipseCircumferenceWithBearingUsingMajorMinorAxis(relativeEllipseAzimuthTheta, targetAreaExtendedFrontDistance, targetAreaExtendedSideDistance);
//				logger.info("SUPER SET distanceToExtendedEllipseCircumferenceOnAzimuthTheta = " + distanceToExtendedEllipseCircumferenceOnAzimuthTheta);

				if(distanceToExtendedEllipseCircumferenceOnAzimuthTheta >= deviceToLookAheadDistance){
					isThisAddressPointInExtendedDistanceRange = true;
					//=== POINT IS INSIDE THE ELLIPSE EXTENDED TARGET AREA
//					logger.info("SUPER SET ADDRESS IS INSIDE THE EXTENDED ELLIPSE CIRCUMFERENCE AT AZIMUTH THETA");
				}
				else{
//					logger.info("SUPER SET ADDRESS IGNORE not INSIDE THE EXTENDED or DELIVERY ELLIPSE CIRCUMFERENCE AT AZIMUTH THETA");

					continue;
				}
			}

			//=== OUT OF RANGE SKIP AND CONTINUE WITH NEXT LIST ITEM
			if(!isThisAddressPointInDeliveryDistanceRange && !isThisAddressPointInExtendedDistanceRange){
				continue;
			}

			//=== QUADRANT IS RELATIVE TO DEVICE AZIMUTH, ZERO QUAD IS ERRANT
			boolean isThisAddressPointInTheTargetQuadrantSweep = false;
			int lookAheadQuadrantForRecord = GPSUtils.determineRelativeQuadrant_Common(deviceForwardAzimuth, deviceToLookAheadAzimuth);
			String lookAheadQuadrantForRecordLabel = GPSUtils.getRelativeQuadrantEnglishString(lookAheadQuadrantForRecord);
			logger.debug("SUPER SET RELATIVE look ahead drop quadrant is " + lookAheadQuadrantForRecordLabel);
			String lookAheadQuadrantForDeliverLabel = GPSUtils.getRelativeQuadrantEnglishString(deliveryTargetQuadrants);
			logger.debug("SUPER SET RELATIVE look ahead delivery quadrant is " + lookAheadQuadrantForDeliverLabel);

			if((deliveryTargetQuadrants & lookAheadQuadrantForRecord) > 0){
				isThisAddressPointInTheTargetQuadrantSweep = true;
			}

			//=== FILTER OUT ANYTHING NOT IN OUR CURRENT QUADRANTs FOR LOOK AHEAD DIRECTION
			if(!isThisAddressPointInTheTargetQuadrantSweep){
				logger.info("SUPER SET IGNORE NOT IN CORRECT QUADRANTs");

				continue;
			}

			DeliveryItem route = new DeliveryItem(lookAheadRouteDetailCursor);

			int deliveredFromRecord = lookAheadRouteDetailCursor.getInt(lookAheadRouteDetailCursor.getColumnIndexOrThrow(DBHelper.KEY_DELIVERED));

			long deliveryTimeFromRecord = lookAheadRouteDetailCursor.getLong(lookAheadRouteDetailCursor.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYDATE));

			int dndWasProcessedFromRecord = lookAheadRouteDetailCursor.getInt(lookAheadRouteDetailCursor.getColumnIndexOrThrow("dndwasprocessed"));

			long listDisplayTimeFromRecord = lookAheadRouteDetailCursor.getLong(lookAheadRouteDetailCursor.getColumnIndexOrThrow("listdisplaytime"));

			int deliveredFinal = deliveredFromRecord;
			int dndWasProcessedFinal = dndWasProcessedFromRecord;
			long deliveryTimeFinal = deliveryTimeFromRecord;//System.currentTimeMillis();
			long listDisplayTimeFinal = listDisplayTimeFromRecord;

			//=== isThisAddressPointInTheTargetQuadrantSweep=TRUE AND
			//=== isThisAddressPointInDeliveryDistanceRange=TRUE
			if(isThisAddressPointInDeliveryDistanceRange){
//				logger.info("SUPER SET WITHIN DELIVERY RANGE DISTANCE");

				if(route.getJobType() != GlobalConstants.JOB_TYPE.VIP_Do_Not_Deliver.ordinal()//1
				   && route.getJobType() != GlobalConstants.JOB_TYPE.Subscriber.ordinal()//2
				   && route.getJobType() != GlobalConstants.JOB_TYPE.Do_Not_Deliver.ordinal()//3
				   && route.getJobType() != GlobalConstants.JOB_TYPE.Cannot_Deliver.ordinal()){//4){
					//===THIS IS A DELIVER ITEM THAT WAS PROCESSED TO RESIDE IN OUR TARGET DELIVERY ZONE
					deliveredFinal = 1;
					dndWasProcessedFinal = 0;
					listDisplayTimeFinal = System.currentTimeMillis();

//					logger.info("SUPER SET POINT IS MARKED AS DELIVERED");
					//=== DO NOT OVER WRITE THE DELIVERY DATE IF PREVIOUSLY DELIVERED
					if(deliveredFromRecord == 1){
						if(deliveryTimeFromRecord == 0){
							deliveryTimeFinal = deliveryTimeStampParameterIn;
//							logger.info("SUPER SET not DND 1 SET deliveryTimeFinal = deliveryTimeStampParameterIn;");
						}
						else{
							deliveryTimeFinal = deliveryTimeFromRecord;
//							logger.info("SUPER SET not DND 2 SET deliveryTimeFinal = deliveryTimeFromRecord;");
						}
					}
					else{
						deliveryTimeFinal = 0;
//						logger.info("SUPER SET not DND 3  deliveryTimeFinal = deliveryTimeStampParameterIn;");
					}
				}
//=== WE PROBABLY DO NOT NEED TO DO THE ELSE HERE
//				else{
//					//===THIS IS A DO NOT DELIVER ITEM THAT WAS PROCESSED TO RESIDE IN OUR TARGET DELIVERY ZONE
//					//===THE SAME AS A DELIVERY ITEM WOULD BE
//					deliveredFinal = 0;
//					dndWasProcessedFinal = 1;
//					listDisplayTimeFinal = System.currentTimeMillis();
//					deliveryTimeFinal = 0;
//
///*
////					logger.info("SUPER SET POINT IS MARKED AS DND processed");
//					//===DO NOT OVER WRITE THE DELIVERY DATE IF PREVIOUSLY DELIVERED
//					if(dndWasProcessedFromRecord == 1){
//						if(deliveryTimeFromRecord == 0){
//							deliveryTimeFinal = deliveryTimeStampParameterIn;
////							logger.info("SUPER SET is DND 1 SET deliveryTimeFinal = deliveryTimeStampParameterIn;");
//						}
//						else{
//							deliveryTimeFinal = deliveryTimeFromRecord;
////							logger.info("SUPER SET is DND 2 SET deliveryTimeFinal = deliveryTimeFromRecord;");
//						}
//					}
//					else{
//						deliveryTimeFinal = deliveryTimeStampParameterIn;
////						logger.info("SUPER SET is DND 3 SET deliveryTimeFinal = deliveryTimeStampParameterIn;");
//					}
//*/
//				}
			}
			//=== isThisAddressPointInTheTargetQuadrantSweep = TRUE AND
			//=== isThisAddressPointInDeliveryDistanceRange = FALSE AND
			//=== isThisAddressPointInExtendedDistanceRange = TRUE
			else if(isThisAddressPointInExtendedDistanceRange){
//				logger.info("SUPER SET is WITHIN EXTENDED RANGE DISTANCE 1 - AN EXTENDED LOOK AHEAD POINT");

				boolean isThisAddressPointInTheCurrentDisplayList = false;

				isThisAddressPointInTheCurrentDisplayList = cachedRouteMapAsHashMap.containsKey(recordIdFromRecord);
//				logger.info("SUPER SET lookAhead ADDRESS is in current display list = " + isThisAddressPointInTheCurrentDisplayList);

				if(route.getJobType() != GlobalConstants.JOB_TYPE.VIP_Do_Not_Deliver.ordinal()//1
				   && route.getJobType() != GlobalConstants.JOB_TYPE.Subscriber.ordinal()//2
				   && route.getJobType() != GlobalConstants.JOB_TYPE.Do_Not_Deliver.ordinal()//3
				   && route.getJobType() != GlobalConstants.JOB_TYPE.Cannot_Deliver.ordinal()){//4){
					//===THIS IS A DELIVER ITEM THAT WAS PROCESSED TO RESIDE IN OUR EXTENDED ZONE
					deliveredFinal = deliveredFromRecord;
					dndWasProcessedFinal = dndWasProcessedFromRecord;
					listDisplayTimeFinal = listDisplayTimeFromRecord;

					//=== DO NOT OVER WRITE THE DELIVERY DATE IF PREVIOUSLY DELIVERED
					if(isThisAddressPointInTheCurrentDisplayList){
						listDisplayTimeFinal = listDisplayTimeFromRecord;
//						logger.info("SUPER SET EXTENDED POINT IS a DELIVERY item IN THE CURRENT DISPLAY LIST");
					}
					else{
						listDisplayTimeFinal = System.currentTimeMillis();
//						logger.info("SUPER SET EXTENDED POINT IS a DELIVERY item NOT IN THE CURRENT DISPLAY LIST");
					}
				}
				else{
					//=== THIS IS A DO NOT DELIVER ITEM THAT WAS PROCESSED TO RESIDE IN OUR EXTENDED ZONE
					//=== THE SAME AS A DELIVERY ITEM WOULD BE BUT PROCESS FOR DISPLAY NOT DELIVERY
					deliveredFinal = deliveredFromRecord;
					dndWasProcessedFinal = dndWasProcessedFromRecord;
					listDisplayTimeFinal = listDisplayTimeFromRecord;

					//=== DO NOT OVER WRITE THE DELIVERY DATE IF PREVIOUSLY DELIVERED
					if(isThisAddressPointInTheCurrentDisplayList){
						listDisplayTimeFinal = listDisplayTimeFromRecord;
//						logger.info("SUPER SET EXTENDED POINT IS a DND item IN THE CURRENT DISPLAY LIST");
					}
					else{
						listDisplayTimeFinal = System.currentTimeMillis();
//						logger.info("SUPER SET EXTENDED POINT IS a DND item NOT IN THE CURRENT DISPLAY LIST");
					}
				}
			}
			else{
				//=== CATCH ALL, SHOULD NOT GET HERE WITH PREVIOUS CHECKS ON isThisAddressPointInExtendedDistanceRange
				//=== CONTINUE WITH TOP OF LOOP PROCESSING
//				logger.info("SUPER SET IGNORE not WITHIN EXTENDED or DELIVERY RANGE DISTANCE 2 - ***A FALL OUT POINT***");
				continue;
			}

//			logger.info("FINISH deliveredFinal = " + (deliveredFinal == 0 ? "FALSE" : "TRUE"));
//			logger.info("FINISH dndWasProcessedFinal = " + (dndWasProcessedFinal == 0 ? "FALSE" : "TRUE"));

			route.setDeliveredTime(deliveryTimeFinal);
			route.setDelivered(deliveredFinal);
			route.setListDisplayTime(listDisplayTimeFinal);
			route.setDndWasProcessed(dndWasProcessedFinal);

			//=== IT HAS CHANGED SO SET IT TO UPLOAD
			route.setUploaded(0);
			route.setDeliveredLatitude(deviceLocation.getLatitude());
			route.setDeliveredLongitude(deviceLocation.getLongitude());
			logger.debug("UPDATING RANDOM ROUTE DETAIL ITEM = " + route.getDeliveryResequencingInfo());
			route.updateDatabaseRecord();

			newRouteDetailsInDeliveryAreaToAdd.add(route);
		}    //end top while loop
//		logger.info("##############SUPER SET loop finished####################");

		//=== THIS REPLACES OR ADDS THE ROUTE DETAIL ITEM
		for(int newlyProcessedItemCounter = 0; newlyProcessedItemCounter < newRouteDetailsInDeliveryAreaToAdd.size(); newlyProcessedItemCounter++){
			//logger.debug("ID: " + newRouteDetailsInDeliveryAreaToAdd.get(newlyProcessedItemCounter).getId() + "Dist: " +
			cachedRouteMapAsHashMap.put(newRouteDetailsInDeliveryAreaToAdd.get(newlyProcessedItemCounter).getId(), newRouteDetailsInDeliveryAreaToAdd.get(newlyProcessedItemCounter));
		}
		//logger.info("ENTER : cachedRouteMap NOW has " + cachedRouteMap.size() + " items");

		//=== WHAT LOGIC TO REMOVE ITEMS??? TKV, 30JUL18, MAKES NO SENSE HERE
		//=== remove anything from the list that is over 100 , do this to limit the
		//=== scrollable screen to 100 items....

		if(cachedRouteMapAsHashMap.size() > GlobalConstants.MAX_ROUTEDETAILS_LIST_SIZE){
			int toRemove = cachedRouteMap.size() - GlobalConstants.MAX_ROUTEDETAILS_LIST_SIZE;
			//logger.debug("Need to remove keys: " + toRemove);

			Set<Integer> keys = cachedRouteMapAsHashMap.keySet();
			Object[] arr = keys.toArray();

			for(int i = 0; i < toRemove; i++){
				Integer myKey = (Integer) arr[i];
				cachedRouteMapAsHashMap.remove(myKey);
			}
		}

		return new ArrayList<DeliveryItem>(cachedRouteMapAsHashMap.values());
	}

	public final ArrayList<DeliveryItem> fetchRouteDetailsWithinDeliveryArea_Sequencing_As_Random(
			int jobDetailId,
			double targetAreaFrontDistance,
			double targetAreaSideDistance,
			double targetAreaExtendedFrontDistance,
			double targetAreaExtendedSideDistance,
			ArrayList<DeliveryItem> cachedRouteMap,
			int deliveryMode,
			Location deviceLocation,
			double deviceForwardAzimuth){
//		logger.info(">>>>>>>>>>>>>>>>>>>>ENTER>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		LinkedHashMap<Integer, DeliveryItem> cachedRouteMapAsHashMap = new LinkedHashMap<Integer, DeliveryItem>();

		//==============================================================================================
		//=== IGNORE THE SUMMARY LIST ITEMS for processing drops
		//==============================================================================================
		Collection<DeliveryItem> cachedCurrentRouteItems = new ArrayList<>();
		for(int i = 0; i < cachedRouteMap.size(); i++){
			if(cachedRouteMap.get(i).getStatus() != GlobalConstants.ROUTE_DETAIL_STATUS_STREETSUMMARY){
				cachedCurrentRouteItems.add(cachedRouteMap.get(i));
				cachedRouteMapAsHashMap.put(cachedRouteMap.get(i).getId(), cachedRouteMap.get(i));
			}
		}
		Iterator<DeliveryItem> iteratorForCachedCurrentRouteItems = cachedCurrentRouteItems.iterator();
		//logger.info("cachedCurrentRouteItems has " + cachedCurrentRouteItems.size() + " items");

		//=== GETS THE LARGEST sequencenew VALUE FOR THE JOB
		int currentSequencingOrdinalCounter = getSequencingLastOrdinalSetForARouteWithJobDetailId_Random(jobDetailId);
		currentSequencingOrdinalCounter = Math.max(currentSequencingOrdinalCounter, 0);
		logger.info("currentSequencingOrdinalCounter = " + currentSequencingOrdinalCounter);

		//====================================================================================================================
		//=== PRE-PROCESS THE CURRENT MASTER LIST
		//=== Loop through the current display elements and modify their distances and quadrants and
		//====================================================================================================================
		while(iteratorForCachedCurrentRouteItems.hasNext()){
			//logger.info("##############CURRENT ROUTE DETAIL top of loop######################");

			DeliveryItem routeDetailFromDisplayList = iteratorForCachedCurrentRouteItems.next();

			//logger.info("CURRENT ROUTE DETAIL point address = " + routeDetailFromDisplayList.getGpsLocationAddressNumber() + " " + routeDetailFromDisplayList.getGpsLocationAddressStreet());

			double distanceToRouteDetailFromDeviceLocation = deviceLocation.distanceTo(routeDetailFromDisplayList.getLocation());

			//=== UPDATE CURRENT AND LAST RECORDED DISTANCES FROM THE DEVICE
			routeDetailFromDisplayList.setDistance(distanceToRouteDetailFromDeviceLocation);
		}
		//logger.info("##############CURRENT ROUTE DETAIL loop finished######################");

		//====================================================================================================================
		//=== Loop through the cursor, determine if the point is in the extended look ahead area or the target delivery area.
		//=== Then determine if the point is in the correct quadrant, has an acceptable range distance, and should be displayed or removed from display
		//====================================================================================================================

		//=== INCREASE targetAreaFrontDistance TO ENLARGE THE LOOK AHEAD DISTANCE TO GET ADDRESSES BEYOND THE DELIVERY TARGET ZONE OF targetAreaFrontDistance
		//logger.debug("$$$$$$$$$$$$$ CENTER POINT = " + deviceLocation.getLatitude() + "," + deviceLocation.getLongitude());
		BoundingPointMatrix lookAheadBoundingBox =
				new BoundingPointMatrix(deviceLocation.getLatitude() + "," + deviceLocation.getLongitude(),
										(int) targetAreaExtendedFrontDistance,
										(int) targetAreaExtendedSideDistance,
										(float) deviceForwardAzimuth);

		//=== GET A LARGER THAN THE TARGET LOOK AHEAD AREA GEO-BOUNDED BOX TO NARROW THE SET OF POSSIBLE DELIVERY ADDRESSES
		ArrayList<DeliveryItem> newRouteDetailsInDeliveryAreaToAdd = new ArrayList<DeliveryItem>();

		//=== THIS IS SORTED BY DISTANCE, I.E. CLOSEST FIRST FOR EASIER SEQUENCING
		ArrayList<DeliveryItem> lookAheadRouteDetails = fetchRouteDetailsByGeoBoxAsArrayList_Random(jobDetailId,
																									lookAheadBoundingBox,
																									deviceLocation,
																									(float) deviceForwardAzimuth);

		//logger.info("##############SUPER SET loop START ######################");
		Iterator<DeliveryItem> lookAheadIterator = lookAheadRouteDetails.iterator();
		while(lookAheadIterator.hasNext()){
			DeliveryItem routeDetail = lookAheadIterator.next();

			//logger.info("##############SUPER SET top of loop ######################");

			//String lookAheadStreetNumber = routeDetail.getGpsLocationAddressNumber();
			//String lookAheadStreetName = routeDetail.getGpsLocationAddressStreet();
			//logger.info("SUPER SET lookAhead ADDRESS = " + lookAheadStreetNumber + " " + lookAheadStreetName);

			double lookAheadLatitude = routeDetail.getGpsLocationLatitude();
			double lookAheadLongitude = routeDetail.getGpsLocationLongitude();
			Location lookAheadLocation = GPSUtils.convertToLocationFromGeoCode(lookAheadLatitude + "," + lookAheadLongitude);
			//logger.info("SUPER SET lookAheadLocation = " + lookAheadLocation);
			//logger.info("SUPER SET deviceLocation = " + deviceLocation);

			double deviceToLookAheadDistance = deviceLocation.distanceTo(lookAheadLocation);
			//logger.info("SUPER SET deviceToLookAheadDistance = " + deviceToLookAheadDistance);

			//=== this is a gross check to weed out any outliers
			if(deviceToLookAheadDistance > targetAreaExtendedFrontDistance){
				//=== IGNORE, IT IS OUTSIDE OF THE EXTENDED LOOK AHEAD TARGET AREA
				//logger.info("SUPER SET IGNORE (an outlier) ADDRESS IS OUTSIDE EXTENDED TARGET AREA");
				continue;
			}

			//=== LOOK AHEAD AREA IS NOW AN ELLIPSOID WHERE THE MAIN AXIS IS targetAreaExtendedFrontDistance
			//=== AND THE MINOR AXIS IS targetAreaExtendedSideDistance. THE ELLIPSOID IS ORIENTED WITH THE MAJOR AXIS
			//=== PARALLEL TO THE DEVICE AZIMUTH
			//=== THE EXTENDED LOOK AHEAD AREA IS THE SAME WITH A GREATER MAJOR AXIS DISTANCE
			float deviceToLookAheadAzimuth = deviceLocation.bearingTo(lookAheadLocation);
			//=== CONVERT BEARING TO AZIMUTH
			deviceToLookAheadAzimuth = GPSUtils.normalizeBearingToAzimuth(deviceToLookAheadAzimuth);
			//logger.info("SUPER SET deviceToLookAheadAzimuth = " + deviceToLookAheadAzimuth);

			double relativeEllipseAzimuthTheta = Math.abs(deviceToLookAheadAzimuth - deviceForwardAzimuth);
			//logger.info("SUPER SET RAW EllipseAzimuthTheta = " + relativeEllipseAzimuthTheta);

			relativeEllipseAzimuthTheta = GPSUtils.normalizeAzimuthToEllipseThetaAngle(relativeEllipseAzimuthTheta);
			double distanceToTargetEllipseCircumferenceOnAzimuthTheta = GPSUtils.getDistanceToPointOnEllipseCircumferenceWithBearingUsingMajorMinorAxis(relativeEllipseAzimuthTheta, targetAreaFrontDistance, targetAreaSideDistance);

			//logger.info("SUPER SET relativeEllipseAzimuthTheta = " + relativeEllipseAzimuthTheta);
			//logger.info("SUPER SET distanceToTargetEllipseCircumferenceOnAzimuthTheta = " + distanceToTargetEllipseCircumferenceOnAzimuthTheta);

			//=== SEE IF THIS DELIVERY ADDRESS IS WITHIN THE DISTANCE TO THE TARGET/DELIVERY ELLIPSE CIRCUMFERENCE AT A GIVEN THETA BEARING
			boolean isThisAddressPointInDeliveryDistanceRange = false;

			//=== SEE IF THIS DELIVERY ADDRESS IS WITHIN THE DISTANCE TO THE EXTENDED ELLIPSE CIRCUMFERENCE AT A GIVEN THETA BEARING
			if(distanceToTargetEllipseCircumferenceOnAzimuthTheta >= deviceToLookAheadDistance){
				isThisAddressPointInDeliveryDistanceRange = true;
				//=== POINT IS INSIDE THE ELLIPSE TARGET AREA
				//logger.info("SUPER SET ADDRESS IS INSIDE THE DELIVERY ELLIPSE CIRCUMFERENCE AT AZIMUTH THETA");
			}
			else{
				continue;
			}

			//=== isThisAddressPointInTheTargetQuadrantSweep
			boolean isThisAddressPointInTheTargetQuadrantSweep = false;
			int lookAheadQuadrantForRecord = GlobalConstants.DEF_DELIVERY_QUADS_NONE;

			if(isThisAddressPointInDeliveryDistanceRange){
				//=== QUADRANT IS RELATIVE TO DEVICE AZIMUTH, ZERO QUAD IS ERRANT
				lookAheadQuadrantForRecord = GPSUtils.determineRelativeQuadrant_Common(deviceForwardAzimuth, deviceToLookAheadAzimuth);
				String lookAheadQuadrantForRecordLabel = GPSUtils.getRelativeQuadrantEnglishString(lookAheadQuadrantForRecord);
				logger.debug("SUPER SET RELATIVE look ahead drop quadrant is " + lookAheadQuadrantForRecordLabel);
				String lookAheadQuadrantForDeliverLabel = GPSUtils.getRelativeQuadrantEnglishString(deliveryMode);
				logger.debug("SUPER SET RELATIVE look ahead delivery quadrant is " + lookAheadQuadrantForDeliverLabel);

				if((deliveryMode & lookAheadQuadrantForRecord) > 0){
					isThisAddressPointInTheTargetQuadrantSweep = true;
				}
			}

			//=== FILTER OUT ANYTHING NOT IN OUR CURRENT QUADRANTs FOR LOOK AHEAD DIRECTION
			if(!isThisAddressPointInTheTargetQuadrantSweep){
				//===I GNORE THIS ONE
				//logger.info("SUPER SET IGNORE NOT IN CORRECT QUADRANTs");
				continue;
			}

			int sequencingOrdinalFinal = -1;

			//=== isThisAddressPointInTheTargetQuadrantSweep=TRUE AND
			//=== isThisAddressPointInDeliveryDistanceRange=TRUE
			if(isThisAddressPointInDeliveryDistanceRange){
				//logger.info("SUPER SET WITHIN DELIVERY RANGE DISTANCE");

				if(routeDetail.getSequenceNew() > 0){
					sequencingOrdinalFinal = routeDetail.getSequenceNew();
				}
				else{
					currentSequencingOrdinalCounter++;
					sequencingOrdinalFinal = currentSequencingOrdinalCounter;
				}
			}
			else{
				//=== CATCH ALL, SHOULD NOT GET HERE WITH PREVIOUS CHECKS ON isThisAddressPointInExtendedDistanceRange
				//=== CONTINUE WITH TOP OF LOOP PROCESSING
				//logger.info("SUPER SET IGNORE not WITHIN EXTENDED or DELIVERY RANGE DISTANCE 2 - ***A FALL OUT POINT***");
				continue;
			}

			logger.info("SEQUENCE NEW = " + sequencingOrdinalFinal);

			routeDetail.setSequenceNew(sequencingOrdinalFinal);
			routeDetail.setDelivered(0);
			//=== IT HAS CHANGED SO SET IT TO DEFERRED UPLOAD
			routeDetail.setUploaded(-99);
			routeDetail.setDeliveredLatitude(0);
			routeDetail.setDeliveredLongitude(0);
			routeDetail.setSequenceModeNew("manual");
			routeDetail.setDeliveredTime(0);

			logger.debug("UPDATING SEQUENCING AS RANDOM ROUTE");
			logger.info("DeliveryItem = " + routeDetail.getDeliveryResequencingInfo());
			routeDetail.updateDatabaseRecord();

			newRouteDetailsInDeliveryAreaToAdd.add(routeDetail);
		}    //end top while loop
		//logger.info("##############SUPER SET loop finished####################");

		//=== THIS REPLACES OR ADDS THE ROUTE DETAIL ITEM
		for(int newlyProcessedItemCounter = 0; newlyProcessedItemCounter < newRouteDetailsInDeliveryAreaToAdd.size(); newlyProcessedItemCounter++){
			// //logger.debug("ID: " + newRouteDetailsInDeliveryAreaToAdd.get(newlyProcessedItemCounter).getId() + "Dist: " +
			cachedRouteMapAsHashMap.put(newRouteDetailsInDeliveryAreaToAdd.get(newlyProcessedItemCounter).getId(), newRouteDetailsInDeliveryAreaToAdd.get(newlyProcessedItemCounter));
		}

		//=== WHAT LOGIC TO REMOVE ITEMS??? TKV, 30JUL18, MAKES NO SENSE HERE
		// remove anything from the list that is over 100 , do this to limit the
		// scrollable screen to 100 items....
		if(cachedRouteMapAsHashMap.size() > GlobalConstants.MAX_ROUTEDETAILS_LIST_SIZE){
			int toRemove = cachedRouteMap.size() - GlobalConstants.MAX_ROUTEDETAILS_LIST_SIZE;
			// //logger.debug("Need to remove keys: " + toRemove);

			Set<Integer> keys = cachedRouteMapAsHashMap.keySet();
			Object[] arr = keys.toArray();

			for(int i = 0; i < toRemove; i++){
				Integer myKey = (Integer) arr[i];
				cachedRouteMapAsHashMap.remove(myKey);
			}
		}

		return new ArrayList<DeliveryItem>(cachedRouteMapAsHashMap.values());
	}

	public HashMap<Integer, Integer> fetchRemainingByJobDetailIdGroupByStreetSummary_Random(
			int jobDetailId){
		// logger.debug("Entry: fetchRemainingGroupByStreetSummary()");
		int count = 0;
		Integer key = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		Cursor cur = null;

		try{
			final String query = "Select summaryid, count(_id) from addressdetaillist WHERE jobdetailid = "
								 + jobDetailId
								 + " and (Delivered is null or Delivered = 0) AND jobtype in (5,6) group by summaryid";
			// //logger.debug(TAG,, query);

			cur = getWritableDatabase().rawQuery(query, null);

			while(cur.moveToNext()){
				key = cur.getInt(0);
				count = cur.getInt(1);
				map.put(key, count);
			}

		}
		catch(Exception e){
			logger.error(">>>>fetchRemainingByJobDetailIdGroupByStreetSummary() : Fail jobid=" + jobDetailId + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return map;
	}

	public HashMap<Integer, Integer> fetchCustSrvByJobDetailIdGroupByStreetSummary_Random(
			int jobDetailId){
		// logger.debug("Entry: fetchCustSrvByJobDetailIdGroupByStreetSummary()");
		int count = 0;
		Integer key = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		Cursor cur = null;

		try{
			final String query = "Select summaryid, count(_id) from addressdetaillist WHERE jobdetailid = "
								 + jobDetailId + " and custsvc=1 group by summaryid";
			// //logger.debug(TAG,, query);

			cur = getWritableDatabase().rawQuery(query, null);

			while(cur.moveToNext()){
				key = cur.getInt(0);
				count = cur.getInt(1);
				map.put(key, count);
				// //logger.debug("KEY: " + key + "  COUNT: " + count);
			}
		}
		catch(Exception e){
			// logger.debug("exception " + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return map;
	}

	public HashMap<Integer, Integer> fetchDNDByJobDetailIdGroupByStreetSummary_Random(
			int jobDetailId){
		// logger.debug("Entry: fetchDNDByJobDetailIDGroupByStreetSummary()");
		int count = 0;
		Integer key = 0;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		Cursor cur = null;

		try{
			final String query = "Select summaryid, count(_id) from addressdetaillist WHERE jobdetailid = "
								 + jobDetailId
								 + " and jobtype in (1,2,3,4) group by summaryid";
			// //logger.debug(TAG,, query);

			cur = getWritableDatabase().rawQuery(query, null);

			while(cur.moveToNext()){
				key = cur.getInt(0);
				count = cur.getInt(1);
				map.put(key, count);
				// logger.debug("KEY: " + key + "  COUNT: " + count);
			}
		}
		catch(Exception e){
			logger.debug(">>>>fetchDNDByJobDetailIdGroupByStreetSummary() : exception " + e);
		}
		finally{
			if(cur != null && !cur.isClosed()){
				cur.close();
			}
		}

		return map;
	}

	public HashMap<Integer, StreetSummaryRandom> fetchStreetSummariesByJobDetailId_Random(int jobDetailId){
		final String[] fieldNames = {DBHelper.KEY_JOBDETAILID};
		final boolean[] isWildCard = {false};
		String[] parameters = {Integer.valueOf(jobDetailId).toString()};

		HashMap<Integer, StreetSummaryRandom> map = new HashMap<Integer, StreetSummaryRandom>();

		Cursor cur1 = fetchAllFromTableByFieldNamesAndParametersInOrderWildCard_Random(
				DBHelper.DB_T_STREETSUMMARYLIST, fieldNames, parameters,
				isWildCard, DBHelper.KEY_JOBDETAILID, "asc", MAX_DB_RETURN);

		if(cur1 != null){
			while(cur1.moveToNext()){
				StreetSummaryRandom s = new StreetSummaryRandom();
				s.setGpsLocationLatitude(cur1.getDouble(cur1.getColumnIndexOrThrow(DBHelper.KEY_LAT)));
				s.setGpsLocationLongitude(cur1.getDouble(cur1.getColumnIndexOrThrow(DBHelper.KEY_LONG)));
				s.setId(cur1.getInt(cur1.getColumnIndexOrThrow(DBHelper.KEY_ID)));
				s.setJobDetailId(cur1.getInt(cur1.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID)));
				s.setStreetName(cur1.getString(cur1.getColumnIndexOrThrow(DBHelper.KEY_STREETNAME)));
				s.setSummaryId(cur1.getInt(cur1.getColumnIndexOrThrow(DBHelper.KEY_SUMMARYID)));

				map.put(s.getSummaryId(), s);
			}
		}

		return map;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transferring (copying) bytestream.
	 */
/*
	public static void copyDataBase_Common(Context context) throws IOException{
		String outFileName = "";
		try{
			//logger.debug(">>>>copyDataBase() : Copying the db");

			// Open your local db as the input stream
			InputStream myInput = context.getAssets().open(GlobalConstants.DATABASE_NAME);

			//logger.debug(">>>>copyDataBase() : Opened up the stream...");

			// Path to the just created empty db
			String dir = FileUtils.getAppDirectoryForDataBaseFiles();
			outFileName = dir + GlobalConstants.DATABASE_NAME;
			//logger.debug(">>>>copyDataBase() : Output: " + outFileName);

			File outPutFile = new File(outFileName);
			outPutFile.getParentFile().mkdirs();
			outPutFile.createNewFile();

			// Open the empty db as the output stream
			OutputStream myOutput = new FileOutputStream(outPutFile);

			// transfer bytes from the inputfile to the outputfile
			//create a buffer that has the same size as the InputStream
			byte[] buffer = new byte[myInput.available()];
			//read the text file as a stream, into the buffer
			myInput.read(buffer);
			//write this buffer to the output stream
			myOutput.write(buffer);
			//Close the Input and Output streams
			myOutput.flush();
			myOutput.close();
			myInput.close();

			//logger.debug(">>>>copyDataBase() : Done: copyDataBase");
		}
		catch(Exception e){
			logger.debug(">>>>copyDataBase() : EXCEPTION: " + outFileName + " : " + e.getMessage());
		}
	}
*/

}
