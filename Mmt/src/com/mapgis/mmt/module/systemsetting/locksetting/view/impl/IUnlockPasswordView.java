package com.mapgis.mmt.module.systemsetting.locksetting.view.impl;

/**
 * Created by Comclay on 2017/3/16.
 * 解锁密码登录界面的统一接口
 * 所有的解锁方式都需要实现该接口
 */

public interface IUnlockPasswordView {

    /**
     * 用户输入的明文密码
     */
    String getEnsurePassword();

    /**
     * 解锁密码
     */
    void unlockPassword();

    /**
     * 忘记本地密码
     */
    void forgetLocalePassword();

    /**
     * 确认密码成功
     */
    void passwordCorrect();

    /**
     * 清除输入的密码
     */
    void clearInputPassword();

    /**
     * 确认密码错误
     */
    void passwordError();

    /**
     * 还有机会验证密码，最多三次
     */
    void hasChanceEnsure();

    /**
     * 没有机会验证密码啦，会自动调用忘记密码操作
     */
    void noChanceEnsure();
}
