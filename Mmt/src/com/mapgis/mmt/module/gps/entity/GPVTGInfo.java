package com.mapgis.mmt.module.gps.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.mapgis.mmt.common.util.BaseClassUtil;

/**
 * Created by Comclay on 2017/2/24.
 */

public class GPVTGInfo implements Parcelable {

    // 以真北为参考基准的地面航向（000~359度，前面的0也将被传输）
    private String mRealNorthDirt;
//    以磁北为参考基准的地面航向（000~359度，前面的0也将被传输）
    private String mMagnNorthDirt;
//    地面速率（000.0~999.9节，前面的0也将被传输）
    private float mSpeedKnot;
//    地面速率（0000.0~1851.8公里/小时，前面的0也将被传输）
    private float mSpeedKilo;
//    模式指示（仅NMEA0183 3.00版本输出，A=自主定位，D=差分，E=估算，N=数据无效）
    private String mModule;

    public GPVTGInfo() {
    }

    /**
     * 解析字符串中的数据
     * @param str $GPVTG,,T,,M,0.841,N,1.557,K,A*28
     */
    public void initFromStr(String str){
        String[] split = str.split(",");
        // 真北
        this.mRealNorthDirt = split[1];
        this.mMagnNorthDirt = split[3];
        if (!BaseClassUtil.isNullOrEmptyString(split[5])){
            this.mSpeedKnot = Float.valueOf(split[5]);
        }
        if (!BaseClassUtil.isNullOrEmptyString(split[7])){
            this.mSpeedKilo = Float.valueOf(split[7]);
        }
    }

    public String getmRealNorthDirt() {
        return mRealNorthDirt;
    }

    public void setmRealNorthDirt(String mRealNorthDirt) {
        this.mRealNorthDirt = mRealNorthDirt;
    }

    public String getmMagnNorthDirt() {
        return mMagnNorthDirt;
    }

    public void setmMagnNorthDirt(String mMagnNorthDirt) {
        this.mMagnNorthDirt = mMagnNorthDirt;
    }

    public float getmSpeedKnot() {
        return mSpeedKnot;
    }

    public void setmSpeedKnot(float mSpeedKnot) {
        this.mSpeedKnot = mSpeedKnot;
    }

    public float getmSpeedKilo() {
        return mSpeedKilo;
    }

    public void setmSpeedKilo(float mSpeedKilo) {
        this.mSpeedKilo = mSpeedKilo;
    }

    public String getmModule() {
        return mModule;
    }

    public void setmModule(String mModule) {
        this.mModule = mModule;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mRealNorthDirt);
        dest.writeString(this.mMagnNorthDirt);
        dest.writeDouble(this.mSpeedKnot);
        dest.writeDouble(this.mSpeedKilo);
        dest.writeString(this.mModule);
    }

    protected GPVTGInfo(Parcel in) {
        this.mRealNorthDirt = in.readString();
        this.mMagnNorthDirt = in.readString();
        this.mSpeedKnot = in.readFloat();
        this.mSpeedKilo = in.readFloat();
        this.mModule = in.readString();
    }

    public static final Parcelable.Creator<GPVTGInfo> CREATOR = new Parcelable.Creator<GPVTGInfo>() {
        @Override
        public GPVTGInfo createFromParcel(Parcel source) {
            return new GPVTGInfo(source);
        }

        @Override
        public GPVTGInfo[] newArray(int size) {
            return new GPVTGInfo[size];
        }
    };
}
