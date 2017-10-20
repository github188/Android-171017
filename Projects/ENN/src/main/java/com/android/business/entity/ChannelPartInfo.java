package com.android.business.entity;

import com.android.business.entity.ChannelInfo.ChannelPartState;

public class ChannelPartInfo extends DataInfo {
	private String mac; // 配件有mac地址 但是没有序列号 接口中的zbDeviceId 指的是mac地址
    private String mode; // 配件型号
    private ChannelPartState partStatus; // 配件状态
    private String partPlan; // 配件计划
    
    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public ChannelPartState getPartStatus() {
        return partStatus;
    }

    public void setPartStatus(ChannelPartState partStatus) {
        this.partStatus = partStatus;
    }

    public String getPartsPlan() {
        return partPlan;
    }

    public void setPartsPlan(String partsPlan) {
        this.partPlan = partsPlan;
    }
}
