package com.patrol.module.posandpath2.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedHashMap;

/**
 * Created by Comclay on 2016/10/24.
 * 用户基本信息
 */

public class PersonInfo implements Parcelable {
    public String Distance; // 距离
    public String IsOline;  // 是否在线
    public String LeaveState;
    public String LeaveTypeID;
    public String PHONE_NUMBER;
    public String Ptime;
    public String Role;
    public String USERID;
    public String UserImg;
    public String name;  // 姓名
    public String partment;  // 部门

    public LinkedHashMap<String,String> toMapData(){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("用户名",this.name);
        map.put("用户编号",this.USERID);
        map.put("电话",this.PHONE_NUMBER);
        if ("1".equals(this.IsOline)){
            map.put("状态","在线");
        }else {
            map.put("状态", "离线");
        }
        map.put("部门",this.partment);
        map.put("角色",this.Role);
//        map.put("图像",this.UserImg);
        map.put("距离",this.Distance);
        return map;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.Distance);
        dest.writeString(this.IsOline);
        dest.writeString(this.LeaveState);
        dest.writeString(this.LeaveTypeID);
        dest.writeString(this.PHONE_NUMBER);
        dest.writeString(this.Ptime);
        dest.writeString(this.Role);
        dest.writeString(this.USERID);
        dest.writeString(this.UserImg);
        dest.writeString(this.name);
        dest.writeString(this.partment);
    }

    public PersonInfo() {
    }

    protected PersonInfo(Parcel in) {
        this.Distance = in.readString();
        this.IsOline = in.readString();
        this.LeaveState = in.readString();
        this.LeaveTypeID = in.readString();
        this.PHONE_NUMBER = in.readString();
        this.Ptime = in.readString();
        this.Role = in.readString();
        this.USERID = in.readString();
        this.UserImg = in.readString();
        this.name = in.readString();
        this.partment = in.readString();
    }

    public static final Creator<PersonInfo> CREATOR = new Creator<PersonInfo>() {
        @Override
        public PersonInfo createFromParcel(Parcel source) {
            return new PersonInfo(source);
        }

        @Override
        public PersonInfo[] newArray(int size) {
            return new PersonInfo[size];
        }
    };

    @Override
    public String toString() {
        return "PersonInfo{" +
                ", IsOline='" + IsOline + '\'' +
                ", Role='" + Role + '\'' +
                ", USERID='" + USERID + '\'' +
                ", name='" + name + '\'' +
                ", partment='" + partment + '\'' +
                '}';
    }
}
