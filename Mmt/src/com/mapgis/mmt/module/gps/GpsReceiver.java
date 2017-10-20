package com.mapgis.mmt.module.gps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.DeviceUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.module.gps.Receiver.BDGpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.BTGpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.BroadcastGpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.GDGpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.HCGpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.MmtLocationListener;
import com.mapgis.mmt.module.gps.Receiver.NCGpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.NativeGpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.NmeaGpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.RandomGpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.ZHDGpsReceiver;
import com.mapgis.mmt.module.gps.entity.GPSTraceInfo;
import com.mapgis.mmt.module.gps.trans.CCoorTransFull;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.simplecache.ACache;
import com.zondy.mapgis.geometry.Dot;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//GPS经纬度坐标接收类
public abstract class GpsReceiver extends BroadcastReceiver {
    private static GpsReceiver instance = null;
    private ACache aCache;
    private final static Object syncRoot = new Object();

    public static GpsReceiver getInstance() {
        if (instance == null) {
            synchronized (syncRoot) {
                if (instance == null) {
                    String name = MyApplication.getInstance().getConfigValue("GpsReceiver");

                    switch (name) {
                        case "Native":
                            instance = new NativeGpsReceiver();
                            break;
                        case "HC":
                            boolean isAvailable = DeviceUtil.isAvilible(MyApplication.getInstance(), "com.huace.gnssserver");

                            if (isAvailable)
                                instance = new HCGpsReceiver();
                            else
                                instance = new BDGpsReceiver();
                            break;
                        case "Random":
                        case "RD":
                            instance = new RandomGpsReceiver();
                            break;
                        case "NC":
                            instance = new NCGpsReceiver();
                            break;
                        case "BT":
                            instance = new BTGpsReceiver();
                            break;
                        case "GD":
                            instance = new GDGpsReceiver();
                            break;
                        case "NMEA":
                        case "HZ":
                            instance = new NmeaGpsReceiver();
                            break;
                        case "ZHD":
                            instance = new ZHDGpsReceiver();
                            break;
                        case "BC":
                            instance = new BroadcastGpsReceiver();
                            break;
                        default:
                            instance = new BDGpsReceiver();
                            break;
                    }
                }
            }
        }

        return instance;
    }

    protected Location lastLocation;
    protected GpsXYZ lastXY;

    public void setLastLocation(Location location) {
        // 中国 经度范围：东经 73度33分——135度05分 纬度范围：北纬 3度51分——53度33分
        if (!(location.getLongitude() > 73 && location.getLongitude() < 136 && location.getLatitude() > 3 && location.getLatitude() < 54)) {
            return;
        }
        GpsXYZ xy = getLastLocation(location);

        GPSTraceInfo info = new GPSTraceInfo(xy);

        DatabaseHelper.getInstance().insert(info);

        setLastXY(xy);

        lastLocation = location;
    }

    public void setLastXY(GpsXYZ xy) {
        if (!xy.isUsefull())
            return;

        lastXY = xy;

        broadcast(xy);
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public GpsXYZ getLastLocalLocation() {
        GpsXYZ xyz = getGpsXYZ();

        try {
            double x = Convert.FormatDouble(xyz.getX());
            double y = Convert.FormatDouble(xyz.getY());
            double z = Convert.FormatDouble(xyz.getZ());

            xyz.setX(x);
            xyz.setY(y);
            xyz.setZ(z);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return xyz;
    }

    private GpsXYZ getGpsXYZ() {
        try {
            String random = MyApplication.getInstance().getConfigValue("RandomGPS");

            if (!TextUtils.isEmpty(random)) {
                if (random.equals("view")) {//取中心点作为随机坐标
                    if (lastXY != null)
                        return lastXY;

                    Location location = new Location("Random");

                    location.setTime(new Date().getTime());

                    Dot dot = MyApplication.getInstance().mapGISFrame.getMapView().getCenterPoint();

                    GpsXYZ xy = new GpsXYZ(dot.x, dot.y);

                    xy.setLocation(location);

                    setLastXY(xy);

                    return xy;
                } else if (random.contains(",")) {//逗号分隔指定XY坐标
                    String[] rangeStrings = random.split(",");
                    //  改为>=2
                    if (rangeStrings.length >= 2) {
                        return new GpsXYZ(Double.valueOf(rangeStrings[0]), Double.valueOf(rangeStrings[1]));
                    }
                } else if (random.contains("-")) {//破折号分隔指定经纬度坐标
                    String[] rangeStrings = random.split("-");

                    Location location = new Location("RandomGPS");

                    location.setLongitude(Double.valueOf(rangeStrings[0]));
                    location.setLatitude(Double.valueOf(rangeStrings[1]));

                    if (rangeStrings.length > 2)
                        location.setAltitude(Double.valueOf(rangeStrings[2]));

                    return getLastLocation(location);
                }
            } else if (lastXY != null) {
                return lastXY;
            } else if (lastLocation != null) {
                return getLastLocation(lastLocation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new GpsXYZ(0, 0);
    }

    public GpsXYZ getLastLocation(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        double altitude = location.getAltitude();

        // 中国 经度范围：东经 73度33分——135度05分 纬度范围：北纬 3度51分——53度33分
        if (longitude > 73 && longitude < 136 && latitude > 3 && latitude < 54 && coordinateConvertor != null) {
            GpsXYZ xyz = new GpsXYZ(longitude, latitude, altitude);

            if (coordinateConvertor.Convert(xyz)) {
                xyz.setX(Convert.FormatDouble(xyz.getX()));
                xyz.setY(Convert.FormatDouble(xyz.getY()));
                //减去设备自身的高度
                double z = Convert.FormatDouble(xyz.getZ());
                if (aCache == null) {
                    aCache = BaseClassUtil.getConfigACache();
                }
                String deviceHStr = aCache.getAsString("deviceH");
                if (!TextUtils.isEmpty(deviceHStr) && BaseClassUtil.isNum(deviceHStr)) {
                    z = z - Double.valueOf(deviceHStr);
                }
                xyz.setZ(z);

                xyz.setLocation(location);

                return xyz;
            }
        }

        return new GpsXYZ(0, 0, 0);
    }

    public Location getLastLocationConverse(GpsXYZ xyz) {
        if (coordinateConvertor.gpsTransFull != null) {
            coordinateConvertor.gpsTransFull.CoorTransConverse(xyz.getX(), xyz.getY(), xyz);
        } else {
            CCoorTransFull.LatLonToMetersConverse(xyz.getX(), xyz.getY(), xyz);
        }

        double longitude = xyz.getX(), latitude = xyz.getY();

        if (longitude > 73 && longitude < 136 && latitude > 3 && latitude < 54) {
            Location location = new Location("");

            location.setLongitude(longitude);
            location.setLatitude(latitude);

            return location;
        } else
            return null;
    }

    protected CoordinateConvertor coordinateConvertor;

    public abstract String start(CoordinateConvertor coordinateConvertor);

    public abstract void stop();

    private List<MmtLocationListener> observers = new CopyOnWriteArrayList<>();

    public CoordinateConvertor getCoordinateConvertor() {
        return coordinateConvertor;
    }

    public synchronized void addObserver(MmtLocationListener listener) {
        this.observers.add(listener);
    }

    public synchronized void removeObserver(MmtLocationListener listener) {
        this.observers.remove(listener);
    }

    private synchronized void broadcast(final GpsXYZ xy) {
        Intent intent = new Intent(MmtMainService.class.getName());

        intent.putExtra("xy", xy);

        MyApplication.getInstance().sendBroadcast(intent);

        if (observers == null || observers.size() == 0)
            return;

        for (final MmtLocationListener listener : observers) {
            MyApplication.getInstance().submitExecutorService(new Runnable() {
                @Override
                public void run() {
                    listener.onLocationChanged(xy);
                }
            });
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}