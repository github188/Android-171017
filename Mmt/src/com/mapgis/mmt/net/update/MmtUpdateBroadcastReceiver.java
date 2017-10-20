package com.mapgis.mmt.net.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.io.File;
import java.util.ArrayList;

public class MmtUpdateBroadcastReceiver extends BroadcastReceiver {
	ArrayList<HandleObj> objs;

	HandleObj obj = new HandleObj();
	Handler handler;

	public MmtUpdateBroadcastReceiver() {
	}

	public MmtUpdateBroadcastReceiver(ArrayList<HandleObj> objs, Handler handler) {
		this.objs = objs;

		this.handler = handler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		for (HandleObj h : objs) {
			if (h.downloadMap.MapName.equals(intent.getStringExtra("mapName"))) {
				obj.downloadMap = h.downloadMap;
				obj.layout = h.layout;

				break;
			}
		}

		obj.startTick = intent.getLongExtra("startTick", 0l);
		obj.preSize = intent.getIntExtra("preSize", 0);
		obj.current = intent.getDoubleExtra("current", 0);
		obj.total = intent.getDoubleExtra("total", 0);

		Bundle bundle = intent.getExtras();
		if (bundle != null && bundle.containsKey("file")) {
			obj.file = new File(intent.getStringExtra("file"));
		}

		handler.obtainMessage(intent.getIntExtra("what", 0), obj).sendToTarget();
	}
}
