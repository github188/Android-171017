package com.repair.zhoushan.module.devicecare.platfromgislink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MapViewExtentChangeListener;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDPlaceSearchResult;
import com.mapgis.mmt.module.gis.place.FindResult;
import com.mapgis.mmt.module.gis.place.Poi;
import com.mapgis.mmt.module.gis.place.SingleSearchResult;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver.LocatorGeocodeResult;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearchResult;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.patrol.entity.KeyPoint;
import com.patrol.module.KeyPoint.PointDetailFragment;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.MapLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuyunfan on 2016/3/23.
 */
public class PlatfromGisLinkMapMenu extends BaseMapMenu {
    Context context;
    String key;
    static String layerName;
    TextView xyTextView;
    //是否是坐标
    private boolean isxy = false;

    //挂接所需变量
    String tableName;
    String taskKey;

    Dot loc;

    private String tipStr = "自动挂接失败";
    //当前的台账任务所处的图层信息
    MapLayer mapLayer;

    Bitmap bitmap = BitmapFactory.decodeResource(
            mapGISFrame.getResources(), R.drawable.icon_mark_pt);

    public PlatfromGisLinkMapMenu(MapGISFrame mapGISFrame, Context context, String key, String layerName, String tableName, String taskKey) {
        super(mapGISFrame);
        this.context = context;
        this.key = key;
        PlatfromGisLinkMapMenu.layerName = layerName;
        this.tableName = tableName;
        this.taskKey = taskKey;
        mapLayer = GisUtil.getPointQueryVectorLayer(mapView, layerName);
    }

    //点击查询
    ImageView view1;
    ImageView view2;

    public void initManualLink() {
        mapView.setMapTool(new MmtMapToolCustrom((MmtMapView) mapView, xyTextView));

        ((MmtMapView) this.mapView).setExtentChangeListener(new MapViewExtentChangeListener() {

            @Override
            public void ExtentChanging() {
                startAni();
            }

            @Override
            public void ExtentChanged() {
                endAni();
            }
        });

        int mapCenterX = (mapView.getRight() - mapView.getLeft()) / 2;
        int mapCenterY = (mapView.getBottom() - mapView.getTop()) / 2;

        view1 = new ImageView(mapGISFrame);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);

        params.setMargins(mapCenterX - DimenTool.dip2px(mapGISFrame, 5),
                mapCenterY - DimenTool.dip2px(mapGISFrame, 1), 0, 0);

        view1.setLayoutParams(params);

        view1.setImageResource(R.drawable.icon_select_shadow);

        mapView.addView(view1);

        view2 = new ImageView(mapGISFrame);

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(-2, -2);
        params2.setMargins(mapCenterX - DimenTool.dip2px(mapGISFrame, 10),
                mapCenterY - DimenTool.dip2px(mapGISFrame, 30), 0, 0);

        view2.setLayoutParams(params2);

        view2.setImageResource(R.drawable.icon_select_point);

        mapView.addView(view2);

        handler = new MyHandle(view2, viewBar, mapGISFrame);
    }

    boolean isRun = false;
    boolean start = false;
    boolean exit = false;
    Graphic graphic;
    List<MapLayer> visibleVectorLayer = new ArrayList<>();

    void notifySatrtPointQuery() {

        start = true;
        synchronized (pointQuery) {
            pointQuery.notifyAll();
        }
    }

    void endAni() {
        if (!isRun)
            return;
        if (isStartNotify(false)) {
            notifySatrtPointQuery();
        }

        if (view2 != null) {
            TranslateAnimation animation = new TranslateAnimation(0, 0, -50, 0);

            animation.setDuration(300);

            handler.obtainMessage(1, animation).sendToTarget();

            //  MyApplication.getInstance().submitExecutorService(findAddr);
        }

        isRun = false;
    }

    long zoomNum = MyApplication.getInstance().getConfigValue("MyPlanDetailLevel", 6);

    final Runnable queryNearByDevices = new Runnable() {
        @Override
        public void run() {
            try {
                MyApplication.getInstance().showMessageWithHandle("正在为您搜索附近范围内可挂接的设备");
                double temp = mapView.getResolution(mapView.getZoom()) * MyApplication.getInstance().getConfigValue("nearBy", 150);
                Rect rect = new Rect();
                Dot mapDot = mapView.getCenterPoint();
                rect.setXMin(mapDot.x - temp);
                rect.setYMin(mapDot.y - temp);
                rect.setXMax(mapDot.x + temp);
                rect.setYMax(mapDot.y + temp);
                queryByRectAndPaint(rect);
            } catch (Exception ex) {

            }
        }
    };
    final Runnable pointQuery = new Runnable() {
        @Override
        public void run() {
            try {
                while (!exit) {
                    if (!start) {
                        synchronized (this) {
                            wait();
                        }
                    }
                    for (int i = 0; i < visibleVectorLayer.size(); i++) {
                        if (!start) {
                            break;
                        }
                        synchronized (this) {
                            MapLayer mapLayer = visibleVectorLayer.get(i);

                            graphic = GisUtil.pointQuerySingle(mapView, mapView.getCenterPoint(), mapLayer);
                            if (graphic != null) {
                                layerName = mapLayer.getName();
                                handler.obtainMessage(3, graphic).sendToTarget();
                                break;
                            }
                        }
                    }
                    start = false;
                }
            } catch (Exception ex) {

            }
        }
    };

    boolean isStartNotify(boolean start) {
        if (start) {
            handler.obtainMessage(4).sendToTarget();
        }
        return visibleVectorLayer.size() > 0 && mapView.getZoom() >= zoomNum;
    }

    void notifyStopPointQuery() {
        start = false;
        synchronized (pointQuery) {
            pointQuery.notifyAll();
        }
    }

    void startAni() {
        if (isRun)
            return;
        if (isStartNotify(true)) {
            notifyStopPointQuery();
        }
        isRun = true;

        if (view2 != null) {
            TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -50);

            animation.setDuration(300);
            animation.setFillAfter(true);

            handler.obtainMessage(0, animation).sendToTarget();
        }
    }

    public void addressLoc(final String loc) {
        new MmtBaseTask<Void, Void, Object>(mapGISFrame, true, "正在为您自动挂接请稍候") {
            @Override
            protected Object doInBackground(Void... params) {
                return BDGeocoder.locFromAddressUtil(mapView, loc, 1);
            }

            @Override
            protected void onSuccess(Object result) {
                try {
                    autoLinkGisFinish(result);
                } catch (Exception e) {
                    MyApplication.getInstance().showMessageWithHandle(tipStr);
                }

            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    void painAnnotiaon2mapView(Dot dot) {
        Graphic graphic = GisUtil.pointQuerySingle(mapView, dot, mapLayer);
        if (graphic != null) {
            MmtAnnotation mmtAnnotation = new MmtAnnotation(graphic, layerName, graphic.getAttributeValue("编号"), dot, bitmap);
            mapView.getAnnotationLayer().addAnnotation(mmtAnnotation);
        }
    }

    void autoLinkGisFinish(Object result) {
        if (result != null) {
            if (result instanceof BDPlaceSearchResult) {// 百度结果
                BDPlaceSearchResult result1s = (BDPlaceSearchResult) result;
                if (result1s != null && result1s.results != null && result1s.results.size() > 0) {
                    SingleSearchResult singleSearchResult = result1s.results.get(0);
                    GpsXYZ xyz = singleSearchResult.getXyz();
                    autoLinkGisSucessStep1(new Dot(xyz.getX(), xyz.getY()));
                    for (int i = 1; i < result1s.results.size(); i++) {
                        GpsXYZ temp = singleSearchResult.getXyz();
                        painAnnotiaon2mapView(new Dot(temp.getX(), temp.getY()));
                    }
                    mapView.refresh();
                } else {
                    Toast.makeText(mapGISFrame, tipStr, Toast.LENGTH_SHORT).show();
                }

            } else if (result instanceof LocatorGeocodeResult) {// GIS服务器结果
                LocatorGeocodeResult locatorGeocodeResult = (LocatorGeocodeResult) result;
                if (locatorGeocodeResult != null && locatorGeocodeResult.candidates != null && locatorGeocodeResult.candidates.length > 0) {
                    LocatorGeocodeResult.Candidate candidate = locatorGeocodeResult.candidates[0];
                    autoLinkGisSucessStep1(candidate.toDot());
                    for (int i = 1; i < locatorGeocodeResult.candidates.length; i++) {
                        painAnnotiaon2mapView(locatorGeocodeResult.candidates[i].toDot());
                    }
                    mapView.refresh();
                } else {
                    Toast.makeText(mapGISFrame, tipStr, Toast.LENGTH_SHORT).show();
                }

            } else if (result instanceof LocalPlaceSearchResult) { // 本地db文件地名搜索结果
                LocalPlaceSearchResult localPlaceSearchResult = (LocalPlaceSearchResult) result;
                if (localPlaceSearchResult != null && localPlaceSearchResult.dataList != null && localPlaceSearchResult.dataList.size() > 0) { // 查询到结果时才重绘地图
                    LocalPlaceSearchResult.LocalPlaceSearchResultItem localPlaceSearchResultItem = localPlaceSearchResult.dataList.get(0);
                    autoLinkGisSucessStep1(new Dot(localPlaceSearchResultItem.loc_x, localPlaceSearchResultItem.loc_y));
                    for (int i = 1; i < localPlaceSearchResult.dataList.size(); i++) {
                        LocalPlaceSearchResult.LocalPlaceSearchResultItem temp = localPlaceSearchResult.dataList.get(i);
                        painAnnotiaon2mapView(new Dot(temp.loc_x, temp.loc_y));
                    }
                    mapView.refresh();
                } else {
                    Toast.makeText(mapGISFrame, tipStr, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    //挂接
    void autoLinkGisSucessStep1(Dot doc) {
        mapView.zoomTo(7, true);
        mapView.panToCenter(doc, true);
        mapView.refresh();
        endAni();
    }

    void searchNearByAbleLinkGisDevice() {
        GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();
        if (xyz == null) {
            MyApplication.getInstance().showMessageWithHandle("GPS定位失败，无法为您搜索附近的设备");
            return;
        }
        Dot cur = new Dot(xyz.getX(), xyz.getY());
        mapView.panToCenter(cur, true);
        mapView.refresh();
        MyApplication.getInstance().submitExecutorService(queryNearByDevices);
    }

    static TextView titleview;
    static ProgressBar burstAnalysisProgressBar;// 加载显示的进度条

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
        burstAnalysisProgressBar = (ProgressBar) view.findViewById(R.id.baseActionBarProgressBar);
        titleview = (TextView) view.findViewById(R.id.baseActionBarTextView);
        titleview.setText("台账挂接");

        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        return view;
    }

    @Override
    public boolean onBackPressed() {
        backActivity();
        return true;
    }

    View viewBar;

    @Override
    public boolean onOptionsItemSelected() {
        try {
            if (mapView == null || mapView.getMap() == null) {
                mapGISFrame.stopMenuFunction();
                return false;
            }
            Intent intent = new Intent(context, MapGISFrame.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);


            if (mapLayer == null) {
                Toast.makeText(mapGISFrame, "错误数据，无法挂接", Toast.LENGTH_SHORT).show();
                return false;
            }
            titleview.setText(layerName + "挂接");

            mapView.getAnnotationLayer().removeAllAnnotations();


            visibleVectorLayer.add(mapLayer);

            mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

            viewBar = mapGISFrame.getLayoutInflater().inflate(
                    R.layout.map_select_point_bar, null);

            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                    -1, -2);

            params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            viewBar.setLayoutParams(params1);
            View viewquery = viewBar.findViewById(R.id.tvOther);
            viewquery.setVisibility(View.VISIBLE);

            viewquery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    queryByRectAndPaint(mapView.getDispRange());
                }
            });

            TextView tvOk = (TextView) viewBar.findViewById(R.id.tvOk);

            tvOk.setText("挂接");
            tvOk.setTextColor(context.getResources().getColor(R.color.grey));
            tvOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (graphic == null) {
                        Toast.makeText(mapGISFrame, "未找到GIS数据无法挂接", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final String gisNO = graphic.getAttributeValue("编号");
                    if (TextUtils.isEmpty(gisNO)) {
                        Toast.makeText(mapGISFrame, "gis编号不存在无法挂接", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(taskKey)) {
                        Toast.makeText(mapGISFrame, "任务ID不存在无法挂接", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定挂接？");
                    okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                        @Override
                        public void onRightButtonClick(View view) {

                            final GisLinkInfo gisLinkInfo = new GisLinkInfo();
                            gisLinkInfo.GISNo = gisNO;
                            gisLinkInfo.GISLayerName = layerName;
                            gisLinkInfo.GISxy = mapView.getCenterPoint().toString();

                            new MmtBaseTask<Void, Void, String>(mapGISFrame) {
                                @Override
                                protected String doInBackground(Void... params) {
                                    StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                                    sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PlatfromLinkGis/")
                                            .append(tableName).append("/" + taskKey);

                                    String result = "";
                                    try {
                                        result = NetUtil.executeHttpPost(sb.toString(), new Gson().toJson(gisLinkInfo));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return result;
                                }

                                @Override
                                protected void onSuccess(String s) {
                                    ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, "挂接错误", "挂接成功");
                                    if (resultWithoutData == null) {
                                        return;
                                    }
                                    clearMap();
                                    AppManager.finishActivity(AppManager.currentActivity());
                                    mapGISFrame.backByReorder(true);
                                }
                            }.mmtExecute();
                        }
                    });
                    okCancelDialogFragment.show(mapGISFrame.getSupportFragmentManager(), "");
                }
            });

            mapView.addView(viewBar);

            xyTextView = ((TextView) viewBar.findViewById(R.id.tvAddr1));
            xyTextView.setText(mapView.getCenterPoint().toString());
            xyTextView.setTextSize(12);
            ((TextView) viewBar.findViewById(R.id.tvAddr2)).setText("在"
                    + (TextUtils.isEmpty(key) ? "地址未知" : key) + "附近");

            //手动挂接
            //点击查询
            initManualLink();

            if (!BaseClassUtil.isNullOrEmptyString(key)) {
                //自动挂接
                //目前只限于通过地名自动挂接
                //港华项目通过地名关键字自动挂接不现实(没外网地名库也不标准)
                // addressLoc(key);
            }

            MyApplication.getInstance().submitExecutorService(pointQuery);

            mapView.setAnnotationListener(new MmtAnnotationListener() {
                @Override
                public void mapViewClickAnnotationView(MapView arg0, AnnotationView arg1) {
                    try {
                        MmtAnnotation mmtAnnotation = (MmtAnnotation) arg1.getAnnotation();
                        HashMap<String, String> attr = mmtAnnotation.attrMap;
                        PointDetailFragment fragment = new PointDetailFragment();
                        Bundle args = new Bundle();
                        args.putParcelable("kp", new KeyPoint());
                        args.putSerializable("attr", attr);
                        args.putStringArray("names", GisUtil.getGISFields(layerName));
                        fragment.setArguments(args);
                        fragment.show(mapGISFrame.getSupportFragmentManager(), "");

                    } catch (Exception ex) {
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void mapViewClickAnnotation(MapView mapview, Annotation annotation) {
                    super.mapViewClickAnnotation(mapview, annotation);
                    MmtAnnotation mmtAnnotation = (MmtAnnotation) annotation;
                    graphic = mmtAnnotation.graphic;
                    chosedAnnotation = mmtAnnotation;
                    handler.obtainMessage(6, "").sendToTarget();
                }
            });

            //根据当前位置搜索附近指定范围内可挂接的设备
            searchNearByAbleLinkGisDevice();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    static boolean isquery = false;

    void queryByRectAndPaint(Rect rect) {
        if (isquery) {
            Toast.makeText(context, "正在查询，请稍候再试", Toast.LENGTH_SHORT).show();
            return;
        }
        isquery = true;
        burstAnalysisProgressBar.setVisibility(View.VISIBLE);
        titleview.setText(layerName + "  搜索中...");
        List<Graphic> graphics = GisUtil.queryAreaGraphicByMapLayer(mapView, mapView.getCenterPoint(), mapLayer, rect, 500);
        handler.obtainMessage(5, graphics).sendToTarget();
    }

    private void backActivity() {
        try {
            Intent intent = ((Activity) context).getIntent();

            intent.setClass(mapGISFrame, context.getClass());

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            mapGISFrame.startActivity(intent);
            MyApplication.getInstance().startActivityAnimation(mapGISFrame);
            clearMap();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void clearMap(){

        mapView.removeView(view1);
        mapView.removeView(view2);
        mapView.removeView(viewBar);

        exit = true;

        ((MmtMapView) this.mapView).setExtentChangeListener(null);
        mapView.setMapTool(null);
        mapGISFrame.resetMenuFunction();
    }

    MyHandle handler;

    static MmtAnnotation chosedAnnotation;
    static int[] icons = {R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd, R.drawable.icon_marke,
            R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj};

    static class MyHandle extends Handler {
        private MapGISFrame mapGISFrame;
        private View view;
        private View viewBar;
        protected String address;
        protected ArrayList<String> names;
        protected boolean isValid = false;

        protected MyHandle(View view, View viewBar, MapGISFrame mapGISFrame) {
            this.view = view;
            this.viewBar = viewBar;
            this.mapGISFrame = mapGISFrame;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 0:
                    case 1:
                        view.startAnimation((Animation) msg.obj);
                        break;
                    case 2:
                        FindResult result = (FindResult) msg.obj;

                        address = result.formatted_address;
                        isValid = result.addressComponent != null;

                        if (isValid)
                            address = result.addressComponent.district + result.addressComponent.street
                                    + result.addressComponent.street_number;

                        ((TextView) viewBar.findViewById(R.id.tvAddr1)).setText(address);

                        this.names = new ArrayList<>();

                        if (result.pois != null && result.pois.size() > 0) {
                            viewBar.findViewById(R.id.tvAddr2).setVisibility(View.VISIBLE);
                            String name = result.pois.get(0).name;

                            ((TextView) viewBar.findViewById(R.id.tvAddr2)).setText("在" + name + "附近");

                            for (Poi poi : result.pois) {
                                this.names.add(poi.name);
                            }
                        } else
                            viewBar.findViewById(R.id.tvAddr2).setVisibility(View.GONE);

                        break;
                    case 3: {
                        Graphic graphic = (Graphic) msg.obj;
                        if (graphic != null) {
                            MapView mapView = mapGISFrame.getMapView();
                            mapView.getAnnotationLayer().removeAllAnnotations();
                            chosedAnnotation = new MmtAnnotation(graphic, layerName, graphic.getAttributeValue("编号"), mapView.getCenterPoint(), null);
                            mapView.getAnnotationLayer().addAnnotation(chosedAnnotation);
                            view.setVisibility(View.GONE);
                            chosedAnnotation.showAnnotationView();
                            ((TextView) viewBar.findViewById(R.id.tvOk)).setTextColor(Color.parseColor("#ff110aff"));
                        }
                    }
                    break;
                    case 4: {
                        if (chosedAnnotation != null) {
                            mapGISFrame.getMapView().getAnnotationLayer().removeAnnotation(chosedAnnotation);
                        }
                        view.setVisibility(View.VISIBLE);
                        ((TextView) viewBar.findViewById(R.id.tvOk)).setTextColor(mapGISFrame.getResources().getColor(R.color.grey));
                    }
                    break;
                    case 5: {
                        List<Graphic> graphics = (List<Graphic>) msg.obj;
                        if (graphics != null) {
                            int resultGraphicCount = 0;
                            MapView mapView = mapGISFrame.getMapView();
                            mapView.getAnnotationLayer().removeAllAnnotations();
                            for (Graphic graphic : graphics) {
                                MmtAnnotation annotation = new MmtAnnotation(graphic, layerName, graphic.getAttributeValue("编号"), graphic.getCenterPoint(), BitmapFactory.decodeResource(
                                        mapGISFrame.getResources(), resultGraphicCount >= icons.length ? R.drawable.icon_mark_normal
                                                : icons[resultGraphicCount]));
                                mapView.getAnnotationLayer().addAnnotation(annotation);
                                resultGraphicCount++;
                            }
                            mapView.refresh();
                        }
                        burstAnalysisProgressBar.setVisibility(View.GONE);
                        titleview.setText(layerName + "挂接");
                        isquery = false;
                    }
                    break;
                    case 6: {
                        ((TextView) viewBar.findViewById(R.id.tvOk)).setTextColor(Color.parseColor("#ff110aff"));
                    }
                    break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }
}