package com.mapgis.mmt.global;

import android.content.Intent;
import android.os.SystemClock;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.PingUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.receiver.NetPingReceiver;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Comclay on 2017/6/8.
 * 检测网络延迟的线程
 */

public class CheckNetPingThread extends MmtBaseThread {
    private final static int PING_COUNT = 2;
    private final static long CHECK_INTERVAL = 1000;
    private final String mIpAddress;
    private volatile AtomicLong mPreCheckTime;

    public CheckNetPingThread() {
        mIpAddress = ServerConnectConfig.getInstance().getServerConfigInfo().IpAddress;
        mPreCheckTime = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public void run() {
        while (!isExit) {
            try {
                if (System.currentTimeMillis() - mPreCheckTime.get() > CHECK_INTERVAL) {
                    int avgRTT = PingUtil.getAvgRTT(mIpAddress, PING_COUNT, 5);
                    sendNetDelay(avgRTT);
                }
                mPreCheckTime.set(System.currentTimeMillis());
                SystemClock.sleep(CHECK_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNetDelay(int delay) {
        Intent intent = new Intent(NetPingReceiver.ACTION_DELAY);
        intent.putExtra(NetPingReceiver.PARAM_NET_DELAY, delay);
        MyApplication.getInstance().sendBroadcast(intent);
    }
}
