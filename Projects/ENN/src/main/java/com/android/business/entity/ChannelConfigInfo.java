package com.android.business.entity;

/**
 * 描述：一键控制中每一个通道的配置 作者： 27926
 */
public class ChannelConfigInfo extends DataInfo {
    private String devUuid;
    private String channelId;
    private String channelName;
    private ChannleStatus channleStatus;

    enum ChannleStatus {
        ON, OFF
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public ChannleStatus getChannleStatus() {
        return channleStatus;
    }

    public void setChannleStatus(ChannleStatus channleStatus) {
        this.channleStatus = channleStatus;
    }

    public String getDevUuid() {
        return devUuid;
    }

    public void setDevUuid(String devUuid) {
        this.devUuid = devUuid;
    }


}
