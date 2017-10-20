package com.mapgis.mmt.module.gis.toolbar.online.query;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.geometry.Dot;

/** 在线地理位置信息模型 */
public class OnlineGermetry implements Parcelable {
	public double x;
	public double y;

	public double paths[][][];

	@Override
	public String toString() {
		return x + "," + y;
	}

	public GraphicPolylin createGraphicPolylin() {
		GraphicPolylin polylin = new GraphicPolylin();

		if (paths != null) {

			polylin.setColor(Color.RED);
			polylin.setLineWidth(5);

			for (double[][] path : paths) {
				for (double[] pathDot : path) {
					polylin.appendPoint(new Dot(pathDot[0], pathDot[1]));
				}
			}

		}

		return polylin;
	}

	/** 点设备返回点坐标，线设备返回中心点坐标 */
	public Dot toDot() {
		if (paths == null) {
			return new Dot(x, y);
		} else {
			if (paths[0].length == 2) {
				return new Dot((paths[0][0][0] + paths[0][1][0]) / 2, (paths[0][0][1] + paths[0][1][1]) / 2);
			} else {
				return new Dot(paths[0][paths.length / 2][0], paths[0][paths.length / 2][1]);
			}
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeSerializable(paths);
	}

	public static final Parcelable.Creator<OnlineGermetry> CREATOR = new Parcelable.Creator<OnlineGermetry>() {
		@Override
		public OnlineGermetry createFromParcel(Parcel in) {
			return new OnlineGermetry(in);
		}

		@Override
		public OnlineGermetry[] newArray(int size) {
			return new OnlineGermetry[size];
		}
	};

	private OnlineGermetry(Parcel in) {
		x = in.readDouble();
		y = in.readDouble();
		paths = (double[][][]) in.readSerializable();
	}
}