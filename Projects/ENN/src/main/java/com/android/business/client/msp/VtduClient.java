package com.android.business.client.msp;

import android.util.Xml;

import com.android.business.exception.BusinessErrorCode;
import com.android.business.exception.BusinessException;
import com.example.dhcommonlib.log.LogHelper;
import com.zhanben.sdk.handle.NativeHandle;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

/**
 * 功能说明：VTDU平台接口
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-20
 */
public class VtduClient implements NativeHandle.VtduNotifyListener, NativeHandle.VtduDataNotifyListener {
    private String TAG = VtduClient.class.getSimpleName();
    private int mSession;
    public int mSoftphoneAnswer;
    public boolean mFirstFrame = false;
    public boolean softphoneSetAccount = false;
    public int mVtduRealHandle;
    private int vtduRealTalkHandle;
    private Map<String, ByteBuffer> directBuffers;

    private VtduClient() {
        NativeHandle.getInstance().setVtduNotifyListener(this);
        NativeHandle.getInstance().setVtduDataNotifyListener(this);
    }


    /*private static class Instance {
        private static VtduClient instance = new VtduClient();
    }

    public static VtduClient getInstance() {
        return Instance.instance;
    }*/

    private static VtduClient instance;

    private final static Object initObj = new Object();

    public static VtduClient getInstance() {
        if (instance == null) {
            synchronized (initObj) {
                if (instance == null) {
                    instance = new VtduClient();
                }
            }
        }
        return instance;
    }

    @Override
    public void onMsg(int session, String msg, int dataSize) {
        LogHelper.d(TAG, "vtdu回调消息:\n" + msg);
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
                            if (session != mSession && !"onlineNotify".equals(currentCmd)) {
                                return;
                            }
                        } else if ("server".equals(parser.getName())) {
                            int newSession = session;
                            int sessionStatus = Integer.parseInt(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "status"));
                            if (sessionStatus == 0) {
                                LogHelper.d(TAG, "VTDU session offline");
                                // 结束设备视频播放后  sdk回调离线   暂时不处理离线通知
//                                newSession = 0;
                            } else {
                                LogHelper.d(TAG, "VTDU session new:" + newSession);
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

    @Override
    public void onData(int handle, int headtype, int type, int datasize) {
        ByteBuffer buffer = directBuffers.get(mVtduRealHandle + "");
        if (buffer == null)
            return;
        byte[] source = buffer.array();
        int n = 0;
        for (int i = 0; i < source.length; i++) {
            int val = (int) source[i];
            if (val != 0) {
                n = i;
                break;
            }
        }
        if (((int) source[n] == 73) && ((int) source[n + 1] == 77) && ((int) source[n + 2] == 75) && ((int) source[n + 3] == 72)
                && ((int) source[n + 4] == 1) && ((int) source[n + 5] == 1) && ((int) source[n + 6] == 0) && ((int) source[n + 7] == 0)) {
            n = n + 40;
            byte[] temp = new byte[datasize];
            for (int i = 0; i < datasize - 40; i++) {
                temp[i] = source[i + n];
            }
            // TODO: 2017/3/22  
        } else {
            if (n >= 4)
                n = 4;
            byte[] temp = new byte[datasize];
            for (int i = 0; i < datasize; i++) {
                temp[i] = source[i + n];
            }
            // TODO: 2017/3/22
        }
    }


    void connect(int cmuRealHandle, String ip, String domid, int svrId) throws Exception {
        char[] buffer = new char[1024];
        Arrays.fill(buffer, '\0');
        int cmuSession = cmuRealHandle;
        int result = NativeHandle.getInstance().VTDU_AC_Connect(cmuSession, ip, domid, svrId,
                buffer, 1024);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        String strResult = new String(buffer);
        LogHelper.d(TAG, "VTDU_SC_Connect result:\n" + strResult);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("vtdusession".equals(parser.getName())) {
                        int session = MspCommon.parseIntIgnoreNull(parser.nextText());
                        mSession = session;
                        NativeHandle.getInstance().VTDU_SC_SetCallBack(mSession);
                    }
                    break;
            }
            event = parser.next();
        }
    }

    void disConnect() {
        NativeHandle.getInstance().VTDU_AC_Disconnect(mSession);
        mSession = -1;
//        instance = null;
    }


    public void realTalking(String domid, String chncode, int timeout) throws BusinessException {
        try {
            char[] buffer = new char[10240];
            Arrays.fill(buffer, '\0');
            int result = NativeHandle.getInstance().VTDU_SC_RealTalkingEx_I(mSession, timeout, domid, chncode, 0, 0, 0, 0, 14, 2, 2, buffer, 1024);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }
            String strResult = new String(buffer);
            LogHelper.d(TAG, "VTDU_SC_RealTalkingEx_I result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            String realhandle = "";
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("realhandle".equals(parser.getName())) {
                            realhandle = parser.nextText();
                            vtduRealTalkHandle = MspCommon.parseIntIgnoreNull(realhandle);
                        }
                }
                event = parser.next();
            }
            buffer = null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public int stopVideo(int vtduRealHandle, int timeout) {
        int result = NativeHandle.getInstance().VTDU_SC_Stop(vtduRealHandle, timeout);
        return result;
    }
}
