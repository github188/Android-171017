package com.mapgis.mmt.module.gps.trans.cp;

import android.util.Log;

import com.mapgis.mmt.module.gps.trans.CCoorTransFull;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;

public class MyTest {
	public static double DuToDFM(double du) {
		int fDu, fFeng;
		double fMiao;

		String strD, strF, strM, strResult;

		// 度
		fDu = (int) Math.floor(du);
		strD = String.valueOf(fDu);

		// 分
		fMiao = (du - fDu) * 60;
		fFeng = (int) Math.floor(fMiao);
		strF = String.valueOf(fFeng);

		if (strF.length() < 2) {
			strF = "0" + strF;
		}

		// 秒
		fMiao = (fMiao - fFeng) * 60;
		strM = String.valueOf(fMiao);
		if (fMiao < 10) {
			strM = "0" + strM;
		}

		strResult = strD + strF + strM;

		return Double.parseDouble(strResult);
	}

	static double PI = 3.14159265358979323846;

	public static double DFMToRad(double ddffmm, double rad) {
		double degree = 0.0, minutes = 0.0, second = 0.0;
		// double tmp=0.0;
		int flag = 0;
		// 判断参数的正负
		if (ddffmm < 0) {
			flag = -1;
		} else {
			flag = 1;
		}
		// 取参数的绝对值
		ddffmm = Math.abs(ddffmm);
		// 取度
		degree = Math.floor(ddffmm / 10000);
		// 取分
		minutes = Math.floor((ddffmm - degree * 10000) / 100);
		// 取秒
		second = ddffmm - degree * 10000 - minutes * 100;
		double dd = 0.0;
		// 转换为弧度
		dd = flag * (degree + minutes / 60 + second / 3600);
		rad = dd * PI / 180.0f;

		return rad;
	}

	public static void test() {
		EllipsoidType ellipsoidType = EllipsoidType.CGCS2000;

		String[] m_WebMercatorToLocalCoords = new String[5];

		/*********************
		 * 绍兴CGCS2000转换参数
		 * 
		 * double middleLine = 120, xoffset = 391, yoffset = -280;
		 * 
		 * m_WebMercatorToLocalCoords[0] =
		 * "120.66886694,30.09787273,64397.084,31205.643";
		 * m_WebMercatorToLocalCoords[1] =
		 * "120.57701083,29.97030788,55614.302,17016.466";
		 * m_WebMercatorToLocalCoords[2] =
		 * "120.52653510,30.064085685,50694.861,27388.45";
		 * m_WebMercatorToLocalCoords[3] =
		 * "120.53329660,30.010484339,51374.524,21449.639";
		 * m_WebMercatorToLocalCoords[4] =
		 * "120.512389299,30.01500894,49355.225,21942.008";
		 * 
		 * Dot source = TransformatBDGeoCoord(30.013833, 120.58794); Dot target
		 * = new Dot(55633.939577289755, 21435.546421336476);
		 * 
		 */

		double middleLine = 117, xoffset = 0, yoffset = 0;// xoffset = -84.5,
															// yoffset =
															// -266.25;

		m_WebMercatorToLocalCoords = buildControlPoints();

		// m_WebMercatorToLocalCoords[0] = "116.1728047251,39.1354644286," + pre
		// + "438793.232,4344482.429";
		// m_WebMercatorToLocalCoords[1] = "116.164807715,39.2629516475," + pre
		// + "438020.01,4367770.176";
		// m_WebMercatorToLocalCoords[2] = "116.181153159,39.2445650728," + pre
		// + "439990.913,4364551.244";
		//
		// m_WebMercatorToLocalCoords[3] = "116.2928531861,39.2003661801," + pre
		// + "456137.632,4355746.565";
		// m_WebMercatorToLocalCoords[4] = "116.2916006192,39.1910645196," + pre
		// + "455828.386,4354113.248";
		// m_WebMercatorToLocalCoords[5] = "116.2831500476,39.1855484852," + pre
		// + "454759.564,4353651.821";

		// Dot source = new Dot(116.2831500476, 39.1855484852);
		// Dot target = new Dot(39454759.564, 4353651.821);

		// Dot source = new Dot(116.4757968508, 39.5436991819);
		// Dot target = new Dot(39482850.639, 4419585.839);

		CoordGaussProject project = new CoordGaussProject(middleLine, ellipsoidType);

		String[] d = "116:17:28.04725,39:13:54.64429".split(",");

		String[] args = d[0].split(":");

		double l = Double.parseDouble(args[0]) + (Double.parseDouble(args[1]) + Double.parseDouble(args[2]) / 60) / 60;

		args = d[1].split(":");
		double b = Double.parseDouble(args[0]) + (Double.parseDouble(args[1]) + Double.parseDouble(args[2]) / 60) / 60;

		CCoorTransFull trans = null;

		GpsXYZ xy = new GpsXYZ();

		b = DFMToRad(DuToDFM(b), b);
		l = DFMToRad(DuToDFM(l), l);

		int result = trans.GaosPrj((short) 3, 117, b, l, xy);

		Dot localDot = project.GaussProject(new Dot(l, b));

		WebMercatorToLocalCoordConvert coorConverter = new WebMercatorToLocalCoordConvert(ellipsoidType, middleLine,
				m_WebMercatorToLocalCoords, xoffset, yoffset);

		String log = "";
		int i = 0;

		for (String p : m_WebMercatorToLocalCoords) {
			args = p.split(",");

			Dot source = new Dot(Double.valueOf(args[0]), Double.valueOf(args[1]));
			Dot target = new Dot(Double.valueOf(args[2]), Double.valueOf(args[3]));

			Dot dimingPoint = coorConverter.GeoCoordToLocal(source);

			boolean isOk = dimingPoint.x == target.x && dimingPoint.y == target.y;

			log += "SN：" + (i++) + "——xoff:" + (int) (dimingPoint.x - target.x) + ";yoff:" + (int) (dimingPoint.y - target.y)
					+ "——isEquals:" + isOk + "\n";
		}

		Log.v("TESTGPS", log);
	}

	private static Dot TransformatBDGeoCoord(double fBDLat, double fBDLng) {
		double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
		double fGoogleLat = 0;
		double fGoogleLng = 0;
		double x = fBDLng - 0.0065;
		double y = fBDLat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);

		fGoogleLng = z * Math.cos(theta);
		fGoogleLat = z * Math.sin(theta);

		return new Dot(fGoogleLng, fGoogleLat);
	}

	private static String[] buildControlPoints() {
		String pre = "";// "39";

		ArrayList<String> points = new ArrayList<String>();

		String p = "4344482.429 	438793.232 	11.147 	39.135464429 	116.172804725 	13.235;"
				+ "4367770.176 	438020.010 	13.042 	39.262951648 	116.164807715 	15.130;"
				+ "4364551.244 	439990.913 	11.904 	39.244565073 	116.181153159 	13.992;";

		// p +=
		// "4355746.565 	456137.632 	12.810 	39.200366180 	116.292853186 	5.790;"
		// +
		// "4354113.248 	455828.386 	11.305 	39.191064520 	116.291600619 	4.279;"
		// +
		// "4353651.821 	454759.564 	11.639 	39.185548485 	116.283150048 	4.590;";
		//
		// p +=
		// "4419585.839 	482850.639 	20.082 	39.543699182 	116.475796851 	14.488;"
		// +
		// "4430720.462 	480958.602 	23.097 	40.003785645 	116.463713754 	17.431;"
		// +
		// "4423640.032 	488244.807 	22.743 	39.564877040 	116.514481286 	17.292;";
		//
		// p +=
		// "4417362.273 	497331.711 	15.661 	39.532550670 	116.580769100 	8.260;"
		// +
		// "4358825.003 	463803.495 	18.824 	39.552906840 	116.535949000 	11.328;"
		// +
		// "4417069.806 	496873.893 	14.981 	39.531601840 	116.574842600 	7.566;";
		//
		// p +=
		// "4396595.239 	490474.971 	11.908 	39.421199159 	116.532017258 	6.194;"
		// +
		// "4402679.663 	500225.701 	10.409 	39.452946301 	117.000948166 	5.144;"
		// +
		// "4408642.428 	499792.586 	10.986 	39.484279687 	116.595127982 	5.534;";
		//
		// p +=
		// "4425829.709 	498613.265 	19.821 	39.580005500 	116.590156700 	12.571;"
		// +
		// "4427798.093 	505254.162 	17.824 	39.590382020 	117.034145200 	10.857;"
		// +
		// "4427449.742 	506643.964 	17.666 	39.584633320 	117.023984100 	11.466;";
		//
		// p +=
		// "4475529.957 	346297.467 	551.926 	40.240036761 	115.112224516 	542.850;"
		// +
		// "4473042.269 	345961.178 	538.198 	40.223953347 	115.111015527 	529.092;"
		// +
		// "4473873.953 	347475.294 	531.916 	40.230748513 	115.121359398 	522.821";

		for (String line : p.split(";")) {
			if (line == null || line.length() == 0) {
				continue;
			}

			String[] args = line.split(" ");

			points.add(args[4].trim() + "," + args[3].trim() + "," + pre + args[1].trim() + "," + args[0].trim());
		}

		return points.toArray(new String[points.size()]);
	}
}
