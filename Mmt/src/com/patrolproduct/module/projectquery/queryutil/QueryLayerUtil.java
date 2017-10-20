package com.patrolproduct.module.projectquery.queryutil;

import android.content.Context;
import android.os.AsyncTask;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.Feature;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.LinkedHashMap;

/**
 * Created by KANG on 2016/9/6.
 */
public class QueryLayerUtil {
    private Context context;
    private MapGISFrame mapGISFrame;
    private MapView mapView;
    private FeaturePagedResult featurePagedResult;

    /**
     * 每页的的个数
     */
    public final static int PAGED_COUNT = 10;

    public QueryLayerUtil(Context context, MapGISFrame mapGISFrame) {
        this.context = context;
        this.mapGISFrame = mapGISFrame;
        this.mapView = mapGISFrame.getMapView();
    }

    /**
     * 根据图层名称获取对象的VectorLayer对象
     *
     * @param name 图层名称
     * @return 图层对象
     */
    public MapLayer findLayerByName(String name) {
        MapLayer layer = null;
        LayerEnum layerEnum = mapView.getMap().getLayerEnum();
        layerEnum.moveToFirst();
        while ((layer = layerEnum.next()) != null) {

            if (layer.getName().equals(name)) {
                break;
            }
        }
        return layer;
    }


    /*
     * 获取地图的矩形范围
     */
    public Rect getQueryMapRect() {
        Rect mapRect = mapView.getMap().getRange();
        Rect dispRect = mapView.getDispRange();
        //取mapRect和dispRect两者的交集，null默认查全部
        rect = GisUtil.getMixRect(mapRect, dispRect);
        if (GisUtil.IsInEnvelope(mapRect, rect)) {
            rect = null;
        }
        return rect;
    }

    private MapLayer layer;
    private Rect rect;

    /**
     * 异步方式查询指定图层数据
     *
     * @param layerName 图层名称
     * @param key       查询条件
     * @param callBack  用来处理数据的回调方法
     */
    public void asyncQueryLayerData(final String layerName, final String key, final QueryLayerCallBack callBack) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected void onPreExecute() {
                layer = findLayerByName(layerName);
                rect = getQueryMapRect();
            }

            @Override
            protected String doInBackground(String... params) {
                try {
//                    Thread.sleep(1000);
                    String where = BaseClassUtil.isNullOrEmptyString(key) ? "" : "工程名称 LIKE '%" + key + "%'";

                    featurePagedResult = FeatureQuery.query((VectorLayer) layer, where,null
                            /*rect == null ? null : new FeatureQuery.QueryBound(rect)*/, FeatureQuery.SPATIAL_REL_OVERLAP, true,
                            true, "", PAGED_COUNT);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
//                    if (featurePagedResult.getTotalFeatureCount() > 0) {
//                        // pageCount = featurePagedResult.getPageCount();
//                        showPageResultOnMap();
//                    } else {
//                        Toast.makeText(mapGISFrame, "当前查询条件下无查询结果返回", Toast.LENGTH_SHORT).show();
//                    }
                    callBack.onHandData(featurePagedResult);
                } catch (Exception e) {
                    e.printStackTrace();
                } //finally {
//                    progressDialog.dismiss();
//                }
            }
        }.execute("");
    }

    public interface QueryLayerCallBack {
        void onHandData(FeaturePagedResult featurePagedResult);
    }

    /**
     * 将一页数据转化为集合数据
     *
     * @return
     */
    public static LinkedHashMap<String, String> featureToHashMap(Feature feature) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        if (feature == null) {
            return null;
        }
        Graphic graphic = feature.toGraphics(true).get(0);
        for (int m = 0; m < graphic.getAttributeNum(); m++) {
            map.put(graphic.getAttributeName(m), graphic.getAttributeValue(m));
        }
        return map;
    }
}
