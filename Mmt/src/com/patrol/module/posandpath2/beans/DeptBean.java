package com.patrol.module.posandpath2.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: zhoukang
 * Date: 2016-03-15
 * Time: 15:43
 * <p/>
 * trunk:
 * 描述部门信息的类
 */
public class DeptBean implements Parcelable,Comparable<DeptBean>{
    // 部门Id
    public String DeptID;
    // 部门名称
    public String DeptName;

    public DeptBean(String deptID, String deptName) {
        DeptID = deptID;
        DeptName = deptName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.DeptID);
        dest.writeString(this.DeptName);
    }

    protected DeptBean(Parcel in) {
        this.DeptID = in.readString();
        this.DeptName = in.readString();
    }

    public static final Creator<DeptBean> CREATOR = new Creator<DeptBean>() {
        @Override
        public DeptBean createFromParcel(Parcel source) {
            return new DeptBean(source);
        }

        @Override
        public DeptBean[] newArray(int size) {
            return new DeptBean[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeptBean deptBean = (DeptBean) o;

        if (DeptID != null ? !DeptID.equals(deptBean.DeptID) : deptBean.DeptID != null)
            return false;
        return DeptName != null ? DeptName.equals(deptBean.DeptName) : deptBean.DeptName == null;

    }

    @Override
    public int hashCode() {
        int result = DeptID != null ? DeptID.hashCode() : 0;
        result = 31 * result + (DeptName != null ? DeptName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DeptBean{" +
                "DeptID='" + DeptID + '\'' +
                ", DeptName='" + DeptName + '\'' +
                '}';
    }

    @Override
    public int compareTo(DeptBean another) {
        return another.DeptID.compareTo(this.DeptID);
    }
}
