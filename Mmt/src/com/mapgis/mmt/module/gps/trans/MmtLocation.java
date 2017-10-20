package com.mapgis.mmt.module.gps.trans;

import android.location.Location;

import com.mapgis.mmt.module.gps.GpsReceiver;

public class MmtLocation {
	public double lat;
	public double lng;

	@Override
	public String toString() {
		return lat + "," + lng;
	}

	@Override
	protected MmtLocation clone() {
		MmtLocation location = new MmtLocation();

		location.lat = lat;
		location.lng = lng;

		return location;
	}

	/**
	 * 将获取到的BD-09(百度经纬度坐标)转换为当地的平面坐标
	 * 
	 * @return 平面坐标
	 */
	public GpsXYZ convert2Xyz() {
		// BD-09==>GCJ-02==>WGS84
		MmtLocation wgs84Location = ChinaMapShift.transformFromGCJToWGS(ChinaMapShift.bd_decrypt(this));

		Location location = new Location("BDPlaceSearch");

		location.setLongitude(wgs84Location.lng);
		location.setLatitude(wgs84Location.lat);

		return GpsReceiver.getInstance().getLastLocation(location);
	}
}