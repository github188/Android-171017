package com.mapgis.mmt.module.systemsetting.locksetting.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mapgis.mmt.common.util.ScreenShot;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.module.login.ServerConfigInfo;
import com.mapgis.mmt.module.systemsetting.locksetting.fingerprint.UnlockFingerprintPasswordActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.gesturepwd.UnlockGesturePasswordActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.numpwd.UnlockNumberPasswordActivity;

/**
 * Created by Comclay on 2017/3/17.
 * 本地登录管理类
 */

public class LoginManager {
//    // 默认登录，即账号密码登录方式
//    public final static int TYPE_DEFAULT_LOGIN = 0;
//    // 本地登录
//    public final static int TYPE_LOCALE_LOGIN = 1;

    public final static String PARAM_BUNDLE = "param_bundle";
    public final static String PARAM_SERVER_CONFIG_INFO = "serverConfigInfo";
    public static final String LOCALE_PASSWORD = "LOCALE_PASSWORD";

    private Context context;

    private LoginManager(Context context) {
        this.context = context;
    }

    public static LoginManager getInstance(Context context) {
        return new LoginManager(context);
    }

    /**
     * 进入登录界面或者解锁界面
     */
    public void enterLoginActivity(Bundle bundle) {
        int passwordType = PasswordManager.getPasswordType();
        switch (passwordType) {
            case PasswordType.PASSWORD_NONE:    // 未设置
            case PasswordType.PASSWORD_UNKNOW:  // 未知
                enterDefaultLoginActivity(bundle);
            default:
                enterLocalUnlockActivity(passwordType, true, bundle);
                break;
        }
    }

    public void enterDefaultLoginActivity(Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, ActivityClassRegistry.getInstance().getActivityClass("登录界面"));
        if (bundle != null) {
            intent.putExtras(bundle);
            intent.putExtra("bitmap", "sdcard/xx.png");
            intent.putExtra("tip", "正在加载菜单,请稍候...");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        if (context instanceof Activity) {
            context.startActivity(intent);
            ((Activity) context).finish();
            ((Activity) this.context).overridePendingTransition(0, 0);
        }
    }

    public void enterLocalUnlockActivity(Bundle bundle) {
        enterLocalUnlockActivity(PasswordManager.getPasswordType(), true, bundle);
    }

    public void enterLocalUnlockActivity(int passwordType, boolean isFinish, Bundle bundle, String flag) {
        Intent intent = new Intent();
        switch (passwordType) {
            case PasswordType.PASSWORD_NUMBER:      // 数字密码
                intent.setClass(context, UnlockNumberPasswordActivity.class);
                break;
            case PasswordType.PASSWORD_GESTURE:     // 手势密码
                intent.setClass(context, UnlockGesturePasswordActivity.class);
                break;
            case PasswordType.PASSWORD_FINGERPRINT: // 指纹解锁登录
                intent.setClass(context, UnlockFingerprintPasswordActivity.class);
                break;
            case PasswordType.PASSWORD_FACE:        // 人脸识别
            case PasswordType.PASSWORD_VOICE:       // 语音识别登录
            case PasswordType.PASSWORD_NONE:        // 未设置
            case PasswordType.PASSWORD_UNKNOW:      // 未知
                return;
        }
        if (bundle != null) {
            intent.putExtra(PARAM_BUNDLE, bundle);
        }
        if (context instanceof Activity) {
            intent.putExtra(flag, "");
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(0, 0);
            if (isFinish) {
                ((Activity) context).finish();
            }
        }
    }

    public void enterLocalUnlockActivity(int passwordType, boolean isFinish, Bundle bundle) {
        Intent intent = new Intent();
        switch (passwordType) {
            case PasswordType.PASSWORD_NUMBER:      // 数字密码
                intent.setClass(context, UnlockNumberPasswordActivity.class);
                break;
            case PasswordType.PASSWORD_GESTURE:     // 手势密码
                intent.setClass(context, UnlockGesturePasswordActivity.class);
                break;
            case PasswordType.PASSWORD_FINGERPRINT: // 指纹解锁登录
                intent.setClass(context, UnlockFingerprintPasswordActivity.class);
                break;
            case PasswordType.PASSWORD_FACE:        // 人脸识别
            case PasswordType.PASSWORD_VOICE:       // 语音识别登录
            case PasswordType.PASSWORD_NONE:        // 未设置
            case PasswordType.PASSWORD_UNKNOW:      // 未知
                return;
        }
        if (bundle != null) {
            intent.putExtra(PARAM_BUNDLE, bundle);
        }
        if (context instanceof Activity) {
            intent.putExtra("Login", "");
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(0, 0);
            if (isFinish) {
                ((Activity) context).finish();
            }
        }
    }

    public static void defaultLogin() {

    }

    /**
     * 本地登录界面验证成功就使用上次的登陆用户名和密码登录app
     */
    public void enterLoginActivityFromUnlock() {
        if (context instanceof Activity) {
            // 截取当前屏幕
            new ScreenShot(false).shoot((Activity) context);
            ServerConfigInfo info = ServerConnectConfig.getInstance().getServerConfigInfo();

            Intent intent = new Intent(context, ActivityClassRegistry.getInstance().getActivityClass("登录界面"));

            intent.putExtra("serverConfigInfo", info);
            intent.putExtra("bitmap", "sdcard/xx.png");
            intent.putExtra("tip", "正在加载菜单,请稍候...");
            intent.putExtra("unlockpwd", "");

            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(intent);
            ((Activity) context).finish();
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

}
