package com.android.business.entity;
import java.io.Serializable;

public class ContactInfo implements Serializable {
    private static final long serialVersionUID = 1l;
    private String domid;
    private String userid;
    private String name;
    private String telphone;
    private String type;
    private String lasterDialTime;
    public String getDomid() {
        return domid;
    }
    public String getLasterDialTime() {
        return lasterDialTime;
    }
    public void setLasterDialTime(String lasterDialTime) {
        this.lasterDialTime = lasterDialTime;
    }
    public void setDomid(String domid) {
        this.domid = domid;
    }
    public String getUserid() {
        return userid;
    }
    public void setUserid(String userid) {
        this.userid = userid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTelphone() {
        return telphone;
    }
    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
