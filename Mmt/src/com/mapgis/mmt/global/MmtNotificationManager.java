package com.mapgis.mmt.global;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.mapgis.mmt.MyApplication;

public class MmtNotificationManager {
    public static int ID = 1000;

    public static synchronized int notify(Notification notification) {
        NotificationManager manager = (NotificationManager) MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(++ID, notification);

        return ID;
    }

    public static synchronized int notify(int id, Notification notification) {
        NotificationManager manager = (NotificationManager) MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(id, notification);

        return id;
    }

    public static void cancelAll() {
        NotificationManager manager = (NotificationManager) MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancelAll();
    }

    public static void cancel(int id) {
        NotificationManager manager = (NotificationManager) MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancel(id);
    }
}
