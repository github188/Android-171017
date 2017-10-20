package com.repair.zhoushan.module.devicecare.szd;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowTableInfo;
import com.repair.zhoushan.entity.TableMetaData;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.ScheduleTask;

import java.util.ArrayList;
import java.util.List;

public class SZDDetailActivity extends BaseActivity {
    private ScheduleTask mScheduleTask;
    private FlowTableInfo mFlowTableInfo;

    private List<String> titleNameList = new ArrayList<String>();
    private int showFragmentIndex = 0;

    private FrameLayout mainFormMid;
    private TextView titleView;

    private Fragment[] mFragments;

    @Override
    protected void setDefaultContentView() {
        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");

        setContentView(R.layout.my_done_case_detail_activity);

        this.mainFormMid = (FrameLayout) findViewById(R.id.mainFormMid);
        this.titleView = getBaseTextView();
        titleView.setText("任务详情");

        getBaseLeftImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListDialogFragment listDialogFragment = new ListDialogFragment("任务详情", titleNameList);
                listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int index, String value) {
                        showFragment(index);
                    }
                });
                listDialogFragment.show(getSupportFragmentManager(), "");
            }
        });

        setSwipeBackEnable(false);

        initView();
    }

    private void initView() {
        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(SZDDetailActivity.this) {
            @Override
            protected String doInBackground(String... params) {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());

                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append(userID).append("/TaskTableInfo")
                        .append("?bizName=").append(mScheduleTask.BizName)
                        .append("&taskCode=").append(mScheduleTask.TaskCode);

                return NetUtil.executeHttpGet(sb.toString());
            }

            protected void onSuccess(String json) {
                ResultData<FlowTableInfo> mData = Utils.json2ResultDataToast(FlowTableInfo.class,
                        SZDDetailActivity.this, json, "获取任务详情失败", false);

                if (mData == null)
                    return;

                mFlowTableInfo = mData.getSingleData();

                createView();
            }
        };

        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
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

    private void createView() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        int tableMetaDataSize = mFlowTableInfo.TableMetaDatas.size();
        mFragments = new Fragment[tableMetaDataSize];

        TableMetaData tableMetaData;
        for (int i = 0; i < tableMetaDataSize; i++) {
            tableMetaData = mFlowTableInfo.TableMetaDatas.get(i);

            titleNameList.add(BaseClassUtil.isNullOrEmptyString(tableMetaData.TableAlias) ? tableMetaData.TableName : tableMetaData.TableAlias);

            FlowBeanFragment fragment = new FlowBeanFragment();
            Bundle args = new Bundle();
            args.putParcelable("GDFormBean", tableMetaData.FlowNodeMeta.mapToGDFormBean());
            fragment.setArguments(args);

            fragment.setFragmentFileRelativePath(mScheduleTask.TaskCode);
            fragment.setCls(SZDDetailActivity.class);
            fragment.setAddEnable(false);
            fragment.setFormOnlyShow();

            mFragments[i] = fragment;

            FrameLayout frameLayout = new FrameLayout(this);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            frameLayout.setId(frameLayout.hashCode());

            mainFormMid.addView(frameLayout);

            ft.replace(frameLayout.getId(), fragment);
        }

        // 隐藏所有的Fragment
        for (Fragment frag : mFragments) {
            ft.hide(frag);
        }

        // 显示第一个Fragment
        ft.show(mFragments[0]);
        titleView.setText(titleNameList.get(0));

        ft.commit();

        if (titleNameList.size() == 1) {
            titleView.setText("任务详情");
            titleView.setClickable(false);
            titleView.setCompoundDrawables(null, null, null, null);
        }
    }
}
