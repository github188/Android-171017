package com.patrol.module.posandpath2.beans;


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
    public ArrayList<UserInfo> Ppoint;
    public ResultInfo rntinfo;

    class ResultInfo{
        public boolean IsSuccess;
        public String Msg;
    }
}
