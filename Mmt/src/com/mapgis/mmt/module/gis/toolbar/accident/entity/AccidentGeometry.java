package com.mapgis.mmt.module.gis.toolbar.accident.entity;

import android.graphics.Color;

import com.google.gson.annotations.Expose;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.geometry.Dot;

public class AccidentGeometry {
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double[][][] paths;

	/** 获取该图形信息的中间点，若是点设备则直接返回坐标，若是线段则返回计算过后的中间点 */
	public Dot getCenterDot() {
		Dot dot = null;

		if (x != 0 && y != 0) {
			dot = new Dot(x, y);
		}

		// 有返回数据且返回数据是有效的
		if (paths != null && paths.length > 0) {

			double[][] positions = paths[0];

			// 返回的线段点个数大于1个
			if (positions.length > 1) {

				if (positions.length % 2 == 0) {// 如果是偶数个点，则取最中间两个点的中间点
					int index = positions.length / 2;

					double[] startPoint = positions[index - 1];
					double[] endPoint = positions[index];

					dot = new Dot((startPoint[0] + endPoint[0]) / 2, (startPoint[1] + endPoint[1]) / 2);

				} else {// 如果是偶数个点，则取中间点
					int index = positions.length / 2;
					dot = new Dot(positions[index][0], positions[index][1]);
				}
			}
		}

		return dot;
	}

	/**
	 * 根据数组信息创建线性路径
	 * 
	 * @return GraphicPolylin
	 */
	public GraphicPolylin createPolylin() {
		GraphicPolylin polylin = new GraphicPolylin();
		polylin.setColor(Color.parseColor("#f87f39"));
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
