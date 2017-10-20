package com.mapgis.mmt.module.gis.toolbar.accident;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentAnnotation;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentFeature;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentResult;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class AccidentQueryTask extends AsyncTask<Dot, Void, String> {
    private final List<AccidentFeature> features;
    private final PipeAccidentMenu menu;
    private final MapGISFrame mapGISFrame;
    private final MapView mapView;
    public static String strResult;
    private AccidentResult accidentResult;

    public AccidentResult getAccidentResult() {
        return accidentResult;
    }

    /**
     * 手动选择的二次关阀的设备
     */
    private final List<AccidentFeature> twiceFeatures = new ArrayList<>();

    /**
     * 不同图层采用的不同图片
     */
    public final static int[] resources = new int[]{R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd,
            R.drawable.icon_marke};

    Rect rect;

    /**
     * 爆管分析服务
     *
     * @param mapGISFrame 爆管分析所在的Activity界面
     * @param menu        爆管分析的菜单
     * @param features    二次分析所需要的设备信息，为null或者长度大于0时，进行爆管分析，否则对该数据进行二次关阀
     */
    public AccidentQueryTask(MapGISFrame mapGISFrame, PipeAccidentMenu menu, List<AccidentFeature> features) {
        this.features = features;
        this.menu = menu;
        this.mapGISFrame = mapGISFrame;
        this.mapView = mapGISFrame.getMapView();

        rect = this.mapView.getDispRange();
    }

    @Override
    protected void onPreExecute() {
        menu.showProgressBar(true);
    }

    @Override
    protected String doInBackground(Dot... params) {
        Dot tagMapDot = params[0];

        // 爆管分析查询的URL地址
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/services/zondy_mapgiscitysvr_pipeaccident/rest/pipeaccidentrest.svc/" + MobileConfig.MapConfigInstance.VectorService
                + "/accidentserver/accident";

        // 屏幕的像素
        int hpx = MyApplication.getInstance().getResources().getDisplayMetrics().heightPixels;
        int wpx = MyApplication.getInstance().getResources().getDisplayMetrics().widthPixels;

        String result;

        // 有设备属性信息，对这些设备进行二次爆管
        if (features != null && features.size() > 0) {
            List<String> valveNods = new ArrayList<>();

            // 遍历属性信息，获取需要的ElemID
            for (AccidentFeature feature : features) {
                if (feature.geometry != null) {
                    LinkedHashMap<String, String> attrMap = feature.attributes.attrStrToMap();

                    // 获取所系的参数值
                    if (attrMap.containsKey("ElemID")) {
                        valveNods.add(attrMap.get("ElemID"));
                    }
                }
            }

            // 二次爆管的地址和参数，主要区别在于加了一个valveNodes参数
            result = NetUtil.executeHttpGet(url, "isexactAcc", "false", "imageDisplay", hpx + "," + wpx + ",96", "tolerance", "10",
                    "geometryType", "Point", "f", "json", "mapExtent", rect.toString(), "geometry", tagMapDot.x + ","
                            + tagMapDot.y, "valveNods", BaseClassUtil.listToString(valveNods));

        } else {// 对点击的位置进行爆管分析
            result = NetUtil.executeHttpGet(url, "isexactAcc", "false", "imageDisplay", hpx + "," + wpx + ",96", "tolerance", "10",
                    "geometryType", "Point", "f", "json", "mapExtent", rect.toString(), "geometry", tagMapDot.x + ","
                            + tagMapDot.y);
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {

        try {
            if (result != null && result.trim().length() != 0) {

                // 去除多余的符号
                result = result.replace("\\\"", "");

                strResult = result;

                final AccidentResult accidentResult = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                        .fromJson(result, AccidentResult.class);

                if (accidentResult != null && accidentResult.getAllAnnotationCount() == 0){
                    selectPointAgain();
                    return ;
                }

//                if (accidentResult != null && accidentResult.line != null) {
//                    accidentResult.line.isAnnotationShow = false;
//                }

                this.accidentResult = accidentResult;
                // 将查询结果显示在地图上
                showResult(accidentResult);
            } else {
                selectPointAgain();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {// 通知前台查询完成
            menu.showProgressBar(false);
        }
    }

    /**
     * 结果为空，提示重新选择点
     */
    private void selectPointAgain() {
        // 未查询到结果，请重新选择
//      mapGISFrame.showToast("未查询到结果:确认是否点击的是有效点,或者确认GIS服务器是否开启");
        mapView.getGraphicLayer().removeAllGraphics();
        mapView.getAnnotationLayer().removeAllAnnotations();
        mapView.refresh();
    }

    /**
     * 查询到结果后，重置界面功能
     *
     * @param accidentResult 爆管分析结果
     */
    private void showResult(final AccidentResult accidentResult) {
        mapView.getGraphicLayer().removeAllGraphics();
        mapView.getAnnotationLayer().removeAllAnnotations();

        if (accidentResult == null) {
            Toast.makeText(mapGISFrame, "未查询到爆管分析结果", Toast.LENGTH_SHORT).show();
            return;
        }

        // 若返回的结果中含有错误信息，则将错误信息显示出来
        if (accidentResult.errorMsg != null && accidentResult.errorMsg.trim().length() > 0) {
            Toast.makeText(mapGISFrame, accidentResult.errorMsg, Toast.LENGTH_SHORT).show();
        }

        // 详情按钮，点击显示详情
        menu.getQueryResultListImg().setVisibility(View.VISIBLE);
        menu.getQueryResultListImg().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                detailOnClick(accidentResult);
            }
        });

        // 绘制结果到地图上
        accidentResult.valve.isAnnotationShow = true;
        showResultOnMap(accidentResult);

        // 展现结果到地图上
        PipeAccidentMenu.state = PipeAccidentMenu.STATE_SHOW_RESULT | PipeAccidentMenu.state;

        mapView.setTapListener(null);

        mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
            @Override
            public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                Annotation annotation = annotationview.getAnnotation();

                if (annotation instanceof AccidentAnnotation) {
                    AccidentAnnotation accidentAnnotation = (AccidentAnnotation) annotation;
                    toDetailActivity(accidentAnnotation.accidentFeature.attributes.attrStrToMap(), "设备详情");
                }
            }
        });
    }

    /**
     * 点击显示设备详细属性
     */
    private void toDetailActivity(LinkedHashMap<String, String> attrMap, String value) {
//        Intent intent = new Intent(mapGISFrame, ActivityClassRegistry.getInstance().getActivityClass(ActivityAlias.PIPE_DETAIL_ACTIVITY));
        Intent intent = new Intent(mapGISFrame, PipeDetailActivity.class);

        intent.putExtra("FragmentClass", DetailListFragment.class);
        intent.putExtra("layerName", value);
        intent.putExtra("graphicMap", attrMap);
        intent.putExtra("graphicMapStr", new Gson().toJson(attrMap));
        intent.putExtra("unvisiable_detail_fragment", true);
//        mapGISFrame.startActivityForResult(intent, 0);
        mapGISFrame.startActivity(intent);
    }

    /**
     * 将结果信息绘制到地图上
     */
    public void showOnMap(AccidentResult accidentResult) {
        mapView.getGraphicLayer().removeAllGraphics();
        mapView.getAnnotationLayer().removeAllAnnotations();

        if (accidentResult.valve != null) {

            if (accidentResult.valve.isAnnotationShow) {
                mapView.getAnnotationLayer()
                        .addAnnotations(
                                accidentResult.valve.getAnnotations(" 需关阀门",
                                        BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[0])));
            }

            mapView.getGraphicLayer().addGraphics(accidentResult.valve.getPolylins());
        }

        if (accidentResult.user != null) {

            if (accidentResult.user.isAnnotationShow) {
                mapView.getAnnotationLayer().addAnnotations(
                        accidentResult.user.getAnnotations(" 用户", BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[4])));
            }

            mapView.getGraphicLayer().addGraphics(accidentResult.user.getPolylins());
        }

        if (accidentResult.line != null) {

            if (accidentResult.line.isAnnotationShow) {
                mapView.getAnnotationLayer().addAnnotations(
                        accidentResult.line.getAnnotations("影响管段", BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[1])));
            }

            mapView.getGraphicLayer().addGraphics(accidentResult.line.getPolylins());
        }

        if (accidentResult.source != null) {

            if (accidentResult.source.isAnnotationShow) {
                mapView.getAnnotationLayer().addAnnotations(
                        accidentResult.source.getAnnotations("水表",
                                BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[2])));
            }

            mapView.getGraphicLayer().addGraphics(accidentResult.source.getPolylins());
        }

        if (accidentResult.center != null && accidentResult.center.isAnnotationShow) {
            mapView.getAnnotationLayer()
                    .addAnnotations(accidentResult.center.getAnnotations("接水点",
                            BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[3])));

            mapView.getGraphicLayer().addGraphics(accidentResult.center.getPolylins());
        }

        mapView.refresh();
    }

    public void showResultOnMap(){
        showResultOnMap(accidentResult);
    }

    /**
     * 将结果信息绘制到地图上
     */
    public void showResultOnMap(AccidentResult accidentResult) {
        mapView.getGraphicLayer().removeAllGraphics();
        mapView.getAnnotationLayer().removeAllAnnotations();

        if (accidentResult.valve != null) {

            if (accidentResult.valve.isAnnotationShow) {
                mapView.getAnnotationLayer()
                        .addAnnotations(
                                accidentResult.valve.getAnnotations(
                                        BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[0])));
            }

            mapView.getGraphicLayer().addGraphics(accidentResult.valve.getPolylins());
        }

        if (accidentResult.user != null) {

            if (accidentResult.user.isAnnotationShow) {
                mapView.getAnnotationLayer().addAnnotations(
                        accidentResult.user.getAnnotations(BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[4])));
            }

            mapView.getGraphicLayer().addGraphics(accidentResult.user.getPolylins());
        }

        if (accidentResult.line != null) {

            if (accidentResult.line.isAnnotationShow) {
                mapView.getAnnotationLayer().addAnnotations(
                        accidentResult.line.getAnnotations(BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[1])));
            }

            mapView.getGraphicLayer().addGraphics(accidentResult.line.getPolylins());
        }

        if (accidentResult.source != null) {

            if (accidentResult.source.isAnnotationShow) {
                mapView.getAnnotationLayer().addAnnotations(
                        accidentResult.source.getAnnotations(
                                BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[2])));
            }

            mapView.getGraphicLayer().addGraphics(accidentResult.source.getPolylins());
        }

        if (accidentResult.center != null && accidentResult.center.isAnnotationShow) {
            mapView.getAnnotationLayer()
                    .addAnnotations(accidentResult.center.getAnnotations(
                            BitmapFactory.decodeResource(mapGISFrame.getResources(), resources[3])));

            mapView.getGraphicLayer().addGraphics(accidentResult.center.getPolylins());
        }

        mapView.refresh();
    }

    /**
     * 详情按钮的点击事件
     *
     * @param accidentResult 爆管分析结果
     */
    private void detailOnClick(final AccidentResult accidentResult) {
        String[] functions = new String[]{"查询结果管理", "二次关阀选取设备", "二次关阀开始分析", "关闭选取设备"};

        ListDialogFragment fragment = new ListDialogFragment("功能选择", functions);

        fragment.setListItemClickListener(new OnListItemClickListener() {
            @Override
            public void onListItemClick(int arg2, String value) {
                switch (arg2) {

                    case 0:
//					final AccidentCheckFragment fragment = new AccidentCheckFragment(mapGISFrame,accidentResult);
//					fragment.setOnOkClickListener(new OnOkClickListener() {
//						@Override
//						public void onClick() {
//							showOnMap(accidentResult);
//							fragment.dismiss();
//						}
//					});
//					fragment.show(mapGISFrame.getSupportFragmentManager(), "");

                        PipeAccidentMenu.state = PipeAccidentMenu.STATE_SHOW_LIST_ACTIVITY | PipeAccidentMenu.state;
                        Intent intent = new Intent(mapGISFrame, AccidentCheckActivity.class);
                        // 传递字符串类型的结果数据
                        intent.putExtra("strResult", strResult);
                        mapGISFrame.startActivity(intent);

                        MyApplication.getInstance().startActivityAnimation(mapGISFrame);

                        break;

                    case 1:
                        mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
                            @Override
                            public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                                Annotation annotation = annotationview.getAnnotation();

                                if (annotation instanceof AccidentAnnotation) {
                                    AccidentAnnotation accidentAnnotation = (AccidentAnnotation) annotation;
                                    toDetailActivity(accidentAnnotation.accidentFeature.attributes.attrStrToMap(), "设备详情");
                                }
                            }

                            @Override
                            public void mapViewClickAnnotation(MapView mapview, Annotation annotation) {

                                if (!(annotation instanceof AccidentAnnotation)) {
                                    return;
                                }

                                AccidentAnnotation accidentAnnotation = (AccidentAnnotation) annotation;

                                if (!twiceFeatures.contains(accidentAnnotation.accidentFeature)) {
                                    twiceFeatures.add(accidentAnnotation.accidentFeature);
                                    annotation.setImage(BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_gcoding));
                                    mapGISFrame.showToast("添加设备成功");
                                } else {
                                    twiceFeatures.remove(accidentAnnotation.accidentFeature);
                                    annotation.setImage(accidentAnnotation.accidentFeature.bitmap);
                                    mapGISFrame.showToast("删减设备成功");
                                }

                                mapview.refresh();
                            }
                        });
                        break;

                    case 2:
                        if (twiceFeatures.size() == 0) {
                            mapGISFrame.showToast("请先选取设备,再进行此操作!");
                            return;
                        }
                        menu.startTwiceQuery(twiceFeatures);
                        break;

                    case 3:
                        mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
                            @Override
                            public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                                Annotation annotation = annotationview.getAnnotation();

                                if (annotation instanceof AccidentAnnotation) {
                                    AccidentAnnotation accidentAnnotation = (AccidentAnnotation) annotation;
                                    toDetailActivity(accidentAnnotation.accidentFeature.attributes.attrStrToMap(), "设备详情");
                                }
                            }
                        });
                        break;

                }
            }
        });

        fragment.show(mapGISFrame.getSupportFragmentManager(), "");
    }
}
