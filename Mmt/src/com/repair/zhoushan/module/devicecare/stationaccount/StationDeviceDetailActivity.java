package com.repair.zhoushan.module.devicecare.stationaccount;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.ScheduleTask;

import java.util.ArrayList;
import java.util.List;

public class StationDeviceDetailActivity extends BaseActivity {

    private ScheduleTask mScheduleTask;
    private List<FlowNodeMeta> flowNodeMetaList = new ArrayList<FlowNodeMeta>();

    private List<String> titleNameList = new ArrayList<String>();
    private int showFragmentIndex = 0;

    private FrameLayout mainFormMid;
    private LinearLayout mianBottomView;
    private TextView titleView;

    private Fragment[] mFragments;

    @Override
    protected void setDefaultContentView() {

        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");

        setContentView(R.layout.my_done_case_detail_activity);

        this.mainFormMid = (FrameLayout) findViewById(R.id.mainFormMid);
        this.mianBottomView = (LinearLayout) findViewById(R.id.baseBottomView);
        this.titleView = getBaseTextView();
        titleView.setText("任务详情");

        addBackBtnListener(getBaseLeftImageView());

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

        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(StationDeviceDetailActivity.this) {
            @Override
            protected String doInBackground(String... params) {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append(userID).append("/StationTaskTableInfo")
                        .append("?bizName=").append(mScheduleTask.BizName)
                        .append("&taskCode=").append(mScheduleTask.TaskCode);

                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onSuccess(String resultStr) {

                ResultData<FlowNodeMeta> mData = Utils.json2ResultDataToast(FlowNodeMeta.class,
                        StationDeviceDetailActivity.this, resultStr, "获取任务详情失败", false);
                if (mData == null) return;

                flowNodeMetaList = mData.DataList;
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

        int tableMetaDataSize = flowNodeMetaList.size();
        mFragments = new Fragment[tableMetaDataSize];

        FlowNodeMeta flowNodeMeta;
        for (int i = 0; i < tableMetaDataSize; i++) {
            flowNodeMeta = flowNodeMetaList.get(i);

            titleNameList.add(flowNodeMeta.Groups.get(0).GroupName);

            FlowBeanFragment fragment = new FlowBeanFragment();
            Bundle args = new Bundle();
            args.putParcelable("GDFormBean", flowNodeMeta.mapToGDFormBean());
            fragment.setArguments(args);

            fragment.setFragmentFileRelativePath(mScheduleTask.TaskCode);
            fragment.setCls(StationDeviceDetailActivity.class);
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
