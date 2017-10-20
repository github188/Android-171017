package com.android.business.client.msp;

import android.text.TextUtils;
import android.util.Log;

import com.android.business.exception.BusinessErrorCode;
import com.android.business.exception.BusinessException;
import com.zhanben.sdk.handle.NativeHandle;

import java.util.Locale;

/**
 * 功能说明：sip话机接口
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-28
 */
public class SoftPhoneClient {
    public static final String MODULE_NAME = "IMDS_SIP";
    private static final String TAG = "Alarm";

    private SoftPhoneClient() {

    }

    /*private static class Instance {
        private static SoftPhoneClient instance = new SoftPhoneClient();
    }

    public static SoftPhoneClient getInstance() {
        return Instance.instance;
    }*/

    private static SoftPhoneClient instance;

    private final static Object initObj = new Object();

    public static SoftPhoneClient getInstance() {
        if (instance == null) {
            synchronized (initObj) {
                if (instance == null) {
                    instance = new SoftPhoneClient();
                }
            }
        }
        return instance;
    }

    public int init() {
        int result = NativeHandle.getInstance().softphone_initial();
        NativeHandle.getInstance().softphoneSetEcDelay(40);
        return result;
    }

    public void unInit() {
        NativeHandle.getInstance().softphone_destroy();
        instance = null;
    }

    public void setAutoAnswer(boolean enable) {
        int i = enable ? 1 : 0;
        NativeHandle.getInstance().softphone_set_auto_answer(i);
    }

  /*  public void initSip() throws BusinessException {
        String domain = CmuClient.getInstance().getServerIP(MODULE_NAME);
        if (TextUtils.isEmpty(domain)) {
            throw new BusinessException(BusinessErrorCode.BEC_COMMON_ILLEGAL_PARAM);
        }
        String[] ipArray = domain.split(";");
        String availableUrl = null;
        for (int i = 0; i < ipArray.length; i++) {
            String[] ipAndPort = ipArray[i].split("\\|");
            try {
                if (ipArray.length < 2) {
                    availableUrl =  ipAndPort[0] + ":" + ipAndPort[1];
                } else {
//                    InetAddress address = InetAddress.getByName(ipAndPort[0]);
//                    boolean isReachable = address.isReachable(10000);
                    float lossRate = PingUtil.getPacketLossFloat(ipAndPort[0], 4, 4);
                    LogHelper.d("Alarm", "url " + ipAndPort[0] + "lossRate" + lossRate);
                    if (lossRate < 25) {
                        availableUrl =  ipAndPort[0] + ":" + ipAndPort[1];
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (availableUrl == null) {
            throw new BusinessException();
        }
        LogHelper.w(TAG, "availableUrl:" + availableUrl);
        String num = McuClient.getInstance().mSoftphoneCallnumber;
        String psw = McuClient.getInstance().mSoftphonePassword;
        int res = NativeHandle.getInstance().softphone_set_account(num, psw, availableUrl, 300);
        if (res != 0) {
            throw new BusinessException();
        }
    }*/

    public void register() throws IllegalArgumentException, BusinessException {
        Log.w("alarm", "initSip: 注册话机");
        String domain = CmuClient.getInstance().getServerIP(MODULE_NAME);
        if (TextUtils.isEmpty(domain)) {
            throw new BusinessException(BusinessErrorCode.BEC_COMMON_ILLEGAL_PARAM);
        }
//        domain = "60.10.20.68|5062;" + domain;
        String[] ipArray = domain.split(";");

        String num = McuClient.getInstance().mSoftphoneCallnumber;
        String psw = McuClient.getInstance().mSoftphonePassword;

        for (String iport : ipArray) {
            iport = iport.replace("|", ":");
            int res = NativeHandle.getInstance().softphone_set_account(num, psw, iport, 300);
            if (res == 0) {
                Log.w(TAG, "注册成功：" + iport);
                return;
            } else {
                Log.w(TAG, "注册失败：" + iport);
            }
        }
        throw new BusinessException("话机注册失败");
    }

    public void register(String iport) {
        String num = McuClient.getInstance().mSoftphoneCallnumber;
        String psw = McuClient.getInstance().mSoftphonePassword;
        Log.w(TAG, String.format(Locale.CHINA, "注册话机信息: num=%s，psw=%s，iport=%s", num, psw, iport));
        int i = NativeHandle.getInstance().softphone_set_account(num, psw, iport, 300);
        Log.d(TAG, "话机注册结果: " + i);
    }

    public int unRegister() {
        return NativeHandle.getInstance().softphone_clear_account();
    }

    public int call(String telephone) {
        return NativeHandle.getInstance().softphone_callout(telephone);
    }

    public int answer() {
        return NativeHandle.getInstance().softphone_answer();
    }

    public void hangup() {
        NativeHandle.getInstance().softphone_hangup();
    }

    public int hold() {
        return NativeHandle.getInstance().softphone_hold();
    }

    public void softphoneSetInputVolume(int volume) {
        NativeHandle.getInstance().softphoneSetInputVolume(volume);
    }

    public void softphoneSetOutputVolume(int volume) {
        NativeHandle.getInstance().softphoneSetOutputVolume(volume);
    }

    public void setEcDelay(int delay){
        NativeHandle.getInstance().softphoneSetEcDelay(delay);
    }


    public void setSoftPhoneListener(NativeHandle.SoftPhoneNotifyListener listener) {
        NativeHandle.getInstance().setSoftPhoneNotifyListener(listener);
    }
}
