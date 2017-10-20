package com.project.enn.dahua;

/**
 * Created by Comclay on 2017/4/20.
 * 大华服务
 */

public interface IDaHuaService {
    String DAHUA_SIGNAL = "DAHUA_SIGNAL";
    String DAHUA_ERROR_CODE = "DAHUA_ERROR_CODE";
    String DAHUA_PARAM_NUMBER = "DAHUA_PARAM_NUMBER";

    String UNKNOW = "UNKNOW";
    String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    String LOGIN_FAILED = "LOGIN_FAILED";
    String LOGOUT_SUCCESS = "LOGOUT_SUCCESS";
    String LOGOUT_FAILED = "LOGOUT_FAILED";
    String LIVE_START = "LIVE_START";
    String LIVE_FINISH = "LIVE_FINISH";
    String SIP_REGIST_SUCCESS = "SIP_REGIST_SUCCESS";
    String SIP_REGIST_FAILED = "SIP_REGIST_FAILED";
    String SIP_RING = "SIP_RING";
    String SIP_HOLD = "SIP_HOLD";
    String SIP_HUNGUP = "SIP_HUNGUP";
    String CONNECT_SUCCESS = "CONNECT_SUCCESS";

    String PHONE_NUMBER = "PHONE_NUMBER";
    String COME_IPHONE_NAME = "COME_IPHONE_NAME";

    void showToast(String msg);

    String getDaHuaLoginName();

    String getDaHuaLoginPassword();

    void loginSuccess();

    void loginFailed(int errCode);

    void logoutSuccess();

    void logoutFailed(int errCode);

    void onSipRegistSuccess();

    void onSipRegistFailed();

    void onSipRing(String number);

    void onSipHold(String number);

    void onSipHungup();

    void onStartLive();

    void onFinishLive();

    void connectSuccess();

    void enterComeIPhoneActivity(String number, String name);

    void enterDaHuaLiveActivity();

    void sendSignalBroadCast(String signal);

    void sendErrSignalBroadcast(String signal, int errCode);
}
