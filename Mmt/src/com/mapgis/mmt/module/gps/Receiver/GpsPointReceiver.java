package com.mapgis.mmt.module.gps.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class GpsPointReceiver extends BroadcastReceiver {
    public interface MyLocationListener {
        void onLocationUpdated(Bundle bundle);
    }

    public MyLocationListener locationListener;

    public GpsPointReceiver() {
    }

    public GpsPointReceiver(MyLocationListener Listener) {
        this.locationListener = Listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (locationListener != null) {
            locationListener.onLocationUpdated(intent.getExtras());
        }
    }
}