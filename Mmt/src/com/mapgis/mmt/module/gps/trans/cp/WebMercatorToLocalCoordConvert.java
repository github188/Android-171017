package com.mapgis.mmt.module.gps.trans.cp;

import com.zondy.mapgis.geometry.Dot;

/* 我们的管网数据都是平面坐标系，以米为单位
 * 
 * 将谷歌影像投影为地方坐标系的转换处理方法，这里的处理方式也许不符合传统的教科书教授的坐标转换处理方法，但是要以实际为主，毕竟达到目的就可以了，
 * 注意以下几点：
 * 1、地方坐标系中的坐标一般与某个参考椭球下的经纬度坐标之间有对应的关系，也就是地方的坐标的控制点信息，这个信息是坐标处理转换的关键
 * 2、谷歌瓦片采用的是web墨卡托投影进行裁剪的，使用的是一个地球半径和wgs84以及CGCS2000相同的值，
 * 3、同一经纬度在web墨卡托和CGCS2000中计算出来的实际空间位置距离大约平面上相差dx=515，dy=-50，在其它的椭球上也应该类似，只是平面差值不同
 * 
 * 这里的转换，实际是上采用了这么一个思路；
 * 1、首先需要提供地方坐标系的控制点信息，建立地方坐标系到某一椭球参考坐标系的平面四参数的转换关系
 * 2、提供web墨卡托参考椭球高斯平面坐标到控制点平面参考坐标系（高斯）的平移参数
 * 3、将谷歌瓦片转换为地方坐标系时，其处理的逻辑是，
 *      谷歌瓦片（web墨卡托投影）->谷歌瓦片（高斯投影）->地方参考椭球瓦片（高斯投影）->地方坐标系瓦片（平面坐标）
 * */

public class WebMercatorToLocalCoordConvert {
	/*
	 * _listPtPair 提供经纬度和用户自定义坐标的对应点 坐标对的第一个点对应经纬度，第二个点对应自定义坐标，单位是m
	 * gaussMiddleLine 高斯投影的中央经线
	 */
	// _controlPointList 是Array<ControlPointPair>
	public WebMercatorToLocalCoordConvert(EllipsoidType _ellipsoidType, double _middleLine, String[] _controlPointList, double _xoffset,
			double _yoffset) {
		ellipsoidType = _ellipsoidType;
		middleLine = _middleLine;
		controlPointList = _controlPointList;
		xoffset = _xoffset;
		yoffset = _yoffset;

		googleGaussProject = new CoordGaussProject(middleLine, EllipsoidType.GoogleEarth);
		localGaussProject = new CoordGaussProject(middleLine, ellipsoidType);

		int count = controlPointList.length;

		double[] srcX = new double[count];
		double[] srcY = new double[count];
		double[] desX = new double[count];
		double[] desY = new double[count];

		// 将地方坐标系和地方参考椭球高斯投影坐标系，建立四参数转换关系
		for (int i = 0; i < count; i++) {
			// var ptPair:ControlPointPair = controlPointList[i] as
			// ControlPointPair;
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

	// 将web莫卡托转为用户坐标
	/*
	 * wx,wy web墨卡托投影坐标 lx,ly 转换结果的本地坐标
	 */
	public Dot WebMercatorToLocal(Dot wPos) {

		// 先计算web墨卡托投影对应的经纬度坐标

		Dot geoPos = GoogleTrans.MercatorProjectReverse(wPos);

		// 计算经纬度坐标对应的地方坐标系坐标
		Dot lPos = GeoCoordToLocal(geoPos);

		return lPos;
	}

	// 将用户坐标转为web莫卡托坐标
	public Dot LocalToWebMercator(Dot lPos) {
		Dot geoPos = LocalToGeoCoord(lPos);

		Dot wPos = GoogleTrans.MercatorProject(geoPos);

		return wPos;
	}

	public Dot GeoCoordToLocal(Dot geoPos) {
		Dot gPos = localGaussProject.GaussProject(geoPos);

		gPos.x = gPos.x - xoffset;
		gPos.y = gPos.y - yoffset;

		Dot lPos = fourParamCoordTrans.TransCoord(gPos);

		return lPos;
	}

	public Dot LocalToGeoCoord(Dot lPos) {
		Dot gPos = fourParamCoordTrans.TransCoordReverse(lPos);

		gPos.x = gPos.x + xoffset;
		gPos.y = gPos.y + yoffset;

		Dot geoPos = localGaussProject.GaussProjectReverse(gPos);
		return geoPos;
	}

	private EllipsoidType ellipsoidType = null;

	// 投影处理的中央经线
	private double middleLine = 0;
	private String[] controlPointList = null;
	// 谷歌椭球和地方参考椭球平面坐标位置之间的位置偏差
	private double xoffset = 0;
	private double yoffset = 0;

	// 地方椭球投影
	private CoordGaussProject localGaussProject = null;
	// 谷歌投影
	private CoordGaussProject googleGaussProject = null;
	// 四参数坐标转换类 ，由于四参数转换的性质，我们只能通过高斯投影坐标来对其进行用户自定义坐标的转换
	private FourParamCoordTrans fourParamCoordTrans = null;
}