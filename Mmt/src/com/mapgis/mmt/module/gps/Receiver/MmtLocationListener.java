package com.mapgis.mmt.module.gps.Receiver;

import com.mapgis.mmt.module.gps.trans.GpsXYZ;

public interface MmtLocationListener {
    void onLocationChanged(GpsXYZ xy);
}
