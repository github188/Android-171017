package com.customform.entity;

/**
 * Created by zoro at 2017/9/5.
 */
public class StationDeviceEvent {
    public String bizType;
    public String deviceName;
    public String deviceID;

    public StationDeviceEvent(String bizType, String deviceName, String deviceID) {
        this.bizType = bizType;
        this.deviceName = deviceName;
        this.deviceID = deviceID;
    }
}