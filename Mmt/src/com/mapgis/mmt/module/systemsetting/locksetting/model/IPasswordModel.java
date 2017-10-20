package com.mapgis.mmt.module.systemsetting.locksetting.model;

import com.mapgis.mmt.module.systemsetting.locksetting.entity.Password;

/**
 * Created by Comclay on 2017/3/16.
 *
 */

public interface IPasswordModel {
    /**
     * 保存密码
     */
    boolean savePassword(Password password);

    /**
     * 保存密码
     *
     * @param password   Password对象
     * @param isSecurity 是否加密
     * @return true, 保存成功，false保存失败
     */
    boolean savePassword(Password password, boolean isSecurity);

    /**
     * 获取密码
     */
    Password getPassword();

    /**
     * 重置密码
     * @return  true重置成功，false重置失败
     */
    boolean resetPassword();

    boolean isAccordance(String ensurePassword);
}
