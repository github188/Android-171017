package com.mapgis.mmt.module.systemsetting.download;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.ConnectivityUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.List;
import java.util.Locale;

/**
 * Created by Comclay on 2017/4/17.
 * 下载的适配器
 */

class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {
    private static final String TAG = "DownloadAdapter";
    private final static int ANIMATION_DURATION = 512;
    private final String DOWNLOAD_NOT_WIFI = "download_not_wifi";
    private Context mContext;
    private LayoutInflater mInflater;
    private List<DownloadInfo> mDownloadList;

    DownloadAdapter(Context context, List<DownloadInfo> downloadList) {
        mContext = context;
        mInflater = ((Activity) context).getLayoutInflater();
        mDownloadList = downloadList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public DownloadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_download_view, parent, false);
        return new DownloadViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final DownloadViewHolder holder, final int position) {
        final DownloadInfo downloadInfo = mDownloadList.get(position);
        holder.initData(downloadInfo);

        holder.tvOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!DownloadUtil.isDownloading(downloadInfo)) {
                    if (isAllowNotWiFi() || ConnectivityUtil.isWiFi()) {
                        downloadInfo.mControl = Downloads.CONTROL_RUN;
                        if (downloadInfo.mStatus == Downloads.STATUS_PENDING) {
                            downloadInfo.mStatus = Downloads.STATUS_PAUSED;
                            holder.tvOpt.setText(downloadInfo.getNextOptText());
                        } else {
                            boolean isReady = DownloadManager.getInstance().readyToDownload(mContext, downloadInfo);
                            if (isReady) {
                                holder.tvOpt.setText("暂停");
                                Toast.makeText(mContext, "准备下载" + downloadInfo.getPrefix(), Toast.LENGTH_SHORT).show();
                            } else {
                                holder.tvOpt.setText("重试");
                                Toast.makeText(mContext, Downloads.statusToString(downloadInfo.mStatus), Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "错误: " + Downloads.statusToString(downloadInfo.mStatus));
                            }
                        }
                    } else {
                        confirmOnNotWiFi(holder);
                    }
                } else {
                    downloadInfo.mControl = Downloads.CONTROL_PAUSED;

                    DownloadManager.getInstance().stopDownload(mContext, downloadInfo);
                    holder.tvOpt.setText("继续");
                    Toast.makeText(mContext, "暂停下载" + downloadInfo.getPrefix(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDownloadList == null ? 0 : mDownloadList.size();
    }

    public class DownloadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvName;
        public TextView tvOpt;
        public TextView tvFileSize;
        public TextView tvStatus;
        public ImageView ivMoreOperate;
        public ProgressBar progressBar;
        public TextView tvSpeed;
        public TextView tvRatio;
        public ViewGroup llMoreOperate;
        public TextView tvCheckUpdate;
        public TextView tvDownloadController;
        public TextView tvDelete;

        private DownloadInfo downloadInfo;

        DownloadViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvOpt = (TextView) itemView.findViewById(R.id.tv_opt);
            tvFileSize = (TextView) itemView.findViewById(R.id.tv_fileSize);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
            ivMoreOperate = (ImageView) itemView.findViewById(R.id.iv_more_operate);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            tvSpeed = (TextView) itemView.findViewById(R.id.tv_speed);
            tvRatio = (TextView) itemView.findViewById(R.id.tv_ratio);
            llMoreOperate = (ViewGroup) itemView.findViewById(R.id.ll_operate);
            tvCheckUpdate = (TextView) itemView.findViewById(R.id.tv_checkUpdate);
            tvDownloadController = (TextView) itemView.findViewById(R.id.tv_download_controller);
            tvDelete = (TextView) itemView.findViewById(R.id.tv_delete);
        }

        public void initData(DownloadInfo info) {
            downloadInfo = info;
            String name = String.format(Locale.CHINA, "%s(%s)"
                    , info.getPrefix(), DownloadType.typeToDescription(info.mMimeType));
            tvName.setText(name);
            tvFileSize.setText(info.getFormatSize());
            itemView.setOnClickListener(this);
            ivMoreOperate.setOnClickListener(this);
            tvDelete.setOnClickListener(this);
            tvCheckUpdate.setOnClickListener(this);

            if (info.mControl == Downloads.CONTROL_RUN && info.mStatus == Downloads.STATUS_PAUSED) {
                tvStatus.setVisibility(View.GONE);
            } else {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText(info.getStatusText());
            }

            if (info.isDownload()) {
                // 下载,更新,继续,暂停
                tvOpt.setText(info.getNextOptText());
                if ("完成".equals(info.getNextOptText())/* || Downloads.isStatusCompleted(info.mStatus) */|| Downloads.isStatusUnzip(info.mStatus)) {
                    // 解压过成功中不允许操作
                    tvOpt.setEnabled(false);
                } else {
                    tvOpt.setEnabled(true);
                }

                if (info.mCurrentBytes == 0L && info.mStatus != Downloads.STATUS_RUNNING
                        || Downloads.isUnzipSuccessed(info.mStatus)) {
                    tvSpeed.setText(info.getFormatDownloadDate());
                    tvRatio.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    tvSpeed.setText(info.getDownloadSpeed());
                    tvRatio.setText(info.getDownloadRatioPercent());
                    progressBar.setProgress(info.getDownloadRatioSize());
                    tvSpeed.setVisibility(View.VISIBLE);
                    tvRatio.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                }

                // 解压完成后才能
                if (Downloads.isStatusSuccess(info.mStatus)) {
                    tvCheckUpdate.setEnabled(true);
                    tvDelete.setEnabled(false);
                } else {
                    tvCheckUpdate.setEnabled(false);
                    tvDelete.setEnabled(true);
                }
            } else {
                tvOpt.setEnabled(false);
                tvOpt.setText("最新");

                tvStatus.setVisibility(View.GONE);

                tvSpeed.setText(info.getFormatDownloadDate()); // 已是最新就只显示时间
                tvRatio.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

                tvCheckUpdate.setEnabled(true);
                tvDelete.setEnabled(false);
            }
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == ivMoreOperate.getId()) {
                clickWithAnimation();
            } else if (id == tvDelete.getId()) {
                cancelDownload();
            } else if (id == tvCheckUpdate.getId()) {
                checkUpdate();
            } else if (id == itemView.getId()) {
                onItemClicked();
            }
        }

        /*点击条目*/
        private void onItemClicked() {
            ivMoreOperate.performClick();
        }

        /*检查更新*/
        private void checkUpdate() {
            new MmtBaseTask<String, Void, String>(mContext, true, mContext.getString(R.string.text_update_checking)) {
                @Override
                protected String doInBackground(String... params) {
                    if (params[0] == null) {
                        return null;
                    }
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/GetMapInfo?";
                    return NetUtil.executeHttpGet(url, "mapRol"
                            , DownloadManager.getMapRole(), "fileName", params[0]);
                }

                @Override
                protected void onSuccess(String result) {
                    if (BaseClassUtil.isNullOrEmptyString(result)) {
                        Toast.makeText(mContext, R.string.text_net_error, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ResultData<DownloadInfo> resultData = new Gson().fromJson(result
                            , new TypeToken<ResultData<DownloadInfo>>() {
                            }.getType());

                    if (ResultWithoutData.isEmptyData(resultData)) {
                        Toast.makeText(mContext, ResultWithoutData.getErrMsg(resultData
                                , mContext.getString(R.string.text_unknow_error)), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DownloadInfo tempInfo = resultData.getSingleData();
                    if (!DownloadUtil.isNeedDownload(tempInfo)) {
                        Toast.makeText(mContext, R.string.text_lastest_version, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, R.string.text_need_update, Toast.LENGTH_SHORT).show();
                        int oldIndex = mDownloadList.indexOf(downloadInfo);
                        int needCount = DownloadManager.getInstance().getNeedDownloadCount();
                        downloadInfo.copyData(tempInfo);
                        if (oldIndex <= needCount) {
                            initData(downloadInfo);
                        } else {
                            int newIndex = needCount;
                            // 移除老位置
                            mDownloadList.remove(oldIndex);
                            // 添加新位置
                            mDownloadList.add(newIndex, downloadInfo);
                            notifyItemMoved(oldIndex, newIndex);
                            initData(mDownloadList.get(oldIndex));
                        }
                    }
                }
            }.execute(downloadInfo.getPrefix());
        }

        /*取消下载*/
        private void cancelDownload() {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.text_notify);
            builder.setMessage(R.string.text_download_cancel);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DownloadManager.getInstance().cancelDownload(mContext, downloadInfo);
                    ivMoreOperate.performClick();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }

        /*点击展开图标*/
        private void clickWithAnimation() {
            if (llMoreOperate.getVisibility() == View.VISIBLE) {
                // 执行隐藏动画
                ViewPropertyAnimator.animate(ivMoreOperate).rotation(0)
                        .setDuration(ANIMATION_DURATION).start();
                llMoreOperate.setVisibility(View.GONE);
            } else {
                // 执行显示动画
                ViewPropertyAnimator.animate(ivMoreOperate).rotation(90)
                        .setDuration(ANIMATION_DURATION).start();
                llMoreOperate.setVisibility(View.VISIBLE);
            }
        }
    }

    private void confirmOnNotWiFi(final DownloadViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.text_notify);
        builder.setMessage(R.string.text_notwifi_notify);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyApplication.getInstance().putConfigValue(DOWNLOAD_NOT_WIFI, 1);
                holder.tvOpt.performClick();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private boolean isAllowNotWiFi() {
        return MyApplication.getInstance().getConfigValue(DOWNLOAD_NOT_WIFI, -1) > 0;
    }
}
