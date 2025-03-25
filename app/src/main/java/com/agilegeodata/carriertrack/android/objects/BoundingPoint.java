package com.agilegeodata.carriertrack.android.objects;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;

public class BoundingPoint{
	double latitude;
	double longitude;

	public BoundingPoint(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public BoundingPoint(String geocode){
		try{
			String[] coords = geocode.split(",");

			this.latitude = new Double(coords[0]);
			this.longitude = new Double(coords[1]);
		}
		catch(Exception e){

		}

	}

	@Override
	public String toString(){
		return CTApp.getCustomAppContext().getResources().getString(R.string.dataObjectBoundingPointCoordinates) + latitude + "," + longitude;
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
}
