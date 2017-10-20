package com.mapgis.mmt.module.gps.trans.cp;

public class ControlPointPair {

	public ControlPointPair() {
	}

	public ControlPointPair(double _x1, double _y1, double _x2, double _y2) {
		x1 = _x1;
		y1 = _y1;
		x2 = _x2;
		y2 = _y2;
	}

	public static ControlPointPair toControlPointPair(String[] cpArray) {
		ControlPointPair cp = null;

		if (cpArray != null) {
			cp = new ControlPointPair();

			cp.x1 = Double.valueOf(cpArray[0]);
			cp.y1 = Double.valueOf(cpArray[1]);
			cp.x2 = Double.valueOf(cpArray[2]);
			cp.y2 = Double.valueOf(cpArray[3]);
		}

		return cp;
	}

	public double x1 = 0;
	public double y1 = 0;
	public double x2 = 0;
	public double y2 = 0;
}
