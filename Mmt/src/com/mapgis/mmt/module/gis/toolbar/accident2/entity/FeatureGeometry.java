package com.mapgis.mmt.module.gis.toolbar.accident2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.zondy.mapgis.geometry.Dot;

import java.util.Arrays;

public class FeatureGeometry implements Parcelable {
    public double x;
    public double y;
    public double[][][] paths;
    public String line;

    /**
     * 获取该图形信息的中间点，若是点设备则直接返回坐标，若是线段则返回计算过后的中间点
     */
    public Dot getDot() {
        Dot dot = null;
        if ((int) x != 0 && (int) y != 0) {
            dot = new Dot(x, y);
        }
        // 有返回数据且返回数据是有效的
        if (paths != null && paths.length > 0) {
            double[][] positions = paths[0];
            // 返回的线段点个数大于1个
            if (positions.length > 1) {
                if (positions.length % 2 == 0) {// 如果是偶数个点，则取最中间两个点的中间点
                    int index = positions.length / 2;
                    double[] startPoint = positions[index - 1];
                    double[] endPoint = positions[index];
                    dot = new Dot((startPoint[0] + endPoint[0]) / 2, (startPoint[1] + endPoint[1]) / 2);
                } else {// 如果是偶数个点，则取中间点
                    int index = positions.length / 2;
                    dot = new Dot(positions[index][0], positions[index][1]);
                }
            }
        }
        return dot;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double[][][] getPaths() {
        return paths;
    }

    public void setPaths(double[][][] paths) {
        this.paths = paths;
    }

    @Override
    public String toString() {
        return "FeatureGeometry{" +
                "x=" + x +
                ", y=" + y +
                ", line='" + line + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.x);
        dest.writeDouble(this.y);
        this.line = arrToStr(this.paths);
        dest.writeString(this.line);
    }

    private String arrToStr(double[][][] array) {
        if (array == null) {
            return null;
        }
        return new Gson().toJson(this.paths);
    }

    public FeatureGeometry() {
    }

    protected FeatureGeometry(Parcel in) {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.line = in.readString();
        this.paths = strToArray(this.line);
    }

    /**
     * 三维数组转字符串
     */
    private double[][][] strToArray(String s) {
        if (BaseClassUtil.isNullOrEmptyString(s)) {
            return null;
        }
        return new Gson().fromJson(s, new TypeToken<double[][][]>() {
        }.getType());
    }

    /**
     * 三维数组转字符串
     */
    public String arrayToStr(double[][][] array) {
        if (array == null) {
            return null;
        }
        if (array[0][0].length == 0) {
            return "[[[]]]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (double[][] d1 : array) {
            sb.append('[');
            for (double[] d2 : d1) {
                sb.append(Arrays.toString(d2));
            }
            sb.append(']');
        }
        sb.append(']');
        return sb.toString();
    }

    public static final Creator<FeatureGeometry> CREATOR = new Creator<FeatureGeometry>() {
        @Override
        public FeatureGeometry createFromParcel(Parcel source) {
            return new FeatureGeometry(source);
        }

        @Override
        public FeatureGeometry[] newArray(int size) {
            return new FeatureGeometry[size];
        }
    };
}
