package com.repair.gisdatagather.common.entity;

import com.mapgis.mmt.MyApplication;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/2/29.
 */
public class TodayGISData {
    public List<TextDot> textDots = new ArrayList<>();
    public List<TextLine> textLines = new ArrayList<>();

    public void lightToday(MapView mapView) {
        if (textDots.size() == 0 && textLines.size() == 0) {
            MyApplication.getInstance().showMessageWithHandle("今日还未采集管点数据");
            return;
        }
        Rect rect = GisDataGatherUtils.getRectFromTextDots(textDots);
        if(rect==null){
            MyApplication.getInstance().showMessageWithHandle("今日还未采集管点数据");
            return;
        }
        mapView.zoomToRange(new Rect(rect.getXMin() - 80, rect.getYMin() - 80, rect.getXMax() + 80, rect.getYMax() + 80), true);
    }
}
