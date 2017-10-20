package com.mapgis.mmt.module.gps.trans.cp;

public class Envelop {
	public Envelop(double _xmin, double _ymin, double _xmax, double _ymax) {
		xmin = _xmin;
		xmax = _xmax;
		ymin = _ymin;
		ymax = _ymax;
	}

	public double width() {
		return xmax - xmin;
	}

	public double height() {
		return ymax - ymin;
	}

	public double xmin = 0;
	public double xmax = 0;
	public double ymin = 0;
	public double ymax = 0;
}
