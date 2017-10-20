package com.patrolproduct.module.myplan;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MapGISFrameFragment;
import com.mapgis.mmt.module.gis.MmtMapViewRenderContextListener;
import com.mapgis.mmt.module.gis.map.pager.MmtMapBottomPager;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryResult;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineWhereQueryTask;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.query.point.OnPointClickListener;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.entity.PatrolTask;
import com.patrolproduct.module.myplan.feedback.MyPlanFeedback;
import com.patrolproduct.module.myplan.list.PlanFragment;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import static android.os.Looper.getMainLooper;

/**
 * 巡检计划地图显示功能类
 */
public class MyPlanMapMenu extends BaseMapMenu {
    private NavigationItem item;

    /**
     * 构造函数
     */
    public MyPlanMapMenu(MapGISFrame mapGISFrame, NavigationItem item) {
        super(mapGISFrame);

        this.item = item;

        View titleView = initTitleView();

        if (titleView != null) {
            this.mapGISFrame.setCustomView(titleView);
        }
    }

    /**
     * 初始化顶部的工具栏
     *
     * @return 工具栏
     */
    @Override
    public View initTitleView() {
        if (this.item == null)
            return null;

        LinearLayout topView = (LinearLayout) mapGISFrame.findViewById(R.id.baseTopView);

        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, topView, false);

        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText(item.Function.Alias);

        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        view.setBackgroundResource(AppStyle.getActionBarStyleResource());

        return view;
    }

    private OnPointClickListener pointClickListener;
    private PatrolTask selectTask;

    /**
     * 启动巡检计划显示功能
     *
     * @return 是否立即关闭侧滑栏
     */
    @Override
    public boolean onOptionsItemSelected() {
        try {
            mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.GONE);

            // 地图Frgament
            MapGISFrameFragment mapFragment = mapGISFrame.getFragment();

            // 计划Fragment
            final PlanFragment planFragment = new PlanFragment(mapGISFrame, item);

            // 给计划Fragment的ListView增加点击事件
            planFragment.setOnMyItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectTask = (PatrolTask) parent.getItemAtPosition(position);

                    start();

                    mapGISFrame.getFragment().getViewPager().setCurrentItem(position - 1);
                }
            });

            // 底部滑动模型，用来作底部滑动显示业务数据
            MmtMapBottomPager bottomPager = new MmtMapBottomPager("巡检计划", planFragment);
            bottomPager.setOnMapBottomPageSeletedListener(new MmtMapBottomPager.OnMapBottomPageSeletedListener() {
                @Override
                public void onMapBottomPageSeleted(int index) {
                    PatrolTask task = SessionManager.patrolTaskList.get(index);

                    if (task.equals(selectTask))
                        return;

                    selectTask = task;
                    start();
                }
            });

            // 将底部滑动页加入到界面上
            mapFragment.setBottomPager(bottomPager);

            pointClickListener = new OnPointClickListener(mapGISFrame, mapView, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }


    /**
     * 点击ListView的Item的时候，启动功能
     */
    private void start() {
        new MmtBaseTask<String, Integer, String>(mapGISFrame) {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/PatrolREST.svc/SetTaskRead";
                    NetUtil.executeHttpGet(url, "TaskID", selectTask.TaskID);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onSuccess(String s) {
                showTaskOnMap();
            }
        }.mmtExecute();
    }

    /**
     * 将数据显示到地图上
     */
    private void showTaskOnMap() {
        try {
            mapView.getGraphicLayer().removeGraphicByAttribute("图形显示类别", "巡检任务");
            mapView.getAnnotationLayer().removeAllAnnotations();

            // 显示地图界面
            mapGISFrame.showMainFragment(true);

            // 判断是否已经处于高亮显示状态,如果是,则跳出,维持不变,否则清空已有的巡检任务图形后重新绘制
            List<Graphic> graphicsByAttribute = mapView.getGraphicLayer().getGraphicsByAttribute("巡检任务编号", selectTask.TaskID);

            if (graphicsByAttribute != null && graphicsByAttribute.size() > 0) {
                return;
            }

            new MmtBaseTask<String, Integer, Rect>(mapGISFrame) {
                @Override
                protected Rect doInBackground(String... params) {
                    try {
                        // 将数据绘制到地图上
                        return selectTask.draw(mapView);
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        return null;
                    }
                }

                @Override
                protected void onSuccess(final Rect rect) {
                    super.onSuccess(rect);

                    if (mapView.getAnnotationLayer().getAnnotationCount() > 0) {
                        mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
                            @Override
                            public void mapViewClickAnnotationView(MapView arg0, AnnotationView arg1) {
                                clickAnnotationView(arg1.getAnnotation());
                            }
                        });

                        mapView.setTapListener(new MapView.MapViewTapListener() {
                            @Override
                            public void mapViewTap(PointF arg0) {
                                onMapViewTap(arg0);
                            }
                        });
                    }

                    // 跳转到制定外接矩形,并保留100米的空间间隙
                    if (rect != null) {
                        if (MmtMapViewRenderContextListener.hasOpened) {
                            mapView.zoomToRange(new Rect(rect.xMin - 100, rect.yMin - 100, rect.xMax + 100, rect.yMax + 100), true);
                        } else {
                            mapView.setRenderContextListener(new MapView.MapViewRenderContextListener() {
                                @Override
                                public void mapViewRenderContextDestroyed() {
                                }

                                @Override
                                public void mapViewRenderContextCreated() {
                                    mapView.zoomToRange(new Rect(rect.xMin - 100, rect.yMin - 100, rect.xMax + 100, rect.yMax + 100), true);

                                    mapView.setRenderContextListener(null);
                                    MmtMapViewRenderContextListener.hasOpened = true;
                                }
                            });
                        }
                    }

                    mapView.refresh();
                }
            }.mmtExecute();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mapView.refresh();
        }
    }

    /**
     * 点击TIP时触发事件
     */
    private void clickAnnotationView(Annotation annotation) {
        try {
            if (!(annotation instanceof MmtAnnotation)) {
                return;
            }

            MmtAnnotation mmtAnnotation = (MmtAnnotation) annotation;

            if (mmtAnnotation.Type == MmtAnnotation.POINT_QUERY) {
                AnnotationView annotationView = new AnnotationView(annotation, mapGISFrame);

                new MmtAnnotationListener().mapViewClickAnnotationView(mapView, annotationView);

                return;
            }

            if (mmtAnnotation.Type != MmtAnnotation.MY_PLAN) {
                return;
            }

            final PatrolDevice device = new PatrolDevice();

            device.TaskId = Integer.valueOf(mmtAnnotation.attrMap.get("TaskId"));
            device.LayerName = mmtAnnotation.attrMap.get("LayerName");
            device.PipeNo = mmtAnnotation.attrMap.get("PipeNo");
            device.FlowId = Integer.valueOf(mmtAnnotation.attrMap.get("FlowId"));
            device.IsArrived = Boolean.valueOf(mmtAnnotation.attrMap.get("IsArrived"));
            device.IsFeedbacked = Boolean.valueOf(mmtAnnotation.attrMap.get("IsFeedbacked"));

            device.X = Double.valueOf(mmtAnnotation.attrMap.get("X"));
            device.Y = Double.valueOf(mmtAnnotation.attrMap.get("Y"));

            for (Hashtable<String, String> kv : SessionManager.taskStateTable) {
                if (device.TaskId == Integer.valueOf(kv.get("taskId")) && device.PipeNo.equals(kv.get("pipeId"))
                        && device.LayerName.equals(kv.get("layerName"))) {
                    device.IsArrived = kv.get("isArrive").equals("1");
                    device.IsFeedbacked = kv.get("isFeedback").equals("1");

                    break;
                }
            }

            //关键点巡检直接跳转到反馈界面；非关键点巡检（传统意义上的GIS设备巡检），先查询设备，跳转到显示设备详情界面
            if (BaseClassUtil.isNullOrEmptyString(device.LayerName) || device.LayerName.equals("拐点") || device.LayerName.equals("关键点")) {
                if (device.IsArrived || device.IsFeedbacked) {
                    Intent intent = new Intent(mapGISFrame, MyPlanFeedback.class);

                    intent.putExtra("device", device);
                    intent.putExtra("planName", selectTask.PlanInfo.PlanName);
                    intent.putExtra("PlanTypeID", selectTask.PlanInfo.PlanTypeID);

                    mapGISFrame.startActivity(intent);
                } else {
                    Toast.makeText(mapGISFrame, "该关键点尚未到位，请到位后反馈", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (mapView == null || mapView.getMap() == null || MobileConfig.MapConfigInstance == null) {
                    Toast.makeText(mapGISFrame, "地图不可用", Toast.LENGTH_SHORT).show();

                    return;
                }

                Boolean isMapOnline = MobileConfig.MapConfigInstance.IsVectorQueryOnline;

                if (isMapOnline) {
                    String where = "编号 like '" + device.PipeNo + "'";

                    new OnlineWhereQueryTask(mapGISFrame, device.LayerName, where) {
                        @Override
                        protected void onTaskDone(OnlineQueryResult queryResult) {
                            LinkedHashMap<String, String> attributes;

                            if (queryResult != null && queryResult.features != null && queryResult.features.length > 0) {
                                OnlineFeature feature = queryResult.features[0];

                                device.X = feature.geometry.x;
                                device.Y = feature.geometry.y;

                                attributes = feature.attributes;
                            } else {
                                attributes = new LinkedHashMap<>();

                                attributes.put("图层", device.LayerName);
                                attributes.put("编号", device.PipeNo);
                            }

                            showDeviceDetail(device, attributes);
                        }
                    }.executeOnExecutor(MyApplication.executorService);
                } else {
                    offlinePipeQuery(device);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 点击地图的响应事件，用于反馈点太密集时候的列表选择
     */
    private void onMapViewTap(PointF arg0) {
        try {
            Dot mp = mapView.viewPointToMapPoint(arg0);
            double radiu = mapView.getResolution(mapView.getZoom()) * 20;// 20像素半径

            final List<Annotation> annotations = new ArrayList<>();
            List<String> items = new ArrayList<>();

            for (int i = 0; i <= mapView.getAnnotationLayer().getAnnotationCount() - 1; i++) {
                MmtAnnotation annotation = (MmtAnnotation) mapView.getAnnotationLayer().getAnnotation(i);

                if (annotation == null) {
                    continue;
                }

                Dot dot = annotation.getPoint();

                double distance = Math.sqrt(Math.pow(dot.x - mp.x, 2) + Math.pow(dot.y - mp.y, 2));

                if (distance <= radiu) {
                    annotations.add(annotation);

                    String layerName = annotation.attrMap.get("LayerName");
                    String pipeNo = annotation.attrMap.get("PipeNo");
                    String state = "未到位";

                    for (Hashtable<String, String> kv : SessionManager.taskStateTable) {
                        if (annotation.attrMap.get("TaskId").equals(kv.get("taskId")) && pipeNo.equals(kv.get("pipeId"))
                                && layerName.equals(kv.get("layerName"))) {
                            if (kv.get("isArrive").equals("1")) {
                                state = "已到位";
                            }

                            if (kv.get("isFeedback").equals("1")) {
                                state = "已反馈";
                            }

                            break;
                        }
                    }

                    items.add(layerName + "/" + pipeNo + "/" + state);
                }
            }

            if (items.size() > 1) {
                ListDialogFragment fragment = new ListDialogFragment("巡检点列表", items);

                fragment.show(mapGISFrame.getSupportFragmentManager(), "");

                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {

                    @Override
                    public void onListItemClick(int arg2, String value) {
                        Annotation annotation = annotations.get(arg2);

                        clickAnnotationView(annotation);

                        annotation.showAnnotationView();
                    }
                });
            } else if (items.size() == 0 && MyApplication.getInstance().getConfigValue("QueryInPatrol", 0) > 0)
                pointClickListener.onClick(arg0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 采用离线地图时，对巡检计划的设备进行条件查询，以获取设备详情信息
     */
    private void offlinePipeQuery(PatrolDevice device) {
        LinkedHashMap<String, String> graphicMap = new LinkedHashMap<>();

        if (!BaseClassUtil.isNullOrEmptyString(device.LayerName) && !BaseClassUtil.isNullOrEmptyString(device.PipeNo)) {
            LayerEnum layerEnum = mapView.getMap().getLayerEnum();
            layerEnum.moveToFirst();
            MapLayer layer;

            while ((layer = layerEnum.next()) != null) {
                if (!(layer instanceof VectorLayer) || !layer.getName().equals(device.LayerName)) {
                    continue;
                }

                try {
                    FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, "编号 like '" + device.PipeNo + "'",
                            null, FeatureQuery.SPATIAL_REL_OVERLAP, true, true, "", 1);

                    if (featurePagedResult != null && featurePagedResult.getTotalFeatureCount() > 0) {
                        Graphic graphic = Convert.fromFeatureToGraphic(featurePagedResult.getPage(1).get(0));

                        Dot dot = graphic.getCenterPoint();

                        device.X = dot.x;
                        device.Y = dot.y;

                        for (int m = 0; m < graphic.getAttributeNum(); m++) {
                            graphicMap.put(graphic.getAttributeName(m), graphic.getAttributeValue(m));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                break;
            }
        }

        showDeviceDetail(device, graphicMap);
    }

    private void showDeviceDetail(PatrolDevice device, LinkedHashMap<String, String> graphicMap) {
        if (graphicMap.size() == 0) {
            graphicMap.put("图层", device.LayerName);
            graphicMap.put("编号", device.PipeNo);
        }

        graphicMap.put("planName", selectTask.PlanInfo.PlanName);
        graphicMap.put("$反馈状态$", "未到位");

        if (device.IsArrived) {
            graphicMap.put("$反馈状态$", "已到位");
        }

        if (device.IsFeedbacked) {
            graphicMap.put("$反馈状态$", "已反馈");
        }

        Intent intent = new Intent(mapGISFrame, PipeDetailActivity.class);

        graphicMap.put("PlanTypeID", selectTask.PlanInfo.PlanTypeID);

        intent.putExtra("graphicMap", graphicMap);
        intent.putExtra("device", device);
        intent.putExtra("fromPlan", true);

        mapGISFrame.startActivity(intent);
    }

    /**
     * 退出巡检计划显示功能
     */
    @Override
    public boolean onBackPressed() {
        if (!mapGISFrame.isOtherFragmentVisible()) {//地图界面则返回列表界面；列表界面则退出此功能
            mapGISFrame.showMainFragment(false);
        } else {
            pointClickListener.onStop();

            Class<?> navigationActivityClass = ActivityClassRegistry.getInstance().getActivityClass("主界面");
            Intent intent = new Intent(mapGISFrame, navigationActivityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            mapGISFrame.startActivity(intent);
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mapGISFrame.resetMenuFunction();
                }
            }, 500);
        }

        return true;
    }
}
