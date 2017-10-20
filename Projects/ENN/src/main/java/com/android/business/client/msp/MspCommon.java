package com.android.business.client.msp;

import android.text.TextUtils;

import com.android.business.entity.DeviceInfo;
import com.android.business.exception.BusinessErrorCode;
import com.example.dhcommonlib.log.LogHelper;

/**
 * 功能说明：MSP公共
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-20
 */
public class MspCommon {
    private static String TAG = MspCommon.class.getSimpleName();
    // 超时时间
    public static final int TIME_OUT = 10000;


    /**
     * MSP SDK返回值转换为BusinessCode
     * @param mspRes
     * @return
     */
    public static int changeRes(int mspRes) {
        int res = BusinessErrorCode.BEC_COMMON_FAIL;
        switch (mspRes) {
            case 200:
                res = BusinessErrorCode.BEC_COMMON_SUCCESS;
                break;

        }

        if (res != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            LogHelper.e(TAG, "fail mspRes:" + SDKExceptionDefine.getMsg(mspRes));
        }

        return res;
    }



    public static DeviceInfo.DeviceType getDeivceType(String devtype) {
        if (TextUtils.isEmpty(devtype)) {
            return DeviceInfo.DeviceType.UNKNOWN;
        }
        try{
            int type = Integer.parseInt(devtype);
            DeviceInfo.DeviceType typeEnum = DeviceInfo.DeviceType.UNKNOWN;
            switch (type) {
                case 1:
                    typeEnum = DeviceInfo.DeviceType.DVR;
                    break;
                case 6:
                    typeEnum = DeviceInfo.DeviceType.IPC;
                    break;
                case 2:
                    typeEnum = DeviceInfo.DeviceType.NVR;
                    break;
                case 25:
                    typeEnum = DeviceInfo.DeviceType.PTZCAMERA;
                    break;
                case 26:
                    // 话机   过滤
                    typeEnum = null;
                    break;
            }

            return typeEnum;
        } catch (NumberFormatException e) {
            LogHelper.e(TAG, "devtype is not num");
            e.printStackTrace();
            return DeviceInfo.DeviceType.UNKNOWN;
        }
    }


    public static int parseIntIgnoreNull(String text) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        }
        if ("null".equalsIgnoreCase(text)) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
