package com.shortcutbadger;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import com.shortcutbadger.implement.AdwHomeBadger;
import com.shortcutbadger.implement.ApexHomeBadger;
import com.shortcutbadger.implement.AsusHomeBadger;
import com.shortcutbadger.implement.DefaultBadger;
import com.shortcutbadger.implement.EverythingMeHomeBadger;
import com.shortcutbadger.implement.HuaweiHomeBadger;
import com.shortcutbadger.implement.LGHomeBadger;
import com.shortcutbadger.implement.NewHtcHomeBadger;
import com.shortcutbadger.implement.NovaHomeBadger;
import com.shortcutbadger.implement.OPPOHomeBader;
import com.shortcutbadger.implement.SamsungHomeBadger;
import com.shortcutbadger.implement.SonyHomeBadger;
import com.shortcutbadger.implement.VivoHomeBadger;
import com.shortcutbadger.implement.XiaomiHomeBadger;
import com.shortcutbadger.implement.ZTEHomeBadger;
import com.shortcutbadger.implement.ZukHomeBadger;

import java.util.LinkedList;
import java.util.List;


/**
 * @author zhouxixiang
 */

public class ShortCutBadger {

    private static final List<Class<? extends Badger>> BADGERS = new LinkedList<>();
    private static final String LOG_TAG = "ShortCutBadger";

    static {
        BADGERS.add(AdwHomeBadger.class);
        BADGERS.add(LGHomeBadger.class);
        BADGERS.add(XiaomiHomeBadger.class);
        BADGERS.add(ApexHomeBadger.class);
        BADGERS.add(NewHtcHomeBadger.class);
        BADGERS.add(NovaHomeBadger.class);
        BADGERS.add(SonyHomeBadger.class);
        BADGERS.add(AsusHomeBadger.class);
        BADGERS.add(HuaweiHomeBadger.class);
        BADGERS.add(OPPOHomeBader.class);
        BADGERS.add(SamsungHomeBadger.class);
        BADGERS.add(ZukHomeBadger.class);
        BADGERS.add(VivoHomeBadger.class);
        BADGERS.add(ZTEHomeBadger.class);
        BADGERS.add(EverythingMeHomeBadger.class);
    }

    private static Badger sShortCutBadger;
    private static ComponentName sComponentName;
    private static Class xiaomiLauncherClass;


    public static boolean removeCount(Context context) {
        try {
            removeCountOrThrow(context);
            return true;
        } catch (ShortCutBadgeException e) {
            return false;
        }
    }

    public static void removeCountOrThrow(Context context) throws ShortCutBadgeException {
        applyCountOrThrow(context, 0);
        //这里如果有通知的话，把通知都清理掉
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    public static boolean applyCount(Context context, int badgeCount) {
        try {
            if (badgeCount <= 0) {
                removeCount(context);
            } else {
                applyCountOrThrow(context, badgeCount);
            }
            return true;
        } catch (ShortCutBadgeException e) {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                Log.d(LOG_TAG, "unable to execute badge", e);
            }
            return false;
        }
    }

    public static void applyCountOrThrow(Context context, int badgeCount) throws ShortCutBadgeException {
        if (sShortCutBadger == null) {
            //初始化过程中至少需要准备badger对象，以及componentName
            boolean laucherReady = initBadger(context);
            if (!laucherReady) {
                throw new ShortCutBadgeException("no default laucher available");
            }
        }
        try {

            if (xiaomiLauncherClass != null) {
                sShortCutBadger.setLaucherClass(xiaomiLauncherClass);
            }
            sShortCutBadger.executeBadge(context, sComponentName, badgeCount);
        } catch (Exception e) {
            throw new ShortCutBadgeException("unable to execute badge", e);
        }
    }

    private static boolean initBadger(Context context) {
        Intent laucherIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (laucherIntent == null) {
            Log.e(LOG_TAG, "unable to find laucher intent from package" + context.getPackageName());
            return false;
        }
        sComponentName = laucherIntent.getComponent();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfo == null || resolveInfo.activityInfo.name.toLowerCase().contains("resolver")) {
            return false;
        }
        String currentHomePackage = resolveInfo.activityInfo.packageName;
        for (Class<? extends Badger> badger : BADGERS) {
            Badger shortCutBadger = null;
            try {
                shortCutBadger = badger.newInstance();
            } catch (Exception ignore) {
            }
            if (shortCutBadger != null && shortCutBadger.getSupportLaunchers().contains(currentHomePackage)) {
                sShortCutBadger = shortCutBadger;
                break;
            }
        }
        if (sShortCutBadger == null) {
            if (Build.MANUFACTURER.equalsIgnoreCase("ZUK")) {
                sShortCutBadger = new ZukHomeBadger();
            } else if (Build.MANUFACTURER.equalsIgnoreCase("OPPO")) {
                sShortCutBadger = new OPPOHomeBader();
            } else if (Build.MANUFACTURER.equalsIgnoreCase("VIVO")) {
                sShortCutBadger = new VivoHomeBadger();
            } else if (Build.MANUFACTURER.equalsIgnoreCase("ZTE")) {
                sShortCutBadger = new ZTEHomeBadger();
            } else {
                sShortCutBadger = new DefaultBadger();
            }

        }
        return true;
    }

    private ShortCutBadger() {
    }

    public static void setXiaomiLaucherClass(Class clazz) {
        xiaomiLauncherClass = clazz;
    }
}
