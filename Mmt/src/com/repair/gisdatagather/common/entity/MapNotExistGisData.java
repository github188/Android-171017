package com.repair.gisdatagather.common.entity;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.zondy.mapgis.android.mapview.MapView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by liuyunfan on 2016/2/26.
 * 对于离线地图：本地没有但是已经采集了的gis数据
 * 规定：本地没有但是已经采集了的gis数据分两部分存储：若是今日采集的就存在今日采集todayGISData中，否则存在MapNotExistGisData中
 */
public class MapNotExistGisData {

    public List<TextDot> textDots = new ArrayList<>();
    public List<TextLine> textLines = new ArrayList<>();
    //从服务器获取的，离线地图更新后的本地没有但已经采集的gis数据
    // private List<GISDataBeanBase> gisDataBeanBases = new ArrayList<>();
    private TodayGISData todayGISData = new TodayGISData();

    public TodayGISData getTodayGISData() {
        return todayGISData;
    }

    //    /**
//     * 初始化时一次性将服务器上存在但本地不存在的的gis数据绘制到地图上
//     *
//     * @additionalParas mapView
//     */
//    public void painNativeNotExistGisData2MapView(MapView mapView, List<GISDataBeanBase> gisDataBeanBases) {
//        if (gisDataBeanBases == null) {
//            return;
//        }
//        Handler handler = new PainGisdata2MapViewHander(mapView);
//        new PainGisData2MapViewThread(mapView, gisDataBeanBases, handler, textDots, textLines, todayGISData, GisDataGatherUtils.GisDataFrom.mapNExist).start();
//    }
//    public Thread painNativeNotExistGisData2MapView(Handler handler,MapView mapView, Rect rect, boolean runFlag) {
//        return new PainGisDatas2MapViewThread(mapView, this.textDots, this.textLines, this.getTodayGISData(), handler, rect, runFlag);
//    }

//    public void clearCacheGisDataOnMapView(MapView mapView) {
//        mapView.getGraphicLayer().removeAllGraphics();
//      //  mapView.refresh();
//
//    }

    private String getNativeNotExistGisData(Context context) {
        String mapUpdateTime = GisUtil.getMapLastUpdateTime(MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name);
        //至少需要获取当天采集的数据，今日高亮需要
        //如果采用在线地图，采集的数据会直接显示出来，这里不用自己画，也只取今日的
        if (!GisQueryUtil.isofflineMap() || !BaseClassUtil.isInLeftDate(mapUpdateTime, new Date())) {
            mapUpdateTime = BaseClassUtil.getSystemTime("yyyy-MM-dd");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date datepreMonth;
        //为了程序稳定性，如果mapUpdateTime小于一个月，只获取一个月内采集的数据
        if (BaseClassUtil.isInLeftDate(mapUpdateTime, (datepreMonth = calendar.getTime()))) {
            mapUpdateTime = new SimpleDateFormat("yyyy-MM-dd").format(datepreMonth);
        }
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetNativeNoExistGISData?mapUpdateTime=" + mapUpdateTime;
        return NetUtil.executeHttpGetAppointLastTime(180,url.replaceAll(" ", "%20"));
    }

//    class Myhander extends Handler {
//        MapView mapView;
//
//        public Myhander(MapView mapView) {
//            this.mapView = mapView;
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            try {
//                switch (msg.what) {
//                    case 1: {
//                        TextDot textDot = (TextDot) msg.obj;
//                        textDot.addTextDot(mapView, false);
//                    }
//                    break;
//                    case 2: {
//                        TextLine textLine = (TextLine) msg.obj;
//                        textLine.addLine(mapView, false);
//                    }
//                    break;
//
//                }
//            } catch (Exception ex) {
//            }
//        }
//    }

//    class PainThread extends Thread {
//        MapView mapView;
//        List<GISDataBeanBase> gisDataBeanBases;
//        Handler handler;
//
//        public PainThread(MapView mapView, List<GISDataBeanBase> gisDataBeanBases, Handler handler) {
//            this.mapView = mapView;
//            this.gisDataBeanBases = gisDataBeanBases;
//            this.handler = handler;
//        }
//
//        @Override
//        public void run() {
//            try {
//                Date now = new Date();
//                for (GISDataBeanBase gisDataBeanBase : gisDataBeanBases) {
//                    boolean istodaydata = BaseClassUtil.isInDate(gisDataBeanBase.更新时间.replace("T", " "), now);
//                    int from = GisDataGatherUtils.GisDataFrom.mapNExist;
//                    if (istodaydata) {
//                        from = GisDataGatherUtils.GisDataFrom.todayProject;
//                    }
//                    if (TextUtils.isEmpty(gisDataBeanBase.GeomType)) {
//                        continue;
//                    }
//                    if (TextUtils.isEmpty(gisDataBeanBase.NewGeom)) {
//                        continue;
//                    }
//                    if (gisDataBeanBase.GeomType.equals("管点")) {
//                        Dot dot = null;
//                        String[] xys = gisDataBeanBase.NewGeom.split(",");
//                        if (xys != null && xys.length == 2) {
//                            dot = new Dot(Double.valueOf(xys[0]), Double.valueOf(xys[1]));
//                        }
//                        if (dot == null) {
//                            continue;
//                        }
//                        TextDot textDot = new TextDot(mapView, gisDataBeanBase, from, dot, 2);
//                        if (istodaydata) {
//                            todayGISData.textDots.add(textDot);
//                        } else {
//                            textDots.add(textDot);
//                        }
//                        //textDot.addTextDot(mapView, false);
//                        handler.obtainMessage(1, textDot).sendToTarget();
//                    } else {
//                        List<TextDot> textDots = new ArrayList<>();
//                        String[] dotxys = gisDataBeanBase.NewGeom.split("\\|");
//                        if (dotxys != null) {
//                            for (String xydot : dotxys) {
//                                String[] xy = xydot.split(",");
//                                if (xy.length == 2) {
//                                    textDots.add(new TextDot(mapView, new GISDataBeanBase(), GisDataGatherUtils.GisDataFrom.mapNExist, new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1])), 0));
//                                }
//                            }
//                        }
//                        if (textDots == null && textDots.size() != 2) {
//                            continue;
//                        }
//                        TextLine textLine = new TextLine(mapView, textDots, gisDataBeanBase, from, 2);
//                        if (istodaydata) {
//                            todayGISData.textLines.add(textLine);
//                        } else {
//                            textLines.add(textLine);
//                        }
//                        //  textLine.addLine(mapView, false);
//                        handler.obtainMessage(2, textLine).sendToTarget();
//                    }
//                    sleep(100);
//                }
//            } catch (Exception ex) {
//            }
//        }
//    }


    public void getHasGatherGisData(Context context, final MapView mapView, MmtBaseTask.OnWxyhTaskListener listenr) {
        MmtBaseTask<Void, Void, Void> task = new MmtBaseTask<Void, Void, Void>(context, false) {
            @Override
            protected Void doInBackground(Void... params) {
                String resultDataStr = getNativeNotExistGisData(context);

                if (TextUtils.isEmpty(resultDataStr)) {
                    MyApplication.getInstance().showMessageWithHandle("获取已上报gis数据错误");
                    return null;
                }
                resultDataStr = resultDataStr.replace("[\"[", "[[").replace("]\"]", "]]");
                resultDataStr = resultDataStr.replace("\\", "");
                Results<List<GISDataBeanBase>> results = new Gson().fromJson(resultDataStr, new TypeToken<Results<List<GISDataBeanBase>>>() {
                }.getType());
                final ResultData<List<GISDataBeanBase>> resultData = results.toResultData();
                if (resultData.ResultCode <= 0) {
                    Toast.makeText(context, resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    MyApplication.getInstance().showMessageWithHandle(resultData.ResultMessage);
                    return null;
                }
                GisDataGatherUtils.handgisDatas(context, resultData.getSingleData(), GisDataGatherUtils.GisDataFrom.mapNExist, textDots, textLines, todayGISData);

                return null;
            }
        };

        task.setListener(listenr);

        task.mmtExecute();

    }

}
