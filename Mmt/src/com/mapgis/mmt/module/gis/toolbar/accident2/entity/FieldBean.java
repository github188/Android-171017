package com.mapgis.mmt.module.gis.toolbar.accident2.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Comclay on 2017/3/1.
 * 字段属性说明实体
 */

class FieldBean implements Parcelable {
    // 名称
    public String name;
    // 别名
    public String alias;
    // 是否可编辑
    public boolean editable;
    // 是否可为空
    public boolean nullable;
    // 是否用户可见
    public boolean visible;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.alias);
        dest.writeByte(this.editable ? (byte) 1 : (byte) 0);
        dest.writeByte(this.nullable ? (byte) 1 : (byte) 0);
        dest.writeByte(this.visible ? (byte) 1 : (byte) 0);
    }

    public FieldBean() {
    }

    protected FieldBean(Parcel in) {
        this.name = in.readString();
        this.alias = in.readString();
        this.editable = in.readByte() != 0;
        this.nullable = in.readByte() != 0;
        this.visible = in.readByte() != 0;
    }

    public static final Parcelable.Creator<FieldBean> CREATOR = new Parcelable.Creator<FieldBean>() {
        @Override
        public FieldBean createFromParcel(Parcel source) {
            return new FieldBean(source);
        }

        @Override
        public FieldBean[] newArray(int size) {
            return new FieldBean[size];
        }
    };
}
