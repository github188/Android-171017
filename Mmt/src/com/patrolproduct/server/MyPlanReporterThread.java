package com.patrolproduct.server;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.FileZipUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.BigFileInfo;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.SavedReportInfo;
import com.mapgis.mmt.module.flowreport.FlowReportTaskParameters;
import com.mapgis.mmt.module.taskcontrol.TaskControlDBHelper;
import com.mapgis.mmt.net.BaseStringTask;
import com.mapgis.mmt.net.BaseTaskParameters;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 后台线程处理保存到数据库的属性编辑,反馈任务和事件任务
 *
 * @author Zoro
 */
public class MyPlanReporterThread implements Runnable {
    private final NetworkInfo wifiInfo;
    private final long state;

    public MyPlanReporterThread() {
        state = MyApplication.getInstance().getConfigValue("uploadBigPicByWifi", -1);

        ConnectivityManager wifiManager = (ConnectivityManager) MyApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiInfo = wifiManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

    }

    SavedReportInfo savedReportInfo;

    @Override
    public void run() {
        try {
            ArrayList<SavedReportInfo> infos = DatabaseHelper.getInstance().query(SavedReportInfo.class);

            for (SavedReportInfo savedReportInfo : infos) {
                this.savedReportInfo = savedReportInfo;

                if (this.savedReportInfo.getState().equals("1")) {
                    continue;
                }

                String type = savedReportInfo.getReportType();

                switch (type) {
                    case "arrive":
                        reportSavedArrive();
                        break;
                    case "feedback":
                        reportSavedFeedback();
                        break;
                    case "flow":
                        reportSavedFlow();
                        break;
                }
            }

            // 上传录音和原始图片文件
            List<BigFileInfo> bigFileInfos = DatabaseHelper.getInstance().query(BigFileInfo.class);

            if (bigFileInfos != null && bigFileInfos.size() > 0) {
                uploadFile(bigFileInfos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    private void reportSavedArrive() throws Exception {
        ArrayList<LinkedHashMap> items = new Gson().fromJson(savedReportInfo.getReportContent(), new TypeToken<ArrayList<LinkedHashMap>>() {
        }.getType());

        Map<String, String> paramters = new HashMap<>();

        for (LinkedHashMap item : items) {
            if (item.get("Type").equals("0") && item.get("Name").equals("taskid")) {
                paramters.put("taskid", String.valueOf(item.get("Value")));
            } else if (item.get("Type").equals("0") && item.get("Name").equals("index")) {
                paramters.put("index", String.valueOf(item.get("Value")));
            }
        }

        String taskid = paramters.get("taskid");
        if (TextUtils.isEmpty(taskid)) {
            return;
        }

        // 待解决bug????[{"Name":"flowid","Type":"0","Value":"0"},{"Name":"index","Type":"0","Value":"0"}]
        if ("0".equalsIgnoreCase(taskid)) {
            DatabaseHelper.getInstance().delete(SavedReportInfo.class, "id =" + savedReportInfo.getId());
        } else {
            String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/PatrolREST.svc";

            BaseStringTask task = new BaseStringTask(new BaseTaskParameters(url + "/ReportArrived", paramters));

            String result = task.execute();

            if (result.contains("到位成功")) {
                // 更新SQLite数据库，删除待反馈的任务
                DatabaseHelper.getInstance().delete(SavedReportInfo.class, "id =" + savedReportInfo.getId());
            }
        }
    }

    /**
     * 上报保存的反馈信息
     *
     * @throws Exception
     */
    private void reportSavedFeedback() throws Exception {
        // 反馈数据
        ArrayList<LinkedHashMap<String, String>> items = new Gson().fromJson(savedReportInfo.getReportContent(), new TypeToken<ArrayList<LinkedHashMap<String, String>>>() {
        }.getType());

        String[] args = new String[3];

        for (LinkedHashMap<String, String> item : items) {
            if (item.get("Type").equals("0") && item.get("Name").equals("flowid")) {
                args[0] = String.valueOf(item.get("Value"));
            } else if (item.get("Type").equals("0") && item.get("Name").equals("equiptype")) {
                args[1] = String.valueOf(item.get("Value"));
            } else if (item.get("Type").equals("0") && item.get("Name").equals("equipentity")) {
                args[2] = String.valueOf(item.get("Value"));
            }
        }

        // 照片名
        List<String> photoNames = savedReportInfo.getMediaList();

        if (photoNames.size() > 0 && savedReportInfo.getState().equals("unreported")) {
            uploadFiles(photoNames);

            ContentValues cv = new ContentValues();

            cv.put("state", "mediaUploaded");

            DatabaseHelper.getInstance().update(SavedReportInfo.class, cv, "id=" + savedReportInfo.getId());
        }

        uploadFeedback(args[0], args[1], args[2]);
    }

    public static void uploadFiles(List<String> files) {
        try {

            String baseUrl = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc";

            for (String f : files) {

                File file = new File(f);

                byte[] buffer = FileZipUtil.DecodeFile(file);

                String url = baseUrl + "/UploadFile?FileName=" + file.getName() + "&Flag=";

                NetUtil.executeHttpPost(url, buffer); // 不抛异常就认为成功

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("UploadFilies", "附件上传失败：" + e.getMessage());
        }
    }

    private void uploadFeedback(String flowid, String equiptype, String equipentity) throws Exception {

        String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/PatrolREST.svc" + "/UploadFeedItems";

        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json; charset=utf-8")
                .post(RequestBody.create(NetUtil.JSON, savedReportInfo.getReportContent()))
                .build();

        Response response = NetUtil.getHttpClient().newCall(request).execute();

        if (response.code() == 200) {

            String result = response.body().string();

            ResultWithoutData resultData = new Gson().fromJson(result, ResultWithoutData.class);

            // modify by Zoro at
            // 2015-04-08，增加容错性，降低要求，只要网络成功即认为成功，不做错误处理，避免重复上报问题
            // if (resultData.ResultCode > 0) {
            // 更新SQLite数据库，删除待反馈的任务
            DatabaseHelper.getInstance().delete(SavedReportInfo.class, "id =" + savedReportInfo.getId());
            // 删除 任务监控 日志
            TaskControlDBHelper.getIntance().deleteControlData(savedReportInfo.getTaskId() + "");
            MyApplication.getInstance().showMessageWithHandle("一条反馈上传完成");
            // } else {
            // // 修改 任务监控 日志
            // TaskControlDBHelper.getIntance().updateControlData(savedReportInfo.getTaskId()
            // + "", resultData.ResultMessage,
            // httpResponse.getStatusLine().getStatusCode());
            // }

        } else {
            // 否则 服务 没有 成功返回 结果 ， 修改 任务监控 日志
            response.close();

            TaskControlDBHelper.getIntance().updateControlData(savedReportInfo.getTaskId() + "",
                    "httpResponse.getStatusLine().getStatusCode()!=200", response.code());
        }
    }

    /**
     * 上报保存的事件信息
     */
    private String reportSavedFlow() throws Exception {

        String uri = savedReportInfo.getReportContent();
        uri = uri.replace(".amr", ".wav");

        List<String> mediaList = savedReportInfo.getMediaList();
        byte[] content = new byte[0];

        // 上传照片，压缩成ZIP文件，并以二进制的形式存在数据库中
        if (mediaList.size() > 0) {

            String pathString = FileZipUtil.Zip(mediaList);
            File file = new File(pathString);
            FileInputStream fileInputStream = new FileInputStream(file);

            content = new byte[(int) file.length()];

            fileInputStream.read(content);
            fileInputStream.close();
        }

        Request request = new Request.Builder().url(uri).post(RequestBody.create(null, content)).build();

        Response response = null;

        try {
            response = NetUtil.getHttpClient().newCall(request).execute();

            if (response.code() == 200) {

                // String fkId = EntityUtils.toString(httpResponse.getEntity()); // 服务端
                DatabaseHelper.getInstance().delete(SavedReportInfo.class, "id =" + savedReportInfo.getId());

                /*
                 * ContentValues cv2 = new ContentValues(); cv2.put("eventId",
                 * fkId); DatabaseHelper.getInstance().update(BigFileInfo.class,
                 * cv2, "taskId ='" + savedReportInfo.getTaskId() + "'");
                 */

                ContentValues cv = new ContentValues();
                cv.put("state", 1);
                DatabaseHelper.getInstance().update(FlowReportTaskParameters.class, cv, "reportRowId = '" + savedReportInfo.getTaskId() + "'");

                // 删除 监控 日志
                TaskControlDBHelper.getIntance().deleteControlData(savedReportInfo.getTaskId() + "");

                MyApplication.getInstance().showMessageWithHandle("一条事件上传完成");

                return savedReportInfo.getTaskId();

            } else {

                TaskControlDBHelper.getIntance().updateControlData(savedReportInfo.getTaskId() + "",
                        "httpResponse.getStatusLine().getStatusCode()!=200", response.code());
                return "网络连接错误";
            }
        } finally {
            if (null != response) {
                response.close();
            }
        }
    }

    /**
     * 上传原始图片文件和录音文件
     */
    private void uploadFile(List<BigFileInfo> infos) {
        for (int i = 0; i < infos.size(); i++) {

            BigFileInfo info = infos.get(i);

            String path = infos.get(i).getFileName();

            File file = new File(path);

            if (!file.exists()) {// 文件已经不存在
                DatabaseHelper.getInstance().delete(BigFileInfo.class, "id=" + info.getId());
                continue;
            }

            String name = file.getName();

            if (file.getName().endsWith(".amr")) {// 录音文件
                name = name.replace(".wav", ".amr");// 将录音文件更名，上传服务端数据库
                name = "/MMT/Patrol/Event/Sound/" + name;// 服务端存储图片的相对路径

                if (uploadFile(file, name)) {
                    DatabaseHelper.getInstance().delete(BigFileInfo.class, "id=" + info.getId());// 上传成功，则将成功上传的数据去除
                }
            } else {
                if (state < 1) {// 不传大图
                    DatabaseHelper.getInstance().delete(BigFileInfo.class, "id=" + info.getId());
                    continue;
                } else if (state == 1) { // 判断是否只在wifi网络下才上传数据
                    if (wifiInfo.isConnected()) {// wifi在连接状态下
                        name = "/MMT/Patrol/Event/BigPic/" + name;
                    } else {
                        continue;
                    }
                } else {// 任意情况下都上传数据
                    name = "/MMT/Patrol/Event/BigPic/" + name;
                }

                if (uploadFile(file, name)) {
                    DatabaseHelper.getInstance().delete(BigFileInfo.class, "id=" + info.getId());// 上传成功，则将成功上传的数据去除
                }
            }

        }
    }

    /**
     * 上传文件
     *
     * @param file     文件
     * @param filename 文件名,通常为相对路径+文件名
     * @return 是否上传成功
     */
    private boolean uploadFile(File file, String filename) {

        try {
            byte[] buffer = FileUtil.file2byte(file);

            String url = ServerConnectConfig.getInstance().getMobileBusinessURL()
                    + "/BaseREST.svc/UploadByteResource?path=" + Uri.encode(filename.trim(), "utf-8");

            NetUtil.executeHttpPost(url, buffer); // 不抛出异常就认为成功

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}