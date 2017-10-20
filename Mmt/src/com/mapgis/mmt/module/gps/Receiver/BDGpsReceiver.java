package com.mapgis.mmt.module.gps.Receiver;

import android.location.Location;

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
 * 百度GPS+基站+WIFI综合定位
 */
public class BDGpsReceiver extends GpsReceiver {
    LocationClient mLocationClient = null;
    MyLocationListenner myListener = new MyLocationListenner();

    @Override
    public String start(CoordinateConvertor coordinateConvertor) {
        this.coordinateConvertor = coordinateConvertor;

        mLocationClient = new LocationClient(MyApplication.getInstance());

        mLocationClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();

        option.setOpenGps(true); // 打开gps
        option.setCoorType("gcj02"); // 设置坐标类型,国测局02坐标系,即火星坐标系
        option.setServiceName("com.baidu.location.service_v2.9");

        option.setScanSpan(2000);// 设置定位模式，小于1秒则一次定位;大于等于1秒则定时定位
        option.setPriority(LocationClientOption.GpsFirst); // 不设置，默认是gps优先

        // option.setAddrType("all"); 设置是否需要地址信息

        option.setPoiExtraInfo(false);// 是否需要POI信息
        option.setPoiNumber(10);

        option.disableCache(true);

        mLocationClient.setLocOption(option);

        mLocationClient.start();

        return "";
    }

    public void stop() {
        try {
            if (mLocationClient != null){
                mLocationClient.unRegisterLocationListener(myListener);
                mLocationClient.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            try {
                if (location == null) {
                    return;
                }

                Location gpsLocation = new Location("");
                gpsLocation.setTime(new Date().getTime());

                gpsLocation.setProvider(location.getLocType() == BDLocation.TypeGpsLocation ? "BD-GPS"
                        : "BD-NET");

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

                //获取速度，仅gps定位结果时有速度信息，单位公里/小时，默认值0.0f
                gpsLocation.setSpeed(location.getSpeed());

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
