package com.maintainproduct.module.maintenance;

public class MaintenanceConstant {

    /**
     * 进入下一个界面的requestCode
     */
    public static final int DEFAULT_REQUEST_CODE = 6001;

    /**
     * 访问查询养护工单列表服务结果正常
     */
    public static final int SERVER_GET_LIST_SUCCESS = 60010;

    /**
     * 访问查询养护工单列表服结果异常
     */
    public static final int SERVER_GET_LIST_FAIL = 60011;

    /**
     * 根据滑动栏控制,定时刷新工单列表排序
     */
    public static final int CASE_LIST_REFREASH = 60012;

    /**
     * 获取养护工单详情表单成功
     */
    public static final int SERVER_GET_DETAIL_SUCCESS = 60020;

    /**
     * 手动选择承办人移交
     */
    public static final int SERVER_SELECT_NEXT = 60021;

    /**
     * 默认承办人移交
     */
    public static final int SERVER_DEFAULT_NEXT = 60022;

    /**
     * 获取养护工单详情表单失败
     */
    public static final int SERVER_GET_DETAIL_FAIL = 60023;

    /**
     * 获取反馈表单
     */
    public static final int SERVER_GET_FEEDBACK = 60030;

    /**
     * 创建反馈表单界面
     */
    public static final int SERVER_CREATE_FEEDBACK_VIEW = 60031;

    /**
     * 工单反馈操作-------反馈
     */
    public static final int SERVER_ONLY_FEEDBACK = 60032;

    /**
     * 工单反馈操作-------反馈并移交，合并为一个操作
     */
    public static final int SERVER_BOTH_FEEDBACK_HANDOVER = 60033;

    /**
     * 工单反馈操作-------移交
     */
    public static final int SERVER_ONLY_HANDOVER = 60034;

    public static final int CASE_LIST_AutoREFREASH = 60035;
    // public static final ArrayList<MaintainSimpleInfo> MAINTENANCE_TASK_LIST =
    // new ArrayList<MaintainSimpleInfo>();
}
