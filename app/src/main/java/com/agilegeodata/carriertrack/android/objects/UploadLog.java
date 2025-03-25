package com.agilegeodata.carriertrack.android.objects;

import android.content.ContentValues;

public class UploadLog{
	private long id;
	private String status;
	private String uploadType;
	private String uploadDataType;
	private long timestamp;
	private long timestampLocal;
	private int filesSentCount;
	private int numFilesConf;
	private int recordsToSendCount;
	private int numRecordsConf;
	private double latitude;
	private double longitude;
	private String address;
	private int newAddressesToSendCount;
	private int numNewAddressesConf;

	public ContentValues createContentValues(){
		ContentValues iVals = new ContentValues();
		iVals.put("_id", id);
		iVals.put("uploaddatatype", uploadDataType);
		iVals.put("uploadtype", uploadType);
		iVals.put("address", address);
		iVals.put("numfilesconf", numFilesConf);
		iVals.put("numfilessent", filesSentCount);
		iVals.put("latitude", latitude);
		iVals.put("longitude", longitude);
		iVals.put("numnewaddressesconf", numNewAddressesConf);
		iVals.put("numnewaddressessent", newAddressesToSendCount);
		iVals.put("numrecordssent", recordsToSendCount);
		iVals.put("numrecordsconf", numRecordsConf);
		iVals.put("timestamp", timestamp);
		iVals.put("timestamplocal", timestampLocal);
		iVals.put("status", status);
		return iVals;

	}

	public int getFilesSentCount(){
		return filesSentCount;
	}

	public void setFilesSentCount(int filesSentCount){
		this.filesSentCount = filesSentCount;
	}

	public int getNumFilesConf(){
		return numFilesConf;
	}

	public void setNumFilesConf(int numFilesConf){
		this.numFilesConf = numFilesConf;
	}

	public int getRecordsToSendCount(){
		return recordsToSendCount;
	}

	public void setRecordsToSendCount(int recordsToSendCount){
		this.recordsToSendCount = recordsToSendCount;
	}

	public int getNumRecordsConf(){
		return numRecordsConf;
	}

	public void setNumRecordsConf(int numRecordsConf){
		this.numRecordsConf = numRecordsConf;
	}

	public String getUploadDataType(){
		return uploadDataType;
	}

	public void setUploadDataType(String uploadDataType){
		this.uploadDataType = uploadDataType;
	}

	public long getId(){
		return id;
	}

	public void setId(long id){
		this.id = id;
	}

	public String getStatus(){
		return status;
	}

	public void setStatus(String status){
		this.status = status;
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

	public String getUploadType(){
		return uploadType;
	}

	public void setUploadType(String uploadType){
		this.uploadType = uploadType;
	}

	public double getLatitude(){
		return latitude;
	}

	public void setLatitude(double latitude){
		this.latitude = latitude;
	}

	public double getLongitude(){
		return longitude;
	}

	public void setLongitude(double longitude){
		this.longitude = longitude;
	}

	public String getAddress(){
		if(address == null){
			return "";
		}
		else{
			return address;
		}
	}

	public void setAddress(String address){
		this.address = address;
	}

	public int getNewAddressesToSendCount(){
		return newAddressesToSendCount;
	}

	public void setNewAddressesToSendCount(int newAddressesToSendCount){
		this.newAddressesToSendCount = newAddressesToSendCount;
	}

	public int getNumNewAddressesConf(){
		return numNewAddressesConf;
	}

	public void setNumNewAddressesConf(int numNewAddressesConf){
		this.numNewAddressesConf = numNewAddressesConf;
	}

	@Override
	public String toString(){
		return "UploadLog [id=" + id + ", status=" + status + ", uploadType="
			   + uploadType + ", uploadDataType=" + uploadDataType
			   + ", timestamp=" + timestamp + ", timestampLocal="
			   + timestampLocal + ", numFilesSent=" + filesSentCount
			   + ", numFilesConf=" + numFilesConf + ", numRecordsSent="
			   + recordsToSendCount + ", numRecordsConf=" + numRecordsConf
			   + ", latitude=" + latitude + ", longitude=" + longitude
			   + ", address=" + address + ", numNewAddressesSent="
			   + newAddressesToSendCount + ", numNewAddressesConf="
			   + numNewAddressesConf + "]";
	}
}
