package com.mapgis.mmt.module.gps.util;

import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.BroadcastGpsReceiver;
import com.mapgis.mmt.module.gps.entity.GPGGAInfo;
import com.mapgis.mmt.module.gps.entity.GPVTGInfo;

/**
 * Created by Comclay on 2017/2/24.
 * 蓝牙定位方式中对NMAE卫星数据的处理方式
 */

public class BTNmeaUtils extends NmeaUtil {
    private static final String TAG = "BTNmeaUtils";
    private GPGGAInfo info = null;

    @Override
    public void handleGpgga(String gpgga) {
        if (info != null) {
            Location location = info.getLocation();
            GpsReceiver.getInstance().setLastLocation(location);
        }
        info = new GPGGAInfo();
        info.GetGPGGA(gpgga);
    }

    /*
    精度
     */
    @Override
    public void handleGpgst(String gpgst) {
        if (info != null) {
            info.GetGPGST(gpgst);
        }
    }

    /**
     * 直接用广播发出去
     *
     * @param gpgsv 卫星信息
     */
    @Override
    public void handleGpgsv(String gpgsv) {
        Intent intent = new Intent(BroadcastGpsReceiver.ACTION_GPGSV);
        intent.putExtra("gpgsv", gpgsv);
        MyApplication.getInstance().sendBroadcast(intent);
    }

    /**
     * 速度
     */
    @Override
    public void handleGpvtg(String nmea) {
        if (info != null) {
            GPVTGInfo gpvtgInfo = new GPVTGInfo();
            gpvtgInfo.initFromStr(nmea);
            float speed = gpvtgInfo.getmSpeedKilo();
            // 公里/小时转换为m/s
            speed = (float) (speed / 3.6);
            info.setSpeed(speed);
        }
    }
}
