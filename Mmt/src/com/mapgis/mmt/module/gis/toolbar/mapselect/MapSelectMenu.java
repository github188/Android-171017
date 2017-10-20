package com.mapgis.mmt.module.gis.toolbar.mapselect;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

import java.io.File;

public class MapSelectMenu extends BaseMapMenu {

    public MapSelectMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        mapGISFrame.startActivityForResult(new Intent(mapGISFrame, MapSelectActivity.class), 0);
        return true;
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {

        if (resultCode == Activity.RESULT_OK) {
            try {
                String name = MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name;

                MobileConfig.MapConfigInstance.IsVectorQueryOnline = false;
                MapMenuRegistry.getInstance().registQueryMenu();

                String pre = MyApplication.getInstance().getMapFilePath() + name + "/" + name;

                if (new File(pre + ".mapx").exists()) {
                    mapView.loadFromFile(pre + ".mapx");
                } else if (new File(pre + ".xml").exists()) {
                    mapView.loadFromFile(pre + ".xml");
                } else {
                    Toast.makeText(mapGISFrame, "指定路径未找到地图文档", Toast.LENGTH_SHORT).show();

                    return super.onActivityResult(resultCode, intent);
                }

                mapView.refresh();
            } catch (Exception e) {
                Toast.makeText(mapGISFrame, "地图数据有异常,无法加载!", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onActivityResult(resultCode, intent);
    }

    @Override
    public View initTitleView() {
        return null;
    }
}
