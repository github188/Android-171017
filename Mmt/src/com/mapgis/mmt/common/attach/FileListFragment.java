package com.mapgis.mmt.common.attach;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.entity.docment.Document;
import com.mapgis.mmt.entity.docment.DocumentFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * 文件列表界面
 */
public class FileListFragment extends Fragment{
    private static final String ARG_PARENT_PATH = "parent_path";

    private String mParentPath;

    private ProgressBar mProgressBar = null;
    private TextView mTvParentPath = null;
    private RecyclerView mRecyclerView = null;
    private FileAdapter mAdapter;

    private TextView mTvNoChildFile = null;
    private TextView mTvSelecSize = null;
    private TextView mTvConfirm = null;
    private LinearLayout mLlChildLayout = null;

    private ArrayList<Document> mDocList;

    private ArrayList<Document> mDocSelects;

    public FileListFragment() {
    }

    public static FileListFragment newInstance(String parentPath) {
        FileListFragment fragment = new FileListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARENT_PATH, parentPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParentPath = getArguments().getString(ARG_PARENT_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        findView(view);

        initData();

        initListener();
        return view;
    }

    private void initListener() {
        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FileListActivity)getActivity()).onCustomBack(true);
            }
        });
    }

    private void findView(View view) {
        mTvParentPath = (TextView) view.findViewById(R.id.tvParentPath);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewFileList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mProgressBar = (ProgressBar) getActivity().findViewById(R.id.baseActionBarProgressBar);

        mLlChildLayout = (LinearLayout) view.findViewById(R.id.llChildFile);
        mTvNoChildFile = (TextView) view.findViewById(R.id.tvNoChildFile);
        mTvSelecSize = (TextView) view.findViewById(R.id.tvSelectSize);
        mTvConfirm = (TextView) view.findViewById(R.id.tvConfirm);
    }

    private void initData() {
        mDocList = new ArrayList<>();
        mAdapter = new FileAdapter(mDocList);
        mRecyclerView.setAdapter(mAdapter);
        mDocSelects = new ArrayList<>();
        ArrayList<Document> documents = getActivity().getIntent().getParcelableArrayListExtra("selected_doc");
        mDocSelects.addAll(documents);

        if (BaseClassUtil.isNullOrEmptyString(mParentPath) || !(new File(mParentPath).exists())) {
            mLlChildLayout.setVisibility(View.INVISIBLE);
            if (getActivity() instanceof FileListActivity)
                ((FileListActivity) getActivity()).showErrorMsg("路径无效");
            return;
        }

        refreshData(mParentPath);
        updateButtonView();
    }

    /**
     * 刷新数据
     */
    private void refreshData(String path) {
        try {
            mProgressBar.setVisibility(View.VISIBLE);
            mLlChildLayout.setVisibility(View.VISIBLE);
            mTvNoChildFile.setVisibility(View.GONE);
            File file = new File(path);
            if (!file.exists()) return;

            mTvParentPath.setText(path);
            mDocList.clear();
            Document document;
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                mLlChildLayout.setVisibility(View.INVISIBLE);
                mTvNoChildFile.setVisibility(View.VISIBLE);
                return;
            }

            for (File tempFile : files) {
                document = DocumentFactory.createDocument(tempFile);

                mDocList.add(document);
                if (tempFile.isDirectory()) {
                    continue;
                }

                if (mDocSelects.contains(document)) {
                    document.setFlag(true);
                } else {
                    document.setFlag(false);
                }
            }
            Collections.sort(mDocList);
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * RecyclerView中点击事件
     *
     * @param checked        是否选中
     * @param position 点击的布局的索引
     */
    private void onItemClickEvent(boolean checked, int position) {
        Document document = mDocList.get(position);
        if (document.isDirectory()) {
            // 进入目录
            refreshData(document.getPath());
        } else {
            document.setFlag(checked);
//            mAdapter.notifyItemChanged(position);

            if (checked) {
                mDocSelects.add(document);
            } else {
                if (!mDocSelects.remove(document)) return;
            }

            updateButtonView();
        }
    }

    /**
     * 更新底部布局
     */
    private void updateButtonView() {
        mTvConfirm.setText(String.format("确认(%d)", mDocSelects.size()));
        long sum = 0L;
        for (Document doc :
                mDocSelects) {
            sum += doc.getSize();
        }
        mTvSelecSize.setText(String.format("已选(%s)", FileUtil.formetFileSize(sum)));
    }

    class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

        private ArrayList<Document> docList;
        private SimpleDateFormat sdf;

        public FileAdapter(ArrayList<Document> docList) {
            this.docList = docList;
            sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        }

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FileViewHolder(View.inflate(getActivity(), R.layout.item_file_list, null));
        }

        @Override
        public void onBindViewHolder(final FileViewHolder holder, final int position) {
            Document doc = docList.get(position);
            holder.tvName.setText(doc.getName());
            doc.setIconToView(getActivity(),holder.ivIcon);

            if (doc.isDirectory()) {
                holder.tvName.setTextSize(16);
                holder.ivEnterChild.setVisibility(View.VISIBLE);
                holder.checkBox.setVisibility(View.GONE);
                holder.tvSize.setVisibility(View.GONE);
                holder.tvLastModified.setVisibility(View.GONE);
            } else {
                holder.tvName.setTextSize(14);
                holder.tvSize.setVisibility(View.VISIBLE);
                holder.tvSize.setText(FileUtil.formetFileSize(doc.getSize()));
                holder.tvLastModified.setVisibility(View.VISIBLE);
                holder.tvLastModified.setText(sdf.format(new Date(doc.getLastModified())));
                holder.ivEnterChild.setVisibility(View.GONE);
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(doc.isFlag());
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v.findViewById(R.id.checkBox);
                    cb.performClick();
                    onItemClickEvent(cb.isChecked(), position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return docList.size();
        }
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivIcon;
        public TextView tvName;
        public TextView tvSize;
        public TextView tvLastModified;
        public CheckBox checkBox;
        public ImageView ivEnterChild;

        public FileViewHolder(View itemView) {
            super(itemView);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_file_icon);
            tvName = (TextView) itemView.findViewById(R.id.tvFileName);
            tvSize = (TextView) itemView.findViewById(R.id.tvFileSize);
            tvLastModified = (TextView) itemView.findViewById(R.id.tvLastModified);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            ivEnterChild = (ImageView) itemView.findViewById(R.id.ivEnterChild);
        }
    }

    /**
     * 返回事件
     */
    public boolean onBack(boolean isBack) {
        String currentParentPath = mTvParentPath.getText().toString().trim();
        if (mParentPath.equals(currentParentPath) || isBack) {

            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("selected_doc", mDocSelects);
            getActivity().setResult(RootFileActivity.RESULT_CODE, intent);

            return true;
        } else {

            refreshData(new File(currentParentPath).getParent());
            return false;
        }
    }
}
