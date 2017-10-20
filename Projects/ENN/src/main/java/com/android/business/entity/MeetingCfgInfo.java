package com.android.business.entity;
import java.util.ArrayList;
import java.util.List;

public class MeetingCfgInfo {
    private int meetid;
    private String domid;
    private String code;
    private String name;
    private String theme;
    private String pwd;
    private int num;
    private int userid;
    private String username;
    private String status;
    private String type;
    private String method;
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    private List<MeetingUserInfo> meetingUserInfoList = new ArrayList<MeetingUserInfo>();
    public int getMeetid() {
        return meetid;
    }
    public void setMeetid(int meetid) {
        this.meetid = meetid;
    }
    public String getDomid() {
        return domid;
    }
    public void setDomid(String domid) {
        this.domid = domid;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTheme() {
        return theme;
    }
    public void setTheme(String theme) {
        this.theme = theme;
    }
    public String getPwd() {
        return pwd;
    }
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    public int getNum() {
        return num;
    }
    public void setNum(int num) {
        this.num = num;
    }
    public int getUserid() {
        return userid;
    }
    public void setUserid(int userid) {
        this.userid = userid;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public List<MeetingUserInfo> getMeetingUserInfoList() {
        return meetingUserInfoList;
    }
    public void setMeetingUserInfoList(List<MeetingUserInfo> meetingUserInfoList) {
        this.meetingUserInfoList = meetingUserInfoList;
    }
    public void addMeetingUserInfo(MeetingUserInfo info) {
        boolean existFlag = false;
        for (int i = 0; i < meetingUserInfoList.size(); i++) {
            if (meetingUserInfoList.get(i).getUserid() == info.getUserid()) {
                return;
            }
        }
        this.meetingUserInfoList.add(info);
    }
}
