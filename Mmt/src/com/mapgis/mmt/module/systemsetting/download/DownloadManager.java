package com.mapgis.mmt.module.systemsetting.download;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.ConnectivityUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.navigation.NavigationController;
import com.mapgis.mmt.module.systemsetting.SettingUtil;
import com.mapgis.mmt.global.MmtBaseTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Comclay on 2017/4/19.
 * 下载管理器
 */

public class DownloadManager {
    private static final String TAG = "DownloadManager";

    /*下载队列*/
    private Queue<DownloadInfo> mDownloadQueue = new ConcurrentLinkedQueue<>();
    private List<DownloadInfo> mFileList = new ArrayList<>();
    private final Object mObjLock = new Object();

    private DownloadManager() {
    }

    private final static DownloadManager mInstance = new DownloadManager();

    public static DownloadManager getInstance() {
        return mInstance;
    }

    public List<DownloadInfo> getFileList() {
        return this.mFileList;
    }

    public void reloadData(List<DownloadInfo> list) {
        this.mFileList.clear();
        this.mFileList.addAll(list);
    }

    public static String getUpdateUrl() {
        return ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/GetMobileDownloadInfos?mapRol="
                + getMapRole();
    }

    /**
     * 执行查询地图更新的任务
     */
    public void queryDownMapsTask() {
        String url = getUpdateUrl();

        if (!NetUtil.testServiceExist(url))
            return;

        String result = NetUtil.executeHttpGet(url);

        if (!BaseClassUtil.isNullOrEmptyString(result)) {
            ResultData<DownloadInfo> resultData = new Gson().fromJson(result
                    , new TypeToken<ResultData<DownloadInfo>>() {
                    }.getType());
            if (resultData != null && resultData.ResultCode > 0) {
                ArrayList<DownloadInfo> serverDataList = resultData.DataList;
                mDownloadQueue.clear();
                mFileList.clear();
                for (DownloadInfo info : serverDataList) {
                    if (DownloadUtil.isNeedDownload(info)) {
//                        mDownloadQueue.add(info);
                    }
                }
                Collections.sort(serverDataList);
                mFileList.addAll(serverDataList);
            }
        }
        Log.w(TAG, "文件列表: " + mFileList.toString());
    }

    public DownloadInfo queryMapConfigInfo(String mapName) {
        DownloadInfo info = null;
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/GetMobileDownloadInfos?";
        String result = NetUtil.executeHttpGet(url, "mapRol", getMapRole(), "fileName", mapName);
        if (!BaseClassUtil.isNullOrEmptyString(result)) {
            ResultData<DownloadInfo> resultData = new Gson().fromJson(result
                    , new TypeToken<ResultData<DownloadInfo>>() {
                    }.getType());
            if (resultData != null && resultData.ResultCode > 0) {
                ArrayList<DownloadInfo> serverDataList = resultData.DataList;
                if (serverDataList != null && serverDataList.size() != 0) {
                    info = serverDataList.get(0);
                }
            }
        }
        return info;
    }

    /**
     * 所有需要下载的文件的数量，包括暂停下载的文件，不包括任务队列中已经下载完成的
     */
    public int getDownloadFileCount() {
        if (mFileList == null || mFileList.size() == 0) {
            return 0;
        }

        int count = 0;
        for (DownloadInfo info : mFileList) {
            if (info.isDownload() && !Downloads.isStatusCompleted(info.mStatus)) {
                count++;
            }
        }
        return count;
    }

    public int getNeedDownloadCount() {
        if (mFileList == null || mFileList.size() == 0) {
            return 0;
        }

        int count = 0;
        for (DownloadInfo info : mFileList) {
            if (info.isDownload()) {
                count++;
            }
        }
        return count;
    }

    public static String getMapRole() {
        try {
            UserBean bean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);
            String mapRole = "";

            if (bean != null && !TextUtils.isEmpty(bean.Role) && bean.Role.contains("地图_")) {
                for (String r : bean.Role.split(",")) {
                    if (r.startsWith("地图_")) {
                        mapRole = r;
                        break;
                    }
                }
            }
            return mapRole;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void readyToDownloadAll(Context context) {
        for (DownloadInfo info : mFileList) {
            if (info.isDownload()) {
                readyToDownload(context, info);
            }
        }
    }

    /**
     * 准备下载文件
     */
    public boolean readyToDownload(Context context, @NonNull DownloadInfo info) {
        info.mControl = Downloads.CONTROL_RUN;
        // 1，网络检测
        if (!ConnectivityUtil.isNetworkUsable()) {
            info.mStatus = Downloads.STATUS_WAITING_FOR_NETWORK;
            return false;
        }
        // 2，设备检测
        if (!Environment.isExternalStorageEmulated()) {
            info.mStatus = Downloads.STATUS_DEVICE_NOT_FOUND_ERROR;
            return false;
        }

        // 3，剩余空间检测
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        int blockSize = statFs.getBlockSize();
        int availableBlocks = statFs.getAvailableBlocks();
        // TODO: 2017/4/26 此处暂未考虑对下载队列中其他文件所需的空间大小的影响
        // TODO: 2017/4/27 如果存储空间的大小超过了long类型能表达的数据范围会有可能为负数,所以比较块数
        if (availableBlocks * 0.95 <= (info.mTotalBytes - info.mCurrentBytes) * 1f / blockSize) {
            // 空间不足
            info.mStatus = Downloads.STATUS_INSUFFICIENT_SPACE_ERROR;
            return false;
        }
        Log.w(TAG, "readyToDownload: 准备下载文件=" + info.mServerFileName);
        mDownloadQueue.add(info);
        info.mStatus = Downloads.STATUS_PENDING;      // 等待下载
        startDownloadService(context, info);
        return true;
    }

    public void stopAll() {
        throw new UnsupportedOperationException();
    }

    public void stopDownload(Context context, @NonNull DownloadInfo info) {
        Log.w(TAG, "stopDownload: 暂停下载=" + info.mServerFileName);
        DownloadBroadcastReceiver.sendBroadcast(context
                , info, DownloadBroadcastReceiver.ACTION_RECEIVER_STOP);
    }

    public synchronized void cancelDownload(Context context, @NonNull DownloadInfo info) {
        Log.w(TAG, "cancelDownload: 取消下载=" + info.mServerFileName);

        String action = DownloadBroadcastReceiver.ACTION_RECEIVER_CANCEL;
        if (!DownloadService.isRunning) {
            action = DownloadBroadcastReceiver.ACTION_RECEIVER_UPDATE;
            if (!BaseClassUtil.isNullOrEmptyString(info.mLocaleUri)) {
                File file = new File(info.mLocaleUri);
                if (file.exists()) file.delete();
            }
            info.mStatus = Downloads.STATUS_CANCELED;
            info.mControl = Downloads.CONTROL_RUN;
            info.mPerSecondBytes = 0L;
            info.mCurrentBytes = 0L;
            info.mCurrentUnzipBytes = 0L;
        }

        DownloadBroadcastReceiver.sendBroadcast(context, info, action);
    }

    private void enterDownloadActivity(Context context, boolean isRefresh) {
        Intent intent = new Intent(context, DownloadActivity.class);
        intent.putExtra(DownloadActivity.EXTRA_REFRESH, isRefresh);
        context.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation((Activity) context);
    }

    public void enterDownloadActivity(Context context) {
        enterDownloadActivity(context, false);
    }

    public void startDownloadService(Context context, DownloadInfo info) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_INFO, info);
        context.startService(intent);
    }

    public void checkMobileFileUpdate(final BaseActivity activity, boolean showDialog) {
        new MmtBaseTask<Void, Void, Boolean>(activity, showDialog,"正在检查地图更新，请稍等...") {

            @Override
            protected Boolean doInBackground(Void... params) {
                boolean isExist = NetUtil.testServiceExist(DownloadManager.getUpdateUrl());
                if (isExist) {
                    DownloadManager.getInstance().queryDownMapsTask();
                }
                return isExist;
            }

            @Override
            protected void onSuccess(Boolean aBoolean) {
                if (!aBoolean) {
                    NavigationController.initMapFile(activity);
                    return;
                }
                if (DownloadManager.getInstance().getDownloadFileCount() == 0
                        || DownloadService.isRunning) {
                    if (showLoading){
                        Toast.makeText(activity,"地图文件已是最新",Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                // 有文件更新
                // 1，如果配置了WiFi下自动下载文件，自动更新
                if (SettingUtil.loadAutoDownloadSetting() && ConnectivityUtil.isWiFi()) {
                    DownloadManager.getInstance().readyToDownloadAll(activity);
                    Log.i("autodownload", activity.getString(R.string.text_auto_download_notify));
                    activity.showToast(activity.getString(R.string.text_auto_download_notify));
                    return;
                }
                // 2，进入下载管理界面更新文件
                OkCancelDialogFragment dialogFragment = new OkCancelDialogFragment(
                        activity.getString(R.string.text_has_update_file));
                dialogFragment.setLeftBottonText(activity.getString(R.string.text_next_say));
                dialogFragment.setRightBottonText(activity.getString(R.string.text_download_ontime));
                dialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        DownloadManager.getInstance().enterDownloadActivity(activity);
                    }
                });
                dialogFragment.show(activity.getSupportFragmentManager(), "updateDialog");
            }
        }.executeOnExecutor(MyApplication.executorService);
    }
}
