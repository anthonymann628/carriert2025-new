package com.agilegeodata.carriertrack.android.objects;

import android.content.ContentValues;

import java.util.ArrayList;

public class StreetSummaryRandom{

	private final ArrayList<DeliveryItem> routeDetails;
	private int id;   // database key
	private int summaryId;   // database key
	private int jobDetailId;   // database key
	private String streetName;
	private double gpsLocationLatitude;
	private double gpsLocationLongitude;
	private int numRemaining;
	private int numDND;
	private int numCustSvc;

	public StreetSummaryRandom(){
		super();
		routeDetails = new ArrayList<DeliveryItem>();
	}

	public ContentValues createCV(){

		ContentValues iVals = new ContentValues();
		iVals.put("summaryId", summaryId);
		iVals.put("jobDetailId", jobDetailId);
		iVals.put("streetName", streetName);
		iVals.put("Lat", gpsLocationLatitude);
		iVals.put("Long", gpsLocationLongitude);
		iVals.put("numRemaining", numRemaining);
		iVals.put("numDND", numDND);
		iVals.put("numCustSvc", numCustSvc);
		return iVals;
	}

	public ArrayList<DeliveryItem> getRouteDetails(){
		return routeDetails;
	}

	public int getSummaryId(){
		return summaryId;
	}

	public void setSummaryId(int summaryId){
		this.summaryId = summaryId;
	}

	public int getJobDetailId(){
		return jobDetailId;
	}

	public void setJobDetailId(int jobDetailId){
		this.jobDetailId = jobDetailId;
	}

	public String getStreetName(){
		return streetName;
	}

	public void setStreetName(String streetName){
		this.streetName = streetName;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
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

	public int getNumRemaining(){
		return numRemaining;
	}

	public void setNumRemaining(int numRemaining){
		this.numRemaining = numRemaining;
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

}
