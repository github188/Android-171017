package com.android.business.exception;

public class BusinessErrorCode {
	
	private final static int BEC_COMMON_BASE= 0;			// 通用
	private final static int BEC_ENVIRONMENT_BASE= 1000;	// 环境配置模块
	private final static int BEC_USER_BASE= 2000;			// 用户模块
	private final static int BEC_DEVICE_BASE= 3000;			// 设备管理模块
	private final static int BEC_MESSAGE_BASE= 4000;		// 消息模块
	private final static int BEC_RECORD_BASE= 5000;			// 录像模块
	private final static int BEC_SCENEMODE_BASE= 6000;		// 情景模式模块
	private final static int BEC_STATISTICS_BASE= 7000;		// 统计模块
	
	private final static int BEC_VIDEOSHARE_BASE = 8000; 	//视频广场模块
	private final static int BEC_MEETING_BASE = 10000;     // 会议
    private final static int BEC_GROUP_BASE = 20000;        // 组
    private final static int BEC_EXCHANGE_BASE = 30000;        // 组
    private final static int BEC_PATROL_BASE = 40000;        // 巡查模块
    private final static int BEC_TASK_BASE = 50000;        // 任务模块
    private final static int BEC_REDPACKET_BASE = 60000;        // 红包模块
	//////////////通用错误/////////////////
	/**
	 *  OK （API调用成功，但是具体返回结果，由content中的code和desc描述）
	 */
//	public final static int BEC_COMMON_OK = BEC_COMMON_BASE + 0; 	// 200 OK （API调用成功，但是具体返回结果，由content中的code和desc描述）

	/**
	 *  未知错误
	 */
	public final static int BEC_COMMON_UNKNOWN = BEC_COMMON_BASE + 1; 	// 未知错误
	/**
	 * // 400 Bad Request （API格式错误，无返回内容） 
	 */
	public final static int BEC_COMMON_BAD_REQUEST = BEC_COMMON_BASE + 2;		// 400 Bad Request （API格式错误，无返回内容） 
	/**
	 * // 401 Unauthorized （用户名密码认证失败，无返回内容）
	 */
	public final static int BEC_COMMON_UNAUTHORIZED = BEC_COMMON_BASE + 3;		// 401 Unauthorized （用户名密码认证失败，无返回内容） 
	/**
	 * // Forbidden （认证成功但是无权限，无返回内容） 
	 */
	public final static int BEC_COMMON_FORBIDDEN = BEC_COMMON_BASE + 4;		// Forbidden （认证成功但是无权限，无返回内容） 
	/**
	 * // 404 Not Found （请求的URI错误，无返回内容)
	 */
	public final static int BEC_COMMON_NOT_FOUND = BEC_COMMON_BASE + 5;		// 404 Not Found （请求的URI错误，无返回内容)
	/**
	 * // Precondition Failed （先决条件失败，无返回内容。通常是由于客户端所带的x-hs-date时间与服务器时间相差过大。） 
	 */
	public final static int BEC_COMMON_PRECONDITION_FAILED = BEC_COMMON_BASE + 6;		// Precondition Failed （先决条件失败，无返回内容。通常是由于客户端所带的x-hs-date时间与服务器时间相差过大。） 
	/**
	 * // 500 Internal Server Error （服务器内部错误，无返回内容） 
	 */
	public final static int BEC_COMMON_SERVER_ERROR= BEC_COMMON_BASE + 7;		// 500 Internal Server Error （服务器内部错误，无返回内容） 
	/**
	 * //503 Service Unavailable （服务不可用，无返回内容。这种情况一般是因为接口调用超出频率限制。） 
	 */
	public final static int BEC_COMMON_SERVICE_UNAVAILABLE = BEC_COMMON_BASE + 8;	//503 Service Unavailable （服务不可用，无返回内容。这种情况一般是因为接口调用超出频率限制。） 
	
	public final static int BEC_COMMON_GETWAY_UNAVAILABLE= BEC_COMMON_BASE + 9;	//502 网关错误 
	/**
	 * 业务空指针异常
	 */
	public final static int BEC_COMMON_NULL_POINT = BEC_COMMON_BASE + 10;	
	
	/**
	 *广播机制发生异常 
	 */
	public final static int BEC_COMMON_BROADCAST_ERROR = BEC_COMMON_BASE + 11;
	
	/**
	 * 数据解析异常
	 */
	public final static int BEC_COMMON_PARSEDATA_ERROR = BEC_COMMON_BASE + 12;
	
	
	public final static int BEC_COMMON_TIME_OUT = BEC_COMMON_BASE + 13;
	
	// 成功
    public static final int BEC_COMMON_SUCCESS = BEC_COMMON_BASE + 14;
    // 失败
    public static final int BEC_COMMON_FAIL = BEC_COMMON_BASE + 15;
    // 任务被取消
    public static final int BEC_COMMON_CANCEL = BEC_COMMON_BASE + 16;
    // 网络异常
    public static final int BEC_COMMON_NET_ERROR = BEC_COMMON_BASE + 17;
    // 非法参数
    public static final int BEC_COMMON_ILLEGAL_PARAM = BEC_COMMON_BASE + 18;
    // 无法连接服务器
    public static final int BEC_COMMON_UNCONNECT_SERVER = BEC_COMMON_BASE + 20;
    //没有更多数据了
    public static final int BEC_COMMON_NO_MORE_DATA = BEC_COMMON_BASE + 21;

	/*---------------------用户-----------------*/
    /** 用户名已经存在 **/
    public static final int BEC_USER_NAME_EXIST = BEC_USER_BASE +1;
    /** 手机号已经存在 **/
    public static final int BEC_USER_PHONE_EXIST = BEC_USER_BASE + 2;
    /** 验证码不匹配 **/
    public static final int BEC_USER_VALID_ERROR = BEC_USER_BASE +3;
    /** 验证码发送失败 **/
    public static final int BEC_USER_VALID_SEND_FAILED = BEC_USER_BASE +4;
    /** 保存用户头像失败 **/
    public static final int BEC_USER_SAVE_ICON_FAILED = BEC_USER_BASE +5;
    /** 第三方账号校验失败 **/
    public static final int BEC_USER_THIRD_ACCOUNT_VALID_FAILED = BEC_USER_BASE +6;
    /** 第三方账号已绑定其他账号 **/
    public static final int BEC_USER_THIRD_ACCOUNT_BIND_OTHERS = BEC_USER_BASE +7;
    /** 用户冻结，5分钟后重试 **/
    public static final int BEC_USER_FREEZE_FIVE_MINUTE = BEC_USER_BASE +8;
    /** 后台主动用户冻结 **/
    public static final int BEC_USER_FREEZE = BEC_USER_BASE +9;
    /** 第三方账号未绑定 **/
    public static final int BEC_USER_THIRD_ACCOUNT_NOT_BIND = BEC_USER_BASE +10;
    /** 用户名和密码不匹配 **/
    public static final int BEC_USER_PASSWORD_ERROR = BEC_USER_BASE +11;
    /** 调用获取验证码太频繁 **/
    public static final int BEC_USER_GET_VALIDCODE_TOO_MANY = BEC_USER_BASE + 12;
    /** 未找到该分享信息 **/
    public static final int BEC_USER_NOT_SHAREINFO = BEC_USER_BASE + 13;
    
    /** 没有更多的分享信息了 **/
    public static final int BEC_USER_NO_MORE = BEC_USER_BASE + 14;
    
    /** 验证本地密码错误 **/
    public static final int BEC_USER_LOCAL_PSW_ERROR = BEC_USER_BASE + 15;
    /**
     * 账号未注册
     */
    public static final int BEC_USER_EMPTY = BEC_USER_BASE + 16;
    
    /**
     * 手势密码错误
     */
    public static final int BEC_USER_GESTURE_ERROR = BEC_USER_BASE + 17;
    
    // 没有该用户
    public static final int BEC_USER_NO_USER = BEC_USER_BASE + 18;
    // 用户已存在登录会话
    public static final int BEC_USER_IS_LOGIN = BEC_USER_BASE + 19;
    // 用户未登录
    public static final int BEC_USER_NO_LOGIN = BEC_USER_BASE + 20;
    // 未找到用户信息
    public static final int BEC_USER_NOT_FOUND = BEC_USER_BASE + 21;
    
    /*---------------------消息-----------------*/
    /** 超过最大的分页限制 **/
    public static final int BEC_MESSAGE_PAGE_SIZE_TOO_BIG = BEC_MESSAGE_BASE + 1;
    /*
     * 未初始化异常
     */
    public static final int BEC_MESSAGE_UNINIT_ERROR = BEC_MESSAGE_BASE +2;
    /**
     * 没有更多的消息数了
     */
    public static final int BEC_MESSAGE_NO_MORE = BEC_MESSAGE_BASE +3;
    
    /*
     * 未发现该消息
     */
    public static final int BEC_MESSAGE_NOT_MESSAGEINFO = BEC_MESSAGE_BASE +3;
    
    /*---------------------设备模块-----------------*/
    /** 设备未注册 **/
    public static final int BEC_DEVICE_NOT_EXIST = BEC_DEVICE_BASE +1;
    /** 设备注册码错误 **/
    public static final int BEC_DEVICE_REGCODE_ERROR = BEC_DEVICE_BASE +2;
    /** 设备被自己添加 **/
    public static final int BEC_DEVICE_ADDBYYOURSELF = BEC_DEVICE_BASE +3;
    /** 设备项目属性和用户不匹配 */
    public static final int BEC_DEVICE_PROJECT_NOT_MATCH = BEC_DEVICE_BASE +4;
    /** 设备未添加 **/
    public static final int BEC_DEVICE_NOT_ADDED = BEC_DEVICE_BASE +5;
    /** 设备被别人添加 **/
    public static final int BEC_DEVICE_ADDBYOTHER = BEC_DEVICE_BASE +6;
    /** 分享失败 **/
    public static final int BEC_DEVICE_SHARE_INFO_ERROR = BEC_DEVICE_BASE +7;
    /** 修改通道失败 **/
    public static final int BEC_DEVICE_UPDATE_CHANNEL_FAIL = BEC_DEVICE_BASE +8;
    /** 设备离线 **/
    public static final int BEC_DEVICE_OFFLINE = BEC_DEVICE_BASE +9;
    
    /** 通道非ptz通道 **/
    public static final int BEC_DEVICE_NOT_PTZ_CHANNEL = BEC_DEVICE_BASE +10;
    
    public static final int BEC_DEVICE_WIFI_NOT_ENABLE = BEC_DEVICE_BASE + 11;
    
    /*---------------------云存储录像-----------------*/
    /** 云存储录像未找到 */
    public static final int BEC_RECORD_CLOUD_STORAGE_NOT_FOUND = BEC_RECORD_BASE +1;
    /** 录像未找到 */
    public static final int BEC_RECORD_STORAGE_NOT_FOUND = BEC_RECORD_BASE +2;
    /** 录像查询失败*/
    public static final int BEC_RECORD_STORAGE_QUERY_FAID = BEC_RECORD_BASE +3;
    
    /*------------情景模式-------------*/
    /**
     * 情景模式定义未找到
     */
    public static final int BEC_SCENE_DEF_NOT_FIND = BEC_SCENEMODE_BASE +1; 
	
    /*----------------视频广场模块---------------------*/
    /**1701:视频已被分享    80001*/
    public static final int BEC_VIDEOSHARE_ALREADY_SHARED = BEC_VIDEOSHARE_BASE + 1;
    /**1702:视频未被分享    80002*/
    public static final int BEC_VIDEOSHARE_NOT_SHARED = BEC_VIDEOSHARE_BASE + 2;
    /**1703:评论不存在    80003*/
    public static final int BEC_VIDEOSHARE_COMMENT_UNEXIST = BEC_VIDEOSHARE_BASE +3;
    /**1704:用户已点赞    80004*/
    public static final int BEC_VIDEOSHARE_ALREADY_PRIZED = BEC_VIDEOSHARE_BASE + 4;
    /**1705:用户未点赞    80005*/
    public static final int BEC_VIDEOSHARE_NOT_PRIZED = BEC_VIDEOSHARE_BASE + 5;
    /****************************会议**************************************/
    // 初始化失败
    public static final int BEC_MEETING_INIT_FAIL = BEC_MEETING_BASE + 0;
    // 初始化成功
    public static final int BEC_MEETING_INIT_SUCCESS = BEC_MEETING_BASE + 1;
    // 音视频服务未初始化
    public static final int BEC_MEETING_UNINIT = BEC_MEETING_BASE + 2;
    // 会议中无该用户
    public static final int BEC_MEETING_NO_USER = BEC_MEETING_BASE + 3;
    // 查询会议失败
    public static final int BEC_MEETING_NOT_FOUND = BEC_MEETING_BASE + 4;
    // 会议Id重复
    public static final int BEC_MEETING_ID_REPEAT = BEC_MEETING_BASE + 5;
    // 会议人数已满
    public static final int BEC_MEETING_NO_RESOURCE = BEC_MEETING_BASE + 6;
    // 未加入该会议
    public static final int BEC_MEETING_NO_JOIN = BEC_MEETING_BASE + 7;
    // 创建失败
    public static final int BEC_MEETING_CREATE_FAIL = BEC_MEETING_BASE + 8;
    // 正在重连
    public static final int BEC_MEETING_RECONNECT = BEC_MEETING_BASE + 10;
    // 断开连接
    public static final int BEC_MEETING_DISCONNECT = BEC_MEETING_BASE + 11;
    // 无录音权限
    public static final int BEC_MEETING_NO_AUDIO_PERMISSION = BEC_MEETING_BASE + 12;
    // 无视频权限
    public static final int BEC_MEETING_NO_VIDEO_PERMISSION = BEC_MEETING_BASE + 13;
    // 音视频服务器异常
    public static final int BEC_MEETING_ERROR = BEC_MEETING_BASE + 14;
    // 设备无效
    public static final int BEC_MEETING_DEVICE_ERROR = BEC_MEETING_BASE + 15;
    // 当前空闲
    public static final int BEC_MEETING_USER_FREE = BEC_MEETING_BASE + 16;
    // 当前正在会议  不能发起新会议
    public static final int BEC_MEETING_USER_IS_IN_MEETING = BEC_MEETING_BASE + 17;
    /**
     * 正在通话 （系统电话）
     */
    public static final int BEC_MEETING_USER_IS_PHONE_CALL = BEC_MEETING_BASE + 18;
    /**
     * 用户未绑定话机号
     */
    public static final int BEC_MEETING_USER_NO_PHONE_NUM = BEC_MEETING_BASE + 19;
    /**
     * 话机服务未初始化
      */
    public static final int BEC_MEETING_SOFT_PHONE_UNINIT = BEC_MEETING_BASE + 20;
    /**
     * 用户视频未开启
     */
    public static final int BEC_MEETING_VIDEO_IS_CLOSE = BEC_MEETING_BASE + 21;
    /**
     * 未鉴权
     */
    public static final int BEC_MEETING_CLIENT_UNAUTHORIZED = BEC_MEETING_BASE + 22;
    /**
     * LICENSE过期
     */
    public static final int BEC_MEETING_LICENSE_EXPIRED = BEC_MEETING_BASE + 24;
    /**
     * 服务不可用
     */
    public static final int BEC_MEETING_SERVICE_UNAVAILABLE = BEC_MEETING_BASE + 25;
    /**
     * 服务超时
     */
    public static final int BEC_MEETING_SERVER_TIME_OUT = BEC_MEETING_BASE + 26;
    /**
     * 版本不兼容
     */
    public static final int BEC_MEETING_VISION_INVALID = BEC_MEETING_BASE + 27;
    /**
     * 用户数已满
     */
    public static final int BEC_MEETING_SERVER_OUT_OF_USER = BEC_MEETING_BASE + 28;
    /**
     * 会议用户数已满
     */
    public static final int BEC_MEETING_ROOM_OUT_OF_USER = BEC_MEETING_BASE + 29;
    /**
     * LICENSE授权数已满
     */
    public static final int BEC_MEETING_LICENSE_OUT_OF_USER = BEC_MEETING_BASE + 30;
    /**
     * 会议ID包含不支持的字符
     */
    public static final int BEC_MEETING_ID_INVALID = BEC_MEETING_BASE + 31;
    /**
     * 鉴权失败
     */
    public static final int BEC_MEETING_AUTH_FAILED = BEC_MEETING_BASE + 32;
    /**
     * 服务数据库异常
     */
    public static final int BEC_MEETING_SERVER_DATABASE_ERROR = BEC_MEETING_BASE + 33;
    /**
     * 会议视频连接数已满
     */
    public static final int BEC_MEETING_ROOM_OUT_OF_VIDEO = BEC_MEETING_BASE + 34;
    /**
     * 会议音频连接数已满
     */
    public static final int BEC_MEETING_ROOM_OUT_OF_AUDIO = BEC_MEETING_BASE + 35;
    /**
     * 重复发布
     */
    public static final int BEC_MEETING_DUPLICATE_PUBLISH = BEC_MEETING_BASE + 36;
    /**
     * 会议错误TOKEN
     */
    public static final int BEC_MEETING_ROOM_ERROR_TOKEN = BEC_MEETING_BASE + 37;
    /**
     * 重复加入
     */
    public static final int BEC_MEETING_DUPLICATE_JOIN = BEC_MEETING_BASE + 38;
    /**
     * 数据传输连接失败
     */
    public static final int BEC_MEETING_DATA_CONN_FAIL = BEC_MEETING_BASE + 39;
    /**
     * 音视频服务未配置
     */
    public static final int BEC_MEETING_UNCONFIG = BEC_MEETING_BASE + 40;

    
    /****************************组**************************************/
    // 没有该组
    public static final int BEC_GROUP_NO_GROUP = BEC_GROUP_BASE + 0;
    
    
    /****************************即时通讯**************************************/
    public static final int BEC_EXCHANGE_FILE_TOO_LARGE = BEC_EXCHANGE_BASE + 0;
    public static final int BEC_EXCHANGE_UPLOAD_FAIL = BEC_EXCHANGE_BASE + 1;

    /****************************巡查模块**************************************/
    public static final int BEC_PATROL_LOAD_FAIL = BEC_PATROL_BASE + 1;
    public static final int BEC_PATROL_DISTANCE_LONG = BEC_PATROL_BASE + 2;
    public static final int BEC_PATROL_SIGNIN_FAIL = BEC_PATROL_BASE + 3;

    /****************************任务模块**************************************/
    public static final int BEC_TASK_NOT_FOUND = BEC_TASK_BASE + 1;
    /****************************红包模块**************************************/
    public static final int BEC_REDPACKET_ISSELF = BEC_REDPACKET_BASE + 1;
}