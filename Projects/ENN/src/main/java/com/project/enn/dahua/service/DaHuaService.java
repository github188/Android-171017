package com.project.enn.dahua.service;


import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.business.client.msp.SDKExceptionDefine;
import com.mapgis.mmt.AppManager;
import com.project.enn.ENNApplication;
import com.project.enn.R;
import com.project.enn.dahua.AlarmHeartWakeReceiver;
import com.project.enn.dahua.Constant;
import com.project.enn.dahua.IDaHuaService;
import com.project.enn.dahua.activity.AlarmActivity;
import com.project.enn.dahua.activity.ComeIPhoneActivity;
import com.project.enn.dahua.activity.DaHuaLiveActivity;
import com.project.enn.dahua.presenter.DaHuaPresenter;

/**
 * Created by Comclay on 2017/4/20.
 * 大华服务
 */

public class DaHuaService extends Service implements IDaHuaService {
    private static final String TAG = "DaHuaService";
    public final static String DAHUA_LOGIN_NAME = "DaHua_login_name";
    public final static String DAHUA_LOGIN_PASSWORD = "dahua_login_password";
    private DaHuaPresenter mPresenter;

    private String mLoginName;
    private String mLoginPassword;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppManager.addService(this);
        showToast("开始连接大华");
        if (mPresenter == null) this.mPresenter = new DaHuaPresenter(this);
        this.mPresenter.initDaHuaTech();
        mPresenter.loginDaHuaTech();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.mPresenter.uninitDaHuaTech();
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
        /*int onStartCommand = super.onStartCommand(intent, flags, startId);
        try {
            if (intent == null) {
                return onStartCommand;
            }
            mLoginName = intent.getStringExtra(DAHUA_LOGIN_NAME);
            mLoginPassword = intent.getStringExtra(DAHUA_LOGIN_PASSWORD);
            if (BaseClassUtil.isNullOrEmptyString(mLoginName)
                    || BaseClassUtil.isNullOrEmptyString(mLoginPassword)) {
                sendErrSignalBroadcast(LOGIN_FAILED, SDKExceptionDefine.CheckUserPwdFaild.getCode());
                return onStartCommand;
            }
            mPresenter.loginDaHuaTech();
        } catch (Exception e) {
            e.printStackTrace();
            return onStartCommand;
        }
        return onStartCommand;*/
    }

    @Override
    public void onDestroy() {
        AppManager.removeService(this);
        super.onDestroy();
    }

    @Override
    public void loginSuccess() {
        Log.w(TAG, "loginSuccess: 登录成功");
        sendSignalBroadCast(LOGIN_SUCCESS);
    }

    @Override
    public void loginFailed(int errCode) {
        Log.w(TAG, "loginFailed: 登录失败：" + SDKExceptionDefine.getMsg(errCode));
        showToast("连接大华失败，" + SDKExceptionDefine.getMsg(errCode));
        sendErrSignalBroadcast(LOGIN_FAILED, errCode);
    }

    @Override
    public void logoutSuccess() {
        Log.w(TAG, "logoutSuccess: 登出成功");
        sendSignalBroadCast(LOGOUT_SUCCESS);
    }

    @Override
    public void logoutFailed(int errCode) {
        Log.w(TAG, "logoutFailed: 登出失败");
        sendErrSignalBroadcast(LOGOUT_FAILED, errCode);
    }

    @Override
    public void onSipRegistSuccess() {
        Log.w(TAG, "onSipRegistSuccess: 话机注册成功");
        sendSignalBroadCast(SIP_REGIST_SUCCESS);
    }

    @Override
    public void onSipRegistFailed() {
        Log.w(TAG, "onSipRegistFailed: 话机注册失败");
        showToast("话机注册失败");
        sendSignalBroadCast(SIP_REGIST_FAILED);
    }

    @Override
    public void onSipRing(String number) {
        Log.w(TAG, "onSipRing: 来电提醒:" + number);
        sendSignalBroadCast(SIP_RING);
        enterComeIPhoneActivity(number, "");
    }

    @Override
    public void onSipHold(String number) {
        Log.w(TAG, "onSipHold: 正在通话");
        Intent intent = new Intent(DaHuaService.class.getName());
        intent.putExtra(DAHUA_SIGNAL, SIP_HOLD);
        intent.putExtra(DAHUA_PARAM_NUMBER, number);
        sendBroadcast(intent);
    }

    @Override
    public void onSipHungup() {
        Log.w(TAG, "onSipHungup: 挂断电话");
        sendSignalBroadCast(SIP_HUNGUP);
    }

    @Override
    public void onStartLive() {
        Log.w(TAG, "onStartLive: 开始传输视频");
        if (AppManager.getActivity(AlarmActivity.class) == null) {
            enterDaHuaLiveActivity();
        }
        sendSignalBroadCast(LIVE_START);
    }

    @Override
    public void onFinishLive() {
        Log.w(TAG, "onFinishLive: 停止传输视频");
        AlarmHeartWakeReceiver.sendNotifyBroadcast(this, 1);
        sendSignalBroadCast(LIVE_FINISH);
    }

    @Override
    public void connectSuccess() {
        Log.w(TAG, "connectSuccess: 大华科技连接成功");
        showToast("大华科技连接成功");
        sendSignalBroadCast(CONNECT_SUCCESS);
        mPresenter.startHeartService();
    }

    @Override
    public void enterComeIPhoneActivity(String number, String name) {
        Activity activity = AppManager.currentActivity();
        if (activity == null) {
            mPresenter.autoAnswer();
            return;
        }
        Intent intent = new Intent(activity, ComeIPhoneActivity.class);
        intent.putExtra(PHONE_NUMBER, number);
        intent.putExtra(COME_IPHONE_NAME, name);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.bottom_in, 0);
    }

    @Override
    public void enterDaHuaLiveActivity() {
        Activity activity = AppManager.currentActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, DaHuaLiveActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, 0);
    }

    @Override
    public void sendSignalBroadCast(String signal) {
        Log.w(TAG, "sendSignalBroadCast: 发送广播信号" + signal);
        Intent intent = new Intent(DaHuaService.class.getName());
        intent.putExtra(DAHUA_SIGNAL, signal);
        sendBroadcast(intent);
    }

    public void sendErrSignalBroadcast(String signal, int errCode) {
        Log.w(TAG, "sendSignalBroadCast: 发送广播信号" + signal + "错误信息" + SDKExceptionDefine.getMsg(errCode));
        Intent intent = new Intent(DaHuaService.class.getName());
        intent.putExtra(DAHUA_SIGNAL, signal);
        intent.putExtra(DAHUA_ERROR_CODE, errCode);
        sendBroadcast(intent);
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(ENNApplication.getInstance().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getDaHuaLoginName() {
        return Constant.getName();
//        return this.mLoginName;
    }

    @Override
    public String getDaHuaLoginPassword() {
        return this.mLoginPassword;
    }
}
