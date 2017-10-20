package com.mapgis.mmt;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.BatteryReceiver;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.widget.customview.ToastView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.global.MmtGuardService;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.navigation.NavigationController;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.patrolproduct.server.OnAllInsiteExec;
import com.zondy.mapgis.android.environment.InitEnv;

import java.io.File;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyApplication extends MultiDexApplication {
    public static ExecutorService executorService;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    protected String mediaPath;
    protected String recordPath;
    protected String userImagePath;

    public ImageLoader imageLoader;

    protected static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    public BMapManager mBMapManager = null;

    public OnAllInsiteExec getOnAllInsiteExec() {
        return null;
    }

    /**
     * 开启巡检计划反馈上报线程
     */
    public void Start(MapGISFrame mapGISFrame) {
        try {
            imageLoader = new ImageLoader(Volley.newRequestQueue(this), CacheUtils.getInstance(this));

            this.mapGISFrame = mapGISFrame;

            String receiver = getConfigValue("GpsReceiver");

            ServerConnectConfig.getInstance().setGpsReceiver(receiver);

            startMmtMainService();

            /**GPS接收内置于后台进程中，主进程只负责接收后台进程的广播来获取GPS信息**/
            if ((receiver.equalsIgnoreCase("Random") || receiver.equalsIgnoreCase("RD"))
                    && TextUtils.isEmpty(getConfigValue("RandomGPS")))
                putConfigValue("RandomGPS", "view");

            putConfigValue("GpsReceiver", "BC");

            GpsReceiver.getInstance().start(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 开启后台服务，运行于新进程main中,同时开启后台守护服务
     */
    private void startMmtMainService() {
        Intent intent = new Intent(this, MmtMainService.class);

        ContentValues configMap = new ContentValues();

        for (Hashtable.Entry<String, Object> kv : configHashtable.entrySet()) {
            if (kv.getValue() instanceof String)
                configMap.put(kv.getKey(), (String) kv.getValue());
            else
                configMap.put(kv.getKey(), kv.getValue().toString());
        }

        intent.putExtra("configMap", configMap);
        intent.putExtra("serverConfigInfo", ServerConnectConfig.getInstance().getServerConfigInfo());

        startService(intent);

        Intent guardIntent = new Intent(this, MmtGuardService.class);

        guardIntent.putExtras(intent);

        startService(guardIntent);
    }

    @Override
    public void onCreate() {
        try {
            super.onCreate();

            if (isInstallCanary()) {
                if (initLeakCanary()) {
                    return;
                }
            }

            instance = this;

            CrashHandler.getInstance().init(this);

//            // 禁止屏幕旋转
//            Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);

            // 注册电量变化广播
            registerReceiver(BatteryReceiver.getInstance(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            // android2.3以上版本默认不允许在主线程中使用耗时的操作（如网络请求）
            if (Float.valueOf(VERSION.RELEASE.substring(0, 3).trim()) > 2.3) {
                StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()));
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                        .build());
            }

            executorService = Executors.newCachedThreadPool();

            systemSharedPreferences = getSharedPreferences("CivSystemSetter", 0);

            if (!systemSharedPreferences.contains("AppModifyTime")) {// 设置APP首次运行时间
                Editor editor = systemSharedPreferences.edit();
                editor.putString("AppModifyTime", BaseClassUtil.getSystemTime());
                editor.apply();
            }

            initFilePath();

            DatabaseHelper.getInstance().initDB(this);

            if (!BaseClassUtil.isMainProcess(this))
                return;

            initImageLoader(this);

            ServerConnectConfig.getInstance().loadLoginInfo(this, false);

            if (FileUtil.isDeviceStorageLow()) {   // 内存不足，清理文件
                submitExecutorService(new Runnable() {
                    @Override
                    public void run() {
                        FileUtil.deleteMediaCache();
                    }
                });
            }

//            startService(new Intent(this, MmtGISService.class));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.e(this.toString(), "====>>>onCreate");
        }

        //initEngineManager(this);
    }

    protected boolean isInstallCanary() {
        return true;
    }

    protected boolean initLeakCanary() {
        return false;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(this.getClass().getSimpleName(), "====>>>onLowMemory");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(this.getClass().getSimpleName(), "====>>>onConfigurationChanged");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e(this.getClass().getSimpleName(), "====>>>onTerminate");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.e(this.getClass().getSimpleName(), "====>>>onTrimMemory");
    }

    private void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by ImageLoaderConfiguration.createDefault(this) method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        // config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config.build());
    }

    public void initEnv() {
        InitEnv.InitEnv(this);
    }

    private void initFilePath() {
        if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return;

        try {
            mapFilePath = Battle360Util.doWork();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!new File(Environment.getExternalStorageDirectory() + "/MapGIS/").exists()) {
            initEnv();
        }

        mediaPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Media);
        File file = new File(mediaPath);
        if (!file.exists() && !file.mkdirs()) {
            Log.e(getClass().getSimpleName(), "创建目录[" + mediaPath + "]失败");
        }

        recordPath = Battle360Util.getFixedPath("Record");
        File recordFile = new File(recordPath);
        if (!recordFile.exists() && !recordFile.mkdirs()) {
            Log.e(getClass().getSimpleName(), "创建目录[" + recordPath + "]失败");
        }

        userImagePath = Battle360Util.getFixedPath("UserImage");
        File userImageFile = new File(userImagePath);
        if (!userImageFile.exists() && !userImageFile.mkdirs()) {
            Log.e(getClass().getSimpleName(), "创建目录[" + userImagePath + "]失败");
        }

        File tempDir = new File(Battle360Util.getFixedPath("Temp"));
        if (!tempDir.exists() && !tempDir.mkdirs())
            Log.e(getClass().getSimpleName(), "创建目录[" + tempDir.getAbsolutePath() + "]失败");

    }

    private String mapFilePath;

    public String getMapFilePath() {
        return mapFilePath;
    }

    protected SharedPreferences systemSharedPreferences;

    public SharedPreferences getSystemSharedPreferences() {
        return systemSharedPreferences;
    }

    public void startActivityAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void finishActivityAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public String getMediaPathString() {
        return mediaPath;
    }

    public String getRecordPathString() {
        return recordPath;
    }

    public <T> Future<T> submitExecutorService(Callable<T> task) {
        return executorService.submit(task);
    }

    public Future<?> submitExecutorService(Runnable task) {
        return executorService.submit(task);
    }

    public void executeSingleExecutorService(Runnable task) {
        singleThreadExecutor.execute(task);
    }

    public int getUserId() {
        UserBean userBean = getUserBean();

        return userBean != null ? userBean.UserID : -1;
    }

    public UserBean getUserBean() {
        return getConfigValue("UserBean", UserBean.class);
    }

    /**
     * ********************运行时需要的键值对类型的配置表（开始）**********************************
     */
    private final Hashtable<String, Object> configHashtable = new Hashtable<>();

    public Boolean containsKey(String key) {
        return configHashtable.containsKey(key);
    }

    public void putConfigValue(String key, Object value) {
        this.configHashtable.put(key, value);
    }

    public long getConfigValue(String key, long defaultValue) {
        try {
            String val = getConfigValue(key);

            if (!TextUtils.isEmpty(val)) {
                return Long.parseLong(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    public boolean getConfigValue(String key, boolean defaultValue) {
        try {
            String val = getConfigValue(key);

            if (!TextUtils.isEmpty(val)) {
                if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false"))
                    return Boolean.parseBoolean(val);
                else
                    return Long.parseLong(val) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    public <T> T getConfigValue(String key, Class<T> classOfT) {
        try {
            if (classOfT == null)
                return null;

            return (T) configHashtable.get(key);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public String getConfigValue(String key) {
        try {
            if (configHashtable.containsKey(key)) {
                return String.valueOf(configHashtable.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 清除操作，目前是清除配置表
     */
    public void clear() {
        configHashtable.clear();
    }

    // ********************运行时需要的键值对类型的配置表（结束）**************************************

    public static final int SHOW_MESSAGE = 1;
    public static final int SHOW_MESSAGE_AND_VIEW = 2;
    public static final int RESTART_APP = 3;
    public static final int SHOW_MESSAGE_AND_VIEW_INTIME = 4;

    private ToastView toast;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_MESSAGE:
                    Toast.makeText(MyApplication.getInstance(), String.valueOf(msg.obj), Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_MESSAGE_AND_VIEW:
                    toast = new ToastView(getApplicationContext(), String.valueOf(msg.obj));
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setLongTime(1000);
                    toast.show();
                    break;
                case RESTART_APP:
                    NavigationController.restartApp(MyApplication.getInstance());
                    break;
                case SHOW_MESSAGE_AND_VIEW_INTIME:
                    toast = new ToastView(getApplicationContext(), String.valueOf(msg.obj));
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setLongTime(msg.arg1);
                    toast.show();
                    break;
                default:
                    break;
            }
        }
    };

    public void showMessageWithHandle(String msg) {
        handler.obtainMessage(SHOW_MESSAGE, msg).sendToTarget();
    }

    /**
     * toastLength ： Toast 显示的 时长， 单位：毫秒
     */
    public void showMessageAndViewWithHandle(String msg, int toastLength) {
        handler.obtainMessage(SHOW_MESSAGE_AND_VIEW_INTIME, toastLength, 0, msg).sendToTarget();
    }

    public MapGISFrame mapGISFrame;

    public void sendToBaseMapHandle(BaseMapCallback callback) {
        if (mapGISFrame == null)
            return;

        callback.setMapGISFrame(mapGISFrame);

        new Handler(mapGISFrame.getMainLooper(), callback).sendEmptyMessage(0);
    }

    public void clearCache() {
        try {
            SharedPreferences preferences = getSharedPreferences("MyPlanList", 0);
            String userId = String.valueOf(MyApplication.getInstance().getUserId());
            if (preferences != null && preferences.contains(userId)) {
                Editor editor = preferences.edit();
                editor.remove(userId);
                editor.apply();
            }

            preferences = getSharedPreferences("PatrolEquipment", 0);
            if (preferences != null) {
                preferences.edit().clear().apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Battle360Util.clearTileCache();

        BaseClassUtil.getACache().clear();

        Toast.makeText(getApplicationContext(), "清理完成", Toast.LENGTH_SHORT).show();
    }

    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(MyApplication.getInstance().getApplicationContext(), "BMapManager  初始化错误!",
                    Toast.LENGTH_LONG).show();
        }
        Log.d("ljx", "initEngineManager");
    }

    public static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
//                Toast.makeText(MyApplication.getInstance().getApplicationContext(),
//                        "请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: " + iError, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MyApplication.getInstance().getApplicationContext(), "key认证成功", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    /*判断是否有连接大华的权限*/
    public boolean hasConnDHPermission() {
        UserBean userBean = getUserBean();
        return userBean != null && !BaseClassUtil.isNullOrEmptyString(userBean.Role)
                && userBean.Role.contains("大华视频");
    }

}

