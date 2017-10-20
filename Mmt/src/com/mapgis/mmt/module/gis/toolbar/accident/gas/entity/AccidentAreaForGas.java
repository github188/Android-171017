package com.mapgis.mmt.module.gis.toolbar.accident.gas.entity;

import android.graphics.Color;

import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.geometry.Dot;

/**
 * Created by liuyunfan on 2015/11/2.
 */
public class AccidentAreaForGas {
    public SpatialReference spatialReference;
    public double[][][] rings;

    /**
     * 根据数组信息创建面积
     *
     * @return GraphicPolygon
     */
    public GraphicPolygon createPolygon() {
        GraphicPolygon polygon = new GraphicPolygon();
        polygon.setColor(Color.argb(100, 0, 255, 0));
        polygon.setAttributeValue("area", "NoPoints");

        if (rings != null) {
            for (double[][] position : rings) {
                for (double[] pos : position) {
                    Dot dot = new Dot(pos[0], pos[1]);
                    polygon.appendPoint(dot);
                }
            }
        }

        return polygon;
    }
}