package com.mapgis.mmt.module.navigation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnLeftButtonClickListener;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.config.CitySystemConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.global.MmtGuardService;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.module.login.ServerConfigInfo;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.systemsetting.DownloadMap;
import com.mapgis.mmt.module.systemsetting.DownloadMapTool;
import com.mapgis.mmt.module.systemsetting.download.DownloadManager;
import com.mapgis.mmt.net.update.DownloadFragment;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NavigationController {

    /**
     * 判断是否含有配置的离线地图文档，若不存在或者需要更新，则下载
     *
     * @param activity
     */
    public static void initMapFile(final BaseActivity activity) {
        new AsyncTask<Void, Void, List<DownloadMap>>() {

            @Override
            protected List<DownloadMap> doInBackground(Void... params) {

                List<DownloadMap> downloadMaps = DownloadMapTool.initMapUpdateInfos();

                return downloadMaps;
            }

            @Override
            protected void onPostExecute(List<DownloadMap> result) {
                try {
                    if (result == null || result.size() == 0) {
                        return;
                    }

                    ArrayList<CitySystemConfig> promptTimeConfig = DatabaseHelper.getInstance().query(CitySystemConfig.class,
                            "ConfigKey = 'MapPromptTime'");

                    // 距离上次点击不再提示的时间,小于7天,则不提示
                    if (promptTimeConfig != null && promptTimeConfig.size() != 0
                            && (System.currentTimeMillis() - Long.valueOf(promptTimeConfig.get(0).ConfigValue) < 7 * 24 * 60 * 60 * 1000)) {
                        return;
                    }

                    final List<DownloadMap> needDownMaps = new ArrayList<>();

                    for (DownloadMap downloadMap : result) {
                        if (downloadMap.hasNew) {
                            needDownMaps.add(downloadMap);
                        }
                    }

                    if (needDownMaps.size() == 0) {
                        return;
                    }

                    OkCancelDialogFragment fragment = new OkCancelDialogFragment("您有地图文件需要更新");

                    fragment.setLeftBottonText("下次再说");
                    fragment.setRightBottonText("立即更新");

                    fragment.setOnLeftButtonClickListener(new OnLeftButtonClickListener() {
                        @Override
                        public void onLeftButtonClick(View view) {
                            CitySystemConfig citySystemConfig = new CitySystemConfig("MapPromptTime", System.currentTimeMillis() + "",
                                    MyApplication.getInstance().getUserId());
                            DatabaseHelper.getInstance().insert(citySystemConfig);
                        }
                    });

                    fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
                        @Override
                        public void onRightButtonClick(View view) {

                            DownloadFragment downloadFragment = DownloadFragment.newInstance(needDownMaps);
                            downloadFragment.show(activity.getSupportFragmentManager(), "");
                        }
                    });

                    fragment.show(activity.getSupportFragmentManager(), "");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }.executeOnExecutor(MyApplication.executorService);
    }

    /**
     * 判断是否含有配置的离线地图文档，若不存在或者需要更新，则下载
     */
    public static void checkMobileFileUpdate(BaseActivity activity) {
        DownloadManager.getInstance().checkMobileFileUpdate(activity, false);
    }

    public static void exitAppSilent(Context context) {
        try {
            if (!MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).isOffline) {
                MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).isOffline = true;
                MyApplication.getInstance().getSystemSharedPreferences().edit()
                        .putBoolean("isWorkTime", MyApplication.getInstance().getConfigValue("defaultWorkTime", 1) > 0).commit();

                MapView mapView = MyApplication.getInstance().getConfigValue("MapView", MapView.class);

                if (mapView != null && mapView.getDispRange() != null) {
                    MyApplication.getInstance().getSystemSharedPreferences().edit()
                            .putString("preDispRange", new Gson().toJson(mapView.getDispRange())).commit();
                }
                new MmtBaseTask<Void, Void, Void>(context) {
                    @Override
                    protected Void doInBackground(Void... params) {
                        NetUtil.executeHttpGet(ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/UserLogOut", "userID", String.valueOf(MyApplication.getInstance().getUserId()), "deviceid", MyApplication.getInstance().getConfigValue("deviceid"));
                        return null;
                    }
                }.executeOnExecutor(MyApplication.executorService);
                //为调用退出服务预留500ms，已确保调用成功
                Thread.sleep(500);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            AppManager.finishProgram();
        }
    }

    public static void exitApp(Context context) {
        MyApplication.getInstance().showMessageWithHandle("正在退出,请稍候...");

        //用户主动退出APP时，结束后台辅助进程
        context.stopService(new Intent(context, MmtGuardService.class));
        context.stopService(new Intent(context, MmtMainService.class));
//        context.stopService(new Intent(context, MmtGISService.class));

        exitAppSilent(context);
    }

    private static void restart(Context context) {

        ServerConfigInfo info = ServerConnectConfig.getInstance().getServerConfigInfo();

        SharedPreferences preferences = MyApplication.getInstance().getSystemSharedPreferences();
        info.LoginName = preferences.getString("userName", MyApplication.getInstance().getString(R.string.login_default_user_name));
        info.LoginPassword = preferences.getString("password", context.getString(R.string.login_default_password));

        Intent intent = new Intent();
        intent.setClass(MyApplication.getInstance(), ActivityClassRegistry.getInstance().getActivityClass("登录界面"));

        intent.putExtra("Restart_ServerConfigInfo", info);
        intent.putExtra("tip", "正在重新登陆,请稍候...");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getInstance().getApplicationContext(),
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) MyApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pendingIntent);
    }

    public static void restartApp(Context context) {
        if (MyApplication.getInstance().getConfigValue("AutoRestart", 0) == 1 && !hasCrashedInLastSeconds()) {
            setLastCrashTimestamp(System.currentTimeMillis());
            restart(context);
        }
    }

    /**
     * INTERNAL method that tells if the app has crashed in the last seconds.
     * This is used to avoid restart loops.
     */
    private static boolean hasCrashedInLastSeconds() {
        long lastCrashedTime = getLastCrashTimestamp();
        long currentTime = System.currentTimeMillis();
        return ((currentTime > lastCrashedTime)
                && (currentTime - lastCrashedTime < TimeUnit.SECONDS.toMillis(RESTART_MIN_INTERVAL)));
    }

    private static void setLastCrashTimestamp(long timestamp) {
        MyApplication.getInstance().getSystemSharedPreferences()
                .edit().putLong(SP_CRASH_FIELD_TIMESTAMP, timestamp).apply();
    }

    private static long getLastCrashTimestamp() {
        return MyApplication.getInstance().getSystemSharedPreferences()
                .getLong(SP_CRASH_FIELD_TIMESTAMP, -1);
    }

    // Shared preferences field
    private static final String SP_CRASH_FIELD_TIMESTAMP = "last_crash_timestamp";
    // Restart time interval. Unit: second.
    private static final int RESTART_MIN_INTERVAL = 5;
}
