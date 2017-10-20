package com.mapgis.mmt.module.gps.Receiver;

import android.location.Location;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.ChinaMapShift;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.gps.trans.MmtLocation;

import java.util.Date;

/**
 * 高德GPS+基站+WIFI综合定位
 */
public class GDGpsReceiver extends GpsReceiver implements AMapLocationListener {
    private AMapLocationClient client;

    @Override
    public String start(CoordinateConvertor coordinateConvertor) {
        this.coordinateConvertor = coordinateConvertor;

        client = new AMapLocationClient(MyApplication.getInstance());

        //初始化定位参数
        AMapLocationClientOption option = new AMapLocationClientOption();

        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        //设置是否返回地址信息（默认返回地址信息）
        option.setNeedAddress(false);

        //设置是否只定位一次,默认为false
        option.setOnceLocation(false);

        //设置是否强制刷新WIFI，默认为强制刷新
        option.setWifiActiveScan(true);

        //设置是否允许模拟位置,默认为false，不允许模拟位置
        option.setMockEnable(true);

        //设置定位间隔,单位毫秒,默认为2000ms
        option.setInterval(1000);

        //设置是否优先返回GPS定位信息,默认值：false,只有在高精度定位模式下的单次定位有效
        option.setGpsFirst(true);

        //给定位客户端对象设置定位参数
        client.setLocationOption(option);

        // 设置定位监听
        client.setLocationListener(this);

        //启动定位
        client.startLocation();

        return null;
    }

    @Override
    public void stop() {
        if (null != client) {
            client.unRegisterLocationListener(this);
            client.stopLocation();

            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            client.onDestroy();
            client = null;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        try {
            if (location == null) {
                return;
            }

            Location gpsLocation = new Location("");
            gpsLocation.setTime(new Date().getTime());

            gpsLocation.setProvider(location.getLocationType() == AMapLocation.LOCATION_TYPE_GPS ? "GD-GPS"
                    : "GD-NET");

            gpsLocation.setAccuracy(location.getAccuracy());
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

            //获取当前速度 单位：米/秒 仅在AMapLocation.getProvider()是gps时有效
            gpsLocation.setSpeed((float) (location.getSpeed() * 3.6));//转换为 公里/小时

            setLastLocation(gpsLocation);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
