package com.mapgis.mmt.common.widget.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.camera.CameraVideoActivtiy;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.VideoViewerActivity;
import com.mapgis.mmt.common.widget.customview.BottomAlertDialog;
import com.mapgis.mmt.global.MmtBaseTask;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Comclay on 2016/10/10.
 * <p>
 * 录制视频的fragment
 */
public class VideoFragment extends PhotoFragment{

    private static final String ARG_RELATIVE_VIDEO_PATH_SEGMENT = "relative_video_path_segment";

    public static class Builder extends PhotoFragment.Builder {
        private String relativeVideoPathSegment;

        /**
         * @param relativePathSegment 视频文件的相对路径
         * @param relativeVideoPathSegment
         */
        public Builder(String relativePathSegment, String relativeVideoPathSegment) {
            super(relativePathSegment);
            this.relativeVideoPathSegment = relativeVideoPathSegment;
        }

        @Override
        public VideoFragment build() {
            return newInstance(this);
        }
    }

    private static VideoFragment newInstance(Builder builder) {
        VideoFragment fragment = new VideoFragment();
        fragment.setArguments(fragment.getFragmentArgs(builder));
        return fragment;
    }

    @NonNull
    protected Bundle getFragmentArgs(Builder builder) {
        Bundle args = super.getFragmentArgs(builder);
        args.putString(ARG_RELATIVE_VIDEO_PATH_SEGMENT, builder.relativeVideoPathSegment);
        // 此处禁止选择框的弹出
        args.putBoolean(ARG_SELECT_ENABLE, false);
        return args;
    }

    private String _relativeVideoPath;
    private String _absoluteVideoPath;

    // 视频录制的时常限制
    private int VIDEO_CAPTURE_DURATION_LIMIT = 60;

    private String _videoName;
    // 视频的名称
    private final ArrayList<String> videoNames = new ArrayList<>();
    private final ArrayList<String> videoAbsolutePaths = new ArrayList<>();

    private final ArrayList<String> uploadVideoAbsolutePaths = new ArrayList<>();
    private final ArrayList<String> uploadVideoRelativePaths = new ArrayList<>();
    private final ArrayList<String> uploadPhotoAbsolutePaths = new ArrayList<>();
    private final ArrayList<String> uploadPhotoRelativePaths = new ArrayList<>();

    private BottomAlertDialog mBottomAlertDialog;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    // 视频浏览的请求码
    private static final int VIDEO_VIEWER_REQUEST_CODE = 300;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initVideoData();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVideoData();
    }

    @Override
    protected void initView(View rootView) {
        super.initView(rootView);
    }

    private void initVideoData() {
        if (videoAbsolutePaths.size() == 0 && photoAbsolutePaths.size() != 0) {
            photoAbsolutePaths.clear();
            photoNames.clear();
        }

        videoNames.clear();

        for (int i = 0; i < videoAbsolutePaths.size(); i++) {
//            String path = videoAbsolutePaths.get(i);

            String fileName = videoAbsolutePaths.get(i);
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1).trim();
//            // 1，视频的url,但先要判断本地是否有该视频
//            if (!(new File(path).exists()) && !path.contains("http")) {
//                // 如果本地不存在就使用网络地址替换
//                String url = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath() + "/OutFiles/UpLoadFiles/"
//                        + videoAbsolutePaths.get(i).replace(MyApplication.getInstance().getMediaPathString(), "");
//
//                videoAbsolutePaths.remove(i);
//                videoAbsolutePaths.add(i, url);
//            }
            videoNames.add(fileName);
        }
    }

    /**
     * 初始化视频和显示的图片路径
     */
    @Override
    protected void initPath(Bundle args) {
        super.initPath(args);

        if (args == null) {
            this._relativeVideoPath = "temp/";
        } else {
            this._relativeVideoPath = args.getString(ARG_RELATIVE_VIDEO_PATH_SEGMENT, "temp/");
        }
        _absoluteVideoPath = MyApplication.getInstance().getMediaPathString() + _relativeVideoPath;

        File videoFile = new File(_absoluteVideoPath);
        if (!videoFile.exists()) {
            videoFile.mkdirs();
        }
    }

    @Override
    protected void initFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        _photoName = timeStamp + ".jpg";
        _videoName = timeStamp + ".mp4";
    }

    // 调用系统照相功能
    @Override
    protected void addOnePic() {
        addOneVideo();
    }

    protected void addOneVideo() {
        // 初始化文件名
        initFileName();
        // 开始录制视频
        startVideoCapture();
    }

    /**
     * 现已只使用自定义相机
     */
    private void startVideoCapture() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // 0：使用系统自带相机；1：使用程序定制相机
        if (MyApplication.getInstance().getConfigValue("VideoCaptureStyle", 1) > 0) {
            intent = new Intent(getActivity(), CameraVideoActivtiy.class);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, _absoluteVideoPath + _videoName);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(_absoluteVideoPath + _videoName)));
        }

//        MediaStore.EXTRA_OUTPUT：设置媒体文件的保存路径。
//        MediaStore.EXTRA_VIDEO_QUALITY：设置视频录制的质量，0为低质量，1为高质量。
//        MediaStore.EXTRA_DURATION_LIMIT：设置视频最大允许录制的时长，单位为毫秒。
//        MediaStore.EXTRA_SIZE_LIMIT：指定视频最大允许的尺寸，单位为byte。

        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // set the video image quality to low
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEO_CAPTURE_DURATION_LIMIT);            //限制持续时长
        // start the Video Capture Intent
        startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                System.out.println("视频路径为：" + data.getData().toString());
                // 判断制定路径与返回的路径是否相同
                String path = _absoluteVideoPath + _videoName;
                onVideoCaptureSuccess(path);
            } else if (resultCode == RESULT_CANCELED) {
                onVideoCaptureCanceled();
            }
        } else if (requestCode == VIDEO_VIEWER_REQUEST_CODE) {
            photoAbsolutePaths.clear();
            photoNames.clear();

            videoAbsolutePaths.clear();
            videoNames.clear();

            photoAbsolutePaths.addAll(data.getStringArrayListExtra(VideoViewerFragment.VIDEO_ABSOLUTE_PATH));
            photoAbsolutePaths.addAll(data.getStringArrayListExtra(VideoViewerFragment.IMAGE_ABSOLUTE_PATH));

            refresh();
        }
    }

    @Override
    protected void refresh() {
        super.refresh();

        videoNames.clear();
        for (String name : videoAbsolutePaths) {
            name = name.substring(name.lastIndexOf('/') + 1).trim();
            videoNames.add(name);
        }
    }

    /**
     * 获取视频中的一帧图片,并将图片保存到本地
     *
     * @return 图片
     */
    private Bitmap getFrameFromVideo(String path, String toPath) {
        // 获取第一帧
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();

        if (bitmap != null)
            // 保存到本地
            saveBitmap(bitmap, toPath);
        return bitmap;
    }

    private void saveBitmap(Bitmap bitmap, String toPath) {
        FileOutputStream fOut = null;

        try {
            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
            File file = new File(toPath);
            if (file.exists()) {
                return;
            }

            fOut = new FileOutputStream(file);
            bitmap.compress(format, 100, fOut);
            fOut.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 视频录制成功
     *
     * @param path 视频路径
     */
    private void onVideoCaptureSuccess(final String path) {
        new MmtBaseTask<String, Void, Bitmap>(getActivity()) {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = MmtProgressDialog.getLoadingProgressDialog(getActivity(), "正在处理...");
                dialog.show();
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                if (new File(params[0]).length() <= 0) {
                    return null;
                }
                String imgPath = _absolutePath + _photoName;

                // 将视频中的一帧取出保存到本地
                Bitmap bitmap = getFrameFromVideo(params[0], imgPath);

                //处理图片自适应旋转
                BitmapUtil.rotateBitmap(imgPath);

                photoNames.add(_photoName);
                photoAbsolutePaths.add(imgPath);
                uploadPhotoRelativePaths.add(getRelativePath() + _photoName);
                uploadPhotoAbsolutePaths.add(imgPath);

                videoNames.add(_videoName);
                videoAbsolutePaths.add(params[0]);
                uploadVideoRelativePaths.add(_relativeVideoPath + _videoName);
                uploadVideoAbsolutePaths.add(params[0]);

                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                dialog.dismiss();
                if (bitmap == null && new File(_absoluteVideoPath + _videoName).length() <= 0) {
                    Toast.makeText(getActivity(), "视频录制失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                addImageView(createImageView(bitmap));
            }
        }.mmtExecute(path);
    }

    /**
     * 根据视频文件路径生成对应的图片的文件名
     *
     * @param path 视频文件路径
     */
    private String getPhotoNameFromVideo(String path) {
        if (BaseClassUtil.isNullOrEmptyString(path)) return null;
        return path.replace(".mp4", ".jpg");
    }

    /**
     * 视频录制失败
     */
    private void onVideoCaptureCanceled() {
    }

    @Override
    protected void showPopupWindow() {
        showDialog();
    }

    private void showDialog() {
        ArrayList<String> contents = new ArrayList<>();
        contents.add("录制");
        contents.add("从本地选择");
        mBottomAlertDialog = new BottomAlertDialog(getActivity(), "视频录制", "取消", contents);
        mBottomAlertDialog.show();

        mBottomAlertDialog.setOnItemClickListener(new BottomAlertDialog.OnItemClickListener() {
            @Override
            public void onItemClick(BottomAlertDialog dialog, int position) {
                //
                if (position == 0) {
                    addOneVideo();
                } else if (position == 1) {
                    // 从本地选择视频上传未完成
                    Toast.makeText(getActivity(), "从本地选择", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void setRelativePhoto(List<String> relativeNames) {
        super.setRelativePhoto(relativeNames);
    }

    public void setRelativeVideo(List<String> relativeVideos) {
        for (String name : relativeVideos) {
            videoAbsolutePaths.add(MyApplication.getInstance().getMediaPathString() + name);
        }
    }

    public void setAbsoluteVideo(List<String> absoluteVideos) {
        this.videoAbsolutePaths.addAll(absoluteVideos);
    }

    public String getVideoRelativePaths() {
        String result = "";
        for (String names : videoNames) {
            result = result + _relativeVideoPath + names + ",";
        }
        return result.trim().length() == 0 ? result : result.substring(0, result.length() - 1);
    }

    public String getVideoNames() {
        String result = "";
        if (videoNames.size() != 0) {
            result = BaseClassUtil.listToString(videoNames);
        }
        return result;
    }

    public String getVideoAbsolutePaths() {
        String result = BaseClassUtil.listToString(videoAbsolutePaths);
        return result;
    }

    @Override
    public String getDatabaseValue() {
        return getFileNames();
    }

    @Override
    public String getLocalAbsolutePaths() {
        return getUploadAbsolutePaths();
    }

    @Override
    public String getServerRelativePaths() {
        return getUploadRelativePaths();
    }

    public String getUploadAbsolutePaths() {
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(uploadVideoAbsolutePaths);
        temp.addAll(uploadPhotoAbsolutePaths);
        return BaseClassUtil.listToString(temp);
    }

    public String getUploadRelativePaths() {
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(uploadVideoRelativePaths);
        temp.addAll(uploadPhotoRelativePaths);
        return BaseClassUtil.listToString(temp);
    }

    public String getFileNames() {
        String filePaths;
        ArrayList<String> temp = new ArrayList<>();
        for (String video : videoNames) {
            temp.add(_relativeVideoPath + video);
        }
        filePaths = BaseClassUtil.listToString(temp);

        return filePaths;
    }

    public ArrayList<String> getVideoAbsolutePathList() {
        return videoAbsolutePaths;
    }

    public List<String> getRelativePhotoFromVideo(List<String> relativeVideos) {
        if (relativeVideos == null || relativeVideos.size() == 0) {
            return null;
        }

        List<String> photoList = new ArrayList<>();
        for (int i = 0; i < relativeVideos.size(); i++) {
            String videoName = relativeVideos.get(i);
            String photoName = getPhotoNameFromVideo(videoName);
            photoList.add(photoName);
        }

        return photoList;
    }

    protected ImageView createImageView(Bitmap bitmap) {
        final ThumbnailImageView imageView = new ThumbnailImageView(getActivity());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                DimenTool.dip2px(getActivity(), photoBitmapWidth),
                DimenTool.dip2px(getActivity(), photoBitmapHeight));
        imageView.setLayoutParams(params);

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
        }
        imageView.setImageBitmap(bitmap);
        imageView.setPadding(DimenTool.dip2px(getActivity(), 5), 0, DimenTool.dip2px(getActivity(), 5), 0);
        imageView.setTag(getImageCount());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (Integer) v.getTag();
                seePic(pos);
            }
        });

        return imageView;
    }

    protected void seePic(int pos) {
        playVideo(pos);
    }

    /**
     * 播放视频
     *
     * @param pos 视频索引
     */
    private void playVideo(int pos) {
        if (videoAbsolutePaths == null || pos < 0 || pos > videoAbsolutePaths.size()) {
            return;
        }

        Intent intent = new Intent(getActivity(), VideoViewerActivity.class);
        intent.putStringArrayListExtra(VideoViewerFragment.VIDEO_ABSOLUTE_PATH, videoAbsolutePaths);
        intent.putStringArrayListExtra(VideoViewerFragment.IMAGE_ABSOLUTE_PATH, photoAbsolutePaths);
        intent.putExtra(VideoViewerFragment.CAN_DELETE, true);
        intent.putExtra(VideoViewerFragment.CURRENT_SELECTED_INDEX, pos);

        getActivity().startActivityForResult(intent, VIDEO_VIEWER_REQUEST_CODE);
    }
}
