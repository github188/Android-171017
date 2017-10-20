package com.mapgis.mmt.module.gps.Receiver;

import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.ChinaMapShift;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.gps.trans.MmtLocation;

import java.util.Date;

/**
 * 中海达广播方式定位
 */
public class ZHDGpsReceiver extends GpsReceiver {
    Context context;

    @Override
    public String start(CoordinateConvertor coordinateConvertor) {
        try {
            this.coordinateConvertor = coordinateConvertor;

            context = MyApplication.getInstance();

            context.registerReceiver(gpsPointReceiver, new IntentFilter("com.zhd.gis.broadcast.gpspoint"));

            startBD();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public void stop() {
        try {
            context.unregisterReceiver(gpsPointReceiver);

            /** 解绑百度定位 **/
            {
                mLocationClient.unRegisterLocationListener(myListener);
                mLocationClient.stop();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    GpsPointReceiver gpsPointReceiver = new GpsPointReceiver(new GpsPointReceiver.MyLocationListener() {
        @Override
        public void onLocationUpdated(Bundle bundle) {
            try {
                int stateSloution = bundle.getInt("solve_type");
                String provider;

                if (stateSloution < 1)
                    provider = "未知";
                else if (stateSloution == 1)
                    provider = "单点";
                else
                    provider = "差分";

                Location gpsLocation = new Location(provider);

                float xrms = bundle.getFloat("xrms"), yrms = bundle.getFloat("yrms");

                float hrms = (float) Math.sqrt(Math.pow(xrms, 2) + Math.pow(yrms, 2));

                gpsLocation.setAccuracy(hrms);
                gpsLocation.setAltitude(bundle.getFloat("altitude"));

                gpsLocation.setLatitude(bundle.getDouble("latitude"));
                gpsLocation.setLongitude(bundle.getDouble("longitude"));

                gpsLocation.setSpeed(bundle.getFloat("speed"));
                gpsLocation.setTime(new Date().getTime());

                setLastLocation(gpsLocation);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    });

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