package com.patrolproduct.server;

import android.app.Notification;
import android.content.ContentValues;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.ConnectivityUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseThread;
import com.mapgis.mmt.global.MmtNotificationManager;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.entity.TracePoint;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PositionDetecterThread extends MmtBaseThread {
    private static final String TAG = "PositionDetecterThread";

    // 没有上传轨迹信息的提醒间隔15分钟
    private static final int TIME_NOT_UPLOAD_MULTIPOI = 900000;
    private static final int DEFAULT_SINGLE_TRACE_INTERVAL = 2;
    private static final int DEFAULT_COUNT_UPLOAD_MULTIPOI = 100;
    private static final int DEFAULT_BATCH_TRACE_INTERVAL = 120;
    private static final int MIN_BATCH_TRACE_INTERVAL = 30;

    private static long mBatchPoiReportInterval = DEFAULT_BATCH_TRACE_INTERVAL;
    private static long mOrignalTimeout;
    private static long mCurrentTimeout;
    private long mPreReportGPSTime = -1;
    private long mPreSuccTime = -1;
    private final int mUserID;
    private int mNotifyID = -1;

    public PositionDetecterThread() {
        //批量上传轨迹记录时间间隔，单位毫秒，默认2分钟
        mBatchPoiReportInterval = MyApplication.getInstance()
                .getConfigValue("batchTraceInterval", DEFAULT_BATCH_TRACE_INTERVAL);
        mUserID = MyApplication.getInstance().getUserId();
        mPreSuccTime = System.currentTimeMillis();

        // 测试用
//        ContentValues cv = new ContentValues();
//        cv.put("isSuccess", 0);
//        DatabaseHelper.getInstance().update(GpsXYZ.class, cv, "date(reporttime)=date('2017-06-01') and userid=1");
    }

    @Override
    public void run() {
        try {
            String packageName = MyApplication.getInstance().getPackageName().toLowerCase();
            if (packageName.contains("onemap")) return;

            TimeUnit.SECONDS.sleep(5);
            BaseClassUtil.logd(this, "开始运行");

            final boolean mExistBatchReportService = testExistBatchReportService();
            if (mExistBatchReportService) {
                mCurrentTimeout = mBatchPoiReportInterval;
            } else {
                mCurrentTimeout = DEFAULT_SINGLE_TRACE_INTERVAL;
            }
            mOrignalTimeout = mCurrentTimeout;

            while (!isExit) {
                if (mExistBatchReportService) {
                    reportGPSBatch();
                } else {
                    reportGPSSingle();
                }
                TimeUnit.SECONDS.sleep(mCurrentTimeout);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void reportGPSBatch() {
        long nowTimeStamp = System.currentTimeMillis();

        // 15分钟没有上传轨迹信息开始语音提醒
        if (nowTimeStamp - mPreSuccTime > TIME_NOT_UPLOAD_MULTIPOI) {
            mPreSuccTime = nowTimeStamp;
            sendNotify();
        }

        if (!ConnectivityUtil.isNetworkUsable()) {
            return;
        }

        try {
            List<TracePoint> pointList;

            List<GpsXYZ> gpsXYZList = getSavedList(mUserID);
            if (gpsXYZList.size() >= DEFAULT_COUNT_UPLOAD_MULTIPOI) {
                mCurrentTimeout /= 2;
                mCurrentTimeout = mCurrentTimeout < MIN_BATCH_TRACE_INTERVAL ? MIN_BATCH_TRACE_INTERVAL : mCurrentTimeout;
            } else {
                mCurrentTimeout = mOrignalTimeout;
            }
            pointList = TracePoint.fromGPSXYZList(gpsXYZList);

            final TracePoint realtimePoint = getRealtimePoint();
            pointList.add(realtimePoint);

            String url = getReportMultiPoiURL(mUserID);
            ResultData<Integer> resultData = reportMultiPoi(url, pointList);

            if (resultData == null) {
                Log.d(TAG, "上传失败：网络不通，后台上传实时位置");
                insertToBackEntity(nowTimeStamp, realtimePoint, url);
                return;
            }

            mPreSuccTime = nowTimeStamp;

            if (resultData.ResultCode < 0) {
                Log.d(TAG, "上传失败：" + resultData.ResultMessage);
                return;
            }

            updateSuccessFlag(gpsXYZList, resultData.DataList);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mPreReportGPSTime = nowTimeStamp;
        }
    }

    /**
     * 更改上传标志isSuccess
     *         0：未上传
     *         1：上传成功   服务端返回码（200：成功，201：距离速度过滤，202：时间过滤）
     * @param gpsXYZList        轨迹点集合
     * @param resultCodeList    批量上传时返回的结果码
     * @return                  更新标志的记录条数
     */
    private int updateSuccessFlag(List<GpsXYZ> gpsXYZList, List<Integer> resultCodeList) {
        int count = 0;
        ContentValues cv = new ContentValues();
        cv.put("isSuccess", 1);
        for (int i = 0; i < gpsXYZList.size() - 1; i++) {
            int code = resultCodeList.get(i);
            if (i == 0 && code <= 0 && code != -2) {
                break;
            }
            /*if (code == 200 || code == 201 || code == 202) {
                cv.put("isSuccess", 1);
            }*/
            count += DatabaseHelper.getInstance().update(GpsXYZ.class, cv, "id= " + gpsXYZList.get(i).getId());
        }
        return count;
    }

    private void reportGPSSingle() {
        List<GpsXYZ> savedList = getSavedList(mUserID);
        if (savedList.size() == 0) return;

        ContentValues cv = new ContentValues();
        cv.put("isSuccess", 1);

        for (GpsXYZ item : savedList) {
            ResultWithoutData data = item.report();
            if (data == null)//网络不畅通
                return;

            int code = data.ResultCode;

            if (code <= 0 && code != -2)
                continue;

            DatabaseHelper.getInstance().update(GpsXYZ.class, cv, "id= " + item.getId());
        }
    }

    private void insertToBackEntity(long nowTimeStamp, TracePoint realtimePoint, String url) {
        boolean isConn = NetUtil.testNetState();
        realtimePoint.State += ";" + (isConn ? "有" : "无") + "网络";
        List<TracePoint> pointList = new ArrayList<>();
        pointList.add(realtimePoint);
        String json = new Gson().toJson(pointList);
        ReportInBackEntity entity = new ReportInBackEntity(json, realtimePoint.UserID, ReportInBackEntity.REPORTING,
                url, String.valueOf(nowTimeStamp), "实时位置", "", "");
        entity.insert();
    }

    private ResultData<Integer> reportMultiPoi(String url, List<TracePoint> pointList) {
        try {
            Gson gson = new Gson();
            String result = NetUtil.executeHttpPost(url, gson.toJson(pointList));
            return gson.fromJson(result, new TypeToken<ResultData<Integer>>() {
            }.getType());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @NonNull
    private String getReportMultiPoiURL(int userId) {
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/BatchReportTrace"
                + "?userID=" + userId;
        String version = MyApplication.getInstance().getConfigValue("BatchReportVersion");
        return url + "&version=" + version;
    }

    /*获取当前实时的轨迹位置信息*/
    @NonNull
    private TracePoint getRealtimePoint() {
        GpsXYZ lastXY = GpsReceiver.getInstance().getLastLocalLocation();
        lastXY.readDeviceInfo();
        TracePoint lastPoint = TracePoint.fromGPSXYZ(lastXY);
        lastPoint.correctTime();
        return lastPoint;
    }

    /*判断是否存在批量上传轨迹服务*/
    private boolean testExistBatchReportService() {
        boolean existBatchReportTrace = MyApplication.getInstance().getConfigValue("BatchReportTrace", 0) > 0;
        if (!existBatchReportTrace) {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/BatchReportTrace";
            existBatchReportTrace = NetUtil.testServiceExist(url);
        }
        return existBatchReportTrace;
    }

    private void sendNotify() {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            /**
             * 通知来显示信息
             */
            private void setNotification(String content) {
                try {
                    Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(mapGISFrame);

                    builder.setSmallIcon(mapGISFrame.getApplicationInfo().icon);

                    // 点击后提示消失
                    builder.setAutoCancel(true);
                    builder.setDefaults(Notification.DEFAULT_VIBRATE);
                    builder.setContentTitle(mapGISFrame.getString(com.mapgis.mmt.R.string.app_name));
                    builder.setContentText(content);

                    builder.setSound(ringUri);

                    Notification notification = builder.build();

                    if (mNotifyID < 1)
                        mNotifyID = MmtNotificationManager.notify(notification);
                    else
                        MmtNotificationManager.notify(mNotifyID, notification);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public boolean handleMessage(Message msg) {
                setNotification("网络异常，请注意检查手机状态");

                return false;
            }
        });
    }

    /**
     * 本地SQLite数据库保存的当日未成功上报的坐标
     *
     * @param userID 用户ID
     * @return 轨迹
     */
    @NonNull
    private List<GpsXYZ> getSavedList(int userID) {
        return getSavedList(userID, DEFAULT_COUNT_UPLOAD_MULTIPOI);
    }

    /**
     * 本地SQLite数据库保存的当日未成功上报的坐标
     *
     * @param userID 用户ID
     * @param count  读取数据的条数
     * @return 轨迹
     */
    private List<GpsXYZ> getSavedList(int userID, int count) {
        String sql = "select * from positonreporter"
                + " where issuccess=0 and userid=" + userID
                + " and reportTime >= date('now','localtime','start of day')"
                + " order by reporttime asc limit " + count;

        return DatabaseHelper.getInstance().queryBySql(GpsXYZ.class, sql);
    }
}
