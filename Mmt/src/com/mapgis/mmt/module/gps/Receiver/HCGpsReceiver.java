package com.mapgis.mmt.module.gps.Receiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.huace.gnssserver.IGnssListener;
import com.huace.gnssserver.IGnssServiceBinder;
import com.huace.gnssserver.gnss.data.CorsConnectionArgs;
import com.huace.gnssserver.gnss.data.DeviceConnectionArgs;
import com.huace.gnssserver.gnss.data.GnssCommand;
import com.huace.gnssserver.gnss.data.GnssInfo;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.ChinaMapShift;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.gps.trans.MmtLocation;
import com.mapgis.mmt.module.login.UserBean;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 华测LT-40设备定位
 */
public class HCGpsReceiver extends GpsReceiver {
    private static final String TAG = "HC";

    private static final String BROADCAST_INTENT = "com.huace.gnssserver.COMMAND";

    IGnssServiceBinder mService;
    private boolean hasCors = false;

    private void startHC() {
        /** 绑定服务 **/
        {
            printf("send intent to startHC");
            Intent intent = new Intent("com.huace.gnssserver.GnssService");

            intent.setPackage("com.huace.gnssserver");

            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        String cors = MyApplication.getInstance().getConfigValue("CORS");

        if (TextUtils.isEmpty(cors))
            return;

        hasCors = true;

        /** 本地连接 **/
        {
            connectLT40();
        }

        /** 登录Cors **/
        {
            connectCors();
        }
    }

    private void connectLT40() {
        // 发送命令之前，服务必须事先绑定，否则命令不会响应
        Intent intent = new Intent(BROADCAST_INTENT);

        /**
         * setAction 命令类型 0：断开  1：连接
         * setConnectionType 连接类型 0：本地 1：wifi 2：蓝牙
         * setProductName 设备名称 可以填“AUTO”,与配置文件中ProductionName字段一致
         */
        DeviceConnectionArgs args = new DeviceConnectionArgs().setAction(1).setConnectionType(0).setProductName("LT40");

        intent.putExtra(GnssCommand.GNSS_COMMAND, GnssCommand.CONNECT).putExtra(GnssCommand.CONNECT, args);

        context.sendBroadcast(intent);
    }

    private void connectCors() {
        Intent intent = new Intent(BROADCAST_INTENT);

        String[] corsArgs;

        try {
            corsArgs = MyApplication.getInstance().getConfigValue("CORS").split(",");

            if (corsArgs.length != 5) {
                throw new Exception("参数长度不符合");
            }
        } catch (Exception e) {
            corsArgs = new String[]{"121.37.17.3", "50009", "RTD", "csxa01", "123"};
        }

        String name = corsArgs[3];

        if (name.equalsIgnoreCase("-"))
            name = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).LoginName;

        /**
         * setAction 命令类型 0：断开 1：连接
         * setType 差分数据源链接类型 0:CORS 1:APIS 2:TCP
         * setIp IP地址
         * setPort 端口
         * setMountName 源名称 根据链接服务器设置类型选填
         * setDiffType 差分类型 根据链接服务器设置类型选填
         * setUserName 登陆用户名 根据链接服务器设置类型选填
         * setPassword 登陆密码 根据链接服务器设置类型选填
         * setStationId 基站ID 根据链接服务器设置类型选填
         */
        CorsConnectionArgs args = new CorsConnectionArgs().setAction(1).setType(0)
                .setIp(corsArgs[0]).setPort(Integer.valueOf(corsArgs[1]))
                .setMountName(corsArgs[2]).setDiffType("diffType").setUserName(name)
                .setPassword(corsArgs[4]).setStationId("stationId");

        intent.putExtra(GnssCommand.GNSS_COMMAND, GnssCommand.CORS).putExtra(GnssCommand.CORS, args);

        context.sendBroadcast(intent);
    }

    Context context;

    @Override
    public String start(CoordinateConvertor coordinateConvertor) {
        try {
            this.coordinateConvertor = coordinateConvertor;

            context = MyApplication.getInstance();

            startHC();

            startBD();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public void stop() {
        try {
            if (hasCors) {
                /** 断开Cors **/
                {
                    Intent intent = new Intent(BROADCAST_INTENT);

                    /**
                     * setAction 命令类型 0：断开 1：连接
                     */
                    CorsConnectionArgs args = new CorsConnectionArgs().setAction(0);

                    intent.putExtra(GnssCommand.GNSS_COMMAND, GnssCommand.CORS).putExtra(GnssCommand.CORS, args);

                    context.sendBroadcast(intent);
                }

                /** 断开与接收机的连接 **/
                {
                    Intent intent = new Intent(BROADCAST_INTENT);

                    /**
                     * setAction 命令类型 0：断开 1：连接
                     */
                    DeviceConnectionArgs args = new DeviceConnectionArgs().setAction(0);

                    intent.putExtra(GnssCommand.GNSS_COMMAND, GnssCommand.CONNECT).putExtra(GnssCommand.CONNECT, args);

                    context.sendBroadcast(intent);
                }
            }

            /** 解绑服务 **/
            {
                printf("send intent to stop");
                try {
                    mService.removeUpdates(listener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                context.unbindService(mConnection);
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

    IGnssListener listener = new IGnssListener.Stub() {
        @Override
        public void onLocationChanged(Location location) {
        }

        /***
         *
         * @param provider
         *            目前有Unknow,Local，Wifi，Bluetooth
         * @param status
         *            状态 0：接收机和Cors都已断开 1:接收机连接，Cors未连接 2：接收机和Cors都已连接
         *            10:已经10秒没有收到数据
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            try {
                if (!hasCors)
                    return;

                switch (status) {
                    case 0:
                        connectLT40();

                        TimeUnit.SECONDS.sleep(10);
                        connectCors();
                        break;
                    case 1:
                        TimeUnit.SECONDS.sleep(10);
                        connectCors();
                        break;
                    case 2:
                        break;
                    case 10:
                        break;
                }

                printf("onStatusChanged: " + status);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /***
         * 主要获取数据的接口
         *
         * @param gnssinfo Gnss数据
         */
        @Override
        public void onGnssInfoChanged(GnssInfo gnssinfo) {
            try {
                if (gnssinfo == null || gnssinfo.staUseCount == 0) {
                    return;
                }

                Location gpsLocation = new Location("");

                gpsLocation.setTime(new Date().getTime());

                String provider;

                switch (gnssinfo.posType) {
                    case 0:
                        provider = "未知";
                        break;
                    case 4:
                        provider = "单点";
                        break;
                    case 10:
                        provider = "差分";
                        break;
                    default:
                        provider = String.valueOf(gnssinfo.posType);
                        break;
                }

                gpsLocation.setProvider("HC-" + provider);

                gpsLocation.setLatitude(gnssinfo.latitude);
                gpsLocation.setLongitude(gnssinfo.longitude);
                gpsLocation.setAltitude(gnssinfo.altitude);
                gpsLocation.setSpeed((float) gnssinfo.speed);
                gpsLocation.setAccuracy((float) gnssinfo.hRMS);

                setLastLocation(gpsLocation);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /***
         * 当连接上接收机时会回调
         *
         * @param provider
         *            目前有Unknow,Local，Wifi，Bluetooth
         * @throws RemoteException
         */
        @Override
        public void onProviderEnabled(String provider) throws RemoteException {
        }

        /***
         * 当断开接收机时会回调
         *
         * @param provider
         *            目前有Unknow,Local，Wifi，Bluetooth
         * @throws RemoteException
         */
        @Override
        public void onProviderDisabled(String provider) throws RemoteException {
        }
    };

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                printf("Service Connected");
                mService = IGnssServiceBinder.Stub.asInterface(service);

                mService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            printf("Service Disconnected");
            mService = null;
        }
    };

    /***
     * 打印log
     *
     * @param str 字符串
     */
    private void printf(String str) {
        Log.e(TAG, "##------ " + str + "------");
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
