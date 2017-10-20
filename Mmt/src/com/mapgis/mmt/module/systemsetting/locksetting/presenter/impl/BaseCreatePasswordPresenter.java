package com.mapgis.mmt.module.systemsetting.locksetting.presenter.impl;

import com.mapgis.mmt.module.systemsetting.locksetting.entity.Password;
import com.mapgis.mmt.module.systemsetting.locksetting.model.BasePasswordModel;
import com.mapgis.mmt.module.systemsetting.locksetting.model.IPasswordModel;
import com.mapgis.mmt.module.systemsetting.locksetting.presenter.ICreatePasswordPresenter;
import com.mapgis.mmt.module.systemsetting.locksetting.view.impl.ICreatePasswordView;

/**
 * Created by Comclay on 2017/3/16.
 *
 */

public class BaseCreatePasswordPresenter implements ICreatePasswordPresenter {
    private ICreatePasswordView mCreatePasswordView;
    private IPasswordModel mPasswordModel;

    public BaseCreatePasswordPresenter(ICreatePasswordView mCreatePasswordView) {
        this.mCreatePasswordView = mCreatePasswordView;
        mPasswordModel = new BasePasswordModel();
    }

    @Override
    public boolean saveLocalePassword(){
        Password password = new Password(mCreatePasswordView.getPwd()
                ,mCreatePasswordView.getPasswordType());
        return mPasswordModel.savePassword(password);
    }

    @Override
    public void resetPassword() {
        this.mPasswordModel.resetPassword();
    }
}
