package com.mapgis.mmt.module.gis.place;

import android.location.Location;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.ChinaMapShift;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.module.gps.trans.MmtLocation;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BDDirectionAPI {
	public static final String BD_SEARCH_CITY = "BdSearchCity";
	public static final String BD_DEV_KEY = "BdDevKey";

	/**
	 * 起始点 目标点导航
	 * 
	 * @param origin
	 *            起点名称或经纬度，或者可同时提供名称和经纬度，此时经纬度优先级高，将作为导航依据，名称只负责展示。
	 * @param destination
	 *            起点名称或经纬度，或者可同时提供名称和经纬度，此时经纬度优先级高，将作为导航依据，名称只负责展示。
	 * @return 路径点序列
	 */
	public static ArrayList<Dot> start(String origin, String destination) {
		try {

			String city = MyApplication.getInstance().getConfigValue(BD_SEARCH_CITY).length() == 0 ? MyApplication.getInstance()
					.getString(R.string.bd_search_city) : MyApplication.getInstance().getConfigValue(BD_SEARCH_CITY);

			String key = MyApplication.getInstance().getConfigValue(BD_DEV_KEY).length() == 0 ? MyApplication.getInstance()
					.getString(R.string.bd_dev_key) : MyApplication.getInstance().getConfigValue(BD_DEV_KEY);

			String result = NetUtil.executeHttpGet("http://api.map.baidu.com/direction/v1", "mode", "walking", "origin", origin,
					"destination", destination, "region", city, "output", "json", "coord_type", "gcj02", "ak", key);

			if (!BaseClassUtil.isNullOrEmptyString(result)) {
				ArrayList<Dot> dots = new ArrayList<Dot>();

				Pattern p = Pattern.compile("\"path\":\"(.*?),(.*?)\",");
				Matcher m = p.matcher(result);
				MmtLocation mmtLocation = new MmtLocation();
				Location location = new Location("");
				GpsXYZ xyz = null;

				while (m.find()) {
					mmtLocation.lng = Double.valueOf(m.group(1));
					mmtLocation.lat = Double.valueOf(m.group(2));

					mmtLocation = ChinaMapShift.bd_decrypt(mmtLocation);// ChinaMapShift.transformFromGCJToWGS(ChinaMapShift.bd_decrypt(mmtLocation));

					location.setLatitude(mmtLocation.lat);
					location.setLongitude(mmtLocation.lng);

					xyz = GpsReceiver.getInstance().getLastLocation(location);

					dots.add(new Dot(xyz.getX(), xyz.getY()));
				}

				return dots;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 经纬度方式的起始点目标点导航
	 * 
	 * @param origin
	 *            经纬度方式的起始点
	 * @param destination
	 *            经纬度方式的目标点
	 * @return
	 */
	public static ArrayList<Dot> start(Location origin, Location destination) {
		return start(origin.getLatitude() + "," + origin.getLongitude(),
				destination.getLatitude() + "," + destination.getLongitude());
	}

	/**
	 * 以当前GPS到位点为起始点,导航到目标点
	 * 
	 * @param destination
	 *            经纬度方式的目标点
	 * @return
	 */
	public static ArrayList<Dot> start(Location destination) {
		Location origin = GpsReceiver.getInstance().getLastLocation();

		return start(origin.getLatitude() + "," + origin.getLongitude(),
				destination.getLatitude() + "," + destination.getLongitude());
	}

	/**
	 * 以当前GPS到位点为起始点,导航到目标点
	 * 
	 * @param destination
	 *            本地平面坐标方式的目标点
	 * @return
	 */
	public static ArrayList<Dot> start(GpsXYZ destination) {
		Location origin = GpsReceiver.getInstance().getLastLocation();

		Location destinationLocation = GpsReceiver.getInstance().getLastLocationConverse(destination);

		return start(origin.getLatitude() + "," + origin.getLongitude(), destinationLocation.getLatitude() + ","
				+ destinationLocation.getLongitude());
	}

	/**
	 * 本地平面坐标方式的起始点目标点导航
	 * 
	 * @param origin
	 *            本地平面坐标方式的起始点
	 * @param destination
	 *            本地平面坐标方式的目标点
	 * @return
	 */
	public static ArrayList<Dot> start(GpsXYZ origin, GpsXYZ destination) {
		Location originLocation = GpsReceiver.getInstance().getLastLocationConverse(origin);

		Location destinationLocation = GpsReceiver.getInstance().getLastLocationConverse(destination);

		return start(originLocation.getLatitude() + "," + originLocation.getLongitude(), destinationLocation.getLatitude() + ","
				+ destinationLocation.getLongitude());
	}
}
