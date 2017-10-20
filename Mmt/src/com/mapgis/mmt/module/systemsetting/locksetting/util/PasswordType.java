package com.mapgis.mmt.module.systemsetting.locksetting.util;

/**
 * Created by Comclay on 2017/3/16.
 * 密码登录方式
 */

public class PasswordType {
    /**
     * 未知密码方式
     */
    public final static int PASSWORD_UNKNOW = -1;
    /**
     * 未设置本地登录密码
     */
    public final static int PASSWORD_NONE = 0;
    /**
     * 数字登录密码
     */
    public final static int PASSWORD_NUMBER = 1;
    /**
     * 手势登录密码
     */
    public final static int PASSWORD_GESTURE = 2;

    /**
     * 指纹识别登录
     */
    public final static int PASSWORD_FINGERPRINT = 3;

    /**
     * 人脸识别登录
     */
    public final static int PASSWORD_FACE = 4;

    /**
     * 语音识别登录
     */
    public final static int PASSWORD_VOICE = 5;
}
