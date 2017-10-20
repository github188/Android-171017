package com.mapgis.mmt.module.systemsetting.locksetting.fingerprint;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.systemsetting.locksetting.BaseCreateActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.presenter.ICreatePasswordPresenter;
import com.mapgis.mmt.module.systemsetting.locksetting.presenter.impl.BaseCreatePasswordPresenter;
import com.mapgis.mmt.module.systemsetting.locksetting.util.FingerprintUtil;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordType;
import com.mapgis.mmt.module.systemsetting.locksetting.view.impl.ICreatePasswordView;

/**
 * Created by Comclay on 2017/3/20.
 * 创建指纹解锁的界面
 */

public class CreateFingerprintPasswordActivity extends BaseCreateActivity
        implements ICreatePasswordView,FingerprintUtil.FingerprintUsableCallback{

    private ICreatePasswordPresenter mPresenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_fingerprint);

        boolean fingerprintUsable = FingerprintUtil.isFingerprintUsable(this);
        if (!fingerprintUsable){
            return;
        }

        if (mPresenter == null){
            mPresenter = new BaseCreatePasswordPresenter(this);
        }

        // 不满足上面的任意条件都无法使用指纹解锁app功能

        // 最后保存指纹识别即可
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("指纹解锁");
        builder.setIcon(R.drawable.ic_fingerprint);
        builder.setMessage("使用指纹登录和解锁应用！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                confirmCreate();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                cancelCreate();
            }
        });
        builder.show();
    }

    public void showAlertDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setIcon(android.R.drawable.stat_notify_error);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                cancelCreate();
            }
        });
    }

    @Override
    public String getPwd() {
        return "";
    }

    @Override
    public String getEnsurePwd() {
        return null;
    }

    @Override
    public int getPasswordType() {
        return PasswordType.PASSWORD_FINGERPRINT;
    }

    @Override
    public void cancelCreate() {
        onBackPressed();
    }

    @Override
    public void confirmCreate() {
        mPresenter.saveLocalePassword();
    }

    @Override
    public void onNotSupport() {
        showAlertDialog("当前设备不支持指纹识别功能！");
    }

    @Override
    public void onInsecurity() {
        showAlertDialog("当前设备没有设置屏幕保护！");
    }

    @Override
    public void onEnrollFailed() {
        showAlertDialog("使用该功能之前，请确保已经在系统中录入指纹！");
    }
}
