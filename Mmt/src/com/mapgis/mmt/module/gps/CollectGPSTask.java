package com.mapgis.mmt.module.gps;

import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CollectGPSTask implements Runnable {
    private GpsXYZ lastXY;
    private long gpsDistance = -1;
    private long gpsMaxAccuracy = 20;

    @Override
    public void run() {
        try {
            Thread.currentThread().setName(this.toString());

            GpsXYZ xy = GpsReceiver.getInstance().getLastLocalLocation();

            // 过滤无效坐标点,放松标准，只根据地图范围和经纬度在中国范围内过滤，精度的过滤放开，改为在服务端过滤网络定位坐标
            if (!xy.isUsefull())
                return;

            if (xy.getLocation() != null) {
                //过滤网络定位的点，不作为轨迹点
                if (",BD-NET,HC-未知,NC-未知,".contains("," + xy.getLocation().getProvider() + ","))
                    return;

                //轨迹过滤精度大于20米的点
                if (xy.getLocation().getAccuracy() > gpsMaxAccuracy)
                    return;
            }

            double speed = 0;

            if (lastXY != null && !xy.isRandom()) {
                Date nowDate = BaseClassUtil.parseTime(xy.getReportTime());
                Date lastDate = BaseClassUtil.parseTime(lastXY.getReportTime());

                if (nowDate != null && lastDate != null) {
                    double span = (nowDate.getTime() - lastDate.getTime()) / 1000.0;
                    double distance = GisUtil.calcDistance(xy.convertToPoint(), lastXY.convertToPoint());

                    if (span == 0 || distance == 0)//位置没有更新
                        return;

                    speed = distance / span;//单位M/S，正常人步行速度约为1.5M/S，最大约束为33.3M/S，即120KM/H

                    if (speed >= 34)
                        return;

                    if (gpsDistance > 0 && span < 60 && distance < gpsDistance)//最小间隔30米,最大时长60秒
                        return;
                }
            }

            xy.readDeviceInfo();

            if (TextUtils.isEmpty(xy.speed) || Double.valueOf(xy.speed) <= 0)
                xy.speed = String.valueOf(speed);

            //为了配合今日轨迹功能，这里保存一天的坐标，在初始化时删除当天前的所有数据
            DatabaseHelper.getInstance().insert(xy);

            lastXY = xy;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            GPSTipUtils.gpsTip(GpsReceiver.getInstance());
        }
    }

    public void start(ScheduledExecutorService executorService) {
        try {
            gpsMaxAccuracy = MyApplication.getInstance().getConfigValue("gpsMaxAccuracy", 20);

            gpsDistance = MyApplication.getInstance().getConfigValue("GPSDistance", -1);

            //轨迹记录时间间隔，单位毫秒，默认10秒
            long interval = MyApplication.getInstance().getConfigValue("gpsReportInterval", 10) * 1000;

            if (gpsDistance > 0) {
                executorService.scheduleWithFixedDelay(this, 2 * 1000, 2 * 1000, TimeUnit.MILLISECONDS);
            } else {
                //5秒后开始执行任务，以后每隔5秒执行一次
                executorService.scheduleAtFixedRate(this, interval, interval, TimeUnit.MILLISECONDS);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
    }
}