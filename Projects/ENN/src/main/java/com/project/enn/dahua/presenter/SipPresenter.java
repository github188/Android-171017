package com.project.enn.dahua.presenter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.android.business.client.listener.UserPhoneNumListener;
import com.android.business.client.msp.McuClient;
import com.android.business.client.msp.PccClient;
import com.android.business.client.msp.SoftPhoneClient;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BasePresenter;
import com.mapgis.mmt.MyApplication;
import com.project.enn.dahua.Constant;
import com.project.enn.dahua.IDaHuaService;
import com.project.enn.dahua.ServiceHelper;
import com.project.enn.dahua.activity.ComeIPhoneActivity;
import com.project.enn.dahua.service.DaHuaService;
import com.zhanben.sdk.handle.NativeHandle;

import java.util.Locale;

/**
 * Created by Comclay on 2017/3/31.
 * 语音相关的Presenter
 */

class SipPresenter implements BasePresenter {
    private static final String TAG = "Alarm";
    private IDaHuaService mDaHuaService;
    private Handler mHandler;
    private boolean isReisterSuccess = false;
    private final Object mObjLock = new Object();

    private int status = -1;

    SipPresenter(IDaHuaService daHuaService) {
        this.mDaHuaService = daHuaService;
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void setSipListener() {
        // 监听来电
        SoftPhoneClient.getInstance().setSoftPhoneListener(new NativeHandle.SoftPhoneNotifyListener() {
            @Override
            public void onMsg(int code, String number, int dataSize) {
                Log.w(TAG, String.format(Locale.CHINA, "电话通知监听：code=%d，number=%s，dataSize=%d", code, number, dataSize));
                status = code;
                switch (code) {
                    case 0:
                        // 待机
                        break;
                    case 1:
                        // 呼入
                        break;
                    case 2:
                        // 响铃
                        onSipRing(number);
                        break;
                    case 3:
                        // 通话中
                        mDaHuaService.onSipHold(number);
                        break;
                    case 4:
                        // 挂机
                        hungup();
                        break;
                    case 5:
                        // 注册成功
                        onRegisterSuccess();
                        break;
                    case 6:
                        //SOFTPHONE反注册成功
                        break;
                    case 7:
                        // 注册失败
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDaHuaService.onSipRegistFailed();
                            }
                        });
                        break;
                    default:
                }
            }


        });
    }
    /*注册成功*/
    private void onRegisterSuccess() {
        Log.w(TAG, "sipRegisterSuccess: 话机注册成功，注册号码为：" + McuClient.getInstance().getSoftPhoneCallnumber());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDaHuaService.onSipRegistSuccess();
                String softPhoneCallnumber = McuClient.getInstance().getSoftPhoneCallnumber();
                // 重新分配
                if (isReisterSuccess) {
                    mDaHuaService.showToast("重新分配内部号码" + softPhoneCallnumber);
                    // 重新上传到zondy服务器
                    startHeartService();
                    return;
                }
                // 第一次分配成功
                mDaHuaService.showToast("话机注册成功：" + McuClient.getInstance().getSoftPhoneCallnumber());
                isReisterSuccess = true;
                synchronized (mObjLock) {
                    boolean isRegisted = PccClient.getInstance().isPccDeviceRegisted();
                    if (isRegisted) {
                        mDaHuaService.connectSuccess();
                        return;
                    }
                    PccClient.getInstance().addRegisterPccDeviceListener(new PccClient.OnRegistPccDeviceListener() {
                        @Override
                        public void onRegisted() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mDaHuaService.connectSuccess();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    /*启动报警心跳服务,将该服务绑定到DaHuaService上*/
    public void startHeartService() {
        /*if (mDaHuaService instanceof DaHuaService) {
            Service service = (DaHuaService) mDaHuaService;
            Intent intent = new Intent(service, AlarmHeartService.class);
            service.startService(intent);
        }*/
        ServiceHelper.getInstance().bindHeartService((DaHuaService) mDaHuaService);
    }

    /*停止报警心跳服务*/
    public void stopHeartService() {
        /*if (mDaHuaService instanceof DaHuaService && AlarmHeartService.isRun) {
            Service service = (DaHuaService) mDaHuaService;
            Intent intent = new Intent(service, AlarmHeartService.class);
            service.stopService(intent);
        }*/
        ServiceHelper.getInstance().unbindHeartService((DaHuaService) mDaHuaService);
    }

    /*响铃*/
    private void onSipRing(final String number) {
        // 如果正在传输视频则自动接听，否则弹出接听界面
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean openVideo = PccClient.getInstance().isOpenVideo();
                if (openVideo) {
                    // 视频已经开启自动接听
                    autoAnswer();
                }else{
                    // 未开启视频手动接听
//                    mDaHuaService.sendSignalBroadCast(IDaHuaService.SIP_RING);
                    mDaHuaService.onSipRing(number);
                }
            }
        });
    }

    public void autoAnswer() {
        answer();
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(MyApplication.getInstance().getApplicationContext(), notification);
        r.play();
    }

    /**
     * 注册一个话机
     */
    void initSip() {
        SoftPhoneClient.getInstance().init();
//        isReisterSuccess = true;
        SoftPhoneClient.getInstance().setAutoAnswer(false);
        setSipListener();
        McuClient.getInstance().setUserPhoneNumListener(new UserPhoneNumListener() {
            @Override
            public void onBind() {
                register();
            }
        });
    }

    private void register() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    SoftPhoneClient.getInstance().register(Constant.getSiport());
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /*挂机*/
    private void hungup() {
        SoftPhoneClient.getInstance().hangup();
        mDaHuaService.onSipHungup();
        Activity activity = AppManager.getActivity(ComeIPhoneActivity.class);
        if ( activity != null){
            activity.finish();
        }
    }

    /*接听*/
    private void answer() {
        SoftPhoneClient.getInstance().answer();
    }

    /*拨打*/
    public void call(String phoneNumber) {
        SoftPhoneClient.getInstance().call(phoneNumber);
    }

    /**
     * 取消注册话机
     */
    void unregister() {
        try {
            SoftPhoneClient.getInstance().setSoftPhoneListener(null);
            McuClient.getInstance().setUserPhoneNumListener(null);
            if (status >= 0 && status <= 5) {
                SoftPhoneClient.getInstance().unRegister();
            }
            SoftPhoneClient.getInstance().unInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置音量
     */
    public void setSipVolume() {
        /*int currentSystemVolume = mSipView.getCurrentSystemVolume();
        SoftPhoneClient.getInstance().softphoneSetInputVolume(currentSystemVolume);
        SoftPhoneClient.getInstance().softphoneSetOutputVolume(currentSystemVolume);*/
    }

    class HeartServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
