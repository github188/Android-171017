package com.repair.zhoushan.module.devicecare.carehistory;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.slidingtab.SlidingTabLayout;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.EventItem;
import com.repair.zhoushan.module.devicecare.ScheduleTask;

import java.util.ArrayList;

public class CareHistoryListActivity extends BaseActivity {

    private ScheduleTask mScheduleTask;

    private final ArrayList<EventItem> relatedEvents = new ArrayList<>();

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    private boolean hasRiskEvent;

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);

        this.mScheduleTask = getIntent().getParcelableExtra("ScheduleTask");
        this.hasRiskEvent = getIntent().getBooleanExtra("HasRiskEvent", false);

        setContentView(R.layout.activity_care_overview_detail);
        initView();
    }

    private void initView() {

        getBaseTextView().setText("历史记录");

        addBackBtnListener(getBaseLeftImageView());

        this.slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        this.viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);

        fetchDeviceRelateEvent();

    }

    private void fetchDeviceRelateEvent() {

        MmtBaseTask<String, Void, ResultData<EventItem>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<EventItem>>(CareHistoryListActivity.this) {
            @Override
            protected ResultData<EventItem> doInBackground(String... params) {

                ResultData<EventItem> resultData;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/"
                        + userID + "/GetDeviceRelateEvent?events=" + mScheduleTask.RelateEvent
                        + "&gisCode=" + mScheduleTask.GisCode
                        + "&deviceCode=" + mScheduleTask.DeviceCode;

                try {
                    String jsonResult = NetUtil.executeHttpGet(url);
                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取关联事件信息失败：网络错误");
                    }
                    Results<EventItem> results = new Gson().fromJson(jsonResult, new TypeToken<Results<EventItem>>() {
                    }.getType());
                    resultData = results.toResultData();

                } catch (Exception e) {
                    e.printStackTrace();
                    resultData = new ResultData<>();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }
                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<EventItem> resultData) {

                if (resultData.DataList != null && resultData.DataList.size() > 0) {
                    relatedEvents.addAll(resultData.DataList);
                }

                ArrayList<String> titles = new ArrayList<>();
                titles.add("历史记录");
                if (relatedEvents.size() > 0) {
                    titles.add("关联事件");
                }
                if (hasRiskEvent) {
                    titles.add("隐患事件");
                }

                viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(),
                        titles.toArray(new String[titles.size()])));

                if (titles.size() > 1) {
                    slidingTabLayout.setViewPager(viewPager);
                } else {
                    slidingTabLayout.setVisibility(View.GONE);
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final String[] pageTitles;

        ViewPagerAdapter(FragmentManager fm, String[] pageTitles) {
            super(fm);
            this.pageTitles = pageTitles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }

        @Override
        public Fragment getItem(int position) {

            switch (pageTitles[position]) {
                case "历史记录":
                    return CareHistoryListFragment.getInstance(mScheduleTask.BizName,
                            mScheduleTask.GisCode, mScheduleTask.ID);
                case "关联事件":
                    return RelatedEventListFragment.getInstance(relatedEvents);
                case "隐患事件":
                    return RiskEventListFragment.getInstance(mScheduleTask.TaskCode);
                default:
                    return null;
            }
        }
    }

}
