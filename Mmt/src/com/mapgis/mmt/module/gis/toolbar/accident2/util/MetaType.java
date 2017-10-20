package com.mapgis.mmt.module.gis.toolbar.accident2.util;

/**
 * Created by Comclay on 2017/3/3.
 * 爆管分析结果类型
 */

public final class MetaType {
    /**
     * 爆管发生点
     */
    public final static String TYPE_INCIDENT_POINT = "civFeatureMetaTypeIncidentPoint";
    /**
     * 需关断设备
     */
    public final static String TYPE_SWITCH = "civFeatureMetaTypeSwitch";
    /**
     * 受影响用户
     */
    public final static String TYPE_SWI_EFFECT = "civFeatureMetaTypeSwieffect";
    /**
     * 受影响管段
     */
    public final static String TYPE_PIPE_LINE = "civFeatureMetaTypePipeLine";
    /**
     * 受影响水源
     */
    public final static String TYPE_RES_CENTER = "civFeatureMetaTypeRescenter";
    /**
     * 失效关断设备
     */
    public final static String TYPE_INVALIDATE_SWITCH = "civFeatureMetaTypeInvalidateSwitch";
    /**
     * 需开启设备
     */
    public final static String TYPE_SHOULD_OPEN_SWITCH = "civFeatureMetaTypeShouldOpenSwitch";
    /**
     * 辅助关断设备
     */
    public final static String TYPE_ASSIST_SWITCH = "civFeatureMetaTypeAssistSwitch";
    /**
     * 已关断设备
     */
    public final static String TYPE_CLOSED_SWITCH = "civFeatureMetaTypeClosedSwitch";
}
