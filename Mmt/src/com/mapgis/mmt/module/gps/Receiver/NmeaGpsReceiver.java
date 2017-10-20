package com.mapgis.mmt.module.gps.Receiver;

import android.content.Context;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.entity.GPGGAInfo;
import com.mapgis.mmt.module.gps.trans.ChinaMapShift;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.gps.trans.MmtLocation;

import java.util.Date;

/**
 * 通用NMEA方式原生定位
 */
public class NmeaGpsReceiver extends GpsReceiver {
    Context context;
    LocationManager locationManager;

    @Override
    public String start(CoordinateConvertor coordinateConvertor) {
        try {
            this.coordinateConvertor = coordinateConvertor;

            context = MyApplication.getInstance();

            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

            locationManager.addNmeaListener(nmeaListener);

            startBD();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public void stop() {
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
                locationManager.removeNmeaListener(nmeaListener);
            }

            /** 解绑百度定位 **/
            {
                mLocationClient.unRegisterLocationListener(myListener);
                mLocationClient.stop();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    NmeaListener nmeaListener = new NmeaListener() {
        @Override
        public void onNmeaReceived(long timestamp, String nmea) {
            try {
                if (nmea.charAt(0) != '$')
                    return;

                // $GPGGA,071536.00,3028.62044,N,11423.98293,E,1,04,2.21,63.2,M,-10.6,M,,*43
                if (nmea.contains("$GPGGA") || nmea.contains("$GNGGA")) {
//            if (nmea.startsWith("$GPGGA")) {
                    GPGGAInfo info = new GPGGAInfo();

                    info.GetGPGGA(nmea);

                    Log.e("ZORO", nmea);

                    Location location = info.getLocation();

                    setLastLocation(location);
                }

                /**
                 // $GPGST, 071536.00,48,,,,40,25,51*74
                 if (nmea.startsWith("$GPGST") && info != null) {
                 info.GetGPGST(nmea);

                 Log.e("ZORO", nmea);

                 Location location = info.getLocation(info);

                 setLastLocation(location);
                 }
                 **/
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };


    LocationClient mLocationClient = null;
    MyLocationListenner myListener = new MyLocationListenner();

    public void startBD() {
        mLocationClient = new LocationClient(context);

        mLocationClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();

        option.setOpenGps(true); // 打开gps
        option.setCoorType("gcj02"); // 设置坐标类型,国测局02坐标系,即火星坐标系
        option.setServiceName("com.baidu.location.service_v2.9");

        option.setScanSpan(10 * 1000);// 设置定位模式，小于1秒则一次定位;大于等于1秒则定时定位
        option.setPriority(LocationClientOption.GpsFirst); // 不设置，默认是gps优先

        option.setPoiExtraInfo(false);// 是否需要POI信息
        option.setPoiNumber(10);

        option.disableCache(true);

        mLocationClient.setLocOption(option);

        mLocationClient.start();
    }

    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            try {
                if (location == null) {
                    return;
                }

                long tick = new Date().getTime();

                if (lastLocation != null && (tick - lastLocation.getTime()) <= 15 * 1000) {
                    return;
                }

                Location gpsLocation = new Location("");
                gpsLocation.setTime(tick);

                gpsLocation.setProvider(location.getLocType() == BDLocation.TypeGpsLocation ? "BD-GPS" : "BD-NET");

                gpsLocation.setAccuracy(location.getRadius());
                gpsLocation.setAltitude(location.getAltitude());

                // 是否从火星坐标转为地球坐标，放置使用国测局标准的地图时出现偏差，如ＧＯＯＧＬＥ地图
                if (MyApplication.getInstance().getConfigValue("Mar2Earth").equals("false")) {
                    gpsLocation.setLatitude(location.getLatitude());
                    gpsLocation.setLongitude(location.getLongitude());
                } else {
                    MmtLocation gcLoc = new MmtLocation();

                    gcLoc.lat = location.getLatitude();
                    gcLoc.lng = location.getLongitude();

                    MmtLocation myLocation = ChinaMapShift.transformFromGCJToWGS(gcLoc);

                    gpsLocation.setLatitude(myLocation.lat);
                    gpsLocation.setLongitude(myLocation.lng);
                }

                gpsLocation.setSpeed(location.getSpeed());

                if (lastLocation != null && (tick - lastLocation.getTime()) <= 15 * 1000) {
                    return;
                }

                setLastLocation(gpsLocation);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}