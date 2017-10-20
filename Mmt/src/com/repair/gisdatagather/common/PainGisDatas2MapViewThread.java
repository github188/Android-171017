package com.repair.gisdatagather.common;

import android.os.Handler;

import com.mapgis.mmt.common.util.GisUtil;
import com.repair.gisdatagather.common.entity.TextDot;
import com.repair.gisdatagather.common.entity.TextLine;
import com.repair.gisdatagather.common.entity.TodayGISData;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.List;

/**
 * Created by liuyunfan on 2016/4/11.
 */
public class PainGisDatas2MapViewThread extends Thread {
    MapView mapView;
    List<TextDot> textDots;
    List<TextLine> textLines;
    TodayGISData todayGISData;
    protected Handler handler;
    protected boolean onlyShowDispRange = false;

    public PainGisDatas2MapViewThread(MapView mapView, List<TextDot> textDots, List<TextLine> textLines, TodayGISData todayGISData, Handler handler) {
        this(mapView, textDots, textLines, todayGISData, handler, false);
    }

    public PainGisDatas2MapViewThread(MapView mapView, List<TextDot> textDots, List<TextLine> textLines, TodayGISData todayGISData, Handler handler, boolean onlyShowDispRange) {
        this.mapView = mapView;
        this.textDots = textDots;
        this.textLines = textLines;
        this.todayGISData = todayGISData;
        this.handler = handler;
        this.onlyShowDispRange = onlyShowDispRange;
    }

    @Override
    public void run() {
        try {

            //优先绘制今日gis数据
            for (TextDot textDot : todayGISData.textDots) {
                if (!onlyShowDispRange) {

                    handler.obtainMessage(1, textDot).sendToTarget();
                    sleep(100);

                } else {
                    if (GisUtil.isInRect(mapView.getDispRange(), textDot.dot)) {
                        handler.obtainMessage(1, textDot).sendToTarget();
                        sleep(100);
                    }
                }

            }
            for (TextLine textLine : todayGISData.textLines) {

                if (!onlyShowDispRange) {
                    handler.obtainMessage(2, textLine).sendToTarget();
                    sleep(100);

                } else {
                    if (GisUtil.isInRect(mapView.getDispRange(), textLine.dots.get(0).dot) && GisUtil.isInRect(mapView.getDispRange(), textLine.dots.get(1).dot)) {
                        handler.obtainMessage(2, textLine).sendToTarget();
                        sleep(100);
                    }
                }

            }
            //再绘制其他的
            for (TextDot textDot : textDots) {

                if (!onlyShowDispRange) {
                    handler.obtainMessage(1, textDot).sendToTarget();
                    sleep(100);
                } else {
                    if (GisUtil.isInRect(mapView.getDispRange(), textDot.dot)) {
                        handler.obtainMessage(1, textDot).sendToTarget();
                        sleep(100);
                    }
                }

            }
            for (TextLine textLine : textLines) {

                if (!onlyShowDispRange) {
                    handler.obtainMessage(2, textLine).sendToTarget();
                    sleep(100);

                } else {
                    if (GisUtil.isInRect(mapView.getDispRange(), textLine.dots.get(0).dot) && GisUtil.isInRect(mapView.getDispRange(), textLine.dots.get(1).dot)) {
                        handler.obtainMessage(2, textLine).sendToTarget();
                        sleep(100);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            handler.obtainMessage(3, "").sendToTarget();
        }
    }
}
