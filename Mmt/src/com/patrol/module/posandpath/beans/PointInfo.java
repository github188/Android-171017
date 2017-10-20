package com.patrol.module.posandpath.beans;

/**
 * User: zhoukang
 * Date: 2016-03-22
 * Time: 16:09
 * <p/>
 * trunk:
 */
public class PointInfo {

    // 用户实时信息
    private String name; // 姓名
    private String dir; //　轨迹方向
    private String time; // 时间
    private String position; // 位置 0,0
    private String accr;  // 定位精度
    private String res;  // 定位来源
    private String lat;   // 经度
    private String lag;   // 纬度
    private String speed; // 速度

    // 手持使用状况
    private String cpu;  // cpu
    private String category; // 电池剩余
    private String memory;  // 内存


    public PointInfo() {
    }

    public PointInfo(String name, String dir, String time, String position, String accr, String res, String lat, String lag, String speed, String cpu, String category, String memory) {
        this.name = name;
        this.dir = dir;
        this.time = time;
        this.position = position;
        this.accr = accr;
        this.res = res;
        this.lat = lat;
        this.lag = lag;
        this.speed = speed;
        this.cpu = cpu;
        this.category = category;
        this.memory = memory;
    }

    public String getName() {
        return name;
    }

    public String getDir() {
        return dir;
    }

    public String getTime() {
        return time;
    }

    public String getAccr() {
        return accr;
    }

    public String getPosition() {
        return position;
    }

    public String getRes() {
        return res;
    }

    public String getLat() {
        return lat;
    }

    public String getLag() {
        return lag;
    }

    public String getSpeed() {
        return speed;
    }

    public String getCpu() {
        return cpu;
    }

    public String getCategory() {
        return category;
    }

    public String getMemory() {
        return memory;
    }
}
