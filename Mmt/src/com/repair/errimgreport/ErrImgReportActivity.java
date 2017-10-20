package com.repair.errimgreport;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.errimgreport.entity.CheckManRnt;
import com.mapgis.mmt.module.gis.toolbar.errimgreport.entity.ReportRnt;
import com.mapgis.mmt.module.gis.toolbar.errimgreport.entity.Rnt;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.BaseDialogActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by liuyunfan on 2016/7/7.
 */
public class ErrImgReportActivity extends BaseDialogActivity {

    private String checkRole = "管网审核";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                new String[]{"DisplayName", "缺失类型", "Name", "geoType", "Type", "值选择器", "Value", "", "ConfigInfo", "点,线,面", "Validate", "1"},
                new String[]{"DisplayName", "审核人", "Name", "auditUser", "Type", "值选择器", "Validate", "1"},
                new String[]{"DisplayName", "范围", "Name", "geoExtent", "Type", "区域控件", "Validate", "1"},
                new String[]{"DisplayName", "问题描述", "Name", "discribe", "Type", "长文本", "DisplayColSpan", "3", "Validate", "0"},
                new String[]{"DisplayName", "照片", "Name", "照片", "Type", "拍照", "Validate", "0"});
        getIntent().putExtra("GDFormBean", gdFormBean);
        getIntent().putExtra("Title", "错误图形上报");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void handleOkEvent(String tag, final List<FeedItem> feedItemList) {

        if (feedItemList == null) {
            return;
        }

        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {

                String rnt = reportData(feedItemList);

                if (TextUtils.isEmpty(rnt)) {
                    MyApplication.getInstance().showMessageWithHandle("网络异常或服务不存在");

                    return null;
                }
                ReportRnt reportRnt = new Gson().fromJson(rnt, ReportRnt.class);

                if (reportRnt == null) {
                    MyApplication.getInstance().showMessageWithHandle("服务返回数据错误");

                    return null;
                }

                if (!reportRnt.IsSuccess) {
                    MyApplication.getInstance().showMessageWithHandle(reportRnt.Msg);

                    return null;
                }


                if ("success".equals(reportImg(reportRnt.ID))) {
                    MyApplication.getInstance().showMessageWithHandle("上报成功");
                    return "success";
                }

                return null;
            }

            @Override
            protected void onPostExecute(String str) {
                if ("success".equals(str)) {
                    AppManager.finishActivity();
                }
            }
        }.mmtExecute();

    }

    @Override
    protected void onViewCreated() {
        //http://192.168.12.200:802/webgis/widgetconfigs/ReportDataLackWidget.xml
        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath() +
                        "/services/zondy_mapgiscitysvr_audit/rest/auditrest.svc/GetAuditRoleName?f=json&ID=&roleName=" + checkRole;
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(ErrImgReportActivity.this, "网络异常或服务错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                final CheckManRnt checkManRnt = new Gson().fromJson(s, CheckManRnt.class);

                if (checkManRnt == null) {
                    Toast.makeText(ErrImgReportActivity.this, "解析结果异常", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!checkManRnt.rnt.IsSuccess) {
                    Toast.makeText(ErrImgReportActivity.this, !TextUtils.isEmpty(checkManRnt.rnt.Msg) ? checkManRnt.rnt.Msg : "错误，但未返回错误信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (checkManRnt.NameList == null || checkManRnt.NameList.size() == 0) {
                    Toast.makeText(ErrImgReportActivity.this, "请配置[" + checkRole + "]人员", Toast.LENGTH_SHORT).show();
                    return;
                }

                View checkManView = getFlowBeanFragment().findViewByName("auditUser");

                if (checkManView instanceof ImageButtonView) {

                    final ImageButtonView imageButtonView = (ImageButtonView) checkManView;
                    imageButtonView.setValue(checkManRnt.NameList.get(0));

                    imageButtonView.getButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ListDialogFragment fragment = new ListDialogFragment("审核人", checkManRnt.NameList);
                            fragment.show(ErrImgReportActivity.this.getSupportFragmentManager(), "");
                            fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                                @Override
                                public void onListItemClick(int arg2, String value) {
                                    imageButtonView.setValue(value);
                                }
                            });
                        }
                    });
                }
            }
        }.mmtExecute();
    }

    String imgUrls = "";

    private String reportData(List<FeedItem> feedItemList) {
        String url = ServerConnectConfig.getInstance().getBaseServerPath() + "/services/zondy_mapgiscitysvr_audit/rest/auditrest.svc/InsertAttrForGeo";

        List<String> list = new ArrayList<String>();

        list.add("reporter");
        list.add(MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName);

        list.add("oper");
        list.add("图形修改");
        //返回的范围没有闭合，这里需要上传闭合的范围
        String geoExtent = "";
        for (FeedItem feedItem : feedItemList) {
            if ("照片".equals(feedItem.Name)) {
                imgUrls = feedItem.Value;
                continue;
            }
            if ("geoExtent".equals(feedItem.Name)) {
                geoExtent = feedItem.Value;
                continue;
            }
            list.add(feedItem.Name);
            list.add(feedItem.Value);

        }

        try {

            JSONObject jsonObject = new JSONObject(geoExtent);
            JSONArray ringsArray = jsonObject.getJSONArray("rings");
            JSONArray dotArr = (JSONArray) ringsArray.get(0);
            dotArr.put(dotArr.get(0));

            JSONArray joParent = new JSONArray();
            joParent.put(dotArr);
            JSONObject geoExtentjo = new JSONObject();
            geoExtentjo.put("rings", joParent);

            geoExtent = geoExtentjo.toString();

            list.add("geoExtent");
            list.add(geoExtent);

        } catch (Exception ex) {
            ex.printStackTrace();
            MyApplication.getInstance().showMessageWithHandle(ex.getMessage());
            return null;
        }

        return NetUtil.executeHttpGet(url, list.toArray(new String[list.size()]));

    }

    private String reportImg(String ID) {

        if (TextUtils.isEmpty(imgUrls)) {
            return "success";
        }

        String reprotImgUrl = ServerConnectConfig.getInstance().getBaseServerPath() +
                "/services/zondy_mapgiscitysvr_audit/rest/auditrest.svc/UpLoad?ID=" + ID;

        try {
            String absoluteUrl = getFlowBeanFragment().getAbsolutePaths();
            for (String imgurl : absoluteUrl.split(",")) {

                final File filetemp = new File(imgurl);
                Map<String, File> files = new HashMap<String, File>();
                files.put("Filedata", BitmapUtil.convertBitmap2File(BitmapUtil.getBitmapFromFile(filetemp, 300, 500), filetemp));

                RequestBody requestBody = new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/octet-stream");
                    }

                    @Override
                    public void writeTo(BufferedSink bufferedSink) throws IOException {
                        Source source = null;
                        try {
                            source = Okio.source(filetemp);
                            bufferedSink.writeAll(source);
                        } finally {
                            Util.closeQuietly(source);
                        }
                    }
                };

                String imgReportRntStr = NetUtil.executeMultipartHttpPost(reprotImgUrl, null, files, "utf8", "application/octet-stream;", requestBody);

                if (TextUtils.isEmpty(imgReportRntStr)) {
                    throw new Exception("上报图片错误");
                }
                Rnt imgReportRnt = new Gson().fromJson(imgReportRntStr, Rnt.class);

                if (imgReportRnt == null) {
                    throw new Exception("上报图片错误");
                }
                if (!imgReportRnt.IsSuccess) {
                    throw new Exception(imgReportRnt.Msg);
                }
            }
        } catch (Exception ex) {

            MyApplication.getInstance().showMessageWithHandle(ex.getMessage());

            return null;
        }
        return "success";
    }
}