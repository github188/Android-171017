package com.patrol.module.posandpath2.beans;

import android.os.Parcel;

import com.patrol.module.posandpath2.detailinfo.DetailInfoMapData;
import com.zondy.mapgis.geometry.Dot;

import java.util.LinkedHashMap;

/**
 * User: zhoukang
 * Date: 2016-03-22
 * Time: 16:09
 * <p/>
 * trunk:
 */
public class PointInfo extends DetailInfoMapData {

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

    public LinkedHashMap<String,LinkedHashMap<String,String>> toMapData(){
        LinkedHashMap<String,LinkedHashMap<String,String>> mapData = new LinkedHashMap<>();
        LinkedHashMap<String,String> userInfoMap = new LinkedHashMap<>();
        userInfoMap.put("姓名",name);
        userInfoMap.put("轨迹方向",dir);
        userInfoMap.put("时间",time);
        userInfoMap.put("位置",position);
        userInfoMap.put("定位精度",accr);
        userInfoMap.put("定位来源",res);
        userInfoMap.put("经度",lat);
        userInfoMap.put("纬度",lag);
        userInfoMap.put("速度",speed);
        mapData.put("用户实时信息",userInfoMap);

        LinkedHashMap<String,String> phoneInfoMap = new LinkedHashMap<>();
        phoneInfoMap.put("cpu",cpu);
        phoneInfoMap.put("电池剩余",category);
        phoneInfoMap.put("内存",memory);
        mapData.put("手持使用状况",phoneInfoMap);
        return mapData;
    }

    public static Dot convertToDot(String position) {
        String[] xy = position.split(",");
        return new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1]));
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.dir);
        dest.writeString(this.time);
        dest.writeString(this.position);
        dest.writeString(this.accr);
        dest.writeString(this.res);
        dest.writeString(this.lat);
        dest.writeString(this.lag);
        dest.writeString(this.speed);
        dest.writeString(this.cpu);
        dest.writeString(this.category);
        dest.writeString(this.memory);
    }

    protected PointInfo(Parcel in) {
        this.name = in.readString();
        this.dir = in.readString();
        this.time = in.readString();
        this.position = in.readString();
        this.accr = in.readString();
        this.res = in.readString();
        this.lat = in.readString();
        this.lag = in.readString();
        this.speed = in.readString();
        this.cpu = in.readString();
        this.category = in.readString();
        this.memory = in.readString();
    }

    public static final Creator<PointInfo> CREATOR = new Creator<PointInfo>() {
        @Override
        public PointInfo createFromParcel(Parcel source) {
            return new PointInfo(source);
        }

        @Override
        public PointInfo[] newArray(int size) {
            return new PointInfo[size];
        }
    };

    @Override
    public String toString() {
        return "PointInfo{" +
                "position='" + position + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
