package com.agilegeodata.carriertrack.android.objects;

import android.database.Cursor;

import com.agilegeodata.carriertrack.android.database.DBHelper;

public class DeliveryItemProduct{

	public static final String TAG = DeliveryItemProduct.class.getSimpleName();
	private int id;   // database key
	private String productCode;
	private int quantity;
	private long deliveryId;
	private int jobDetailId;
	private String scanCode;
	private String productType;

	private long deliveryDate;
	private long deliveryLatitude;
	private long deliveryLongitude;
	private int uploaded;
	private int uploadBatchId;

	public DeliveryItemProduct(){
	}

	public DeliveryItemProduct(Cursor cur){
		setId(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_ID)));
		setProductCode(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_PRODUCTCODE)));
		setQuantity(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_QUANTITY)));
		setJobDetailId(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID)));
		setDeliveryId(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYID)));
		setScanCode(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_SCANCODE)));

		setDeliveryDate(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYDATE)));
		setDeliveryLatitude(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERY_LATITUDE)));
		setDeliveryLongitude(cur.getLong(cur.getColumnIndexOrThrow(DBHelper.KEY_DELIVERY_LONGITUDE)));
		setUploaded(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADED)));
		setUploadBatchId(cur.getInt(cur.getColumnIndexOrThrow(DBHelper.KEY_UPLOADBATCHID)));
		setProductType(cur.getString(cur.getColumnIndexOrThrow(DBHelper.KEY_PRODUCTTYPE)));
	}

	public int getId(){
		return this.id;
	}

	public void setId(int id){
		this.id = id;
	}

	public String getProductType(){
		return productType;
	}

	public void setProductType(String type){
		this.productType = type;
	}

	public String getProductCode(){
		return productCode;
	}

	public void setProductCode(String code){
		this.productCode = code;
	}

	public String getScanCode(){
		return scanCode;
	}

	public void setScanCode(String code){
		this.scanCode = code;
	}

	public int getQuantity(){
		return quantity;
	}

	public void setQuantity(int quant){
		this.quantity = quant;
	}

	public int getJobDetailId(){
		return jobDetailId;
	}

	public void setJobDetailId(int jobDetailId){
		this.jobDetailId = jobDetailId;
	}

	public long getDeliveryId(){
		return deliveryId;
	}

	public void setDeliveryId(long deliveryId){
		this.deliveryId = deliveryId;
	}

	public long getDeliveryDate(){
		return deliveryDate;
	}

	public void setDeliveryDate(long date){
		deliveryDate = date;
	}

	public long getDeliveryLatitude(){
		return deliveryLatitude;
	}

	public void setDeliveryLatitude(long latitude1){
		deliveryLatitude = latitude1;
	}

	public long getDeliveryLongitude(){
		return deliveryLongitude;
	}

	public void setDeliveryLongitude(long longitude1){
		deliveryLongitude = longitude1;
	}

	public int getUploaded(){
		return uploaded;
	}

	public void setUploaded(int uploaded1){
		uploaded = uploaded1;
	}

	public int getUploadBatchId(){
		return uploadBatchId;
	}

	public void setUploadBatchId(int batchId){
		uploadBatchId = batchId;
	}
}
