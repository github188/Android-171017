package com.project.enn.dahua;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.project.enn.R;

/**
 * Created by Comclay on 2017/3/28.
 * 常量类
 */

public final class Constant {
    public final static int TIME_OUT = 10000;
    public final static String TEST_IMEI = "352299050625567";
    public final static String TEST_IP = "60.10.20.68";
    public final static int TEST_PORT = 9800;
    public final static String TEXT_NAME = "zk";
    public final static String TEST_PWD = "zk";

    public final static int VIDEO_BITRATE = 512;

    public static String getName() {
        try {
            return MyApplication.getInstance().getUserBean().LoginName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPassword() {
//        return MyApplication.getInstance().getUserBean().password;
        return "a";
    }

    public static String getIP() {
//        ServerConfigInfo serverConfigInfo =
//                ServerConnectConfig.getInstance().getServerConfigInfo();
//        return serverConfigInfo.IpAddress;
        return "60.10.20.68";
    }

    public static int getPort() {
//        ServerConfigInfo serverConfigInfo =
//                ServerConnectConfig.getInstance().getServerConfigInfo();
//        return Integer.valueOf(serverConfigInfo.Port);
        return 9800;
    }

    public static String getImei() {
        TelephonyManager tm = (TelephonyManager) MyApplication.getInstance().getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
//        return TEST_IMEI;
    }

    /**
     * 注册sip话机的IP和端口
     * 默认  "60.10.20.68:5062"
     */
    public static String getSiport() {
        String siport = MyApplication.getInstance().getConfigValue("Siport");
        if (siport != null && !BaseClassUtil.isNullOrEmptyString(siport)) {
            return siport;
        }
        return MyApplication.getInstance().getString(R.string.text_siport);
    }
}
