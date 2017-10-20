package com.mapgis.mmt.module.gps.trans;

import com.mapgis.mmt.MyApplication;

/**
 * 北京54椭球参数
 *
 * @author Zoro
 */
class BJ_ELLIPSE {
    /**
     * 椭球长半轴，单位：米
     */
    public double a;

    /**
     * 扁率，单位：无
     */
    public double f;

    /**
     * 第一偏心率的平方，单位：无
     */
    public double e2;

    /**
     * 测绘计算常数
     */
    public double A1, A2, A3, A4;
}

/**
 * 西安80椭球参数
 *
 * @author Zoro
 */
class XA_ELIPSE {
    /**
     * 椭球长半轴，单位：米
     */
    public double a;

    /**
     * 扁率，单位，无
     */
    public double f;

    /**
     * 第一偏心率的平方，单位：无
     */
    public double e2;

    /**
     * 测绘计算常数
     */
    public double A1, A2, A3, A4;
}

/**
 * WGS-84椭球参数
 *
 * @author Zoro
 */
class GPS_ELLIPSE {
    /**
     * 椭球长半轴，单位：米
     */
    public double a;

    /**
     * 扁率，单位：无
     */
    public double f;

    /**
     * 第一偏心率的平方，单位：无
     */
    public double e2;

    /**
     * 测绘计算常数
     */
    public double A1, A2, A3, A4;
}

// / <summary>
// / 坐标转换大全类
// / </summary>
public class CCoorTransFull {
    static double PI = 3.14159265358979323846;

    TransParams params;

    // 构造函数
    public CCoorTransFull(TransParams params) {
        this.params = params;

        // 转换类型
        m_transType = 0;

        // 椭球类型
        m_ellipseType = 0;

        // 中央经线
        m_middleLine = 0.0;

        // 北京-54椭球参数
        m_bjEllipse.a = 6378245;
        m_bjEllipse.f = 1 / 298.3;
        m_bjEllipse.e2 = 0.006693421623;
        m_bjEllipse.A1 = 111134.8611;
        m_bjEllipse.A2 = -16036.4803;
        m_bjEllipse.A3 = 16.8281;
        m_bjEllipse.A4 = -0.0220;

        // 西安-80椭球参数
        m_xaEllipse.a = 6378140;
        m_xaEllipse.f = 1 / 298.257;
        m_xaEllipse.e2 = 0.0066943849995879;
        m_xaEllipse.A1 = 111133.0047;
        m_xaEllipse.A2 = -16038.5282;
        m_xaEllipse.A3 = 16.8326;
        m_xaEllipse.A4 = -0.0220;

        // WGS-84椭球参数
        m_gpsEllipse.a = 6378137;
        m_gpsEllipse.f = 1 / 298.257233563;
        m_gpsEllipse.e2 = 0.006694379989;
        m_gpsEllipse.A1 = 111133.0047;
        m_gpsEllipse.A2 = -16038.5282;
        m_gpsEllipse.A3 = 16.8326;
        m_gpsEllipse.A4 = -0.0220;

        // 初始化转换参数
        InitTransParams();
    }

    // 椭球类型
    private enum EllipseType {
        Unkonwn(-1),
        BeiJing54(0),
        XiAn80(1),
        WGS84(2),
        CGCS2000(3),
        GoogleEarth(4);  // google 地球， 定义了一个球体

        private int index;

        EllipseType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public class EllipsoidParameters {
        public EllipsoidParameters(EllipseType ellipseType) throws Exception {
            switch (ellipseType) {
                case BeiJing54://北京-54椭球参数
                    this.a = 6378245;
                    this.f = 1 / 298.3;
                    this.e2 = 0.006693421623;
                    this.A1 = 111134.8611;
                    this.A2 = -16036.4803;
                    this.A3 = 16.8281;
                    this.A4 = -0.0220;
                    break;
                case XiAn80://西安-80椭球参数
                    this.a = 6378140;
                    this.f = 1 / 298.257;
                    this.e2 = 0.0066943849995879;
                    this.A1 = 111133.0047;
                    this.A2 = -16038.5282;
                    this.A3 = 16.8326;
                    this.A4 = -0.0220;
                    break;
                case WGS84: //WGS-84椭球参数
                    this.a = 6378137;
                    this.f = 1 / 298.257223563;
                    this.e2 = 0.00669437999013;
                    this.A1 = 111133.0047;
                    this.A2 = -16038.5282;
                    this.A3 = 16.8326;
                    this.A4 = -0.0220;
                    break;
                case CGCS2000:  //CGCS2000椭球参数
                    this.a = 6378137.0;
                    this.f = 1 / 298.257222101;
                    this.e2 = 0.0066943800229;
                    this.A1 = 111133.0047;
                    this.A2 = -16038.5282;
                    this.A3 = 16.8326;
                    this.A4 = -0.0220;
                    break;
                case GoogleEarth:   // google是一个标准球
                    this.a = 6378137.0;
                    this.f = 0;
                    this.e2 = 0;
                    this.A1 = 111319.490793274;
                    this.A2 = 0;
                    this.A3 = 0;
                    this.A4 = 0;
                    break;
                default:
                    throw new Exception("Unkown EllipseType");
            }
        }

        //椭球长半轴，单位：米
        public double a;

        //扁率，单位：无
        public double f;

        //第一偏心率的平方，单位：无
        public double e2;

        //测绘计算常数
        double A1;
        double A2;
        double A3;
        double A4;
    }

    /**
     * 转换类型，1：为七参数法，2为七参数+四参数法，3为四参数法，4为六参数法,6为七参数+四参数反转
     */
    private short m_transType;

    // 客户地图所采用的椭球类型，1为北京54椭球，2为西安80椭球,3为WGS-84椭球
    private short m_ellipseType;

    // 中央经线，单位：度
    private double m_middleLine;

    // XY坐标方向与mapgis是否取反 1/0 反向/正向
    private int m_rev;

    // 西安80椭球
    private XA_ELIPSE m_xaEllipse = new XA_ELIPSE();

    // 北京54椭球
    private BJ_ELLIPSE m_bjEllipse = new BJ_ELLIPSE();

    // WGS-84椭球
    private GPS_ELLIPSE m_gpsEllipse = new GPS_ELLIPSE();

    // 六参数
    private SIXPARAM m_sixParam = new SIXPARAM();

    // 四参数
    private FOURPARAM m_fourParam = new FOURPARAM();

    // 七参数
    private SEVENPARAM m_sevenParam = new SEVENPARAM();

    // 二参数
    private TWOPARAM m_twoParam = new TWOPARAM();

    // 高斯投影
    // ellipseType 椭球类型
    // middleLine 中央经线，单位：度
    // B,L 纬度和经度，单位弧度
    // x y 高斯投影后的平面坐标，单位：米
    // 返回值：操作是否成功，0-失败，1-成功
    public short GaosPrj(short ellipseType, double middleLine, double B, double L, GpsXYZ xy) {
        double x = xy.getX();
        double y = xy.getY();

        double e2 = 0.0;
        double a = 0.0;
        // e2=0.00669437999013;
        // a=6378137;
        // 根据椭球类型取相应的参数
        switch (ellipseType) {
            // 北京54椭球
            case 1:
                a = m_bjEllipse.a;
                e2 = m_bjEllipse.e2;
                break;
            // 西安80椭球
            case 2:
                a = m_xaEllipse.a;
                e2 = m_xaEllipse.e2;
                break;
            // WGS-84椭球
            case 3:
                a = m_gpsEllipse.a;
                e2 = m_gpsEllipse.e2;
                break;
            default:
                break;
        }

        // 将度转换为弧度
        double b = B;
        // DFMToRad(B,b);

        // 计算经度差
        double l1 = 0.0;
        double mid = 0.0;
        mid = middleLine * PI / 180.0;
        l1 = L - mid;

        // 高斯投影所需的系数
        double g = 0.0;
        g = Math.sqrt(e2 / (1 - e2)) * Math.cos(b);
        double g2 = 0.0, g4 = 0.0;
        g2 = g * g;
        g4 = g2 * g2;
        double t = 0.0;
        t = Math.tan(b);
        double t2 = 0.0, t4 = 0.0;
        t2 = t * t;
        t4 = t2 * t2;
        double m = 0.0;
        m = l1 * Math.cos(b);
        double m2 = 0.0, m3 = 0.0, m4 = 0.0, m5 = 0.0, m6 = 0.0;
        m2 = m * m;
        m3 = m2 * m;
        m4 = m3 * m;
        m5 = m4 * m;
        m6 = m5 * m;

        double N = 0.0;
        N = a / Math.sqrt(1 - e2 * Math.sin(b) * Math.sin(b));
        // 子午线弧长
        double x0 = 0.0;
        // 求子午线弧长
        x0 = MeriddianArcLength(ellipseType, B, 5);
        // 计算高斯平面坐标
        x = x0 + N * t * m2 / 2 + N * t * (5 - t2 + 9 * g2 + 4 * g4) * m4 / 24 + N * t * (61 - 58 * t2 + t4 + 270 * g2 - 330 * g2 * t2)
                * m6 / 720;
        y = N * m + N * (1 - t2 + g2) * m3 / 6 + N * (5 - 18 * t2 + 14 * g2 - 58 * g2 * t2) * m5 / 120 + 500000;

        xy.setX(x);
        xy.setY(y);
        return 1;
    }

    // 高斯逆向投影
    // ellipseType 椭球类型
    // middleLine 中央经线，单位：度
    // x y 高斯投影后的平面坐标，单位：米
    // B,L 纬度和经度，单位弧度
    // 返回值：操作是否成功，0-失败，1-成功
    private short GaussProjInvCal(short ellipseType, double middleLine, double x, double y, GpsXYZ bl) {
        double B = bl.getX();
        double L = bl.getY();

        // 调整X,Y坐标
        y -= 500000;

        // 得到常数
        double a = 0.0f, e2 = 0.0f;
        double A1 = 0.0f, A2 = 0.0f, A3 = 0.0f, A4 = 0.0f;
        switch (ellipseType) {
            // 北京54椭球
            case 1:
                a = m_bjEllipse.a;
                e2 = m_bjEllipse.e2;
                A1 = m_bjEllipse.A1;
                A2 = m_bjEllipse.A2;
                A3 = m_bjEllipse.A3;
                A4 = m_bjEllipse.A4;
                break;
            // 西安80椭球
            case 2:
                a = m_xaEllipse.a;
                e2 = m_xaEllipse.e2;
                A1 = m_xaEllipse.A1;
                A2 = m_xaEllipse.A2;
                A3 = m_xaEllipse.A3;
                A4 = m_xaEllipse.A4;
                break;
            // WGS-84椭球
            case 3:
                a = m_gpsEllipse.a;
                e2 = m_gpsEllipse.e2;
                A1 = m_gpsEllipse.A1;
                A2 = m_gpsEllipse.A2;
                A3 = m_gpsEllipse.A3;
                A4 = m_gpsEllipse.A4;
                break;
            default:
                break;
        }

        // 计算底点纬度
        double B0 = x / A1;
        double preB0 = 0.0f;
        double eta = 0.0f;
        do {
            preB0 = B0;
            B0 = B0 * PI / 180.0;
            B0 = (x - (A2 * Math.sin(2 * B0) + A3 * Math.sin(4 * B0) + A4 * Math.sin(6 * B0))) / A1;
            eta = Math.abs(B0 - preB0);
        } while (eta > 0.000000001);
        B0 = B0 * PI / 180.0;

        // 计算其它常数
        double sinB = Math.sin(B0);
        double cosB = Math.cos(B0);
        double t = Math.tan(B0);
        double t2 = t * t;
        double N = a / Math.sqrt(1 - e2 * sinB * sinB);
        double ng2 = cosB * cosB * e2 / (1 - e2);
        double V = Math.sqrt(1 + ng2);
        double yN = y / N;

        // 得到经纬度
        double L0 = middleLine * PI / 180.0;
        B = B0
                - (yN * yN - (5 + 3 * t2 + ng2 - 9 * ng2 * t2) * yN * yN * yN * yN / 12.0 + (61 + 90 * t2 + 45 * t2 * t2) * yN * yN * yN
                * yN * yN * yN / 360.0) * V * V * t / 2;
        L = L0
                + (yN - (1 + 2 * t2 + ng2) * yN * yN * yN / 6.0 + (5 + 28 * t2 + 24 * t2 * t2 + 6 * ng2 + 8 * ng2 * t2) * yN * yN * yN * yN
                * yN / 120.0) / cosB;

        bl.setX(B);
        bl.setY(L);
        return 1;
    }

    // 计算子午线弧长
    // B纬度，单位：弧度，N为迭代次数
    // 返回值：子午线弧长
    private double MeriddianArcLength(short ellipseType, double B, int N) {
        // 椭球长半轴和扁率
        double a = 0.0;
        double f = 0.0;
        // 椭球类型，1：54椭球，2：80椭球，3：84椭球
        switch (ellipseType) {
            case 1:
                a = m_bjEllipse.a;
                f = m_bjEllipse.f;
                break;
            case 2:
                a = m_xaEllipse.a;
                f = m_xaEllipse.f;
                break;
            case 3:
                a = m_gpsEllipse.a;
                f = m_gpsEllipse.f;
                break;
            default:
                break;
        }
        // 将度转换为弧度
        double lat = B;
        // DFMToRad(B,lat);
        int i, n, m;
        double e2, ra, c, ff1, k, ff2, sin2;
        double[] k2 = new double[5];
        for (i = 0; i < N; i++) {
            k2[i] = 0.0;
        }
        // 计算椭球第一篇心率
        e2 = f * (2 - f);
        //
        ra = a * (1 - e2);
        for (c = 1.0, n = 1; n <= N; n++) {
            c *= (2 * n - 1.0) * (2 * n + 1.0) / (4 * n * n) * e2;
            for (m = 0; m < n; m++) {
                k2[m] += c;
            }
        }
        ff1 = 1.0 + k2[0];
        ff2 = -k2[0];
        sin2 = Math.sin(lat) * Math.sin(lat);
        for (k = 1.0, n = 1; n < N; n++) {
            k *= 2 * n / (2 * n + 1.0) * sin2;
            ff2 += -k2[n] * k;
        }
        return ra * (lat * ff1 + 0.5 * ff2 * Math.sin(2.0 * lat));
    }

    // 度分秒转换为弧度
    // ddffmm，要转换的参数，单位ddffmm（1202532.6）
    // rad，转换后的结果参数，单位：弧度
    // 返回值：操作是否成功，0-失败，1-成功
    public double DFMToRad(double ddffmm, double rad) {
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

    // 弧度转为ddffmm
    public double RadToDFM(double rad, double ddffmm) {
        // 转化为度
        double du = rad * 180.0f / PI;
        double degree = Math.floor(du);
        double bigminutes = (du - degree) * 60;
        double minutes = Math.floor(bigminutes);
        double second = (bigminutes - minutes) * 60;
        ddffmm = degree * 10000 + minutes * 100 + second;

        return ddffmm;
    }

	/*
     * // 七参数转换 // param_7 七参数结构体 // X，Y，Z待转换的空间直角坐标，单位 米 //
	 * X1，Y1,Z1转换后的空间直角坐标，单位：米 // 返回值：操作是否成功，0-失败，1-成功 private short
	 * SevenParamTrans(SEVENPARAM param_7, double X, double Y, double Z, GpsXYZ
	 * xyz) { double X1 = xyz.getX(), Y1 = xyz.getY(), Z1 = xyz.getZ();
	 * 
	 * X1 = param_7.x_off + Y * param_7.z_angle - Z * param_7.y_angle + (1 +
	 * param_7.m) * X; Y1 = param_7.y_off - X * param_7.z_angle + Z *
	 * param_7.x_angle + (1 + param_7.m) * Y; Z1 = param_7.z_off + X *
	 * param_7.y_angle - Y * param_7.x_angle + (1 + param_7.m) * Z;
	 * 
	 * return 1; }
	 */

    // 七参数转换(矩阵版)
    // param_7 七参数结构体
    // X，Y，Z待转换的空间直角坐标，单位 米
    // X1，Y1,Z1转换后的空间直角坐标，单位：米
    // 返回值：操作是否成功，0-失败，1-成功
    private short SevenParamTrans_Multi(SEVENPARAM param_7, double X, double Y, double Z, GpsXYZ xyz) {
        double X1 = xyz.getX(), Y1 = xyz.getY(), Z1 = xyz.getZ();

        // 秒转换
        SEVENPARAM Cvt_Param_7 = param_7;

        // 条件
        double transX = X;
        double transY = Y;
        double transZ = Z;

        // Xi矩阵
        NNMatrix Xi = new NNMatrix(3, 1);
        Xi.Matrix[0][0] = transX;
        Xi.Matrix[1][0] = transY;
        Xi.Matrix[2][0] = transZ;

        // DX矩阵
        NNMatrix DX = new NNMatrix(3, 1);
        DX.Matrix[0][0] = Cvt_Param_7.x_off;
        DX.Matrix[1][0] = Cvt_Param_7.y_off;
        DX.Matrix[2][0] = Cvt_Param_7.z_off;

        // tY矩阵
        NNMatrix tY = new NNMatrix(3, 1);

        // k矩阵
        NNMatrix K = new NNMatrix(1, 1);
        K.Matrix[0][0] = 1 + Cvt_Param_7.m;

        // Mx矩阵
        NNMatrix Mx = new NNMatrix(3, 3);
        Mx.Matrix[0][0] = 1.0f;
        Mx.Matrix[0][1] = 0.0f;
        Mx.Matrix[0][2] = 0.0f;

        Mx.Matrix[1][0] = 0.0f;
        Mx.Matrix[1][1] = Math.cos(Cvt_Param_7.x_angle);
        Mx.Matrix[1][2] = Math.sin(Cvt_Param_7.x_angle);

        Mx.Matrix[2][0] = 0.0f;
        Mx.Matrix[2][1] = -Math.sin(Cvt_Param_7.x_angle);
        Mx.Matrix[2][2] = Math.cos(Cvt_Param_7.x_angle);

        // My矩阵
        NNMatrix My = new NNMatrix(3, 3);

        My.Matrix[0][0] = Math.cos(Cvt_Param_7.y_angle);
        My.Matrix[0][1] = 0.0f;
        My.Matrix[0][2] = -Math.sin(Cvt_Param_7.y_angle);

        My.Matrix[1][0] = 0.0f;
        My.Matrix[1][1] = 1.0f;
        My.Matrix[1][2] = 0.0f;

        My.Matrix[2][0] = Math.sin(Cvt_Param_7.y_angle);
        My.Matrix[2][1] = 0.0f;
        My.Matrix[2][2] = Math.cos(Cvt_Param_7.y_angle);

        // Mz矩阵
        NNMatrix Mz = new NNMatrix(3, 3);

        Mz.Matrix[0][0] = Math.cos(Cvt_Param_7.z_angle);
        Mz.Matrix[0][1] = Math.sin(Cvt_Param_7.z_angle);
        Mz.Matrix[0][2] = 0.0f;

        Mz.Matrix[1][0] = -Math.sin(Cvt_Param_7.z_angle);
        Mz.Matrix[1][1] = Math.cos(Cvt_Param_7.z_angle);
        Mz.Matrix[1][2] = 0.0f;

        Mz.Matrix[2][0] = 0.0f;
        Mz.Matrix[2][1] = 0.0f;
        Mz.Matrix[2][2] = 1.0f;

        // 计算M矩阵
        NNMatrix M = new NNMatrix(3, 3);
        M = NNMatrix.Multiplication(Mz, My);
        M = NNMatrix.Multiplication(M, Mx);

        // 7参数矩阵变换
        tY = NNMatrix.Multiplication(Xi, K); // 缩放
        tY = NNMatrix.Multiplication(M, tY); // 旋转
        tY = NNMatrix.Add(tY, DX); // 平移

        // 返回
        X1 = tY.Matrix[0][0];
        Y1 = tY.Matrix[1][0];
        Z1 = tY.Matrix[2][0];

        xyz.setX(X1);
        xyz.setY(Y1);
        xyz.setZ(Z1);
        return 1;
    }

    protected void ParamTransReverse(SEVENPARAM _param, double x, double y, double z, GpsXYZ xyz) {
        // Xi矩阵
        NNMatrix Xi = new NNMatrix(3, 1);
        Xi.Matrix[0][0] = x;
        Xi.Matrix[1][0] = y;
        Xi.Matrix[2][0] = z;
        // DX矩阵
        NNMatrix DX = new NNMatrix(3, 1);
        DX.Matrix[0][0] = -_param.x_off;
        DX.Matrix[1][0] = -_param.y_off;
        DX.Matrix[2][0] = -_param.z_off;

        // k矩阵
        NNMatrix K = new NNMatrix(1, 1);
        K.Matrix[0][0] = 1.0 / (1.0 + _param.m);
        // Mx矩阵

        NNMatrix Mx = new NNMatrix(3, 3);
        Mx.Matrix = new double[][]{
                {
                        1.0f, 0.0f, 0.0f
                },
                {
                        0.0f, Math.cos(-_param.x_angle), Math.sin(-_param.x_angle)
                },
                {
                        0.0f, -Math.sin(-_param.x_angle), Math.cos(-_param.x_angle)
                }
        };
        // My矩阵
        NNMatrix My = new NNMatrix(3, 3);
        My.Matrix = new double[][]{
                {
                        Math.cos(-_param.y_angle), 0.0f, -Math.sin(-_param.y_angle)
                },
                {
                        0.0f, 1.0f, 0.0f
                },
                {
                        Math.sin(-_param.y_angle), 0.0f, Math.cos(-_param.y_angle)
                }
        };
        // Mz矩阵
        NNMatrix Mz = new NNMatrix(3, 3);
        Mz.Matrix = new double[][]{
                {
                        Math.cos(-_param.z_angle), Math.sin(-_param.z_angle), 0.0f
                },
                {
                        -Math.sin(-_param.z_angle), Math.cos(-_param.z_angle), 0.0f
                },
                {
                        0.0f, 0.0f, 1.0f
                }
        };

        // 计算M矩阵
        NNMatrix M = NNMatrix.Multiplication(Mx, My);
        M = NNMatrix.Multiplication(M, Mz);

        // 7参数矩阵变换
        NNMatrix tY = NNMatrix.Add(Xi, DX);       //平移
        tY = NNMatrix.Multiplication(M, tY);        //旋转
        tY = NNMatrix.Multiplication(tY, K);        //缩放

        xyz.setX(tY.Matrix[0][0]);
        xyz.setY(tY.Matrix[1][0]);
        xyz.setZ(tY.Matrix[2][0]);
    }

    private static short FourParamTransSimple(FOURPARAM para, double x, double y, GpsXYZ xy) {
        double X;
        double Y;
        double t = ((para.angle / 3600) / 180) * PI;//angle:单位为角度的秒，秒转弧度公式：1度=3600秒，PI弧度=180度

        X = para.x_off + x * para.m * Math.cos(t) - y * para.m * Math.sin(t);
        Y = para.y_off + y * para.m * Math.cos(t) + x * para.m * Math.sin(t);

        xy.setX(X);
        xy.setY(Y);

        return 1;
    }

    private static short FourParamTransSimpleRev(FOURPARAM para, double x, double y, GpsXYZ xy) {
        double t = ((para.angle / 3600) / 180) * PI;// angle:单位为角度的秒，秒转弧度公式：1度=3600秒，PI弧度=180度

        double t1 = Math.cos(t), t2 = Math.sin(t);

        double x0 = (t1 * (x - para.x_off) + t2 * (y - para.y_off)) / (para.m * (t1 * t1 + t2 * t2));
        double y0 = (t1 * (y - para.y_off) - t2 * (x - para.x_off)) / (para.m * (t1 * t1 + t2 * t2));

        xy.setX(x0);
        xy.setY(y0);

        return 1;
    }

    // 四参数转换
    // param_4 四参数结构体
    // x0,y0 待转换的平面直角坐标，单位：米
    // x，y转换后的平面直角坐标，单位：米
    // 返回值：操作是否成功，0-失败，1-成功
    private short FourParamTrans(FOURPARAM param_4, double x0, double y0, GpsXYZ xy) {
        double x = xy.getX(), y = xy.getY();

        // 原点矩阵
        NNMatrix X0 = new NNMatrix(2, 1);
        X0.Matrix[0][0] = param_4.x_off;
        X0.Matrix[1][0] = param_4.y_off;

        // 尺度差矩阵
        NNMatrix ppm = new NNMatrix(1, 1);
        ppm.Matrix[0][0] = 1 + param_4.m;

        // 角度矩阵
        double ang_Cvt = param_4.angle * PI / 180.0f / 3600.0f;
        NNMatrix ang_Matrix = new NNMatrix(2, 2);
        if (m_rev == 1) {
            ang_Matrix.Matrix[0][0] = Math.cos(ang_Cvt);
            ang_Matrix.Matrix[0][1] = Math.sin(ang_Cvt);

            ang_Matrix.Matrix[1][0] = -Math.sin(ang_Cvt);
            ang_Matrix.Matrix[1][1] = Math.cos(ang_Cvt);
        } else {
            ang_Matrix.Matrix[0][0] = Math.cos(ang_Cvt);
            ang_Matrix.Matrix[0][1] = -Math.sin(ang_Cvt);

            ang_Matrix.Matrix[1][0] = Math.sin(ang_Cvt);
            ang_Matrix.Matrix[1][1] = Math.cos(ang_Cvt);
        }

        // 入口矩阵
        NNMatrix input_Matrix = new NNMatrix(2, 1);
        input_Matrix.Matrix[0][0] = x0;
        input_Matrix.Matrix[1][0] = y0;

        // 矩阵变换
        NNMatrix out_Matrix = new NNMatrix(2, 1);
        input_Matrix = NNMatrix.Multiplication(ang_Matrix, input_Matrix);
        input_Matrix = NNMatrix.Multiplication(input_Matrix, ppm);
        out_Matrix = NNMatrix.Add(input_Matrix, X0);

        // 结果
        x = out_Matrix.Matrix[0][0];
        y = out_Matrix.Matrix[1][0];

        xy.setX(x);
        xy.setY(y);
        return 1;
    }

    private void FourParamTransReverse(FOURPARAM param_4, double x, double y, GpsXYZ xy) {
        // 原点矩阵
        NNMatrix X0 = new NNMatrix(2, 1);
        X0.Matrix[0][0] = -param_4.x_off;
        X0.Matrix[1][0] = -param_4.y_off;

        // 尺度差矩阵
        NNMatrix ppm = new NNMatrix(1, 1);
        ppm.Matrix[0][0] = 1 / (1 + param_4.m);

        // 角度矩阵
        double ang_Cvt = param_4.angle * Math.PI / 180.0f / 3600.0f;
        NNMatrix ang_Matrix = new NNMatrix(2, 2);

        ang_Matrix.Matrix = new double[][]{
                {Math.cos(ang_Cvt), Math.sin(ang_Cvt)},
                {-Math.sin(ang_Cvt), Math.cos(ang_Cvt)},
        };

        // 入口矩阵
        NNMatrix input_Matrix = new NNMatrix(2, 1);
        input_Matrix.Matrix[0][0] = x;
        input_Matrix.Matrix[1][0] = y;

        // 矩阵变换
        input_Matrix = NNMatrix.Add(input_Matrix, X0);
        input_Matrix = NNMatrix.Multiplication(input_Matrix, ppm);

        NNMatrix out_Matrix = NNMatrix.Multiplication(ang_Matrix, input_Matrix);

        // 结果
        xy.setX(out_Matrix.Matrix[0][0]);
        xy.setY(out_Matrix.Matrix[1][0]);
    }

    // 六参数转换
    // param_6 六参数结构体
    // x0 y0 待转换的平面坐标，单位：米
    // x y转换后的平面坐标，单位：米
    // 返回值：操作是否成功，0-失败，1-成功
    private short SixParamTrans(SIXPARAM param_6, double x0, double y0, GpsXYZ xy) {
        double x = xy.getX(), y = xy.getY();

        x = param_6.x0_local + (1 + param_6.m)
                * ((x0 - param_6.x0_gps) * Math.cos(param_6.angle) + (y0 - param_6.y0_gps) * Math.sin(param_6.angle));
        y = param_6.y0_local + (1 + param_6.m)
                * ((y0 - param_6.y0_gps) * Math.cos(param_6.angle) - (x0 - param_6.x0_gps) * Math.sin(param_6.angle));

        xy.setX(x);
        xy.setY(y);
        return 1;
    }

    // 将WGS-84椭球下的经纬度坐标转换为空间直角坐标
    // B 纬度，单位，弧度
    // L经度，单位：弧度
    // H高程，单位：米
    // X,Y,Z,单位：米
    // 返回值：操作是否成功，0-失败，1-成功
    private short BLH2XYZ(short ellipseType, double B, double L, double H, GpsXYZ xyz) {
        double X = xyz.getX(), Y = xyz.getY(), Z = xyz.getZ();

        // 根据椭球类型取相应的参数
        double a = 0.0, e2 = 0.0;
        switch (ellipseType) {
            // 北京54椭球
            case 1:
                a = m_bjEllipse.a;
                e2 = m_bjEllipse.e2;
                break;
            // 西安80椭球
            case 2:
                a = m_xaEllipse.a;
                e2 = m_xaEllipse.e2;
                break;
            // WGS-84椭球
            case 3:
                a = m_gpsEllipse.a;
                e2 = m_gpsEllipse.e2;
                break;
            default:
                break;
        }

        // 转换
        double N = 0.0;
        N = a / Math.sqrt(1 - e2 * Math.sin(B) * Math.sin(B));
        X = (N + H) * Math.cos(B) * Math.cos(L);
        Y = (N + H) * Math.cos(B) * Math.sin(L);
        Z = (N * (1 - e2) + H) * Math.sin(B);

        xyz.setX(X);
        xyz.setY(Y);
        xyz.setZ(Z);
        return 1;
    }

    // 空间直角坐标转换为经纬度
    // X,Y,Z,单位：米
    // B, 纬度，单位：弧度
    // L 经度，单位：弧度
    // 客户地图所采用的椭球类型，1为北京54椭球，2为西安80椭球
    // 返回值：操作是否成功，0-失败，1-成功
    private short XYZ2BLH(short ellipseType, double X, double Y, double Z, GpsXYZ xy) {
        double B = xy.getX(), L = xy.getY(), H = xy.getZ();

        // 根据椭球类型取相应的参数
        double a = 0.0, e2 = 0.0;
        switch (ellipseType) {
            // 北京54椭球
            case 1:
                a = m_bjEllipse.a;
                e2 = m_bjEllipse.e2;
                break;
            // 西安80椭球
            case 2:
                a = m_xaEllipse.a;
                e2 = m_xaEllipse.e2;
                break;
            // WGS-84椭球
            case 3:
                a = m_gpsEllipse.a;
                e2 = m_gpsEllipse.e2;
                break;
            default:
                break;
        }

        // 经度arctan(y/x)
        double fResult = Math.atan2(Y, X);
        double val = 0;
        double ang = fResult;
        while (true) {
            if (ang >= 0.0f && ang <= PI) {
                val = ang;
                break;
            }

            if (ang < 0.0f) {
                val += PI;
                break;
            }

            if (ang > PI) {
                val -= PI;
                break;
            }
        }
        fResult = val;
        L = fResult;

        // 纬度 atan((z+E*N*sin(B)) / sqrt(x*x + y*y))
        double eta = 0.0f, preLat = 0.0f, lat = 0.0f, fTmpL = 0.0f, fTmpR = 0.0f;
        do {
            preLat = lat;
            double E = e2; // 偏心率
            double N = a / Math.sqrt(1 - e2 * Math.sin(preLat) * Math.sin(preLat));
            fTmpL = Z + E * N * Math.sin(preLat);
            fTmpR = Math.sqrt(X * X + Y * Y);
            lat = Math.atan2(fTmpL, fTmpR);
            eta = Math.abs(lat - preLat);
        } while (eta > 0.000000001);
        ang = lat;
        while (true) {
            if (ang >= 0.0f && ang <= PI) {
                val = ang;
                break;
            }

            if (ang < 0.0f) {
                val += PI;
                break;
            }

            if (ang > PI) {
                val -= PI;
                break;
            }
        }
        B = val;

        double aAxis = a, bAxis = a * Math.sqrt(1 - e2);
        double ea = (Math.pow(aAxis, 2) - Math.pow(bAxis, 2)) / Math.pow(aAxis, 2);
        double eb = (Math.pow(aAxis, 2) - Math.pow(bAxis, 2)) / Math.pow(bAxis, 2);

        double c = aAxis * aAxis / bAxis;

        double ll = Math.pow(Math.cos(B), 2) * eb;
        double N = c / Math.sqrt(1 + ll);

        H = Z / Math.sin(B) - N * (1 - ea);

        xy.setX(B);
        xy.setY(L);
        xy.setZ(H);

        return 1;
    }

    void convertAngle() {
        m_sevenParam.x_angle = m_sevenParam.x_angle * PI / 648000;
        m_sevenParam.y_angle = m_sevenParam.y_angle * PI / 648000;
        m_sevenParam.z_angle = m_sevenParam.z_angle * PI / 648000;
    }

    // 取转换参数，转换参数、转换类型和椭球类型保存在xml文件中，通过该函数读取转换参数
    // 返回值：操作是否成功，0-失败，1-成功
    private short InitTransParams() {
        try {
            TransParams params = this.params.clone();

            m_ellipseType = params.ellipseType;
            m_middleLine = params.middleLine;
            m_transType = params.transType;
            m_rev = params.rev;

            // 根据转换类型取相应的转换参数
            switch (m_transType) {
                // 四参数转换
                case 1:
                    m_fourParam = params.four_param;
                    break;
                // 六参数转换
                case 2:
                    m_sixParam = params.six_param;
                    break;
                // 七参数转换
                case 3:
                    m_twoParam = params.seven_two.two_param;
                    m_sevenParam = params.seven_two.seven_param;

                    convertAngle();
                    break;
                // 七参数+四参数转换
                case 4:
                    m_fourParam = params.seven_four.four_param;
                    m_sevenParam = params.seven_four.seven_param;

                    convertAngle();
                    break;
                // 二参数，中央经线投影
                case 5:
                    m_twoParam = params.two_param;
                    break;
                // 七参数+二参数反转
                case 6:
                    m_twoParam = params.seven_two_rev.two_param;

                    m_sevenParam = params.seven_two_rev.seven_param;

                    convertAngle();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    // / <summary>
    // 逆向转变,将本地坐标转换为WGS84坐标
    // / </summary>
    // / <additionalParas name="x">本地坐标X</additionalParas>
    // / <additionalParas name="y">本地坐标Y</additionalParas>
    // / <additionalParas name="B">WGS84坐标 经度</additionalParas>
    // / <additionalParas name="L">纬度</additionalParas>
    // / <returns>是否成功</returns>
    public short CoorTransConverse(double x, double y, GpsXYZ result) {
        // 初始化转换参数
        // InitTransParams();

//        double X = 0.0, Y = 0.0, Z = 0.0;
//        double b = 0.0;
//        // 将参数都转为负值
//        // 暂时只做4参数和7参数转换
//        m_fourParam.x_off = -m_fourParam.x_off;
//        m_fourParam.y_off = -m_fourParam.y_off;
//        m_fourParam.angle = -m_fourParam.angle;
//        m_fourParam.m = -m_fourParam.m;
//        m_sevenParam.x_off = -m_sevenParam.x_off;
//        m_sevenParam.y_off = -m_sevenParam.y_off;
//        m_sevenParam.z_off = -m_sevenParam.z_off;
//        m_sevenParam.x_angle = -m_sevenParam.x_angle;
//        m_sevenParam.y_angle = -m_sevenParam.y_angle;
//        m_sevenParam.z_angle = -m_sevenParam.z_angle;
//        m_sevenParam.m = -m_sevenParam.m;

        try {
//            // 偏移量
//            X = x;
//            Y = y;
//            X = X + m_fourParam.y_off;
//            Y = Y + m_fourParam.x_off;

            if (m_transType == 1)//四参数
                return CoorTransFourConverse(x, y, result);
            else if (m_transType == 5)//二参数
                return CoorTransTwoConverse(y, x, result);

            GpsXYZ xy = new GpsXYZ();

            if (m_transType == 3) {
                x -= m_twoParam.x_off;
                y -= m_twoParam.y_off;
            } else {
                FourParamTransReverse(m_fourParam, y, x, xy);

                x = xy.getY();
                y = xy.getX();
            }

            // 高斯逆向投影
            GaussProjInvCal(m_ellipseType, m_middleLine, y, x, xy);

            GpsXYZ xyz1 = new GpsXYZ();

            //经纬度换算为空间直角坐标
            BLH2XYZ(m_ellipseType, xy.getX(), xy.getY(), 0, xyz1);

            //七参数坐标逆向转换
            ParamTransReverse(m_sevenParam, xyz1.getX(), xyz1.getY(), xyz1.getZ(), xyz1);

            //空间直角坐标转换为经纬度
            XYZ2BLH(m_ellipseType, xyz1.getX(), xyz1.getY(), xyz1.getZ(), xy);

            // 转换回来
            y = RadToDFM(xy.getY(), y);
            x = RadToDFM(xy.getX(), x);

            result.setX(DFMToRange(y, result.getX()));
            result.setY(DFMToRange(x, result.getY()));

            return 1;
        } catch (Exception e) {
            e.printStackTrace();

            return 0;
        } finally {
            InitTransParams();
        }
    }

    /**
     * 四参数+高斯投影反转，根据本地坐标获取经纬度
     *
     * @param x    本地坐标X
     * @param y    本地坐标Y
     * @param data 经纬度值，getX（）代表经度，getY（）代表纬度
     * @return 是否成功
     */
    public short CoorTransFourConverse(double x, double y, GpsXYZ data) {
        //四参数反转，本地坐标==》标准椭球坐标（如标准北京54坐标）
        FourParamTransSimpleRev(m_fourParam, y, x, data);//坐标横纵轴反转

        // 高斯逆向投影，标准椭球坐标==》经纬度坐标，单位弧度
        GaussProjInvCal(m_ellipseType, m_middleLine, data.getX(), data.getY(), data);

        // 弧度转换为度为单位的经纬度坐标
        double lat = data.getX() * 180.0f / PI, lon = data.getY() * 180.0f / PI;

        data.setX(lon);
        data.setY(lat);

        return 1;
    }

    /**
     * 中央经线二参数投影反转，根据本地坐标获取经纬度
     *
     * @param x    本地坐标X
     * @param y    本地坐标Y
     * @param data 经纬度值，getX（）代表经度，getY（）代表纬度
     * @return 是否成功
     */
    public short CoorTransTwoConverse(double x, double y, GpsXYZ data) {
        x = x - m_twoParam.y_off;
        y = y - m_twoParam.x_off;

        // 高斯逆向投影，标准椭球坐标==》经纬度坐标，单位弧度
        GaussProjInvCal(m_ellipseType, m_middleLine, x, y, data);

        // 弧度转换为度为单位的经纬度坐标
        double lat = data.getX() * 180.0f / PI, lon = data.getY() * 180.0f / PI;

        data.setX(lon);
        data.setY(lat);

        return 1;
    }

    // 坐标转换，
    // B 维度单位（度分秒 ddffmm如1203036.23）L经度，单位（度分秒 ddffmm）
    // x y为转换后的平面坐标，单位为 米
    // 返回值：操作是否成功，0-失败，1-成功
    public short CoorTrans(double B, double L, double H, GpsXYZ xySource) {
        double x = xySource.getX(), y = xySource.getY();

        double x0 = 0.0, y0 = 0.0;
        double X = 0.0, Y = 0.0, Z = 0.0, X1 = 0.0, Y1 = 0.0, Z1 = 0.0;
        double rev_L = 0.0, rev_B = 0.0;
        double b = 0.0, l = 0.0;// h=0.0;
        double X2 = 0.0, Y2 = 0.0, Z2 = 0.0;

        // 将度分秒转换为弧度
        if (m_transType < 6) {
            b = DFMToRad(B, b);
            l = DFMToRad(L, l);
        } else {
            b = B;
            l = L;
        }

        boolean isCorrect = MyApplication.getInstance().getConfigValue("CorrectTransErr", 0) > 0;

        // 根据转换类型进行分类处理
        // 1为四参数，2六参数，3七参数，4：七参数+四参数,5中央经线投影，二参数,6七参数+二参数反转
        // 椭球类型：1:北京54，2：西安80，3：WGS-84 椭球
        switch (m_transType) {
            case 1:
                // 高斯投影
                GpsXYZ xy0 = new GpsXYZ(x0, y0);
                GaosPrj(m_ellipseType, m_middleLine, b, l, xy0);

                x0 = xy0.getX();
                y0 = xy0.getY();

                GpsXYZ xy = new GpsXYZ(x, y);

                // 四参数坐标转换
                FourParamTransSimple(m_fourParam, x0, y0, xy);

                x = xy.getX();
                y = xy.getY();
                break;
            case 2:
                // 高斯投影
                GpsXYZ xy01 = new GpsXYZ(x0, y0);
                GaosPrj(m_ellipseType, m_middleLine, b, l, xy01);

                x0 = xy01.getX();
                y0 = xy01.getY();

                // 六参数坐标转换
                GpsXYZ xy1 = new GpsXYZ(x, y);
                SixParamTrans(m_sixParam, x0, y0, xy1);

                x = xy1.getX();
                y = xy1.getY();
                break;
            case 3:
                double t_B = 0.0,
                        t_L = 0.0;
                // 经纬度换算为空间直角坐标
                GpsXYZ xyz = new GpsXYZ(X, Y, Z);

                BLH2XYZ(isCorrect ? ((short) 3) : m_ellipseType, b, l, H, xyz);

                X = xyz.getX();
                Y = xyz.getY();
                Z = xyz.getZ();

                // 七参数坐标转换
                GpsXYZ xyz1 = new GpsXYZ(X1, Y1, Z1);
                SevenParamTrans_Multi(m_sevenParam, X, Y, Z, xyz1);

                X1 = xyz1.getX();
                Y1 = xyz1.getY();
                Z1 = xyz1.getZ();

                // 空间直角坐标转换为经纬度
                GpsXYZ bl = new GpsXYZ(t_B, t_L);
                XYZ2BLH(m_ellipseType, X1, Y1, Z1, bl);

                xySource.setZ(bl.getZ());

                t_B = bl.getX();
                t_L = bl.getY();

                // 高斯投影
                GpsXYZ xy11 = new GpsXYZ(x, y);
                GaosPrj(m_ellipseType, m_middleLine, t_B, t_L, xy11);
                x = xy11.getX();
                y = xy11.getY();

                x = x + m_twoParam.y_off;
                y = y + m_twoParam.x_off;
                break;
            case 4:
                t_B = 0.0;
                t_L = 0.0;
                // 经纬度换算为空间直角坐标
                GpsXYZ xyz11 = new GpsXYZ(X, Y, Z);
                BLH2XYZ(isCorrect ? ((short) 3) : m_ellipseType, b, l, H, xyz11);

                X = xyz11.getX();
                Y = xyz11.getY();
                Z = xyz11.getZ();

                // 七参数坐标转换
                GpsXYZ xyz111 = new GpsXYZ(X1, Y1, Z1);
                SevenParamTrans_Multi(m_sevenParam, X, Y, Z, xyz111);

                X1 = xyz111.getX();
                Y1 = xyz111.getY();
                Z1 = xyz111.getZ();

                // 空间直角坐标转换为经纬度
                GpsXYZ bl1 = new GpsXYZ(t_B, t_L);
                XYZ2BLH(m_ellipseType, X1, Y1, Z1, bl1);

                xySource.setZ(bl1.getZ());

                t_B = bl1.getX();
                t_L = bl1.getY();

                // 高斯投影
                GpsXYZ xy111 = new GpsXYZ(x, y);
                GaosPrj(m_ellipseType, m_middleLine, t_B, t_L, xy111);
                x = xy111.getX();
                y = xy111.getY();

                // 四参数坐标转换
                GpsXYZ xy1111 = new GpsXYZ(x, y);
                FourParamTrans(m_fourParam, x, y, xy1111);
                x = xy1111.getX();
                y = xy1111.getY();
                break;
            case 5:
                // 进行高斯投影
                GpsXYZ xy11111 = new GpsXYZ(x, y);
                GaosPrj(m_ellipseType, m_middleLine, b, l, xy11111);
                x = xy11111.getX();
                y = xy11111.getY();

                // 偏移量
                x = x + m_twoParam.y_off;
                y = y + m_twoParam.x_off;
                break;
            case 6:
                // 偏移量
                X = l;
                Y = b;
                X = X + m_twoParam.x_off;
                Y = Y + m_twoParam.y_off;
                // 高斯逆向投影
                GpsXYZ bl11 = new GpsXYZ(rev_B, rev_L);
                GaussProjInvCal(m_ellipseType, m_middleLine, Y, X, bl11);
                rev_B = bl11.getX();
                rev_L = bl11.getY();

                x = RadToDFM(rev_B, x);
                y = RadToDFM(rev_L, y);

                // 经纬度转化为直角坐标
                X1 = Y1 = Z1 = 0.0f;

                GpsXYZ xyz1111 = new GpsXYZ(X1, Y1, Z1);
                BLH2XYZ(m_ellipseType, rev_B, rev_L, 0, xyz1111);
                X1 = xyz1111.getX();
                Y1 = xyz1111.getY();
                Z1 = xyz1111.getZ();

                X2 = Y2 = Z2 = 0.0f;

                GpsXYZ xyz2 = new GpsXYZ(X2, Y2, Z2);
                SevenParamTrans_Multi(m_sevenParam, X1, Y1, Z1, xyz2);
                X2 = xyz2.getX();
                Y2 = xyz2.getY();
                Z2 = xyz2.getZ();

                GpsXYZ bl111 = new GpsXYZ(rev_B, rev_L);
                XYZ2BLH(m_ellipseType, X2, Y2, Z2, bl111);
                rev_B = bl111.getX();
                rev_L = bl111.getY();

                // 转换回来
                x = RadToDFM(rev_L, x);
                y = RadToDFM(rev_B, y);
                break;
            default:
                break;
        }

        xySource.setX(x);
        xySource.setY(y);
        return 1;
    }

    // DDFFMM转换为小数格式
    private double DFMToRange(double dfm, double range) {
        int d = (int) dfm / 10000;
        double f = ((int) (dfm - d * 10000) / 100) / 60.0;
        double m = (dfm - d * 10000 - f * 6000) / 3600.0;

        range = d + f + m;

        return range;
    }

    // 小数格式转换为DDFFMM
    public double RangeToDFM(double range, double dfm) {
        int d = ((int) range);
        int f = (int) ((range - d) * 60);
        double m = ((range - d) * 60 - f) * 60;
        dfm = d * 10000 + f * 100 + m;

        return dfm;
    }

    /**
     * WGS84经纬度转WGS84 WebMercator投影
     *
     * @param lon 经度
     * @param lat 纬度
     * @param mxy WebMercator投影坐标
     */
    public static void LatLonToMeters(double lon, double lat, GpsXYZ mxy) {
        double mx = mxy.getX(), my = mxy.getY();

        // lon:经度
        // lat：纬度
        double originShift = 2 * Math.PI * 6378137 / 2.0;

        mx = lon * originShift / 180.0;

        my = Math.log(Math.tan((90 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0);
        my = my * originShift / 180.0;

        mxy.setX(mx);
        mxy.setY(my);
    }

    /**
     * WebMercator 转为 WGS84 经纬度
     *
     * @param x  WebMercator X坐标
     * @param y  WebMercator Y坐标
     * @param nt WGS84经纬度坐标
     */
    public static void LatLonToMetersConverse(double x, double y, GpsXYZ nt) {
        double lon = nt.getX(), lat = nt.getY();

        lon = (x / 20037508.342787) * 180;
        lat = (y / 20037508.342787) * 180;

        lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);

        nt.setX(lon);
        nt.setY(lat);
    }
}