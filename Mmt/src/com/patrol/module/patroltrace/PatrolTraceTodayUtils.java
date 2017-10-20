package com.patrol.module.patroltrace;

import android.view.View;
import android.widget.CheckBox;

import com.zondy.mapgis.geometry.Rect;

/**
 * Created by lyunfan on 16/12/20.
 */

public class PatrolTraceTodayUtils {

    public static void hidePatrolTraceCbox(boolean isChecked, View view, Rect rect) {
        if (view instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view;
            if (isChecked) {
                checkBox.setChecked(false);
            }
            view.setVisibility(View.GONE);

//            if (rect != null) {
//                MapView mapView = MyApplication.getInstance().mapGISFrame.getMapView();
//                mapView.zoomToRange(new Rect(rect.xMin - 100, rect.yMin - 100, rect.xMax + 100, rect.yMax + 100), true);
//            }
        }
    }

    public static void showPatrolTraceCbox(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

}
