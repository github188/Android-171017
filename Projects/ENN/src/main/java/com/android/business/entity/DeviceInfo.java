package com.android.business.entity;

import java.util.ArrayList;

/**
 * 文件描述：文件名、类名 功能说明： 版权申明：
 * 
 * @author：chenhongqiang
 * @version:2015-3-7上午10:23:55
 */

public class DeviceInfo extends DataInfo {

    public DeviceInfo() {
        type = DeviceType.UNKNOWN;
        state = DeviceState.Offline;
    }

    public String getDomId() {
        return domId;
    }

    public void setDomId(String domId) {
        this.domId = domId;
    }

    // 设备类型
    public enum DeviceType {
        UNKNOWN, // 未知型号
        IPC, // 普通ipc
        NVR, // 普通nvr
        DVR, // 普通dvr
        HDVR, // 混合dvr
        BOX, // 盒子主机
        SWITCH, // 智能开关
        JACK, // 智能插座
        INFRARED_SENSOR, // 红外感应器
        MOVE_SENSOR, // 移动感应器
        SMARTIPC, // smart ipc
        PTZCAMERA, // 云台摄像机
        GATEWAY, // 家庭网关
        ALARMBOX // 报警盒子
    }

    public enum DeviceState {
        Online, // 在线
        Offline, // 离线
        Upgrade, // 升级中
    }

    public enum ConnectWifiResult {
        Success, Timeout, AuthFail,
    }

    /**
     * 设备能力集合
     * 
     * @author 23930
     * 
     */
    public class Ability {
    	
    	//此种能力只会在设备上标识，不会在通道上标识。表示设备支持该能力。
        public static final int WLAN = 0x1; // 设备支持接入无线局域网
        public static final int AlarmPIR = 0x2;// 设备支持人体红外报警
        public static final int DHP2P = 0x4; // 设备支持大华P2P服务
        public static final int HSEncrypt = 0x8; // 设备支持华视微讯码流加密
        public static final int CloudStorage = 0x10; // 设备支持华视微讯平台云存储
        public static final int CloudEncrypt = 0x20; // 支持云端存储加密(录像和图片)
        public static final int DPS = 0x40; // 设备支持华视微讯平台云存储
        public static final int AGW = 0x80; // 支持配件接入，即设备是一个配件网关（Accessory Gateway）
        public static final int LocalStorage = 0x100; // 支持设备本地存储，如有SD卡或硬盘
        public static final int PlaybackByFilename = 0x200; // 设备支持根据文件名回放 
        public static final int BreathingLight = 0x400; // 设备有呼吸灯 
        
        //此种能力对于单通道设备，标识在设备上；对于多通道设备，标识在通道上。表示通道支持该能力。
        public static final int AlarmMD = 0x800; // 设备支持动检报警
        public static final int PTZ = 0x1000; // 设备支持云台操作
        public static final int AudioEncode = 0x2000; // 支持音频编码（伴音）
        public static final int FrameReverse = 0x4000; // 支持画面翻转
        
        //此种能力既能标识在设备上，也能标识在通道上，表示相应的设备或通道支持该能力。 
        public static final int AudioTalk = 0x8000; // 支持语音对讲 
        public static final int AlarmSound = 0x10000; //支持报警音设置

    }

    public class Relation {
        public static final int ShareTo = 1; // 分享中
        public static final int ShareFrom = 2;// 被分享
        public static final int AuthorizeTo = 4; // 授权中
        public static final int AuthorizeFrom = 8; // 被授权
    }

    private String snCode; // 设备序列号
    private String name; // 设备名称
    private DeviceType type; // 设备类型
    private String mode; // 设备型号
    private String version; // 设备版本信息
    private ArrayList<String> shareUserIDList; // 分享用户列表
    private DeviceState state; // 设备状态
    private String ip; // 设备ip
    private String port; // 设备端口
    private String superID; // 父亲节点id
    private boolean canUpgrade; // 是否可以升级
    private int ability; // 设备能力集
    private int relation; // 设备与人的关系
    private String logo;// 设备的web url
    private String groupUuid; // 设备管理的组Uuid
    private String groupId; // 设备管理的组id
	private String desc; // 设备描述
    private String ownerUser; // 拥有者
    private String ownerNickName; // 拥有者昵称
    private String protocolVersion; // 设备协议版本号
    private String dms;//访问设备的DMS入口地址，例如122.233.34.45:9200",
    private String splitCap;//设备分屏能力集
    private int inputCount;
    private int outputCount;
    private int partCount;
    private String domId;

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getSnCode() {
        return snCode;
    }

    public void setSnCode(String snCode) {
        this.snCode = snCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDms() {
		return dms;
	}

	public void setDms(String dms) {
		this.dms = dms;
	}

	public ArrayList<String> getShareUserIDList() {
        return shareUserIDList;
    }

    public void setShareUserIDList(ArrayList<String> shareUserIDList) {
        this.shareUserIDList = shareUserIDList;
    }

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSuperID() {
        return superID;
    }

    public void setSuperID(String supperID) {
        this.superID = supperID;
    }

    public boolean isCanUpgrade() {
        return canUpgrade;
    }

    public void setCanUpgrade(boolean canUpgrade) {
        this.canUpgrade = canUpgrade;
    }

    public int getAbility() {
        return ability;
    }

    public void setAbility(int ability) {
        this.ability = ability;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }

    public String getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(String ownerUser) {
        this.ownerUser = ownerUser;
    }

    public String getOwnerNickName() {
        return ownerNickName;
    }

    public void setOwnerNickName(String ownerNickName) {
        this.ownerNickName = ownerNickName;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

	public String getSplitCap() {
		return splitCap;
	}

	public void setSplitCap(String splitCap) {
		this.splitCap = splitCap;
	}

	public int getInputCount() {
		return inputCount;
	}

	public void setInputCount(int inputCount) {
		this.inputCount = inputCount;
	}

	public int getOutputCount() {
		return outputCount;
	}

	public void setOutputCount(int outputCount) {
		this.outputCount = outputCount;
	}

	public int getPartCount() {
		return partCount;
	}

	public void setPartCount(int partCount) {
		this.partCount = partCount;
	}
    public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}


}
