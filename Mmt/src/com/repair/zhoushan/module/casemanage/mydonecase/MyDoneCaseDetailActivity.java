package com.repair.zhoushan.module.casemanage.mydonecase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.CorrectData;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.FlowTableInfo;
import com.repair.zhoushan.entity.TableMetaData;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.casemanage.casedetail.CaseBackTask;
import com.repair.zhoushan.module.casemanage.casedetail.CaseHandleProcedureFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyDoneCaseDetailActivity extends BaseActivity {

    private CaseItem caseItem;
    private FlowTableInfo mFlowTableInfo;

    private List<String> titleNameList = new ArrayList<String>();
    private int showFragmentIndex = 0;

    private FrameLayout mainFormMid;
    private TextView titleView;

    private Fragment[] mFragments;

    @Override
    protected void setDefaultContentView() {

        this.caseItem = getIntent().getParcelableExtra("ListItemEntity");

        setContentView(R.layout.my_done_case_detail_activity);

        this.mainFormMid = (FrameLayout) findViewById(R.id.mainFormMid);
        this.titleView = getBaseTextView();

        addBackBtnListener(getBaseLeftImageView());

        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListDialogFragment listDialogFragment = new ListDialogFragment("工单详情", titleNameList);
                listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int index, String value) {
                        showFragment(index);
                        //动态切换是否有保存补正信息的按钮
                        toggleCorrectBtn(index, value);

                    }
                });
                listDialogFragment.show(getSupportFragmentManager(), "");
            }
        });

        setSwipeBackEnable(false);

        initView();
    }

    View correctBtn;

    private void toggleCorrectBtn(int index, final String tableName) {

        if (correctBtn != null) {
            correctBtn.setVisibility(View.GONE);
        }

        Fragment fragment = mFragments[index];
        if (!(fragment instanceof FlowBeanFragment)) {
            return;
        }

        final FlowBeanFragment flowBeanFragment = (FlowBeanFragment) fragment;
        List<String> exceptFieldList = flowBeanFragment.getExceptFiledList();
        if (exceptFieldList == null || exceptFieldList.size() == 0) {
            return;
        }
        if (correctBtn == null) {
            correctBtn = addBottomUnitView("保存", true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCorrectField(tableName, flowBeanFragment);
                }
            });
        }
        correctBtn.setVisibility(View.VISIBLE);

    }

    private void saveCorrectField(String tableName, FlowBeanFragment fragment) {

        TableMetaData tableMetaData = null;
        for (TableMetaData item : mFlowTableInfo.TableMetaDatas) {
            String tableNameItem = TextUtils.isEmpty(item.TableAlias) ? item.TableName : item.TableAlias;
            if (!tableName.equals(tableNameItem)) {
                continue;
            }
            tableMetaData = item;
            break;
        }
        if (tableMetaData == null) {
            return;
        }


        FlowInfoPostParam flowInfoPostParam = new FlowInfoPostParam();
        flowInfoPostParam.caseInfo = caseItem.mapToCaseInfo();
        flowInfoPostParam.flowNodeMeta = tableMetaData.FlowNodeMeta;

        List<FeedItem> feedItems = fragment.getFeedbackItems(ReportInBackEntity.REPORTING, null, fragment.getExceptFiledList());
        flowInfoPostParam.flowNodeMeta.Values.clear();

        for (FeedItem feedItem : feedItems) {
            FlowNodeMeta.TableValue value = flowInfoPostParam.flowNodeMeta.new TableValue(feedItem.Name, feedItem.Value);
            flowInfoPostParam.flowNodeMeta.Values.add(value);
        }


        final CorrectData correctData = new CorrectData();
        correctData.TableName = tableMetaData.TableName;
        correctData.DataParam = flowInfoPostParam;


        new MmtBaseTask<Void, Void, String>(MyDoneCaseDetailActivity.this) {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                    sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveTableData");

                    return NetUtil.executeHttpPost(sb.toString(), new Gson().toJson(correctData));

                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                if (TextUtils.isEmpty(s)) {
                    MyApplication.getInstance().showMessageWithHandle("服务异常或网络错误");
                }

                boolean isSuccess = Utils.json2ResultToast(MyDoneCaseDetailActivity.this, s, "服务返回错误");
                if (isSuccess) {
                    MyApplication.getInstance().showMessageWithHandle("保存成功");
                }
            }
        }.mmtExecute();
    }

    private void initView() {

        new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String result) {

                ResultData<FlowTableInfo> mData = Utils.json2ResultDataToast(FlowTableInfo.class,
                        MyDoneCaseDetailActivity.this, result, "获取工单详情失败", false);
                if (mData == null) return;

                mFlowTableInfo = mData.getSingleData();

                createView();
                createBottomView();
            }
        }) {
            @Override
            protected String doInBackground(String... params) {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/")
                        .append(userID).append("/GetAllowCorrectInfoByCaseNo?eventCode=")
                        .append(caseItem.EventCode).append("&caseNo=")
                        .append(caseItem.CaseNo).append("&flowName=")
                        .append(caseItem.FlowName);

                return NetUtil.executeHttpGet(sb.toString());
            }
        }.mmtExecute();
    }

    private void createView() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        int tableMetaDataSize = mFlowTableInfo.TableMetaDatas.size();
        mFragments = new Fragment[tableMetaDataSize + 1];

        TableMetaData tableMetaData;
        for (int i = 0; i < tableMetaDataSize; i++) {
            tableMetaData = mFlowTableInfo.TableMetaDatas.get(i);

            titleNameList.add(BaseClassUtil.isNullOrEmptyString(tableMetaData.TableAlias) ? tableMetaData.TableName : tableMetaData.TableAlias);

            FlowBeanFragment fragment = new FlowBeanFragment();
            Bundle args = new Bundle();
            args.putParcelable("GDFormBean", tableMetaData.FlowNodeMeta.mapToGDFormBean());
            fragment.setArguments(args);

            fragment.setFragmentFileRelativePath(caseItem.CaseNo); // caseItem.EventCode
            fragment.setCls(MyDoneCaseDetailActivity.class);
            fragment.setAddEnable(false);

            List<String> exceptFieldList = null;
            if (!TextUtils.isEmpty(tableMetaData.CorrectFields)) {
                exceptFieldList = Arrays.asList(tableMetaData.CorrectFields.split(","));
            }
            fragment.setFormOnlyShow(true, exceptFieldList);

            mFragments[i] = fragment;

            FrameLayout frameLayout = new FrameLayout(this);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            frameLayout.setId(frameLayout.hashCode());

            mainFormMid.addView(frameLayout);

            ft.replace(frameLayout.getId(), fragment);
        }

        titleNameList.add("办理过程");

        CaseHandleProcedureFragment hpFragment = new CaseHandleProcedureFragment();
        Bundle argBundle = new Bundle();
        argBundle.putParcelable("ListItemEntity", caseItem);
        hpFragment.setArguments(argBundle);

        mFragments[tableMetaDataSize] = hpFragment;

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        frameLayout.setId(frameLayout.hashCode());

        mainFormMid.addView(frameLayout);
        ft.replace(frameLayout.getId(), hpFragment);

        // 隐藏所有的Fragment
        for (Fragment frag : mFragments) {
            ft.hide(frag);
        }

        // 显示第一个Fragment
        ft.show(mFragments[0]);
        titleView.setText(titleNameList.get(0));

        ft.commit();

    }

    private void createBottomView() {
        addBottomUnitView("撤回", true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRollback();
            }
        });
    }

    private void doRollback() {

        if (!TextUtils.isEmpty(caseItem.NextStepID) && "0".equals(caseItem.IsOver)) {
            final OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("要撤回此工单?");
            okCancelDialogFragment.setOnRightButtonClickListener(
                    new OkCancelDialogFragment.OnRightButtonClickListener() {
                        @Override
                        public void onRightButtonClick(View view) {

                            CaseInfo caseInfo = caseItem.mapToCaseInfo();
                            caseInfo.NodeName = caseItem.UndertakeNodes;
                            new CaseBackTask(MyDoneCaseDetailActivity.this, true, "CaseDrawBack", "撤回失败",
                                    new MmtBaseTask.OnWxyhTaskListener<String>() {
                                @Override
                                public void doAfter(String result) {

                                    if (!TextUtils.isEmpty(result)) {
                                        Toast.makeText(MyDoneCaseDetailActivity.this, result, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MyDoneCaseDetailActivity.this, "撤回成功", Toast.LENGTH_SHORT).show();
                                        //成功后自己打开自己，达到重置界面的目的
                                        Intent intent = new Intent(MyDoneCaseDetailActivity.this, MyDoneCaseListActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                    okCancelDialogFragment.dismiss();
                                }
                            }).mmtExecute(caseInfo);
                        }
                    }
            );
            okCancelDialogFragment.setCancelable(true);
            okCancelDialogFragment.show(getSupportFragmentManager(), "");
        } else {
            Toast.makeText(this, "已被后续人员处理，无法撤回", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFragment(int index) {

        if (showFragmentIndex != index) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(mFragments[showFragmentIndex]);
            ft.show(mFragments[index]);
            titleView.setText(titleNameList.get(index));
            showFragmentIndex = index;
            ft.commit();
        }
    }
}
