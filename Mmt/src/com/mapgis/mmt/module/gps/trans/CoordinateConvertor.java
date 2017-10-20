package com.mapgis.mmt.module.gps.trans;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.GlobalPathManager;
import com.mapgis.mmt.entity.ResultData;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

//GPS经纬度坐标向本地坐标转换类
public class CoordinateConvertor {

	public void LoadTransParamsFromWeb(String name) throws Exception {

		String json = "";
		TransParams params = null;

		String uri = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/GetTransParamsConfig";
		json = NetUtil.executeHttpGet(uri);

		if (!TextUtils.isEmpty(json)) {

			ResultData<TransParams> data = new Gson().fromJson(json, new TypeToken<ResultData<TransParams>>() {
			}.getType());

			if (data.ResultCode > 0) {
				params = data.getSingleData();
			}
		}

		if (params == null) {
			json = NetUtil.downloadStringResource(name);
			params = new Gson().fromJson(json, TransParams.class);
		}

		if (params.transType <= 0) {
			throw new Exception("获取或者解析七参数失败");
		} else {
			gpsTransFull = new CCoorTransFull(params);
		}
	}

	public void LoadTransParamsFromLocal(String path) throws Exception {
		File file = new File(path);

        //兼容配置文件在conf下的系统
        if (!file.exists()) {
            String srcPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Conf) + GlobalPathManager.TRANS_PARAMS_FILE;
            File srcFile = new File(srcPath);
            if (srcFile.exists()) {
                FileUtil.copyFile(srcFile, file);
            }
        }

		if (!file.exists() && !FileUtil.copyAssetToSD("cfg/" + GlobalPathManager.TRANS_PARAMS_FILE, path)) {
			return;
		}

		InputStreamReader reader = new InputStreamReader(new FileInputStream(file));

		TransParams params = new Gson().fromJson(reader, TransParams.class);

		reader.close();

		if (params.transType <= 0) {
			throw new Exception("获取或者解析七参数失败");
		} else {
			gpsTransFull = new CCoorTransFull(params);
		}
	}

	public double DuToDFM(double du) {
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

	public CCoorTransFull gpsTransFull;

	// 项目类型，0：母盘；1：上海
	public short ProjectType = 0;

	// GPS经纬度坐标转换为本地坐标
	public boolean Convert(GpsXYZ xy) {
		try {
			double lon = xy.getX(), lat = xy.getY();

			// 高斯坐标转换
			if (lon > 0) {
				double dLon = DuToDFM(lon);
				double dLat = DuToDFM(lat);

				switch (ProjectType) {
				case 1:
					CoorTrans_SH(dLat, dLon, xy);
					break;
				default:
					if (gpsTransFull != null) {
						gpsTransFull.CoorTrans(dLat, dLon, xy.getZ(), xy);
						break;
					} else {
						// 没有七参数文件默认使用web莫卡托投影变化
						CCoorTransFull.LatLonToMeters(lon, lat, xy);

						return true;
					}
				}
			}

			// xy值反转
			lon = xy.getX();

			xy.setX(xy.getY());
			xy.setY(lon);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public double D_LATPERX = 0.032325203; // 每单位长度对应的纬度 (秒/米)
	public double D_LONGPERY = 0.037532153; // 每单位长度对应的经度 (秒/米)
	public double D_SHORGX = -49389.359295; // 初始点上海市坐标点X(米)
	public double D_SHORGY = 851.160763; // 初始点上海市坐标点Y(米);
	public double D_LATORG = 304723.0; // 初始点纬度(度分秒)
	public double D_LONORG = 1212833.7; // 初始点纬度(度分秒)

	/**
	 * 上海坐标转换
	 * 
	 * @param dLat
	 * @param dLon
	 * @param xy
	 */
	public void CoorTrans_SH(double dLat, double dLon, GpsXYZ xy) {
		double dHGX = xy.getX(), dHGY = xy.getY();

		double dSHX = 0;
		double dSHY = 0;
		dHGX = 0;
		dHGY = 0;

		GpsXYZ hxy = new GpsXYZ(dSHX, dSHY);
		// 经纬度转换为上海市坐标
		LCnvToSH(dLat, dLon, hxy);

		dSHX = hxy.getX();
		dSHY = hxy.getY();

		GpsXYZ gxy = new GpsXYZ(dHGY, dHGX);

		// 上海市坐标转换为化工区坐标
		SHCnvToHG(dSHX, dSHY, gxy);

		dHGY = gxy.getX();
		dHGX = gxy.getY();

		xy.setX(dHGX);
		xy.setY(dHGY);
	}

	public double GetduByDFM(double x0) {
		double a0, b0, c0;

		c0 = x0 % 100;// 得到秒
		b0 = x0 % 10000;// 去掉度
		a0 = x0 / 10000;// 得到度
		a0 = a0 - b0 / 10000;

		b0 = (b0 - c0) / 100;// 得到分
		x0 = c0 / 3600 + b0 / 60 + a0;

		return x0;
	}

	/**
	 * 经纬度转换为上海市坐标
	 * 
	 * @param dLat
	 * @param dLon
	 * @param xy
	 */
	public void LCnvToSH(double dLat, double dLon, GpsXYZ xy) {
		double dSHX = xy.getX(), dSHY = xy.getY();

		// 新坐标
		double dLatSec = GetduByDFM(dLat) * 60 * 60;
		double dLonSec = GetduByDFM(dLon) * 60 * 60;
		// 原坐标
		double dLatOrg = GetduByDFM(D_LATORG) * 3600;
		double dLonOrg = GetduByDFM(D_LONORG) * 3600;

		dSHX = D_SHORGX - (dLatOrg - dLatSec) / D_LATPERX;
		dSHY = D_SHORGY - (dLonOrg - dLonSec) / D_LONGPERY;

		xy.setX(dSHX);
		xy.setY(dSHY);
	}

	/**
	 * 上海市坐标转换为化工区坐标
	 * 
	 * @param dSHX
	 * @param dSHY
	 * @param xy
	 */
	public void SHCnvToHG(double dSHX, double dSHY, GpsXYZ xy) {
		double dHGY = xy.getX(), dHGX = xy.getY();

		try {
			double a = (22.9562611 * Math.PI) / 180;
			double x0 = -50089.86;
			double y0 = -3230.048;
			dHGX = (dSHX - x0) * Math.cos(a) - (dSHY - y0) * Math.sin(a) + 3504;
			dHGY = (dSHX - x0) * Math.sin(a) + (dSHY - y0) * Math.cos(a) + 4000;
		} catch (Exception ex) {
			dHGX = 0;
			dHGY = 0;
		}

		xy.setX(dHGY);
		xy.setY(dHGX);
	}

	/**
	 * 深圳坐标转换
	 * 
	 * @param ellipseType
	 *            1:北京54椭球 | 2:西安80椭球 | 3:WGS-84椭球
	 * @param middleLine
	 *            中央经线，单位：度
	 * @param B
	 *            纬度，单位弧度
	 * @param L
	 *            经度，单位弧度
	 * @param xy
	 *            x 高斯投影后的平面坐标，单位：米
	 */
	public void CoorTrans_SZ(short ellipseType, double middleLine, double B, double L, GpsXYZ xy) {
		double x = xy.getX(), y = xy.getY();

		double dX54 = 0;
		double dY54 = 0;

		GpsXYZ xy54 = new GpsXYZ(dX54, dY54);
		gpsTransFull.GaosPrj(ellipseType, middleLine, B, L, xy54);

		dX54 = xy54.getX();
		dY54 = xy54.getY();

		double a = 0.9998464254;
		double b = 0.01707929357;
		double c = 2472721.333;
		double d = 391032.019;

		double dXsz = (1 / (a * a + b * b)) * (a * dX54 - b * dY54 + b * d - a * c);
		double dYsz = (1 / (a * a + b * b)) * (b * dX54 + a * dY54 - b * c - a * d);

		x = dXsz;
		y = dYsz;

		xy.setX(x);
		xy.setY(y);
	}
}
