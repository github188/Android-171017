package com.repair.zhoushan.module.eventmanage.eventbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.ListCheckBoxDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.EventItem;
import com.repair.zhoushan.entity.FlowInfoConfig;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.HandoverUserFragment;
import com.repair.zhoushan.module.eventreport.GetHandoverUsersTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventDispatchActivity extends BaseActivity implements HandoverUserFragment.OnUserSelectedInterface{

    // 0:调度箱模式(默认)  1：领单箱模式
    private int mMode = 0;

    private EventItem mEventItem;
    private FlowInfoConfig mFlowInfoConfig;
    private FlowNodeMeta mFlowNodeMeta;

    private FlowBeanFragment mFlowBeanFragment;

    private FlowInfoPostParam mFlowInfoPostParam = new FlowInfoPostParam();
    private List<FeedItem> feedbackItems = null;

    private int userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent argIntent = getIntent();
        mEventItem = argIntent.getParcelableExtra("ListItemEntity");
        mFlowInfoConfig = argIntent.getParcelableExtra("FlowInfoConfigItem");
        mMode = argIntent.getIntExtra("MODE", EventListActivity.Mode.DISPATCH);

        UserBean userBean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);
        this.userId = userBean.UserID;
        this.userName = userBean.TrueName;

        if (mEventItem == null || mFlowInfoConfig == null) {
            this.showErrorMsg("未获取到参数");
            return;
        }
        getBaseTextView().setText("事件分派");

        initView();
    }

    private void initView() {

        new MmtBaseTask<String, Void, String>(EventDispatchActivity.this, true) {
            @Override
            protected String doInBackground(String... params) {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetFlowNodeMeta")
                        .append("?flowName=").append(mFlowInfoConfig.FlowName)
                        .append("&nodeName=").append(mFlowInfoConfig.NodeName)
                        .append("&_token=").append(UUID.randomUUID().toString());

                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onSuccess(String resultJson) {

                ResultData<FlowNodeMeta> newData = Utils.json2ResultDataActivity(FlowNodeMeta.class, EventDispatchActivity.this, resultJson, "获取分派信息失败", false);
                if (newData == null) return;

                mFlowNodeMeta = newData.getSingleData();
                mFlowInfoPostParam.flowNodeMeta = mFlowNodeMeta;

                createView();
            }
        }.mmtExecute();

    }

    private void createView() {

        mFlowBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", mFlowNodeMeta.mapToGDFormBean());
        mFlowBeanFragment.setArguments(args);

        mFlowBeanFragment.setFragmentFileRelativePath(mEventItem.EventCode);
        mFlowBeanFragment.setCls(EventDispatchActivity.class);
        mFlowBeanFragment.setAddEnable(true);
        addFragment(mFlowBeanFragment);

        createBottomView();
    }

    private void createBottomView() {

        BottomUnitView dispatchUnitView = new BottomUnitView(EventDispatchActivity.this);
        dispatchUnitView.setImageResource(R.drawable.handoverform_report);

        if (mMode == 1) {
            dispatchUnitView.setContent("领取任务"); // 领单模式
        } else {
            dispatchUnitView.setContent(mFlowInfoConfig.NodeName); // 调度箱模式
        }

        addBottomUnitView(dispatchUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {

                if (mFlowBeanFragment == null) {
                    return;
                }
                feedbackItems = mFlowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

                if (feedbackItems == null) return;

                // 领单模式
                if (mMode == 1) {
                    new GetHandoverUsersTask(EventDispatchActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<Node>() {
                        @Override
                        public void doAfter(Node node) {
                            if (node == null) {
                                Toast.makeText(EventDispatchActivity.this, "获取节点失败", Toast.LENGTH_SHORT).show();
                            } else {

                                final List<Node> levelOneChildList = node.getChildren();
                                final ArrayList<String> nodeNameList = new ArrayList<String>();
                                for (Node nodeItem : levelOneChildList) {
                                    nodeNameList.add(nodeItem.getText());
                                }

                                if (levelOneChildList.size() > 1) {
                                    ListCheckBoxDialogFragment fragment
                                            = ListCheckBoxDialogFragment.newInstance("请选择流程节点", nodeNameList, null);
                                    fragment.setSingleMode(true); // 设置单选
                                    fragment.setDisallowEmpty(true); // 设备不允许为空
                                    fragment.show(EventDispatchActivity.this.getSupportFragmentManager(), "");
                                    fragment.setOnRightButtonClickListener(new ListCheckBoxDialogFragment.OnRightButtonClickListener() {
                                        @Override
                                        public void onRightButtonClick(View view, List<String> selectedItems) {
                                            String nextNodeIdStr = levelOneChildList.get(nodeNameList.indexOf(selectedItems.get(0))).getValue();
                                            mFlowInfoPostParam.caseInfo.Undertakeman = nextNodeIdStr + "/" + userId;
                                            mFlowInfoPostParam.caseInfo.Opinion = "自领任务单 - " + userName;

                                            dispatchEvent();
                                        }
                                    });
                                } else if (levelOneChildList.size() == 1) {
                                    mFlowInfoPostParam.caseInfo.Undertakeman = levelOneChildList.get(0).getValue() + "/" + userId;
                                    mFlowInfoPostParam.caseInfo.Opinion = "自领任务单 - " + userName;
                                    dispatchEvent();
                                }
                            }
                        }
                    }).mmtExecute(mFlowInfoConfig.FlowName, String.valueOf(userId), mEventItem.DealStation);

                } else { // 调度箱模式

                    final String handoverMode = mFlowInfoConfig.HandOverMode;
                    switch (handoverMode) {

                        case "跨站移交":
                        case "自处理或移交选择人":
                            getHandoverUsers("");
                            break;
                        case "移交选择人":
                            getHandoverUsers(mEventItem.DealStation);
                            break;
                        case "处理站点移交":
                            String station = "";
                            boolean hasStationConfig = false;
                            // 读取界面上选中的处理站点(如果存在)
                            if (mFlowBeanFragment != null) {
                                List<FeedItem> feedbackItems = mFlowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
                                if (feedbackItems != null) {
                                    for (FeedItem feedItem : feedbackItems) {
                                        if ("处理站点".equals(feedItem.Name)) {
                                            hasStationConfig = true;
                                            station = feedItem.Value;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!hasStationConfig) {
                                Toast.makeText(EventDispatchActivity.this, "流程节点信息，请配置处理站点字段", Toast.LENGTH_LONG).show();
                                return;
                            }
                            getHandoverUsers(station);
                            break;
                        case "本人站点移交":
                            List<String> stations = MyApplication.getInstance().getUserBean().getBelongStation();
                            String selfStation = BaseClassUtil.listToString(stations);
                            getHandoverUsers(selfStation);
                            break;
                        case "自处理":
                            mFlowInfoPostParam.caseInfo.Undertakeman = String.valueOf(userId);
                            mFlowInfoPostParam.caseInfo.Opinion = "";
                            dispatchEvent();
                            break;

                        case "移交默认人":
                            mFlowInfoPostParam.caseInfo.Undertakeman = "";
                            mFlowInfoPostParam.caseInfo.Opinion = "";
                            dispatchEvent();
                            break;
                    }
                }
            }
        });
    }

    private void getHandoverUsers(String station) {
        new GetHandoverUsersTask(EventDispatchActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<Node>() {
            @Override
            public void doAfter(Node node) {
                if (node == null) {
                    Toast.makeText(EventDispatchActivity.this, "获取节点失败", Toast.LENGTH_SHORT).show();
                } else {

                    HandoverUserFragment fragment = new HandoverUserFragment(node);
                    // fragment.setIsContainNodeId(false);
                    fragment.show(getSupportFragmentManager(), "");
                }
            }
        }).mmtExecute(mFlowInfoConfig.FlowName, String.valueOf(userId), station);
    }

    @Override
    public void onUserSelected(String userIds, String option) {
        mFlowInfoPostParam.caseInfo.Undertakeman = userIds;
        mFlowInfoPostParam.caseInfo.Opinion = option;

        dispatchEvent();
    }

    private void dispatchEvent() {

        if (feedbackItems == null) {
            return;
        }

        // Event Dispatch
        mFlowInfoPostParam.caseInfo.FlowName = mFlowInfoConfig.FlowName;
        mFlowInfoPostParam.caseInfo.NodeName = mFlowInfoConfig.NodeName;
        mFlowInfoPostParam.caseInfo.FieldGroup = mFlowInfoConfig.FieldGroup;

        mFlowInfoPostParam.caseInfo.EventMainTable = mEventItem.EventMainTable;
        mFlowInfoPostParam.caseInfo.IsCreate = Integer.parseInt(mEventItem.IsCreate);
        mFlowInfoPostParam.caseInfo.EventName = mEventItem.EventName;
        mFlowInfoPostParam.caseInfo.EventCode = mEventItem.EventCode;
        mFlowInfoPostParam.caseInfo.BizCode = mEventItem.BizCode;
        mFlowInfoPostParam.caseInfo.Station = mEventItem.DealStation;
        mFlowInfoPostParam.caseInfo.Direction = 1;
        mFlowInfoPostParam.caseInfo.TableGroup = "";
        mFlowInfoPostParam.caseInfo.UserID = userId;

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = mFlowInfoPostParam.flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                    break;
                }
            }
        }

        // 创建服务路径
        String uri;
        if (mMode == EventListActivity.Mode.RECEIVE) {
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/ReceiveCaseFromMobile"
                    + "?eventID=" + mEventItem.ID + "&EventMainTable=" + mEventItem.EventMainTable;
        } else {
            // 默认“调度箱”模式
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostFlowNodeData";
        }

        // 将信息转换为 JSON字符串
        String reportData = new Gson().toJson(mFlowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
            }.getType());

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                userId,
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                mEventItem.EventName,
                mFlowBeanFragment.getAbsolutePaths(),
                mFlowBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {

            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    // 服务 PostFlowNodeData 的返回的状态码为新插入记录的 Id
                    if (result.ResultCode > 0) {
                        Toast.makeText(EventDispatchActivity.this, "上传成功", Toast.LENGTH_SHORT).show();

                        success();
                    } else {
                        Toast.makeText(EventDispatchActivity.this, (mMode == 0 ? "分派失败：" : "领单失败：")
                                + result.ResultMessage, Toast.LENGTH_LONG).show();

                        // 失败返回时候也刷新列表
                        setResult(Activity.RESULT_OK);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    private void success() {

        Toast.makeText(EventDispatchActivity.this, mMode == 0 ? "分派成功" : "领单成功", Toast.LENGTH_SHORT).show();

        // 回退到列表页时控制列表刷新
        setResult(Activity.RESULT_OK);

        if (mMode == EventListActivity.Mode.DISPATCH) {
            this.finish();
        } else if (mMode == EventListActivity.Mode.RECEIVE) {

            // 成功后跳转至列表页面
            Intent intent = new Intent(EventDispatchActivity.this, EventListActivity.class);
            intent.putExtra("MODE", mMode);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addCategory(Constants.CATEGORY_BACK_TO_LIST);

            // Finish detail activity
            AppManager.finishActivity(AppManager.secondLastActivity());
            // Bring list activity to front
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            this.finish();

        }
    }
}
