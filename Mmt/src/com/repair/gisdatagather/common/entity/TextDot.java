package com.repair.gisdatagather.common.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.utils.DotIcoResourceUtils;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.repair.zhoushan.common.Utils;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.Iterator;
import java.util.List;

/**
 * Created by liuyunfan on 2016/1/13.
 */
public class TextDot {

    //点的来源：当前工程的current，今日采集的today，已上报但本地没有的mapNExist，地图上的mapExist
    public int from = GisDataGatherUtils.GisDataFrom.currentProject;

    public GISDataBeanBase gisDataBean;

    public Dot dot;
    //默认颜色，未上报
    /**
     * 0:未编辑的
     * 1：已编辑的
     */
    public int state = 0;

    //是否是当前工程中选中的点
    public boolean isGlint = false;

    private transient GraphicImage graphicImage;

    private transient GraphicPoint graphicPoint;

    public TextDot(Context context, GISDataBeanBase gisDataBean, int from, Dot dot, int state) {
        this.gisDataBean = gisDataBean;
        this.from = from;
        this.dot = dot;
        this.state = state;

        graphicImage = new GraphicImage(dot, DotIcoResourceUtils.getDotIcoResource(context, gisDataBean.LayerName, state));

        graphicImage.setAnchorPoint(new PointF(0.5f, 0.5f));
        graphicImage.setAttributeValue("GISType", "GISPoint");
        graphicImage.setAttributeValue("flag", from + "-" + "Point");
        if (gisDataBean != null && !TextUtils.isEmpty(gisDataBean.LayerName)) {
            graphicImage.setAttributeValue("$图层名称$", gisDataBean.LayerName);
        }
        GisDataGatherUtils.putAttr2Graphic(graphicImage, gisDataBean);
    }

    public TextDot(Graphic graphic, GISDataBeanBase gisDataBean, int from, Dot dot, int state) {
        this.gisDataBean = gisDataBean;
        this.from = from;
        this.dot = dot;
        this.state = state;
        if (graphic instanceof GraphicImage) {
            graphicImage = (GraphicImage) graphic;
        } else if (graphic instanceof GraphicPoint) {
            graphicPoint = (GraphicPoint) graphic;
        }
    }

    /**
     * 单独删点
     *
     * @param mapView
     * @param refresh
     */
    public void deleteTextDot(MapView mapView, boolean refresh) {
        if (graphicImage != null) {
            mapView.getGraphicLayer().removeGraphic(graphicImage);
            if (refresh) {
                mapView.refresh();
            }
        }
    }

    public void addTextDot(MapView mapView, boolean refresh) {
        if (graphicImage == null && graphicPoint != null) {
            Bitmap image = DotIcoResourceUtils.getDotIcoResource(mapView.getContext(), gisDataBean.LayerName, state);
            graphicImage = new GraphicImage(graphicPoint.getPoint(), image);
        }
        if (graphicImage != null) {
            mapView.getGraphicLayer().addGraphic(graphicImage);
            if (refresh) {
                mapView.refresh();
            }
        }
    }

    public void updateAttr() {
        if (graphicImage != null) {
            GisDataGatherUtils.putAttr2Graphic(graphicImage, gisDataBean);
        }
        if (graphicPoint != null) {
            GisDataGatherUtils.putAttr2Graphic(graphicPoint, gisDataBean);
        }
    }

//    /**
//     * 根据text
//     * 删除点相关联的线
//     *
//     * @additionalParas mapView
//     * @additionalParas
//     */
//    public void deleteDotAndLinkLine(final Context context, final MapView mapView, List<TextLine> todayTextLines, List<TextLine> currentTextLines) {
//        if (todayTextLines != null) {
//            for (final Iterator<TextLine> it = todayTextLines.iterator(); it.hasNext(); ) {
//                final TextLine textLine = it.next();
//                List<TextDot> dots = textLine.dots;
//                if (dots == null || dots.size() < 2) {
//                    continue;
//                }
//                for (TextDot textDot : dots) {
//                    if (textDot.dot.toString().equals(this.dot.toString())) {
//                        textLine.gisDataBean.deleteFromServer(context, new MmtBaseTask.OnWxyhTaskListener<String>() {
//                            @Override
//                            public void doAfter(String s) {
//                                ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, "删除GIS数据失败", "删除GIS数据成功");
//                                if (resultWithoutData == null) {
//                                    return;
//                                }
//                            }
//                        });
//                        textLine.deleteTextLine(mapView, false);
//                        it.remove();
//                        break;
//                    }
//                }
//            }
//        }
//        if (currentTextLines != null) {
//            for (final Iterator<TextLine> it = currentTextLines.iterator(); it.hasNext(); ) {
//                final TextLine textLine = it.next();
//                List<TextDot> dots = textLine.dots;
//                if (dots == null || dots.size() < 2) {
//                    continue;
//                }
//                for (TextDot textDot : dots) {
//                    if (textDot.dot.toString().equals(this.dot.toString())) {
//                        textLine.gisDataBean.deleteFromServer(context, new MmtBaseTask.OnWxyhTaskListener<String>() {
//                            @Override
//                            public void doAfter(String s) {
//                                ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, "删除GIS数据失败", "删除GIS数据成功");
//                                if (resultWithoutData == null) {
//                                    return;
//                                }
//                            }
//                        });
//                        textLine.deleteTextLine(mapView, false);
//                        it.remove();
//                        break;
//                    }
//                }
//            }
//        }
//        this.deleteTextDot(mapView, true);
//    }

    public void deleteLinkLine(final Context context, final MapView mapView, List<TextLine> todayTextLines, List<TextLine> currentTextLines) {
        if (todayTextLines != null) {
            for (final Iterator<TextLine> it = todayTextLines.iterator(); it.hasNext(); ) {
                final TextLine textLine = it.next();
                List<TextDot> dots = textLine.dots;
                if (dots == null || dots.size() < 2) {
                    continue;
                }
                for (TextDot textDot : dots) {
                    if (GisUtil.equals(textDot.dot, this.dot, 0.001f)) {
                        textLine.gisDataBean.deleteFromServer(context, new MmtBaseTask.OnWxyhTaskListener<String>() {
                            @Override
                            public void doAfter(String s) {
                                ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, "删除GIS数据失败", "删除GIS数据成功");
                                if (resultWithoutData == null) {
                                    return;
                                }
                                textLine.deleteTextLine(mapView, true);
                            }
                        });
                        it.remove();
                        break;
                    }
                }
            }
        }
        if (currentTextLines != null) {
            for (final Iterator<TextLine> it = currentTextLines.iterator(); it.hasNext(); ) {
                final TextLine textLine = it.next();
                List<TextDot> dots = textLine.dots;
                if (dots == null || dots.size() < 2) {
                    continue;
                }
                for (TextDot textDot : dots) {
                    if (GisUtil.equals(textDot.dot, this.dot, 0.001f)) {
                        textLine.gisDataBean.deleteFromServer(context, new MmtBaseTask.OnWxyhTaskListener<String>() {
                            @Override
                            public void doAfter(String s) {
                                ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, "删除GIS数据失败", "删除GIS数据成功");
                                if (resultWithoutData == null) {
                                    return;
                                }
                                textLine.deleteTextLine(mapView, true);
                            }
                        });

                        it.remove();
                        break;
                    }
                }
            }
        }

    }

    public void lightTextDot(MapView mapView, int from, int state, boolean refresh) {
        this.from = from;
        this.state = state;
        if (graphicImage != null) {
            //graphicImage.setImage(BitmapFactory.decodeResource(mapView.getResources(), GisDataGatherUtils.getDrawByGisDataFrom(from, gisDataBean, state)));
            graphicImage.setImage(DotIcoResourceUtils.getDotIcoResource(mapView.getContext(), gisDataBean.LayerName, state));

            if (refresh) {
                mapView.refresh();
            }
        }
        if (graphicPoint != null) {
            //已有管点不变
        }
    }

    public void submit2Server(Context context, final int projectid, MmtBaseTask.OnWxyhTaskListener<String> listener) {
        new MmtBaseTask<Void, Void, String>(context, true, listener) {
            @Override
            protected String doInBackground(Void... params) {

                try {
                    return NetUtil.executeHttpPost(ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostSingleGISData/" + projectid, new Gson().toJson(gisDataBean));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.mmtExecute();
    }

    public void glintTextDot(final MapView mapView) {
        this.isGlint = true;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (isGlint) {
                    graphicImage.setAlpha(0);
                    mapView.refresh();
                    try {
                        synchronized (this) {
                            wait(500);
                        }
                    } catch (Exception ex) {
                    }
                    graphicImage.setAlpha(255);
                    mapView.refresh();
                    try {
                        synchronized (this) {
                            wait(500);
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        };

        MyApplication.getInstance().submitExecutorService(runnable);

    }

    public void stopAllGlint(List<TextDot> textDots, MapView mapView) {
        if (textDots == null) {
            return;
        }
        for (TextDot textDot : textDots) {
            if (!textDot.isGlint) {
                continue;
            }
            textDot.isGlint = false;
            textDot.graphicImage.setAlpha(255);
        }
        mapView.refresh();
    }

}
