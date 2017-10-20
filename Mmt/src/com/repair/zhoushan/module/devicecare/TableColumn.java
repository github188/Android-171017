package com.repair.zhoushan.module.devicecare;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * 设备养护动态列表字段结构
 */
public class TableColumn implements Parcelable {

    public TableColumn() {
        FieldName = "";
        FieldValue = "";
        FontSize = "";
        FontColor = "";
        Weight = 0;
    }

    public TableColumn(String fieldName, String fieldValue, String fontSize, String fontColor, int weight) {
        this.FieldName = fieldName;
        this.FieldValue = fieldValue;
        this.FontSize = fontSize;
        this.FontColor = fontColor;
        this.Weight = weight;
    }

    /**
     * 字段名
     */
    public String FieldName;
    /**
     * 字段值
     */
    public String FieldValue;
    /**
     * 字体大小
     */
    public String FontSize;
    /**
     * 字体颜色
     */
    public String FontColor;
    /**
     * 列宽权重(手持)
     */
    public int Weight;

    public static final Creator<TableColumn> CREATOR = new Creator<TableColumn>() {
        @Override
        public TableColumn createFromParcel(Parcel in) {
            return new TableColumn(in);
        }

        @Override
        public TableColumn[] newArray(int size) {
            return new TableColumn[size];
        }
    };

    private TableColumn(Parcel in) {
        FieldName = in.readString();
        FieldValue = in.readString();
        FontSize = in.readString();
        FontColor = in.readString();
        Weight = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FieldName);
        dest.writeString(FieldValue);
        dest.writeString(FontSize);
        dest.writeString(FontColor);
        dest.writeInt(Weight);
    }
}