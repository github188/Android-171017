package com.mapgis.mmt.module.gps.trans;

import com.google.gson.Gson;

/**
 * 中央经线投影时所需的偏移参数结构体（二参数结构体）
 *
 * @author Zoro
 */
class TWOPARAM {
    /**
     * x方向偏移量
     */
    public double x_off;

    /**
     * y方向偏移量
     */
    public double y_off;
}

/**
 * 四参数结构体
 *
 * @author Zoro
 */
class FOURPARAM {
    /**
     * x方向偏移量，单位：米
     */
    public double x_off;

    /**
     * y方向偏移量，单位：米
     */
    public double y_off;

    /**
     * 尺度因子，单位，无
     */
    public double m;

    /**
     * 旋转角度，单位弧度
     */
    public double angle;
}

/**
 * 六参数结构体
 *
 * @author Zoro
 */
class SIXPARAM {
    /**
     * a为地方独立坐标系下已知的一点 a在地方坐标系下的x坐标，单位：米
     */
    public double x0_local;

    /**
     * a在地方独立坐标下的y坐标，单位：米
     */
    public double y0_local;

    /**
     * a点的gps经纬度经高斯投影后的平面坐标x，单位：米
     */
    public double x0_gps;

    /**
     * a点的gps经纬度经高斯投影后的平面坐标y，单位：米
     */
    public double y0_gps;

    /**
     * 旋转角度，单位：弧度
     */
    public double angle;

    /**
     * 尺度因子，单位：无
     */
    public double m;
}

/**
 * 七参数结构体
 *
 * @author Zoro
 */
class SEVENPARAM {
    /**
     * x方向偏移量，单位：米
     */
    public double x_off;

    /**
     * y方向偏移量，单位：米
     */
    public double y_off;

    /**
     * z方向偏移量，单位：米
     */
    public double z_off;

    /**
     * 绕x轴旋转的角度，单位：弧度
     */
    public double x_angle;

    /**
     * 绕y轴旋转的角度，单位：弧度
     */
    public double y_angle;

    /**
     * 绕z轴旋转的角度，单位：弧度
     */
    public double z_angle;

    /**
     * 尺度因子，单位，无
     */
    public double m;
}

/**
 * 七参数+二参数(传统意义上的七参数)
 *
 * @author Zoro
 */
class SEVENTWO {
    /**
     * 二参数
     */
    TWOPARAM two_param;
    /**
     * 七参数
     */
    SEVENPARAM seven_param;
}

/**
 * 七参数+四参数
 *
 * @author Zoro
 */
class SEVENFOUR {
    /**
     * 四参数
     */
    FOURPARAM four_param;
    /**
     * 七参数
     */
    SEVENPARAM seven_param;
}

/**
 * 坐标转换使用的参数配置文件
 *
 * @author Zoro
 */
public class TransParams {
    /**
     * 椭球类型 1：54椭球，2：80椭球，3：84椭球
     */
    public short ellipseType;

    /**
     * 中央经线
     */
    public double middleLine;

    /**
     * 转换类型 1为四参数，2-六参数，3-七参数+二参数，4-七参数+四参数，5-中央经线投影（二参数）,6-七参数+二参数反转
     */
    public short transType;

    /**
     * 0-与MapGIS的X,Y方向相同，1-与MapGIS的X,Y方向相反
     */
    public int rev;

    /**
     * 四参数
     */
    public FOURPARAM four_param;

    /**
     * 六参数
     */
    public SIXPARAM six_param;

    /**
     * 七参数+二参数(传统意义上的七参数)
     */
    public SEVENTWO seven_two;

    /**
     * 七参数+四参数
     */
    public SEVENFOUR seven_four;

    /**
     * 中央经线投影（二参数）
     */
    public TWOPARAM two_param;

    /**
     * 七参数+二参数反转
     */
    public SEVENTWO seven_two_rev;

    public TransParams clone() {
        try {
            Gson gson = new Gson();

            String json = gson.toJson(this);

            TransParams params = gson.fromJson(json, TransParams.class);

            return params;
        } catch (Exception ex) {
            ex.printStackTrace();

            return this;
        }
    }
}
