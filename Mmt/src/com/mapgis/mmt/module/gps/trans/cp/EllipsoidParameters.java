package com.mapgis.mmt.module.gps.trans.cp;

public class EllipsoidParameters {

	public EllipsoidParameters(EllipsoidType ellipsoidType) {
		switch (ellipsoidType) {
		case BeiJing54: {
			this.a = 6378245;
			this.f = 1 / 298.3;
			this.e2 = 0.006693421623;
			this.A1 = 111134.8611;
			this.A2 = -16036.4803;
			this.A3 = 16.8281;
			this.A4 = -0.0220;
		}
			break;
		case XiAn80: {
			this.a = 6378140;
			this.f = 1 / 298.257;
			this.e2 = 0.0066943849995879;
			this.A1 = 111133.0047;
			this.A2 = -16038.5282;
			this.A3 = 16.8326;
			this.A4 = -0.0220;
		}
			break;
		case WGS84: {
			this.a = 6378137;
			this.f = 1 / 298.257223563;
			this.e2 = 0.00669437999013;
			this.A1 = 111133.0047;
			this.A2 = -16038.5282;
			this.A3 = 16.8326;
			this.A4 = -0.0220;
		}
			break;
		case CGCS2000: {
			this.a = 6378137.0;
			this.f = 1 / 298.257222101;
			this.e2 = 0.0066943800229;
			this.A1 = 111133.0047;
			this.A2 = -16038.5282;
			this.A3 = 16.8326;
			this.A4 = -0.0220;
		}
			break;
		case GoogleEarth: {
			this.a = 6378137.0;
			this.f = 0;
			this.e2 = 0;
			this.A1 = 111319.490793274;
			this.A2 = 0;
			this.A3 = 0;
			this.A4 = 0;
		}
			break;
		}
	}

	// 椭球长半轴，单位米
	public double a = 0;
	// 扁率，单位无
	public double f = 0;
	// 第一偏心率的平方，单位 无
	public double e2 = 0;
	// 测绘计算常数
	public double A1 = 0;
	public double A2 = 0;
	public double A3 = 0;
	public double A4 = 0;
}
