package com.android.business.client.msp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.android.business.client.listener.AlarmMeetingListener;
import com.android.business.entity.PccDevInfo;
import com.android.business.exception.BusinessErrorCode;
import com.android.business.exception.BusinessException;
import com.example.dhcommonlib.log.LogHelper;
import com.zhanben.sdk.handle.NativeHandle;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * 功能说明：PccClient平台接口
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-20
 */
public class PccClient implements NativeHandle.PccNotifyListener {
    private String TAG = PccClient.class.getSimpleName();
    private int mSession;
    private boolean isOpenVideo;
    String pccDevcode;
    String pccIpinfo;
    int pccSequence;
    int mTransproto;
    boolean isPccDeviceRegisted = false;
    private AlarmMeetingListener mAlarmMeetingListener;
    private Object mAlarmMeetingListenerLock = new Object();
    private Object mPccDeviceRegistLock = new Object();
    private Context mContext;

    private PccClient() {
        NativeHandle.getInstance().setPccNotifyListener(this);
    }


    /*private static class Instance {
        private static PccClient instance = new PccClient();
    }

    public static PccClient getInstance() {
        return Instance.instance;
    }*/

    private static PccClient instance;

    private final static Object initObj = new Object();

    public static PccClient getInstance() {
        if (instance == null) {
            synchronized (initObj) {
                if (instance == null) {
                    instance = new PccClient();
                }
            }
        }
        return instance;
    }

    public boolean isOpenVideo() {
        return isOpenVideo;
    }

    public String getPccDevcode() {
        return this.pccDevcode;
    }

    public void setAlarmMeetingListener(AlarmMeetingListener listener) {
        synchronized (mAlarmMeetingListenerLock) {
            mAlarmMeetingListener = listener;
        }
    }


    public void init(Context context) {
        mContext = context;
    }


    @Override
    public void onMsg(int session, String msg, int dataSize) {
        LogHelper.w(TAG, "pcc 回调消息:\n" + msg);
        try {
            int opt = -1;
            String ipinfo = "";
            String sequence = "";
            String strResult = msg;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("root".equals(parser.getName())) {
                            String currentCmd = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                    "cmd");
                            if ("onlineNotify".equals(currentCmd) && pccDevcode == null) {
                                getUserDevCode();
                                registPccDevice(MspCommon.TIME_OUT);
                            }
                        } else if ("revered".equals(parser.getName())) {
                            pccSequence = MspCommon.parseIntIgnoreNull(parser.nextText());
                        } else if ("opt".equals(parser.getName())) {
                            opt = MspCommon.parseIntIgnoreNull(parser.nextText());
                        } else if ("ipinfo".equals(parser.getName())) {
                            ipinfo = parser.nextText();
                            pccIpinfo = ipinfo;
                        } else if ("transproto".equals(parser.getName())) {
                            mTransproto = Integer.parseInt(parser.nextText());
                        }
                }
                event = parser.next();
            }
            if (opt == 0) {
                int temp = NativeHandle.getInstance().PCC_SC_StartVideoEx(ipinfo, mTransproto,
                        MspCommon.TIME_OUT);
                LogHelper.w(TAG, String.format(Locale.CHINA, "开始打开视频回调：ipinfo=%s，mTransproto=%d，temp=%d", ipinfo, mTransproto, temp));
                NativeHandle.getInstance().PCC_SC_SendPccErrCode(mSession, pccSequence, temp);
                int result = MspCommon.changeRes(temp);
                if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                    throw new BusinessException(result);
                }
                isOpenVideo = true;
                synchronized (mAlarmMeetingListenerLock) {
                    if (mAlarmMeetingListener != null) {
                        mAlarmMeetingListener.onStart(CmuClient.getInstance().mCmuRealHandle);
                    }
                }
            } else if (opt == 1) {
                LogHelper.d(TAG, "开始关闭视频回调");
                isOpenVideo = false;
                synchronized (mAlarmMeetingListenerLock) {
                    if (mAlarmMeetingListener != null) {
                        mAlarmMeetingListener.onFinish();
                    }
                }
                /*int temp = NativeHandle.getInstance().PCC_SC_CloseVideo();
                int errCode = NativeHandle.getInstance().PCC_SC_SendPccErrCode(mSession, pccSequence, temp);
                Log.w(TAG, "PCC_SC_CloseVideo:关闭视频操作结果：" + SDKExceptionDefine.getMsg(temp));
                Log.w(TAG, "PCC_SC_SendPccErrCode:关闭视频操作结果：" + SDKExceptionDefine.getMsg(errCode));
                int result = MspCommon.changeRes(temp);
                if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                    throw new BusinessException(temp);
                }*/

                closeVideo();

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    void connect(int cmuRealHandle, String ip, String domid, int svrId) throws Exception {
        char[] buffer = new char[1024];
        Arrays.fill(buffer, '\0');
        int cmuSession = cmuRealHandle;
        int result = NativeHandle.getInstance().PCC_AC_Connect(cmuSession, ip, domid, svrId,
                buffer, 1024);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        String strResult = new String(buffer);
        LogHelper.d(TAG, "PCC_AC_Connect result:\n" + strResult);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("handle".equals(parser.getName())) {
                        int session = MspCommon.parseIntIgnoreNull(parser.nextText());
                        mSession = session;
                        NativeHandle.getInstance().PCC_SC_SetPccMsg(mSession);
                    }
                    break;
            }
            event = parser.next();
        }
    }

    public void disConnect() {
        NativeHandle.getInstance().PCC_SC_Disconn(mSession);
        mSession = -1;
        pccDevcode = null;
        pccIpinfo = null;
    }

    private void registPccDevice(int timeout) throws BusinessException {
        String devname = CmuClient.getInstance().loginName;
        String devtype = "27";
        String phonenum = "";
        String inputXml =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <device \n" +
                        "    devcode=\"" + pccDevcode + "\"\n" +
                        "    devname=\"" + "APP_" + devname + "\"\n" +
                        "    devtype=\"" + devtype + "\"\n" +
                        "    phonenum=\"" + phonenum + "\"\n" +
                        "    />\n" +
                        "</root>";
        LogHelper.w(TAG, "PccRegPccDev input:\n" + inputXml);
        int result = NativeHandle.getInstance().PCC_SC_RegPccDev(mSession, timeout, inputXml);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        synchronized (mPccDeviceRegistLock) {
            isPccDeviceRegisted = true;
            if (mRegisteListener != null) mRegisteListener.onRegisted();
        }
        Log.w("alarm", "registPccDevice: 摄像头绑定成功！");
    }

    public boolean isPccDeviceRegisted() {
        return this.isPccDeviceRegisted;
    }

    private OnRegistPccDeviceListener mRegisteListener;

    public void addRegisterPccDeviceListener(OnRegistPccDeviceListener listener) {
        this.mRegisteListener = listener;
    }

    /*设备注册监听*/
    public interface OnRegistPccDeviceListener {
        void onRegisted();
    }

    public void closeVideo() throws BusinessException {
        LogHelper.d(TAG, "开始关闭视频回调");
        isOpenVideo = false;
        int temp = NativeHandle.getInstance().PCC_SC_CloseVideo();
        Log.w(TAG, "closeVideo: " + SDKExceptionDefine.getMsg(temp));
        NativeHandle.getInstance().PCC_SC_SendPccErrCode(mSession, pccSequence, temp);
        sendAnnounce(MspCommon.TIME_OUT);
        int result = MspCommon.changeRes(temp);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }

    private void getUserDevCode() throws BusinessException {
        SharedPreferences preferences = mContext.getSharedPreferences("dev_code",
                Context.MODE_PRIVATE);
        String devcode = preferences.getString("devcode", "");
        if (TextUtils.isEmpty(devcode)) {
            devcode = UUID.randomUUID().toString();
            preferences.edit().putString("devcode", devcode).apply();
        }
        LogHelper.d(TAG, "getUserDevCode:" + devcode);
        pccDevcode = devcode;
    }

   /* private void getUserDevCode() throws BusinessException {
        int result = 0;
        try {
            char[] buffer = new char[10240];
            Arrays.fill(buffer, '\0');
            result = NativeHandle.getInstance().PCC_SC_GetUserDevCode(buffer, 10240);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }
            int total = 0;
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "PccGetUserDevCode result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("devcode".equals(parser.getName())) {
                            String devcode = parser.nextText();
                            pccDevcode = devcode;
                        }
                }
                event = parser.next();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }*/

    public PccDevInfo getDefaultDeviceInfo() throws BusinessException {
        PccDevInfo info = null;
        try {
            char[] buffer = new char[10240];
            Arrays.fill(buffer, '\0');
            int result = NativeHandle.getInstance().PCC_SC_GetDefaultDevInfo(buffer, 10240);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "PccGetDefaultDevInfo result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("device".equals(parser.getName())) {
                            String devcode = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "devcode");
                            if ("".equals(devcode)) {
                                info = new PccDevInfo();
                                info.setDevcode(devcode);
                                info.setDevname(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "devname"));
                                info.setDevtype(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "devtype"));
                                info.setIp(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "ip"));
                                info.setPort(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "port"));
                                info.setUsername(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "username"));
                                info.setPwd(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "pwd"));
                            } else {
                                break;
                            }
                        }
                }
                event = parser.next();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return info;
    }


    public void setDefaultDeviceInfo(PccDevInfo info) throws BusinessException {
        String inputXml =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <device\n" +
                        "       devcode=\"" + info.getDevcode() + "\"\n" +
                        "       devname=\"" + info.getDevname() + "\"\n" +
                        "       devtype=\"" + info.getDevtype() + "\"\n" +
                        "       ip=\"" + info.getIp() + "\"\n" +
                        "       port=\"" + info.getPort() + "\"\n" +
                        "       username=\"" + info.getUsername() + "\"\n" +
                        "       pwd=\"" + info.getPwd() + "\"\n" +
                        "    />\n" +
                        "</root>";
        LogHelper.d(TAG, "PccSetDefaultDevInfo input:\n" + inputXml);
        int result = NativeHandle.getInstance().PCC_SC_SetDefaultDevInfo(inputXml);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }

    public void createDevice(PccDevInfo info) throws BusinessException {
        int result = 0;
        String inputXml =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <device\n" +
                        "       devcode=\"" + info.getDevcode() + "\"\n" +
                        "       devname=\"" + info.getDevname() + "\"\n" +
                        "       devtype=\"" + info.getDevtype() + "\"\n" +
                        "       ip=\"" + info.getIp() + "\"\n" +
                        "       port=\"" + info.getPort() + "\"\n" +
                        "       username=\"" + info.getUsername() + "\"\n" +
                        "       pwd=\"" + info.getPwd() + "\"\n" +
                        "    />\n" +
                        "</root>";
        LogHelper.d(TAG, "PccCreateDevice input:\n" + inputXml);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }

    public void sendAnnounce(int timeout) throws BusinessException {
        LogHelper.w(TAG, "发送关闭视频信令");
        int result = NativeHandle.getInstance().PCC_SC_SendAnnounce(mSession, timeout);
        Log.w(TAG, "sendAnnounce: " + SDKExceptionDefine.getMsg(result));
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }

    public void sendVideoData(byte[] buf, int dataType, int width, int height) {
        if (dataType == 80) {
            dataType = 0;
        } else if (dataType == 73) {
            dataType = 1;
        } else {
            LogHelper.w(TAG, "unkown h264 frame type");
            return;
        }
        NativeHandle.getInstance().PCC_SC_SendVideoData(buf, buf.length, dataType, width, height);
    }
}
