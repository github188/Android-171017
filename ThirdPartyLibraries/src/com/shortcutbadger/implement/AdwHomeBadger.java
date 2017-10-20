package com.shortcutbadger.implement;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


import com.shortcutbadger.Badger;
import com.shortcutbadger.BroadCastHelper;
import com.shortcutbadger.ShortCutBadgeException;

import java.util.Arrays;
import java.util.List;




/**
 * @author zhouxixiang
 */
public class AdwHomeBadger implements Badger {

    public static final String INTENT_UPDATE_COUNTER = "org.adw.launcher.counter.SEND";
    public static final String PACKAGENAME = "PNAME";
    public static final String CLASSNAME = "CNAME";
    public static final String COUNT = "COUNT";

    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortCutBadgeException {

        Intent intent = new Intent(INTENT_UPDATE_COUNTER);
        intent.putExtra(PACKAGENAME, componentName.getPackageName());
        intent.putExtra(CLASSNAME, componentName.getClassName());
        intent.putExtra(COUNT, badgeCount);
        if (BroadCastHelper.canResolveBroadCast(context, intent)) {
            context.sendBroadcast(intent);
        } else {
            throw new ShortCutBadgeException("unable to resolve intent: " + intent.toString());
        }
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList(
                "org.adw.launcher",
                "org.adwfreak.launcher"
        );
    }

    @Override
    public void setLaucherClass(Class clazz) {

    }

}
