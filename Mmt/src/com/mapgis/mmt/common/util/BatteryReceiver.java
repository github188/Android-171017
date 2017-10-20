package com.mapgis.mmt.common.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryReceiver extends BroadcastReceiver {

	private static BatteryReceiver instance = null;

	public static BatteryReceiver getInstance() {
		if (instance == null) {
			instance = new BatteryReceiver();
		}

		return instance;
	}

	private int batteryLevel;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
			batteryLevel = intent.getIntExtra("level", 0);
		}
	}

	public String getBatteryLevel() {
		return batteryLevel + "%";
	}
}
