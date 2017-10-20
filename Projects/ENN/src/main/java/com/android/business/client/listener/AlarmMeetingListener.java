package com.android.business.client.listener;

/**
 * 功能说明：报警成功后音视频会议请求监听
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-31
 */
public interface AlarmMeetingListener {
    /**
     * 开始
     */
    void onStart(int handle);

    /**
     * 结束
     */
    void onFinish();
}
