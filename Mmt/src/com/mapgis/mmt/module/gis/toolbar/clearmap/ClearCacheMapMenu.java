package com.mapgis.mmt.module.gis.toolbar.clearmap;

import android.view.View;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.zondy.mapgis.map.ServerLayer;

/**
 * 清除服务图层的内存缓存数据
 *
 * @author Administrator
 */
public class ClearCacheMapMenu extends BaseMapMenu {

    public ClearCacheMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        for (int i = 0; i < mapView.getMap().getLayerCount(); i++) {
            if (mapView.getMap().getLayer(i) instanceof ServerLayer)
                ((ServerLayer) mapView.getMap().getLayer(i)).clearCache();
        }

        return true;
    }

    @Override
    public View initTitleView() {
        return null;
    }
}
