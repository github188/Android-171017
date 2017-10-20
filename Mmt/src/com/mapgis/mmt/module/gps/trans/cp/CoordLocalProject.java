package com.mapgis.mmt.module.gps.trans.cp;

import com.zondy.mapgis.geometry.Dot;

//坐标转换类
// 本地坐标与CGCS2000经纬度坐标之间的相互转换
public class CoordLocalProject {
	// _controlPointList 是Array<ControlPointPair>
	public CoordLocalProject(EllipsoidType _ellipsoidType, double _middleLine, String[] _controlPointList, double _xoffset, double _yoffset) {
		ellipsoidType = _ellipsoidType;
		middleLine = _middleLine;
		controlPointList = _controlPointList;
		xoffset = _xoffset;
		yoffset = _yoffset;

		localGaussProject = new CoordGaussProject(middleLine, ellipsoidType);

		int count = controlPointList.length;

		double[] srcX = new double[count];
		double[] srcY = new double[count];
		double[] desX = new double[count];
		double[] desY = new double[count];

		// 将地方坐标系和地方参考椭球高斯投影坐标系，建立四参数转换关系
		for (int i = 0; i < count; i++) {
			ControlPointPair ptPair = ControlPointPair.toControlPointPair(controlPointList[i].split(","));

			// 对应的高斯坐标gx,gy
			Dot geoPos = new Dot(ptPair.x1, ptPair.y1);
			Dot gaussPos = localGaussProject.GaussProject(geoPos);

			srcX[i] = gaussPos.x;
			srcY[i] = gaussPos.y;
			desX[i] = ptPair.x2;
			desY[i] = ptPair.y2;
		}

		fourParamCoordTrans = new FourParamCoordTrans(srcX, srcY, desX, desY);
	}

	public Dot GeoCoordToLocal(Dot geoPos) {
		Dot gaussPos = localGaussProject.GaussProject(geoPos);// /(lat, lon, out
																// gy, out gx);

		gaussPos.x = gaussPos.x - xoffset;
		gaussPos.y = gaussPos.y - yoffset;

		Dot localPos = fourParamCoordTrans.TransCoord(gaussPos);
		return localPos;
	}

	public Dot LocalToGeoCoord(Dot localPos) {
		Dot gaussPos = fourParamCoordTrans.TransCoordReverse(localPos);

		gaussPos.x = gaussPos.x + xoffset;
		gaussPos.y = gaussPos.y + yoffset;

		Dot geoPos = localGaussProject.GaussProjectReverse(gaussPos);
		return geoPos;
	}

	private EllipsoidType ellipsoidType = null;
	private double middleLine = 0;
	private String[] controlPointList = null;
	private double xoffset = 0;
	private double yoffset = 0;

	private CoordGaussProject localGaussProject = null;
	private FourParamCoordTrans fourParamCoordTrans = null;

}
