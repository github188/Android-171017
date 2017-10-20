package com.mapgis.mmt.module.systemsetting.download;

import com.mapgis.mmt.config.ServerConnectConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Comclay on 2017/4/24.
 *
 */

public class DownloadThread implements Runnable {
    // 标记下载服务是否正在运行
    public static boolean isRunning = false;
    String url = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath() + "/Mobile/";

    @Override
    public void run() {
        DownloadInfo downloadInfo = new DownloadInfo();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .addHeader("RANGE", "bytes=" + downloadInfo.mCurrentBytes + "-")
                .url(url + downloadInfo.mServerUri)
                .build();

        try {
            RandomAccessFile raf = new RandomAccessFile(downloadInfo.mLocaleUri, "rw");
            raf.seek(downloadInfo.mCurrentBytes);

            Response response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            byte[] buffer = new byte[2048];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                if (downloadInfo.mControl == Downloads.CONTROL_PAUSED) {
                    // 暂停下载
                    return;
                }
                if (downloadInfo.mStatus == Downloads.STATUS_CANCELED) {
                    // 取消下载
                    return;
                }
                downloadInfo.mCurrentBytes += length;
                raf.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
