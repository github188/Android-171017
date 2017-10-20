package com.repair.common;

import android.text.TextUtils;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public abstract class SortByTimeAndDistanceOper {

    /**
     * 间隔时间，毫秒,当前时间-目标时间，所以正值代表超期了，负值代表还未超期
     */
    public long intervalTime;

    /**
     * 间隔距离，米
     */
    public double intervalDistance;

    /**
     * 间隔时间，转换为天，时，分
     */
    public String intervalTimeStr;

    /**
     * 间隔距离，转换为千米，百米，米
     */
    public String intervalDistanceStr;

    public abstract String getSortTimeKey();

    public abstract String getSortDistanceKey();

    /**
     * 计算当前时间与目标时间的时差，并设置当当前工单信息中
     */
    public void calculateBetTime(String... keys) {
        String key = "";

        try {
            if (keys != null && keys.length > 0)
                key = keys[0];
            else
                key = getSortTimeKey();

            long betTime = 0;

            intervalTimeStr = "未含有" + key + "信息";
            intervalTime = betTime;

            if (!BaseClassUtil.isNullOrEmptyString(key)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                long currentTime = System.currentTimeMillis();

                Calendar calendar = Calendar.getInstance();

                calendar.setTime(format.parse(key));

                long time = calendar.getTimeInMillis();

                betTime = currentTime - time;

                intervalTimeStr = getBetTime(betTime);
                intervalTime = betTime;
            }

        } catch (ParseException e) {
            e.printStackTrace();

            intervalTimeStr = "未含有正确格式的" + key + "信息";
            intervalTime = 0;
        }
    }

    /**
     * 计算当前坐标与目标位置的距离，并设置当当前工单信息中
     */
    public void calculateDistance(GpsXYZ xyz) {
        intervalDistance = Double.MAX_VALUE;
        intervalDistanceStr = "";

        if (TextUtils.isEmpty(getSortDistanceKey()))
            return;

        if (!xyz.isUsefull()) {//保证排序时，没有获取到GPS情况下，有坐标的比没有坐标的显示靠前
            intervalDistance = Integer.MAX_VALUE;

            return;
        }

        String pos = getSortDistanceKey();

        double x = Double.valueOf(pos.split(",")[0]);
        double y = Double.valueOf(pos.split(",")[1]);

        double gpsX = xyz.getX();
        double gpsY = xyz.getY();

        intervalDistance = Math.sqrt((gpsX - x) * (gpsX - x) + (gpsY - y) * (gpsY - y)) / 1000;

        if (intervalDistance >= 10) {
            intervalDistanceStr = "/" + (int) intervalDistance + "公里";
        } else {
            intervalDistanceStr = "/" + new DecimalFormat("#0.0").format(intervalDistance) + "公里";
        }
    }

    /**
     * 根据时间差转换为字符串显示
     */
    private String getBetTime(long betTime) {
        if (betTime == 0)
            return "即将超期";

        String pre = betTime > 0 ? "超期" : "剩余";

        double day = 24 * 60.0;
        double remain = Math.ceil(Math.abs(betTime) / (60 * 1000.0));//剩余分钟数

        if (remain >= 10 * day) {//10天及以上,只显示天数，如 剩余32天
            return pre + (int) Math.ceil(remain / day) + "天";
        } else if (remain > day) {//1天以上10天以下，显示天数和小时数，如 超出3天5小时
            return pre + (int) (remain / day) + "天" + (remain % day != 0 ? ((int) Math.ceil((remain % day) / 60.0) + "小时") : "");
        } else if (remain > 60) {//1到24小时以内的，显示小时数和分钟数，如 剩余8小时22分钟
            return pre + (int) (remain / 60) + "小时" + (remain % 60 != 0 ? ((int) Math.ceil(remain % 60) + "分钟") : "");
        } else {//一小时内的，只显示分钟数
            return pre + (int) remain + "分钟";
        }
    }
}
