package com.mapgis.mmt.module.gis.place;

import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.module.gps.trans.MmtLocation;

public class Poi {
	public String addr;
	public String cp;
	public String distance;
	public String name;
	public String poiType;

	public BDPoint point;

	public String tel;
	public String uid;
	public String zip;

	/**
	 * 将获取到的BD-09(百度经纬度坐标)转换为当地的平面坐标
	 * 
	 * @return 平面坐标
	 */
	public GpsXYZ getXyz() {
		MmtLocation location = new MmtLocation();

		location.lng = point.x;
		location.lat = point.y;

		return location.convert2Xyz();
	}
}