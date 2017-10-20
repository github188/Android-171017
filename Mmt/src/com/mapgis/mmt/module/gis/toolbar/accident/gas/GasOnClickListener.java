package com.mapgis.mmt.module.gis.toolbar.accident.gas;

import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.mapgis.mmt.module.gis.toolbar.query.point.OnPointClickListener;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.graphic.GraphicType;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.GeomType;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.List;

/**
 * Created by Comclay on 2016/11/2.
 * 燃气中管段的点击处理：通过点击查询功能
 */

public class GasOnClickListener extends OnPointClickListener {
    protected List<Graphic> mGraphicList;
    public GasOnClickListener(MapGISFrame mapGISFrame, MapView mapView) {
        super(mapGISFrame, mapView);
    }

    public void onClick(PointF arg0) {
        try {
            resultGraphicCount = 0;

            pointClickLayer.removeAllGraphics();
            GisUtil.removeAnnotationByType(mapView, MmtAnnotation.POINT_QUERY);

            clickDot = mapView.viewPointToMapPoint(arg0);

            // 获取 经过 mobileconfig.json 的 PointQueryLayerFilter 值 过滤后的 可查 图层列表
            visibleVectorLayerNames = GisQueryUtil.getPointQueryVectorLayerNames(mapView);

            searchTargetGeomLayer(GeomType.GeomLin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean searchTargetGeomLayer(GeomType targetGeomType) {
        try {
            LayerEnum layerEnum = mapView.getMap().getLayerEnum();
            layerEnum.moveToFirst();

            MapLayer layer;

            while ((layer = layerEnum.next()) != null) {
                if (!visibleVectorLayerNames.contains(layer.getName()) || !layer.GetGeometryType().equals(targetGeomType))
                    continue;

                if (mapView.getZoom() <= 8 && resultGraphicCount > 0)//前八级地图默认只查询一层数据即可，之后再遍历每个图层
                    continue;

                Rect rect = new Rect();// 20像素的缓冲半径的点击查询模拟

                rect.setXMin(clickDot.x - mapView.getResolution(mapView.getZoom()) * 10);
                rect.setYMin(clickDot.y - mapView.getResolution(mapView.getZoom()) * 10);
                rect.setXMax(clickDot.x + mapView.getResolution(mapView.getZoom()) * 10);
                rect.setYMax(clickDot.y + mapView.getResolution(mapView.getZoom()) * 10);


                // 存储要素查询结果
                FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, "", new FeatureQuery.QueryBound(rect),
                        FeatureQuery.SPATIAL_REL_MBROVERLAP, true, true, "", mapView.getZoom() <= 8 ? 1 : 8);

                if (featurePagedResult.getTotalFeatureCount() > 0) {
                    mGraphicList = Convert.fromFeaturesToGraphics(featurePagedResult.getPage(1), layer.getName());
                }
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * 点击查询管段
     * @return
     */
    public List<Graphic> searchGraphiPolylines(PointF arg0){
        this.onClick(arg0);

        return mGraphicList;
    }
}
