package com.shortcutbadger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/8/8 0008.
 */

public class BroadCastHelper {
    public static boolean canResolveBroadCast(Context context, Intent intent){
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryBroadcastReceivers(intent, 0);
        return resolveInfos!=null&&resolveInfos.size()>0;
    }
}
