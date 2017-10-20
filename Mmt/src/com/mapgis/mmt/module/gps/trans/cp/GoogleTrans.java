package com.mapgis.mmt.module.gps.trans.cp;

import com.zondy.mapgis.geometry.Dot;

public class GoogleTrans {

	public GoogleTrans() {
	}

	// web墨卡托投影
	/*
	 * 经纬度使用的是弧度值
	 */
	public static Dot MercatorProject(Dot geoPos) {
		double lon = geoPos.x, lat = geoPos.y;
		double _gloabxmin = -180.0;
		double _gloabxmax = 180.0;
		double _gloabymin = -85.05112877980659;
		double _gloabymax = 85.05112877980659;
		double _r = 6378137;// 地球半径

		double x = 0;
		double y = 0;

		if (lon < _gloabxmin || lon > _gloabxmax) {
			return null;
		}
		if (lat < _gloabymin || lat > _gloabymax) {
			return null;
		}

		lon = lon * Math.PI / 180.0;
		x = _r * lon;

		lat = lat * Math.PI / 180.0;
		double sinLatitude = Math.sin(lat);
		y = _r / 2 * Math.log((1 + sinLatitude) / (1 - sinLatitude));
		return new Dot(x, y);
	}

	// 反向web墨卡托投影
	/*
	 * 经纬度使用的是度值
	 */
	public static Dot MercatorProjectReverse(Dot wPos) {
		double x = wPos.x;
		double y = wPos.y;

		double _gloabmin = -20037508.3427892;
		double _gloabmax = 20037508.3427892;
		double _r = 6378137;// 地球半径

		double lon = 0;
		double lat = 0;

		if (x < _gloabmin || x > _gloabmax || y < _gloabmin || y > _gloabmax) {
			return null;
		}

		lon = x / (Math.PI * _r) * 180.0;
		lat = y / (Math.PI * _r) * 180.0;
		lat = 180.0 / Math.PI * (2.0 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2.0);

		return new Dot(lon, lat);
	}
}
