package com.project.enn.dahua.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;

import com.android.business.client.msp.SoftPhoneClient;
import com.mapgis.mmt.BaseActivity;
import com.project.enn.dahua.IncomePhoneView;
import com.project.enn.R;
import com.project.enn.dahua.DaHuaBroadcastReceiver;
import com.project.enn.dahua.IDaHuaService;

import java.io.IOException;

/**
 * Created by Comclay on 2017/4/20.
 * 来电显示界面
 */

public class ComeIPhoneActivity extends BaseActivity implements IncomePhoneView.OnSipOperationCallback {
    protected DaHuaBroadcastReceiver mDaHuaBroadcastReceiver;
    private IncomePhoneView mComeIPhoneView;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;

    @Override
    protected void setDefaultContentView() {
        setContentView(R.layout.activity_come_in_phone);
        mComeIPhoneView = (IncomePhoneView) findViewById(R.id.incomePhoneView);
        mComeIPhoneView.addOnSipOerationCallback(this);

        initData();
        playComePhoneRing();
    }

    private void initData() {
        Intent intent = getIntent();
        String comeIPhoneNumber = intent.getStringExtra(IDaHuaService.PHONE_NUMBER);
        mComeIPhoneView.setNumber(comeIPhoneNumber);
        mComeIPhoneView.setMsgVisibility(View.GONE);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean b = super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            int currentSystemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            SoftPhoneClient.getInstance().softphoneSetInputVolume(currentSystemVolume);
            SoftPhoneClient.getInstance().softphoneSetOutputVolume(currentSystemVolume);
        }
        return b;
    }

    /**
     * 播放来电音乐
     */
    public void playComePhoneRing() {
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(this, ringtoneUri);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAnswerOperation() {
        mMediaPlayer.stop();
        SoftPhoneClient.getInstance().answer();
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    private boolean isHangup = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mComeIPhoneView.setTvTime(msg.what);
            if (!isHangup) {
                mHandler.sendEmptyMessageDelayed(msg.what + 1, 1000);
            }
        }
    };

    @Override
    public void onHungupOperation() {
        mMediaPlayer.stop();
        SoftPhoneClient.getInstance().hangup();
        isHangup = true;
        onBackPressed();
    }

    @Override
    protected void onStart() {
        mDaHuaBroadcastReceiver = DaHuaBroadcastReceiver.registerReceiver(this);
        mDaHuaBroadcastReceiver.setOnDaHuaSignalReceivedListener(
                new DaHuaBroadcastReceiver.OnDaHuaSignalReceivedListener() {
                    @Override
                    protected void onSipHangupSignal() {
                        finish();
                    }

                    @Override
                    protected void onLiveStartSignal() {
                        finish();
                    }
                });
        super.onStart();
    }

    @Override
    protected void onStop() {
        DaHuaBroadcastReceiver.unregistReceiver(this, mDaHuaBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mMediaPlayer == null){
            super.onDestroy();
            return;
        }
        if (mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        /*super.onBackPressed();*/
    }
}
