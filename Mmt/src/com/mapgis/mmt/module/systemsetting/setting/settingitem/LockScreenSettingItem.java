package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.systemsetting.SystemSettingActivity;
import com.mapgis.mmt.module.systemsetting.SystemSettingFragment;
import com.mapgis.mmt.module.systemsetting.locksetting.fingerprint.UnlockFingerprintPasswordActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.gesturepwd.UnlockGesturePasswordActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.numpwd.UnlockNumberPasswordActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.util.FingerprintUtil;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordHandleException;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordManager;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordType;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2017/6/14.
 * 屏幕锁定
 */

public class LockScreenSettingItem extends BaseMoreSettingItem {

    private int mPasswordType = 0;
    private List<String> mPasswordTypeDescs;

    public LockScreenSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void init() {
        setPasswordTypeText();
    }

    public void setPasswordTypeText() {
        try {
            mPasswordType = PasswordManager.readPassword().getPasswordType();
            String typeString = "无";
            switch (mPasswordType) {
                case PasswordType.PASSWORD_NONE:
                case PasswordType.PASSWORD_UNKNOW:
                case PasswordType.PASSWORD_VOICE:
                case PasswordType.PASSWORD_FACE:
                    break;
                case PasswordType.PASSWORD_NUMBER:
                    typeString = "密码";
                    break;
                case PasswordType.PASSWORD_GESTURE:
                    typeString = "手势";
                    break;
                case PasswordType.PASSWORD_FINGERPRINT:
                    typeString = "指纹";
                    break;
            }
            mItemView.setRightMessage(typeString);
            mItemView.setMessage(typeString);
        } catch (PasswordHandleException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void save() {

    }

    @Override
    public void performClicked() {
        enterUnlockActivity();
    }

    public void enterUnlockActivity() {
        Intent intent = new Intent();
        switch (mPasswordType) {
            case PasswordType.PASSWORD_NUMBER:      // 数字密码
                intent.setClass(mContext, UnlockNumberPasswordActivity.class);
                break;
            case PasswordType.PASSWORD_GESTURE:     // 手势密码
                intent.setClass(mContext, UnlockGesturePasswordActivity.class);
                break;
            case PasswordType.PASSWORD_FINGERPRINT: // 指纹解锁登录
                intent.setClass(mContext, UnlockFingerprintPasswordActivity.class);
                break;
            case PasswordType.PASSWORD_FACE:        // 人脸识别
            case PasswordType.PASSWORD_VOICE:       // 语音识别登录
            case PasswordType.PASSWORD_NONE:        // 未设置
            case PasswordType.PASSWORD_UNKNOW:      // 未知
            default:
                choiceUnlockedType();
                return;
        }
        intent.putExtra(SystemSettingActivity.class.getSimpleName(), "");
        SystemSettingFragment fragment = ((SystemSettingActivity) mContext).getSettingFragment();
        fragment.startActivityForResult(intent, Activity.RESULT_CANCELED);
        ((Activity) mContext).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * 弹出对话框选择解锁方式
     */
    public void choiceUnlockedType() {
        // 设置密码前需确认上次设置的密码
        getPasswordTypeDescs();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("选择屏幕锁定方式");
        String[] types = new String[mPasswordTypeDescs.size()];
        mPasswordTypeDescs.toArray(types);
        builder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mPasswordType != 0 && which == 0 && mContext instanceof BaseActivity) {
                    ((BaseActivity) mContext).stopLock();
                }
                mPasswordType = which;
                mItemView.setMessage(mPasswordTypeDescs.get(which));
                mItemView.setRightMessage(mPasswordTypeDescs.get(which));
                PasswordManager.enterCreatePasswordActivity(mContext, which);
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mPasswordType = 0;
                mItemView.setRightMessage(mPasswordTypeDescs.get(mPasswordType));
                mItemView.setMessage(mPasswordTypeDescs.get(mPasswordType));
                PasswordManager.enterCreatePasswordActivity(mContext, mPasswordType);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private List<String> getPasswordTypeDescs() {
        if (mPasswordTypeDescs == null || mPasswordTypeDescs.size() == 0) {
            String[] stringArray = mContext.getResources().getStringArray(R.array.str_password_type);
            // Arrays.asList(.)返回的是Arrays.ArrayList对象而非ArrayList对象，所以不能执行remove,add操作
            boolean fingerprintUsable = FingerprintUtil.isFingerprintUsable(null);
            mPasswordTypeDescs = new ArrayList<>();
            for (String type : stringArray) {
                if (!fingerprintUsable && "指纹".equals(type)) {
                    continue;
                }
                mPasswordTypeDescs.add(type);
            }
        }
        return this.mPasswordTypeDescs;
    }
}
