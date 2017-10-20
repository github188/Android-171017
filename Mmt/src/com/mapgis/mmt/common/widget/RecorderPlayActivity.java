package com.mapgis.mmt.common.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 录音播放Activity
 *
 * @author meikai
 */
public class RecorderPlayActivity extends BaseActivity {

    private RecorderViewFragment fragment;
    private MediaPlayer mediaPlayer;
    private List<String> recordList;
    private int currentPos;

    private List<String> deleteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseTextView().setText("录音播放");
        getBaseRightImageView().setImageResource(R.drawable.delete_white);

        if (getIntent().getBooleanExtra("hideDelBtn", false))
            getBaseRightImageView().setVisibility(View.INVISIBLE);
        else
            getBaseRightImageView().setVisibility(View.VISIBLE);

        // edit by WL - 20150525 原方法删除第二个录音，会导致程序异常崩溃
        getBaseRightImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OkCancelDialogFragment deleteFragment = new OkCancelDialogFragment("是否删除该录音");
                deleteFragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        fragment.removeCurrentRec();
                    }
                });
                deleteFragment.show(getSupportFragmentManager(), "");
            }
        });
        getBaseLeftImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onFinish();
            }
        });
        recordList = getIntent().getStringArrayListExtra("fileList");
        currentPos = getIntent().getIntExtra("pos", 0);
        mediaPlayer = new MediaPlayer();

        fragment = new RecorderViewFragment();

        addFragment(fragment);
    }

    @Override
    protected void onDestroy() {
        try {
            Intent intent = new Intent();

            if (deleteList != null)
                intent.putStringArrayListExtra("deleteRecList", (ArrayList<String>) deleteList);

            setResult(29, intent);

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            super.onDestroy();
        }
    }

    class RecorderViewFragment extends Fragment {
        private LinearLayout linearLayout;
        public ImageView playBtn;
        private ImageView preBtn;
        private ImageView nextBtn;
        public TextView recordName;
        public TextView recordLength;
        public TextView recordTime;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.record_play_fragment, null);
            linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout);

            // 创建MyVisualizerView组件，用于显示波形图
            final MyVisualizerView mVisualizerView = new MyVisualizerView(getActivity());
            // mVisualizerView.setLayoutParams(new
            // ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            // (int) (120f * getResources().getDisplayMetrics().density)));

            // 将MyVisualizerView组件添加到layout容器中
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (120f * getResources().getDisplayMetrics().density));
            linearLayout.addView(mVisualizerView, lp);

            // 以MediaPlayer的AudioSessionId创建Visualizer
            // 相当于设置Visualizer负责显示该MediaPlayer的音频数据
            Visualizer mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
//            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // 为mVisualizer设置监听器
            mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

                }

                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    // 用waveform波形数据更新mVisualizerView组件
                    mVisualizerView.updateVisualizer(waveform);
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);
            mVisualizer.setEnabled(true);

            recordName = (TextView) view.findViewById(R.id.recordFileName);
            String filePath = recordList.get(currentPos);
            String fileName = filePath.substring(recordList.get(currentPos).lastIndexOf("/") + 1);
            recordName.setText(fileName);

            recordLength = (TextView) view.findViewById(R.id.recordLength);
            recordLength.setVisibility(View.GONE);

            recordTime = (TextView) view.findViewById(R.id.recordTime);
            recordTime.setText("第 " + (currentPos + 1) + " 条 / " + "共 " + recordList.size() + " 条");
            // recordTime.setVisibility( View.GONE );

            playBtn = (ImageView) view.findViewById(R.id.playBtn);
            preBtn = (ImageView) view.findViewById(R.id.preItem);
            nextBtn = (ImageView) view.findViewById(R.id.nextItem);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playBtn.setImageResource(R.drawable.playrecord);
                }
            });

            playBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlay(v);
                }
            });

            preBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPos > 0) {
                        currentPos--;
                        String filePath = recordList.get(currentPos);
                        String fileName = filePath.substring(recordList.get(currentPos).lastIndexOf("/") + 1);
                        recordName.setText(fileName);

                        File file = new File(filePath);
                        recordLength.setText("时长：" + file.length() / (1024 * 176) + " 秒");

                        recordTime.setText("第 " + (currentPos + 1) + " 条 / " + "共 " + recordList.size() + " 条");

                        startPlay(v);
                    }
                }
            });

            nextBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPos < recordList.size() - 1) {
                        currentPos++;
                        String filePath = recordList.get(currentPos);
                        String fileName = filePath.substring(recordList.get(currentPos).lastIndexOf("/") + 1);
                        recordName.setText(fileName);

                        File file = new File(filePath);
                        recordLength.setText("时长：" + file.length() / (1024 * 176) + " 秒");

                        recordTime.setText("第 " + (currentPos + 1) + " 条 / " + "共 " + recordList.size() + " 条");

                        startPlay(v);
                    }
                }
            });

            // 创建完成 后 立即 播放
            startPlay(null);

            return view;
        }

        private void startPlay(View v) {
            try {
                if (v != null && v.getId() == R.id.playBtn) {//暂停、播放
                    if (mediaPlayer.isPlaying()) {
                        playBtn.setImageResource(R.drawable.playrecord);

                        mediaPlayer.pause();
                    } else {
                        playBtn.setImageResource(R.drawable.pauserecord);

                        mediaPlayer.start();
                    }
                } else {//重置播放当前选择录音
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }

                    mediaPlayer.reset();

                    mediaPlayer.setDataSource(getActivity(), Uri.fromFile(new File(recordList.get(currentPos))));
                    mediaPlayer.prepare();

                    mediaPlayer.start();
                    playBtn.setImageResource(R.drawable.pauserecord);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void removeCurrentRec() {
            if (recordList.size() == 0)
                return;

            if (deleteList == null)
                deleteList = new ArrayList<>();

            deleteList.add(recordList.get(currentPos));

            File file = new File(deleteList.get(0));
            if (file.exists())
                file.delete(); // 同时 删除 录音 文件

            recordList.remove(currentPos);

            if (recordList.size() == 0) {
                //没有录音文件直接返回上一界面，和在图片界面删除最后一张后的效果保持一致；
                onFinish();
                return;
//                playBtn.setEnabled(false);
//                playBtn.setImageResource(R.drawable.pauserecord);
//                recordName.setText("没有录音文件");
//                recordLength.setText("时长：" + "0 秒");
//                recordTime.setText("第 0 条 / " + "共 0 条");
            } else {
                // 防止 删除的是 最后一条录音
                if (currentPos >= recordList.size())
                    currentPos--;

                // 显示 删除 后 剩余的 录音名称
                String filePath = recordList.get(currentPos);
                String fileName = filePath.substring(recordList.get(currentPos).lastIndexOf("/") + 1);

                recordName.setText(fileName);
                recordLength.setText("时长：" + file.length() / (1024 * 176) + " 秒");
                recordTime.setText("第 " + (currentPos + 1) + " 条 / " + "共 " + recordList.size() + " 条");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onFinish();
        }
        return true;
    }
    private void onFinish(){
        Intent intent = new Intent();
        if (deleteList != null)
            intent.putStringArrayListExtra("deleteRecList", (ArrayList<String>) deleteList);
        setResult(29, intent);

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        finish();
        MyApplication.getInstance().finishActivityAnimation(this);
    }
    /**
     * 示波器 View
     *
     * @author meikai
     */
    private static class MyVisualizerView extends View {
        // bytes数组保存了波形抽样点的值
        private byte[] bytes;
        private float[] points;
        private final Paint paint = new Paint();
        private final Rect rect = new Rect();
        private byte type = 2;

        public MyVisualizerView(Context context) {
            super(context);
            bytes = null;
            // 设置画笔的属性
            paint.setStrokeWidth(1f);
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN);
            paint.setStyle(Style.FILL);
        }

        public void updateVisualizer(byte[] ftt) {
            bytes = ftt;
            // 通知该组件重绘自己。
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent me) {
            // 当用户触碰该组件时，切换波形类型
            if (me == null || me.getAction() != MotionEvent.ACTION_DOWN) {
                return false;
            }
            type++;
            if (type >= 3) {
                type = 0;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (bytes == null) {
                return;
            }
            canvas.drawColor(Color.WHITE);
            // 使用rect对象记录该组件的宽度和高度
            rect.set(0, 0, getWidth(), getHeight());

            // 经过 amr2wav.exe 转换后 的wav 文件， 每次捕获的 bytes 为 1024 字节， 但是 从 185 字节
            // 开始到1024字节 的 全是 -128，
            // 原因不明， 可能是 amr2wav.exe 对 amr文件进行格式转换时使用的波特率、声道之类的有关
            // 为了让 声音播放 时的 波形图 正常显示，此处添加一个 横坐标 的 比例因子 added by meikai 2014-11-19
            // 17:39
            int effectiveLength = 185;
            float xScaleFactor = bytes.length * 1.0f / effectiveLength;

            switch (type) {
                // -------绘制块状的波形图-------
                case 0:
                    for (int i = 0; i < effectiveLength - 1; i++) {
                        float left = getWidth() * i / (bytes.length - 1) * xScaleFactor;
                        // 根据波形值计算该矩形的高度
                        float top = rect.height() - (byte) (bytes[i + 1] + 128) * rect.height() / 128;
                        float right = left + 1;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------绘制柱状的波形图（每隔18个抽样点绘制一个矩形）-------
                case 1:
                    for (int i = 0; i < effectiveLength - 1; i += 6) {
                        float left = rect.width() * i / (bytes.length - 1) * xScaleFactor;
                        // 根据波形值计算该矩形的高度
                        float top = rect.height() - (byte) (bytes[i + 1] + 128) * rect.height() / 128;
                        float right = left + 6;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------绘制曲线波形图-------
                case 2:
                    // 如果point数组还未初始化
                    if (points == null || points.length < bytes.length * 4) {
                        points = new float[bytes.length * 4];
                    }
                    for (int i = 0; i < effectiveLength - 1; i++) {
                        // 计算第i个点的x坐标
                        points[i * 4] = rect.width() * i / (bytes.length - 1) * xScaleFactor;
                        // 根据bytes[i]的值（波形点的值）计算第i个点的y坐标
                        points[i * 4 + 1] = (rect.height() / 2) + ((byte) (bytes[i] + 128)) * 128 / (rect.height() / 2);
                        // 计算第i+1个点的x坐标
                        points[i * 4 + 2] = rect.width() * (i + 1) / (bytes.length - 1) * xScaleFactor;
                        // 根据bytes[i+1]的值（波形点的值）计算第i+1个点的y坐标
                        points[i * 4 + 3] = (rect.height() / 2) + ((byte) (bytes[i + 1] + 128)) * 128 / (rect.height() / 2);
                    }
                    // 绘制波形曲线
                    canvas.drawLines(points, paint);
                    break;
            }
        }
    }

}
