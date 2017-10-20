package com.repair.zhoushan.module.casemanage.mydonecase;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.DeviceUtil;
import com.mapgis.mmt.R;

import java.util.ArrayList;

public class DoneCaseDetailActivity extends BaseActivity {

    private ColumnHorizontalScrollView mColumnHorizontalScrollView = null;
    LinearLayout mRadioGroup_content = null;

    // 左阴影部分
    public ImageView shade_left;
    // 右阴影部分
    public ImageView shade_right;

    private ViewPager mViewPager;

    // The width of the screen.
    private int mScreenWidth = 0;
    // The width of the item.
    private int mItemWidth = 0;

    // 当前选中的栏目
    private int columnSelectIndex = 0;

    private ArrayList<String> mColumnList = new ArrayList<String>();

    @Override
    protected void setDefaultContentView() {

        setContentView(R.layout.done_case_detail_view);

        getBaseTextView().setText("工单详情");

        addBackBtnListener(getBaseLeftImageView());

        initView();
    }

    private void initView() {

        this.mScreenWidth = DeviceUtil.getWindowsWidth(this);
        this.mItemWidth = mScreenWidth / 6;
        this.mColumnHorizontalScrollView = (ColumnHorizontalScrollView) findViewById(R.id.mColumnHorizontalScrollView);
        this.mRadioGroup_content = (LinearLayout) findViewById(R.id.mRadioGroup_content);

        this.shade_left = (ImageView) findViewById(R.id.shade_left);
        this.shade_right = (ImageView) findViewById(R.id.shade_right);

        this.mViewPager = (ViewPager) findViewById(R.id.mViewPager);

        initTabColumn();
        initFragment();
    }

    private void initFragment() {
        mViewPager.setAdapter(new TabFragmentPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }

    /**
     * 初始化Column栏目项
     */
    private void initTabColumn() {

        mRadioGroup_content.removeAllViews();
        int count = mColumnList.size();
        // mColumnHorizontalScrollView.setParam(this, mScreenWidth, mRadioGroup_content, shade_left, shade_right, ll_more_columns, rl_column);

        for (int i = 0; i < count; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mItemWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 5;
            params.rightMargin = 5;
//			TextView localTextView = (TextView) mInflater.inflate(R.layout.column_radio_item, null);
            TextView columnTextView = new TextView(this);
            columnTextView.setTextAppearance(this, R.style.top_category_scroll_view_item_text);
//			localTextView.setBackground(getResources().getDrawable(R.drawable.top_category_scroll_text_view_bg));
            columnTextView.setBackgroundResource(R.drawable.radio_buttong_bg);
            columnTextView.setGravity(Gravity.CENTER);
            columnTextView.setPadding(5, 5, 5, 5);
            columnTextView.setId(i);
            columnTextView.setText(mColumnList.get(i));
            columnTextView.setTextColor(getResources().getColorStateList(R.color.top_category_scroll_text_color_day));

            if (columnSelectIndex == i) {
                columnTextView.setSelected(true);
            }

            columnTextView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    for (int i = 0; i < mRadioGroup_content.getChildCount(); i++) {
                        View localView = mRadioGroup_content.getChildAt(i);
                        if (localView != view)
                            localView.setSelected(false);
                        else {
                            localView.setSelected(true);
                            mViewPager.setCurrentItem(i);
                        }
                    }
                    Toast.makeText(getApplicationContext(), mColumnList.get(view.getId()), Toast.LENGTH_SHORT).show();
                }
            });
            mRadioGroup_content.addView(columnTextView, i, params);
        }
    }

    public ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }

        @Override
        public void onPageSelected(int position) {
            mViewPager.setCurrentItem(position);
            selectTab(position);
        }
    };

    private void selectTab(int tab_postion) {

        columnSelectIndex = tab_postion;
        for (int i = 0; i < mRadioGroup_content.getChildCount(); i++) {
            View checkView = mRadioGroup_content.getChildAt(tab_postion);
            int k = checkView.getMeasuredWidth();
            int l = checkView.getLeft();
            int i2 = l + k / 2 - mScreenWidth / 2;
            // rg_nav_content.getParent()).smoothScrollTo(i2, 0);
            mColumnHorizontalScrollView.smoothScrollTo(i2, 0);
            // mColumnHorizontalScrollView.smoothScrollTo((position - 2) *
            // mItemWidth , 0);
        }
        //判断是否选中
        for (int j = 0; j < mRadioGroup_content.getChildCount(); j++) {
            View checkView = mRadioGroup_content.getChildAt(j);
            boolean ischeck;
            ischeck = j == tab_postion;
            checkView.setSelected(ischeck);
        }
    }

    public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

        public TabFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return 0;
        }
    }
}
