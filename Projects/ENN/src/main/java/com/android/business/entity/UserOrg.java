package com.android.business.entity;

import com.lidroid.xutils.db.annotation.Table;

/**
 * 描述：组织信息 作者： 25845
 */
@Table(name = "UserOrg")
public class UserOrg {

    private String id;
    private String domId;
    private String parentId;
    private String parentdomid;
    private String userOrgName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserOrgName() {
        return userOrgName;
    }

    public void setUserOrgName(String userOrgName) {
        this.userOrgName = userOrgName;
    }

    public UserOrg(String name) {
        userOrgName = name;
    }

    public UserOrg() {}

//    public int getOnlineCount() {
//        int onlineCount = 0;
//        for (ContactsUserInfo info : users) {
//            if (info.getStatus() != ContactsUserInfo.OFFLINE) {
//                onlineCount++;
//            }
//        }
//        return onlineCount;
//    }

//    public int getCount() {
//        return users.size();
//    }

//    public boolean isEmpty() {
//        return users.isEmpty();
//    }

//    public void addUserInfo(ContactsUserInfo info) {
//        users.add(info);
//    }
//
//    public void addUserInfo(List<ContactsUserInfo> list) {
//        users.clear();
//        users.addAll(list);
//    }

//    public List<ContactsUserInfo> getUsers() {
//        return users;
//    }

    public String getDomId() {
        return domId;
    }

    public void setDomId(String domId) {
        this.domId = domId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentdomid() {
        return parentdomid;
    }

    public void setParentdomid(String parentdomid) {
        this.parentdomid = parentdomid;
    }

    @Override
    public String toString() {
        return "UserOrg{" +
                "id='" + id + '\'' +
                ", domId='" + domId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", parentdomid='" + parentdomid + '\'' +
                ", userOrgName='" + userOrgName + '\'' +
                '}';
    }
}
