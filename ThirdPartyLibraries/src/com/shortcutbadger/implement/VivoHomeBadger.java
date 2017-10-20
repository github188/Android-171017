package com.shortcutbadger.implement;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.shortcutbadger.Badger;
import com.shortcutbadger.ShortCutBadgeException;

import java.util.Arrays;
import java.util.List;


/**
 * @author zhouxixiang
 */
public class VivoHomeBadger implements Badger {

    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortCutBadgeException {
        Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
        intent.putExtra("packageName", context.getPackageName());
        intent.putExtra("className", componentName.getClassName());
        intent.putExtra("notificationNum", badgeCount);
        context.sendBroadcast(intent);
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList("com.vivo.launcher");
    }

    @Override
    public void setLaucherClass(Class clazz) {

    }
}
