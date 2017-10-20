package com.mapgis.mmt.module.systemsetting.locksetting.view.impl;

/**
 * Created by Comclay on 2017/3/16.
 * 创建密码的统一接口
 * 所有的本地密码登录的设置界面都需要实现该接口
 */

public interface ICreatePasswordView {
    /**
     * 获取密码
     */
    String getPwd();

    /**
     * 获取确认密码
     */
    String getEnsurePwd();

    int getPasswordType();

    /**
     * 取消创建密码
     */
    void cancelCreate();

    /**
     * 确认创建密码
     */
    void confirmCreate();
}
