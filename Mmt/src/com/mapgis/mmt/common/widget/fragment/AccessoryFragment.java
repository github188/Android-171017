package com.mapgis.mmt.common.widget.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cameralibary.CameraActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.CompressWaterMarkTask;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.PictureViewActivity;
import com.mapgis.mmt.common.widget.RecorderPlayActivity;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.global.OnResultListener;

import org.apache.tools.ant.util.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by liuyunfan on 2015/8/28.
 * 录音和照片选择
 */
public class AccessoryFragment extends Fragment {

    private final int REQUEST_CODE_SEE_PIC = 10002;
    private final int REQUEST_CODE_SELECT_PIC = 10003;

    //点击录音后返回
    private final int RESULT_CODE_SELECT_REC = 10001;
    private ImageView popImageView;
    private ImageView imageView;
    private HorizontalScrollView picScrollView;
    private LinearLayout horizontalScrollViewLinear;

    private boolean isAddEnable = true;

    //当前所有录音名
    private final ArrayList<String> recorderNames = new ArrayList<String>();
    /**
     * 记录 录音 的个数， 包括 已经 删除的 录音
     */
    private int recorderCount = 0;

    // 录音相对路径
    private final String _recordRelativePath;
    // 录音绝对路径
    private final String _recordAbsolutePath;
    // 当前所有录音的绝对路径
    private final ArrayList<String> recordAbsolutePaths = new ArrayList<String>();
    private boolean hideDelBtn = false;

    public boolean isHideDelBtn() {
        return hideDelBtn;
    }

    public void setHideDelBtn(boolean hideDelBtn) {
        this.hideDelBtn = hideDelBtn;
    }


    // 当前所有照片名称
    private final ArrayList<String> photoNames = new ArrayList<String>();

    // 当前所有照片的绝对路径
    private final ArrayList<String> photoAbsolutePaths = new ArrayList<String>();

    // 是否通过UUID命名
    private boolean isUUIDName;
    // 命名时可能需要的前缀
    private String prefixName;

    // 当前操作的照片名称
    private String _photoName;
    // 相对路径
    private final String _photoRelativePath;
    // 绝对路径
    private final String _photoAbsolutePath;

    private int photoBitmapWidth = 80;

    private int photoBitmapHeight = 60;


    //附件选择对话框
    private View contentView;
    PopupWindow popupWindow = null;

    //是否显示从相册选取，默认没有从相册选取一项
    private boolean choseFromAlbum = false;

//    //水印文本，waterTexts.size()>0 则代表加水印
//    private ArrayList<String> waterTexts;
    public AccessoryFragment() {
        this(null, null);
    }
//    public void setWaterTexts(ArrayList<String> waterTexts) {
//        this.waterTexts = waterTexts;
//    }

    public void setChoseFromAlbum(boolean choseFromAlbum) {
        this.choseFromAlbum = choseFromAlbum;
    }

    @SuppressLint("ValidFragment")
    public AccessoryFragment(String photoRelativePath, String recordRelativePath) {

        //照片
        if (BaseClassUtil.isNullOrEmptyString(photoRelativePath)) {
            photoRelativePath = "temp/";
        }

        this._photoRelativePath = photoRelativePath;

        _photoAbsolutePath = MyApplication.getInstance().getMediaPathString() + photoRelativePath;

        File photoFile = new File(_photoAbsolutePath);
        if (!photoFile.exists()) {
            photoFile.mkdirs();
        }


        //录音
        if (BaseClassUtil.isNullOrEmptyString(recordRelativePath)) {
            recordRelativePath = "temp/";
        }

        this._recordRelativePath = recordRelativePath;

        _recordAbsolutePath = MyApplication.getInstance().getMediaPathString() + recordRelativePath;

        File recordFile = new File(_recordAbsolutePath);
        if (!recordFile.exists()) {
            recordFile.mkdirs();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.accessory_fragment, container, false);
        initView(view);

        choseFromAlbum = MyApplication.getInstance().getConfigValue("choseFromAlbum", 0) == 1;

        if (choseFromAlbum) {
            contentView.findViewById(R.id.tvAlbum).setVisibility(View.VISIBLE);
            contentView.findViewById(R.id.tvAlbumLine).setVisibility(View.VISIBLE);
        } else {
            contentView.findViewById(R.id.tvAlbum).setVisibility(View.GONE);
            contentView.findViewById(R.id.tvAlbumLine).setVisibility(View.GONE);
        }
        return view;
    }

    // 界面初始化
    private void initView(View rootView) {
        popImageView = (ImageView) rootView.findViewById(R.id.ivAdd);
        popImageView.setVisibility(isAddEnable ? View.VISIBLE : View.GONE);
        popImageView.setMinimumHeight(DimenTool.dip2px(getActivity(), photoBitmapHeight));

        picScrollView = (HorizontalScrollView) rootView.findViewById(R.id.recorderScrollView);
        picScrollView.setHorizontalScrollBarEnabled(false);
        horizontalScrollViewLinear = (LinearLayout) rootView.findViewById(R.id.horizontalScrollViewLinear);

        // refreash();
        horizontalScrollViewLinear.removeAllViews();
        initPopUpWindow();
        popImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });

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


    private void recordVoice() {
        MediaRecorderDialogFragment recDialogFrg = new MediaRecorderDialogFragment();

        Bundle bundle = new Bundle();

        bundle.putString("path", _recordAbsolutePath);

        recDialogFrg.setArguments(bundle);

        recDialogFrg.setSaveBtnListener(new MediaRecorderDialogFragment.OnSaveBtnClickListener() {
            @Override
            public void onSaveBtnClick(String recorderFileName) {
                // recorderFileName 是 录音文件名
                addOneRecord(recorderFileName);
            }
        });
        recDialogFrg.setCancelable(true);
        recDialogFrg.show(getFragmentManager(), "");
    }

    private void addOneRecord(String recorderFileName) {
        // 绝对路径 列表
        if (!recorderNames.contains(recorderFileName)) {
            recordAbsolutePaths.add(_recordAbsolutePath + recorderFileName);
            recorderNames.add(recorderFileName);
        }

        FrameLayout itemLayout = new FrameLayout(getActivity());
        itemLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.record_item, null);
        itemLayout.setId(itemLayout.hashCode());
        itemLayout.setTag(recorderFileName);

        TextView titleTV = (TextView) itemLayout.findViewById(R.id.titleTV);
        titleTV.setText("录音" + (++recorderCount));

        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecord((String) (v.getTag()), recorderNames.indexOf((v.getTag())));
            }
        });

        itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                final String deleteRecord = (String) (v.getTag());

                TextView textView = new TextView(getActivity());
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setText("确定要删除这个录音文件吗？");
                textView.setTextAppearance(getActivity(), R.style.default_text_medium_1);

                final OkCancelDialogFragment okCancelDlgFrgt = new OkCancelDialogFragment("删除录音", textView);
                okCancelDlgFrgt.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                    @Override
                    public void onLeftButtonClick(View view) {
                        okCancelDlgFrgt.dismiss();
                    }
                });

                okCancelDlgFrgt.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        recorderNames.remove(recorderNames.indexOf(deleteRecord));
                        File delFile = new File(_recordAbsolutePath + deleteRecord);
                        if (delFile.exists())
                            delFile.delete();
                        horizontalScrollViewLinear.removeView(v);
                        horizontalScrollViewLinear.postInvalidate();
                        Toast.makeText(getActivity(), "已删除" + deleteRecord, Toast.LENGTH_SHORT).show();
                    }
                });

                okCancelDlgFrgt.show(getFragmentManager(), "");

                return false;
            }
        });

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        horizontalScrollViewLinear.addView(itemLayout, lp);

    }

    private void oneRecDown(String recorderFileName) {
        recorderNames.add(recorderFileName);

        FrameLayout itemLayout = new FrameLayout(getActivity());
        itemLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.record_item, null);
        itemLayout.setId(itemLayout.hashCode());
        itemLayout.setTag(recorderFileName);

        TextView titleTV = (TextView) itemLayout.findViewById(R.id.titleTV);
        titleTV.setText("录音" + (++recorderCount));

        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecord((String) (v.getTag()), recorderNames.indexOf((v.getTag())));
            }
        });

        itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                final String deleteRecord = (String) (v.getTag());

                TextView textView = new TextView(getActivity());
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setText("确定要删除这个录音文件吗？");
                textView.setTextAppearance(getActivity(), R.style.default_text_medium_1);

                final OkCancelDialogFragment okCancelDlgFrgt = new OkCancelDialogFragment("删除录音", textView);
                okCancelDlgFrgt.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                    @Override
                    public void onLeftButtonClick(View view) {
                        okCancelDlgFrgt.dismiss();
                    }
                });

                okCancelDlgFrgt.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        recorderNames.remove(recorderNames.indexOf(deleteRecord));
                        File delFile = new File(_recordAbsolutePath + deleteRecord);
                        if (delFile.exists())
                            delFile.delete();
                        horizontalScrollViewLinear.removeView(v);
                        horizontalScrollViewLinear.postInvalidate();
                        Toast.makeText(getActivity(), "已删除" + deleteRecord, Toast.LENGTH_SHORT).show();
                    }
                });

                okCancelDlgFrgt.show(getFragmentManager(), "");

                return false;
            }
        });

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        horizontalScrollViewLinear.addView(itemLayout, lp);

    }

    /**
     * 播放指定路径的声音文件
     *
     * @param recordPath 文件的完整路径名
     * @param pos        当前点击的 是 第几条 录音 ， pos=0 表示 第一条，
     */
    private void playRecord(String recordPath, int pos) {

        Intent intent = new Intent(getActivity(), RecorderPlayActivity.class);
        intent.putStringArrayListExtra("fileList", recordAbsolutePaths);
        intent.putExtra("hideDelBtn", hideDelBtn);
        intent.putExtra("pos", pos);
        startActivityForResult(intent, RESULT_CODE_SELECT_REC);
        MyApplication.getInstance().startActivityAnimation(getActivity());

    }

    // 调用系统照相功能
    private void addOnePic() {
        if (isUUIDName) {
            _photoName = BaseClassUtil.isNullOrEmptyString(prefixName) ? UUID.randomUUID().toString() : prefixName
                    + UUID.randomUUID().toString() + ".jpg";
        } else {
            _photoName = BaseClassUtil.getSystemTimeForFile() + ".jpg";
        }

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        // 0：使用系统自带相机；1：使用程序定制相机
        if (MyApplication.getInstance().getConfigValue("MmtCamera", -1) > 0) {
            intent = new Intent(getActivity(), CameraActivity.class);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(_photoAbsolutePath + _photoName)));

        if (getActivity() instanceof MapGISFrame) {
            getActivity().startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
        } else {
            AccessoryFragment.this.startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1) {// 拍照成功后返回

            photoNames.add(_photoName);
            String absPath = _photoAbsolutePath + _photoName;
            photoAbsolutePaths.add(absPath);

            imgAddWaterText(new ArrayList<String>() {{
                add(_photoAbsolutePath + _photoName);
            }});

        } else if (requestCode == REQUEST_CODE_SEE_PIC) {// 预览图片之后的回调

            if (data == null) {
                return;
            }
            List<String> deleteList = data.getStringArrayListExtra("delList");
            if (deleteList == null || deleteList.size() <= 0) {
                return;
            }
            for (int i = 0; i < horizontalScrollViewLinear.getChildCount(); i++) {

                if (horizontalScrollViewLinear.getChildAt(i).getTag() instanceof Integer) {
                    int index = (int) horizontalScrollViewLinear.getChildAt(i).getTag();
                    if (deleteList.contains(photoAbsolutePaths.get(index))) {
                        horizontalScrollViewLinear.removeView(horizontalScrollViewLinear.getChildAt(i));
                    }
                }
            }
            photoAbsolutePaths.remove(deleteList);
            for (String filePath : deleteList) {
                photoNames.remove(filePath.substring(filePath.lastIndexOf("/")));
            }

        } else if (requestCode == REQUEST_CODE_SELECT_PIC && resultCode == Activity.RESULT_OK) {
            List<File> files = null;
            if (data != null) {
                String imgRealPath = Convert.getRealFilePath(getActivity(), data.getData());
                List<String> temp = new ArrayList<String>();
                temp.add(imgRealPath);
                files = copyImage(temp);
            }
            if (null == files || files.size() <= 0) {
                Toast.makeText(getActivity(), "该照片不存在", Toast.LENGTH_SHORT).show();
                return;

            }


            List<String> photoAbsolutePaths_newAdd = new ArrayList<String>();
            for (File file : files) {
                photoNames.add(file.getName());
                photoAbsolutePaths_newAdd.add(file.getAbsolutePath());
            }
            photoAbsolutePaths.addAll(photoAbsolutePaths_newAdd);

            imgAddWaterText(photoAbsolutePaths_newAdd);

        } else if (requestCode == RESULT_CODE_SELECT_REC) {//预览录音后回调

            if (data == null) {
                return;
            }
            List<String> deleteList = data.getStringArrayListExtra("deleteRecList");
            if (deleteList == null || deleteList.size() <= 0) {
                return;
            }

            for (int i = 0; i < horizontalScrollViewLinear.getChildCount(); i++) {
                if (horizontalScrollViewLinear.getChildAt(i).getTag() instanceof String) {
                    String name = (String) horizontalScrollViewLinear.getChildAt(i).getTag();
                    if (deleteList.contains(_recordAbsolutePath + name)) {
                        horizontalScrollViewLinear.removeView(horizontalScrollViewLinear.getChildAt(i));
                        recorderNames.remove(name);
                        recordAbsolutePaths.remove(_recordAbsolutePath + name);
                    }
                }
            }
        }

    }

    /**
     * @param urls 图片绝对路径
     */
    private void imgAddWaterText(final List<String> urls) {

        if (urls == null || urls.size() == 0) {
            return;
        }

        boolean isAddWater = MyApplication.getInstance().getConfigValue("AddWaterLogo", 1) == 1;

        if (!isAddWater) {
            for (String url : urls) {
                horizontalScrollViewLinear.addView(createImageView(url));
            }
            Toast.makeText(getActivity(), "已添加" + photoNames.size() + "张照片", Toast.LENGTH_SHORT).show();

            return;
        }

        new CompressWaterMarkTask.Builder(getActivity(), urls)
                .setOnResultListener(new OnResultListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        for (String url : urls) {
                            horizontalScrollViewLinear.addView(createImageView(url));
                        }
                        Toast.makeText(getActivity(), "已添加" + photoNames.size() + "张照片", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        MyApplication.getInstance().showMessageWithHandle(errMsg);
                    }
                }).build().execute();

    }

    private void initPopUpWindow() {
        contentView = LayoutInflater.from(getActivity()).inflate(R.layout.add_attachment, null);
        popupWindow = new PopupWindow(contentView, -1, -2, true);
        popupWindow.setContentView(contentView);
        //设置SelectPicPopupWindow弹出窗体动画效果
        popupWindow.setAnimationStyle(R.style.AnimBottom);

        //实例化一个ColorDrawable颜色为半透明,设置SelectPicPopupWindow弹出窗体的背景
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xb0000000));

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        contentView.findViewById(R.id.tvCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOnePic();
                popupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.tvAlbum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent("android.intent.action.PICK");
                    intent.setType("image/*");
                    if (getActivity() instanceof MapGISFrame) {
                        getActivity().startActivityForResult(intent, REQUEST_CODE_SELECT_PIC);
                    } else {
                        startActivityForResult(intent, REQUEST_CODE_SELECT_PIC);
                    }
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), "未找到相册", Toast.LENGTH_LONG).show();
                    return;
                }
                popupWindow.dismiss();

            }
        });

        contentView.findViewById(R.id.tvVoice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordVoice();
                popupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void showPopupWindow(View view) {
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    // 将照片生成为一个ImageView增加到视图上
    private ImageView createImageView(int pos) {

        return createImageView(photoAbsolutePaths.get(pos), pos);

    }

    // 将照片生成为一个ImageView增加到视图上
    private ImageView createImageView(String path) {
        int pos = -1;

        for (int i = 0; i < photoAbsolutePaths.size(); i++) {
            if (path.equals(photoAbsolutePaths.get(i))) {
                pos = i;
                break;
            }
        }

        return createImageView(path, pos);
    }

    // 将照片生成为一个ImageView增加到视图上
    private ImageView createImageView(String path, int pos) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(DimenTool.dip2px(getActivity(), photoBitmapWidth), DimenTool.dip2px(getActivity(),
                photoBitmapHeight));
        final ImageView imageView = new ImageView(getActivity());
        imageView.setLayoutParams(params);

        if (pos < 0) {
            return imageView;
        }
        if (pos > photoAbsolutePaths.size() - 1) {
            return imageView;
        }

        if (BaseClassUtil.isImg(path)) {
            Bitmap bitmap = BitmapUtil.getBitmapFromFile(path, photoBitmapWidth, photoBitmapHeight);

            imageView.setImageBitmap(bitmap);
        } else if (path.toLowerCase().endsWith("doc") || path.toLowerCase().endsWith("docx")) {
            imageView.setImageResource(R.drawable.doctype);
        } else if (path.toLowerCase().endsWith("xls") || path.toLowerCase().endsWith("xlsx")) {
            imageView.setImageResource(R.drawable.xlstype);
        } else if (path.toLowerCase().endsWith("pdf")) {
            imageView.setImageResource(R.drawable.pdftype);
        } else if (path.toLowerCase().endsWith("ppt") || path.toLowerCase().endsWith("pptx")) {
            imageView.setImageResource(R.drawable.ppttype);
        } else if (path.toLowerCase().endsWith("wav")) {
            imageView.setImageResource(R.drawable.record_pen);
        } else {
            imageView.setImageResource(R.drawable.file_attachment);
        }
        imageView.setPadding(DimenTool.dip2px(getActivity(), 5), 0, DimenTool.dip2px(getActivity(), 5), 0);

        imageView.setTag(pos);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果是图片就直接查看图片，录音打开录音播放按钮，其他的打开浏览器下载
                int pos = (Integer) v.getTag();
                seePic(pos);
            }
        });

        return imageView;
    }

    /**
     * 设置照片显示宽度,默认80
     */
    public void setPhotoBitmapWidth(int photoBitmapWidth) {
        this.photoBitmapWidth = photoBitmapWidth;
    }

//    /**
//     * 设置照片显示宽度,默认60
//     */
//    public void setPhotoBitmapHeight(int photoBitmapHeight) {
//        this.photoBitmapHeight = photoBitmapHeight;
//    }

    // 查看照片
    private void seePic(int pos) {
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

//    private Bitmap decode(String url, int size) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inDither = false; /* 不进行图片抖动处理 */
//        // options.inPreferredConfig=null; /*设置让解码器以最佳方式解码*/
//        options.inSampleSize = size; /* 图片长宽方向缩小倍数 */
//
//        try {
//            return (BitmapFactory.decodeFile(url, options));
//        } catch (OutOfMemoryError e) {
//            return decode(url, size + 1);
//        }
//    }

    /**
     * 清空视图
     */
    public void clear() {
        horizontalScrollViewLinear.removeAllViews();
        photoNames.clear();
        photoAbsolutePaths.clear();
    }

    /**
     * 是否可以添加照片
     */
    public void setAddEnable(boolean isAddEnable) {
        this.isAddEnable = isAddEnable;
    }

    /**
     * 用户文件名称填充数据
     */
    public void setPhotoNames(List<String> names) {
        for (String name : names) {
            photoAbsolutePaths.add(_photoAbsolutePath + name);
        }
    }

    /**
     * 用相对Media路径填充数据
     */
    public void setRelativePhoto(List<String> relativeNames) {
        for (String name : relativeNames) {
            photoAbsolutePaths.add(MyApplication.getInstance().getMediaPathString() + name);
        }
    }


    /**
     * 获取录音绝对路径
     */
    public String getAbsoluteRec() {
        String result = BaseClassUtil.listToString(recordAbsolutePaths);
        return result;
    }

    /**
     * 获取录音相对路径
     */
    public String getRelativeRec() {
        String result = "";
        for (String names : recorderNames) {
            result = result + _recordRelativePath + names + ",";
        }
        return result.trim().length() == 0 ? result : result.substring(0, result.length() - 1);
    }

    /**
     * 用绝对路径填充数据
     */
    public void setAbsolutePhoto(List<String> absoluteNames) {
        photoAbsolutePaths.addAll(absoluteNames);
    }

    /**
     * 获取照片绝对路径
     */
    public String getAbsolutePhoto() {
        String result = BaseClassUtil.listToString(photoAbsolutePaths);
        return result;
    }


    public List<String> getAbsolutePhotoList() {
        return photoAbsolutePaths;
    }

    public String getRelativePhoto() {
        String result = "";
        for (String names : photoNames) {
            result = result + _photoRelativePath + names + ",";
        }
        return result.trim().length() == 0 ? result : result.substring(0, result.length() - 1);
    }

    //获取附件的相对路径，包括照片和录音
    public String getRelatePath() {
        return getRelativeRec().length() > 0 ? getRelativePhoto() + "," + getRelativeRec() : getRelativePhoto();
    }

    //获取附件的绝对路径，包括照片和录音
    public String getAbsolutePath() {
        return getAbsoluteRec().length() > 0 ? getAbsolutePhoto() + "," + getAbsoluteRec() : getAbsolutePhoto();
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
     * 将目标文件复制到当前控件存放照片的位置,文件名用老文件的创建时间
     *
     * @param imageFilePaths
     * @return 复制后的文件
     */
    private List<File> copyImage(List<String> imageFilePaths) {
        List<File> files = new ArrayList<File>();

        if (imageFilePaths == null || imageFilePaths.size() == 0) {
            return files;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyMMdd_HHmmss");

        for (String path : imageFilePaths) {

            try {
                File file = new File(path);

                if (!file.exists()) {
                    continue;
                }

                File destFile = new File(_photoAbsolutePath + format.format(new Date(file.lastModified())) + ".jpg");

                if (destFile.exists()) {
                    destFile.delete();
                }

                FileUtils.newFileUtils().copyFile(file, destFile);

                files.add(destFile);

                //  file.delete();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return files;
    }
}
