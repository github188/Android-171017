package com.mapgis.mmt.module.systemsetting.download;

/**
 * Created by Comclay on 2017/4/24.
 * 下载过程中的状态管理类
 */

public class Downloads {
    /**
     * 下载，更新，继续都是运行状态
     */
    public static final int CONTROL_RUN = 0;
    /**
     * 暂停状态
     */
    public static final int CONTROL_PAUSED = 1;

    public static final int STATUS_PAUSED = 0;

    public static final int STATUS_UNZIP_PENDING = 210;
    public static final int STATUS_UNZIP_RUNNING = 220;
    public static final int STATUS_UNZIP_FAILED = 230;
    public static final int STATUS_UNZIP_RENAME_ERROR = 240;
    public static final int STATUS_UNZIP_DELETE_FAILED = 250;
    public static final int STATUS_UNZIP_OLDFILE_DELETE_FAILED = 270;
    public static final int STATUS_UNZIP_SUCCESS = 260;

    public static boolean isStatusUnzip(int status) {
        return status > 200 && status < 300;
    }

    public static boolean isCanRetry(int status) {
        // 可以重试的情况有
        switch (status) {
            // 解压失败部分
            case STATUS_UNZIP_RUNNING:
            case STATUS_UNZIP_FAILED:
            case STATUS_UNZIP_RENAME_ERROR:
                // 下载失败部分
            case STATUS_FILE_ERROR:
            case STATUS_WAITING_FOR_NETWORK:
            case STATUS_INSUFFICIENT_SPACE_ERROR:
            case STATUS_NOT_ACCEPTABLE:
                return true;
        }
        return false;
    }

    public static boolean isDownloadSuccessed(int status) {
        return status > 200 && status < 300;
    }

    public static boolean isUnzipSuccessed(int status) {
        return status == STATUS_UNZIP_DELETE_FAILED
                || status == STATUS_UNZIP_SUCCESS
                || status == STATUS_UNZIP_OLDFILE_DELETE_FAILED
                || status == STATUS_UNZIP_RENAME_ERROR;
    }

    public static boolean isUnzipFailed(int status) {
        switch (status) {
            case STATUS_UNZIP_PENDING:
            case STATUS_UNZIP_RUNNING:
            case STATUS_UNZIP_FAILED:
                return true;
        }
        return false;
    }

    public static void isControlDownload() {

    }

        /*
         * Lists the states that the download manager can set on a download
         * to notify applications of the download progress.
         * The codes follow the HTTP families:<br>
         * 1xx: informational<br>
         * 2xx: success<br>
         * 3xx: redirects (not used by the download manager)<br>
         * 4xx: client errors<br>
         * 5xx: server errors
         */

    /**
     * Returns whether the status is informational (i.e. 1xx).
     */
    public static boolean isStatusInformational(int status) {
        return (status >= 100 && status < 200);
    }

    /**
     * Returns whether the status is a success (i.e. 2xx).
     */
    public static boolean isStatusSuccess(int status) {
        return (status == 200 || status == 260);
    }

    /**
     * Returns whether the status is an error (i.e. 4xx or 5xx).
     */
    public static boolean isStatusError(int status) {
        return (status >= 400 && status < 600);
    }

    /**
     * Returns whether the status is a client error (i.e. 4xx).
     */
    public static boolean isStatusClientError(int status) {
        return (status >= 400 && status < 500);
    }

    /**
     * Returns whether the status is a server error (i.e. 5xx).
     */
    public static boolean isStatusServerError(int status) {
        return (status >= 500 && status < 600);
    }

    /**
     * Returns whether the download has completed (either with success or
     * error).
     */
    public static boolean isStatusCompleted(int status) {
        // 取消不算完成
        return (status == 200 || status == 260) || (status >= 400 && status < 600) && status != 490;
    }

    /**
     * This download hasn't stated yet
     */
    public static final int STATUS_PENDING = 190;

    /**
     * This download has started
     */
    public static final int STATUS_RUNNING = 192;

    /**
     * This download has been paused by the owning app.
     */
    public static final int STATUS_PAUSED_BY_APP = 193;

    /**
     * This download encountered some network error and is waiting before retrying the request.
     */
    public static final int STATUS_WAITING_TO_RETRY = 194;

    /**
     * This download is waiting for network connectivity to proceed.
     */
    public static final int STATUS_WAITING_FOR_NETWORK = 195;

    /**
     * This download exceeded a size limit for mobile networks and is waiting for a Wi-Fi
     * connection to proceed.
     */
    public static final int STATUS_QUEUED_FOR_WIFI = 196;

    /**
     * This download couldn't be completed due to insufficient storage
     * space.  Typically, this is because the SD card is full.
     */
    public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 198;

    /**
     * This download couldn't be completed because no external storage
     * device was found.  Typically, this is because the SD card is not
     * mounted.
     */
    public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 199;

    /**
     * This download has successfully completed.
     * Warning: there might be other status values that indicate success
     * in the future.
     * Use isSucccess() to capture the entire category.
     */
    public static final int STATUS_SUCCESS = 200;
    public static final int STATUS_DOWNLOAD_SUCCESS = 197;

    /**
     * This request couldn't be parsed. This is also used when processing
     * requests with unknown/unsupported URI schemes.
     */
    public static final int STATUS_BAD_REQUEST = 400;

    /**
     * This download can't be performed because the content type cannot be
     * handled.
     */
    public static final int STATUS_NOT_ACCEPTABLE = 406;

    /**
     * This download cannot be performed because the length cannot be
     * determined accurately. This is the code for the HTTP error "Length
     * Required", which is typically used when making requests that require
     * a content length but don't have one, and it is also used in the
     * client when a response is received whose length cannot be determined
     * accurately (therefore making it impossible to know when a download
     * completes).
     */
    public static final int STATUS_LENGTH_REQUIRED = 411;

    /**
     * This download was interrupted and cannot be resumed.
     * This is the code for the HTTP error "Precondition Failed", and it is
     * also used in situations where the client doesn't have an ETag at all.
     */
    public static final int STATUS_PRECONDITION_FAILED = 412;

    /**
     * The lowest-valued error status that is not an actual HTTP status code.
     */
    public static final int MIN_ARTIFICIAL_ERROR_STATUS = 488;

    /**
     * The requested destination file already exists.
     */
    public static final int STATUS_FILE_ALREADY_EXISTS_ERROR = 488;

    /**
     * Some possibly transient error occurred, but we can't resume the download.
     */
    public static final int STATUS_CANNOT_RESUME = 489;

    /**
     * This download was canceled
     */
    public static final int STATUS_CANCELED = 490;

    /**
     * This download has completed with an error.
     * Warning: there will be other status values that indicate errors in
     * the future. Use isStatusError() to capture the entire category.
     */
    public static final int STATUS_UNKNOWN_ERROR = 491;

    /**
     * This download couldn't be completed because of a storage issue.
     * Typically, that's because the filesystem is missing or full.
     * Use the more specific {@link #STATUS_INSUFFICIENT_SPACE_ERROR}
     * and {@link #STATUS_DEVICE_NOT_FOUND_ERROR} when appropriate.
     */
    public static final int STATUS_FILE_ERROR = 492;

    /**
     * This download couldn't be completed because of an HTTP
     * redirect response that the download manager couldn't
     * handle.
     */
    public static final int STATUS_UNHANDLED_REDIRECT = 493;

    /**
     * This download couldn't be completed because of an
     * unspecified unhandled HTTP code.
     */
    public static final int STATUS_UNHANDLED_HTTP_CODE = 494;

    /**
     * This download couldn't be completed because of an
     * error receiving or processing data at the HTTP level.
     */
    public static final int STATUS_HTTP_DATA_ERROR = 495;

    /**
     * This download couldn't be completed because of an
     * HttpException while setting up the request.
     */
    public static final int STATUS_HTTP_EXCEPTION = 496;

    /**
     * This download couldn't be completed because there were
     * too many redirects.
     */
    public static final int STATUS_TOO_MANY_REDIRECTS = 497;

    /**
     * This download has failed because requesting application has been
     * blocked by NetworkPolicyManager.
     *
     * @hide
     * @deprecated since behavior now uses
     * {@link #STATUS_WAITING_FOR_NETWORK}
     */
    @Deprecated
    public static final int STATUS_BLOCKED = 498;

    /**
     * {@hide}
     */
    public static String statusToString(int status) {
        switch (status) {
            case STATUS_PENDING:
                return "等待下载";
            case STATUS_RUNNING:
                return "正在下载";
            case STATUS_PAUSED_BY_APP:
                return "停止下载";
            case STATUS_WAITING_TO_RETRY:
                return "等待重试";
            case STATUS_WAITING_FOR_NETWORK:
                return "网络不可用";
            case STATUS_QUEUED_FOR_WIFI:
                return "QUEUED_FOR_WIFI";
            case STATUS_INSUFFICIENT_SPACE_ERROR:
                return "可用空间不足";
            case STATUS_DEVICE_NOT_FOUND_ERROR:
                return "没有找到存储设备";
            case STATUS_DOWNLOAD_SUCCESS:
                return "下载成功";
            case STATUS_SUCCESS:
                return "下载完成";
            case STATUS_BAD_REQUEST:
                return "请求错误";
            case STATUS_NOT_ACCEPTABLE:
                return "NOT_ACCEPTABLE";
            case STATUS_LENGTH_REQUIRED:
                return "LENGTH_REQUIRED";
            case STATUS_PRECONDITION_FAILED:
                return "PRECONDITION_FAILED";
            case STATUS_FILE_ALREADY_EXISTS_ERROR:
                return "FILE_ALREADY_EXISTS_ERROR";
            case STATUS_CANNOT_RESUME:
                return "CANNOT_RESUME";
            case STATUS_CANCELED:
                return "已取消";
            case STATUS_UNKNOWN_ERROR:
                return "未知错误";
            case STATUS_FILE_ERROR:
                return "文件错误";
            case STATUS_UNHANDLED_REDIRECT:
                return "UNHANDLED_REDIRECT";
            case STATUS_UNHANDLED_HTTP_CODE:
                return "UNHANDLED_HTTP_CODE";
            case STATUS_HTTP_DATA_ERROR:
                return "HTTP_DATA_ERROR";
            case STATUS_HTTP_EXCEPTION:
                return "HTTP_EXCEPTION";
            case STATUS_TOO_MANY_REDIRECTS:
                return "TOO_MANY_REDIRECTS";
            case STATUS_BLOCKED:
                return "BLOCKED";
            case STATUS_UNZIP_PENDING:
                return "开始解压";
            case STATUS_UNZIP_RUNNING:
                return "正在解压";
            case STATUS_UNZIP_FAILED:
                return "解压失败";
            case STATUS_UNZIP_RENAME_ERROR:
                return "解压时文件重命名失败";
            case STATUS_UNZIP_DELETE_FAILED:
                return "删除下载文件失败";
            case STATUS_UNZIP_OLDFILE_DELETE_FAILED:
                return "旧文件删除失败";
            case STATUS_UNZIP_SUCCESS:
                return "解压完成，重启APP后生效";
            default:
                return Integer.toString(status);
        }
    }
}
