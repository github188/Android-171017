package com.mapgis.mmt.module.gps.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.global.MmtBaseThread;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

import java.util.Date;

/**
 * 随机模拟坐标定位
 */
public class RandomGpsReceiver extends GpsReceiver {
    boolean isRun;
    Context context;

    @Override
    public String start(CoordinateConvertor coordinateConvertor) {
        this.coordinateConvertor = coordinateConvertor;
        this.context = MyApplication.getInstance();

        context.registerReceiver(receiver, new IntentFilter(MmtMainService.class.getName()));

        isRun = true;

        worker.start();

        return null;
    }

    MmtBaseThread worker = new MmtBaseThread() {
        @Override
        public void run() {
            while (isRun) {
                try {
                    if (Double.isNaN(x) || Double.isNaN(y))
                        continue;

                    GpsXYZ xy = new GpsXYZ(x, y);

                    Location location = getLastLocationConverse(new GpsXYZ(x, y));

                    if (location == null) {
                        location = new Location("Random");
                    } else
                        location.setProvider("Random");

                    location.setAccuracy(0.1f);
                    location.setSpeed(0.1f);
                    location.setTime(new Date().getTime());

                    xy.setLocation(location);

                    setLastXY(xy);

                    lastLocation = location;

                    Thread.sleep(3 * 1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    double x = Double.NaN, y = Double.NaN;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.hasExtra("cx"))
                return;

            x = intent.getDoubleExtra("cx", Double.NaN);
            y = intent.getDoubleExtra("cy", Double.NaN);
        }
    };

    public void stop() {
        context.unregisterReceiver(receiver);

        isRun = false;
    }
}
