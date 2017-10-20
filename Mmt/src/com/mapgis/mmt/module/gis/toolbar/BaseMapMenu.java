package com.mapgis.mmt.module.gis.toolbar;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.mapview.MapView;

public abstract class BaseMapMenu {
    protected MapGISFrame mapGISFrame;
    protected MapView mapView;

    public BaseMapMenu(MapGISFrame mapGISFrame) {
        this.mapGISFrame = mapGISFrame;
        this.mapView = mapGISFrame.getMapView();

        View titleView = initTitleView();
        if (titleView != null) {
            mapGISFrame.setCustomView(titleView);
        }

        setActionBarBg(titleView);

        removeCurXYShow();
    }

    /**
     * 移除 点定位标题上显示坐标 的影响
     */
    private void removeCurXYShow() {

        String curName = this.getClass().getCanonicalName();
        if (curName.contains(".MyLocationMapMenu")) {
            return;
        }

        Intent intent = mapGISFrame.getIntent();
        if (intent == null) {
            return;
        }
        if (!intent.hasExtra("fromMapScan")) {
            return;
        }
        intent.removeExtra("fromMapScan");
    }

    protected void setActionBarBg(View titleView) {
        View actionBarView = titleView;

        if (actionBarView == null) {
            actionBarView = this.mapGISFrame.findViewById(R.id.mainActionBar);
        }

        if (actionBarView == null) {
            actionBarView = this.mapGISFrame.findViewById(R.id.layoutActionBar);
        }

        if (actionBarView != null) {
            actionBarView.setBackgroundResource(AppStyle.getActionBarStyleResource());
        }
    }

    public abstract boolean onOptionsItemSelected();

    /**
     * 初始化该菜单功能自己的标题栏,屏蔽掉以前的基类的返回按钮,所有自定义标题栏都必需带有返回按钮,用来退出该菜单的功能
     */
    public abstract View initTitleView();

    public boolean onActivityResult(int resultCode, Intent intent) {
        return false;
    }

    public boolean onBackPressed() {
        mapGISFrame.resetMenuFunction();

        return true;
    }

    private View viewBar;

    public void removeBottomViewBar() {
        if (viewBar != null) {
            mapView.removeView(viewBar);
            viewBar = null;
        }
    }

    protected View initBottomBarView(int layoutResId) {
        View viewBar = mapGISFrame.getLayoutInflater().inflate(layoutResId, mapView, false);
        return initBottomBarView(viewBar);
    }

    protected View initBottomBarView(View viewBar) {

        this.viewBar = viewBar;
        mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        viewBar.setLayoutParams(layoutParams);
        mapView.addView(viewBar);

        return viewBar;
    }

    public void onReceive(Context context, Intent intent) {
    }
}
