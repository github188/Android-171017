package com.mapgis.mmt.module.gps.entity;

import android.location.Location;
import android.text.TextUtils;

import com.mapgis.mmt.common.util.Convert;

import java.util.Date;

public class GPGGAInfo {
    public UtcTime utcTime = new UtcTime();
    public BLHInfo BLH = new BLHInfo();

    public int stateSloution;
    public char flagLongitude;
    public char flagLatitude;
    public double geoidalUndulation;
    public int satellitesNumber;
    public double HDOP;
    public String baseID;
    public double Age;
    public boolean received;
    public double nTimeoutCount;

    public float hRMS = 0;
    public float vRMS = 0;
    public float speed = 0;

    public boolean isAvailable() {
        return BLH != null && BLH.Latitude > 0 && BLH.Longitude > 0 && hRMS < 100;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * 获取差分状态
     *
     * @return 差分状态
     */
    public String getSignalTag() {
        String strSolutionState;

        switch (stateSloution) {
            case 0:
                strSolutionState = "无效解";
                break;
            case 1:
                strSolutionState = "单点解";
                break;
            case 2:
                strSolutionState = "差分解";
                break;
            case 4:
                strSolutionState = "固定解";
                break;
            case 5:
                strSolutionState = "浮点解";
                break;
            case 7:
                strSolutionState = "基站";
                break;

            default:
                strSolutionState = "未知";
                break;
        }

        return strSolutionState;

    }

    /**
     * 解析GGA语句$GPGGA,070217.00,3000.72472495,N,12035.10801604,E,1,15,1.2,20.181,M,8.772,M,,*68
     *
     * @param data 标准的NMEA语句
     */
    public void GetGPGGA(String data) {
        try {
            int ArrayMaxSize = 18;
            String[] strArray = new String[ArrayMaxSize];

            // 分离字符串并遍历，生成字符串数组;
            int nIndex = 0;
            for (String subString : data.split(","))//data.split(",|\\*")) // 使用正则表达式;
            {
                if (nIndex > ArrayMaxSize) {
                    break;
                }

                strArray[nIndex] = subString;

                nIndex++;
            }

            String strTem;

            // 1.时间;
            strTem = strArray[1];

            if (strTem.length() < 6) {
                this.utcTime.Hour = 0;
                this.utcTime.Minute = 0;
                this.utcTime.Second = 0;

                return;
            } else {
                this.utcTime.Hour = GetInt(strTem.substring(0, 2)) + 8;// utc时间转中国时间
                this.utcTime.Minute = GetInt(strTem.substring(2, 4));
                this.utcTime.Second = GetDouble(strTem.substring(4, 6));
            }

            // 2.解算状态;
            strTem = strArray[6];
            if (strTem == null || strTem.isEmpty()) {
                this.stateSloution = 0;
            } else {
                this.stateSloution = GetInt(strTem);
            }

            // 3.经纬度坐标;
            strTem = strArray[2];

            if (strTem.isEmpty() || strTem.length() < 2) {
                this.BLH.Latitude = 0;

                return;
            } else {
                this.BLH.Latitude = GetInt(strTem.substring(0, 2)) + GetDouble(strTem.substring(2)) / 60.0;
            }

            strTem = strArray[4];

            if (strTem.isEmpty() || strTem.length() < 2) {
                this.BLH.Longitude = 0;

                return;
            } else {
                this.BLH.Longitude = GetInt(strTem.substring(0, 3)) + GetDouble(strTem.substring(3)) / 60.0;
            }

            // 增加南北标识;
            strTem = strArray[3];

            if (!strTem.isEmpty()) {
                this.flagLatitude = strTem.charAt(0);
            }

            if (strTem.equalsIgnoreCase("S")) {
                this.BLH.Latitude = this.BLH.Latitude * -1.0;
            }

            strTem = strArray[5];

            if (!strTem.isEmpty()) {
                this.flagLongitude = strTem.charAt(0);
            }

            if (strTem.equalsIgnoreCase("W")) {
                this.BLH.Longitude = this.BLH.Longitude * -1.0;
            }

            // 高程异常和椭球高;
            strTem = strArray[11];

            this.geoidalUndulation = GetDouble(strTem);

            strTem = strArray[9];

            this.BLH.Altitude = GetDouble(strTem) + this.geoidalUndulation;

            // 使用卫星数;
            strTem = strArray[7];

            this.satellitesNumber = GetInt(strTem);

            // HDOP
            strTem = strArray[8];

            this.HDOP = GetDouble(strTem);

            // 5. 基准站ID
            strTem = strArray[14];

            this.baseID = strTem;

            // 基站ID如果和校验项之间无逗号分隔
            int nTmp = this.baseID.indexOf('*');

            if (nTmp != -1) {
                String[] arr = this.baseID.split("\\*");
                this.baseID = arr[0];
            }

            // 6.差分类型Age;
            strTem = strArray[13];

            this.Age = GetDouble(strTem);

            this.received = true;
            this.nTimeoutCount = 0;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取水平和垂直误差
     *
     * @param data 输入信息
     */
    public void GetGPGST(String data) {
        try {
            String[] args = data.split(",|\\*");

            if (args.length < 9) {
                return;
            }

            double hrms = Convert.FormatDouble(Math.sqrt(Math.pow(Double.valueOf(args[6]), 2) + Math.pow(Double.valueOf(args[7]), 2)),".0000");

            this.hRMS = (float)hrms;
            this.vRMS = Float.valueOf(args[8]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    int GetInt(String value) {
        try {
            if (TextUtils.isEmpty(value)) {
                return 0;
            }

            return Integer.valueOf(value);
        } catch (Exception ex) {
            ex.printStackTrace();

            return 0;
        }
    }

    double GetDouble(String value) {
        try {
            if (TextUtils.isEmpty(value)) {
                return 0;
            }

            return Double.valueOf(value);
        } catch (Exception ex) {
            ex.printStackTrace();

            return 0;
        }
    }

    public Location getLocation() {
        Location gpsLocation = new Location(getSignalTag());

        gpsLocation.setAccuracy(this.hRMS > 0 ? this.hRMS : (float) this.HDOP);//(info.hRMS);
        gpsLocation.setAltitude(this.BLH.Altitude);

        gpsLocation.setLatitude(this.BLH.Latitude);
        gpsLocation.setLongitude(this.BLH.Longitude);

        gpsLocation.setSpeed(this.speed);
        gpsLocation.setTime(new Date().getTime());

        return gpsLocation;
    }
}