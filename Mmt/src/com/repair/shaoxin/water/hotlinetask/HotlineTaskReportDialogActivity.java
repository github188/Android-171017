package com.repair.shaoxin.water.hotlinetask;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.FileZipUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.io.File;
import java.util.List;

public class HotlineTaskReportDialogActivity extends BaseDialogActivity {

    private int noteType;
    private String taskID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.noteType = getIntent().getIntExtra("NodeType", -1);
        this.taskID = getIntent().getStringExtra("TaskID");
    }

    @Override
    protected void handleOkEvent(String tag, List<FeedItem> feedItemList) {
        switch (tag) {
            case "到达":
            case "处理":
            case "销单":
                report(feedItemList);
                break;
        }
    }

    String photoName = "";
    String audio = "";
    String description = "";
    String position = "";

    private void report(List<FeedItem> feedItemList) {
        photoName = "";
        audio = "";
        description = "";
        position = "";

        for (FeedItem feedItem : feedItemList) {
            switch (feedItem.Name) {
                case "照片":
                    photoName = feedItem.Value;
                    break;
                case "录音":
                    audio = feedItem.Value;
                    break;
                case "描述":
                    description = feedItem.Value;
                    break;
            }
        }

        if (TextUtils.isEmpty(photoName)) {
            Toast.makeText(HotlineTaskReportDialogActivity.this, "请先拍摄照片,再进行上传", Toast.LENGTH_SHORT).show();
            return;
        }

        photoName = photoName.split(",")[0];

        if (!TextUtils.isEmpty(audio))
            audio = audio.split(",")[0];

        position = GpsReceiver.getInstance().getLastLocalLocation().toXY();

        doReport();
    }

    private void doReport() {
        MmtBaseTask<String, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<String, Void, ResultWithoutData>(HotlineTaskReportDialogActivity.this) {
            @Override
            protected ResultWithoutData doInBackground(String... params) {
                ResultWithoutData data;

                try {
                    if (!TextUtils.isEmpty(audio)) {
                        String path = MyApplication.getInstance().getRecordPathString() + audio.replace(".wav", ".amr");
                        String name = "/SXRepairImage" + audio;

                        int code = new ReportInBackEntity().uploadFiles(path, name);

                        if (code <= 0)
                            throw new Exception("录音上传失败");
                    }

                    File file = new File(MyApplication.getInstance().getMediaPathString() + photoName);

                    byte[] buffer = FileZipUtil.DecodeFile(file);

                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_Mobile/REST/MobileREST.svc/MobileService/UploadFile?fileName="
                            + photoName + "&tableName=绍兴抢修工单节点反馈";

                    try {
                        NetUtil.executeHttpPost(url, buffer); // 不抛异常就认为成功
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        throw new Exception("图片上传失败");
                    }

                    HotlineFeedback feedback = new HotlineFeedback();

                    feedback.TaskID = taskID;
                    feedback.NodeType = noteType;
                    feedback.ReporterID = MyApplication.getInstance().getUserId();

                    if (TextUtils.isEmpty(audio))
                        feedback.Picture = photoName;
                    else
                        feedback.Picture = photoName + "," + audio;

                    feedback.Description = description;
                    feedback.Position = position;

                    url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/CallCenterREST.svc/Feedback";

                    String json = NetUtil.executeHttpPost(url, new Gson().toJson(feedback));

                    if (TextUtils.isEmpty(json))
                        throw new Exception("上报失败");

                    data = new Gson().fromJson(json, ResultWithoutData.class);

                    if (data == null)
                        throw new Exception("上报失败");
                } catch (Exception e) {
                    e.printStackTrace();

                    data = new ResultWithoutData();
                    data.ResultCode = -400;
                    data.ResultMessage = e.getMessage();
                }

                return data;
            }

            @Override
            protected void onSuccess(final ResultWithoutData data) {
                if (TextUtils.isEmpty(data.ResultMessage))
                    data.ResultMessage = data.ResultCode > 0 ? "上报成功" : "上报失败";

                OkDialogFragment fragment = new OkDialogFragment("运行结果：" + data.ResultMessage);

                fragment.setOnButtonClickListener(new OkDialogFragment.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(View view) {
                        if (data.ResultCode > 0) {
                            HotlineTaskReportDialogActivity.super.onSuccess();
                        }
                    }
                });

                fragment.setCancelable(false);
                fragment.show(getSupportFragmentManager(), "");
            }
        };

        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private class HotlineFeedback {
        String Description;
        int NodeType;
        String Picture;
        String Position;
        int ReporterID;
        String TaskID;
    }

}
