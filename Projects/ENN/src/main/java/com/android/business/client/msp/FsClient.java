package com.android.business.client.msp;

import android.util.Xml;

import com.android.business.entity.FSFileInfo;
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
 * 功能说明：FS平台接口
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-20
 */
public class FsClient implements NativeHandle.FSNotifyListener {
    private String TAG = FsClient.class.getSimpleName();
    private int mSession;

    private FsClient() {
        NativeHandle.getInstance().setFSNotifyListener(this);
    }


    /*private static class Instance {
        private static FsClient instance = new FsClient();
    }

    public static FsClient getInstance() {
        return Instance.instance;
    }*/

    private static FsClient instance;

    private final static Object initObj = new Object();

    public static FsClient getInstance() {
        if (instance == null) {
            synchronized (initObj) {
                if (instance == null) {
                    instance = new FsClient();
                }
            }
        }
        return instance;
    }

    @Override
    public void onMsg(int session, String msg, int dataSize) {
        LogHelper.d(TAG, "fs 回调消息:\n" + msg);
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
                                LogHelper.d(TAG, "FS session offline");
//                                newSession = 0;
                            } else {
                                LogHelper.d(TAG, "FS session new:" + newSession);
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


    void connect(int cmuRealHandle, String ip, String domid, int svrId) throws Exception {
        char[] buffer = new char[1024];
        Arrays.fill(buffer, '\0');
        int cmuSession = cmuRealHandle;
        int result = NativeHandle.getInstance().FS_AC_Connect(cmuSession, ip, domid, svrId,
                buffer, 1024);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        String strResult = new String(buffer);
        LogHelper.d(TAG, "FS_AC_Connect result:\n" + strResult);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(strResult));
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if ("handle".equals(parser.getName())) {
                        int session = MspCommon.parseIntIgnoreNull(parser.nextText());
                        mSession = session;
                        NativeHandle.getInstance().FS_SC_SetFSMsg(mSession);
                    }
                    break;
            }
            event = parser.next();
        }
    }

    void disConnect() {
        NativeHandle.getInstance().FS_SC_Disconn(mSession);
        mSession = -1;
//        instance = null;
    }

    public void downloadFile(String domid, String guid, String localPath, int timeout) throws BusinessException {
        String inputXml =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <domid>" + domid + "</domid>\n" +
                        "    <guid>" + guid + "</guid>\n" +
                        "    <filepath>" + localPath + "</filepath>\n" +
                        "</root>";
        LogHelper.d(TAG, "FsDownloadFile input:\n" + inputXml);
        char[] buffer = new char[10240];
        Arrays.fill(buffer, '\0');
        int result = NativeHandle.getInstance().FS_SC_DownloadFile(mSession, timeout,
                inputXml);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }

    public void uploadFile(String uploader, String domid, String localPath, String filename, String modulename, int mainid, String subid, int timeout) throws BusinessException {
        String inputXml =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <domainid>" + domid + "</domainid>\n" +
                        "    <filepath>" + localPath + filename + "</filepath>\n" +
                        "    <modulename>" + modulename + "</modulename>\n" +
                        "    <mainid>" + mainid + "</mainid>\n" +
                        "    <subid>" + subid + "</subid>\n" +
                        "    <validtime>99999999</validtime>\n" +
                        "    <uploader>" + uploader + "</uploader>\n" +
                        "    <desc>1</desc>\n" +
                        "</root>";
        LogHelper.d(TAG, "FsUploadFile input:\n" + inputXml);
        char[] buffer = new char[10240];
        int result = NativeHandle.getInstance().FS_SC_UploadFile(mSession, timeout,
                inputXml, buffer, 10240);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
    }

    public List<FSFileInfo> queryFile(String domid, String moduleName, String mainid, String subid, int curepage, int pagesize, int timeout) throws BusinessException {
        char[] buffer = new char[102400];
        Arrays.fill(buffer, '\0');
        String queryFile =
                "<?xml version='1.0' encoding='utf-8' standalone='no' ?>\n" +
                        "<root>\n" +
                        "    <domainid>" + domid + "</domainid>\n" +
                        "    <filename></filename>\n" +
                        "    <modulename>" + moduleName + "</modulename>\n" +
                        "    <mainid>" + mainid + "</mainid>\n" +
                        "    <subid>" + subid + "</subid>\n" +
                        "    <size>" + pagesize + "</size>\n" +
                        "    <curpage>" + curepage + "</curpage>\n" +
                        "</root>";
        int result = NativeHandle.getInstance().FS_SC_QueryFile(mSession, timeout,
                queryFile, buffer, 102400);
        result = MspCommon.changeRes(result);
        if (result != BusinessErrorCode.BEC_COMMON_SUCCESS) {
            throw new BusinessException(result);
        }
        try {
            String strResult = new String(buffer);
            strResult = new String(strResult.getBytes("iso8859-1"));
            LogHelper.d(TAG, "FS_SC_QueryFile result:\n" + strResult);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(strResult));
            int event = parser.getEventType();
            List<FSFileInfo> fsFileInfos = new ArrayList<>();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("item".equals(parser.getName())) {
                            FSFileInfo info = new FSFileInfo();
                            info.setDomid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "domainid"));
                            info.setFilename(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "filename"));
                            info.setModulename(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "modulename"));
                            info.setMainid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "mainid"));
                            info.setSubid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "subid"));
                            info.setGuid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "guid"));
                            info.setUploaderdomainid(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "uploaderdomainid"));
                            info.setUploader(parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "uploader"));
                            fsFileInfos.add(info);
                        }
                        break;
                }
                event = parser.next();
            }
            return fsFileInfos;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        throw new BusinessException();
    }
}
