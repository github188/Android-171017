package com.mapgis.mmt.common.attach;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.widget.PictureViewActivity;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.entity.docment.Document;
import com.mapgis.mmt.entity.docment.DocumentFactory;
import com.mapgis.mmt.module.gis.MapGISFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * 附件控件
 */
public class AttachFileFragment extends Fragment implements IUploadFile {
    private final static String TAG = AttachFileFragment.class.getSimpleName();

    private static final int REQUEST_CODE = 1;
    // 默认，
    public final static int FLAG_DEFAULT = 0;

    // 附件在本地存储位置的父目录的相对路径
    private String mParentRelativePath;
    // 用逗号隔开的文件列表
    private String mPaths;
    private boolean mAddEnable = true;
    private boolean mCanSelect = false;

    //    private ArrayList<String> mPathList;
    private ArrayList<Document> mDocList;

    private ImageView mIvAddFile = null;
    private RecyclerView mRecyclerView = null;

    private AttachAdapter mAdapter;

    private int mFlag = FLAG_DEFAULT;

    public AttachFileFragment() {
    }

    public static class Builder {
        public int flag;
        public String paths;
        public String relativePath;
        public boolean addEnable = true;
        public boolean canSelected = true;

        public Builder(int flag, String relativePath) {
            this.flag = flag;
            this.relativePath = relativePath;
        }

        public Builder setPathList(String paths) {
            this.paths = paths;
            return this;
        }

        public Builder setAddEnable(boolean addEnable) {
            this.addEnable = addEnable;
            return this;
        }

        public Builder setCanSelected(boolean canSelected) {
            this.canSelected = canSelected;
            return this;
        }
    }

    /**
     * 附件的构造函数
     *
     * @param param1 附件父目录的相对路径
     * @param param2 附件的路径列表
     * @param param3 能否添加附件
     * @param param4 添加的时候是否有不同的方式选择
     * @return AttachFileFragment对象
     */
    public static AttachFileFragment newInstance(String param1, String param2, boolean param3, boolean param4) {
        AttachFileFragment fragment = new AttachFileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RELATIVE_PATH_SEGMENT, param1);
        args.putString(ARG_FILE_PATHS, param2);
        args.putBoolean(ARG_ADD_ENABLE, param3);
        args.putBoolean(ARG_SELECT_ENABLE, param4);
        fragment.setArguments(args);
        return fragment;
    }

    public static AttachFileFragment newInstance(Builder builder) {
        AttachFileFragment fragment = new AttachFileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FLAG, builder.flag);
        args.putString(ARG_RELATIVE_PATH_SEGMENT, builder.relativePath);
        args.putString(ARG_FILE_PATHS, builder.paths);
        args.putBoolean(ARG_ADD_ENABLE, builder.addEnable);
        args.putBoolean(ARG_SELECT_ENABLE, builder.canSelected);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFlag = getArguments().getInt(ARG_FLAG);
            mParentRelativePath = getArguments().getString(ARG_RELATIVE_PATH_SEGMENT);
            mPaths = getArguments().getString(ARG_FILE_PATHS);
            mAddEnable = getArguments().getBoolean(ARG_ADD_ENABLE);
            mCanSelect = getArguments().getBoolean(ARG_SELECT_ENABLE);

            Log.i(TAG, String.format("父目录相对路径：%s\n附件列表：%s", mParentRelativePath, mPaths));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attach_file, container, false);

        findView(view);

        initListener();

        initData();

        return view;
    }

    /**
     * 初始化布局
     */
    private void findView(View view) {
        mIvAddFile = (ImageView) view.findViewById(R.id.ivAddFile);
        if (!mAddEnable) {
            mIvAddFile.setVisibility(View.GONE);
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);               // 水平方向
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(layoutManager);
    }

    /**
     * 设置监听器
     */
    private void initListener() {
        mIvAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickOperate();
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mDocList = new ArrayList<>();

        Document.relativePath = mParentRelativePath;
        mAdapter = new AttachAdapter(getActivity(), mDocList);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemLongClickListener(new AttachAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View v, Document doc) {
                Toast.makeText(getActivity(), "删除" + "=" + doc.getName(), Toast.LENGTH_SHORT).show();
                // 长按删除
                deleteOperate(mDocList.indexOf(doc));
                return true;
            }
        });

        mAdapter.setOnItemClickListener(new AttachAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Document doc) {
                previewItem(mDocList.indexOf(doc));
            }
        });

        pathToDocument();
    }

    /**
     * 点击后预览附件
     *
     * @param position 附件的Document对象
     */
    private void previewItem(int position) {
        Toast.makeText(getActivity(), "预览" + mDocList.get(position).getName(), Toast.LENGTH_SHORT).show();
        // 查看照片

        ArrayList<String> strList = new ArrayList<>();
        for (Document doc : mDocList) {
            strList.add(doc.getPath());
        }

        Intent intent = new Intent(getActivity(), PictureViewActivity.class);

        intent.putStringArrayListExtra("fileList", strList);
        intent.putExtra("canDelete", mAddEnable);
        intent.putExtra("pos", position);

        if (getActivity() instanceof MapGISFrame) {
            getActivity().startActivityForResult(intent, PhotoFragment.REQUEST_CODE_SEE_PIC);
        } else {
            startActivityForResult(intent, PhotoFragment.REQUEST_CODE_SEE_PIC);
        }
    }

    /**
     * 初始化RecyclerView
     */
    private void pathToDocument() {
        // Todo 默认前面添加的都是Media路径
        String parentPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Media);
        List<Document> documents = DocumentFactory.createDocuments(mPaths, parentPath, Document.MimeType.Attach);
        addOperate(documents);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_CODE && resultCode == RootFileActivity.RESULT_CODE) {
                ArrayList<Document> documents = data.getParcelableArrayListExtra("selected_doc");

                Log.i(TAG, "新添加的文件：" + documents.toString());
                mDocList.clear();
                mAdapter.notifyDataSetChanged();

                addOperate(documents);
            } else if (requestCode == PhotoFragment.REQUEST_CODE_SEE_PIC) {
                // 文件有可能被删除
                mDocList.clear();
                mAdapter.notifyDataSetChanged();

                addOperate(DocumentFactory.createDocuments(data.getStringArrayListExtra("fileList")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到添加附件的界面
     */
    private void enterAttachFileActivity() {
        Intent intent = new Intent(getActivity(), RootFileActivity.class);
        intent.putParcelableArrayListExtra("selected_doc", mDocList);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 添加按钮的点击操作
     */
    protected void onClickOperate() {
        switch (this.mFlag) {
            case FLAG_DEFAULT:
                enterAttachFileActivity();
                break;
            default:
                // not defined
                break;
        }
    }

    /**
     * 添加的操作
     */
    /*protected void addOperate(List<String> pathList) {
        if (pathList == null || pathList.size() == 0) return;

        Log.i(TAG,"添加了：" + pathList.toString());
        int lastCount = mPathList.size();
        mPathList.addAll(pathList);
        for (int i = 0; i < pathList.size(); i++) {
            mAdapter.notifyItemInserted(lastCount + i);
        }
    }*/
    protected void addOperate(List<Document> documents) {
        if (documents == null || documents.size() == 0) return;

        Log.i(TAG, "添加了：" + documents.toString());
        int lastCount = mDocList.size();
        mDocList.addAll(documents);
        for (int i = 0; i < documents.size(); i++) {
            mAdapter.notifyItemInserted(lastCount + i);
        }
    }

    /**
     * 删除的操作
     */
    protected void deleteOperate(int position) {
        mDocList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    protected void deleteOperate(List<Document> documents) {
        if (documents == null || documents.size() == 0) return;

        Log.i(TAG, "删除了：" + documents.toString());
        int[] indexs = new int[documents.size()];
        for (int i = 0; i < documents.size(); i++) {
            indexs[i] = mDocList.indexOf(documents.get(i));
        }

        mDocList.removeAll(documents);
        for (int i : indexs) {
            mAdapter.notifyItemRemoved(i);
        }
    }

    /**
     * 当前所有的文件名
     *
     * @return 逗号隔开的文件名称列表
     */
    public String getDatabaseValue() {
        String names = "";
        for (Document document : mDocList) {
            names += "," + document.getDatabaseValue();
        }
        if (!BaseClassUtil.isNullOrEmptyString(names)) {
            // 列表不为空
            names = names.substring(1);
        }
        return names;
    }

    /**
     * 当前所有文件的绝对路径
     *
     * @return 逗号隔开的文件绝对路径列表
     */
    public String getLocalAbsolutePaths() {
        String absolutePaths = "";
        for (Document document : mDocList) {
            absolutePaths += "," + document.getLocalAbsolutePath();
        }
        if (!BaseClassUtil.isNullOrEmptyString(absolutePaths)) {
            // 列表不为空
            absolutePaths = absolutePaths.substring(1);
        }
        return absolutePaths;
    }

    /**
     * 当前所有文件的相对路径
     * parentPath + name;
     *
     * @return 逗号隔开的文件相对路径列表
     */
    public String getServerRelativePaths() {
        String relativePaths = "";
        for (Document document : mDocList) {
            relativePaths += "," + document.getServerRelativePath();
        }
        if (!BaseClassUtil.isNullOrEmptyString(relativePaths)) {
            // 列表不为空
            relativePaths = relativePaths.substring(1);
        }
        return relativePaths;
    }
}
