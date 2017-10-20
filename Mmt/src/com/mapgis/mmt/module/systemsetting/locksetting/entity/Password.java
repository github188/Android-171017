package com.mapgis.mmt.module.systemsetting.locksetting.entity;

import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordType;

/**
 * Created by Comclay on 2017/3/16.
 * 密码对象
 */

public class Password {
    private String mUserId;
    // 密码，此处密码为输入密码框里的值或者是本地设置的登陆密码
    private String mLocalePwd;
    // 本地确认密码，这个在登陆界面就是用户输入的密码
    private String mLocaleEnsurePwd;

    /**
     * 本地登录方式
     */
    private int mPasswordType = PasswordType.PASSWORD_NONE;

    public Password() {
    }

    public Password(String pwd, int loginType) {
        this.mLocalePwd = pwd;
        this.mPasswordType = loginType;
    }

    public String getLocalePwd() {
        return mLocalePwd;
    }

    public void setLocalePwd(String localePwd) {
        this.mLocalePwd = localePwd;
    }

    public String getLocaleEnsurePwd() {
        return mLocaleEnsurePwd;
    }

    public void setLocaleEnsurePwd(String localeEnsurePwd) {
        this.mLocaleEnsurePwd = localeEnsurePwd;
    }

    public int getPasswordType() {
        return mPasswordType;
    }

    public void setPasswordType(int loginType) {
        this.mPasswordType = loginType;
    }

    @Override
    public String toString() {
        return "Password{" +
                "mLocalePwd='" + mLocalePwd + '\'' +
                ", mLocaleEnsurePwd='" + mLocaleEnsurePwd + '\'' +
                ", mPasswordType=" + mPasswordType +
                '}';
    }
}
