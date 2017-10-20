package com.repair.gisdatagather.enn;

import android.os.Handler;

import com.mapgis.mmt.common.util.GisUtil;
import com.repair.gisdatagather.common.entity.TextDot;
import com.repair.gisdatagather.common.entity.TextLine;
import com.repair.gisdatagather.common.entity.TodayGISData;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Rect;

import java.util.Iterator;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/12.
 */
public class EnnPainGisDatas2MapViewThread extends Thread {

    MapView mapView;
    List<TextDot> textDots;
    List<TextLine> textLines;
    TodayGISData todayGISData;
    protected Handler handler;

    //线程是否运行的标志
    boolean isOpen = true;
    //是否正在绘制的标志
    boolean runFlag = false;

    //是否打断绘制的标志
    public void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public EnnPainGisDatas2MapViewThread(MapView mapView, List<TextDot> textDots, List<TextLine> textLines, TodayGISData todayGISData, Handler handler) {
        this.mapView = mapView;
        this.textDots = textDots;
        this.textLines = textLines;
        this.todayGISData = todayGISData;
        this.handler = handler;
    }

    @Override
    public void run() {

        while (isOpen) {
            try {
                if (!runFlag) {
                    synchronized (this) {
                        wait();
                    }
                }
                if (runFlag) {
                    Iterator<TextDot> todayTextDots = todayGISData.textDots.iterator();
                    Iterator<TextLine> todayTextLines = todayGISData.textLines.iterator();
                    Iterator<TextDot> otherTextDots = textDots.iterator();
                    Iterator<TextLine> otherTextLines = textLines.iterator();
                    synchronized (this) {
                        //优先绘制今日gis数据
                        while (todayTextDots.hasNext()) {

                            if (!runFlag) {
                                break;
                            }
                            synchronized (this) {
                                Rect rect = mapView.getDispRange();

                                if (rect == null) {
                                    break;
                                }

                                TextDot textDot = todayTextDots.next();

                                if (!GisUtil.isInRect(rect, textDot.dot)) {
                                    continue;
                                }

                                handler.obtainMessage(1, textDot).sendToTarget();
                                todayTextDots.remove();
                            }
                            // sleep(100);
                        }
                        while (todayTextLines.hasNext()) {
                            if (!runFlag) {
                                break;
                            }
                            synchronized (this) {
                                Rect rect = mapView.getDispRange();

                                if (rect == null) {
                                    break;
                                }
                                TextLine textLine = todayTextLines.next();

                                if ((textLine.dots == null) || (textLine.dots.size() != 2)) {
                                    break;
                                }

                                for (TextDot textDot : textLine.dots) {
                                    if (!GisUtil.isInRect(rect, textDot.dot)) {
                                        continue;
                                    }
                                }

                                handler.obtainMessage(2, textLine).sendToTarget();
                                todayTextLines.remove();
                            }
                            //sleep(100);
                        }

                        while (otherTextDots.hasNext()) {
                            if (!runFlag) {
                                break;
                            }
                            synchronized (this) {
                                Rect rect = mapView.getDispRange();

                                if (rect == null) {
                                    break;
                                }
                                TextDot textDot = otherTextDots.next();

                                if (!GisUtil.isInRect(rect, textDot.dot)) {
                                    continue;
                                }

                                handler.obtainMessage(1, textDot).sendToTarget();
                                otherTextDots.remove();
                            }
                            // sleep(100);
                        }
                        while (otherTextLines.hasNext()) {
                            if (!runFlag) {
                                break;
                            }
                            synchronized (this) {
                                Rect rect = mapView.getDispRange();

                                if (rect == null) {
                                    break;
                                }
                                TextLine textLine = otherTextLines.next();

                                if ((textLine.dots == null) || (textLine.dots.size() != 2)) {
                                    break;
                                }

                                for (TextDot textDot : textLine.dots) {
                                    if (!GisUtil.isInRect(rect, textDot.dot)) {
                                        continue;
                                    }
                                }

                                handler.obtainMessage(2, textLine).sendToTarget();
                                otherTextLines.remove();
                            }
                            //sleep(100);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                handler.obtainMessage(3, "").sendToTarget();
                runFlag = false;
            }
        }
    }
}
