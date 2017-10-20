package com.mapgis.mmt.common.widget.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;

import java.io.File;

/**
 * MediaRecorder 录音类
 *
 * @author meikai
 */
public class MediaRecorderDialogFragment extends DialogFragment {
    private TextView listDialogTitle;
    private ImageView microphone;
    private TextView recorderName;
    private LinearLayout linearLayout1;

    private OnSaveBtnClickListener saveBtnListener;

    /**
     * 保存录制的音频文件的名字， 不含其路径
     */
    private String recorderFileName;
    /**
     * 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
     */
    // private final int sampleRateInHz = 44100;
    private MediaRecorder mRecorder;
    private File soundFile;
    /**
     * 录音生成的文件 存储 的 文件夹，形如 /mnt/sdcard/MapGIS/Record/2014巡检产品
     */
    private String absolutePath;
    private Boolean isRecording = false;

    public void setSaveBtnListener(OnSaveBtnClickListener outListener) {
        this.saveBtnListener = outListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.absolutePath = getArguments().getString("path");

        View rootView = inflater.inflate(R.layout.recorder_dialog_fragment, null);
        initView(rootView);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        this.setCancelable(false);
        return rootView;
    }

    boolean isErr;

    private void initView(View rootView) {
        listDialogTitle = (TextView) rootView.findViewById(R.id.listDialogTitle);
        microphone = (ImageView) rootView.findViewById(R.id.microphone);
        recorderName = (TextView) rootView.findViewById(R.id.recorderName);
        linearLayout1 = (LinearLayout) rootView.findViewById(R.id.linearLayout1);

        rootView.findViewById(R.id.recorder_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果 已经 录音， 则删除此录音文件
                if (recorderFileName != null && recorderFileName.length() > 0) {
                    File wavFile = new File(absolutePath + recorderFileName);

                    if (wavFile.exists() && !wavFile.delete())
                        Log.v("MediaRecorder", "删除文件失败");
                }

                MediaRecorderDialogFragment.this.dismiss();
            }
        });

        microphone.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    listDialogTitle.setText("正在录音……");
                    isRecording = true;

                    // 创建保存录音的音频文件
                    recorderFileName = BaseClassUtil.getSystemTimeForFile() + ".amr";
                    soundFile = new File(absolutePath + recorderFileName);
                    mRecorder = new MediaRecorder();

                    // 设置录音的声音来源
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    // 设置录制的声音的输出格式（必须在设置声音编码格式之前设置）
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                    // 设置声音编码的格式
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mRecorder.setOutputFile(soundFile.getAbsolutePath());
                    // mRecorder.setAudioSamplingRate(sampleRateInHz);
                    // mRecorder.setAudioChannels(2);
                    // mRecorder.setAudioEncodingBitRate(16);

                    mRecorder.prepare();
                    // 开始录音
                    mRecorder.start();

                    microphone.setImageResource(R.drawable.record_mic_duration);
                    ((AnimationDrawable) microphone.getDrawable()).start();

                    return true;
                } catch (Exception ex) {
                    isErr = true;
                    ex.printStackTrace();

                    stopMediaRecord();

                    return false;
                }
            }
        });

        microphone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (isErr)
                            return false;

                        if (!isRecording) {
                            Toast.makeText(getActivity(), "请长按进行录音", Toast.LENGTH_SHORT).show();

                            return false;
                        }

                        listDialogTitle.setText("录音完成");

                        stopMediaRecord();

                        String noSuffix = recorderFileName.substring(0, recorderFileName.indexOf("."));
                        recorderName.setText((noSuffix.length() > 15 ? noSuffix.substring(0, 15) : noSuffix) + ".amr\n");

                        recorderName.setVisibility(View.VISIBLE);
                        linearLayout1.setVisibility(View.VISIBLE);

                        microphone.setOnTouchListener(null);
                        microphone.setOnLongClickListener(null);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return false;
            }
        });

        rootView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBtnListener.onSaveBtnClick(recorderFileName);

                MediaRecorderDialogFragment.this.dismiss();
            }
        });

        rootView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击 取消 后 删除 wav 文件
                File wavFile = new File(absolutePath + recorderFileName);

                if (wavFile.exists() && !wavFile.delete())
                    Log.v("MediaRecorder", "删除文件失败");

                MediaRecorderDialogFragment.this.dismiss();
            }
        });
    }

    public interface OnSaveBtnClickListener {
        void onSaveBtnClick(String recorderPath);
    }

    private void stopMediaRecord() {
        try {
            ((AnimationDrawable) microphone.getDrawable()).stop();
            microphone.setImageResource(R.drawable.icon_voice_volume_1);

            if (mRecorder != null) {
                isRecording = false;

                // 停止录音
                if (!isErr)
                    mRecorder.stop();

                // 释放资源
                mRecorder.release();
                mRecorder = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (soundFile.length() == 0) {
                Toast.makeText(getActivity(), "录音失败，请尝试开启录音权限", Toast.LENGTH_LONG).show();

                if (!soundFile.delete())
                    Log.v("MediaRecorder", "删除文件失败");

                dismiss();
            }
        }
    }
}
