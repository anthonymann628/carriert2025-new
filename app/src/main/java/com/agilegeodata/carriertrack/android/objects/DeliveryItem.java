package com.agilegeodata.carriertrack.android.objects;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;
import com.agilegeodata.carriertrack.android.utils.GPSUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.LinkedHashMap;

public class DeliveryItem implements Comparable<DeliveryItem>{

	public static final String TAG = DeliveryItem.class.getSimpleName();
	static public final Comparator<DeliveryItem> ORDERING_SEQUENCEID =
			new Comparator<DeliveryItem>(){
				public int compare(DeliveryItem e1, DeliveryItem e2){
					try{
						Integer e1int = Integer.valueOf(e1.getSequence());
						Integer e2int = Integer.valueOf(e2.getSequence());
						//	logger.debug("Comparing: " + e1int +" to " + e2int + "e1int.compareTo(e2int): " + e1int.compareTo(e2int));

						return e1int.compareTo(e2int);
					}
					catch(Exception e){
						return -1;
					}
				}
			};
	static public final Comparator<DeliveryItem> ORDERING_SUMMARYID =
			new Comparator<DeliveryItem>(){
				public int compare(DeliveryItem e1, DeliveryItem e2){
					try{
						Integer e1int = Integer.valueOf(e1.getSummaryId());
						Integer e2int = Integer.valueOf(e2.getSummaryId());
						//	logger.debug("Comparing: " + e1int +" to " + e2int + "e1int.compareTo(e2int): " + e1int.compareTo(e2int));

						return e1int.compareTo(e2int);
					}
					catch(Exception e){
						return -1;
					}
				}
			};
	static public final Comparator<DeliveryItem> ORDERING_DISTANCE_ASC =
			new Comparator<DeliveryItem>(){
				public int compare(DeliveryItem e1, DeliveryItem e2){
					try{
						Double e1int = new Double(e1.getDistance());
						Double e2int = new Double(e2.getDistance());

						return e1int.compareTo(e2int);
					}
					catch(Exception e){
						return -1;
					}
				}
			};
	static public final Comparator<DeliveryItem> ORDERING_DISTANCE_DESC =
			new Comparator<DeliveryItem>(){
				public int compare(DeliveryItem e1, DeliveryItem e2){

					try{

						Double e1int = new Double(e1.getDistance());
						Double e2int = new Double(e2.getDistance());
						return e2int.compareTo(e1int);

					}
					catch(Exception e){
						return -1;
					}
				}
			};
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);
	LinkedHashMap<Integer, DeliveryItemProduct> productHashMap = new LinkedHashMap<Integer, DeliveryItemProduct>();
	private int id;
	private int jobDetailId;
	private int summaryId;
	private long deliveryId;
	private int status;
	private int delivered = -1;    //-1 not processed, 0 non-delivery processed, 1 delivery delivered
	private double deliveredLatitude;    //-1 not processed, 0 non-delivery processed, 1 delivery delivered
	private double deliveredLongitude;    //-1 not processed, 0 non-delivery processed, 1 delivery delivered
	private int wasReconciled;
	private int numPhotosUploaded;
	private double distance;
	private double gpsLocationLatitude;
	private double gpsLocationLongitude;
	private double gpsPhotoLocationLatitude;
	private double gpsPhotoLocationLongitude;
	private String gpsLocationAddressStreet;
	private String gpsLocationAddressNumber;
	private String recordType;
	private long deliveredTime;
	private int jobType;
	private Location location;
	private int quantity;
	private int numDelivered;
	private int numDND;
	private int numCustSvc;
	private String notes;
	private boolean photoRequired;
	private boolean photoTaken;
	private long listDisplayTime = 0;
	private int dndWasProcessed = 0;
	private int sequence = -1;
	private int sequenceNew = -1;
	private int uploaded = -1;
	private String sequenceModeNew;
	private boolean startingPoint = false;
	private boolean isInvalidAddress = false;

	public DeliveryItem(){
	}

	public DeliveryItem(Cursor cur){
//		logger.debug("cursor column count = " + cur.getColumnCount());
//		logger.debug("cursor row count = " + cur.getCount());
		setId(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID)));

		int columIndex = cur.getColumnIndexOrThrow(DBHelper.KEY_SUMMARYID);
		if(columIndex == -1){
			logger.debug("EXCEPTION : DBHelper.KEY_SUMMARYID column index is -1");
		}

		setSummaryId(cur.getInt(columIndex));
		setDeliveryId(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYID)));

		setGpsLocationAddressNumber(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_ADDRESS_NUMBER)));
		setGpsLocationAddressStreet(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_STREETADDRESS)));

		setGpsLocationLatitude(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT)));
		setGpsLocationLongitude(cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG)));

		int jobTypeFromRecord = cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBTYPE));
		setJobType(jobTypeFromRecord);

		setNotes(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_NOTES)));
		setNumCustSvc(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_CUSTSVC)));
		setQuantity(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_QUANTITY)));
		setNumDelivered(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_NUMDELIVERED)));

		setJobDetailId(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID)));
		setWasReconciled(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_WASRECONCILED)));

		setSequence(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_SEQUENCE)));
		setSequenceNew(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_SEQUENCENEW)));
		setSequenceModeNew(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_SEQMODENEW)));

		int photoRequiredFromRecord = cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_PHOTOREQUIRED));
		if(photoRequiredFromRecord == 1){
			setPhotoRequired(true);
		}
		int photoTakenFromRecord = cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_PHOTOTAKEN));
		if(photoTakenFromRecord == 1){
			setPhotoTaken(true);
		}

		PhotoDetail photo = DBHelper.getInstance().fetchPhotoByJobDetailAndDeliveryId_Common(this.jobDetailId + "", this.deliveryId + "");
		setGpsPhotoLocationLatitude(photo.getLat());
		setGpsPhotoLocationLongitude(photo.getLon());

		double thisLatitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LAT));
		double thisLongitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONG));
		Location thisLocation = GPSUtils.convertToLocationFromGeoCode(thisLatitude + "," + thisLongitude);
		setLocation(thisLocation);

		//=== NEGATIVE SEQUENCE
		if(getSequence() <= 0 || (thisLatitude == 0 && thisLongitude == 0)){
			setIsInvalidAddress(true);
		}

		setUploaded(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADED)));

		boolean deliveredFromRecordIsNull = cur.isNull(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERED));
		int deliveredFromRecord = deliveredFromRecordIsNull ? -1 : cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERED));
		setDelivered(deliveredFromRecord);

		double deliveryLatitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LATDELIVERED));
		setDeliveredLatitude(deliveryLatitude);
		double deliveryLongitude = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.KEY_LONGDELIVERED));
		setDeliveredLongitude(deliveryLongitude);

		long deliveryTimeFromRecord = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYDATE));
		setDeliveredTime(deliveryTimeFromRecord);

		setDndWasProcessed(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_DNDWASPROCESSED)));

		setNumDND(0);
		setNumPhotosUploaded(0);

		long listDisplayTimeFromRecord = cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_LISTDISPLAYTIME));
		if(listDisplayTimeFromRecord > 0){
			listDisplayTimeFromRecord = System.currentTimeMillis();
		}
		setListDisplayTime(listDisplayTimeFromRecord);
		setStartingPoint(false);
		setStatus(GlobalConstants.ROUTE_DETAIL_STATUS_LIST_ITEM);

		buildProductTable();
//logger.debug("DeliveryItem getProductsAsText() = " + getProductsAsText());
	}

	public int updateDatabaseRecord(){
		Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

		int rowsAffected = -1;

		logger.debug("DeliveryItem.updateDatabaseRecord() : delivered = " + delivered + " for jobDetailId = " + jobDetailId + " and deliveryId = " + deliveryId);

		String query = "update addressdetaillist set delivered = " + delivered
					   + ", latdelivered = '" + deliveredLatitude + "'"
					   + ", longdelivered = '" + deliveredLongitude + "'"
					   + ", listdisplaytime = " + listDisplayTime
					   + ", dndwasprocessed = " + dndWasProcessed
					   + ", uploaded = 0"
					   + ", deliverydate = " + deliveredTime
					   + ", seqmodenew = '" + sequenceModeNew + "'"
					   + ", sequence = " + sequence
					   + ", sequencenew = " + sequenceNew
					   + " where (jobdetailid = " + jobDetailId + " and deliveryid = " + deliveryId + ")";

		DBHelper.getInstance().doExecQuery_Common(query);

		String[] whereArgs = new String[]{jobDetailId + "", deliveryId + ""};

		SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();

		Cursor updated = db.rawQuery("select * from addressdetaillist where jobdetailid = ? and deliveryid = ?", new String[]{jobDetailId + "", deliveryId + ""});
		updated.moveToNext();
		DeliveryItem di = new DeliveryItem(updated);
		logger.debug("DeliveryItem.updateDatabaseRecord() : UPDATED DELIVERY : address= " + di.getDeliveryResequencingInfo());

		return rowsAffected;
	}

	public String getDeliveryResequencingInfo(){
		String content = "";

		content += "DeliveryItem VALUES" + "\n";
		content += "  jobDetailId           = " + jobDetailId + "\n";
		content += "  deliveryId            = " + deliveryId + "\n";
		content += "  delivered             = " + delivered + "\n";
		content += "  uploaded              = " + uploaded + "\n";
		content += "  sequence              = " + sequenceNew + "\n";
		content += "  sequencenew           = " + sequenceNew + "\n";
		content += "  seqmodenew            = " + sequenceModeNew + "\n";
		content += "  latdelivered          = " + deliveredLatitude + "\n";
		content += "  longdelivered         = " + deliveredLongitude + "\n";
		content += "  deliverydate          = " + deliveredTime + "\n";
		content += "  gpsLocationLongitude  = " + gpsLocationLongitude + "\n";
		content += "  gpsLocationLatitude   = " + gpsLocationLatitude + "\n";

		return content;
	}

	public LinkedHashMap<Integer, DeliveryItemProduct> getProducts(){
		return productHashMap;
	}

	public void buildProductTable(){
		productHashMap = DBHelper.getInstance().fetchProductsForDeliveryItem_Common(this.jobDetailId, this.deliveryId);
	}

	public String getProductsAsText(){
		String productText = "";

		DeliveryItemProduct[] productArray = new DeliveryItemProduct[]{};
		productArray = getProducts().values().toArray(productArray);

		for(int i = 0; i < productArray.length; i++){
			DeliveryItemProduct product = productArray[i];
			productText += (product.getQuantity() + " " + product.getProductCode());
			if(productArray.length > 1 && i < productArray.length - 1){
				productText += " and ";
			}
		}

		return productText;
	}

	public int getUploaded(){
		return uploaded;
	}

	public void setUploaded(int uploaded){
		this.uploaded = uploaded;
	}

	public String getSequenceModeNew(){
		return sequenceModeNew;
	}

	public void setSequenceModeNew(String sequenceModeNew){
		this.sequenceModeNew = sequenceModeNew;
	}

	public boolean getIsInvalidAddress(){
		return isInvalidAddress;
	}

	public void setIsInvalidAddress(boolean addressValid){
		this.isInvalidAddress = addressValid;
	}

	public boolean getStartingPoint(){
		return startingPoint;
	}

	public void setStartingPoint(boolean startingPoint){
		this.startingPoint = startingPoint;
	}

	public int getSequence(){
		return sequence;
	}

	public void setSequence(int sequence){
		this.sequence = sequence;
	}

	public int getSequenceNew(){
		return sequenceNew;
	}

	public void setSequenceNew(int sequenceNew){
		this.sequenceNew = sequenceNew;
	}

	public int getDndWasProcessed(){
		return dndWasProcessed;
	}

	public void setDndWasProcessed(int dndWasProcessed){
		this.dndWasProcessed = dndWasProcessed;
	}

	public long getListDisplayTime(){
		return listDisplayTime;
	}

	public void setListDisplayTime(long listDisplayTime){
		this.listDisplayTime = listDisplayTime;
	}

	public String getNotes(){
		return notes;
	}

	public void setNotes(String notes){
		this.notes = notes;
	}

	public Location getLocation(){
		return location;
	}

	public void setLocation(Location location){
		this.location = location;
	}

	public int getDelivered(){
		return delivered;
	}

	public void setDelivered(int delivered){
		this.delivered = delivered;
	}

	public int getWasReconciled(){
		return wasReconciled;
	}

	public void setWasReconciled(int wasReconciled){
		this.wasReconciled = wasReconciled;
	}

	public String getRecordType(){
		return recordType;
	}

	public void setRecordType(String recordType){
		this.recordType = recordType;
	}

	public int getJobType(){
		return jobType;
	}

	public void setJobType(int jobType){
		this.jobType = jobType;
	}

	public int getStatus(){
		return status;
	}

	public void setStatus(int status){
		this.status = status;
	}

	public boolean isPhotoRequired(){
		return photoRequired;
	}

	public void setPhotoRequired(boolean photoRequired){
		this.photoRequired = photoRequired;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public long getDeliveredTime(){
		return deliveredTime;
	}

	public void setDeliveredTime(long deliveredTime){
		this.deliveredTime = deliveredTime;
	}

	public int getNumPhotosUploaded(){
		return numPhotosUploaded;
	}

	public void setNumPhotosUploaded(int numPhotos){
		this.numPhotosUploaded = numPhotos;
	}

	public double getGpsPhotoLocationLatitude(){
		return gpsPhotoLocationLatitude;
	}

	public void setGpsPhotoLocationLatitude(double gpsPhotoLocationLatitude){
		this.gpsPhotoLocationLatitude = gpsPhotoLocationLatitude;
	}

	public double getGpsPhotoLocationLongitude(){
		return gpsPhotoLocationLongitude;
	}

	public void setGpsPhotoLocationLongitude(double gpsPhotoLocationLongitude){
		this.gpsPhotoLocationLongitude = gpsPhotoLocationLongitude;
	}

	public double getGpsLocationLatitude(){
		return gpsLocationLatitude;
	}

	public void setGpsLocationLatitude(double gpsLocationLatitude){
		this.gpsLocationLatitude = gpsLocationLatitude;
	}

	public double getGpsLocationLongitude(){
		return gpsLocationLongitude;
	}

	public void setGpsLocationLongitude(double gpsLocationLongitude){
		this.gpsLocationLongitude = gpsLocationLongitude;
	}

	public double getDeliveredLatitude(){
		return deliveredLatitude;
	}

	public void setDeliveredLatitude(double gpsDeliveryLatitude){
		this.deliveredLatitude = gpsDeliveryLatitude;
	}

	public double getDeliveredLongitude(){
		return deliveredLongitude;
	}

	public void setDeliveredLongitude(double gpsDeliveryLongitude){
		this.deliveredLongitude = gpsDeliveryLongitude;
	}

	public String getGpsLocationAddressStreet(){
		return gpsLocationAddressStreet;
	}

	public void setGpsLocationAddressStreet(String gpsLocationAddressStreet){
		this.gpsLocationAddressStreet = gpsLocationAddressStreet;
	}

	public String getGpsLocationAddressNumber(){
		return gpsLocationAddressNumber;
	}

	public void setGpsLocationAddressNumber(String gpsLocationAddressNumber){
		this.gpsLocationAddressNumber = gpsLocationAddressNumber;
	}

	public int getNumRemaining(){
		return quantity - numDelivered - numDND;
	}

	public int getQuantity(){
		return quantity;
	}

	public void setQuantity(int quant){
		this.quantity = quant;
	}

	public int getNumDelivered(){
		return numDelivered;
	}

	public void setNumDelivered(int numDelivered){
		this.numDelivered = numDelivered;
	}

	public int getNumDND(){
		return numDND;
	}

	public void setNumDND(int numDND){
		this.numDND = numDND;
	}

	public int getNumCustSvc(){
		return numCustSvc;
	}

	public void setNumCustSvc(int numCustSvc){
		this.numCustSvc = numCustSvc;
	}

	public int getJobDetailId(){
		return jobDetailId;
	}

	public void setJobDetailId(int jobDetailId){
		this.jobDetailId = jobDetailId;
	}

	public int getSummaryId(){
		return summaryId;
	}

	public void setSummaryId(int summaryId){
		this.summaryId = summaryId;
	}

	public long getDeliveryId(){
		return deliveryId;
	}

	public void setDeliveryId(long deliveryId){
		this.deliveryId = deliveryId;
	}

	public boolean isPhotoTaken(){
		return photoTaken;
	}

	public void setPhotoTaken(boolean photoTaken){
		this.photoTaken = photoTaken;
	}

	public int compareTo(DeliveryItem arg0){
		//logger.debug("In generic compareTO");
		return 0;
	}

	public double getDistance(){
		return distance;
	}

	public void setDistance(double distance){
		this.distance = distance;
	}
}
