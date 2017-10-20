package com.mapgis.mmt.module.systemsetting.locksetting.presenter.impl;

import com.mapgis.mmt.module.systemsetting.locksetting.model.BasePasswordModel;
import com.mapgis.mmt.module.systemsetting.locksetting.model.IPasswordModel;
import com.mapgis.mmt.module.systemsetting.locksetting.presenter.IUnlockPasswordPresenter;
import com.mapgis.mmt.module.systemsetting.locksetting.view.impl.IUnlockPasswordView;

/**
 * Created by Comclay on 2017/3/17.
 *
 */

public class BaseUnlockPasswordPresenter implements IUnlockPasswordPresenter {
    private IUnlockPasswordView mUnlockPasswordView;
    private IPasswordModel mPasswordModel;

    public BaseUnlockPasswordPresenter(IUnlockPasswordView unlockPasswordView) {
        this.mUnlockPasswordView = unlockPasswordView;
        mPasswordModel = new BasePasswordModel();
    }

    @Override
    public boolean unlockPassword() {
        String ensurePassword = this.mUnlockPasswordView.getEnsurePassword();
        return mPasswordModel.isAccordance(ensurePassword);
    }
}
