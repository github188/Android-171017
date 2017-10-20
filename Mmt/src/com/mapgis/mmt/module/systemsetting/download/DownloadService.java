package com.mapgis.mmt.module.systemsetting.download;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.config.ServerConnectConfig;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Comclay on 2017/4/24.
 * 文件下载服务
 */

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    public static final String EXTRA_DOWNLOAD_INFO = "EXTRA_DOWNLOAD_INFO";
    // 下载和解压缓冲区大小8kb,4MB
    private final static int BUFFER_SIZE = 8192;
    private final static int BUFFER_UNZIP_SIZE = 1024 * 1024 * 4/*131072*/;
    // 发送广播的最小间隔500ms
    private final static int MIN_INTERVAL = 500;
    private final Object mObjLock = new Object();

    String url = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath() + "/Mobile/";
    private Queue<DownloadInfo> mDownloadQueue = new ConcurrentLinkedQueue<>();
    private DownloadBroadcastReceiver mDownloadReceiver;
    private DownloadNotification mNotification;
    private Handler mHandler;
    public static boolean isRunning = false;
    private boolean isSleepWait = false;

    @Override
    public void onCreate() {
        super.onCreate();
//        AppManager.addService(this);
        mHandler = new Handler();
        if (mNotification == null) {
            mNotification = new DownloadNotification(this);
        }

        mDownloadReceiver = DownloadBroadcastReceiver.registReceiver(this
                , DownloadBroadcastReceiver.ACTION_RECEIVER_STOP
                , DownloadBroadcastReceiver.ACTION_RECEIVER_CANCEL);
        mDownloadReceiver.setOnDownloadNotificationListener(
                new DownloadBroadcastReceiver.OnDownloadNotificationListener() {
                    @Override
                    void onStopDownload(DownloadInfo info) {
                        isSleepWait = true;
                        stopDownload(info);
                        synchronized (mObjLock) {
                            mObjLock.notify();
                        }
                    }

                    @Override
                    void onCancelDownload(DownloadInfo info) {
                        isSleepWait = true;
                        cancelDownload(info);
                        synchronized (mObjLock) {
                            mObjLock.notify();
                        }
                    }
                });
        Log.i(TAG, "DownloadService下载服务已开启");
    }

    private void cancelDownload(DownloadInfo info) {
        for (DownloadInfo tempInfo : mDownloadQueue) {
            if (info.equals(tempInfo)) {
                if (tempInfo.mStatus == Downloads.STATUS_PENDING) {
                    mDownloadQueue.remove(tempInfo);
                }
                info = tempInfo;
                break;
            }
        }

        if (!BaseClassUtil.isNullOrEmptyString(info.mLocaleUri)
                && info.mStatus != Downloads.STATUS_RUNNING) {
            File file = new File(info.mLocaleUri);
            if (file.exists()) {
                FileUtil.deleteFile(file);
            }
        }
        info.mControl = Downloads.CONTROL_RUN;
        info.mStatus = Downloads.STATUS_CANCELED;
        info.mCurrentBytes = 0L;
        info.mPerSecondBytes = 0L;
        sendBroadcast(info);
    }

    private void startDownloadThread() {
        Thread thread = new Thread(new DownloadThread());
//        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    private void stopDownloadThread() {
        if (isRunning) {
            isSleepWait = true;
            for (DownloadInfo info : mDownloadQueue) {
                stopDownload(info);
            }
            synchronized (mObjLock) {
                mObjLock.notify();
            }
        }

        DownloadBroadcastReceiver.unregistReceiver(this, mDownloadReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_DOWNLOAD_INFO)) {
            DownloadInfo info = intent.getParcelableExtra(EXTRA_DOWNLOAD_INFO);
            startDownload(info);
        }
        if (!isRunning) {
            startDownloadThread();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownload(DownloadInfo info) {
        isSleepWait = true;
        if (!info.equals(mDownloadQueue.peek())) {
            boolean flag = false;
            for (DownloadInfo temp : mDownloadQueue) {
                if (info.equals(temp)) {
                    if (temp.isReadyToDownload()) {
                        temp.mStatus = Downloads.STATUS_PENDING;
                    }
                    flag = true;
                    break;
                }
            }
            if (!flag) mDownloadQueue.add(info);
        }
        sendBroadcast(info);
        synchronized (mObjLock) {
            mObjLock.notify();
            isSleepWait = false;
        }
    }

    private void stopDownload(DownloadInfo info) {
        for (DownloadInfo tempInfo : mDownloadQueue) {
            if (info.equals(tempInfo)) {
                tempInfo.mControl = Downloads.CONTROL_PAUSED;
                if (tempInfo.mStatus == Downloads.STATUS_PENDING) {
                    mDownloadQueue.remove(tempInfo);
                }
                sendBroadcast(tempInfo);
                break;
            }
        }
    }

    private class DownloadThread implements Runnable {
        @Override
        public void run() {
            isRunning = true;
            Log.i(TAG, "DownloadThread下载线程已开启");
            synchronized (mObjLock) {
                DownloadInfo downloadInfo = mDownloadQueue.peek();
                while (downloadInfo != null) {
                    Log.i(TAG, "开始下载：" + downloadInfo.toString());

                    mNotification.init(downloadInfo);

                    if (downloadInfo.isReadyToDownload()) {
                        downloadFile(downloadInfo);
                    }

                    if (downloadInfo.isReadyToUnzip()) {
                        // 下载成功，开始解压
                        unzipFile(downloadInfo);
                    }
                    Log.i(TAG, "结束下载：" + downloadInfo.toString());
                    // 移除队首下载的文件对象
                    mDownloadQueue.poll();
                    downloadInfo = mDownloadQueue.peek();
                }
            }
            isRunning = false;
            Log.i(TAG, "DownloadThread下载线程已结束");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    DownloadService.this.stopSelf();
                }
            });
        }
    }

    private boolean downloadFile(DownloadInfo downloadInfo) {
        if (downloadInfo.mCurrentBytes != 0
                && downloadInfo.mCurrentBytes != new File(downloadInfo.mLocaleUri).length()) {
            downloadInfo.mStatus = Downloads.STATUS_FILE_ERROR;
            return false;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("RANGE", "bytes=" + downloadInfo.mCurrentBytes + "-")
                .url(url + downloadInfo.mServerUri)
                .build();
        RandomAccessFile raf = null;
        InputStream inputStream = null;

        try {
            downloadInfo.generateLocalePath();
            raf = new RandomAccessFile(downloadInfo.mLocaleUri, "rw");
            raf.seek(downloadInfo.mCurrentBytes);

            Response response = client.newCall(request).execute();
            inputStream = response.body().byteStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            downloadInfo.mStatus = Downloads.STATUS_RUNNING;
            downloadInfo.mDownloadDate = System.currentTimeMillis();

            sendBroadcast(downloadInfo);

            long lastTime = System.currentTimeMillis();
            long midMillis;
            long downloadSize = 0L;

            while ((length = inputStream.read(buffer)) != -1) {
                if (isSleepWait) {
                    synchronized (mObjLock) {
                        if (isSleepWait) {
                            try {
                                mObjLock.wait();
                                isSleepWait = false;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (downloadInfo.mStatus == Downloads.STATUS_CANCELED) {
                            // 取消下载
                            cancelOnRunning(downloadInfo);
                            mNotification.cancel();
                            return false;
                        }

                        if (downloadInfo.mControl == Downloads.CONTROL_PAUSED) {
                            sendBroadcast(downloadInfo);
                            mNotification.update(downloadInfo);
                            // 暂停下载
                            return false;
                        }
                    }
                }

                raf.write(buffer, 0, length);
                downloadInfo.mCurrentBytes += length;
                downloadInfo.updateDatabase();
                downloadSize += length;

                midMillis = System.currentTimeMillis() - lastTime;
                if (midMillis >= MIN_INTERVAL) {
                    // 大于1000ms发一次广播
                    downloadInfo.mPerSecondBytes = downloadSize / (midMillis == 0 ? 1 : midMillis);

                    sendBroadcast(downloadInfo);
                    mNotification.update(downloadInfo);
                    downloadSize = 0L;
                    lastTime = System.currentTimeMillis();
                }
            }
            downloadInfo.mPerSecondBytes = 0L;
            downloadInfo.mStatus = Downloads.STATUS_DOWNLOAD_SUCCESS;
            mNotification.update(downloadInfo);
            return true;
        } catch (SocketTimeoutException netErr) {
            downloadInfo.mStatus = Downloads.STATUS_WAITING_FOR_NETWORK;
            netErr.printStackTrace();
        } catch (IOException e) {
            downloadInfo.mStatus = Downloads.STATUS_FILE_ERROR;
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            downloadInfo.mPerSecondBytes = 0L;
            sendBroadcast(downloadInfo);
        }
        return false;
    }

    private void cancelOnRunning(DownloadInfo downloadInfo) {
        File file = new File(downloadInfo.mLocaleUri);
        if (file.exists()) {
            FileUtil.deleteFile(file);
        }

        downloadInfo.mCurrentBytes = 0L;
        downloadInfo.mPerSecondBytes = 0L;
        downloadInfo.mLocaleUri = "";
        sendBroadcast(downloadInfo);
    }

    /*解压文件*/
    private boolean unzipFile(final DownloadInfo info) {
        final File file = new File(info.mLocaleUri);
        final String unzipPath = info.generateUnzipPath();

        // TODO: 2017/4/28 如果是因为重命名失败，直接重命名
        if (info.mStatus == Downloads.STATUS_UNZIP_RENAME_ERROR
                || info.mStatus == Downloads.STATUS_UNZIP_DELETE_FAILED
                || info.mStatus == Downloads.STATUS_UNZIP_OLDFILE_DELETE_FAILED) {
            unzipSuccess(info, file, unzipPath);
            return true;
        }

        info.mCurrentUnzipBytes = 0L;
        info.mPerSecondBytes = 0L;

        InputStream in = null;
        BufferedInputStream bis;
        FileOutputStream fos = null;
        BufferedOutputStream bos;
        try {
            boolean ignoreDir = true;
            // 开始解压
            info.mStatus = Downloads.STATUS_UNZIP_PENDING;

            File desDir = new File(unzipPath);
            if (!desDir.exists()) {
                if (!desDir.mkdirs())
                    return false;
            }

            ZipFile zipFile = new ZipFile(file, "gbk");
            Enumeration<?> e = zipFile.getEntries();
            ZipEntry zipEntry;

            while (e.hasMoreElements()) {
                zipEntry = (ZipEntry) e.nextElement();
                info.mTotalUnzipBytes += zipEntry.getSize();
            }
            sendBroadcast(info);
            e = zipFile.getEntries();

            byte[] buffer = new byte[BUFFER_UNZIP_SIZE];
            int length;
            long lastTime = System.currentTimeMillis();
            long midMillis;
            long downloadSize = 0L;

            while (e.hasMoreElements()) {
                zipEntry = (ZipEntry) e.nextElement();
                String name = new String(zipEntry.getName().getBytes(zipFile.getEncoding()), "gbk");
                File entryFile = new File(unzipPath + name);
                // 文件夹
                if (zipEntry.isDirectory()) {
                    if (ignoreDir)
                        continue;

                    if (!entryFile.mkdirs())
                        Log.e("Zoro", "创建文件夹失败");
                }

                if (ignoreDir && (name.contains("\\") || name.contains("/"))) {
                    name = entryFile.getName();
                    entryFile = new File(unzipPath + name);
                }

                if (entryFile.exists()) {
                    entryFile.deleteOnExit();
                } else if (!entryFile.getParentFile().exists()) {
                    if (!entryFile.getParentFile().mkdirs())
                        continue;
                }

                if (!entryFile.createNewFile())
                    continue;

                in = zipFile.getInputStream(zipEntry);
                bis = new BufferedInputStream(in);
                fos = new FileOutputStream(entryFile);
                bos = new BufferedOutputStream(fos);

                info.mStatus = Downloads.STATUS_UNZIP_RUNNING;
                sendBroadcast(info);

                while ((length = bis.read(buffer, 0, buffer.length)) != -1) {
                    if (info.mStatus == Downloads.STATUS_CANCELED) {
                        // 解压过程中不允许暂停，只能取消，取消时会删除已经解压部分的文件,及下载文件
                        FileUtil.deleteFile(desDir);
                        cancelOnRunning(info);
                        mNotification.cancel();
                        return false;
                    }
                    bos.write(buffer, 0, length);
                    info.mCurrentUnzipBytes += length;
                    info.updateDatabase();
                    downloadSize += length;

                    midMillis = System.currentTimeMillis() - lastTime;
                    if (midMillis >= MIN_INTERVAL) {
                        info.mPerSecondBytes = downloadSize / (midMillis == 0 ? 1 : midMillis);
                        // 每秒钟解压的大小
                        sendBroadcast(info);
                        mNotification.update(info);

                        downloadSize = 0L;
                        lastTime = System.currentTimeMillis();
                    }
                }
                bos.flush();
                bos.close();
                bis.close();
            }
            // 解压成功
            return !unzipSuccess(info, file, unzipPath);
        } catch (Exception ex) {
            info.mPerSecondBytes = 0L;
            sendBroadcast(info);
            ex.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    private boolean unzipSuccess(DownloadInfo info, File file, String unzipPath) {
        info.mPerSecondBytes = 0L;

        String oldPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Map, true) + info.getPrefix();
        File oldFile = new File(oldPath);
        boolean b = FileUtil.deleteFile(oldFile);
        if (!b) {
            // 老文件删除失败
            info.mStatus = Downloads.STATUS_UNZIP_OLDFILE_DELETE_FAILED;
            sendBroadcast(info);
            mNotification.update(info);
            return false;
        }

        boolean isOk = new File(unzipPath).renameTo(oldFile);
        if (!isOk) {
            info.mStatus = Downloads.STATUS_UNZIP_RENAME_ERROR;
            sendBroadcast(info);
            mNotification.update(info);
            return true;
        }

        info.mLocaleModify = info.mServerModify;
        isOk = file.delete();
        if (isOk) {
            info.mStatus = Downloads.STATUS_UNZIP_SUCCESS;
        } else {
            info.mStatus = Downloads.STATUS_UNZIP_DELETE_FAILED;
        }
        sendBroadcast(info);
        mNotification.update(info);
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopDownloadThread();
        if (mNotification != null) {
            mNotification.cancel();
        }
//        AppManager.removeService(this);
        Log.i(TAG, "DownloadService下载服务已停止");
        super.onDestroy();
    }

    public void sendBroadcast(DownloadInfo info) {
        info.updateDatabase();
        DownloadBroadcastReceiver.sendBroadcast(this, info
                , DownloadBroadcastReceiver.ACTION_RECEIVER_UPDATE);
    }
}
