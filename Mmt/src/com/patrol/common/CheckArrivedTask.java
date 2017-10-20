package com.patrol.common;

import android.app.Notification;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.global.MmtNotificationManager;
import com.mapgis.mmt.module.gps.GPSTipUtils;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.module.login.UserBean;
import com.patrol.entity.KeyPoint;
import com.patrol.entity.ReportArriveEntity;
import com.patrol.entity.TaskInfo;
import com.patrol.module.myplan.FetchPointsTask;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.GeoPolygon;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.spatial.SpaRelation;
import com.zondy.mapgis.util.objects.IntList;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 2017-6-14 增加批量上传到位点的接口
 */
public class CheckArrivedTask implements Runnable {
    private int notifyID;
    private Notification notification;
    private long lastArriveTipDate = -1, lastGPSTime = -1, lastDownloadTime = -1;
    private FetchPointsTask task;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    private int userID;
    private String userName;
    private String fetchDoingCaseUrl;
    private String postArriveUrl;
    private static int existBatchReportService = 0;

    private List<TaskInfo> taskList = new ArrayList<>();

    public List<TaskInfo> getTaskBaseList() {
        List<TaskInfo> infoList = new ArrayList<>();

        for (TaskInfo info : taskList)
            infoList.add(info.toBaseInfo());

        return infoList;
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName(this.toString());

            if (MyApplication.getInstance().getConfigValue("CheckXXArrive", 1) > 0) {
                downloadAllTask(true);

                testExistBatchReportService();
                checkXXTaskArrived();
            }

            if (MyApplication.getInstance().getConfigValue("CheckMyCaseArrive", 0) > 0)
                checkDoingCaseArrived();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*判断是否存在批量上传轨迹服务*/
    private void testExistBatchReportService() {
        if (existBatchReportService == 0) {
            boolean exist = NetUtil.testServiceExist(MyPlanUtil.getStandardURL() + "/ArriveKeyPoints");

            if (exist)
                existBatchReportService++;
            else
                existBatchReportService--;
        }
    }

    /**
     * 返回不带管段对象的任务实体，防止因为管段过多导致进程通信失败
     *
     * @param id 任务ID
     * @return 任务实体
     */
    public TaskInfo getTaskInfo(int id) {
        for (TaskInfo info : taskList) {
            if (info.ID != id)
                continue;

            TaskInfo task = info.toBaseInfo();

            task.Points = new ArrayList<>();

            for (KeyPoint kp : info.Points) {
                if (kp.Type == 2)
                    continue;

                task.Points.add(kp);
            }

            return task;
        }

        return null;
    }

    /**
     * 返回指定范围的特定任务的管段对象列表
     *
     * @param id   任务ID
     * @param xmin XMIN
     * @param ymin YMIN
     * @param xmax XMAX
     * @param ymax YMAX
     * @return 管段对象列表
     */
    public List<KeyPoint> fetchPipeLines(int id, double xmin, double ymin, double xmax, double ymax) {
        List<KeyPoint> points = new ArrayList<>();

        try {
            TaskInfo task = null;

            for (TaskInfo info : taskList) {
                if (info.ID != id)
                    continue;

                task = info;
            }

            if (task == null)
                return points;

            Rect rect = new Rect(xmin, ymin, xmax, ymax);

            for (KeyPoint kp : task.Points) {
                if (kp.Type != 2 || !GisUtil.isInRect(rect, kp.getDot(), 0))
                    continue;

                points.add(kp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return points;
    }

    public CheckArrivedTask() {
        notifyID = -1;

        notification = new Notification();

        notification.defaults |= Notification.DEFAULT_VIBRATE;

        String packageName = MyApplication.getInstance().getApplicationContext().getPackageName();
        notification.sound = Uri.parse("android.resource://" + packageName + "/" + R.raw.msgring);

        userID = MyApplication.getInstance().getUserId();
        userName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

        fetchDoingCaseUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/CitySvr_Biz_SXGS/REST/BizSXGSRest.svc/" + userID + "/GetAllDoingCaseCoor";
        postArriveUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/CitySvr_Biz_SXGS/REST/BizSXGSRest.svc/ReportInFence";
    }

    public List<TaskInfo> refreshMyPlan() {
        lastDownloadTime = -1;
        boolean needCache = MyApplication.getInstance().getConfigValue("PatrolNeedCache", 1) == 1;

        downloadAllTask(needCache);

        return getTaskBaseList();
    }

    public void onKeyPointFeedback(int taskID, int keyPointID) {
        for (TaskInfo task : taskList) {
            if (task.ID != taskID)
                continue;

            for (KeyPoint kp : task.Points) {
                if (kp.ID != keyPointID)
                    continue;

                if (kp.IsFeedback != 1)
                    task.FeedbackSum++;

                kp.IsFeedback = 1;
                kp.FeedbackTime = BaseClassUtil.getSystemTime();

                break;
            }
        }
    }

    /**
     * 下载所有巡线任务，包含任务点，以达到开机即可做巡检计划到位检测
     */
    private synchronized void downloadAllTask(boolean needCache) {
        try {
            //没有计划的时候五分钟下载一次，有计划就十分钟检测下载一次
            long span = 5 * 60 * 1000;

            if (taskList != null && taskList.size() > 0)
                span = 10 * 60 * 1000;

            if (lastDownloadTime > 0 && (new Date().getTime() - lastDownloadTime) < span)
                return;

            Log.v("巡线计划", "获取计划主体==>后台自动获取");

            String ids = ",";

            if (taskList != null) {
                for (TaskInfo info : taskList) {
                    ids += info.ID + ",";
                }
            }

            String url = MyPlanUtil.getStandardURL() + "/FetchCurrentTaskNoPoints";

            String json = NetUtil.executeHttpGet(url, "userID", String.valueOf(userID), "existIDs", ids);

            ResultData<TaskInfo> data = new Gson().fromJson(json, new TypeToken<ResultData<TaskInfo>>() {
            }.getType());

            lastDownloadTime = new Date().getTime();

            if (data == null || data.ResultCode < 0 || data.DataList == null || data.DataList.size() == 0)
                return;

            task.isGZip = data.ResultCode == 202;

            ArrayList<TaskInfo> dataList = data.DataList;//将旧的任务的点内容赋值给新的点

            for (TaskInfo info : dataList) {
                if (needCache) {
                    for (TaskInfo oldInfo : taskList) {
                        if (oldInfo.ID == info.ID && oldInfo.Points != null && oldInfo.Points.size() > 0) {
                            info.Points = oldInfo.Points;

                            break;
                        }
                    }
                }

                if (info.Points != null && info.Points.size() > 0)
                    continue;

                ResultData<KeyPoint> pointsData = task.doInBackground(info);

                if (pointsData != null && pointsData.DataList != null)
                    info.Points = pointsData.DataList;
            }

            taskList = dataList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 检查巡线计划到位状态
     */
    private void checkXXTaskArrived() {
        if (taskList == null)
            return;

        // 到位的任务
        List<ReportArriveEntity> entities = new ArrayList<>();

        try {
            GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

            for (TaskInfo task : taskList) {
                if (!task.IsStart)
                    continue;

                ReportArriveEntity entity = new ReportArriveEntity(task.ID);

                for (KeyPoint kp : task.Points) {
                    try {
                        //二秒钟更新坐标一次
                        if (System.currentTimeMillis() - lastGPSTime > 2 * 1000) {
                            xyz = GpsReceiver.getInstance().getLastLocalLocation();

                            if (!xyz.isUsefullGPS())
                                break;

                            lastGPSTime = System.currentTimeMillis();
                        }

                        if (xyz == null || !xyz.isUsefullGPS())
                            break;

                        // 是否到位
                        if (!isArrived(xyz, kp))
                            continue;

                        entity.keyIdList.add(kp.ID);

                        kp.TaskID = task.ID;
                        kp.IsArrive = 1;
                        kp.ArriveTime = BaseClassUtil.getSystemTime();

                        if (kp.Type == 2)
                            task.PipeLenth += kp.Lenth;
                        else
                            task.ArriveSum++;

                        //2.更新界面显示状态，包括地图管点到位图标和下方的到位状态统计条
                        refreshViewData(task, kp);

                        long nowDate = System.currentTimeMillis();

                        if (kp.Type != 2 && (nowDate - lastArriveTipDate) > 5000) {// 5秒以上重复响铃震动提示，避免过于频繁
                            if (notifyID < 0)
                                notifyID = MmtNotificationManager.notify(notification);
                            else
                                MmtNotificationManager.notify(notifyID, notification);

                            lastArriveTipDate = nowDate;
                            GPSTipUtils.arrivedTip();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (entity.keyIdList.size() > 0)
                    entities.add(entity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // 批量上传到位点
            if (entities.size() > 0)
                singleThreadExecutor.execute(new BatchArriveReportRunnable(entities));
        }
    }

    /*刷新界面所显示数据*/
    private void refreshViewData(TaskInfo task, KeyPoint kp) {
        Intent intent = new Intent(MmtMainService.class.getName());
        intent.putExtra("kp", kp);
        intent.putExtra("len", task.PipeLenth);
        intent.putExtra("sum", task.ArriveSum);
        MyApplication.getInstance().sendBroadcast(intent);
    }

    /*判断一个点是否到位*/
    private boolean isArrived(GpsXYZ xyz, KeyPoint kp) {
        if (kp.IsArrive == 1) return false;

        Dot dot = GisUtil.convertDot(kp.Position);
        if (dot == null) return false;

        if (kp.Type == 2) {
            if (Math.abs(dot.x - xyz.getX()) > lineR || Math.abs(dot.y - xyz.getY()) > lineR)
                return false;
            else {
                double distance = GisUtil.calcDistance(dot, xyz.convertToPoint());
                if (distance > lineR) return false;
            }
        } else {
            if (Math.abs(dot.x - xyz.getX()) > r || Math.abs(dot.y - xyz.getY()) > r) return false;
            else {
                double distance = GisUtil.calcDistance(dot, xyz.convertToPoint());
                if (distance > r) return false;
            }
        }
        return true;
    }

    private class DoingCaseArriveReportRunnable implements Runnable {

        String caseNo, XY, area, currentXY;

        DoingCaseArriveReportRunnable(String caseNo, String XY, String area, String currentXY) {
            this.caseNo = caseNo;
            this.XY = XY;
            this.area = area;
            this.currentXY = currentXY;
        }

        @Override
        public void run() {
            try {
                ArriveInRecord arriveInRecord = new ArriveInRecord(caseNo, XY, area, userID,
                        userName, BaseClassUtil.getSystemTime(), currentXY);

                NetUtil.executeHttpPost(postArriveUrl, new Gson().toJson(arriveInRecord));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Hashtable<String, Boolean> arriveTable = new Hashtable<>();
    private long lastDownloadCaseTime = -1;
    private ArrayList<CaseCoordinate> myCaseList = null;

    private void checkDoingCaseArrived() {
        try {
            GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

            if (xyz == null || !xyz.isUsefullGPS()) {
                return;
            }

            long tick = new Date().getTime();

            if ((tick - lastDownloadCaseTime) > 2 * 60 * 1000) {
                String json = NetUtil.executeHttpGet(fetchDoingCaseUrl);

                lastDownloadCaseTime = tick;

                if (TextUtils.isEmpty(json))
                    throw new Exception("获取在办列表失败：网络错误");

                ResultData<CaseCoordinate> data = new Gson().fromJson(json,
                        new TypeToken<ResultData<CaseCoordinate>>() {
                        }.getType());

                ResultWithoutData.checkEmptyData(data, "获取在办列表失败");

                myCaseList = data.DataList;
            }

            if (myCaseList == null || myCaseList.size() == 0)
                return;

            Dot dot = xyz.convertToPoint();

            for (CaseCoordinate myCase : myCaseList) {
                boolean isArrive = checkInFence(myCase.Area, myCase.XY, r, dot);

                if (isArrive && !(arriveTable.containsKey(myCase.CaseNo) && arriveTable.get(myCase.CaseNo))) {
                    singleThreadExecutor.execute(new DoingCaseArriveReportRunnable(myCase.CaseNo,
                            myCase.XY, myCase.Area, dot.toString()));
                }

                arriveTable.put(myCase.CaseNo, isArrive);
            }

            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Dots area2Dots(String area) {
        Dots dots = new Dots();

        try {
            JSONObject jsonObject = new JSONObject(area);
            JSONArray jsonArray = jsonObject.getJSONArray("rings").getJSONArray(0);

            String dotStr;

            for (int i = 0, length = jsonArray.length(); i < length; i++) {
                dotStr = jsonArray.getString(i);

                dotStr = dotStr.replace("[", "").replace("]", "");

                if (!BaseClassUtil.isNullOrEmptyString(dotStr)) {
                    String[] xy = dotStr.split(",");

                    double x = Double.valueOf(xy[0]);
                    double y = Double.valueOf(xy[1]);

                    dots.append(new Dot(x, y));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dots;
    }

    /**
     * 检查是否在围栏内（点半径内或区域内）
     *
     * @param area   区域
     * @param xy     点
     * @param radius 半径
     * @param gpsDot 用户当前所在位置
     */
    private boolean checkInFence(String area, String xy, double radius, Dot gpsDot) {
        try {
            if (TextUtils.isEmpty(area) && TextUtils.isEmpty(xy))
                return false;

            // 1.检查是否在区域内
            if (!TextUtils.isEmpty(area)) {
                GeoPolygon polygon = new GeoPolygon();

                Dots dots = area2Dots(area);

                IntList intList = new IntList();

                intList.append(dots.size());

                polygon.setDots(dots, intList);

                short result = SpaRelation.isDotInReg(gpsDot, polygon, (short) 1, 0.00001);

                return result > 0;
            }

            // 2.检查是否在点的半径内
            Dot dot = GisUtil.convertDot(xy);

            if (dot == null) {
                return false;
            }

            if (Math.abs(dot.x - gpsDot.x) > radius || Math.abs(dot.y - gpsDot.y) > radius) {
                return false;
            } else {
                double distance = GisUtil.calcDistance(dot, gpsDot);

                return distance <= radius;
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

    private double r, lineR;

    public void start(ScheduledExecutorService executorService) {
        try {
            r = MyApplication.getInstance().getConfigValue("PointRadius", 100);
            lineR = MyApplication.getInstance().getConfigValue("PolylineRadius", 100);

            if (MyApplication.getInstance().getConfigValue("CheckXXArrive", 1) > 0) {
                task = new FetchPointsTask(null);
            }

            executorService.scheduleWithFixedDelay(this, 2 * 1000, 2 * 1000, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class CaseCoordinate {
        /**
         * 工单编号
         */
        public String CaseNo;
        /**
         * 坐标位置
         */
        public String XY;
        /**
         * 区域位置
         */
        String Area;

        @Override
        public String toString() {
            return "CaseCoordinate{" +
                    "CaseNo='" + CaseNo + '\'' +
                    ", XY='" + XY + '\'' +
                    ", Area='" + Area + '\'' +
                    '}';
        }
    }

    private class ArriveInRecord {
        ArriveInRecord(String caseNo, String XY, String area, int userID, String userName, String arriveTime, String arriveXY) {
            CaseNo = caseNo;
            this.XY = XY;
            Area = area;
            UserID = userID;
            UserName = userName;
            ArriveTime = arriveTime;
            ArriveXY = arriveXY;
        }

        /**
         * 工单编号
         */
        public String CaseNo;
        /**
         * 坐标位置(工单)
         */
        public String XY;
        /**
         * 区域位置(工单)
         */
        String Area;
        /**
         * 到位人UserID
         */
        public int UserID;
        /**
         * 到位人
         */
        public String UserName;
        /**
         * 到位时间
         */
        String ArriveTime;
        /**
         * 到位坐标
         */
        String ArriveXY;
    }

    @Override
    public String toString() {
        return "CheckArrivedTask{lastGPSTime=" + lastGPSTime + ", userName='" + userName + '\'' + '}';
    }

    /**
     * 批量到位点上传
     */
    private class BatchArriveReportRunnable implements Runnable {
        List<ReportArriveEntity> list;

        private BatchArriveReportRunnable(List<ReportArriveEntity> list) {
            this.list = list;
        }

        private void reportOne(int id) {
            try {
                //1.更新数据库点状态
                String url = MyPlanUtil.getStandardURL() + "/ArriveKeyPoint?id=" + id + "&userID=" + userID;

                ReportInBackEntity entity = new ReportInBackEntity(ReportInBackEntity.GET_FLAG, userID,
                        ReportInBackEntity.REPORTING, url, String.valueOf(id), "巡线到位", "", "");

                ResultData<Integer> data = entity.report();

                if (data.ResultCode > 0)
                    return;

                entity.insert();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                Log.v("巡线计划", "完成到位点上传：" + id);
            }
        }

        private void reportBatch() {
            try {
                //1.更新数据库点状态
                String url = MyPlanUtil.getStandardURL() + "/ArriveKeyPoints?userID=" + userID;

                String data = new Gson().toJson(list);

                ReportInBackEntity entity = new ReportInBackEntity(data, userID, ReportInBackEntity.REPORTING
                        , url, String.valueOf(System.currentTimeMillis()), "巡线到位", "", "");

                ResultData<Integer> result = entity.report();

                if (result.ResultCode > 0)
                    return;

                entity.insert();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                Log.v("巡线计划", "完成到位点上传：" + list.toString());
            }
        }

        @Override
        public void run() {
            try {
                if (existBatchReportService > 0)
                    reportBatch();
                else {
                    for (ReportArriveEntity entity : list) {
                        for (int id : entity.keyIdList) {
                            reportOne(id);

                            TimeUnit.MILLISECONDS.sleep(10);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
