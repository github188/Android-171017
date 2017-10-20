package com.mapgis.mmt.module.gis.toolbar.accident.gas.entity;

import android.graphics.Color;

import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentGeometry;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.geometry.Dot;

/**
 * Created by liuyunfan on 2015/11/16.
 */
public class AccidentGeometryFroGas extends AccidentGeometry {

    /**
     * 根据数组信息创建线性路径
     *
     * @return GraphicPolylin
     */
    public GraphicPolylin createPolylin() {
        GraphicPolylin polylin = new GraphicPolylin();
        polylin.setColor(Color.parseColor("red"));
        polylin.setLineWidth(8f);

        if (paths != null) {
            for (double[][] position : paths) {
                for (double[] pos : position) {
                    Dot dot = new Dot(pos[0], pos[1]);
                    polylin.appendPoint(dot);
                }
            }
        }

        return polylin;
    }
}
