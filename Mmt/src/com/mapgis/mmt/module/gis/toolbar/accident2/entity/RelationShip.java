package com.mapgis.mmt.module.gis.toolbar.accident2.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Comclay on 2017/5/17.
 * 设备相关附属属性
 */
public class RelationShip implements Parcelable {
    public String cardinality;
    public int id;
    public boolean isComposite;
    public String keyField;
    public String keyFieldInRelationshipTable;
    public String name;
    public String relatedTableId;
    public String relatedTableName;
    public String relationshipTableId;
    // 关联属性表名
    public String relationshipTableName;
    public String role;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.cardinality);
        dest.writeInt(this.id);
        dest.writeByte(this.isComposite ? (byte) 1 : (byte) 0);
        dest.writeString(this.keyField);
        dest.writeString(this.keyFieldInRelationshipTable);
        dest.writeString(this.name);
        dest.writeString(this.relatedTableId);
        dest.writeString(this.relatedTableName);
        dest.writeString(this.relationshipTableId);
        dest.writeString(this.relationshipTableName);
        dest.writeString(this.role);
    }

    public RelationShip() {
    }

    protected RelationShip(Parcel in) {
        this.cardinality = in.readString();
        this.id = in.readInt();
        this.isComposite = in.readByte() != 0;
        this.keyField = in.readString();
        this.keyFieldInRelationshipTable = in.readString();
        this.name = in.readString();
        this.relatedTableId = in.readString();
        this.relatedTableName = in.readString();
        this.relationshipTableId = in.readString();
        this.relationshipTableName = in.readString();
        this.role = in.readString();
    }

    public static final Parcelable.Creator<RelationShip> CREATOR = new Parcelable.Creator<RelationShip>() {
        @Override
        public RelationShip createFromParcel(Parcel source) {
            return new RelationShip(source);
        }

        @Override
        public RelationShip[] newArray(int size) {
            return new RelationShip[size];
        }
    };
}
