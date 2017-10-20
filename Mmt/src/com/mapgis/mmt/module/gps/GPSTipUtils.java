package com.mapgis.mmt.module.gps;

import android.location.Location;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.simplecache.ACache;

/**
 * Created by lyunfan on 17/3/22.
 */

public class GPSTipUtils {


    private static boolean isRun = false;

    public static void gpsTip(GpsReceiver gpsReceiver) {
        if (isRun) {
            return;
        }
        isRun = true;

        if (gpsReceiver == null) {
            return;
        }
        Location location = gpsReceiver.getLastLocation();
        CoordinateConvertor coordinateConvertor = gpsReceiver.getCoordinateConvertor();

        try {

            String name = MyApplication.getInstance().getConfigValue("GpsReceiver");

            if ("Random".equals(name) || "RD".equals(name)) {
                return;
            }

            GPSTipUtils.gpsSignalTip(location, coordinateConvertor);

            //精度提醒
            GPSTipUtils.gpsAccracyTip(location);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            isRun = false;
        }
    }


    private static ACache mCache = BaseClassUtil.getConfigACache();

    //无信号提醒
    private static MediaPlayer gpsSignalMediaPlayer;
    private static long NSignalTimeTick = System.currentTimeMillis();

    //无信号情况：1.本身没接收到经纬度，2.七参数未获取到,3.范围不在国内,4.信号不合格
    public static void gpsSignalTip(Location location, CoordinateConvertor coordinateConvertor) {

        boolean isNTip = "0".equals(mCache.getAsString("GPSSignalTip"));
        if (isNTip) {
            return;
        }

        //120s 接收不到xy就提醒
        long curTimeTick = System.currentTimeMillis();
        long NgpsMaxAccuracy = MyApplication.getInstance().getConfigValue("NgpsTipSpan", 120);

        if ((curTimeTick - NSignalTimeTick) <= NgpsMaxAccuracy * 1000) {
            return;
        }
        NSignalTimeTick = curTimeTick;

        boolean isTip = false;
        //1
        if (location == null) {
            isTip = true;
        }
        //2
        if (!isTip) {
            if (coordinateConvertor == null) {
                isTip = true;
            }
        }
        //3
        if (!isTip) {
            if (!(location.getLongitude() > 73 && location.getLongitude() < 136 && location.getLatitude() > 3 && location.getLatitude() < 54)) {
                isTip = true;
            }

        }
        //4
        if (!isTip) {
            String provider = location.getProvider();
            if (TextUtils.isEmpty(provider) || provider.contains("未知") || provider.toUpperCase().contains("NET")) {
                isTip = true;
            }
        }
        if (!isTip) {
            return;
        }

        if (gpsSignalMediaPlayer == null) {
            gpsSignalMediaPlayer = MediaPlayer.create(MyApplication.getInstance().getApplicationContext(), R.raw.no_gps_signal_tip);
        }
        if (gpsSignalMediaPlayer.isPlaying()) {
            return;
        }
        gpsSignalMediaPlayer.start();
    }


    //精度不准提醒
    private static MediaPlayer accracyTipMediaPlayer;
    private static long gpsAccracyTipTick = System.currentTimeMillis();

    public static void gpsAccracyTip(Location location) {

        boolean isNTip = "0".equals(mCache.getAsString("GPSAccuracyTip"));
        if (isNTip) {
            return;
        }

        if (location == null) {
            return;
        }

        long curTimeTick = System.currentTimeMillis();
        long gpsAccracyTipSpan = MyApplication.getInstance().getConfigValue("gpsAccracyTipSpan", 60);

        if ((curTimeTick - gpsAccracyTipTick) <= gpsAccracyTipSpan * 1000) {
            return;
        }
        gpsAccracyTipTick = curTimeTick;

        float curAccuracy = location.getAccuracy();
        long gpsMaxAccuracy = MyApplication.getInstance().getConfigValue("gpsMaxAccuracy", 20);

        if (curAccuracy <= gpsMaxAccuracy) {
            return;
        }

        //这种情况已作为未获取到gps信号处理了，不能重复处理
        String provider = location.getProvider();
        if (TextUtils.isEmpty(provider) || provider.contains("未知") || provider.toUpperCase().contains("NET")) {
            return;
        }


        if (accracyTipMediaPlayer == null) {
            accracyTipMediaPlayer = MediaPlayer.create(MyApplication.getInstance().getApplicationContext(), R.raw.gps_tolerance_tip);
        }
        if (accracyTipMediaPlayer.isPlaying()) {
            return;
        }
        accracyTipMediaPlayer.start();
    }

    //到位提醒,前面有时间间隔判断
    private static MediaPlayer arrivedTipMediaPlayer;
    //private static long arrivedTipTick = System.currentTimeMillis();

    public static void arrivedTip() {
//        long curTimeTick = System.currentTimeMillis();
//        long arrivedTipSpan = MyApplication.getInstance().getConfigValue("arrivedTipSpan", 10);
//
//        if ((curTimeTick - arrivedTipTick) <= arrivedTipSpan * 1000) {
//            return;
//        }

        boolean isNTip = "0".equals(mCache.getAsString("arrivedTip"));
        if (isNTip) {
            return;
        }

        if (arrivedTipMediaPlayer == null) {
            arrivedTipMediaPlayer = MediaPlayer.create(MyApplication.getInstance().getApplicationContext(), R.raw.arrivedtip);
        }
        if (arrivedTipMediaPlayer.isPlaying()) {
            return;
        }
        arrivedTipMediaPlayer.start();
    }

    //新工单提醒
    private static MediaPlayer newTaskTipMediaPlayer;
    //private static long arrivedTipTick = System.currentTimeMillis();

    public static void newTip() {
//        long curTimeTick = System.currentTimeMillis();
//        long arrivedTipSpan = MyApplication.getInstance().getConfigValue("arrivedTipSpan", 10);
//
//        if ((curTimeTick - arrivedTipTick) <= arrivedTipSpan * 1000) {
//            return;
//        }

        boolean isNTip = "0".equals(mCache.getAsString("newTip"));
        if (isNTip) {
            return;
        }

        if (newTaskTipMediaPlayer == null) {
            long tipStyle = MyApplication.getInstance().getConfigValue("RepeatNetTipRing", 0);
            if (tipStyle == 1) {
                newTaskTipMediaPlayer = new MediaPlayer();
                try {

                    newTaskTipMediaPlayer.setDataSource(ServerConnectConfig.getInstance().getCityServerMobileBufFilePath() + "/Mobile/Assets/tipring.mp3");

                    newTaskTipMediaPlayer.prepare();
                } catch (Exception e) {
                    Log.d("tipring.mp3", e.getMessage());
                }
            }
            if (newTaskTipMediaPlayer == null) {
                newTaskTipMediaPlayer = MediaPlayer.create(MyApplication.getInstance().getApplicationContext(), R.raw.newtip);
            }
        }
        if (newTaskTipMediaPlayer.isPlaying()) {
            return;
        }
        newTaskTipMediaPlayer.start();
    }

    public static void releaseNewTip() {
        if (newTaskTipMediaPlayer == null) {
            return;
        }
        newTaskTipMediaPlayer.stop();
        newTaskTipMediaPlayer.release();
        newTaskTipMediaPlayer = null;
    }

}
