package com.mapgis.mmt.module.gps.Receiver;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.ChinaMapShift;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.gps.trans.MmtLocation;
import com.southgnss.location.SouthGnssManager;

import java.util.Date;

/**
 * 南方测绘极光X2设备定位
 */
public class NCGpsReceiver extends GpsReceiver {
    Context context;
    //GNSS管理类
    private SouthGnssManager mGPSManager;

    private void startNC() {
        //初始化位置服务
        mGPSManager = SouthGnssManager.getInstence(context);
        mGPSManager.initialize();

        //注册位置监听
        mGPSManager.addListener(mLocationListener);
    }

    String provider = "NC-未知";

    LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location arg0) {
            if (arg0 == null)
                return;

            Bundle bundle = arg0.getExtras();

            if (bundle != null && bundle.getInt(SouthGnssManager.SOUTHGNSSSolutionState, -1) >= 0)
                onStatusChanged("gps", SouthGnssManager.SOUTHGNSS_EVENT_FIX, bundle);
            else
                provider = "NC-未知";

            arg0.setProvider(provider);

            arg0.setSpeed((float) (arg0.getSpeed() * 3.6));//转换为 公里/小时

            setLastLocation(arg0);
        }

        @Override
        public void onProviderDisabled(String arg0) {
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }

        @Override
        public void onStatusChanged(String arg0, int event, Bundle extras) {
            //android 本身GPS不会
            if (event == SouthGnssManager.SOUTHGNSS_EVENT_FIX) {
                int iSatInView = extras.getInt(SouthGnssManager.SOUTHGNSSSatInView, 0);
                int iSatInLock = extras.getInt(SouthGnssManager.SOUTHGNSSSatInLock, 0);
                int iGpsInLock = extras.getInt(SouthGnssManager.SOUTHGNSSGpsInLock, 0);
                int iBdInLock = extras.getInt(SouthGnssManager.SOUTHGNSSBdInLock, 0);
                int iGlonassInLock = extras.getInt(SouthGnssManager.SOUTHGNSSGlonassInLock, 0);
                int iGaileoInLock = extras.getInt(SouthGnssManager.SOUTHGNSSGaileoInLock, 0);

                int nSolutionStateType = extras.getInt(SouthGnssManager.SOUTHGNSSSolutionState, 0);

                String state = String.format("%d|G%d+C%d+R%d+J%d/%d -- %s", iSatInLock, iGpsInLock, iBdInLock, iGlonassInLock,
                        iGaileoInLock, iSatInView, GetnSolutionState(nSolutionStateType));

                Log.i("NCGpsReceiver", state);
            }
        }
    };

    private String GetnSolutionState(int nType) {
        String strSolutionState;

        switch (nType) {
            case 0:
                strSolutionState = "无效解";
                break;
            case 1:
                strSolutionState = "单点解";
                break;
            case 2:
                strSolutionState = "差分解";
                break;
            case 4:
                strSolutionState = "固定解";
                break;
            case 5:
                strSolutionState = "浮点解";
                break;
            case 7:
                strSolutionState = "基站";
                break;

            default:
                strSolutionState = "" + nType;
                break;
        }

        if (nType < 1)
            provider = "NC-未知";
        else if (nType == 1)
            provider = "NC-单点";
        else
            provider = "NC-差分";

        return strSolutionState;
    }

    @Override
    public String start(CoordinateConvertor coordinateConvertor) {
        try {
            this.coordinateConvertor = coordinateConvertor;

            context = MyApplication.getInstance();

            startNC();

            startBD();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public void stop() {
        try {
            //移除位置监听
            mGPSManager.removeListener(mLocationListener);

            /** 解绑百度定位 **/
            {
                mLocationClient.unRegisterLocationListener(myListener);
                mLocationClient.stop();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
