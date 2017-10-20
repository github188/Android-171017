package com.mapgis.mmt.module.gis.toolbar.online.query.spatial;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.constant.Constants;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryResult;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineSpatialQueryTask;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.query.spatial.LayerSelectActivity;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OnlineSpatialQueryMapMenu extends BaseMapMenu {
    protected String layerName;
    protected OnlineFeature[] onlineFeatures;
    protected final MmtAnnotationListener listener;
    protected int currentPage = 1;
    protected int totalRcdNum;
    protected ArrayList<OnlineFeature> mOnlinFeatureList;
    protected Rect rect;

    private OnlineLayerInfo onlineLayer;

    public OnlineSpatialQueryMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
        listener = new MmtAnnotationListener();
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }
        mOnlinFeatureList = new ArrayList<>();
        enterLayerSelectActivity();
        return true;
    }

    /**
     * 进入图层选择界面
     */
    protected void enterLayerSelectActivity() {
        Intent intent = new Intent(mapGISFrame, LayerSelectActivity.class);
        ArrayList<String> layerNames = new ArrayList<>();
        for (OnlineLayerInfo layer : MapServiceInfo.getInstance().getLayers()) {
            layerNames.add(layer.name);
        }
        intent.putStringArrayListExtra("layers", layerNames);
        mapGISFrame.startActivityForResult(intent, 0);
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.header_bar_plan_name, null);
        view.findViewById(R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGISFrame.resetMenuFunction();
            }
        });

        ((TextView) view.findViewById(R.id.tvPlanName)).setText(R.string.online_query_title);

        view.findViewById(R.id.ivPlanDetail).setVisibility(View.VISIBLE);
        view.findViewById(R.id.ivPlanDetail).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onlineFeatures == null) {
                    MyApplication.getInstance().showMessageWithHandle(mapGISFrame.getString(R.string.online_query_null));
                    return;
                }

                onlineFeatures = new OnlineFeature[mOnlinFeatureList.size()];
                mOnlinFeatureList.toArray(onlineFeatures);

                Intent intent = new Intent(mapGISFrame, OnlineSpatialQueryResultActivity.class);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_data), onlineFeatures);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_datastr), new Gson().toJson(onlineFeatures));
                intent.putExtra(mapGISFrame.getString(R.string.online_query_layername), layerName);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_clickwhichindex), listener.clickWhichIndex);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_currentpage), currentPage);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_totalrcdnum), totalRcdNum);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_rect), mapView.getDispRange());
                intent.putExtra(mapGISFrame.getString(R.string.online_query_objectids), onlineLayer.id);
                mapGISFrame.startActivityForResult(intent, 0);
                MyApplication.getInstance().startActivityAnimation(mapGISFrame);
            }
        });
        return view;
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return true;
        }

        switch (resultCode) {
            case ResultCode.RESULT_LAYER_SELECTED:
                layerName = intent.getStringExtra("layer");
                ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText(layerName + "查询");
                onlineLayer = MapServiceInfo.getInstance().getLayerByName(layerName);
                rect = mapView.getDispRange();
//                new SpatialQueryTask(mapGISFrame, rect, onlineLayer.id)
//                        .executeOnExecutor(MyApplication.executorService);

                initFirstPageData();

                break;
            case ResultCode.RESULT_PIPE_LOCATE:
            case ResultCode.RESULT_PIPE_REFREASH:
                currentPage = intent.getIntExtra("page", currentPage);
                listener.clickWhichIndex = intent.getIntExtra("clickWhichIndex", listener.clickWhichIndex);

                if (intent.hasExtra("dataStr")) {
                    mOnlinFeatureList.clear();
                    List<OnlineFeature> tempList = new Gson().fromJson(intent.getStringExtra("dataStr")
                            , new TypeToken<List<OnlineFeature>>() {
                            }.getType());
                    mOnlinFeatureList.addAll(tempList);
                }

                showOnMap();
                break;
        }
        return true;
    }

    protected void showOnMap() {
        try {
            if (mOnlinFeatureList == null || mOnlinFeatureList.size() == 0) {
                return;
            }

            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();

            int startIndex = (currentPage - 1) * Constants.PAGE_ITEM_NUMBER;
            int endIndex = startIndex + Constants.PAGE_ITEM_NUMBER;

            if (endIndex > mOnlinFeatureList.size()) {
                endIndex = mOnlinFeatureList.size();
            }

            for (int i = startIndex; i < endIndex; i++) {
                showFeatureOnMap(mOnlinFeatureList.get(i), i % Constants.PAGE_ITEM_NUMBER);
            }

            mapView.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将一个在线属性对象数组绘制到地图上，
     *
     * @param featureArray 属性数组
     */
    protected void showFeaturesOnMap(OnlineFeature[] featureArray) {
        if (featureArray == null) {
            return;
        }
        mapView.getAnnotationLayer().removeAllAnnotations();
        mapView.getGraphicLayer().removeAllGraphics();

        for (int i = 0; i < featureArray.length; i++) {
            showFeatureOnMap(featureArray[i], i);
        }

        mapView.refresh();
    }

    /**
     * 将一个在线属性对象集合绘制到地图上，
     *
     * @param featureList 属性集合
     */
    protected void showFeaturesOnMap(List<OnlineFeature> featureList) {
        if (featureList == null) {
            return;
        }

        for (int i = 0; i < featureList.size(); i++) {
            showFeatureOnMap(featureList.get(i), i);
        }

        mapView.refresh();
    }

    /**
     * 在地图上面绘制一个标注
     *
     * @param pageFeature 在线属性对象
     * @param i           所用的图标的索引
     */
    protected void showFeatureOnMap(OnlineFeature pageFeature, int i) {
        String field = LayerConfig.getInstance().getConfigInfo(layerName).HighlightField;

        String highlight = TextUtils.isEmpty(field) ? "" : pageFeature.attributes.get(field);

        if (TextUtils.isEmpty(highlight))
            highlight = "-";

        MmtAnnotation mmtAnnotation = pageFeature.showAnnotationOnMap(mapView, highlight, String.valueOf(i),
                BitmapFactory.decodeResource(mapGISFrame.getResources(), Constants.SERIALIZE_ICONS[i]));

        if (listener.clickWhichIndex != -1 && listener.clickWhichIndex == i) {
            mmtAnnotation.setImage(BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_gcoding));
            mapView.panToCenter(mmtAnnotation.getPoint(), true);
            mmtAnnotation.showAnnotationView();
        }

        mmtAnnotation.attrMap.putAll(pageFeature.attributes);

        if (!mmtAnnotation.attrMap.containsKey("编号")) {
            mmtAnnotation.attrMap.put("编号", "");
        }
        if (!mmtAnnotation.attrMap.containsKey("$图层名称$")) {
            mmtAnnotation.attrMap.put("$图层名称$", pageFeature.layerName);
        }
        if (!mmtAnnotation.attrMap.containsKey("$geometryType$")) {
            mmtAnnotation.attrMap.put("$geometryType$", pageFeature.geometryType);
        }
    }

    /**
     * 查询第一页的数据
     */
    protected void initFirstPageData() {
        new OnlineSpatialQueryTask(mapGISFrame, rect, onlineLayer.id, "mid:1," + Constants.PAGE_ITEM_NUMBER) {

            @Override
            protected void onTaskDone(OnlineQueryResult data) {
                mapView.setAnnotationListener(listener);
                onlineFeatures = data.features;
                if (onlineFeatures != null) {
                    for (OnlineFeature onlineFeature : onlineFeatures) {
                        onlineFeature.geometryType = data.geometryType;
                        onlineFeature.layerName = layerName;
                    }
                }
                totalRcdNum = data.totalRcdNum;
                // 将查询到的当前页的数据拷贝到集合中
                Collections.addAll(mOnlinFeatureList, onlineFeatures);

                showFeaturesOnMap(onlineFeatures);
            }
        }.executeOnExecutor(MyApplication.executorService);
    }
}
