package com.mapgis.mmt.module.gis.toolbar.clearmap;

import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * 清除地图自定义绘制的图形
 *
 * @author Administrator
 */
public class ClearMapMenu extends BaseMapMenu {

    public ClearMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
//		// 工具块的复原
//		mapGISFrame.setTitleAndClear(Product.getInstance().Title);
//		mapGISFrame.setBottomBarClear();
//
//		// mapGISFrame.showBottomBar(View.VISIBLE);
//
//		mapGISFrame.getFragment().showViewPagerVisibility(View.GONE);
//		mapGISFrame.getFragment().getViewPager().removeAllViews();
//
//		mapGISFrame.getFragment().getModuleToolbar().setVisibility(View.GONE);
//		mapGISFrame.getFragment().getModuleToolbar().removeAllViews();
//
//		mapView.setZoomChangedListener(null);
//		mapView.setTapListener(null);
//
//		// 地图的复原
//		mapView.getGraphicLayer().removeAllGraphics();
//		mapView.getAnnotationLayer().removeAllAnnotations();
//
//		mapView.turnOffMagnifier();
//
//		mapView.refresh();

        mapGISFrame.resetMenuFunction();

        // 删除定在在屏幕上的View
        List<View> myViews = new ArrayList<>();

        for (int i = 0; i < mapView.getChildCount(); i++) {

            Object object = mapView.getChildAt(i).getTag();

            if (object == null)
                continue;

            String mark = object.toString();
            if (mark.equals("MapViewScreenView"))
                myViews.add(mapView.getChildAt(i));
        }

        for (View v : myViews) {
            mapView.removeView(v);
        }

        return true;
    }

    @Override
    public View initTitleView() {
        return null;
    }
}
