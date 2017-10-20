package com.repair.beihai.poj.hbpoj.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liuyunfan on 2016/8/17.
 */
public class RoleUserInfo {
    @SerializedName("机构ID")
    public String groupID;

    @SerializedName("用户ID")
    public String userID;

    @SerializedName("用户名")
    public String userName;

    @SerializedName("角色信息")
    public String roleName;

}
