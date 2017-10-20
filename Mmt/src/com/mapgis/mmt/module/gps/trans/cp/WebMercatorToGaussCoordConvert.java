package com.mapgis.mmt.module.gps.trans.cp;

import com.zondy.mapgis.geometry.Dot;

/*
 * web墨卡托到高斯投影坐标的直接转换
 * 在web墨卡托球上进行高斯投影
 * */
public class WebMercatorToGaussCoordConvert {
	public WebMercatorToGaussCoordConvert(double middleLine, double xoffset, double yoffset) {
		_middleLine = middleLine;
		googleGaussProject = new CoordGaussProject(middleLine, EllipsoidType.GoogleEarth);
		_xoffset = xoffset;
		_yoffset = yoffset;
	}

	/*
	 * wx，wy wPos web墨卡托 投影坐标 gx，gy gPos web球下的高斯投影坐标
	 */

	public Dot WebMercatorToGauss(Dot wPos) {
		Dot geoPos = GoogleTrans.MercatorProjectReverse(wPos);

		double deltaLon = Math.abs(geoPos.x - _middleLine);
		// 不在该投影带内
		if (deltaLon > 3) {
			return new Dot(0, 0);
		}

		Dot gPos = googleGaussProject.GaussProject(geoPos);
		gPos.x = gPos.x - _xoffset;
		gPos.y = gPos.y - _yoffset;
		return gPos;
	}

	public Dot GeoToGauss(Dot geoPos) {
		double deltaLon = Math.abs(geoPos.x - _middleLine);
		// 不在该投影带内
		if (deltaLon > 3) {
			return new Dot(0, 0);
		}

		Dot gPos = googleGaussProject.GaussProject(geoPos);
		gPos.x = gPos.x - _xoffset;
		gPos.y = gPos.y - _yoffset;
		return gPos;
	}

	public Dot GaussToGeo(Dot gPos) {
		gPos.x = gPos.x + _xoffset;
		gPos.y = gPos.y + _yoffset;

		Dot geoPos = googleGaussProject.GaussProjectReverse(gPos);
		return geoPos;
	}

	public Dot GaussToWebMercator(Dot gPos) {
		gPos.x = gPos.x + _xoffset;
		gPos.y = gPos.y + _yoffset;

		Dot geoPos = googleGaussProject.GaussProjectReverse(gPos);

		Dot wPos = GoogleTrans.MercatorProject(geoPos);
		return wPos;
	}

	private EllipsoidType ellipsoidType = null;
	// 投影处理的中央经线
	private double _middleLine = 0;

	// 谷歌椭球和地方参考椭球平面坐标位置之间的位置偏差
	private double _xoffset = 0;
	private double _yoffset = 0;

	// 谷歌投影
	private CoordGaussProject googleGaussProject = null;

}
