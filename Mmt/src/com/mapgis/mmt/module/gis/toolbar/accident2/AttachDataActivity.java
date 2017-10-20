package com.mapgis.mmt.module.gis.toolbar.accident2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2017/5/17.
 * 附属数据查看界面
 */

public class AttachDataActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "AttachDataActivity";

    public static final String PARAMS = "params";
    private FeatureMetaGroup mMetaGroup;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private AttDataAdapter mAdapter;
    private TextView mTvIndex;
    private ImageButton mExportButton;
    private WeakReference[] mWeakRefFragments;

    @Override
    protected void setDefaultContentView() {
        setContentView(R.layout.activity_attach_data);

        setSwipeBackEnable(false);
        defaultBackBtn = findViewById(R.id.baseActionBarImageView);
        addBackBtnListener(defaultBackBtn);
        mExportButton = (ImageButton) findViewById(R.id.baseActionBarRightImageView);

        mExportButton.setOnClickListener(this);
        findViewById(R.id.linearLayout1).setOnClickListener(this);
        findViewById(R.id.baseErrorLayout).setOnClickListener(this);

        initView();
        initData();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.linearLayout1) {
            getBaseRightImageView().performClick();
        } else if (id == R.id.baseErrorLayout) {
            dismissErrorMsg();
        } else if (id == R.id.baseActionBarRightImageView) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        int position = mTabLayout.getSelectedTabPosition();
        AttachDataFragment fragment = (AttachDataFragment) getFragmentFromWeakRef(
                position, false);
        if (fragment == null) {
            showToast("页面加载出错，无法导出附属数据");
            return;
        }
        fragment.showExportDialog();
    }

    public String getCurrentTabTitle() {
        int selectedTabPosition = mTabLayout.getSelectedTabPosition();
        TabLayout.Tab tab = mTabLayout.getTabAt(selectedTabPosition);
        if (tab != null) {
            return (String) tab.getText();
        }
        return "";
    }

    private void initView() {
        getBaseTextView().setText(R.string.text_att_data);
        mTvIndex = (TextView) findViewById(R.id.tv_index);
        mTvIndex.setVisibility(View.GONE);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
    }

    private void initData() {
        Intent intent = getIntent();
        if (!intent.hasExtra(PARAMS)) {
            showErrorMsg(getString(R.string.text_eff_none));
            return;
        }
        mMetaGroup = intent.getParcelableExtra(PARAMS);

        initViewPager();
        // tablayout与viewpager绑定
        mTabLayout.setupWithViewPager(mViewPager);
        initTabLayout();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                try {
                    Fragment fragment = getFragmentFromWeakRef(position, false);
                    setTitleIndex(((AttachDataFragment)fragment).getTitleIndex());
                } catch (Exception e) {
                    setTitleIndex("");
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initTabLayout() {
        List<String> tabTitleList = getLayerNames();
        int count = mTabLayout.getTabCount();
        if (count > 3) {
            mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        } else {
            mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        }
        if (count < 2) mTabLayout.setVisibility(View.GONE);

        for (int i = 0; i < count; i++) {
            String title = tabTitleList.get(i);
            TabLayout.Tab tabAt = mTabLayout.getTabAt(i);
            if (tabAt != null) {
                tabAt.setText(title);
            }
        }
    }

    private void initViewPager() {
        mAdapter = new AttDataAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
    }

    @NonNull
    private List<String> getLayerNames() {
        List<String> list = new ArrayList<>();
        for (FeatureMetaItem item : mMetaGroup.resultList) {
            list.add(item.layerName);
        }
        return list;
    }

    private class AttDataAdapter extends FragmentStatePagerAdapter {
        AttDataAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = getFragmentFromWeakRef(position, true);

           /*try {
                setTitleIndex(((AttachDataFragment)fragment).getTitleIndex());
            } catch (Exception e) {
                setTitleIndex("");
                e.printStackTrace();
            }*/
            return fragment;
        }

        @Override
        public int getCount() {
            return mMetaGroup.resultList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mWeakRefFragments[position] = null;
            super.destroyItem(container, position, object);
        }
    }

    /**
     * 从Fragment的弱引用集合中获取Fragment
     */
    @Nullable
    private Fragment getFragmentFromWeakRef(int position, boolean isAutoCreate) {
        AttachDataFragment fragment = null;
        if (mWeakRefFragments == null) {
            mWeakRefFragments = new WeakReference[mMetaGroup.resultList.size()];
        }

        FeatureMetaItem item = mMetaGroup.resultList.get(position);
        WeakReference weakRef = mWeakRefFragments[position];
        if (weakRef == null || weakRef.get() == null) {
            if (isAutoCreate) {
                fragment = AttachDataFragment.getInstance(item);
                weakRef = new WeakReference<Fragment>(fragment);
                mWeakRefFragments[position] = weakRef;
            }
        } else {
            fragment = (AttachDataFragment) weakRef.get();
        }
        return fragment;
    }

    public void setTitleIndex(String msg) {
        if (BaseClassUtil.isNullOrEmptyString(msg)) {
            this.mTvIndex.setVisibility(View.GONE);
        } else {
            this.mTvIndex.setText(msg);
            this.mTvIndex.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
