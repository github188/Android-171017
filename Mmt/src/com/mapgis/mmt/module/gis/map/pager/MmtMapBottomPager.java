package com.mapgis.mmt.module.gis.map.pager;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.mapgis.mmt.module.gis.MapGISFrame;

public class MmtMapBottomPager {

    /**
     * 标题名称
     */
    private final String title;

    /**
     * 当前模块的Fragment
     */
    private final Fragment fragment;

    /**
     * 底部滑动页滑动监听器
     */
    private OnMapBottomPageSeletedListener onMapBottomPageSeletedListener;

    public MmtMapBottomPager(String title, Fragment fragment) {
        this.title = title;
        this.fragment = fragment;
    }

    /**
     * 地图底部滑动页的滑动监听
     */
    public void setOnMapBottomPageSeletedListener(OnMapBottomPageSeletedListener onMapBottomPageSeletedListener) {
        this.onMapBottomPageSeletedListener = onMapBottomPageSeletedListener;
    }

    /**
     * 将结果显示在地图上
     */
    public final void addOnMap(final MapGISFrame mapGISFrame) {
        // 判断有没有标题和Fragment
        if (title == null || fragment == null) {
            return;
        }

        // 显示功能的Fragment
        mapGISFrame.replaceOtherFragment(fragment);

        // 添加滑动监听器
        mapGISFrame.getFragment().getViewPager().setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                if (onMapBottomPageSeletedListener != null) {
                    onMapBottomPageSeletedListener.onMapBottomPageSeleted(arg0);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    public interface OnMapBottomPageSeletedListener {
        void onMapBottomPageSeleted(int index);
    }
}
