package com.project.enn.dahua;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.android.business.client.msp.SDKExceptionDefine;
import com.project.enn.dahua.service.DaHuaService;

/**
 * Created by Comclay on 2017/4/20.
 * DaHuaService发出的广播信号
 */

public class DaHuaBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "DaHuaBroadcastReceiver";
    private OnDaHuaSignalReceivedListener mListener;

    public static DaHuaBroadcastReceiver registerReceiver(Context context) {
        DaHuaBroadcastReceiver receiver = new DaHuaBroadcastReceiver();
        IntentFilter filter = new IntentFilter(DaHuaService.class.getName());
        context.registerReceiver(receiver, filter);
        return receiver;
    }

    public static void unregistReceiver(Context context
            , DaHuaBroadcastReceiver receiver) {
        receiver.removeListener();
        context.unregisterReceiver(receiver);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String dahuaSignal = intent.getStringExtra(IDaHuaService.DAHUA_SIGNAL);
        int errCode = SDKExceptionDefine.UnknownErrorLibrary.getCode();
        if (intent.hasExtra(IDaHuaService.DAHUA_ERROR_CODE)) {
            errCode = intent.getIntExtra(IDaHuaService.DAHUA_ERROR_CODE, errCode);
        }

        switch (dahuaSignal) {
            case IDaHuaService.LOGIN_FAILED://登录失败
                if (mListener != null) mListener.onDaHuaLoginFailedSignal(errCode);
                break;
            case IDaHuaService.SIP_REGIST_SUCCESS://话机注册成功
                if (mListener != null) mListener.onSipRegistSuccessSignal();
                break;
            case IDaHuaService.SIP_REGIST_FAILED://话机注册失败
                if (mListener != null) mListener.onSipRegistFailedSignal(errCode);
                break;
            case IDaHuaService.CONNECT_SUCCESS://大华连接成功
                if (mListener != null) mListener.onDaHuaConnectSuccessSignal();
                break;
            case IDaHuaService.SIP_HOLD://正在通话
                if (mListener != null) mListener.onSipHoldSignal();
                break;
            case IDaHuaService.SIP_HUNGUP://挂断电话
                if (mListener != null) mListener.onSipHangupSignal();
                break;
            case IDaHuaService.LIVE_START://停止视频传输
                if (mListener != null) mListener.onLiveStartSignal();
                break;
            case IDaHuaService.LIVE_FINISH://停止视频传输
                if (mListener != null) mListener.onLiveFinishSignal();
                break;
            default:
                break;
        }
    }

    public void setOnDaHuaSignalReceivedListener(OnDaHuaSignalReceivedListener onDaHuaSignalReceivedListener) {
        this.mListener = onDaHuaSignalReceivedListener;
    }

    public void removeListener(){
        this.mListener = null;
    }

    /**
     *
     */
    public abstract static class OnDaHuaSignalReceivedListener {
        /**
         * 大华登录失败信号
         * @param dahuaErrCode 登录失败的错误码
         */
        protected void onDaHuaLoginFailedSignal(int dahuaErrCode) {
            Log.w(TAG, "onDaHuaLoginFailedSignal: " + SDKExceptionDefine.getMsg(dahuaErrCode));
        }

        /**
         * 话机注册成功信号
         */
        protected void onSipRegistSuccessSignal() {
        }

        /**
         * 话机注册失败的信号
         * @param dahuaErrCode 注册失败错误码
         */
        protected void onSipRegistFailedSignal(int dahuaErrCode) {
            Log.w(TAG, "onSipRegistFailedSignal: " + SDKExceptionDefine.getMsg(dahuaErrCode));
        }

        /**
         * 连接大华成功的信号
         */
        protected void onDaHuaConnectSuccessSignal() {
        }

        /**
         * 接通电话的信号
         */
        protected void onSipHoldSignal() {
        }

        /**
         * 挂断电话的信号
         */
        protected void onSipHangupSignal() {
        }

        /**
         * 开启视频的信号
         */
        protected void onLiveStartSignal() {
        }

        /**
         * 结束视频的信号
         */
        protected void onLiveFinishSignal() {
        }
    }
}
