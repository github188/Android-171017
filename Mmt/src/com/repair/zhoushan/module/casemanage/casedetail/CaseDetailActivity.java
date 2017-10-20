package com.repair.zhoushan.module.casemanage.casedetail;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultStatus;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.entity.AssistModule;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FeedbackInfo;
import com.repair.zhoushan.entity.FlowInfoConfig;
import com.repair.zhoushan.entity.FlowInfoItem;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.FlowTableInfo;
import com.repair.zhoushan.entity.TableMetaData;
import com.repair.zhoushan.module.casemanage.casehandover.CaseHandoverActivity;
import com.repair.zhoushan.module.casemanage.mycase.MyCaseListActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static com.repair.zhoushan.common.Utils.json2ResultDataToast;

public class CaseDetailActivity extends BaseActivity {

    /**
     * 获取工单详情
     */
    protected final int GET_DETAIL_FORM = 0;
    /**
     * 获取反馈表单信息
     */
    protected final int GET_FEEDBACK_FORM = 1;
    /**
     * 移交给默认承办人
     */
    protected final int TO_DEFAULT_NEXT = 2;

    public static final int RC_CASE_HANDOVER_DATA_DIRTY = 100;
    private boolean isDataDirty = false;

    protected Fragment caseDetailFragment;

    //  protected MaintainSimpleInfo itemEntity;

    protected CaseItem caseItemEntity;

    //在办箱
    protected ArrayList<FlowInfoItem> flowInfoItemList;
    protected String flowInfoItemListStr;
    protected int editableFormIndex = -1;
    protected boolean isAllCaseScan = false;

    //配置的自已的界面类
    private Class clz = null;
    //工单总览需要的数据
    protected List<FlowTableInfo> mFlowTableInfos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            isAllCaseScan = ("工单总览".equals(CaseDetailActivity.this.getIntent().getStringExtra("name")));
            onCreateCus();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void doingBoxOnCreateCus() {
        final Intent intent = getIntent();

        caseItemEntity = intent.getParcelableExtra("ListItemEntity");
        if (caseItemEntity == null) {

            // 办理页面如移交方式是"自处理"，则跳转至本详情页面，CaseItem 的信息变化需要重新从服务获取
            String caseNo = getIntent().getStringExtra("CaseItemCaseNo");

            new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
                @Override
                public void doAfter(String jsonResult) {

                    ResultData<CaseItem> newData = json2ResultDataToast(CaseItem.class, CaseDetailActivity.this, jsonResult, "获取工单信息失败", false);
                    if (newData == null) return;

                    caseItemEntity = newData.getSingleData();

                    handler.sendEmptyMessage(GET_DETAIL_FORM);
                }
            }) {
                @Override
                protected String doInBackground(String... params) {

                    StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                    sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/")
                            .append(params[0])
                            .append("/StardEventDoingBox?_mid=").append(params[1])
                            .append("&pageIndex=1&pageSize=10&sortFields=ID0&direction=desc")
                            .append("&eventInfo=").append(params[2]);
                    return NetUtil.executeHttpGet(sb.toString());
                }
            }.mmtExecute(String.valueOf(MyApplication.getInstance().getUserId()), UUID.randomUUID().toString(), caseNo);
        } else {

            doingOnView(intent);
        }
    }

    protected void nonDoingOnView(Intent intent) {
        if (intent == null) {
            return;
        }

        String mFlowTableInfoStr = intent.getStringExtra("mFlowTableInfos");

        if (TextUtils.isEmpty(mFlowTableInfoStr)) {
            handler.sendEmptyMessage(GET_DETAIL_FORM);
        } else {
            mFlowTableInfos = new Gson().fromJson(mFlowTableInfoStr, new TypeToken<ArrayList<FlowTableInfo>>() {
            }.getType());

            if (mFlowTableInfos == null || mFlowTableInfos.size() == 0) {
                this.showErrorMsg("未获取到流程节点信息");
                return;
            }

            createView2(mFlowTableInfos);
        }
    }

    protected void doingOnView(Intent intent) {

        if (intent == null) {
            return;
        }
        flowInfoItemListStr = intent.getStringExtra("FlowInfoItem");
        if (TextUtils.isEmpty(flowInfoItemListStr)) {

            handler.sendEmptyMessage(GET_DETAIL_FORM);
        } else {

            flowInfoItemList = new Gson().fromJson(flowInfoItemListStr, new TypeToken<ArrayList<FlowInfoItem>>() {
            }.getType());

            if (flowInfoItemList == null || flowInfoItemList.size() == 0) {
                this.showErrorMsg("未获取到界面信息");
                return;
            }

            createView(flowInfoItemList);
        }
    }

    /**
     * 工单总览
     */
    protected void nonDoingBoxOnCreateCus() {
        final Intent intent = getIntent();

        caseItemEntity = intent.getParcelableExtra("ListItemEntity");
        if (caseItemEntity == null) {

            // 办理页面如移交方式是"自处理"，则跳转至本详情页面，CaseItem 的信息变化需要重新从服务获取
            String caseNo = getIntent().getStringExtra("CaseItemCaseNo");

            new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
                @Override
                public void doAfter(String jsonResult) {

                    ResultData<CaseItem> newData = json2ResultDataToast(CaseItem.class, CaseDetailActivity.this, jsonResult, "获取工单信息失败", false);
                    if (newData == null) return;

                    caseItemEntity = newData.getSingleData();

                    handler.sendEmptyMessage(GET_DETAIL_FORM);

                }
            }) {
                @Override
                protected String doInBackground(String... params) {

                    StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                    sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                            .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseManage/GetCaseOverviewBoxWithPaging")
                            .append("?pageIndex=1&pageSize=1&sortFields=ID0")
                            .append("&eventInfo=").append(params[2]);
                    return NetUtil.executeHttpGet(sb.toString());
                }
            }.mmtExecute(String.valueOf(MyApplication.getInstance().getUserId()), UUID.randomUUID().toString(), caseNo);
        } else {

            nonDoingOnView(intent);

        }
    }

    protected void onCreateCus() {
        getBaseTextView().setText("工单详情");

        if (!isAllCaseScan) {
            doingBoxOnCreateCus();
        } else {
            nonDoingBoxOnCreateCus();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * 获取详情配置表单信息
     */
    protected void getDetailForm(String eventCode, String caseNo, String flowName, String
            nodeName) {

        new FetchCaseDetailTask(CaseDetailActivity.this, handler, isAllCaseScan).executeOnExecutor(MyApplication.executorService,
                eventCode, caseNo, flowName, nodeName);
    }

    protected void createView(List<FlowInfoItem> flowInfoItemList) {
        createView(flowInfoItemList, false);
    }

    /**
     * 工单总览界面构造
     *
     * @param mFlowTableInfos
     */
    protected void createView2(List<FlowTableInfo> mFlowTableInfos) {
        if (mFlowTableInfos == null) {
            return;
        }
        ArrayList<GDFormBean> gdFormBeans = new ArrayList<>();
        for (FlowTableInfo mFlowTableInfo : mFlowTableInfos) {
            int tableMetaDataSize = mFlowTableInfo.TableMetaDatas.size();
            for (int i = 0; i < tableMetaDataSize; i++) {
                TableMetaData tableMetaData = mFlowTableInfo.TableMetaDatas.get(i);
                gdFormBeans.add(tableMetaData.FlowNodeMeta.mapToGDFormBean(true));
            }
        }

        initContent(gdFormBeans, null);
    }


    //多反馈（之前的节点反馈模式）
    protected List<FeedbackInfo> feedBackInfos;

    //产品功能多反馈 辅助视图模式
    protected List<FeedbackInfo> assistFeedBackInfos;

    //手持辅助视图
    protected List<AssistModule> mobileAssistModules;


    protected void startInitContent(List<FeedbackInfo> feedBackInfos, ArrayList<GDFormBean> gdFormBeans, List<FeedbackInfo> productFeedBackInfos) {

        int feedBackInfoSize = feedBackInfos == null ? 0 : feedBackInfos.size();
        int productFeedBackSize = productFeedBackInfos == null ? 0 : productFeedBackInfos.size();

        int total = feedBackInfoSize + productFeedBackSize;

        if (total <= 1) {

            caseDetailFragment = new CaseDetailFragment(gdFormBeans, caseItemEntity);
            Bundle bundle = new Bundle();
            bundle.putBoolean("isDoingBox", !isAllCaseScan);

            if (total == 1) {
                List<FeedbackInfo> totalInfo = new ArrayList<>();
                if (feedBackInfoSize == 1) {
                    totalInfo.addAll(feedBackInfos);
                } else {
                    totalInfo.addAll(productFeedBackInfos);
                }
                bundle.putString("feedbackInfos", new Gson().toJson(totalInfo)); // 多次反馈 辅助模块
            }
            caseDetailFragment.setArguments(bundle);
            addFragment(caseDetailFragment);
        } else {
            List<FeedbackInfo> totalInfo = new ArrayList<>();
            if (feedBackInfoSize > 0) {
                totalInfo.addAll(feedBackInfos);
            }
            if (productFeedBackSize > 0) {
                totalInfo.addAll(productFeedBackInfos);
            }
            //暂时使用viewpage处理tab过多的情况
            caseDetailFragment = new CaseDetailFragmentV2(gdFormBeans, caseItemEntity);
            Bundle bundle = new Bundle();
            bundle.putBoolean("isDoingBox", !isAllCaseScan);
            bundle.putString("feedbackInfos", new Gson().toJson(totalInfo));
            caseDetailFragment.setArguments(bundle);
            addFragment(caseDetailFragment);
        }
    }

    protected void initContent(final ArrayList<GDFormBean> gdFormBeans, final CallBack callBack) {

        feedBackInfos = getFeedbackInfos();


        mobileAssistModules = getMobileAssistModules();

        final List<AssistModule> assistModulesFeedBackInfos = getFeedBackAssistModules();

        if (assistModulesFeedBackInfos != null && assistModulesFeedBackInfos.size() > 0) {

            new MmtBaseTask<Void, Void, List<FeedbackInfo>>(CaseDetailActivity.this) {
                @Override
                protected List<FeedbackInfo> doInBackground(Void... params) {

                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/WorkFlow/GetFeedbackInfo?fbBiz=";

                    List<FeedbackInfo> reslutList = new ArrayList<>();
                    for (AssistModule fbAssistModule : assistModulesFeedBackInfos) {

                        String ViewParams = fbAssistModule.MobileViewParam;
                        if(TextUtils.isEmpty(ViewParams)){
                            ViewParams=fbAssistModule.ViewParam;
                        }

                        if (TextUtils.isEmpty(ViewParams)) {
                            continue;
                        }

                        String[] ViewParamArr = ViewParams.split(",");
                        String biz = ViewParamArr[0];
                        String type = ViewParamArr.length > 1 ? ViewParamArr[1] : "scan";

                        String result = NetUtil.executeHttpGet(url + biz);

                        if (TextUtils.isEmpty(result)) {
                            continue;
                        }

                        Results<FeedbackInfo> feedbackInfoResults = new Gson().fromJson(result, new TypeToken<Results<FeedbackInfo>>() {
                        }.getType());

                        if (feedbackInfoResults == null) {
                            continue;
                        }
                        if (!TextUtils.isEmpty(feedbackInfoResults.say.errMsg)) {
                            MyApplication.getInstance().showMessageWithHandle(feedbackInfoResults.say.errMsg);
                            continue;
                        }

                        List<FeedbackInfo> feedbackInfosTemp = feedbackInfoResults.getMe;

                        if (feedbackInfosTemp == null) {
                            continue;
                        }

                        for (FeedbackInfo fb : feedbackInfosTemp) {
                            fb.isRead = ("scan".equals(type.toLowerCase())||isAllCaseScan);
                            reslutList.add(fb);
                        }
                    }

                    return reslutList;
                }

                @Override
                protected void onSuccess(List<FeedbackInfo> s) {
                    super.onSuccess(s);

                    if (s != null && s.size() > 0) {
                        if (assistFeedBackInfos == null) {
                            assistFeedBackInfos = new ArrayList<>();
                        }
                        assistFeedBackInfos.addAll(s);
                    }

                    startInitContent(feedBackInfos, gdFormBeans, assistFeedBackInfos);
                    if (callBack != null) {
                        callBack.CallBack();
                    }

                }
            }.mmtExecute();

            return;

        }
        startInitContent(feedBackInfos, gdFormBeans, null);
        if (callBack != null) {
            callBack.CallBack();
        }

    }

    //暂时先都取第0个,后续考虑工单总览的
    protected List<FeedbackInfo> getFeedbackInfos() {

        List<FeedbackInfo> results = new ArrayList<>();

        //已办箱（工单总览）
        if (mFlowTableInfos != null) {
            for (FlowTableInfo fti : mFlowTableInfos) {
                List<FeedbackInfo> feedbackInfos = fti.Feedback;
                if (feedbackInfos != null) {

                    for (FeedbackInfo fbi : feedbackInfos) {
                        if (TextUtils.isEmpty(fbi.FBBiz) || TextUtils.isEmpty(fbi.FBTable)) {
                            continue;
                        }
                        results.add(fbi);
                    }
                }
            }
            return results;
        }

        //在办箱,只取第0个
        if (flowInfoItemList != null && flowInfoItemList.size() > editableFormIndex) {
            List<FeedbackInfo> feedbackInfos = flowInfoItemList.get(editableFormIndex).Feedback;
            if (feedbackInfos != null && feedbackInfos.size() > 0) {
                FeedbackInfo fbi = feedbackInfos.get(0);
                if (!TextUtils.isEmpty(fbi.FBBiz) || !TextUtils.isEmpty(fbi.FBTable)) {
                    results.add(fbi);
                }
            }
            return results;
        }

        return results;
    }

    @NonNull
    private List<AssistModule> getMobileAssistModules() {

        ArrayList<AssistModule> moduleList = new ArrayList<>();
        if (editableFormIndex < 0) {
            return moduleList;
        }
        List<AssistModule> assistModules = flowInfoItemList.get(editableFormIndex).AssistModules;
        if (assistModules != null) {
            for (AssistModule assistModule : assistModules) {
                if(TextUtils.isEmpty(assistModule.MobileViewModule)){
                    continue;
                }
                if ("feedbackassistview".equals(assistModule.MobileViewModule.toLowerCase())) {
                    continue;
                }
                moduleList.add(assistModule);
            }
        }
        return moduleList;
    }


    @NonNull
    private List<AssistModule> getFeedBackAssistModules() {

        ArrayList<AssistModule> feedBackList = new ArrayList<>();

        List<AssistModule> assistModules = null;

        if (flowInfoItemList != null && flowInfoItemList.size() > editableFormIndex) {
            if (editableFormIndex < 0) {
                return feedBackList;
            }
            assistModules = flowInfoItemList.get(editableFormIndex).AssistModules;
        }
        if (mFlowTableInfos != null && mFlowTableInfos.size() > 0) {
            assistModules = mFlowTableInfos.get(mFlowTableInfos.size() - 1).AssistModules;
        }
        if (assistModules == null) {
            return feedBackList;
        }

        for (AssistModule assistModule : assistModules) {
            String configView=assistModule.MobileViewModule;
            if(TextUtils.isEmpty(configView)){
                configView=assistModule.ViewModule;
            }
            if(TextUtils.isEmpty(configView)){
                continue;
            }
            if ("feedbackassistview".equals(configView.toLowerCase())) {
                feedBackList.add(assistModule);
            }
        }

        return feedBackList;
    }

    public interface CallBack {
        void CallBack();
    }

    /**
     * 工单办理界面构造
     * 需要过滤重复字段
     *
     * @param flowInfoItemList
     * @param needFilterSchema
     */
    protected void createView(List<FlowInfoItem> flowInfoItemList, boolean needFilterSchema) {

        createDoingViewNoBtn(flowInfoItemList, needFilterSchema, new CallBack() {
            @Override
            public void CallBack() {
                createBottomView();
            }
        });
    }

    protected void createDoingViewNoBtn(List<FlowInfoItem> flowInfoItemList, boolean needFilterSchema, CallBack callBack) {

        ArrayList<String> groupTitles = new ArrayList<>();
        ArrayList<GDFormBean> gdFormBeans = new ArrayList<>();

        onMegerGroup(flowInfoItemList);

        int index = -1;
        for (FlowInfoItem flowInfoItem : flowInfoItemList) {
            index++;
            // "scan" tab is only for showing.
            if (flowInfoItem.FlowInfoConfig.ViewState.equalsIgnoreCase("scan")) {
                String groupTitle = flowInfoItem.FlowInfoConfig.NodeName;
                groupTitles.add(groupTitle);
                GDFormBean gdFormBean = flowInfoItem.FlowNodeMeta.mapToGDFormBean();
                gdFormBean.setOnlyShow();
                gdFormBeans.add(gdFormBean);
            } else { // "edit"
                editableFormIndex = index;
            }
        }

        initContent(gdFormBeans, callBack);
    }

    /**
     * 合并分组,相同组名内容合并后去掉重复的组
     *
     * @param flowInfoItemList
     */
    private void onMegerGroup(List<FlowInfoItem> flowInfoItemList) {

        List<String> tableNames = new ArrayList<>();
        List<String> groupTitles = new ArrayList<>();
        List<FlowNodeMeta.TableGroup> tableGroups = new ArrayList<>();

        ListIterator<FlowInfoItem> flowInfoItemListIterator = flowInfoItemList.listIterator();
        while (flowInfoItemListIterator.hasNext()) {
            FlowInfoItem flowInfoItem = flowInfoItemListIterator.next();
            if (flowInfoItem.FlowInfoConfig.ViewState.equalsIgnoreCase("edit")) {
                continue;
            }
            String tableName = flowInfoItem.FlowInfoConfig.TableName;
            int indexTable = tableNames.indexOf(tableName);
            boolean isSameTable = indexTable > -1;

            //优化效率（当节点信息的对应的表名不一样，就算组名一样也不能算同一分组）
            if (!isSameTable) {
                tableNames.add(tableName);

                for (FlowNodeMeta.TableGroup group : flowInfoItem.FlowNodeMeta.Groups) {
                    groupTitles.add(group.GroupName);
                    tableGroups.add(group);
                }

                continue;
            }


            List<FlowNodeMeta.TableGroup> groups = flowInfoItem.FlowNodeMeta.Groups;
            List<FlowNodeMeta.TableValue> values = flowInfoItem.FlowNodeMeta.Values;

            ListIterator<FlowNodeMeta.TableGroup> groupListIterator = groups.listIterator();
            while (groupListIterator.hasNext()) {
                FlowNodeMeta.TableGroup group = groupListIterator.next();
                String groupTitle = group.GroupName;
                int index = groupTitles.indexOf(groupTitle);

                //严格判断是否重复（当出现表相同，组名也相同才能判断是重复的）
                boolean isRepeat = index > -1;
                if (isRepeat) {
                    FlowNodeMeta.TableGroup preGroup = tableGroups.get(index);
                    boolean hasMerge = startMegerGroup(preGroup, group, values);
                    if (hasMerge) {
                        //有新增需要加上新增字段的值
                        startMergeValues(flowInfoItemList, indexTable, values);
                    }
                    groupListIterator.remove();

                } else {
                    groupTitles.add(groupTitle);
                    tableGroups.add(group);
                }
            }


        }
    }

    /**
     * @param flowInfoItemList
     * @param preIndex         已有分组的索引
     * @param values           新增的值
     */
    private void startMergeValues(List<FlowInfoItem> flowInfoItemList, int preIndex, List<FlowNodeMeta.TableValue> values) {

        int size = flowInfoItemList.size();
        if (preIndex < 0) {
            return;
        }
        if (preIndex >= size) {
            return;
        }

        FlowInfoItem preFlowInfoItem = flowInfoItemList.get(preIndex);
        List<FlowNodeMeta.TableValue> preValues = preFlowInfoItem.FlowNodeMeta.Values;
        preValues.addAll(values);

    }

    private boolean startMegerGroup(FlowNodeMeta.TableGroup preGroup, FlowNodeMeta.TableGroup curGroup, List<FlowNodeMeta.TableValue> curValues) {
        List<FlowNodeMeta.FieldSchema> curSchemas = curGroup.Schema;

        ListIterator<FlowNodeMeta.FieldSchema> schemaListIterator = curSchemas.listIterator();
        List<FlowNodeMeta.TableValue> mergeValues = new ArrayList<>();
        boolean hasMerge = false;
        while (schemaListIterator.hasNext()) {
            FlowNodeMeta.FieldSchema curSchema = schemaListIterator.next();
            String curAlias = curSchema.Alias;
            if (TextUtils.isEmpty(curAlias)) {
                continue;
            }

            List<FlowNodeMeta.FieldSchema> preSchemas = preGroup.Schema;

            boolean isExist = true;
            for (FlowNodeMeta.FieldSchema preSchema : preSchemas) {
                if (curAlias.equals(preSchema.Alias)) {
                    isExist = true;
                    break;
                }

                isExist = false;
            }

            if (!isExist) {
                schemaListIterator.add(curSchema);

                for (FlowNodeMeta.TableValue curValue : curValues) {
                    if (curValue.FieldName.equals(curSchema.FieldName)) {
                        mergeValues.add(curValue);
                        break;
                    }
                }
            }
        }
        curValues.clear();
        curValues.addAll(mergeValues);

        return hasMerge;
    }

    protected Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {

                    case GET_DETAIL_FORM:
                        getDetailForm(caseItemEntity.EventCode, caseItemEntity.CaseNo, caseItemEntity.FlowName, caseItemEntity.ActiveName);
                        break;

                    case MaintenanceConstant.SERVER_GET_DETAIL_SUCCESS:
                        // 将Fragment显示在界面上

                        if (isAllCaseScan) {
                            mFlowTableInfos = (List<FlowTableInfo>) msg.obj;
                            for (FlowTableInfo mFlowTableInfo : mFlowTableInfos) {
                                for (FlowInfoConfig mFlowTableInfoSingle : mFlowTableInfo.FlowInfoConfig) {
                                    if (!BaseClassUtil.isNullOrEmptyString(mFlowTableInfoSingle.MobileViewModule)) {
                                        try {
                                            if (mFlowTableInfoSingle.MobileViewModule.contains("OnSiteConfirmDetailActivity")) { // Temporary compatibility, remove later.
                                                mFlowTableInfoSingle.MobileViewModule
                                                        = "com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver.OnSiteConfirmDetailActivity";
                                            } else if (mFlowTableInfoSingle.MobileViewModule.contains("CaregiverDetailActivity")) {
                                                mFlowTableInfoSingle.MobileViewModule
                                                        = "com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver.CaregiverDetailActivity";
                                            }
                                            clz = Class.forName(mFlowTableInfoSingle.MobileViewModule);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            (CaseDetailActivity.this).showErrorMsg("配置的视图模块有误");
                                            return;
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            flowInfoItemList = (ArrayList<FlowInfoItem>) msg.obj;
                            // 自定义视图模块判断

                            for (FlowInfoItem flowInfoItem : flowInfoItemList) {
                                // 当前节点配置自定义视图 ("scan" tab is only for showing.)
                                if (flowInfoItem.FlowInfoConfig.ViewState.equalsIgnoreCase("edit")) {
                                    if (!BaseClassUtil.isNullOrEmptyString(flowInfoItem.FlowInfoConfig.MobileViewModule)) {
                                        try {
                                            if (flowInfoItem.FlowInfoConfig.MobileViewModule.contains("OnSiteConfirmDetailActivity")) { // Temporary Compatibility, remove later.
                                                flowInfoItem.FlowInfoConfig.MobileViewModule
                                                        = "com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver.OnSiteConfirmDetailActivity";
                                            } else if (flowInfoItem.FlowInfoConfig.MobileViewModule.contains("CaregiverDetailActivity")) {
                                                flowInfoItem.FlowInfoConfig.MobileViewModule
                                                        = "com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver.CaregiverDetailActivity";
                                            }
                                            clz = Class.forName(flowInfoItem.FlowInfoConfig.MobileViewModule);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            (CaseDetailActivity.this).showErrorMsg("配置的视图模块有误");
                                            return;
                                        }
                                        break;
                                    }
                                }
                            }

                            if (TextUtils.isEmpty(caseItemEntity.ReadCaseTime)) {
                                updateReadCaseTime();
                                setResult(Activity.RESULT_OK);
                            }
                        }
                        if (clz != null) {
                            Intent intent = new Intent(CaseDetailActivity.this, clz);
                            intent.putExtra("ListItemEntity", caseItemEntity);

                            if (isAllCaseScan) {
                                intent.putExtra("mFlowTableInfos", new Gson().toJson(mFlowTableInfos));

                                intent.putExtra("name", "工单总览");

                            } else {
                                intent.putExtra("FlowInfoItem", new Gson().toJson(flowInfoItemList));

                                intent.removeExtra("name");
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                            startActivity(intent);
                            overridePendingTransition(0, 0);

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    CaseDetailActivity.this.finish();
                                    overridePendingTransition(0, 0);
                                }
                            }, 300);
                        } else {
                            if (isAllCaseScan) {
                                createView2(mFlowTableInfos);
                            } else {
                                createView(flowInfoItemList, true);
                            }
                        }

                        break;

                    case MaintenanceConstant.SERVER_SELECT_NEXT:
                        // 用于传递表单数据，避免重复访问数据库
                        // MyApplication.getInstance().putConfigValue("HandoverFormEntity", flowInfoItemList.get(editableFormIndex));

                        Intent intent = new Intent(CaseDetailActivity.this, CaseHandoverActivity.class);
                        intent.putExtra("ListItemEntity", caseItemEntity);
                        if (!isDataDirty) {
                            intent.putExtra("FlowInfoItemList", new Gson().toJson(flowInfoItemList));
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(intent, RC_CASE_HANDOVER_DATA_DIRTY);
                        MyApplication.getInstance().startActivityAnimation(CaseDetailActivity.this);

                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
                (CaseDetailActivity.this).showErrorMsg(e.getMessage());
            }
        }
    };

    /**
     * 对于新工单，进入详情页面后更新阅单时间，表示已读
     */
    private void updateReadCaseTime() {

        new AsyncTask<String, Void, ResultStatus>() {

            @Override
            protected ResultStatus doInBackground(String... params) {

                ResultStatus resultStatus;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/ReadCase";

                try {
                    String result = NetUtil.executeHttpPost(url, params[0],
                            "Content-Type", "application/json; charset=utf-8");

                    if (TextUtils.isEmpty(result)) {
                        throw new Exception("返回结果为空");
                    }

                    resultStatus = new Gson().fromJson(result, ResultStatus.class);

                } catch (Exception e) {
                    e.printStackTrace();
                    resultStatus = new ResultStatus();
                    resultStatus.statusCode = "-100";
                    resultStatus.errMsg = e.getMessage();
                }

                return resultStatus;
            }

            @Override
            protected void onPostExecute(ResultStatus resultStatus) {

                ResultWithoutData resultWithoutData = resultStatus.toResultWithoutData();
                if (resultWithoutData.ResultCode != 200) {
                    Toast.makeText(CaseDetailActivity.this, resultStatus.errMsg, Toast.LENGTH_SHORT).show();
                }
            }

        }.executeOnExecutor(MyApplication.executorService, new Gson().toJson(caseItemEntity.mapToCaseInfo()));

    }

    /**
     * 创建对工单进行操作的按钮
     */
    protected void createBottomView() {

        // 创建回退按钮
        createRollbackBottomButton(CaseDetailActivity.this);

        //手持辅助视图，以前的节点反馈，和现在产品的节点反馈
        multFBAndAssistModule();

        // 创建移交按钮
        createHandoverBtn();
    }

    protected void createHandoverBtn() {
        String handoverBtnText = editableFormIndex >= 0
                ? flowInfoItemList.get(editableFormIndex).FlowInfoConfig.NodeName : "移交";
        addBottomUnitView(handoverBtnText, false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(MaintenanceConstant.SERVER_SELECT_NEXT);
            }
        });
    }


    protected void multFBAndAssistModule() {

        if (isAllCaseScan) {
            return;
        }

        int mobileAssistModulesSize = mobileAssistModules == null ? 0 : mobileAssistModules.size();

        int feedBackInfosSize = feedBackInfos == null ? 0 : feedBackInfos.size();

        //产品反馈模型中只读的配置不参与按钮的统计
        List<FeedbackInfo> productNeedReportFBlist = new ArrayList<>();
        if (assistFeedBackInfos != null) {
            for (FeedbackInfo fb : assistFeedBackInfos) {
                if (fb.isRead) {
                    continue;
                }
                productNeedReportFBlist.add(fb);
            }
        }

        int productNodeFeedBackInfosSize = productNeedReportFBlist.size();

        int totalSize = mobileAssistModulesSize + feedBackInfosSize + productNodeFeedBackInfosSize;

        if (totalSize == 0) {
            return;
        }

        if (totalSize == 1) {
            if (feedBackInfosSize == 1) {

                final FeedbackInfo feedbackInfo = feedBackInfos.get(0);

                if (TextUtils.isEmpty(feedbackInfo.FBBiz) || TextUtils.isEmpty(feedbackInfo.FBTable)) {
                    return;
                }

                BottomUnitView manageUnitView = new BottomUnitView(CaseDetailActivity.this);
                manageUnitView.setContent(feedbackInfo.FBBiz);
                addBottomUnitView(manageUnitView, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        feedbackReportActivity(caseItemEntity.CaseNo, feedbackInfo);
                    }
                });

            } else if (mobileAssistModulesSize == 1) {
                final AssistModule assistModule = mobileAssistModules.get(0);
                if (!TextUtils.isEmpty(assistModule.MobileViewModule)) {
                    addBottomUnitView(assistModule.MobileViewLabel, false, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            navigateToAssistModule(assistModule);
                        }
                    });
                }
            } else {

                final FeedbackInfo feedbackInfo = productNeedReportFBlist.get(0);

                if (TextUtils.isEmpty(feedbackInfo.FBBiz) || TextUtils.isEmpty(feedbackInfo.FBTable)) {
                    return;
                }

                BottomUnitView manageUnitView = new BottomUnitView(CaseDetailActivity.this);
                manageUnitView.setContent(feedbackInfo.FBBiz);
                addBottomUnitView(manageUnitView, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        feedbackReportActivity(caseItemEntity.CaseNo, feedbackInfo);
                    }
                });
            }


        } else {

            addMoreFunction(feedBackInfos, mobileAssistModules, productNeedReportFBlist);

        }

    }

    /**
     * 跳转到配置的辅助模块界面
     */
    private void navigateToAssistModule(AssistModule assistModule) {
        Class<?> moduleClz = null;
        try {
            moduleClz = Class.forName(assistModule.MobileViewModule);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (moduleClz == null) {
            Toast.makeText(CaseDetailActivity.this, "手持视图模块配置有误", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(CaseDetailActivity.this, moduleClz);
        intent.putExtra("ListItemEntity", caseItemEntity);
        intent.putExtra("AssistModule", assistModule);
        //region ShaoXin ValveInstruction
        intent.putExtra("CurrentNodeName", editableFormIndex >= 0 ? flowInfoItemList.get(editableFormIndex).FlowInfoConfig.NodeName : "移交");
        //endregion
        startActivity(intent);
    }


    private ArrayList<String> moreFuns = new ArrayList<>();

    private void addMoreFunction(final List<FeedbackInfo> feedBackInfos, final List<AssistModule> mobileAssistModules, final List<FeedbackInfo> productFeedBackInfos) {

        if (moreFuns.size() != 0) {
            return;
        }

        if (feedBackInfos != null) {
            for (FeedbackInfo feedbackInfo : feedBackInfos) {
                if (TextUtils.isEmpty(feedbackInfo.FBBiz) || TextUtils.isEmpty(feedbackInfo.FBTable)) {
                    continue;
                }
                moreFuns.add(feedbackInfo.FBBiz);
            }
        }

        if (mobileAssistModules != null) {
            for (AssistModule assistModule : mobileAssistModules) {
                if (TextUtils.isEmpty(assistModule.MobileViewModule)) {
                    continue;
                }
                moreFuns.add(assistModule.MobileViewLabel);
            }
        }

        if (productFeedBackInfos != null) {
            for (FeedbackInfo feedbackInfo : productFeedBackInfos) {
                if (TextUtils.isEmpty(feedbackInfo.FBBiz) || TextUtils.isEmpty(feedbackInfo.FBTable)) {
                    continue;
                }
                moreFuns.add(feedbackInfo.FBBiz);
            }
        }


        BottomUnitView manageUnitView = new BottomUnitView(CaseDetailActivity.this);
        manageUnitView.setContent("更多");
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListDialogFragment listDia = new ListDialogFragment("更多功能", moreFuns);
                listDia.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        handleMoreFunction(arg2, feedBackInfos, mobileAssistModules, productFeedBackInfos);
                    }
                });
                listDia.show(getSupportFragmentManager(), "moreFb");
            }
        });
    }

    private void handleMoreFunction(int index, List<FeedbackInfo> feedBackInfos, List<AssistModule> mobileAssistModules, List<FeedbackInfo> productFeedBackInfos) {

        String functionName = moreFuns.get(index);

        if (feedBackInfos != null) {
            for (FeedbackInfo feedbackInfo : feedBackInfos) {
                if (functionName.equals(feedbackInfo.FBBiz)) {
                    String caseNo = caseItemEntity.CaseNo;
                    feedbackReportActivity(caseNo, feedbackInfo);
                    return;
                }
            }
        }
        if (mobileAssistModules != null) {
            for (AssistModule assistModule : mobileAssistModules) {
                if (functionName.equals(assistModule.MobileViewLabel)) {
                    navigateToAssistModule(assistModule);
                    return;
                }
            }
        }

        if (productFeedBackInfos != null) {
            for (FeedbackInfo feedbackInfo : productFeedBackInfos) {
                if (functionName.equals(feedbackInfo.FBBiz)) {
                    String caseNo = caseItemEntity.CaseNo;
                    feedbackReportActivity(caseNo, feedbackInfo);
                    return;
                }
            }
        }
    }

    protected void feedbackReportActivity(String caseNo, FeedbackInfo feedbackInfo) {

        Intent intent = new Intent(CaseDetailActivity.this, FeebReportActivity.class);
        intent.putExtra("caseno", caseNo);
        intent.putExtra("bizName", feedbackInfo.FBBiz);
        intent.putExtra("tableName", feedbackInfo.FBTable);
        intent.putExtra("viewMode", TabltViewMode.REPORT.getTableViewMode());
        intent.putExtra("eventCode", caseItemEntity.EventCode);
        intent.putExtra("eventTableName", caseItemEntity.EventMainTable);
        intent.putExtra("feedbackInfo", new Gson().toJson(feedbackInfo));
        CaseDetailActivity.this.startActivityForResult(intent, Constants.SPECIAL_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(CaseDetailActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Activity.RESULT_OK == resultCode) {
            if (requestCode == Constants.SPECIAL_REQUEST_CODE) {
                // 事件表没有没有是否已到位 是否已反馈的 对应都是-1, 此时不必做与反馈相关的操作
                if (caseItemEntity.IsArrive != -1 || caseItemEntity.IsFeedback != -1) {
                    if (!TextUtils.isEmpty(caseItemEntity.ReadCaseTime)) {
                        setResult(100); // 多次反馈反馈成功，返回列表界面后本地更新数据的反馈标志字段
                    } else {
                        setResult(200); // 返回列表置反馈标志并更新阅读时间
                    }
                }
            } else if (requestCode == RC_CASE_HANDOVER_DATA_DIRTY) {
                isDataDirty = true;
            }
        }
    }

    protected void createRollbackBottomButton(final BaseActivity activity) {
        FlowInfoConfig curFlowInfoConfig = flowInfoItemList.get(editableFormIndex).FlowInfoConfig;
        if (curFlowInfoConfig.AllowBack == 0) {
            return;
        }
        // 开始节点没有有“回退”按钮
        if (curFlowInfoConfig.NodeType == 1) {
            return;
        }

        BottomUnitView backUnitView = new BottomUnitView(activity);
        backUnitView.setContent("回退");
        addBottomUnitView(backUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View backView = getLayoutInflater().inflate(R.layout.maintenance_back, null);
                final OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("回退原因", backView);
                okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {

                        String opinion = ((EditText) backView.findViewById(R.id.maintenanceBackReason)).getText().toString();
                        if (TextUtils.isEmpty(opinion)) {
                            Toast.makeText(activity, "退单原因不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        CaseInfo caseInfo = caseItemEntity.mapToCaseInfo();
                        caseInfo.Opinion = opinion;
                        new CaseBackTask(activity, true, "CaseHandBack", "回退失败",
                                new MmtBaseTask.OnWxyhTaskListener<String>() {
                                    @Override
                                    public void doAfter(String result) {

                                        if (!TextUtils.isEmpty(result)&&!result.equals("\"\"")) {
                                            Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(activity, "回退成功", Toast.LENGTH_SHORT).show();
                                            //成功后自己打开自己，达到重置界面的目的
                                            Intent intent = new Intent(activity, MyCaseListActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }
                                        okCancelDialogFragment.dismiss();
                                    }
                                }).mmtExecute(caseInfo);
                    }
                });
                okCancelDialogFragment.setAutoDismiss(false);
                okCancelDialogFragment.setCancelable(true);
                okCancelDialogFragment.show(getSupportFragmentManager(), "1");
            }
        });
    }

    protected String getValueByFieldName(String fieldName) {
        for (FlowInfoItem flowInfoItem : flowInfoItemList) {
            for (FlowNodeMeta.TableValue value : flowInfoItem.FlowNodeMeta.Values) {
                if (value.FieldName.equals(fieldName)) {
                    return value.FieldValue;
                }
            }
        }
        return "";
    }

    protected ArrayList<FlowNodeMeta.FieldSchema> getSchemaListByNames(String... names) {

        ArrayList<FlowNodeMeta.FieldSchema> schemas = new ArrayList<>();

        if (names.length == 0) {
            return schemas;
        }

        List<String> nameList = Arrays.asList(names);

        for (FlowInfoItem flowInfoItem : flowInfoItemList) {
            for (FlowNodeMeta.TableGroup tableGroup : flowInfoItem.FlowNodeMeta.Groups) {
                for (FlowNodeMeta.FieldSchema fieldSchema : tableGroup.Schema) {
                    if (nameList.contains(fieldSchema.FieldName)) {
                        schemas.add(fieldSchema);
                    }
                }
            }
        }
        return schemas;
    }

}
