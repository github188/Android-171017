package com.repair.gisdatagather.enn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowCenterData;

/**
 * Created by liuyunfan on 2015/12/18.
 * <p>
 * 仅仅是过渡页面
 */
public class GISDataGatherActivity extends BaseActivity {
    GISDataGatherBottomBtnBar gisGatherData;
    FlowCenterData flowCenterData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

        if (intent != null) {
            flowCenterData = intent.getParcelableExtra("FlowCenterData");
        }

        if (flowCenterData != null) {
            init(flowCenterData);

            //缓存界面
            if (intent.getBooleanExtra("cacheFromServer", true)) {

                cacheView(flowCenterData);
            }
            return;
        }

        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/GetFlowCenterDataAppointEventName?eventName=GIS属性上报";

                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);

                ResultData<FlowCenterData> resultData = Utils.json2ResultDataToast(FlowCenterData.class, GISDataGatherActivity.this, s, "事件名错误", true);

                if (resultData != null) {
                    flowCenterData = resultData.getSingleData();
                }

                if (flowCenterData == null) {
                    flowCenterData = getGISAttrUpdatFlowConfig();
                }
                init(flowCenterData);

                //缓存界面
                if (intent.getBooleanExtra("cacheFromServer", true)) {

                    cacheView(flowCenterData);
                }
            }
        }.mmtExecute();

    }

    public void cacheView(final FlowCenterData flowCenterData) {
        MyApplication.getInstance().submitExecutorService(new Runnable() {
            @Override
            public void run() {

                try {
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetBizMetaData";

                    String result = NetUtil.executeHttpPost(url, new Gson().toJson(flowCenterData));

                    if (!TextUtils.isEmpty(result)) {
                        BaseClassUtil.getACache().put("baseInfo_view", result);
                    }

                    url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GisUpdate/GetGISDeviceConfigList";

                    result = NetUtil.executeHttpGet(url);

                    if (!TextUtils.isEmpty(result)) {
                        BaseClassUtil.getACache().put("layerInfo", result);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static FlowCenterData getGISAttrUpdatFlowConfig() {
        FlowCenterData flowCenterData = new FlowCenterData();
        flowCenterData.BizCode = "GIS";
        flowCenterData.BusinessType = "GIS属性上报";
        flowCenterData.EventName = "GIS属性上报";
        flowCenterData.FieldGroup = ",现场图片,";
        flowCenterData.FlowName = "GIS属性更新流程";
        flowCenterData.HandoverMode = "移交默认人";
        flowCenterData.TableName = "CIV_PATROL_ATT_REPORT";
        flowCenterData.OperType = "上报";
        flowCenterData.NodeName = "GIS属性上报";
        flowCenterData.IsCreate = 1;
        return flowCenterData;
    }

    public void init(final FlowCenterData flowCenterData) {

        Intent intent = new Intent(GISDataGatherActivity.this, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

        MapGISFrame mapGISFrame = MyApplication.getInstance().mapGISFrame;

        mapGISFrame.findViewById(R.id.mapviewClear).performClick();

        mapGISFrame.setCustomView(singleGatherCustomView(mapGISFrame));

        gisGatherData = new GISDataGatherBottomBtnBar(mapGISFrame, flowCenterData, getIntent().getBooleanExtra("isOnlyEdit", false));

        gisGatherData.init();

        mapGISFrame.findViewById(R.id.mapviewLocate).setVisibility(View.GONE);

    }

    public View singleGatherCustomView(final MapGISFrame mapGISFrame) {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.singledot_header_bar, null);

        String title = getIntent().getStringExtra("title");
        if (TextUtils.isEmpty(title)) {
            title = "GIS属性上报";
        }
        ((TextView) view.findViewById(R.id.tvTitle)).setText(title);

        mapGISFrame.setDefaultBackBtn(view.findViewById(R.id.tvBack));

        mapGISFrame.getDefaultBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gisGatherBack(mapGISFrame);
            }
        });

        view.setBackgroundResource(AppStyle.getActionBarStyleResource());

        return view;
    }

    @Override
    protected void onDestroy() {
        //关闭画管点管线线程
        if (gisGatherData != null && gisGatherData.thread != null) {
            gisGatherData.thread.setOpen(false);
        }
        super.onDestroy();
    }


    private void gisGatherBack(MapGISFrame mapGISFrame) {
        try {
            mapGISFrame.findViewById(R.id.mapviewLocate).setVisibility(View.VISIBLE);
            mapGISFrame.findViewById(R.id.mapviewClear).performClick();
            View centerView = mapGISFrame.getMapView().findViewWithTag("MapViewScreenView");
            if (centerView != null) {
                mapGISFrame.getMapView().removeView(centerView);
            }

            //关闭过渡的 GISDataGatherActivity
            AppManager.finishActivity();

            mapGISFrame.backByReorder();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
