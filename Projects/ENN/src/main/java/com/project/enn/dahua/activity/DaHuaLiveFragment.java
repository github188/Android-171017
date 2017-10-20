package com.project.enn.dahua.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.business.client.msp.CmuClient;
import com.android.business.client.msp.PccClient;
import com.android.business.client.msp.SoftPhoneClient;
import com.android.business.exception.BusinessException;
import com.mapgis.mmt.MyApplication;
import com.mm.dss.videocapture.VCBase.VCConfig;
import com.mm.dss.videoce.imp.VideoCaptureEncode;
import com.mm.dss.videoencode.VEBase.IVEListener;
import com.mm.dss.videoencode.VEBase.VEConfig;
import com.mm.dss.videoencode.VEFactory.VEStrategy;
import com.mm.dss.videoencode.VEFactory.VEStreamType;
import com.project.enn.R;
import com.project.enn.dahua.DaHuaBroadcastReceiver;

/**
 * Created by Comclay on 2017/4/20.
 * 大华视频界面
 * <p>
 * 1，收到视频回调广播后打开该界面
 * 2，
 */

public class DaHuaLiveFragment extends Fragment implements IVEListener {

    private static final String TAG = "DaHuaLiveFragment";

    private DaHuaBroadcastReceiver mDaHuaBroadcastReceiver;
    private VideoCaptureEncode mEncode;
    private final static int VIDEO_WIDTH = 640;
    private final static int VIDEO_HEIGHT = 480;
    private final static int VIDEO_FRAME_RATE = 15;
    public final static int VIDEO_BITRATE = 256;

    private SurfaceView mSurfaceView;
    private LinearLayout mLoadingView;

    public DaHuaLiveFragment() {
    }

    public static DaHuaLiveFragment newInstance() {
        return new DaHuaLiveFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater
            , @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dahua_live_view, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mSurfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
        mLoadingView = (LinearLayout) view.findViewById(R.id.loadingView);

        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startEncodeVideo();
    }

    /*开始编码视频*/
    private void startEncodeVideo() {
        mEncode = new VideoCaptureEncode();
        mEncode.setEncodeParam(VEStreamType.VE_STREAM_TYPE_H264, VEStrategy.VE_STRATEGY_MEDIACODEC);
        mEncode.init(CmuClient.getInstance().mCmuRealHandle);
        mEncode.addListener(this);

        mEncode.setSurfaceView(mSurfaceView);

        VCConfig config = mEncode.getCaptureConfig();
        config.setPreviewHeight(VIDEO_HEIGHT);
        config.setPreviewWidth(VIDEO_WIDTH);
        config.setFrameRate(VIDEO_FRAME_RATE);
        config.setCameraAngle(VCConfig.CAMERA_ANGLE_90);
        // 设置摄像头
//        config.setCameraId(getCurrentCameraId());
        mEncode.configCapture(config);

        VEConfig veConfig = mEncode.getEncodeConfig();
        veConfig.seBitrate(getBitRate());
        veConfig.setFramerate(VIDEO_FRAME_RATE);
        veConfig.setHeight(VIDEO_HEIGHT);
        veConfig.setWidth(VIDEO_WIDTH);
        veConfig.setEncodeInMainThread(true);

        mEncode.startPreview();
        mLoadingView.setVisibility(View.GONE);
    }

    @Override
    public void onEncodeData(byte[] bytes) {
    }

    @Override
    public void onEncodeData(byte[] bytes, int i) {
    }

    @Override
    public void onEncodeData(byte[] bytes, int length, int type) {
        Log.w(TAG, "我正在发送视频: " + type);
        PccClient.getInstance().sendVideoData(bytes, type, VIDEO_WIDTH, VIDEO_HEIGHT);
    }

    @Override
    public void onEncodeState(int i) {
        Log.w(TAG, "编码状态: " + i);
    }

    @Override
    public void onEncodeError(int i) {
        Log.e(TAG, "编码出错: " + i);
    }

    /*服务器断开连接*/
    private void serverDisconnectLive() {
        mEncode.stopPreview();
        mEncode.unInit();
        mEncode = null;
        getActivity().finish();
    }

    /*客户端断开连接*/
    private void clientDisconnect() {
        try {
            serverDisconnectLive();
            PccClient.getInstance().closeVideo();
            getActivity().finish();
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    private int getBitRate() {
        SharedPreferences systemSharedPreferences = MyApplication.getInstance().getSystemSharedPreferences();
        return systemSharedPreferences.getInt("DHBitRate", VIDEO_BITRATE);
    }

    @Override
    public void onStart() {
        mDaHuaBroadcastReceiver = DaHuaBroadcastReceiver.registerReceiver(getActivity());
        mDaHuaBroadcastReceiver.setOnDaHuaSignalReceivedListener(
                new DaHuaBroadcastReceiver.OnDaHuaSignalReceivedListener() {
                    @Override
                    protected void onLiveFinishSignal() {
                        serverDisconnectLive();
                    }

                    @Override
                    protected void onSipRegistSuccessSignal() {
                        setTitleNumber();
                    }

                    @Override
                    protected void onSipHoldSignal() {
                        ((DaHuaLiveActivity) getActivity()).setSipHoldIconVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onSipHangupSignal() {
                        ((DaHuaLiveActivity) getActivity()).setSipHoldIconVisibility(View.GONE);
                    }
                });
        super.onStart();
    }

    @Override
    public void onStop() {
        DaHuaBroadcastReceiver.unregistReceiver(getActivity(), mDaHuaBroadcastReceiver);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        SoftPhoneClient.getInstance().hangup();
        super.onDestroy();
    }

    private void setTitleNumber() {
        FragmentActivity activity = getActivity();
        if (activity instanceof DaHuaLiveActivity) {
            ((DaHuaLiveActivity) activity).setTitleNumber();
        }
    }
}
