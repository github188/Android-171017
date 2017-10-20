package com.mapgis.mmt.common.widget.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cameralibary.CameraActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.attach.IUploadFile;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.common.util.CompressWaterMarkTask;
import com.mapgis.mmt.common.util.DeviceUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.MmtImageLoader;
import com.mapgis.mmt.common.widget.PictureViewActivity;
import com.mapgis.mmt.common.widget.popupwindow.ThreeButtonPopupWindow;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.global.OnResultListener;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.SelectedItem;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class PhotoFragment extends Fragment implements IUploadFile {

    public static final int REQUEST_CODE_SEE_PIC = 10002;
    public static final int REQUEST_CODE_SELECT_PIC = 10003;

    private int maxCountInFirstRowEditable;
    private int maxCountInFirstRowReadonly;

    private final String MEDIA_TEMP_FOLDER_NAME = "temp";

    private TextView tvNum;

    private HorizontalScrollView mediaScrollView;
    private LinearLayout mediaContainer;
    private HorizontalScrollView mediaMoreScrollView;
    private LinearLayout mediaMoreContainer;

    // 当前所有照片名称
    protected final ArrayList<String> photoNames = new ArrayList<>();

    // 当前所有照片的绝对路径,存的都是大图
    protected final ArrayList<String> photoAbsolutePaths = new ArrayList<>();
    // 一些情况下，服务返回的完全 url形式的图片的 url地址
    private final ArrayList<String> rawPhotoFullWebUrl = new ArrayList<>();

    // 是否通过UUID命名
    private boolean isUUIDName;
    // 命名时可能需要的前缀
    private String prefixName;

    // 当前操作的照片名称
    protected String _photoName;
    // 相对路径
    private String _relativePath;
    // 绝对路径
    protected String _absolutePath;

    protected int photoBitmapWidth = 55;
    protected int photoBitmapHeight = 55;

    private boolean isAddEnable = true;
    private boolean canSelect = true;
    private boolean isCreateByOpenCamera;

    // 字段上传到数据库的最大长度（即所有文件的相对路径长度）
    private int maxValueLength = 0;

    public String getRelativePath() {
        return _relativePath;
    }

    private ArrayList<SelectedItem> mSelectedItem = new ArrayList<>();

    public static class Builder {
        private String relativePathSegment;
        private String pathValue;
        // default enable.
        private boolean selectEnable = true;
        // default enable.
        private boolean addEnable = true;
        // 字段长度限制
        private int maxValueLength = 0;

        public Builder(String relativePathSegment) {
            this.relativePathSegment = relativePathSegment;
        }

        public Builder setAddEnable(boolean addEnable) {
            this.addEnable = addEnable;
            return this;
        }

        public Builder setSelectEnable(boolean selectEnable) {
            this.selectEnable = selectEnable;
            return this;
        }

        public Builder setValue(String paths) {
            this.pathValue = paths;
            return this;
        }

        public Builder setMaxValueLength(int maxValueLength) {
            this.maxValueLength = maxValueLength;
            return this;
        }

        public PhotoFragment build() {
            return newInstance(this);
        }
    }

    private static PhotoFragment newInstance(Builder builder) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = fragment.getFragmentArgs(builder);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    protected Bundle getFragmentArgs(Builder builder) {
        Bundle args = new Bundle();
        args.putString(ARG_RELATIVE_PATH_SEGMENT, builder.relativePathSegment);
        args.putBoolean(ARG_ADD_ENABLE, builder.addEnable);
        args.putBoolean(ARG_SELECT_ENABLE, builder.selectEnable);
        args.putString(ARG_FILE_PATHS, builder.pathValue);
        args.putInt(ARG_MAX_VALUE_LENGTH, builder.maxValueLength);
        return args;
    }

    public PhotoFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (DeviceUtil.isTablet(context)) {
            this.maxCountInFirstRowEditable = 7;
            this.maxCountInFirstRowReadonly = 8;
        } else {
            this.maxCountInFirstRowEditable = 4;
            this.maxCountInFirstRowReadonly = 5;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            this.isAddEnable = args.getBoolean(ARG_ADD_ENABLE);
            this.canSelect = args.getBoolean(ARG_SELECT_ENABLE);
            this._relativePath = args.getString(ARG_RELATIVE_PATH_SEGMENT);
            this.maxValueLength = args.getInt(ARG_MAX_VALUE_LENGTH);

            String defaultValue = args.getString(ARG_FILE_PATHS);
            if (!BaseClassUtil.isNullOrEmptyString(defaultValue)) {
                List<String> mediaList = BaseClassUtil.StringToList(defaultValue, ",");
                setRelativePhoto(mediaList);
            }
        }
        initPath(args);
    }

    /**
     * Initialize path info.
     */
    protected void initPath(Bundle args) {
        String relativePath = null;
        if (args != null) {
            relativePath = args.getString(ARG_RELATIVE_PATH_SEGMENT);
        }
        if (TextUtils.isEmpty(relativePath)) {
            relativePath = "temp/";
        }

        this._relativePath = relativePath;
        this._absolutePath = MyApplication.getInstance().getMediaPathString() + relativePath;

        File file = new File(_absolutePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_recorder_fragment, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            if (isCreateByOpenCamera) {
                addOnePic();
            }

            downloadFromWeb();
            refreshNum();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 界面初始化
    protected void initView(View rootView) {
        try {
            ImageView ivAddButton = (ImageView) rootView.findViewById(R.id.recorderImg);
            ivAddButton.setVisibility(isAddEnable ? View.VISIBLE : View.GONE);
            ivAddButton.setMinimumHeight(DimenTool.dip2px(getActivity(), photoBitmapHeight));

            mediaScrollView = (HorizontalScrollView) rootView.findViewById(R.id.recorderScrollView);
            mediaScrollView.setHorizontalScrollBarEnabled(false);
            mediaContainer = (LinearLayout) rootView.findViewById(R.id.horizontalScrollViewLinear);

            mediaMoreScrollView = (HorizontalScrollView) rootView.findViewById(R.id.hsv_more_contents);
            mediaMoreScrollView.setHorizontalScrollBarEnabled(false);
            mediaMoreContainer = (LinearLayout) rootView.findViewById(R.id.ll_more_contents);
            mediaMoreScrollView.setVisibility(View.GONE);

            tvNum = (TextView) rootView.findViewById(R.id.tv_num);
            // GradientDrawable sd = (GradientDrawable) tvNum.getBackground();
            // sd.setColor(0xff2881a2);

            ivAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (maxValueLength > 0) {
                        int currentLen = getRelativePathLength();
                        if (currentLen > 0) {
                            int predictLen = currentLen / photoNames.size() + currentLen;
                            if (predictLen >= maxValueLength) {
                                Toast.makeText(getContext(), "数量超限，无法继续添加", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }

                    if (canSelect) {
                        showPopupWindow();
                    } else {
                        addOnePic();
                    }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 采用UUID的方式对文件命名
     */
    public void createFileByUUID() {
        this.isUUIDName = true;
    }

    /**
     * 采用UUID方式命名时,需要的前缀
     */
    public void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }

    // 调用系统照相功能
    protected void addOnePic() {
        initFileName();

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        // 0：使用系统自带相机；1：使用程序定制相机
        if (MyApplication.getInstance().getConfigValue("MmtCamera", -1) > 0) {
            intent = new Intent(getActivity(), CameraActivity.class);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(_absolutePath + _photoName)));

        if (getActivity() instanceof MapGISFrame) {
            getActivity().startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
        } else {
            PhotoFragment.this.startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
        }
    }

    protected void initFileName() {
        if (isUUIDName) {
            _photoName = BaseClassUtil.isNullOrEmptyString(prefixName) ? UUID.randomUUID().toString() : prefixName
                    + UUID.randomUUID().toString() + ".jpg";
        } else {
            _photoName = BaseClassUtil.getSystemTimeForFile() + ".jpg";
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1) { // 拍照成功后返回

                photoNames.add(_photoName);
                final String imgLocalAbsPath = _absolutePath + _photoName;
                photoAbsolutePaths.add(imgLocalAbsPath);

                formatPic(Collections.singletonList(imgLocalAbsPath));

            } else if (requestCode == REQUEST_CODE_SEE_PIC) { // 预览图片之后的回调

                final List<String> newPathList = data.getStringArrayListExtra("fileList");
                ListIterator<SelectedItem> itemListIterator = mSelectedItem.listIterator();
                while (itemListIterator.hasNext()) {
                    SelectedItem selectedItem = itemListIterator.next();
                    if (!newPathList.contains(selectedItem.getNewAbsolutePath())) {
                        itemListIterator.remove();
                    }
                }
                refresh(newPathList);

            } else if (requestCode == REQUEST_CODE_SELECT_PIC && resultCode == Activity.RESULT_OK) {

                ArrayList<SelectedItem> resultItems =
                        SelectedItem.createList(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
                handleSelectResult(resultItems);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            refreshNum();
        }
    }

    private void handleSelectResult(ArrayList<SelectedItem> resultItems) {

        // 1.找出新添加的图片
        // 2.对新添加的图片绘制水印（包含复制转存）异步，绘制成功后更新 mSelectedItem、photoAbsolutePaths算添加成功
        // 3.所有的新添加图片水印绘制完毕后调用 refresh(String[]) 刷新控件

        // 新选中的
        final ArrayList<SelectedItem> newItems = new ArrayList<>(); // without new path
        // 之前选中本次仍然选中的
        ArrayList<SelectedItem> existItems = new ArrayList<>(); // with new path
        for (SelectedItem item : resultItems) {
            int index = mSelectedItem.indexOf(item);
            if (index >= 0) {
                existItems.add(mSelectedItem.get(index));
            } else {
                newItems.add(item);
            }
        }

        // 之前选中本次又取消选中的删掉
        final ArrayList<SelectedItem> excludedItems = new ArrayList<>();
        for (SelectedItem selectedItem : mSelectedItem) {
            if (!existItems.contains(selectedItem)) {
                excludedItems.add(selectedItem);
            }
        }
        mSelectedItem.removeAll(excludedItems);

        if (newItems.isEmpty() && excludedItems.isEmpty()) {
            return;
        }

        if (newItems.isEmpty() && !excludedItems.isEmpty()) {
            List<String> excludedNewPath = SelectedItem.getNewPathList(excludedItems);
            photoAbsolutePaths.removeAll(excludedNewPath);
            refresh();
            return;
        }

        ArrayList<String> rawAbsolutePathList = new ArrayList<>();
        final ArrayList<String> newAbsolutePathList = new ArrayList<>();
        for (SelectedItem rawItem : newItems) {
            rawAbsolutePathList.add(rawItem.getRawAbsolutePath());
            String newPath = _absolutePath + rawItem.getSimpleNewFileName();
            rawItem.setNewAbsolutePath(newPath);
            newAbsolutePathList.add(newPath);
        }

        new CompressWaterMarkTask.Builder(getActivity(), rawAbsolutePathList)
                .setDestPaths(newAbsolutePathList)
                .setAddWaterMark(true)
                .setOnResultListener(new OnResultListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        if (!excludedItems.isEmpty()) {
                            photoAbsolutePaths.removeAll(SelectedItem.getNewPathList(excludedItems));
                        }
                        mSelectedItem.addAll(newItems);
                        photoAbsolutePaths.addAll(newAbsolutePathList);
                        refresh();
                        refreshNum();
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();

                        if (!excludedItems.isEmpty()) {
                            photoAbsolutePaths.removeAll(SelectedItem.getNewPathList(excludedItems));
                            refresh();
                            refreshNum();
                        }
                    }
                })
                .build()
                .executeOnExecutor(MyApplication.executorService);
    }

    /**
     * @param imgLocalAbsPaths 图片绝对路径
     */
    private void formatPic(final List<String> imgLocalAbsPaths) {
        try {
            if (imgLocalAbsPaths == null || imgLocalAbsPaths.size() == 0) {
                return;
            }

            boolean isAddWater = MyApplication.getInstance().getConfigValue("AddWaterLogo", 1) == 1;

            new CompressWaterMarkTask.Builder(getActivity(), imgLocalAbsPaths)
                    .setAddWaterMark(isAddWater)
                    .setOnResultListener(new OnResultListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            for (String path : imgLocalAbsPaths) {
                                addImageView(path);
                            }
                            Toast.makeText(getActivity(), "已添加" + photoNames.size() + "张照片", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailed(String errMsg) {
                            MyApplication.getInstance().showMessageWithHandle(errMsg);
                        }
                    })
                    .build()
                    .execute();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 点击拍照按钮，弹出选择 拍照 还是 从相册获取对话框
     */
    protected void showPopupWindow() {
        final ThreeButtonPopupWindow popupWindow = new ThreeButtonPopupWindow(getActivity());

        popupWindow.setFirstView("拍照", new OnClickListener() {
            @Override
            public void onClick(View v) {
                addOnePic();
                popupWindow.dismiss();
            }
        });

        popupWindow.setSecondView("从相册获取", new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    List<Uri> rawUris = SelectedItem.getRawUriList(mSelectedItem);
                    Matisse matisse;
                    if (getActivity() instanceof MapGISFrame) {
                        matisse = Matisse.from(getActivity());
                    } else {
                        matisse = Matisse.from(PhotoFragment.this);
                    }
                    matisse.choose(MimeType.ofImage())
                            .theme(R.style.Matisse_Zhihu_ENN)
                            .countable(false)
                            .maxSelectable(9)
                            .imageEngine(new GlideEngine())
                            .defaultSelectedItems(rawUris)
                            .forResult(REQUEST_CODE_SELECT_PIC);

                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), "未找到相册", Toast.LENGTH_LONG).show();
                    return;
                }
                popupWindow.dismiss();
            }
        });

        popupWindow.setThirdView("取消", new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.show(getActivity());
    }

    // 将照片生成为一个ImageView增加到视图上
    protected ImageView createImageView(String mediaLocalAbsPath) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                DimenTool.dip2px(getActivity(), photoBitmapWidth),
                DimenTool.dip2px(getActivity(), photoBitmapHeight));
        params.setMargins(DimenTool.dip2px(getActivity(), 2), 0, DimenTool.dip2px(getActivity(), 2), 0);

        final ThumbnailImageView imageView = new ThumbnailImageView(getActivity());
        imageView.setLayoutParams(params);

        if (photoAbsolutePaths.indexOf(mediaLocalAbsPath) == -1) {
            return imageView;
        }

        if (BaseClassUtil.isImg(mediaLocalAbsPath)) {
            Bitmap bitmap = BitmapUtil.getBitmapFromFile(mediaLocalAbsPath,
                    DimenTool.dip2px(getActivity(), photoBitmapWidth),
                    DimenTool.dip2px(getActivity(), photoBitmapHeight), true);
            if (bitmap != null) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
            }
            imageView.setImageBitmap(bitmap);
        } else if (mediaLocalAbsPath.toLowerCase().endsWith("doc") || mediaLocalAbsPath.toLowerCase().endsWith("docx")) {
            imageView.setImageResource(R.drawable.doctype);
        } else if (mediaLocalAbsPath.toLowerCase().endsWith("xls") || mediaLocalAbsPath.toLowerCase().endsWith("xlsx")) {
            imageView.setImageResource(R.drawable.xlstype);
        } else if (mediaLocalAbsPath.toLowerCase().endsWith("pdf")) {
            imageView.setImageResource(R.drawable.pdftype);
        } else if (mediaLocalAbsPath.toLowerCase().endsWith("ppt") || mediaLocalAbsPath.toLowerCase().endsWith("pptx")) {
            imageView.setImageResource(R.drawable.ppttype);
        } else {
            imageView.setImageResource(R.drawable.file_attachment);
        }

        imageView.setTag(getImageCount());
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (Integer) v.getTag();
                seePic(pos);
            }
        });

        return imageView;
    }

    private void refreshNum() {
        if (photoAbsolutePaths.size() == 0) {
            tvNum.setVisibility(View.INVISIBLE);
        } else {
            tvNum.setVisibility(View.VISIBLE);
            tvNum.setText(String.valueOf(photoAbsolutePaths.size()));
        }
    }

    /**
     * 设置照片显示宽度,默认80
     */
    public void setPhotoBitmapWidth(int photoBitmapWidth) {
        this.photoBitmapWidth = photoBitmapWidth;
    }

    /**
     * 设置照片显示宽度,默认60
     */
    public void setPhotoBitmapHeight(int photoBitmapHeight) {
        this.photoBitmapHeight = photoBitmapHeight;
    }

    /**
     * 是否创建立刻打开照相功能
     */
    public void setCreateByOpenCamera(boolean isCreateByOpenCamera) {
        this.isCreateByOpenCamera = isCreateByOpenCamera;
    }

    // 查看照片
    protected void seePic(int pos) {
        Intent intent = new Intent(getActivity(), PictureViewActivity.class);

        intent.putStringArrayListExtra("fileList", photoAbsolutePaths);
        intent.putExtra("canDelete", isAddEnable);
        intent.putExtra("pos", pos);

        if (getActivity() instanceof MapGISFrame) {
            getActivity().startActivityForResult(intent, REQUEST_CODE_SEE_PIC);
        } else {
            startActivityForResult(intent, REQUEST_CODE_SEE_PIC);
        }
    }

    /**
     * Clear then add.
     */
    protected void refresh(List<String> absolutePaths) {
        if (absolutePaths != null) {
            photoAbsolutePaths.clear();
            photoAbsolutePaths.addAll(absolutePaths);
        }
        refresh();
    }

    /**
     * 刷新界面
     */
    protected void refresh() {
        photoNames.clear();
        mediaContainer.removeAllViews();
        mediaMoreContainer.removeAllViews();
        mediaMoreScrollView.setVisibility(View.GONE);

        for (int i = 0; i < photoAbsolutePaths.size(); i++) {
            String photoAbsolutePath = photoAbsolutePaths.get(i);
            String fileName = photoAbsolutePath.substring(photoAbsolutePath.lastIndexOf('/') + 1).trim();
            photoNames.add(fileName);
            addImageView(photoAbsolutePath);
        }
    }

    /**
     * 清空视图
     */
    public void clear() {
        mediaContainer.removeAllViews();
        mediaMoreContainer.removeAllViews();
        mediaMoreScrollView.setVisibility(View.GONE);
        photoNames.clear();
        photoAbsolutePaths.clear();
    }

    /**
     * 是否可以添加照片
     */
    public void setAddEnable(boolean isAddEnable) {
        this.isAddEnable = isAddEnable;
    }

    public boolean isAddEnable() {
        return this.isAddEnable;
    }

    /**
     * 用绝对路径填充数据
     */
    public void setAbsolutePhoto(List<String> absoluteNames) {
        photoAbsolutePaths.addAll(absoluteNames);
    }

    /**
     * 获取绝对路径
     */
    public String getAbsolutePhoto() {
        return BaseClassUtil.listToString(photoAbsolutePaths);
    }

    @Override
    public String getDatabaseValue() {
        return getRelativePhoto();
    }

    @Override
    public String getLocalAbsolutePaths() {
        return getAbsolutePhoto();
    }

    @Override
    public String getServerRelativePaths() {
        return getRelativePhoto();
    }

    /**
     * 获取绝对路径
     */
    public List<String> getAbsolutePhotoList() {
        return photoAbsolutePaths;
    }

    /**
     * 获取相对路径
     */
    public String getRelativePhoto() {
        String result = "";
        for (String names : photoNames) {
            result = result + _relativePath + names + ",";
        }
        return result.trim().length() == 0 ? result : result.substring(0, result.length() - 1);
    }

    /**
     * 获取照片名
     */
    public String getNames() {
        String result = "";
        if (photoNames.size() != 0) {
            result = BaseClassUtil.listToString(photoNames);
        }
        return result;
    }

    /**
     * 获取相对路径长度值（中文占用两个长度）
     */
    private int getRelativePathLength() {
        String relativePath = getDatabaseValue();
        // 中文占用两个长度
        return relativePath.length() + BaseClassUtil.getNonAsciiCount(relativePath);
    }

    /**
     * 用相对Media路径填充数据
     */
    protected void setRelativePhoto(List<String> relativeNames) {
        for (final String relName : relativeNames) {
            String relativePath;
            if (!BaseClassUtil.isFullWebUrl(relName)) {
                relativePath = relName;
            } else {
                // 对于数据库存储的是完整 url的图片，本地都下载到 Media/temp/ 文件夹下
                // 如：http://***/buffile/OutFiles/UpLoadFiles/Repair/QX-2017-0030376/filename.jpg
                // 会存储到 Media/temp/filename.jpg
                relativePath = MEDIA_TEMP_FOLDER_NAME + relName.substring(relName.lastIndexOf("/"));
                // 存储完全 url
                rawPhotoFullWebUrl.add(relName);
            }
            photoAbsolutePaths.add(MyApplication.getInstance().getMediaPathString() + relativePath);
        }
    }

    /**
     * 从服务下载图片
     */
    protected void downloadFromWeb() {
        if (photoAbsolutePaths.size() == 0) {
            return;
        }

        final String mediaRootPath =  MyApplication.getInstance().getMediaPathString();
        final String mediaRootWebUrl = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                + "/OutFiles/UpLoadFiles/";
        try {
            for (int i = 0, length = photoAbsolutePaths.size(); i < length; i++) {
                final String absolutePath = photoAbsolutePaths.get(i);
                if (!BaseClassUtil.isImg(absolutePath)) {
                    continue;
                }

                String fileName = absolutePath.substring(absolutePath.lastIndexOf('/') + 1).trim();
                photoNames.add(fileName);

                ImageView imageView = createImageView(absolutePath);
                addImageView(imageView);

                String photoUrl = "";
                final String relativePath = absolutePath.replace(mediaRootPath, "");
                // 判断是否是有可能是完全 url的图片，规则与构造的时候相匹配
                if (relativePath.startsWith(MEDIA_TEMP_FOLDER_NAME + "/")) {
                    String tempPath = relativePath.replace(MEDIA_TEMP_FOLDER_NAME, ""); // eg: /filename.jpg
                    for (String rawWebUrl : rawPhotoFullWebUrl) {
                        // 简单的通过名字匹配
                        if (rawWebUrl.endsWith(tempPath)) {
                            photoUrl = rawWebUrl;
                            break;
                        }
                    }
                }
                if (TextUtils.isEmpty(photoUrl)) {
                    // 正常存储的相对路径的 url拼接
                    photoUrl = mediaRootWebUrl + relativePath;
                }
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                MmtImageLoader.getInstance().showBitmap(photoUrl, absolutePath, imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addImageView(String imgLocalAbsPath) {
        int index = photoAbsolutePaths.indexOf(imgLocalAbsPath);
        if (index == -1) {
            return;
        }
        ImageView imageView = createImageView(imgLocalAbsPath);
        addImageView(imageView);
    }

    protected void addImageView(ImageView imageView) {
        if (getImageCountInFirstRow() < getMaxCountInFirstRow()) {
            if (mediaMoreScrollView.getVisibility() != View.GONE) {
                mediaMoreScrollView.setVisibility(View.GONE);
            }
            mediaContainer.addView(imageView);
        } else {
            if (mediaMoreScrollView.getVisibility() != View.VISIBLE) {
                mediaMoreScrollView.setVisibility(View.VISIBLE);
            }
            mediaMoreContainer.addView(imageView);
        }
    }

    protected int getImageCount() {
        int count = getImageCountInFirstRow();
        for (int i = 0, length = mediaMoreContainer.getChildCount(); i < length; i++) {
            View childView = mediaMoreContainer.getChildAt(i);
            if (childView instanceof ThumbnailImageView) {
                count++;
            }
        }
        return count;
    }

    private int getImageCountInFirstRow() {
        int count = 0;
        for (int i = 0, length = mediaContainer.getChildCount(); i < length; i++) {
            View childView = mediaContainer.getChildAt(i);
            if (childView instanceof ThumbnailImageView) {
                count++;
            }
        }
        return count;
    }

    private int getMaxCountInFirstRow() {
        return isAddEnable ? maxCountInFirstRowEditable : maxCountInFirstRowReadonly;
    }

    public class ThumbnailImageView extends android.support.v7.widget.AppCompatImageView {
        public ThumbnailImageView(Context context) {
            super(context);
        }
        public ThumbnailImageView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }
        public ThumbnailImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
    }
}
