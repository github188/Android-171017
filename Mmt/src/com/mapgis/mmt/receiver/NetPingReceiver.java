package com.mapgis.mmt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetPingReceiver extends BroadcastReceiver {
    public final static String ACTION_DELAY = "com.mapgis.mmt.netdelayreceiver";
    public final static String ACTION_CANCEL = "com.mapgis.mmt.netdelayreceiver.cancel";
    public final static String ACTION_START = "com.mapgis.mmt.netdelayreceiver.start";
    public final static String PARAM_NET_DELAY = "NetDelay";
    private CaculatedNetDelayListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_START:
                // 开始检测网络延迟
                if (mListener != null) mListener.onStart();
                break;
            case ACTION_DELAY:
                // 发送网络延迟结果
                if (intent.hasExtra(PARAM_NET_DELAY)) {
                    int netDelay = intent.getIntExtra(PARAM_NET_DELAY, 0);
                    if (mListener != null) mListener.onNetDelay(netDelay);
                }
                break;
            case ACTION_CANCEL:
                // 取消检测网络延迟
                if (mListener != null) mListener.onCancel();
                break;
        }
    }

    public void setCaculatedNetDelayListener(CaculatedNetDelayListener listener) {
        this.mListener = listener;
    }

    public interface CaculatedNetDelayListener {
        void onStart();

        void onNetDelay(int delay);

        void onCancel();
    }
}
