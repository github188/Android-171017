package com.mapgis.mmt.net.update;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.FileZipUtil;
import com.mapgis.mmt.config.CitySystemConfig;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.global.MmtNotificationManager;
import com.mapgis.mmt.net.multhreaddownloader.DownloadProgressListener;
import com.mapgis.mmt.net.multhreaddownloader.FileDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MmtUpdateService extends IntentService {
    /**
     * 处理中，包括下载处理中或者解压处理中
     */
    public static final int FLAG_LOADING = 1;

    /**
     * 下载完成，通知准备解压
     */
    public static final int FLAG_DOWNLOADED = 2;

    /**
     * 下载解压删除成功
     */
    public static final int FLAG_COMPLETE = 3;

    /**
     * 删除下载的压缩文件失败
     */
    public static final int FLAG_DEL_ZIP_FAILED = 4;

    /**
     * 未捕捉异常
     */
    public static final int FLAG_UNCATCH_EXCEPTION = -1;

    /**
     * 解压的文件夹重命名失败
     */
    public static final int FLAG_RENAME_FAILED = -2;

    Intent broadIntent = null;

    private String oper;

    public static boolean isRunning = false;

    public MmtUpdateService() {
        super(MmtUpdateService.class.getSimpleName());
    }

    DownloadProgressListener listener = new DownloadProgressListener() {
        private double radio = 0;

        @Override
        public void onStart() {
            broadIntent.putExtra("startTick", new Date().getTime());

            Notification notification = buildNotification("准备" + oper + "离线地图");

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(notifyID, notification);
        }

        @Override
        public void onLoading(double current, double total) {
            broadIntent.putExtra("what", FLAG_LOADING).putExtra("current", current).putExtra("total", total);

            sendBroadcast(broadIntent);

            int r = (int) (current / total * 100);

            if (Math.abs(r - radio) > 10) {
                Notification notification = buildNotification("正在" + oper + "离线地图：" + r + "%");

                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(notifyID, notification);

                this.radio = r;
            }
        }

        @Override
        public void onSuccess(File f) {
            Notification notification = buildNotification(oper + "离线地图成功");

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(notifyID, notification);
        }
    };

    private int notifyID;

    @Override
    public void onCreate() {
        super.onCreate();

        AppManager.addService(this);

        Notification notification = buildNotification("离线地图更新正在运行");

        notifyID = MmtNotificationManager.notify(notification);
    }

    public Notification buildNotification(String text) {

        Intent intent = new Intent(MyApplication.getInstance(), ActivityClassRegistry.getInstance().getActivityClass("登录界面"));
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent appIntent = PendingIntent.getActivity(MyApplication.getInstance().getApplicationContext(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(appIntent);
        builder.setSmallIcon(getApplicationInfo().icon);
        builder.setAutoCancel(false);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(text);

        Notification notification = builder.build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_SHOW_LIGHTS;
        notification.tickerText = getString(R.string.app_name) + "正在运行";
        return notification;
    }

    @Override
    public void onDestroy() {
        AppManager.removeService(this);

        mDownloaderList.clear();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notifyID);

        super.onDestroy();
    }

    private List<FileDownloader> mDownloaderList = new ArrayList<>();

    /**
     * 这里处理已经下载完成的地图文件，并拦截重复下载操作
     * 且这个操作是在线程之外进行的
     */
    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        // 这里处理当退出下载查看对话框，第二次进入的时候接着下载，不需要再开启线程，否则这里会重复下载
        boolean isStartThread = false;
        if (isRunning && intent != null) {
            String url = intent.getStringExtra("url");
            for (FileDownloader tempLoader : mDownloaderList) {
                if (tempLoader.getDownloadUrl().equals(url)) {
                    // 下载路径相同
                    if (tempLoader.isDownloadComplete()) {
                        // 判断文件是否下载完成
                        Intent tempIntent = new Intent(getClass().getName());
                        String mapName = intent.getStringExtra("mapName");
                        tempIntent.putExtra("mapName", mapName);

                        tempIntent.putExtra("what", FLAG_LOADING).putExtra("current", 100.00).putExtra("total", 100.00);
                        sendBroadcast(tempIntent);
                        tempIntent.putExtra("what", tempLoader.getWhat());
                        sendBroadcast(tempIntent);
                    }
                    isStartThread = true;
                }
            }
        }
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 先下载，后解压
     * 这里需要拦截上一次已经发出了下载的消息，但是Handler没有处理，且第二次发送消息依然会进入到消息队列中
     * 因此会重复下载
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        FileDownloader downloader = null;
        try {
            isRunning = true;

            this.broadIntent = new Intent(getClass().getName());

            String url = intent.getStringExtra("url");
            String path = intent.getStringExtra("path");
            String mapName = intent.getStringExtra("mapName");
            String serverTime = intent.getStringExtra("serverTime");
            int userID = intent.getIntExtra("userID", 0);

            // 01.开始下载
            /*
             * 这里再做一次拦截操作
             */
            for (FileDownloader tempLoader : mDownloaderList) {
                if (tempLoader.getDownloadUrl().equals(url)) {
                    return;
                }
            }
            downloader = new FileDownloader(url, new File(path), 1, serverTime);
            mDownloaderList.add(downloader);

            this.oper = "下载";
            this.broadIntent.putExtra("mapName", mapName);
            this.broadIntent.putExtra("preSize", downloader.downloadSize);
            File file;

            if (getString(R.string.listen_net_state_when_update).equals("true")) {
                IntentFilter filter = new IntentFilter();

                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

                registerReceiver(downloader, filter);

                file = downloader.download(listener);

                unregisterReceiver(downloader);
            } else {
                file = downloader.download(listener);
            }

            // 02.下载完成，准备解压
            broadIntent.putExtra("what", FLAG_DOWNLOADED);

            sendBroadcast(broadIntent);

            this.oper = "解压";
            this.broadIntent.putExtra("preSize", 0);

            // 03.开始解压
            FileZipUtil.unZip(file, path + mapName + "-unzip/", true, listener);

            // 04.解压完成，更新数据库
            CitySystemConfig configValue = new CitySystemConfig(mapName + "ModifyTime", serverTime, userID);

            DatabaseHelper.getInstance().update(CitySystemConfig.class, configValue.generateContentValues(),
                    "ConfigKey = '" + mapName + "ModifyTime'");

            // 05.处理文件夹，删除老的，更新新的
            File oldFile = new File(path + mapName);

            FileUtil.deleteFile(oldFile);

            boolean isOk = new File(path + mapName + "-unzip/").renameTo(oldFile);

            if (!isOk) {
                broadIntent.putExtra("what", FLAG_RENAME_FAILED);

                return;
            }

            isOk = file.delete();

            // 06.通知完成
            if (isOk) {
                broadIntent.putExtra("what", FLAG_COMPLETE);
            } else {
                broadIntent.putExtra("what", FLAG_DEL_ZIP_FAILED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            broadIntent.putExtra("what", FLAG_UNCATCH_EXCEPTION);
        } finally {
            isRunning = false;
            if (downloader != null) {
                downloader.setDownloadComplete(true);
                downloader.setWhat(broadIntent.getIntExtra("what", FLAG_UNCATCH_EXCEPTION));
            }

            if (broadIntent != null) {
                sendBroadcast(broadIntent);
            }

            /*int completeCount = 0;
            for (FileDownloader tempLoader : mDownloaderList) {
                // 如果所有的都下载完成，避免那种被拦截的下载消息，没有进入到looper队列导致IntentService成为一个普通的服务，没法杀死
                // 所以手动判断集合中的所有线程是否下载完成，如果完成就手动结束掉IntentService，结束服务
                if (tempLoader.isDownloadComplete()) {
                    completeCount++;
                }
            }
            if (completeCount == mDownloaderList.size()) {
                stopSelf();
                mDownloaderList.clear();
            }*/
        }
    }
}
