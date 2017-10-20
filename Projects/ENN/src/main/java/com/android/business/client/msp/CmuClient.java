package com.android.business.client.msp;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.android.business.client.listener.DeviceTreeListener;
import com.android.business.client.listener.ServerStatusListener;
import com.android.business.entity.ChannelInfo;
import com.android.business.entity.ContactsUserInfo;
import com.android.business.entity.DeviceInfo;
import com.android.business.entity.UserOrg;
import com.android.business.exception.BusinessErrorCode;
import com.android.business.exception.BusinessException;
import com.example.dhcommonlib.log.LogHelper;
import com.zhanben.sdk.handle.NativeHandle;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能说明：CMU平台接口
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-20
 */
public class CmuClient implements NativeHandle.CmuNotifyListener {
    private static final String TAG = CmuClient.class.getSimpleName();
    public volatile int mCmuRealHandle;
    public int mUserId;
    public String loginName;
    public String mCmuDomid;
    private int mCmuSession;

    private DeviceTreeListener mDeviceTreeListener;
    private Object mDeviceTreeListenerLock = new Object();
    private ServerStatusListener mServerStatusListener;
    private Object mServerStatusListenerLock = new Object();

    private Map<String, String> mServerIpMap = new HashMap<>(6);

    private CmuClient() {
        NativeHandle.getInstance().setCmuNotifyListener(this);
    }

    private static CmuClient instance;

    private final static Object initObj = new Object();

    public static CmuClient getInstance() {
        if (instance == null) {
            synchronized (initObj) {
                if (instance == null) {
                    instance = new CmuClient();
                }
            }
        }
        return instance;
    }

    public void setDeviceTreeListener(DeviceTreeListener listener) {
        synchronized (mDeviceTreeListenerLock) {
            mDeviceTreeListener = listener;
        }
    }

    public void setServerStatusListener(ServerStatusListener listener) {
        synchronized (mServerStatusListenerLock) {
            mServerStatusListener = listener;
        }
    }

    public ServerStatusListener getServerStatusListener() {
        synchronized (mServerStatusListenerLock) {
            return mServerStatusListener;
        }
    }

    public int login(String ip, int port, String user, String password, String imei, int timeout)
            throws BusinessException {
        char[] buffer = new char[1024];
        Arrays.fill(buffer, '\0');
        loginName = user;
        String info =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>" +
                        "<root>" +
                        "<ip>" + ip + "</ip>" +
                        "<port>" + port + "</port>" +
                        "<username>" + user + "</username>" +
                        "<userpass>" + password + "</userpass>" +
                        "<type>" + 2 + "</type>" +
                        "<imei>" + imei + "</imei>" +
                        "</root>";
        Arrays.fill(buffer, '\0');
        LogHelper.d(TAG, "CMU_SC_LoginEx input:\n" + info);
        int errorCode = NativeHandle.getInstance().CMU_SC_LoginEx(timeout, info, buffer, 1024);
        Log.w("login", "login: " + SDKExceptionDefine.getMsg(errorCode));
        int result = MspCommon.changeRes(errorCode);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(errorCode);
        }
        try {
            String strResult = new String(buffer);
            LogHelper.d(TAG, "CMU_SC_LoginEx result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("handle".equals(parser.getName())) {
                            mCmuRealHandle = MspCommon.parseIntIgnoreNull(parser.nextText());
                            NativeHandle.getInstance().CMU_SC_SetMsg(mCmuRealHandle);
                        } else if ("userid".equals(parser.getName())) {
                            mUserId = MspCommon.parseIntIgnoreNull(parser.nextText());
                        } else if ("domid".equals(parser.getName())) {
                            mCmuDomid = parser.nextText();
//                            mCmuDomid = "57309";
                        } else if ("session".equals(parser.getName())) {
                            mCmuSession = MspCommon.parseIntIgnoreNull(parser.nextText());
                        }
                        break;
                }
                event = parser.next();
            }
            CmuOptSubscribe(timeout);

            return mUserId;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        throw new BusinessException();
    }

    private void CmuOptSubscribe(int timeout) throws BusinessException {
        char[] buffer = new char[10240];
        Arrays.fill(buffer, '\0');
        String info =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>" +
                        "<root>" +
                        "<srcdomid>" + mCmuDomid + "</srcdomid>" +
                        "<modulename>virgo</modulename>" +
                        "<domid>" + mCmuDomid + "</domid>" +
                        "<userid>" + mUserId + "</userid>" +
                        "<enable>1</enable>" +
                        "<item  cmd=\"200\" type=\"0\"/>" +
                        "<item  cmd=\"203\" type=\"0\"/>" +
                        "<item  cmd=\"315\" type=\"0\"/>" +
                        "</root>";
        Arrays.fill(buffer, '\0');
        LogHelper.d(TAG, "CMU_SC_OptSubscribe  input:\n" + info);
        int result = NativeHandle.getInstance().
                CMU_SC_OptSubscribe(mCmuRealHandle, timeout, info);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }

    public void logout() throws BusinessException {
        int errCode = NativeHandle.getInstance().CMU_SC_Logout(mCmuRealHandle);
        PccClient.getInstance().disConnect();
        McuClient.getInstance().disConnect();
        DmuClient.getInstance().disConnect();
        FsClient.getInstance().disConnect();
        LcuClient.getInstance().disConnect();
        VtduClient.getInstance().disConnect();
        FsClient.getInstance().disConnect();
        int result = MspCommon.changeRes(errCode);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(errCode);
        } else {
            mServerIpMap.clear();
            mCmuRealHandle = -1;
            mCmuDomid = null;
            mCmuSession = -1;
        }
    }

    public List<ContactsUserInfo> getUsersInfo(int timeout) throws BusinessException {
        char[] buffer = new char[1024000];
        Arrays.fill(buffer, '\0');
        LogHelper.d(TAG, "CMU_SC_QueryAllUser");
        int result = NativeHandle.getInstance().CMU_SC_QueryAllUser(mCmuRealHandle, timeout,
                buffer, 1024000);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        try {
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "CMU_SC_QueryAllUser result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            List<ContactsUserInfo> users = new ArrayList<>();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("user".equals(parser.getName())) {
                            String orgdomid = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                    "orgdomid");
                            // 只获取自己所在dom
                            if (TextUtils.equals(orgdomid, mCmuDomid)) {
                                String userid = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                        "userid");
                                String username = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                        "username");
                                String orgcode = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                        "orgcode");
                                String status = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                        "status");
                                if (!TextUtils.equals("invisibleadmin", username)
                                        && !TextUtils.isEmpty(userid)) {
                                    ContactsUserInfo user = new ContactsUserInfo();
                                    user.setId(userid);
                                    user.setUserName(username);
                                    user.setOrgCode(orgcode);
                                    user.setOrgDomId(orgdomid);
                                    if ("1".equals(status)) {
                                        user.setStatus(ContactsUserInfo.ONLINE);
                                    } else {
                                        user.setStatus(ContactsUserInfo.OFFLINE);
                                    }
                                    users.add(user);
                                }
                            }
                        }
                        break;
                }
                event = parser.next();
            }
            return users;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        throw new BusinessException();
    }

    public List<UserOrg> getOrgs(int timeout) throws BusinessException {
        char[] buffer = new char[1024000];
        Arrays.fill(buffer, '\0');
        LogHelper.d(TAG, "CMU_SC_QueryAllOrg");
        int result = NativeHandle.getInstance().CMU_SC_QueryAllOrg(mCmuRealHandle, timeout, buffer,
                1024000);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        String strResult = new String(buffer);
        try {
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "CMU_SC_QueryAllOrg result:\n" + strResult);

            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            List<UserOrg> orgs = new ArrayList<>();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("org".equals(parser.getName())) {
                            String domid = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                    "domid");
                            // 只获取自己所在dom
                            if (TextUtils.equals(domid, mCmuDomid)) {
                                String orgcode = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                        "orgcode");
                                String parentdomid = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                        "parentdomid");
                                String parentorgcode = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                        "parentorgcode");
                                String title = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                        "title");
                                if (!TextUtils.isEmpty(orgcode) && !TextUtils.isEmpty(domid)) {
                                    UserOrg org = new UserOrg();
                                    org.setDomId(domid);
                                    org.setId(orgcode);
                                    org.setParentdomid(parentdomid);
                                    org.setParentId(parentorgcode);
                                    org.setUserOrgName(title);
                                    orgs.add(org);
                                }
                            }
                        }
                        break;
                }
                event = parser.next();
            }

            return orgs;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new BusinessException();
    }


    public List<String> getOnlineUsers(int timeout) throws BusinessException {
        char[] buffer = new char[1024000];
        Arrays.fill(buffer, '\0');
        LogHelper.d(TAG, "CMU_SC_QueryOnlineUser");
        int result = NativeHandle.getInstance().CMU_SC_QueryOnlineUser(mCmuRealHandle, timeout,
                buffer, 1024000);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        try {
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "CMU_SC_QueryOnlineUser result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            List<String> users = new ArrayList<>();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("user".equals(parser.getName())) {
                            String userid = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE,
                                    "userid");
                            if (!TextUtils.isEmpty(userid)) {
                                users.add(userid);
                            }
                        }
                        break;
                }
                event = parser.next();
            }
            return users;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        throw new BusinessException();
    }


    public List<DeviceInfo> queryOrgDevice(String groupuuid, String orgCode, int timeout)
            throws BusinessException {
        char[] buffer = new char[1024000];
        Arrays.fill(buffer, '\0');
        LogHelper.d(TAG, "CMU_SC_QueryDeviceByFuncIdEx input:" + orgCode);
        int result = NativeHandle.getInstance().CMU_SC_QueryDeviceByFuncIdEx(mCmuRealHandle,
                timeout, mCmuDomid, orgCode, "1,2,3,4,5,6,102", 3, buffer, 1024000);//3 预览权限
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        try {
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "CMU_SC_QueryDeviceByFuncIdEx result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            List<DeviceInfo> devs = new ArrayList<>();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("device".equals(parser.getName())) {
                            String domid = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "domid");
                            String devcode = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "devcode");
                            String orgcode = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "orgcode");
                            String devtype = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "devtype");
                            String title = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "title");
                            String status = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "status");
                            DeviceInfo deviceInfo = new DeviceInfo();
                            deviceInfo.setName(title);
                            deviceInfo.setSnCode(devcode);
                            deviceInfo.setGroupUuid(groupuuid);
                            deviceInfo.setGroupId(orgcode);
                            deviceInfo.setDomId(domid);
                            if ("1".equals(status)) {
                                deviceInfo.setState(DeviceInfo.DeviceState.Online);
                            } else {
                                deviceInfo.setState(DeviceInfo.DeviceState.Offline);
                            }
                            deviceInfo.setType(MspCommon.getDeivceType(devtype));
                            if (deviceInfo.getType() == null) {
                                continue;
                            }

                            devs.add(deviceInfo);
                        }
                        break;
                }
                event = parser.next();
            }

            return devs;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        throw new BusinessException();
    }


    public List<ChannelInfo> queryDeviceChannel(DeviceInfo deviceInfo, int timeout)
            throws BusinessException {

        char[] buffer = new char[1024000];
        Arrays.fill(buffer, '\0');
        LogHelper.d(TAG, "CMU_SC_QueryDevDetail input:");
        int result = NativeHandle.getInstance().CMU_SC_QueryDevDetail(mCmuRealHandle,
                timeout, deviceInfo.getDomId(), deviceInfo.getSnCode(), 0, buffer, 1024000);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        try {
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "CMU_SC_QueryDevDetail result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            List<ChannelInfo> channels = new ArrayList<>();
            int channelIndex = 0;
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("channel".equals(parser.getName())) {
                            String domid = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "domid");
                            String devcode = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "devcode");
                            String title = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "title");
                            String status = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "status");
                            String devtype = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "devtype");
                            ChannelInfo channelInfo = new ChannelInfo(null, null, null);
                            channelInfo.setDomId(domid);
                            if ("25".equals(devtype)) {
                                channelInfo.setType(ChannelInfo.ChannelType.PtzCamera);
                            } else {
                                channelInfo.setType(ChannelInfo.ChannelType.Camera);
                            }
                            channelInfo.setChnSncode(devcode);
                            channelInfo.setIndex(channelIndex);
                            if ("1".equals(status)) {
                                channelInfo.setState(ChannelInfo.ChannelState.Online);
                            } else {
                                channelInfo.setState(ChannelInfo.ChannelState.Offline);
                            }
                            channelInfo.setName(title);

                            channels.add(channelInfo);
                            channelIndex++;
                        }
                        break;
                }
                event = parser.next();
            }

            return channels;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        throw new BusinessException();
    }


    @Override
    public void onMsg(int session, String msg, int dataSize) {
        LogHelper.d(TAG, "cmu session:" + session + " onMsg:\n" + msg);
        try {
            String strResult = msg;
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            String currentCmd = "";
            boolean needRefreshDeviceTree = false;

            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("root".equals(parser.getName())) {
                            currentCmd = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "cmd");
                            if (session != mCmuRealHandle
                                    && !"onlineNotify".equals(currentCmd)) {
                                return;
                            }
                            if ("reportMasterInfo".equals(currentCmd)) {
                                reportMasterInfo(strResult);
                            } else if ("subscribeNotify".equals(currentCmd)) {
                                needRefreshDeviceTree = true;
                            }
                        } else if ("server".equals(parser.getName())) {
                            int newSession = session;
                            int sessionStatus = Integer.parseInt(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "status"));
                            if (sessionStatus == 0) {
                                LogHelper.d(TAG, "cmu session offline");
                            } else {
                                LogHelper.d(TAG, "cmu session new:" + newSession);
                            }
                            mCmuRealHandle = newSession;
                        } else if ("intitem".equals(parser.getName())) {
                            try {
                                int intitem = Integer.parseInt(parser.nextText());
                                // 手机等绑定相机信息    不刷新设备树
                                if (intitem == 27) {
                                    needRefreshDeviceTree = false;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                }
                event = parser.next();
            }

            if (needRefreshDeviceTree) {
                synchronized (mDeviceTreeListenerLock) {
                    if (mDeviceTreeListener != null) {
                        mDeviceTreeListener.onChange();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void reportMasterInfo(String strResult) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        String modulename = null;
        String ipinfo = null;
        int svrid = 0;
        String domid = null;
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("modulename".equals(parser.getName())) {
                        modulename = parser.nextText();
                    } else if ("ipinfo".equals(parser.getName())) {
                        ipinfo = parser.nextText();
                    } else if ("svrid".equals(parser.getName())) {
                        svrid = MspCommon.parseIntIgnoreNull(parser.nextText());
                    } else if ("domid".equals(parser.getName())) {
                        domid = parser.nextText();
                    }
                default:
            }
            event = parser.next();
        }
        if (TextUtils.isEmpty(modulename) || TextUtils.isEmpty(ipinfo) || TextUtils.isEmpty(domid)) {
            return;
        }

        // 第一次上报服务配置信息或者该服务配置信息变更时调用连接
        // 其它情况不处理（如重连时也会上报,SDK会自动重连）


        String ip = mServerIpMap.get(modulename);
        if (TextUtils.equals(ip, ipinfo)) {
            LogHelper.i(TAG, "is connect ip:" + ip + " modulename:" + modulename);
            return;
        }

        int errorcode = -1;
        try {
            switch (modulename) {
                case "VTDU":
                    VtduClient.getInstance().connect(mCmuRealHandle, ipinfo, domid, svrid);
                    break;
                case "DMU":
                    DmuClient.getInstance().connect(mCmuRealHandle, ipinfo, domid, svrid);
                    break;
                case "MCU":
                    McuClient.getInstance().connect(mCmuRealHandle, ipinfo, domid, svrid);
                    break;
                case "LCU":
                    LcuClient.getInstance().connect(mCmuRealHandle, ipinfo, domid, svrid);
                    break;
                case "FS":
                    FsClient.getInstance().connect(mCmuRealHandle, ipinfo, domid, svrid);
                    break;
                case "PCC":
                    PccClient.getInstance().connect(mCmuRealHandle, ipinfo, domid, svrid);
                    break;
                case "IMDS_SIP":
                    // TODO: 2017/3/20
                    break;
            }
            mServerIpMap.put(modulename, ipinfo);
        } catch (BusinessException e) {
            LogHelper.e(TAG, "reportMasterInfo connect" + modulename + " fail");
            e.printStackTrace();
            errorcode = e.errorCode;
        } catch (Exception e) {
            LogHelper.e(TAG, "reportMasterInfo connect" + modulename + " fail");
            e.printStackTrace();
            errorcode = BusinessErrorCode.BEC_COMMON_FAIL;
        }

        if (errorcode != -1) {
            synchronized (mServerStatusListenerLock) {
                if (mServerStatusListener != null) {
                    mServerStatusListener.onStatusChange(modulename, 0, errorcode);
                }
            }
        }

    }

    public String getServerIP(String modulename) {
        return mServerIpMap.get(modulename);
    }


}
