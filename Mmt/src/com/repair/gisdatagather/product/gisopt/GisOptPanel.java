package com.repair.gisdatagather.product.gisopt;

import android.content.Context;
import android.graphics.PointF;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.entity.GISDataBeanBase;
import com.repair.gisdatagather.common.entity.TextDot;
import com.repair.gisdatagather.common.entity.TextDotState;
import com.repair.gisdatagather.common.entity.TextLine;
import com.repair.gisdatagather.common.entity.TextLineState;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.repair.gisdatagather.product.gisgather.GisGather;
import com.repair.zhoushan.common.Utils;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.List;

import static com.repair.gisdatagather.product.gisgather.GisGather.gisDataProject;
import static com.repair.gisdatagather.product.gisgather.GisGather.layerDefaultAttrs;

/**
 * Created by liuyunfan on 2016/5/4.
 */
public class GisOptPanel implements GisOptInterface, View.OnClickListener {
    protected GisGather gisGather;
    protected View view;
    public List<TextDot> autoLinkLinehasCatchTextDots = new ArrayList<>();

    View targetAddDot;
    View addLine;
    View editGraphic;
    View deleteGraphic;

    //上一步添点所在图层
    private String preDotLayer;

    public GisOptPanel(GisGather gisGather, View view) {
        this.gisGather = gisGather;
        this.view = view;
    }

    public void initGisOptPanel() {
        targetAddDot = view.findViewById(R.id.targetAddDot);
        targetAddDot.setOnClickListener(this);
        addLine = view.findViewById(R.id.addLine);
        addLine.setOnClickListener(this);
        editGraphic = view.findViewById(R.id.editGraphic);
        editGraphic.setOnClickListener(this);
        deleteGraphic = view.findViewById(R.id.deleteGraphic);
        deleteGraphic.setOnClickListener(this);
        setChosed(null);
    }

    @Override
    public void addDot2MapView() {

        if (gisGather.hasChoseGISDeviceSetBean.layerType == 2) {
            if (!TextUtils.isEmpty(preDotLayer)) {
                gisGather.layerShow.clickRadioButton(preDotLayer);
                MyApplication.getInstance().showMessageWithHandle("已为您自动选择<" + preDotLayer + ">");
            }
        }

        if (gisGather.hasChoseGISDeviceSetBean.layerType == 2) {
            Toast.makeText(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean.layerName + "上不允许添点，请重新选择", Toast.LENGTH_SHORT).show();
            return;
        }

        if (addDotSuccess == null) {
            addDotSuccess = new AddDotSuccess() {
                @Override
                public void addDotSuccessExec(final TextDot textDot) {
                    preDotLayer = textDot.gisDataBean.LayerName;

                    gisDataProject.setHasAddDoted(true);

                    //判断是否自动连线
                    if (!TextUtils.isEmpty(gisGather.layerShow.uniquenessLineLayer) && gisGather.isAutoLinkLine) {

                        int size = autoLinkLinehasCatchTextDots.size();
                        if (size >= 1) {
                            TextDot preTextDot = autoLinkLinehasCatchTextDots.get(size - 1);
                            startAddLine2MapView(preTextDot, textDot, gisGather.layerShow.uniquenessLineLayer, new AddLineSuccess() {
                                @Override
                                public void addLineSuccessExec() {
                                    autoLinkLinehasCatchTextDots.add(textDot);
                                    autoLinkLinehasCatchTextDots.remove(0);
                                }
                            });

                        }
                        if (size == 0) {
                            autoLinkLinehasCatchTextDots.add(textDot);
                        }
                    }

                    //编辑点属性
                    GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, false, textDot.gisDataBean, gisDataProject.getTodayGISData().textDots.size() - 1, GisDataGatherUtils.GisDataFrom.todayProject, gisGather.isPad);

                }
            };
        }
        if (queryFinishForAddGraphicDot == null) {
            queryFinishForAddGraphicDot = new QueryFinish() {
                @Override
                public void queryFinishExec(Graphic graphic) {
                    if (graphic != null) {
                        String layerName = graphic.getAttributeValue("$图层名称$");
                        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("所选位置已存在<" + layerName + ">，是否继续添加" + gisGather.hasChoseGISDeviceSetBean.layerName + "？");
                        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                            @Override
                            public void onRightButtonClick(View view) {
                                startAddDot2MapView(addDotSuccess);
                            }
                        });
                        okCancelDialogFragment.show(gisGather.mapGISFrame.getSupportFragmentManager(), "");
                        return;
                    }
                    startAddDot2MapView(addDotSuccess);
                }
            };
        }
        Dot curDot = gisGather.mapView.getCenterPoint();
        PointF curPointF = gisGather.mapView.mapPointToViewPoint(curDot);

        List<String> allEditPointLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 1);
        queryGraphicPoint(gisGather.mapGISFrame, gisGather.mapView, curDot, curPointF, allEditPointLayers, true, queryFinishForAddGraphicDot);

    }

    @Override
    public void addLine2MapView() {
        //只有一个管线，自动选择管线
        if (gisGather.hasChoseGISDeviceSetBean.layerType == 1) {
            if (TextUtils.isEmpty(gisGather.layerShow.uniquenessLineLayer)) {
                Toast.makeText(gisGather.mapGISFrame, "请手动选择管线", Toast.LENGTH_SHORT).show();
                return;
            }
            gisGather.layerShow.clickRadioButton(gisGather.layerShow.uniquenessLineLayer);
        }
        //自动选择失败，手动选择
        if (gisGather.hasChoseGISDeviceSetBean.layerType == 1) {
            Toast.makeText(gisGather.mapGISFrame, "无法在" + gisGather.hasChoseGISDeviceSetBean.layerName + "上连线，请手动选择管线", Toast.LENGTH_SHORT).show();
            return;
        }
        if (queryFinishForLinkLine == null) {
            queryFinishForLinkLine = new QueryFinish() {
                @Override
                public void queryFinishExec(Graphic graphic) {
                    if (graphic == null) {
                        Toast.makeText(gisGather.mapGISFrame, "未捕获到数据", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String flag = graphic.getAttributeValue("flag");
                    TextDot curTextDotTemp = null;
                    List<TextDot> allTextDots = new ArrayList<TextDot>() {{
                        addAll(gisDataProject.getTodayGISData().textDots);
                        addAll(gisDataProject.getTextDots());
                    }};
                    //主管网的点
                    if (TextUtils.isEmpty(flag)) {
                        String LayerName = graphic.getAttributeValue("$图层名称$");
                        String bdh = graphic.getAttributeValue("本点号");
                        if (TextUtils.isEmpty(bdh)) {
                            Toast.makeText(gisGather.mapGISFrame, "主管网<" + LayerName + ">不符合外接边条件（不含本点号）", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        GraphicPoint graphicPoint = (GraphicPoint) graphic;
                        Dot curDot = graphicPoint.getPoint();
                        String Operation = "编辑";//此处没有实际意义
                        String GeomType = "管点";
                        GISDataBeanBase gisDataBean = new GISDataBeanBase(graphic, Operation, LayerName, GeomType);
                        int from = GisDataGatherUtils.GisDataFrom.todayProject;
                        int state = TextDotState.OHTER.getState();
                        //当前curTextDotTemp只是为了连线所需，无实际意义（需含本点号）
                        curTextDotTemp = new TextDot(graphic, gisDataBean, from, curDot, state);
                    } else {
                        //自定义的点
                        if (graphic instanceof GraphicImage) {
                            if (TextUtils.isEmpty(flag)) {
                                Toast.makeText(gisGather.mapGISFrame, "未知错误", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            int from = Integer.parseInt(flag.split("-")[0]);
                            if (from == GisDataGatherUtils.GisDataFrom.mapNExist) {
                                Toast.makeText(gisGather.mapGISFrame, "暂不支持外接其他工程", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            GraphicImage graphicImage = (GraphicImage) graphic;
                            Dot curDot = graphicImage.getPoint();

                            //必须找到准确的textdot（需要本点号）
                            curTextDotTemp = GisDataGatherUtils.findTextDotInTextDots(allTextDots, curDot);
                        }
                    }
                    if (curTextDotTemp == null) {
                        Toast.makeText(gisGather.mapGISFrame, "未知错误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(curTextDotTemp.gisDataBean.事件编号)) {
                        Toast.makeText(gisGather.mapGISFrame, "本点号不存在，无法接边", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final TextDot curTextDot = curTextDotTemp;
                    int hasCatchTextDotsSize = gisGather.hasCatchTextDots.size();
                    if (hasCatchTextDotsSize >= 1) {
                        Dot preDot = gisGather.hasCatchTextDots.get(hasCatchTextDotsSize - 1).dot;
                        if (GisUtil.equals(curTextDot.dot, preDot, 0.001f)) {
                            Toast.makeText(gisGather.mapGISFrame, "不能和上一个点相同，请重新捕获", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<TextLine> allTextLines = new ArrayList<TextLine>() {{
                            addAll(gisDataProject.getTodayGISData().textLines);
                            addAll(gisDataProject.getTextLines());
                        }};
                        if (GisDataGatherUtils.hasContaionTextLine(allTextLines, preDot, curTextDot.dot) > -1) {
                            Toast.makeText(gisGather.mapGISFrame, "该线已连接过，请重新连接", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }


                    if (hasCatchTextDotsSize >= 1) {
                        TextDot preTextDot = gisGather.hasCatchTextDots.get(hasCatchTextDotsSize - 1);
                        startAddLine2MapView(preTextDot, curTextDot, gisGather.hasChoseGISDeviceSetBean.layerName, new AddLineSuccess() {
                            @Override
                            public void addLineSuccessExec() {
                                gisGather.hasCatchTextDots.add(curTextDot);
                                gisGather.hasCatchTextDots.remove(0);
                            }
                        });

                    }

                    curTextDot.stopAllGlint(allTextDots, gisGather.mapView);
                    curTextDot.glintTextDot(gisGather.mapView);

                    if (hasCatchTextDotsSize == 0) {
                        gisGather.hasCatchTextDots.add(curTextDot);
                    }

                }
            };
        }
        gisGather.hasCatchTextDots.clear();

        final List<String> allEditPointLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 1);

        gisGather.mapView.setTapListener(new MapView.MapViewTapListener() {
            @Override
            public void mapViewTap(PointF pointF) {
                Dot tagMapDot = gisGather.mapView.viewPointToMapPoint(pointF);

                queryGraphicPoint(gisGather.mapGISFrame, gisGather.mapView, tagMapDot, pointF, allEditPointLayers, false, queryFinishForLinkLine);

            }
        });
    }

    //编辑指定的图层
    @Override
    public void editGraphic() {
        if (queryFinishForEditGraphic == null) {
            queryFinishForEditGraphic = new QueryFinish() {
                @Override
                public void queryFinishExec(final Graphic graphic) {
                    if (graphic == null) {
                        MyApplication.getInstance().showMessageWithHandle("未捕获到数据");
                        return;
                    }

                    final String layerName = graphic.getAttributeValue("$图层名称$");

                    //是否可编辑图层
                    boolean isCanEdit = false;
                    List<String> allEditPointLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 1);
                    List<String> allEditLineLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 2);

                    if (allEditPointLayers != null && allEditPointLayers.size() > 0) {
                        if (allEditPointLayers.contains(layerName)) {
                            isCanEdit = true;
                        }
                    }

                    if (allEditLineLayers != null && allEditLineLayers.size() > 0) {
                        if (allEditLineLayers.contains(layerName)) {
                            isCanEdit = true;
                        }
                    }

                    if (!isCanEdit) {
                        MyApplication.getInstance().showMessageWithHandle("<" + layerName + ">不允许编辑");
                        return;
                    }
                    //设置图层选中状态
                    gisGather.layerShow.clickRadioButton(layerName);

                    String flag = graphic.getAttributeValue("flag");

                    //主管网数据(若编辑则将该数据加入到当前工程)
                    if (TextUtils.isEmpty(flag)) {
                        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("是否编辑主管网<" + layerName + ">？");
                        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                            @Override
                            public void onRightButtonClick(View view) {

                                if (graphic instanceof GraphicPoint) {
                                    GISDataBeanBase gisDataBeanBase = new GISDataBeanBase(graphic, "编辑", layerName, "管点");
                                    final TextDot textDot = new TextDot(gisGather.mapGISFrame, gisDataBeanBase, GisDataGatherUtils.GisDataFrom.todayProject, graphic.getCenterPoint(), TextDotState.EDIT.getState());
                                    textDot.submit2Server(gisGather.mapGISFrame, gisDataProject.getID(), new MmtBaseTask.OnWxyhTaskListener<String>() {
                                        @Override
                                        public void doAfter(String s) {
                                            ResultData<GISDataBeanBase> resultData = Utils.json2ResultDataToast(GISDataBeanBase.class, gisGather.mapGISFrame, s, "保存" + textDot.gisDataBean.LayerName + "失败", false);
                                            if (resultData == null) {
                                                return;
                                            }
                                            textDot.gisDataBean = resultData.getSingleData();

                                            gisDataProject.getTodayGISData().textDots.add(textDot);
                                            textDot.addTextDot(gisGather.mapView, true);
                                            GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, false, textDot.gisDataBean, gisDataProject.getTodayGISData().textDots.size() - 1, GisDataGatherUtils.GisDataFrom.todayProject, gisGather.isPad);

                                            List<TextDot> allTextDots = new ArrayList<TextDot>() {{
                                                addAll(gisDataProject.getTodayGISData().textDots);
                                                addAll(gisDataProject.getTextDots());
                                            }};
                                            textDot.stopAllGlint(allTextDots, gisGather.mapView);
                                            textDot.glintTextDot(gisGather.mapView);
                                        }
                                    });
                                    return;
                                }
                                if (graphic instanceof GraphicPolylin) {
                                    GraphicPolylin graphicPolylin = (GraphicPolylin) graphic;
                                    if (graphicPolylin.getPointSize() < 2) {
                                        return;
                                    }

                                    GISDataBeanBase gisDataBeanBase = new GISDataBeanBase(graphic, "编辑", layerName, "管线");
                                    TextDot textDot1 = new TextDot(gisGather.mapGISFrame, new GISDataBeanBase(), GisDataGatherUtils.GisDataFrom.todayProject, graphicPolylin.getPoint(0), TextDotState.OHTER.getState());
                                    TextDot textDot2 = new TextDot(gisGather.mapGISFrame, new GISDataBeanBase(), GisDataGatherUtils.GisDataFrom.todayProject, graphicPolylin.getPoint(1), TextDotState.OHTER.getState());
                                    List<TextDot> textDots = new ArrayList<>();
                                    textDots.add(textDot1);
                                    textDots.add(textDot2);
                                    final TextLine textLine = new TextLine(gisGather.mapGISFrame, textDots, gisDataBeanBase, GisDataGatherUtils.GisDataFrom.todayProject, TextLineState.EDIT.getState());

                                    textLine.submit2Server(gisGather.mapGISFrame, gisDataProject.getID(), new MmtBaseTask.OnWxyhTaskListener<String>() {
                                        @Override
                                        public void doAfter(String s) {
                                            ResultData<GISDataBeanBase> resultData = Utils.json2ResultDataToast(GISDataBeanBase.class, gisGather.mapGISFrame, s, "保存" + textLine.gisDataBean.LayerName + "失败", false);
                                            if (resultData == null) {
                                                return;
                                            }
                                            textLine.gisDataBean = resultData.getSingleData();
                                            gisDataProject.getTodayGISData().textLines.add(textLine);
                                            textLine.addLine(gisGather.mapView, true);

                                            GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, false, textLine.gisDataBean, gisDataProject.getTodayGISData().textLines.size() - 1, GisDataGatherUtils.GisDataFrom.todayProject, gisGather.isPad);
                                        }
                                    });
                                }
                            }
                        });
                        okCancelDialogFragment.show(gisGather.mapGISFrame.getSupportFragmentManager(), "");
                        return;
                    }


                    //自定义的graphic
                    String[] flags = flag.split("-");
                    final int from = Integer.parseInt(flags[0]);

                    //自定义中非当前工程(若编辑则将该数据加入到当前工程)
                    if (from == GisDataGatherUtils.GisDataFrom.mapNExist) {
                        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("是否编辑其他工程中的<" + layerName + ">？");
                        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                            @Override
                            public void onRightButtonClick(View view) {

                                if (graphic instanceof GraphicPoint) {

                                    GISDataBeanBase gisDataBeanBase = new GISDataBeanBase(graphic, "编辑", layerName, "管点");
                                    final TextDot textDot = new TextDot(gisGather.mapGISFrame, gisDataBeanBase, GisDataGatherUtils.GisDataFrom.todayProject, graphic.getCenterPoint(), TextDotState.EDIT.getState());

                                    textDot.submit2Server(gisGather.mapGISFrame, gisDataProject.getID(), new MmtBaseTask.OnWxyhTaskListener<String>() {
                                        @Override
                                        public void doAfter(String s) {
                                            ResultData<GISDataBeanBase> resultData = Utils.json2ResultDataToast(GISDataBeanBase.class, gisGather.mapGISFrame, s, "保存" + textDot.gisDataBean.LayerName + "失败", false);
                                            if (resultData == null) {
                                                return;
                                            }
                                            textDot.gisDataBean = resultData.getSingleData();
                                            GisGather.gisDataProject.getTodayGISData().textDots.add(textDot);
                                            GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, false, textDot.gisDataBean, gisDataProject.getTodayGISData().textDots.size() - 1, GisDataGatherUtils.GisDataFrom.todayProject, gisGather.isPad);

                                            List<TextDot> allTextDots = new ArrayList<TextDot>() {{
                                                addAll(gisDataProject.getTodayGISData().textDots);
                                                addAll(gisDataProject.getTextDots());
                                            }};
                                            textDot.stopAllGlint(allTextDots, gisGather.mapView);
                                            textDot.glintTextDot(gisGather.mapView);
                                        }
                                    });
                                    return;
                                }
                                if (graphic instanceof GraphicPolylin) {
                                    GraphicPolylin graphicPolylin = (GraphicPolylin) graphic;
                                    if (graphicPolylin.getPointSize() < 2) {
                                        return;
                                    }
                                    GISDataBeanBase gisDataBeanBase = new GISDataBeanBase(graphic, "编辑", layerName, "管线");
                                    TextDot textDot1 = new TextDot(gisGather.mapGISFrame, new GISDataBeanBase(), GisDataGatherUtils.GisDataFrom.todayProject, graphicPolylin.getPoint(0), TextDotState.OHTER.getState());
                                    TextDot textDot2 = new TextDot(gisGather.mapGISFrame, new GISDataBeanBase(), GisDataGatherUtils.GisDataFrom.todayProject, graphicPolylin.getPoint(1), TextDotState.OHTER.getState());
                                    List<TextDot> textDots = new ArrayList<>();
                                    textDots.add(textDot1);
                                    textDots.add(textDot2);
                                    final TextLine textLine = new TextLine(gisGather.mapGISFrame, textDots, gisDataBeanBase, GisDataGatherUtils.GisDataFrom.todayProject, TextLineState.EDIT.getState());

                                    textLine.submit2Server(gisGather.mapGISFrame, gisDataProject.getID(), new MmtBaseTask.OnWxyhTaskListener<String>() {
                                        @Override
                                        public void doAfter(String s) {
                                            ResultData<GISDataBeanBase> resultData = Utils.json2ResultDataToast(GISDataBeanBase.class, gisGather.mapGISFrame, s, "保存" + textLine.gisDataBean.LayerName + "失败", false);
                                            if (resultData == null) {
                                                return;
                                            }
                                            textLine.gisDataBean = resultData.getSingleData();
                                            gisDataProject.getTodayGISData().textLines.add(textLine);
                                            GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, false, textLine.gisDataBean, gisDataProject.getTodayGISData().textLines.size() - 1, GisDataGatherUtils.GisDataFrom.todayProject, gisGather.isPad);
                                        }
                                    });
                                }
                            }
                        });
                        okCancelDialogFragment.show(gisGather.mapGISFrame.getSupportFragmentManager(), "");
                        return;
                    }


                    //当前工程

                    if (graphic instanceof GraphicPolylin) {
                        GraphicPolylin graphicPolylin = (GraphicPolylin) graphic;
                        final int dataIndexToday = GisDataGatherUtils.hasContaionLine(gisDataProject.getTodayGISData().textLines, graphicPolylin);
                        final int dataIndexCurrent = GisDataGatherUtils.hasContaionLine(gisDataProject.getTextLines(), graphicPolylin);

                        if (dataIndexToday < 0 && dataIndexCurrent < 0) {
                            return;
                        }

                        TextLine curPojTextLine;
                        if (dataIndexToday >= 0) {
                            curPojTextLine = gisDataProject.getTodayGISData().textLines.get(dataIndexToday);
                        } else {
                            curPojTextLine = gisDataProject.getTextLines().get(dataIndexCurrent);
                        }
                        if (curPojTextLine == null) {
                            return;
                        }
                        if (dataIndexToday > -1) {
                            GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, false, curPojTextLine.gisDataBean, dataIndexToday, GisDataGatherUtils.GisDataFrom.todayProject, gisGather.isPad);
                        } else {
                            GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, false, curPojTextLine.gisDataBean, dataIndexCurrent, GisDataGatherUtils.GisDataFrom.currentProject, gisGather.isPad);
                        }

                        return;

                    }

                    Dot curDot = null;
                    if (graphic instanceof GraphicPoint) {
                        curDot = ((GraphicPoint) graphic).getPoint();
                    }
                    if (graphic instanceof GraphicImage) {
                        curDot = ((GraphicImage) graphic).getPoint();
                    }
                    if (curDot == null) {
                        return;
                    }

                    final int dataIndexToday = GisDataGatherUtils.hasContaionDot(gisDataProject.getTodayGISData().textDots, curDot);

                    final int dataIndexCurrent = GisDataGatherUtils.hasContaionDot(gisDataProject.getTextDots(), curDot);

                    if (dataIndexToday == -1 && dataIndexCurrent == -1) {
                        return;
                    }

                    TextDot curTextDot;
                    if (dataIndexToday > -1) {
                        curTextDot = gisDataProject.getTodayGISData().textDots.get(dataIndexToday);
                        GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, false, curTextDot.gisDataBean, dataIndexToday, GisDataGatherUtils.GisDataFrom.todayProject, gisGather.isPad);

                    } else {
                        curTextDot = gisDataProject.getTextDots().get(dataIndexCurrent);

                        GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, false, curTextDot.gisDataBean, dataIndexCurrent, GisDataGatherUtils.GisDataFrom.currentProject, gisGather.isPad);
                    }

                    List<TextDot> allTextDots = new ArrayList<TextDot>() {{
                        addAll(gisDataProject.getTodayGISData().textDots);
                        addAll(gisDataProject.getTextDots());
                    }};
                    curTextDot.stopAllGlint(allTextDots, gisGather.mapView);
                    curTextDot.glintTextDot(gisGather.mapView);

                }
            };
        }
        final List<String> allEditPointLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 1);
        final List<String> allEditLineLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 2);

        gisGather.mapView.setTapListener(new MapView.MapViewTapListener() {
            @Override
            public void mapViewTap(PointF pointF) {
                Dot queryDot = gisGather.mapView.viewPointToMapPoint(pointF);
                queryGraphic(gisGather.mapGISFrame, gisGather.mapView, queryDot, pointF, allEditPointLayers, allEditLineLayers, false, queryFinishForEditGraphic);

            }
        });
    }

    private QueryFinish queryFinishForDeleteGraphic;
    private QueryFinish queryFinishForEditGraphic;
    private QueryFinish queryFinishForLinkLine;
    private QueryFinish queryFinishForAddGraphicDot;

    private AddDotSuccess addDotSuccess;

    //只允许删除当前工程中可编辑图层的graphic
    @Override
    public void deleteGraphic() {
        if (queryFinishForDeleteGraphic == null) {
            queryFinishForDeleteGraphic = new QueryFinish() {
                @Override
                public void queryFinishExec(Graphic graphic) {

                    if (graphic == null) {
                        MyApplication.getInstance().showMessageWithHandle("未捕获到数据");
                        return;
                    }

                    String layerName = graphic.getAttributeValue("$图层名称$");

                    String flag = graphic.getAttributeValue("flag");
                    //主管网
                    if (TextUtils.isEmpty(flag)) {
                        MyApplication.getInstance().showMessageWithHandle("主管网<" + layerName + ">，不允许删除");
                        return;
                    }
                    //自定义的graphic
                    String[] flags = flag.split("-");
                    int from = Integer.parseInt(flags[0]);
                    if (from == GisDataGatherUtils.GisDataFrom.mapNExist) {
                        MyApplication.getInstance().showMessageWithHandle("非当前工程<" + layerName + ">，不允许删除");
                        return;
                    }

                    //是否是可删除图层
                    boolean isCanDeleted = false;
                    List<String> allEditPointLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 1);
                    List<String> allEditLineLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 2);

                    if (allEditPointLayers != null && allEditPointLayers.size() > 0) {
                        if (allEditPointLayers.contains(layerName)) {
                            isCanDeleted = true;
                        }
                    }

                    if (allEditLineLayers != null && allEditLineLayers.size() > 0) {
                        if (allEditLineLayers.contains(layerName)) {
                            isCanDeleted = true;
                        }
                    }

                    if (!isCanDeleted) {
                        MyApplication.getInstance().showMessageWithHandle("<" + layerName + ">不允许删除");
                        return;
                    }

                    //开始删除
                    if (graphic instanceof GraphicPolylin) {
                        GraphicPolylin graphicPolylin = (GraphicPolylin) graphic;
                        final int dataIndexToday = GisDataGatherUtils.hasContaionLine(gisDataProject.getTodayGISData().textLines, graphicPolylin);
                        final int dataIndexCurrent = GisDataGatherUtils.hasContaionLine(gisDataProject.getTextLines(), graphicPolylin);

                        if (dataIndexToday < 0 && dataIndexCurrent < 0) {
                            MyApplication.getInstance().showMessageWithHandle("未知错误");
                            return;
                        }

                        TextLine curPojTextLine;
                        if (dataIndexToday >= 0) {
                            curPojTextLine = gisDataProject.getTodayGISData().textLines.get(dataIndexToday);
                        } else {
                            curPojTextLine = gisDataProject.getTextLines().get(dataIndexCurrent);
                        }
                        if (curPojTextLine == null) {
                            return;
                        }

                        final TextLine textLine = curPojTextLine;
                        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定删除管线<" + layerName + ">?");
                        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                            @Override
                            public void onRightButtonClick(View view) {
                                textLine.gisDataBean.deleteFromServer(gisGather.mapGISFrame, new MmtBaseTask.OnWxyhTaskListener<String>() {
                                    @Override
                                    public void doAfter(String s) {
                                        ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(gisGather.mapGISFrame, s, "删除GIS数据失败", "删除GIS数据成功");
                                        if (resultWithoutData == null) {
                                            return;
                                        }
                                        textLine.deleteTextLine(gisGather.mapView, true);
                                        if (dataIndexToday > -1) {
                                            gisDataProject.getTodayGISData().textLines.remove(dataIndexToday);
                                        } else {
                                            gisDataProject.getTextLines().remove(dataIndexCurrent);
                                        }
                                    }
                                });
                            }
                        });
                        okCancelDialogFragment.show(gisGather.mapGISFrame.getSupportFragmentManager(), "");
                        return;
                    }

                    Dot pointDot = null;
                    if (graphic instanceof GraphicPoint) {
                        GraphicPoint graphicPoint = (GraphicPoint) graphic;
                        pointDot = graphicPoint.getPoint();
                    }
                    if (graphic instanceof GraphicImage) {
                        GraphicImage graphicImage = (GraphicImage) graphic;
                        pointDot = graphicImage.getPoint();
                    }
                    if (pointDot == null) {
                        return;
                    }


                    final int dataIndexToday = GisDataGatherUtils.hasContaionDot(gisDataProject.getTodayGISData().textDots, pointDot);

                    final int dataIndexCurrent = GisDataGatherUtils.hasContaionDot(gisDataProject.getTextDots(), pointDot);

                    if (dataIndexToday < 0 && dataIndexCurrent < 0) {
                        MyApplication.getInstance().showMessageWithHandle("未知错误");
                        return;
                    }

                    TextDot textDotTemp;
                    if (dataIndexToday >= 0) {
                        textDotTemp = gisDataProject.getTodayGISData().textDots.get(dataIndexToday);
                    } else {
                        textDotTemp = gisDataProject.getTextDots().get(dataIndexCurrent);
                    }
                    if (textDotTemp == null) {
                        return;
                    }
                    final TextDot textDot = textDotTemp;

                    OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定删除管点<" + layerName + ">?");
                    okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                        @Override
                        public void onRightButtonClick(View view) {
                            textDot.gisDataBean.deleteFromServer(gisGather.mapGISFrame, new MmtBaseTask.OnWxyhTaskListener<String>() {
                                @Override
                                public void doAfter(String s) {
                                    ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(gisGather.mapGISFrame, s, "删除GIS数据失败", "删除GIS数据成功");
                                    if (resultWithoutData == null) {
                                        return;
                                    }
                                    textDot.deleteTextDot(gisGather.mapView, true);
                                    textDot.deleteLinkLine(gisGather.mapGISFrame, gisGather.mapView, gisDataProject.getTodayGISData().textLines, gisDataProject.getTextLines());

                                    if (dataIndexToday > -1) {
                                        gisDataProject.getTodayGISData().textDots.remove(dataIndexToday);
                                    } else {
                                        gisDataProject.getTextDots().remove(dataIndexToday);
                                    }
                                    //如果删除的点是连线需要的点，则将连线容器中的点也删除
                                    if (gisGather.gisOpt.autoLinkLinehasCatchTextDots.contains(textDot)) {
                                        gisGather.gisOpt.autoLinkLinehasCatchTextDots.remove(textDot);
                                    }
                                    if (gisGather.hasCatchTextDots.contains(textDot)) {
                                        gisGather.hasCatchTextDots.remove(textDot);
                                    }
                                }
                            });
                        }
                    });
                    okCancelDialogFragment.show(gisGather.mapGISFrame.getSupportFragmentManager(), "");
                }
            };
        }
        final List<String> allEditPointLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 1);
        final List<String> allEditLineLayers = GisDataGatherUtils.getAllEditLayer(gisGather.gisDeviceSetBeans, 2);

        gisGather.mapView.setTapListener(new MapView.MapViewTapListener() {
            @Override
            public void mapViewTap(PointF pointF) {
                Dot queryDot = gisGather.mapView.viewPointToMapPoint(pointF);
                queryGraphic(gisGather.mapGISFrame, gisGather.mapView, queryDot, pointF, allEditPointLayers, allEditLineLayers, false, queryFinishForDeleteGraphic);
            }
        });
    }


    interface QueryFinish {
        void queryFinishExec(Graphic graphic);
    }

    private void queryGraphic(Context context, final MmtMapView mapView, final Dot queryDot, final PointF pointF, final List<String> queryPointLayers, final List<String> queryLineLayers, final boolean isExact, final QueryFinish queryFinish) {
        new MmtBaseTask<Void, Void, Graphic>(context) {
            @Override
            protected Graphic doInBackground(Void... params) {
                String typeStr = "Point";

                String attrName = "flag";
                String attrValue = GisDataGatherUtils.GisDataFrom.todayProject + "-" + typeStr;

                //自定义点查询
                Graphic graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryPointLayers, attrName, attrValue, isExact);

                if (graphic == null) {
                    attrValue = GisDataGatherUtils.GisDataFrom.currentProject + "-" + typeStr;
                    graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryPointLayers, attrName, attrValue, isExact);
                }
                if (graphic == null) {
                    attrValue = GisDataGatherUtils.GisDataFrom.mapNExist + "-" + typeStr;
                    graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryPointLayers, attrName, attrValue, isExact);
                }

                typeStr = "Line";
                //自定义线查询
                if (graphic == null) {
                    attrValue = GisDataGatherUtils.GisDataFrom.todayProject + "-" + typeStr;
                    graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryLineLayers, attrName, attrValue, isExact);
                }
                if (graphic == null) {
                    attrValue = GisDataGatherUtils.GisDataFrom.currentProject + "-" + typeStr;
                    graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryLineLayers, attrName, attrValue, isExact);
                }
                if (graphic == null) {
                    attrValue = GisDataGatherUtils.GisDataFrom.mapNExist + "-" + typeStr;
                    graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryLineLayers, attrName, attrValue, isExact);
                }

                //主管网点线查询
                if (graphic == null) {
                    graphic = GisQueryUtil.pointQueryForSingle(mapView, queryDot);
                }
                return graphic;
            }

            @Override
            protected void onSuccess(Graphic graphic) {
                super.onSuccess(graphic);
                if (queryFinish != null) {
                    queryFinish.queryFinishExec(graphic);
                }
            }
        }.mmtExecute();
    }


    private void queryGraphicPoint(Context context, final MmtMapView mapView, final Dot queryDot, final PointF pointF, final List<String> queryLayers, final boolean isExact, final QueryFinish queryFinish) {
        new MmtBaseTask<Void, Void, Graphic>(context) {
            @Override
            protected Graphic doInBackground(Void... params) {
                String typeStr = "Point";

                String attrName = "flag";
                String attrValue = GisDataGatherUtils.GisDataFrom.todayProject + "-" + typeStr;

                //自定义点查询
                Graphic graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryLayers, attrName, attrValue, isExact);

                if (graphic == null) {
                    attrValue = GisDataGatherUtils.GisDataFrom.currentProject + "-" + typeStr;
                    graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryLayers, attrName, attrValue, isExact);
                }
                if (graphic == null) {
                    attrValue = GisDataGatherUtils.GisDataFrom.mapNExist + "-" + typeStr;
                    graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryLayers, attrName, attrValue, isExact);
                }

                //主管网点查询
                if (graphic == null) {
                    graphic = GisQueryUtil.pointQueryForSingle(mapView, queryDot);
                    if (graphic instanceof GraphicPoint) {
                        return graphic;
                    }
                    graphic = null;
                }
                return graphic;
            }

            @Override
            protected void onSuccess(Graphic graphic) {
                super.onSuccess(graphic);
                if (queryFinish != null) {
                    queryFinish.queryFinishExec(graphic);
                }
            }
        }.mmtExecute();
    }

    @Override
    public void onClick(View v) {
        if (gisGather.mapView == null || gisGather.mapView.getMap() == null) {
            gisGather.mapGISFrame.stopMenuFunction();
            return;
        }
        gisGather.mapView.setTapListener(null);
        setChosed(v);
        String text = ((TextView) ((ViewGroup) v).getChildAt(0)).getText().toString();
        switch (text) {
            case "添点": {
                addDot2MapView();
            }
            break;
            case "连线": {
                addLine2MapView();
            }
            break;
            case "编辑": {
                editGraphic();
            }
            break;
            case "删除": {
                deleteGraphic();
            }
            break;
            default: {
            }
        }
    }


    private void setChosed(View view) {
        targetAddDot.setBackgroundResource(R.drawable.corners_bg);
        addLine.setBackgroundResource(R.drawable.corners_bg);
        editGraphic.setBackgroundResource(R.drawable.corners_bg);
        deleteGraphic.setBackgroundResource(R.drawable.corners_bg);
        if (view == null) {
            return;
        }
        view.setBackgroundResource(R.drawable.corners_bg_color);
    }

    interface AddLineSuccess {
        void addLineSuccessExec();
    }

    private void startAddLine2MapView(final TextDot textDot1, final TextDot textDot2, final String layerNamae, final AddLineSuccess addLineSuccess) {
        GISDataBeanBase gisDataBeanBase = new GISDataBeanBase();
        if (GisGather.layerDefaultAttrs.containsKey(layerNamae)) {
            gisDataBeanBase = gisDataBeanBase.copyFromGISDataBeanBase(layerDefaultAttrs.get(layerNamae));
            gisDataBeanBase.copyNewAttrs2OldAttrs();
        }

        List<TextDot> textDots = new ArrayList<TextDot>() {{
            add(textDot1);
            add(textDot2);
        }};
        gisDataBeanBase.GeomType = "管线";
        gisDataBeanBase.LayerName = layerNamae;
        gisDataBeanBase.NewGeom = textDots.get(0).dot.toString() + "|" + textDots.get(1).dot.toString();
        gisDataBeanBase.Operation = "新增";

        final TextLine tl = new TextLine(gisGather.mapGISFrame, textDots, gisDataBeanBase, GisDataGatherUtils.GisDataFrom.todayProject, TextLineState.ADD.getState());


        tl.submit2Server(gisGather.mapGISFrame, gisDataProject.getID(), new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String s) {
                ResultData<GISDataBeanBase> resultData = Utils.json2ResultDataToast(GISDataBeanBase.class, gisGather.mapGISFrame, s, "保存" + layerNamae + "失败", false);
                if (resultData == null) {
                    return;
                }
                tl.gisDataBean = resultData.getSingleData();
                gisDataProject.getTodayGISData().textLines.add(tl);
                tl.addLine(gisGather.mapView, true);

                if (addLineSuccess != null) {
                    addLineSuccess.addLineSuccessExec();
                }
            }
        });
    }

    interface AddDotSuccess {
        void addDotSuccessExec(TextDot textDot);
    }

    private void startAddDot2MapView(final AddDotSuccess addDotSuccess) {

        GISDataBeanBase gisDataBean = new GISDataBeanBase();
        if (layerDefaultAttrs.containsKey(gisGather.hasChoseGISDeviceSetBean.layerName)) {
            gisDataBean = gisDataBean.copyFromGISDataBeanBase(layerDefaultAttrs.get(gisGather.hasChoseGISDeviceSetBean.layerName));
            gisDataBean.copyNewAttrs2OldAttrs();
        }
        final String layerName = gisGather.hasChoseGISDeviceSetBean.layerName;
        gisDataBean.GeomType = "管点";
        gisDataBean.LayerName = layerName;
        gisDataBean.NewGeom = gisGather.mapView.getCenterPoint().toString();
        gisDataBean.Operation = "新增";

        final TextDot td = new TextDot(gisGather.mapGISFrame, gisDataBean, GisDataGatherUtils.GisDataFrom.todayProject, gisGather.mapView.getCenterPoint(), TextDotState.ADD.getState());
        td.submit2Server(gisGather.mapGISFrame, gisDataProject.getID(), new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String s) {
                ResultData<GISDataBeanBase> resultData = Utils.json2ResultDataToast(GISDataBeanBase.class, gisGather.mapGISFrame, s, "保存" + layerName + "失败", false);
                if (resultData == null) {
                    return;
                }
                td.gisDataBean = resultData.getSingleData();
                gisDataProject.getTodayGISData().textDots.add(td);

                td.addTextDot(gisGather.mapView, true);

                List<TextDot> allTextDots = new ArrayList<TextDot>() {{
                    addAll(gisDataProject.getTodayGISData().textDots);
                    addAll(gisDataProject.getTextDots());
                }};

                td.stopAllGlint(allTextDots, gisGather.mapView);
                td.glintTextDot(gisGather.mapView);


                if (addDotSuccess != null) {
                    addDotSuccess.addDotSuccessExec(td);
                }
            }
        });
    }
}
