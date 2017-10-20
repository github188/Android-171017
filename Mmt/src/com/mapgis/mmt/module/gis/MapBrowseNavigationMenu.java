package com.mapgis.mmt.module.gis;

import android.content.Intent;
import android.os.Message;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 地图浏览
 */
public class MapBrowseNavigationMenu extends BaseNavigationMenu {

    public MapBrowseNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        try {
            MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        mapGISFrame.resetMenuFunction();
                        mapGISFrame.findViewById(R.id.mapviewClear).performClick();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    return false;
                }
            });

            Intent intent = new Intent(navigationActivity, MapGISFrame.class);

            intent.putExtra("fromMapScan",true);

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            navigationActivity.startActivity(intent);

            AppManager.finishActivity(navigationActivity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_map, R.drawable.home_map, R.drawable.home_circle_relation};
    }
}
