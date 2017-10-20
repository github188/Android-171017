package com.maintainproduct.module.maintenance.list;

import com.maintainproduct.v2.caselist.GDItem;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;

public class MaintenanceListUtil {
    /**
     * 当前GPS位置距离目标点的距离
     */
    public static final String Distance = "Distance";
    /**
     * 当前GPS位置距离目标点的距离,转换计量单位显示
     */
    public static final String DistanceStr = "DistanceStr";
    /**
     * 当前时间距离目标时间的剩余时间
     */
    public static final String BetTime = "BetTime";
    /**
     * 当前时间距离目标时间的剩余时间 ,转换计量单位显示
     */
    public static final String BetTimeStr = "BetTimeStr";

    //目前剩余时间是按照承办时间来表示的
    //如果指定了完成时间，那么应该已指定的完成时间为准
    private final static String reqiureTime = "承办时间";

    private final static String finishTime = "完成时间";

    /**
     * 计算当前坐标与目标位置的距离，并设置当当前工单信息中
     */
    public static void putDistance(LinkedHashMap<String, String> hashMap, GpsXYZ xyz) {
        double distance = 0;

        hashMap.put(Distance, distance + "");
        hashMap.put(DistanceStr, "未含有坐标信息");
        try {
            if (hashMap.containsKey("坐标")) {
                String pos1 = hashMap.get("坐标");
                double x = Double.valueOf(pos1.split(",")[0]);
                double y = Double.valueOf(pos1.split(",")[1]);

                double gpsX = xyz.getX();
                double gpsY = xyz.getY();

                distance = Math.sqrt((gpsX - x) * (gpsX - x) + (gpsY - y) * (gpsY - y));
                hashMap.put(Distance, distance + "");
                hashMap.put(DistanceStr, getDistance(distance));
            }
        } catch (Exception ex) {
            hashMap.put(Distance, distance + "");
            hashMap.put(DistanceStr, "未含有坐标信息");
        }
    }

    /**
     * 计算当前时间与目标时间的时差，并设置当当前工单信息中
     * 如果指定了目标时间那么以目标时间为准
     */
    public static void putBetTime(LinkedHashMap<String, String> hashMap) {
        try {

            long betTime = 0;

            hashMap.put(BetTimeStr, "未含有" + reqiureTime + "信息");
            hashMap.put(BetTime, betTime + "");
            String basetime = "";
            String sortTime=hashMap.get(reqiureTime).replace("T", " ");
            if (hashMap.containsKey(finishTime)) {
                basetime = hashMap.get(finishTime).replace("T", " ");

            } else if (hashMap.containsKey(reqiureTime)) {
                basetime = sortTime;
            }
            if (!BaseClassUtil.isNullOrEmptyString(basetime)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                long currentTime = System.currentTimeMillis();

                Calendar calendar = Calendar.getInstance();

                calendar.setTime(format.parse(basetime));

                long time = calendar.getTimeInMillis();

                betTime = currentTime - time;
                //BetTimeStr用于显示超时多少，剩余多少时间
                hashMap.put(BetTimeStr, getBetTime(betTime));

               //BetTime用于排序，目前只按承办时间排序
               // Calendar calendarSortTime = Calendar.getInstance();

                calendar.setTime(format.parse(sortTime));

                long timeForSort = calendar.getTimeInMillis();
                long sortTimes= currentTime - timeForSort;
                hashMap.put(BetTime, sortTimes + "");
            }

        } catch (ParseException e) {
            e.printStackTrace();

            hashMap.put(BetTime, "0");
            hashMap.put(BetTimeStr, "未含有正确格式的" + reqiureTime + "信息");
        }
    }

    /**
     * 计算当前时间与目标时间的时差，并设置当当前工单信息中
     */
    public static void calculateBetTime(GDItem item) {
        try {

            long betTime = 0;

            if (!BaseClassUtil.isNullOrEmptyString(item.Receivetime)) {

                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                long currentTime = System.currentTimeMillis();

                Calendar calendar = Calendar.getInstance();

                calendar.setTime(format.parse(item.Receivetime));

                long time = calendar.getTimeInMillis();

                betTime = currentTime - time;

                item.BetTime = betTime + "";
            } else {
                item.BetTime = "未含有 Receivetime信息";
            }

        } catch (ParseException e) {
            e.printStackTrace();

            item.BetTime = "未含有 Receivetime信息格式不正确";
        }
    }

    /**
     * 计算当前坐标与目标位置的距离，并设置当当前工单信息中
     */
    public static void calculateDistance(GDItem item, GpsXYZ xyz) {
        double distance = 0;

        if (!BaseClassUtil.isNullOrEmptyString(item.Position) && xyz != null) {
            String pos1 = item.Position;
            double x = Double.valueOf(pos1.split(",")[0]);
            double y = Double.valueOf(pos1.split(",")[1]);

            double gpsX = xyz.getX();
            double gpsY = xyz.getY();

            distance = Math.sqrt((gpsX - x) * (gpsX - x) + (gpsY - y) * (gpsY - y));
            item.Distance = distance + "";
        } else {
            item.Distance = "未含有坐标信息";
        }
    }

    /**
     * 根据距离转换为字符串显示
     */
    public static String getDistance(double distance) {
        if (distance > 1000) {
            return (Convert.FormatDouble((distance / 1000))) + "千米";
        } else if (distance > 100) {
            return (Convert.FormatDouble((distance / 100))) + "百米";
        } else {
            return (Convert.FormatDouble(distance)) + "米";
        }
    }

    /**
     * 根据时间差转换为字符串显示
     */
    public static String getBetTime(long betTime) {
        String str = betTime >= 0 ? "超出" : "剩余";
        betTime = Math.abs(betTime);
        if (betTime > (24 * 60 * 60 * 1000)) {
            return str + ((betTime / (24 * 60 * 60 * 1000))) + "天";
        } else if (betTime > (60 * 60 * 1000)) {
            return str + ((betTime / (60 * 60 * 1000))) + "小时";
        } else {
            return str + ((betTime / (60 * 1000))) + "分钟";
        }
    }
}
