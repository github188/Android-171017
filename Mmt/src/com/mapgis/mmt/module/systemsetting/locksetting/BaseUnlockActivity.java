package com.mapgis.mmt.module.systemsetting.locksetting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.module.systemsetting.SystemSettingActivity;
import com.mapgis.mmt.module.systemsetting.locksetting.presenter.IUnlockPasswordPresenter;
import com.mapgis.mmt.module.systemsetting.locksetting.presenter.impl.BaseUnlockPasswordPresenter;
import com.mapgis.mmt.module.systemsetting.locksetting.util.LoginManager;
import com.mapgis.mmt.module.systemsetting.locksetting.view.impl.IUnlockPasswordView;
import com.mapgis.mmt.module.welcome.Welcome;

/**
 * Created by Comclay on 2017/3/17.
 * 解锁界面的基本实现类
 */

public abstract class BaseUnlockActivity extends Activity implements IUnlockPasswordView {

    // 密码解锁的最多次数
    private static final int MAX_CONFIRM_COUNT = 3;
    // 剩余解锁次数
    protected int mCountChance = MAX_CONFIRM_COUNT;

    protected IUnlockPasswordPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        onCreatePresenter();
    }

    /**
     * 创建presenter
     */
    protected void onCreatePresenter() {
        if (mPresenter == null) {
            mPresenter = new BaseUnlockPasswordPresenter(this);
        }
    }

    @Override
    public void unlockPassword() {
        boolean isSuccess = mPresenter.unlockPassword();
        if (isSuccess) {
            passwordCorrect();
        } else {
            passwordError();
        }
    }

    @Override
    public void passwordCorrect() {
        // 进入登录界面
        if (getIntent().hasExtra("Login")) {
            LoginManager.getInstance(this).enterLoginActivityFromUnlock();
        }else if (getIntent().hasExtra(BaseActivity.class.getSimpleName())){
            this.finish();
        }else{
            Intent intent = new Intent();
            intent.putExtra("clearScreenPassword", true);
            setResult(Activity.RESULT_CANCELED, intent);

            finish();
        }
    }

    @Override
    public void passwordError() {
        this.mCountChance--;
        if (mCountChance > 0) {
            clearInputPassword();
            hasChanceEnsure();
        } else {
            noChanceEnsure();
        }
    }

    @Override
    public void hasChanceEnsure() {
        Toast.makeText(this, "密码错误，请重新输入", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void noChanceEnsure() {
        Toast.makeText(this, "请用账户登录！", Toast.LENGTH_LONG).show();
        forgetLocalePassword();
    }

    @Override
    public void forgetLocalePassword() {
        Intent intent = new Intent(this, ActivityClassRegistry.getInstance().getActivityClass("登录界面"));
        intent.putExtra("forgetpassword", "");
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * 需要对所有的解锁界面的返回键做统一的处理，不解锁成功不允许做下一步的操作
     */
    @Override
    public void onBackPressed() {
        Log.i("BaseUnlockActivity", "onBackPressed: " + "按下返回按钮！");
        if (onBack()) {
            return;
        }

        // 在设置界面重置锁屏密码时可以取消锁定
        if (getIntent().hasExtra(SystemSettingActivity.class.getSimpleName())) {
            Intent intent = new Intent();
            setResult(-10, intent);
            super.onBackPressed();
        } else if (getIntent().hasExtra(LoginManager.PARAM_BUNDLE)) {
            if (getIntent().getBundleExtra(LoginManager.PARAM_BUNDLE).containsKey(Welcome.class.getSimpleName())) {
                super.onBackPressed();
            }
        }
    }

    protected boolean onBack() {
        return false;
    }
}

