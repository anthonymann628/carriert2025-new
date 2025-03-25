package com.agilegeodata.carriertrack.android.objects;

import android.content.ContentValues;

public class LoginDetail{
	private int id;
	private long loginDate;
	private String status;
	private int uploaded;
	private int uploadBatchId;

	public LoginDetail(){
	}

	public ContentValues createIntialValues(){
		ContentValues iVals = new ContentValues();
		iVals.put("logindate", this.loginDate);
		iVals.put("status", this.status);
		iVals.put("uploaded", this.uploaded);
		iVals.put("uploadbatchid", this.uploadBatchId);
		return iVals;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public long getLoginDate(){
		return loginDate;
	}

	public void setLoginDate(long loginDate){
		this.loginDate = loginDate;
	}

	public String getStatus(){
		return status;
	}

	public void setStatus(String status){
		this.status = status;
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
