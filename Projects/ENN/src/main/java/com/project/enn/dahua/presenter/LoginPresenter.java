package com.project.enn.dahua.presenter;

import android.os.AsyncTask;
import android.util.Log;

import com.android.business.client.msp.CmuClient;
import com.android.business.client.msp.SDKExceptionDefine;
import com.android.business.exception.BusinessException;
import com.mapgis.mmt.BasePresenter;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.project.enn.dahua.Constant;
import com.project.enn.dahua.IDaHuaService;

/**
 * Created by Comclay on 2017/3/28.
 * 登录登出操作
 */

class LoginPresenter implements BasePresenter {
    private IDaHuaService mDaHuaService;

    LoginPresenter(IDaHuaService daHuaService) {
        this.mDaHuaService = daHuaService;
    }

    public void login(String ip, String port, String user, String password, String imei) {
        new AsyncTask<String, Void, Integer>() {
            protected Integer doInBackground(String... params) {
                try {
                    CmuClient.getInstance().login(params[0]
                            , Integer.valueOf(params[1]), params[2]
                            , params[3], params[4], Constant.TIME_OUT);
                    return 200;
                } catch (BusinessException e) {
                    e.printStackTrace();
                    return e.errorCode;
                }
            }

            protected void onPostExecute(Integer integer) {
                if (integer == SDKExceptionDefine.Success.getCode() || integer == 1) {
                    mDaHuaService.loginSuccess();
                } else {
                    mDaHuaService.loginFailed(integer);
                }
            }
        }.execute(ip, port, user, password, imei);
    }

    /**
     * 登出
     */
    public void logout() {
        new AsyncTask<Void, Void, Integer>() {
            protected Integer doInBackground(Void... params) {
                try {
                    CmuClient.getInstance().logout();
//                    zondyLogout();
                    return SDKExceptionDefine.Success.getCode();
                } catch (BusinessException e) {
                    e.printStackTrace();
                    return e.errorCode;
                }
            }

            protected void onPostExecute(Integer resultCode) {
                if (resultCode == SDKExceptionDefine.Success.getCode()) {
                    // 登出成功
                    mDaHuaService.logoutSuccess();
                } else {
                    // 登出失败
                    mDaHuaService.logoutFailed(resultCode);
                }
            }
        }.execute();
    }

    /*zondy退出大华时所做的操作*/
    private void zondyLogout() {
        Log.w("zondyLogout", "zondyLogout: 正在退出大华部分");
        String userName = Constant.getName();
        String sb = ServerConnectConfig.getInstance().getBaseServerPath() +
                "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/LogoutDHApp?" +
                "userName=" + userName;
        NetUtil.executeHttpGet(sb);
    }
}
