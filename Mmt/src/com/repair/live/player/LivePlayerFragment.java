package com.repair.live.player;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.R;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;

import java.io.IOException;

/**
 * 播放界面
 */
public class LivePlayerFragment extends Fragment {
    private static final String ARG_PARAM_RTMP_URL = "rtmpUrl";

    private static final int LIVE_PLAY_START = 0;
    private static final int LIVE_PLAY_STOP = -1;
    private static final int LIVE_PLAY_PAUSE = 1;
    private static final int LIVE_PLAY_RESUME = 2;

    private static final String TAG = LivePlayerFragment.class.getSimpleName();

    private SurfaceView mSurfaceView;
    private PLMediaPlayer mMediaPlayer;
    private View mLoadingView;
    private AVOptions mAVOptions;

    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;

    private String mRtmpUrl = null;
    private boolean mIsStopped = false;
    private boolean mIsActivityPaused = true;

    private ImageView mIvStopIcon = null;

    public LivePlayerFragment() {
    }

    /**
     * @param rtmpUrl 直播拉流地址.
     * @return A new instance of fragment LivePlayerFragment.
     */
    public static LivePlayerFragment newInstance(String rtmpUrl) {
        LivePlayerFragment fragment = new LivePlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_RTMP_URL, rtmpUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRtmpUrl = getArguments().getString(ARG_PARAM_RTMP_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_player, container, false);
        initView(view);
        initListener();
        return view;
    }

    private void initView(View view) {
        mLoadingView = view.findViewById(R.id.LoadingView);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.SurfaceView);
        mSurfaceView.getHolder().addCallback(mCallback);

        mIvStopIcon = (ImageView) view.findViewById(R.id.iv_stop);
        mIvStopIcon.setVisibility(View.INVISIBLE);

        mAVOptions = new AVOptions();

        if (isLiveStreaming(mRtmpUrl)) {
            // the unit of timeout is ms
            mAVOptions.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 15 * 1000);
            mAVOptions.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 15 * 1000);
            // Some optimization with buffering mechanism when be set to 1
            mAVOptions.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        }

        // 1 -> hw codec enable, 0 -> disable [recommended]
        int codec = getActivity().getIntent().getIntExtra("mediaCodec", 0);
        mAVOptions.setInteger(AVOptions.KEY_MEDIACODEC, codec);

        // whether start play automatically after prepared, default value is 1
        mAVOptions.setInteger(AVOptions.KEY_START_ON_PREPARED, 0);

        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsStopped) {
                    switchOperate(LIVE_PLAY_START);
                    return;
                }

                if (mMediaPlayer.isPlaying()) {
                    // 暂停
                    switchOperate(LIVE_PLAY_PAUSE);
                } else {
                    switchOperate(LIVE_PLAY_RESUME);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsActivityPaused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsActivityPaused = true;
    }

    public void switchOperate(int s) {
        switch (s) {
            case LIVE_PLAY_START:
                onPlay();
                mIvStopIcon.setVisibility(View.INVISIBLE);
                break;
            case LIVE_PLAY_PAUSE:
                onPlayPause();
                mIvStopIcon.setVisibility(View.VISIBLE);
                break;
            case LIVE_PLAY_RESUME:
                onPlayResume();
                mIvStopIcon.setVisibility(View.INVISIBLE);
                break;
            case LIVE_PLAY_STOP:
                onPlayStop();
                mIvStopIcon.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * 播放
     */
    public void onPlay() {
        if (mIsStopped) {
            prepare();
        } else {
            mMediaPlayer.start();
        }
    }

    /**
     * 暂停播放
     */
    public void onPlayPause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    /**
     * 继续播放
     */
    public void onPlayResume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    /**
     * 停止播放
     */
    public void onPlayStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        mIsStopped = true;
    }

    public void releaseWithoutStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(null);
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void prepare() {
        if (BaseClassUtil.isNullOrEmptyString(mRtmpUrl) || !mRtmpUrl.startsWith("rtmp://")) {
            ((LivePlayerActivity) getActivity()).showErrorMsg(getString(R.string.live_invalid_url));
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            return;
        }

        try {
            mMediaPlayer = new PLMediaPlayer(mAVOptions);

            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);

            // set replay if completed
            // mMediaPlayer.setLooping(true);

            mMediaPlayer.setWakeMode(getActivity().getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mMediaPlayer.setDataSource(mRtmpUrl);
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            mMediaPlayer.prepareAsync();

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            prepare();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSurfaceWidth = width;
            mSurfaceHeight = height;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // release();
            releaseWithoutStop();
        }
    };

    private PLMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLMediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(PLMediaPlayer mp, int width, int height) {
            Log.i(TAG, "onVideoSizeChanged, width = " + width + ",height = " + height);
            // resize the display window to fit the screen
            if (width != 0 && height != 0) {
                float ratioW = (float) width / (float) mSurfaceWidth;
                float ratioH = (float) height / (float) mSurfaceHeight;
                float ratio = Math.max(ratioW, ratioH);
                width = (int) Math.ceil((float) width / ratio);
                height = (int) Math.ceil((float) height / ratio);
                FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(width, height);
                layout.gravity = Gravity.CENTER;
                mSurfaceView.setLayoutParams(layout);
            }
        }
    };

    private PLMediaPlayer.OnPreparedListener mOnPreparedListener = new PLMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(PLMediaPlayer mp) {
            Log.i(TAG, "On Prepared !");
            mMediaPlayer.start();
            mIsStopped = false;
        }
    };

    private PLMediaPlayer.OnInfoListener mOnInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer mp, int what, int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mLoadingView.setVisibility(View.VISIBLE);
                    Log.e(TAG, "正在加载。。。");
                    break;
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                case PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    mLoadingView.setVisibility(View.GONE);
                    Log.e(TAG, "加载完毕。。。");
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer mp, int percent) {
            Log.d(TAG, "onBufferingUpdate: " + percent + "%");
        }
    };

    /**
     * Listen the event of playing complete
     * For playing local file, it's called when reading the file EOF
     * For playing network stream, it's called when the buffered bytes played over
     * <p>
     * If setLooping(true) is called, the player will restart automatically
     * And ｀onCompletion｀ will not be called
     */
    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer mp) {
            Log.d(TAG, "Play Completed !");
            showToastTips(getString(R.string.player_completed));
            getActivity().finish();
        }
    };

    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer mp, int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLMediaPlayer.ERROR_CODE_INVALID_URI:
                    showToastTips(getString(R.string.invalid_player_url));
                    break;
                case PLMediaPlayer.ERROR_CODE_404_NOT_FOUND:
                    showToastTips(getString(R.string.resourse_not_found));
                    break;
                case PLMediaPlayer.ERROR_CODE_CONNECTION_REFUSED:
                    showToastTips(getString(R.string.connection_refused));
                    break;
                case PLMediaPlayer.ERROR_CODE_CONNECTION_TIMEOUT:
                    showToastTips(getString(R.string.connection_timeout));
                    break;
                case PLMediaPlayer.ERROR_CODE_EMPTY_PLAYLIST:
                    showToastTips(getString(R.string.empty_playlist));
                    break;
                case PLMediaPlayer.ERROR_CODE_STREAM_DISCONNECTED:
                    showToastTips(getString(R.string.stream_disconnected));
                    break;
                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
                    showToastTips(getString(R.string.network_io_error));
                    break;
                case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
                default:
                    showToastTips(getString(R.string.unknown_error));
                    break;
            }
            // Todo pls handle the error status here, retry or call finish()
//            getActivity().finish();
//             The PLMediaPlayer has moved to the Error state, if you want to retry, must reset first !
//            try {
//                mMediaPlayer.reset();
//                mMediaPlayer.setDisplay(mSurfaceView.getHolder());
//                mMediaPlayer.setDataSource(mRtmpUrl);
//                mMediaPlayer.prepareAsync();
//                return true;
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//             Return true means the error has been handled
//             If return false, then `onCompletion` will be called
            return false;
        }
    };

    private void showToastTips(final String tips) {
        if (mIsActivityPaused) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), tips, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 判断是不是流媒体地址
     *
     * @param url 播放地址
     * @return true流媒体，false不是流媒体
     */
    private boolean isLiveStreaming(String url) {
        return url.startsWith("rtmp://")
                || (url.startsWith("http://") && url.endsWith(".m3u8"))
                || (url.startsWith("http://") && url.endsWith(".flv"));
    }
}
