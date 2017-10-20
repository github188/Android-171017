package com.mapgis.mmt.common.widget.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.attach.IUploadFile;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.RecorderPlayActivity;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.engine.SDCardManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecorderFragment extends Fragment implements IUploadFile {

    private final ArrayList<String> recorderNames = new ArrayList<String>();
    /**
     * 记录录音的个数，包括已经删除的录音
     */
    private int recorderCount = 0;

    /**
     * 字段上传到数据库的最大长度（即所有文件的相对路径长度）
     */
    private int maxValueLength = 0;

    private ImageView imageView;
    private HorizontalScrollView recorderScrollView;
    private LinearLayout horizontalScrollViewLinear;

    private boolean isAddEnable = true;

    // 相对路径
    private String _relativePath;
    // 绝对路径
    private String _absolutePath;

    // 当前所有录音的绝对路径
    private final ArrayList<String> recordAbsolutePaths = new ArrayList<>();

    /**
     * 相对路径，格式形如： “Maintenance/wxyh-1111-11111111/” 前面不要斜杠，末尾加斜杠
     */
    private boolean hideDelBtn = false;

    public boolean isHideDelBtn() {
        return hideDelBtn;
    }

    public void setHideDelBtn(boolean hideDelBtn) {
        this.hideDelBtn = hideDelBtn;
    }

    public void setMaxValueLength(int maxLength) {
        this.maxValueLength = maxLength;
    }

    public static RecorderFragment newInstance(String relativePath) {
        if (TextUtils.isEmpty(relativePath)) {
            throw new IllegalArgumentException("relativePath must not be empty.");
        }

        Bundle args = new Bundle();
        args.putString("relativePath", relativePath);
        RecorderFragment fragment = new RecorderFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPath();
    }

    /**
     * Initialize path info.
     */
    private void initPath() {

        Bundle args = getArguments();

        this._relativePath = args.getString("relativePath");
        this._absolutePath = MyApplication.getInstance().getRecordPathString() + _relativePath;

        File file = new File(_absolutePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private GDControl gdControl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recorder_fragment, container, false);

        if (container.getParent() instanceof ImageFragmentView
                && (((ImageFragmentView) container.getParent()).getTag()) instanceof GDControl)
            gdControl = (GDControl) ((ImageFragmentView) container.getParent()).getTag();

        initView(view);

        initData();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private List<File> findCallRecFiles() {
        List<File> files = new ArrayList<>();

        String path = MyApplication.getInstance().getSystemSharedPreferences().getString("call_rec_dir", "");

        if (TextUtils.isEmpty(path)) {
            ArrayMap map = MyApplication.getInstance().getConfigValue("CallRecDir", ArrayMap.class);

            if (map.containsKey(Build.MANUFACTURER))
                path = map.get(Build.MANUFACTURER).toString();
            else {
                String msg = "【" + this.gdControl.DisplayName + "】未适配该型号【" + Build.MANUFACTURER + "】手机";

                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

                return files;
            }
        }

        SDCardManager manager = new SDCardManager(getActivity());

        for (String dir : manager.getVolumePaths()) {
            File recDir = new File(dir + path);

            if (recDir.exists() && recDir.list() != null && recDir.list().length > 0) {
                files.addAll(Arrays.asList(recDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith(".mp3") || filename.endsWith(".amr");
                    }
                })));
            }
        }

        if (files.size() == 0)
            return files;

        Collections.sort(files, new Comparator<File>() {

            @Override
            public int compare(File lhs, File rhs) {
                return (lhs.lastModified() > rhs.lastModified()) ? -1 : 1;
            }
        });

        return files;
    }

    private void onCallRecSelected(File file) {
        try {
            String name = file.getName();

            String path = _absolutePath + name;

            if (recordAbsolutePaths.contains(path)) {
                Toast.makeText(getActivity(), "录音已添加，无需重复添加", Toast.LENGTH_SHORT).show();

                return;
            }

            File mmtFile = new File(path);

            if (!mmtFile.exists()) {
                FileUtil.copyFileByChannel(file, mmtFile);
            }

            addOneRecord(name);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean selectCallRec() {
        try {
            final List<File> files = findCallRecFiles();

            if (files.size() == 0)
                return false;

            List<String> names = new ArrayList<>();

            for (int k = 0; k < files.size() && k < 6; k++) {
                names.add(files.get(k).getName());
            }

            ListDialogFragment fragment = new ListDialogFragment("最近通话录音", names);

            fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                @Override
                public void onListItemClick(int arg2, String value) {
                    onCallRecSelected(files.get(arg2));
                }
            });

            fragment.show(getFragmentManager(), "");

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

    private void initView(View rootView) {
        imageView = (ImageView) rootView.findViewById(R.id.recorderImg);
        imageView.setVisibility(isAddEnable ? View.VISIBLE : View.GONE);

        recorderScrollView = (HorizontalScrollView) rootView.findViewById(R.id.recorderScrollView);
        recorderScrollView.setHorizontalScrollBarEnabled(false);
        horizontalScrollViewLinear = (LinearLayout) rootView.findViewById(R.id.horizontalScrollViewLinear);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (maxValueLength > 0) {
                        int currentLen = getRelativePathLength();
                        if (currentLen > 0) {
                            int predictLen = currentLen / recorderNames.size() + currentLen;
                            if (predictLen >= maxValueLength) {
                                Toast.makeText(getContext(), "录音数量超限，无法继续添加", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }

                    if (gdControl != null && gdControl.ConfigInfo.equals("通话录音")) {
                        if (selectCallRec())
                            return;
                        else
                            Toast.makeText(getActivity(), "未找到最近通话录音,请录音说明", Toast.LENGTH_SHORT).show();
                    }

                    MediaRecorderDialogFragment fragment = new MediaRecorderDialogFragment();

                    Bundle bundle = new Bundle();

                    bundle.putString("path", _absolutePath);

                    fragment.setArguments(bundle);
                    fragment.setSaveBtnListener(new MediaRecorderDialogFragment.OnSaveBtnClickListener() {
                        @Override
                        public void onSaveBtnClick(String recorderFileName) {
                            // recorderFileName 是 录音文件名
                            addOneRecord(recorderFileName);
                        }
                    });

                    fragment.setCancelable(true);
                    fragment.show(getFragmentManager(), "");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * 将需要显示的数据初始化在界面上，例 在历史事件中 需要查看上报过的录音
     */
    private void initData() {
        for (String path : recordAbsolutePaths) {
            FrameLayout itemLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.record_item, null);
            itemLayout.setId(itemLayout.hashCode());
            itemLayout.setTag(path);

            TextView titleTV = (TextView) itemLayout.findViewById(R.id.titleTV);
            titleTV.setText("录音" + (++recorderCount));

            TextView lengthTV = (TextView) itemLayout.findViewById(R.id.lengthTV);
            lengthTV.setVisibility(View.GONE);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playRecord((String) (v.getTag()), recordAbsolutePaths.indexOf((v.getTag())));
                }
            });

            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            horizontalScrollViewLinear.addView(itemLayout, lp);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 29 && data != null && data.getStringArrayListExtra("deleteRecList") != null) {
            List<String> deleteList = data.getStringArrayListExtra("deleteRecList");

            List<View> delViewList = new ArrayList<View>(); // 存储 需要 删除 的 录音View
            for (int i = 0; i < horizontalScrollViewLinear.getChildCount(); i++) {
                if (deleteList.contains(_absolutePath + horizontalScrollViewLinear.getChildAt(i).getTag())) {
                    delViewList.add(horizontalScrollViewLinear.getChildAt(i));
                }
            }
            for (View one : delViewList) { // 　遍历删除　view
                horizontalScrollViewLinear.removeView(one);
                recorderNames.remove(one.getTag());
            }
            // 删除 绝对路径列表 中 的 录音路径
            recordAbsolutePaths.removeAll(deleteList);
        }
    }

    private void addOneRecord(String recorderFileName) {
        // 绝对路径 列表
        if (!recordAbsolutePaths.contains(recorderFileName)) {
            recordAbsolutePaths.add(_absolutePath + recorderFileName);
            recorderNames.add(recorderFileName);
        }

        FrameLayout itemLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.record_item, null);
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
                textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
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
                        File delFile = new File(_absolutePath + deleteRecord);
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

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
                textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
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
                        File delFile = new File(_absolutePath + deleteRecord);
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

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        horizontalScrollViewLinear.addView(itemLayout, lp);

    }

    /**
     * 清空视图
     */
    public void clear() {
        horizontalScrollViewLinear.removeAllViews();
        recorderNames.clear();
        recordAbsolutePaths.clear();
    }

    /**
     * 设置是否允许使用录音功能
     */
    public void setRecoderEnable(boolean enable) {
        isAddEnable = enable;
    }

    public void setRecorderData(List<String> names) {
        for (String name : names) {
            recorderNames.add(name);
        }
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
        startActivityForResult(intent, 3);
        MyApplication.getInstance().startActivityAnimation(getActivity());

    }

    public List<String> getRecordList() {
        return recorderNames;
    }

    public String getValue() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recorderNames.size(); i++) {
            String fileName = recorderNames.get(i).substring(recorderNames.get(i).lastIndexOf("/") + 1);
            sb.append(fileName);
            if (i < recorderNames.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public String getPathValue() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recorderNames.size(); i++) {
            sb.append(recorderNames.get(i));
            if (i < recorderNames.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * 用户文件名称填充数据
     */
    public void setNames(List<String> names) {
        for (String name : names) {
            recordAbsolutePaths.add(_absolutePath + name);
        }
    }

    /**
     * 用相对Media路径填充数据
     */
    public void setRelativeRec(List<String> relativeNames) {
        for (String name : relativeNames) {
            recordAbsolutePaths.add(MyApplication.getInstance().getRecordPathString() + name);
        }
    }

    /**
     * 用绝对路径填充数据
     */
    public void setAbsoluteRec(List<String> absoluteNames) {
        recordAbsolutePaths.addAll(absoluteNames);
    }

    /**
     * 获取绝对路径
     */
    public String getAbsoluteRec() {
        String result = BaseClassUtil.listToString(recordAbsolutePaths);
        return result;
    }

    /**
     * 获取相对路径
     */
    public String getRelativeRec() {
        String result = "";
        for (String names : recorderNames) {
            result = result + _relativePath + names + ",";
        }
        return result.trim().length() == 0 ? result : result.substring(0, result.length() - 1);
    }

    /**
     * 获取录音名
     */
    public String getNames() {
        String result = BaseClassUtil.listToString(recorderNames);
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

    public void downloadFromWeb(String relativePhoto) {
        new DownloadTask().executeOnExecutor(MyApplication.executorService, relativePhoto);
    }

    @Override
    public String getDatabaseValue() {
//        return getNames();
        return getServerRelativePaths();
    }

    @Override
    public String getLocalAbsolutePaths() {
        return getAbsoluteRec();
    }

    @Override
    public String getServerRelativePaths() {
        return getRelativeRec().replaceAll(".amr", ".wav");
    }

    /**
     * 下载附属文件信息
     */
    private class DownloadTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {

                for (String path : params[0].split(",")) {

                    String absolutePath = MyApplication.getInstance().getRecordPathString() + path;

                    if (!recordAbsolutePaths.contains(absolutePath)) {
                        recordAbsolutePaths.add(absolutePath);
                    }

                    File file = new File(absolutePath);

                    // 文件夹路径不存在，则创建文件夹
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    // 文件不存在，则创建并下载文件
                    if (!file.exists()) {
                        file.createNewFile();
                        NetUtil.downloadFile(path, file);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            horizontalScrollViewLinear.removeAllViews();

            for (int i = 0; i < recordAbsolutePaths.size(); i++) {
                String fileName = recordAbsolutePaths.get(i);

                fileName = fileName.substring(fileName.lastIndexOf('/') + 1).trim();

                oneRecDown(fileName);
            }
        }
    }
}
