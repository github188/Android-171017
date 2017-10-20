package com.android.business.client.msp;

import android.util.Xml;

import com.android.business.entity.LcuMessageInfo;
import com.android.business.exception.BusinessErrorCode;
import com.android.business.exception.BusinessException;
import com.example.dhcommonlib.log.LogHelper;
import com.zhanben.sdk.handle.NativeHandle;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 功能说明：LcuClient平台接口
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-20
 */
public class LcuClient implements NativeHandle.LcuMsgNotifyListener {
    private String TAG = LcuClient.class.getSimpleName();
    private int mSession;

    private LcuClient() {
        NativeHandle.getInstance().setLcuMsgListener(this);
    }


    /*private static class Instance {
        private static LcuClient instance = new LcuClient();
    }

    public static LcuClient getInstance() {
        return Instance.instance;
    }*/

    private static LcuClient instance;

    private final static Object initObj = new Object();

    public static LcuClient getInstance() {
        if (instance == null) {
            synchronized (initObj) {
                if (instance == null) {
                    instance = new LcuClient();
                }
            }
        }
        return instance;
    }

    @Override
    public void onMsg(int session, String msg, int dataSize) {
        LogHelper.d(TAG, "lcu回调消息:\n" + msg);
        try {
            String cmd = "";
            String domid = "";
            String filename = "";
            String guid = "";
            String subtype = "";
            String notifycode = "";
            String notifyname = "";
            String progress = "";
            String fromdomid = "";
            String fromuserid = "";
            String todomid = "";
            String touserid = "";
            String id = "";
            String time = "";
            String status = "";
            String usersession = "";
            String message = "";
            String ctrlinfo = "";
            String type = "";
            String strResult = msg;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("root".equals(parser.getName())) {
                            cmd = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "cmd");
                        } else if ("domid".equals(parser.getName())) {
                            domid = parser.nextText();
                        } else if ("filename".equals(parser.getName())) {
                            filename = parser.nextText();
                        } else if ("guid".equals(parser.getName())) {
                            guid = parser.nextText();
                        } else if ("subtype".equals(parser.getName())) {
                            subtype = parser.nextText();
                        } else if ("notifycode".equals(parser.getName())) {
                            notifycode = parser.nextText();
                        } else if ("notifyname".equals(parser.getName())) {
                            notifyname = parser.nextText();
                        } else if ("fromdomid".equals(parser.getName())) {
                            fromdomid = parser.nextText();
                        } else if ("fromuserid".equals(parser.getName())) {
                            fromuserid = parser.nextText();
                        } else if ("todomid".equals(parser.getName())) {
                            todomid = parser.nextText();
                        } else if ("touserid".equals(parser.getName())) {
                            touserid = parser.nextText();
                        } else if ("id".equals(parser.getName())) {
                            id = parser.nextText();
                        } else if ("time".equals(parser.getName())) {
                            time = parser.nextText();
                        } else if ("status".equals(parser.getName())) {
                            status = parser.nextText();
                        } else if ("usersession".equals(parser.getName())) {
                            usersession = parser.nextText();
                        } else if ("msg".equals(parser.getName())) {
                            msg = parser.nextText();
                        } else if ("ctrlinfo".equals(parser.getName())) {
                            ctrlinfo = parser.nextText();
                        } else if ("type".equals(parser.getName())) {
                            type = parser.nextText();
                        }
                }
                event = parser.next();
            }
            if ("lcuSendFile".equals(cmd)) {
                // TODO: 2017/3/22  
            } else if ("commonNotify".equals(cmd)) {
                // TODO: 2017/3/22  
            } else if ("lcuReceiveMsg".equals(cmd)) {
                LcuMessageInfo info = new LcuMessageInfo();
                info.setTodomainid(todomid);
                info.setTouserid(touserid);
                info.setFromdomainid(fromdomid);
                info.setFromuserid(fromuserid);
                info.setType(type);
                info.setStatus(status);
                info.setMsg(message);
                info.setTime(time);
                parser = Xml.newPullParser();
                parser.setInput(new StringReader(ctrlinfo));
                event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    switch (event) {
                        case XmlPullParser.START_TAG:
                            if ("np1".equals(parser.getName())) {
                                info.setNp1(parser.nextText());
                            } else if ("np2".equals(parser.getName())) {
                                info.setNp2(parser.nextText());
                            } else if ("np3".equals(parser.getName())) {
                                info.setNp3(parser.nextText());
                            } else if ("np4".equals(parser.getName())) {
                                info.setNp4(parser.nextText());
                            } else if ("np5".equals(parser.getName())) {
                                info.setNp5(parser.nextText());
                            } else if ("str1".equals(parser.getName())) {
                                info.setStr1(parser.nextText());
                            } else if ("str2".equals(parser.getName())) {
                                info.setStr2(parser.nextText());
                            } else if ("str3".equals(parser.getName())) {
                                info.setStr3(parser.nextText());
                            } else if ("str4".equals(parser.getName())) {
                                info.setStr4(parser.nextText());
                            } else if ("str5".equals(parser.getName())) {
                                info.setStr5(parser.nextText());
                            } else if ("str6".equals(parser.getName())) {
                                info.setStr6(parser.nextText());
                            }
                    }
                    event = parser.next();
                }
                // TODO: 2017/3/22
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    void connect(int cmuRealHandle, String ip, String domid, int svrId) throws Exception {
        char[] buffer = new char[1024];
        Arrays.fill(buffer, '\0');
        int cmuSession = cmuRealHandle;
        int result = NativeHandle.getInstance().LCU_AC_Connect(cmuSession, ip, domid, svrId,
                buffer, 1024);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        String strResult = new String(buffer);
        LogHelper.d(TAG, "LCU_AC_Connect result:\n" + strResult);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("handle".equals(parser.getName())) {
                        int session = MspCommon.parseIntIgnoreNull(parser.nextText());
                        mSession = session;
                        NativeHandle.getInstance().LCU_SC_SetLCUMsg(mSession);
                    }
                    break;
            }
            event = parser.next();
        }
    }

    void disConnect() {
        NativeHandle.getInstance().LCU_SC_Disconn(mSession);
        mSession = -1;
//        instance = null;
    }

    public List<LcuMessageInfo> getMessageInfo(String cmuDomid, String toUserId, String type, int curpaage, int size, int timeout) throws BusinessException {
        try {
            String inputXml =
                    "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                            "<root>\n" +
                            "    <recordid></recordid>\n" +
                            "    <fromdomainid></fromdomainid>\n" +
                            "    <fromuserid></fromuserid>\n" +
                            "    <todomainid>" + cmuDomid + "</todomainid>\n" +
                            "    <touserid>" + toUserId + "</touserid>\n" +
                            "    <cmd></cmd>\n" +
                            "    <type>" + type + "</type>\n" +
                            "    <status>7</status>\n" +
                            "    <curpage>" + curpaage + "</curpage>\n" +
                            "    <size>" + size + "</size>\n" +
                            "    <starttime></starttime>\n" +
                            "    <endtime></endtime>\n" +
                            "    <np1></np1>\n" +
                            "    <np2></np2>\n" +
                            "    <np3></np3>\n" +
                            "    <np4></np4>\n" +
                            "    <np5></np5>\n" +
                            "    <str1></str1>\n" +
                            "    <str2></str2>\n" +
                            "    <str3></str3>\n" +
                            "    <str4></str4>\n" +
                            "    <str5></str5>\n" +
                            "    <str6></str6>\n" +
                            "</root>";
            LogHelper.d(TAG, "LcuGetQueryInfo input:\n" + inputXml);
            char[] buffer = new char[102400];
            Arrays.fill(buffer, '\0');
            int result = NativeHandle.getInstance().LCU_SC_GetQueryInfo(mSession, timeout,
                    inputXml, buffer, 102400);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }
            int total = 0;
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "LcuGetQueryInfo result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            List<LcuMessageInfo> lcuMessageInfos = new ArrayList<>();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("total".equals(parser.getName())) {
                            total = MspCommon.parseIntIgnoreNull(parser.nextText());
                        } else if ("item".equals(parser.getName())) {
                            LcuMessageInfo info = new LcuMessageInfo();
                            info.setRecordid(MspCommon.parseIntIgnoreNull(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "recordid")));
                            info.setTodomainid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "todomainid"));
                            info.setTouserid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "touserid"));
                            info.setFromdomainid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "fromdomainid"));
                            info.setFromuserid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "fromuserid"));
                            info.setCmd(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "cmd"));
                            info.setType(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "type"));
                            info.setStatus(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "status"));
                            info.setMsg(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "msg"));
                            info.setTime(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "time"));
                            info.setNp1(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "np1"));
                            info.setNp2(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "np2"));
                            info.setNp3(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "np3"));
                            info.setNp4(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "np4"));
                            info.setNp5(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "np5"));
                            info.setStr1(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "str1"));
                            info.setStr2(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "str2"));
                            info.setStr3(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "str3"));
                            info.setStr4(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "str4"));
                            info.setStr5(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "str5"));
                            info.setStr6(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "str6"));
                            if (info != null) {
                                lcuMessageInfos.add(info);
                            }
                        }
                }
                event = parser.next();
            }
            return lcuMessageInfos;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        throw new BusinessException();
    }


    public void sendMessage(String msg, String ctrlInfo, String toDomainId, String toUserId, int tag, int timeout) throws BusinessException {
        LogHelper.d(TAG, "msg xml Input:" + msg);
        LogHelper.d(TAG, "ctrlInfo xml Input:" + ctrlInfo);
        int result = NativeHandle.getInstance().LCU_SC_SendMessge(mSession, timeout,
                msg, ctrlInfo, toDomainId, MspCommon.parseIntIgnoreNull(toUserId), 0, tag);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }

    public void changeTaskStatus(int id, int status, int timeout) throws BusinessException {
        int result = NativeHandle.getInstance().LCU_SC_ChangTaskStatus(mSession, timeout, id, status);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }


}
