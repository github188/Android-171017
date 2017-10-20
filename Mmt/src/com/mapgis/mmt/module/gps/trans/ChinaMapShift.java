package com.mapgis.mmt.module.gps.trans;

/**
 * 国内WGS-84/GCJ-02/BD-09坐标互转类
 * 
 * @author Zoro
 * 
 */
public class ChinaMapShift {
	static double a = 6378245.0;
	static double ee = 0.00669342162296594323;

	/**
	 * WGS-84转GCJ-02,标准经纬度坐标转国测局坐标
	 * 
	 * @param wgLoc
	 *            WGS -84坐标
	 * @return GCJ-02坐标
	 */
	public static MmtLocation transformFromWGSToGCJ(MmtLocation wgLoc) {
		MmtLocation mgLoc = new MmtLocation();

		if (outOfChina(wgLoc.lat, wgLoc.lng)) {
			mgLoc = wgLoc.clone();
			return mgLoc;
		}

		double dLat = transformLat(wgLoc.lng - 105.0, wgLoc.lat - 35.0);
		double dLon = transformLon(wgLoc.lng - 105.0, wgLoc.lat - 35.0);

		double radLat = wgLoc.lat / 180.0 * Math.PI;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;

		double sqrtMagic = Math.sqrt(magic);

		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);

		mgLoc.lat = wgLoc.lat + dLat;
		mgLoc.lng = wgLoc.lng + dLon;

		return mgLoc;
	}

	/**
	 * GCJ-02转WGS-84,国测局坐标转标准经纬度坐标,迭代反算
	 * 
	 * @param gcLoc
	 *            GCJ -02坐标
	 * @return WGS-84坐标
	 */
	public static MmtLocation transformFromGCJToWGS(MmtLocation gcLoc) {
		MmtLocation wgLoc = gcLoc.clone();

		MmtLocation currGcLoc = new MmtLocation(), dLoc = new MmtLocation();
		while (true) {
			currGcLoc = transformFromWGSToGCJ(wgLoc);
			dLoc.lat = gcLoc.lat - currGcLoc.lat;
			dLoc.lng = gcLoc.lng - currGcLoc.lng;

			// 1e-7 centimeter level accuracy 精度要求
			// Result of experiment:
			// Most of the time 2 iterations would be enough for an 1e-8
			// accuracy (milimeter level).
			if (Math.abs(dLoc.lat) < 1e-7 && Math.abs(dLoc.lng) < 1e-7) {
				return wgLoc;
			}

			wgLoc.lat += dLoc.lat;
			wgLoc.lng += dLoc.lng;
		}
	}

	static double x_pi = Math.PI * 3000.0 / 180.0;

	/**
	 * GCJ-02转BD-09,国测局坐标加密为百度坐标
	 * 
	 * @param gcLoc
	 *            GCJ -02坐标
	 * @return BD-09坐标
	 */
	public static MmtLocation bd_encrypt(MmtLocation gcLoc) {
		double x = gcLoc.lng, y = gcLoc.lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
		MmtLocation res = new MmtLocation();
		res.lat = z * Math.sin(theta) + 0.006;
		res.lng = z * Math.cos(theta) + 0.0065;
		return res;
	}

	/**
	 * BD-09转GCJ-02,百度坐标解密为国测局坐标
	 * 
	 * @param bdLoc
	 *            BD-09坐标
	 * @return GCJ-02坐标
	 */
	public static MmtLocation bd_decrypt(MmtLocation bdLoc) {
		double x = bdLoc.lng - 0.0065, y = bdLoc.lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		MmtLocation res = new MmtLocation();
		res.lat = z * Math.sin(theta);
		res.lng = z * Math.cos(theta);
		return res;

	}

	/**
	 * 判断是否出离中国,国外返回原始坐标,因为国外不存在偏移
	 * 
	 * @param lat
	 *            纬度
	 * @param lon
	 *            经度
	 * @return 国内返回false,国外返回true
	 */
	private static boolean outOfChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347) {
			return true;
		}
        return lat < 0.8293 || lat > 55.8271;
    }

	private static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(x > 0 ? x : -x);
		ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	private static double transformLon(double x, double y) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(x > 0 ? x : -x);
		ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
		return ret;
	}

}
