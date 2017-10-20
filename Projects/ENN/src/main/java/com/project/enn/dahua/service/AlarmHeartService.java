package com.project.enn.dahua.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.business.client.msp.CmuClient;
import com.android.business.client.msp.McuClient;
import com.android.business.client.msp.PccClient;
import com.android.business.client.msp.SDKExceptionDefine;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.project.enn.dahua.AlarmHeartWakeReceiver;
import com.project.enn.dahua.Constant;
import com.project.enn.dahua.DaHuaBroadcastReceiver;
import com.project.enn.dahua.IDaHuaService;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * Created by Comclay on 2017/4/13.
 * 报警心跳应用程序
 */

public class AlarmHeartService extends Service {
    private static final String TAG = "AlarmHeartService";
    private final static Object mObjLock = new Object();
    private final static int HEART_INTERVAL = 120000;
    public static boolean isRun = false;
    private AlarmHeartWakeReceiver mWakeReceiver;

    private DaHuaBroadcastReceiver mReceiver;
    private String mLoginName;
    private String mLoginPassword;

    @Override
    public void onCreate() {
        super.onCreate();
        AppManager.addService(this);
        MyApplication.getInstance().putConfigValue("DHLoginState", 1);
        startHeartThread();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mReceiver = DaHuaBroadcastReceiver.registerReceiver(this);
        mReceiver.setOnDaHuaSignalReceivedListener(new DaHuaBroadcastReceiver.OnDaHuaSignalReceivedListener() {
            @Override
            protected void onSipRegistSuccessSignal() {
                notifyHeartThread();
            }
        });

        mWakeReceiver = AlarmHeartWakeReceiver.registerReceiver(this);
        mWakeReceiver.setOnWakeHeartListener(new AlarmHeartWakeReceiver.OnWakeHeartListener() {
            @Override
            public void onWake() {
                notifyHeartThread();
            }
        });

        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        DaHuaBroadcastReceiver.unregistReceiver(this, mReceiver);
        AlarmHeartWakeReceiver.unregistReceiver(this, mWakeReceiver);
        stopHeartThread();
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int onStartCommand = super.onStartCommand(intent, flags, startId);

        if (intent != null && intent.hasExtra(DaHuaService.DAHUA_LOGIN_NAME)) {
            mLoginName = intent.getStringExtra(DaHuaService.DAHUA_LOGIN_NAME);
            mLoginPassword = intent.getStringExtra(DaHuaService.DAHUA_LOGIN_PASSWORD);
            if (BaseClassUtil.isNullOrEmptyString(mLoginName)
                    || BaseClassUtil.isNullOrEmptyString(mLoginPassword)) {
                sendErrSignalBroadcast(DaHuaService.LOGIN_FAILED, SDKExceptionDefine.CheckUserPwdFaild.getCode());
                return onStartCommand;
            }
        }
        synchronized (mObjLock) {
            if (isWait && isRun) {
                mObjLock.notify();
            }
        }
        return onStartCommand;
    }

    public void sendErrSignalBroadcast(String signal, int errCode) {
        Log.w(TAG, "sendSignalBroadCast: 发送广播信号" + signal + "错误信息" + SDKExceptionDefine.getMsg(errCode));
        Intent intent = new Intent(DaHuaService.class.getName());
        intent.putExtra(IDaHuaService.DAHUA_SIGNAL, signal);
        intent.putExtra(IDaHuaService.DAHUA_ERROR_CODE, errCode);
        sendBroadcast(intent);
    }

    private void startHeartThread() {
        Log.w(TAG, "startHeartThread: 启动报警心跳线程");
        HeartRunnable heartRunnable = new HeartRunnable();
        Thread thread = new Thread(heartRunnable);
        thread.start();
    }

    private boolean isWait = false;

    /*心跳程序等待*/
    private void waitHeartThread() {
        synchronized (mObjLock) {
            if (isWait) return;
            isWait = true;
            try {
                mObjLock.wait(HEART_INTERVAL);
                isWait = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*心跳程序等待*/
    private void notifyHeartThread() {
        synchronized (mObjLock) {
            if (isWait && isRun) {
                mObjLock.notify();
            }
        }
    }

    /*停止心跳线程*/
    private void stopHeartThread() {
        synchronized (mObjLock) {
            if (!isRun) return;
            isRun = false;
            Log.w(TAG, "stopHeartThread: 停止报警心跳线程");
            mObjLock.notify();
            isWait = false;
//            zondyLogout();
        }
    }

    /*心跳的Runnable*/
    private class HeartRunnable implements Runnable {
        @Override
        public void run() {
            synchronized (mObjLock) {
                isRun = true;
                while (isRun) {
                    reportToZondyServer();
                    waitHeartThread();
                }
            }
        }

    }

    /*zondy退出大华时所做的操作*/
    private void zondyLogout() {
        try {
            new MmtBaseTask<String, Void, Void>(null, false) {
                @Override
                protected Void doInBackground(String... params) {
                    Log.w(TAG, "退出大华");
                    String sb = ServerConnectConfig.getInstance().getBaseServerPath() +
                            "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/LogoutDHApp?" +
                            "userName=" + params[0];
                    NetUtil.executeHttpGet(sb);
                    return null;
                }
            }.execute(Constant.getName());
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*上报到zondy的服务器上*/
    void reportToZondyServer() {
        String chnCode = PccClient.getInstance().getPccDevcode() + "1";
        String domID = CmuClient.getInstance().mCmuDomid;
        int userID = CmuClient.getInstance().mUserId;
        String userName = Constant.getName();
        String sipTel = McuClient.getInstance().getSoftPhoneCallnumber();
        long state = MyApplication.getInstance().getConfigValue("DHLoginState", -1);
        if (BaseClassUtil.isNullOrEmptyString(chnCode)
                || BaseClassUtil.isNullOrEmptyString(domID)
                || BaseClassUtil.isNullOrEmptyString(userName)
                || BaseClassUtil.isNullOrEmptyString(sipTel)) {
            return;
        }
        Log.w(TAG, String.format("上报到中地报警信息: userID=%d，userName=%s，domID=%s，chnCode=%s，sipTel=%s，state=%s"
                , userID, userName, domID, chnCode, sipTel, state));
        String sb = ServerConnectConfig.getInstance().getBaseServerPath() +
                "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/LoginDHApp?" +
                "userID=" + userID +
                "&userName=" + userName +
                "&domID=" + domID +
                "&chnCode=" + chnCode +
                "&sipTel=" + sipTel +
                "&state=" + state;
        String result = NetUtil.executeHttpGet(sb);
        if (state == 1 && !BaseClassUtil.isNullOrEmptyString(result)) {
            // 登录成功存入数据库之后开始维持心跳
            MyApplication.getInstance().putConfigValue("DHLoginState", -1);
        }
    }

    @Override
    public void onDestroy() {
        stopHeartThread();
        AppManager.removeService(this);
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }
}
