package com.android.business.client.msp;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.android.business.client.listener.ServerStatusListener;
import com.android.business.client.listener.UserPhoneNumListener;
import com.android.business.entity.ContactInfo;
import com.android.business.entity.MeetingCfgInfo;
import com.android.business.entity.MeetingUserInfo;
import com.android.business.exception.BusinessErrorCode;
import com.android.business.exception.BusinessException;
import com.example.dhcommonlib.log.LogHelper;
import com.zhanben.sdk.handle.NativeHandle;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 功能说明：MCU平台接口
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-20
 */
public class McuClient implements NativeHandle.McuNotifyListener {
    public static final String MODULE_NAME = "MCU";
    private String TAG = "meeting";
    private static final int TIME_OUT = 20000;
    public int mSession;
    public String mCurrentDomid;
    public int mCurrentMeetid;
    public String mCurrentMeetpass;
    public String mTelephone;
    private MeetingUserInfo mMeetingUserInfo;
    // 连接状态 0离线 1在线
    private int mStatus;
    private String mCurrentMeetDomid;
    String mSoftphoneCallnumber;
    String mSoftphonePassword;
    private UserPhoneNumListener mUserPhoneNumListener;
    private Object mUserPhoneNumListenerLock = new Object();

    private McuClient() {
        NativeHandle.getInstance().setMcuNotifyListener(this);
    }

    /*private static class Instance {
        private static McuClient instance = new McuClient();
    }

    public static McuClient getInstance() {
        return Instance.instance;
    }*/

    private static McuClient instance;

    private final static Object initObj = new Object();

    public static McuClient getInstance() {
        if (instance == null) {
            synchronized (initObj) {
                if (instance == null) {
                    instance = new McuClient();
                }
            }
        }
        return instance;
    }

    public String getSoftPhoneCallnumber() {
        return this.mSoftphoneCallnumber;
    }

    public String getSoftPhonePassword() {
        return this.mSoftphonePassword;
    }

    public void setUserPhoneNumListener(UserPhoneNumListener listener) {
        synchronized (mUserPhoneNumListenerLock) {
            mUserPhoneNumListener = listener;
        }
    }

    void connect(int cmuRealHandle, String ip, String domid, int svrId) throws Exception {
        char[] buffer = new char[1024];
        Arrays.fill(buffer, '\0');
        int cmuSession = cmuRealHandle;
        int result = NativeHandle.getInstance().MCU_AC_Connect(cmuSession, ip, domid, svrId,
                buffer, 1024);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        String strResult = new String(buffer);
        LogHelper.d(TAG, "MCU_AC_Connect result:\n" + strResult);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("handle".equals(parser.getName())) {
                        int session = MspCommon.parseIntIgnoreNull(parser.nextText());
                        mSession = session;
                        NativeHandle.getInstance().MCU_SC_SetMCUMsg(mSession);
                    }
                    break;
            }
            event = parser.next();
        }
    }

    void disConnect() {
        NativeHandle.getInstance().MCU_SC_Disconn(mSession);
        mSession = -1;
        mCurrentDomid = null;
        mCurrentMeetid = 0;
        mCurrentMeetpass = null;
        mTelephone = null;
        mMeetingUserInfo = null;
        instance = null;
    }

    public int addMeeting(String name, String pass, String time, String starttime, int user, int num) {
        int result = 0;
        try {
            String optMeetingCfg =
                    "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                            "<root>\n" +
                            "    <opt>0</opt>       \n" +
                            "    <item \n" +
                            "        meetid=\"\"      \n" +
                            "        domid=\"" + CmuClient.getInstance().mCmuDomid + "\"       \n" +
                            "        code=\"" + UUID.randomUUID() + "\"        \n" +
                            "        name=\"" + name + "\"        \n" +
                            "        theme=\"\"       \n" +
                            "        pwd=\"" + pass + "\"         \n" +
                            "        num=\"" + num + "\"         \n" +
                            "        userid=\"" + user + "\"      \n" +
                            "        status=\"\"      \n" +
                            "        type=\"0\"        \n" +
                            "        fast=\"\"        \n" +
                            "        creatorid=\"" + CmuClient.getInstance().mUserId + "\"   \n" +
                            "        time=\"" + time + "\"        \n" +
                            "        starttime=\"" + starttime + "\"   \n" +
                            "        endtime=\"" + starttime + "\"     \n" +
                            "        updatetime=\"\"  \n" +
                            "        orgid=\"-1\"       \n" +
                            "        orgdomid=\"\"    \n" +
                            "        orgcode=\"\"     \n" +
                            "        orgpath=\"\"     \n" +
                            "    />\n" +
                            "</root>";

            LogHelper.d(TAG, "MCU_SC_OptMeetingCfgInfo input:" + optMeetingCfg);
            result = NativeHandle.getInstance().MCU_SC_OptMeetingCfgInfo(McuClient.getInstance().mSession, TIME_OUT, optMeetingCfg);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }

            return result;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int queryMeetingCfgInfo() {
        int result = 0;
        try {
            char[] buffer = new char[102400];
            Arrays.fill(buffer, '\0');
            String queryMettingCfg =
                    "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                            "<root>\n" +
                            "    <meetid />\n" +
                            "    <code />\n" +
                            "    <type />\n" +
                            "    <size />\n" +
                            "    <curpage />\n" +
                            "</root>";
            result = NativeHandle.getInstance().MCU_SC_QueryMeetingCfgInfo(McuClient.getInstance().mSession, TIME_OUT,
                    queryMettingCfg, buffer, 102400);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }

            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "MCU_SC_QueryMeetingCfgInfo result:\n" + strResult);
            String meetDomid = "";
            int meetid = 0;
            MeetingCfgInfo info = null;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            Map meetingMap = new HashMap();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("item".equals(parser.getName())) {
                            if ("meetid".equals(parser.getAttributeName(0))) {
                                info = new MeetingCfgInfo();
                                info.setMeetid(parseIntIgnoreNull(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "meetid")));
                                info.setDomid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "domid"));
                                info.setCode(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "code"));
                                info.setName(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "name"));
                                info.setPwd(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "pwd"));
                                info.setNum(parseIntIgnoreNull(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "num")));
                                info.setUserid(parseIntIgnoreNull(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "userid")));
                                info.setUsername(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "username"));
                                meetDomid = info.getDomid();
                                meetid = info.getMeetid();
                                meetingMap.put(meetid, info.getMeetingUserInfoList());
                            } else if ("userid".equals(parser.getAttributeName(0))) {
                                MeetingUserInfo userInfo = new MeetingUserInfo();
                                userInfo.setUserid(parseIntIgnoreNull(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "userid")));
                                userInfo.setUsername(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "username"));
                                userInfo.setDevcode(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "devcode"));
                                userInfo.setTelephone(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "telephone"));
                                userInfo.setPwd(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "pwd"));
                                userInfo.setStatus(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "status"));
                                userInfo.setSessionid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "sessionid"));
                                userInfo.setType(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "type"));
                                userInfo.setMeetDomid(meetDomid);
                                userInfo.setMeetid(meetid);
                                List<MeetingUserInfo> userList = (List<MeetingUserInfo>) meetingMap.get(meetid);
                                userList.add(userInfo);
                            }
                            if (info.getMeetid() > 0) {
                                info.setMethod("McuQueryMeetingCfgInfo");
                                // EventBus.getDefault().post(info);
                            }
                        }
                        break;
                }
                event = parser.next();
            }
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int parseIntIgnoreNull(String text) {
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

    public List<ContactInfo> queryContacts(int curpage, int pageSize, String keywords) throws BusinessException {
        char[] buffer = new char[1024000];
        Arrays.fill(buffer, '\0');
        if (TextUtils.isEmpty(keywords)) {
            keywords = "-1";
        }
        String queryAllContacts =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <keywords>" + keywords + "</keywords>\n" +
                        "    <curpage>" + curpage + "</curpage>\n" +
                        "    <size>" + pageSize + "</size>\n" +
                        "</root>";
        int result = NativeHandle.getInstance().MCU_SC_QueryAllContact(McuClient.getInstance().mSession, TIME_OUT,
                queryAllContacts, buffer, 1024000);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        try {
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "MCU_SC_QueryAllContact result:\n" + strResult);
            int total = 0;
            int curSize = 0;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            List<ContactInfo> infoList = new ArrayList<>();
            ContactInfo info = null;
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("total".equals(parser.getName())) {
                            total = parseIntIgnoreNull(parser.nextText());
                        } else if ("contact".equals(parser.getName())) {
                            info = new ContactInfo();
                            info.setDomid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "domid"));
                            info.setUserid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "userid"));
                            info.setName(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "name"));
                            info.setType(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "type"));
                        } else if ("telephone".equals(parser.getName())) {
                            if (info != null && !"".equals(info.getName())) {
                                info.setTelphone(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "number"));
                                infoList.add(info);
                            }
                            curSize++;
                        }
                        break;
                }
                event = parser.next();
            }
            return infoList;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        throw new BusinessException();
    }

    public int loginOrOutMeetingRoom(String meetDomid, int meetid, String pass, int inOrOut) {
        int result = 0;
        try {
            char[] buffer = new char[1024];
            Arrays.fill(buffer, '\0');
            result = NativeHandle.getInstance().MCU_SC_LoginMeetingRoom(mSession, TIME_OUT,
                    CmuClient.getInstance().mUserId, meetDomid, meetid, pass, inOrOut, buffer, 1024);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "MCU_SC_LoginMeetingRoom result:\n" + strResult);
            int flag = 0;
            int userid = 0;
            String userdomid = "";
            String telephone = "";
            String username = "";
            MeetingUserInfo info = new MeetingUserInfo();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("flag".equals(parser.getName())) {
                            info.setUserFlag(parseIntIgnoreNull(parser.nextText()));
                        } else if ("userid".equals(parser.getName())) {
                            info.setUserid(parseIntIgnoreNull(parser.nextText()));
                        } else if ("userdomid".equals(parser.getName())) {
                            info.setUserdomid(parser.nextText());
                        } else if ("username".equals(parser.getName())) {
                            info.setUsername(parser.nextText());
                        } else if ("domid".equals(parser.getName())) {
                            info.setDomid(parser.nextText());
                        } else if ("devcode".equals(parser.getName())) {
                            info.setDevcode(parser.nextText());
                        } else if ("telephone".equals(parser.getName())) {
                            info.setTelephone(parser.nextText());
                        } else if ("pwd".equals(parser.getName())) {
                            info.setPwd(parser.nextText());
                        } else if ("status".equals(parser.getName())) {
                            info.setStatus(parser.nextText());
                        }
                        break;
                }
                event = parser.next();
            }
            info.setMethod("McuLoginOrOutMeetingRoom" + inOrOut);
            mTelephone = telephone;
            mCurrentDomid = meetDomid;
            mCurrentMeetid = meetid;
            mCurrentMeetpass = pass;
            McuClient.getInstance().setMeetingUserInfo(info);
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int queryMeetingUserInfo(String meetDomid, int meetid) {
        return queryMeetingUserTemplate(meetDomid, meetid, "LIST");
    }

    private int queryMeetingUserTemplate(String meetDomid, int meetid, String command) {
        int result = 0;
        try {
            char[] buffer = new char[10240];
            Arrays.fill(buffer, '\0');
            result = NativeHandle.getInstance().MCU_SC_QueryMeetingUserInfo(McuClient.getInstance().mSession, TIME_OUT,
                    meetDomid, meetid, buffer, 10240);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "MCU_SC_QueryMeetingUserInfo result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            int count = 0;
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("item".equals(parser.getName())) {
                            if ("LIST".equals(command)) {
                                MeetingUserInfo info = new MeetingUserInfo();
                                info.setUserdomid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "userdomid"));
                                info.setUserid(parseIntIgnoreNull(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "userid")));
                                info.setUsername(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "username"));
                                info.setDomid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "domid"));
                                info.setDevcode(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "devcode"));
                                info.setTelephone(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "telephone"));
                                info.setPwd(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "pwd"));
                                info.setStatus(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "status"));
                                info.setSessionid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "sessionid"));
                                info.setMeetid(meetid);
                                info.setMeetDomid(meetDomid);
                                info.setMethod("McuQueryMeetingUserInfo");
                                LogHelper.d(TAG, "是否主持人:" + (info.getFlag() == 1 ? "是" : "否") + "\t姓名:" + info.getUsername() + "\t状态:" + ("1".equals(info.getStatus()) ? "禁言" : "发言"));
                                //EventBus.getDefault().post(info);
                            } else if ("COUNT".equals(command)) {
                                count++;
                            }
                        }
                        break;
                }
                event = parser.next();
            }
            if ("COUNT".equals(command)) {
                return count;
            }
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            if ("COUNT".equals(command)) {
                return -1;
            }
        }
        return 0;
    }

    public int optUserMute(String userDomid, int userid, String sessionid, String meetDomid, int meetid, int flag) {
        int result = 0;
        try {
            LogHelper.d(TAG, "McuOptUserMute input:\n" + "userDomid:" + userDomid + " userid:" + userid + " sessionid:" + sessionid
                    + " meetDomid:" + meetDomid + " meetid:" + meetid + " flag:" + flag);
            long lsession = Long.parseLong(sessionid);
            result = NativeHandle.getInstance().MCU_SC_OptUserMute(McuClient.getInstance().mSession, TIME_OUT, userDomid, userid, lsession, meetDomid, meetid, flag);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int optAllUserMute(int flag) {
        return NativeHandle.getInstance().MCU_SC_OptUserMute(McuClient.getInstance().mSession, TIME_OUT,
                CmuClient.getInstance().mCmuDomid, CmuClient.getInstance().mUserId, -1,
                McuClient.getInstance().mCurrentDomid,
                McuClient.getInstance().mCurrentMeetid, flag);
    }

    public int optMeetingMember(String meetDomid, int meetid, String userDomid, String session, String number, String devcode, int opt) {
        int result = 0;
        try {
            char[] buffer = new char[10240];
            Arrays.fill(buffer, '\0');
            String optMeetingUser =
                    "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                            "<root>\n" +
                            "    <opt>" + opt + "</opt>       \n" +
                            "    <domid>" + meetDomid + "</domid>       \n" +
                            "    <roomid>" + meetid + "</roomid>       \n" +
                            "    <meetingmember  \n" +
                            "        no=\"\"      \n" +
                            "        domid=\"" + userDomid + "\"       \n";
            if (session != null && !"".equals(session) && !"0".equals(session))
                optMeetingUser += "        session=\"" + session + "\"        \n";
            else
                optMeetingUser += "        session=\"\"        \n";
            if (number != null && !"".equals(number))
                optMeetingUser += "        number=\"" + number + "\"        \n";
            else
                optMeetingUser += "        number=\"\"        \n";
            if (devcode != null && !"".equals(devcode))
                optMeetingUser += "        devcode=\"" + devcode + "\"        \n";
            else
                optMeetingUser += "        devcode=\"\"        \n";
            optMeetingUser += "    />\n";
            optMeetingUser += "</root>";
            LogHelper.d(TAG, " MCU_SC_OptMeetingMember  input:" + optMeetingUser);
            result = NativeHandle.getInstance().MCU_SC_OptMeetingMember(McuClient.getInstance().mSession, TIME_OUT, optMeetingUser, buffer, 10240);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }
            LogHelper.d(TAG, " MCU_SC_OptMeetingMember 成功!");
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int optMeetingFile(String roomDomId, int roomid, String filename) {
        int result = 0;
        try {
            result = NativeHandle.getInstance().MCU_SC_OptMeetingFile(McuClient.getInstance().mSession, TIME_OUT, 2, roomDomId, roomid, filename, 1, 1);
            result = MspCommon.changeRes(result);
            if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
                throw new BusinessException(result);
            }
            LogHelper.d(TAG, " MCU_SC_OptMeetingFile 成功!");
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int exitMeeting(String meetDomid, int meetid) {
        return NativeHandle.getInstance().MCU_SC_ExitMeeting(McuClient.getInstance().mSession,
                TIME_OUT,
                CmuClient.getInstance().mCmuDomid,
                CmuClient.getInstance().mUserId, meetDomid, meetid);
    }

    public MeetingUserInfo getMeetingUserInfo() {
        return mMeetingUserInfo;
    }

    public void setMeetingUserInfo(MeetingUserInfo mMeetingUserInfo) {
        this.mMeetingUserInfo = mMeetingUserInfo;
    }

    @Override
    public void onMsg(int session, String msg, int dataSize) {
        LogHelper.d("alarm", "mcu session:" + session + "回调消息:\n" + msg);
        try {
            String strResult = msg;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            String currentCmd = "";
            String notifycode = "";
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("root".equals(parser.getName())) {
                            currentCmd = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "cmd");
                            break;
                        } else if ("notifycode".equals(parser.getName())) {
                            notifycode = parser.nextText();
                        }
                    default:
                }
                event = parser.next();
            }
            switch (currentCmd) {
                case "onlineNotify":
                    OnMcuOnlineNotify(session, strResult);
                    break;
                case "meetLoginNotify":
                    OnMcuMeetLoginNotify(strResult);
                    break;
                case "meetUserBanNotify":
                    OnMcuMeetUserBanNotify(strResult);
                    break;
                case "commonNotify":
                    switch (notifycode) {
                        case "401":
                            break;
                        case "402":
                            onMcuKickUser(strResult);
                            break;
                        case "403":
                            onMcuMeetEnd(strResult);
                            break;
                        case "404":
                            break;
                        case "405":
                            break;
                        case "406":
                            break;
                        case "408":
                            onMcuInviteUser(strResult);
                            break;
                        case "409":
                            onMcuBindNumber(strResult);
                            break;
                        case "410":
                            break;
                        case "411":
                            break;
                        case "412":
                            break;
                        case "417":
                            onMcuErrorMsg(strResult);
                            break;
                        default:
                    }
                default:
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void OnMcuOnlineNotify(int session, String strResult) throws IOException, XmlPullParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("server".equals(parser.getName())) {
                        int status = parseIntIgnoreNull(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "status"));
                        mStatus = status;
                        ServerStatusListener listener = CmuClient.getInstance()
                                .getServerStatusListener();
                        if (listener != null) {
                            listener.onStatusChange(MODULE_NAME, status, -1);
                        }
                        if (status != 0) {
                            mSession = session;
                        }
                    }
                default:
            }
            event = parser.next();
        }
    }

    private void OnMcuMeetLoginNotify(String strResult) throws IOException, XmlPullParserException {
        MeetingUserInfo info = new MeetingUserInfo();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("userdomid".equals(parser.getName())) {
                        info.setUserdomid(parser.nextText());
                    } else if ("userid".equals(parser.getName())) {
                        info.setUserid(parseIntIgnoreNull(parser.nextText()));
                    } else if ("username".equals(parser.getName())) {
                        info.setUsername(parser.nextText());
                    } else if ("devcode".equals(parser.getName())) {
                        info.setDevcode(parser.nextText());
                    } else if ("telephone".equals(parser.getName())) {
                        info.setTelephone(parser.nextText());
                    } else if ("pwd".equals(parser.getName())) {
                        info.setPwd(parser.nextText());
                    } else if ("status".equals(parser.getName())) {
                        info.setStatus(parser.nextText());
                    } else if ("flag".equals(parser.getName())) {
                        info.setFlag(parseIntIgnoreNull(parser.nextText()));
                    } else if ("roomdomid".equals(parser.getName())) {
                        info.setMeetDomid(parser.nextText());
                    } else if ("meetid".equals(parser.getName())) {
                        info.setMeetid(parseIntIgnoreNull(parser.nextText()));
                    } else if ("sessionid".equals(parser.getName())) {
                        info.setSessionid(parser.nextText());
                    }
                default:
            }
            event = parser.next();
        }
        info.setMethod("OnMcuMeetLoginNotify");
        if (info.getMeetDomid().equals(mCurrentDomid) && info.getMeetid() == mCurrentMeetid) {
            // TODO: 2017/3/28
        } else {
            // TODO: 2017/3/28 会议用户状态刷新
        }
    }

    private void OnMcuMeetUserBanNotify(String strResult) throws IOException, XmlPullParserException {
        MeetingUserInfo info = new MeetingUserInfo();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("userdomid".equals(parser.getName())) {
                        info.setUserdomid(parser.nextText());
                    } else if ("userid".equals(parser.getName())) {
                        info.setUserid(parseIntIgnoreNull(parser.nextText()));
                    } else if ("flag".equals(parser.getName())) {
                        info.setStatus(parser.nextText());
                    } else if ("roomdomid".equals(parser.getName())) {
                        info.setMeetDomid(parser.nextText());
                    } else if ("meetid".equals(parser.getName())) {
                        info.setMeetid(parseIntIgnoreNull(parser.nextText()));
                    } else if ("sessionid".equals(parser.getName())) {
                        info.setSessionid(parser.nextText());
                    }
                default:
            }
            event = parser.next();
        }
        if (info.getMeetDomid().equals(mCurrentDomid) && info.getMeetid() == mCurrentMeetid) {
            // TODO: 2017/3/28
            if (-1 == info.getUserid()) {
                Map param = new HashMap();
                param.put("status", info.getStatus());
            } else {
                info.setMethod("OnMcuMeetUserBanNotify");
            }
        }
    }

    private void onMcuKickUser(String strResult) throws IOException, XmlPullParserException {
        int userid = 0;
        int meetid = 0;
        String meetdomid = "";
        String telephone = "";
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("userid".equals(parser.getName())) {
                        userid = parseIntIgnoreNull(parser.nextText());
                    }
                    if ("roomid".equals(parser.getName())) {
                        meetid = parseIntIgnoreNull(parser.nextText());
                    }
                    if ("roomdomid".equals(parser.getName())) {
                        meetdomid = parser.nextText();
                    }
                    if ("telephone".equals(parser.getName())) {
                        telephone = parser.nextText();
                    }
                default:
            }
            event = parser.next();
        }
        if (meetdomid.equals(mCurrentDomid) && meetid == mCurrentMeetid) {
            // TODO: 2017/3/28
            Map param = new HashMap();
            param.put("telephone", telephone);
        }
    }

    private void onMcuMeetEnd(String strResult) throws IOException, XmlPullParserException {
        int meetid = 0;
        String meetdomid = "";
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("roomid".equals(parser.getName())) {
                        meetid = parseIntIgnoreNull(parser.nextText());
                    }
                    if ("roomdomid".equals(parser.getName())) {
                        meetdomid = parser.nextText();
                    }
                default:
            }
            event = parser.next();
        }
        if (meetdomid.equals(mCurrentDomid) && meetid == mCurrentMeetid) {
            // TODO: 2017/3/28
        }
    }

    /*会议邀请*/
    private void onMcuInviteUser(String strResult) throws IOException, XmlPullParserException {
        Log.w("meeting", "onMcuInviteUser: " + strResult);
        MeetingUserInfo info = new MeetingUserInfo();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("newroomid".equals(parser.getName())) {
                        info.setMeetid(parseIntIgnoreNull(parser.nextText()));
                    } else if ("newroomdomid".equals(parser.getName())) {
                        info.setDomid(parser.nextText());
                    } else if ("newroomtype".equals(parser.getName())) {
                        info.setType(parser.nextText());
                    } else if ("newroompassword".equals(parser.getName())) {
                        info.setPwd(parser.nextText());
                    } else if ("newroomname".equals(parser.getName())) {
                        info.setMeetName(parser.nextText());
                    } else if ("userdomid".equals(parser.getName())) {
                        info.setUserdomid(parser.nextText());
                    } else if ("userid".equals(parser.getName())) {
                        info.setUserid(parseIntIgnoreNull(parser.nextText()));
                    } else if ("usernumber".equals(parser.getName())) {
                        info.setTelephone(parser.nextText());
                    } else if ("usersession".equals(parser.getName())) {
                        info.setSessionid(parser.nextText());
                    }
                default:
            }
            event = parser.next();
        }
    }

    private void onMcuBindNumber(String strResult) throws IOException, XmlPullParserException {
        String callnumber = "";
        String password = "";
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("callnumber".equals(parser.getName())) {
                        callnumber = parser.nextText();
                    } else if ("password".equals(parser.getName())) {
                        password = parser.nextText();
                    }
                default:
            }
            event = parser.next();
        }
        mSoftphoneCallnumber = callnumber;
        mSoftphonePassword = password;
        synchronized (mUserPhoneNumListenerLock) {
            if (mUserPhoneNumListener != null) {
                mUserPhoneNumListener.onBind();
            }
        }

    }


    private void onMcuErrorMsg(String strResult) throws IOException, XmlPullParserException {
        String nofityname = "";
        int errorcode = 0;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("nofityname".equals(parser.getName())) {
                        nofityname = parser.nextText();
                    } else if ("errorcode".equals(parser.getName())) {
                        errorcode = Integer.parseInt(parser.nextText());
                    }
                default:
            }
            event = parser.next();
        }
        // TODO: 2017/3/28 错误消息
    }


}
