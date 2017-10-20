package com.repair.gisdatagather.enn.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liuyunfan on 2015/12/25.
 */
public class GISDeviceSetBean implements Parcelable {
  //  @SerializedName("图层名称")
    public String alias;

   // @SerializedName("反馈表名")
    public String table;

   // @SerializedName("字段集")
    public String fileds;

   // @SerializedName("GIS图层")
    public String layerName;

   // @SerializedName("图层类型")
    public int layerType;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(alias);
        out.writeString(table);
        out.writeString(fileds);
        out.writeString(layerName);
        out.writeInt(layerType);
    }

    public static final Parcelable.Creator<GISDeviceSetBean> CREATOR = new Parcelable.Creator<GISDeviceSetBean>() {
        @Override
        public GISDeviceSetBean createFromParcel(Parcel in) {
            GISDeviceSetBean gisDeviceSetBean = new GISDeviceSetBean();
            gisDeviceSetBean.alias = in.readString();
            gisDeviceSetBean.table = in.readString();
            gisDeviceSetBean.fileds = in.readString();
            gisDeviceSetBean.layerName = in.readString();
            gisDeviceSetBean.layerType = in.readInt();
            return gisDeviceSetBean;
        }

        @Override
        public GISDeviceSetBean[] newArray(int size) {
            return new GISDeviceSetBean[size];
        }
    };

}
