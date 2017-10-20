package com.android.business.entity;

/**
 * 文件描述：文件名、类名 功能说明： 版权申明：
 * 
 * @author：ding_qili
 * @version:2015-3-20上午9:31:57
 */

public class AlarmPlanInfo extends DataInfo {
    /** 重复周期 */

    public enum SceneMode {
        Private, // 隐私时段，设备不报警不录像。
        Normal, // 普通时段，设备不报警但是录像。
        Nervous; // 警惕时段，设备既报警又录像。
    }

    public enum SceneState {
        On, // 开
        Off;// 关
    }

    private long beginTime; // 开始时间，毫秒
    private long endTime; // 结束时间 毫秒
    boolean[] repeatWeek = new boolean[7]; // 重复周期

    /** 模式。nervous警惕模式；private隐私模式；normal普通模式 */
    private SceneMode Mode;

    /** 计划状态 */
    private SceneState state;

    public boolean[] getPeriod() {
        return repeatWeek;
    }

    public void setPeriod(boolean[] repeatWeek) {
        this.repeatWeek = repeatWeek;
    }

    public SceneMode getMode() {
        return Mode;
    }

    public void setMode(SceneMode mode) {
        Mode = mode;
    }

    public SceneState getState() {
        return state;
    }

    public void setState(SceneState state) {
        this.state = state;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
