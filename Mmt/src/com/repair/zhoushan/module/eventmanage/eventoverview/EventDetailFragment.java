package com.repair.zhoushan.module.eventmanage.eventoverview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.slidingtab.SlidingTabLayout;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.EventItem;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

public class EventDetailFragment extends Fragment {

    private EventItem mEventItem;
    private GDFormBean gdFormBean;

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey("ListItemEntity")) {
            mEventItem = args.getParcelable("ListItemEntity");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);

        loadData();
    }

    private void loadData() {

        if (mEventItem == null) {
            return;
        }

        new MmtBaseTask<String, Void, String>(getActivity(), true, new MmtBaseTask.OnWxyhTaskListener<String>() {

            @Override
            public void doAfter(String resultStr) {

                ResultData<FlowNodeMeta> newData = Utils.json2ResultDataActivity(
                        FlowNodeMeta.class, (BaseActivity) getActivity(), resultStr, "获取事件详情失败", false);
                if (newData == null) return;

                gdFormBean = newData.getSingleData().mapToGDFormBean();
                gdFormBean.setOnlyShow();

                createView();
            }
        }) {
            @Override
            protected String doInBackground(String... params) {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FlexAndMobile/GetEventDetailInfo")
                        .append("?tableName=").append(mEventItem.EventMainTable)
                        .append("&eventName=").append(mEventItem.EventName)
                        .append("&eventCode=").append(mEventItem.EventCode)
                        .append("&fieldGroup=").append(mEventItem.FieldGroup);

                return NetUtil.executeHttpGet(sb.toString());
            }
        }.mmtExecute();
    }

    private void createView() {

        mViewPager.setAdapter(new ViewPagerAdapter(getFragmentManager(), new String[]{"事件信息", "事件流程"}));
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private String[] pageTitles;

        public ViewPagerAdapter(FragmentManager fm, String[] pageTitles) {
            super(fm);
            this.pageTitles = pageTitles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0) {

                Bundle args = new Bundle();
                args.putParcelable("GDFormBean", gdFormBean);
                return Fragment.instantiate(getActivity(), FlowBeanFragment.class.getName(), args);

            } else if (position == 1) {

                Bundle args = new Bundle();
                args.putParcelable("ListItemEntity", mEventItem);
                return Fragment.instantiate(getActivity(), EventFlowInfoFragment.class.getName(), args);
            }
            return null;
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }
    }
}
