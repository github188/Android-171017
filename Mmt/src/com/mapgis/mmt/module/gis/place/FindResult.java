package com.mapgis.mmt.module.gis.place;

import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.module.gps.trans.MmtLocation;

import java.util.ArrayList;

public class FindResult {
	public MmtLocation location;
	public String formatted_address;
	public String business;
	public AddressComponent addressComponent;
	public ArrayList<Poi> pois;
	public int cityCode;

	/**
	 * 将获取到的BD-09(百度经纬度坐标)转换为当地的平面坐标
	 * 
	 * @return 平面坐标
	 */
	public GpsXYZ getXyz() {
		return location.convert2Xyz();
	}
}