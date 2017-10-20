package com.shortcutbadger.implement;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.shortcutbadger.Badger;
import com.shortcutbadger.BroadCastHelper;
import com.shortcutbadger.ShortCutBadgeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;


/**
 * @author zhouxixiang
 */
public class XiaomiHomeBadger implements Badger {

    public static final String INTENT_ACTION = "android.intent.action.APPLICATION_MESSAGE_UPDATE";
    public static final String EXTRA_UPDATE_APP_COMPONENT_NAME = "android.intent.extra.update_application_component_name";
    public static final String EXTRA_UPDATE_APP_MSG_TEXT = "android.intent.extra.update_application_message_text";

    private int DEFAULT_NOTIFYID = NOTIFYID_A;
    private final static int NOTIFYID_A = 333;
    private final static int NOTIFYID_B = 444;
    private static Class launcherClass;

    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortCutBadgeException {
        try {
            //小米官网m6到m9的做法
            //需要创建notification对象
            Intent intent = new Intent(context, launcherClass);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            PendingIntent appIntent = PendingIntent.getActivity(context, 0, intent, 0);

            Notification notifycation = new NotificationCompat.Builder(context)
                    .setSmallIcon(context.getApplicationInfo().icon)
                    .setContentText("您有新短消息")
                    .setAutoCancel(true)
                    .setContentIntent(appIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //小米系统每次的通知Id必须不同，否则不会显示

            notificationManager.cancel(DEFAULT_NOTIFYID);
            DEFAULT_NOTIFYID = DEFAULT_NOTIFYID == NOTIFYID_A ? NOTIFYID_B : NOTIFYID_A;

            Field field = notifycation.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notifycation);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, badgeCount);

            notificationManager.notify(DEFAULT_NOTIFYID, notifycation);

        } catch (Exception e) {
            //M6以下的做法
            Intent localIntent = new Intent(
                    INTENT_ACTION);
            localIntent.putExtra(EXTRA_UPDATE_APP_COMPONENT_NAME, componentName.getPackageName() + "/" + componentName.getClassName());
            localIntent.putExtra(EXTRA_UPDATE_APP_MSG_TEXT, String.valueOf(badgeCount == 0 ? "" : badgeCount));
            if (BroadCastHelper.canResolveBroadCast(context, localIntent)) {
                context.sendBroadcast(localIntent);
            }
        }
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList(
                "com.miui.miuilite",
                "com.miui.home",
                "com.miui.miuihome",
                "com.miui.miuihome2",
                "com.miui.mihome",
                "com.miui.mihome2",
                "com.i.miui.launcher"
        );
    }
    public void setLaucherClass(Class clazz){
        if(launcherClass==null){
            launcherClass=clazz;
        }

    }
}
