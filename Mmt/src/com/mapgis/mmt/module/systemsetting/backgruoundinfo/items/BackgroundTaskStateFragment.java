package com.mapgis.mmt.module.systemsetting.backgruoundinfo.items;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class BackgroundTaskStateFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String FLAG_FAILED = "failed";
    private final String FLAG_SUCCESS = "success";

    private RecyclerView mRecyclerView;
    private CursorRecyclerAdapter<TaskViewHolder> adapter;

    private Button btnBatchReport;

    private LinearLayout emptyView;

    private ImageLoader imageLoader;

    public static Fragment getInstance() {
        BackgroundTaskStateFragment fragment = new BackgroundTaskStateFragment();
        return fragment;
    }

    public BackgroundTaskStateFragment() {
        this.imageLoader = ImageLoader.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bg_task_state, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        this.btnBatchReport = (Button) view.findViewById(R.id.btnReportBundleTask);
        btnBatchReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batchReport();
            }
        });

        int customBtnStyleResource = AppStyle.getCustromBtnStyleResource();
        if (customBtnStyleResource > 0) {
            btnBatchReport.setBackgroundResource(customBtnStyleResource);
        }
        emptyView= (LinearLayout) view.findViewById(R.id.emptyView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String uri = ServerConnectConfig.getInstance().getBaseServerPath();
        TaskCursorLoader loader = new TaskCursorLoader(getContext(),
                new SQLiteQueryParameters("status=" + ReportInBackEntity.REPORTING + " and uri like '" + uri + "%'"));

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (adapter == null) {
            this.adapter = new BackgroundTaskAdapter(data, getActivity());
            mRecyclerView.setAdapter(adapter);
        } else {
            adapter.swapCursor(data);
        }

        btnBatchReport.setVisibility(data.getCount() > 0 ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(data.getCount()>0?View.GONE:View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private class BackgroundTaskAdapter extends CursorRecyclerAdapter<TaskViewHolder> {

        private LayoutInflater mLayoutInflater;

        public BackgroundTaskAdapter(Cursor cursor, Context context) {
            super(cursor);
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public void onBindViewHolderCursor(TaskViewHolder holder, Cursor cursor) {
            ReportInBackEntity entity = new ReportInBackEntity();
            entity.buildFromCursor(cursor);

            holder.bindData(entity);
        }

        @Override
        public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = mLayoutInflater.inflate(R.layout.view_bg_task_listitem, parent, false);
            return new TaskViewHolder(view);
        }
    }

    private class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView imgDesc;

        private TextView tvTypeInfo;
        private TextView tvCreateTime;
        private TextView tvRetryTimes;

        private ImageButton iBtnDelete;
        private ImageButton iBtnRetry;

        private ReportInBackEntity entity;

        public TaskViewHolder(View itemView) {
            super(itemView);

            this.imgDesc = (ImageView) itemView.findViewById(R.id.iv_img);

            this.tvTypeInfo = (TextView) itemView.findViewById(R.id.tv_type_info);
            this.tvCreateTime = (TextView) itemView.findViewById(R.id.tv_create_time);
            this.tvRetryTimes = (TextView) itemView.findViewById(R.id.tv_retry_times);

            this.iBtnDelete = (ImageButton) itemView.findViewById(R.id.btn_delete);
            this.iBtnRetry = (ImageButton) itemView.findViewById(R.id.btn_retry);
        }

        public void bindData(ReportInBackEntity entity) {

            this.entity = entity;

            String filePaths = entity.getFilePaths();
            String uri;
            if (TextUtils.isEmpty(filePaths)) {
                uri = "drawable://" + R.drawable.ic_default_photo;
            } else {
                uri = "file://" + (filePaths.contains(",") ?
                        filePaths.substring(0, filePaths.indexOf(",")) : filePaths);
            }
            imageLoader.displayImage(uri, new ImageViewAware(imgDesc));

            tvTypeInfo.setText(entity.getType());
            tvCreateTime.setText(entity.getCreateTime());
            tvRetryTimes.setText(String.valueOf(entity.getRetryTimes()));

            iBtnDelete.setOnClickListener(this);
            iBtnRetry.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(final View view) {

            int viewId = view.getId();
            if (viewId == R.id.btn_delete) {
                new AlertDialog.Builder(getContext())
                        .setTitle(Html.fromHtml("<font color='#FF0000'>警告</font>"))
                        .setMessage("确认删除该后台任务？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (entity != null) {
                                    deleteTask(entity);
                                }
                            }
                        }).show();

            } else if (viewId == R.id.btn_retry) {
                new AlertDialog.Builder(getContext())
                        .setTitle("提示")
                        .setMessage("确认重试上报该任务？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (entity != null) {
                                    retryReportTask(entity);
                                }
                            }
                        }).show();
            }
        }

        @Override
        public boolean onLongClick(View view) {

            new AlertDialog.Builder(getContext())
                    .setTitle(Html.fromHtml("信息"))
                    .setMessage(entity.getData())
                    .setPositiveButton("关闭", null)
                    .show();

            return true;
        }
    }

    private void batchReport() {

        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(getContext()) {

            @Override
            protected String doInBackground(String... params) {

                String uri = ServerConnectConfig.getInstance().getBaseServerPath();
                int userID = MyApplication.getInstance().getUserId();

                try {
                    List<ReportInBackEntity> data = DatabaseHelper.getInstance().query(ReportInBackEntity.class,
                            new SQLiteQueryParameters("status=" + ReportInBackEntity.REPORTING + " and uri like '" + uri + "%'"));

                    if (data.size() == 0) {
                        MyApplication.getInstance().showMessageWithHandle("暂无后台任务");
                        return FLAG_SUCCESS;
                    }

                    String baseDir = Battle360Util.getFixedPath("temp") + "失败后台任务/";
                    String descFileName = userID + "@" + BaseClassUtil.getSystemTimeForFile();

                    File tempFile = new File(baseDir);
                    if (!tempFile.exists()) {
                        tempFile.mkdirs();
                    }

                    String localDescFilePath = baseDir + descFileName + ".txt"; // 失败后台任务/36@yyMMdd_HHmmss.txt

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(localDescFilePath);
                        byte[] buffer;
                        for (ReportInBackEntity entity : data) {
                            buffer = new Gson().toJson(entity).replace(".amr", ".wav").getBytes();
                            fos.write(buffer);
                        }
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Upload filePath & attachments.
                    for (ReportInBackEntity entity : data) {

                        if (!BaseClassUtil.isNullOrEmptyString(entity.getFilePaths())) {
                            // 若文件数据存在，则判断是否是多个文件
                            List<String> absolutePaths = BaseClassUtil.StringToList(entity.getFilePaths(), ",");
                            List<String> fileNames = BaseClassUtil.StringToList(entity.getReportFileName(), ",");

                            for (int i = 0; i < absolutePaths.size(); i++) {
                                if (new File(absolutePaths.get(i)).isDirectory()) {
                                    continue;
                                }

                                int resultFlag = entity.uploadFiles(absolutePaths.get(i), "失败后台任务/"+ descFileName + "/" + fileNames.get(i));
                                if (resultFlag < 0) {
                                    MyApplication.getInstance().showMessageWithHandle("上传失败，请稍后重试");
                                    return FLAG_FAILED;
                                }
                            }
                        }
                    }

                    int result = data.get(0).uploadFiles(localDescFilePath, "失败后台任务/" + descFileName + ".txt");

                    if (result == -1) {
                        return FLAG_FAILED;
                    } else {
                        DatabaseHelper.getInstance().delete(ReportInBackEntity.class, null);
                        MyApplication.getInstance().showMessageWithHandle("上传成功");
                        return FLAG_SUCCESS;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return FLAG_FAILED;
                }
            }

            @Override
            protected void onSuccess(String s) {

                if (FLAG_SUCCESS.equals(s)) {
                    reloadData();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    /**
     * 删除后台任务
     */
    private void deleteTask(final ReportInBackEntity entity) {

        // TODO: 11/16/16 界面数据刷新问题，界面上的数据可能已经被后台上传后删除，再次处检查数据是否存在，还是列表监听数据变化刷新，权衡

        new MmtBaseTask<String, Void, Long>(getContext()) {
            @Override
            protected Long doInBackground(String... params) {

                return entity.delete();
            }

            @Override
            protected void onSuccess(Long affectedRows) {

                // affectedRows = -1: 删除记录失败
                // affectedRows =  0: 数据库中不存在该记录，已被后台任务上传后删除

                if (affectedRows == -1) {
                    Toast.makeText(getContext(), "删除任务失败", Toast.LENGTH_LONG).show();
                } else {

                    if (affectedRows == 0) {
                        Toast.makeText(getContext(), "记录已在后台任务中上传", Toast.LENGTH_SHORT).show();
                    } else {

                        // 被删除的任务需要将任务以.txt文件的形式存入错误日志
                        FileUtil.dropRubbish(entity.getType(), entity);
                        Toast.makeText(getContext(), "删除任务成功", Toast.LENGTH_SHORT).show();
                    }

                    reloadData();
                }
            }
        }.mmtExecute();
    }

    /**
     * 重试上报后台任务
     */
    private void retryReportTask(final ReportInBackEntity entity) {

        // TODO: 11/16/16 界面数据刷新问题，界面上的数据可能已经被后台上传后删除，再次处检查数据是否存在，还是列表监听数据变化刷新，权衡
        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(getContext()) {
            @Override
            protected String doInBackground(String... params) {

                if (!NetUtil.testNetState()) {
                    MyApplication.getInstance().showMessageWithHandle("网络故障，请检查网络链接");
                    return FLAG_FAILED;
                }

//                long retryInterval = MyApplication.getInstance().getConfigValue("ReportInBackInterval", 5);
//                while (!ReportInBackThread.getInstance().isInterrupted()) {
//                    try {
//                        TimeUnit.SECONDS.sleep(retryInterval >> 1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }

                List<ReportInBackEntity> data = DatabaseHelper.getInstance().query(ReportInBackEntity.class,
                        new SQLiteQueryParameters("_id=" + entity.getId()));

                // 检查数据库发现记录已被后台现场上传，需要重新加载数据刷新列表
                if (data.size() == 0) {
                    MyApplication.getInstance().showMessageWithHandle("[" + entity.getType() + "] 已上传");
                    return FLAG_SUCCESS;
                }

                ResultData<Integer> result = data.get(0).report();

                if (result.ResultCode > 0) {

                    MyApplication.getInstance().showMessageWithHandle("[" + entity.getType() + "] 上传成功");
                    // 上报成功后从数据库中删除该条记录
                    entity.delete();

                } else {

                    // 失败则更新数据库中记录的重试次数
                    entity.setRetryTimes(entity.getRetryTimes() + 1);
                    entity.update(new String[]{"retryTimes"});

                    MyApplication.getInstance().showMessageWithHandle("[" + entity.getType() + "] " +
                            (TextUtils.isEmpty(result.ResultMessage) ? "服务器处理失败" : result.ResultMessage));
                }
                return FLAG_SUCCESS;
            }

            @Override
            protected void onSuccess(String s) {
                if (FLAG_SUCCESS.equals(s)) {
                    reloadData();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void reloadData() {
        getLoaderManager().getLoader(0).forceLoad();
    }

}
