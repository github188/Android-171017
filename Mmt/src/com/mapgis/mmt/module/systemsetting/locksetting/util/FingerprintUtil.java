package com.mapgis.mmt.module.systemsetting.locksetting.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

import com.mapgis.mmt.MyApplication;

/**
 * Created by Comclay on 2017/3/20.
 * 指纹识别的工具类
 */

public class FingerprintUtil {
    private static CancellationSignal cancellationSignal;

    private static boolean isCancel = false;

    /**
     * 指纹解锁是否可用
     */
    public static boolean isFingerprintUsable(FingerprintUsableCallback callback) {
        // 1，检测当前设备是否是6.0以上系统
        Context applicationContext = MyApplication.getInstance().getApplicationContext();
        FingerprintManagerCompat managerCompat = FingerprintManagerCompat.from(applicationContext);
        if (!managerCompat.isHardwareDetected()) { //判断设备是否支持
            Log.i("Fingerprint", "设备不支持指纹识别！");
            if (callback != null)
                callback.onNotSupport();
            return false;
        }
        KeyguardManager keyguardManager = (KeyguardManager) applicationContext.getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (!keyguardManager.isKeyguardSecure()) {//判断设备是否处于安全保护中
                Log.i("Fingerprint", "设备未处于安全保护中！");
                if (callback != null)
                    callback.onInsecurity();
                return false;
            }
        }
        if (!managerCompat.hasEnrolledFingerprints()) { //判断设备是否已经注册过指纹
            Log.i("Fingerprint", "设备未录入指纹！");
            if (callback != null)
                callback.onEnrollFailed(); //未注册
            return false;
        }

        return true;
    }

    /**
     * 验证指纹
     */
    public static void authenticateFingerprint(final AuthoenticateFingerprintCallback callback) {
        Context applicationContext = MyApplication.getInstance().getApplicationContext();
        FingerprintManagerCompat managerCompat = FingerprintManagerCompat.from(applicationContext);
        cancellationSignal = new CancellationSignal(); //必须重新实例化，否则cancel 过一次就不能再使用了
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                Log.i("Fingerprint", "onCancel: " + "指纹识别取消。。。");
                if (!isCancel)
                    authenticateFingerprint(callback);
            }
        });
        if (callback != null) {
            callback.onAuthenticationStart();
        }
        managerCompat.authenticate(null, 0, cancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                if (callback != null) {
                    callback.onAuthenticationError(errMsgId, errString);
                }
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                if (callback != null) {
                    callback.onAuthenticationHelp(helpMsgId, helpString);
                }
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (callback != null) {
                    callback.onAuthenticationSucceeded(result);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (callback != null) {
                    callback.onAuthenticationFailed();
                }
            }
        }, null);
    }

    public static void cancel() {
        if (cancellationSignal != null) {
            isCancel = true;
            cancellationSignal.cancel();
        }
    }

    public static boolean isNull(){
        return cancellationSignal == null;
    }

    public static boolean isCancel() {
        if (cancellationSignal == null || cancellationSignal.isCanceled()) {
            return true;
        }
        return false;
    }

    public interface FingerprintUsableCallback {
        // 硬件不支持
        void onNotSupport();

        // 未开启屏幕锁
        void onInsecurity();

        // 未注册指纹
        void onEnrollFailed();
    }

    public interface AuthoenticateFingerprintCallback {
        // 开始验证
        void onAuthenticationStart();

        // 验证错误
        void onAuthenticationError(int errMsgId, CharSequence errString);

        // 验证失败
        void onAuthenticationFailed();

        // 验证帮助
        void onAuthenticationHelp(int helpMsgId, CharSequence helpString);

        // 验证成功
        void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result);
    }
}