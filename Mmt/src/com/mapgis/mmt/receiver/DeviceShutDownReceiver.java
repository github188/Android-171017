package com.mapgis.mmt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.mapgis.mmt.module.navigation.NavigationController;
import com.mapgis.mmt.common.util.TrafficUtil;

/**
 * 接收关机的广播
 */
public class DeviceShutDownReceiver extends BroadcastReceiver {

	public static final String TRAFFIC = "traffic";
	public static final String TRAFFICFILE = "historyTraffic  l;d";
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = context.getSharedPreferences("historyTraffic", Context.MODE_PRIVATE);
		Editor edit = sp.edit();

		long lastTraffic = sp.getLong(TRAFFIC, 0); // 上次关机之前记录的流量数据

		// 上次开机到现在的流量数据
		long currTraffic = TrafficUtil.getTraffic(context, context.getPackageName());
		
		long lastMonth = sp.getLong("lastMonth", 0);

		// 更新截止到目前为止使用的流量
		edit.putLong(TRAFFIC, lastTraffic + currTraffic - lastMonth);

		edit.putLong("lastMonth", 0); // 关机就将上个月的流量数据清除
		edit.commit();

        NavigationController.exitAppSilent(context);
	}
}
