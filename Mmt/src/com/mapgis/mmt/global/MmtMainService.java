package com.mapgis.mmt.global;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.aidl.ICityMobile;
import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.FileZipUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.GlobalPathManager;
import com.mapgis.mmt.doinback.ReportInBackThread;
import com.mapgis.mmt.module.gps.CollectGPSTask;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.login.ServerConfigInfo;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.navigation.NavigationController;
import com.mapgis.mmt.module.systemsetting.SettingUtil;
import com.mapgis.mmt.receiver.NetPingReceiver;
import com.patrol.common.CheckArrivedTask;
import com.patrol.entity.KeyPoint;
import com.patrol.entity.TaskInfo;
import com.patrolproduct.server.PositionDetecterThread;
import com.zondy.mapgis.android.environment.InitEnv;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MmtMainService extends Service {

    private PowerManager.WakeLock wakeLock;
    // The name of UI process. MmtMainService process's name append a suffix ":main" to it.
    private volatile String uiProcessName;

    private NetPingReceiver mDelayReceiver;
    private WindowManager.LayoutParams mParams;
    private TextView mDelayView;
    private WindowManager mWindowManager;

    @Override
    public void onCreate() {
//        Debug.waitForDebugger();

        super.onCreate();

        BaseClassUtil.loge(this, "开始创建后台主服务onCreate");

        this.uiProcessName = getApplicationInfo().processName;
        AppManager.addService(this);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(getApplicationInfo().icon);
        builder.setAutoCancel(false);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText("后台服务正在运行");

        Notification notification = builder.build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_SHOW_LIGHTS;
        notification.tickerText = getString(R.string.app_name) + "正在运行";

        startForeground(888, notification);

        //创建PowerManager对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        //保持cpu一直运行，不管屏幕是否黑屏
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPUKeepRunning");

        wakeLock.acquire();
    }

    private float orgX;
    private float orgY;
    private float fingerX;
    private float fingerY;
    private float dx;
    private float dy;

    /*显示当前网络延时*/
    private void addNetDelayView(int delay) {
        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;// 系统提示window
        mParams.format = PixelFormat.TRANSLUCENT;// 支持透明
        //mParams.format = PixelFormat.RGBA_8888;
        mParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 焦点
        mParams.width = DimenTool.dip2px(this, 40);//窗口的宽和高
        mParams.height = DimenTool.dip2px(this, 40);
        mParams.gravity = Gravity.TOP | Gravity.LEFT;

        // 初始位置左上角
        Point point = getCurrentPosition();
        mParams.x = point.x;//窗口位置的偏移量
        mParams.y = point.y < DimenTool.dip2px(this, 50)
                ? mWindowManager.getDefaultDisplay().getHeight() / 3 : point.y;
        mParams.alpha = 0.9f;//窗口的透明度
        mDelayView = new TextView(this);
        mDelayView.setTextColor(Color.BLUE);
        setContent(delay);
        mDelayView.setGravity(Gravity.CENTER);
        mDelayView.setBackgroundResource(R.drawable.shape_net_delay_bg);
        mWindowManager.addView(mDelayView, mParams);
        mDelayView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        orgX = fingerX = event.getRawX();
                        orgY = fingerY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dx = event.getRawX() - fingerX;
                        dy = event.getRawY() - fingerY;

                        fingerX = event.getRawX();
                        fingerY = event.getRawY();

                        float tempX = mParams.x + dx;
                        float tempY = mParams.y + dy;

                        if (tempX < 0) tempX = 0;
                        else if (tempX + mDelayView.getWidth() > mWindowManager.getDefaultDisplay().getWidth()) {
                            tempX = mWindowManager.getDefaultDisplay().getWidth() - mDelayView.getWidth();
                        }
                        if (tempY < 0/*DimenTool.dip2px(MmtMainService.this, 50)*/)
                            tempY = 0/* DimenTool.dip2px(MmtMainService.this, 50)*/;
                        else if (tempY + mDelayView.getHeight() > mWindowManager.getDefaultDisplay().getHeight()) {
                            tempY = mWindowManager.getDefaultDisplay().getHeight() - mDelayView.getHeight();
                        }

                        mParams.x = (int) tempX;
                        mParams.y = (int) tempY;
                        mWindowManager.updateViewLayout(mDelayView, mParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        int minScroll = ViewConfiguration.get(MmtMainService.this).getScaledTouchSlop();
                        if (Math.abs(event.getRawY() - orgY) < minScroll
                                && Math.abs(event.getRawX() - orgX) < minScroll) {
                            showDelayToast();
                            moveTaskToFront();
                            break;
                        }
                        if (mParams.x < mWindowManager.getDefaultDisplay().getWidth() / 2) {
                            mParams.x = 0;
                        } else {
                            mParams.x = mWindowManager.getDefaultDisplay().getWidth() - mDelayView.getWidth();
                        }
                        mWindowManager.updateViewLayout(mDelayView, mParams);
                        saveCurrentPosition(mParams);
                        break;
                }
                return true;
            }
        });
    }

    private void moveTaskToFront() {
        try {
            ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfos = manager.getRunningTasks(20);

            String className = "";
            Intent intent = new Intent();

            for (ActivityManager.RunningTaskInfo task : taskInfos) {
                if (this.getPackageName().equals(task.topActivity.getPackageName())) {
                    System.out.println("后台  " + task.topActivity.getClassName());
                    className = task.topActivity.getClassName();
                    // 从后天返回到前台
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName(this, Class.forName(className)));
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    this.startActivity(intent);
                    return;
                }
            }
            // 没有运行就直接重新打开前台页面
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showDelayToast() {
        String trim = mDelayView.getText().toString().trim();
        String msg = "当前网络不可用";
        if (!"---".equals(trim)) {
            msg = "当前网络延迟" + trim + "ms";
        }
        MyApplication.getInstance().showMessageWithHandle(msg);
    }

    private void setContent(int delay) {
        Resources res = getResources();
        if (delay < 0 || delay > 5000) {
            mDelayView.setText("---");
            mDelayView.setTextColor(res.getColor(R.color.color_net_not_accessable));
            return;
        }
        mDelayView.setText(String.valueOf(delay));
        if (delay < 0 || delay >= 1000) {
            mDelayView.setTextColor(res.getColor(R.color.color_net_not_accessable));
        } else if (delay < 100) {
            mDelayView.setTextColor(res.getColor(R.color.color_net_best));
        } else if (delay < 300) {
            mDelayView.setTextColor(res.getColor(R.color.color_net_better));
        } else if (delay < 500) {
            mDelayView.setTextColor(res.getColor(R.color.color_net_good));
        } else if (delay < 1000) {
            mDelayView.setTextColor(res.getColor(R.color.color_net_bad));
        }
    }

    private void saveCurrentPosition(WindowManager.LayoutParams mParams) {
        SharedPreferences.Editor edit = MyApplication.getInstance().getSystemSharedPreferences().edit();
        edit.putString("netDelayViewPosition", String.format(Locale.CHINA, "%d,%d", mParams.x, mParams.y)).apply();
    }

    private Point getCurrentPosition() {
        SharedPreferences sp = MyApplication.getInstance().getSystemSharedPreferences();
        String str = sp.getString("netDelayViewPosition", "0,0");
        String[] split = str.split(",");
        return new Point(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
    }

    private ServerConfigInfo serverConfigInfo;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Debug.waitForDebugger();
        try {
            registNetDelayReceiver();

            ServerConfigInfo info = intent.getParcelableExtra("serverConfigInfo");
            BaseClassUtil.loge(this, "onStartCommand==>" + info + "==>" + (worker == null ? "初次" : "非初"));

            if (worker != null) {
                if (ServerConnectConfig.getInstance().getServerConfigInfo().equals(info))
                    BaseClassUtil.loge(this, "初始服务参数【相同】的后台服务已经在运行了");
                else {
                    BaseClassUtil.loge(this, "初始服务参数【不同】的后台服务已经在运行了");

                    stopSelf();

                    Intent newIntent = new Intent(this, MmtMainService.class);
                    newIntent.putExtras(intent);
                    startService(newIntent);
                }

                return START_REDELIVER_INTENT;
            }

            serverConfigInfo = info;

            ServerConnectConfig.getInstance().setInfo(info);

            ContentValues configMap = intent.getParcelableExtra("configMap");

            for (String name : configMap.keySet()) {
                String val = configMap.getAsString(name);

                switch (name) {
                    case "UserBean":
                        MyApplication.getInstance().putConfigValue(name, new Gson().fromJson(val, UserBean.class));
                        break;
                    default:
                        MyApplication.getInstance().putConfigValue(name, val);
                        break;
                }
            }

            worker = new Worker();

            AppManager.addThread(worker);

            worker.start();

            monitor = new MmtBaseThread() {
                @Override
                public void run() {
                    while (!isExit) {
                        try {
                            BaseClassUtil.logi(this.getName(), "监听前台进程状态：" + monitor.getState());
                            TimeUnit.SECONDS.sleep(10);
                            checkMainService();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            };

            monitor.start();
            monitor.setName(this.getClass().getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return START_REDELIVER_INTENT;
    }

    private void registNetDelayReceiver() {
        mDelayReceiver = new NetPingReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NetPingReceiver.ACTION_START);
        intentFilter.addAction(NetPingReceiver.ACTION_DELAY);
        intentFilter.addAction(NetPingReceiver.ACTION_CANCEL);
        registerReceiver(mDelayReceiver, intentFilter);
        mDelayReceiver.setCaculatedNetDelayListener(new NetPingReceiver.CaculatedNetDelayListener() {
            @Override
            public void onStart() {
                if (mNetDelayThread == null || mNetDelayThread.isExit) {
                    startCheckNetPing();
                }
            }

            @Override
            public void onNetDelay(int delay) {
                if (mNetDelayThread == null || mNetDelayThread.isExit) return;
                if (mDelayView == null) {
                    addNetDelayView(delay);
                    return;
                }
                setContent(delay);
                mWindowManager.updateViewLayout(mDelayView, mParams);
            }

            @Override
            public void onCancel() {
                synchronized (this) {
                    if (mNetDelayThread != null && !mNetDelayThread.isExit) {
                        mNetDelayThread.abort();
                        mNetDelayThread = null;
                    }
                }
                removeDelayView();
            }
        });
    }

    private void removeDelayView() {
        try {
            if (mDelayView == null) {
                return;
            }
            mWindowManager.removeView(mDelayView);
            mDelayView = null;
            mParams = null;
            mWindowManager = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregistNetDelayReceiver() {
        if (mDelayReceiver != null) {
            unregisterReceiver(mDelayReceiver);
        }
    }

    private MmtBaseThread monitor;

    public void checkMainService() {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // Check whether the UI process is alive.
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : manager.getRunningAppProcesses()) {
            if (runningAppProcessInfo.processName.equals(uiProcessName)) {
                return;
            }
        }
        new Handler(this.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    BaseClassUtil.logi(MmtMainService.this, "即将重启前台主界面进程");
                    NavigationController.restartApp(MmtMainService.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private CollectGPSTask collectGPSTask;
    private CheckArrivedTask checkArrivedTask;
    private PositionDetecterThread positionDetecterThread;
    private ReportInBackThread reportInBackThread;
    private GpsReceiver gpsReceiver;
    private CheckNetPingThread mNetDelayThread;

    private ScheduledExecutorService executorService;

    class Worker extends MmtBaseThread {
        private void ttsTask() {
            try {
                if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
                    return;

                String fontsPath = Environment.getExternalStorageDirectory() + "/MapGIS/Fonts";

                if (!new File(fontsPath + "/DroidSansFallback.ttf").exists()) {
                    FileUtil.copyAssetToSD("fonts/droid_sans_fallback.ttf", fontsPath + "/DroidSansFallback.ttf");
                }

                if (!new File(fontsPath + "/fonts.xml").exists()) {
                    FileUtil.copyAssetToSD("fonts/fonts.xml", fontsPath + "/fonts.xml");
                } else {
                    String fonts = new String(FileUtil.file2byte(new File(fontsPath + "/fonts.xml")), "UTF-8");

                    if (!fonts.contains("<fonts ttf=\"DroidSansFallback.ttf\">"))
                        FileUtil.copyAssetToSD("fonts/fonts.xml", fontsPath + "/fonts.xml");
                }

                if (new File(Environment.getExternalStorageDirectory() + "/MapGIS/Vlib").exists()) {
                    return;
                }

                String[] args = new String[]{"Clib", "Slib"};

                for (String arg : args) {
                    File file = new File(Environment.getExternalStorageDirectory() + "/MapGIS/" + arg);

                    if (file.exists()) {
                        boolean isOk = file.renameTo(new File(Environment.getExternalStorageDirectory() + "/MapGIS/" + arg + "_Zoro"));

                        if (!isOk)
                            throw new Exception("文件重命名失败");
                    }
                }

                InitEnv.InitEnv(MyApplication.getInstance());

                for (String arg : args) {
                    File file = new File(Environment.getExternalStorageDirectory() + "/MapGIS/" + arg + "_Zoro");

                    if (file.exists()) {
                        FileZipUtil.DeleteDir(new File(Environment.getExternalStorageDirectory() + "/MapGIS/" + arg));

                        boolean isOk = file.renameTo(new File(Environment.getExternalStorageDirectory() + "/MapGIS/" + arg));

                        if (!isOk)
                            throw new Exception("文件重命名失败");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * 加载七参数文件,首先监听GPS硬件,然后开启坐标上传线程
         */
        private void mainTask() {
            try {
                final CoordinateConvertor convertor = new CoordinateConvertor();

                UserBean bean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);

                if (bean != null && bean.isOffline) {
                    convertor.LoadTransParamsFromLocal(GlobalPathManager.getLocalConfigPath() + GlobalPathManager.TRANS_PARAMS_FILE);
                } else {
                    convertor.LoadTransParamsFromWeb(GlobalPathManager.TRANS_PARAMS_FILE);
                }

                new Handler(MmtMainService.this.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BaseClassUtil.loge(this, Thread.currentThread().getName());

                            if (SettingUtil.getConfig(SettingUtil.Config.PING_NET_DELAY, true)) {
                                startCheckNetPing();
                            }

                            //需要主线程运行
                            gpsReceiver = GpsReceiver.getInstance();
                            gpsReceiver.start(convertor);

                            positionDetecterThread = new PositionDetecterThread();
                            positionDetecterThread.start();

                            executorService = new ScheduledThreadPoolExecutor(2);

                            collectGPSTask = new CollectGPSTask();
                            collectGPSTask.start(executorService);

                            checkArrivedTask = new CheckArrivedTask();
                            checkArrivedTask.start(executorService);

                            reportInBackThread = ReportInBackThread.getInstance();
                            reportInBackThread.start();
                        } catch (Exception ex) {
                            ex.printStackTrace();

                            stopSelf();
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();

                stopSelf();
            }
        }

        @Override
        public void run() {
            try {
                ttsTask();

                mainTask();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void startCheckNetPing() {
        mNetDelayThread = new CheckNetPingThread();
        mNetDelayThread.start();
    }

    MmtBaseThread worker = null;

    @Override
    public void onDestroy() {
//        Debug.waitForDebugger();
        unregistNetDelayReceiver();
        removeDelayView();

        try {
            BaseClassUtil.loge(this, "销毁后台主服务onDestroy");

            monitor.abort();
            wakeLock.release();

            executorService.shutdownNow();

            collectGPSTask.stop();

            AppManager.finishProgram(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            super.onDestroy();
        }
    }

//    IConfigInfo.Stub iConfigInfo = new IConfigInfo.Stub() {
//        @Override
//        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
//
//        }
//
//        @Override
//        public ServerConfigInfo getConfigInfo() throws RemoteException {
//            return ServerConnectConfig.getInstance().getServerConfigInfo();
//        }
//    };

    ICityMobile.Stub iCityMobile = new ICityMobile.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            Toast.makeText(MmtMainService.this, "anInt:" + anInt, Toast.LENGTH_SHORT).show();
        }

        @Override
        public ServerConfigInfo getConfigInfo() throws RemoteException {
            return serverConfigInfo;
        }

        @Override
        public List<TaskInfo> getAllTaskBase() throws RemoteException {
            return checkArrivedTask.getTaskBaseList();
        }

        @Override
        public TaskInfo getTaskInfo(int id) throws RemoteException {
            return checkArrivedTask.getTaskInfo(id);
        }

        @Override
        public List<TaskInfo> refreshMyPlan() throws RemoteException {
            return checkArrivedTask.refreshMyPlan();
        }

        public void onKeyPointFeedback(int taskID, int keyPointID) throws RemoteException {
            checkArrivedTask.onKeyPointFeedback(taskID, keyPointID);
        }

        public void setTaskState(int id, String taskState) throws RemoteException {
            TaskInfo taskInfo = checkArrivedTask.getTaskInfo(id);

            if (taskInfo == null) {
                return;
            }

            taskInfo.TaskState = taskState;
        }

        @Override
        public List<KeyPoint> fetchPipeLines(int id, double xmin, double ymin, double xmax, double ymax) throws RemoteException {
            return checkArrivedTask.fetchPipeLines(id, xmin, ymin, xmax, ymax);
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
//        Debug.waitForDebugger();

        BaseClassUtil.loge(this, "开始绑定后台主服务onBind");

        return iCityMobile;
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        Debug.waitForDebugger();

        BaseClassUtil.loge(this, "onUnbind");

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
//        Debug.waitForDebugger();

        BaseClassUtil.loge(this, "onRebind");

        super.onRebind(intent);
    }

    @Override
    public void onLowMemory() {
//        Debug.waitForDebugger();

        BaseClassUtil.loge(this, "onLowMemory");

        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
//        Debug.waitForDebugger();

        BaseClassUtil.loge(this, "onTrimMemory");

        super.onTrimMemory(level);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
//        Debug.waitForDebugger();

        BaseClassUtil.loge(this, "onTaskRemoved");

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        Debug.waitForDebugger();

        BaseClassUtil.loge(this, "onConfigurationChanged");

        super.onConfigurationChanged(newConfig);
    }

    private final static int GRAY_SERVICE_ID = 1001;

    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());

            stopForeground(true);

            stopSelf();

            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }
}
