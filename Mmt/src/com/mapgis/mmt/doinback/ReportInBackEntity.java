package com.mapgis.mmt.doinback;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultStatus;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.Reporter;
import com.mapgis.mmt.module.taskcontrol.ITaskControlOper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 后台反馈数据模型
 */
public class ReportInBackEntity implements ISQLiteOper, ITaskControlOper {
    /**
     * 正在上报
     */
    public static final int REPORTING = 1;
    /**
     * 正在保存
     */
    public static final int SAVING = 2;

    public static final String GET_FLAG = "$GET$";

    private int _id;
    // 将对象转换为Json字符串存储
    private String data;
    private int userId;
    private int status;

    private String uri;

    // 关键属性，用来存储该数据的唯一标识
    // 判断该数据在本地是否存储
    private String key;

    // 记录上报类型，是什么信息的上报
    private String type;

    // 记录文件位置，多个以,分割
    private String filePaths;

    // 上报时文件的采用的名称 Maintenance\WX2012-02-12-0809\140215_121357.jpg
    private String reportFileName;

    // 后台任务创建时间
    private String createTime;

    // 后台上报重试次数
    private int retryTimes;

    private Reporter<String> progressReporter;

    public ReportInBackEntity() {
    }

    /**
     * @param data           json字符串类型的数据，即为将要上报的数据
     * @param userId         用户id
     * @param status         数据状态（UNREPORTED暂不上报，REPORTING需要上报）
     * @param uri            数据上报的URI地址
     * @param key            查询数据时需要的条件信息
     * @param type           该数据的存储的事件类型
     * @param filePaths      照片存储的绝对路径，手机磁盘的绝对存储路径，为了读取文件
     * @param reportFileName 照片名称（含相对路径），服务器上文件目录的相对路径，为了存储到数据库
     */
    public ReportInBackEntity(String data, int userId, int status, String uri, String key, String type,
                              String filePaths, String reportFileName) {
        this.data = data;
        this.userId = userId;
        this.status = status;
        this.uri = uri;
        this.key = key;
        this.type = type;
        this.filePaths = filePaths;
        this.reportFileName = reportFileName;

        this.createTime = BaseClassUtil.getSystemTime();
        this.retryTimes = 0;
    }

    @Override
    public String getTableName() {
        return "ReportInBack";
    }

    @Override
    public String getCreateTableSQL() {
        return "(_id integer primary key,data,userId,status,uri,key,type,filePaths,reportFileName,createTime,retryTimes)";
    }

    @Override
    public SQLiteQueryParameters getSqLiteQueryParameters() {
        return null;
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put("data", data);
        contentValues.put("userId", userId);
        contentValues.put("status", status);
        contentValues.put("uri", uri);
        contentValues.put("key", key);
        contentValues.put("type", type);
        contentValues.put("filePaths", filePaths);
        contentValues.put("reportFileName", reportFileName);
        contentValues.put("createTime", createTime);
        contentValues.put("retryTimes", retryTimes);

        return contentValues;
    }

    public ContentValues generateContentValues(String[] includeColumns) {

        ContentValues contentValues = new ContentValues();

        for (String columnName : includeColumns) {
            switch (columnName) {
                case "data":
                    contentValues.put("data", data);
                    break;
                case "userId":
                    contentValues.put("userId", userId);
                    break;
                case "status":
                    contentValues.put("status", status);
                    break;
                case "uri":
                    contentValues.put("uri", uri);
                    break;
                case "key":
                    contentValues.put("key", key);
                    break;
                case "type":
                    contentValues.put("type", type);
                    break;
                case "filePaths":
                    contentValues.put("filePaths", filePaths);
                    break;
                case "reportFileName":
                    contentValues.put("reportFileName", reportFileName);
                    break;
                case "createTime":
                    contentValues.put("createTime", createTime);
                    break;
                case "retryTimes":
                    contentValues.put("retryTimes", retryTimes);
                    break;
            }
        }

        return contentValues;
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        this._id = cursor.getInt(0);
        this.data = cursor.getString(1);
        this.userId = cursor.getInt(2);
        this.status = cursor.getInt(3);
        this.uri = cursor.getString(4);
        this.key = cursor.getString(5);
        this.type = cursor.getString(6);
        this.filePaths = cursor.getString(7);
        this.reportFileName = cursor.getString(8);
        this.createTime = cursor.getString(9);
        this.retryTimes = cursor.getInt(10);
    }

    /**
     * 插入后台反馈数据
     *
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert() {
        long newRowId = DatabaseHelper.getInstance().insert(this);

        // 插入后台任务成功则唤醒后台上报线程
        if (newRowId >= 0) {
            ReportInBackThread.getInstance().notifyDoWork();
        }

        return newRowId;
    }

    /**
     * 更新后台反馈数据
     *
     * @return the number of rows affected
     */
    public long update() {
        return DatabaseHelper.getInstance().update(ReportInBackEntity.class, generateContentValues(), "key='" + key + "'");
    }

    /**
     * 对指定的列进行更新
     *
     * @param includeColumns 需要更新的列
     */
    public long update(String[] includeColumns) {
        return DatabaseHelper.getInstance().update(ReportInBackEntity.class, generateContentValues(includeColumns), "_id=" + _id);
    }

    public long delete() {
        return DatabaseHelper.getInstance().delete(ReportInBackEntity.class, "_id=" + _id);
    }

    /**
     * 获取该数据的 主键ID
     *
     * @return 主键ID
     */
    public int getIdInSQLite() {
        ArrayList<ReportInBackEntity> list = DatabaseHelper.getInstance().query(ReportInBackEntity.class,
                new SQLiteQueryParameters("key = '" + key + "'"));

        if (list != null && list.size() != 0) {

            this._id = list.get(0)._id;

            return list.get(0)._id;
        }
        return -1;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilePaths() {
        return filePaths;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    @Override
    public String showData() {
        data = data.replace("}", "").replace("{", "").replace("[", "").replace("]", "").replace(",\"", "\n\"");
        return data;
    }

    public ResultData<Integer> report() {
        return report(null);
    }

    public ResultData<Integer> report(Reporter<String> progressReporter) {
        this.progressReporter = progressReporter;

        ResultData<Integer> data;
        int status = 200;

        try {
            reportProgress("正在连接服务器...");

            if (!NetUtil.testNetState()) {
                data = new ResultData<>();
                data.ResultCode = -200;
                data.ResultMessage = "与服务器连接不通畅";
                data.DataList.add(-200);

                return data;
            }

            if (!BaseClassUtil.isNullOrEmptyString(filePaths)) {
                // 若文件数据存在，则判断是否是多个文件
                List<String> absolutePaths = BaseClassUtil.StringToList(filePaths, ",");
                List<String> fileNames = BaseClassUtil.StringToList(reportFileName, ",");

                int total = absolutePaths.size();

                for (int i = 0; i < total; i++) {
                    String path = absolutePaths.get(i);
                    String name = fileNames.get(i);

                    if (new File(path).isDirectory()) {
                        continue;
                    }

                    reportProgress("正在上传第[" + (i + 1) + "/" + total + "]个文件...");

                    int resultFlag = uploadFiles(path, name);

                    if (resultFlag < 0) {
                        data = new ResultData<>();

                        data.ResultCode = -200;
                        data.ResultMessage = "上传失败，请稍后重试";
                        data.DataList.add(-200);

                        return data;
                    }
                }
            }

            reportProgress("正在上传表单数据...");

            String jsonData = this.data.replace(".amr", ".wav");

            String json;

            if (jsonData.toUpperCase().equals(GET_FLAG))
                json = NetUtil.executeHttpGet(uri);
            else
                json = NetUtil.executeHttpPost(uri, jsonData, "Content-Type", "application/json; charset=utf-8");

            if (BaseClassUtil.isNullOrEmptyString(json))
                throw new Exception("返回结果为空");

            // 用于兼容新的返回格式
            if (json.contains("currentPageIndex")) {
                Results<Integer> rawResult = new Gson().fromJson(json, new TypeToken<Results<Integer>>() {
                }.getType());
                data = rawResult.toResultData();
            } else if (json.contains("statusCode")) {
                ResultStatus resultStatus = new Gson().fromJson(json, new TypeToken<ResultStatus>() {
                }.getType());
                ResultWithoutData resultWithoutData = resultStatus.toResultWithoutData();
                data = new ResultData<>();
                data.ResultCode = resultWithoutData.ResultCode;
                data.ResultMessage = resultWithoutData.ResultMessage;
            } else {
                data = new Gson().fromJson(json, new TypeToken<ResultData<Integer>>() {
                }.getType());
            }

            data.DataList.add(0, status);
            reportProgress("完成上报任务...");

        } catch (Exception ex) {
            data = new ResultData<>();
            data.ResultCode = -100;
            data.ResultMessage = ex.getMessage();
            data.DataList.add(status);
            reportProgress("上报失败：" + ex.getMessage());
        }

        return data;
    }

    private void reportProgress(String progress) {
        if (progressReporter != null) {
            progressReporter.report(progress);
        }
    }

    public int uploadFiles(String filePath, String reportFileNames) {

        int retryTimes = 0;

        while (true) {
            try {
                File file = new File(filePath.trim());

                // 若文件不存在，则返回1。
                // 考虑用户操作时候可能存在文件未上传，但文件被误操作删除掉，会导致数据一直上传不上去，所以文件不存在，则默认上传成功
                if (!file.exists()) {
                    return 1;
                }

                /* 上传文本信息跟上传文件时不同的服务 */
                // 1.虽然本地录音文件虽然为amr格式，但手机数据库里要求存用wav后缀命名，这样服务端数据库存储就为wav格式的名称
                // 2.因为wav格式在上传文件过后会重用reportFileNames进行命名，则会出现wav后缀，导致文件格式错误，故需要再次进行再次改名
                if (reportFileNames.endsWith(".wav")) {
                    reportFileNames = reportFileNames.replace(".wav", ".amr");
                }

                String path = Uri.encode(reportFileNames.trim());

                if (MyApplication.getInstance().getConfigValue("TestFileExist", 0) > 0 && NetUtil.testFileExist(path))
                    return 200;

                byte[] buffer;

//                if (filePath.toLowerCase().endsWith(".jpg")) {
//                    buffer = FileZipUtil.DecodeFile(file);
//                } else {
//                    buffer = FileUtil.file2byte(file);
//                }
                buffer = FileUtil.file2byte(file);

                String url = ServerConnectConfig.getInstance().getMobileBusinessURL()
                        + "/BaseREST.svc/UploadByteResource?path=" + path;

                String uploadResultStr = NetUtil.executeHttpPost(url, buffer); // 不抛异常就认为成功
                if (!BaseClassUtil.isNullOrEmptyString(uploadResultStr)) {
                    ResultWithoutData result = new Gson().fromJson(uploadResultStr, ResultWithoutData.class);
                    if (result.ResultCode == 200) {
//                        if (!file.delete())
//                            Log.e("zoro", "后台上报后文件删除错误");

                        return 200;
                    }
                }

                return -1;

            } catch (Exception e) {
                e.printStackTrace();
                if (++retryTimes > 2) {
                    return -1;
                }
            }
        }
    }
}
