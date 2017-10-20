package com.repair.gisdatagather.common.entity;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/1/14.
 */
public class TextLine {
    public int from = GisDataGatherUtils.GisDataFrom.currentProject;
    public List<TextDot> dots;
    public GISDataBeanBase gisDataBean;
    /**
     * 只对当前工程有效
     * 0:未编辑
     * 1：已编辑
     */
    public int state = TextLineState.ADD.getState();
    private transient GraphicPolylin polylin;

    public TextLine(Context context, List<TextDot> dots, GISDataBeanBase gisDataBean, int from, int state) {
        this.dots = dots;
        this.gisDataBean = gisDataBean;
        this.from = from;
        this.state = state;

        if (dots != null && dots.size() >= 2) {
            polylin = new GraphicPolylin();

            polylin.setColor(GisDataGatherUtils.getColorByGIsDataFrom(from, state));
            polylin.setLineWidth(2f);
            for (TextDot textDot : dots) {
                polylin.appendPoint(textDot.dot);
            }
            polylin.setAttributeValue("GISType", "GISLine");
            polylin.setAttributeValue("flag", from + "-" + "Line");
            if (gisDataBean != null && !TextUtils.isEmpty(gisDataBean.LayerName)) {
                polylin.setAttributeValue("$图层名称$", gisDataBean.LayerName);
            }
            GisDataGatherUtils.putAttr2Graphic(polylin, gisDataBean);

        }
    }

    public TextLine(Context context, Graphic graphic, GISDataBeanBase gisDataBean, int from, int state) {
        // this.dots = dots;
        this.gisDataBean = gisDataBean;
        this.from = from;
        this.state = state;
        if (graphic instanceof GraphicPolylin) {
            polylin = (GraphicPolylin) graphic;

            List<TextDot> textDots = new ArrayList<>();
            for (int i = 0; i < polylin.getPointCount(); i++) {
                textDots.add(new TextDot(context, new GISDataBeanBase(), from, polylin.getPoint(i), TextDotState.OHTER.getState()));
            }
            this.dots = textDots;
        }
    }

    public boolean addLine(MapView mapView, boolean refresh) {
        if (polylin != null) {
            mapView.getGraphicLayer().addGraphic(polylin);
            if (refresh) {
                mapView.refresh();
            }
            return true;
        }
        return false;
    }

    /**
     * 删除管线
     *
     * @param mapView
     * @param refresh
     */
    public void deleteTextLine(MapView mapView, boolean refresh) {
        if (polylin != null) {
            mapView.getGraphicLayer().removeGraphic(polylin);
            if (refresh) {
                mapView.refresh();
            }
        }
    }

    public void updateAttr() {
        if (polylin != null) {
            GisDataGatherUtils.putAttr2Graphic(polylin, gisDataBean);
        }
    }

    public void lightTextDot(MapView mapView, int from, int state, boolean refresh) {
        this.from = from;
        this.state = state;
        if (polylin != null) {
            polylin.setColor(GisDataGatherUtils.getColorByGIsDataFrom(from, state));
            if (refresh) {
                mapView.refresh();
            }
        }
    }


//    public void submit2Server(Context context, final int projectid,final GISDataProject gisDataProject) {
//        if (dots != null && dots.size() == 2) {
//            String bdh = dots.get(1).gisDataBean.事件编号;
//            String sdh = dots.get(0).gisDataBean.事件编号;
//            if (TextUtils.isEmpty(bdh) || TextUtils.isEmpty(sdh)) {
//                MyApplication.getInstance().showMessageWithHandle(gisDataBean.LayerName + "的事件编号异常，提交失败");
//                return;
//            }
//            gisDataBean.事件编号 = bdh + "-" + sdh;
//        }
//        if (TextUtils.isEmpty(gisDataBean.事件编号)) {
//            MyApplication.getInstance().showMessageWithHandle(gisDataBean.LayerName + "的本点号和上点号生成异常");
//            return;
//        }
//        new MmtBaseTask<Void, Void, String>(context) {
//            @Override
//            protected String doInBackground(Void... params) {
//                return NetUtil.executeHttpPost(ServerConnectConfig.getInstance().getBaseServerPath()
//                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostSingleGISData/" + projectid, new Gson().toJson(gisDataBean));
//            }
//
//            @Override
//            protected void onSuccess(String s) {
//                ResultData<GISDataBeanBase> resultData = Utils.json2ResultDataToast(GISDataBeanBase.class, context, s, "保存" + gisDataBean.LayerName + "失败", false);
//                if (resultData == null) {
//                    return;
//                }
//                gisDataBean = resultData.getSingleData();
//                gisDataProject.getTodayGISData().textLines.add(TextLine.this);
//                MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
//                    @Override
//                    public boolean handleMessage(Message msg) {
//                        addLine(mapView, true);
//                        return true;
//                    }
//                });
//
//            }
//        }.mmtExecute();
//    }


    public void submit2Server(Context context, final int projectid, MmtBaseTask.OnWxyhTaskListener<String> listener) {
        if (TextUtils.isEmpty(gisDataBean.事件编号)) {
            if (dots != null && dots.size() == 2) {
                String bdh = dots.get(1).gisDataBean.事件编号;
                String sdh = dots.get(0).gisDataBean.事件编号;
                if (TextUtils.isEmpty(bdh) || TextUtils.isEmpty(sdh)) {
                    MyApplication.getInstance().showMessageWithHandle(gisDataBean.LayerName + "的事件编号异常，提交失败");
                    return;
                }
                gisDataBean.事件编号 = bdh + "-" + sdh;
            }
        }
        if (TextUtils.isEmpty(gisDataBean.事件编号)) {
            MyApplication.getInstance().showMessageWithHandle(gisDataBean.LayerName + "的本点号和上点号生成异常");
            return;
        }
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

//    public long deleteFromServersilent() {
//        return NetUtil.executeHttpPost(ServerConnectConfig.getInstance().getBaseServerPath()
//                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostSingleGISData/" + projectid, new Gson().toJson(gisDataBean));
//        ReportInBackEntity reportInBackEntity=new ReportInBackEntity(
//                reportData,
//                MyApplication.getInstance().getUserId(),
//                ReportInBackEntity.REPORTING,
//                uri,
//                UUID.randomUUID().toString(),
//                GisDataGatherUtils.BusinessType,
//                "",
//                "");
//        return reportInBackEntity.insert();
//    }
}
