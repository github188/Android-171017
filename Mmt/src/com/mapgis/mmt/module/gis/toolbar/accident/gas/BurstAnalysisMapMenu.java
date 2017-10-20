package com.mapgis.mmt.module.gis.toolbar.accident.gas;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.fragment.SplitListViewFragment;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentGeometry;
import com.mapgis.mmt.module.gis.toolbar.accident.gas.entity.AccidentAreaForGas;
import com.mapgis.mmt.module.gis.toolbar.accident.gas.entity.DeviceInfo;
import com.mapgis.mmt.module.gis.toolbar.accident.gas.task.QueryBurstTask;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicType;
import com.zondy.mapgis.android.mapview.MapView.MapViewTapListener;
import com.zondy.mapgis.geometry.Dot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 燃气爆管分析功能类
 */
public class BurstAnalysisMapMenu extends BaseMapMenu implements MapViewTapListener {

    // 详情弹出框的左边显示的信息
    private List<String> fragmentLeftValues = new ArrayList<>();
    // 详情弹出框的右边显示的信息
    private List<List<String>> fragmentRightValues = new ArrayList<>();

    private static Boolean isAnalying = false;

    private SplitListViewFragment splitListViewFragment;// 详细信息的对话框

    private ProgressBar burstAnalysisProgressBar;// 加载显示的进度条
    private ImageView burstAnalysisTitleDetail;// 详细信息按钮
    private TextView burstAnalysisTitleState;// 描述信息

    private HashMap<String, JSONObject> detailHashmap = new HashMap<>();
    private BurstAnalysisAnnotationListener burstAnalysisAnnotationListener;

    private TextView mAnalysisResultView;
    private View mBottomViewBar;

    // 离线管段的Annotation对象
    private Annotation mOffLineAnnotation;

    private GraphicLayer mPolylinLayer;

    private int[] icons = {R.drawable.identify_e, R.drawable.icon_marka, R.drawable.icon_markb
            , R.drawable.icon_markc, R.drawable.icon_markd, R.drawable.icon_marke
            , R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh};

    public BurstAnalysisMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
        mapGISFrame.setCustomView(initCustomView());
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.showToast(mapGISFrame.getResources().getString(R.string.mapmenu_error));
            return false;
        }

        this.mapView.setTapListener(this);

        Toast.makeText(mapGISFrame, "在地图上点击设备点,对该设备进行爆管分析", Toast.LENGTH_SHORT).show();

        initBottomView();
        return true;
    }

    @Override
    public View initTitleView() {
        return null;
    }

    /**
     * 初始化底部布局
     */
    public void initBottomView() {
        //         获取底部布局
        LinearLayout bottomLinearLayout = new LinearLayout(mapGISFrame);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, DimenTool.dip2px(mapGISFrame, 48));
        bottomLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        bottomLinearLayout.setLayoutParams(params);
        bottomLinearLayout.setGravity(Gravity.CENTER);
        bottomLinearLayout.setBackgroundResource(R.drawable.mapview_bottombar_bg);

        // 描述信息
        mAnalysisResultView = new TextView(mapGISFrame);

        mAnalysisResultView.setText("爆管分析结果");
        mAnalysisResultView.setTextSize(16);
        mAnalysisResultView.setEllipsize(TextUtils.TruncateAt.END);
        mAnalysisResultView.setTextColor(Color.BLACK);
        mAnalysisResultView.setGravity(Gravity.CENTER);
        bottomLinearLayout.addView(mAnalysisResultView);

        bottomLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                burstAnalysisTitleDetail.performClick();
            }
        });

        this.mBottomViewBar = initBottomBarView(bottomLinearLayout);
        mBottomViewBar.setVisibility(View.INVISIBLE);
    }

//    @Override
//    public View initBottomBarView(View viewBar) {
//        this.viewBar = viewBar;
//
//        mapGISFrame.findViewById(com.mapgis.mmt.R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);
//
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
//        int margin = DimenTool.dip2px(mapGISFrame, 8);
//        params.bottomMargin = margin;
//        params.leftMargin = margin;
//        params.rightMargin = margin;
//        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        viewBar.setLayoutParams(params);
//        viewBar.setBackgroundResource(R.drawable.mapview_bottombar_bg);
//
//        mapView.addView(viewBar);
//
//        viewBar.setVisibility(View.INVISIBLE);
//
//        return viewBar;
//    }

    @Override
    public void mapViewTap(PointF arg0) {
        if (burstAnalysisAnnotationListener != null && burstAnalysisAnnotationListener.getHideTapListener()) {
            burstAnalysisAnnotationListener.setHideTapListener(false);
            return;
        }
        if (isAnalying == true) {
            MyApplication.getInstance().showMessageWithHandle("正在进行爆管分析，请稍后");
            return;
        }

        Dot dot = mapView.viewPointToMapPoint(arg0);

        mapView.getAnnotationLayer().removeAllAnnotations();
        mapView.getGraphicLayer().removeAllGraphics();

        GraphicPoint current = new GraphicPoint(dot, 0);
        current.setColor(Color.RED);
        current.setSize(10.0f);
        mapView.getGraphicLayer().addGraphic(current);

        mapView.refresh();

        new QueryBurstTask(mapGISFrame, this, mHandler).execute(dot.toString(), mapView.getDispRange().toString());
        this.mapView.setTapListener(null);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Toast.makeText(mapGISFrame, "爆管点选取位置不正确,请重新选择!", Toast.LENGTH_SHORT).show();
                // 让用户重新爆管
                mapView.getGraphicLayer().removeAllGraphics();
                mapView.getAnnotationLayer().removeAllAnnotations();
                mapView.refresh();
                mapView.setTapListener(BurstAnalysisMapMenu.this);
            }
        }
    };

    /**
     * 初始化标题来信息
     */
    private View initCustomView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.burst_analysis_title_view, null);

        // 退出功能按钮
        ImageButton backButton = (ImageButton) view.findViewById(R.id.burstAnalysisTitleBack);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BurstAnalysisMapMenu.this.onBackPressed();
            }
        });

        TextView burstAnalysisTitleName = (TextView) view.findViewById(R.id.burstAnalysisTitleName);
        burstAnalysisTitleName.setText("爆管分析");

        burstAnalysisTitleState = (TextView) view.findViewById(R.id.burstAnalysisTitleState);
        burstAnalysisTitleState.setVisibility(View.GONE);

        burstAnalysisProgressBar = (ProgressBar) view.findViewById(R.id.burstAnalysisTitleBar);
        burstAnalysisTitleDetail = (ImageView) view.findViewById(R.id.burstAnalysisTitleDetail);
        burstAnalysisTitleDetail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (splitListViewFragment != null && !splitListViewFragment.isVisible()) {
                    splitListViewFragment.show(mapGISFrame.getSupportFragmentManager(), "");
                }
            }
        });

        return view;
    }

    /**
     * 分析过程中 断节，如 爆管分析服务返回异常
     */
    public void errorAnalysis() {
        burstAnalysisTitleState.setText(BaseClassUtil.listToString(fragmentLeftValues).replaceAll("\n", ""));

        burstAnalysisTitleDetail.setVisibility(View.INVISIBLE);

        mAnalysisResultView.setText(BaseClassUtil.listToString(fragmentLeftValues).replaceAll("\n", ""));

        if (mBottomViewBar != null) {
            mBottomViewBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 分析前准备
     */
    public void preAnalysis() {
        isAnalying = true;
        burstAnalysisProgressBar.setVisibility(View.VISIBLE);

    }

    public void closeAnalysis() {
        isAnalying = false;
        burstAnalysisProgressBar.setVisibility(View.GONE);
    }

    public void parseDatas(JSONObject result) {

        try {
            if (result == null) {
                return;
            }
            if (result.get("identify") == null || result.get("identify").toString().equals("null")) {
                mHandler.sendEmptyMessageDelayed(0, 500);
                return;
            }
            fragmentLeftValues.clear();
            fragmentRightValues.clear();

            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();

            Object offLineObject = null;
            Iterator it = result.keys();
            while (it.hasNext()) {
                String key = it.next().toString();
                Object valueobject = result.get(key);
                if (valueobject == null || valueobject.equals("null")) {
                    continue;
                }

                //是设备实体
                if (key.equals("area")) {
                    AccidentAreaForGas accidentAreaForGas = new Gson().fromJson(valueobject.toString(), AccidentAreaForGas.class);
                    mapView.getGraphicLayer().addGraphic(accidentAreaForGas.createPolygon());
                    continue;
                }
                // 管段设备
                if (key.equalsIgnoreCase("offline")) {
                    offLineObject = valueobject;
                    continue;
                }
                showOnMap(key, valueobject);
            }

            if (offLineObject != null) {
                showOnMap("offLine", offLineObject);
            }

            mapView.refresh();
            if (fragmentLeftValues.size() > 0) {
                burstAnalysisAnnotationListener = new BurstAnalysisAnnotationListener(mapGISFrame, detailHashmap);
                mapView.setAnnotationListener(burstAnalysisAnnotationListener);

//                // 管段的点击查询
//                mOnPointClickListener = new OnPointClickListener(mapGISFrame,mapView);
//                mapView.setTapListener(new MapViewTapListener() {
//                    @Override
//                    public void mapViewTap(PointF pointF) {
//                        mOnPointClickListener.onClick(pointF);
//                    }
//                });

                completeAnalysis();
            }
        } catch (Exception ex) {
            errorAnalysis();
        }
    }

    private void showOnMap(String key, Object valueobject) {
        try {
            JSONObject value = null;

            value = new JSONObject(valueobject.toString());

            Object totalRcdNum = value.get("totalRcdNum");

            if (totalRcdNum == null) {
                return;
            }
            // try {
            int totalRcdNumInt = Integer.parseInt(totalRcdNum.toString());
            if (totalRcdNumInt > 0) {

                JSONArray featuresArray = value.getJSONArray("features");
                if (featuresArray == null) {
                    return;
                }
                //图层名
                DeviceInfo deviceInfo = getDeviceInfoByLayer(key);
                fragmentLeftValues.add(deviceInfo.displayName + "(" + totalRcdNumInt + ")");

                //坐标
                List<String> rightvalues = new ArrayList<>();
                for (int i = 0; i < featuresArray.length(); i++) {
                    JSONObject features = new JSONObject(featuresArray.get(i).toString());
                    Object geometry = features.get("geometry");

                    //设备实体详情,用hashmap存储起来方便查找
                    Object attributes = features.get("attributes");
                    if (attributes != null) {
                        JSONObject attributesjo = new JSONObject(attributes.toString());
                        deviceInfo.OID = attributesjo.getString("OID");
//                                detailHashmap.put(deviceInfo.OID, attributesjo);
                        // 将坐标信息也存到集合中去
                        detailHashmap.put(deviceInfo.OID, features);
                    }

                    if (geometry != null) {
                        //所需的数据节点，用Gson转换更方便使用
                        AccidentGeometry accidentGeometry = new Gson().fromJson(geometry.toString(), AccidentGeometry.class);
                        Dot dot = accidentGeometry.getCenterDot();
                        //加说明
                        rightvalues.add(deviceInfo.OID);
                        //如果是线，线和点都加上
                        if (accidentGeometry.paths != null) {
//                            mPolylinLayer = new GraphicLayer();
//                            mPolylinLayer.addGraphic(accidentGeometry.createPolylin());
//                            mapView.getGraphicLayers().add(mPolylinLayer);
                            mapView.getGraphicLayer().addGraphic(accidentGeometry.createPolylin());
                            continue;
                        }
                        //加tip
                        Annotation annotation = new Annotation(deviceInfo.displayName, deviceInfo.OID,
                                dot, deviceInfo.icon);
                        mapView.getAnnotationLayer().addAnnotation(annotation);
                    }
                }
                fragmentRightValues.add(rightvalues);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分析完成
     */
    public void completeAnalysis() {
        //初始化右上角详情按钮
        initSplitListViewFragment();

        burstAnalysisTitleState.setText(BaseClassUtil.listToString(fragmentLeftValues).replaceAll("\n", ""));

        burstAnalysisTitleDetail.setVisibility(View.VISIBLE);

        mAnalysisResultView.setText(BaseClassUtil.listToString(fragmentLeftValues).replaceAll("\n", ""));

        mBottomViewBar.setVisibility(View.VISIBLE);

        // 对管线的点击事件
        mapView.setTapListener(new MapViewTapListener() {
            @Override
            public void mapViewTap(PointF pointF) {
                try {
                    GasOnClickListener gasOnClickListener = new GasOnClickListener(mapGISFrame, mapView);
                    List<Graphic> graphics = gasOnClickListener.searchGraphiPolylines(pointF);
                    if (graphics == null || graphics.size() == 0) {
                        return;
                    }

                    for (Graphic graphic : graphics) {
                        if (!graphic.getGraphicType().equals(GraphicType.PolylinType)) {
                            continue;
                        }
                        // GIS编号
                        String gisID = graphic.getAttributeValue("GIS编号");

                        Set<String> keySet = detailHashmap.keySet();
                        for (String keyOID : keySet) {
                            JSONObject features = detailHashmap.get(keyOID);
                            if (!features.toString().contains(gisID)) {
                                continue;
                            }
                            showLineMidAnnotation(keyOID);
                        }

                        if (mOffLineAnnotation != null){
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initSplitListViewFragment() {
        splitListViewFragment = new SplitListViewFragment("爆管分析结果", "图层", "编号", fragmentLeftValues, fragmentRightValues);
        splitListViewFragment.setRightListItemSingleLine(true);
        splitListViewFragment.setCancelable(false);
        splitListViewFragment.setLeftLayoutWeight(4);
        splitListViewFragment.setRightLayoutWeight(6);
        splitListViewFragment.setSplitListViewPositiveClick(new SplitListViewFragment.SplitListViewPositiveClick() {
            @Override
            public void onSplitListViewPositiveClick(String leftListValue, String rightListValue, int leftPos, int rightPos) {
                try {
                    // 点击管段
                    if (leftListValue.contains("停气管线")) {
                        showLineMidAnnotation(rightListValue);
                        return;
                    }

                    List<Annotation> annotationList = mapView.getAnnotationLayer().getAllAnnotations();
                    for (Annotation annotation : annotationList) {
                        String desc = annotation.getDescription();
                        if (rightListValue.equals(desc)) {
                            annotation.showAnnotationView();
                            mapView.panToCenter(annotation.getPoint(), true);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(mapGISFrame, "坐标不正确", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLineMidAnnotation(String rightListValue) throws JSONException {
        if (mOffLineAnnotation != null && mOffLineAnnotation.getDescription().equals(rightListValue)) {
            mOffLineAnnotation.showAnnotationView();
            mapView.panToCenter(mOffLineAnnotation.getPoint(), true);
            return;
        }

        if (mOffLineAnnotation != null) {
            mapView.getAnnotationLayer().removeAnnotation(mOffLineAnnotation);
        }
        //图层名
        DeviceInfo deviceInfo = getDeviceInfoByLayer("offLine");
        JSONObject features = detailHashmap.get(rightListValue);
        Object geometry = features.get("geometry");

        //所需的数据节点，用Gson转换更方便使用
        AccidentGeometry accidentGeometry = new Gson().fromJson(geometry.toString(), AccidentGeometry.class);
        Dot dot = accidentGeometry.getCenterDot();

        mOffLineAnnotation = new Annotation(deviceInfo.displayName, rightListValue,
                dot, deviceInfo.icon);
        mapView.getAnnotationLayer().addAnnotation(mOffLineAnnotation);
        mOffLineAnnotation.showAnnotationView();
        mapView.panToCenter(mOffLineAnnotation.getPoint(), true);

        // TODO:将选中的管段用别的颜色标记出来
        mapView.refresh();
    }

    private int icoNumber = -1;
    private String lastLayerName;

    //设备实体用汉字表示
    //重点标注爆管点，阀门，调压站
    public DeviceInfo getDeviceInfoByLayer(String english) {
        DeviceInfo di = new DeviceInfo();
        di.displayName = "未知设备";
        di.layerName = english;

        if (lastLayerName == null || !lastLayerName.equals(english)) {
            icoNumber++;
            lastLayerName = english;
        }

        if (BaseClassUtil.isNullOrEmptyString(english)) {
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), icons[icoNumber]);
            return di;
        }
        if (english.equals("identify")) {
            di.displayName = "爆管点";
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), icons[icoNumber]);
            di.hasTextTip = true;
            di.colorid = mapView.getContext().getResources().getColor(R.color.red);
            return di;
        }
        if (english.equals("valve")) {
            di.displayName = "需关阀门";
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), icons[icoNumber]);
            di.hasTextTip = true;
            di.colorid = mapView.getContext().getResources().getColor(R.color.red);
            return di;
        }
        if (english.equals("auxValve")) {
            di.displayName = "辅助需关阀门";
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), icons[icoNumber]);
            di.hasTextTip = true;
            di.colorid = mapView.getContext().getResources().getColor(R.color.red);
            return di;
        }
        if (english.equals("station")) {
            di.displayName = "需关调压站";
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), icons[icoNumber]);
            di.hasTextTip = true;
            di.colorid = mapView.getContext().getResources().getColor(R.color.progressbar_orange);
            return di;
        }
        if (english.equals("auxStation")) {
            di.displayName = "辅助需关调压站";
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), icons[icoNumber]);
            di.hasTextTip = true;
            di.colorid = mapView.getContext().getResources().getColor(R.color.progressbar_orange);

            return di;
        }
        if (english.equals("offStation")) {
            di.displayName = "停气调压站";
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), icons[icoNumber]);
            di.hasTextTip = true;
            return di;
        }
        if (english.equals("offUser")) {
            di.displayName = "停气立管";
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), icons[icoNumber]);
            return di;
        }
        if (english.equals("offBigUser")) {
            di.displayName = "停气工商户";
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), icons[icoNumber]);
            return di;
        }
        if (english.equals("offLine")) {
            di.displayName = "停气管线";
            di.icon = BitmapFactory.decodeResource(mapView.getContext().getResources(), R.drawable.icon_gcoding);
            return di;
        }
        return di;
    }
}
