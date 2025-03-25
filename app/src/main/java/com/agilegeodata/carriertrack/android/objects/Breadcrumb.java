package com.agilegeodata.carriertrack.android.objects;

import android.content.ContentValues;

public class Breadcrumb{
	private int id;
	private double lat;
	private double lon;
	private int jobDetailId;
	private double resolution;
	private double direction;
	private double speed;
	private String deliveryMode;
	private long timestamp;
	private long timestampLocal;
	private int uploaded;
	private int uploadBatchId;
	private String address;

	public Breadcrumb(){
	}

	public String getAddress(){
		return address;
	}

	public void setAddress(String address){
		this.address = address;
	}

	public ContentValues createIntialValues(){
		ContentValues iVals = new ContentValues();
		iVals.put("jobdetailid", this.jobDetailId);
		iVals.put("resolution", this.resolution);
		iVals.put("direction", this.direction);
		iVals.put("speed", this.speed);
		iVals.put("deliverymode", this.deliveryMode);
		iVals.put("timestamplocal", this.timestampLocal);
		iVals.put("timestamp", this.timestamp);
		iVals.put("uploaded", this.uploaded);
		iVals.put("uploadbatchid", this.uploadBatchId);
		iVals.put("lat", this.lat);
		iVals.put("long", this.lon);
		iVals.put("resolvedaddress", this.address);

		return iVals;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
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

	public int getJobDetailId(){
		return jobDetailId;
	}

	public void setJobDetailId(int jobDetailId){
		this.jobDetailId = jobDetailId;
	}

	public double getResolution(){
		return resolution;
	}

	public void setResolution(double resolution){
		this.resolution = resolution;
	}

	public double getDirection(){
		return direction;
	}

	public void setDirection(double direction){
		this.direction = direction;
	}

	public double getSpeed(){
		return speed;
	}

	public void setSpeed(double speed){
		this.speed = speed;
	}

	public String getDeliveryMode(){
		return deliveryMode;
	}

	public void setDeliveryMode(String deliveryMode){
		this.deliveryMode = deliveryMode;
	}

	public long getTimestamp(){
		return timestamp;
	}

	public void setTimestamp(long timestamp){
		this.timestamp = timestamp;
	}

	public long getTimestampLocal(){
		return timestampLocal;
	}

	public void setTimestampLocal(long timestampLocal){
		this.timestampLocal = timestampLocal;
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
}
