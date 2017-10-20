package com.patrolproduct.server;

import android.app.Notification;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.SavedReportInfo;
import com.mapgis.mmt.global.MmtBaseThread;
import com.mapgis.mmt.global.MmtNotificationManager;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.MyPlanUtil;
import com.patrolproduct.module.myplan.SessionManager;
import com.patrolproduct.module.myplan.entity.PatrolTask;
import com.patrolproduct.module.myplan.feedback.FeedItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CheckArrivedTaskForOldPlan extends MmtBaseThread {
    private int notifyID;
    private Notification notification;

    /**
     * 用于老的计划到位判断
     */
    private long preArriveTipTime = -1;

    private OnAllInsiteExec onAllInsiteExec = null;

    private CheckArrivedTaskForOldPlan() {
        notifyID = -1;

        notification = new Notification();

        notification.defaults |= Notification.DEFAULT_VIBRATE;

        String packageName = MyApplication.getInstance().getApplicationContext().getPackageName();
        notification.sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.msgring);

        onAllInsiteExec=MyApplication.getInstance().getOnAllInsiteExec();
    }

    private static CheckArrivedTaskForOldPlan instance = null;

    public static synchronized void fire() {
        if (instance != null)
            return;

        if (SessionManager.patrolTaskList == null || SessionManager.patrolTaskList.size() == 0)
            return;

        instance = new CheckArrivedTaskForOldPlan();
        AppManager.addThread(instance);

        instance.start();
    }

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(5);

            BaseClassUtil.logd(this, "开始运行");

            String packageName = MyApplication.getInstance().getPackageName().toLowerCase();

            if (packageName.contains("onemap"))
                return;

            while (!isExit) {
                try {
                    TimeUnit.SECONDS.sleep(2);// 强制休眠2秒

                    //02.检查到位状态
                    checkArrived();

                    //03.检查计划是否全部到位
                    checkAllArrived();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
   private void  checkAllArrived(){
       if(onAllInsiteExec==null){
           return;
       }
       onAllInsiteExec.autoCompeletTask();
   }

    /**
     * 检查巡检计划到位状态
     */
    private void checkArrived() {
        try {
            if (SessionManager.patrolTaskList == null || SessionManager.patrolTaskList.size() == 0)
                return;

            GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

            if (!xyz.isUsefullGPS())
                return;

            List<String> ids = new ArrayList<>();

            for (PatrolTask pt : SessionManager.patrolTaskList) {
                ids.add(pt.TaskID);
            }

            String idList = TextUtils.join(",", ids.toArray());

            double x = xyz.getX(), y = xyz.getY(), r = MyApplication.getInstance().getConfigValue("PointRadius", 100);

            String where = " where (x-(" + x + "))*(x-(" + x + "))+(y-(" + y + "))*(y-(" + y + "))<=" + r + "*" + r
                    + " and isarrived=0 and taskid in (" + idList + ")";//防止x或者y有负值存在，加括号是必须的

            String sql = "select * from patroldevice" + where;

            List<PatrolDevice> devices = DatabaseHelper.getInstance().queryBySql(PatrolDevice.class, sql);

            if (devices == null || devices.size() <= 0)
                return;

            String arrivedDate = BaseClassUtil.getSystemTime();

            sql = "update patroldevice set isarrived=1,arriveddate='" + arrivedDate + "'" + where;

            DatabaseHelper.getInstance().getSqLiteDatabase().execSQL(sql);

            for (PatrolDevice device : devices) {
                device.IsArrived = true;
                device.ArrivedDate = arrivedDate;

                //1.更新内存到位状态
                MyPlanUtil.updateArriveOrFeedbackState(device.TaskId, device.LayerName, device.PipeNo, true);

                //2.更新服务到位状态
                FeedItem[] items = new FeedItem[]{
                        new FeedItem("taskid", "0", String.valueOf(device.TaskId)),
                        new FeedItem("index", "0", String.valueOf(device.Index))
                };

                String json = new Gson().toJson(items);

                SavedReportInfo info = new SavedReportInfo(device.TaskId, json, "", "arrive");

                DatabaseHelper.getInstance().insert(info);
            }

            MyApplication.getInstance().sendToBaseMapHandle(new DeviceArrivedCallback(devices));

            long now = new Date().getTime();

            // 5秒以上重复响铃震动提示，避免过于频繁
            if (preArriveTipTime > 0 && (now - preArriveTipTime) < 5000)
                return;

            if (notifyID < 0)
                notifyID = MmtNotificationManager.notify(notification);
            else
                MmtNotificationManager.notify(notifyID, notification);

            preArriveTipTime = now;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
