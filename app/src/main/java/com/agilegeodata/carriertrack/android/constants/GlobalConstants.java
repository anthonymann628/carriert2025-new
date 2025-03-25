package com.agilegeodata.carriertrack.android.constants;

import com.agilegeodata.carriertrack.android.services.DownloadService;
import com.agilegeodata.carriertrack.android.services.LocationUpdateService;
import com.agilegeodata.carriertrack.android.services.UploadService;

public class GlobalConstants{

	public static final String CARRIERTRACK_LOGGER = "<AGD>";
	public static final String ENGLISH_LANGUAGE = "en";
	public static final String SPANISH_LANGUAGE = "es";
	//=== FOR BUNDLED DATA PASSING TO ACTIVITIES
	public static final String EXTRA_SOLO_NAVIGATION_SCREEN = "soloNavigationScreen";
	public static final String EXTRA_UPLOAD_STATUS = "uploadStatus";
	public static final String EXTRA_DOWNLOAD_STATUS = "downloadStatus";
	public static final String EXTRA_PAUSE_MODE = "pauseMode";
	public static final String EXTRA_URL = "url";
	//===	public static final String EXTRA_DEVICE_CURRENT_GEOCODE = "deviceCurrentGeoCode";
	public static final String EXTRA_ROUTE_ID = "routeId";
	public static final String EXTRA_JOB_ID = "jobId";
	public static final String EXTRA_ERROR_MESSAGE = "errorMessage";
	public static final String EXTRA_MANUAL_UPLOAD = "doManualUpload";
	public static final String EXTRA_MANUAL_DOWNLOAD = "doManualDownload";
	public static final String EXTRA_JOBDETAILSEQUENCE = "jobDetailISequence";
	public static final String EXTRA_LASTJOBDETAILSEQUENCE = "lastJobDetailSequence";
	public static final String EXTRA_SEQUENCE_INTERVAL = "sequenceInterval";
	public static final String EXTRA_JOBDETAILID = "jobDetailId";
	public static final String EXTRA_JOBRECORDID = "jobRecordId";
	public static final String EXTRA_DELIVERYID = "deliveryId";
	public static final String EXTRA_DUPLICATE = "duplicate";
	public static final String EXTRA_LATITUDE = "latitude";
	public static final String EXTRA_LONGITUDE = "longitude";
	public static final String EXTRA_TASK_TYPE = "taskType";
	public static final String EXTRA_CURRENT_ADDRESS = "address";  // camera
	public static final String EXTRA_DELIVERY_QUADS = "deliveryQuads";
	public static final String EXTRA_SEARCH_FILTER = "searchFilter";
	public static final String EXTRA_DEVICE_GPS_ENABLED = "gpsProviderEnabled";
	public static final String EXTRA_CURRENT_SPEED = "gpsCurrentSpeed";
	public static final String EXTRA_CURRENT_BEARING = "gpsCurrentBearing";
	public static final String EXTRA_ROUTE_TYPE = "routeType";
	public static final String DATABASE_NAME = "carriertrack_bootstrapsmall.db";
	public static final String DB_TABLE_COLUMN_LAST_SCREEN = "lastScreen";
	//=== PERSISTED PREFERENCES
	public static final String PREF_USERFIRSTNAME = "userFirstName";
	public static final String PREF_LANGUAGE = "language";
	public static final String PREF_DBVERSION = "currentDBVersion";
	public static final String PREF_CUR_DELIVERY_MODE = "curDeliveryMode";
	public static final String PREF_DEVICE_ID = "deviceId";   // USED AS THE USER NAME
	public static final String PREF_RUNNINGDATAUSAGE = "runningDataUsage";
	public static final String PREF_LASTTIME_LOGIN = "lastTimeLogin";
	public static final String PREF_LASTTIME_DOWNLOAD_SYNC = "lastTimeSync";
	public static final String PREF_USE_MAPS = "useMaps";
	public static final String PREF_USE_TEST_DATA = "useTestData";
	public static final String PREF_USE_SPEECH = "useSpeech";
	public static final String PREF_LOCAL_TIME_ZONE_OFFSET = "timeZoneOffSet";
	public static final String PREF_IS_DST = "pisDst";//3/11
	public static final String PREF_LOCAL_TIME_ZONE = "timeZone";
	public static final String PREF_LASTGPS_SYNC = "lastGPSSync";    // used in the device status
	public static final String PREF_LAST_NOT_FINISHED_JOBDETAIL_ID = "lastNotFinishedJobDetailId";
	public static final String EXTRA_DATA_OPERATIONS_MODE = "dataOperationsMode";
	//=== VALIDATION CONSTANTS
	public static final int MIN_PASSWORD_LENGTH = 5;
	public static final String PASSWORD_VALIDATIONS = ".[0-9a-z#]+";
	//=== search filters
	public static final int ROUTE_CHARS_MIN_SEARCH = 2;
	// FILE Constants
	public static final String DBUPDATE_ZIPFILENAME = "dbfile.zip";
	public static final String DBUPDATE_FILENAME = "dbfile.txt";
	//=== NAME for last connection to server time check
	public static final String LAST_CONNECTION_TO_SERVER = "LastConnectionToServer";
	//=== APP CONSTANTS
	public static final String DEFAULT_PREF_FILE = "defaultPrefFile";
	public static final String DEFAULT_DATETIME_FORMAT_SQL = "yyyy-MM-dd H:mm:ss"; // ie. gmt time 2011-07-22 14:39:45
	public static final String DEFAULT_DATETIME_FORMAT_PICTURE = "MM/dd/yyyy/hh:mm:ss";
	public static final String DEFAULT_DATETIME_FORMAT = "MM/d/yyyy 'at' h:mm:ss a";
	public static final String DEFAULT_DATETIME_FORMAT_SHORT = "MM/d/yyyy 'at' h:mm a";
	public static final String DEFAULT_DATE_FORMAT = "EEE, MMM d, yyyy";
	public static final String DEFAULT_TODAYDATE_FORMAT = "'Today @' hh:mm a";
	//=== CONSTANTS FOR DELIVERY ACTIVITIES
	public static final int DEF_UPLOADED_FALSE = 0;
	public static final int DEF_UPLOADED_BATCH_ID = -1;
	public static final String DEF_STATUS_LOGGEDIN = "1";
	public static final String DEF_DELETED = "1";
	public static final String DEF_NOT_DELETED = "0";
	public static final int DEF_DELIVERY_QUADS_NONE = 0;
	public static final int DEF_DELIVERY_QUADS_LEFT_FRONT = 8;
	public static final int DEF_DELIVERY_QUADS_LEFT_REAR = 4;
	public static final int DEF_DELIVERY_QUADS_RIGHT_REAR = 2;
	public static final int DEF_DELIVERY_QUADS_RIGHT_FRONT = 1;
	public static final int DEF_DELIVERY_QUADS_LEFT_FRONT_AND_RIGHT_FRONT =
			DEF_DELIVERY_QUADS_LEFT_FRONT | DEF_DELIVERY_QUADS_RIGHT_FRONT;
	public static final int DEF_DELIVERY_QUADS_ALL_LEFT_RIGHT_FRONT_REAR =
			DEF_DELIVERY_QUADS_LEFT_FRONT |
			DEF_DELIVERY_QUADS_LEFT_REAR |
			DEF_DELIVERY_QUADS_RIGHT_REAR |
			DEF_DELIVERY_QUADS_RIGHT_FRONT;
	public static final String DEF_DELIVERYSTATUS_NONE = "none";
	public static final String DEF_ERROR_MESSAGE = "-";  // USED BY UPLOAD AND DOWNLOAD SERVICE TO INDICTATE NO ERROR MESSAGE
	public static final int MIN_WIFI_SIGNAL = -90;  // below this signal strength wifi is not strong....
	public static final int MIN_CELL_SIGNAL = -110;  // below this signal strength cell is not strong....
	public static final int MAX_ROUTEDETAILS_LIST_SIZE = 200;
	//=== BOOT	SERVICE
	public static final long DEF_MIN_UPDATE_TIME = 1000;      // GPS Default update time based on last update.
	public static final long SERVICE_RUN_GPSTASK_EVERY_MILLIS = 1000;
	public static final long SERVICE_DELAY_FIRST_UPLOADTASK_MILLIS = 2 * 60 * 1000;
	public static final long SERVICE_RUN_UPLOADTASK_EVERY_MILLIS = 5 * 60 * 1000;   // every 5 minutes if wifi is off
	//=== and it's run in the past ten minutes, it will be skipped
	public static final long SERVICE_RUN_DOWNLOADTASK_EVERY_MILLIS = 10 * 60 * 1000;   // every 10 minutes
	public static final long SERVICE_DELAY_FIRST_DOWNLOADTASK_MILLIS = 8 * 1000;   // delay 8 seconds
	public static final String SERVICE_LOCATION = "com.agilegeodata.carriertrack.android.services.LocationUpdateService";
	public static final String SERVICE_DOWNLOAD = "com.agilegeodata.carriertrack.android.services.DownloadService";
	public static final String SERVICE_UPLOAD = "com.agilegeodata.carriertrack.android.services.UploadService";
	public static final String INTENT_PAUSE_MODE = "com.agilegeodata.carriertrack.android.fragments.RouteDetailsRightSideFragment.PAUSE_MODE";
	public static final String INTENT_SKIPPED_DELIVERY = "com.agilegeodata.carriertrack.android.fragments.RouteDetailsRightSideFragment.SKIPPED_DELIVERY";
	public static final String INTENT_UPDATE_NAV_MAP_PINS = "com.agilegeodata.carriertrack.android.fragments.MyMapboxNavigationFragment";
	public static final String DOWNLOAD_ROUTE = "com.agilegeodata.carriertrack.android.fragments.RouteSelectFragment";
	public static final String ROUTE_ID = "routeID";
	public static final String JOB_ID = "jobID";
	public static final int ROUTE_DETAIL_STATUS_STREETSUMMARY = 1;
	public static final int ROUTE_DETAIL_STATUS_LIST_ITEM = 0;
	public static final String URL_DOWNLOADCONFIRM_DATA = "https://agdandroid.carriertrack.com/agdConfirmPhoneData.php";
	public static final String URL_DOWNLOAD_DATA = "https://agdandroid.carriertrack.com/agdDownloadPhoneData.php";
	//=== FOR MERGED VERSION APP
	public static final String URL_DOWNLOAD_ZIP_NEW = "https://agdandroid.carriertrack.com/agdDownloadPhoneDataZipV5.php";
	public static final String URL_UPLOAD_DATA = "https://data.agilegeodata.com/pushdata.php";
	//=== public static final String URL_LOG_DATAUSAGE = "https://data.agilegeodata.com/api/log_device_data.php";
	public static final String URL_PROVISION_DEVICE = "https://data.agilegeodata.com/provisionDevice.php";
	public static final String URL_UPLOAD_DATAPHOTO = "https://data.agilegeodata.com/pushphotos.php";
	public static final String URL_UPLOAD_DATASIGNATURE = "https://data.agilegeodata.com/pushsignatures.php";
	public static final String URL_UPLOAD_DB = "https://data.agilegeodata.com/deviceDataUpload.php";
	public static final String URLPARAM_DEVICEDESC = "deviceDesc";
	public static final String URLPARAM_FILETYPE = "fileType";
	public static final String URLPARAM_DATA = "data";
	public static final String URLPARAM_CTVERSION = "ctVersion";
	public static final String URLPARAM_ANDROIDVERSION = "androidVersion";
	public static final String URLPARAM_DEVICETYPE = "deviceType";
	public static final String URLPARAM_ISDST = "isDST";
	public static final String URLPARAM_LOCALTIMESTAMP = "localTime";
	public static final String URLPARAM_DEVICEID = "deviceId";
	public static final String URLPARAM_FORCERESET = "forcereset";
	public static final String URLPARAM_FORCERESET_TRUE = "1";
	public static final String UPLOAD_FIELD_DELIMINATOR = "#@@#";
	public static final String URLPARAM_UPLOAD_MANUAL = "manual";
	public static final String URLPARAM_UPLOAD_AUTO = "auto";
	public static final String URLPARAM_FILETYPE_BREADCRUMB = "breadcrumbs";
	public static final String URLPARAM_FILETYPE_UPLOADLOG = "uploadlog";
	public static final String URLPARAM_FILETYPE_ADDRESSDETAILIST = "addressdetaillist";
	public static final String URLPARAM_FILETYPE_PROVISIONDEVICE = "ProvisionDevice";
	public static final String URLPARAM_FILETYPE_LOGINS = "logins";
	public static final String URLPARAM_FILETYPE_ROUTELISTACTIVITY = "routelistactivity";
	public static final String URLPARAM_FILETYPE_ROUTEWORKACTIVITY = "routeworkactivity";
	public static final String URLPARAM_FILETYPE_PHOTOS = "photos";
	public static final String URLPARAM_FILETYPE_SIGNATURES = "signatures";
	public static final String URLPARAM_FILETYPE_PHOTOJPG = "photojpg";
	public static final String URLPARAM_FILETYPE_SIGNATUREJPG = "signaturejpg";
	public static final String URLPARAM_FILETYPE_SCANS = "codescans";
	public static final String URLPARAM_JOBID = "jobdetailId";
	public static final String URLPARAM_ROUTEID = "routeId";
	public static final String UPLOAD_STATUS_CREATE = "0";
	public static final String UPLOAD_STATUS_CONFIRMED = "1";
	public static final String UPLOAD_COUNT_DELIMINATOR = "::::";
	public static final int MANUAL_UPLOAD = 0;
	public static final int MANUAL_DOWNLOAD = 1;
	public static final int ERROR_CODE_NONE = 0;
	public static final int ERROR_CODE_DOWNLOAD_BAD_RESPONSE = 1000;
	public static final int ERROR_CODE_DOWNLOAD_DATABASE_LOADERROR = 1001;
	public static final String EXTRA_FILENAME = "fileName";
	public static final String EXTRA_SIGNATUREFILENAME = "signaturefileName";
	public static final String TEMP_PICTURE_FILE = "tempPictureFile.jpg";
	public static final String TEMP_SIGNATURE_FILE = "tempSignatureFile.jpg";
	public static final int RESULT_CONFIRM_PICTURE = 1;
	public static final int RESULT_CANCEL_PICTURE = 2;
	public static final int RESULT_REJECT_PICTURE = 3;
	public static final String DCONF_NUMRECORDS = "numRecords";
	public static final String DCONF_NUM_DEL_CNT = "0";  // DO NOT NEED TO SEND NUMBER
	public static final int MAX_DATA_REPORT_ADDRESS_LEN = 22;
	public static final int HTTP_TIMEOUT = 60000;
	public static final int BREADCRUMB_COUNTER = 15;   // should send about 2 bcs per minute.
	//=== GPS Constants
	public static final String HAS_GPS_FIX = "hasGPSfix";
	//=== WIFI Constants
	public static final int ROUTEDETAILS = 0;
	public static final int DEF_LOGIN_EXPIRED_VALUE = -24;  // NUMBER OF HOURS BEFORE HTE LOGIN EXPIRES
	public static final String EXTRA_BEARING = "bearing";
	public static final String DOWNLOAD_APP_LOCATION = "downloadapp.apk";
	public static final String PREF_ADMIN_MODE = "isAdminMode";
	public static final String PREF_HAVE_SHORTCUT = "haveDesktopShortcut";
	//=== public static final String DELINFOSTATUS_VACANT = "VAC";
	public static final String DELINFOSTATUS_NO_PICKUP = "NPU";
	public static final String DELINFOSTATUS_PLACE = "PLC";
	//=== Cleanup Intent Service
	public static final long MAX_FILE_AGE = 1000L * 60L * 24L * 7L;//10 days in milliseconds
	public static final String LOGGER_FILENAME = "AGD";
	public static int BAD_LOCATION_SANITY_DISTANCE_CHECK_METERS = 160000;    //Kilometers or ~100 miles

	public static int CONNECTIVITY_NONE = -99999;
	public static int CONNECTIVITY_SIGNAL_STRENGTH_ZERO = -99999;
	public static Class[] ALL_APP_SERVICES = {
			LocationUpdateService.class,
			UploadService.class,
			DownloadService.class
	};
	//=== public static final String DELINFOSTATUS_DO_NOT_DELIVER = "DND";

	public enum NAVIGATION_MODE{NAVIGATION_OFF, NAVIGATION_SOLO, NAVIGATION_SPLIT_SCREEN}

	public enum OPERATIONS_MODE{DELIVERING, SEQUENCING, RENUMBERING}

	public enum JOB_TYPE{UNDEFINED, VIP_Do_Not_Deliver, Subscriber, Do_Not_Deliver, Cannot_Deliver, Must_Deliver, Delivery}
}
