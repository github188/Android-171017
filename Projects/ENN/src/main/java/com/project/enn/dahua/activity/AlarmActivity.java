package com.project.enn.dahua.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.business.client.msp.DmuClient;
import com.android.business.client.msp.McuClient;
import com.android.business.client.msp.PccClient;
import com.android.business.client.msp.SDKExceptionDefine;
import com.android.business.exception.BusinessException;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.project.enn.R;
import com.project.enn.dahua.AlarmHeartWakeReceiver;
import com.project.enn.dahua.Constant;
import com.project.enn.dahua.DaHuaBroadcastReceiver;

/**
 * Created by Comclay on 2017/4/20.
 * 报警界面
 */

public class AlarmActivity extends BaseActivity {
    private static final String TAG = "AlarmActivity2";
    private ViewGroup mLoadingView;
    private Button mBtnAlarm;
    private ProgressDialog mDialog;

    private TextView mTvMsg;
    private DaHuaBroadcastReceiver mDaHuaBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        reportAlarm();
    }

    @Override
    protected void setDefaultContentView() {
        setContentView(R.layout.activity_alarm_layout);
        setSwipeBackEnable(false);

        addBackBtnListener(findViewById(R.id.baseActionBarImageView));
        getBaseTextView().setText("报警");

        initView();
    }

    private void initView() {
        mLoadingView = (ViewGroup) findViewById(R.id.loadingView);
        mBtnAlarm = (Button) findViewById(R.id.btn_alarm);
        TextView tvName = (TextView) findViewById(R.id.tvAlarmName);
        TextView tvNum = (TextView) findViewById(R.id.tvAlarmNum);
        TextView tvChncode = (TextView) findViewById(R.id.tvAlarmUUID);
        mTvMsg = (TextView) findViewById(R.id.tv_msg);

        mLoadingView.setVisibility(View.GONE);
        tvName.setText(MyApplication.getInstance().getUserBean().TrueName);
        tvNum.setText(McuClient.getInstance().getSoftPhoneCallnumber());
        tvChncode.setText(PccClient.getInstance().getPccDevcode());
        String htmlText = "<font color='#111111' ><b>报警说明：</b><font>"
                + "<font color='#666666'>手持端报警成功过后，监控端会根据UUID值请求手持设备的摄像头，并自动打开摄像头传输视频！<font>";
        ((TextView) findViewById(R.id.tvInstruction)).setText(Html.fromHtml(htmlText));

        mDialog = MmtProgressDialog.getLoadingProgressDialog(this, "正在报警...");
        int custromBtnStyleResource = AppStyle.getCustromBtnStyleResource();
        if (custromBtnStyleResource > 0) {
            mBtnAlarm.setBackgroundResource(custromBtnStyleResource);
        }
        mBtnAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportAlarm();
            }
        });
    }

    private void reportAlarm() {
        execReportAlarmTask();
    }

    /**
     * 上报报警信息
     */
    public void execReportAlarmTask() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected void onPreExecute() {
                mDialog.show();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                return reportToDaHuaServer();
            }

            @Override
            protected void onPostExecute(Integer result) {
                mDialog.dismiss();
                if (result == SDKExceptionDefine.Success.getCode()) {
                    alarmSuccess();
                } else {
                    alarmFailed();
                }
            }
        }.execute();
    }

    private void alarmSuccess() {
        AlarmHeartWakeReceiver.sendNotifyBroadcast(this, 2);
        showToast("报警成功");
        finish();
    }

    private void alarmFailed() {
        showToast("报警失败");
        finish();
    }

    /*上报到大华服务器上*/
    private int reportToDaHuaServer() {
        String alarmtime = String.valueOf(System.currentTimeMillis());
        GpsXYZ lastLocalLocation = GpsReceiver.getInstance().getLastLocalLocation();
        Location location = GpsReceiver.getInstance().getLastLocationConverse(lastLocalLocation);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        try {
            Log.w(TAG, String.format("上报到大华报警信息: 时间=%s，坐标=(%f,%f)，话机号=%s，密码=%s"
                    , alarmtime, longitude, latitude
                    , McuClient.getInstance().getSoftPhoneCallnumber()
                    , McuClient.getInstance().getSoftPhonePassword()));
            DmuClient.getInstance().reportAlarm(alarmtime, longitude, latitude
                    , McuClient.getInstance().getSoftPhoneCallnumber()
                    , McuClient.getInstance().getSoftPhonePassword()
                    , Constant.TIME_OUT);
            return SDKExceptionDefine.Success.getCode();
        } catch (BusinessException e) {
            e.printStackTrace();
            return e.errorCode;
        }
    }

    @Override
    protected void onStart() {
        mDaHuaBroadcastReceiver = DaHuaBroadcastReceiver.registerReceiver(this);
        mDaHuaBroadcastReceiver.setOnDaHuaSignalReceivedListener(
                new DaHuaBroadcastReceiver.OnDaHuaSignalReceivedListener() {
                    @Override
                    protected void onLiveStartSignal() {
                        super.onLiveStartSignal();
                        enterDaHuaLiveActivity();
                        finish();
                        overridePendingTransition(0, 0);
                    }
                });
        super.onStart();
    }

    public void enterDaHuaLiveActivity() {
        Activity activity = AppManager.currentActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, DaHuaLiveActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onStop() {
        DaHuaBroadcastReceiver.unregistReceiver(this, mDaHuaBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.show();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
