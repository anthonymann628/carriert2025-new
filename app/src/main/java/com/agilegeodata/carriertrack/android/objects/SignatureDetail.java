package com.agilegeodata.carriertrack.android.objects;

import android.content.ContentValues;

public class SignatureDetail{
	private int id;
	private long deliveryId;
	private int jobDetailId;
	private long signatureDate;
	private int uploaded;
	private int uploadBatchId;
	private String filePath;
	private double lat;
	private double lon;

	public void Photo(){
	}

	public ContentValues createIntialValues(){
		ContentValues iVals = new ContentValues();
		//iVals.put("Id", this.Id);
		iVals.put("filepath", this.filePath);
		iVals.put("deliveryid", this.deliveryId);
		iVals.put("jobdetailid", this.jobDetailId);
		iVals.put("signaturedate", this.signatureDate);
		iVals.put("uploaded", this.uploaded);
		iVals.put("uploadbatchid", this.uploadBatchId);
		iVals.put("lat", this.lat);
		iVals.put("long", this.lon);
		return iVals;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public long getDeliveryId(){
		return deliveryId;
	}

	public void setDeliveryId(long deliveryId){
		this.deliveryId = deliveryId;
	}

	public int getJobDetailId(){
		return jobDetailId;
	}

	public void setJobDetailId(int jobDetailId){
		this.jobDetailId = jobDetailId;
	}

	public long getSignatureDate(){
		return signatureDate;
	}

	public void setSignatureDate(long signatureDate){
		this.signatureDate = signatureDate;
	}

	public int getUploaded(){
		return uploaded;
	}

	public void setUploaded(int uploaded){
		this.uploaded = uploaded;
	}

	public int getUploadBatchId(){
		return uploadBatchId;
	}

	public void setUploadBatchId(int uploadBatchId){
		this.uploadBatchId = uploadBatchId;
	}

	public String getFilePath(){
		return filePath;
	}

	public void setFilePath(String filePath){
		this.filePath = filePath;
	}

	public double getLat(){
		return lat;
	}

	public void setLat(double lat){
		this.lat = lat;
	}

	public double getLon(){
		return lon;
	}

	public void setLon(double lon){
		this.lon = lon;
	}
}
