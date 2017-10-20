package com.mapgis.mmt.module.systemsetting.locksetting.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.SecurityUtil;
import com.mapgis.mmt.module.systemsetting.locksetting.entity.Password;
import com.mapgis.mmt.module.systemsetting.locksetting.fingerprint.CreateFingerprintPasswordActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.gesturepwd.CreateGesturePasswordActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.numpwd.CreateNumberPasswordActivity;

/**
 * Created by Comclay on 2017/3/16.
 * 本地登录管理类，提供统一的调用接口
 */

public class PasswordManager {
    private static final String TAG = "LocalePasswordManager";
    private final static String PASSWORD_LOCALE_VALUE = "PASSWORD_VALUE";
    private final static String PASSWORD_LOCALE_TYPE = "PASSWORD_TYPE";

    /**
     * 创建制定类型的密码
     *
     * @param passwordType 密码类型
     * @see PasswordType
     */
    public static void enterCreatePasswordActivity(Context context, int passwordType) {
        Intent intent = new Intent();
        switch (passwordType) {
            case PasswordType.PASSWORD_NUMBER:      // 数字密码
                intent.setClass(context, CreateNumberPasswordActivity.class);
                break;
            case PasswordType.PASSWORD_GESTURE:     // 手势密码
                intent.setClass(context, CreateGesturePasswordActivity.class);
                break;
            case PasswordType.PASSWORD_FINGERPRINT: // 指纹解锁登录
                intent.setClass(context, CreateFingerprintPasswordActivity.class);
                break;
            case PasswordType.PASSWORD_FACE:        // 人脸识别
            case PasswordType.PASSWORD_VOICE:       // 语音识别登录
                Toast.makeText(context, "暂时不支持设置该锁定方式!", Toast.LENGTH_SHORT).show();
                return;
            case PasswordType.PASSWORD_UNKNOW:      // 未知
            case PasswordType.PASSWORD_NONE:        // 未设置
            default:
                return;
        }
        context.startActivity(intent);

        if (context instanceof Activity){
            ((Activity) context).overridePendingTransition(0, android.R.anim.fade_in);
        }
    }

    /**
     * 保存密码
     *
     * @param password 密码对象
     * @throws PasswordHandleException 密码处理错误
     */
    public static void savePassword(Password password)
            throws PasswordHandleException {
        savePassword(password, true);
    }

    /**
     * 保存密码
     *
     * @param password   密码对象
     * @param isSecurity 是否采用加密算法
     * @throws PasswordHandleException 密码处理错误
     */
    public static void savePassword(Password password, boolean isSecurity)
            throws PasswordHandleException {
        Log.i(TAG, "保存密码：" + password.toString());
        try {
            SharedPreferences sp = MyApplication.getInstance().getSystemSharedPreferences();
            SharedPreferences.Editor edit = sp.edit();
            String pwd = password.getLocalePwd();
            if (isSecurity) {
                // 这里如果加密算法出错，就会保存失败
                pwd = SecurityUtil.encrypt(pwd);
            }
            edit.putString(PASSWORD_LOCALE_VALUE, pwd);
            edit.putInt(PASSWORD_LOCALE_TYPE, password.getPasswordType());
            edit.apply();
        } catch (Exception e) {
            throw new PasswordHandleException("occur exception when save locale password and pwdType...");
        }
    }

    public static Password readPassword() throws PasswordHandleException {
        try {
            // 从内存中读取Password
            SharedPreferences sp = MyApplication.getInstance().getSystemSharedPreferences();
            String pwd = sp.getString(PASSWORD_LOCALE_VALUE, "");
            int type = sp.getInt(PASSWORD_LOCALE_TYPE, PasswordType.PASSWORD_NONE);
            return new Password(pwd, type);
        } catch (Exception e) {
            throw new PasswordHandleException("occur exception when read locale password and pwdType...");
        }
    }

    public static void clearPassword() throws PasswordHandleException {
        try {
            SharedPreferences sp = MyApplication.getInstance().getSystemSharedPreferences();
            SharedPreferences.Editor edit = sp.edit();
            edit.remove(PASSWORD_LOCALE_VALUE);
            edit.remove(PASSWORD_LOCALE_TYPE);
            edit.apply();
        } catch (Exception e) {
            throw new PasswordHandleException("occur exception when reset locale password and pwdType...");
        }
    }

    public static boolean isNeedLockScreen() {
        try {
            Password password = readPassword();
            int passwordType = password.getPasswordType();
            switch (passwordType) {
                case PasswordType.PASSWORD_NUMBER:
                case PasswordType.PASSWORD_GESTURE:
                case PasswordType.PASSWORD_FINGERPRINT:
                    return true;
                case PasswordType.PASSWORD_FACE:
                case PasswordType.PASSWORD_VOICE:
                case PasswordType.PASSWORD_NONE:
                case PasswordType.PASSWORD_UNKNOW:
                default:
                    return false;
            }
        } catch (PasswordHandleException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getPasswordType() {
        Password password = null;
        try {
            password = readPassword();
        } catch (PasswordHandleException e) {
            e.printStackTrace();
        }
        int passwordType = PasswordType.PASSWORD_NONE;
        if (password != null) {
            passwordType = password.getPasswordType();
        }
        return passwordType;
    }
}
