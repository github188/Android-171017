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
public class ApexHomeBadger implements Badger {

    private static final String INTENT_UPDATE_COUNTER = "com.anddoes.launcher.COUNTER_CHANGED";
    private static final String PACKAGENAME = "package";
    private static final String COUNT = "count";
    private static final String CLASS = "class";

    @Override
    public void executeBadge(Context context, ComponentName componentName, int badgeCount) throws ShortCutBadgeException {

        Intent intent = new Intent(INTENT_UPDATE_COUNTER);
        intent.putExtra(PACKAGENAME, componentName.getPackageName());
        intent.putExtra(COUNT, badgeCount);
        intent.putExtra(CLASS, componentName.getClassName());
        if (BroadCastHelper.canResolveBroadCast(context, intent)) {
            context.sendBroadcast(intent);
        } else {
            throw new ShortCutBadgeException("unable to resolve intent: " + intent.toString());
        }
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList("com.anddoes.launcher");
    }

    @Override
    public void setLaucherClass(Class clazz) {

    }

}
