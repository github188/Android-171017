package com.repair.zhoushan.module.devicecare;

import com.repair.zhoushan.entity.FlowNodeMeta;

/**
 * 鍙拌处鍜屼换锷＄殑淇濆瓨缁撴瀯
 */
public class ScheduleTaskSaveInfo {

    public ScheduleTaskSaveInfo() {
        flowNodeMeta = new FlowNodeMeta();
        scheduleTask = new ScheduleTask();
    }

    public FlowNodeMeta flowNodeMeta;
    public ScheduleTask scheduleTask;
}