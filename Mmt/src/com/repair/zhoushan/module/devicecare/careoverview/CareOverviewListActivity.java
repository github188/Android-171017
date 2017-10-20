package com.repair.zhoushan.module.devicecare.careoverview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.slidingtab.SlidingTabLayout;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;

import net.tsz.afinal.core.Arrays;

import java.util.List;

public class CareOverviewListActivity extends BaseActivity implements CareListFragment.Searchable {

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter pagerAdapter;

    private List<String> bizNameList;

    private EditText txtSearch; // ActionBar搜索框
    private String[] searchKeys;

    @Override
    protected void setDefaultContentView() {

        setSwipeBackEnable(false);
        setContentView(R.layout.activity_care_overview);

        initView();
    }

    private void initView() {

        initActionBar();

        this.mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        this.mViewPager = (ViewPager) findViewById(R.id.viewpager);

        fetchCareBizNames();
    }

    private void fetchCareBizNames() {

        MmtBaseTask<Void, Void, ResultData<String>> mmtBaseTask
                = new MmtBaseTask<Void, Void, ResultData<String>>(CareOverviewListActivity.this) {

            @Override
            protected ResultData<String> doInBackground(Void... params) {

                ResultData<String> resultData;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FetchYHBizNames";
                try {

                    String jsonResult = NetUtil.executeHttpGet(url);

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取业务列表失败");
                    }
                    resultData = new Gson().fromJson(jsonResult, new TypeToken<ResultData<String>>() {
                    }.getType());

                } catch (Exception e) {
                    e.printStackTrace();
                    resultData = new ResultData<>();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<String> result) {
                if (result.ResultCode != 200) {
                    Toast.makeText(CareOverviewListActivity.this, result.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    bizNameList = result.DataList;

                    bizNameList = Arrays.asList("场站设备", "场站设备检定", "车用设备检定", "调压器养护", "阀门养护");

                    // 不同 Tab页的当前搜索关键字列表
                    searchKeys = new String[bizNameList.size()];
                    Arrays.fill(searchKeys, "");

                    createView();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void createView() {

        mViewPager.setOffscreenPageLimit(10);

        this.pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                bizNameList.toArray(new String[bizNameList.size()]));
        mViewPager.setAdapter(pagerAdapter);
        mSlidingTabLayout.setViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                txtSearch.setText(searchKeys[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void initActionBar() {

        addBackBtnListener(findViewById(R.id.btnBack));

        findViewById(R.id.btnOtherAction).setVisibility(View.GONE);

        this.txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.setHint("任务编号");
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CareOverviewListActivity.this, CaseSearchActivity.class);

                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "任务编号");
                intent.putExtra("searchHistoryKey", "CareTaskOverviewSearchKey");

                startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1 && requestCode == Constants.SEARCH_REQUEST_CODE) {
            // 搜索界面返回的搜索关键字
            String key = data.getStringExtra("key"); //返回空代表查全部
            searchKeys[mViewPager.getCurrentItem()] = key;
            txtSearch.setText(key);

            refreshData();
        }
    }

    private void refreshData() {
        ((CareListFragment) pagerAdapter.getFragmentInstance(mViewPager.getCurrentItem())).refreshData();
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final String[] pageTitles;

        private SparseArray<Fragment> fragments;

        public ViewPagerAdapter(FragmentManager fm, String[] pageTitles) {
            super(fm);
            this.pageTitles = pageTitles;
            this.fragments = new SparseArray<>(pageTitles.length);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }

        @Override
        public Fragment getItem(int position) {

            Bundle args = new Bundle();
            args.putString("BizName", pageTitles[position]);
            return Fragment.instantiate(CareOverviewListActivity.this, CareListFragment.class.getName(), args);
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            fragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            fragments.remove(position);
        }

        public Fragment getFragmentInstance(int position) {
            return fragments.get(position);
        }
    }

    @Override
    public String getSearchKey() {
        return txtSearch.getText().toString();
    }
}
