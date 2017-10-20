package com.mapgis.mmt.engine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileUtil;

import java.io.File;
import java.util.Locale;

/**
 * Created by Comclay on 2016/12/9.
 * 内存管理
 */
public class MemoryManager {
    private final int DELETE_FILE_BEFOR = 0;
    private final int DELETE_FILE_FAILED = 1;
    private final int DELETE_FILE_SUCCESS = 2;
    private final int DELETE_FILE_COMPLETE = 3;

    public Context mContext;
    private AlertDialog mDialog;

    private TextView mTvCurrentDelete = null;
    private TextView mTvListDelete = null;
    private TextView mTvResultDelete = null;
    private ProgressBar mProgressBar = null;
    private ScrollView mSvListDelete = null;
    private Button mBtnNegative = null;
    private Button mBtnPositive = null;

    // 内存大小
    private long mMemorySize;
    // 文件数量
    private int mCount;
    private int mBeforeAppendHeight = 0;
    private int mAfterAppendHeight = 0;

    // 取消清理内存
    private boolean isCancelDelete = false;
    // 后台清理内存
    private boolean isBackgroundDelete = false;

    private MemoryManager(Context context) {
        this.mContext = context;
    }

    public static MemoryManager newInstance(Context context) {
        return new MemoryManager(context);
    }

    private AlertDialog buildDialog(String title, String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setIcon(R.drawable.regular_clear);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确认", listener);

        return builder.create();
    }

    /**
     * 内存不足的提醒对话框
     */
    public void showAlertDialog() {
        showConfirmDialog("警告：内存不足，建议清理缓存文件！");
    }

    /**
     * 文件清理确认对话框
     *
     * @param message 提醒的内容
     */
    public void showConfirmDialog(String message) {
        mDialog = buildDialog(mContext.getResources().getString(R.string.memory_manager)
                , message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showClearProgressDialog();
                    }
                });

        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    /**
     * 文件清理的进度对话框
     */
    private void showClearProgressDialog() {
        // 清理进度的对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setIcon(R.drawable.regular_clear);
        builder.setTitle("文件清理");
        View view = View.inflate(mContext, R.layout.dialog_clear_progress, null);

        mTvCurrentDelete = (TextView) view.findViewById(R.id.tv_current_delete);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mTvListDelete = (TextView) view.findViewById(R.id.tv_list_delete);
        mTvResultDelete = (TextView) view.findViewById(R.id.tv_delete_result);
        mSvListDelete = (ScrollView) view.findViewById(R.id.sv_list_delete);
        mBtnNegative = (Button) view.findViewById(R.id.btn_negative);
        mBtnPositive = (Button) view.findViewById(R.id.btn_positive);
        mBtnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消清理
                onCancelClear();
            }
        });

        mBtnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 转为后台清理
                onBackgroundClear();
            }
        });

        builder.setView(view);

        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();

        clearMemory();
    }

    /**
     * 转为后台清理
     */
    private void onBackgroundClear() {
        isBackgroundDelete = true;
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    /**
     * 取消清理
     */
    private void onCancelClear() {
        isCancelDelete = true;
        dismissDialog();
    }

    /**
     * 清理内存
     */
    private void clearMemory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteMediaCache(new DefaultOnDeleteFileListener());
            }
        }).start();
    }

    public void deleteMediaCache(OnDeleteFileListener listener) {
        try {
            // 1，得到要清理的文件的路径
            // 媒体文件
            String mediaPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Media);
            // 录音文件
            String recordPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Record);

            // 2，逐一遍历子文件，如果是文件则立马删除，如果是文件夹则继续遍历其子文件
            deleteFile(new File(recordPath)
                    , Battle360Util.newMapGISName + File.separator + Battle360Util.GlobalPath.Record
                    , listener);
            deleteFile(new File(mediaPath)
                    , Battle360Util.newMapGISName + File.separator + Battle360Util.GlobalPath.Media
                    , listener);

            Log.i("delete", String.format("总共删除了%d个文件，为您节约了%d的空间大小", mCount, mMemorySize));

            if (isBackgroundDelete) {
                showToast(String.format("总共删除了%d个文件，为您节约了%s的空间大小", mCount, FileUtil.formetFileSize(mMemorySize)));
                return;
            }

            mHandler.sendEmptyMessage(DELETE_FILE_COMPLETE);
        } catch (Exception e) {
            dismissDialog();
            showToast("清理失败");
            e.printStackTrace();
        }
    }

    private void showToast(final String msg) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppManager.currentActivity()
                        , msg
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBeforeAppendHeight = mTvListDelete.getHeight();
            switch (msg.what) {
                case DELETE_FILE_BEFOR:
                    if (!isBackgroundDelete) {
                        mTvCurrentDelete.setText(String.format("正在清理：%s", msg.getData().getString("fileName")));
                    }
                    break;
                case DELETE_FILE_FAILED:
                    if (!isBackgroundDelete) {
                        mTvListDelete.append(Html.fromHtml(String.format("<font color='red'>%s</font>", msg.getData().getString("fileName"))));
                        mTvListDelete.append("\n");
                        mAfterAppendHeight = mTvListDelete.getHeight();
                    }
                    break;
                case DELETE_FILE_SUCCESS:
                    if (!isBackgroundDelete) {
                        mTvListDelete.append(msg.getData().getString("fileName") + "\n");
                        String s = "";
                        if (mMemorySize == 0) {
                            s = String.format(Locale.CHINA, "总共清理了%d个空文件夹!"
                                    , mCount);
                        } else {
                            s = String.format(Locale.CHINA, "总共清理了%d个文件，为您节约了%s的空间!"
                                    , mCount
                                    , FileUtil.formetFileSize(mMemorySize));
                        }
                        mTvResultDelete.setText(s);
                        mAfterAppendHeight = mTvListDelete.getHeight();
                    }
                    break;
                case DELETE_FILE_COMPLETE:
                    if (mDialog == null) return;

                    mTvCurrentDelete.setText("清理完成");

                    mBtnPositive.setText("完成");
                    mBtnPositive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismissDialog();
                        }
                    });
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setMax(100);
                    mProgressBar.setProgress(100);
                    return;
                default:
            }
            //滑动ScrollView
            mSvListDelete.scrollBy(0, mAfterAppendHeight - mBeforeAppendHeight);
        }
    };

    /**
     * 删除文件
     *
     * @param file 遍历的目录
     */
    private void deleteFile(File file, String relativeName, OnDeleteFileListener listener) throws Exception {
        if (isCancelDelete || !FileUtil.isDelete(file)) {
            return;
        }

        // 1,路径不存在
        if (file == null || !file.exists()) {
            return;
        }
        // 1,路径不是一个目录
        if (!file.isDirectory()) {  // 如果是一个目录必须是空目录
            long tempSize = file.length();   // 如果路径不存在返回0
            if (file.delete()) {  // 删除成功
                mCount++;
                mMemorySize += tempSize;
                Log.i("FileDelete", "成功删除了：" + relativeName + "     " + file.lastModified());
                if (listener != null) listener.onSuccessedDelete(file, relativeName);
            } else {
                Log.e("FileDelete", "删除失败：" + relativeName + "     " + file.lastModified());
                if (listener != null) listener.onFailedDelete(file, relativeName);
            }
            return;
        }

        // 只将文件夹显示到标题上
        if (listener != null) listener.onDeleteBefore(file, relativeName);
        // 2,遍历路径下所有的文件
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            // 递归循环删除
            if (listener != null)
                deleteFile(childFile, relativeName + File.separator + childFile.getName(), listener);
        }

        // 3，删除了文件中的所有子文件，最后再删除该目录
        if (file.delete()) {
            mCount++;
            if (listener != null) listener.onSuccessedDelete(file, relativeName);
        } else {
            Log.e("FileDelete", "删除失败：" + relativeName + "     " + file.lastModified());
//            if (listener != null) listener.onFailedDelete(file, relativeName);
        }
    }

    private class DefaultOnDeleteFileListener implements OnDeleteFileListener {
        @Override
        public void onDeleteBefore(File file, String relativePath) {
            flushView(relativePath, DELETE_FILE_BEFOR);
        }

        @Override
        public void onFailedDelete(File file, String relativePath) {
            flushView(relativePath, DELETE_FILE_FAILED);
        }

        @Override
        public void onSuccessedDelete(File file, String relativePath) {
            flushView(relativePath, DELETE_FILE_SUCCESS);
        }
    }

    /**
     * 刷新界面
     */
    private void flushView(String path, int what) {
        Message msg = new Message();
        msg.what = what;
        Bundle bundle = new Bundle();
        bundle.putString("fileName", path);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * 文件刪除的监听器
     */
    interface OnDeleteFileListener {
        /**
         * 删除之前
         */
        void onDeleteBefore(File file, String relativePath);

        /**
         * 删除失败
         */
        void onFailedDelete(File file, String relativePath);

        /**
         * 删除成功
         */
        void onSuccessedDelete(File file, String relativePath);
    }

    /**
     * 销毁对话框
     */
    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
