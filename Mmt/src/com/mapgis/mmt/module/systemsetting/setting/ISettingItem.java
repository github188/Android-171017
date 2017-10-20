package com.mapgis.mmt.module.systemsetting.setting;

/**
 * Created by Comclay on 2017/6/14.
 * 系统设置的条目的接口类
 *         |--BaseMoreSettingItem       对应MoreItemSettingView
 *         |--BaseSwitchSettingItem     对应SwitchItemSettingView
 */

public interface ISettingItem {
    /**
     * 初始化数据
     */
    void init();

    /**
     * 保存数据的接口
     */
    void save();

    /**
     * 获取每项设置的id，便于在SystemSettingFragment中获取对应的ISettingItem实例
     * @return  布局ID
     */
    int getViewId();
}
