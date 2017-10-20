package com.mapgis.mmt.global;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.patrolproduct.server.CheckArrivedTaskForOldPlan;

import java.util.concurrent.TimeUnit;

public class MmtGuardService extends Service {
    private MmtBaseThread worker = null;

    public void onCreate() {
        super.onCreate();

        AppManager.addService(this);

        Intent intent = new Intent(MyApplication.getInstance(), ActivityClassRegistry.getInstance().getActivityClass("主界面"));
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent appIntent = PendingIntent.getActivity(MyApplication.getInstance().getApplicationContext(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(appIntent);
        builder.setSmallIcon(getApplicationInfo().icon);
        builder.setAutoCancel(false);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText("正在运行");

        Notification notification = builder.build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_SHOW_LIGHTS;
        notification.tickerText = getString(R.string.app_name) + "正在运行";

        startForeground(666, notification);

        worker = new MmtBaseThread() {
            @Override
            public void run() {
                while (!isExit) {
                    try {
                        BaseClassUtil.logi(this.getName(), "守护线程状态：" + worker.getState());

                        TimeUnit.SECONDS.sleep(10);

                        if (extras != null)
                            checkMainService();

                        CheckArrivedTaskForOldPlan.fire();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        worker.start();
        worker.setName(this.getClass().getName());
    }

    private Bundle extras;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            extras = intent.getExtras();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    boolean isMainAlive = false; //isGisAlive = false;

    public void checkMainService() {
        isMainAlive = false;
//        isGisAlive = false;

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MmtMainService.class.getName().equals(service.service.getClassName())) {
                isMainAlive = true;
            }

//            if (MmtGISService.class.getName().equals(service.service.getClassName())) {
//                isGisAlive = service.started;
//            }
        }

        new Handler(this.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isMainAlive) {
                        BaseClassUtil.logi(MmtGuardService.this, "即将重启后台辅助进程MAIN服务");

                        Intent intent = new Intent(MmtGuardService.this, MmtMainService.class);

                        intent.putExtras(extras);

                        startService(intent);
                    }

////                    if (!isGisAlive || gisServer == null) {
//                    if (!isGisAlive) {
//                        BaseClassUtil.logi(MmtGuardService.this, "即将重启后台辅助进程GIS服务");
//
////                        bindService(new Intent(MmtGuardService.this, MmtGISService.class), connection, Context.BIND_AUTO_CREATE);
//                        Intent intent = new Intent(MmtGuardService.this, MmtGISService.class);
//
//                        intent.putExtras(extras);
//
//                        startService(intent);
//                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

//    public static IGisServer gisServer;
//
//    private ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            try {
//                BaseClassUtil.logi(MmtGuardService.this, "connect gis server");
//
//                gisServer = IGisServer.Stub.asInterface(service);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            BaseClassUtil.logi(MmtGuardService.this, "disconnect gis server");
//
//            gisServer = null;
//        }
//    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        worker.abort();
        worker = null;

//        unbindService(connection);

        super.onDestroy();
    }
}
