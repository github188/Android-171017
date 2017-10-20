package com.patrol.module.posandpath.beans;

import java.util.List;

/**
 * User: zhoukang
 * Date: 2016-03-22
 * Time: 15:49
 * <p/>
 * 轨迹业务bean
 */
public class PathBean {
    public List<Path> Ppoint;
    public Result rntinfo;

    public class Path {
        public String PerID;
        public String PerName;
        public String Ppoint;
    }

    public class Result {
        public String IsSuccess;
        public String Msg;
    }
}
