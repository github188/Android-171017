package com.android.business.client.listener;

/**
 * 功能说明：服务连接状态通知
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-20
 */
public interface ServerStatusListener {
    /**
     *
     * @param serverName 服务模块
     * @param status 0断开连接 1已连接
     * @param errorCode 错误码
     */
    void onStatusChange(String serverName, int status, int errorCode);

}
