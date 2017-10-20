package com.mapgis.mmt.module.gps.trans.cp;

import com.zondy.mapgis.geometry.Dot;

public class CoordGaussProject {

	public CoordGaussProject(double middleLine, EllipsoidType ellipsiodType) {
		// 中央经线
		_middleLine = middleLine;

		// 椭球参数
		_ep = new EllipsoidParameters(ellipsiodType);
	}

	// 高斯正投影
	/*
	 * 函数功能：输入地理坐标，根据高斯投影算法，计算该点的平面投影坐标 输入参数：geoPos 地理坐标值 Point.x
	 * 存储经度值，Point.y存储纬度值,输入的以度为单位的坐标 函数返回：高斯平面投影坐标值
	 */

	// 高斯投影
	public Dot GaussProject(Dot geoPos)
	// void GaussProject( double B, double L,out double x, out double y)
	{
		// int ProjNo = 0; int ZoneWide; ////带宽
		double longitude1, latitude1, longitude0, latitude0, xval, yval;
		double a, f, e2, ee, NN, T, C, A, M, iPI;
		iPI = 0.0174532925199433; // //3.1415926535898/180.0;

		a = _ep.a;
		f = _ep.f; // WGS84

		longitude0 = _middleLine * iPI;
		latitude0 = 0;
		longitude1 = geoPos.x * iPI;
		latitude1 = geoPos.y * iPI;
		e2 = 2 * f - f * f;
		ee = e2 * (1.0 - e2);
		NN = a / Math.sqrt(1.0 - e2 * Math.sin(latitude1) * Math.sin(latitude1));
		T = Math.tan(latitude1) * Math.tan(latitude1);
		C = ee * Math.cos(latitude1) * Math.cos(latitude1);
		A = (longitude1 - longitude0) * Math.cos(latitude1);

		M = a
				* ((1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256) * latitude1
						- (3 * e2 / 8 + 3 * e2 * e2 / 32 + 45 * e2 * e2 * e2 / 1024) * Math.sin(2 * latitude1)
						+ (15 * e2 * e2 / 256 + 45 * e2 * e2 * e2 / 1024) * Math.sin(4 * latitude1) - (35 * e2 * e2 * e2 / 3072)
						* Math.sin(6 * latitude1));
		xval = NN * (A + (1 - T + C) * A * A * A / 6 + (5 - 18 * T + T * T + 72 * C - 58 * ee) * A * A * A * A * A / 120);
		yval = M
				+ NN
				* Math.tan(latitude1)
				* (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24 + (61 - 58 * T + T * T + 600 * C - 330 * ee) * A * A * A
						* A * A * A / 720);

		xval = xval + 500000;

		return new Dot(xval, yval);
	}

	// 高斯逆投影
	/*
	 * 函数功能：输入高斯平面坐标，根据高斯逆投影算法，计算该点的经纬度坐标 输入参数：gaussPos 高斯平面坐标值 Point.x
	 * 存储横坐标，Point.y存储纵坐标,输入的以米为单位的坐标 函数返回：高斯经纬度坐标值，返回结果以度为单位
	 */
	// 反向高斯投影
	public Dot GaussProjectReverse(Dot gaussPos)
	// void GaussProjectReverse(double x, double y, out double B, out double L)
	{
		// 定义经纬度
		double B, L;
		B = L = 0.0;

		// int ProjNo; int ZoneWide; ////带宽
		double longitude1, latitude1, longitude0, X0, Y0, xval, yval;
		double e1, e2, f, a, ee, NN, T, C, M, D, R, u, fai, iPI;
		iPI = 0.0174532925199433; // //3.1415926535898/180.0;

		a = _ep.a;
		f = _ep.f; // WGS84

		longitude0 = _middleLine * iPI; // 中央经线
		X0 = 500000;
		Y0 = 0;
		xval = gaussPos.x - X0;
		yval = gaussPos.y - Y0; // 带内大地坐标
		e2 = 2 * f - f * f;
		e1 = (1.0 - Math.sqrt(1 - e2)) / (1.0 + Math.sqrt(1 - e2));
		ee = e2 / (1 - e2);
		M = yval;
		u = M / (a * (1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256));
		fai = u + (3 * e1 / 2 - 27 * e1 * e1 * e1 / 32) * Math.sin(2 * u) + (21 * e1 * e1 / 16 - 55 * e1 * e1 * e1 * e1 / 32)
				* Math.sin(4 * u) + (151 * e1 * e1 * e1 / 96) * Math.sin(6 * u) + (1097 * e1 * e1 * e1 * e1 / 512) * Math.sin(8 * u);
		C = ee * Math.cos(fai) * Math.cos(fai);
		T = Math.tan(fai) * Math.tan(fai);
		NN = a / Math.sqrt(1.0 - e2 * Math.sin(fai) * Math.sin(fai));
		R = a
				* (1 - e2)
				/ Math.sqrt((1 - e2 * Math.sin(fai) * Math.sin(fai)) * (1 - e2 * Math.sin(fai) * Math.sin(fai))
						* (1 - e2 * Math.sin(fai) * Math.sin(fai)));
		D = xval / NN;
		// 计算经度(Longitude) 纬度(Latitude)
		longitude1 = longitude0
				+ (D - (1 + 2 * T + C) * D * D * D / 6 + (5 - 2 * C + 28 * T - 3 * C * C + 8 * ee + 24 * T * T) * D * D * D * D * D / 120)
				/ Math.cos(fai);
		latitude1 = fai
				- (NN * Math.tan(fai) / R)
				* (D * D / 2 - (5 + 3 * T + 10 * C - 4 * C * C - 9 * ee) * D * D * D * D / 24 + (61 + 90 * T + 298 * C + 45 * T * T - 256
						* ee - 3 * C * C)
						* D * D * D * D * D * D / 720);
		B = latitude1;
		L = longitude1;

		// 将弧度转换为度
		B = B * 180.0 / Math.PI;
		L = L * 180.0 / Math.PI;
		return new Dot(L, B);
	}

	// B维度，N计算椭圆弧长迭代的次数
	private double MeridianArcLen(double B, int N) {
		double[] k2 = new double[N];
		// 初始化k2中的值，否则下面使用时，k2中的成员会变成NaN

		for (int n = 0; n < N; n++) {
			k2[n] = 0.0;
		}

		// 计算椭球第一篇心率
		double e2 = _ep.f * (2 - _ep.f);
		double ra = _ep.a * (1 - e2);
		double c = 1.0;

		for (int n = 1; n <= N; n++) {
			c *= (2 * n - 1.0) * (2 * n + 1.0) / (4 * n * n) * e2;
			for (int m = 0; m < n; m++) {
				k2[m] += c;
			}
		}

		double ff1 = 1.0 + k2[0];
		double ff2 = -k2[0];

		double k = 1.0;

		for (int n = 1; n < N; n++) {
			k *= 2 * n / (2 * n + 1.0) * Math.sin(B) * Math.sin(B);
            ff2 += -k2[n] * k;
		}

		return ra * (B * ff1 + 0.5 * ff2 * Math.sin(2.0 * B));
	}

	// static var PI:Number = Math.PI;

	// 高斯投影中央经线
	protected double _middleLine = 0;
	// 椭球参数
	protected EllipsoidParameters _ep;
}
