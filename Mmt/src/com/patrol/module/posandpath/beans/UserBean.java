package com.patrol.module.posandpath.beans;

import java.util.ArrayList;

/**
 * User: zhoukang
 * Date: 2016-03-15
 * Time: 15:55
 * <p/>
 * trunk:
 * 描述用户的类
 */
public class UserBean {
    public ArrayList<BodyInfo> Ppoint;
    public ResultInfo rntinfo;

    public class BodyInfo{
        public UserInfo Perinfo;
        public Point point;
    }

    public class Point{
        public String Position;   // "187552.426,351425.41"
        public String time;       //"2016/1/29 17:54:22"
    }

    public class UserInfo{
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
    }


    class ResultInfo{
        public boolean IsSuccess;
        public String Msg;
    }
}
