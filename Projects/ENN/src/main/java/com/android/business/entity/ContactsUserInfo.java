package com.android.business.entity;

import com.lidroid.xutils.db.annotation.Table;

@Table(name = "UserInfo")
public class ContactsUserInfo extends DataInfo {

    /**
     * 在线
     */
    public final static int ONLINE = 1;
    /**
     * 离线
     */
    public final static int OFFLINE = 0;
    /**
     * 繁忙
     */
    public final static int BUSY = 2;

    private String id;
    private String loginName;
    // 0离线 1在线 2繁忙
    private int status;
    private String userName;
    private String mail;
    private String phone;
    // 话机注册密码
    private String phonePsw;
    private String userMeno;
    private String userTheme;
    private String userCode;
    private String userMAC;
    private String userIP;

    private String orgCode;
    private String orgDomId;
    
    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgDomId() {
        return orgDomId;
    }

    public void setOrgDomId(String orgDomId) {
        this.orgDomId = orgDomId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhonePsw() {
        return phonePsw;
    }

    public void setPhonePsw(String phonePsw) {
        this.phonePsw = phonePsw;
    }

    public String getUserMeno() {
        return userMeno;
    }

    public void setUserMeno(String userMeno) {
        this.userMeno = userMeno;
    }

    public String getUserTheme() {
        return userTheme;
    }

    public void setUserTheme(String userTheme) {
        this.userTheme = userTheme;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserMAC() {
        return userMAC;
    }

    public void setUserMAC(String userMAC) {
        this.userMAC = userMAC;
    }

    public String getUserIP() {
        return userIP;
    }

    public void setUserIP(String userIP) {
        this.userIP = userIP;
    }

    @Override
    public String toString() {
        return "ContactsUserInfo{" +
                "id='" + id + '\'' +
                ", loginName='" + loginName + '\'' +
                ", status=" + status +
                ", userName='" + userName + '\'' +
                ", mail='" + mail + '\'' +
                ", phone='" + phone + '\'' +
                ", phonePsw='" + phonePsw + '\'' +
                ", userMeno='" + userMeno + '\'' +
                ", userTheme='" + userTheme + '\'' +
                ", userCode='" + userCode + '\'' +
                ", userMAC='" + userMAC + '\'' +
                ", userIP='" + userIP + '\'' +
                ", orgCode='" + orgCode + '\'' +
                ", orgDomId='" + orgDomId + '\'' +
                '}';
    }
}
