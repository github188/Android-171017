package com.mapgis.mmt.module.systemsetting.download;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.mapgis.mmt.global.MmtNotificationManager;

import java.util.Locale;

/**
 * Created by Comclay on 2017/5/3.
 * 下载时的通知
 */

public class DownloadNotification {
    private Context mContext;
    private NotificationCompat.Builder mBuilder;
    private static int id = 0;

    public DownloadNotification(Context context) {
        this.mContext = context;
        this.mBuilder = new NotificationCompat.Builder(context);
        if (id < MmtNotificationManager.ID){
            id = MmtNotificationManager.ID++;
        }
    }

    public void init(DownloadInfo info) {
        mBuilder.setContentTitle(String.format(Locale.CHINA, "%s(%s)-%s"
                , info.getPrefix(), DownloadType.typeToDescription(info.mMimeType)
                , info.getFormatSize()))
                .setContentText(info.getDownloadSpeed())
                .setSmallIcon(mContext.getApplicationInfo().icon)
                .setProgress(100, info.getDownloadRatioSize(), false)
                .setContentIntent(getPendingIntent(mContext))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentInfo(info.getDownloadRatioPercent())
                .setAutoCancel(true)
                .setTicker("开始下载");
        Notification notification = mBuilder.build();
        MmtNotificationManager.notify(id, notification);
    }

    public void update(DownloadInfo info) {
        if (info.mStatus == Downloads.STATUS_CANCELED) {
            cancel();
            return;
        }

        if (info.mCurrentBytes == 0L && info.mStatus != Downloads.STATUS_RUNNING
                || Downloads.isUnzipSuccessed(info.mStatus)) {
            mBuilder.setContentText(Downloads.statusToString(info.mStatus))
                    .setProgress(0, 0, false)
                    .setContentInfo(null);
        } else if (info.mControl == Downloads.CONTROL_PAUSED) {
            mBuilder.setContentText(info.getFormatDownloadSize()+"-"+info.getStatusText())
                    .setProgress(100, info.getDownloadRatioSize(), false)
                    .setContentInfo(info.getDownloadRatioPercent());
        } else {
            mBuilder.setContentText(info.getDownloadSpeed())
                    .setProgress(100, info.getDownloadRatioSize(), false)
                    .setContentInfo(info.getDownloadRatioPercent());
        }

        Notification notification = mBuilder.build();
        notification.flags |= NotificationCompat.FLAG_NO_CLEAR;
        MmtNotificationManager.notify(id, notification);
    }

    public void cancel() {
        MmtNotificationManager.cancel(id);
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, DownloadActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

//        PendingIntent.getActivity()
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addParentStack(DownloadActivity.class);
//        stackBuilder.addNextIntent(intent);
//        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        return PendingIntent.getActivity(context
                , 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
