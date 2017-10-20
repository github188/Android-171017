package com.mapgis.mmt.module.systemsetting.locksetting.model;

import android.util.Log;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.SecurityUtil;
import com.mapgis.mmt.module.systemsetting.locksetting.entity.Password;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordHandleException;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordManager;

import java.security.NoSuchAlgorithmException;

/**
 * Created by Comclay on 2017/3/16.
 * 本地快速登录的密码操作模型
 */

public class BasePasswordModel implements IPasswordModel {
    private static final String TAG = "BasePasswordModel";
    private Password mPassword;

    @Override
    public boolean savePassword(Password password) {
        return savePassword(password, true);
    }

    @Override
    public boolean savePassword(Password password, boolean isSecurity) {
        Log.i(TAG, "保存密码：" + password.toString());
        try {
            PasswordManager.savePassword(password, isSecurity);
            return true;
        } catch (PasswordHandleException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Password getPassword() {
        if (this.mPassword == null) {
            // 从内存中读取Password
            try {
                this.mPassword = PasswordManager.readPassword();
            } catch (PasswordHandleException e) {
                e.printStackTrace();
            }
        }
        return this.mPassword;
    }

    @Override
    public boolean resetPassword() {
        try {
            PasswordManager.clearPassword();
            return true;
        } catch (PasswordHandleException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isAccordance(String ensurePassword) {
        try {
            Password password = getPassword();
            Log.i(TAG, "密码: "+password.toString());
            String encryptPassword = SecurityUtil.encrypt(ensurePassword);
            String localePwd = password.getLocalePwd();
            if (!BaseClassUtil.isNullOrEmptyString(localePwd) && localePwd.equals(encryptPassword)) {
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setPassword(Password password) {
        this.mPassword = password;
    }
}
