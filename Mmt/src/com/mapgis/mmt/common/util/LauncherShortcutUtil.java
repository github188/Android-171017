package com.mapgis.mmt.common.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;

public class LauncherShortcutUtil {

    private static final String STRING_EMPTY = "";

    private LauncherShortcutUtil() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation.");
    }

    public static void addShortcut(@NonNull Context context, @NonNull Class<? extends Activity> launcherActivity,
                                   @NonNull String shortcutName, int iconResId, boolean allowDuplicate) {

        context = context.getApplicationContext();
        Intent launcherIntent = getLauncherIntent(context, launcherActivity);

        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(context, iconResId);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        intent.putExtra("duplicate", allowDuplicate);
        context.sendBroadcast(intent);
    }

    @NonNull
    private static Intent getLauncherIntent(Context context, Class<? extends Activity> launcherActivity) {
        Intent launcherIntent = new Intent(context, launcherActivity);
        launcherIntent.setAction(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return launcherIntent;
    }

    public static void deleteShortcut(@NonNull Context context,
                                      @NonNull Class<? extends Activity> launcherActivity,
                                      @NonNull String shortcutName) {

        context = context.getApplicationContext();
        Intent launcherIntent = getLauncherIntent(context, launcherActivity);

        Intent intent = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        context.sendBroadcast(intent);
    }

    public static boolean checkShortcutExist(@NonNull Context context, @NonNull String shortcutName) {

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {

            Uri queryUri = getLauncherQueryUri(context);
            cursor = resolver.query(queryUri, new String[]{"title"}, "title=?", new String[]{shortcutName}, null);
            return cursor != null && cursor.getCount() > 0;

        } catch (SecurityException e) {
            // Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return false;
    }

    private static Uri getLauncherQueryUri(Context context) {

        String authority = getDefaultProviderAuthority(context);

        if (TextUtils.isEmpty(authority)) {

            String requiredPermission = getCurrentLauncherPackageName(context) + ".permission.READ_SETTINGS";
            authority = getProviderAuthorityByPermission(context, requiredPermission);

            if (TextUtils.isEmpty(authority)) {
                int sdkInt = android.os.Build.VERSION.SDK_INT;
                if (sdkInt < 8) {
                    authority = "com.android.launcher.settings";
                } else if (sdkInt < 19) {
                    authority = "com.android.launcher2.settings";
                } else {
                    authority = "com.android.launcher3.settings";
                }
            }
        }
        String uriStr = "content://" + authority + "/favorites?notify=true";
        return Uri.parse(uriStr);
    }

    private static String getDefaultProviderAuthority(Context context) {
        return getProviderAuthorityByPermission(context, "com.android.launcher.permission.READ_SETTINGS");
    }

    private static String getProviderAuthorityByPermission(Context context, String requiredPermission) {
        List<PackageInfo> pkgInfos = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
        for (PackageInfo packageInfo : pkgInfos) {
            if (packageInfo.providers == null) {
                continue;
            }
            for (ProviderInfo providerInfo : packageInfo.providers) {
                if (requiredPermission.equals(providerInfo.readPermission)
                        || requiredPermission.equals(providerInfo.writePermission)) {
                    return providerInfo.authority;
                }
            }
        }
        return STRING_EMPTY;
    }

    private static String getCurrentLauncherPackageName(Context context) {
        Intent launcherIntent = new Intent();
        launcherIntent.setAction(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);

        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(launcherIntent, 0);
        if (resolveInfo == null || resolveInfo.activityInfo == null) {
            return STRING_EMPTY;
        }
        String packageName = resolveInfo.activityInfo.packageName;
        if (packageName.equals("android")) {
            return STRING_EMPTY;
        } else if (packageName.startsWith("com.google.android")) {
            // Compatibility for google instant desktop
            return "com.google.android.launcher";
        }
        return packageName;
    }
}
