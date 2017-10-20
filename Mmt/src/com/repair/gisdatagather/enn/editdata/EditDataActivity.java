package com.repair.gisdatagather.enn.editdata;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.common.widget.customview.ImageEditButtonView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.Poi;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.repair.gisdatagather.enn.GISDataGatherActivity;
import com.repair.gisdatagather.enn.bean.GISDataBean;
import com.repair.gisdatagather.enn.bean.GISDeviceSetBean;
import com.repair.zhoushan.entity.EventInfoPostParam;
import com.repair.zhoushan.entity.FlowCenterData;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.HandoverUserFragment;
import com.repair.zhoushan.module.eventreport.GetHandoverUsersTask;
import com.repair.zhoushan.module.eventreport.ZSEventReportActivity;
import com.simplecache.ACache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class EditDataActivity extends BaseActivity {
    Bundle bundle;
    FlowBeanFragment formBeanFragment;

    GISDataBean gisDataBean;
    GISDeviceSetBean gisDeviceSetBean;
    FlowCenterData flowCenterData;
    int isEdit = 0;
    EventInfoPostParam eventInfoPostParam = new EventInfoPostParam();

    //已有gis属性值
    HashMap<String, String> oldValues = new LinkedHashMap<>();
    //当前上报的gis属性值
    HashMap<String, String> newValues = new LinkedHashMap<>();

    List<String> editedAttrFileds = new ArrayList<>();

    List<FeedItem> feedbackItems = null;

    List<FlowNodeMeta.TableValue> gisDataBean_tvs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getIntent().getBundleExtra("bundle");

        gisDeviceSetBean = bundle.getParcelable("gisDeviceSetBean");
        gisDataBean = new Gson().fromJson(bundle.getString("gisDataBean"), GISDataBean.class);
        flowCenterData = bundle.getParcelable("FlowCenterData");
        isEdit = bundle.getInt("isEdit", 0);

        createView();
    }

    private void createView() {
        getBaseTextView().setText((isEdit == 1 ? "GIS属性编辑" : "GIS属性上报") + "(" + gisDataBean.LayerName + ")");

        new MmtBaseTask<Void, Void, GDFormBean>(this, true) {
            @Override
            protected GDFormBean doInBackground(Void... params) {
                try {

                    ACache aCache = BaseClassUtil.getACache();
                    String result = aCache.getAsString("baseInfo_view");
                    if (TextUtils.isEmpty(result)) {
                        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetBizMetaData";

                        result = NetUtil.executeHttpPost(url, new Gson().toJson(flowCenterData));
                        aCache.put("baseInfo_view", result);
                    }
                    if (BaseClassUtil.isNullOrEmptyString(result)) {
                        return null;
                    }
                    //基本信息
                    Results<FlowNodeMeta> rawData = new Gson().fromJson(result, new TypeToken<Results<FlowNodeMeta>>() {
                    }.getType());

                    if (rawData == null) {
                        MyApplication.getInstance().showMessageWithHandle("基本信息获取错误");
                        return null;
                    }
                    String result2 = aCache.getAsString(gisDeviceSetBean.table + "_view");
                    if (TextUtils.isEmpty(result2)) {
                        //获取设备属性的表架构
                        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMetaV2?tableName=" + gisDeviceSetBean.table;
                        result2 = NetUtil.executeHttpGet(url);
                        aCache.put(gisDeviceSetBean.table + "_view", result2);
                    }
                    if (BaseClassUtil.isNullOrEmptyString(result2)) {
                        MyApplication.getInstance().showMessageWithHandle("获取设备属性的表架构错误");
                        return null;
                    }
                    //GIS信息
                    Results<FlowNodeMeta> rawData2 = new Gson().fromJson(result2, new TypeToken<Results<FlowNodeMeta>>() {
                    }.getType());

                    if (rawData2 == null) {
                        MyApplication.getInstance().showMessageWithHandle("获取GIS信息错误");
                        return null;
                    }

                    if ("1002".equals(rawData2.say.statusCode)) {
                        MyApplication.getInstance().showMessageWithHandle(rawData2.say.errMsg);
                        return null;
                    }

                    OnlineLayerInfo.OnlineLayerAttribute[] fields = null;
                    if (isEdit == 1) {
                        String onlineLayerInfoStr = bundle.getString("onlineLayer");
                        if (BaseClassUtil.isNullOrEmptyString(onlineLayerInfoStr)) {
                            MyApplication.getInstance().showMessageWithHandle("获取GIS原有值错误");
                            return null;
                        }
                        OnlineLayerInfo onlineLayerInfo = new Gson().fromJson(onlineLayerInfoStr, OnlineLayerInfo.class);
                        if (onlineLayerInfo == null) {
                            MyApplication.getInstance().showMessageWithHandle("获取GIS原有值错误");
                            return null;
                        }

                        fields = onlineLayerInfo.fields;

                    }

                    //将两者拼接起来构造架构,并赋值
                    eventInfoPostParam.DataParam.flowNodeMeta = getFlowNodeMeta(rawData, rawData2, fields);

                    return eventInfoPostParam.DataParam.flowNodeMeta.mapToGDFormBean();

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onSuccess(GDFormBean gdFormBean) {
                super.onSuccess(gdFormBean);
                if (gdFormBean == null) {
                    Toast.makeText(context, "生成界面失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                createContentView(gdFormBean);
            }
        }.executeOnExecutor(MyApplication.executorService);

        createBottomView();
    }

    private FlowNodeMeta getFlowNodeMeta(Results<FlowNodeMeta> meta1, Results<FlowNodeMeta> meta2, OnlineLayerInfo.OnlineLayerAttribute[] fileds) {
        FlowNodeMeta flowNodeMeta = new FlowNodeMeta();
        FlowNodeMeta flowNodeMeta1 = meta1.getMe.get(0);
        FlowNodeMeta flowNodeMeta2 = meta2.getMe.get(0);
        flowNodeMeta.Groups.addAll(flowNodeMeta1.Groups);
        flowNodeMeta.Values.addAll(flowNodeMeta1.Values);

        List<FlowNodeMeta.TableGroup> gisgroups = getFilterGroupOnlyByConfig(flowNodeMeta2.Groups);

        //将值赋到架构上
        if (fileds != null && fileds.length > 0) {
            setValue2Schme(gisgroups, fileds);
        }

        //添加坐标信息组
        if (gisDataBean.GeomType.equals("管点")) {
            gisgroups = addPositionGroup(gisgroups);
        }
        flowNodeMeta.Groups.addAll(gisgroups);

        return flowNodeMeta;
    }

    public void setValue2Schme(List<FlowNodeMeta.TableGroup> groupList, OnlineLayerInfo.OnlineLayerAttribute[] fileds) {
        FlowNodeMeta.TableGroup group = groupList.get(0);
        for (FlowNodeMeta.FieldSchema schema : group.Schema) {
            for (OnlineLayerInfo.OnlineLayerAttribute filed : fileds) {
                if (schema.FieldName.equals(filed.name) || schema.FieldName.equals(filed.alias) || schema.Alias.equals(filed.name) || schema.Alias.equals(filed.alias)) {
                    filed.DefVal = GisDataGatherUtils.getRightGisVal(filed.DefVal);
                    schema.PresetValue = filed.DefVal;
                    if (!oldValues.containsKey(filed.name)) {
                        oldValues.put(filed.name, filed.DefVal);
                    }
                    break;
                }
            }
        }
    }

    public List<FlowNodeMeta.TableGroup> addPositionGroup(List<FlowNodeMeta.TableGroup> gisgroups) {
        FlowNodeMeta flowNodeMeta = new FlowNodeMeta();
        FlowNodeMeta.TableGroup newgroup = flowNodeMeta.new TableGroup();
        newgroup.Visible = 1;
        newgroup.GroupName = "坐标信息";

        boolean editNewGeom = true;
        if (isEdit == 1 && MyApplication.getInstance().getConfigValue("GISEditXY", 1) == 0) {
            editNewGeom = false;
        }
        if (editNewGeom) {
            FlowNodeMeta.FieldSchema filedSchema2 = new FlowNodeMeta().new FieldSchema();
            filedSchema2.Shape = "坐标控件V3";
            filedSchema2.FieldName = "NewGeom";
            filedSchema2.Alias = "新坐标";
            filedSchema2.PresetValue = gisDataBean.NewGeom.length() == 0 ? " " : gisDataBean.NewGeom;
            filedSchema2.Visible = 1;
            filedSchema2.ReadOnly = 0;
            newgroup.Schema.add(filedSchema2);
        }
        if (isEdit == 1) {
            FlowNodeMeta.FieldSchema filedSchema = new FlowNodeMeta().new FieldSchema();
            filedSchema.Shape = "坐标控件";
            filedSchema.FieldName = "OldGeom";
            filedSchema.Alias = "原坐标";
            filedSchema.PresetValue = gisDataBean.OldGeom.length() == 0 ? " " : gisDataBean.OldGeom;
            filedSchema.Visible = 1;
            filedSchema.ReadOnly = 1;
            newgroup.Schema.add(filedSchema);
        }

        gisgroups.add(newgroup);

        return gisgroups;

    }

    public List<FlowNodeMeta.TableGroup> getFilterGroupOnlyByConfig(List<FlowNodeMeta.TableGroup> gisgroups) {
        FlowNodeMeta flowNodeMeta = new FlowNodeMeta();
        List<FlowNodeMeta.TableGroup> newgisgroups = new ArrayList<>();

        String fliters = gisDeviceSetBean.fileds;

        for (FlowNodeMeta.TableGroup group : gisgroups) {
            FlowNodeMeta.TableGroup newgroup = flowNodeMeta.new TableGroup();
            for (FlowNodeMeta.FieldSchema filedSchema : group.Schema) {
                //字段集过滤，只判断别名|| fliters.contains(filedSchema.FieldName)
                if (fliters.contains(filedSchema.Alias)) {
                    newgroup.Schema.add(filedSchema);
                }
            }
            newgroup.GroupName = group.GroupName;
            newgroup.Visible = group.Visible;
            newgisgroups.add(newgroup);
        }
        return newgisgroups;
    }

    private void createContentView(GDFormBean formBean) {
        formBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", formBean);
        formBeanFragment.setArguments(args);

        formBeanFragment.setCls(ZSEventReportActivity.class);
        formBeanFragment.setAddEnable(true);
        formBeanFragment.setFragmentFileRelativePath(flowCenterData.BizCode);

        addFragment(formBeanFragment);

        formBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
            @Override
            public void onCreated() {
                linkPositionAndAddress();
            }
        });
    }

    private void createBottomView() {
        clearAllBottomUnitView();

        addReportBottomView();

        if (isEdit == 1 && "管点".equals(gisDataBean.GeomType)) {
            addDeleteBottomView();
        }
    }

    private void addReportBottomView() {
        addBottomUnitView(BottomUnitView.create(this, BottomUnitView.EditMode.Report)
                , new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reportGisData();
                    }
                });
    }

    private void reportGisData() {
        if (formBeanFragment == null) {
            return;
        }
        feedbackItems = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

        if (feedbackItems == null) {
            Toast.makeText(EditDataActivity.this, "未知异常!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isChange()) {
            startSubmit();
            return;
        }

        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment(isEdit == 0 ? "属性未新增,是否继续？" : "属性未编辑,是否继续？");
        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {
                startSubmit();
            }
        });
        okCancelDialogFragment.show(getSupportFragmentManager(), "");
    }

    private void addDeleteBottomView() {
        addBottomUnitView(BottomUnitView.create(this, BottomUnitView.EditMode.Delete)
                , new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteGisData();
                    }
                });
    }

    /*删除GIS属性数据*/
    private void deleteGisData() {
        gisDataBean.Operation = "删除";
        reportEvent();
    }

    @Override
    public void onCustomBack() {

        reportSuccess(this);
    }

    public boolean isChange() {
        newValues.clear();
        editedAttrFileds.clear();
        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = eventInfoPostParam.DataParam.flowNodeMeta.Values;

        for (FeedItem item : feedbackItems) {
            boolean isGisData = true;
            for (FlowNodeMeta.TableValue value : values) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                    isGisData = false;
                    break;
                }
            }
            if (item.Name.equals("NewGeom")) {
                if (item.Value.trim().length() == 0) {
                    gisDataBean.NewGeom = "";
                } else {
                    gisDataBean.NewGeom = item.Value;
                }
            }
            if (isGisData && !item.Name.equals("NewGeom") && !item.Name.equals("OldGeom")) {
                if (newValues.containsKey(item.Name)) {
                    continue;
                }
                if (!oldValues.containsKey(item.Name) && item.Value.trim().equals("")) {
                    continue;
                }
                item.Value = GisDataGatherUtils.getRightGisVal(item.Value);
                newValues.put(item.Name, item.Value);

                if (!oldValues.containsKey(item.Name)) {
                    oldValues.put(item.Name, "");
                    editedAttrFileds.add(item.Name);
                    continue;
                }

                if (!item.Value.equals(oldValues.get(item.Name))) {
                    editedAttrFileds.add(item.Name);
                }

            }
        }

        return editedAttrFileds.size() > 0;
    }

    public void startSubmit() {

        //数据必需性判断
        if (!isDataValidated()) {
            return;
        }

        //精度比对
        if (!isRightPosition()) {
            OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("采集误差较大,是否继续？");
            okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    submit();
                }
            });
            okCancelDialogFragment.show(getSupportFragmentManager(), "");

            return;
        }
        submit();
    }

    private boolean isDataValidated() {

        if (isEdit == 1) {
            //编辑 旧坐标不能为空
            if (TextUtils.isEmpty(gisDataBean.OldGeom)) {
                Toast.makeText(EditDataActivity.this, "编辑模式老坐标不能为空!", Toast.LENGTH_SHORT).show();
                return false;
            }
//
//
//            View positionView = formBeanFragment.findViewByName("OldGeom");
//
//            if (!(positionView instanceof ImageDotView)) {
//                Toast.makeText(EditDataActivity.this, "OldGeom类型异常!", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//
//            ImageDotView dv = (ImageDotView) positionView;
//
//            String reportPos = dv.getValue();
//
//            if (TextUtils.isEmpty(reportPos)) {
//                Toast.makeText(EditDataActivity.this, "编辑模式老坐标不能为空!", Toast.LENGTH_SHORT).show();
//                return false;
//            }
        } else {
            //新增，新坐标不能为空
            View positionView = formBeanFragment.findViewByName("NewGeom");

            if (!(positionView instanceof ImageDotView)) {
                Toast.makeText(EditDataActivity.this, "NewGeom类型异常!", Toast.LENGTH_SHORT).show();
                return false;
            }

            ImageDotView dv = (ImageDotView) positionView;

            String reportPos = dv.getValue();

            if (TextUtils.isEmpty(reportPos)) {
                Toast.makeText(EditDataActivity.this, "新增模式新坐标不能为空!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    public void submit() {

        if (flowCenterData.IsCreate == 1) {
            final int TO_DEFAULT_NEXT = 2;

            switch (flowCenterData.HandoverMode) {
                case "移交选择人": {
                    String stationName = "";
                    for (FeedItem item : feedbackItems) {
                        if (item.Name.equals("站点名称")) {
                            stationName = item.Value;
                            break;
                        }
                    }

                    new GetHandoverUsersTask(EditDataActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<Node>() {
                        @Override
                        public void doAfter(Node node) {
                            if (node == null) {
                                Toast.makeText(EditDataActivity.this, "获取上报人员失败", Toast.LENGTH_SHORT).show();
                            } else {
                                HandoverUserFragment fragment
                                        = new HandoverUserFragment(new SelectUserHandler(), TO_DEFAULT_NEXT, node);
                                fragment.setIsContainNodeId(false);
                                fragment.show(EditDataActivity.this.getSupportFragmentManager(), "");
                            }
                        }
                    }).executeOnExecutor(MyApplication.executorService, flowCenterData.FlowName, MyApplication.getInstance().getUserId() + "", stationName);

                }
                break;
                case "移交默认人": {
                    eventInfoPostParam.DataParam.caseInfo.Undertakeman = "";
                    reportEvent();
                }
                break;
                case "跨站移交": {
                    new GetHandoverUsersTask(EditDataActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<Node>() {
                        @Override
                        public void doAfter(Node node) {
                            if (node == null) {
                                Toast.makeText(EditDataActivity.this, "获取上报人员失败", Toast.LENGTH_SHORT).show();
                            } else {
                                HandoverUserFragment fragment
                                        = new HandoverUserFragment(new SelectUserHandler(), TO_DEFAULT_NEXT, node);
                                fragment.setIsContainNodeId(false);
                                fragment.show(EditDataActivity.this.getSupportFragmentManager(), "");
                            }
                        }
                    }).executeOnExecutor(MyApplication.executorService, flowCenterData.FlowName, MyApplication.getInstance().getUserId() + "", "");

                }
                break;
                case "自处理": {
                    eventInfoPostParam.DataParam.caseInfo.Undertakeman = MyApplication.getInstance().getUserId() + "";
                    reportEvent();
                }
                break;

            }

        } else {
            reportEvent();
        }
    }

    private void reportEvent() {

        eventInfoPostParam.DataParam.caseInfo.FlowName = flowCenterData.FlowName;
        eventInfoPostParam.DataParam.caseInfo.NodeName = flowCenterData.NodeName;
        eventInfoPostParam.DataParam.caseInfo.TableGroup = "";
        eventInfoPostParam.DataParam.caseInfo.UserID = MyApplication.getInstance().getUserId();
        eventInfoPostParam.DataParam.caseInfo.IsCreate = flowCenterData.IsCreate;
        eventInfoPostParam.DataParam.caseInfo.BizCode = flowCenterData.BizCode;
        eventInfoPostParam.DataParam.caseInfo.EventMainTable = flowCenterData.TableName;
        eventInfoPostParam.DataParam.caseInfo.EventName = flowCenterData.EventName;
        eventInfoPostParam.DataParam.caseInfo.UpdateEvent = flowCenterData.HandoverMode;

        eventInfoPostParam.BizCode = flowCenterData.BizCode;
        eventInfoPostParam.EventName = flowCenterData.EventName;
        eventInfoPostParam.TableName = flowCenterData.TableName;

        List<String> newAttrs = new ArrayList<>();
        List<String> oldAttrs = new ArrayList<>();

        for (String key : editedAttrFileds) {
            newAttrs.add(key + ":" + newValues.get(key));
            oldAttrs.add(key + ":" + oldValues.get(key));
        }

        gisDataBean.NewAtt = GisDataGatherUtils.gisKVs2Str(newAttrs);
        gisDataBean.OldAtt = GisDataGatherUtils.gisKVs2Str(oldAttrs);

        //将gisDataBean 赋给group
        FlowNodeMeta.TableGroup Gisgroup = eventInfoPostParam.DataParam.flowNodeMeta.Groups.get(eventInfoPostParam.DataParam.flowNodeMeta.Groups.size() - 1);
        Gisgroup.Schema.clear();
        gisDataBean_tvs = gisDataBean.convert2TableValue();
        eventInfoPostParam.DataParam.flowNodeMeta.Values.addAll(gisDataBean_tvs);

        // 创建服务路径
        String uri;
        String reportData = "";
        if (flowCenterData.IsCreate == 1) {
            // 创建流程
            //移交
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostFlowNodeData";

            reportData = new Gson().toJson(eventInfoPostParam.DataParam, new TypeToken<FlowInfoPostParam>() {
            }.getType());
        } else {
            // 不创建流程
            //只上报
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostEventData";
            // 将信息转换为JSON字符串
            reportData = new Gson().toJson(eventInfoPostParam, new TypeToken<EventInfoPostParam>() {
            }.getType());
        }

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                flowCenterData.NodeName,
                formBeanFragment.getAbsolutePaths(),
                formBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode > 0) {

                        OkDialogFragment fragment = new OkDialogFragment("GIS属性上报成功！");
                        fragment.setOnButtonClickListener(new OkDialogFragment.OnButtonClickListener() {
                            @Override
                            public void onButtonClick(View view) {
                                reportSuccess(EditDataActivity.this);
                            }
                        });
                        fragment.setCancelable(false);
                        fragment.show(getSupportFragmentManager(), "");

                    } else {
                        Toast.makeText(EditDataActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                        if (gisDataBean_tvs != null) {
                            eventInfoPostParam.DataParam.flowNodeMeta.Values.removeAll(gisDataBean_tvs);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }

    public boolean isRightPosition() {

        if (formBeanFragment == null) {
            return false;
        }


        View positionView = formBeanFragment.findViewByName("NewGeom");

        if (!(positionView instanceof ImageDotView)) {
            return false;
        }

        ImageDotView dv = (ImageDotView) positionView;

        String reportPos = dv.getValue();

        return GisDataGatherUtils.isUsefullPosition(reportPos);
    }

    public void reportSuccess(BaseActivity baseActivity) {
        AppManager.finishActivity(baseActivity);
        MyApplication.getInstance().finishActivityAnimation(baseActivity);

        //在上报页面选点后，地图被清空，重新采集时需重新构造
        //关闭过渡的 GISDataGatherActivity
        AppManager.finishActivity();

        Intent intent = new Intent(baseActivity, GISDataGatherActivity.class);
        intent.putExtra("isOnlyEdit", bundle.getBoolean("isOnlyEdit", false));
        intent.putExtra("cacheFromServer", false);
        startActivity(intent);

        eventInfoPostParam = null;
        gisDataBean = null;
        flowCenterData = null;
        editedAttrFileds.clear();
        editedAttrFileds = null;
        newValues.clear();
        newValues = null;
        oldValues.clear();
        oldValues = null;
        formBeanFragment = null;
    }

    public void linkPositionAndAddress() {

        try {

            View viewAddr = formBeanFragment.findViewByType("百度地址");
            if (!(viewAddr instanceof ImageEditButtonView)) {
                return;
            }

            View viewPosition = formBeanFragment.findViewByName("NewGeom");

            if (!(viewPosition instanceof ImageDotView)) {
                return;
            }

            String positionStr = ((ImageDotView) viewPosition).getValue();

            final ImageEditButtonView addressView = (ImageEditButtonView) viewAddr;

            if (TextUtils.isEmpty(positionStr)) {

                addressView.setValue("");
                addressView.button.setTag(new ArrayList<>());

                positionStr = "";
            }

            String[] xy = positionStr.split(",");

            if (xy.length != 2) {
                addressView.setValue("");
                addressView.button.setTag(new ArrayList<>());
            }

            if (!BaseClassUtil.isNum(xy[0]) || !BaseClassUtil.isNum(xy[1])) {
                addressView.setValue("");
                addressView.button.setTag(new ArrayList<>());
            }

            float x = Float.valueOf(xy[0]);
            float y = Float.valueOf(xy[1]);
            GpsXYZ gpsXYZ = new GpsXYZ(x, y);
            final Location location = GpsReceiver.getInstance().getLastLocationConverse(gpsXYZ);

            new MmtBaseTask<Void, Void, BDGeocoderResult>(this) {
                @Override
                protected BDGeocoderResult doInBackground(Void... params) {
                    return BDGeocoder.find(location);
                }

                @Override
                protected void onSuccess(BDGeocoderResult bdResult) {
                    super.onSuccess(bdResult);
                    List<String> values = new ArrayList<>();

                    for (Poi poi : bdResult.result.pois) {
                        values.add(poi.addr + poi.name);
                    }

                    if (values.size() == 0) {
                        return;
                    }

                    addressView.setValue(values.get(0));

                    addressView.button.setTag(values);
                }
            }.mmtExecute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    class SelectUserHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 2:
                        eventInfoPostParam.DataParam.caseInfo.Undertakeman = msg.getData().getString("undertakeman");
                        eventInfoPostParam.DataParam.caseInfo.Opinion = msg.getData().getString("option");
                        reportEvent();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
