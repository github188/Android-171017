package com.android.business.entity;

import com.android.business.entity.AlarmPlanInfo.SceneState;
import com.android.business.entity.ChannelInfo.FormatSdcardResult;
import com.android.business.entity.ChannelInfo.SdcardStatus;

public class ChannelVideoInputInfo extends DataInfo {

	private int ability; // 设备能力集
    private SceneState alarmState; // 报警状态
    private int functions; // 如果是分享过来的通道，填分享的功能列表
    private int encrypt; // 是否支持加密
    private SdcardStatus sdcradStatus; // sd卡状态
    private FormatSdcardResult formatSdcardResult; // sd卡初始化状态
    
    public SceneState getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(SceneState alarmState) {
        this.alarmState = alarmState;
    }

    public int getAbility() {
        return ability;
    }

    public void setAbility(int ability) {
        this.ability = ability;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public SdcardStatus getSdcardStatus() {
        return sdcradStatus;
    }

    public void setSdcradStatus(SdcardStatus sdcardStatus) {
        this.sdcradStatus = sdcardStatus;
    }

    public FormatSdcardResult getFormatSdcardResult() {
        return formatSdcardResult;
    }

    public void setFormatSdcardResult(FormatSdcardResult formatSdcardResult) {
        this.formatSdcardResult = formatSdcardResult;
    }

    public int getFunctions() {
        return functions;
    }

    public void setFunctions(int functions) {
        this.functions = functions;
    }
    
}
