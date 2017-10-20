package com.mapgis.mmt.common.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;

public class TrafficUtil {
	/**
	 * @param context
	 * @param packageName
	 * @return 从上次关机到现在所使用的流量
	 */
	public static long getTraffic(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		ApplicationInfo appInfo;
		try {
			appInfo = pm.getApplicationInfo(packageName, 0);
			int uid = appInfo.uid;
			long tx = TrafficStats.getUidTxBytes(uid);
			long rx = TrafficStats.getUidRxBytes(uid);
			if (tx != -1 && rx != -1) {
				return tx + rx;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
