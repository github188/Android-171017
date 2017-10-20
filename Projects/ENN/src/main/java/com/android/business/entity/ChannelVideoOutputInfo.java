package com.android.business.entity;

import com.android.business.entity.AlarmPlanInfo.SceneState;

public class ChannelVideoOutputInfo extends DataInfo {

	private int ability; // 设备能力集
    private SceneState alarmState; // 报警状态
    private int functions; // 如果是分享过来的通道，填分享的功能列表
    private int encrypt; // 是否支持加密
//    private SdcardStatus sdcradStatus; // sd卡状态
//    private FormatSdcardResult formatSdcardResult; // sd卡初始化状态
}
