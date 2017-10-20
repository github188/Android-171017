package com.repair.shaoxin.water.repairtask;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.FileZipUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.BaseDialogActivity;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RepairTaskReportDialogActivity extends BaseDialogActivity {

    private int noteType;
    private int taskID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.noteType = getIntent().getIntExtra("NodeType", -1);
        this.taskID = getIntent().getIntExtra("TaskID", -1);
    }

    @Override
    protected void handleOkEvent(String tag, List<FeedItem> feedItemList) {

        switch (tag) {

            case "到达":
            case "处理":
            case "销单":
                report(feedItemList);
                break;

            case "退单":
                String description = "";
                for (FeedItem feedItem : feedItemList) {
                    if ("描述".equals(feedItem.Name)) {
                        description = feedItem.Value;
                        break;
                    }
                }
                doReport("", description,"","","","");
                break;
        }
    }

    private void report(List<FeedItem> feedItemList) {

        String photoName = "";
        String description = "";

        String Depth = "";
        String Diameter = "";
        String Material = "";
        String AddrDes = "";

        for (FeedItem feedItem : feedItemList) {
            switch (feedItem.Name) {
                case "照片":
                    photoName = feedItem.Value;
                    break;
                case "描述":
                    description = feedItem.Value;
                    break;
                case "埋深":
                    Depth = feedItem.Value;
                    break;
                case "管径":
                    Diameter = feedItem.Value;
                    break;
                case "材质":
                    Material = feedItem.Value;
                    break;
                case "所处位置":
                    AddrDes = feedItem.Value;
                    break;
            }
        }

        if (TextUtils.isEmpty(photoName)) {
            Toast.makeText(RepairTaskReportDialogActivity.this, "请先拍摄照片,再进行上传", Toast.LENGTH_SHORT).show();
            return;
        }

        doReport(photoName, description, Depth, Diameter, Material, AddrDes);
    }

    static final JsonFactory jsonFactory = new JsonFactory();

    private void doReport(final String photoName, final String description, final String Depth, final String Diameter, final String Material, final String AddrDes) {

        final String position = GpsReceiver.getInstance().getLastLocalLocation().toXY();

        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(RepairTaskReportDialogActivity.this) {
            @Override
            protected String doInBackground(String... params) {

                String result = "上报失败";

                StringBuilder sb = new StringBuilder();
                sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                        .append("/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/PatrolREST.svc/ReportRepairTask")
                        .append("?userId=").append(MyApplication.getInstance().getUserId())
                        .append("&taskId=").append(taskID)
                        .append("&nodeType=").append(noteType)
                        .append("&description=").append(description)
                        .append("&position=").append(position)
                        .append("&Depth=").append(Depth)
                        .append("&Diameter=").append(Diameter)
                        .append("&Material=").append(Material)
                        .append("&AddrDes=").append(AddrDes);

                if (!TextUtils.isEmpty(photoName)) {
                    sb.append("&picName=").append(photoName);
                }

                try {

                    // 上报反馈信息
                    String str = NetUtil.executeHttpGet(sb.toString());
                    result = parseJsonStr(str); // 成功为“上报成功”

                    if (TextUtils.isEmpty(result)) {
                        result = "上报失败";
                    }

                    // 反馈信息上报成功后上传反馈图片
                    if ("上报成功".equals(result) && !TextUtils.isEmpty(photoName)) {

                        File file = new File(MyApplication.getInstance().getMediaPathString() + photoName);
                        byte[] buffer = FileZipUtil.DecodeFile(file);

                        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_Mobile/REST/MobileREST.svc/MobileService/UploadFile?fileName="
                                + photoName + "&tableName=绍兴抢修工单节点反馈";

                        NetUtil.executeHttpPost(url, buffer); // 不抛异常就认为成功

                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    if ("上报成功".equals(result)) {
                        result = "反馈图片上传异常";
                    }
                }

                return result;
            }

            @Override
            protected void onSuccess(final String result) {

                OkDialogFragment fragment = new OkDialogFragment("运行结果：" + result);
                fragment.setOnButtonClickListener(new OkDialogFragment.OnButtonClickListener() {
                    @Override
                    public void onButtonClick(View view) {

                        if (result.equals("上报成功")) {
                            RepairTaskReportDialogActivity.super.onSuccess();
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

    @Nullable
    private String parseJsonStr(String str) throws IOException {

        if (TextUtils.isEmpty(str)) {
            return "";
        }

        if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1 && (str.charAt(1) == '[' || str.charAt(1) == '{')) {
            str = str.substring(1, str.length() - 1);
        }
        str = str.replace("\\", "");

        JsonParser jsonParser = jsonFactory.createJsonParser(str);
        jsonParser.nextToken();

        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
            return jsonParser.getText();
        }

        String name = "", text = "";
        JsonToken token;

        while (jsonParser.nextToken() != null) {

            token = jsonParser.getCurrentToken();
            name = jsonParser.getCurrentName();
            text = jsonParser.getText();

            if (name.equals("Msg") && token == JsonToken.VALUE_STRING) {
                return text;
            }
        }

        return "";
    }

}
