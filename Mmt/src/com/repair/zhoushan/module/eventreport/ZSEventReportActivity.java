package com.repair.zhoushan.module.eventreport;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.customview.FeedBackView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.common.widget.customview.ImageEditButtonView;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.Poi;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.patrol.module.feedback.PlanFeedbackFragment;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.EventInfoPostParam;
import com.repair.zhoushan.entity.EventLinkDevice;
import com.repair.zhoushan.entity.FlowCenterData;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.HandoverUserFragment;
import com.mapgis.mmt.global.OnResultListener;
import com.repair.zhoushan.module.QRCodeResolver;
import com.repair.zhoushan.module.eventreport.qrcodescan.ZSQRCodeResolver;
import com.zbar.lib.CaptureActivity;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ZSEventReportActivity extends BaseActivity {

    public static final String FLAG_FROM_NAVIGATION_MENU = "FromNavigation";

    /**
     * 获取工单详情
     */
    public final int GET_DETAIL_FORM = 0;
    /**
     * 获取表单信息
     */
    public final int GET_DETAIL_SUCCESS = 1;
    /**
     * 移交给默认承办人
     */
    public static final int TO_DEFAULT_NEXT = 2;
    /**
     * 自处理
     */
    public static final int TO_SELF_NEXT = 3;

    // 二维码扫描
    public static final int RC_QRCODE_SCAN = 4;
    private String mQRCodeScanConfig;
    private QRCodeResolver<Map<String, String>> mQRCodeResolver;

    protected FlowBeanFragment formBeanFragment;

    private FlowCenterData mFlowCenterData = null;
    private List<FeedItem> feedbackItems = null;

    // 标志是否创建流程
    private boolean isCreateWorkFlow;

    // 上报信息，两种：流程上报、事件上报
    private FlowInfoPostParam flowInfoPostParam = new FlowInfoPostParam();
    public final EventInfoPostParam eventInfoPostParam = new EventInfoPostParam();

    private int userId;
    private boolean isSuccess = false;

    private Intent outerIntent;
    // 标志是从主界面导航到流程中心，只有这种情况才需要缓存数据
    private boolean isFromNavigation;

    private TextView tvTopReport;
    private BottomUnitView manageUnitView;
    Bundle gisInfoBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            // 底部"上报"按钮不被输入法弹起，防止用户误操作
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            userId = MyApplication.getInstance().getUserId();
            this.mQRCodeScanConfig = MyApplication.getInstance().getConfigValue("QRCodeScanInReport");

            outerIntent = getIntent();
            mFlowCenterData = outerIntent.getParcelableExtra("FlowCenterData");
            this.isFromNavigation = outerIntent.getBooleanExtra(FLAG_FROM_NAVIGATION_MENU, false);

            getBaseTextView().setText(mFlowCenterData.EventName);

            setCustomView(getTopView());

            handler.sendEmptyMessage(GET_DETAIL_FORM);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private View getTopView() {

        final View view = LayoutInflater.from(this).inflate(R.layout.head_eventrport, null);

        addBackBtnListener(view.findViewById(R.id.btnBack));

        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        txtTitle.setMaxWidth(400);
        txtTitle.setEllipsize(TextUtils.TruncateAt.END);
        txtTitle.setText(mFlowCenterData.EventName);

        this.tvTopReport = (TextView) view.findViewById(R.id.txtChangeType);
        if (!TextUtils.isEmpty(mQRCodeScanConfig)) {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_scan_qrcode_24dp);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tvTopReport.setCompoundDrawables(drawable, null, null, null);
            tvTopReport.setText("");
            tvTopReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ZSEventReportActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, RC_QRCODE_SCAN);
                }
            });
        } else {
            tvTopReport.setText("上报");
            tvTopReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (manageUnitView != null) {
                        manageUnitView.performClick();
                    }
                }
            });
        }

        return view;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == RC_QRCODE_SCAN) {
            String code = data.getExtras().getString("code");
            resolveQRCode(code);
        }
    }

    private void resolveQRCode(String code) {

        // ZS:tableName;where;columns
        String[] config = mQRCodeScanConfig.split(":");

        switch (config[0]) {
            case "ZS":
                if (mQRCodeResolver == null) {
                    String configInfo = config.length > 1 ? config[1] : "";
                    mQRCodeResolver = new ZSQRCodeResolver(ZSEventReportActivity.this, configInfo);
                }
                mQRCodeResolver.resolve(code, new OnResultListener<Map<String, String>>() {
                    @Override
                    public void onSuccess(Map<String, String> stringStringMap) {
                        formBeanFragment.updateFormData(stringStringMap);
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        Toast.makeText(ZSEventReportActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            default:
                Toast.makeText(ZSEventReportActivity.this, "解析器配置错误", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 获取详情配置表单信息
     */
    protected void getDetailForm() {
        new FetchFormBeanTask(ZSEventReportActivity.this, handler, GET_DETAIL_SUCCESS)
                .executeOnExecutor(MyApplication.executorService, new Gson().toJson(this.mFlowCenterData));
    }

    protected void createView(final GDFormBean formBean) {

        formBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", formBean);
        if (isFromNavigation) {
            args.putString("CacheSearchParam", ("userId=" + userId + " and key='" + mFlowCenterData.BusinessType + mFlowCenterData.EventName + "'"));
        }
        formBeanFragment.setArguments(args);
        formBeanFragment.setCls(ZSEventReportActivity.class);
        formBeanFragment.setAddEnable(true);

        formBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
            @Override
            public void onCreated() {
                try {
                    gisSpecialHand();
                    otherSpecialHand();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        addFragment(formBeanFragment);

        createBottomView();

    }

    //对时间特殊处理：
    //页面没有区分编辑和浏览，只对单个控件进行了区分，照成配置时间空间，指定为只读，且为上报界面时，时间字段默认为空
    private void otherSpecialHand() {

        View view = formBeanFragment.findViewByType("时间");
        if (view instanceof ImageTextView) {
            ImageTextView imageTextView = (ImageTextView) view;
            String val = TextUtils.isEmpty(imageTextView.getValue()) ? BaseClassUtil.getSystemTime() : imageTextView.getValue();
            imageTextView.setValue(val);
        }
        view = formBeanFragment.findViewByType("仅时间");
        if (view instanceof ImageTextView) {
            ImageTextView imageTextView = (ImageTextView) view;
            String val = TextUtils.isEmpty(imageTextView.getValue()) ? BaseClassUtil.getSystemTimePart() : imageTextView.getValue();
            imageTextView.setValue(val);
        }
        view = formBeanFragment.findViewByType("仅日期");
        if (view instanceof ImageTextView) {
            ImageTextView imageTextView = (ImageTextView) view;
            String val = TextUtils.isEmpty(imageTextView.getValue()) ? BaseClassUtil.getSystemDate() : imageTextView.getValue();
            imageTextView.setValue(val);

        }
        view = formBeanFragment.findViewByType("日期框");
        if (view instanceof ImageTextView) {
            ImageTextView imageTextView = (ImageTextView) view;
            String val = TextUtils.isEmpty(imageTextView.getValue()) ? BaseClassUtil.getSystemTime() : imageTextView.getValue();
            imageTextView.setValue(val);
        }

    }

    private void gisSpecialHand() {
        gisInfoBundle = outerIntent.getBundleExtra("gisInfo");
        if (gisInfoBundle == null)
            return;

        // 从巡检跳过来的处理，巡检上报需要有巡检编号
        if (gisInfoBundle.containsKey("patrolNo")) {
            String patrolNo = gisInfoBundle.getString("patrolNo");
            if (TextUtils.isEmpty(patrolNo) || Integer.valueOf(patrolNo) <= 0)
                return;
        }

        String position = gisInfoBundle.getString("position"); // 坐标
        String addr = gisInfoBundle.getString("addr"); // 地址
        String gisLayer = gisInfoBundle.getString("layerName"); // 图层名称
        String gisCode = gisInfoBundle.getString("filedVal"); // GIS编号

        // GIS编号
        View gisCodeView = formBeanFragment.findViewByName("GIS编号");
        if (gisCodeView instanceof ImageTextView) {
            ImageTextView gisCodeitv = (ImageTextView) gisCodeView;
            gisCodeitv.setValue(gisCode);
        } else if (gisCodeView instanceof ImageEditView) {
            ImageEditView gisCodeiev = (ImageEditView) gisCodeView;
            gisCodeiev.setValue(gisCode);
        }

        // GIS图层
        View gisLayerView = formBeanFragment.findViewByName("GIS图层");
        if (gisLayerView instanceof ImageTextView) {
            ImageTextView gisLayeritv = (ImageTextView) gisLayerView;
            gisLayeritv.setValue(gisLayer);
        } else if (gisLayerView instanceof ImageEditView) {
            ImageEditView gisLayeriev = (ImageEditView) gisLayerView;
            gisLayeriev.setValue(gisLayer);
        }

        //region 坐标&地址
        if (BaseClassUtil.isNullOrEmptyString(position)) {
            return;
        }
        String[] xy = position.split(",");
        if (Double.valueOf(xy[0]) == 0 && Double.valueOf(xy[1]) == 0) {
            return;
        }

        // 设备选择
        View deviceSelectView = formBeanFragment.findViewByType("设备选择");
        if (deviceSelectView instanceof FeedBackView) {
            ((FeedBackView) deviceSelectView).setValue(position);
        }
        if (deviceSelectView instanceof ImageButtonView) {
            ((ImageButtonView) deviceSelectView).getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyApplication.getInstance().showMessageWithHandle("不允许更改坐标");
                }
            });
        }

        // 坐标
        ImageDotView coordView = (ImageDotView) formBeanFragment.findViewByType("坐标");
        if (coordView == null) {
            return;
        }
        coordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getInstance().showMessageWithHandle("不允许更改坐标");
            }
        });
        coordView.getValueEditView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getInstance().showMessageWithHandle("不允许更改坐标");
            }
        });
        coordView.setValue(position);

        // 地址
        final View addressView = formBeanFragment.findViewByType("百度地址");
        if (!(addressView instanceof ImageEditButtonView)) {
            return;
        }
        if (!TextUtils.isEmpty(addr)) {
            ((ImageEditButtonView) addressView).setValue(addr);
            return;
        }

        final Location location = new Location("BDPlaceSearch");
        location.setLongitude(Double.valueOf(xy[0]));
        location.setLatitude(Double.valueOf(xy[1]));

        new MmtBaseTask<Location, Integer, BDGeocoderResult>(ZSEventReportActivity.this, false,
                new MmtBaseTask.OnWxyhTaskListener<BDGeocoderResult>() {
                    @Override
                    public void doAfter(BDGeocoderResult bdResult) {
                        try {

                            ImageEditButtonView addrView = (ImageEditButtonView) addressView;

                            String address = bdResult.result.addressComponent.district
                                    + bdResult.result.addressComponent.street
                                    + bdResult.result.addressComponent.street_number;

                            ArrayList<String> addrNameList = new ArrayList<>();
                            for (Poi poi : bdResult.result.pois) {
                                addrNameList.add(address + "-" + poi.name);
                            }

                            if (addrNameList.size() == 0) {
                                addrNameList.add("未获取到地址，点击重新获取");
                            } else {
                                addrView.setValue(addrNameList.get(0));
                            }
                            // 暂存可选地址列表数据
                            addrView.button.setTag(addrNameList);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }) {
            @Override
            protected BDGeocoderResult doInBackground(Location... params) {
                return BDGeocoder.find(params[0]);
            }
        }.mmtExecute(location);
    }

    protected Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {

                    case GET_DETAIL_FORM:
                        getDetailForm();
                        break;

                    case GET_DETAIL_SUCCESS:

                        // 将Fragment显示在界面上
                        flowInfoPostParam.flowNodeMeta = (FlowNodeMeta) msg.obj;

                        resolveFormData();

                        createView(flowInfoPostParam.flowNodeMeta.mapToGDFormBean());
                        break;

                    case TO_DEFAULT_NEXT:
                        flowInfoPostParam.caseInfo.Undertakeman = msg.getData().getString("undertakeman");
                        flowInfoPostParam.caseInfo.Opinion = msg.getData().getString("option");

                        reportEvent();
                        break;

                    case TO_SELF_NEXT:
                        flowInfoPostParam.caseInfo.Undertakeman = String.valueOf(MyApplication.getInstance().getUserId());
                        flowInfoPostParam.caseInfo.Opinion = "";

                        reportEvent();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void resolveFormData() {
        try {

            if ("隐患上报".equals(mFlowCenterData.EventName)) {

                // 工商户相关、设备养护相关，可以直接跳转到上报界面进行隐患上报，会携带一些设备数据
                if (outerIntent.hasExtra("TaskCode")) {
                    String userName = outerIntent.getStringExtra("UserName");
                    String userTel = outerIntent.getStringExtra("UserTel");
                    String gasLocation = outerIntent.getStringExtra("GasLocation");

                    String gisNo = outerIntent.getStringExtra("GisNo");
                    String gisLayer = outerIntent.getStringExtra("GisLayer");
                    String coordinate = outerIntent.getStringExtra("Coordinate");

                    for (FlowNodeMeta.TableValue tableValue : flowInfoPostParam.flowNodeMeta.Values) {

                        switch (tableValue.FieldName) {
                            case "用户名称":
                                tableValue.FieldValue = userName;
                                break;
                            case "联系电话":
                                tableValue.FieldValue = userTel;
                                break;
                            case "用气地址":
                                tableValue.FieldValue = gasLocation;
                                break;
                            case "GIS编号":
                                tableValue.FieldValue = gisNo;
                                break;
                            case "GIS图层":
                                tableValue.FieldValue = gisLayer;
                                break;
                            case "坐标位置":
                                tableValue.FieldValue = coordinate;
                                break;
                        }
                    }
                }
            } else if ("隐患事件".equals(mFlowCenterData.EventName)) {
                // 绍兴的特殊需求：选择器web端与mobile端默认选中项不一样
                for (FlowNodeMeta.TableGroup tableGroup : flowInfoPostParam.flowNodeMeta.Groups) {
                    for (FlowNodeMeta.FieldSchema fieldShema : tableGroup.Schema) {
                        if ("发现方式".equals(fieldShema.FieldName)) {

                            if (!TextUtils.isEmpty(fieldShema.ConfigInfo)) {
                                String items = fieldShema.ConfigInfo;

                                int index = items.indexOf("主动");
                                if (index > 0 && (',' == items.charAt(index - 1))) {
                                    fieldShema.ConfigInfo = "主动," + items.replaceFirst(",主动", "");
                                }
                            }
                        } else if ("事件类型".equals(fieldShema.FieldName)) {

                            if (!TextUtils.isEmpty(fieldShema.ConfigInfo)) {
                                String items = fieldShema.ConfigInfo;

                                int index = items.indexOf("隐患点");
                                if (index > 0 && (',' == items.charAt(index - 1))) {
                                    fieldShema.ConfigInfo = "隐患点," + items.replaceFirst(",隐患点", "");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void eventLinkToTask(final int eventId) {

        final String taskCode = outerIntent.getStringExtra("TaskCode");
        final String bizType = outerIntent.getStringExtra("BizType");

        MmtBaseTask<Void, Void, String> mmtBaseTask = new MmtBaseTask<Void, Void, String>(ZSEventReportActivity.this) {
            @Override
            protected String doInBackground(Void... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/RelateEventToTask?bizType="
                        + bizType + "&eventName=隐患上报&eventID=" + eventId + "&taskCode=" + taskCode;
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String jsonResult) {
                if (Utils.json2ResultToast(ZSEventReportActivity.this, jsonResult, "事件关联任务失败")) {
                    Toast.makeText(ZSEventReportActivity.this, "事件关联任务成功", Toast.LENGTH_SHORT).show();
                }

                success(null);
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    @Override
    protected void onStop() {
        clearGisInfo();
        super.onStop();
    }

    private void clearGisInfo() {
        if (outerIntent.hasExtra("gisInfo")) {
            outerIntent.removeExtra("gisInfo");
        }
        if (gisInfoBundle != null) {
            gisInfoBundle = null;
        }
    }

    private void reportEvent() {

        if (formBeanFragment == null) {
            return;
        }

        if (feedbackItems == null) {
            feedbackItems = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
            if (feedbackItems == null) {
                return;
            }
        }

        // Flow Report
        flowInfoPostParam.caseInfo.FlowName = mFlowCenterData.FlowName;
        flowInfoPostParam.caseInfo.NodeName = mFlowCenterData.NodeName;
        flowInfoPostParam.caseInfo.IsCreate = mFlowCenterData.IsCreate;
        flowInfoPostParam.caseInfo.EventName = mFlowCenterData.EventName;
        flowInfoPostParam.caseInfo.FieldGroup = mFlowCenterData.FieldGroup;
        flowInfoPostParam.caseInfo.BizCode = mFlowCenterData.BizCode;
        flowInfoPostParam.caseInfo.EventMainTable = mFlowCenterData.TableName;
        flowInfoPostParam.caseInfo.TableGroup = "";
        flowInfoPostParam.caseInfo.UserID = userId;

        // 坐标控件与当前位置的距离提示
        StringBuilder distanceTipSP = new StringBuilder();
        Dot currentLocDot = null;
        GpsXYZ gpsXYZ = GpsReceiver.getInstance().getLastLocalLocation();
        if (gpsXYZ.isUsefull()) {
            currentLocDot = gpsXYZ.convertToPoint();
        }

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = flowInfoPostParam.flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {

                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;

                    if (currentLocDot != null) {
                        if ("坐标V2当前坐标坐标V3".contains(item.Type) && !TextUtils.isEmpty(item.Value)) {
                            Dot dot = GisUtil.convertDot(item.Value);
                            if (dot != null) {
                                double distance = GisUtil.calcDistance(currentLocDot, dot);
                                if (distance > 0) {
                                    distanceTipSP.append(item.Name).append(":").append(String.valueOf((int) distance)).append("米；");
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        if (distanceTipSP.length() == 0) {
            report();
        } else {
            distanceTipSP.insert(0, "坐标与当前位置的距离：\n").append("\n是否确认上报？");
            new AlertDialog.Builder(ZSEventReportActivity.this, R.style.MmtBaseThemeAlertDialog)
                    .setTitle("提示")
                    .setMessage(distanceTipSP.toString())
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            report();
                        }
                    }).show();
        }
    }

    private void report() {
        // 创建服务路径、将信息转换为JSON字符串
        String uri;
        String reportData;

        // 创建流程
        // if (mFlowCenterData.IsCreate >= 1) {
        if (isCreateWorkFlow) {

            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostFlowNodeData";

            reportData = new Gson().toJson(flowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
            }.getType());

        } else {
            // 不创建流程
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostEventData";

            // Event Report Only
            eventInfoPostParam.DataParam = flowInfoPostParam;
            eventInfoPostParam.BizCode = mFlowCenterData.BizCode;
            eventInfoPostParam.EventName = mFlowCenterData.EventName;
            eventInfoPostParam.TableName = mFlowCenterData.TableName;

            reportData = new Gson().toJson(eventInfoPostParam, new TypeToken<EventInfoPostParam>() {
            }.getType());
        }

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                userId,
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                mFlowCenterData.EventName,
                formBeanFragment.getAbsolutePaths(),
                formBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {

            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode > 0) {
                        // Toast.makeText(ZSEventReportActivity.this, "上传成功", Toast.LENGTH_SHORT).show();

                        if ("隐患上报".equals(mFlowCenterData.EventName) && outerIntent.hasExtra("TaskCode")) {
                            // #工商户安检# #设备养护# 上报隐患的时候，进行事件关联到任务操作
                            eventLinkToTask(result.ResultCode);
                        } else {
                            success(result);
                        }

                    } else if (result.getSingleData() == 200) { // 降低要求，只要网络传输没问题就认为成功
                        Toast.makeText(ZSEventReportActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    } else {

                        new AlertDialog.Builder(ZSEventReportActivity.this, R.style.MmtBaseThemeAlertDialog)
                                .setTitle(Html.fromHtml("<font color='#FF0000'>警告</font>"))
                                .setMessage(result.ResultMessage + "，是否保存至后台等待上传？")
                                .setCancelable(false)
                                .setNegativeButton("放弃", null)
                                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        entity.insert();
                                        success(null);
                                    }
                                }).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    private void success(ResultData<Integer> resultData) {
        PlanFeedbackFragment.hasReportEvent = true;

        isSuccess = true;

        if (isFromNavigation) {
            formBeanFragment.deleteCacheData(userId, (mFlowCenterData.BusinessType + mFlowCenterData.EventName));
        }

        if (resultData != null) {
            //如果从巡检过来的，需要记录设备信息
            if (gisInfoBundle == null) {
                gisInfoBundle = outerIntent.getBundleExtra("gisInfo");
            }
            if (gisInfoBundle != null) {

                if (gisInfoBundle.containsKey("patrolNo")) {

                    String patrolNo = gisInfoBundle.getString("patrolNo");
                    boolean isFromPatrol = BaseClassUtil.isNum(patrolNo) && Integer.valueOf(patrolNo) > 0;

                    // resultData.ResultCode 为事件id
                    if (resultData.ResultCode > 0 && isFromPatrol) {
                        reportEventLinkDeviceInfo(resultData.ResultCode, gisInfoBundle);
                    }

                    if (isFromPatrol) {
                        final boolean closePreActivity = gisInfoBundle.getBoolean("closePreActivity", true);
                        clearGisInfo();

                        new AlertDialog.Builder(ZSEventReportActivity.this, R.style.MmtBaseThemeAlertDialog)
                                .setTitle("提示")
                                .setMessage("事件上报成功！")
                                .setCancelable(false)
                                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AppManager.finishActivity(ZSEventReportActivity.this);
                                        if (closePreActivity) {
                                            AppManager.finishActivity();
                                        }
                                    }
                                }).show();

                        return;
                    }
                }
            }
        }

        clearGisInfo();

        new AlertDialog.Builder(ZSEventReportActivity.this, R.style.MmtBaseThemeAlertDialog)
                .setTitle("提示")
                .setMessage("事件上报成功！")
                .setCancelable(false)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        backByReorder();
                    }
                }).show();
    }

    private void reportEventLinkDeviceInfo(final int ID0, final Bundle gisInfo) {

        new MmtBaseTask<Void, Void, Void>(ZSEventReportActivity.this, false) {
            @Override
            protected Void doInBackground(Void... params) {
                EventLinkDevice eventLinkDevice = new EventLinkDevice();
                eventLinkDevice.layerName = gisInfo.getString("layerName");
                eventLinkDevice.filed = gisInfo.getString("filed");
                eventLinkDevice.filedVal = gisInfo.getString("filedVal");
                eventLinkDevice.patrolNo = Integer.valueOf(gisInfo.getString("patrolNo"));

                eventLinkDevice.eventName = mFlowCenterData.EventName;
                eventLinkDevice.eventID = ID0;

                try {
                    NetUtil.executeHttpPost(ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostEventDeviceData", new Gson().toJson(eventLinkDevice));
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                String result = NetUtil.executeHttpPost(ServerConnectConfig.getInstance().getBaseServerPath()
//                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostEventDeviceData", new Gson().toJson(eventLinkDevice));
//
//                //隐藏错误提示信息
//                if (BaseClassUtil.isNullOrEmptyString(result)) {
//                    MyApplication.getInstance().showMessageWithHandle("事件设备关联错误：网络异常！");
//                }
//                ResultWithoutData resultWithoutData = new Gson().fromJson(result, ResultStatus.class).toResultWithoutData();
//                if (resultWithoutData.ResultCode <= 0) {
//                    MyApplication.getInstance().showMessageWithHandle("事件设备关联错误：" + resultWithoutData.ResultMessage);
//                }
                return null;
            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    protected void createBottomView() {

        // 事件上报过程中 HandoverMode为"自处理或移交选择人"的特殊情况：
        // 两个按钮：上报（不建流程直接上报）、移交选择人
        String reportBtnName = "上报";
        if (mFlowCenterData.HandoverMode.equals("自处理或移交选择人")) {
            addBottomUnitView("上报", true, new View.OnClickListener() { //直接上报，不创建流程
                @Override
                public void onClick(View v) {
                    isCreateWorkFlow = false;
                    handler.sendEmptyMessage(TO_DEFAULT_NEXT);
                }
            });
            reportBtnName = "移交";
        }

        if (TextUtils.isEmpty(mQRCodeScanConfig)) {
            tvTopReport.setText(reportBtnName);
        }
        manageUnitView = new BottomUnitView(ZSEventReportActivity.this);
        manageUnitView.setContent(reportBtnName);
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                if (MyApplication.getInstance().getConfigValue("ReportEventConfirm", 0) != 0) {
                    new AlertDialog.Builder(ZSEventReportActivity.this, R.style.MmtBaseThemeAlertDialog)
                            .setTitle(Html.fromHtml("<font color='#FF0000'>警告</font>"))
                            .setMessage("是否确认上报？")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doReport();
                                }
                            }).show();
                } else {
                    doReport();
                }
            }
        });
    }

    private void doReport() {
        if (formBeanFragment == null) {
            return;
        }
        feedbackItems = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
        if (feedbackItems == null) {
            return;
        }

        // 上报事件并创建流程
        if (mFlowCenterData.IsCreate >= 1) {
            isCreateWorkFlow = true;
            switch (mFlowCenterData.HandoverMode) {
                case "移交默认人":
                    handler.sendEmptyMessage(TO_DEFAULT_NEXT); // 移交默认人，Undertakeman传空
                    break;
                case "自处理":
                    handler.sendEmptyMessage(TO_SELF_NEXT); // 自处理，Undertakeman传入userId
                    break;
                case "跨站移交":
                    fetchHandoverUser("");
                    break;
                default:
                    // 移交选择人
                    String stationName = "";
                    for (FeedItem item : feedbackItems) {
                        if (item.Name.equals("处理站点") || item.Name.equals("站点名称")) {
                            stationName = item.Value;
                            break;
                        }
                    }
                    fetchHandoverUser(stationName);
                    break;
            }
        } else { // 只上报事件不创建流程
            isCreateWorkFlow = false;
            handler.sendEmptyMessage(TO_DEFAULT_NEXT);
        }
    }

    private void fetchHandoverUser(final String stationName) {

        new GetHandoverUsersTask(ZSEventReportActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<Node>() {
            @Override
            public void doAfter(Node node) {
                if (node == null) {
                    Toast.makeText(ZSEventReportActivity.this, "获取上报人员失败", Toast.LENGTH_SHORT).show();
                } else {
                    HandoverUserFragment fragment
                            = new HandoverUserFragment(handler, TO_DEFAULT_NEXT, node);
                    // fragment.setIsContainNodeId(false);
                    fragment.show(getSupportFragmentManager(), "");
                }
            }
        }).executeOnExecutor(MyApplication.executorService, mFlowCenterData.FlowName, userId + "", stationName);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 上报成功后就不需要再缓存
        if (!isSuccess && formBeanFragment != null && isFromNavigation) {
            formBeanFragment.saveCacheData(userId, (mFlowCenterData.BusinessType + mFlowCenterData.EventName));
        }
    }
}
