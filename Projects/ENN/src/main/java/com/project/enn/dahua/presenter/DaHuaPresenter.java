package com.project.enn.dahua.presenter;

import com.android.business.client.listener.AlarmMeetingListener;
import com.android.business.client.msp.PccClient;
import com.project.enn.dahua.Constant;
import com.project.enn.ENNApplication;
import com.project.enn.dahua.IDaHuaService;

/**
 * Created by Comclay on 2017/4/20.
 */

public class DaHuaPresenter {
    private static final String TAG = "DaHuaPresenter";
    private IDaHuaService mDaHuaService;
    private LoginPresenter mLoginPresenter;
    private SipPresenter mSipresenter;

    public DaHuaPresenter(IDaHuaService daHuaService) {
        mDaHuaService = daHuaService;
        mLoginPresenter = new LoginPresenter(daHuaService);
        mSipresenter = new SipPresenter(daHuaService);
    }

    /**
     * 初始化大华科技
     */
    public void initDaHuaTech() {
        PccClient.getInstance().init(ENNApplication.getInstance().getApplicationContext());
        mSipresenter.initSip();
        setAlarmListener();
    }

    public void loginDaHuaTech() {
        this.mLoginPresenter.login(Constant.getIP(), String.valueOf(Constant.getPort())
                , mDaHuaService.getDaHuaLoginName(), Constant.getPassword(), Constant.getImei());
    }

    private void setAlarmListener() {
        PccClient.getInstance().setAlarmMeetingListener(new AlarmMeetingListener() {
            @Override
            public void onStart(int handle) {
                mDaHuaService.onStartLive();
            }

            @Override
            public void onFinish() {
                mDaHuaService.onFinishLive();
            }
        });
    }

    /**
     * 退出大华科技
     */
    public void uninitDaHuaTech() {
        try {
            this.mSipresenter.stopHeartService();
            this.mSipresenter.unregister();
            this.mLoginPresenter.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void autoAnswer() {
        mSipresenter.autoAnswer();
    }

    public void startHeartService() {
        mSipresenter.startHeartService();
    }
}
