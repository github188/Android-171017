package com.repair.live.publisher;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.global.MmtBaseTask;

import org.anyrtc.core.AnyRTMP;
import org.anyrtc.core.RTMPHosterHelper;
import org.anyrtc.core.RTMPHosterKit;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

public class LivePublishActivity extends BaseActivity implements RTMPHosterHelper {
    private static final int RTMP_CONNECT_SUCCESS = 100;
    private static final int RTMP_DISCONNECT_SUCCESS = 200;
    private boolean isLiving = false;
    private boolean isShown = true;

    private String rtmpUrl; // = "rtmp://192.168.12.6:1935/live/stream";

    private Button btnPublish = null;
    private LinearLayout mBottomView = null;
    private View mTopView = null;
    private TextView mTxtStatus = null;
    private SurfaceViewRenderer mSurfaceView = null;
    private VideoRenderer mRenderer = null;

    private LinearLayout mLoadingView = null;
    private TextView mLoadingTextView = null;

    private Animation mTopInAnim;
    private Animation mBottomInAnim;
    private Animation mTopOutAnim;
    private Animation mBottomOutAnim;

    private RTMPHosterKit mHoster = null;

    private String mStreamName = String.format("stream%d", MyApplication.getInstance().getUserId() + 10000);     // 根据当前时间生成

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void setDefaultContentView() {
        setContentView(R.layout.activity_anyrtc_publisher);

        defaultBackBtn = findViewById(R.id.baseActionBarImageView);

        mTopView = findViewById(R.id.baseTopView);

        addBackBtnListener(defaultBackBtn);

        findViewById(R.id.linearLayout1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBaseRightImageView().performClick();
            }
        });

        // restore data.
        rtmpUrl = ServerConnectConfig.getInstance().getRTMPLiveUrl(mStreamName);

        // initialize url.
        final EditText efu = (EditText) findViewById(R.id.url);
        efu.setText(rtmpUrl);

        mLoadingView = (LinearLayout) findViewById(R.id.LoadingView);
        mLoadingTextView = (TextView) findViewById(R.id.loadingTextView);
        mLoadingView.setVisibility(View.INVISIBLE);

        btnPublish = (Button) findViewById(R.id.publish);

        mBottomView = (LinearLayout) findViewById(R.id.ll_bottomView);

        {//* Init UI
            mTxtStatus = (TextView) findViewById(R.id.txt_rtmp_status);
            mSurfaceView = (SurfaceViewRenderer) findViewById(R.id.suface_view);
            mSurfaceView.init(AnyRTMP.Inst().Egl().getEglBaseContext(), null);
            mRenderer = new VideoRenderer(mSurfaceView);
        }

        initListener(efu);
        initAnim();

        {
            mHoster = new RTMPHosterKit(this, this);
            mHoster.SetVideoCapturer(mRenderer.GetRenderPointer(), true);
//            mHoster.StartRtmpStream(rtmpUrl);
            btnPublish.performClick();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    hidenView(true);
                    break;
                case RTMP_CONNECT_SUCCESS:
                    rtmpConnected();
                    break;
                case RTMP_DISCONNECT_SUCCESS:
                    rtmpDisconnected();
                    break;
                default:
                    // unknow message...
                    break;
            }
        }
    };

    /**
     * 初始化动画效果
     */
    private void initAnim() {
        // 进入动画
        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.live_top_in);
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.live_bottom_in);
        // 退出动画
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.live_top_out);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.live_bottom_out);

        mTopInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO 通知3s之后隐藏布局
                mHandler.sendEmptyMessageDelayed(0, getResources().getInteger(R.integer.delay_hiden_view));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void initListener(final EditText efu) {
        if(Camera.getNumberOfCameras() >= 2){
            ImageButton rightImageView = getBaseRightImageView();
            rightImageView.setVisibility(View.VISIBLE);
            rightImageView.setImageResource(R.drawable.btn_switch);
            rightImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.btn_switch_camera).performClick();
                }
            });
        }


        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPublish.getText().toString().contentEquals(getString(R.string.live_start_publish))) {
                    btnPublish.setText(getResources().getString(R.string.live_stop_publish));
                    mHoster.StartRtmpStream(rtmpUrl);
                    mLoadingView.setVisibility(View.VISIBLE);
                    mLoadingTextView.setText(getResources().getString(R.string.live_loading));

                } else if (btnPublish.getText().toString().contentEquals(getString(R.string.live_stop_publish))) {
                    btnPublish.setText(getResources().getString(R.string.live_start_publish));
                    mHoster.StopRtmpStream();
                    if (isLiving) {
                        mHandler.sendEmptyMessage(RTMP_DISCONNECT_SUCCESS);
                    }
                    setLiveState(false, isLiving);
                    mLoadingView.setVisibility(View.INVISIBLE);
                    mLoadingTextView.setText(getResources().getString(R.string.live_loading));
                }
            }
        });


        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLiving && isShown)
                    hidenView(true);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isLiving || isShown) {
            return super.dispatchTouchEvent(ev);
        }

        showView(true);
        return true;
    }

    /**
     * 设置直播装态
     *
     * @param isLiving true表示正在直播
     *                 false 表示直播结束
     */
    public void setLiveState(boolean isLiving, boolean isAnim) {
        this.isLiving = isLiving;
        if (isLiving && isShown) {
            // 正在直播，3秒之后隐藏标题栏和底部的控制按钮
            mHandler.sendEmptyMessageDelayed(0, getResources().getInteger(R.integer.delay_hiden_view));
        } else if (!isShown) {
            // 没有直播，显示标题栏和控制按钮
            showView(isAnim);
        }
    }

    /**
     * 显示布局
     *
     * @param isAnim 是否执行动画效果，true:执行、false:不执行
     */
    private void showView(boolean isAnim) {
        isShown = true;
        mTopView.setVisibility(View.VISIBLE);
        mBottomView.setVisibility(View.VISIBLE);
        if (!isAnim && isLiving) {
            // 不执行动画效果
            mHandler.sendEmptyMessageDelayed(0, getResources().getInteger(R.integer.delay_hiden_view));
            return;
        }
        mTopView.startAnimation(mTopInAnim);
        mBottomView.startAnimation(mBottomInAnim);
    }

    /**
     * 隐藏布局
     *
     * @param isAnim 是否执行动画，同上
     */
    private void hidenView(boolean isAnim) {
        if (!isLiving) {
            isShown = true;
            // 如果停止直播就不在隐藏布局
            return;
        }

        if (!isShown) return;
        isShown = false;
        if (!isAnim) {
            // 不执行动画
            mTopView.setVisibility(View.GONE);
            mBottomView.setVisibility(View.GONE);
            return;
        }

        // 执行隐藏动画
        mTopView.startAnimation(mTopOutAnim);
        mBottomView.startAnimation(mBottomOutAnim);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mHoster != null) {
            mHoster.StopRtmpStream();
            mHoster.Clear();
            mHoster = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHoster != null) {
            mHoster.StopRtmpStream();
            rtmpDisconnected();
        }
    }

    /**
     * the button click event listener
     *
     * @param btn 点击的布局
     */
    public void OnBtnClicked(View btn) {
        try {
            if (btn.getId() == R.id.btn_switch_camera) {
                if(Camera.getNumberOfCameras() <= 1){
                    showToast("该设备不支持切换摄像头！");
                    return;
                }

                if (null != mHoster) {
                    mHoster.SwitchCamera();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (mHoster != null) {
            mHoster.StopRtmpStream();
            rtmpDisconnected();
            mHoster.Clear();
            mHoster = null;
        }
        super.onBackPressed();
    }

    /**
     * Implements for RTMPHosterHelper
     */
    @Override
    public void OnRtmpStreamOK() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoadingView.setVisibility(View.INVISIBLE);
                mTxtStatus.setText(R.string.str_rtmp_success);
                mHandler.sendEmptyMessageDelayed(0, getResources().getInteger(R.integer.delay_hiden_view));
                setLiveState(true, false);
                Toast.makeText(LivePublishActivity.this, "RTMP 连接成功", Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessage(RTMP_CONNECT_SUCCESS);
            }
        });
    }

    private void rtmpConnected() {
        // 开启直播
        new MmtBaseTask<String, Void, String>(this) {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(String... params) {
                StringBuilder sb = new StringBuilder();
                //http://192.168.1.113:9999/CityInterface/Services/Zondy_MapGISCitySvr_MobileBusiness/REST
                // /LiveREST.svc/StartLive?userId=208&streamName=&position=85094,49058&preImgUrl=
                sb.append(ServerConnectConfig.getInstance().getMobileBusinessURL())
                        .append("/LiveREST.svc/StartLive?")
                        .append("userId=").append(MyApplication.getInstance().getUserId())
                        .append("&streamName=").append(mStreamName)     // 默认为用户的工号
                        .append("&position=").append(GpsReceiver.getInstance().getLastLocalLocation().toXY())
                        .append("&preImgUrl=").append("");
                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onPostExecute(String s) {
                try {
                    if (BaseClassUtil.isNullOrEmptyString(s)) {
                        Toast.makeText(LivePublishActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (new JSONObject(s).getInt("ResultCode") != 200) {
                        Toast.makeText(LivePublishActivity.this, "直播开启失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    @Override
    public void OnRtmpStreamReconnecting(final int times) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mTxtStatus.setText(String.format(getString(R.string.str_rtmp_reconnecting), times));
                mLoadingView.setVisibility(View.VISIBLE);
                mLoadingTextView.setText(String.format(getString(R.string.str_rtmp_reconnecting), times));
                Toast.makeText(LivePublishActivity.this, String.format(getString(R.string.str_rtmp_reconnecting), times), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void OnRtmpStreamStatus(final int delayMs, final int netBand) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxtStatus.setText(String.format(getString(R.string.str_rtmp_status), delayMs, netBand));
            }
        });
    }

    @Override
    public void OnRtmpStreamFailed(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxtStatus.setText(R.string.str_rtmp_failed);
                Toast.makeText(LivePublishActivity.this, "RTMP 连接失败", Toast.LENGTH_SHORT).show();
                btnPublish.setText(getResources().getString(R.string.live_start_publish));

                mLoadingView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void OnRtmpStreamClosed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LivePublishActivity.this, "RTMP 关闭", Toast.LENGTH_SHORT).show();
                mTxtStatus.setText(R.string.str_rtmp);
                rtmpDisconnected();

                mLoadingView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void rtmpDisconnected() {
        // 直播结束
        // http://192.168.1.115:9999/CityInterface/Services/Zondy_MapGISCitySvr_MobileBusiness/REST
        // /LiveREST.svc/StopLive?userId=1
        new MmtBaseTask<String, Void, String>(this) {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(String... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(ServerConnectConfig.getInstance().getMobileBusinessURL())
                        .append("/LiveREST.svc/StopLive?")
                        .append("userId=").append(MyApplication.getInstance().getUserId());
                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onPostExecute(String s) {
                try {
                    if (BaseClassUtil.isNullOrEmptyString(s)) {
                        showToast("网络异常");
                        return;
                    }

                    if (new JSONObject(s).getInt("ResultCode") != 200) {
                        showToast("关闭失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}
