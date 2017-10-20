package com.android.business.client.msp;

import android.util.Xml;

import com.android.business.entity.ChannelInfo;
import com.android.business.exception.BusinessErrorCode;
import com.android.business.exception.BusinessException;
import com.example.dhcommonlib.log.LogHelper;
import com.zhanben.sdk.handle.NativeHandle;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.Arrays;

/**
 * 功能说明：DMU平台接口
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-20
 */
public class DmuClient implements NativeHandle.DmuNotifyListener {

    private String TAG = DmuClient.class.getSimpleName();
    private int mSession;
    private int mLastPtzCmd = -1;

    private DmuClient() {
        NativeHandle.getInstance().setDmuNotifyListener(this);
    }

    private static DmuClient instance;
    /*private static class Instance {
        private static DmuClient instance = new DmuClient();
    }*/
    private final static Object initObj = new Object();
    public static DmuClient getInstance() {
        if (instance == null) {
            synchronized (initObj) {
                if (instance == null) {
                    instance = new DmuClient();
                }
            }
        }
        return instance;
    }

    void connect(int cmuRealHandle, String ip, String domid, int svrId) throws Exception {
        char[] buffer = new char[1024];
        Arrays.fill(buffer, '\0');
        int cmuSession = cmuRealHandle;
        int result = NativeHandle.getInstance().DMU_AC_Connect(cmuSession, ip, domid, svrId, buffer,
                1024);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        String strResult = new String(buffer);
        LogHelper.d(TAG, "DMU_AC_Connect result:\n" + strResult);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("handle".equals(parser.getName())) {
                        int session = MspCommon.parseIntIgnoreNull(parser.nextText());
                        mSession = session;
                        NativeHandle.getInstance().DMU_SC_SetDmuMsg(session);
                    }
                    break;
            }
            event = parser.next();
        }
    }

    void disConnect() {
        NativeHandle.getInstance().DMU_SC_Disconn(mSession);
        mSession = -1;
//        mLastPtzCmd = -1;
//        instance = null;
    }

    public void ptzControl(ChannelInfo channelInfo, ChannelInfo.PtzOperation operation, int timeout)
            throws BusinessException {
        if (operation == null) {
            throw new BusinessException(BusinessErrorCode.BEC_COMMON_ILLEGAL_PARAM);
        }
        int stop = operation == ChannelInfo.PtzOperation.stop ? 1 : 0;
        int cmd = 0;// unKown
        if (operation != ChannelInfo.PtzOperation.stop) {
            switch (operation) {
                case up:
                    cmd = 1;
                    break;
                case down:
                    cmd = 2;
                    break;
                case left:
                    cmd = 3;
                    break;
                case right:
                    cmd = 4;
                    break;
                case leftUp:
                    cmd = 5;
                    break;
                case rightUp:
                    cmd = 6;
                    break;
                case leftDown:
                    cmd = 7;
                    break;
                case RightDown:
                    cmd = 8;
                    break;
                case zoomin:
                    cmd = 13;
                    break;
                case zoomout:
                    cmd = 14;
                    break;
            }
        } else if (mLastPtzCmd!= -1) {
            cmd = mLastPtzCmd;
        }

        String ptzControlInput =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <domid>" + channelInfo.getDomId() + "</domid>             \n" +
                        "    <chncode>" + channelInfo.getChnSncode() + "</chncode>     \n" +
                        "    <stop>" + stop + "</stop>              \n" +
                        "    <cmd>" + cmd + "</cmd>                 \n" +
                        "    <intparam1>" + 4 + "</intparam1>\n" + // 步长
                        "    <intparam2>" + 4 + "</intparam2>\n" +
                        "    <intparam3></intparam3>\n" +
                        "    <intparam4></intparam4>\n" +
                        "    <intparam5></intparam5>\n" +
                        "    <intparam6></intparam6>\n" +
                        "    <intparam7></intparam7>\n" +
                        "    <intparam8></intparam8>\n" +
                        "    <strparam1></strparam1>\n" +
                        "    <strparam2></strparam2>\n" +
                        "</root>";
        LogHelper.d(TAG, "DmuPtzControl input:\n" + ptzControlInput);
        char[] buffer = new char[10240];
        Arrays.fill(buffer, '\0');
        int result = NativeHandle.getInstance().DMU_SC_PTZControl(mSession, timeout,
                ptzControlInput, buffer, 10240);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        String strResult = new String(buffer);
        LogHelper.d(TAG, "DMU_SC_PTZControl result:\n" + strResult);
    }

    public void reportAlarm(String alarmtime, double longitude, double latitude,
                           String phoneNum, String phonePsw, int timeout) throws BusinessException {
        String devcode = PccClient.getInstance().pccDevcode;
        String alarmInfo =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <item             \n" +
                        "       devcode=\"" + devcode + "\"\n" +
                        "       chncode=\"" + devcode + "1\"\n" +
                        "       phonenum=\"" + phoneNum + "\"\n" +
                        "       phonepass=\"" + phonePsw + "\"\n" +
                        "       type=\"201\"\n" +
                        "       alarmtime=\"" + alarmtime + "\"\n" +
                        "       longitude=\"" + longitude + "\"\n" +
                        "       latitude=\"" + latitude + "\"\n" +
                        "    />\n" +
                        "</root>";
        LogHelper.d(TAG, "DMU_SC_SendClientAlarm input:\n" + alarmInfo);

        int errCode = NativeHandle.getInstance().DMU_SC_SendClientAlarm(mSession, timeout,
                alarmInfo);
        int result = MspCommon.changeRes(errCode);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(errCode);
        }
    }

    public void sendRealData(String devcode, String time, String longitude, String latitude,
                            String phoneNum, String phonePsw, int timeout) throws BusinessException {
        String alarmInfo =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <item             \n" +
                        "       devcode=\"" + devcode + "\"\n" +
                        "       chncode=\"" + devcode + "1\"\n" +
                        "       phonenum=\"" + phoneNum + "\"\n" +
                        "       phonepass=\"" + phonePsw + "\"\n" +
                        "       type=\"9\"\n" +
                        "       time=\"" + time + "\"\n" +
                        "    >\n" +
                        "       <expand             \n" +
                        "           longitude=\"" + longitude + "\"\n" +
                        "           latitude=\"" + latitude + "\"\n" +
                        "       />\n" +
                        "    </item>\n" +
                        "</root>";
        LogHelper.d(TAG, "DMU_SC_SendClientRealData input:\n" + alarmInfo);
        int result = NativeHandle.getInstance().DMU_SC_SendClientRealData(mSession, timeout,
                alarmInfo);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }

    @Override
    public void onMsg(int session, String msg, int dataSize) {
        LogHelper.w(TAG, "dmu session:" + session + "回调消息:\n" + msg);
        try {
            String strResult = msg;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("root".equals(parser.getName())) {
                            String currentCmd = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "cmd");
                            if (session != mSession
                                    && !"onlineNotify".equals(currentCmd)) {
                                return;
                            }
                        } else if ("server".equals(parser.getName())) {
                            int newSession = session;
                            int sessionStatus = Integer.parseInt(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "status"));
                            if (sessionStatus == 0) {
                                LogHelper.d(TAG, "dmu session offline");
//                                newSession = 0;
                            } else {
                                LogHelper.d(TAG, "dmu session new:" + newSession);
                            }
                            mSession = newSession;
                        }

                }
                event = parser.next();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
