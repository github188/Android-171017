package com.repair.zhoushan.module.devicecare.stationaccount;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DateUtil;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.DateSelectDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.SAPBean;
import com.repair.zhoushan.module.devicecare.consumables.view.ExpandTabView;
import com.repair.zhoushan.module.devicecare.consumables.view.ListTreeSingleListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class StationAccountListActivity extends SimplePagerListActivity {

    //region Variable for Filter

    private ExpandTabView filterTabView;

    private ArrayList<View> mViewArray = new ArrayList<View>();
    // 场站名称
    private ListTreeSingleListView viewLeft;
    // 设备类型
    private ListTreeSingleListView viewMid;
    // 设备类型
    private ListTreeSingleListView viewRight;
    // 养护时间
    private ListTreeSingleListView viewRightest;

    private String bizName = "";

    private List<SAPBean> stationNameFilterList = new LinkedList<SAPBean>();
    private List<SAPBean> deviceTypeFilterList = new LinkedList<SAPBean>();
    private List<SAPBean> taskStateFilterList = new LinkedList<SAPBean>();
    private List<SAPBean> timeFilterList = new LinkedList<SAPBean>();

    private SAPBean curStationName;
    private SAPBean curDeviceType;
    private SAPBean curTaskState;
    private SAPBean curTimeRange;
    private String startTime;
    private String endTime;

    //endregion

    @Override
    public void init() {

        this.bizName = getIntent().getStringExtra("BizName");

//        if (TextUtils.isEmpty(bizName)) {
//            Toast.makeText(StationAccountListActivity.this, "缺少业务类型参数", Toast.LENGTH_SHORT).show();
//            return;
//        }

        final ArrayList<ScheduleTask> dataList = new ArrayList<ScheduleTask>();

        mSimplePagerListDelegate = new SimplePagerListDelegate<ScheduleTask>(StationAccountListActivity.this, dataList, ScheduleTask.class) {

            private String userName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new ScheduleTaskAdapter(StationAccountListActivity.this, dataList);
            }

            @Override
            protected String generateUrl() {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());

                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append(getUserIdStr())
                        .append("/StationScheduleTasks?_mid=").append(UUID.randomUUID().toString())
                        .append("&bizName=").append(bizName)
                        .append("&cureMan=").append(Uri.encode(userName))
                        .append("&pageSize=").append(getPageSize())
                        .append("&pageIndex=").append(getLoadPageIndex())
                        .append("&sortFields=开始时间&direction=desc");

                if (curTaskState == null) {
                    sb.append("&checkState=").append("已分派");
                } else {
                    sb.append("&checkState=").append(curTaskState.getCode());
                }

                if (curStationName != null && !curStationName.getCode().equals("全部")) {
                    sb.append("&stationName=").append(curStationName.getCode());
                }
                if (curDeviceType != null && !curDeviceType.getCode().equals("全部")) {
                    sb.append("&equipmentType=").append(curDeviceType.getCode());
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

        curStationName = new SAPBean("全部", "全部");
        stationNameFilterList.add(curStationName);

        curDeviceType = new SAPBean("全部", "全部");
        deviceTypeFilterList.add(curDeviceType);

        curTaskState = new SAPBean("已分派", "已分派");
        taskStateFilterList.add(curTaskState);
        taskStateFilterList.add(new SAPBean("已完成", "已完成"));

        String[] timeArr = {"全部", "昨天", "本周", "上周", "本月", "上月", "自定义"};
        for (String item : timeArr) {
            timeFilterList.add(new SAPBean(item, item));
        }
        curTimeRange = timeFilterList.get(0);

        this.viewLeft = new ListTreeSingleListView(StationAccountListActivity.this, stationNameFilterList);
        this.viewMid = new ListTreeSingleListView(StationAccountListActivity.this, deviceTypeFilterList);
        this.viewRight = new ListTreeSingleListView(StationAccountListActivity.this, taskStateFilterList);
        this.viewRightest = new ListTreeSingleListView(StationAccountListActivity.this, timeFilterList);

        mViewArray.add(viewLeft);
        mViewArray.add(viewMid);
        mViewArray.add(viewRight);
        mViewArray.add(viewRightest);

        ArrayList<String> mTextArray = new ArrayList<String>();
        mTextArray.addAll(Arrays.asList("场站名称", "设备类型", "任务状态", "任务时间"));
        filterTabView.setValue(mTextArray, mViewArray);

        viewLeft.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {
            @Override
            public void getValue(int selectIndex, String showText) {
                onRefresh(viewLeft, showText);

                if (!curStationName.getCode().equals(stationNameFilterList.get(selectIndex).getCode())) {
                    curStationName = stationNameFilterList.get(selectIndex);
                    updateData();
                }
            }
        });

        viewMid.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {
            @Override
            public void getValue(int selectIndex, String showText) {
                onRefresh(viewMid, showText);

                if (!curDeviceType.getCode().equals(deviceTypeFilterList.get(selectIndex).getCode())) {
                    curDeviceType = deviceTypeFilterList.get(selectIndex);
                    updateData();
                }
            }
        });

        viewRight.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {
            @Override
            public void getValue(int selectIndex, String showText) {
                onRefresh(viewRight, showText);

                if (!curTaskState.getCode().equals(taskStateFilterList.get(selectIndex).getCode())) {
                    curTaskState = taskStateFilterList.get(selectIndex);
                    updateData();
                }
            }
        });

        viewRightest.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {

            int lastSelectedIndex = 0;

            @Override
            public void getValue(final int selectIndex, final String showText) {

                if ("自定义".equals(showText)) {
                    DateSelectDialogFragment dateSelectDialogFragment = new DateSelectDialogFragment() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            super.onCancel(dialog);
                            viewRightest.setSelectedPosition(lastSelectedIndex);
                            filterTabView.onPressBack();
                        }
                    };
                    dateSelectDialogFragment.show(StationAccountListActivity.this.getSupportFragmentManager(), "2");
                    dateSelectDialogFragment.setOnDateSelectPositiveClick(new DateSelectDialogFragment.OnDateSelectPositiveClick() {
                        @Override
                        public void setOnDateSelectPositiveClickListener(View view, String startDate,
                                                                         String endDate, long startTimeLong, long endTimeLong) {
                            onRefresh(viewRightest, showText);
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
                            viewRightest.setSelectedPosition(lastSelectedIndex);
                            filterTabView.onPressBack();
                        }
                    });
                    return;
                }

                onRefresh(viewRightest, showText);
                lastSelectedIndex = selectIndex;

                if (!curTimeRange.getCode().equals(timeFilterList.get(selectIndex).getCode())) {
                    curTimeRange = timeFilterList.get(selectIndex);
                    updateData();
                }
            }
        });

        getFilterData();
    }

    private void getFilterData() {

        MmtBaseTask<Void, Void, String[]> mmtBaseTask = new MmtBaseTask<Void, Void, String[]>(StationAccountListActivity.this) {

            @Override
            protected String[] doInBackground(Void... params) {

                // 0.StationName;  1.DeviceType;
                String[] results = new String[2];

                String url0 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/" + userID + "/StationNames";

                results[0] = NetUtil.executeHttpGet(url0, "bizName", bizName, "level", String.valueOf(1));

                String url1 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/" + userID + "/DeviceNames";

                results[1] = NetUtil.executeHttpGet(url1, "bizName", bizName, "level", String.valueOf(2));

                return results;
            }

            @Override
            protected void onSuccess(String[] results) {

                ResultData<String> stationNameData = Utils.json2ResultDataToast(String.class,
                        StationAccountListActivity.this, results[0], "获取场站名称列表失败", false);
                if (stationNameData != null) {
                    for (String name : stationNameData.DataList) {
                        stationNameFilterList.add(new SAPBean(name, name));
                    }
                }
                viewLeft.setSelectedPosition(0);

                ResultData<String> deviceTypeData = Utils.json2ResultDataToast(String.class,
                        StationAccountListActivity.this, results[1], "获取设备类型列表失败", false);
                if (deviceTypeData != null) {
                    for (String name : deviceTypeData.DataList) {
                        deviceTypeFilterList.add(new SAPBean(name, name));
                    }
                }
                viewMid.setSelectedPosition(0);
                viewRight.setSelectedPosition(0);
                viewRightest.setSelectedPosition(0);
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
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

    final class ScheduleTaskAdapter extends SimpleBaseAdapter {

        private final ArrayList<ScheduleTask> dataList;
        private final LayoutInflater mLayoutInflater;
        private final Activity mContext;

        public ScheduleTaskAdapter(Activity mActivity, ArrayList<ScheduleTask> dataList) {
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
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.base_list_item, null);
            }

            ScheduleTask scheduleTask = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex)).setText((position + 1) + ".");

            TextView taskCode = MmtViewHolder.get(convertView, R.id.desc_top_left);
            taskCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            taskCode.getPaint().setFakeBoldText(true);
            //stationInfo.setTextColor(Color.parseColor("#673ab7"));
            taskCode.setText(scheduleTask.getColumnValueByName("任务编号"));

            TextView deviceInfo = MmtViewHolder.get(convertView, R.id.desc_top_right);
            deviceInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            deviceInfo.setTextColor(Color.parseColor("#5677fc"));
            deviceInfo.setText(scheduleTask.getColumnValueByName("养护周期"));

            TextView stationDeviceInfo = MmtViewHolder.get(convertView, R.id.desc_mid_left);
            stationDeviceInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            stationDeviceInfo.setTextColor(Color.BLACK);

            if ("场站设备".equals(bizName) || "场站设备检定".equals(bizName)) {

                String summaryInfo = scheduleTask.getColumnValueByName("场站名称");

                String deviceType = scheduleTask.getColumnValueByName("设备类型");
                String deviceName = scheduleTask.getColumnValueByName("设备名称");
                if (!TextUtils.isEmpty(deviceName) && !deviceName.equals(deviceType)) {
                    summaryInfo += (" /  " + deviceType + " /  " + deviceName);
                } else {
                    summaryInfo += (" /  " + deviceType);
                }
                String deviceNo = scheduleTask.getColumnValueByName("设备编号");
                if (!TextUtils.isEmpty(deviceNo)) {
                    summaryInfo += (" /  " + deviceNo);
                }

                stationDeviceInfo.setText(summaryInfo);

                // 站名/设备类型/设备名称/设备编号/反馈项
            } else if ("车用设备".equals(bizName)) {
                stationDeviceInfo.setText(scheduleTask.getColumnValueByName("场站名称") + " / " + scheduleTask.getColumnValueByName("设备类型")
                        + " - " + scheduleTask.getColumnValueByName("设备编号") + " / " + scheduleTask.getColumnValueByName("部件类型"));
            }

            TextView careContent = MmtViewHolder.get(convertView, R.id.desc_mid_bottom_left);
            careContent.setVisibility(View.VISIBLE);
            careContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            careContent.setTextColor(Color.BLACK);
            careContent.setText(scheduleTask.getColumnValueByName("养护内容"));

            TextView startTime = MmtViewHolder.get(convertView, R.id.desc_bottom_left);
            startTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            startTime.setTextColor(0xFF808A87);
            startTime.setText(scheduleTask.getColumnValueByName("开始时间"));

            TextView finishTime = MmtViewHolder.get(convertView, R.id.desc_bottom_right);
            finishTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
            finishTime.setTextColor(0xFF808A87);
            finishTime.setText(scheduleTask.getColumnValueByName("结束时间"));

            return convertView;
        }

        @Override
        public void onItemClick(int position) {
            super.onItemClick(position);

            Intent intent = new Intent(mContext, StationDeviceFeedbackActivity.class);
            intent.putExtra("ListItemEntity", dataList.get(position));

            mContext.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            MyApplication.getInstance().startActivityAnimation(mContext);
        }
    }

}
