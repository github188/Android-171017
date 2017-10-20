package com.patrol.module.feedback;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.patrol.common.MyPlanUtil;
import com.patrol.entity.KeyPoint;
import com.patrol.module.KeyPoint.PointListFragment;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FeedbackData;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.flowcenter.FlowCenterNavigationActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlanFeedbackFragment extends Fragment {

    private KeyPoint keyPoint;

    private String defaultParam;
    private String ID;

    // 上报信息
    private final FeedbackData feedbackData = new FeedbackData();
    // 主体界面
    protected FlowBeanFragment formBeanFragment;

    private FragmentActivity hostActivity;

    public static PlanFeedbackFragment newInstance(KeyPoint kp, String flowName, String nodeName, String defaultParam) {
        //巡检反馈前是否需要先上报一次事件
        hasReportEvent = MyApplication.getInstance().getConfigValue("EventBeforePatrol", 0) <= 0;

        Bundle args = new Bundle();

        args.putParcelable("KeyPoint", kp);
        args.putString("FlowName", flowName);
        args.putString("NodeName", nodeName);
        args.putString("DefaultParam", defaultParam);

        PlanFeedbackFragment fragment = new PlanFeedbackFragment();

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = (FragmentActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        this.keyPoint = args.getParcelable("KeyPoint");
        if (keyPoint == null) {
            Toast.makeText(getContext(), "获取参数失败", Toast.LENGTH_SHORT).show();
            return;
        }

        this.defaultParam = args.getString("DefaultParam", "");
        this.ID = String.valueOf(keyPoint.ID);

        String flowName = args.getString("FlowName", "");
        String nodeName = args.getString("NodeName", "");
        String feedbackID = TextUtils.isEmpty(String.valueOf(keyPoint.FeedbackID)) ? "" : String.valueOf(keyPoint.FeedbackID);

        fetchFeedbackTableInfo(keyPoint.LayerName, flowName, nodeName, feedbackID, defaultParam);
    }

    private void fetchFeedbackTableInfo(String layerName, String flowName, String nodeName, String feedbackID, String defaultParam) {

        new MmtBaseTask<String, Void, String>(getActivity()) {
            @Override
            protected String doInBackground(String... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetFeedbackTableInfo";

                return NetUtil.executeHttpGet(url,
                        "layerName", params[0], "flowName", params[1], "nodeName", params[2], "feedbackID", params[3], "defaultParam", params[4]);
            }

            @Override
            protected void onSuccess(String jsonResult) {

                ResultData<FlowNodeMeta> result = Utils.json2ResultDataToast(FlowNodeMeta.class,
                        getActivity(), jsonResult, "未正确获取信息", false);
                if (result == null) return;

                feedbackData.DataParam.flowNodeMeta = result.getSingleData();
                feedbackData.TableName = feedbackData.DataParam.flowNodeMeta.Groups.get(0).Schema.get(0).TableName;

                createView(feedbackData.DataParam.flowNodeMeta.mapToGDFormBean());
            }
        }.mmtExecute(layerName, flowName, nodeName, feedbackID, defaultParam);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.base_fragment_noactionbar, container, false);
    }

    private void createView(GDFormBean gdFormBean) {

        formBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", gdFormBean);
        formBeanFragment.setArguments(args);

        formBeanFragment.setCls(PlanFeedbackActivity.class);
        formBeanFragment.setAddEnable(true);
        addFragment(formBeanFragment);

        createBottomView();
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(com.mapgis.mmt.R.id.baseFragment, fragment);
        ft.show(fragment);

        if (!fragment.isRemoving()) {
            ft.commitAllowingStateLoss();
        }
    }

    private void createBottomView() {

        View view = getView();
        if (view != null) {
            LinearLayout bottomView = (LinearLayout) view.findViewById(com.mapgis.mmt.R.id.baseBottomView);
            bottomView.setVisibility(View.VISIBLE);

            BottomUnitView feedbackUnitView = new BottomUnitView(getContext());
            feedbackUnitView.setContent("反馈");
            bottomView.addView(feedbackUnitView);
            feedbackUnitView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MmtBaseThemeAlertDialog));
                    builder.setTitle("提示")
                            .setMessage("是否确认反馈？")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("反馈", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doFeedback();
                                }
                            }).show();
                }
            });
        }
    }

    public static boolean hasReportEvent = false;

    private void doFeedback() {
        if (keyPoint.IsArrive == 0) {
            Toast.makeText(getContext(), "该巡线点还未到位，不能反馈", Toast.LENGTH_SHORT).show();
            return;
        }

        if (formBeanFragment == null) {
            return;
        }

        if (MyApplication.getInstance().getConfigValue("MulArriveFeedBack", 1) == 0 && keyPoint.IsFeedback == 1) {
            Toast.makeText(getContext(), "该巡线点已反馈，不能反馈", Toast.LENGTH_SHORT).show();
            return;
        }

        List<FeedItem> feedItemList = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
        if (feedItemList == null) {
            return;
        }

        if (!hasReportEvent) {
            Intent intent = new Intent(getActivity(), FlowCenterNavigationActivity.class);

            Bundle bundle = new Bundle();

            bundle.putString("layerName", keyPoint.GisLayer); // GIS图层
            bundle.putString("filedVal", keyPoint.FieldValue); // GIS编号
            bundle.putString("position", keyPoint.Position); // 坐标
            bundle.putString("addr", ""); // 地址

            bundle.putString("patrolNo", String.valueOf(keyPoint.ID));

            intent.putExtra("gisInfo", bundle);

            startActivity(intent);

            return;
        }

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = feedbackData.DataParam.flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedItemList) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                    break;
                }
            }
        }

        feedbackData.DefaultParam = defaultParam;

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveFeedbackData";

        // 将对信息转换为JSON字符串
        String data = new Gson().toJson(feedbackData, new TypeToken<FeedbackData>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                data,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                "",
                formBeanFragment.getAbsolutePaths(),
                formBeanFragment.getRelativePaths());

        MmtBaseTask<ReportInBackEntity, String, ResultWithoutData> mmtBaseTask = new MmtBaseTask<ReportInBackEntity, String, ResultWithoutData>(getContext()) {
            @Override
            protected ResultWithoutData doInBackground(ReportInBackEntity... params) {
                ResultWithoutData resultWithoutData = null;

                try {
                    // 反馈信息上报
                    ResultData<Integer> resultDatas = params[0].report(this);

                    if (resultDatas.ResultCode < 0) {
                        return resultDatas;
                    }

                    // ReportInBackEntity的上报方法返回的DataList第一个值是网络返回的状态码
                    if (resultDatas.DataList.size() < 2 || resultDatas.DataList.get(1) < 0) {
                        resultDatas.ResultCode = -100;
                        return resultDatas;
                    }
                    // 更新反馈表
                    int feedbackId = resultDatas.DataList.get(1);

                    // ==0：feedbackID已存在不需要更新；==-1：反馈失败；>0：返回新插入的id
                    if (feedbackId == 0) {
                        return resultDatas;
                    }

                    String url = MyPlanUtil.getStandardURL() + "/FeedbackTask";
                    String userID = String.valueOf(MyApplication.getInstance().getUserId());

                    String json = NetUtil.executeHttpGet(url,
                            "id", ID, "userID", userID, "feedbackID", String.valueOf(feedbackId));

                    if (BaseClassUtil.isNullOrEmptyString(json)) {
                        resultWithoutData = new ResultWithoutData();
                        resultWithoutData.ResultMessage = "更新反馈信息失败";
                        return resultWithoutData;
                    }

                    resultWithoutData = ResultWithoutData.fromJson(json);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return resultWithoutData;
            }

            @Override
            protected void onSuccess(ResultWithoutData data) {
                if (data == null) {
                    Toast.makeText(getContext(), "反馈失败", Toast.LENGTH_SHORT).show();
                } else if (data.ResultCode < 0) {
                    String errorMsg = "反馈失败";
                    if (!BaseClassUtil.isNullOrEmptyString(data.ResultMessage)) {
                        errorMsg = data.ResultMessage;
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "反馈成功", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.putExtra("ID", ID);

                    hostActivity.setResult(200, intent);
                    hostActivity.finish();

                    EventBus.getDefault().post(new PointListFragment.FeedBackSuccessEvent());
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute(entity);
    }

}
