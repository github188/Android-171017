package com.mapgis.mmt.module.systemsetting.locksetting.fingerprint;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.systemsetting.SystemSettingActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.BaseUnlockActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.util.FingerprintUtil;
import com.mapgis.mmt.module.systemsetting.locksetting.util.LoginManager;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordHandleException;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordManager;
import com.mapgis.mmt.module.welcome.Welcome;

/**
 * Created by Comclay on 2017/3/20.
 * 指纹解锁登录界面
 */

public class UnlockFingerprintPasswordActivity extends BaseUnlockActivity {
    private static final String TAG = "Fingerprint";

    private AlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_fingerprint);

        showVerifyFingerprintDialog();

        if (!FingerprintUtil.isFingerprintUsable(new FingerprintUtil.FingerprintUsableCallback() {
            @Override
            public void onNotSupport() {

            }

            @Override
            public void onInsecurity() {
                try {
                    PasswordManager.clearPassword();
                } catch (PasswordHandleException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEnrollFailed() {
                try {
                    PasswordManager.clearPassword();
                } catch (PasswordHandleException e) {
                    e.printStackTrace();
                }
            }
        })){
            forgetLocalePassword();
            return;
        }

        authenticate();
    }

    private void authenticate() {
        FingerprintUtil.authenticateFingerprint(new FingerprintUtil.AuthoenticateFingerprintCallback() {
            @Override
            public void onAuthenticationStart() {

            }

            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                Log.i(TAG, "onAuthenticationError: " + errMsgId + "     " + errString);
                passwordError();
//                if(mDialog != null){
//                    mDialog.setMessage(errString);
//                }
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                Log.i(TAG, "onAuthenticationHelp: " + helpMsgId + "     " + helpString);
                if(mDialog != null){
                    mDialog.setMessage(helpString);
                }
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                Log.i(TAG, "onAuthenticationSucceeded: "+result.getCryptoObject());
                passwordCorrect();
            }

            @Override
            public void onAuthenticationFailed() {
                Log.i(TAG, "onAuthenticationFailed: ");
                passwordError();
            }
        });
    }

    @Override
    protected void onDestroy() {
        FingerprintUtil.cancel();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    /**
     * 验证指纹
     */
    private void showVerifyFingerprintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_fingerprint);
        builder.setIcon(R.drawable.ic_fingerprint);
        builder.setMessage(R.string.text_authenticate_fingerprint);
        builder.setPositiveButton(R.string.text_normal_login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                forgetLocalePassword();
            }
        });
        builder.setCancelable(false);
        mDialog = builder.create();

        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK){
                    // 在设置界面重置锁屏密码时可以取消锁定
                    if (getIntent().hasExtra(SystemSettingActivity.class.getSimpleName())) {
                        Intent intent = new Intent();
                        setResult(-10, intent);
                        finish();
                        return true;
                    } else if (getIntent().hasExtra(LoginManager.PARAM_BUNDLE)) {
                        if (getIntent().getBundleExtra(LoginManager.PARAM_BUNDLE).containsKey(Welcome.class.getSimpleName())) {
                            finish();
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        mDialog.show();
    }

    /**
     * 由于手机在息屏后，再点亮屏幕系统会调用指纹解锁，
     * 则当前的指纹解锁验证请求会取消
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!FingerprintUtil.isNull()){
            FingerprintUtil.cancel();
            if (mDialog != null){
                mDialog.setMessage(getString(R.string.text_authenticate_fingerprint));
            }
            authenticate();
        }
    }

    @Override
    public void passwordError() {

    }

    @Override
    public String getEnsurePassword() {
        return null;
    }

    @Override
    public void clearInputPassword() {

    }
}
