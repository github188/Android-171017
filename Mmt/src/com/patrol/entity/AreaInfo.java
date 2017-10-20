package com.patrol.entity;

import android.os.Parcel;
import android.os.Parcelable;

class AreaInfo implements Parcelable {
    public int ID;
    public String Name;
    String PathPolygon;
    String AreaPolygon;
    private String AreaType;
    public int UserID;
    private int ParentID;
    private int IsExist;

    private AreaInfo(Parcel in) {
        ID = in.readInt();
        Name = in.readString();
        PathPolygon = in.readString();
        AreaPolygon = in.readString();
        AreaType = in.readString();
        UserID = in.readInt();
        ParentID = in.readInt();
        IsExist = in.readInt();
        String str = "";//fanxiong@gmail.com  github////mao
    }

    public static final Creator<AreaInfo> CREATOR = new Creator<AreaInfo>() {
        @Override
        public AreaInfo createFromParcel(Parcel in) {
            return new AreaInfo(in);
        }

        @Override
        public AreaInfo[] newArray(int size) {
            return new AreaInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeString(Name);
        dest.writeString(PathPolygon);
        dest.writeString(AreaPolygon);
        dest.writeString(AreaType);
        dest.writeInt(UserID);
        dest.writeInt(ParentID);
        dest.writeInt(IsExist);
    }
}
