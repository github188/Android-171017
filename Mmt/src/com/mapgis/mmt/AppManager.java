package com.mapgis.mmt;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.widget.customview.ToastView;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.global.MmtBaseThread;
import com.mapgis.mmt.global.MmtGuardService;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.global.MmtNotificationManager;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gps.GpsReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * app管理类
 */
public class AppManager {
    public static List<Activity> activityList = new CopyOnWriteArrayList<>();
    private static List<Service> serviceList = new CopyOnWriteArrayList<>();
    private static List<MmtBaseThread> threadList = new CopyOnWriteArrayList<>();

    /**
     * activity管理：添加activity到列表
     *
     * @param activity 操作对象
     */
    public static void addActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(activity);
        }
    }

    /**
     * service管理：添加service到列表
     *
     * @param service 服务action名
     */
    public static synchronized void addService(Service service) {
        if (!serviceList.contains(service)) {
            serviceList.add(service);
        }
    }

    public static synchronized void addThread(MmtBaseThread thread) {
        if (!threadList.contains(thread)) {
            threadList.add(thread);
        }
    }

    /**
     * activity管理：当前activity(默认是列表的最后一个)
     *
     * @return 操作对象
     */
    public static Activity currentActivity() {
        if (activityList.size() == 0) {
            return null;
        }

        return activityList.get(activityList.size() - 1);
    }

    public static Activity getActivity(Class<? extends BaseActivity> clazz) {
        for (Activity activity : activityList) {
            if (clazz.getSimpleName().equals(activity.getClass().getSimpleName())) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 获取倒数第二个activity
     *
     * @return 结果对象
     */
    public synchronized static Activity secondLastActivity() {
        if (activityList.size() < 2) {
            return null;
        }

        return activityList.get(activityList.size() - 2);
    }

    /**
     * @param context 操作对象
     * @deprecated 将地图页面重新放回栈底，按之前的顺序重新排列
     */
    public static void resetActivityStack(Context context) {
        for (int i = 1; i < activityList.size(); i++) {
            Intent intent = new Intent(context, activityList.get(i).getClass());

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            context.startActivity(intent);
        }
    }

    /**
     * activity管理：结束当前activity(默认是列表的最后一个)
     */
    public static void finishActivity() {
        Activity activity = currentActivity();

        finishActivity(activity);
    }

    public static synchronized void removeService(Service service) {
        serviceList.remove(service);
    }

    /**
     * activity管理：从列表中移除activity
     *
     * @param activity 移除的对象
     */
    public static void finishActivity(Activity activity) {
        if (activity == null || !activityList.contains(activity) || activity instanceof MapGISFrame)
            return;

        activityList.remove(activity);

        if (!activity.isFinishing())
            activity.finish();
    }

    /**
     * activity管理：删除除地图界外其他所有Activity
     */
    public static void finishToMap() {
        List<Activity> activities = new ArrayList<>(activityList);

        for (int i = 1; i < activities.size(); i++) {
            finishActivity(activities.get(i));
        }
    }

    /**
     * activity管理：删除除地图和九宫格外其他所有Activity,并充值堆栈关系
     *
     * @param activity 操作对象
     */
    public static void finishToNavigation(Activity activity) {
        List<Activity> activities = new ArrayList<>(activityList);

        for (int i = 2; i < activities.size(); i++) {
            finishActivity(activities.get(i));
        }

        resetActivityStack(activity);
    }

    /**
     * activity管理：结束指定类型的activity
     *
     * @param cls 操作对象
     */
    public static void finishActivity(Class<?> cls) {
        for (Activity activity : activityList) {
            if (activity != null && activity.getClass().equals(cls)) {
                activity.finish();
            }
        }
    }

    /**
     * activity管理：结束所有activity
     */
    private static void finishAllActivity() {
        for (Activity activity : activityList) {
            if (activity != null) {
                activity.finish();
            }
        }

        activityList.clear();
    }

    private static void stopService(Service service) {
        if (service == null)
            return;

        service.stopSelf();

        serviceList.remove(service);
    }

    /*根据类名获取对应的Service对象*/
    public static Service getService(Class clz) {
        for (Service service : serviceList) {
            if (clz.getSimpleName().equals(service.getClass().getSimpleName())) {
                return service;
            }
        }
        return null;
    }

    /**
     * 停止所有Service
     */
    private static void stopAllService() {
        for (Service service : serviceList) {
            Log.e("MmtService", service + "____stopAllService方法被执行");

            stopService(service);
        }

        serviceList.clear();

//        Context context = MyApplication.getInstance();
//
//        context.stopService(new Intent(context, MmtMainService.class));
    }

    private static void stopAllThread() {
        for (MmtBaseThread thread : threadList) {
            if (thread == null || thread.getState() == Thread.State.TERMINATED)
                continue;

            thread.abort();
        }

        threadList.clear();
    }

    /**
     * activity管理：结束所有activity，彻底关闭应用
     */
    public static void finishProgram(boolean isMainService) {
        if (!isMainService) {
            finishAllActivity();

            stopAllService();
        }

        stopAllThread();

        GpsReceiver.getInstance().stop();

        ToastView.cancel();

        MmtNotificationManager.cancelAll();

        CacheUtils.getInstance().flush();

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * activity管理：结束所有activity，彻底关闭应用
     */
    public static void finishProgram() {
        finishProgram(false);
    }

    /**
     * 判断Activity是否存在
     */
    public static <T extends BaseActivity> boolean existActivity(Class<T> cls) {
        for (Activity activity : activityList) {
            if (activity != null && activity.getClass().equals(cls)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重新启动app
     */
    public static void restartApp(){
        AlarmManager alarmManager= (AlarmManager) MyApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        Intent laucherIntent = new Intent(MyApplication.getInstance(), ActivityClassRegistry.getInstance().getActivityClass("登录界面"));
        laucherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent operation=PendingIntent.getActivity(MyApplication.getInstance(),0,laucherIntent,laucherIntent.getFlags());
        alarmManager.set(AlarmManager.RTC,System.currentTimeMillis()+2000,operation);
        //延时2秒
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //kill守护
                MyApplication.getInstance().stopService(new Intent(MyApplication.getInstance(), MmtGuardService.class));
                MyApplication.getInstance().stopService(new Intent(MyApplication.getInstance(), MmtMainService.class));
                finishProgram();
            }
        },2000);

    }
    public static void clearAppData(){
        //清空偏好设置
        MyApplication.getInstance().getSystemSharedPreferences().edit().clear().apply();
        //删除所有文件夹
        List<String> enviriomentPaths=new ArrayList<>();
        String externalStorage0="";
        String externalStorage1="";
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //内部存储
            externalStorage0="/storage/emulated/0";
            enviriomentPaths.add(externalStorage0+"/MapGIS");
            enviriomentPaths.add(externalStorage0+"/CityMobile");
            //sd卡
            externalStorage1=Environment.getExternalStorageDirectory().getAbsolutePath();
            enviriomentPaths.add(externalStorage1+"/MapGIS");
            enviriomentPaths.add(externalStorage1+"/CityMobile");
        }else {
            externalStorage0=Environment.getExternalStorageDirectory().getAbsolutePath();
            enviriomentPaths.add(externalStorage0+"/MapGIS");
            enviriomentPaths.add(externalStorage0+"/CityMobile");
        }
        for(String item:enviriomentPaths){
            File file=new File(item);
            if(file.exists()){
                FileUtil.deleteDirectory(file);
            }
        }
    }

}