package com.patrol.module.myplan;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.patrol.common.MyPlanUtil;
import com.patrol.entity.KeyPoint;
import com.patrol.entity.TaskInfo;
import com.mapgis.mmt.global.MmtBaseTask;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class FetchPointsTask extends MmtBaseTask<TaskInfo, Integer, ResultData<KeyPoint>> {
    /**
     * 直接以GZIP字节流返回关联的点，不再以中间zip文件桥接
     */
    public boolean isGZip = false;

    public FetchPointsTask(Context context) {
        super(context);
    }

    private ResultData<String> getTaskZipName(int taskID) throws Exception {
        String url = MyPlanUtil.getStandardURL() + "/FetchPointsByZip?taskID=" + taskID;

        String result = NetUtil.executeHttpGetAppointLastTime(30, url);

        return new Gson().fromJson(result, new TypeToken<ResultData<String>>() {
        }.getType());
    }

    @Override
    public ResultData<KeyPoint> doInBackground(TaskInfo... params) {
        ResultData<KeyPoint> data = new ResultData<>();

        try {
            TaskInfo task = params[0];

            Log.v("巡线计划", "获取计划关联任务点==>" + task.ID);

            InputStream inputStream;

            if (!isGZip) {
                ResultData<String> rData = getTaskZipName(task.ID);

                if (rData.ResultCode < 0 || (rData.DataList != null && rData.DataList.size() == 0)) {
                    data.ResultCode = rData.ResultCode;
                    data.ResultMessage = rData.ResultMessage;

                    return data;
                }

                String name = rData.getSingleData();

                String url = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                        + "/OutFiles/DownLoadFiles/" + name + ".zip";

                InputStream stream = NetUtil.executeHttpGetInputStream(30, url);

                ZipInputStream zin = new ZipInputStream(stream);
                zin.getNextEntry();

                inputStream = zin;
            } else {
                String url = MyPlanUtil.getStandardURL() + "/FetchPointsWithGZip?taskID=" + task.ID;

                InputStream stream = NetUtil.executeHttpGetInputStream(30, url);

                inputStream = new GZIPInputStream(stream);
            }

            InputStreamReader reader = new InputStreamReader(inputStream);

            data = new Gson().fromJson(reader, new TypeToken<ResultData<KeyPoint>>() {
            }.getType());

            if (data != null && data.DataList != null && data.DataList.size() > 0) {
                checkOfflineArrived(task, data.DataList);
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * 检查离线保存的还没有上传成功的到位点数据
     *
     * @param task   巡线任务主体
     * @param points 关联设备点
     */
    private void checkOfflineArrived(TaskInfo task, ArrayList<KeyPoint> points) {
        try {
            String uri = ServerConnectConfig.getInstance().getBaseServerPath();
            int userID = MyApplication.getInstance().getUserId();

            List<ReportInBackEntity> entities = DatabaseHelper.getInstance().query(ReportInBackEntity.class,
                    new SQLiteQueryParameters("status=" + ReportInBackEntity.REPORTING + " and uri like '" + uri + "%' "
                            + "and type='巡线到位' and data='$GET$' and userId=" + userID));

            if (entities == null || entities.size() == 0)
                return;

            for (ReportInBackEntity entity : entities) {
                long key = Long.valueOf(entity.getKey());//批量到位点上传时，使用的key是时间戳，long类型

                if (key >= Integer.MAX_VALUE)
                    continue;

                int id = (int) key;

                for (KeyPoint kp : points) {
                    if (kp.ID == id) {
                        kp.IsArrive = 1;
                        kp.ArriveTime = BaseClassUtil.getSystemTime();
                        kp.ArriveMan = String.valueOf(userID);

                        if (kp.Type == 2)
                            task.PipeLenth += kp.Lenth;
                        else
                            task.ArriveSum++;

                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
