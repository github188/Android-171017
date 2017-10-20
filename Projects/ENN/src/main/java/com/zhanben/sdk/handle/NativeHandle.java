package com.zhanben.sdk.handle;

public class NativeHandle {
    private static final String[] moduleArray = new String[]{
            "MCU",
            "FS",
            "IMDS_SIP",
            "LCU",
            "PCC",
            "VTDU"};
    static {
        System.loadLibrary("gnustl_shared");
    }
    static {
        System.loadLibrary("json");
        System.loadLibrary("framework");
        System.loadLibrary("ibp_utils");
        System.loadLibrary("sdk_framework");
        System.loadLibrary("streamTrans1.1");
        System.loadLibrary("new_dhtp_protocol");
        System.loadLibrary("rtsp_protocol");
        System.loadLibrary("rtpProtocol");
        System.loadLibrary("vru_sdk");
        System.loadLibrary("vtdu_sdk");
        System.loadLibrary("cmu_sdk");          
        System.loadLibrary("dmu_sdk");
        System.loadLibrary("imds_protocol");
        System.loadLibrary("imds_sdk");
        System.loadLibrary("mcu_sdk");
        System.loadLibrary("vmu_sdk");
        System.loadLibrary("fs_sdk");
        System.loadLibrary("lcu_sdk");
        System.loadLibrary("pcc_sdk");
        System.loadLibrary("softphone");
        System.loadLibrary("dhibpjdkjni");
    }
    private NativeHandle() {
        init();
    }
    /*public static NativeHandle nativeHandle = new NativeHandle();
    public static NativeHandle getInstance() {
        return nativeHandle;
    }*/

    private static NativeHandle instance;

    private final static Object initObj = new Object();

    public static NativeHandle getInstance() {
        if (instance == null) {
            synchronized (initObj) {
                if (instance == null) {
                    instance = new NativeHandle();
                }
            }
        }
        return instance;
    }

    public int init() {
        int result = 0;
        result = CMU_SC_Init();
        if (result != 200) {
            destroy();
            return result;
        }
        result = VTDU_SC_Init();
        if (result != 200) {
            destroy();
            return result;
        }
        result = DMU_SC_Init();
        if (result != 200) {
            destroy();
            return result;
        }
        result = MCU_SC_Init();
        if (result != 200) {
            destroy();
            return result;
        }
        result = LCU_SC_Init();
        if (result != 200) {
            destroy();
            return result;
        }
        result = FS_SC_Init();
        if (result != 200) {
            destroy();
            return result;
        }
        result = PCC_SC_Init();
        if (result != 200) {
            destroy();
            return result;
        }
        return 200;
    }

    public void unInit(){
        instance = null;
    }

    public int destroy() {
        CMU_SC_Cleanup();
        VTDU_SC_Cleanup();
        DMU_SC_Cleanup();
        MCU_SC_Cleanup();
        LCU_SC_Cleanup();
        FS_SC_Cleanup();
//        softphone_destroy();
        PCC_SC_Cleanup();
        return 0;
    }
//    public native String test();

    /********************************** CMU 方舟 *****************************************/
    public native int CMU_SC_Init();
    public native int CMU_SC_Cleanup();
    public native int CMU_SC_LoginEx(int timeout, String info, char[] outxml, int maxlen);
    public native int CMU_SC_Logout(int session);
    public native int CMU_SC_SetMsg(int session);
    public native int CMU_SC_OptSubscribe(int session, int timeout, String info);
    public native int CMU_SC_QueryAllOrg(int session, int timeout, char[] outXml, int length);
    public native int CMU_SC_QueryDeviceByFuncIdEx(int handle, int timeout, String domid,
                                                   String orgcode, String devtype, int funcid,
                                                   char[] outxml, int maxlen);
    public native int CMU_SC_QueryDevDetail(int session, int timeout, String domid, String devcode,
                                            int devtype, char[] outxml, int length);
    public native int CMU_SC_QueryAllUser(int session, int timeout, char[] outxml, int length);
    public native int CMU_SC_QueryOnlineUser(int session, int timeout, char[] outxml, int length);
    // jni callback
    private void OnCmuMsgCallBack(int session, String msg, int dataSize) {
        synchronized (mCmuNotifyListenerLock) {
            if (mCmuNotifyListener != null) {
                mCmuNotifyListener.onMsg(session, msg, dataSize);
            }
        }
    }

    private CmuNotifyListener mCmuNotifyListener;
    private Object mCmuNotifyListenerLock = new Object();

    public void setCmuNotifyListener(CmuNotifyListener listener){
        synchronized (mCmuNotifyListenerLock) {
            mCmuNotifyListener = listener;
        }
    }

    public interface CmuNotifyListener{
        void onMsg(int session, String msg, int dataSize);
    }


    /********************************** VTDU  蔡涛*****************************************/
    public native int VTDU_SC_Init();
    public native int VTDU_SC_Cleanup();
    public native int VTDU_AC_Connect(int cmuLoginHandle, String ipInfo, String svrDomId, int svrId, char[] xmlOutput, int maxlen);
    public native int VTDU_AC_Disconnect(int vtduSession);
    public native int VTDU_SC_SetCallBack(int vtduSession);
    public native int VTDU_SC_StartVideo_I(int vtduSession, int timeout, String xmlInput, char[] xmlOutput, int maxlen);
    public native int VTDU_SC_StartVideo(int vtduSession, int timeout, String xmlInput, char[] xmlOutput, int maxlen);
    public native int VTDU_SC_SetRealDataCallBack(int realHandle, Object buffer, int len);
    public native int VTDU_SC_Stop(int realHandle, int timeout);
    public native int VTDU_SC_RealTalkingEx_I(int vtduSession,int timeout,String domainId,String channelCode,int talkType,int transType,int streamPackType,
        int convType,int audioEncodeType,int audioSamplingRate,int audioChannelNum,char[] xmlOutput,int maxLen);
    public native int VTDU_SC_SendTalkData(int realHandle,byte[] buffer,int bufferLen);
    // jni callback 消息通知
    private void OnVtduMsgCallBack(int session, String msg, int dataSize) {
        synchronized (mVtduListenerLock){
            mVtduNotifyListener.onMsg(session, msg, dataSize);
        }
    }

    // 视频实时数据回调
    private void OnVtduRealDataCallBack(int handle, int headtype, int type, int datasize){
        synchronized (mVtduDataListenerLock){
            mVtduDataNotifyListener.onData(handle, headtype, type, datasize);
        }
    }

    private VtduDataNotifyListener mVtduDataNotifyListener;
    private Object mVtduDataListenerLock = new Object();

    public interface VtduDataNotifyListener {
        public void onData(int handle, int headtype, int type, int datasize);
    }

    public void setVtduDataNotifyListener(VtduDataNotifyListener listener){
       synchronized (mVtduDataListenerLock){
           this.mVtduDataNotifyListener = listener;
       }
    }

    private VtduNotifyListener mVtduNotifyListener;
    private Object mVtduListenerLock = new Object();

    public void setVtduNotifyListener(VtduNotifyListener listener) {
        synchronized (mVtduListenerLock){
        this.mVtduNotifyListener = listener;

        }
    }

    public interface VtduNotifyListener{
        public void onMsg(int session, String msg, int dataSize);
    }


    /********************************** MCU *****************************************/
    public native int MCU_SC_Init();
    public native int MCU_SC_Cleanup();
    public native int MCU_AC_Connect(int userSession, String ipinfo, String svrdomid, int serverid, char[] outxml, int maxlen);
    public native int MCU_SC_Disconn(int session);
    public native int MCU_SC_SetMCUMsg(int session);

    public native int MCU_SC_OptMeetingCfgInfo(int session, int timeout, String info);
    public native int MCU_SC_QueryMeetingCfgInfo(int session, int timeout, String info, char[] outxml, int maxlen);
    public native int MCU_SC_QueryAllContact(int session, int timeout, String info, char[] outxml, int maxlen);
    public native int MCU_SC_LoginMeetingRoom(int session, int timeout, int userid, String domid, int meetid, String pwd, int flag, char[] outxml, int maxlen);
    public native int MCU_SC_QueryMeetingUserInfo(int session, int timeout, String roomDomId, int roomid, char[] outxml, int maxlen);
    public native int MCU_SC_QueryMeetingDeviceInfo(int session, int timeout, char[] outxml, int maxlen);
    public native int MCU_SC_OptUserMute(int session, int timeout, String userDomid, int userid, long sessionId, String meetDomid, int meetid, int flag);
    public native int MCU_SC_OptMeetingMember(int session, int timeout, String info, char[] outxml, int maxlen);
    public native int MCU_SC_QueryMeetingFilePage(int session, int timeout, String roomDomId, int roomid, char[] outxml, int maxlen);
    public native int MCU_SC_OptMeetingFile(int session, int timeout, int opt, String roomDomId, int roomid, String filename, int total, int current);
    public native int MCU_SC_OptMeetingCurrentPage(int session, int timeout, int opt, String roomDomId, int roomid, String filename, int total, int current);
    public native int MCU_SC_ExitMeeting(int session, int timeout, String userDomid, int userid, String meetDomid, int meetid);
    public native int MCU_SC_Call(int session, int timeout, String userDomid, int userid, String telphone);
    public native int MCU_SC_QueryCallRecord(int session, int timeout, String info, char[] outxml, int maxlen);
    public native int MCU_SC_OptCallRecord(int session, int timeout, String info, char[] outxml, int maxlen);
    // jni callback
    private void OnMcuMsgCallBack(int session, String msg, int dataSize) {
        synchronized (mMcuNotifyListenerLock) {
            if (mMcuNotifyListener != null) {
                mMcuNotifyListener.onMsg(session, msg, dataSize);
            }
        }
    }

    private McuNotifyListener mMcuNotifyListener;
    private Object mMcuNotifyListenerLock = new Object();

    public void setMcuNotifyListener(McuNotifyListener listener){
        synchronized (mMcuNotifyListenerLock) {
            mMcuNotifyListener = listener;
        }
    }

    public interface McuNotifyListener{
        void onMsg(int session, String msg, int dataSize);
    }



    /********************************** DMU 朱安波*****************************************/
    public native int DMU_SC_Init();
    public native int DMU_SC_Cleanup();
    public native int DMU_AC_Connect(int cmusession, String info, String svrdomid, int serverid, char[] outxml, int len);
    public native int DMU_SC_Disconn(int session);
    public native int DMU_SC_SetDmuMsg(int mduSession);
    public native int DMU_SC_PTZControl(int session, int timeout, String info, char[] outxml, int len);
    public native int DMU_SC_SendClientAlarm(int session, int timeout, String inxml);
    public native int DMU_SC_SendClientRealData(int session, int timeout, String inxml);
    // jni callback
    private void OnDmuMsgCallBack(int session, String msg, int dataSize) {
        synchronized (mDmuNotifyListenerLock) {
            if (mDmuNotifyListener != null) {
                mDmuNotifyListener.onMsg(session, msg, dataSize);
            }
        }
    }

    private DmuNotifyListener mDmuNotifyListener;
    private Object mDmuNotifyListenerLock = new Object();

    public void setDmuNotifyListener(DmuNotifyListener listener){
        synchronized (mDmuNotifyListenerLock) {
            mDmuNotifyListener = listener;
        }
    }

    public interface DmuNotifyListener{
        void onMsg(int session, String msg, int dataSize);
    }

    /********************************** LCU *****************************************/

    public native int LCU_SC_Init();
    public native int LCU_SC_Cleanup();
    public native int LCU_AC_Connect(int cmusession, String info, String svrdomid, int serverid, char[] outxml, int len);
    public native int LCU_SC_Disconn(int session);
    public native int LCU_SC_SetLCUMsg(int lcuSession);
    public native int LCU_SC_GetQueryInfo(int session, int timeout, String condition, char[] outxml, int len);
    public native int LCU_SC_ReceiveFile(int session, int timeout, String info);
    public native int LCU_SC_SendMessge(int session, int timeout, String msg, String ctrlInfo, String toDomainId, int toUserId, int toUserSession, int tag);
    public native int LCU_SC_SendFile(int session, int timeout, String toDomainId, int toUserId, int toUserSession, String info, String guid);
    public native int LCU_SC_ChangTaskStatus(int session, int timeout, int id, int status);
    // jni callback
    private void OnLcuMsgCallBack(int session, String msg, int dataSize) {
        synchronized (mLcuMsgNotifyListenerLock){
            if(mLcuMsgNotifyListener != null){
                mLcuMsgNotifyListener.onMsg(session, msg, dataSize);
            }
        }
    }

    private LcuMsgNotifyListener mLcuMsgNotifyListener;
    private Object mLcuMsgNotifyListenerLock = new Object();

    public void setLcuMsgListener(LcuMsgNotifyListener listener){
        synchronized (mLcuMsgNotifyListenerLock){
            this.mLcuMsgNotifyListener = listener;
        }
    }

    public interface LcuMsgNotifyListener{
        public void onMsg(int session, String msg, int dataSize);
    }



    /********************************** FS *****************************************/
    public native int FS_SC_Init();
    public native int FS_SC_Cleanup();
    public native int FS_AC_Connect(int cmusession, String info, String svrdomid, int serverid, char[] outxml, int len);
    public native int FS_SC_Disconn(int session);
    public native int FS_SC_SetFSMsg(int fsSession);
    public native int FS_SC_DownloadFile(int session, int timeout, String info);
    public native int FS_SC_UploadFile(int session, int timeout, String info, char[] outxml, int len);
    public native int FS_SC_QueryFile(int session, int timeout, String info, char[] outxml, int len);
    // jni callback
    private void OnFsMsgCallBack(int session, String msg, int dataSize) {
        synchronized (mFSNotifyListenerLock){
            if(mFSNotifyListener != null){
                mFSNotifyListener.onMsg(session, msg, dataSize);
            }
        }
    }

    private FSNotifyListener mFSNotifyListener;
    private Object mFSNotifyListenerLock = new Object();

    public void setFSNotifyListener(FSNotifyListener listener){
        synchronized (mFSNotifyListenerLock) {
            mFSNotifyListener = listener;
        }
    }

    public interface FSNotifyListener{
        void onMsg(int session, String msg, int dataSize);
    }

    /********************************** PCC *****************************************/
    public native int PCC_SC_Init();
    public native int PCC_SC_Cleanup();
    public native int PCC_AC_Connect(int cmusession, String info, String svrdomid, int serverid,
                                     char[] outxml, int len);
    public native int PCC_SC_Disconn(int session);
    public native int PCC_SC_SetPccMsg(int pccSession);
    public native int PCC_SC_RegPccDev(int session, int timeout, String info);
    public native int PCC_SC_StartVideo(String ipinfo, int timeout);
    public native int PCC_SC_StartVideoEx(String ipinfo, int transportProtocol, int timeout);
    public native int PCC_SC_CloseVideo();
    public native int PCC_SC_SendVideoData(byte[] buf, int bufsize, int datatype, int nwidth,
                                           int nheight);
    public native int PCC_SC_GetUserDevCode(char[] outxml, int len);
    public native int PCC_SC_GetDefaultDevInfo(char[] outxml, int len);
    public native int PCC_SC_SetDefaultDevInfo(String info);
    public native int PCC_SC_CreateDevice(String info);
    public native int PCC_SC_DestroyDevice();
    public native int PCC_SC_SendPccErrCode(int session, int seq, int ec);
    public native int PCC_SC_SendAnnounce(int session, int timeout);
    // jni callback
    private void OnPccMsgCallBack(int session, String msg, int dataSize) {
        synchronized (mPccNotifyListenerLock){
            mPccNotifyListener.onMsg(session, msg, dataSize);
        }
    }

    private PccNotifyListener mPccNotifyListener;
    private Object mPccNotifyListenerLock = new Object();

    public void setPccNotifyListener(PccNotifyListener listener){
        synchronized (mPccNotifyListenerLock){
            this.mPccNotifyListener = listener;
        }
    }

    public interface PccNotifyListener{
        void onMsg(int session, String msg, int dataSize);
    }


    /********************************** 话机 *****************************************/
    public native int softphone_initial();
    public native void softphone_destroy();
    public native void softphone_set_auto_answer(int enable);
    public native int softphone_set_account(String username, String password, String domain, int expires);
    public native int softphone_clear_account();
    public native int softphone_callout(String telephone);
    public native int softphone_answer();
    public native void softphone_hangup();
    public native int softphone_hold();
    // 0静音
    public native void softphoneSetInputVolume(int volume);
    // 0静音
    public native void softphoneSetOutputVolume(int volume);
    // 回音消除（单位为毫秒，一般填40效果较好,默认为0表示不需要EC）
    public native void softphoneSetEcDelay(int delay);
    // jni callback
    private void OnSoftphoneMsgCallBack(int code, String number, int dataSize) {
        synchronized (mSoftPhoneNotifyListenerLock){
            mSoftPhoneNotifyListener.onMsg(code, number, dataSize);
        }
    }

    private SoftPhoneNotifyListener mSoftPhoneNotifyListener;
    private Object mSoftPhoneNotifyListenerLock = new Object();

    public void setSoftPhoneNotifyListener(SoftPhoneNotifyListener listener){
        synchronized (mSoftPhoneNotifyListenerLock){
            this.mSoftPhoneNotifyListener = listener;
        }
    }

    public interface SoftPhoneNotifyListener{
        void onMsg(int code, String number, int dataSize);
    }



    public native int PackDh(byte[] buf, int nbuflen, int datatype, int nwidth, int nheight,
                             byte[] pOutBuf, int maxLen, Integer nOutLen);
}
