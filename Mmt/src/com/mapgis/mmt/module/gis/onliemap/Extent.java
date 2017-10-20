package com.mapgis.mmt.module.gis.onliemap;

import com.zondy.mapgis.geometry.Rect;

public class Extent {
	public SpatialReference spatialReference;

	public double xmin;
	public double ymin;
	public double xmax;
	public double ymax;

	public Rect getRect() {
		return new Rect(xmin, ymin, xmax, ymax);
	}

	public Extent() {
	}

	public Extent(String ext) {
		String[] rects = ext.split(",");

		this.xmin = Double.valueOf(rects[0]);
		this.ymin = Double.valueOf(rects[1]);
		this.xmax = Double.valueOf(rects[2]);
		this.ymax = Double.valueOf(rects[3]);
	}

	public Extent(double... args) {
		this.xmin = args[0];
		this.ymin = args[1];
		this.xmax = args[2];
		this.ymax = args[3];
	}

	@Override
	public String toString() {
		return getRect().toString();
	}
}