package com.mapgis.mmt.common.widget.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoViewerFragment extends Fragment {

    // 视频路径
    private ArrayList<String> mVideoList;
    // 图片路径
    private ArrayList<String> mImgList;
    // 是否可以删除
    private boolean mCanDelete = true;

    private boolean isShowDeleteButton = false;

    private int mCurrentIndex = 0;

    private SimpleImageAdapter mImageAdapter;

    private OnOptionsListener mOnOptionsListener;

    public final static String VIDEO_ABSOLUTE_PATH = "video_absolute_path";

//    public final static String VIDEO_FILE_NAME = "video_file_name";

    public final static String IMAGE_ABSOLUTE_PATH = "image_absolute_path";

    public final static String CAN_DELETE = "can_delete";

    public final static String CURRENT_SELECTED_INDEX = "current_selected_index";

    // 视频播放控件
    private VideoView mVideoView;
    // 用来展示图片的控件
    private RecyclerView mRecyclerView;
    // 圆形进度条
    private ProgressBar mProgressBar;

    private MediaController mMediaController;

    private View mDismissView;

    public VideoViewerFragment() {
        // Required empty public constructor
    }

    public static VideoViewerFragment newInstance(ArrayList<String> videoList
            /*,ArrayList<String> videoNames*/
            , ArrayList<String> imgList, int selectedIndex
            , boolean canDelete) {

        Bundle args = new Bundle();
        args.putStringArrayList(VIDEO_ABSOLUTE_PATH, videoList);
//        args.putStringArrayList(VIDEO_FILE_NAME,videoNames);
        args.putStringArrayList(IMAGE_ABSOLUTE_PATH, imgList);
        args.putBoolean(CAN_DELETE, canDelete);
        args.putInt(CURRENT_SELECTED_INDEX, selectedIndex);

        VideoViewerFragment fragment = new VideoViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ArrayList<String> getVideoList() {
        return mVideoList;
    }

    public ArrayList<String> getImgList() {
        return mImgList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getParams();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_viewer, container, false);
        initView(view);

        initData();

        initListener();
        return view;
    }

    /**
     * 初始化参数
     */
    private void getParams() {
        if (this.getArguments() == null) return;

        mVideoList = this.getArguments().getStringArrayList(VIDEO_ABSOLUTE_PATH);
        mImgList = this.getArguments().getStringArrayList(IMAGE_ABSOLUTE_PATH);
        mCanDelete = this.getArguments().getBoolean(CAN_DELETE, true);
        mCurrentIndex = this.getArguments().getInt(CURRENT_SELECTED_INDEX, -1);
    }

    /**
     * 初始化布局
     */
    private void initView(View view) {
        mVideoView = (VideoView) view.findViewById(R.id.videoView);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.imgRecyyclerView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mDismissView = view.findViewById(R.id.dimissActivity);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        /* 获取MediaController对象，控制媒体播放 */
        mMediaController = new MediaController(getActivity());
        mVideoView.setMediaController(mMediaController);
        // 展示工具条
        mMediaController.show();

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        // 水平布局
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        // 设置动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mImageAdapter = new SimpleImageAdapter(mImgList);
        mRecyclerView.setAdapter(mImageAdapter);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        displayVideo(mCurrentIndex);
    }

    private void initListener() {
//        mRecyclerView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (!mCanDelete) return false;
//
//                isShowDeleteButton = true;
//                mImageAdapter.notifyDataSetChanged();
//                return true;
//            }
//        });

        mMediaController.setPrevNextListeners(new View.OnClickListener() {  // next
            @Override
            public void onClick(View view) {
                if (mCurrentIndex == mVideoList.size() - 1){
                    displayVideo(0);
                }else{
                    displayVideo(mCurrentIndex + 1);
                }
            }
        }, new View.OnClickListener() {  // pre
            @Override
            public void onClick(View view) {
                if (mCurrentIndex == 0){
                    displayVideo(mVideoList.size() - 1);
                }else{
                    displayVideo(mCurrentIndex - 1);
                }
            }
        });

        mDismissView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                VideoViewerFragment.this.getActivity().onBackPressed();
                return false;
            }
        });
    }

    /**
     * 设置监听器
     */
    public void setOnOptionsListener(OnOptionsListener onOptionsListener) {
        this.mOnOptionsListener = onOptionsListener;
    }

    /**
     * 选中并播放一个视频
     *
     * @param index 选中的索引位置
     */
    public void setSelectedItem(int index) {
        displayVideo(index);
    }

    /**
     * 播放索引为index的视频
     *
     * @param index 索引
     */
    private void displayVideo(final int index) {
        if (mVideoList == null || index < 0 || index >= mVideoList.size()) {
            return;
        }
        try {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
                mVideoView.resume();
            }
            // 显示加载对话框
            mProgressBar.setVisibility(View.VISIBLE);
            // 刷新布局
            int temp = mCurrentIndex;
            mCurrentIndex = index;
            mImageAdapter.notifyItemChanged(temp);
            mImageAdapter.notifyItemChanged(mCurrentIndex);

            final String absoluteVideoFile = mVideoList.get(index);

            File file = new File(absoluteVideoFile);

            // 文件夹路径不存在，则创建文件夹
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            // 文件不存在，则创建并下载文件
            if (!file.exists() || file.length() <= 0) {
                String url = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                        + "/OutFiles/UpLoadFiles/" + absoluteVideoFile.replace(MyApplication.getInstance().getMediaPathString(), "");

                new FinalHttp().download(url, absoluteVideoFile, new AjaxCallBack<File>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        mProgressBar.setVisibility(View.GONE);
                        displayVideo(file.getAbsolutePath());
                    }

                    @Override
                    public void onFailure(Throwable t, int errorNo, String strMsg) {
                        super.onFailure(t, errorNo, strMsg);
                    }
                });
            }else{
                displayVideo(absoluteVideoFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayVideo(String path) {
        mProgressBar.setVisibility(View.GONE);

        mVideoView.setVideoPath(path);
        mVideoView.start();

        if (mOnOptionsListener != null) {
            mOnOptionsListener.onSelectedItem(mCurrentIndex);
        }
    }

    /**
     * 下载视频文件
     */
    private void downloadVideoFile(String absoluteVideoFile) {
        String url = absoluteVideoFile.replace(MyApplication.getInstance().getMediaPathString(), "");
        NetUtil.downloadFile(url, new File(absoluteVideoFile));
    }

    /**
     * 删除索引为index的视频
     *
     * @param index 索引
     */
    public void deleteVideoItem(int index) {
        if (mVideoList == null || index < 0 || index >= mVideoList.size()) {
            return;
        }
        Toast.makeText(getActivity(), "删除索引为" + index + "的视频：" + mVideoList.get(index), Toast.LENGTH_SHORT).show();
        mVideoList.remove(index);
        mImgList.remove(index);
        mImageAdapter.notifyItemRemoved(index);

        if (mOnOptionsListener != null) {
            mOnOptionsListener.onDeleteItem(index);
        }
    }

    public class SimpleImageAdapter extends RecyclerView.Adapter<ViewHolder> {
        private ArrayList<String> mValues;

        private LayoutInflater inflater;

        public SimpleImageAdapter(ArrayList<String> imgAbsolutePaths) {
//        TypedValue val = new TypedValue();
//        if (context.getTheme() != null) {
//            context.getTheme().resolveAttribute(
//                    android.R.attr.selectableItemBackground, val, true);
//        }
//        mBackground = val.resourceId;
            inflater = LayoutInflater.from(getActivity());

            mValues = imgAbsolutePaths;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.list_item_video, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Bitmap bitmap = BitmapFactory.decodeFile(mValues.get(position));
            if (bitmap != null) {
                holder.mImageView.setImageBitmap(bitmap);
            } else {
                holder.mImageView.setImageResource(R.drawable.no_image);
            }

            if (mCanDelete && isShowDeleteButton) {
                holder.mImageButton.setVisibility(View.VISIBLE);
            } else {
                holder.mImageButton.setVisibility(View.GONE);
            }

            if (mCurrentIndex == position) {
                holder.mImageView.setBackgroundResource(R.drawable.shape_bound_bg);
            } else {
                holder.mImageView.setBackgroundColor(Color.WHITE);
//                holder.mImageView.setBackgroundResource();
            }

            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    isShowDeleteButton = true;
                    SimpleImageAdapter.this.notifyDataSetChanged();
                    return true;
                }
            });

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedItem(position);
                }
            });

            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteVideoItem(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues == null ? 0 : mValues.size();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View view;
        public ImageView mImageView;
        public ImageButton mImageButton;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            mImageButton = (ImageButton) view.findViewById(R.id.imageButton);
            mImageView = (ImageView) view.findViewById(R.id.imageView);
        }
    }

    /**
     * 对选中和删除操作的监听
     */
    public interface OnOptionsListener {
        void onSelectedItem(int index);

        void onDeleteItem(int index);
    }

    private ViewGroup rootView;
    private ViewGroup contentContainer;
    private FrameLayout contentView;

    private Animation inAnim;
    private Animation outAnim;

    private OnViewerDismissListener mOnViewerDismissListener;

    /**
     * 展示视频浏览的布局
     *
     * @param ft FragmentTransaction对象
     */
    public void showViewer(FragmentTransaction ft) {
        // 根布局
        rootView = (ViewGroup) getActivity().findViewById(android.R.id.content);
        contentContainer = (ViewGroup) View.inflate(getActivity(), R.layout.view_video_viewer, rootView);
        contentView = (FrameLayout) contentContainer.findViewById(R.id.frame_content);

        ft.replace(R.id.frame_content, this);
        ft.commit();

        inAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_in);
        contentView.startAnimation(inAnim);
    }

    public void dismissViewer() {
        if (contentView == null) {
            return;
        }

        outAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_out);
        contentView.startAnimation(outAnim);
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rootView.removeView(contentContainer);

                if (mOnViewerDismissListener != null) {
                    mOnViewerDismissListener.onDismissViewer(mVideoList, mImgList);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void setOnViewerDismissListener(OnViewerDismissListener listener) {
        this.mOnViewerDismissListener = listener;
    }

    public interface OnViewerDismissListener {
        void onDismissViewer(ArrayList<String> videoList, ArrayList<String> imgList);
    }
}
