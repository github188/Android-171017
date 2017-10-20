package com.repair.zhoushan.module.projectmanage.waterplantpatrolhistory;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;
import com.repair.zhoushan.module.devicecare.consumables.entity.SAPBean;
import com.repair.zhoushan.module.devicecare.consumables.view.ExpandTabView;
import com.repair.zhoushan.module.devicecare.consumables.view.ListTreeSingleListView;
import com.sleepbot.base.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class WaterPlantPatrolHistoryListActivity extends SimplePagerListActivity {

    private ExpandTabView filterTabView;

    private ArrayList<View> mViewArray = new ArrayList<View>();
    // 任务状态
    private ListTreeSingleListView viewLeft;
    // 时间
    private ListTreeSingleListView viewRight;

    private List<SAPBean> taskStateFilterList = new LinkedList<SAPBean>();
    private List<SAPBean> timeFilterList = new LinkedList<SAPBean>();
    private SAPBean curTaskState;
    private SAPBean curTimeRange;
    private String startTime;
    private String endTime;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    @Override
    public void init() {

        final ArrayList<FeedbackMobileModel> feedbackMobileModelList = new ArrayList<FeedbackMobileModel>();

        mSimplePagerListDelegate = new SimplePagerListDelegate<FeedbackMobileModel>(WaterPlantPatrolHistoryListActivity.this,
                feedbackMobileModelList, FeedbackMobileModel.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new FeedbackModelAdapter(WaterPlantPatrolHistoryListActivity.this, feedbackMobileModelList);
            }

            @Override
            protected String generateUrl() {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/MapgisCity_ProjectManage_ZS/REST/ProjectManageREST.svc/Feedback/GetFeedbackResultListForMobile")
                        .append("?pageSize=").append(getPageSize())
                        .append("&pageIndex=").append(getLoadPageIndex())
                        .append("&sortFields=完成时间&direction=desc")
                        .append("&userId=").append(getUserIdStr())
                        .append("&dateFrom=").append(startTime)
                        .append("&dateTo=").append(endTime);

                switch (curTaskState.getCode()) {
                    case "已检":
                        sb.append("&isFeedback=").append("1");
                        break;
                    case "未检":
                        sb.append("&isFeedback=").append("0");
                        break;
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

        taskStateFilterList.add(new SAPBean("全部", "全部"));
        curTaskState = new SAPBean("已检", "已检");
        taskStateFilterList.add(curTaskState);
        taskStateFilterList.add(new SAPBean("未检", "未检"));

        timeFilterList.add(new SAPBean("昨天", "昨天"));
        curTimeRange = new SAPBean("今天", "今天");
        timeFilterList.add(curTimeRange);
        startTime = endTime = dateFormat.format(new Date());
        timeFilterList.add(new SAPBean("自定义", "自定义"));

        this.viewLeft = new ListTreeSingleListView(WaterPlantPatrolHistoryListActivity.this, taskStateFilterList);
        this.viewRight = new ListTreeSingleListView(WaterPlantPatrolHistoryListActivity.this, timeFilterList);
        mViewArray.add(viewLeft);
        mViewArray.add(viewRight);

        ArrayList<String> mTextArray = new ArrayList<String>();
        mTextArray.addAll(Arrays.asList("任务状态", "任务时间"));

        filterTabView.setValue(mTextArray, mViewArray);

        viewLeft.setSelectedPosition(1);
        viewRight.setSelectedPosition(1);

        viewLeft.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {
            @Override
            public void getValue(int selectIndex, String showText) {
                onRefresh(viewLeft, showText);

                if (!curTaskState.getCode().equals(taskStateFilterList.get(selectIndex).getCode())) {
                    curTaskState = taskStateFilterList.get(selectIndex);
                    updateData();
                }
            }
        });

        viewRight.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {

            int lastSelectedIndex = 1;

            @Override
            public void getValue(final int selectIndex, final String showText) {

                if ("自定义".equals(showText)) {

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());

                    final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

                            lastSelectedIndex = selectIndex;

                            int m = month + 1;
                            startTime = endTime = (year + "-" + (m >= 10 ? m : "0" + m) + "-"
                                    + (day >= 10 ? day : "0" + day));

                            onRefresh(viewRight, m + "月" + day + "日");

                            curTimeRange = timeFilterList.get(selectIndex);
                            updateData();
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

                    datePickerDialog.setYearRange(1985, 2028);
                    datePickerDialog.setCloseOnSingleTapDay(false);
                    datePickerDialog.show(WaterPlantPatrolHistoryListActivity.this.getSupportFragmentManager(), "");
                    viewRight.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            datePickerDialog.getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    viewRight.setSelectedPosition(lastSelectedIndex);
                                    filterTabView.onPressBack();
                                }
                            });
                        }
                    }, 300);

                } else {

                    onRefresh(viewRight, showText);
                    lastSelectedIndex = selectIndex;

                    if (!curTimeRange.getCode().equals(timeFilterList.get(selectIndex).getCode())) {

                        curTimeRange = timeFilterList.get(selectIndex);

                        if (curTimeRange.getName().equals("今天")) {
                            startTime = endTime = dateFormat.format(new Date());
                        } else if (curTimeRange.getName().equals("昨天")) {
                            startTime = endTime = dateFormat.format(new Date().getTime() - 24 * 3600 * 1000);
                        }

                        updateData();
                    }
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

    final class FeedbackModelAdapter extends SimpleBaseAdapter {

        private final ArrayList<FeedbackMobileModel> dataList;
        private final LayoutInflater mLayoutInflater;
        private final Activity mContext;

        public FeedbackModelAdapter(Activity mActivity, ArrayList<FeedbackMobileModel> dataList) {
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
                convertView = mLayoutInflater.inflate(R.layout.base_list_item, parent, false);
            }

            final FeedbackMobileModel model = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex)).setText((position + 1) + ".");

            TextView tvTaskCode = MmtViewHolder.get(convertView, R.id.desc_top_left);
            tvTaskCode.setText(model.TaskCode); // 加粗
            tvTaskCode.getPaint().setFakeBoldText(true);

            TextView txtNum = MmtViewHolder.get(convertView, R.id.desc_top_right);
            txtNum.setTextColor(0xFF5677FC);
            if (model.IsFeedback.equals("已检")) {
                if (model.State.equals("异常")) {
                    txtNum.setText(Html.fromHtml("<font color=#5677FC >" + "已检 : " + "</font>" + "<font color=#FF0000 >" + "异常" + "</font>"));
                } else {
                    txtNum.setText(model.IsFeedback + " : " + model.State);
                }
            } else {
                txtNum.setText(model.IsFeedback);
            }

            TextView tvSiteCode = MmtViewHolder.get(convertView, R.id.desc_mid_left);
            tvSiteCode.setText(model.Code + " - " + model.FactoryName);
            tvSiteCode.setTextColor(Color.BLACK);

            TextView tvSiteType = MmtViewHolder.get(convertView, R.id.desc_mid_bottom_left);
            tvSiteType.setText(model.Type + " : " + model.Name);
            tvSiteType.setTextColor(Color.BLACK);

            if (!TextUtils.isEmpty(model.FinishTime)) {

                MmtViewHolder.get(convertView, R.id.desc_bottom_right).setVisibility(View.VISIBLE);
                TextView textView = MmtViewHolder.get(convertView, R.id.desc_bottom_left);
                textView.setVisibility(View.VISIBLE);
                textView.setText(model.FinishTime);

            } else {
                MmtViewHolder.get(convertView, R.id.desc_bottom_left).setVisibility(View.GONE);
                MmtViewHolder.get(convertView, R.id.desc_bottom_right).setVisibility(View.GONE);
            }

            return convertView;
        }

        @Override
        public void onItemClick(int position) {
            super.onItemClick(position);

            FeedbackMobileModel model = dataList.get(position);

            if (model.IsFeedback.equals("已检")) {
                Intent intent = new Intent(mContext, WaterPlantPatrolHistoryDetailActivity.class);
                intent.putExtra("ListItemEntity", model);

                mContext.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
                MyApplication.getInstance().startActivityAnimation(mContext);

            } else {

                Toast.makeText(mContext, "该巡检点暂未反馈", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
