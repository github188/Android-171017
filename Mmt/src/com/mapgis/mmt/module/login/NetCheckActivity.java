package com.mapgis.mmt.module.login;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.util.PingUtil;
import com.mapgis.mmt.common.widget.NetCheckView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * 作者 : zhoukang
 * 日期 : 2017-06-19  12:52
 * 说明 : 网络检查Activity
 * <p>
 * <p>
 * 检查项：配置是否正确，是否能访问外网，服务器连接是否正常
 * 1，网络是否连接，配置是否为空
 * 2，IP是否能ping通
 * 3，外网是否能ping通（www.baidu.com）
 * 4，Testdb服务是否能调成功
 * 5，大华IP是否能ping通（新奥可选，用户未登录无法获取权限，所以暂未考虑）
 */

public class NetCheckActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int CHECK_NET_CONFIG = 0;
    private static final int CHECK_NET_INNER = 1;
    private static final int CHECK_NET_OUTER = 2;
    private static final int CHECK_NET_SERVER = 3;

    private static final String RESULT_NORMAL = "正常";
    private static final String RESULT_EXCEPTION = "异常";
    private static final String OUTER_TEST_IP = "www.baidu.com";
    private static final int TIME_SLEEP = 1000;

    private NetCheckView mCheckView;
    private Button mBtnControl;

    private ViewHolder configHolder;
    private ViewHolder innerHolder;
    private ViewHolder outerHolder;
    private ViewHolder serverHolder;

    private ValueAnimator animator;

    private MmtBaseTask<Void, Void, CheckResult> task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_net_check);
        findViewById(R.id.baseActionBarImageView).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.baseActionBarTextView);
        tvTitle.setText(R.string.text_check_net);

        mCheckView = (NetCheckView) findViewById(R.id.netCheckView);
        mCheckView.setText(getNetText());

        mBtnControl = (Button) findViewById(R.id.btn_check_control);
        mBtnControl.setOnClickListener(this);

        initView();
    }

    private void initView() {
        configHolder = new ViewHolder(findViewById(R.id.item_net_config));
        innerHolder = new ViewHolder(findViewById(R.id.item_net_inner));
        outerHolder = new ViewHolder(findViewById(R.id.item_net_outer));
        serverHolder = new ViewHolder(findViewById(R.id.item_net_server));

        String[] stringArray = getResources().getStringArray(R.array.textCheckItem);
        configHolder.setTitle(stringArray[0]);
        innerHolder.setTitle(stringArray[1]);
        outerHolder.setTitle(stringArray[2]);
        serverHolder.setTitle(stringArray[3]);

        resetCheckItemViews();
    }

    private void resetCheckItemViews() {
        configHolder.switchState(ViewHolder.STATE_NOT_CHECK);
        innerHolder.switchState(ViewHolder.STATE_NOT_CHECK);
        outerHolder.switchState(ViewHolder.STATE_NOT_CHECK);
        serverHolder.switchState(ViewHolder.STATE_NOT_CHECK);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.baseActionBarImageView) {
            finish();
            MyApplication.getInstance().finishActivityAnimation(this);
        } else if (v.getId() == R.id.btn_check_control) {
            controlCheck();
        }
    }

    private void controlCheck() {
        String text = mBtnControl.getText().toString().trim();
        if ("开始检测".equals(text)) {
            startCheck();
            mBtnControl.setText("取消");
        } else if ("取消".equals(text)) {
            stopCheck();
            mBtnControl.setText("开始检测");
        } else if ("完成".equals(text)) {
            finish();
            MyApplication.getInstance().finishActivityAnimation(this);
        }
    }

    /*开始检测*/
    private void startCheck() {
        check(CHECK_NET_CONFIG);
    }

    /*检测某一项*/
    private void check(int checkType) {
        switch (checkType) {
            case CHECK_NET_CONFIG:
                checkNetConfig();
                break;
            case CHECK_NET_INNER:
                checkNetInner();
                break;
            case CHECK_NET_OUTER:
                checkNetOuter();
                break;
            case CHECK_NET_SERVER:
                checkNetServer();
                break;
        }
    }

    /*检查网络配置是否正确*/
    private void checkNetConfig() {
        task = new MmtBaseTask<Void, Void, CheckResult>(this, false) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                configHolder.switchState(ViewHolder.STATE_CHECKING);
                mCheckView.setCheckType(NetCheckView.CHECK_NET);
                startAnim();
            }

            @Override
            protected CheckResult doInBackground(Void... params) {
                CheckResult result = new CheckResult(CHECK_NET_CONFIG);

                ServerConfigInfo serverConfigInfo = ServerConnectConfig.getInstance().getServerConfigInfo();
                if (BaseClassUtil.isNullOrEmptyString(serverConfigInfo.IpAddress)
                        || BaseClassUtil.isNullOrEmptyString(serverConfigInfo.Port)
                        || BaseClassUtil.isNullOrEmptyString(serverConfigInfo.VirtualPath)) {
                    result.setResult(RESULT_EXCEPTION, "配置错误");
                } else {
                    result.setResult(RESULT_NORMAL, RESULT_NORMAL);
                }
                // 先睡他个几秒钟
                SystemClock.sleep(TIME_SLEEP);
                return result;
            }

            @Override
            protected void onPostExecute(CheckResult result) {
                super.onPostExecute(result);

                if (RESULT_NORMAL.equals(result.resultType)) {
                    configHolder.switchState(ViewHolder.STATE_CHECK_NORMAL);
                } else {
                    mCheckView.setLeftLineColor(getResources().getColor(R.color.color_net_bad));
                    configHolder.result.setText(result.result);
                    configHolder.switchState(ViewHolder.STATE_CHECK_EXCEPTION);
                }

                check(CHECK_NET_INNER);
            }
        };
        task.execute();
    }

    private void startAnim() {
        animator = ValueAnimator.ofInt(-1, mCheckView.getMaxCount() + 2);
        animator.setTarget(mCheckView);
        animator.setEvaluator(new IntEvaluator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCheckView.setIndex((Integer) animation.getAnimatedValue());
            }
        });
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(Integer.MAX_VALUE);
        animator.setDuration(mCheckView.getMaxCount() * 80).start();
    }

    private void stopAnim() {
        if (animator != null) {
            animator.removeAllUpdateListeners();
        }
        mCheckView.removeAnim();
    }

    /*检查配置的ip地址是否能够ping通*/
    private void checkNetInner() {
        task = new MmtBaseTask<Void, Void, CheckResult>(this, false) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                innerHolder.switchState(ViewHolder.STATE_CHECKING);
            }

            @Override
            protected CheckResult doInBackground(Void... params) {
                CheckResult result = new CheckResult(CHECK_NET_INNER);
                ServerConfigInfo serverConfigInfo = ServerConnectConfig.getInstance().getServerConfigInfo();
                int avgRTT = PingUtil.getAvgRTT(serverConfigInfo.IpAddress, 4, 10);

                if (avgRTT <= 0 || avgRTT > 500) {
                    result.setResult(RESULT_EXCEPTION, "异常");
                } else {
                    result.setResult(RESULT_NORMAL, avgRTT + "ms");
                }
                // 先睡他个几秒钟
                SystemClock.sleep(TIME_SLEEP);
                return result;
            }

            @Override
            protected void onPostExecute(CheckResult result) {
                super.onPostExecute(result);

                if (RESULT_NORMAL.equals(result.resultType)) {
                    innerHolder.switchState(ViewHolder.STATE_CHECK_NORMAL);
                    if (!RESULT_NORMAL.equals(result.result)) {
                        innerHolder.result.setText(result.result);
                    }
                } else {
                    mCheckView.setLeftLineColor(getResources().getColor(R.color.color_net_bad));
                    innerHolder.result.setText(result.result);
                    innerHolder.switchState(ViewHolder.STATE_CHECK_EXCEPTION);
                }

                check(CHECK_NET_OUTER);
            }
        };
        task.execute();
    }

    /*检查外网www.baidu.com地址是否能ping通*/
    private void checkNetOuter() {
        task = new MmtBaseTask<Void, Void, CheckResult>(this, false) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                outerHolder.switchState(ViewHolder.STATE_CHECKING);
            }

            @Override
            protected CheckResult doInBackground(Void... params) {
                CheckResult result = new CheckResult(CHECK_NET_INNER);
                int avgRTT = PingUtil.getAvgRTT(OUTER_TEST_IP, 4, 10);

                if (avgRTT < 0 || avgRTT > 500) {
                    result.setResult(RESULT_EXCEPTION, "异常");
                } else {
                    result.setResult(RESULT_NORMAL, avgRTT + "ms");
                }
                return result;
            }

            @Override
            protected void onPostExecute(CheckResult result) {
                super.onPostExecute(result);

                if (RESULT_NORMAL.equals(result.resultType)) {
                    outerHolder.switchState(ViewHolder.STATE_CHECK_NORMAL);
                    if (!RESULT_NORMAL.equals(result.result)) {
                        outerHolder.result.setText(result.result);
                    }
                } else {
                    mCheckView.setLeftLineColor(getResources().getColor(R.color.color_net_bad));
                    outerHolder.result.setText(result.result);
                    outerHolder.switchState(ViewHolder.STATE_CHECK_EXCEPTION);
                }

                stopAnim();
                check(CHECK_NET_SERVER);
            }
        };
        task.execute();
    }

    /*检查CityInterface服务器是否连通*/
    private void checkNetServer() {
        task = new MmtBaseTask<Void, Void, CheckResult>(this, false) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                serverHolder.switchState(ViewHolder.STATE_CHECKING);
                mCheckView.setCheckType(NetCheckView.CHECK_SERVER);
                startAnim();
            }

            @Override
            protected CheckResult doInBackground(Void... params) {
                CheckResult result = new CheckResult(CHECK_NET_INNER);
                try {
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/TestDB";

                    String json = NetUtil.executeHttpGet(url);

                    if (!TextUtils.isEmpty(json)) {
                        ResultWithoutData data = new Gson().fromJson(json, ResultWithoutData.class);

                        if (data.ResultCode > 0) {
                            result.setResult(RESULT_NORMAL, RESULT_NORMAL);
                            return result;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.setResult(RESULT_EXCEPTION, RESULT_EXCEPTION);
                SystemClock.sleep(TIME_SLEEP);
                return result;
            }

            @Override
            protected void onPostExecute(CheckResult result) {
                super.onPostExecute(result);

                if (RESULT_NORMAL.equals(result.resultType)) {
                    serverHolder.switchState(ViewHolder.STATE_CHECK_NORMAL);
                    if (!RESULT_NORMAL.equals(result.result)) {
                        serverHolder.result.setText(result.result);
                    }
                } else {
                    mCheckView.setRightLineColor(getResources().getColor(R.color.color_net_bad));
                    serverHolder.result.setText(result.result);
                    serverHolder.switchState(ViewHolder.STATE_CHECK_EXCEPTION);
                }

                checkFinish();
            }
        };
        task.execute();
    }

    private void checkFinish() {
        stopAnim();
        mBtnControl.setText("完成");
    }

    /*取消检测*/
    private void stopCheck() {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
        resetCheckItemViews();
        mCheckView.reset();
        stopAnim();
    }

    @Override
    protected void onDestroy() {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
        super.onDestroy();
    }

    private String getNetText() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            return "网络未连接";
        }

        if (!networkInfo.isAvailable()) {
            return "网络不可用";
        }

        int type = cm.getActiveNetworkInfo().getType();
        if (type == ConnectivityManager.TYPE_MOBILE) {
            return "移动网络";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiMgr.getConnectionInfo();
            if (info != null) {
                return info.getSSID().replace("\"", "");
            } else {
                return "WiFi网络";
            }
        }
        return "网络未连接";
    }

    static class ViewHolder {
        TextView title;
        TextView result;
        ProgressBar progressBar;
        static final int STATE_NOT_CHECK = 0;
        static final int STATE_CHECKING = 2;
        static final int STATE_CHECK_EXCEPTION = -1;
        static final int STATE_CHECK_NORMAL = 1;


        ViewHolder(View itemView) {
            title = (TextView) itemView.findViewById(R.id.title);
            result = (TextView) itemView.findViewById(R.id.result);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }

        void setTitle(String title) {
            this.title.setText(title);
        }

        void switchState(int state) {
            switch (state) {
                case STATE_NOT_CHECK:
                    result.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    result.setText("未检查");
                    result.setTextColor(MyApplication.getInstance().getResources().getColor(R.color.check_normal));
                    break;
                case STATE_CHECKING:
                    result.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case STATE_CHECK_EXCEPTION:
                    result.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    result.setTextColor(MyApplication.getInstance().getResources().getColor(R.color.color_net_bad));
                    break;
                case STATE_CHECK_NORMAL:
                    result.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    result.setText("正常");
                    result.setTextColor(MyApplication.getInstance().getResources().getColor(R.color.check_normal));
                    break;
            }

        }
    }

    private class CheckResult {
        String resultType;
        String result;

        int checkIndex;

        CheckResult(int checkIndex) {
            this.checkIndex = checkIndex;
        }

        void setResult(String resultType, String result) {
            this.resultType = resultType;
            this.result = result;
        }
    }
}
