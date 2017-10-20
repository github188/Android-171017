package com.mapgis.mmt.module.systemsetting.download;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.ConnectivityUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 文件实体对象
 */
public class DownloadInfo implements ISQLiteOper, Comparable<DownloadInfo>, Parcelable {

    public static final String EXTRA_IS_WIFI_REQUIRED = "isWifiRequired";

    public int mId;
    @SerializedName("Title")
    public String mTitle;
    @SerializedName("Description")
    public String mDescription;
    // 文件类型，DownloadType
    @SerializedName("Type")
    public String mMimeType;
    // 文件名
    public String mLocaleFileName;
    public String mLocaleUri;
    public long mLocaleModify;

    @SerializedName("FileName")
    public String mServerFileName;
    @SerializedName("Uri")
    public String mServerUri;
    @SerializedName("LastMod")
    public long mServerModify;
    // 控制状态，下载，暂停。。
    public int mControl;
    // 状态
    public int mStatus;
    // 失败次数
    public int mNumFailed;
    // 上次修改时间
    public long mDownloadDate;
    @SerializedName("TotalBytes")
    public long mTotalBytes;
    public long mCurrentBytes;
    public String mErrMsg;

    // 多久后自动重试
    public int mRetryAfter;
    // 当前网络类型是否允许下载
    public int mAllowedNetworkTypes;
    // 当前已经解压的大小
    public long mCurrentUnzipBytes;
    // 需要解压的文件的总大小
    public long mTotalUnzipBytes;
    // 每秒中处理数据的大小
    public long mPerSecondBytes;

    private boolean isDownload = false;

    public DownloadInfo() {
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    /**
     * 格式化的大小
     */
    public String getFormatSize() {
        return DownloadUtil.formetFileSize(this.mTotalBytes);
    }

    public String getFormatDownloadSize() {
        if (mStatus > 200 && mStatus < 300) {
            return DownloadUtil.formetFileSize(this.mCurrentUnzipBytes);
        }
        return DownloadUtil.formetFileSize(this.mCurrentBytes);
    }

    public String getDownloadSpeed() {
        String formatDownloadSize = getFormatDownloadSize();
        if (mPerSecondBytes == 0L) {
            return formatDownloadSize;
        }
        String perSecondSize = getFormatPerSecondSize();
        String formatResidueTime = getFormatResudueTime();
//        Log.w("download", "getDownloadSpeed: " + String.format(Locale.CHINA, "%s-%s  剩余时间：%s"
//                , formatDownloadSize, perSecondSize, formatResidueTime));
        return String.format(Locale.CHINA, "%s-%s  剩余时间：%s"
                , formatDownloadSize, perSecondSize, formatResidueTime);

    }

    public String getFormatPerSecondSize() {
        return DownloadUtil.formetFileSize(this.mPerSecondBytes * 1000) + "/S";
    }

    /**
     * 计算剩余时间
     */
    public String getFormatResudueTime() {
        if (this.mPerSecondBytes == 0) {
            return "";
        }
        long seconds;
        if (mStatus > 200 && mStatus < 300) {
            // 解压过程
            seconds = (this.mTotalUnzipBytes - this.mCurrentUnzipBytes) / this.mPerSecondBytes; // 单位ms
        } else {
            seconds = (this.mTotalBytes - this.mCurrentBytes) / this.mPerSecondBytes; // 单位ms
        }

        if (seconds == 0L) {
            return "";
        }
        long days = seconds / (86400000);// 24*60*60*1000
        long hours = seconds / (3600000);//60*60*1000
        long mins = seconds / (60000);//60*1000
        long ss = seconds % (60000) / 1000;
        DecimalFormat format = new DecimalFormat("00");
        StringBuilder sb = new StringBuilder();
        if (days != 0L) {
            format.applyPattern("00 ");
            sb.append(format.format(days));
        }
        format.applyPattern("00:");
        sb.append(format.format(hours));
        sb.append(format.format(mins));
        format.applyPattern("00");
        sb.append(format.format(ss));

        return sb.toString();
    }

    /**
     * 下载的比率:百分比
     */
    public String getDownloadRatioPercent() {
        double ratio = 0;

        if (mTotalBytes != 0) {
            if (mStatus > 200 && mStatus < 300) ratio = mCurrentUnzipBytes * 1f / mTotalUnzipBytes;
            else ratio = mCurrentBytes * 1f / mTotalBytes;
        }
        DecimalFormat format = new DecimalFormat("#%");
        return format.format(ratio);
    }

    /**
     * 下载的比率:整数，用于计算ProgressBar的位置
     */
    public int getDownloadRatioSize() {
        int ratio = 0;
        if (mTotalBytes != 0L) {
            if (mStatus > 200 && mStatus < 300)
                ratio = (int) (mCurrentUnzipBytes * 1f / mTotalUnzipBytes * 100);
            else ratio = (int) (mCurrentBytes * 1f / mTotalBytes * 100);
        }
        return ratio;
    }

    /**
     * 获取下载状态对应的操作符
     */
    public String getNextOptText() {
        String desc;
        if (mControl == Downloads.CONTROL_PAUSED
                || mStatus == Downloads.STATUS_RUNNING && !DownloadService.isRunning) {
            if (mCurrentBytes == mTotalBytes){
                desc = "解压";
            }else{
                desc = "继续";
            }
        } else {
            if (mStatus == 0 || mStatus == Downloads.STATUS_CANCELED) {
                File file = getMobileFile();
                if (file.exists()) {
                    // 更新文件
                    desc = "更新";
                } else {
                    // 第一次下载文件
                    desc = "下载";
                }
            } else if ((mStatus == Downloads.STATUS_SUCCESS
                    || mStatus == Downloads.STATUS_UNZIP_SUCCESS) && isDownload) {
                desc = "完成";
            } else if (mStatus == Downloads.STATUS_DOWNLOAD_SUCCESS
                    || Downloads.isStatusUnzip(this.mStatus)) {
                desc = "解压";
            } else if (Downloads.isCanRetry(mStatus)) {
                // 判断当前状态是否可以重拾
                desc = "重试";
            } else {
                desc = "暂停";
            }
        }
        return desc;
    }

    public String getStatusText() {
        if (this.mControl == Downloads.CONTROL_PAUSED) {
            return "暂停下载";
        }
        return Downloads.statusToString(mStatus);
    }

    /**
     * 对应的本地文件对象，目前只考虑了地图文件
     */
    public File getMobileFile() {
        String prefix = getPrefix();
        // TODO: 2017/4/26 目前只考虑了地图文件
        return new File(Battle360Util.getFixedPath(Battle360Util.GlobalPath.Map, true)
                + prefix + File.separator + prefix + "." + DownloadType.getSuffix(this));
    }

    /**
     * 文件前缀
     */
    public String getPrefix() {
        int dotIndex = this.mServerFileName.lastIndexOf(".");
        return this.mServerFileName.substring(0, dotIndex);
    }

    @Override
    public String getTableName() {
        return "DownloadInfo";
    }

    @Override
    public String getCreateTableSQL() {
        return "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "description TEXT," +
                "mimeType TEXT," +
                "serverFileName TEXT," +
                "serverUri TEXT," +
                "serverModify BIGINT," +
                "localFileName TEXT," +
                "localUri TEXT," +
                "localModify BIGINT," +
                "downloadDate BIGINT," +
                "control INTEGER," +
                "status INTEGER," +
                "numFailed INTEGER," +
                "currentBytes BIGINT," +
                "totalBytes BIGINT," +
                "errMsg TEXT)";
    }

    @Override
    public SQLiteQueryParameters getSqLiteQueryParameters() {
        return new SQLiteQueryParameters();
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("title", this.mTitle);
        cv.put("description", this.mDescription);
        cv.put("mimeType", this.mMimeType);
        cv.put("serverFileName", this.mServerFileName);
        cv.put("serverUri", this.mServerUri);
        cv.put("serverModify", this.mServerModify);
        cv.put("localFileName", this.mLocaleFileName);
        cv.put("localUri", this.mLocaleUri);
        cv.put("localModify", this.mLocaleModify);
        cv.put("downloadDate", this.mDownloadDate);
        cv.put("control", this.mControl);
        cv.put("status", this.mStatus);
        cv.put("currentBytes", this.mCurrentBytes);
        cv.put("totalBytes", this.mTotalBytes);
        cv.put("errMsg", this.mErrMsg);
        return cv;
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        this.mId = cursor.getInt(0);
        this.mTitle = cursor.getString(1);
        this.mDescription = cursor.getString(2);
        this.mMimeType = cursor.getString(3);
        this.mServerFileName = cursor.getString(4);
        this.mServerUri = cursor.getString(5);
        this.mServerModify = cursor.getLong(6);
        this.mLocaleFileName = cursor.getString(7);
        this.mLocaleUri = cursor.getString(8);
        this.mLocaleModify = cursor.getLong(9);
        this.mDownloadDate = cursor.getLong(10);
        this.mControl = cursor.getInt(11);
        this.mStatus = cursor.getInt(12);
        this.mCurrentBytes = cursor.getInt(14);
        this.mTotalBytes = cursor.getInt(15);
        this.mErrMsg = cursor.getString(16);
    }

    public void updateDatabase() {
        String where = "serverFileName='" + this.mServerFileName + "'";
        ContentValues contentValues = this.generateContentValues();
        contentValues.remove("serverFileName");
        DatabaseHelper.getInstance().update(DownloadInfo.class, contentValues, where);
    }

    /**
     * 需要下载的文件大于不需要下载的文件，下载状态相同就按照默认顺序排列
     */
    @Override
    public int compareTo(@NonNull DownloadInfo another) {
        int result = 0;
        if (isDownload) {
            result = -1;
        } else if (another.isDownload()) {
            result = 1;
        }
        return result;
    }

    /**
     * 生成本地文件的路径，文件名前缀+数字的命名方式直到目录中不存在该文件
     */
    public boolean generateLocalePath() {
        boolean isCreateSuccess = false;
        if (BaseClassUtil.isNullOrEmptyString(mLocaleUri)) {
            String tempPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Temp, true);

            String fileName = mServerFileName;
            String prefix = getPrefix();
            int index = 1;
            while (true) {
                File file = new File(tempPath + fileName);
                if (!file.exists()) {
                    // 存在
                    try {
                        isCreateSuccess = file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                fileName = mServerFileName.replace(prefix, prefix + index);
                index++;
            }
            mLocaleFileName = fileName;
            mLocaleUri = tempPath + fileName;
        }
        return isCreateSuccess;
    }

    /**
     * 生成解压过程中的临时文件目录，生成原则同@generateLocalePath
     */
    public String generateUnzipPath() {
        int index = 1;
        String fileSuffix = "-unzip";
        String parentPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Map, true)
                + getPrefix() + fileSuffix;
        String path = parentPath;
        File file = new File(path);
        while (file.exists()) {
            path = parentPath + index;
            file = new File(path);
            index++;
        }
        return path + File.separator;
    }

    /**
     * 查询该文件在数据库中的存储记录时的条件
     */
    public String getSelecter() {
        return "serverFileName='" + this.mServerFileName + "'";
    }

    /**
     * Constants used to indicate network state for a specific download, after
     * applying any requested constraints.
     */
    public enum NetworkState {
        /**
         * The network is usable for the given download.
         */
        OK,

        /**
         * There is no network connectivity.
         */
        NO_CONNECTION,

        /**
         * The download exceeds the maximum size for this network.
         */
        UNUSABLE_DUE_TO_SIZE,

        /**
         * The download exceeds the recommended maximum size for this network,
         * the user must confirm for this download to proceed without WiFi.
         */
        RECOMMENDED_UNUSABLE_DUE_TO_SIZE,

        /**
         * The current connection is roaming, and the download can't proceed
         * over a roaming connection.
         */
        CANNOT_USE_ROAMING,

        /**
         * The app requesting the download specific that it can't use the
         * current network connection.
         */
        TYPE_DISALLOWED_BY_REQUESTOR,

        /**
         * Current network is blocked for requesting application.
         */
        BLOCKED;
    }

    /**
     * Returns the time when a download should be restarted.
     */
    public long restartTime(long now) {
        if (mNumFailed == 0) {
            return now;
        }
        if (mRetryAfter > 0) {
            return mLocaleModify + mRetryAfter;
        }
        return mLocaleModify + 60 * 1000;
    }

    /**
     * 准备下载
     */
    public boolean isReadyToDownload() {
        // 暂停状态
        if (mControl == Downloads.CONTROL_PAUSED) {
            return false;
        }
        switch (mStatus) {
            case 0: // status hasn't been initialized yet, this is a new download
            case Downloads.STATUS_PENDING: // download is explicit marked as ready to start
            case Downloads.STATUS_RUNNING: // download interrupted (process killed etc) while
                // running, without a chance to update the database
                return true;

            case Downloads.STATUS_WAITING_FOR_NETWORK:
            case Downloads.STATUS_QUEUED_FOR_WIFI:
                return checkCanUseNetwork(mTotalBytes) == NetworkState.OK;

            case Downloads.STATUS_WAITING_TO_RETRY:
                // download was waiting for a delayed restart
                final long now = System.currentTimeMillis();
                return restartTime(now) <= now;
            case Downloads.STATUS_DEVICE_NOT_FOUND_ERROR:

            case Downloads.STATUS_INSUFFICIENT_SPACE_ERROR:
                // avoids repetition of retrying download
                return false;
        }
        return false;
    }

    public boolean isReadyToUnzip() {
        // 暂停状态
        if (mControl == Downloads.CONTROL_PAUSED) {
            return false;
        }
        switch (mStatus) {
            case Downloads.STATUS_DOWNLOAD_SUCCESS:
            case Downloads.STATUS_UNZIP_PENDING:
            case Downloads.STATUS_UNZIP_RUNNING:
            case Downloads.STATUS_UNZIP_FAILED:
            case Downloads.STATUS_UNZIP_RENAME_ERROR:
            case Downloads.STATUS_UNZIP_DELETE_FAILED:
            case Downloads.STATUS_UNZIP_SUCCESS:
                return true;
        }
        return false;
    }

    /**
     * Returns whether this download is allowed to use the network.
     */
    public NetworkState checkCanUseNetwork(long totalBytes) {
        final NetworkInfo info = ConnectivityUtil.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return NetworkState.NO_CONNECTION;
        }
        return checkIsNetworkTypeAllowed(info.getType(), totalBytes);
    }

    /**
     * 判断当前网络连接类型是否允许下载文件
     */
    private NetworkState checkIsNetworkTypeAllowed(int networkType, long totalBytes) {
        final int flag = translateNetworkTypeToApiFlag(networkType);
        final boolean allowAllNetworkTypes = mAllowedNetworkTypes == ~0;
        if (!allowAllNetworkTypes && (flag & mAllowedNetworkTypes) == 0) {
            return NetworkState.TYPE_DISALLOWED_BY_REQUESTOR;
        }
        return NetworkState.OK;
    }

    /**
     * Translate a ConnectivityManager.TYPE_* constant to the corresponding
     * DownloadManager.Request.NETWORK_* bit flag.
     */
    private int translateNetworkTypeToApiFlag(int networkType) {
        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
                return DownloadManager.Request.NETWORK_MOBILE;

            case ConnectivityManager.TYPE_WIFI:
                return DownloadManager.Request.NETWORK_WIFI;

            default:
                return 0;
        }
    }

    /*@Override
    public String toString() {
        final CharArrayWriter writer = new CharArrayWriter();
        dump();
        return writer.toString();
    }*/

    @Override
    public String toString() {
        return "DownloadInfo{\n" +
                "mServerFileName='" + mServerFileName + '\'' +
                ", mServerUri='" + mServerUri + '\'' +
                ", mServerModify=" + mServerModify +
                "\n, mLocaleFileName='" + mLocaleFileName + '\'' +
                ", mLocaleUri='" + mLocaleUri + '\'' +
                ", mLocaleModify=" + mLocaleModify +
                "\n, mTotalBytes=" + mTotalBytes +
                ", mCurrentBytes=" + mCurrentBytes +
                ", isDownload=" + isDownload +
                "\n}";
    }

    public void dump() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Log.d("mLastMod", String.valueOf(mLocaleModify));
        Log.d("mMimeType", mMimeType);
        Log.d("mServerFileName", String.valueOf(mServerFileName));
        Log.d("mServerModify", sdf.format(new Date(mServerModify)));
        Log.d("mServerUri", String.valueOf(mServerUri));
        Log.d("mLocaleFileName", String.valueOf(mLocaleFileName));
        Log.d("mLocaleModify", sdf.format(new Date(mLocaleModify)));
        Log.d("mLocaleUri", String.valueOf(mLocaleUri));
        Log.d("mStatus", Downloads.statusToString(mStatus));
        Log.d("mCurrentBytes", String.valueOf(mCurrentBytes));
        Log.d("mTotalBytes", String.valueOf(mTotalBytes));
        Log.d("mNumFailed", String.valueOf(mNumFailed));
        Log.d("mRetryAfter", String.valueOf(mRetryAfter));
        Log.d("mAllowedNetworkTypes", String.valueOf(mAllowedNetworkTypes));
        Log.d("==", "====================================================");
    }

    /**
     * Return time when this download will be ready for its next action, in
     * milliseconds after given time.
     *
     * @return If {@code 0}, download is ready to proceed immediately. If
     * {@link Long#MAX_VALUE}, then download has no future actions.
     */
    public long nextActionMillis(long now) {
        if (Downloads.isStatusCompleted(mStatus)) {
            return Long.MAX_VALUE;
        }
        if (mStatus != Downloads.STATUS_WAITING_TO_RETRY) {
            return 0;
        }
        long when = restartTime(now);
        if (when <= now) {
            return 0;
        }
        return when - now;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DownloadInfo) {
            if (mServerFileName.equals(((DownloadInfo) o).mServerFileName)
                    && mServerUri.equals(((DownloadInfo) o).mServerUri)
                    && mServerModify == ((DownloadInfo) o).mServerModify) {
                return true;
            }
        }
        return super.equals(o);
    }

    /**
     * 判断两个DownloadInfo的状态是否相同
     */
    public boolean equalsStates(DownloadInfo info) {
        return this.mControl == info.mControl && this.mStatus == info.mStatus;
    }

    public void copyData(DownloadInfo info) {
        this.mId = info.mId;
        this.mTitle = info.mTitle;
        this.mDescription = info.mDescription;
        this.mServerFileName = info.mServerFileName;
        this.mServerUri = info.mServerUri;
        this.mServerModify = info.mServerModify;
        this.mLocaleFileName = info.mLocaleFileName;
        this.mLocaleUri = info.mLocaleUri;
        this.mLocaleModify = info.mLocaleModify;
        this.mControl = info.mControl;
        this.mStatus = info.mStatus;
        this.mDownloadDate = info.mDownloadDate;
        this.isDownload = info.isDownload();
        this.mMimeType = info.mMimeType;
        this.mAllowedNetworkTypes = info.mAllowedNetworkTypes;
        this.mNumFailed = info.mNumFailed;
        this.mRetryAfter = info.mRetryAfter;
        this.mCurrentBytes = info.mCurrentBytes;
        this.mTotalBytes = info.mTotalBytes;
        this.mCurrentUnzipBytes = info.mCurrentUnzipBytes;
        this.mTotalUnzipBytes = info.mTotalUnzipBytes;
        this.mPerSecondBytes = info.mPerSecondBytes;
    }

    /**
     * 下载时间
     * @return  格式：yyyy-MM-dd HH:mm:ss
     */
    public String getFormatDownloadDate() {
        String time = "未知";
        if (this.mDownloadDate != 0L) {
            Date date = new Date(this.mDownloadDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            time = sdf.format(date);
        }
        return time;
    }

    /**
     * 是否下载成功
     */
    public boolean isDownloadSuccess() {
        return mStatus == Downloads.STATUS_SUCCESS && mCurrentBytes == mTotalBytes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mTitle);
        dest.writeString(this.mDescription);
        dest.writeString(this.mMimeType);
        dest.writeString(this.mLocaleFileName);
        dest.writeString(this.mLocaleUri);
        dest.writeLong(this.mLocaleModify);
        dest.writeString(this.mServerFileName);
        dest.writeString(this.mServerUri);
        dest.writeLong(this.mServerModify);
        dest.writeInt(this.mControl);
        dest.writeInt(this.mStatus);
        dest.writeInt(this.mNumFailed);
        dest.writeLong(this.mDownloadDate);
        dest.writeLong(this.mTotalBytes);
        dest.writeLong(this.mCurrentBytes);
        dest.writeString(this.mErrMsg);
        dest.writeInt(this.mRetryAfter);
        dest.writeInt(this.mAllowedNetworkTypes);
        dest.writeLong(this.mCurrentUnzipBytes);
        dest.writeLong(this.mTotalUnzipBytes);
        dest.writeLong(this.mPerSecondBytes);
        dest.writeByte(this.isDownload ? (byte) 1 : (byte) 0);
    }

    protected DownloadInfo(Parcel in) {
        this.mId = in.readInt();
        this.mTitle = in.readString();
        this.mDescription = in.readString();
        this.mMimeType = in.readString();
        this.mLocaleFileName = in.readString();
        this.mLocaleUri = in.readString();
        this.mLocaleModify = in.readLong();
        this.mServerFileName = in.readString();
        this.mServerUri = in.readString();
        this.mServerModify = in.readLong();
        this.mControl = in.readInt();
        this.mStatus = in.readInt();
        this.mNumFailed = in.readInt();
        this.mDownloadDate = in.readLong();
        this.mTotalBytes = in.readLong();
        this.mCurrentBytes = in.readLong();
        this.mErrMsg = in.readString();
        this.mRetryAfter = in.readInt();
        this.mAllowedNetworkTypes = in.readInt();
        this.mCurrentUnzipBytes = in.readLong();
        this.mTotalUnzipBytes = in.readLong();
        this.mPerSecondBytes = in.readLong();
        this.isDownload = in.readByte() != 0;
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        @Override
        public DownloadInfo createFromParcel(Parcel source) {
            return new DownloadInfo(source);
        }

        @Override
        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };
}
