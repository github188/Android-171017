package com.repair.zhoushan.module.projectmanage.projectsitereport;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DateUtil;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.widget.DateSelectDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.areaopr.ShowAreaMapCallback;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;
import com.repair.zhoushan.module.devicecare.consumables.entity.SAPBean;
import com.repair.zhoushan.module.devicecare.consumables.view.ExpandTabView;
import com.repair.zhoushan.module.devicecare.consumables.view.ListTreeSingleListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ProjectSiteReportListActivity extends SimplePagerListActivity {

    private ExpandTabView filterTabView;

    private ArrayList<View> mViewArray = new ArrayList<View>();
    // 项目类别
    private ListTreeSingleListView viewLeft;
    // 时间
    private ListTreeSingleListView viewRight;

    private List<SAPBean> projectTypeFilterList = new LinkedList<SAPBean>();
    private List<SAPBean> timeFilterList = new LinkedList<SAPBean>();

    private SAPBean curProjectType;
    private SAPBean curTimeRange;
    private String startTime;
    private String endTime;

    @Override
    public void init() {

        final ArrayList<ProjectInfoModel> dataList = new ArrayList<ProjectInfoModel>();

        mSimplePagerListDelegate = new SimplePagerListDelegate<ProjectInfoModel>(ProjectSiteReportListActivity.this, dataList, ProjectInfoModel.class) {

            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new ProjectInfoAdapter(ProjectSiteReportListActivity.this, dataList);
            }

            @Override
            protected String generateUrl() {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/MapgisCity_ProjectManage_ZS/REST/ProjectManageREST.svc/ProjectManage/GetProjectInfoForLogWithPaging")
                        .append("?_mid=").append(UUID.randomUUID().toString())
                        .append("&pageSize=").append(getPageSize())
                        .append("&pageIndex=").append(getLoadPageIndex())
                        .append("&projectInfo=").append("")
                        .append("&userId=").append(getUserIdStr())
                        .append("&sortFields=ID&direction=desc");

                if (!curProjectType.getCode().equals("全部")) {
                    sb.append("&projectType=").append(curProjectType.getCode());
                }

                if (!curTimeRange.getCode().equals("全部")) {
                    if (curTimeRange.getCode().equals("自定义")) {
                        if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
                            sb.append("&dateFrom=").append(Uri.encode(startTime))
                                    .append("&dateTo=").append(Uri.encode(endTime));
                        }
                    } else {
                        String[] dateRange = DateUtil.getDateSpanString(curTimeRange.getCode());
                        if (dateRange != null && !BaseClassUtil.isNullOrEmptyString(dateRange[0])
                                && !BaseClassUtil.isNullOrEmptyString(dateRange[1])) {
                            sb.append("&dateFrom=").append(Uri.encode(dateRange[0]))
                                    .append("&dateTo=").append(Uri.encode(dateRange[1]));
                        }
                    }
                }

                return sb.toString();
            }
        };
    }

    @Override
    protected void afterViewCreated() {
        super.afterViewCreated();

        // 初始化 ExpandTabView
        this.filterTabView = (ExpandTabView) findViewById(R.id.filterView);
        filterTabView.setVisibility(View.VISIBLE);

        curProjectType = new SAPBean("全部", "全部");
        curTimeRange = new SAPBean("全部", "全部");
        projectTypeFilterList.add(curProjectType);
        timeFilterList.add(curTimeRange);
        this.viewLeft = new ListTreeSingleListView(ProjectSiteReportListActivity.this, projectTypeFilterList);
        this.viewRight = new ListTreeSingleListView(ProjectSiteReportListActivity.this, timeFilterList);

        mViewArray.add(viewLeft);
        mViewArray.add(viewRight);
        ArrayList<String> mTextArray = new ArrayList<String>();
        mTextArray.addAll(Arrays.asList("项目类别", "项目发起时间"));
        filterTabView.setValue(mTextArray, mViewArray);

        String[] projectTypeArr = {"外来资金", "大修理", "新建", "技改", "设备"};
        String[] timeArr = {"昨天", "本周", "上周", "本月", "上月", "自定义"};
        for (String item : projectTypeArr) {
            projectTypeFilterList.add(new SAPBean(item, item));
        }
        for (String item : timeArr) {
            timeFilterList.add(new SAPBean(item, item));
        }
        viewLeft.setSelectedPosition(0);
        viewRight.setSelectedPosition(0);

        viewLeft.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {
            @Override
            public void getValue(int selectIndex, String showText) {
                onRefresh(viewLeft, showText);

                if (!curProjectType.getCode().equals(projectTypeFilterList.get(selectIndex).getCode())) {
                    curProjectType = projectTypeFilterList.get(selectIndex);
                    updateData();
                }
            }
        });

        viewRight.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {

            int lastSelectedIndex = 0;

            @Override
            public void getValue(final int selectIndex, final String showText) {

                if ("自定义".equals(showText)) {
                    DateSelectDialogFragment dateSelectDialogFragment = new DateSelectDialogFragment() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            super.onCancel(dialog);
                            viewRight.setSelectedPosition(lastSelectedIndex);
                            filterTabView.onPressBack();
                        }
                    };
                    dateSelectDialogFragment.show(ProjectSiteReportListActivity.this.getSupportFragmentManager(), "2");
                    dateSelectDialogFragment.setOnDateSelectPositiveClick(new DateSelectDialogFragment.OnDateSelectPositiveClick() {
                        @Override
                        public void setOnDateSelectPositiveClickListener(View view, String startDate,
                                                                         String endDate, long startTimeLong, long endTimeLong) {
                            onRefresh(viewRight, showText);
                            lastSelectedIndex = selectIndex;

                            startTime = startDate + " 00:00:00";
                            endTime = endDate + " 23:59:59";
                            curTimeRange = timeFilterList.get(selectIndex);

                            updateData();
                        }
                    });
                    dateSelectDialogFragment.setOnDateSelectNegativeClick(new DateSelectDialogFragment.OnDateSelectNegativeClick() {
                        @Override
                        public void setOnDateSelectNegativeClickListener(View view) {
                            viewRight.setSelectedPosition(lastSelectedIndex);
                            filterTabView.onPressBack();
                        }
                    });
                    return;
                }

                onRefresh(viewRight, showText);
                lastSelectedIndex = selectIndex;

                if (!curTimeRange.getCode().equals(timeFilterList.get(selectIndex).getCode())) {
                    curTimeRange = timeFilterList.get(selectIndex);
                    updateData();
                }
            }
        });

    }

    private void onRefresh(View view, String showText) {

        filterTabView.onPressBack();
        int position = mViewArray.indexOf(view);
        if (position >= 0 && !filterTabView.getTitle(position).equals(showText)) {
            filterTabView.setTitle(showText, position);
        }
    }

    @Override
    public void onBackPressed() {

        if (!filterTabView.onPressBack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasCategory(Constants.CATEGORY_BACK_TO_LIST)) {
            intent.removeCategory(Constants.CATEGORY_BACK_TO_LIST);
            updateData();
        }
    }

    final class ProjectInfoAdapter extends SimpleBaseAdapter implements View.OnClickListener {

        private final ArrayList<ProjectInfoModel> dataList;
        private final LayoutInflater mLayoutInflater;
        private final Activity mContext;

        public ProjectInfoAdapter(Activity mActivity, ArrayList<ProjectInfoModel> dataList) {
            this.mContext = mActivity;
            this.mLayoutInflater = LayoutInflater.from(mActivity);
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return this.dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.case_overview_list_item, parent, false);
            }

            final ProjectInfoModel projectInfoModel = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_index))
                    .setText(getString(R.string.string_listitem_index, (position + 1)));

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_top_left)).setText(projectInfoModel.ProjectName);

            TextView tvContractNo = MmtViewHolder.get(convertView, R.id.tv_mid_one_left);
            tvContractNo.setTextColor(Color.DKGRAY);
            tvContractNo.setText(getString(R.string.string_two_with_colon, "合同编号",
                    TextUtils.isEmpty(projectInfoModel.ProjectContractNo) ? "暂无" : projectInfoModel.ProjectContractNo));

            TextView tvProjectType = MmtViewHolder.get(convertView, R.id.tv_mid_one_right);
            tvProjectType.setTextColor(Color.BLUE);
            tvProjectType.setText(projectInfoModel.ProjectType);

            TextView tvReplyNo = MmtViewHolder.get(convertView, R.id.tv_mid_two_left);
            tvReplyNo.setTextColor(Color.DKGRAY);
            tvReplyNo.setText(getString(R.string.string_two_with_colon, "项目批复号",
                            TextUtils.isEmpty(projectInfoModel.ProjectReplyNo) ? "暂无" : projectInfoModel.ProjectReplyNo));

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_mid_three_left)).setText(projectInfoModel.ProjectDesc);

            MmtViewHolder.get(convertView, R.id.tv_mid_four_left).setVisibility(View.GONE);
            MmtViewHolder.get(convertView, R.id.tv_mid_four_right).setVisibility(View.GONE);

            ((TextView) MmtViewHolder.get(convertView, R.id.tv_time)).setText(projectInfoModel.ReportTime);

            TextView tvLoc = MmtViewHolder.get(convertView, R.id.tv_loc);
            tvLoc.setText("区域");
            tvLoc.setTag(String.valueOf(position));
            tvLoc.setOnClickListener(this);

            return convertView;
        }

        @Override
        public void onItemClick(int position) {
            super.onItemClick(position);

            Intent intent = new Intent(mContext, ProjectSiteReportActivity.class);
            intent.putExtra("ListItemEntity", dataList.get(position));

            mContext.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            MyApplication.getInstance().startActivityAnimation(mContext);
        }

        @Override
        public void onClick(View v) {

            // 定位按钮
            if (v.getId() == R.id.tv_loc) {

                ProjectInfoModel model = dataList.get(Integer.parseInt(v.getTag().toString()));

                BaseMapCallback callback = new ShowAreaMapCallback(ProjectSiteReportListActivity.this, model.PlanArea);
                MyApplication.getInstance().sendToBaseMapHandle(callback);

            }
        }
    }
}
