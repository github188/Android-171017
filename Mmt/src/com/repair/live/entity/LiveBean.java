package com.repair.live.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Comclay on 2016/11/30.
 * 直播对象
 *
 */
public class LiveBean implements Parcelable{
    // 每个用户拥有固定的直播地址，并在数据库中记录直播状态
    public String rtmpURL;
    public int id;
    public String jid;
    public int liveStatus;
    public String pos;
    public String userImg;
    public String userName;
    public String preImgUrl;
    public String startTime;
    public String stopTime;
    public String streamName;

    public LiveBean(int id, String jid, int liveStatus, String pos, String userImg, String userName, String preImgUrl, String startTime, String stopTime, String streamName) {
        this.id = id;
        this.jid = jid;
        this.liveStatus = liveStatus;
        this.pos = pos;
        this.userImg = userImg;
        this.userName = userName;
        this.preImgUrl = preImgUrl;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.streamName = streamName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.rtmpURL);
        dest.writeInt(this.id);
        dest.writeString(this.jid);
        dest.writeInt(this.liveStatus);
        dest.writeString(this.pos);
        dest.writeString(this.userImg);
        dest.writeString(this.userName);
        dest.writeString(this.preImgUrl);
        dest.writeString(this.startTime);
        dest.writeString(this.stopTime);
        dest.writeString(this.streamName);
    }

    public LiveBean() {
    }

    protected LiveBean(Parcel in) {
        this.rtmpURL = in.readString();
        this.id = in.readInt();
        this.jid = in.readString();
        this.liveStatus = in.readInt();
        this.pos = in.readString();
        this.userImg = in.readString();
        this.userName = in.readString();
        this.preImgUrl = in.readString();
        this.startTime = in.readString();
        this.stopTime = in.readString();
        this.streamName = in.readString();
    }

    public static final Creator<LiveBean> CREATOR = new Creator<LiveBean>() {
        @Override
        public LiveBean createFromParcel(Parcel source) {
            return new LiveBean(source);
        }

        @Override
        public LiveBean[] newArray(int size) {
            return new LiveBean[size];
        }
    };

    @Override
    public String toString() {
        return "LiveBean{" +
                ", id=" + id +
                ", jid='" + jid + '\'' +
                ", liveStatus=" + liveStatus +
                ", pos='" + pos + '\'' +
                ", userImg='" + userImg + '\'' +
                ", userName='" + userName + '\'' +
                ", preImgUrl='" + preImgUrl + '\'' +
                ", startTime='" + startTime + '\'' +
                ", stopTime='" + stopTime + '\'' +
                ", streamName='" + streamName + '\'' +
                '}';
    }
}
