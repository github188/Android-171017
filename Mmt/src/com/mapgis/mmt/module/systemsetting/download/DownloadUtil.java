package com.mapgis.mmt.module.systemsetting.download;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.db.DatabaseHelper;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Comclay on 2017/4/25.
 * 下载工具类
 */

public class DownloadUtil {

    /**
     * 判断是否需要下载
     */
    public static boolean isNeedDownload(DownloadInfo info) {
        File mobileFile = info.getMobileFile();
        boolean exists = mobileFile.exists();
        boolean isNeedDownload;

        // 2，判断是否有记录
        DownloadInfo downloadInfo = getDownloadInfoByFileName(info.mServerFileName);
        if (downloadInfo != null) {
            // 2.1  有记录就根据记录来判断
            if (info.mServerModify <= downloadInfo.mLocaleModify) {
                // 本地时间和服务器时间相同，更新完成
                // 这时候需要判断文件是否存在，如果本地文件已经被删除了，则需要删除数据库中记录并重新下载地图文件
                // 1，判断文件是否存在
                if (!exists) {
                    // TODO: 2017/4/27 本条记录所对应的文件已经被删除，则记录无意义，只有上次更新时间有点用
                    info.mDownloadDate = downloadInfo.mDownloadDate;
                    info.updateDatabase();
                } else {
                    // 记录显示更新完成且本地文件存在，则不需要更新
                    info.copyData(downloadInfo);
                }
            } else if (info.mServerModify > downloadInfo.mLocaleModify) {
                // 服务器时间大于本地时间，需要更新
                if (downloadInfo.mStatus == Downloads.STATUS_RUNNING) {//下载过程意外中断
                    downloadInfo.mControl = Downloads.CONTROL_PAUSED;
                } else if (downloadInfo.mStatus == Downloads.STATUS_UNZIP_RUNNING) {// 解压过程意外中断
                    downloadInfo.mStatus = Downloads.STATUS_DOWNLOAD_SUCCESS;
                }
                if (downloadInfo.mCurrentBytes > 0 && downloadInfo.mDownloadDate > info.mServerModify) {
                    // 下载了一部分文件，则要判断已经下载部分的文件是否存在
                    File file = new File(downloadInfo.mLocaleUri);
                    if (file.exists()) {
                        info.copyData(downloadInfo);
                        info.mControl = Downloads.CONTROL_PAUSED;
                    } else {
//                         不小心删了，重新下载吧
                        info.mLocaleModify = downloadInfo.mLocaleModify;
                        info.mDownloadDate = downloadInfo.mDownloadDate;
                        info.updateDatabase();
                    }
                } else {
                    info.mLocaleModify = downloadInfo.mLocaleModify;
                    info.mDownloadDate = downloadInfo.mDownloadDate;
                    info.updateDatabase();
                }
            }
        } else {
            // 2.1  没有记录根据文件最后修改时间来判断
            if (exists) {
                info.mLocaleModify = mobileFile.lastModified();
            }
            DatabaseHelper.getInstance().insert(info);
        }
        // 更新
        isNeedDownload = info.mLocaleModify < info.mServerModify // 更新
                || info.mLocaleModify == info.mServerModify
                && info.mCurrentBytes < info.mTotalBytes; // 上次未下载完成

        if (isNeedDownload && info.mStatus == 0 &&
                (info.mCurrentBytes == 0 || info.mCurrentBytes == info.mTotalBytes)) {
            // 如果需要更新，直接检测temp目录下是否存在对应的压缩文件，要求压缩文件修改日期大于服务器修改日期
            checkLocalZipFile(info);
        }

        info.setDownload(isNeedDownload);
        return isNeedDownload;
    }


    /**
     * 在需要下载的时候在
     *
     * @param info
     */
    public static void checkLocalZipFile(DownloadInfo info) {
        String path = info.mLocaleUri;
        if (BaseClassUtil.isNullOrEmptyString(path)) {
            path = getLocalTempPath() + info.mServerFileName;
        }
        File zipFile = new File(path);
        if (zipFile.exists() && zipFile.lastModified() >= info.mServerModify
                && zipFile.length() == info.mTotalBytes) {
            // 如果本地zip文件存在，且修改时间大于服务器时间，且大小相同，就直接使用本地文件解压就好了
            info.mCurrentBytes = info.mTotalBytes;
            info.mDownloadDate = zipFile.lastModified();
            info.mLocaleUri = path;
            info.mLocaleFileName = info.mServerFileName;
            info.mStatus = Downloads.STATUS_DOWNLOAD_SUCCESS;
            info.mControl = Downloads.CONTROL_RUN;
            info.updateDatabase();
        }
    }

    public static String getLocalTempPath() {
        return Battle360Util.getFixedPath(Battle360Util.GlobalPath.Temp, true);
    }

    /**
     * 继续下载
     */
    public static boolean isContinueDownload(DownloadInfo info) {
        DownloadInfo localeInfo = getDownloadInfoByFileName(info.mServerFileName);
        if (localeInfo == null) return false;
        if (localeInfo.mLocaleModify == info.mServerModify
                && localeInfo.mCurrentBytes < localeInfo.mTotalBytes) {
            return true;
        }
        return false;
    }

    /**
     * 根据文件名称获取本地存储的下载信息
     */
    public static DownloadInfo getDownloadInfoByFileName(String fileName) {
        ArrayList<DownloadInfo> localeDataList =
                DatabaseHelper.getInstance().query(DownloadInfo.class
                        , String.format(Locale.CHINA, "serverFileName='%s' order by _id desc", fileName));
        if (localeDataList.size() == 0) return null;
        return localeDataList.get(0);
    }

    public static String getMapDbName(String zipFileName) {
        return zipFileName.replace(".zip", ".db");
    }

    /**
     * 判断DownloadInfo文件是否正在下载
     */
    public static boolean isDownloading(DownloadInfo info) {
        if (info.mControl == Downloads.CONTROL_PAUSED) {
            return false;
        }
        switch (info.mStatus) {
            case Downloads.STATUS_RUNNING:
                return true;
        }
        return false;
    }

    public static String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#");
        String fileSizeString;
        if (fileS < 0) {
            fileSizeString = "未知";
        } else if (fileS < 1024) {
            fileSizeString = fileS + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            df.applyPattern("#.00");
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            df.applyPattern("#.00");
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

}
