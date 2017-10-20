package com.mapgis.mmt.module.systemsetting.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by Comclay on 2017/4/26.
 * 下载广播接受者
 */

public class DownloadBroadcastReceiver extends BroadcastReceiver {
    public final static String ACTION_RECEIVER_UPDATE = "com.mapgis.mmt.downloadinfo.update";
    public final static String ACTION_RECEIVER_STOP = "com.mapgis.mmt.downloadinfo.stop";
    public final static String ACTION_RECEIVER_CANCEL = "com.mapgis.mmt.downloadinfo.cancel";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(DownloadService.EXTRA_DOWNLOAD_INFO)) {
            DownloadInfo info = intent.getParcelableExtra(DownloadService.EXTRA_DOWNLOAD_INFO);
            switch (intent.getAction()){
                case ACTION_RECEIVER_UPDATE:
                    if (listener != null) listener.onNotify(info);
                    break;
                case ACTION_RECEIVER_STOP:
                    if (listener != null) listener.onStopDownload(info);
                    break;
                case ACTION_RECEIVER_CANCEL:
                    if (listener!=null)listener.onCancelDownload(info);
                    break;
            }
        }
    }

    public static DownloadBroadcastReceiver registReceiver(Context context, String... action) {
        IntentFilter intentFilter = new IntentFilter();
        for (String temp : action){
            intentFilter.addAction(temp);
        }
        DownloadBroadcastReceiver receiver = new DownloadBroadcastReceiver();
        context.registerReceiver(receiver, intentFilter);
        return receiver;
    }

    public static DownloadBroadcastReceiver registReceiver(Context context, String action) {
        IntentFilter intentFilter = new IntentFilter(action);
        DownloadBroadcastReceiver receiver = new DownloadBroadcastReceiver();
        context.registerReceiver(receiver, intentFilter);
        return receiver;
    }

    public static void unregistReceiver(Context context, DownloadBroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    public static void sendBroadcast(Context context, DownloadInfo info, String action) {
        Intent intent = new Intent(action);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_INFO, info);
        context.sendBroadcast(intent);
    }

    private OnDownloadNotificationListener listener;

    public void setOnDownloadNotificationListener(OnDownloadNotificationListener listener) {
        this.listener = listener;
    }

    public abstract static class OnDownloadNotificationListener {
        void onNotify(DownloadInfo info){

        }

        void onStopDownload(DownloadInfo info){

        }

        void onCancelDownload(DownloadInfo info){

        }
    }
}
