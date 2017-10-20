package com.android.business.entity;

import com.android.business.entity.AlarmPlanInfo.SceneState;

public class ChannelInfo extends DataInfo {

	public String getDomId() {
		return domId;
	}

	public void setDomId(String domId) {
		this.domId = domId;
	}

	public enum ChannelState {
		Online, 		// 在线
		Offline, 		// 离线
		Upgrade, 		// 升级中
	}

	public enum ChannelPartState {
		ON, 			// 关闭
		OFF, 			// 开启
	}

	public enum ChannelType {
		Camera, 		// 普通摄像机
		PtzCamera, 		// 云台摄像机
		LIGHT, 			// 灯
		CURTAIN, 		// 窗帘

		EG, 			// 门磁 MAGNETOMETEREG
		PIR, 			// 人体红外感应 INFAREDSENSOR
		SD, 			// 烟感
		IS, 			// 智能插座 SMARTSOCKET
		controler, 		// 控制器
		wiredalarm 		// 有限警号
	}
	
	public enum ChannelCategory{
		videoInputChannel,		//输入通道
		videoOutputChannel,	//输出通道
		zbChannel				//配件通道
	}

	/**
	 * 云台操作枚举
	 * 
	 * @author 23930
	 * 
	 */
	public enum PtzOperation {
		up, down, left, right, leftUp, rightUp, leftDown, RightDown, zoomin, zoomout, stop
	}

	/**
	 * SD卡状态：0-异常，1-正常，2-无SD卡，3-格式化中
	 * 
	 * @author 16730
	 * 
	 */
	public enum SdcardStatus {
		Exception, Nor, Nocard, Initing
	}

	/**
	 * start-recover：开始初始化（正常情况下） no-sdcard：插槽内无SD卡
	 * in-recover：正在初始化（有可能别的客户端已经请求初始化）
	 * already-recover：已被初始化（有可能别的客户端已经把SD卡初始化掉了） sdcard-error：其他SD卡错误
	 * 
	 * @author 16730
	 * 
	 */
	public enum FormatSdcardResult {
		StartRecover, NoSdcard, InRecover, AlreadyRecover, SdcardError,
	}

	public class Functions {
		public static final int VIDEOMONITOR = 1; 		// 实时视频
		public static final int CONFIGURE = 2; 			// 设备配置
		public static final int ALARMMSG = 4; 			// 报警消息
		public static final int VIDEORECORD = 8; 		// 录像回放
	}

	// All channels have these parms
	private int index; 				// 通道索引
	private String chnSncode; 		// 通道sncode
	private String name; 			// 通道名称
	private ChannelState state; 	// 通道状态 是否在线
	private String backgroudImgURL; // 背景图URL
	private String deviceUuid; 		// 关联的设备id
	private ChannelType type; 		// 通道类型
	private ChannelCategory category; 

	private ChannelVideoInputInfo mVideoInputInfo;
	private ChannelVideoOutputInfo mVideoOutputInfo;
	private ChannelPartInfo mPartInfo;
	private String domId;

	public ChannelInfo(ChannelVideoInputInfo mVideoInputInfo,
                       ChannelVideoOutputInfo mVideoOutputInfo, ChannelPartInfo mPartInfo) {
		this.mVideoInputInfo = mVideoInputInfo;
		this.mVideoOutputInfo = mVideoOutputInfo;
		this.mPartInfo = mPartInfo;
	}

	public ChannelVideoInputInfo getmVideoInputInfo() {
		return mVideoInputInfo;
	}

	public void setmVideoInputInfo(ChannelVideoInputInfo mVideoInputInfo) {
		this.mVideoInputInfo = mVideoInputInfo;
	}

	public ChannelVideoOutputInfo getmVideoOutputInfo() {
		return mVideoOutputInfo;
	}

	public void setmVideoOutputInfo(ChannelVideoOutputInfo mVideoOutputInfo) {
		this.mVideoOutputInfo = mVideoOutputInfo;
	}

	public ChannelPartInfo getmPartInfo() {
		return mPartInfo;
	}

	// special parms' getter/setter
	public void setmPartInfo(ChannelPartInfo mPartInfo) {
		this.mPartInfo = mPartInfo;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ChannelState getState() {
		return state;
	}

	public void setState(ChannelState state) {
		this.state = state;
	}

	public void setState(int state) {
		if (state == 1) {
			this.state = ChannelState.Online;
		} else {
			this.state = ChannelState.Offline;
		}
	}

	public String getBackgroudImgURL() {
		return backgroudImgURL;
	}

	public void setBackgroudImgURL(String backgroudImgURL) {
		this.backgroudImgURL = backgroudImgURL;
	}

	public String getDeviceUuid() {
		return deviceUuid;
	}

	public void setDeviceUuid(String deviceUuid) {
		this.deviceUuid = deviceUuid;
	}

	public ChannelType getType() {
		return type;
	}

	public void setType(ChannelType type) {
		this.type = type;
	}

	public void setType(String type) {
		// TODO
		if (type.contains("light")) {
			this.type = ChannelType.LIGHT;
		} else if (type.contains("Curtain")) {
			this.type = ChannelType.CURTAIN;
		} else if (type.contains("")) {

		} else {
			this.type = ChannelType.Camera;
		}
	}
	
	public ChannelCategory getCategory() {
		return category;
	}

	public void setCategory(ChannelCategory category) {
		this.category = category;
	}

	public SceneState getAlarmState() {
		return mVideoInputInfo == null ? null : mVideoInputInfo.getAlarmState();
	}

	public void setAlarmState(SceneState alarmState) {
		mVideoInputInfo.setAlarmState(alarmState);
	}

	public int getAbility() {
		return mVideoInputInfo == null ? -1 : mVideoInputInfo.getAbility();
	}

	public void setAbility(int ability) {
		mVideoInputInfo.setAbility(ability);
	}

	public int getEncrypt() {
		return mVideoInputInfo == null ? -1 : mVideoInputInfo.getEncrypt();
	}

	public void setEncrypt(int encrypt) {
		mVideoInputInfo.setEncrypt(encrypt);
	}

	public SdcardStatus getSdcardStatus() {
		return mVideoInputInfo == null ? null : mVideoInputInfo
				.getSdcardStatus();
	}

	public void setSdcradStatus(SdcardStatus sdcardStatus) {
		mVideoInputInfo.setSdcradStatus(sdcardStatus);
	}

	public FormatSdcardResult getFormatSdcardResult() {
		return mVideoInputInfo == null ? null : mVideoInputInfo
				.getFormatSdcardResult();
	}

	public void setFormatSdcardResult(FormatSdcardResult formatSdcardResult) {
		mVideoInputInfo.setFormatSdcardResult(formatSdcardResult);
	}

	public int getFunctions() {
		return mVideoInputInfo == null ? -1 : mVideoInputInfo.getFunctions();
	}

	public void setFunctions(int functions) {
		mVideoInputInfo.setFunctions(functions);
	}

	public String getMac() {
		return mPartInfo == null ? null : mPartInfo.getMac();
	}

	public void setMac(String mac) {
		mPartInfo.getMac();
	}

	public String getMode() {
		return mPartInfo == null ? null : mPartInfo.getMode();
	}

	public void setMode(String mode) {
		mPartInfo.setMode(mode);
	}

	public ChannelPartState getPartStatus() {
		return mPartInfo == null ? null : mPartInfo.getPartStatus();
	}

	public void setPartsStatus(ChannelPartState partStatus) {
		mPartInfo.setPartStatus(partStatus);
	}

	public String getPartsPlan() {
		return mPartInfo == null ? null : mPartInfo.getPartsPlan();
	}

	public void setPartsPlan(String partsPlan) {
		mPartInfo.setPartsPlan(partsPlan);
	}

	public String getChnSncode() {
		return chnSncode;
	}

	public void setChnSncode(String chnSncode) {
		this.chnSncode = chnSncode;
	}

}
