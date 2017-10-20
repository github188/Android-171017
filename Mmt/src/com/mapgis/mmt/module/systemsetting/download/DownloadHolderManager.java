package com.mapgis.mmt.module.systemsetting.download;

import com.mapgis.mmt.module.systemsetting.download.DownloadAdapter.DownloadViewHolder;

/**
 * Created by Comclay on 2017/4/28.
 * DownloadViewHolder的管理器
 */

public class DownloadHolderManager {
    private DownloadHolderManager() {
    }

    public DownloadViewHolder mDownloadHolder;
    private static DownloadHolderManager mInstance = new DownloadHolderManager();

    public static DownloadHolderManager getInstance() {
        return mInstance;
    }

    public void setDownloadHolder(DownloadViewHolder holder) {
        this.mDownloadHolder = holder;
    }

    public void clearDownloadHolder() {
        this.mDownloadHolder = null;
    }

    public void closeCurrentHolder(DownloadViewHolder holder) {
        if (mDownloadHolder != null && !this.mDownloadHolder.equals(holder)) {
            this.mDownloadHolder.tvOpt.performClick();
        }
    }
}
