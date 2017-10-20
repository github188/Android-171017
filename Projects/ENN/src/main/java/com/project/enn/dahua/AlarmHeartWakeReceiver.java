package com.project.enn.dahua;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.mapgis.mmt.MyApplication;

/**
 * 作者 : zhoukang
 * 日期 : 2017-06-21  17:23
 * 说明 : 用来唤醒服务并立即上报数据的广播接受器
 */

public class AlarmHeartWakeReceiver extends BroadcastReceiver {
    private OnWakeHeartListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("DHState")) {
            MyApplication.getInstance().putConfigValue("DHLoginState", intent.getIntExtra("DHState", -1));
        }
        if (mListener != null) {
            mListener.onWake();
        }
    }

    public static void sendNotifyBroadcast(Context context, int state) {
        Intent intent = new Intent(AlarmHeartWakeReceiver.class.getName());
        intent.putExtra("DHState", state);
        context.sendBroadcast(intent);
    }

    public static AlarmHeartWakeReceiver registerReceiver(Context context) {
        AlarmHeartWakeReceiver receiver = new AlarmHeartWakeReceiver();
        IntentFilter filter = new IntentFilter(AlarmHeartWakeReceiver.class.getName());
        context.registerReceiver(receiver, filter);
        return receiver;
    }

    public static void unregistReceiver(Context context
            , AlarmHeartWakeReceiver receiver) {
        receiver.removeListener();
        context.unregisterReceiver(receiver);
    }

    public void setOnWakeHeartListener(OnWakeHeartListener listener) {
        this.mListener = listener;
    }

    public void removeListener() {
        this.mListener = null;
    }

    public interface OnWakeHeartListener {
        void onWake();
    }
}
