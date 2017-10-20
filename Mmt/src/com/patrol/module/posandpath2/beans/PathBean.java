package com.patrol.module.posandpath2.beans;

import com.mapgis.mmt.common.util.BaseClassUtil;

import java.util.ArrayList;
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

    /**
     * 数据格式:2016-10-21 11:06:31,148167.155,303144.555,71%,42%,19%,0.0,0.0_Random_0_0,1
     * @param name 用户名
     * @param pointInfo 字符串形式的数据
     * @return PointInfo形式的数据
     */
    public static PointInfo stringToPointInfo(String name,String pointInfo) {
        String[] infos = pointInfo.split(",");

        String[] equipmentInfo = infos[7].split("_");

        PointInfo info = new PointInfo(
                name,
                infos[8],
                infos[0],
                infos[1] + "," + infos[2],
                infos[6],
                equipmentInfo[1],
                equipmentInfo[2],
                equipmentInfo[3],
                equipmentInfo[0],
                infos[3],
                infos[4],
                infos[5]
        );
        return info;
    }

    /**
     * 将字符串数据转化为PointInfo集合的数据
     * @param name
     * @param pointInfos
     * @return
     */
    public static ArrayList<PointInfo> stringToPointList(String name,String pointInfos) {
        if (BaseClassUtil.isNullOrEmptyString(pointInfos)) return null;

        ArrayList<PointInfo> pointList = new ArrayList<>();
        String[] points = pointInfos.split("\\|");

        for (int i = 0; i < points.length; i++) {
            pointList.add(stringToPointInfo(name,points[i]));
        }
        return pointList;
    }
}
