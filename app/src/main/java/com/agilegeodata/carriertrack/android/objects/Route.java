package com.agilegeodata.carriertrack.android.objects;

import android.database.Cursor;

import com.agilegeodata.carriertrack.android.database.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class Route{
	private int id;   // database key
	private String routeId;
	private String interfaceType;
	private String routeJobType;
	private int routeFinished;
	private int jobDetailId;
	private int numAddress;
	private int numPhotosUploaded;
	private int numPhotos;
	private HashMap<Integer, StreetSummaryRandom> streetSummaryMap;
	private HashMap<Integer, Integer> addressStreetMap;

	private int numDelivered;
	private long lastStarted;
	private long lastEnded;
	private String jobId;
	private ArrayList<StreetSummaryRandom> streetSummaries;
	private String dateValidTo;
	private String dateValidFrom;
	private String deleted;
	private int projected;
	private int downloaded;

	private int lookaheadForward = 160;
	private int lookaheadSide = 80;
	private int deliveryForward = 40;
	private int deliverySide = 40;

	private String carrierType;

	public Route(){
		super();
		streetSummaries = new ArrayList<StreetSummaryRandom>();
	}

	public Route(Cursor cursor){
		super();

		//=== POPULATED LATER IF ROUTE IS RANDOM
		streetSummaries = new ArrayList<StreetSummaryRandom>();

		setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_ID)));   // database key
		setRouteId(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_ROUTEID)));   // database key
		setInterfaceType(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_INTERFACETYPE)));   // database key
		setRouteJobType(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_ROUTETYPE)));   // database key
		setRouteFinished(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_ROUTEFINISHED)));   // database key
		setJobDetailId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_JOBDETAILID)));   // database key
		setJobId(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_JOBID)));   // database key

		HashMap<Integer, Integer> numAddresses = new HashMap<Integer, Integer>();
		numAddresses.putAll(DBHelper.getInstance().fetchNumAddressesByJobDetailId_Common(getJobDetailId()));
		if(numAddresses.isEmpty() || numAddresses.get(getJobDetailId()) == null){
			setNumAddress(0);
		}
		else{
			setNumAddress(numAddresses.get(getJobDetailId()));   // database key
		}

		HashMap<Integer, Integer> numDeliveredAddresses = new HashMap<Integer, Integer>();
		numDeliveredAddresses.putAll(DBHelper.getInstance().fetchNumDeliveredAddressesByJobDetailId_Common(getJobDetailId()));
		if(numDeliveredAddresses.isEmpty() || numDeliveredAddresses.get(getJobDetailId()) == null){
			setNumDelivered(0);
		}
		else{
			setNumDelivered(numDeliveredAddresses.get(getJobDetailId()));
		}

		HashMap<Integer, Integer> numPhotos = new HashMap<Integer, Integer>();
		numPhotos.putAll(DBHelper.getInstance().fetchNumPhotosByJobDetailId_Common(getJobDetailId()));
		if(numPhotos.isEmpty() || numPhotos.get(getJobDetailId()) == null){
			setNumPhotos(0);
		}
		else{
			setNumPhotos(numPhotos.get(getJobDetailId()));
		}

		HashMap<Integer, Integer> numPhotosUploaded = new HashMap<Integer, Integer>();
		numPhotosUploaded.putAll(DBHelper.getInstance().fetchNumUploadedPhotosByJobDetailId_Common(getJobDetailId()));
		if(numPhotosUploaded.isEmpty() || numPhotosUploaded.get(getJobDetailId()) == null){
			setNumPhotosUploaded(0);
		}
		else{
			setNumPhotosUploaded(numPhotosUploaded.get(getJobDetailId()));   // database key
		}

		//=== THESE WILL BE SET LATER IN THE RANDOM DELIVERY FUNCTIONS
		//setStreetSummaryMap(DBHelper.fetchStreetSummariesByJobDetailId_Random(getJobDetailId()));
		//setAddressStreetMap();
		//setStreetSummaries();

		HashMap<Integer, Integer> numDelivered = new HashMap<Integer, Integer>();
		numDelivered.putAll(DBHelper.getInstance().fetchNumDeliveredAddressesByJobDetailId_Common(getJobDetailId()));
		if(numDelivered.isEmpty() || numDelivered.get(getJobDetailId()) == null){
			setNumDelivered(0);
		}
		else{
			setNumDelivered(numDelivered.get(getJobDetailId()));   // database key
		}

		setLastStarted(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.KEY_LASTSTARTDATE)));   // database key
		setLastEnded(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.KEY_LASTENDDATE)));   // database key
//		setJobId(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_JOBID)));   // database key

		setDateValidTo(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_DATEVALIDTO)));   // database key
		setDateValidFrom(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_DATEVALIDFROM)));   // database key
		setDeleted(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_DELETED)));   // database key
		setProjected(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_PROJECTED)));   // database key
		setDownloaded(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_DOWNLOADED)));   // database key

		try{
			setLookaheadForward(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_LOOKAHEADFORWARD)));   // database key
			setLookaheadSide(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_LOOKAHEADSIDE)));   // database key
			setDeliveryForward(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYFORWARD)));   // database key
			setDeliverySide(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_DELIVERYSIDE)));   // database key
		}
		catch(Exception e){
			setLookaheadForward(160);
			setLookaheadSide(80);
			setDeliveryForward(40);
			setDeliverySide(40);
		}

		setCarrierType(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_CARRIER_TYPE)));   // database key
	}

	public String getCarrierType(){
		return carrierType;
	}

	public void setCarrierType(String carrierType){
		this.carrierType = carrierType;
	}

	public boolean isContractor(){
		boolean isContractor = carrierType != null && carrierType.toUpperCase().equals(RouteCarrierType.CONTRACT.toString());
		return isContractor;
	}

	public boolean isEmployee(){
		boolean isEmployee = carrierType == null || carrierType.toUpperCase().equals(RouteCarrierType.EMPLOYEE.toString());
		return isEmployee;
	}

	public int getLookaheadForward(){
		return lookaheadForward;
	}

	public void setLookaheadForward(int lookaheadForward){
		this.lookaheadForward = lookaheadForward;
	}

	public int getLookaheadSide(){
		return lookaheadSide;
	}

	public void setLookaheadSide(int lookaheadSide){
		this.lookaheadSide = lookaheadSide;
	}

	public int getDeliveryForward(){
		return deliveryForward;
	}

	public void setDeliveryForward(int deliveryForward){
		this.deliveryForward = deliveryForward;
	}

	public int getDeliverySide(){
		return deliverySide;
	}

	public void setDeliverySide(int deliverySide){
		this.deliverySide = deliverySide;
	}

	public String getRouteJobType(){
		return routeJobType;
	}

	public void setRouteJobType(String routeJobType){
		if(routeJobType == null){
			routeJobType = Route.RouteJobType.RANDOM.toString();
		}
		this.routeJobType = routeJobType;
	}

	public int getRouteFinished(){
		return routeFinished;
	}

	public void setRouteFinished(int routeFinished){
		this.routeFinished = routeFinished;
	}

	public int getNumPhotos(){
		return numPhotos;
	}

	public void setNumPhotos(int numPhotos){
		this.numPhotos = numPhotos;
	}

	public long getLastEnded(){
		return lastEnded;
	}

	public void setLastEnded(long lastEnded){
		this.lastEnded = lastEnded;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public String getInterfaceType(){
		return interfaceType;
	}

	public void setInterfaceType(String interfaceType){
		this.interfaceType = interfaceType;
	}

	public HashMap<Integer, Integer> getAddressStreetMap(){
		return addressStreetMap;
	}

	public void setAddressStreetMap(HashMap<Integer, Integer> addressStreetMap){
		this.addressStreetMap = addressStreetMap;
	}

	public HashMap<Integer, StreetSummaryRandom> getStreetSummaryMap(){
		return streetSummaryMap;
	}

	public void setStreetSummaryMap(HashMap<Integer, StreetSummaryRandom> streetSummaryMap){
		this.streetSummaryMap = streetSummaryMap;
	}

	public int getJobDetailId(){
		return jobDetailId;
	}

	public void setJobDetailId(int jobDetailId){
		this.jobDetailId = jobDetailId;
	}

	public String getRouteId(){
		return routeId;
	}

	public void setRouteId(String routeId){
		this.routeId = routeId;
	}

	public ArrayList<StreetSummaryRandom> getStreetSummaries(){
		return streetSummaries;
	}

	public void setStreetSummaries(ArrayList<StreetSummaryRandom> streetSummaries){
		this.streetSummaries = streetSummaries;
	}

	public int getNumAddress(){
		return numAddress;
	}

	public void setNumAddress(int numAddress){
		this.numAddress = numAddress;
	}

	public int getNumPhotosUploaded(){
		return numPhotosUploaded;
	}

	public void setNumPhotosUploaded(int numPhotosUploaded){
		this.numPhotosUploaded = numPhotosUploaded;
	}

	public int getNumDelivered(){
		return numDelivered;
	}

	public void setNumDelivered(int numDelivered){
		this.numDelivered = numDelivered;
	}

	public long getLastStarted(){
		return lastStarted;
	}

	public void setLastStarted(long lastStarted){
		this.lastStarted = lastStarted;
	}

	public String getDateValidTo(){
		return dateValidTo;
	}

	public void setDateValidTo(String dateValidTo1){
		this.dateValidTo = dateValidTo1;
	}

	public String getDateValidFrom(){
		return dateValidFrom;
	}

	public void setDateValidFrom(String dateValidFrom1){
		this.dateValidFrom = dateValidFrom1;
	}

	public String getDeleted(){
		return deleted;
	}

	public void setDeleted(String deleted1){
		this.deleted = deleted1;
	}

	public String getJobId(){
		return jobId;
	}

	public void setJobId(String jobId){
		this.jobId = jobId;
	}

	public int getProjected(){
		return projected;
	}

	public void setProjected(int projected1){
		this.projected = projected1;
	}

	public int getDownloaded(){
		return downloaded;
	}

	public void setDownloaded(int downloaded1){
		this.downloaded = downloaded1;
	}

	public enum RouteJobType{SEQUENCED, RANDOM, UNSEQ}

	public enum RouteCarrierType{CONTRACT, EMPLOYEE}
}
