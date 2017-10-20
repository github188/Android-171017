package com.repair.zhoushan.module.devicecare.careoverview;

import com.google.gson.annotations.SerializedName;

class UserInfo {

    @SerializedName("用户ID")
    public String userID;

    @SerializedName("机构ID")
    public String deptID;

    @SerializedName("角色信息")
    public String roleInfo;

    @SerializedName("用户名")
    public String userName;
}