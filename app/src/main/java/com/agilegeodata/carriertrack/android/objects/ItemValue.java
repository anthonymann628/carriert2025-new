package com.agilegeodata.carriertrack.android.objects;

import android.content.ContentValues;

public class ItemValue{
	private int id;
	private String itemName;
	private String itemValue;

	public ItemValue(){
	}

	public ContentValues createIntialValues(){
		ContentValues iVals = new ContentValues();
		iVals.put("_id", this.id);
		iVals.put("itemname", this.itemName);
		iVals.put("itemvalue", this.itemValue);
		return iVals;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public String getItemName(){
		return itemName;
	}

	public void setItemName(String itemName){
		this.itemName = itemName;
	}

	public String getItemValue(){
		return itemValue;
	}

	public void setItemValue(String itemValue){
		this.itemValue = itemValue;
	}
}
